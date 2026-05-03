ALTER TABLE usuario ADD COLUMN IF NOT EXISTS plano_assinatura VARCHAR(20);
ALTER TABLE usuario ADD COLUMN IF NOT EXISTS status_pagamento VARCHAR(20);
ALTER TABLE usuario ADD COLUMN IF NOT EXISTS data_inicio_assinatura TIMESTAMP;
ALTER TABLE usuario ADD COLUMN IF NOT EXISTS id_assinatura_externa VARCHAR(100);
ALTER TABLE usuario ADD COLUMN IF NOT EXISTS gateway_pagamento VARCHAR(50);

UPDATE usuario
SET plano_assinatura = CASE
        WHEN status_assinatura = 'ATIVO' THEN 'PRO'
        WHEN status_assinatura = 'TRIAL' THEN 'TRIAL'
        ELSE 'BASICO'
    END,
    status_pagamento = CASE
        WHEN status_assinatura = 'ATIVO' THEN 'PAGO'
        WHEN status_assinatura = 'TRIAL' THEN 'TRIAL'
        ELSE 'EXPIRADO'
    END,
    data_inicio_assinatura = COALESCE(data_cadastro, CURRENT_TIMESTAMP)
WHERE plano_assinatura IS NULL
   OR status_pagamento IS NULL
   OR data_inicio_assinatura IS NULL;
