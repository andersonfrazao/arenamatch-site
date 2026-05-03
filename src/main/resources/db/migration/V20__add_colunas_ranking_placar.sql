-- 1. Adicionando estatísticas de Ranking na tabela 'time'
ALTER TABLE time 
ADD COLUMN pontos INTEGER NOT NULL DEFAULT 0,
ADD COLUMN partidas_jogadas INTEGER NOT NULL DEFAULT 0,
ADD COLUMN vitorias INTEGER NOT NULL DEFAULT 0,
ADD COLUMN empates INTEGER NOT NULL DEFAULT 0,
ADD COLUMN derrotas INTEGER NOT NULL DEFAULT 0,
ADD COLUMN gols_pro INTEGER NOT NULL DEFAULT 0,
ADD COLUMN gols_contra INTEGER NOT NULL DEFAULT 0;

-- 2. Adicionando controle de Placar na tabela 'partida'
ALTER TABLE partida 
ADD COLUMN gols_mandante INTEGER,
ADD COLUMN gols_visitante INTEGER,
ADD COLUMN status_placar VARCHAR(30) NOT NULL DEFAULT 'PENDENTE',
ADD COLUMN id_time_que_informou BIGINT;

-- 3. (Opcional, mas recomendado) Adicionando a restrição de chave estrangeira 
-- para garantir que o id_time_que_informou realmente existe na tabela time.
ALTER TABLE partida
ADD CONSTRAINT fk_partida_time_informou
FOREIGN KEY (id_time_que_informou) REFERENCES time(id);