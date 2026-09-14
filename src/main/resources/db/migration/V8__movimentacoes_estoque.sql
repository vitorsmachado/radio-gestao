-- =============================================================================
-- HISTÓRICO DE MOVIMENTAÇÕES DE ESTOQUE
-- Registra cada entrada/saída/ajuste de quantidade em Acessório ou Peça, com
-- motivo opcional, permitindo auditar por que o saldo mudou. Nunca é
-- atualizado após criado.
-- =============================================================================

CREATE TABLE movimentacoes_estoque (
    id                UUID         NOT NULL DEFAULT gen_random_uuid(),
    data_criacao      TIMESTAMP    NOT NULL DEFAULT NOW(),
    data_atualizacao  TIMESTAMP,
    item_id           UUID         NOT NULL,
    tipo_item         VARCHAR(20)  NOT NULL,
    tipo_movimentacao VARCHAR(20)  NOT NULL,
    saldo_anterior    INTEGER      NOT NULL,
    saldo_novo        INTEGER      NOT NULL,
    motivo            VARCHAR(500),
    CONSTRAINT pk_movimentacoes_estoque  PRIMARY KEY (id),
    CONSTRAINT chk_mov_estoque_tipo_item CHECK (tipo_item IN ('EQUIPAMENTO', 'ACESSORIO', 'PECA', 'SERVICO')),
    CONSTRAINT chk_mov_estoque_tipo_mov  CHECK (tipo_movimentacao IN ('ENTRADA', 'SAIDA', 'AJUSTE'))
);

CREATE INDEX idx_mov_estoque_item ON movimentacoes_estoque (item_id);
