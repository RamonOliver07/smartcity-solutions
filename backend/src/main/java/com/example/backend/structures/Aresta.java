package com.example.backend.structures;

public class Aresta {
    public int destinoId;
    public double distanciaKm;
    public double tempoMinutos;

    public Aresta(int destinoId, double distanciaKm, double tempoMinutos) {
        this.destinoId = destinoId;
        this.distanciaKm = distanciaKm;
        this.tempoMinutos = tempoMinutos;
    }

    @Override
    public String toString() {
        return String.format("-> [%d] (%.1f km | %.1f min)", destinoId, distanciaKm, tempoMinutos);
    }
}