MIGRACAO EM MATERIAL MB

ALTER TABLE MATERIAL_INDISPONIVEL RENAME TO EMPRESTIMO_KIT;

ALTER TABLE EMPRESTIMO_KIT DROP COLUMN VALOR;

ALTER TABLE EMPRESTIMO_KIT DROP COLUMN DATA_EXCLUSAO;

ALTER TABLE EMPRESTIMO_KIT DROP COLUMN EXCLUIDO_POR;

ALTER TABLE EMPRESTIMO_KIT DROP COLUMN EXCLUIDO;

ALTER TABLE ESTERILIZACAO_KIT RENAME COLUMN ID_MATERIAL_INDISPONIVEL TO EMPRESTIMO_KIT_ID;

ALTER TABLE LAVAGEM_KIT RENAME COLUMN ID_MATERIAL_INDISPONIVEL TO EMPRESTIMO_KIT_ID;

ALTER TABLE ABASTECIMENTO RENAME TO EMPRESTIMO_UNITARIO;

ALTER TABLE EMPRESTIMO_UNITARIO DROP COLUMN VALOR;

ALTER TABLE EMPRESTIMO_UNITARIO DROP COLUMN DATA_EXCLUSAO;

ALTER TABLE EMPRESTIMO_UNITARIO DROP COLUMN EXCLUIDO_POR;

ALTER TABLE EMPRESTIMO_UNITARIO DROP COLUMN EXCLUIDO;

ALTER TABLE LAVAGEM_KIT RENAME COLUMN ID_ABASTECIMENTO TO EMPRESTIMO_UNITARIO_ID;

ALTER TABLE ESTERILIZACAO_KIT RENAME COLUMN ID_ABASTECIMENTO TO EMPRESTIMO_UNITARIO_ID;

emprestimo_unitario_id

ALTER TABLE MATERIAL_LOG RENAME COLUMN ID_ABASTECIMENTO TO EMPRESTIMO_UNITARIO_ID;

ALTER TABLE ABASTECIMENTO_AGENDAMENTO_PLANO_TRATAMENTO_PROCEDIMENTO  RENAME COLUMN ID_ABASTECIMENTO TO EMPRESTIMO_UNITARIO_ID;

ALTER TABLE ABASTECIMENTO_AGENDAMENTO_PLANO_TRATAMENTO_PROCEDIMENTO RENAME TO EMPRESTIMO_UNITARIO_AGENDAMENTO_PLANO_TRATAMENTO_PROCEDIMENTO;

update seg_objeto set obj_str_caminho = 'emprestimoKit.jsf' where obj_str_caminho ilike '%controlema%'

update seg_objeto set obj_str_caminho = 'emprestimoUnitario.jsf' where obj_str_caminho = 'abastecimento.jsf'

update seg_objeto set obj_str_caminho = 'devolucaoEmprestimoUnitario.jsf' where obj_str_caminho = 'devolucaoAbastecimento.jsf'

ALTER TABLE MATERIAL_LOG RENAME COLUMN ID_MATERIAL_INDISPONIVEL TO EMPRESTIMO_KIT_ID;


---------------------------------

CREATE TABLE ESTOQUE (
	ID SERIAL PRIMARY KEY,
	ID_LOCAL BIGINT REFERENCES LOCAL(ID),
	ID_MATERIAL BIGINT REFERENCES MATERIAL(ID),
	QUANTIDADE NUMERIC(10,2)
);


ALTER TABLE table_name
ALTER COLUMN column_name [SET DATA] TYPE new_data_type;
 

CREATE TABLE TRANSFERENCIA_ESTOQUE(
	ID SERIAL PRIMARY KEY,
	ID_LOCAL_ORIGEM BIGINT REFERENCES LOCAL(ID),
	ID_LOCAL_DESTINO BIGINT REFERENCES LOCAL(ID),
	ID_MATERIAL BIGINT REFERENCES MATERIAL(ID),
	ID_PROFISSIONAL BIGINT REFERENCES PROFISSIONAL(ID),
	DESCRICAO varchar (500),
	DATA timestamp without time zone,
	QUANTIDADE BIGINT
)

alter table material drop column QUANTIDADE_UNIDADE

alter table material drop column QUANTIDADE_TOTAL


ALTER TABLE material 
RENAME COLUMN QUANTIDADE TO QUANTIDADE_PACOTES;

--EM - emprestimo
--PARA CONTROLAR MATERIAIS NAO DISPONIVEIS ESTOQUES EMPRESTADOS



