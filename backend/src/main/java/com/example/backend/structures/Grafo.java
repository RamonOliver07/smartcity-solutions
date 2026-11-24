package com.example.backend.structures;

import java.util.*;

public class Grafo {

    // Lista de adjacência: ID do vértice -> Lista de arestas de saída
    private final Map<Integer, List<Aresta>> adjacencias = new HashMap<>();

    // =========================================================
    // CLASSES AUXILIARES DE RETORNO (Para Terminal e API REST)
    // =========================================================
    public static class ResultadoBFS {
        public List<Integer> ordemVisita = new ArrayList<>();
        public Map<Integer, Integer> niveis = new LinkedHashMap<>();
        public List<Integer> naoAlcancados = new ArrayList<>();
        public List<Integer> menorCaminhoArestas = new ArrayList<>();
    }

    public static class ResultadoDFS {
        public List<Integer> ordemVisita = new ArrayList<>();
        public boolean possuiCiclo;
    }

    public static class ResultadoCaminho {
        public List<Integer> caminho = new ArrayList<>();
        public double custoTotal;
        public int quantidadeArestas;
        public boolean alcancavel;

        public ResultadoCaminho(List<Integer> caminho, double custoTotal, boolean alcancavel) {
            this.caminho = caminho;
            this.custoTotal = custoTotal;
            this.alcancavel = alcancavel;
            this.quantidadeArestas = caminho.isEmpty() ? 0 : caminho.size() - 1;
        }
    }

    // =========================================================
    // 1. OPERAÇÕES BÁSICAS DE VÉRTICES E ARESTAS
    // =========================================================
    public boolean adicionarVertice(int id) {
        if (adjacencias.containsKey(id)) return false;
        adjacencias.put(id, new ArrayList<>());
        return true;
    }

    public boolean removerVertice(int id) {
        if (!adjacencias.containsKey(id)) return false;
        adjacencias.remove(id);
        // Remove todas as arestas incidentes que apontavam para este vértice
        for (List<Aresta> lista : adjacencias.values()) {
            lista.removeIf(a -> a.destinoId == id);
        }
        return true;
    }

    public boolean adicionarAresta(int origem, int destino, double distanciaKm, double tempoMinutos, boolean bidirecional) {
        if (!adjacencias.containsKey(origem) || !adjacencias.containsKey(destino) || origem == destino) {
            return false;
        }
        // Impede aresta duplicada na origem -> destino
        if (saoAdjacentes(origem, destino)) return false;
        // Se for mão dupla, garante também que não exista destino -> origem
        if (bidirecional && saoAdjacentes(destino, origem)) return false;

        inserirArestaOrdenada(origem, new Aresta(destino, distanciaKm, tempoMinutos));
        if (bidirecional) {
            inserirArestaOrdenada(destino, new Aresta(origem, distanciaKm, tempoMinutos));
        }
        return true;
    }

    // Mantém os vizinhos ordenados por ID para garantir BFS determinística
    private void inserirArestaOrdenada(int origem, Aresta nova) {
        List<Aresta> lista = adjacencias.get(origem);
        lista.add(nova);
        lista.sort(Comparator.comparingInt(a -> a.destinoId));
    }

    public boolean removerAresta(int origem, int destino, boolean removerVoltaSeExistir) {
        if (!adjacencias.containsKey(origem) || !adjacencias.containsKey(destino)) return false;
        boolean removeu = adjacencias.get(origem).removeIf(a -> a.destinoId == destino);
        if (removerVoltaSeExistir) {
            adjacencias.get(destino).removeIf(a -> a.destinoId == origem);
        }
        return removeu;
    }

    public boolean alterarPesosAresta(int origem, int destino, double novaDist, double novoTempo, boolean bidirecional) {
        boolean alterouIda = atualizarArestaUnica(origem, destino, novaDist, novoTempo);
        if (bidirecional) {
            atualizarArestaUnica(destino, origem, novaDist, novoTempo);
        }
        return alterouIda;
    }

    private boolean atualizarArestaUnica(int origem, int destino, double novaDist, double novoTempo) {
        List<Aresta> lista = adjacencias.get(origem);
        if (lista == null) return false;
        for (Aresta a : lista) {
            if (a.destinoId == destino) {
                a.distanciaKm = novaDist;
                a.tempoMinutos = novoTempo;
                return true;
            }
        }
        return false;
    }

