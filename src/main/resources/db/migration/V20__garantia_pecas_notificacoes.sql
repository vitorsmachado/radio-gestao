-- =============================================================================
-- GARANTIA DE MANUTENÇÃO + NOTIFICAÇÕES
-- garantia_pecas: cobertura de garantia por peça trocada num reparo, por
-- equipamento/acessório do cliente (item_estoque_id). Se o mesmo equipamento
-- voltar com defeito na mesma peça dentro do prazo, o técnico já resolve sem
-- passar por orçamento (ver ItemEntradaDomainService.salvarAvaliacaoTecnica).
-- notificacoes: avisos genéricos pro admin agir — hoje só conflito de
-- garantia (equipamento em garantia de uma peça, mas defeito novo avaliado).
-- =============================================================================

CREATE TABLE garantia_pecas (
    id                     UUID      NOT NULL DEFAULT gen_random_uuid(),
    data_criacao           TIMESTAMP NOT NULL DEFAULT NOW(),
    data_atualizacao       TIMESTAMP,
    item_estoque_id        UUID      NOT NULL,
    peca_estoque_id        UUID      NOT NULL,
    descricao_peca         VARCHAR(255),
    data_inicio            DATE      NOT NULL,
    data_fim               DATE      NOT NULL,
    item_entrada_origem_id UUID      NOT NULL,
    CONSTRAINT pk_garantia_pecas PRIMARY KEY (id)
);

CREATE INDEX idx_garantia_peca_item_estoque ON garantia_pecas (item_estoque_id);

CREATE TABLE notificacoes (
    id               UUID          NOT NULL DEFAULT gen_random_uuid(),
    data_criacao     TIMESTAMP     NOT NULL DEFAULT NOW(),
    data_atualizacao TIMESTAMP,
    tipo             VARCHAR(30)   NOT NULL,
    titulo           VARCHAR(255)  NOT NULL,
    mensagem         VARCHAR(1000),
    link             VARCHAR(255),
    lida             BOOLEAN       NOT NULL DEFAULT FALSE,
    CONSTRAINT pk_notificacoes PRIMARY KEY (id),
    CONSTRAINT chk_notificacoes_tipo CHECK (tipo IN ('GARANTIA_CONFLITO'))
);

CREATE INDEX idx_notificacoes_lida ON notificacoes (lida);