somente depois de rodar o construtor do materialmb
alter table material drop column id_local
alter table material drop column quantidade_atual



----TODO PENSAR COMO POPULAR MATERIAL PARA ESSES NOVOS LOCIS



---migracao ----

-- rodar mudanca pra estoque, esta em materialmb

para novas empresas inserir todos os locais padrao



#####################################financeiro

ALTER TABLE CONTA RENAME TO PLANO_CONTA;

ALTER TABLE PACIENTE_SALDO RENAME TO CONTA;

ALTER TABLE CONTA RENAME COLUMN TIPO_SALDO TO TIPO_MOVIMENTACAO;

ALTER TABLE CONTA RENAME COLUMN PACIENTE_SALDO TO SALDO;

ALTER TABLE CONTA ADD COLUMN TIPO_CONTA VARCHAR(50);

ALTER TABLE CONTA ADD COLUMN PROFISSIONAL_ID BIGINT REFERENCES PROFISSIONAL(ID);

ALTER TABLE CONTA ADD COLUMN EMPRESA_ID BIGINT REFERENCES SEG_EMPRESA(EMP_INT_COD);

alter table PACIENTE_SALDO_ORIGEM_FATURA drop column paciente_saldo_id;

alter table PACIENTE_SALDO_ORIGEM_FATURA ADD COLUMN CONTA_ID BIGINT REFERENCES CONTA(ID);

alter table PACIENTE_SALDO_ORIGEM_FATURA RENAME TO CONTA_ORIGEM_FATURA;



ALTER TABLE CONTA ADD COLUMN ATIVO VARCHAR(1) NOT NULL DEFAULT 'S';
ALTER TABLE CONTA ADD COLUMN ALTERACAO_STATUS_ID BIGINT NULL REFERENCES PROFISSIONAL(ID);
ALTER TABLE CONTA ADD COLUMN DATA_ALTERACAO_STATUS TIMESTAMP(10) NULL; 


ALTER TABLE CONTA drop column tipo_movimentacao;

ALTER TABLE CONTA RENAME COLUMN VALOR_ITEM TO SALDO;

CREATE TABLE TRANSFERENCIA_CONTA(
	ID SERIAL PRIMARY KEY,
	ID_CONTA_ORIGEM BIGINT REFERENCES CONTA(ID),
	ID_CONTA_DESTINO BIGINT REFERENCES CONTA(ID),
	DESCRICAO varchar (500),
	DATA timestamp without time zone,
	VALOR NUMERIC(14,4),
	ID_PROFISSIONAL BIGINT REFERENCES PROFISSIONAL(ID)
)


alter table CONTA alter column paciente_id drop not null;
alter table CONTA alter column profissional_id drop not null;
alter table CONTA alter column empresa_id drop not null;

alter table CONTA_ORIGEM_FATURA RENAME TO TRANSFERENCIA_CONTA_ORIGEM_LANCAMENTO;

alter table TRANSFERENCIA_CONTA_ORIGEM_LANCAMENTO drop column CONTA_ID;

alter table TRANSFERENCIA_CONTA_ORIGEM_LANCAMENTO drop column FATURA_ID;

alter table TRANSFERENCIA_CONTA_ORIGEM_LANCAMENTO ADD COLUMN TRANSFERENCIA_CONTA_ID BIGINT REFERENCES TRANSFERENCIA_CONTA(ID);

alter table TRANSFERENCIA_CONTA_ORIGEM_LANCAMENTO ADD COLUMN LANCAMENTO_ID BIGINT REFERENCES LANCAMENTO(ID);

ALTER TABLE TRANSFERENCIA_CONTA ADD COLUMN ATIVO VARCHAR(1) NOT NULL DEFAULT 'S';
ALTER TABLE TRANSFERENCIA_CONTA ADD COLUMN ALTERACAO_STATUS_ID BIGINT NULL REFERENCES PROFISSIONAL(ID);
ALTER TABLE TRANSFERENCIA_CONTA ADD COLUMN DATA_ALTERACAO_STATUS TIMESTAMP(10) NULL; 


ALTER TABLE FATURA_ITEM ADD COLUMN valor_com_desconto NUMERIC(14,2);

insert into seg_objeto (obj_int_codpai,obj_str_des,obj_cha_sts,obj_str_caminho,sis_int_cod,obj_int_ordem,obj_cha_tipo) values
(11,	'Relat??rio de Faturas','A','relatorioFaturas.jsf',123,6,'T')	

