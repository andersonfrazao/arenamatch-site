-- 1. Criação da Sequence
CREATE SEQUENCE seq_usuario
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- 2. Criação da Tabela usando a Sequence
CREATE TABLE usuario (
    id BIGINT DEFAULT nextval('seq_usuario') PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    senha VARCHAR(255) NOT NULL,
    perfil VARCHAR(20) NOT NULL,
    
    -- Campos de Monetização/Trial
    status_assinatura VARCHAR(20) DEFAULT 'TRIAL',
    data_expiracao TIMESTAMP,
    
    data_cadastro TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 3. Insert do Admin (Usando a sequence explicitamente)
INSERT INTO usuario (id, nome, email, senha, perfil, status_assinatura, data_expiracao) 
VALUES (nextval('seq_usuario'), 'Admin Arena', 'admin@arena.com', '123456', 'ADMIN', 'ATIVO', '2099-12-31 23:59:59');