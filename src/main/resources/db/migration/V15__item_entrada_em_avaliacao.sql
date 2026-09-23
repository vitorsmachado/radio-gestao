-- =============================================================================
-- ITEM DE ENTRADA: novo status EM_AVALIACAO
-- Entra entre PENDENTE_AVALIACAO e AVALIADO — o técnico "reivindica" o item
-- ao abrir a tela de avaliação, antes de salvar. Precisa atualizar as CHECK
-- constraints que listam os valores válidos de status: a da própria tabela
-- e as duas do histórico de status (status_anterior/status_novo).
-- =============================================================================

ALTER TABLE os_itens_entrada DROP CONSTRAINT chk_item_entrada_status;
ALTER TABLE os_itens_entrada ADD CONSTRAINT chk_item_entrada_status CHECK (status IN (
    'PENDENTE_AVALIACAO', 'EM_AVALIACAO', 'AVALIADO', 'PENDENTE_AUTORIZACAO', 'AUTORIZADO', 'NAO_AUTORIZADO',
    'PENDENTE_MANUTENCAO', 'AGUARDANDO_PECA', 'EM_MANUTENCAO', 'MANUTENCAO_CONCLUIDA',
    'AGUARDANDO_ENTREGA', 'ENTREGUE'
));

ALTER TABLE item_entrada_status_historico DROP CONSTRAINT chk_item_status_historico_anterior;
ALTER TABLE item_entrada_status_historico ADD CONSTRAINT chk_item_status_historico_anterior CHECK (status_anterior IN (
    'PENDENTE_AVALIACAO', 'EM_AVALIACAO', 'AVALIADO', 'PENDENTE_AUTORIZACAO', 'AUTORIZADO', 'NAO_AUTORIZADO',
    'PENDENTE_MANUTENCAO', 'AGUARDANDO_PECA', 'EM_MANUTENCAO', 'MANUTENCAO_CONCLUIDA',
    'AGUARDANDO_ENTREGA', 'ENTREGUE'
));

ALTER TABLE item_entrada_status_historico DROP CONSTRAINT chk_item_status_historico_novo;
ALTER TABLE item_entrada_status_historico ADD CONSTRAINT chk_item_status_historico_novo CHECK (status_novo IN (
    'PENDENTE_AVALIACAO', 'EM_AVALIACAO', 'AVALIADO', 'PENDENTE_AUTORIZACAO', 'AUTORIZADO', 'NAO_AUTORIZADO',
    'PENDENTE_MANUTENCAO', 'AGUARDANDO_PECA', 'EM_MANUTENCAO', 'MANUTENCAO_CONCLUIDA',
    'AGUARDANDO_ENTREGA', 'ENTREGUE'
));
