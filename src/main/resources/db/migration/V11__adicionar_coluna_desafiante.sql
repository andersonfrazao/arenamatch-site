ALTER TABLE partida ADD COLUMN id_desafiante BIGINT;
ALTER TABLE partida ADD CONSTRAINT fk_partida_desafiante FOREIGN KEY (id_desafiante) REFERENCES time(id);