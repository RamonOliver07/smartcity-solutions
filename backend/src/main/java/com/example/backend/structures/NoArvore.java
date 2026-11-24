package com.example.backend.structures;

import com.example.backend.model.Localidade;

public class NoArvore {
    public Localidade dado;
    public NoArvore esquerda;
    public NoArvore direita;

    public NoArvore(Localidade dado) {
        this.dado = dado;
        this.esquerda = null;
        this.direita = null;
    }
}