ALTER TABLE LANCAMENTO_CONTABIL ADD COLUMN VALIDADO VARCHAR(1) NOT NULL DEFAULT 'S';
ALTER TABLE LANCAMENTO_CONTABIL ADD COLUMN DATA_VALIDACAO TIMESTAMP(10);
ALTER TABLE LANCAMENTO_CONTABIL ADD COLUMN VALIDADO_POR BIGINT REFERENCES PROFISSIONAL(ID);

UPDATE PLANO_TRATAMENTO_PROCEDIMENTO SET DENTISTA_EXECUTOR_ID = FINALIZADO_POR WHERE DENTISTA_EXECUTOR_ID IS NULL;


-----------------------ACIMA JA RODADO ---------------


ALTER TABLE FATURA_ITEM ADD COLUMN motivo_id bigint REFERENCES MOTIVO(ID);

--------------------acima ja rodado --------------------

SELECT
    OBJ_INT_COD, 'Relatório de Relacionamento',
    'A', 'relatorioPacienteAgendamento.jsf',
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
        WHERE OBJ_STR_DES = 'Relatório de Relacionamento'
        LIMIT 1
    ),
    OP.ID_PROFISSIONAL
FROM OBJETO_PROFISSIONAL OP
LEFT JOIN SEG_OBJETO O
    ON O.OBJ_INT_COD = OP.OBJ_INT_COD
WHERE O.OBJ_STR_DES = 'Repasse dos Profissionais'
  AND O.OBJ_CHA_STS = 'A';

UPDATE SEG_OBJETO SET OBJ_INT_ORDEM = 11 WHERE OBJ_STR_DES = 'Relatório de Relacionamento';

CREATE OR REPLACE FUNCTION inteli.unaccent_string(
	text)
    RETURNS text
    LANGUAGE 'sql'
    IMMUTABLE STRICT 
AS $function$
SELECT UPPER(translate(
    LOWER(
        translate(
            UPPER($1),
            'âãäåāăąÁÂÃÄÅĂĄèééêëēĕėęěĒĔĖĘĚìíîïìĩīĭÌÍÎÏÌĨĪĬóôõöōŏőÒÓÔÕÖŌŎŐùúûüũūŭůÙÚÛÜŨŪŬŮçÇ',
            'aaaaaaaaaaaaaaaeeeeeeeeeeeeeeeiiiiiiiiiiiiiiiiooooooooooooooouuuuuuuuuuuuuuuucc'
        )
    ),
    'âãäåāăąÁÂÃÄÅĂĄèééêëēĕėęěĒĔĖĘĚìíîïìĩīĭÌÍÎÏÌĨĪĬóôõöōŏőÒÓÔÕÖŌŎŐùúûüũūŭůÙÚÛÜŨŪŬŮçÇ',
    'aaaaaaaaaaaaaaaeeeeeeeeeeeeeeeiiiiiiiiiiiiiiiiooooooooooooooouuuuuuuuuuuuuuuucc'
));
$function$;

UPDATE FATURA SET ID_EMPRESA = (
    SELECT ID_EMPRESA
    FROM PACIENTE
    WHERE ID = FATURA.PACIENTE_ID
) WHERE PACIENTE_ID IS NOT NULL;

UPDATE FATURA SET ID_EMPRESA = (
    SELECT ID_EMPRESA
    FROM PROFISSIONAL
    WHERE ID = FATURA.profissional_id
) WHERE profissional_id IS NOT NULL;

UPDATE FATURA SET ID_EMPRESA = (
    SELECT ID_EMPRESA
    FROM FORNECEDOR
    WHERE ID = FATURA.fornecedor_id
) WHERE fornecedor_id IS NOT NULL;

UPDATE FATURA SET ID_EMPRESA = (
    SELECT ID_EMPRESA
    FROM ORIGEM
    WHERE ID = FATURA.origem_id
) WHERE origem_id IS NOT NULL;

INSERT INTO local (descricao, tipo, id_empresa, excluido) SELECT distinct 'ENTREGA_LAVAGEM_DEVOLUCAO_UNITARIA','SI',emp_int_cod,'N' FROM seg_empresa;

