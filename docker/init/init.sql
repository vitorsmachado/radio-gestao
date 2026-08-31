-- =============================================================================
-- radio-gestao — Script de inicialização do banco de desenvolvimento
-- Executado automaticamente pelo PostgreSQL na primeira criação do container.
-- NÃO é executado em containers já existentes (dados preservados no volume).
-- =============================================================================

-- Extensão para geração de UUID (necessária para @UuidGenerator do Hibernate)
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

SET timezone = 'America/Sao_Paulo';

-- O schema e as tabelas são criados pelo Flyway na inicialização da aplicação.
-- Este script existe apenas para garantir as extensões e configurações base.