--SE NAO TIVER AINDA O 155 USAR OUTRO
insert into objeto_profissional(obj_int_cod,id_profissional) 
select 157, id_profissional  from objeto_profissional where obj_int_cod = 155







CREATE OR REPLACE FUNCTION relatorio_fatura(id_empresa_param numeric, tipo character varying, inicio_busca timestamp, fim_busca timestamp,id_paciente_param numeric,id_profissional_param numeric)
 RETURNS TABLE("ID" numeric,"TIPO_FATURA" character varying,"DATA_CRIACAO" date, "PACIENTE_NOME" text, "PROFISSIONAL_NOME" text, "DESCRICAO_PT" text, "VALOR" NUMERIC, "VALOR_COM_DESCONTO" NUMERIC)
 LANGUAGE plpgsql
AS $function$
BEGIN
	SET lc_time = 'pt_BR.UTF8';
	RETURN QUERY
	select
		    (ROW_NUMBER() OVER ())::NUMERIC AS ID, 
			F.TIPO_FATURA as TIPO_FATURA,
			F.DATA_CRIACAO::DATE AS DATA_CRIACAO,	
			PDB.NOME::TEXT AS PACIENTE_NOME,
			PDB2.NOME::TEXT AS PROFISSIONAL_NOME,
			PT.DESCRICAO::TEXT AS DESCRICAO_PT,
			sum(ITEM.valor_item) as VALOR,
			sum(ITEM.valor_com_desconto) as VALOR_COM_DESCONTO		
		FROM FATURA F			
		LEFT JOIN PACIENTE P
			ON P.ID = F.PACIENTE_ID
		LEFT JOIN PROFISSIONAL PROF
			ON PROF.ID = F.PROFISSIONAL_ID					
		LEFT JOIN DADOS_BASICOS PDB
			ON PDB.ID = P.ID_DADOS_BASICOS
		LEFT JOIN FATURA_ITEM ITEM
			ON ITEM.FATURA_ID = F.ID

		LEFT JOIN REPASSE_FATURAS_ITEM RPI
			ON RPI.FATURA_ITEM_REPASSE_ID = ITEM.ID
		LEFT JOIN FATURA_ITEM ITEM2
			ON ITEM2.ID = RPI.FATURA_ITEM_ORIGEM_ID

		LEFT JOIN FATURA_ITEM_ORCAMENTO_ITEM FIOI
			ON (FIOI.FATURA_ITEM_ID = ITEM.ID OR FIOI.FATURA_ITEM_ID = ITEM2.ID)

		LEFT JOIN ORCAMENTO_ITEM OI
			ON OI.ID = FIOI.ORCAMENTO_ITEM_ID
		LEFT JOIN ORCAMENTO_PROCEDIMENTO OP
			ON OP.ORCAMENTO_ITEM_ID = OI.ID
		LEFT JOIN PLANO_TRATAMENTO_PROCEDIMENTO PTP
			ON PTP.ID = OP.PLANO_TRATAMENTO_PROCEDIMENTO_ID
		LEFT JOIN PLANO_TRATAMENTO PT
			ON PT.ID = PTP.ID_PLANO_TRATAMENTO
		LEFT JOIN PROFISSIONAL PRO
			ON PRO.ID = PT.ID_PROFISSIONAL		
		LEFT JOIN DADOS_BASICOS PDB2
			ON PDB2.ID = PROF.ID_DADOS_BASICOS			
		WHERE 
		  (TIPO = '' or (TIPO <> '' and F.TIPO_FATURA = TIPO))
		  AND (INICIO_BUSCA is null or (INICIO_BUSCA is not null AND DATE(F.DATA_CRIACAO) BETWEEN INICIO_BUSCA AND FIM_BUSCA))
		  AND (PROF.ID_EMPRESA = ID_EMPRESA_PARAM or P.ID_EMPRESA = ID_EMPRESA_PARAM)
		  AND (0 = 0 or (ID_PACIENTE_PARAM  <> 0 and P.ID = ID_PACIENTE))
		  AND (0  = 0 or (ID_PROFISSIONAL_PARAM  <> 0 AND PT.ID_PROFISSIONAL = ID_PROFISSIONAL))
		  GROUP BY F.TIPO_FATURA,F.DATA_CRIACAO,PDB.NOME,PDB2.NOME,PT.DESCRICAO;
END
$function$
;
