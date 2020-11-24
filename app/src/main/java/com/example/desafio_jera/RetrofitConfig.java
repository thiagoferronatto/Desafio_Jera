package com.example.desafio_jera;

import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

public class RetrofitConfig {
  private final Retrofit retrofit;

  public RetrofitConfig() {
    this.retrofit = new Retrofit.Builder()
      .baseUrl("https://api.themoviedb.org/3/search/")
      .addConverterFactory(JacksonConverterFactory.create())
      .build();
  }

  public FilmeService getFilmeService() {
    return this.retrofit.create(FilmeService.class);
  }
}