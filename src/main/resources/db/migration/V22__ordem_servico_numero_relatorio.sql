-- =============================================================================
-- NÚMERO DO RELATÓRIO MANUAL
-- Relatório preenchido à mão na retirada física dos itens do cliente,
-- fora do sistema — só um campo de referência pra localizar esse papel.
-- =============================================================================

ALTER TABLE ordens_servico ADD COLUMN numero_relatorio VARCHAR(100);
