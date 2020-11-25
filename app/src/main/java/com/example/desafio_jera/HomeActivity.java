package com.example.desafio_jera;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity {

  // Eu não tenho uma maneira melhor de guardar a key. Isso teoricamente não importa, já que,
  // se o app não fosse open-source, ninguém poderia encontrar essa linha sem usar um
  // reverse engineering brabo. Eu poderia colocar ela em um arquivo de resource e
  // adicionar ele ao .gitignore, PORÉM, o Git integrado ao Android Studio parece
  // ignorar qualquer ordem e simplesmente dá o push em tudo (até no .gitignore).
  String apiKey = "caf2cc00ea28ef7039348f15b01f2fcc";

  FirebaseAuth mAuth;
  FirebaseFirestore db;

  EditText nomeDoFilmeET;

  Button btnSair, btnPesquisar;

  ListView listaDeFilmes;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_home);

    // As imagens dos filmes não são mostradas e o app crasha se eu não fizer isso aqui, por alguma razão.
    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
    StrictMode.setThreadPolicy(policy);

    btnSair = findViewById(R.id.btn_sair);
    btnPesquisar = findViewById(R.id.btn_pesquisar);

    nomeDoFilmeET = findViewById(R.id.input_nome_filme);

    // Isso vai mostrar novamente a watchlist do perfil assim que a caixa de pesquisa esvaziar
    nomeDoFilmeET.addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        // sem uso
      }

      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {
        // sem uso
      }

      @Override
      public void afterTextChanged(Editable s) {
        if (nomeDoFilmeET.getEditableText().toString().isEmpty()) {
          AlertDialog loading = carregando();
          inicializarLista(loading);
          listaDeFilmes.setAdapter(null);
        }
      }
    });

    listaDeFilmes = findViewById(R.id.lista_principal);

    mAuth = FirebaseAuth.getInstance();
    db = FirebaseFirestore.getInstance();

    AlertDialog loading = carregando();
    inicializarLista(loading);

    btnSair.setOnClickListener(v -> {
      mAuth.signOut();
      Intent sair = new Intent(HomeActivity.this, AuthActivity.class);
      sair.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
      startActivity(sair);
      finish();
    });

    btnPesquisar.setOnClickListener(v -> chamarAPI());
  }

  void chamarAPI() {
    listaDeFilmes.setOnItemClickListener((parent, view, position, id) -> {
      Filme clicado = (Filme) listaDeFilmes.getAdapter().getItem(position);
      new AlertDialog.Builder(HomeActivity.this)
        .setTitle(String.format("Adicionar %s à sua lista?", clicado.getOriginal_title()))
        .setMessage("Esse filme será inserido em sua lista de desejos.")
        .setPositiveButton("Sim", (d, w) -> adicionarFilme(clicado))
        .setNegativeButton("Não", null)
        .setCancelable(false)
        .show();
    });

    if (nomeDoFilmeET.getEditableText().toString().isEmpty()) {
      new AlertDialog.Builder(this)
        .setTitle("Digite algo!")
        .setMessage("O campo de pesquisa não pode estar vazio.")
        .setCancelable(false)
        .setPositiveButton("OK", null)
        .show();
      return;
    }

    AlertDialog loading = carregando();

    Call<ListaDeFilmes> call = new RetrofitConfig()
      .getFilmeService()
      .buscarFilme(nomeDoFilmeET.getEditableText().toString(), apiKey);

    call.enqueue(new Callback<ListaDeFilmes>() {
      @Override
      public void onResponse(Call<ListaDeFilmes> call, Response<ListaDeFilmes> response) {
        loading.dismiss();
        ListaDeFilmes lista = response.body();
        if (lista != null && lista.getResults().length > 0)
          atualizarLista(lista.getResults());
      }

      @Override
      public void onFailure(Call<ListaDeFilmes> call, Throwable t) {
        loading.dismiss();
        new AlertDialog.Builder(HomeActivity.this)
          .setTitle("Erro!")
          .setMessage("Tivemos um problema desconhecido, tente novamente mais tarde.")
          .show();
      }
    });
  }

  void atualizarLista(Filme[] lista) {
    List<Filme> filmes = Arrays.asList(lista);
    FilmeAdapter adapter = new FilmeAdapter(this, filmes);
    listaDeFilmes.setAdapter(adapter);
  }

  AlertDialog carregando() {
    return new AlertDialog.Builder(this)
      .setTitle("Aguarde")
      .setMessage("Carregando...")
      .setCancelable(false)
      .show();
  }

  @Override
  protected void onResume() {
    super.onResume();
    if (!nomeDoFilmeET.getEditableText().toString().isEmpty())
      chamarAPI();
  }

  void adicionarFilme(Filme filme) {
    AlertDialog loading = carregando();

    String email = mAuth.getCurrentUser().getEmail(),
      username = email.substring(0, email.indexOf("@"));

    db.collection("usuarios")
      .document(username)
      .collection("perfis")
      .document("default")
      .get()
      .addOnSuccessListener(docSnap -> {
        if (docSnap.exists())
          realizarInsercao(username, filme, loading);
        else
          inicializarPerfil(username, filme, loading);
      });
  }

  void realizarInsercao(String username, @NotNull Filme filme, AlertDialog loading) {

    db.collection("usuarios")
      .document(username)
      .collection("perfis")
      .document("default")
      .update("filmes", FieldValue.arrayUnion(filme))
      .addOnSuccessListener(docRef -> {
        loading.dismiss();
        new AlertDialog.Builder(HomeActivity.this)
          .setTitle("Pronto!")
          .setMessage(String.format("%s está na sua lista!", filme.getOriginal_title()))
          .setPositiveButton("OK", null)
          .show();
      })
      .addOnFailureListener(e -> {
        loading.dismiss();
        new AlertDialog.Builder(HomeActivity.this)
          .setTitle("Erro desconhecido!")
          .setMessage("Tente novamente mais tarde.")
          .setCancelable(false)
          .setPositiveButton("OK", null)
          .show();
      });
  }

  void inicializarPerfil(String username, Filme filme, AlertDialog loading) {
    db.collection("usuarios")
      .document(username)
      .collection("perfis")
      .document("default")
      .set(new HashMap<String, Object>())
      .addOnSuccessListener(docRef -> realizarInsercao(username, filme, loading))
      .addOnFailureListener(e -> new AlertDialog.Builder(HomeActivity.this)
        .setTitle("Erro desconhecido!")
        .setMessage("Tente novamente mais tarde.")
        .setCancelable(false)
        .setPositiveButton("OK", null)
        .show());
  }

  void inicializarLista(AlertDialog loading) {
    listaDeFilmes.setOnItemClickListener(null);

    String email = mAuth.getCurrentUser().getEmail(),
      username = email.substring(0, email.indexOf("@"));
    db.collection("usuarios")
      .document(username)
      .collection("perfis")
      .document("default")
      .get()
      .addOnSuccessListener(docSnap -> {
        loading.dismiss();
        if (docSnap.exists()) {
          List<HashMap<String, Object>> filmes =
            (ArrayList<HashMap<String, Object>>) docSnap.get("filmes");

          List<Filme> filmesAL = new ArrayList<>();

          for (HashMap<String, Object> i : filmes) {
            Filme temp = new Filme();

            temp.setOriginal_title(i.get("original_title").toString());
            temp.setOverview(i.get("overview").toString());
            temp.setPoster_path(i.get("poster_path").toString());
            temp.setRelease_date(i.get("release_date").toString());
            temp.setVote_average(Double.parseDouble(i.get("vote_average").toString()));

            filmesAL.add(temp);
          }

          FilmeAdapter adapter = new FilmeAdapter(HomeActivity.this, filmesAL);
          listaDeFilmes.setAdapter(adapter);
        }
      })
      .addOnFailureListener(e -> new AlertDialog.Builder(HomeActivity.this)
        .setTitle("Erro desconhecido!")
        .setMessage("Tente novamente mais tarde.")
        .setCancelable(false)
        .setPositiveButton("OK", null)
        .show());
  }
}