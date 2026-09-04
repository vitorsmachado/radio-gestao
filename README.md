# radio-gestao

Sistema de gestão para empresa de rádio comunicação — API para orçamento de manutenção/conserto e ordens de serviço.

> **Status: em construção.** Este repositório é uma versão enxuta, focada em duas rotas de negócio, extraída de um sistema maior em evolução. O objetivo é entregar um núcleo sólido, testado e documentado, e evoluir a partir dele.

## Arquitetura

Organizado por módulo de domínio (DDD leve), cada um com suas próprias camadas:

```
com.radiocom.<modulo>
├── domain/        # entidades, regras de negócio, repositórios (interfaces)
├── application/   # casos de uso, DTOs, mapeamento
└── interfaces/     # REST controllers, exception handlers
```

## Rota implementada

O sistema cobre o fluxo completo de um orçamento de manutenção, do cadastro do cliente até a geração da ordem de serviço:

```
Cliente cadastrado
      │
      ▼
Orçamento criado (RASCUNHO) ── itens de conserto referenciam o catálogo de Estoque
      │
      ▼
Enviado para análise (AGUARDANDO_APROVAÇÃO)
      │
      ├── aprovado ──► APROVADO ──► convertido em Ordem de Serviço
      │
      └── rejeitado ─► REJEITADO
```

## Módulos

| Módulo | Status | Descrição |
|---|---|---|
| `auth` | ✅ | Autenticação JWT e papéis de usuário |
| `cliente` | ✅ | Cadastro de clientes e postos |
| `estoque` | ✅ | Catálogo de equipamentos/acessórios/peças usados nos orçamentos |
| `orcamento` | ⬜ | Orçamento de manutenção/conserto |
| `ordemservico` | ⬜ | Conversão de orçamento aprovado em ordem de serviço |

## Como rodar localmente

Pré-requisitos: Java 17, Docker.

```bash
docker compose up -d          # sobe o Postgres (+ Adminer em http://localhost:9090)
./mvnw spring-boot:run         # sobe a aplicação em http://localhost:8080/api
```

Documentação da API (Swagger): `http://localhost:8080/api/swagger-ui.html`

## Testes

```bash
./mvnw test
```

Testes unitários cobrem regras de domínio e serviços de aplicação; testes de integração usam Testcontainers com Postgres real.
