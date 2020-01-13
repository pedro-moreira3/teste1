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


------------------- acima ja rodado -------------------------------


INSERT INTO local (descricao, tipo, id_empresa, excluido) SELECT distinct 'ENTREGA_LAVAGEM_DEVOLUCAO_UNITARIA','SI',emp_int_cod,'N' FROM seg_empresa;

  