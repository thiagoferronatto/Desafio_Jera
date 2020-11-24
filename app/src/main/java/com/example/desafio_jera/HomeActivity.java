package com.example.desafio_jera;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

import java.util.Arrays;
import java.util.List;

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
        if (nomeDoFilmeET.getEditableText().toString().isEmpty())
          // TODO: Reiniciar a lista com os itens da biblioteca do usuário
          listaDeFilmes.setAdapter(null);
      }
    });

    listaDeFilmes = findViewById(R.id.lista_principal);
    // TODO: Iniciar a lista com os itens da biblioteca do usuário

    mAuth = FirebaseAuth.getInstance();

    btnSair.setOnClickListener(v -> {
      mAuth.signOut();
      Intent sair = new Intent(HomeActivity.this, AuthActivity.class);
      sair.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
      startActivity(sair);
      finish();
    });

    btnPesquisar.setOnClickListener(v -> chamarAPI());

    // TODO: Implementar clique em itens da lista para adicioná-los à biblioteca
  }

  void chamarAPI() {
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
}