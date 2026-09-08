-- =============================================================================
-- MÓDULO: ESTOQUE
-- Estratégia TABLE_PER_CLASS: cada subclasse tem sua própria tabela completa.
-- =============================================================================

-- Criado primeiro pois equipamentos, acessorios e pecas referenciam esta tabela.
CREATE TABLE catalogo_modelos (
    id               UUID         NOT NULL DEFAULT gen_random_uuid(),
    data_criacao     TIMESTAMP    NOT NULL DEFAULT NOW(),
    data_atualizacao TIMESTAMP,
    tipo_item        VARCHAR(20)  NOT NULL,
    tipo_acessorio   VARCHAR(30),
    referencia       VARCHAR(50),
    marca            VARCHAR(100) NOT NULL,
    modelo           VARCHAR(100) NOT NULL,
    descricao        VARCHAR(255),
    status           VARCHAR(20)  NOT NULL DEFAULT 'ATIVO',
    controle_por_serie  BOOLEAN    NOT NULL DEFAULT FALSE,
    possui_patrimonio   BOOLEAN    NOT NULL DEFAULT FALSE,
    CONSTRAINT pk_catalogo_modelos          PRIMARY KEY (id),
    CONSTRAINT uk_catalogo_tipo_marca_modelo UNIQUE (tipo_item, marca, modelo),
    CONSTRAINT uq_catalogo_referencia       UNIQUE (referencia),
    CONSTRAINT chk_catalogo_tipo_item       CHECK (tipo_item IN ('EQUIPAMENTO', 'ACESSORIO', 'PECA', 'SERVICO')),
    CONSTRAINT chk_catalogo_status          CHECK (status    IN ('ATIVO', 'INATIVO', 'OBSOLETO'))
);

CREATE TABLE equipamentos (
    -- Colunas de ItemEstoque (herdadas)
    id                    UUID         NOT NULL DEFAULT gen_random_uuid(),
    data_criacao          TIMESTAMP    NOT NULL DEFAULT NOW(),
    data_atualizacao      TIMESTAMP,
    codigo                VARCHAR(50)  NOT NULL,
    descricao             VARCHAR(255) NOT NULL,
    tipo                  VARCHAR(20)  NOT NULL,
    catalogo_modelo_id    UUID,
    quantidade_disponivel INTEGER      NOT NULL DEFAULT 0,
    quantidade_minima     INTEGER,
    localizacao_fisica    VARCHAR(50),
    status                VARCHAR(20)  NOT NULL DEFAULT 'ATIVO',
    observacoes           TEXT,
    codigo_cliente        VARCHAR(100),
    -- Colunas próprias de Equipamento
    proprietario          VARCHAR(20)  NOT NULL,
    numero_serie          VARCHAR(50)  NOT NULL,
    patrimonio            VARCHAR(50),
    cliente_id            UUID,
    garantia_fim          DATE,
    faixa                 VARCHAR(20)  NOT NULL,
    estado                VARCHAR(20)  NOT NULL DEFAULT 'DISPONIVEL',
    CONSTRAINT pk_equipamentos        PRIMARY KEY (id),
    CONSTRAINT uq_equipamentos_codigo UNIQUE (codigo),
    CONSTRAINT fk_equip_catalogo      FOREIGN KEY (catalogo_modelo_id) REFERENCES catalogo_modelos (id) ON DELETE SET NULL,
    CONSTRAINT fk_equip_cliente       FOREIGN KEY (cliente_id)         REFERENCES clientes         (id) ON DELETE SET NULL,
    CONSTRAINT chk_equip_tipo         CHECK (tipo         IN ('EQUIPAMENTO', 'ACESSORIO', 'PECA', 'SERVICO')),
    CONSTRAINT chk_equip_status       CHECK (status       IN ('ATIVO', 'INATIVO', 'OBSOLETO')),
    CONSTRAINT chk_equip_proprietario CHECK (proprietario IN ('NOSSO', 'CLIENTE')),
    CONSTRAINT chk_equip_estado       CHECK (estado       IN ('DISPONIVEL', 'MANUTENCAO', 'DESCARTADO')),
    CONSTRAINT chk_equip_faixa        CHECK (faixa        IN ('VHF', 'UHF', 'DUAL_BAND'))
);

CREATE INDEX idx_equip_ns           ON equipamentos (numero_serie);
CREATE INDEX idx_equip_proprietario ON equipamentos (proprietario);
CREATE INDEX idx_equip_faixa        ON equipamentos (faixa);
CREATE INDEX idx_equip_estado       ON equipamentos (estado);
CREATE INDEX idx_equip_cliente      ON equipamentos (cliente_id);
CREATE INDEX idx_equip_catalogo     ON equipamentos (catalogo_modelo_id);

