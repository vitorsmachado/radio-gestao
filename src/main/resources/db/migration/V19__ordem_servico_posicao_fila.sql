-- =============================================================================
-- ORDEM DE SERVIÇO: troca prioridade por posição manual na fila
-- Prioridade (nível fixo, só admin) virou reordenação manual (setas/arrastar,
-- também só admin) — posicao_fila só tem sentido como critério de desempate
-- entre OS do mesmo bloco da fila de manutenção; renumerada a cada reordenação.
-- =============================================================================

ALTER TABLE ordens_servico DROP CONSTRAINT IF EXISTS chk_os_prioridade;
ALTER TABLE ordens_servico DROP COLUMN IF EXISTS prioridade;

ALTER TABLE ordens_servico ADD COLUMN posicao_fila BIGINT NOT NULL DEFAULT 0;
