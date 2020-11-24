package com.example.desafio_jera;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties({
  "id", "genre_ids", "backdrop_path", "original_language",
  "video", "popularity", "vote_count", "adult", "title"
})
public class Filme {
  private double vote_average;
  private String
    original_title, poster_path, release_date, overview;

  public double getVote_average() {
    return vote_average;
  }

  public void setVote_average(double vote_average) {
    this.vote_average = vote_average;
  }

  public String getOriginal_title() {
    return original_title;
  }

  public void setOriginal_title(String original_title) {
    this.original_title = original_title;
  }

  public String getPoster_path() {
    return poster_path;
  }

  public void setPoster_path(String poster_path) {
    this.poster_path = poster_path;
  }

  public String getRelease_date() {
    return release_date;
  }

  public void setRelease_date(String release_date) {
    this.release_date = release_date;
  }

  public String getOverview() {
    return overview;
  }

  public void setOverview(String overview) {
    this.overview = overview;
  }
}
