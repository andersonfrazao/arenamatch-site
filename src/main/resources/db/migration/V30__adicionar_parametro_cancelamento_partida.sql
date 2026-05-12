INSERT INTO parametro_sistema (chave, valor, descricao)
VALUES (
    'MIN_DIAS_ANTECEDENCIA_CANCELAMENTO',
    '3',
    'Quantidade minima de dias de antecedencia para solicitar cancelamento de um jogo marcado.'
)
ON CONFLICT (chave) DO NOTHING;
