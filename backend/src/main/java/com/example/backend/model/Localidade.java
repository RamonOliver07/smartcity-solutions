package com.example.backend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "localidades")
public class Localidade {

    @Id
    private int id; // ID fornecido/gerenciado conforme a ABB
    private String nome;
    private int populacao;
    private String regiao;
    private String tipo; // Cidade, bairro ou distrito

    public Localidade() {}

    public Localidade(int id, String nome, int populacao, String regiao, String tipo) {
        this.id = id;
        this.nome = nome;
        this.populacao = populacao;
        this.regiao = regiao;
        this.tipo = tipo;
    }

    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public int getPopulacao() { return populacao; }
    public void setPopulacao(int populacao) { this.populacao = populacao; }

    public String getRegiao() { return regiao; }
    public void setRegiao(String regiao) { this.regiao = regiao; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    @Override
    public String toString() {
        return String.format("[%d] %s (%s - %s) | Pop: %d", id, nome, tipo, regiao, populacao);
    }
}