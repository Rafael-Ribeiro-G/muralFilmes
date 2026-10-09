
package com.aula.muralfilmes.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aula.muralfilmes.R;
import com.aula.muralfilmes.model.Filme;

import java.util.ArrayList;
import java.util.List;

public class FilmeAdapter extends RecyclerView.Adapter<FilmeAdapter.FilmeViewHolder> {

    public interface OnFilmeClickListener {
        void onVotar(Filme filme);
        void onExcluir(Filme filme);
    }

    private final List<Filme> filmes = new ArrayList<>();
    private final OnFilmeClickListener listener;

    public FilmeAdapter(OnFilmeClickListener listener) {
        this.listener = listener;
    }

    public void atualizarLista(List<Filme> novaLista) {
        filmes.clear();
        filmes.addAll(novaLista);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FilmeViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_filme, parent, false);

        return new FilmeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull FilmeViewHolder holder, int position) {

        Filme filme = filmes.get(position);

        holder.txtTitulo.setText(filme.getTitulo());
        holder.txtGenero.setText(filme.getGenero());
        holder.txtIndicadoPor.setText(
                "Indicado por " + filme.getIndicadoPor()
        );
        holder.txtVotos.setText(String.valueOf(filme.getVotos()));

        holder.btnVotar.setOnClickListener(v -> {
            if (listener != null) {
                listener.onVotar(filme);
            }
        });

        holder.btnExcluir.setOnClickListener(v -> {
            if (listener != null) {
                listener.onExcluir(filme);
            }
        });
    }

    @Override
    public int getItemCount() {
        return filmes.size();
    }

    static class FilmeViewHolder extends RecyclerView.ViewHolder {

        TextView txtTitulo;
        TextView txtGenero;
        TextView txtIndicadoPor;
        TextView txtVotos;
        ImageButton btnVotar;
        ImageButton btnExcluir;

        FilmeViewHolder(@NonNull View itemView) {
            super(itemView);

            txtTitulo = itemView.findViewById(R.id.txtTitulo);
            txtGenero = itemView.findViewById(R.id.txtGenero);
            txtIndicadoPor = itemView.findViewById(R.id.txtIndicadoPor);
            txtVotos = itemView.findViewById(R.id.txtVotos);
            btnVotar = itemView.findViewById(R.id.btnVotar);
            btnExcluir = itemView.findViewById(R.id.btnExcluir);
        }
    }
}
