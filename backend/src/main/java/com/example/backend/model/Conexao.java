package com.example.backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "conexoes")
public class Conexao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int origemId;
    private int destinoId;
    private double distanciaKm;
    private double tempoMinutos;
    private boolean bidirecional; // true = mão dupla, false = mão única

    public Conexao() {}

    public Conexao(int origemId, int destinoId, double distanciaKm, double tempoMinutos, boolean bidirecional) {
        this.origemId = origemId;
        this.destinoId = destinoId;
        this.distanciaKm = distanciaKm;
        this.tempoMinutos = tempoMinutos;
        this.bidirecional = bidirecional;
    }

    // Getters e Setters
    public Long getId() { return id; }
    public int getOrigemId() { return origemId; }
    public void setOrigemId(int origemId) { this.origemId = origemId; }

    public int getDestinoId() { return destinoId; }
    public void setDestinoId(int destinoId) { this.destinoId = destinoId; }

    public double getDistanciaKm() { return distanciaKm; }
    public void setDistanciaKm(double distanciaKm) { this.distanciaKm = distanciaKm; }

    public double getTempoMinutos() { return tempoMinutos; }
    public void setTempoMinutos(double tempoMinutos) { this.tempoMinutos = tempoMinutos; }

    public boolean isBidirecional() { return bidirecional; }
    public void setBidirecional(boolean bidirecional) { this.bidirecional = bidirecional; }
}