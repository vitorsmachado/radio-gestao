ALTER TABLE configuracoes ADD COLUMN nome_empresa VARCHAR(255) NOT NULL DEFAULT 'Teletrom';
ALTER TABLE configuracoes ADD COLUMN documento_empresa VARCHAR(14) NOT NULL DEFAULT '59273032000103';
ALTER TABLE configuracoes ADD COLUMN inscricao_estadual_empresa VARCHAR(20);
ALTER TABLE configuracoes ADD COLUMN endereco_empresa VARCHAR(500);
ALTER TABLE configuracoes ADD COLUMN telefone_empresa VARCHAR(20);

UPDATE configuracoes SET inscricao_estadual_empresa = '0836629500144' WHERE inscricao_estadual_empresa IS NULL;
