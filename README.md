# radio-gestão

Sistema de gestão para uma empresa de rádio comunicação — API para todo o fluxo de manutenção de equipamentos: recebimento, avaliação técnica, orçamento, garantia e entrega.

> Frontend (React + TypeScript): [radio-gestao-front](https://github.com/vitorsmachado/radio-gestao-front)

## Funcionalidades

- **Ordem de serviço com ciclo de status por item**, não por OS — cada equipamento/acessório/peça trazido pelo cliente avança independente (avaliação → autorização → manutenção → entrega), permitindo separar, desmembrar ou unir itens entre OS conforme o ritmo de cada um.
- **Orçamento** que agrupa os itens avaliados de uma OS numa proposta formal (validade, condições de pagamento, desconto).
- **Garantia em duas camadas**: cobertura por peça trocada num reparo (com atalho automático pra entrega quando o defeito é 100% coberto) e garantia de fábrica/venda do equipamento/acessório, com prazos configuráveis.
- **Catálogo e estoque** de equipamentos/acessórios/peças, com resolução automática por número de série e baixa de estoque ao concluir manutenção.
- **Configurações administráveis em runtime** — prazos de garantia, valor padrão de mão de obra e dados da empresa (nome, CNPJ, endereço, contato) usados no cabeçalho dos documentos, sem precisar de deploy pra mudar.
- **Geração de PDF** (OS e orçamento) com cabeçalho personalizado (logo, dados da empresa), formatação de documento (CPF/CNPJ), e proteção contra quebra de página no meio de um item — cada equipamento/peça fica inteiro numa página só.
- **Notificações** internas (ex.: conflito de garantia) e sugestões de texto (autocomplete) pros campos de avaliação técnica mais usados.

## Arquitetura

Organizado por módulo de domínio (DDD leve), cada um com suas próprias camadas:

```
com.radiocom.<modulo>
├── domain/        # entidades, regras de negócio, repositórios (interfaces)
├── application/   # casos de uso, DTOs, mapeamento
└── interfaces/    # REST controllers, exception handlers
```

## Fluxo principal

```
Cliente cadastrado
      │
      ▼
OS aberta ── itens de entrada registrados (equipamento/acessório/peça)
      │
      ▼
Avaliação técnica ──► orçamento agrupado automaticamente ──► autorização do cliente
      │
      ├── autorizado ──► fila de manutenção (ou aguardando peça, se faltar em estoque)
      │                        │
      │                        ▼
      │                  manutenção concluída ──► aguardando entrega ──► entregue
      │
      └── não autorizado ─► aguardando entrega ──► entregue
```

Itens podem ser movidos entre OS a qualquer momento: **separar** (um ou mais itens saem pra uma ou mais OS novas, tudo numa transação atômica), **desmembrar** (uma quantidade de um item vira um item novo, útil quando parte de um lote toma destino diferente) e **unir** (várias OS do mesmo cliente viram uma OS nova só).

## Módulos

| Módulo | Descrição |
|---|---|
| `auth` | Autenticação JWT e papéis de usuário |
| `cliente` | Cadastro de clientes, contatos, postos e endereço |
| `estoque` | Catálogo e estoque de equipamentos/acessórios/peças |
| `ordemservico` | OS, itens de entrada (ciclo próprio de status), itens de conserto, garantia por peça e sugestões de texto |
| `orcamento` | Envelope que agrupa itens de uma OS numa proposta formal ao cliente |
| `configuracao` | Valores administráveis em runtime (prazos de garantia, dados da empresa) |
| `notificacao` | Notificações internas (ex.: conflito de garantia) |

## Tecnologias

Java 17 · Spring Boot 3.2 (Web, Data JPA, Security, Validation) · PostgreSQL · Flyway · JWT (jjwt) · Thymeleaf + Flying Saucer/OpenPDF (geração de PDF) · JUnit 5 + Mockito + Testcontainers

## Como rodar localmente

Pré-requisitos: Java 17, Docker.

```bash
docker compose up -d          # sobe o Postgres (+ Adminer em http://localhost:9090)
./mvnw spring-boot:run         # sobe a aplicação em http://localhost:8080/api
```

Documentação da API (Swagger): `http://localhost:8080/api/swagger-ui.html`

### Trocando a logo e os dados da empresa

A logo do cabeçalho dos PDFs vem da propriedade `empresa.logo` (variável `EMPRESA_LOGO`), que aceita qualquer recurso do Spring — o padrão é o arquivo empacotado `src/main/resources/static/logo-teletrom.png`, mas dá pra apontar outro sem recompilar (ex.: `EMPRESA_LOGO=file:/dados/logo.png`). Nome, CNPJ, endereço e demais dados da empresa são editáveis direto na tela de **Configurações**, sem precisar de deploy.

## Perfis e deploy

| Perfil | Uso |
|---|---|
| `dev` (padrão) | Desenvolvimento local, com SQL e logs detalhados |
| `prod` | Produção — credenciais só por variável de ambiente, sem valores padrão |
| `demo` | Combinado com outro (`PROFILE=prod,demo`): empresa fictícia, logo de demonstração e banco vazio populado com clientes e OS em todas as etapas do fluxo |

O `Dockerfile` gera a imagem de produção. Variáveis usadas pelo perfil `prod`: `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASS`, `JWT_SECRET` (e, no `demo`, `DEMO_SENHA` para o usuário `demo`).


## Testes

```bash
./mvnw test      # testes unitários + slices de API (mocks, H2 só pra subir contexto) — não precisa de Docker
./mvnw verify     # os de cima + testes de integração (*IT.java) com Postgres real via Testcontainers — precisa de Docker
```

Mais de 500 testes automatizados. Testes unitários cobrem regras de domínio e serviços de aplicação; slices de API (`*ControllerTest`) sobem só a camada web com os serviços mockados. Os testes de integração (`*IT.java`, separados via Maven Failsafe) sobem o contexto inteiro contra um Postgres real, validando que as migrations Flyway aplicam sem erro e que as entidades JPA batem com o schema (`ddl-auto=validate`).