------------------- acima ja rodado -------------------------------

DROP VIEW REPASSE_MIGRACAO;
DROP VIEW REPASSE_FATURAS_CREATE;
ALTER TABLE FATURA_ITEM ALTER COLUMN VALOR_ITEM TYPE NUMERIC(14, 4);
ALTER TABLE FATURA_ITEM ALTER COLUMN VALOR_COM_DESCONTO TYPE NUMERIC(14, 4);
ALTER TABLE LANCAMENTO ALTER COLUMN VALOR TYPE NUMERIC(14, 4);
ALTER TABLE LANCAMENTO ALTER COLUMN VALOR_ORIGINAL TYPE NUMERIC(14, 4);
ALTER TABLE FATURA_ITEM ALTER COLUMN VALOR_COM_DESCONTO SET NOT NULL;
CREATE VIEW REPASSE_FATURAS_CREATE AS
SELECT DISTINCT
	PTP.ID_PLANO_TRATAMENTO AS "PTP_ID",
	PTP.FINALIZADO_POR AS "PTP_FP",
	F.ID AS "F_ID"
FROM ORCAMENTO_ITEM OI
LEFT JOIN ORCAMENTO O ON O.ID = OI.ORCAMENTO_ID
LEFT JOIN ORCAMENTO_PROCEDIMENTO OP ON OP.ORCAMENTO_ITEM_ID = OI.ID
LEFT JOIN PLANO_TRATAMENTO_PROCEDIMENTO PTP ON PTP.ID = OP.PLANO_TRATAMENTO_PROCEDIMENTO_ID
LEFT JOIN FATURA_ITEM_ORCAMENTO_ITEM FIOI ON FIOI.ORCAMENTO_ITEM_ID = OI.ID
LEFT JOIN FATURA_ITEM FI ON FI.ID = FIOI.FATURA_ITEM_ID
LEFT JOIN FATURA F ON F.ID = FI.FATURA_ID
WHERE O.APROVADO = 'S'
  AND O.ATIVO = 'S'
  AND OI.INCLUSO = 'S'
  AND PTP.FINALIZADO_POR IS NOT NULL;
CREATE VIEW REPASSE_MIGRACAO AS
SELECT DISTINCT
	P.DESCRICAO AS "PROC",
	PTP.ID AS "PTP_ID",
	F.ID AS "F_ID",
	FI.ID AS "FI_ID",
	CASE WHEN FI.VALOR_COM_DESCONTO IS NOT NULL
		THEN FI.VALOR_COM_DESCONTO
		ELSE FI.VALOR_ITEM
	END AS "FI_VALOR",
	PP.ID AS "PP_ID",
	COALESCE(SUM(RI_R.VALOR), 0) AS "VR",
	COALESCE(SUM(RI_L.VALOR), 0) + COALESCE(SUM(RI_P.VALOR), 0) AS "VP",
	RP.DATA AS "DATA",
	RP.DATA_PAGAMENTO AS "DATA_PAGTO",
	RP.ID_PROFISSIONAL AS "PROF_PAGTO_ID"
