-- =============================================================================
-- ITEM DE ENTRADA: vínculo com o catálogo de modelos
-- Liga o item ao modelo de catálogo escolhido na entrada (sugestão, não
-- obrigatório) — usado para trazer o valor de referência (preço de um novo)
-- quando o item não tem conserto, e no futuro para filtrar OS por modelo em
-- relatórios.
-- =============================================================================

ALTER TABLE os_itens_entrada ADD COLUMN catalogo_modelo_id UUID;

ALTER TABLE os_itens_entrada
    ADD CONSTRAINT fk_item_entrada_catalogo_modelo
    FOREIGN KEY (catalogo_modelo_id) REFERENCES catalogo_modelos (id) ON DELETE SET NULL;

CREATE INDEX idx_item_entrada_catalogo_modelo ON os_itens_entrada (catalogo_modelo_id);