    public boolean saoAdjacentes(int origem, int destino) {
        List<Aresta> lista = adjacencias.get(origem);
        if (lista == null) return false;
        return lista.stream().anyMatch(a -> a.destinoId == destino);
    }

    public Set<Integer> getVertices() {
        return new TreeSet<>(adjacencias.keySet());
    }

    public List<Aresta> getVizinhos(int id) {
        return adjacencias.getOrDefault(id, Collections.emptyList());
    }

    public Map<Integer, List<Aresta>> getListaAdjacencia() {
        return adjacencias;
    }

    public int grauSaida(int id) {
        return adjacencias.containsKey(id) ? adjacencias.get(id).size() : -1;
    }

    public int grauEntrada(int id) {
        if (!adjacencias.containsKey(id)) return -1;
        int grau = 0;
        for (List<Aresta> lista : adjacencias.values()) {
            for (Aresta a : lista) {
                if (a.destinoId == id) grau++;
            }
        }
        return grau;
    }

    public List<Integer> getVerticesIsolados() {
        List<Integer> isolados = new ArrayList<>();
        for (int v : getVertices()) {
            if (grauEntrada(v) == 0 && grauSaida(v) == 0) {
                isolados.add(v);
            }
        }
        return isolados;
    }

    public int contarArestasDirecionadas() {
        int total = 0;
        for (List<Aresta> lista : adjacencias.values()) {
            total += lista.size();
        }
        return total;
    }

    // =========================================================
    // 2. BUSCA EM LARGURA (BFS DETERMINÍSTICA COM FILA)
    // =========================================================
    public ResultadoBFS executarBFS(int inicio, Integer destinoOpcional) {
        ResultadoBFS res = new ResultadoBFS();
        if (!adjacencias.containsKey(inicio)) return res;

        Queue<Integer> fila = new LinkedList<>();
        Map<Integer, Integer> pai = new HashMap<>();
        Set<Integer> visitados = new HashSet<>();

        fila.add(inicio);
        visitados.add(inicio);
        res.niveis.put(inicio, 0);
        pai.put(inicio, null);

        while (!fila.isEmpty()) {
            int atual = fila.poll();
            res.ordemVisita.add(atual);
            int nivelAtual = res.niveis.get(atual);

            // Os vizinhos já estão armazenados em ordem crescente de ID
            for (Aresta aresta : adjacencias.get(atual)) {
                int vizinho = aresta.destinoId;
                if (!visitados.contains(vizinho)) {
                    visitados.add(vizinho);
                    res.niveis.put(vizinho, nivelAtual + 1);
                    pai.put(vizinho, atual);
                    fila.add(vizinho);
                }
            }
        }

        // Vértices não alcançados
        for (int v : getVertices()) {
            if (!visitados.contains(v)) {
                res.naoAlcancados.add(v);
            }
        }

        // Reconstrói o menor caminho em número de arestas se um destino foi informado
        if (destinoOpcional != null && visitados.contains(destinoOpcional)) {
            LinkedList<Integer> caminho = new LinkedList<>();
            Integer curr = destinoOpcional;
            while (curr != null) {
                caminho.addFirst(curr);
                curr = pai.get(curr);
            }
            res.menorCaminhoArestas = caminho;
        }

        return res;
    }

    // =========================================================
    // 3. BUSCA EM PROFUNDIDADE (DFS) E DETECÇÃO DE CICLOS
    // =========================================================
    public ResultadoDFS executarDFS(int inicio) {
        ResultadoDFS res = new ResultadoDFS();
        if (!adjacencias.containsKey(inicio)) return res;

        Set<Integer> visitados = new HashSet<>();
        dfsRec(inicio, visitados, res.ordemVisita);
        res.possuiCiclo = temCicloDirecionado();
        return res;
    }

    private void dfsRec(int atual, Set<Integer> visitados, List<Integer> ordem) {
        visitados.add(atual);
        ordem.add(atual);
        for (Aresta a : adjacencias.get(atual)) {
            if (!visitados.contains(a.destinoId)) {
                dfsRec(a.destinoId, visitados, ordem);
            }
        }
    }

