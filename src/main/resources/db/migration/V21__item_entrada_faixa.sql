-- =============================================================================
-- FAIXA DO EQUIPAMENTO NO ITEM DE ENTRADA
-- Só relevante quando tipo_item = EQUIPAMENTO (rádio) — mesmo enum já usado
-- em com.radiocom.estoque.domain.model.enums.FaixaEquipamento.
-- =============================================================================

ALTER TABLE os_itens_entrada ADD COLUMN faixa VARCHAR(20);

ALTER TABLE os_itens_entrada ADD CONSTRAINT chk_item_entrada_faixa
    CHECK (faixa IS NULL OR faixa IN ('VHF', 'UHF', 'DUAL_BAND'));
