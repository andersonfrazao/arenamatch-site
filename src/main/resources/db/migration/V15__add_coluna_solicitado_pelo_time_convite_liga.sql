-- Adiciona a flag para saber se o convite partiu do Time (solicitando entrada) 
-- ou se partiu da Liga (convidando o time)
ALTER TABLE convite_liga 
ADD COLUMN solicitado_pelo_time BOOLEAN NOT NULL DEFAULT FALSE;