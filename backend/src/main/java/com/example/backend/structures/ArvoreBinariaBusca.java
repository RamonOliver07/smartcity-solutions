package com.example.backend.structures;

import com.example.backend.model.Localidade;
import java.util.*;

public class ArvoreBinariaBusca {
    private NoArvore raiz;

    public boolean estaVazia() {
        return raiz == null;
    }

    public NoArvore getRaiz() {
        return raiz;
    }

    // 1. INSERIR
    public boolean inserir(Localidade loc) {
        if (pesquisar(loc.getId()) != null) return false; // Impede ID duplicado
        raiz = inserirRec(raiz, loc);
        return true;
    }

    private NoArvore inserirRec(NoArvore atual, Localidade loc) {
        if (atual == null) return new NoArvore(loc);
        if (loc.getId() < atual.dado.getId()) {
            atual.esquerda = inserirRec(atual.esquerda, loc);
        } else if (loc.getId() > atual.dado.getId()) {
            atual.direita = inserirRec(atual.direita, loc);
        }
        return atual;
    }

    // 2. PESQUISAR POR ID
    public Localidade pesquisar(int id) {
        NoArvore atual = raiz;
        while (atual != null) {
            if (id == atual.dado.getId()) return atual.dado;
            atual = (id < atual.dado.getId()) ? atual.esquerda : atual.direita;
        }
        return null;
    }

    // 3. ATUALIZAR PRESERVANDO ID
    public boolean atualizar(int id, String novoNome, int novaPop, String novaRegiao, String novoTipo) {
        Localidade loc = pesquisar(id);
        if (loc == null) return false;
        loc.setNome(novoNome);
        loc.setPopulacao(novaPop);
        loc.setRegiao(novaRegiao);
        loc.setTipo(novoTipo);
        return true;
    }

    // 4. REMOVER (3 CASOS: folha, 1 filho, 2 filhos)
    public boolean remover(int id) {
        if (pesquisar(id) == null) return false;
        raiz = removerRec(raiz, id);
        return true;
    }

    private NoArvore removerRec(NoArvore atual, int id) {
        if (atual == null) return null;

        if (id < atual.dado.getId()) {
            atual.esquerda = removerRec(atual.esquerda, id);
        } else if (id > atual.dado.getId()) {
            atual.direita = removerRec(atual.direita, id);
        } else {
            // Caso 1: Nó folha
            if (atual.esquerda == null && atual.direita == null) return null;
            // Caso 2: Apenas um filho
            if (atual.esquerda == null) return atual.direita;
            if (atual.direita == null) return atual.esquerda;
            // Caso 3: Dois filhos (substitui pelo menor da subárvore direita)
            NoArvore sucessor = encontrarMenorNo(atual.direita);
            atual.dado = sucessor.dado;
            atual.direita = removerRec(atual.direita, sucessor.dado.getId());
        }
        return atual;
    }

    private NoArvore encontrarMenorNo(NoArvore no) {
        while (no.esquerda != null) no = no.esquerda;
        return no;
    }

    // 5. PERCURSOS
    public List<Localidade> emOrdem() {
        List<Localidade> lista = new ArrayList<>();
        emOrdemRec(raiz, lista);
        return lista;
    }

    private void emOrdemRec(NoArvore no, List<Localidade> lista) {
        if (no != null) {
            emOrdemRec(no.esquerda, lista);
            lista.add(no.dado);
            emOrdemRec(no.direita, lista);
        }
    }

    public List<Localidade> preOrdem() {
        List<Localidade> lista = new ArrayList<>();
        preOrdemRec(raiz, lista);
        return lista;
    }

    private void preOrdemRec(NoArvore no, List<Localidade> lista) {
        if (no != null) {
            lista.add(no.dado);
            preOrdemRec(no.esquerda, lista);
            preOrdemRec(no.direita, lista);
        }
    }

    public List<Localidade> posOrdem() {
        List<Localidade> lista = new ArrayList<>();
        posOrdemRec(raiz, lista);
        return lista;
    }

    private void posOrdemRec(NoArvore no, List<Localidade> lista) {
        if (no != null) {
            posOrdemRec(no.esquerda, lista);
            posOrdemRec(no.direita, lista);
            lista.add(no.dado);
        }
    }

    public List<Localidade> porNiveis() {
        List<Localidade> lista = new ArrayList<>();
        if (raiz == null) return lista;
        Queue<NoArvore> fila = new LinkedList<>();
        fila.add(raiz);
        while (!fila.isEmpty()) {
            NoArvore atual = fila.poll();
            lista.add(atual.dado);
            if (atual.esquerda != null) fila.add(atual.esquerda);
            if (atual.direita != null) fila.add(atual.direita);
        }
        return lista;
    }

    // 6. ALTURA E BALANCEAMENTO
    public int altura() {
        return alturaRec(raiz);
    }

    private int alturaRec(NoArvore no) {
        if (no == null) return -1;
        return 1 + Math.max(alturaRec(no.esquerda), alturaRec(no.direita));
    }

