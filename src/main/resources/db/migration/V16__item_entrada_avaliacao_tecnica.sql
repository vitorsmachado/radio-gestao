-- =============================================================================
-- ITEM DE ENTRADA: avaliação técnica estruturada
-- Campos que a tela do técnico preenche (resultado, ajuste, laudo detalhado)
-- e o marcador de confirmação de "aguardando peça" (usado pela fila de
-- manutenção pra mandar o item pro final e trazer de volta como próximo).
-- =============================================================================

ALTER TABLE os_itens_entrada ADD COLUMN resultado_avaliacao VARCHAR(20);
ALTER TABLE os_itens_entrada ADD CONSTRAINT chk_item_entrada_resultado_avaliacao
    CHECK (resultado_avaliacao IN ('AJUSTE', 'ORCAMENTO', 'SEM_DEFEITO', 'SEM_CONSERTO'));

ALTER TABLE os_itens_entrada ADD COLUMN detalhe_ajuste       VARCHAR(500);
ALTER TABLE os_itens_entrada ADD COLUMN defeito_encontrado   VARCHAR(1000);
ALTER TABLE os_itens_entrada ADD COLUMN causa_defeito        VARCHAR(1000);
ALTER TABLE os_itens_entrada ADD COLUMN solucao_recomendada  VARCHAR(1000);
ALTER TABLE os_itens_entrada ADD COLUMN observacoes_tecnicas VARCHAR(1000);
ALTER TABLE os_itens_entrada ADD COLUMN confirmado_aguardando_peca_em TIMESTAMP;
