INSERT INTO parametro_sistema (chave, valor, descricao)
VALUES (
    'RAIO_MAXIMO_BUSCA_PLANO_BASICO_KM',
    '10',
    'Raio maximo em quilometros permitido na busca de times para usuarios do plano basico.'
)
ON CONFLICT (chave) DO NOTHING;