-- PK composta: um equipamento não pode ter a mesma chave duas vezes
CREATE TABLE equipamento_especificacoes (
    equipamento_id UUID        NOT NULL,
    chave          VARCHAR(50) NOT NULL,
    valor          VARCHAR(255),
    CONSTRAINT pk_equip_espec PRIMARY KEY (equipamento_id, chave),
    CONSTRAINT fk_equip_espec FOREIGN KEY (equipamento_id) REFERENCES equipamentos (id) ON DELETE CASCADE
);

CREATE TABLE acessorios (
    -- Colunas de ItemEstoque (herdadas)
    id                    UUID         NOT NULL DEFAULT gen_random_uuid(),
    data_criacao          TIMESTAMP    NOT NULL DEFAULT NOW(),
    data_atualizacao      TIMESTAMP,
    codigo                VARCHAR(50)  NOT NULL,
    descricao             VARCHAR(255) NOT NULL,
    tipo                  VARCHAR(20)  NOT NULL,
    catalogo_modelo_id    UUID,
    quantidade_disponivel INTEGER      NOT NULL DEFAULT 0,
    quantidade_minima     INTEGER,
    localizacao_fisica    VARCHAR(50),
    status                VARCHAR(20)  NOT NULL DEFAULT 'ATIVO',
    garantia_fim          DATE,
    observacoes           TEXT,
    codigo_cliente        VARCHAR(100),
    -- Colunas próprias de Acessorio
    numero_serie          VARCHAR(50),
    patrimonio            VARCHAR(50),
    tipo_acessorio        VARCHAR(30)  NOT NULL,
    proprietario          VARCHAR(20),
    cliente_id            UUID,
    estado                VARCHAR(20)  NOT NULL DEFAULT 'DISPONIVEL',
    CONSTRAINT pk_acessorios            PRIMARY KEY (id),
    CONSTRAINT uq_acessorios_codigo     UNIQUE (codigo),
    CONSTRAINT fk_acess_catalogo        FOREIGN KEY (catalogo_modelo_id) REFERENCES catalogo_modelos (id) ON DELETE SET NULL,
    CONSTRAINT fk_acess_cliente         FOREIGN KEY (cliente_id)         REFERENCES clientes         (id) ON DELETE SET NULL,
    CONSTRAINT chk_acess_tipo           CHECK (tipo            IN ('EQUIPAMENTO', 'ACESSORIO', 'PECA', 'SERVICO')),
    CONSTRAINT chk_acess_status         CHECK (status          IN ('ATIVO', 'INATIVO', 'OBSOLETO')),
    CONSTRAINT chk_acess_proprietario   CHECK (proprietario    IN ('NOSSO', 'CLIENTE')),
    CONSTRAINT chk_acess_tipo_acessorio CHECK (tipo_acessorio  IN ('BATERIA', 'ANTENA', 'BASE', 'FONTE', 'CAPA_COURO', 'CLIP_CINTO', 'FONE_OUVIDO', 'OUTRO')),
    CONSTRAINT chk_acess_estado         CHECK (estado          IN ('DISPONIVEL', 'MANUTENCAO', 'DESCARTADO'))
);

CREATE INDEX idx_acess_ns         ON acessorios (numero_serie);
CREATE INDEX idx_acess_patrimonio ON acessorios (patrimonio);
CREATE INDEX idx_acess_tipo       ON acessorios (tipo_acessorio);
CREATE INDEX idx_acess_cliente    ON acessorios (cliente_id);
CREATE INDEX idx_acess_catalogo   ON acessorios (catalogo_modelo_id);

CREATE TABLE pecas (
    -- Colunas de ItemEstoque (herdadas)
    id                    UUID         NOT NULL DEFAULT gen_random_uuid(),
    data_criacao          TIMESTAMP    NOT NULL DEFAULT NOW(),
    data_atualizacao      TIMESTAMP,
    codigo                VARCHAR(50)  NOT NULL,
    descricao             VARCHAR(255) NOT NULL,
    tipo                  VARCHAR(20)  NOT NULL,
    catalogo_modelo_id    UUID,
    quantidade_disponivel INTEGER      NOT NULL DEFAULT 0,
    quantidade_minima     INTEGER,
    localizacao_fisica    VARCHAR(50),
    status                VARCHAR(20)  NOT NULL DEFAULT 'ATIVO',
    observacoes           TEXT,
    codigo_cliente        VARCHAR(100),
    CONSTRAINT pk_pecas        PRIMARY KEY (id),
    CONSTRAINT uq_pecas_codigo UNIQUE (codigo),
    CONSTRAINT fk_peca_catalogo FOREIGN KEY (catalogo_modelo_id) REFERENCES catalogo_modelos (id) ON DELETE SET NULL,
    CONSTRAINT chk_peca_tipo   CHECK (tipo   IN ('EQUIPAMENTO', 'ACESSORIO', 'PECA', 'SERVICO')),
    CONSTRAINT chk_peca_status CHECK (status IN ('ATIVO', 'INATIVO', 'OBSOLETO'))
);

CREATE INDEX idx_peca_catalogo ON pecas (catalogo_modelo_id);
