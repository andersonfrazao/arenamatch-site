INSERT INTO parametro_sistema (chave, valor, descricao)
VALUES (
    'DIAS_INTERVALO_AGENDAMENTO_PLANO_BASICO',
    '15',
    'Quantidade de dias entre agendamentos permitidos para usuarios do plano basico.'
)
ON CONFLICT (chave) DO NOTHING;

INSERT INTO parametro_sistema (chave, valor, descricao)
VALUES (
    'DIAS_TRIAL',
    '90',
    'Quantidade de dias de trial concedidos para novos usuarios.'
)
ON CONFLICT (chave) DO NOTHING;
