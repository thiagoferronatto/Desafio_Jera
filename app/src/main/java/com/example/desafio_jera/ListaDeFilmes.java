package com.example.desafio_jera;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties({"page", "total_pages"})
public class ListaDeFilmes {
  private int total_results;
  private Filme[] results;

  public Filme[] getResults() {
    return results;
  }

  public void setResults(Filme[] results) {
    this.results = results;
  }

  public int getTotal_results() {
    return total_results;
  }

  public void setTotal_results(int total_results) {
    this.total_results = total_results;
  }
}
