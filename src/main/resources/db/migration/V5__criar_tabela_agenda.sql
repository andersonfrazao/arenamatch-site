CREATE SEQUENCE seq_agenda START WITH 1 INCREMENT BY 1;

CREATE TABLE agenda (
    id BIGINT DEFAULT nextval('seq_agenda') PRIMARY KEY,
    
    dia_semana VARCHAR(20) NOT NULL, -- Segunda, Terça...
    hora_inicio VARCHAR(5) NOT NULL, -- 14:00
    hora_fim VARCHAR(5) NOT NULL,    -- 16:00
    categoria VARCHAR(50) NOT NULL,  -- ESPORTE, VETERANO...
    
    id_time BIGINT NOT NULL,
    
    FOREIGN KEY (id_time) REFERENCES time(id)
);