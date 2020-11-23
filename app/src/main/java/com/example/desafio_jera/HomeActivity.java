package com.example.desafio_jera;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class HomeActivity extends AppCompatActivity {

  FirebaseAuth mAuth;

  Button btnSair;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_home);

    btnSair = findViewById(R.id.btn_sair);

    mAuth = FirebaseAuth.getInstance();

    btnSair.setOnClickListener(v -> {
      mAuth.signOut();
      Intent sair = new Intent(HomeActivity.this, AuthActivity.class);
      sair.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
      startActivity(sair);
      finish();
    });
  }
}