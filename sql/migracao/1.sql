--35 minutos para rodar em dev o script de migracao para essas tabelas

CREATE TABLE inteli.status_agendamento
(
    ID SERIAL PRIMARY KEY,
    DESCRICAO VARCHAR(200) NOT NULL    
);

INSERT INTO STATUS_AGENDAMENTO (DESCRICAO) VALUES ('Atendido');
INSERT INTO STATUS_AGENDAMENTO (DESCRICAO) VALUES('Não Atendido');
INSERT INTO STATUS_AGENDAMENTO (DESCRICAO) VALUES ('Agendado');

CREATE TABLE inteli.origem_agendamento
(
    ID SERIAL PRIMARY KEY,
     DESCRICAO VARCHAR(200) NOT NULL       
);

INSERT INTO ORIGEM_AGENDAMENTO (DESCRICAO) VALUES ('Encaixe');
INSERT INTO ORIGEM_AGENDAMENTO (DESCRICAO) VALUES ('Cliente');
INSERT INTO ORIGEM_AGENDAMENTO (DESCRICAO) VALUES ('Remarcado');

CREATE TABLE inteli.finalidade_agendamento
(
    ID SERIAL PRIMARY KEY,
    DESCRICAO VARCHAR(200) NOT NULL    
);

INSERT INTO FINALIDADE_AGENDAMENTO (DESCRICAO) VALUES ('Consulta Inicial');


CREATE TABLE inteli.situacao_agendamento
(
    ID SERIAL PRIMARY KEY,
    cliente_clinica timestamp without time zone,
    confirmado timestamp without time zone,
    em_atendimento timestamp without time zone
   );
   
   CREATE TABLE inteli.motivo_status_agendamento
(
    ID SERIAL PRIMARY KEY,
  DESCRICAO VARCHAR(200) NOT NULL    
);

INSERT INTO MOTIVO_STATUS_AGENDAMENTO (DESCRICAO) VALUES ('Cancelado');
INSERT INTO MOTIVO_STATUS_AGENDAMENTO (DESCRICAO) VALUES ('Erro de Agendamento');
INSERT INTO MOTIVO_STATUS_AGENDAMENTO (DESCRICAO) VALUES ('Falta');
INSERT INTO MOTIVO_STATUS_AGENDAMENTO (DESCRICAO) VALUES ('Remarcado'); 
   
ALTER TABLE AGENDAMENTO ADD COLUMN ID_STATUS_AGENDAMENTO BIGINT REFERENCES STATUS_AGENDAMENTO(ID);
ALTER TABLE AGENDAMENTO ADD COLUMN ID_ORIGEM_AGENDAMENTO BIGINT REFERENCES ORIGEM_AGENDAMENTO(ID);
ALTER TABLE AGENDAMENTO ADD COLUMN ID_FINALIDADE_AGENDAMENTO BIGINT REFERENCES FINALIDADE_AGENDAMENTO(ID);
ALTER TABLE AGENDAMENTO ADD COLUMN ID_SITUACAO_AGENDAMENTO BIGINT REFERENCES SITUACAO_AGENDAMENTO(ID);
ALTER TABLE AGENDAMENTO ADD COLUMN ID_MOTIVO_STATUS_AGENDAMENTO BIGINT REFERENCES MOTIVO_STATUS_AGENDAMENTO(ID);

CREATE TABLE inteli.LOG_AGENDAMENTO
(
    ID SERIAL PRIMARY KEY,
    ID_AGENDAMENTO BIGINT REFERENCES AGENDAMENTO(ID),
    DATA timestamp without time zone, 	
    id_paciente bigint NOT NULL,
    id_dentista bigint NOT NULL,
    chegou_as timestamp without time zone,
    iniciou_as timestamp without time zone,  
    inicio timestamp without time zone,
    fim timestamp without time zone,
    hash character varying(10) COLLATE pg_catalog."default" NOT NULL DEFAULT ''::character varying,
    descricao character varying(200) COLLATE pg_catalog."default",
    excluido character(1) COLLATE pg_catalog."default" DEFAULT 'N'::bpchar,
    data_exclusao timestamp without time zone,
    excluido_por bigint,
    id_agendador bigint,
    data_agendamento timestamp without time zone,
    justificativa character varying(100) COLLATE pg_catalog."default",
    finalizou_as timestamp without time zone,
    id_filial bigint,
    encaixe character(1) COLLATE pg_catalog."default",
    id_plano_tratamento bigint,
    auxiliar boolean DEFAULT false,
    cadeira integer DEFAULT 0,
    data_ult_alteracao timestamp without time zone,
    id_ult_alteracao bigint,
    id_status_agendamento bigint,
    id_origem_agendamento bigint,
    id_finalidade_agendamento bigint,
    id_situacao_agendamento bigint,
    id_motivo_status_agendamento bigint
);

ALTER TABLE paciente ADD COLUMN CARTEIRA_CONVENIO_TITULAR varchar(255);
ALTER TABLE dados_basicos ADD COLUMN EMPRESA_ONDE_TRABALHA varchar(255);
ALTER TABLE dados_basicos ADD COLUMN PROFISSAO varchar(255);
