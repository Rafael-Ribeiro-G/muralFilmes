package com.aula.muralfilmes;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aula.muralfilmes.adapter.FilmeAdapter;
import com.aula.muralfilmes.model.Filme;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements FilmeAdapter.OnFilmeClickListener {

    private String meuNome;

    private FirebaseFirestore db;
    private FilmeAdapter adapter;
    private RecyclerView listaFilmes;
    private TextView txtVazio;
    private ListenerRegistration listenerFilmes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main), (v, insets) -> {
                    Insets systemBars = insets.getInsets(
                            WindowInsetsCompat.Type.systemBars());
                    v.setPadding(
                            systemBars.left,
                            systemBars.top,
                            systemBars.right,
                            systemBars.bottom);
                    return insets;
                });

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        db = FirebaseFirestore.getInstance();

        listaFilmes = findViewById(R.id.listaFilmes);
        txtVazio = findViewById(R.id.txtVazio);

        listaFilmes.setLayoutManager(new LinearLayoutManager(this));
        adapter = new FilmeAdapter(this);
        listaFilmes.setAdapter(adapter);

        carregarNome();

        FloatingActionButton fab = findViewById(R.id.fabAdicionar);
        fab.setOnClickListener(v -> mostrarDialogoNovoFilme());

        observarFilmes();
    }

    private void carregarNome() {
        SharedPreferences localNome =
                getSharedPreferences("nomeUsuario", MODE_PRIVATE);

        meuNome = localNome.getString("nomeUsuario", null);

        if (meuNome != null && !meuNome.trim().isEmpty()) {
            Toast.makeText(
                    this,
                    "Bem-vindo(a) " + meuNome,
                    Toast.LENGTH_LONG
            ).show();
            return;
        }

        EditText campo = new EditText(this);
        campo.setHint("Seu nome");

        new AlertDialog.Builder(this)
                .setTitle("Quem é você")
                .setMessage(
                        "Vai aparecer ao lado dos filmes que você indicar.")
                .setView(campo)
                .setCancelable(false)
                .setPositiveButton("Pronto", (dialogInterface, i) -> {
                    meuNome = campo.getText().toString().trim();

                    if (meuNome.isEmpty()) {
                        meuNome = "Anônimo";
                    }

                    localNome.edit()
                            .putString("nomeUsuario", meuNome)
                            .apply();
                })
                .show();
    }

    private void mostrarDialogoNovoFilme() {
        View form = LayoutInflater.from(this)
                .inflate(R.layout.tela_filme, null);

        EditText campoTitulo = form.findViewById(R.id.campoTitulo);
        EditText campoGenero = form.findViewById(R.id.campoGenero);

        new AlertDialog.Builder(this)
                .setTitle("Indicar filme")
                .setView(form)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Adicionar", (d, w) -> {
                    String titulo =
                            campoTitulo.getText().toString().trim();
                    String genero =
                            campoGenero.getText().toString().trim();

                    if (TextUtils.isEmpty(titulo)
                            || TextUtils.isEmpty(genero)) {
                        Toast.makeText(
                                this,
                                "Preencha título e gênero",
                                Toast.LENGTH_SHORT
                        ).show();
                        return;
                    }

                    if (meuNome == null || meuNome.trim().isEmpty()) {
                        Toast.makeText(
                                this,
                                "Informe seu nome antes de indicar um filme",
                                Toast.LENGTH_SHORT
                        ).show();
                        return;
                    }

                    Filme filme = new Filme(titulo, genero, meuNome);

                    db.collection("filmes")
                            .add(filme)
                            .addOnFailureListener(e ->
                                    Toast.makeText(
                                            this,
                                            "Erro ao adicionar filme",
                                            Toast.LENGTH_SHORT
                                    ).show());
                })
                .show();
    }

    private void observarFilmes() {
        listenerFilmes = db.collection("filmes")
                .orderBy("votos", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshot, erro) -> {
                    if (erro != null) {
                        Toast.makeText(
                                this,
                                "Erro ao carregar filmes",
                                Toast.LENGTH_SHORT
                        ).show();
                        return;
                    }

                    if (snapshot == null) {
                        return;
                    }

                    List<Filme> filmes = snapshot.toObjects(Filme.class);
                    adapter.atualizarLista(filmes);

                    boolean listaVazia = filmes.isEmpty();
                    txtVazio.setVisibility(
                            listaVazia ? View.VISIBLE : View.GONE);
                    listaFilmes.setVisibility(
                            listaVazia ? View.GONE : View.VISIBLE);
                });
    }

    @Override
    public void onVotar(Filme filme) {
        if (filme.getId() == null) {
            return;
        }

        db.collection("filmes")
                .document(filme.getId())
                .update("votos", FieldValue.increment(1))
                .addOnFailureListener(e ->
                        Toast.makeText(
                                this,
                                "Erro ao registrar voto",
                                Toast.LENGTH_SHORT
                        ).show());
    }

    @Override
    public void onExcluir(Filme filme) {
        if (filme.getIndicadoPor() == null
                || !filme.getIndicadoPor().equals(meuNome)) {
            Toast.makeText(
                    this,
                    "Esse filme é de " + filme.getIndicadoPor(),
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Excluir")
                .setMessage(
                        "Tirar \"" + filme.getTitulo()
                                + "\" do mural?")
                .setNegativeButton("Não", null)
                .setPositiveButton("Excluir", (dialog, which) -> {
                    if (filme.getId() == null) {
                        return;
                    }

                    db.collection("filmes")
                            .document(filme.getId())
                            .delete()
                            .addOnSuccessListener(unused ->
                                    Snackbar.make(
                                            findViewById(R.id.main),
                                            "Filme removido",
                                            Snackbar.LENGTH_SHORT
                                    ).show())
                            .addOnFailureListener(e ->
                                    Toast.makeText(
                                            this,
                                            "Erro ao remover filme",
                                            Toast.LENGTH_SHORT
                                    ).show());
                })
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (listenerFilmes != null) {
            listenerFilmes.remove();
        }
    }

}