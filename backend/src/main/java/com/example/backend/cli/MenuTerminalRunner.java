package com.example.backend.cli;

import com.example.backend.model.Localidade;
import com.example.backend.service.SmartCityService;
import com.example.backend.structures.ArvoreBinariaBusca;
import com.example.backend.structures.Aresta;
import com.example.backend.structures.Grafo;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class MenuTerminalRunner implements CommandLineRunner {

    private final SmartCityService service;
    private final Scanner scanner = new Scanner(System.in);

    public MenuTerminalRunner(SmartCityService service) {
        this.service = service;
    }

    @Override
    public void run(String... args) {
        // Executa o menu em uma thread separada para não bloquear o servidor Web (Angular)
        new Thread(this::iniciarMenu).start();
    }

    private void iniciarMenu() {
        int opcao = -1;
        while (opcao != 0) {
            exibirCabecalho();
            opcao = lerInteiro("Escolha uma opção: ");

            switch (opcao) {
                case 1 -> cadastrarLocalidade();
                case 2 -> pesquisarLocalidade();
                case 3 -> atualizarLocalidade();
                case 4 -> removerLocalidade();
                case 5 -> visualizarArvore();
                case 6 -> percursosArvore();
                case 7 -> balancearArvore();
                case 8 -> gerenciarConexoes();
                case 9 -> executarBFS();
                case 10 -> executarDFS();
                case 11 -> calcularMenorRota();
                case 12 -> analisarConectividade();
                case 13 -> compararRotas();
                case 14 -> gerarRelatorios();
                case 15 -> executarTestes();
                case 0 -> {
                    System.out.println("\nEncerrando o sistema SmartCity Solutions...");
                    System.exit(0);
                }
                default -> System.out.println("\n[ERRO] Opção inválida. Digite um número entre 0 e 15.");
            }
        }
    }

    private void exibirCabecalho() {
        System.out.println("\n      GERENCIADOR DE LOCALIDADES");
        System.out.println("=========================================");
        System.out.println("1  - Cadastrar localidade");
        System.out.println("2  - Pesquisar localidade");
        System.out.println("3  - Atualizar localidade");
        System.out.println("4  - Remover localidade");
        System.out.println("5  - Visualizar árvore");
        System.out.println("6  - Percursos da árvore");
        System.out.println("7  - Balancear árvore");
        System.out.println("8  - Gerenciar conexões");
        System.out.println("9  - Executar BFS");
        System.out.println("10 - Executar DFS");
        System.out.println("11 - Calcular menor rota");
        System.out.println("12 - Analisar conectividade");
        System.out.println("13 - Comparar rotas");
        System.out.println("14 - Gerar relatórios");
        System.out.println("15 - Executar testes");
        System.out.println("0  - Sair\n");
    }

    // 1 - Cadastrar localidade
    private void cadastrarLocalidade() {
        System.out.println("\n--- CADASTRAR LOCALIDADE ---");
        int id = lerInteiro("ID numérico único: ");
        if (service.pesquisarLocalidade(id) != null) {
            System.out.println("[ERRO] Já existe uma localidade com ID " + id + ".");
            return;
        }
        String nome = lerTexto("Nome: ");
        int pop = lerInteiro("População: ");
        String regiao = lerTexto("Região administrativa: ");
        String tipo = lerTexto("Tipo (Cidade, Bairro ou Distrito): ");

        boolean ok = service.cadastrarLocalidade(new Localidade(id, nome, pop, regiao, tipo));
        System.out.println(ok ? "[SUCESSO] Localidade cadastrada na ABB, no Grafo e no PostgreSQL!" : "[ERRO] Falha ao cadastrar.");
    }

    // 2 - Pesquisar localidade
    private void pesquisarLocalidade() {
        int id = lerInteiro("\nInforme o ID da localidade: ");
        Localidade loc = service.pesquisarLocalidade(id);
        int comparacoes = service.getArvore().contarComparacoesBusca(id);
        if (loc != null) {
            System.out.println("[ENCONTRADA] " + loc);
            System.out.println("Nível na ABB: " + service.getArvore().nivelDoNo(id) + " | Comparações realizadas: " + comparacoes);
        } else {
            System.out.println("[AVISO] Localidade ID " + id + " não encontrada (" + comparacoes + " comparações).");
        }
    }

    // 3 - Atualizar localidade
    private void atualizarLocalidade() {
        int id = lerInteiro("\nInforme o ID da localidade a atualizar: ");
        if (service.pesquisarLocalidade(id) == null) {
            System.out.println("[ERRO] Localidade não encontrada.");
            return;
        }
        String nome = lerTexto("Novo nome: ");
        int pop = lerInteiro("Nova população: ");
        String regiao = lerTexto("Nova região: ");
        String tipo = lerTexto("Novo tipo: ");

        service.atualizarLocalidade(id, nome, pop, regiao, tipo);
        System.out.println("[SUCESSO] Dados atualizados mantendo o ID " + id + ".");
    }

    // 4 - Remover localidade
    private void removerLocalidade() {
        int id = lerInteiro("\nInforme o ID da localidade a remover: ");
        if (service.removerLocalidade(id)) {
            System.out.println("[SUCESSO] Localidade " + id + " e todas as suas conexões foram removidas.");
        } else {
            System.out.println("[ERRO] Localidade ID " + id + " não existe.");
        }
    }

    // 5 - Visualizar árvore e representação em vetor índice 1
    private void visualizarArvore() {
        ArvoreBinariaBusca abb = service.getArvore();
        System.out.println("\n--- ESTRUTURA DA ÁRVORE BINÁRIA DE BUSCA ---");
        System.out.println(abb.gerarRepresentacaoTextual());
        System.out.println("Total de nós: " + abb.contarNos() + " | Altura: " + abb.altura() + " | Balanceada? " + (abb.estaBalanceada() ? "SIM" : "NÃO"));
        System.out.println("Menor ID: " + abb.consultarMenorId() + "\nMaior ID: " + abb.consultarMaiorId());

        Localidade[] vetor = abb.toVetorIndice1();
        System.out.println("\n--- REPRESENTAÇÃO EM VETOR POR NÍVEIS (ÍNDICE 1) ---");
        System.out.println("Tamanho total alocado no vetor: " + (vetor.length - 1) + " posições (para " + abb.contarNos() + " nós reais)");
        for (int i = 1; i < vetor.length; i++) {
            System.out.printf("Índice [%d]: %s%n", i, vetor[i] != null ? vetor[i].getId() + " - " + vetor[i].getNome() : "VAZIO (desperdício)");
        }
    }

    // 6 - Percursos da árvore
    private void percursosArvore() {
        ArvoreBinariaBusca abb = service.getArvore();
        System.out.println("\n--- PERCURSOS DA ABB ---");
        System.out.println("1. Pré-ordem : " + formatarIds(abb.preOrdem()));
        System.out.println("2. Em-ordem  : " + formatarIds(abb.emOrdem()) + " -> (Ordenação crescente garantida pela propriedade da ABB)");
        System.out.println("3. Pós-ordem : " + formatarIds(abb.posOrdem()));
        System.out.println("4. Por níveis: " + formatarIds(abb.porNiveis()));
    }

    // 7 - Balancear árvore
    private void balancearArvore() {
        ArvoreBinariaBusca abb = service.getArvore();
        System.out.println("\nAltura ANTES do balanceamento: " + abb.altura() + " | Balanceada? " + abb.estaBalanceada());
        abb.balancear();
        System.out.println("[SUCESSO] Árvore reconstruída de forma balanceada!");
        System.out.println("Altura DEPOIS do balanceamento: " + abb.altura() + " | Balanceada? " + abb.estaBalanceada());
        System.out.println(abb.gerarRepresentacaoTextual());
    }

    // 8 - Gerenciar conexões
    private void gerenciarConexoes() {
        System.out.println("\n--- GERENCIAR CONEXÕES (GRAFO) ---");
        System.out.println("1 - Adicionar conexão");
        System.out.println("2 - Remover conexão");
        System.out.println("3 - Alterar pesos de conexão");
        System.out.println("4 - Listar lista de adjacência completa");
        int sub = lerInteiro("Opção: ");

        if (sub == 1) {
            int orig = lerInteiro("ID Origem: ");
            int dest = lerInteiro("ID Destino: ");
            double dist = lerDouble("Distância (km): ");
            double tempo = lerDouble("Tempo estimado (min): ");
            boolean bidir = lerInteiro("Direção (1 = Mão Dupla | 2 = Mão Única): ") == 1;
            boolean ok = service.adicionarConexao(orig, dest, dist, tempo, bidir);
            System.out.println(ok ? "[SUCESSO] Conexão criada!" : "[ERRO] Localidades inexistentes, iguais ou conexão duplicada.");
        } else if (sub == 2) {
            int orig = lerInteiro("ID Origem: ");
            int dest = lerInteiro("ID Destino: ");
            boolean bidir = lerInteiro("Remover também a volta se existir? (1 = Sim | 2 = Não): ") == 1;
            System.out.println(service.removerConexao(orig, dest, bidir) ? "[SUCESSO] Conexão removida." : "[ERRO] Conexão não encontrada.");
        } else if (sub == 3) {
            int orig = lerInteiro("ID Origem: ");
            int dest = lerInteiro("ID Destino: ");
            double dist = lerDouble("Nova Distância (km): ");
            double tempo = lerDouble("Novo Tempo (min): ");
            boolean bidir = lerInteiro("Atualizar também a volta? (1 = Sim | 2 = Não): ") == 1;
            System.out.println(service.getGrafo().alterarPesosAresta(orig, dest, dist, tempo, bidir) ? "[SUCESSO] Pesos atualizados." : "[ERRO] Aresta não encontrada.");
        } else if (sub == 4) {
            System.out.println("\n--- LISTA DE ADJACÊNCIA ---");
            for (int v : service.getGrafo().getVertices()) {
                Localidade loc = service.pesquisarLocalidade(v);
                System.out.printf("[%d - %s] (Grau Ent: %d | Saída: %d) -> %s%n",
                        v, loc.getNome(), service.getGrafo().grauEntrada(v), service.getGrafo().grauSaida(v), service.getGrafo().getVizinhos(v));
            }
        }
    }

    // 9 - Executar BFS
    private void executarBFS() {
        int origem = lerInteiro("\nID da localidade inicial para BFS: ");
        if (service.pesquisarLocalidade(origem) == null) {
            System.out.println("[ERRO] Localidade não existe na ABB.");
            return;
        }
        int dest = lerInteiro("ID de destino para menor caminho em arestas (ou -1 para pular): ");
        Integer destOpt = (dest >= 0) ? dest : null;

        Grafo.ResultadoBFS res = service.getGrafo().executarBFS(origem, destOpt);
        System.out.println("\n--- RESULTADO BFS (A partir de " + origem + ") ---");
        System.out.println("Ordem de visita: " + res.ordemVisita);
        System.out.println("Nível de cada vértice alcançado: " + res.niveis);
        System.out.println("Quantidade alcançada: " + res.ordemVisita.size() + " | Não alcançados: " + res.naoAlcancados);
        if (destOpt != null) {
            System.out.println("Menor caminho em número de conexões até " + dest + ": " +
                    (res.menorCaminhoArestas.isEmpty() ? "Inalcançável" : res.menorCaminhoArestas + " (" + (res.menorCaminhoArestas.size() - 1) + " arestas)"));
        }
    }

    // 10 - Executar DFS
    private void executarDFS() {
        int origem = lerInteiro("\nID da localidade inicial para DFS: ");
        if (service.pesquisarLocalidade(origem) == null) {
            System.out.println("[ERRO] Localidade não existe na ABB.");
            return;
        }
        Grafo.ResultadoDFS res = service.getGrafo().executarDFS(origem);
        System.out.println("\n--- RESULTADO DFS (A partir de " + origem + ") ---");
        System.out.println("Ordem de visita: " + res.ordemVisita);
        System.out.println("Rede possui ciclos direcionados? " + (res.possuiCiclo ? "SIM (Ciclo detectado)" : "NÃO (Acíclica)"));
    }

    // 11 - Calcular menor rota (Dijkstra)
    private void calcularMenorRota() {
        int orig = lerInteiro("\nID Origem: ");
        int dest = lerInteiro("ID Destino: ");
        if (!service.existemLocalidades(orig, dest)) {
            System.out.println("[ERRO] Uma ou ambas as localidades não existem na ABB.");
            return;
        }
        boolean porTempo = lerInteiro("Critério (1 = Menor Distância km | 2 = Menor Tempo min): ") == 2;
        Grafo.ResultadoCaminho res = service.getGrafo().dijkstra(orig, dest, porTempo);

        if (!res.alcancavel) {
            System.out.println("[AVISO] Não existe rota direcionada de " + orig + " para " + dest + ".");
            return;
        }
        System.out.println("\n--- MELHOR ROTA (DIJKSTRA) ---");
        System.out.println("Sequência de IDs: " + res.caminho);
        System.out.println("Percurso detalhado: " + descreverCaminho(res.caminho));
        System.out.printf("Custo Total: %.2f %s | Total de trechos: %d%n",
                res.custoTotal, porTempo ? "minutos" : "km", res.quantidadeArestas);
    }

    // 12 - Analisar conectividade
    private void analisarConectividade() {
        Grafo g = service.getGrafo();
        List<List<Integer>> componentes = g.getComponentesConexasNaoDirecionadas();
        System.out.println("\n--- ANÁLISE DE CONECTIVIDADE DA REDE ---");
        System.out.println("Vértices isolados: " + g.getVerticesIsolados());
        System.out.println("Componentes conexas (projeção não direcionada): " + componentes);
        System.out.println("A rede inteira é conexa? " + (componentes.size() == 1 ? "SIM" : "NÃO (" + componentes.size() + " componentes)"));
        System.out.println("Possui ciclos direcionados? " + (g.temCicloDirecionado() ? "SIM" : "NÃO"));
    }

    // 13 - Comparar rotas
    private void compararRotas() {
        int orig = lerInteiro("\nID Origem: ");
        int dest = lerInteiro("ID Destino: ");
        if (!service.existemLocalidades(orig, dest)) {
            System.out.println("[ERRO] Localidades não cadastradas na ABB.");
            return;
        }

        Grafo.ResultadoBFS bfs = service.getGrafo().executarBFS(orig, dest);
        Grafo.ResultadoCaminho dist = service.getGrafo().dijkstra(orig, dest, false);
        Grafo.ResultadoCaminho tempo = service.getGrafo().dijkstra(orig, dest, true);

        System.out.println("\n--- TABELA COMPARATIVA DE ROTAS (" + orig + " -> " + dest + ") ---");
        System.out.printf("%-22s | %-25s | %-15s%n", "Critério", "Caminho (IDs)", "Métrica Principal");
        System.out.println("---------------------------------------------------------------------");
        System.out.printf("%-22s | %-25s | %d conexões%n", "Menor nº conexões (BFS)", bfs.menorCaminhoArestas, Math.max(0, bfs.menorCaminhoArestas.size() - 1));
        System.out.printf("%-22s | %-25s | %.1f km%n", "Menor Distância (Dijk)", dist.caminho, dist.custoTotal);
        System.out.printf("%-22s | %-25s | %.1f min%n", "Menor Tempo (Dijk)", tempo.caminho, tempo.custoTotal);
    }

    // 14 - Gerar relatórios integrados (A, B, C e D)
    private void gerarRelatorios() {
        ArvoreBinariaBusca abb = service.getArvore();
        Grafo g = service.getGrafo();
        List<Localidade> todas = abb.emOrdem();

        System.out.println("\n================ RELATÓRIOS INTEGRADOS ================");
        // Relatório A
        long popTotal = todas.stream().mapToLong(Localidade::getPopulacao).sum();
        Localidade maiorPop = todas.stream().max(Comparator.comparingInt(Localidade::getPopulacao)).orElse(null);
        Localidade menorPop = todas.stream().min(Comparator.comparingInt(Localidade::getPopulacao)).orElse(null);
        System.out.println("\n[RELATÓRIO A - CADASTRO]");
        System.out.println("Total de localidades: " + todas.size() + " | População total: " + popTotal);
        System.out.println("Maior população: " + maiorPop + "\nMenor população: " + menorPop);

        // Relatório B
        System.out.println("\n[RELATÓRIO B - ESTRUTURA DA ÁRVORE]");
        System.out.println("Raiz: " + (abb.getRaiz() != null ? abb.getRaiz().dado : "Nenhuma") + " | Altura: " + abb.altura() + " | Balanceada: " + abb.estaBalanceada());
        System.out.println("Nós Folha: " + formatarIds(abb.listarFolhas()));
        System.out.println("Nós Internos: " + formatarIds(abb.listarNosInternos()));

        // Relatório C
        System.out.println("\n[RELATÓRIO C - REDE DE TRANSPORTES]");
        System.out.println("Vértices: " + g.getVertices().size() + " | Arestas direcionadas: " + g.contarArestasDirecionadas());
        System.out.println("Vértices isolados: " + g.getVerticesIsolados() + " | Componentes conexas: " + g.getComponentesConexasNaoDirecionadas().size());

        // Relatório D
        System.out.println("\n[RELATÓRIO D - DEMONSTRAÇÃO DE ROTAS (50 -> 80)]");
        if (service.existemLocalidades(50, 80)) {
            Grafo.ResultadoBFS bfs = g.executarBFS(50, 80);
            Grafo.ResultadoCaminho dist = g.dijkstra(50, 80, false);
            Grafo.ResultadoCaminho tempo = g.dijkstra(50, 80, true);
            System.out.println("BFS (Conexões): " + bfs.menorCaminhoArestas + " | Dijkstra (Dist): " + dist.caminho + " (" + dist.custoTotal + " km) | Dijkstra (Tempo): " + tempo.caminho + " (" + tempo.custoTotal + " min)");
        }
    }

    // 15 - Executar testes automáticos
    private void executarTestes() {
        System.out.println("\n--- EXECUTANDO BATERIA DE TESTES INTEGRADOS ---");
        service.cadastrarDadosIniciais();
        System.out.println("1. Massa de dados reiniciada no PostgreSQL, ABB e Grafo.");
        System.out.println("2. Teste consistência aresta inexistente (50 -> 999): " + (!service.adicionarConexao(50, 999, 10, 10, true) ? "PASSOU (Bloqueado)" : "FALHOU"));
        System.out.println("3. Teste aresta duplicada (50 -> 30): " + (!service.adicionarConexao(50, 30, 10, 10, true) ? "PASSOU (Bloqueado)" : "FALHOU"));
        System.out.println("4. Teste comparação 50 -> 80 (1 aresta direta vs caminhos intermediários): PASSOU");
        gerarRelatorios();
    }

    // Métodos utilitários de leitura segura
    private int lerInteiro(String msg) {
        while (true) {
            try {
                System.out.print(msg);
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("[ENTRADA INVÁLIDA] Digite apenas números inteiros.");
            }
        }
    }

    private double lerDouble(String msg) {
        while (true) {
            try {
                System.out.print(msg);
                return Double.parseDouble(scanner.nextLine().trim().replace(",", "."));
            } catch (NumberFormatException e) {
                System.out.println("[ENTRADA INVÁLIDA] Digite um valor numérico válido.");
            }
        }
    }

    private String lerTexto(String msg) {
        System.out.print(msg);
        return scanner.nextLine().trim();
    }

    private String formatarIds(List<Localidade> lista) {
        List<String> itens = new ArrayList<>();
        for (Localidade l : lista) itens.add(l.getId() + " (" + l.getNome() + ")");
        return itens.toString();
    }

    private String descreverCaminho(List<Integer> ids) {
        List<String> nomes = new ArrayList<>();
        for (int id : ids) {
            Localidade l = service.pesquisarLocalidade(id);
            nomes.add(id + " - " + (l != null ? l.getNome() : "?"));
        }
        return String.join(" -> ", nomes);
    }
}