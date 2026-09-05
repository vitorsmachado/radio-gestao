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

O sistema cobre o fluxo completo de uma ordem de serviço de manutenção, do cadastro do cliente até a entrega do item consertado. Cada item trazido pelo cliente (`ItemEntrada`) tem seu próprio ciclo de status, independente do status geral da OS — permitindo dividir uma OS em várias (ou uni-las de volta) conforme itens são aprovados, aguardam peça ou ficam prontos em ritmos diferentes:

```
Cliente cadastrado
      │
      ▼
OS aberta ── itens de entrada registrados (equipamento/acessório/peça)
      │
      ▼
Avaliação técnica ──► autorização do cliente
      │
      ├── autorizado ──► fila de manutenção (ou aguardando peça, se faltar em estoque)
      │                        │
      │                        ▼
      │                  manutenção concluída ──► aguardando entrega ──► entregue
      │
      └── não autorizado ─► aguardando entrega ──► entregue
```

Itens podem ser movidos entre OS (`mover`), ou uma OS pode ser dividida/unida (`dividir`/`unir`) conforme o cliente aprova só parte do conserto.

## Módulos

| Módulo | Status | Descrição |
|---|---|---|
| `auth` | ✅ | Autenticação JWT e papéis de usuário |
| `cliente` | ✅ | Cadastro de clientes e postos |
| `estoque` | ✅ | Catálogo de equipamentos/acessórios/peças usados nos orçamentos |
| `ordemservico` | ✅ | OS, itens de entrada (ciclo próprio de status) e itens de conserto |
| `orcamento` | ⬜ | Orçamento de manutenção/conserto |

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
