-- =============================================================================
-- CATÁLOGO: valor de referência
-- Preço sugerido por modelo, usado para pré-preencher o valor ao adicionar o
-- item num orçamento — não é preço de venda nem de locação (módulos que ainda
-- não existem).
-- =============================================================================

ALTER TABLE catalogo_modelos
    ADD COLUMN valor_referencia NUMERIC(15, 2);
