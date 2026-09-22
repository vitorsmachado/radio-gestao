-- =============================================================================
-- ITEM DE ENTRADA: quantidade
-- Permite registrar um acessório não rastreado individualmente (sem N/S nem
-- patrimônio) como uma única linha com quantidade > 1, em vez de uma linha
-- por unidade. Equipamento e item rastreado por N/S continuam com quantidade 1.
-- =============================================================================

ALTER TABLE os_itens_entrada ADD COLUMN quantidade INTEGER NOT NULL DEFAULT 1;
