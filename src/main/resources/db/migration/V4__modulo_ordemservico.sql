-- =============================================================================
-- MÓDULO: ORDEM DE SERVIÇO
-- Envelope simples (numero, cliente, técnico, status agregado) — o progresso
-- fino mora em cada item de entrada, não na OS.
-- =============================================================================

CREATE TABLE ordens_servico (
    id               UUID         NOT NULL DEFAULT uuid_generate_v4(),
    data_criacao     TIMESTAMP    NOT NULL DEFAULT NOW(),
    data_atualizacao TIMESTAMP,
    numero           VARCHAR(20)  NOT NULL,
    cliente_id       UUID         NOT NULL,
    posto_id         UUID,
    tecnico_id       UUID,
    solicitante      VARCHAR(100),
    recebedor_nome   VARCHAR(100),
    status           VARCHAR(20)  NOT NULL DEFAULT 'ABERTA',
    data_abertura    TIMESTAMP    NOT NULL DEFAULT NOW(),
    data_conclusao   TIMESTAMP,
    observacoes      VARCHAR(2000),
    CONSTRAINT pk_ordens_servico    PRIMARY KEY (id),
    CONSTRAINT uq_ordens_servico_numero UNIQUE (numero),
    CONSTRAINT fk_os_cliente        FOREIGN KEY (cliente_id) REFERENCES clientes (id) ON DELETE RESTRICT,
    CONSTRAINT fk_os_posto          FOREIGN KEY (posto_id)   REFERENCES postos   (id) ON DELETE SET NULL,
    CONSTRAINT chk_os_status        CHECK (status IN ('ABERTA', 'EM_ANDAMENTO', 'CONCLUIDA', 'CANCELADA'))
);

CREATE INDEX idx_os_cliente ON ordens_servico (cliente_id);
CREATE INDEX idx_os_status  ON ordens_servico (status);

CREATE TABLE os_itens_entrada (
    id                   UUID         NOT NULL DEFAULT uuid_generate_v4(),
    data_criacao         TIMESTAMP    NOT NULL DEFAULT NOW(),
    data_atualizacao     TIMESTAMP,
    os_id                UUID         NOT NULL,
    orcamento_id         UUID,
    item_estoque_id      UUID,
    tipo_item            VARCHAR(20)  NOT NULL,
    descricao            VARCHAR(255) NOT NULL,
    numero_serie         VARCHAR(50),
    patrimonio           VARCHAR(50),
    marca                VARCHAR(100),
    modelo               VARCHAR(100),
    defeito_relatado     VARCHAR(1000),
    avaliacao_tecnica    VARCHAR(1000),
    sem_defeito          BOOLEAN      NOT NULL DEFAULT FALSE,
    garantia             BOOLEAN      NOT NULL DEFAULT FALSE,
    status               VARCHAR(20)  NOT NULL DEFAULT 'PENDENTE_AVALIACAO',
    motivo_nao_autorizado VARCHAR(500),
    CONSTRAINT pk_os_itens_entrada PRIMARY KEY (id),
    CONSTRAINT fk_item_entrada_os  FOREIGN KEY (os_id) REFERENCES ordens_servico (id) ON DELETE CASCADE,
    CONSTRAINT chk_item_entrada_tipo   CHECK (tipo_item IN ('EQUIPAMENTO', 'ACESSORIO', 'PECA', 'SERVICO')),
    CONSTRAINT chk_item_entrada_status CHECK (status IN (
        'PENDENTE_AVALIACAO', 'AVALIADO', 'PENDENTE_AUTORIZACAO', 'AUTORIZADO', 'NAO_AUTORIZADO',
        'PENDENTE_MANUTENCAO', 'AGUARDANDO_PECA', 'EM_MANUTENCAO', 'MANUTENCAO_CONCLUIDA',
        'AGUARDANDO_ENTREGA', 'ENTREGUE'
    ))
);

CREATE INDEX idx_item_entrada_os        ON os_itens_entrada (os_id);
CREATE INDEX idx_item_entrada_orcamento ON os_itens_entrada (orcamento_id);
CREATE INDEX idx_item_entrada_status    ON os_itens_entrada (status);

CREATE TABLE os_itens_conserto (
    id               UUID         NOT NULL DEFAULT uuid_generate_v4(),
    data_criacao     TIMESTAMP    NOT NULL DEFAULT NOW(),
    data_atualizacao TIMESTAMP,
    item_entrada_id  UUID         NOT NULL,
    tipo             VARCHAR(20)  NOT NULL,
    item_estoque_id  UUID,
    descricao        VARCHAR(255),
    quantidade       INTEGER      NOT NULL,
    valor_unitario   NUMERIC(15, 2) NOT NULL,
    valor_total      NUMERIC(15, 2) NOT NULL,
    CONSTRAINT pk_os_itens_conserto PRIMARY KEY (id),
    CONSTRAINT fk_item_conserto_entrada FOREIGN KEY (item_entrada_id) REFERENCES os_itens_entrada (id) ON DELETE CASCADE,
    CONSTRAINT chk_item_conserto_tipo CHECK (tipo IN ('PECA', 'MAO_DE_OBRA', 'DESLOCAMENTO'))
);

CREATE INDEX idx_item_conserto_entrada ON os_itens_conserto (item_entrada_id);
