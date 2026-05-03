CREATE TABLE IF NOT EXISTS parametro_sistema (
    chave VARCHAR(80) PRIMARY KEY,
    valor VARCHAR(255) NOT NULL,
    descricao VARCHAR(255)
);

INSERT INTO parametro_sistema (chave, valor, descricao)
VALUES (
    'MIN_DIAS_ANTECEDENCIA_AGENDAMENTO',
    '3',
    'Quantidade minima de dias de antecedencia para marcar um jogo.'
)
ON CONFLICT (chave) DO NOTHING;
