ALTER TABLE partida
ADD COLUMN IF NOT EXISTS data_informacao_placar TIMESTAMP;

UPDATE partida
SET data_informacao_placar = COALESCE(data_solicitacao, data_hora, CURRENT_TIMESTAMP)
WHERE status_placar = 'AGUARDANDO_CONFIRMACAO'
  AND data_informacao_placar IS NULL;

INSERT INTO parametro_sistema (chave, valor, descricao)
VALUES (
    'DIAS_CONFIRMACAO_AUTOMATICA_PLACAR',
    '3',
    'Quantidade de dias para confirmar automaticamente um placar quando o adversario nao responder.'
)
ON CONFLICT (chave) DO NOTHING;
