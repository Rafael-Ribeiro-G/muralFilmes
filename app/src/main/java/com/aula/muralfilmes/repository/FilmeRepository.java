package com.aula.muralfilmes.repository;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class FilmeRepository {

    private final FirebaseFirestore db;
    private final CollectionReference filmesRef;

    public FilmeRepository() {
        db = FirebaseFirestore.getInstance();
        filmesRef = db.collection("filmes");
    }

    public CollectionReference getFilmesRef() {
        return filmesRef;
    }
}
