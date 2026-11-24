# SmartCity Solutions - Sistema de Gestao de Localidades e Rotas

Projeto desenvolvido para a disciplina de **Estrutura de Dados II**, integrando **Arvore Binaria de Busca (ABB)** e **Grafos Ponderados** com dados reais da regiao da RIDE-DF (Brasilia e Entorno).

## Tecnologias Utilizadas
- **Back-end:** Java 21, Spring Boot, Spring Data JPA, PostgreSQL
- **Front-end:** Angular, TypeScript, Bootstrap 5
- **Estruturas Implementadas:** Arvore Binaria de Busca (ABB) com reconstrucao balanceada e Grafo por Lista de Adjacencia (BFS, DFS e Dijkstra).

## Como Executar
1. Configure o banco PostgreSQL em `backend/src/main/resources/application.properties` e inicie o `BackendApplication`.
2. Na pasta `frontend`, execute `npm install` e `ng serve -o`.
