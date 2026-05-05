ALTER TABLE usuario ADD COLUMN status_usuario VARCHAR(30) NOT NULL DEFAULT 'ATIVO';
ALTER TABLE usuario ADD COLUMN codigo_ativacao_email VARCHAR(5);
ALTER TABLE usuario ADD COLUMN validade_codigo_ativacao_email TIMESTAMP;
