package com.example.desafio_jera;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Locale;

public class FilmeAdapter extends BaseAdapter {
  private final Context context;
  private final List<Filme> filmes;

  public FilmeAdapter(Context context, List<Filme> filmes) {
    this.context = context;
    this.filmes = filmes;
  }

  @Override
  public int getCount() {
    return this.filmes.size();
  }

  @Override
  public Filme getItem(int position) {
    return this.filmes.get(position);
  }

  @Override
  public long getItemId(int position) {
    return position;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    if (convertView == null)
      convertView = LayoutInflater.from(context)
        .inflate(R.layout.item_list_view, parent, false);

    Filme atual = getItem(position);

    TextView titulo = convertView.findViewById(R.id.item_titulo);
    TextView overview = convertView.findViewById(R.id.item_overview);
    TextView data = convertView.findViewById(R.id.item_data);
    TextView reviews = convertView.findViewById(R.id.item_reviews);
    ImageView poster = convertView.findViewById(R.id.img_poster);
    CardView cardView = convertView.findViewById(R.id.card_view);

    int nightModeFlags =
      context.getResources().getConfiguration().uiMode &
        Configuration.UI_MODE_NIGHT_MASK;
    switch (nightModeFlags) {
      case Configuration.UI_MODE_NIGHT_YES:
        titulo.setTextColor(context.getResources().getColor(R.color.white));
        cardView.setCardBackgroundColor(context.getResources().getColor(R.color.dark));
        break;
      case Configuration.UI_MODE_NIGHT_NO:
        titulo.setTextColor(context.getResources().getColor(R.color.black));
        cardView.setCardBackgroundColor(context.getResources().getColor(R.color.light));
    }

    titulo.setText(atual.getOriginal_title());
    overview.setText(atual.getOverview());
    if (atual.getRelease_date() != null && atual.getRelease_date().contains("-")) {
      String[] d;
      d = atual.getRelease_date().split("-");
      data.setText(String.format("Lançamento: %s/%s/%s", d[2], d[1], d[0]));
    } else if (atual.getRelease_date() != null)
      data.setText(String.format("Lançamento: %s", atual.getRelease_date()));
    else
      data.setText(R.string.sem_info_lancamento);
    reviews.setText(
      String.format(
        new Locale("pt", "BR"),
        "Nota: %.2f",
        atual.getVote_average())
    );

    String url = "https://image.tmdb.org/t/p/w185" + atual.getPoster_path();
    poster.setImageDrawable(carregarImagemDeURL(url));

    return convertView;
  }

  Drawable carregarImagemDeURL(String url) {
    try {
      InputStream is = (InputStream) new URL(url).getContent();
      return Drawable.createFromStream(is, null);
    } catch (Exception e) {
      return null;
    }
  }
}
