package com.example.desafio_jera;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface FilmeService {
  @GET("movie")
  Call<ListaDeFilmes> buscarFilme(@Query("query") String nomeDoFilme, @Query("api_key") String apiKey);
}
