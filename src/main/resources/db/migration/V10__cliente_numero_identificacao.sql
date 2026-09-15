-- =============================================================================
-- CLIENTE: número de identificação
-- Número interno (não o documento) atribuído ao cliente no cadastro,
-- editável depois. Usado como ordenação padrão da listagem.
-- =============================================================================

ALTER TABLE clientes ADD COLUMN numero_identificacao INTEGER;

CREATE SEQUENCE IF NOT EXISTS cliente_numero_seq START WITH 1 INCREMENT BY 1;

-- Backfill dos clientes já cadastrados, em ordem de criação.
WITH numerados AS (
    SELECT id, ROW_NUMBER() OVER (ORDER BY data_criacao) AS rn FROM clientes
)
UPDATE clientes c SET numero_identificacao = numerados.rn
FROM numerados WHERE c.id = numerados.id;

-- Só ajusta a sequence se já existir cliente (setval não aceita 0 — a sequence
-- já nasce em 1, então numa base vazia não há nada a ajustar).
SELECT setval('cliente_numero_seq', m.maximo)
FROM (SELECT MAX(numero_identificacao) AS maximo FROM clientes) m
WHERE m.maximo IS NOT NULL;

ALTER TABLE clientes ALTER COLUMN numero_identificacao SET NOT NULL;
ALTER TABLE clientes ADD CONSTRAINT uq_clientes_numero_identificacao UNIQUE (numero_identificacao);
