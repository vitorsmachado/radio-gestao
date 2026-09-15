-- =============================================================================
-- CLIENTE: histórico de transições de status
-- Registra cada ativação/inativação/bloqueio, com motivo opcional, para
-- auditar por que o status do cliente mudou. Nunca é atualizado após criado.
-- =============================================================================

CREATE TABLE cliente_status_historico (
    id               UUID         NOT NULL DEFAULT gen_random_uuid(),
    data_criacao     TIMESTAMP    NOT NULL DEFAULT NOW(),
    data_atualizacao TIMESTAMP,
    cliente_id       UUID         NOT NULL,
    status_anterior  VARCHAR(20)  NOT NULL,
    status_novo      VARCHAR(20)  NOT NULL,
    motivo           VARCHAR(500),
    CONSTRAINT pk_cliente_status_historico       PRIMARY KEY (id),
    CONSTRAINT fk_cliente_status_historico_cliente FOREIGN KEY (cliente_id) REFERENCES clientes (id) ON DELETE CASCADE,
    CONSTRAINT chk_cliente_status_historico_anterior CHECK (status_anterior IN ('ATIVO', 'INATIVO', 'BLOQUEADO')),
    CONSTRAINT chk_cliente_status_historico_novo      CHECK (status_novo IN ('ATIVO', 'INATIVO', 'BLOQUEADO'))
);

CREATE INDEX idx_cliente_status_historico_cliente ON cliente_status_historico (cliente_id);
