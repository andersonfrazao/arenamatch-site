-- Adiciona a coluna celular na tabela usuario
ALTER TABLE usuario ADD COLUMN celular VARCHAR(20);
ALTER TABLE usuario ALTER COLUMN cpf TYPE VARCHAR(11);
UPDATE usuario SET cpf = '00000000000' WHERE cpf IS NULL;
ALTER TABLE usuario ALTER COLUMN cpf SET NOT NULL;
ALTER TABLE usuario ADD CONSTRAINT usuario_cpf_unique UNIQUE (cpf);