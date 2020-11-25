CREATE TABLE PROFISSIONAL_DIARIA (
	ID SERIAL PRIMARY KEY,
	DATA_PONTO TIMESTAMP(6) NOT NULL,
	DATA_MARCACAO TIMESTAMP(6) NOT NULL,
	DATA_ALTERACAO TIMESTAMP(6) NULL,
	TIPO_PONTO VARCHAR NOT NULL,
	VALOR_DIARIA_INTEGRAL NUMERIC(11, 2) NULL,
	VALOR_DIARIA_REDUZIDA NUMERIC(11, 2) NULL,
	VALOR_DIARIA NUMERIC(11, 2) NULL,
	
	ATIVO VARCHAR(1) NOT NULL DEFAULT 'S',
	DATA_ALTERACAO_STATUS TIMESTAMP(6) NULL,
	ALTERACAO_STATUS_ID BIGINT NULL REFERENCES PROFISSIONAL(ID),
	
	PROFISSIONAL_ID BIGINT NOT NULL REFERENCES PROFISSIONAL(ID),
	PROFISSIONAL_MARCACAO_ID BIGINT NOT NULL REFERENCES PROFISSIONAL(ID),
	PROFISSIONAL_ALTERACAO_ID BIGINT NULL REFERENCES PROFISSIONAL(ID),
	EMPRESA_ID BIGINT NOT NULL REFERENCES SEG_EMPRESA(EMP_INT_COD)
);
CREATE TABLE RECIBO_REPASSE_PROFISSIONAL_DIARIA (
	ID SERIAL PRIMARY KEY,
	FATURA_ID BIGINT NULL REFERENCES FATURA(ID),
	DIARIA_ID BIGINT NOT NULL REFERENCES PROFISSIONAL_DIARIA(ID),
	RECIBO_ID BIGINT NOT NULL REFERENCES RECIBO_REPASSE_PROFISSIONAL(ID)
);
CREATE TABLE RECIBO_REPASSE_PROFISSIONAL_DIARIA_DADOS_IMUTAVEIS (
	ID SERIAL PRIMARY KEY,
	DATA_DIARIA TIMESTAMP(6) NOT NULL,
	TIPO_PONTO VARCHAR NOT NULL,
	VALOR_DIARIA NUMERIC(11, 2) NOT NULL,
	PROCEDIMENTOS TEXT NULL,
	RECIBO_ID BIGINT NOT NULL REFERENCES RECIBO_REPASSE_PROFISSIONAL_DIARIA(ID)
);

ALTER TABLE FATURA ADD COLUMN OBSERVACAO TEXT NULL;
ALTER TABLE FATURA ADD COLUMN RECORRENTE TEXT NULL;