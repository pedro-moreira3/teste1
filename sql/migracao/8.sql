INSERT INTO SEG_OBJETO(OBJ_INT_CODPAI, OBJ_STR_DES, OBJ_CHA_STS, OBJ_STR_CAMINHO,
                       SIS_INT_COD, OBJ_INT_ORDEM, OBJ_CHA_TIPO, OBJ_STR_ICON)
SELECT
	OBJ_INT_COD, 'Agendamento Rápido',
	'A', 'agendamentoRapido.jsf',
	123, MAX(OBJ_INT_ORDEM) + 1,
	'T', NULL
FROM SEG_OBJETO
WHERE OBJ_STR_DES = 'Clínica'
GROUP BY OBJ_INT_COD, OBJ_INT_ORDEM
LIMIT 1;
INSERT INTO OBJETO_PROFISSIONAL(OBJ_INT_COD, ID_PROFISSIONAL)
SELECT
	(
		SELECT OBJ_INT_COD FROM SEG_OBJETO
		WHERE OBJ_STR_DES = 'Agendamento Rápido'
		LIMIT 1
	),
	OP.ID_PROFISSIONAL
FROM OBJETO_PROFISSIONAL OP
LEFT JOIN SEG_OBJETO O
	ON O.OBJ_INT_COD = OP.OBJ_INT_COD
WHERE O.OBJ_STR_DES = 'Paciente';

alter table horas_uteis_profissional add column HORA_INI_TARDE TIME;
alter table horas_uteis_profissional add column HORA_FIM_TARDE TIME;
alter table convenio_procedimento add column status varchar(1);
update convenio_procedimento set status = 'A' where excluido = 'N';
update convenio_procedimento set status = 'I' where excluido = 'S';

alter table seg_empresa add column PROFISSIONAL_HORA_INICIAL_MANHA TIME DEFAULT '08:00:00';
alter table seg_empresa add column PROFISSIONAL_HORA_FINAL_MANHA TIME DEFAULT '12:00:00';
alter table seg_empresa add column PROFISSIONAL_HORA_INICIAL_TARDE TIME DEFAULT '13:00:00';
alter table seg_empresa add column PROFISSIONAL_HORA_FINAL_TARDE TIME DEFAULT '18:00:00';
alter table seg_empresa add column PROFISSIONAL_TEMPO_PADRAO_CONSULTA NUMERIC DEFAULT 30;
alter table seg_empresa add column PROFISSIONAL_DIAS_SEMANA varchar(100) DEFAULT 'SEG;TER;QUA;QUI;SEX;';
alter table seg_empresa RENAME PROFISSIONAL_HORA_INICIAL_MANHA TO HORA_INICIAL_MANHA;
alter table seg_empresa RENAME PROFISSIONAL_HORA_FINAL_MANHA TO HORA_FINAL_MANHA;
alter table seg_empresa RENAME PROFISSIONAL_HORA_INICIAL_TARDE TO HORA_INICIAL_TARDE;
alter table seg_empresa RENAME PROFISSIONAL_HORA_FINAL_TARDE TO HORA_FINAL_TARDE;
alter table seg_empresa RENAME PROFISSIONAL_TEMPO_PADRAO_CONSULTA TO TEMPO_PADRAO_CONSULTA;
alter table seg_empresa RENAME PROFISSIONAL_DIAS_SEMANA TO DIAS_SEMANA;
alter table horas_uteis_profissional alter column hora_ini drop not null;
alter table horas_uteis_profissional alter column hora_fim drop not null;

CREATE TABLE AUDIT (
	ID SERIAL PRIMARY KEY,
	METHODS TEXT,
	TIME_EXEC NUMERIC(25),
	
	TELA VARCHAR(100),
	DATA TIMESTAMP(10),
	
	EMPRESA_ID BIGINT REFERENCES SEG_EMPRESA(EMP_INT_COD),
	PROFISSIONAL_ID BIGINT REFERENCES PROFISSIONAL(ID)
);