FROM PLANO_TRATAMENTO_PROCEDIMENTO PTP
LEFT JOIN PROCEDIMENTO P ON P.ID = PTP.ID_PROCEDIMENTO
LEFT JOIN PLANO_TRATAMENTO PT ON PT.ID = PTP.ID_PLANO_TRATAMENTO
LEFT JOIN ORCAMENTO_PROCEDIMENTO OP ON OP.PLANO_TRATAMENTO_PROCEDIMENTO_ID = PTP.ID
LEFT JOIN ORCAMENTO_ITEM OI ON OI.ID = OP.ORCAMENTO_ITEM_ID
LEFT JOIN FATURA_ITEM_ORCAMENTO_ITEM FIOI ON FIOI.ORCAMENTO_ITEM_ID = OI.ID
LEFT JOIN FATURA_ITEM FI ON FI.ID = FIOI.FATURA_ITEM_ID
LEFT JOIN FATURA F ON F.ID = FI.FATURA_ID
LEFT JOIN REPASSE_ITEM RI ON RI.ID_PLANO_TRATAMENTO_PROCEDIMENTO = PTP.ID
LEFT JOIN REPASSE_PROFISSIONAL RP ON RP.ID = RI.ID_REPASSE
LEFT JOIN REPASSE_ITEM RI_R ON RI_R.ID_REPASSE = RP.ID AND RI_R.ID_PLANO_TRATAMENTO_PROCEDIMENTO = PTP.ID AND RI_R.STATUS = 'R'
LEFT JOIN REPASSE_ITEM RI_P ON RI_P.ID_REPASSE = RP.ID AND RI_P.ID_PLANO_TRATAMENTO_PROCEDIMENTO = PTP.ID AND RI_P.STATUS = 'P'
LEFT JOIN REPASSE_ITEM RI_L ON RI_L.ID_REPASSE = RP.ID AND RI_L.ID_PLANO_TRATAMENTO_PROCEDIMENTO = PTP.ID AND RI_L.STATUS = 'L'
LEFT JOIN PROFISSIONAL PP ON PP.ID = RP.ID_PROFISSIONAL
WHERE RP.ID_PROFISSIONAL IS NOT NULL
GROUP BY PTP.ID, P.DESCRICAO, F.ID, FI.ID, PP.ID, RP.DATA, RP.DATA_PAGAMENTO, RP.ID_PROFISSIONAL
HAVING COALESCE(SUM(RI_R.VALOR), 0) > 0 OR COALESCE(SUM(RI_L.VALOR), 0) + COALESCE(SUM(RI_P.VALOR), 0) > 0
ORDER BY PTP.ID ASC;

ALTER TABLE LOCAL ADD COLUMN PASSIVEL_EMPRESTIMO VARCHAR(1) NOT NULL DEFAULT 'S';
  
update LOCAL set passivel_emprestimo = 'N' where id_empresa = 41 and tipo = 'ES';

ALTER TABLE LOCAL alter column tipo drop not null; 

------------------- acima ja rodado -------------------------------

CREATE TABLE ENVIO_EMAIL (
	ID SERIAL PRIMARY KEY,
	DATA TIMESTAMP(10) NOT NULL DEFAULT CURRENT_TIMESTAMP,
	
	MOTIVO VARCHAR(255) NOT NULL,
	REMETENTE VARCHAR(255) NOT NULL,
	ASSUNTO TEXT NOT NULL,
	CONTEUDO TEXT NULL,
	
	HOST VARCHAR(255) NOT NULL,
	PORTA NUMERIC NOT NULL,
	
	SUCESSO BOOLEAN NOT NULL,
	ERRO TEXT NULL,
	STACK_TRACE TEXT NULL
);

CREATE TABLE ENVIO_EMAIL_DESTINATARIO (
	ID SERIAL PRIMARY KEY,
	EMAIL VARCHAR(255) NOT NULL,
	ENVIO_EMAIL_ID BIGINT NOT NULL REFERENCES ENVIO_EMAIL(ID)
);


ALTER TABLE PACIENTE ADD COLUMN CODIGO TEXT NULL;
------------------- acima ja rodado -------------------------------


ALTER TABLE ORCAMENTO ADD COLUMN VERSAO VARCHAR(1) NOT NULL DEFAULT 'N';
ALTER TABLE ORCAMENTO ADD COLUMN N_VERSAO NUMERIC(8) NULL;
ALTER TABLE ORCAMENTO ADD COLUMN ORCAMENTO_ORIGINAL_ID BIGINT NULL REFERENCES ORCAMENTO(ID);

CREATE TABLE indice_reajuste(

	id serial primary key,
	plano_tratamento_id bigint NOT NULL REFERENCES PLANO_TRATAMENTO(id),
	reajuste numeric(5,2),
	data_criacao timestamp(10)

);

ALTER TABLE orcamento ADD COLUMN id_indice_reajuste bigint REFERENCES INDICE_REAJUSTE(ID);
ALTER TABLE INDICE_REAJUSTE ADD COLUMN PROFISSIONAL_CRIACAO_ID BIGINT REFERENCES PROFISSIONAL(ID);

--------------------- acima ja rodado -------------------------------

