package com.example.backend.service;

import com.example.backend.model.Conexao;
import com.example.backend.model.Localidade;
import com.example.backend.repository.ConexaoRepository;
import com.example.backend.repository.LocalidadeRepository;
import com.example.backend.structures.ArvoreBinariaBusca;
import com.example.backend.structures.Grafo;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SmartCityService {

    private final LocalidadeRepository localidadeRepo;
    private final ConexaoRepository conexaoRepo;

    // Estruturas centrais na memória (sem 'final' para permitir reset completo nos testes)
    private ArvoreBinariaBusca arvore = new ArvoreBinariaBusca();
    private Grafo grafo = new Grafo();

    public SmartCityService(LocalidadeRepository localidadeRepo, ConexaoRepository conexaoRepo) {
        this.localidadeRepo = localidadeRepo;
        this.conexaoRepo = conexaoRepo;
    }

    @PostConstruct
    public void inicializarEstruturas() {
        List<Localidade> salvas = localidadeRepo.findAll();

        // Se o banco estiver vazio, cadastra massa de dados inicial automaticamente
        if (salvas.isEmpty()) {
            cadastrarDadosIniciais();
            return;
        }

        // Reconstrói ABB e Grafo a partir do PostgreSQL
        for (Localidade loc : salvas) {
            arvore.inserir(loc);
            grafo.adicionarVertice(loc.getId());
        }

        for (Conexao con : conexaoRepo.findAll()) {
            grafo.adicionarAresta(
                    con.getOrigemId(),
                    con.getDestinoId(),
                    con.getDistanciaKm(),
                    con.getTempoMinutos(),
                    con.isBidirecional()
            );
        }
    }

    @Transactional
    public void cadastrarDadosIniciais() {
        // 1. Limpa o banco de dados PostgreSQL
        conexaoRepo.deleteAllInBatch();
        localidadeRepo.deleteAllInBatch();

        // 2. Reseta as estruturas na memória
        this.arvore = new ArvoreBinariaBusca();
        this.grafo = new Grafo();

        // 3. Nível Raiz e Principais Eixos (ABB balanceada na inserção)
        cadastrarLocalidade(new Localidade(50, "Plano Piloto (Brasília)", 207996, "Centro-DF", "Região Adm."));
        cadastrarLocalidade(new Localidade(30, "Gama", 133948, "Sul-DF", "Região Adm."));
        cadastrarLocalidade(new Localidade(70, "Valparaíso de Goiás", 223210, "Entorno Sul-GO", "Cidade"));

        // 4. Sub-eixos Intermediários
        cadastrarLocalidade(new Localidade(20, "Taguatinga", 210498, "Oeste-DF", "Região Adm."));
        cadastrarLocalidade(new Localidade(40, "Santa Maria", 121635, "Sul-DF", "Região Adm."));
        cadastrarLocalidade(new Localidade(60, "Cidade Ocidental", 104114, "Entorno Sul-GO", "Cidade"));
        cadastrarLocalidade(new Localidade(80, "Luziânia", 223596, "Entorno Sul-GO", "Cidade"));

        // 5. Regiões Administrativas do DF e Cidades do Entorno
        cadastrarLocalidade(new Localidade(10, "Ceilândia", 350347, "Oeste-DF", "Região Adm."));
        cadastrarLocalidade(new Localidade(25, "Águas Claras", 124212, "Oeste-DF", "Região Adm."));
        cadastrarLocalidade(new Localidade(35, "Guará", 125703, "Centro-Sul-DF", "Região Adm."));
        cadastrarLocalidade(new Localidade(45, "Sudoeste/Octogonal", 53208, "Centro-DF", "Região Adm."));
        cadastrarLocalidade(new Localidade(55, "Paranoá", 67142, "Leste-DF", "Região Adm."));
        cadastrarLocalidade(new Localidade(65, "Novo Gama", 108219, "Entorno Sul-GO", "Cidade"));
        cadastrarLocalidade(new Localidade(75, "Recanto das Emas", 131737, "Sudoeste-DF", "Região Adm."));
        cadastrarLocalidade(new Localidade(90, "Cristalina", 67922, "Entorno Sul-GO", "Cidade"));
        cadastrarLocalidade(new Localidade(95, "Núcleo Rural Jardim", 4500, "Leste-DF", "Distrito")); // Vértice isolado para testes

        // =========================================================================
        // CONEXÕES RODOVIÁRIAS REAIS (EPTG, EPIA, BR-060, DF-290, BR-040, GO-521)
        // =========================================================================

        // Eixo Central & Leste (Plano Piloto <-> Sudoeste <-> Paranoá <-> Guará)
        adicionarConexao(50, 45, 7.5, 11.0, true);   // Plano Piloto <-> Sudoeste/Octogonal (Eixo Monumental)
        adicionarConexao(50, 55, 22.0, 26.0, true);  // Plano Piloto <-> Paranoá (Ponte JK / DF-001)
        adicionarConexao(45, 35, 8.0, 12.0, true);   // Sudoeste <-> Guará (EPTG)
        adicionarConexao(50, 35, 13.0, 16.0, true);  // Plano Piloto <-> Guará (EPIA Sul)

        // Eixo Oeste (Guará <-> Águas Claras <-> Taguatinga <-> Ceilândia <-> Recanto das Emas)
        adicionarConexao(35, 25, 9.0, 13.0, true);   // Guará <-> Águas Claras (EPTG)
        adicionarConexao(25, 20, 4.5, 9.0, true);    // Águas Claras <-> Taguatinga (Av. das Castanheiras)
        adicionarConexao(20, 10, 8.5, 14.0, true);   // Taguatinga <-> Ceilândia (Av. Hélio Prates)
        adicionarConexao(20, 75, 14.0, 19.0, true);  // Taguatinga <-> Recanto das Emas (BR-060)
        adicionarConexao(75, 30, 15.0, 18.0, true);  // Recanto das Emas <-> Gama (DF-480)

        // Eixo Sul-DF & Entorno (Guará <-> Gama <-> Santa Maria <-> Novo Gama <-> Valparaíso)
        adicionarConexao(35, 30, 24.0, 27.0, true);  // Guará <-> Gama (EPIA Sul)
        adicionarConexao(30, 40, 12.0, 15.0, true);  // Gama <-> Santa Maria (DF-290)
        adicionarConexao(30, 65, 11.0, 16.0, true);  // Gama <-> Novo Gama (DF-290)
        adicionarConexao(40, 70, 9.5, 14.0, true);   // Santa Maria <-> Valparaíso (BR-040)
        adicionarConexao(65, 70, 13.5, 20.0, true);  // Novo Gama <-> Valparaíso (Via Perimetral)

        // Eixo Entorno Sul-GO (Valparaíso <-> Cidade Ocidental <-> Luziânia <-> Cristalina)
        adicionarConexao(70, 60, 12.0, 18.0, true);  // Valparaíso <-> Cidade Ocidental (GO-521)
        adicionarConexao(70, 80, 26.0, 28.0, true);  // Valparaíso <-> Luziânia (BR-040)
        adicionarConexao(60, 80, 22.0, 26.0, true);  // Cidade Ocidental <-> Luziânia (GO-521)
        adicionarConexao(80, 90, 74.0, 55.0, true);  // Luziânia <-> Cristalina (BR-040)

        // Ligação Expressa Direta para demonstrar trade-off BFS (1 aresta) vs Dijkstra (menor custo)
        adicionarConexao(50, 80, 68.0, 65.0, false); // Plano Piloto -> Luziânia direto (1 conexão)
    }

    // =========================================================
    // OPERAÇÕES INTEGRADAS (ABB + GRAFO + POSTGRESQL)
    // =========================================================
    public boolean cadastrarLocalidade(Localidade loc) {
        if (!arvore.inserir(loc)) return false;
        grafo.adicionarVertice(loc.getId());
        localidadeRepo.save(loc);
        return true;
    }

    public Localidade pesquisarLocalidade(int id) {
        return arvore.pesquisar(id);
    }

    public boolean atualizarLocalidade(int id, String nome, int populacao, String regiao, String tipo) {
        boolean ok = arvore.atualizar(id, nome, populacao, regiao, tipo);
        if (ok) {
            localidadeRepo.save(arvore.pesquisar(id));
        }
        return ok;
    }

    @Transactional
    public boolean removerLocalidade(int id) {
        if (!arvore.remover(id)) return false;
        grafo.removerVertice(id);
        conexaoRepo.deleteByOrigemIdOrDestinoId(id, id);
        localidadeRepo.deleteById(id);
        return true;
    }

    public boolean adicionarConexao(int origemId, int destinoId, double distKm, double tempoMin, boolean bidirecional) {
        if (arvore.pesquisar(origemId) == null || arvore.pesquisar(destinoId) == null) {
            return false;
        }
        boolean adicionou = grafo.adicionarAresta(origemId, destinoId, distKm, tempoMin, bidirecional);
        if (adicionou) {
            conexaoRepo.save(new Conexao(origemId, destinoId, distKm, tempoMin, bidirecional));
        }
        return adicionou;
    }

    @Transactional
    public boolean removerConexao(int origemId, int destinoId, boolean bidirecional) {
        if (arvore.pesquisar(origemId) == null || arvore.pesquisar(destinoId) == null) return false;
        boolean removeu = grafo.removerAresta(origemId, destinoId, bidirecional);
        if (removeu) {
            conexaoRepo.deleteByOrigemIdAndDestinoId(origemId, destinoId);
            if (bidirecional) {
                conexaoRepo.deleteByOrigemIdAndDestinoId(destinoId, origemId);
            }
        }
        return removeu;
    }

    public boolean existemLocalidades(int origemId, int destinoId) {
        return arvore.pesquisar(origemId) != null && arvore.pesquisar(destinoId) != null;
    }

    public ArvoreBinariaBusca getArvore() {
        return arvore;
    }

    public Grafo getGrafo() {
        return grafo;
    }
}