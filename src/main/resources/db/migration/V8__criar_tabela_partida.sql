CREATE SEQUENCE seq_partida START WITH 1 INCREMENT BY 1;

CREATE TABLE partida (
    id BIGINT DEFAULT nextval('seq_partida') PRIMARY KEY,
    
    id_mandante BIGINT NOT NULL,
    id_visitante BIGINT NOT NULL,
    
    data_hora TIMESTAMP NOT NULL,
    
    status VARCHAR(50) NOT NULL, -- AGENDADO, SOLICITACAO_CANCELAMENTO, CANCELADO
    
    -- Campos para o fluxo de cancelamento
    id_solicitante_cancelamento BIGINT, -- Quem pediu pra cancelar?
    motivo_cancelamento TEXT,
    data_solicitacao TIMESTAMP,
    
    FOREIGN KEY (id_mandante) REFERENCES time(id),
    FOREIGN KEY (id_visitante) REFERENCES time(id),
    FOREIGN KEY (id_solicitante_cancelamento) REFERENCES time(id)
);