ALTER TABLE repasse_faturas_lancamento ALTER COLUMN lancamento_origem_id DROP NOT NULL;
ALTER TABLE repasse_faturas ALTER COLUMN fatura_origem_id DROP NOT NULL;
ALTER TABLE repasse_faturas_item ALTER COLUMN fatura_item_origem_id DROP NOT NULL;
ALTER TABLE repasse_faturas_lancamento ALTER COLUMN fatura_item_id DROP NOT NULL;

update procedimento set valor_repasse = 0 where valor_repasse is null;
ALTER TABLE procedimento ALTER COLUMN valor_repasse SET DEFAULT 0;

-------------------- acima ja rodado -------------------------------

alter table repasse_faturas ADD COLUMN plano_tratamento_procedimento_id BIGINT REFERENCES plano_tratamento_procedimento(ID);
-------------------- acima ja rodado -------------------------------

ALTER TABLE SEG_EMPRESA ADD COLUMN VALIDAR_GERA_RECIBO_VALOR_ZERADO VARCHAR(1) NOT NULL DEFAULT 'N';  
 
INSERT INTO SEG_OBJETO(OBJ_INT_CODPAI, OBJ_STR_DES, OBJ_CHA_STS, OBJ_STR_CAMINHO,
                       SIS_INT_COD, OBJ_INT_ORDEM, OBJ_CHA_TIPO, OBJ_STR_ICON)
SELECT
	OBJ_INT_COD, 'Repasse com Recibo',
	'A', 'repasseComRecibo.jsf',
	123, MAX(OBJ_INT_ORDEM) + 1,
	'T', NULL
FROM SEG_OBJETO
WHERE OBJ_STR_DES ILIKE 'FINANCEIRO'
GROUP BY OBJ_INT_COD, OBJ_INT_ORDEM
LIMIT 1;
INSERT INTO OBJETO_PROFISSIONAL(OBJ_INT_COD, ID_PROFISSIONAL)
SELECT
	(
		SELECT OBJ_INT_COD FROM SEG_OBJETO
		WHERE OBJ_STR_DES LIKE 'Repasse com Recibo'
		LIMIT 1
	),
	OP.ID_PROFISSIONAL
FROM OBJETO_PROFISSIONAL OP
LEFT JOIN SEG_OBJETO O
	ON O.OBJ_INT_COD = OP.OBJ_INT_COD
WHERE O.OBJ_STR_DES ILIKE 'Pagamentos/Recebimentos'
  AND O.OBJ_CHA_STS = 'A';

update plano_tratamento_procedimento set dentista_executor_id = finalizado_por
where dentista_executor_id is null and data_finalizado is not null

--TODO verificar como popular plano_tratamento_procedimento_id do repasse_faturas para os antigos, quando so tinha procedimento



ALTER TABLE PROFISSIONAL ADD COLUMN CRIADO_POR_ID BIGINT NULL REFERENCES PROFISSIONAL(ID);
ALTER TABLE PROFISSIONAL ADD COLUMN DATA_CRIACAO TIMESTAMP(10) NULL;

-------------------- acima ja rodado -------------------------------

UPDATE SEG_OBJETO SET OBJ_CHA_STS = 'I' WHERE OBJ_STR_CAMINHO = 'reciboRepasseProfissional.jsf';
UPDATE SEG_OBJETO SET OBJ_STR_DES = 'Repasse dos Profissionais por Fatura' WHERE OBJ_STR_CAMINHO = 'repasseProfissional.jsf';
UPDATE SEG_OBJETO SET OBJ_STR_DES = 'Repasse dos Profissionais' WHERE OBJ_STR_CAMINHO = 'repasseComRecibo.jsf';



--------- AINDA NAO RODADO, ESTOQUE -----------


ALTER TABLE LOCAL ADD COLUMN DISPONIVEL_PARA_USO VARCHAR(1) NOT NULL DEFAULT 'S';

--INSERT INTO local (descricao, tipo, id_empresa, excluido,PASSIVEL_EMPRESTIMO,DISPONIVEL_PARA_USO) 
--SELECT distinct 'Para Lavar','SI',emp_int_cod,'N','N','N' FROM seg_empresa;

