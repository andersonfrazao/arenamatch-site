-- Sequence de Convite
CREATE SEQUENCE convite_liga_id_seq START WITH 1 INCREMENT BY 1;

-- Tabela de Convites para Liga
CREATE TABLE convite_liga (
    id BIGINT DEFAULT nextval('convite_liga_id_seq') PRIMARY KEY,
    liga_id BIGINT NOT NULL,
    time_id BIGINT NOT NULL,
    mensagem VARCHAR(255),
    status VARCHAR(20) NOT NULL,
    data_convite TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_cl_liga FOREIGN KEY (liga_id) REFERENCES liga(id),
    CONSTRAINT fk_cl_time FOREIGN KEY (time_id) REFERENCES time(id)
);