    public boolean estaBalanceada() {
        return verificarBalanceamento(raiz);
    }

    private boolean verificarBalanceamento(NoArvore no) {
        if (no == null) return true;
        int diff = Math.abs(alturaRec(no.esquerda) - alturaRec(no.direita));
        return diff <= 1 && verificarBalanceamento(no.esquerda) && verificarBalanceamento(no.direita);
    }

    // Reconstrução balanceada a partir do percurso em-ordem
    public void balancear() {
        List<Localidade> ordenados = emOrdem();
        raiz = construirBalanceada(ordenados, 0, ordenados.size() - 1);
    }

    private NoArvore construirBalanceada(List<Localidade> lista, int inicio, int fim) {
        if (inicio > fim) return null;
        int meio = (inicio + fim) / 2;
        NoArvore no = new NoArvore(lista.get(meio));
        no.esquerda = construirBalanceada(lista, inicio, meio - 1);
        no.direita = construirBalanceada(lista, meio + 1, fim);
        return no;
    }

    // 7. REPRESENTAÇÃO EM VETOR POR NÍVEIS (ÍNDICE INICIANDO EM 1)
    public Localidade[] toVetorIndice1() {
        int h = altura();
        if (h < 0) return new Localidade[1];
        int tamanho = (int) Math.pow(2, h + 1); // índices de 1 até 2^(h+1) - 1
        Localidade[] vetor = new Localidade[tamanho];
        preencherVetor(raiz, vetor, 1);
        return vetor;
    }

    private void preencherVetor(NoArvore no, Localidade[] vetor, int indice) {
        if (no == null || indice >= vetor.length) return;
        vetor[indice] = no.dado;
        preencherVetor(no.esquerda, vetor, 2 * indice);
        preencherVetor(no.direita, vetor, 2 * indice + 1);
    }

    // =========================================================
    // MÉTODOS COMPLEMENTARES PARA RELATÓRIOS E VISUALIZAÇÃO
    // =========================================================
    public Localidade consultarMenorId() {
        if (raiz == null) return null;
        return encontrarMenorNo(raiz).dado;
    }

    public Localidade consultarMaiorId() {
        if (raiz == null) return null;
        NoArvore atual = raiz;
        while (atual.direita != null) atual = atual.direita;
        return atual.dado;
    }

    public int contarNos() {
        return contarNosRec(raiz);
    }

    private int contarNosRec(NoArvore no) {
        if (no == null) return 0;
        return 1 + contarNosRec(no.esquerda) + contarNosRec(no.direita);
    }

    public List<Localidade> listarFolhas() {
        List<Localidade> folhas = new ArrayList<>();
        listarFolhasRec(raiz, folhas);
        return folhas;
    }

    private void listarFolhasRec(NoArvore no, List<Localidade> lista) {
        if (no == null) return;
        if (no.esquerda == null && no.direita == null) {
            lista.add(no.dado);
        }
        listarFolhasRec(no.esquerda, lista);
        listarFolhasRec(no.direita, lista);
    }

    public List<Localidade> listarNosInternos() {
        List<Localidade> internos = new ArrayList<>();
        listarInternosRec(raiz, internos);
        return internos;
    }

    private void listarInternosRec(NoArvore no, List<Localidade> lista) {
        if (no == null) return;
        if (no.esquerda != null || no.direita != null) {
            lista.add(no.dado);
        }
        listarInternosRec(no.esquerda, lista);
        listarInternosRec(no.direita, lista);
    }

    public int nivelDoNo(int id) {
        NoArvore atual = raiz;
        int nivel = 0;
        while (atual != null) {
            if (id == atual.dado.getId()) return nivel;
            atual = (id < atual.dado.getId()) ? atual.esquerda : atual.direita;
            nivel++;
        }
        return -1; // Não encontrado
    }

    public int contarComparacoesBusca(int id) {
        NoArvore atual = raiz;
        int comparacoes = 0;
        while (atual != null) {
            comparacoes++;
            if (id == atual.dado.getId()) return comparacoes;
            atual = (id < atual.dado.getId()) ? atual.esquerda : atual.direita;
        }
        return comparacoes;
    }

    // Representação textual hierárquica da ABB
    public String gerarRepresentacaoTextual() {
        if (raiz == null) return "(Árvore vazia)";
        StringBuilder sb = new StringBuilder();
        gerarTextoRec(raiz, "", true, sb);
        return sb.toString();
    }

    private void gerarTextoRec(NoArvore no, String prefixo, boolean ehUltimo, StringBuilder sb) {
        if (no == null) return;
        sb.append(prefixo).append(ehUltimo ? "└── " : "├── ")
                .append("[").append(no.dado.getId()).append("] ").append(no.dado.getNome()).append("\n");
        String novoPrefixo = prefixo + (ehUltimo ? "    " : "│   ");
        if (no.esquerda != null || no.direita != null) {
            if (no.esquerda != null) gerarTextoRec(no.esquerda, novoPrefixo, no.direita == null, sb);
            if (no.direita != null) gerarTextoRec(no.direita, novoPrefixo, true, sb);
        }
    }
}