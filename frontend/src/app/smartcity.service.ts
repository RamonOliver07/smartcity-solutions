import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Localidade {
  id: number;
  nome: string;
  populacao: number;
  regiao: string;
  tipo: string;
}

export interface Conexao {
  origemId: number;
  destinoId: number;
  distanciaKm: number;
  tempoMinutos: number;
  bidirecional: boolean;
}

@Injectable({
  providedIn: 'root',
})
export class SmartCityService {
  private readonly apiUrl = 'http://localhost:8080/api';

  constructor(private http: HttpClient) {}

  // Localidades & ABB
  listarLocalidades(): Observable<Localidade[]> {
    return this.http.get<Localidade[]>(`${this.apiUrl}/localidades`);
  }

  cadastrarLocalidade(loc: Localidade): Observable<any> {
    return this.http.post(`${this.apiUrl}/localidades`, loc);
  }

  atualizarLocalidade(id: number, loc: Localidade): Observable<any> {
    return this.http.put(`${this.apiUrl}/localidades/${id}`, loc);
  }

  removerLocalidade(id: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/localidades/${id}`);
  }

  getInfoArvore(): Observable<any> {
    return this.http.get(`${this.apiUrl}/arvore/info`);
  }

  balancearArvore(): Observable<any> {
    return this.http.post(`${this.apiUrl}/arvore/balancear`, {});
  }

  // Grafo, Conexões & Algoritmos
  getDadosGrafo(): Observable<any> {
    return this.http.get(`${this.apiUrl}/grafo`);
  }

  adicionarConexao(con: Conexao): Observable<any> {
    return this.http.post(`${this.apiUrl}/conexoes`, con);
  }

  executarBFS(origem: number, destino?: number): Observable<any> {
    const url =
      destino !== undefined && destino !== null
        ? `${this.apiUrl}/algoritmos/bfs?origem=${origem}&destino=${destino}`
        : `${this.apiUrl}/algoritmos/bfs?origem=${origem}`;
    return this.http.get(url);
  }

  executarDFS(origem: number): Observable<any> {
    return this.http.get(`${this.apiUrl}/algoritmos/dfs?origem=${origem}`);
  }

  compararRotas(origem: number, destino: number): Observable<any> {
    return this.http.get(`${this.apiUrl}/algoritmos/comparar?origem=${origem}&destino=${destino}`);
  }
}
