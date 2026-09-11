-- =============================================================================
-- PEÇA: modelos de equipamento compatíveis
-- N:N entre Peca e CatalogoModelo -- ex: uma bateria compatível com o EP450,
-- o DEP450 e o CP200. Usado pra sugerir/filtrar peças por compatibilidade.
-- =============================================================================

CREATE TABLE peca_modelos_compativeis (
    peca_id            UUID NOT NULL,
    catalogo_modelo_id UUID NOT NULL,
    CONSTRAINT pk_peca_modelos_compativeis PRIMARY KEY (peca_id, catalogo_modelo_id),
    CONSTRAINT fk_pmc_peca            FOREIGN KEY (peca_id) REFERENCES pecas (id) ON DELETE CASCADE,
    CONSTRAINT fk_pmc_catalogo_modelo FOREIGN KEY (catalogo_modelo_id) REFERENCES catalogo_modelos (id) ON DELETE CASCADE
);

CREATE INDEX idx_pmc_catalogo_modelo ON peca_modelos_compativeis (catalogo_modelo_id);
