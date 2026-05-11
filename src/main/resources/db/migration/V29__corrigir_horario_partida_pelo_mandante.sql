UPDATE partida p
SET data_hora = p.data_hora::date + agenda_mandante.hora_inicio::time
FROM (
    SELECT DISTINCT ON (a.id_time, a.dia_semana)
        a.id_time,
        a.dia_semana,
        a.hora_inicio
    FROM agenda a
    WHERE a.hora_inicio IS NOT NULL
      AND a.hora_inicio <> ''
    ORDER BY a.id_time, a.dia_semana, a.hora_inicio
) agenda_mandante
WHERE agenda_mandante.id_time = p.id_mandante
  AND agenda_mandante.dia_semana = CASE EXTRACT(ISODOW FROM p.data_hora)
      WHEN 1 THEN 'Segunda'
      WHEN 2 THEN 'Terça'
      WHEN 3 THEN 'Quarta'
      WHEN 4 THEN 'Quinta'
      WHEN 5 THEN 'Sexta'
      WHEN 6 THEN 'Sábado'
      WHEN 7 THEN 'Domingo'
  END
  AND p.status IN ('PENDENTE', 'AGENDADO', 'SOLICITACAO_CANCELAMENTO')
  AND p.data_hora IS NOT NULL;
