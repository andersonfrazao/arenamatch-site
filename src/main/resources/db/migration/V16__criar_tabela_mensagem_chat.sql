CREATE TABLE mensagem_chat (
    id BIGSERIAL PRIMARY KEY,
    id_partida BIGINT NOT NULL,
    id_time_remetente BIGINT NOT NULL,
    texto VARCHAR(500) NOT NULL,
    data_hora TIMESTAMP NOT NULL,
    
    CONSTRAINT fk_mensagem_partida FOREIGN KEY (id_partida) REFERENCES partida (id),
    CONSTRAINT fk_mensagem_time_remetente FOREIGN KEY (id_time_remetente) REFERENCES time (id)
);