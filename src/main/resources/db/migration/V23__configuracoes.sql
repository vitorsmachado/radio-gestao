-- =============================================================================
-- CONFIGURAÇÕES DO SISTEMA
-- Linha única (singleton) — pensada pra crescer conforme surgem outros
-- valores configuráveis, sem precisar de tabela/tela nova pra cada um.
-- =============================================================================

CREATE TABLE configuracoes (
    id                       UUID          NOT NULL DEFAULT gen_random_uuid(),
    data_criacao             TIMESTAMP     NOT NULL DEFAULT NOW(),
    data_atualizacao         TIMESTAMP,
    valor_mao_de_obra_padrao NUMERIC(15,2) NOT NULL DEFAULT 0,
    CONSTRAINT pk_configuracoes PRIMARY KEY (id)
);

INSERT INTO configuracoes (valor_mao_de_obra_padrao) VALUES (0);
