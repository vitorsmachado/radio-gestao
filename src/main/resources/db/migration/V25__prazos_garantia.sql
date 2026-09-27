ALTER TABLE configuracoes ADD COLUMN prazo_garantia_peca_dias INTEGER NOT NULL DEFAULT 90;
ALTER TABLE configuracoes ADD COLUMN prazo_garantia_equipamento_dias INTEGER NOT NULL DEFAULT 90;
ALTER TABLE configuracoes ADD COLUMN prazo_garantia_acessorio_dias INTEGER NOT NULL DEFAULT 90;
