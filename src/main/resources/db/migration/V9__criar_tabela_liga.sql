-- Sequence da Liga
CREATE SEQUENCE liga_id_seq START WITH 1 INCREMENT BY 1;

-- Tabela da Liga
CREATE TABLE liga (
    id BIGINT DEFAULT nextval('liga_id_seq') PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    descricao VARCHAR(255),
    data_criacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    id_time_admin BIGINT NOT NULL,
    CONSTRAINT fk_liga_admin FOREIGN KEY (id_time_admin) REFERENCES time(id)
);

-- Tabela de relacionamento (times na liga)
CREATE TABLE time_liga (
    time_id BIGINT NOT NULL,
    liga_id BIGINT NOT NULL,
    data_entrada TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (time_id, liga_id),
    CONSTRAINT fk_tl_time FOREIGN KEY (time_id) REFERENCES time(id),
    CONSTRAINT fk_tl_liga FOREIGN KEY (liga_id) REFERENCES liga(id)
);