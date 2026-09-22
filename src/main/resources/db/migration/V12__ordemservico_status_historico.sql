-- =============================================================================
-- ORDEM DE SERVIÇO / ITEM DE ENTRADA: histórico de transições de status
-- Mesmo padrão de cliente_status_historico (V11) — registro imutável de cada
-- mudança de status, com motivo opcional, para auditar a jornada da OS/item.
-- =============================================================================

CREATE TABLE ordem_servico_status_historico (
    id               UUID         NOT NULL DEFAULT gen_random_uuid(),
    data_criacao     TIMESTAMP    NOT NULL DEFAULT NOW(),
    data_atualizacao TIMESTAMP,
    ordem_servico_id UUID         NOT NULL,
    status_anterior  VARCHAR(20)  NOT NULL,
    status_novo      VARCHAR(20)  NOT NULL,
    motivo           VARCHAR(500),
    CONSTRAINT pk_ordem_servico_status_historico PRIMARY KEY (id),
    CONSTRAINT fk_os_status_historico_os FOREIGN KEY (ordem_servico_id) REFERENCES ordens_servico (id) ON DELETE CASCADE,
    CONSTRAINT chk_os_status_historico_anterior CHECK (status_anterior IN ('ABERTA', 'EM_ANDAMENTO', 'CONCLUIDA', 'CANCELADA')),
    CONSTRAINT chk_os_status_historico_novo      CHECK (status_novo IN ('ABERTA', 'EM_ANDAMENTO', 'CONCLUIDA', 'CANCELADA'))
);

CREATE INDEX idx_os_status_historico_os ON ordem_servico_status_historico (ordem_servico_id);

CREATE TABLE item_entrada_status_historico (
    id               UUID         NOT NULL DEFAULT gen_random_uuid(),
    data_criacao     TIMESTAMP    NOT NULL DEFAULT NOW(),
    data_atualizacao TIMESTAMP,
    item_entrada_id  UUID         NOT NULL,
    status_anterior  VARCHAR(20)  NOT NULL,
    status_novo      VARCHAR(20)  NOT NULL,
    motivo           VARCHAR(500),
    CONSTRAINT pk_item_entrada_status_historico PRIMARY KEY (id),
    CONSTRAINT fk_item_status_historico_item FOREIGN KEY (item_entrada_id) REFERENCES os_itens_entrada (id) ON DELETE CASCADE,
    CONSTRAINT chk_item_status_historico_anterior CHECK (status_anterior IN (
        'PENDENTE_AVALIACAO', 'AVALIADO', 'PENDENTE_AUTORIZACAO', 'AUTORIZADO', 'NAO_AUTORIZADO',
        'PENDENTE_MANUTENCAO', 'AGUARDANDO_PECA', 'EM_MANUTENCAO', 'MANUTENCAO_CONCLUIDA',
        'AGUARDANDO_ENTREGA', 'ENTREGUE'
    )),
    CONSTRAINT chk_item_status_historico_novo CHECK (status_novo IN (
        'PENDENTE_AVALIACAO', 'AVALIADO', 'PENDENTE_AUTORIZACAO', 'AUTORIZADO', 'NAO_AUTORIZADO',
        'PENDENTE_MANUTENCAO', 'AGUARDANDO_PECA', 'EM_MANUTENCAO', 'MANUTENCAO_CONCLUIDA',
        'AGUARDANDO_ENTREGA', 'ENTREGUE'
    ))
);

CREATE INDEX idx_item_status_historico_item ON item_entrada_status_historico (item_entrada_id);
