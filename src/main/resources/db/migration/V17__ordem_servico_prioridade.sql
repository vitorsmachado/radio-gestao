-- =============================================================================
-- ORDEM DE SERVIÇO: prioridade na fila de manutenção
-- Só o admin altera — desempata a ordem dentro de cada bloco de status na
-- fila de manutenção do técnico.
-- =============================================================================

ALTER TABLE ordens_servico ADD COLUMN prioridade VARCHAR(20) NOT NULL DEFAULT 'NORMAL';
ALTER TABLE ordens_servico ADD CONSTRAINT chk_os_prioridade CHECK (prioridade IN ('BAIXA', 'NORMAL', 'ALTA', 'URGENTE'));
