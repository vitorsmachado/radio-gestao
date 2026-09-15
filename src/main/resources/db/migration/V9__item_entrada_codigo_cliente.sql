-- =============================================================================
-- ITEM DE ENTRADA: código próprio do cliente para o item
-- Identificação/tag que o próprio cliente usa internamente pro equipamento
-- (distinto do nosso patrimônio interno) — usado como critério de busca de OS.
-- =============================================================================

ALTER TABLE os_itens_entrada ADD COLUMN codigo_cliente VARCHAR(100);
