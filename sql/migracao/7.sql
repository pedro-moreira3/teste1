ALTER TABLE SEG_EMPRESA ADD COLUMN VALIDAR_REPASSE_LANCAMENTO_ORIGEM_VALIDADO_ORTODONTICO VARCHAR(1) NOT NULL DEFAULT 'N';
ALTER TABLE SEG_EMPRESA ADD COLUMN VALIDAR_REPASSE_POR_PROCEDIMENTO_ORTODONTICO VARCHAR(1) NOT NULL DEFAULT 'S';


UPDATE REPASSE_FATURAS SET PLANO_TRATAMENTO_PROCEDIMENTO_ID = (
	SELECT PTP.ID
	FROM PLANO_TRATAMENTO_PROCEDIMENTO PTP
	LEFT JOIN ORCAMENTO_PROCEDIMENTO OP ON OP.PLANO_TRATAMENTO_PROCEDIMENTO_ID = PTP.ID
	LEFT JOIN ORCAMENTO_ITEM OI ON OI.ID = OP.ORCAMENTO_ITEM_ID
	LEFT JOIN ORCAMENTO O ON O.ID = OI.ORCAMENTO_ID
	LEFT JOIN FATURA_ITEM_ORCAMENTO_ITEM FIOI ON FIOI.ORCAMENTO_ITEM_ID = OI.ID
	LEFT JOIN FATURA_ITEM FI ON FI.ID = FIOI.FATURA_ITEM_ID
	LEFT JOIN FATURA FORG ON FORG.ID = FI.FATURA_ID
	LEFT JOIN REPASSE_FATURAS_ITEM RI ON RI.FATURA_ITEM_ORIGEM_ID = FI.ID
	LEFT JOIN REPASSE_FATURAS RF ON RF.ID = RI.REPASSE_FATURAS_ID
	LEFT JOIN FATURA FREP ON FREP.ID = RF.FATURA_REPASSE_ID
	LEFT JOIN FATURA_ITEM FIREP ON FIREP.ID = RI.FATURA_ITEM_REPASSE_ID
	WHERE RF.ID = REPASSE_FATURAS.ID
	  AND OI.INCLUSO = 'S'
	  AND OI.ATIVO = 'S'
	  AND O.APROVADO = 'S'
	  AND O.ATIVO = 'S'
	  AND FI.ATIVO = 'S'
	  AND FORG.ATIVO = 'S'
	  AND FREP.ATIVO = 'S'
	  AND FIREP.ATIVO = 'S'
) WHERE PLANO_TRATAMENTO_PROCEDIMENTO_ID IS NULL;

CREATE TABLE RECIBO_REPASSE_PROFISSIONAL_LANCAMENTO_DADOS_IMUTAVEIS (
	ID SERIAL PRIMARY KEY,
	PACIENTE VARCHAR(255),
	PLANO_TRATAMENTO VARCHAR(255),
	PLANO_TRATAMENTO_PROCEDIMENTO VARCHAR(255),
	LANCAMENTO_PAGO VARCHAR(255),
	VALOR_TOTAL VARCHAR(255),
	VALOR_JA_PAGO VARCHAR(255),
	VALOR_A_PAGAR VARCHAR(255),
	DATA_EXECUCAO VARCHAR(255),
	RECIBO_ID BIGINT NOT NULL REFERENCES RECIBO_REPASSE_PROFISSIONAL_LANCAMENTO(ID)
);

-------------------------acima ja rodado ----------------------------------------------------

