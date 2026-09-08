-- =============================================================================
-- MÓDULO: CLIENTE
-- =============================================================================

CREATE TABLE clientes (
    id                  UUID         NOT NULL DEFAULT gen_random_uuid(),
    data_criacao        TIMESTAMP    NOT NULL DEFAULT NOW(),
    data_atualizacao    TIMESTAMP,
    tipo                VARCHAR(20)  NOT NULL,
    documento           VARCHAR(14)  NOT NULL,
    nome_razao_social   VARCHAR(255) NOT NULL,
    nome_fantasia       VARCHAR(255),
    inscricao_estadual  VARCHAR(20),
    -- Endereco (embedded)
    cep                 VARCHAR(8),
    logradouro          TEXT,
    end_numero          VARCHAR(20),
    complemento         VARCHAR(100),
    bairro              VARCHAR(100),
    cidade              VARCHAR(100),
    estado              VARCHAR(2),
    status              VARCHAR(20)  NOT NULL DEFAULT 'ATIVO',
    CONSTRAINT pk_clientes           PRIMARY KEY (id),
    CONSTRAINT uq_clientes_documento UNIQUE (documento),
    CONSTRAINT chk_clientes_tipo     CHECK (tipo   IN ('PESSOA_FISICA', 'PESSOA_JURIDICA')),
    CONSTRAINT chk_clientes_status   CHECK (status IN ('ATIVO', 'INATIVO', 'BLOQUEADO'))
);

-- uq_clientes_documento já cria índice implícito
CREATE INDEX idx_cliente_status ON clientes (status);
CREATE INDEX idx_cliente_tipo   ON clientes (tipo);

CREATE TABLE contatos (
    id               UUID         NOT NULL DEFAULT gen_random_uuid(),
    data_criacao     TIMESTAMP    NOT NULL DEFAULT NOW(),
    data_atualizacao TIMESTAMP,
    nome             VARCHAR(255) NOT NULL,
    tipo             VARCHAR(30)  NOT NULL,
    telefone         VARCHAR(11),
    email            VARCHAR(255),
    cargo            VARCHAR(100),
    principal        BOOLEAN      NOT NULL DEFAULT FALSE,
    cliente_id       UUID         NOT NULL,
    CONSTRAINT pk_contatos       PRIMARY KEY (id),
    CONSTRAINT fk_contatos_cli   FOREIGN KEY (cliente_id) REFERENCES clientes (id) ON DELETE CASCADE,
    CONSTRAINT chk_contatos_tipo CHECK (tipo IN ('COMERCIAL', 'TECNICO', 'FINANCEIRO', 'GERENCIAL'))
);

CREATE INDEX idx_contatos_cliente ON contatos (cliente_id);

CREATE TABLE postos (
    id               UUID         NOT NULL DEFAULT gen_random_uuid(),
    data_criacao     TIMESTAMP    NOT NULL DEFAULT NOW(),
    data_atualizacao TIMESTAMP,
    nome             VARCHAR(255) NOT NULL,
    -- Endereco (embedded)
    cep              VARCHAR(8),
    logradouro       TEXT,
    numero           VARCHAR(20),
    complemento      VARCHAR(100),
    bairro           VARCHAR(100),
    cidade           VARCHAR(100),
    estado           VARCHAR(2),
    responsavel      VARCHAR(255),
    padrao           BOOLEAN      NOT NULL DEFAULT FALSE,
    cliente_id       UUID         NOT NULL,
    CONSTRAINT pk_postos       PRIMARY KEY (id),
    CONSTRAINT fk_postos_cli   FOREIGN KEY (cliente_id) REFERENCES clientes (id) ON DELETE RESTRICT
);

CREATE INDEX idx_postos_cliente ON postos (cliente_id);