INSERT INTO local (descricao, tipo, id_empresa, excluido,PASSIVEL_EMPRESTIMO,DISPONIVEL_PARA_USO) 
SELECT distinct 'Em Lavagem','SI',emp_int_cod,'N','N','N' FROM seg_empresa;

INSERT INTO local (descricao, tipo, id_empresa, excluido,PASSIVEL_EMPRESTIMO,DISPONIVEL_PARA_USO) 
SELECT distinct 'Para Esterilizar','SI',emp_int_cod,'N','N','N' FROM seg_empresa;

INSERT INTO local (descricao, tipo, id_empresa, excluido,PASSIVEL_EMPRESTIMO,DISPONIVEL_PARA_USO) 
SELECT distinct 'Em Esterilização','SI',emp_int_cod,'N','N','N' FROM seg_empresa;

INSERT INTO local (descricao, tipo, id_empresa, excluido,PASSIVEL_EMPRESTIMO,DISPONIVEL_PARA_USO) 
SELECT distinct 'Empacotado','SI',emp_int_cod,'N','N','N' FROM seg_empresa;

INSERT INTO local (descricao, tipo, id_empresa, excluido,PASSIVEL_EMPRESTIMO,DISPONIVEL_PARA_USO) 
SELECT distinct 'Descartado','SI',emp_int_cod,'N','N','N' FROM seg_empresa;

update  SEG_OBJETO set OBJ_STR_DES = 'Processo de Lavagem' WHERE OBJ_STR_DES = 'Devolução da Lavagem';

delete from OBJETO_PROFISSIONAL where  OBJ_INT_COD in (
SELECT
    (
        SELECT OBJ_INT_COD FROM SEG_OBJETO
        WHERE OBJ_STR_DES = 'Entrega para Lavagem Manual'
        LIMIT 1
    )
    )    
    
CREATE TABLE LOG_LAVAGEM(
	ID SERIAL PRIMARY KEY,
	ID_LOCAL_ORIGEM BIGINT REFERENCES LOCAL(ID),
	ID_LOCAL_DESTINO BIGINT REFERENCES LOCAL(ID),
	ID_MATERIAL BIGINT REFERENCES MATERIAL(ID),
	ID_PROFISSIONAL BIGINT REFERENCES PROFISSIONAL(ID),	
	DATA timestamp without time zone,
	QUANTIDADE BIGINT
)  

update  SEG_OBJETO set OBJ_STR_DES = 'Processo de Esterilização' WHERE OBJ_STR_DES = 'Devolução da Esterilização';

CREATE TABLE LOG_ESTERILIZACAO(
	ID SERIAL PRIMARY KEY,
	ID_LOCAL_ORIGEM BIGINT REFERENCES LOCAL(ID),
	ID_LOCAL_DESTINO BIGINT REFERENCES LOCAL(ID),
	ID_MATERIAL BIGINT REFERENCES MATERIAL(ID),
	ID_PROFISSIONAL BIGINT REFERENCES PROFISSIONAL(ID),	
	DATA timestamp without time zone,
	QUANTIDADE BIGINT
) 

alter table estoque ADD COLUMN data_validade TIMESTAMP(10)

UPDATE SEG_OBJETO SET OBJ_INT_ORDEM = 1 WHERE OBJ_STR_DES = 'Processo de Lavagem';
UPDATE SEG_OBJETO SET OBJ_INT_ORDEM = 2 WHERE OBJ_STR_DES = 'Processo de Esterilização';
UPDATE SEG_OBJETO SET OBJ_INT_ORDEM = 3 WHERE OBJ_STR_DES = 'Relatório de Lavagem';
UPDATE SEG_OBJETO SET OBJ_INT_ORDEM = 4 WHERE OBJ_STR_DES = 'Relatório de Esterilização';

INSERT INTO local (descricao, tipo, id_empresa, excluido,PASSIVEL_EMPRESTIMO,DISPONIVEL_PARA_USO) 
SELECT distinct 'Descartado','SI',emp_int_cod,'N','N','N' FROM seg_empresa;

INSERT INTO local (descricao, tipo, id_empresa, excluido,PASSIVEL_EMPRESTIMO,DISPONIVEL_PARA_USO) 
SELECT distinct 'Material Devolvido','SI',emp_int_cod,'N','N','N' FROM seg_empresa;


