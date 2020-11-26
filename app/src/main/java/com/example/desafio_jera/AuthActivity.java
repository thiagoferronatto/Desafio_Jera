package com.example.desafio_jera;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AuthActivity extends AppCompatActivity {

  FirebaseAuth mAuth;
  FirebaseFirestore db;

  Intent irParaHome;

  Button cadastro, login;
  EditText emailET, senhaET, nomeET, nascET;
  TextView lblEmail, lblSenha, lblNome, lblNasc;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    irParaHome = new Intent(this, HomeActivity.class);

    mAuth = FirebaseAuth.getInstance();
    db = FirebaseFirestore.getInstance();
  }

  @Override
  protected void onStart() {
    super.onStart();

    FirebaseUser usuarioAtual = mAuth.getCurrentUser();

    router(usuarioAtual);

    // Isso só é executado se o usuário não estiver logado

    cadastro.setOnClickListener(v -> preCadastro());

    login.setOnClickListener(v -> preLogin());
  }

  void router(FirebaseUser usuario) {
    if (usuario != null) {
      startActivity(irParaHome);
    } else {
      setContentView(R.layout.activity_auth);

      cadastro = findViewById(R.id.btn_cadastro);
      login = findViewById(R.id.btn_login);

      emailET = findViewById(R.id.input_email);
      senhaET = findViewById(R.id.input_senha);
      nomeET = findViewById(R.id.input_nome);
      nascET = findViewById(R.id.input_nasc);

      lblEmail = findViewById(R.id.label_email);
      lblSenha = findViewById(R.id.label_senha);
      lblNome = findViewById(R.id.label_nome);
      lblNasc = findViewById(R.id.label_nasc);
    }
  }

  void camposVazios() {
    new AlertDialog.Builder(this)
      .setTitle("Campo(s) vazio(s)!")
      .setMessage("Todos os campos devem estar preenchidos.")
      .setPositiveButton("OK", null)
      .setCancelable(false)
      .show();
  }

  void preCadastro() {
    String
      email = emailET.getEditableText().toString(),
      senha = senhaET.getEditableText().toString();
    if (email.isEmpty() || senha.isEmpty())
      camposVazios();
    else {
      login.setVisibility(View.GONE);

      lblNome.setVisibility(View.VISIBLE);
      lblNasc.setVisibility(View.VISIBLE);
      nomeET.setVisibility(View.VISIBLE);
      nascET.setVisibility(View.VISIBLE);

      cadastro.setText(R.string.finalizar);
      cadastro.setOnClickListener(v2 -> cadastrar(email, senha));
    }
  }

  void cadastrar(String email, String senha) {
    AlertDialog loading = carregando();

    String
      nome = nomeET.getEditableText().toString(),
      nasc = nascET.getEditableText().toString();

    if (nome.isEmpty() || nasc.isEmpty()) {
      loading.dismiss();
      camposVazios();
      return;
    }

    mAuth.createUserWithEmailAndPassword(email, senha)
      .addOnSuccessListener(authResult -> cadastrarDados(nome, nasc, email))
      .addOnFailureListener(e -> {
        loading.dismiss();
        if (e instanceof FirebaseAuthInvalidCredentialsException) {
          new AlertDialog.Builder(this)
            .setCancelable(false)
            .setTitle("Erro!")
            .setMessage("Endereço de e-mail inválido ou senha muito curta!")
            .setPositiveButton("OK", null)
            .show();
          cadastro.setOnClickListener(v -> preCadastro());
        } else if (e instanceof FirebaseAuthUserCollisionException) {
          new AlertDialog.Builder(this)
            .setCancelable(false)
            .setTitle("Erro!")
            .setMessage("Já existe uma conta com o e-mail informado.")
            .setPositiveButton("OK", (dialog, which) -> resetarLayout())
            .show();
        }
      });
  }

  void resetarLayout() {
    lblNome.setVisibility(View.GONE);
    nomeET.setVisibility(View.GONE);
    nomeET.setText("");
    lblNasc.setVisibility(View.GONE);
    nascET.setVisibility(View.GONE);
    nascET.setText("");

    emailET.setText("");
    senhaET.setText("");

    login.setVisibility(View.VISIBLE);

    cadastro.setText(R.string.cadastre_se);
    cadastro.setOnClickListener(v -> preCadastro());
  }

  AlertDialog carregando() {
    return new AlertDialog.Builder(this)
      .setTitle("Aguarde")
      .setMessage("Carregando...")
      .setCancelable(false)
      .show();
  }

  void preLogin() {
    String
      email = emailET.getEditableText().toString(),
      senha = senhaET.getEditableText().toString();
    if (email.isEmpty() || senha.isEmpty())
      camposVazios();
    else {
      AlertDialog loading = carregando();
      mAuth.signInWithEmailAndPassword(email, senha)
        .addOnSuccessListener(authResult -> startActivity(irParaHome))
        .addOnFailureListener(e -> {
          loading.dismiss();
          if (e instanceof FirebaseAuthInvalidUserException) {
            new AlertDialog.Builder(this)
              .setTitle("Erro!")
              .setMessage("Conta não encontrada.")
              .setCancelable(false)
              .setPositiveButton("OK", null)
              .show();
          } else if (e instanceof FirebaseAuthInvalidCredentialsException) {
            new AlertDialog.Builder(this)
              .setTitle("Erro!")
              .setMessage("Credenciais incorretas.")
              .setCancelable(false)
              .setPositiveButton("OK", null)
              .show();
          }
        });
    }
  }

  void cadastrarDados(String nome, String nasc, String email) {
    Map<String, Object> usuario = new HashMap<>();

    usuario.put("nome", nome);
    usuario.put("nasc", nasc);

    String username = email.substring(0, email.indexOf('@'));

    db.collection("usuarios").document(username).set(usuario)
      .addOnSuccessListener(docRef -> startActivity(irParaHome))
      .addOnFailureListener(e -> {
        new AlertDialog.Builder(this)
          .setTitle("Erro desconhecido!")
          .setMessage("Tente novamente mais tarde.")
          .setCancelable(false)
          .setPositiveButton("OK", (a, b) -> resetarLayout())
          .show();
        Log.e("ERRO", e.getMessage());
      });
  }

  @Override
  public void onBackPressed() {
    if (login.getVisibility() != View.VISIBLE)
      resetarLayout();
    else
      super.onBackPressed();
  }
}