CREATE TABLE AJUSTE_CONTAS (
	ID SERIAL PRIMARY KEY,
	VALOR DECIMAL(13, 4) NOT NULL,
	TIPO_PAGAMENTO VARCHAR(1) NOT NULL,
	
	STATUS VARCHAR(1) NOT NULL,
	ATIVO VARCHAR(1) NOT NULL DEFAULT 'S',
	DATA_CRIACAO TIMESTAMP(10) NOT NULL,
	RESOLVIDO VARCHAR(1) NOT NULL DEFAULT 'N',
	DATA_RESOLUCAO TIMESTAMP(10) NOT NULL,
	DATA_ALTERACAO_STATUS TIMESTAMP(10) NOT NULL,

	CRIADO_POR BIGINT REFERENCES PROFISSIONAL(ID),
	RESOLVIDO_POR BIGINT REFERENCES PROFISSIONAL(ID),
	PACIENTE_ID BIGINT REFERENCES PACIENTE(ID),
	PROFISSIONAL_ID BIGINT REFERENCES PROFISSIONAL(ID),
	EMPRESA_ID BIGINT REFERENCES SEG_EMPRESA(EMP_INT_COD),
	ALTERACAO_STATUS_ID BIGINT REFERENCES PROFISSIONAL(ID),
	
	EMPRESA_PROPRIETARIA_ID BIGINT REFERENCES SEG_EMPRESA(EMP_INT_COD)
);
CREATE TABLE AJUSTE_CONTAS_ORIGEM_PTP (
	ID SERIAL PRIMARY KEY,
	VALOR_TOTAL NUMERIC(13, 2) NOT NULL,
	VALOR_PAGO NUMERIC(13, 2) NOT NULL,
	VALOR_EXECUTADO NUMERIC(13, 2) NOT NULL,
	
	FATURA_ORIGEM_ID BIGINT REFERENCES FATURA(ID),
	FATURA_AJUSTE_ID BIGINT REFERENCES FATURA(ID),
	PLANO_TRATAMENTO_ID BIGINT REFERENCES PLANO_TRATAMENTO(ID),
	
	AJUSTE_CONTAS_ID BIGINT REFERENCES AJUSTE_CONTAS(ID),
	AJUSTE_CONTAS_ORIGEM_ID BIGINT REFERENCES AJUSTE_CONTAS(ID)
);
CREATE TABLE AJUSTE_CONTAS_ORIGEM_PTP_ITEM (
	ID SERIAL PRIMARY KEY,
	PLANO_TRATAMENTO_PROCEDIMENTO_ID BIGINT REFERENCES PLANO_TRATAMENTO_PROCEDIMENTO(ID),
	
	AJUSTE_CONTAS_ORIGEM_ID BIGINT REFERENCES AJUSTE_CONTAS_ORIGEM_PTP(ID)
);
CREATE TABLE AJUSTE_CONTAS_ORIGEM_REPASSE (
	ID SERIAL PRIMARY KEY,
	FATURA_AJUSTE_ID BIGINT REFERENCES FATURA(ID),
	REPASSE_FATURAS_ID BIGINT REFERENCES REPASSE_FATURAS(ID),
	
	AJUSTE_CONTAS_ID BIGINT REFERENCES AJUSTE_CONTAS(ID)
);

ALTER TABLE DADOS_BASICOS
ALTER COLUMN numero TYPE VARCHAR(100);

INSERT INTO SEG_OBJETO(OBJ_INT_CODPAI, OBJ_STR_DES, OBJ_CHA_STS, OBJ_STR_CAMINHO,
                       SIS_INT_COD, OBJ_INT_ORDEM, OBJ_CHA_TIPO, OBJ_STR_ICON)
SELECT
	OBJ_INT_COD, 'Ajuste de Contas',
	'A', 'ajusteContas.jsf',
	123, MAX(OBJ_INT_ORDEM) + 1,
	'T', NULL
FROM SEG_OBJETO
WHERE OBJ_STR_DES = 'Financeiro'
GROUP BY OBJ_INT_COD, OBJ_INT_ORDEM
LIMIT 1;
INSERT INTO OBJETO_PROFISSIONAL(OBJ_INT_COD, ID_PROFISSIONAL)
SELECT
	(
		SELECT OBJ_INT_COD FROM SEG_OBJETO
		WHERE OBJ_STR_DES = 'Ajuste de Contas'
		LIMIT 1
	),
	OP.ID_PROFISSIONAL
FROM OBJETO_PROFISSIONAL OP
LEFT JOIN SEG_OBJETO O
	ON O.OBJ_INT_COD = OP.OBJ_INT_COD
WHERE O.OBJ_STR_DES = 'Pagamentos/Recebimentos';

alter table tarifa add column status varchar(1);
alter table tarifa add column ultima_alteracao_por bigint references PROFISSIONAL(ID);
update tarifa set status = 'A' where excluido = 'N'
update tarifa set status = 'N' where excluido = 'S'