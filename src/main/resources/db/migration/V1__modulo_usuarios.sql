-- =============================================================================
-- MÓDULO: USUÁRIOS
-- =============================================================================

CREATE TABLE usuarios (
    id               UUID         NOT NULL DEFAULT uuid_generate_v4(),
    data_criacao     TIMESTAMP    NOT NULL DEFAULT NOW(),
    data_atualizacao TIMESTAMP,
    nome             VARCHAR(100) NOT NULL,
    email            VARCHAR(150) NOT NULL,
    login            VARCHAR(150) NOT NULL,
    senha_hash       TEXT         NOT NULL,
    role             VARCHAR(20)  NOT NULL,
    ativo            BOOLEAN      NOT NULL DEFAULT TRUE,
    CONSTRAINT pk_usuarios       PRIMARY KEY (id),
    CONSTRAINT uq_usuarios_email UNIQUE (email),
    CONSTRAINT uq_usuarios_login UNIQUE (login),
    CONSTRAINT chk_usuarios_role CHECK (role IN ('ADMIN', 'TECNICO', 'AUXILIAR'))
);

-- uq_usuarios_email e uq_usuarios_login já criam índices implícitos
CREATE INDEX idx_usuario_role ON usuarios (role);

-- Usuário ADMIN inicial — senha: Admin@123
-- Hash BCrypt gerado com strength 10
-- TROCAR A SENHA NO PRIMEIRO LOGIN!
INSERT INTO usuarios (id, nome, email, login, senha_hash, role, ativo)
VALUES (
    uuid_generate_v4(),
    'Administrador',
    'admin@radiogestao.com.br',
    'admin',
    '$2a$10$ki1XUxalaHg6ZqrGTd6xJuPofl/QAgXQHxwYdaXxY36XkbGfTFICe',
    'ADMIN',
    TRUE
);
