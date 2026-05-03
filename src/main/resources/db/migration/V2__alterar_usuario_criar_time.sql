-- 1. Alteração na Tabela Usuário (Adicionar CPF)
ALTER TABLE usuario ADD COLUMN cpf VARCHAR(14);

-- Garante que não teremos dois cadastros com o mesmo CPF
ALTER TABLE usuario ADD CONSTRAINT uk_usuario_cpf UNIQUE (cpf);

-- 2. Criação da Tabela Time (A mesma de antes)
CREATE SEQUENCE seq_time START WITH 1 INCREMENT BY 1;

CREATE TABLE time (
    id BIGINT DEFAULT nextval('seq_time') PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    categoria VARCHAR(50),
    
    cep VARCHAR(10),
    logradouro VARCHAR(150),
    bairro VARCHAR(100),
    cidade VARCHAR(100),
    uf VARCHAR(2),
    mando_campo BOOLEAN DEFAULT FALSE,
    
    id_responsavel BIGINT NOT NULL,
    data_cadastro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (id_responsavel) REFERENCES usuario(id)
);