    // Detecção de ciclo em grafo direcionado (0 = não visitado, 1 = na pilha de recursão, 2 = finalizado)
    public boolean temCicloDirecionado() {
        Map<Integer, Integer> estado = new HashMap<>();
        for (int v : adjacencias.keySet()) estado.put(v, 0);

        for (int v : adjacencias.keySet()) {
            if (estado.get(v) == 0 && detectarCicloRec(v, estado)) {
                return true;
            }
        }
        return false;
    }

    private boolean detectarCicloRec(int u, Map<Integer, Integer> estado) {
        estado.put(u, 1); // Visitando (em processamento)
        for (Aresta a : adjacencias.get(u)) {
            int v = a.destinoId;
            if (estado.get(v) == 1) return true; // Aresta de retorno -> Ciclo encontrado!
            if (estado.get(v) == 0 && detectarCicloRec(v, estado)) return true;
        }
        estado.put(u, 2); // Concluído
        return false;
    }

    // =========================================================
    // 4. ALGORITMO DE DIJKSTRA (POR DISTÂNCIA OU POR TEMPO)
    // =========================================================
    public ResultadoCaminho dijkstra(int origem, int destino, boolean porTempo) {
        if (!adjacencias.containsKey(origem) || !adjacencias.containsKey(destino)) {
            return new ResultadoCaminho(Collections.emptyList(), 0, false);
        }

        Map<Integer, Double> dist = new HashMap<>();
        Map<Integer, Integer> anterior = new HashMap<>();
        PriorityQueue<double[]> pq = new PriorityQueue<>(Comparator.comparingDouble(a -> a[1]));

        for (int v : adjacencias.keySet()) {
            dist.put(v, Double.POSITIVE_INFINITY);
        }
        dist.put(origem, 0.0);
        pq.add(new double[]{origem, 0.0});

        while (!pq.isEmpty()) {
            double[] atualArr = pq.poll();
            int u = (int) atualArr[0];
            double dAtual = atualArr[1];

            if (dAtual > dist.get(u)) continue;
            if (u == destino) break;

            for (Aresta a : adjacencias.get(u)) {
                double peso = porTempo ? a.tempoMinutos : a.distanciaKm;
                double novaDist = dist.get(u) + peso;

                if (novaDist < dist.get(a.destinoId)) {
                    dist.put(a.destinoId, novaDist);
                    anterior.put(a.destinoId, u);
                    pq.add(new double[]{a.destinoId, novaDist});
                }
            }
        }

        if (dist.get(destino) == Double.POSITIVE_INFINITY) {
            return new ResultadoCaminho(Collections.emptyList(), 0, false);
        }

        LinkedList<Integer> caminho = new LinkedList<>();
        Integer curr = destino;
        while (curr != null) {
            caminho.addFirst(curr);
            curr = anterior.get(curr);
        }

        return new ResultadoCaminho(caminho, dist.get(destino), true);
    }

    // =========================================================
    // 5. ANÁLISE DE CONECTIVIDADE (PROJEÇÃO NÃO DIRECIONADA)
    // =========================================================
    public List<List<Integer>> getComponentesConexasNaoDirecionadas() {
        // Constrói projeção não direcionada explícita conforme permitido no enunciado
        Map<Integer, Set<Integer>> naoDirecionado = new HashMap<>();
        for (int v : getVertices()) naoDirecionado.put(v, new TreeSet<>());

        for (Map.Entry<Integer, List<Aresta>> entry : adjacencias.entrySet()) {
            int u = entry.getKey();
            for (Aresta a : entry.getValue()) {
                naoDirecionado.get(u).add(a.destinoId);
                naoDirecionado.get(a.destinoId).add(u);
            }
        }

        List<List<Integer>> componentes = new ArrayList<>();
        Set<Integer> visitados = new HashSet<>();

        for (int v : getVertices()) {
            if (!visitados.contains(v)) {
                List<Integer> comp = new ArrayList<>();
                Queue<Integer> fila = new LinkedList<>();
                fila.add(v);
                visitados.add(v);

                while (!fila.isEmpty()) {
                    int atual = fila.poll();
                    comp.add(atual);
                    for (int viz : naoDirecionado.get(atual)) {
                        if (!visitados.contains(viz)) {
                            visitados.add(viz);
                            fila.add(viz);
                        }
                    }
                }
                Collections.sort(comp);
                componentes.add(comp);
            }
        }
        return componentes;
    }
}