-- =============================================================================
-- SUGESTÕES DE TEXTO
-- Valores já digitados antes nos campos de laudo técnico (defeito encontrado,
-- causa, solução recomendada, observações), reaproveitáveis como sugestão de
-- autocomplete. Um registro por (campo, valor) — contagem_uso cresce a cada
-- vez que o mesmo texto é salvo de novo, usada pra ordenar as sugestões mais
-- usadas primeiro.
-- =============================================================================

CREATE TABLE sugestoes_texto (
    id               UUID          NOT NULL DEFAULT gen_random_uuid(),
    data_criacao     TIMESTAMP     NOT NULL DEFAULT NOW(),
    data_atualizacao TIMESTAMP,
    campo            VARCHAR(30)   NOT NULL,
    valor            VARCHAR(1000) NOT NULL,
    contagem_uso     INTEGER       NOT NULL DEFAULT 1,
    CONSTRAINT pk_sugestoes_texto PRIMARY KEY (id),
    CONSTRAINT uq_sugestoes_texto_campo_valor UNIQUE (campo, valor),
    CONSTRAINT chk_sugestoes_texto_campo CHECK (campo IN (
        'DEFEITO_ENCONTRADO', 'CAUSA_DEFEITO', 'SOLUCAO_RECOMENDADA', 'OBSERVACOES_TECNICAS'
    ))
);

CREATE INDEX idx_sugestoes_texto_campo ON sugestoes_texto (campo);