ALTER TABLE TRANSFERENCIA_ESTOQUE DROP COLUMN ID_LOCAL_ORIGEM;
ALTER TABLE TRANSFERENCIA_ESTOQUE DROP COLUMN ID_LOCAL_DESTINO;
ALTER TABLE TRANSFERENCIA_ESTOQUE ADD COLUMN ID_ESTOQUE_ORIGEM BIGINT REFERENCES ESTOQUE(ID);
ALTER TABLE TRANSFERENCIA_ESTOQUE ADD COLUMN ID_ESTOQUE_DESTINO BIGINT REFERENCES ESTOQUE(ID);

ALTER TABLE LOCAL ADD COLUMN DESCONSIDERA_DO_ESTOQUE VARCHAR(1) NOT NULL DEFAULT 'N';

update local set DESCONSIDERA_DO_ESTOQUE = 'S' where descricao = 'Descartado';
update local set DESCONSIDERA_DO_ESTOQUE = 'S' where descricao = 'Material Devolvido';
update local set DESCONSIDERA_DO_ESTOQUE = 'S' where descricao = 'COMPRA';


insert into dominio (objeto,tipo,nome,valor,editavel,excluido,id_empresa)
values( 'material','justificativa','Erro nos dados','ED',true,'N',41)

INSERT INTO local (descricao, tipo, id_empresa, excluido,PASSIVEL_EMPRESTIMO,DISPONIVEL_PARA_USO) 
SELECT distinct 'Consumido','SI',emp_int_cod,'N','N','N' FROM seg_empresa;


 
INSERT INTO SEG_OBJETO(OBJ_INT_CODPAI, OBJ_STR_DES, OBJ_CHA_STS, OBJ_STR_CAMINHO,
                       SIS_INT_COD, OBJ_INT_ORDEM, OBJ_CHA_TIPO, OBJ_STR_ICON)
SELECT
	OBJ_INT_COD, 'Consumo Avulso',
	'A', 'consumoAvulso.jsf',
	123, MAX(OBJ_INT_ORDEM) + 1,
	'T', NULL
FROM SEG_OBJETO
WHERE OBJ_STR_DES ILIKE 'FINANCEIRO'
GROUP BY OBJ_INT_COD, OBJ_INT_ORDEM
LIMIT 1;

INSERT INTO OBJETO_PROFISSIONAL(OBJ_INT_COD, ID_PROFISSIONAL)
SELECT
	(
		SELECT OBJ_INT_COD FROM SEG_OBJETO
		WHERE OBJ_STR_DES LIKE 'Consumo Avulso'
		LIMIT 1
	),
	OP.ID_PROFISSIONAL
FROM OBJETO_PROFISSIONAL OP
LEFT JOIN SEG_OBJETO O
	ON O.OBJ_INT_COD = OP.OBJ_INT_COD
WHERE O.OBJ_STR_DES ILIKE 'Pagamentos/Recebimentos'
  AND O.OBJ_CHA_STS = 'A';

update seg_objeto set obj_int_codpai = 36 where OBJ_STR_DES = 'Consumo Avulso';  
  
--TODO deletar estoques SI antigos, criar
Emprestado
Reservado


"KIT_UTILIZADO_CONSUMIDO"
"EMPRESTIMO_UNITARIO"
"DEVOLUCAO_UNITARIA"
"CANCELAMENTO_EMPRESTIMO_KIT"
"EMPRESTIMO_KIT"
"DEVOLUCAO_KIT_LAVAGEM"
"KIT_UTILIZADO_DEVOLVIDO"
"KIT_NAO_UTILIZADO"
"DEVOLUCAO_KIT"
"EXCLUSAO_RESERVA_KIT"
"EXCLUSAO_RESERVA"
"CANCELAMENTO_RESERVA"

ALTER TABLE LOCAL ADD COLUMN DISPONIVEL_RETORNO_ESTERILIZACAO VARCHAR(1) NOT NULL DEFAULT 'S';

--------- AINDA NAO RODADO, ESTOQUE -----------
