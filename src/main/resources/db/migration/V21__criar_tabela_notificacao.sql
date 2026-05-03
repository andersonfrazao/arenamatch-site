CREATE TABLE notificacao (
    id BIGSERIAL PRIMARY KEY,
    time_id BIGINT NOT NULL,
    tipo VARCHAR(50) NOT NULL, -- 'JOGO', 'LIGA', 'PLACAR'
    id_referencia BIGINT NOT NULL, -- Guarda o ID da Partida ou do ConviteLiga
    titulo VARCHAR(150) NOT NULL,
    subtitulo VARCHAR(255),
    data_criacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    lida BOOLEAN NOT NULL DEFAULT FALSE,
    
    -- Chave estrangeira para garantir que a notificação pertence a um time real
    CONSTRAINT fk_notificacao_time FOREIGN KEY (time_id) REFERENCES time(id)
);

-- Criar um índice para deixar a busca do sininho super rápida
CREATE INDEX idx_notificacao_time ON notificacao(time_id, data_criacao DESC);