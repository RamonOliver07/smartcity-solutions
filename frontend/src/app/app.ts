import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SmartCityService, Localidade, Conexao } from './smartcity.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './app.html',
  styleUrl: './app.css',
})
export class App implements OnInit {
  abaAtiva: string = 'localidades';
  mensagem: { texto: string; tipo: 'success' | 'danger' } | null = null;

  localidades: Localidade[] = [];
  infoArvore: any = null;
  dadosGrafo: any = null;

  novaLoc: Localidade = { id: 0, nome: '', populacao: 0, regiao: '', tipo: 'Cidade' };
  editando: boolean = false;

  novaCon: Conexao = {
    origemId: 50,
    destinoId: 30,
    distanciaKm: 10,
    tempoMinutos: 15,
    bidirecional: true,
  };

  origemAlgo: number = 50;
  destinoAlgo: number = 80;
  resultadoBFS: any = null;
  resultadoDFS: any = null;
  resultadoComparacao: any = null;

  constructor(private service: SmartCityService) {}

  ngOnInit(): void {
    this.carregarTudo();
  }

  carregarTudo(): void {
    this.service.listarLocalidades().subscribe((res) => (this.localidades = res));
    this.service.getInfoArvore().subscribe((res) => (this.infoArvore = res));
    this.service.getDadosGrafo().subscribe((res) => (this.dadosGrafo = res));
  }

  exibirAlerta(texto: string, tipo: 'success' | 'danger'): void {
    this.mensagem = { texto, tipo };
    setTimeout(() => (this.mensagem = null), 4000);
  }

  salvarLocalidade(): void {
    if (this.editando) {
      this.service.atualizarLocalidade(this.novaLoc.id, this.novaLoc).subscribe({
        next: () => {
          this.exibirAlerta('Localidade atualizada mantendo o ID na ABB!', 'success');
          this.limparFormLoc();
          this.carregarTudo();
        },
        error: () => this.exibirAlerta('Erro ao atualizar localidade.', 'danger'),
      });
    } else {
      this.service.cadastrarLocalidade(this.novaLoc).subscribe({
        next: () => {
          this.exibirAlerta('Localidade cadastrada na ABB, Grafo e PostgreSQL!', 'success');
          this.limparFormLoc();
          this.carregarTudo();
        },
        error: (err) => this.exibirAlerta(err.error?.erro || 'ID já existe na ABB!', 'danger'),
      });
    }
  }

  prepararEdicao(loc: Localidade): void {
    this.novaLoc = { ...loc };
    this.editando = true;
  }

  limparFormLoc(): void {
    this.novaLoc = { id: 0, nome: '', populacao: 0, regiao: '', tipo: 'Cidade' };
    this.editando = false;
  }

  removerLocalidade(id: number): void {
    this.service.removerLocalidade(id).subscribe({
      next: () => {
        this.exibirAlerta(`Localidade ${id} e suas conexões foram removidas.`, 'success');
        this.carregarTudo();
      },
    });
  }

  balancearABB(): void {
    this.service.balancearArvore().subscribe((res) => {
      this.infoArvore = res;
      this.exibirAlerta('Árvore Binária de Busca reconstruída de forma balanceada!', 'success');
    });
  }

  criarConexao(): void {
    this.service.adicionarConexao(this.novaCon).subscribe({
      next: () => {
        this.exibirAlerta('Conexão adicionada ao Grafo e salva no banco!', 'success');
        this.carregarTudo();
      },
      error: (err) =>
        this.exibirAlerta(
          err.error?.erro || 'Localidades inválidas ou conexão duplicada.',
          'danger',
        ),
    });
  }

  rodarBFS(): void {
    this.service
      .executarBFS(this.origemAlgo, this.destinoAlgo)
      .subscribe((res) => (this.resultadoBFS = res));
  }

  rodarDFS(): void {
    this.service.executarDFS(this.origemAlgo).subscribe((res) => (this.resultadoDFS = res));
  }

  rodarComparacao(): void {
    this.service.compararRotas(this.origemAlgo, this.destinoAlgo).subscribe({
      next: (res) => (this.resultadoComparacao = res),
      error: (err) => this.exibirAlerta(err.error?.erro || 'IDs inválidos.', 'danger'),
    });
  }

  formatarLista(lista: Localidade[]): string {
    return lista ? lista.map((l) => `${l.id} (${l.nome})`).join(' → ') : '';
  }

  getChavesAdjacencia(): string[] {
    return this.dadosGrafo?.adjacencias ? Object.keys(this.dadosGrafo.adjacencias) : [];
  }
}
