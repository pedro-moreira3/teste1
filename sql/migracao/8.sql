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
	
	TELA TEXT,
	DATA TIMESTAMP(10),
	
	HOST_ADDRESS TEXT,	
	BROWSER TEXT,	
	OS TEXT,
	
	EMPRESA_ID BIGINT REFERENCES SEG_EMPRESA(EMP_INT_COD),
	PROFISSIONAL_ID BIGINT REFERENCES PROFISSIONAL(ID)
);

ALTER TABLE AUDIT ADD COLUMN PHASE_ID TEXT DEFAULT 'INVOKE_APPLICATION';

CREATE TABLE FATURA_SUB_STATUS (
	ID SERIAL PRIMARY KEY,
	SUB_STATUS_FATURA_ROTULO VARCHAR(10) NOT NULL,
	FATURA_ID BIGINT REFERENCES FATURA(ID)
);

--------------------------------------ACIMA JA RODADO

UPDATE FATURA SET STATUS_FATURA = (
	CASE
		WHEN (SELECT COUNT(L.*) FROM LANCAMENTO L WHERE L.FATURA_ID = FATURA.ID AND ATIVO = 'S' AND VALIDADO = 'S') = 0 AND
			 (FATURA.TIPO_FATURA) = 'RP'
			THEN 'RP'
		WHEN (SELECT COUNT(L.*) FROM LANCAMENTO L WHERE L.FATURA_ID = FATURA.ID AND ATIVO = 'S' AND VALIDADO = 'S') = 0 AND
			 (FATURA.TIPO_FATURA) <> 'RP'
			THEN 'PP'
		WHEN (SELECT SUM(COALESCE(ROUND(L.VALOR, 2), 0)) FROM LANCAMENTO L WHERE L.FATURA_ID = FATURA.ID AND ATIVO = 'S' AND VALIDADO = 'S') <
			 (SELECT SUM(COALESCE(ROUND(FI.VALOR_COM_DESCONTO, 2), 0)) FROM FATURA_ITEM FI WHERE FI.FATURA_ID = FATURA.ID AND ATIVO = 'S' ) AND
			 (FATURA.TIPO_FATURA) = 'RP'
			THEN 'RP'
		WHEN (SELECT SUM(COALESCE(ROUND(L.VALOR, 2), 0)) FROM LANCAMENTO L WHERE L.FATURA_ID = FATURA.ID AND ATIVO = 'S' AND VALIDADO = 'S') <
			 (SELECT SUM(COALESCE(ROUND(FI.VALOR_COM_DESCONTO, 2), 0)) FROM FATURA_ITEM FI WHERE FI.FATURA_ID = FATURA.ID AND ATIVO = 'S' ) AND
			 (FATURA.TIPO_FATURA) <> 'RP'
			THEN 'PP'
		WHEN (SELECT SUM(COALESCE(ROUND(L.VALOR, 2), 0)) FROM LANCAMENTO L WHERE L.FATURA_ID = FATURA.ID AND ATIVO = 'S' AND VALIDADO = 'S') >=
			 (SELECT SUM(COALESCE(ROUND(FI.VALOR_COM_DESCONTO, 2), 0)) FROM FATURA_ITEM FI WHERE FI.FATURA_ID = FATURA.ID AND ATIVO = 'S' ) AND
			 (FATURA.TIPO_FATURA) = 'RP'
			THEN 'RE'
		WHEN (SELECT SUM(COALESCE(ROUND(L.VALOR, 2), 0)) FROM LANCAMENTO L WHERE L.FATURA_ID = FATURA.ID AND ATIVO = 'S' AND VALIDADO = 'S') >=
			 (SELECT SUM(COALESCE(ROUND(FI.VALOR_COM_DESCONTO, 2), 0)) FROM FATURA_ITEM FI WHERE FI.FATURA_ID = FATURA.ID AND ATIVO = 'S' ) AND
			 (FATURA.TIPO_FATURA) <> 'RP'
			THEN 'PA'
	ELSE NULL END
) WHERE STATUS_FATURA IS NULL OR STATUS_FATURA <> 'I';

WITH FATURA_A_PLANEJAR AS (
	SELECT DISTINCT F.* FROM FATURA F
	LEFT JOIN (
		SELECT DISTINCT FATURA_ID, SUM(VALOR) AS TOTAL
		FROM LANCAMENTO WHERE ATIVO = 'S' AND FATURA_ID IS NOT NULL
		GROUP BY FATURA_ID
	) LSA ON LSA.FATURA_ID = F.ID
	LEFT JOIN ( 
		SELECT DISTINCT FATURA_ID, SUM(VALOR_COM_DESCONTO) AS TOTAL
		FROM FATURA_ITEM WHERE ATIVO = 'S' AND FATURA_ID IS NOT NULL
		GROUP BY FATURA_ID
	) FIS ON FIS.FATURA_ID = F.ID
	WHERE F.STATUS_FATURA IN ('RP', 'PP')
	GROUP BY F.ID, LSA.TOTAL, FIS.TOTAL
	HAVING (
		CAST(COALESCE(LSA.TOTAL, 0) AS DECIMAL(15, 2)) < CAST(COALESCE(FIS.TOTAL, 0) AS DECIMAL(15, 2)) OR
		COUNT(LSA.*) = 0
	)
), FATURA_A_CONFERIR AS (
	SELECT DISTINCT F.* FROM FATURA F
	LEFT JOIN LANCAMENTO L ON L.FATURA_ID = F.ID
	WHERE F.STATUS_FATURA IN ('RP', 'PP')
	  AND L.ATIVO = 'S'
	  AND L.PAGAMENTO_CONFERIDO = 'N'
	  AND L.VALIDADO = 'N'
), FATURA_A_VALIDAR AS (
	SELECT DISTINCT F.* FROM FATURA F
	LEFT JOIN LANCAMENTO L ON L.FATURA_ID = F.ID
	WHERE F.STATUS_FATURA IN ('RP', 'PP')
	  AND L.ATIVO = 'S'
	  AND L.PAGAMENTO_CONFERIDO = 'S'
	  AND L.VALIDADO = 'N'
), INSERT_FATURA_A_PLANEJAR AS (
	INSERT INTO FATURA_SUB_STATUS(SUB_STATUS_FATURA_ROTULO, FATURA_ID)
	SELECT 'AP', F.ID FROM FATURA_A_PLANEJAR F
	RETURNING *
), INSERT_FATURA_A_CONFERIR AS (
	INSERT INTO FATURA_SUB_STATUS(SUB_STATUS_FATURA_ROTULO, FATURA_ID)
	SELECT 'AC', F.ID FROM FATURA_A_CONFERIR F
	RETURNING *
), INSERT_FATURA_A_VALIDAR AS (
	INSERT INTO FATURA_SUB_STATUS(SUB_STATUS_FATURA_ROTULO, FATURA_ID)
	SELECT 'AV', F.ID FROM FATURA_A_VALIDAR F
	RETURNING *
) SELECT 'Status: Ok' AS "Status",
	(SELECT COUNT(*) FROM INSERT_FATURA_A_PLANEJAR) AS "Registros Afetados (A Planejar)",
	(SELECT COUNT(*) FROM INSERT_FATURA_A_CONFERIR) AS "Registros Afetados (A Conferir)",
	(SELECT COUNT(*) FROM INSERT_FATURA_A_VALIDAR) AS "Registros Afetados (A Validar)";