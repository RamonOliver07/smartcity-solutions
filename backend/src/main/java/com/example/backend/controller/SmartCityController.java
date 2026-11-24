package com.example.backend.controller;

import com.example.backend.model.Conexao;
import com.example.backend.model.Localidade;
import com.example.backend.service.SmartCityService;
import com.example.backend.structures.ArvoreBinariaBusca;
import com.example.backend.structures.Grafo;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:4200")
public class SmartCityController {

    private final SmartCityService service;

    public SmartCityController(SmartCityService service) {
        this.service = service;
    }

    // LOCALIDADES & ÁRVORE BINÁRIA DE BUSCA
    @GetMapping("/localidades")
    public List<Localidade> listarOrdenado() {
        return service.getArvore().emOrdem();
    }

    @GetMapping("/localidades/{id}")
    public ResponseEntity<Localidade> buscarPorId(@PathVariable int id) {
        Localidade loc = service.pesquisarLocalidade(id);
        return loc != null ? ResponseEntity.ok(loc) : ResponseEntity.notFound().build();
    }

    @PostMapping("/localidades")
    public ResponseEntity<?> cadastrar(@RequestBody Localidade loc) {
        if (!service.cadastrarLocalidade(loc)) {
            return ResponseEntity.badRequest().body(Map.of("erro", "ID já cadastrado na árvore."));
        }
        return ResponseEntity.ok(loc);
    }

    @PutMapping("/localidades/{id}")
    public ResponseEntity<?> atualizar(@PathVariable int id, @RequestBody Localidade loc) {
        if (!service.atualizarLocalidade(id, loc.getNome(), loc.getPopulacao(), loc.getRegiao(), loc.getTipo())) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(service.pesquisarLocalidade(id));
    }

    @DeleteMapping("/localidades/{id}")
    public ResponseEntity<?> remover(@PathVariable int id) {
        if (!service.removerLocalidade(id)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(Map.of("mensagem", "Localidade e conexões removidas."));
    }

    @GetMapping("/arvore/info")
    public Map<String, Object> infoArvore() {
        ArvoreBinariaBusca abb = service.getArvore();
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("altura", abb.altura());
        info.put("totalNos", abb.contarNos());
        info.put("balanceada", abb.estaBalanceada());
        info.put("preOrdem", abb.preOrdem());
        info.put("emOrdem", abb.emOrdem());
        info.put("posOrdem", abb.posOrdem());
        info.put("porNiveis", abb.porNiveis());
        info.put("vetorIndice1", abb.toVetorIndice1());
        info.put("arvoreTexto", abb.gerarRepresentacaoTextual());
        return info;
    }

    @PostMapping("/arvore/balancear")
    public Map<String, Object> balancear() {
        service.getArvore().balancear();
        return infoArvore();
    }

    // GRAFO & CONEXÕES
    @GetMapping("/grafo")
    public Map<String, Object> dadosGrafo() {
        Grafo g = service.getGrafo();
        Map<String, Object> res = new LinkedHashMap<>();
        res.put("adjacencias", g.getListaAdjacencia());
        res.put("isolados", g.getVerticesIsolados());
        res.put("componentesConexas", g.getComponentesConexasNaoDirecionadas());
        res.put("possuiCiclo", g.temCicloDirecionado());
        return res;
    }

    @PostMapping("/conexoes")
    public ResponseEntity<?> adicionarConexao(@RequestBody Conexao con) {
        boolean ok = service.adicionarConexao(
                con.getOrigemId(), con.getDestinoId(), con.getDistanciaKm(), con.getTempoMinutos(), con.isBidirecional());
        if (!ok) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Vértices inválidos ou conexão duplicada."));
        }
        return ResponseEntity.ok(Map.of("mensagem", "Conexão adicionada."));
    }

    // ALGORITMOS (BFS, DFS, DIJKSTRA E COMPARAÇÃO)
    @GetMapping("/algoritmos/bfs")
    public Grafo.ResultadoBFS bfs(@RequestParam int origem, @RequestParam(required = false) Integer destino) {
        return service.getGrafo().executarBFS(origem, destino);
    }

    @GetMapping("/algoritmos/dfs")
    public Grafo.ResultadoDFS dfs(@RequestParam int origem) {
        return service.getGrafo().executarDFS(origem);
    }

    @GetMapping("/algoritmos/comparar")
    public ResponseEntity<?> comparar(@RequestParam int origem, @RequestParam int destino) {
        if (!service.existemLocalidades(origem, destino)) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Localidades não existem na ABB."));
        }
        Map<String, Object> comp = new LinkedHashMap<>();
        comp.put("bfsConexoes", service.getGrafo().executarBFS(origem, destino));
        comp.put("dijkstraDistancia", service.getGrafo().dijkstra(origem, destino, false));
        comp.put("dijkstraTempo", service.getGrafo().dijkstra(origem, destino, true));
        return ResponseEntity.ok(comp);
    }
}