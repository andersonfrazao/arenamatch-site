CREATE TABLE mensagem_chat_liga (
    id BIGSERIAL PRIMARY KEY,
    id_liga BIGINT NOT NULL,
    id_time_remetente BIGINT NOT NULL,
    texto VARCHAR(500) NOT NULL,
    data_hora TIMESTAMP NOT NULL,
    lida BOOLEAN NOT NULL DEFAULT FALSE,
    
    CONSTRAINT fk_msg_liga_liga FOREIGN KEY (id_liga) REFERENCES liga (id),
    CONSTRAINT fk_msg_liga_remetente FOREIGN KEY (id_time_remetente) REFERENCES time (id)
);