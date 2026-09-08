-- =============================================================================
-- MÓDULO: ORÇAMENTO
-- Envelope fino que agrupa ItemEntrada de uma mesma OS para apresentar uma
-- proposta formal ao cliente. Aprovação/rejeição continua por item — aqui só
-- fica o que é do envelope (validade, condições, desconto).
-- =============================================================================

CREATE TABLE orcamentos (
    id                   UUID         NOT NULL DEFAULT uuid_generate_v4(),
    data_criacao         TIMESTAMP    NOT NULL DEFAULT NOW(),
    data_atualizacao     TIMESTAMP,
    numero               VARCHAR(20)  NOT NULL,
    os_id                UUID         NOT NULL,
    cliente_id           UUID         NOT NULL,
    validade             DATE,
    condicoes_pagamento  VARCHAR(500),
    desconto             NUMERIC(15, 2) NOT NULL DEFAULT 0,
    status               VARCHAR(20)  NOT NULL DEFAULT 'RASCUNHO',
    data_emissao         TIMESTAMP    NOT NULL DEFAULT NOW(),
    observacoes          VARCHAR(2000),
    CONSTRAINT pk_orcamentos           PRIMARY KEY (id),
    CONSTRAINT uq_orcamentos_numero    UNIQUE (numero),
    CONSTRAINT fk_orcamento_os         FOREIGN KEY (os_id) REFERENCES ordens_servico (id) ON DELETE RESTRICT,
    CONSTRAINT fk_orcamento_cliente    FOREIGN KEY (cliente_id) REFERENCES clientes (id) ON DELETE RESTRICT,
    CONSTRAINT chk_orcamento_status    CHECK (status IN ('RASCUNHO', 'ENVIADO', 'CANCELADO'))
);

CREATE INDEX idx_orcamento_os      ON orcamentos (os_id);
CREATE INDEX idx_orcamento_cliente ON orcamentos (cliente_id);

-- os_itens_entrada.orcamento_id já existia (V4) sem FK, pois orcamentos ainda
-- não existia. ON DELETE SET NULL: se um orçamento for removido, o item só
-- perde o agrupamento, sem quebrar a integridade.
ALTER TABLE os_itens_entrada
    ADD CONSTRAINT fk_item_entrada_orcamento
    FOREIGN KEY (orcamento_id) REFERENCES orcamentos (id) ON DELETE SET NULL;
