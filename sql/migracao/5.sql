UPDATE SEG_OBJETO SET OBJ_INT_ORDEM = 2 WHERE OBJ_STR_DES = 'Cadastro da Clínica';
UPDATE SEG_OBJETO SET OBJ_INT_ORDEM = 3 WHERE OBJ_STR_DES = 'Cadastro de Especialidades';
UPDATE SEG_OBJETO SET OBJ_INT_ORDEM = 4 WHERE OBJ_STR_DES = 'Cadastro de Procedimentos';
UPDATE SEG_OBJETO SET OBJ_INT_ORDEM = 5 WHERE OBJ_STR_DES = 'Cadastro de Convênios';
UPDATE SEG_OBJETO SET OBJ_INT_ORDEM = 6 WHERE OBJ_STR_DES = 'Convênio-Procedimento';
UPDATE SEG_OBJETO SET OBJ_INT_ORDEM = 7 WHERE OBJ_STR_DES = 'Cadastro de Profissionais';
UPDATE SEG_OBJETO SET OBJ_INT_ORDEM = 8 WHERE OBJ_STR_DES = 'Config. de Anamnese';

UPDATE SEG_OBJETO SET OBJ_INT_CODPAI = (
	SELECT OBJ_INT_COD FROM SEG_OBJETO
	WHERE SEG_OBJETO.OBJ_STR_DES = 'Emissão de Documentos'
) WHERE OBJ_STR_DES = 'Configuração de Documentos';

UPDATE SEG_OBJETO SET OBJ_STR_DES = 'Agenda' WHERE OBJ_STR_DES = 'Agendamento';
UPDATE SEG_OBJETO SET OBJ_STR_DES = 'Devolução Unitária' WHERE OBJ_STR_DES = 'Unitária';
UPDATE SEG_OBJETO SET OBJ_STR_DES = 'Relatório de Agendamento' WHERE OBJ_STR_DES = 'Relatório de Atendimento';

UPDATE SEG_OBJETO SET OBJ_INT_ORDEM = 2 WHERE OBJ_STR_DES = 'Fila de Atendimento';

UPDATE SEG_OBJETO SET OBJ_CHA_STS = 'I' WHERE OBJ_STR_DES = 'Repasse dos Profissionais' AND OBJ_STR_CAMINHO = 'faturamento.jsf';
UPDATE SEG_OBJETO SET OBJ_CHA_STS = 'I' WHERE OBJ_STR_DES = 'Rel. Atendimento' AND OBJ_STR_CAMINHO = 'filaAtendimento.jsf';
UPDATE SEG_OBJETO SET OBJ_CHA_STS = 'I' WHERE OBJ_STR_DES = 'Relatório de Avaliação de Atendimentos';
UPDATE SEG_OBJETO SET OBJ_CHA_STS = 'I' WHERE OBJ_STR_DES = 'Registro de Custos Diretos';
UPDATE SEG_OBJETO SET OBJ_CHA_STS = 'I' WHERE OBJ_STR_DES = 'Pagamentos/Recebimentos';
UPDATE SEG_OBJETO SET OBJ_CHA_STS = 'I' WHERE OBJ_STR_DES = 'Relatório de Resultados';
UPDATE SEG_OBJETO SET OBJ_CHA_STS = 'I' WHERE OBJ_STR_DES = 'Relatório de Bloqueios';
UPDATE SEG_OBJETO SET OBJ_CHA_STS = 'I' WHERE OBJ_STR_DES = 'Atualizações';
UPDATE SEG_OBJETO SET OBJ_CHA_STS = 'I' WHERE OBJ_STR_DES = 'Dashboard';

-------------------

ALTER TABLE AGENDAMENTO_PLANO_TRATAMENTO_PROCEDIMENTO ADD COLUMN ATIVO VARCHAR(1) NOT NULL DEFAULT 'S';
ALTER TABLE AGENDAMENTO_PLANO_TRATAMENTO_PROCEDIMENTO ADD COLUMN ALTERACAO_STATUS_ID BIGINT NULL REFERENCES PROFISSIONAL(ID);
ALTER TABLE AGENDAMENTO_PLANO_TRATAMENTO_PROCEDIMENTO ADD COLUMN DATA_ALTERACAO_STATUS TIMESTAMP(10) NULL;

ALTER TABLE SEG_EMPRESA ADD COLUMN EMP_STR_WPP_MESSAGE_AGENDAMENTO TEXT NULL;
ALTER TABLE SEG_EMPRESA ADD COLUMN EMP_STR_WPP_MESSAGE_PACIENTE TEXT NULL;
ALTER TABLE SEG_EMPRESA ADD COLUMN EMP_STR_WPP_MESSAGE_RETORNO TEXT NULL;

ALTER TABLE PACIENTE ADD COLUMN CRIADO_POR BIGINT NULL;
-----------------

insert into dominio (objeto,tipo,nome,valor,id_empresa,editavel,excluido) values ('indicacao','indicacao','Indicado por Paciente','0',0,false,'N');
insert into dominio (objeto,tipo,nome,valor,id_empresa,editavel,excluido) values ('indicacao','indicacao','Indicado por Profissional','1',0,false,'N');
insert into dominio (objeto,tipo,nome,valor,id_empresa,editavel,excluido) values ('indicacao','indicacao','Google','2',0,false,'N');
insert into dominio (objeto,tipo,nome,valor,id_empresa,editavel,excluido) values ('indicacao','indicacao','Facebook','3',0,false,'N');
insert into dominio (objeto,tipo,nome,valor,id_empresa,editavel,excluido) values ('indicacao','indicacao','Instagram','4',0,false,'N');
insert into dominio (objeto,tipo,nome,valor,id_empresa,editavel,excluido) values ('indicacao','indicacao','Propaganda','5',0,false,'N');
insert into dominio (objeto,tipo,nome,valor,id_empresa,editavel,excluido) values ('indicacao','indicacao','Outros','6',0,false,'N');

alter table PACIENTE ADD COLUMN INDICACAO_DOMINIO_ID BIGINT REFERENCES DOMINIO(ID);

alter table PACIENTE ADD COLUMN INDICACAO_PACIENTE_ID BIGINT REFERENCES PACIENTE(ID);
alter table PACIENTE ADD COLUMN INDICACAO_PROFISSIONAL_ID BIGINT REFERENCES PROFISSIONAL(ID);

UPDATE PACIENTE SET INDICACAO_DOMINIO_ID = (SELECT ID FROM dominio WHERE TIPO = 'indicacao' AND nome = 'Outros') WHERE INDICACAO IS NOT NULL AND INDICACAO <> '';

ALTER TABLE PERGUNTA ALTER COLUMN requerida DROP DEFAULT;
ALTER TABLE PERGUNTA ALTER requerida TYPE bool USING CASE WHEN requerida='false' THEN FALSE ELSE TRUE END;
ALTER TABLE PERGUNTA ALTER COLUMN requerida set DEFAULT false;
-----------------

UPDATE PLANO_TRATAMENTO SET ID_ODONTOGRAMA = (
	SELECT
		CASE WHEN UP."ODONTOGRAMA" IS NOT NULL
			THEN UP."ODONTOGRAMA"
		ELSE (
			SELECT ODO.ID
			FROM PLANO_TRATAMENTO PT
			LEFT JOIN PLANO_TRATAMENTO_PROCEDIMENTO PTP ON PTP.ID_PLANO_TRATAMENTO = PT.ID
			LEFT JOIN DENTE D ON D.ID = PTP.ID_DENTE
			LEFT JOIN ODONTOGRAMA ODO ON ODO.ID = D.ID_ODONTOGRAMA
			WHERE PT.ID = UP."PT"
			ORDER BY PTP.ID DESC, D.ID DESC, ODO.ID DESC
			LIMIT 1
		) END
	FROM (
		SELECT DISTINCT PT.ID AS "PT", (
			SELECT O.ID FROM ODONTOGRAMA O
			WHERE O.DATA_CADASTRO < PT.DATA_HORA
			  AND O.ID_PACIENTE = PT.ID_PACIENTE
			ORDER BY ID DESC LIMIT 1
		) AS "ODONTOGRAMA" FROM PLANO_TRATAMENTO PT
	) AS UP
	WHERE UP."PT" = PLANO_TRATAMENTO.ID
) WHERE ID IN (
	SELECT ID FROM PLANO_TRATAMENTO WHERE ID_ODONTOGRAMA IS NULL
);
CREATE TABLE REGIAO_REGIAO (
	ID SERIAL PRIMARY KEY,
	REGIAO VARCHAR(255) NOT NULL,
	EXCLUIDO VARCHAR(1) NOT NULL DEFAULT 'N',
	DATA_EXCLUSAO TIMESTAMP(10) NULL,
	
	EXCLUIDO_POR BIGINT NULL REFERENCES PROFISSIONAL(ID),
	ODONTOGRAMA_ID BIGINT NULL REFERENCES ODONTOGRAMA(ID),
	ID_STATUS_DENTE BIGINT NOT NULL REFERENCES STATUS_DENTE(ID)
);
-------------------------
CREATE TABLE AFILIACAO (
	ID SERIAL PRIMARY KEY,
	NOME VARCHAR(255) NOT NULL,
	EMAIL VARCHAR(255) NULL,
	TELEFONE VARCHAR(50) NULL,
	
	ATIVO VARCHAR(1) NOT NULL DEFAULT 'N',
	DATA_ALTERACAO_STATUS TIMESTAMP(10) NULL,
	ALTERACAO_STATUS_ID BIGINT NULL REFERENCES PROFISSIONAL(ID)
);
INSERT INTO AFILIACAO(NOME, ATIVO)
VALUES('Grupo QS - Qualidade e Saúde', 'S');
ALTER TABLE SEG_EMPRESA ADD COLUMN AFILIACAO_ID BIGINT NULL REFERENCES AFILIACAO(ID);
-------------------------
ALTER TABLE TRANSFERENCIA_CONTA
   DROP CONSTRAINT transferencia_conta_id_conta_origem_fkey
 , ADD  CONSTRAINT transferencia_conta_id_conta_origem_fkey FOREIGN KEY(ID_CONTA_ORIGEM)
      REFERENCES CONTA(ID) DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE TRANSFERENCIA_CONTA
   DROP CONSTRAINT transferencia_conta_id_conta_destino_fkey
 , ADD  CONSTRAINT transferencia_conta_id_conta_destino_fkey FOREIGN KEY(ID_CONTA_DESTINO)
      REFERENCES CONTA(ID) DEFERRABLE INITIALLY DEFERRED;
      
----------------------------
update ESPECIALIDADE set descricao = 'GENERICAS' where descricao ilike 'GEN%'      
ALTER TABLE SEG_EMPRESA ADD COLUMN VALIDAR_REPASSE_PROCEDIMENTO_FINALIZADO VARCHAR(1) NOT NULL DEFAULT 'S';
ALTER TABLE SEG_EMPRESA ADD COLUMN VALIDAR_REPASSE_LANCAMENTO_ORIGEM_VALIDADO VARCHAR(1) NOT NULL DEFAULT 'S';
----------------------------
CREATE TABLE EVOLUCAO_PLANO_TRATAMENTO_PROCEDIMENTO (
	ID SERIAL PRIMARY KEY,
	PLANO_TRATAMENTO VARCHAR(255) NULL,
	PLANO_TRATAMENTO_PROCEDIMENTO TEXT NULL,
	ACAO_EVOLUCAO VARCHAR(255) NULL,
	REGIAO_DENTE_FACE VARCHAR(255) NULL,	
	EVOLUCAO_ID BIGINT NOT NULL REFERENCES EVOLUCAO(ID),
	PLANO_TRATAMENTO_PROCEDIMENTO_ID BIGINT NULL REFERENCES PLANO_TRATAMENTO_PROCEDIMENTO(ID)
);

ALTER TABLE PLANO_TRATAMENTO_PROCEDIMENTO ADD COLUMN DATA_CRIADO timestamp without time zone;

----------------------------
UPDATE SEG_OBJETO SET OBJ_STR_DES = 'Relatório de Estoque Antigo' WHERE OBJ_STR_DES = 'Relatório de Estoque';
UPDATE SEG_OBJETO SET OBJ_STR_DES = 'Relatório de Estoque' WHERE OBJ_STR_DES = 'Estoque Mínimo';
UPDATE SEG_OBJETO SET OBJ_CHA_STS = 'I' WHERE OBJ_STR_DES = 'Relatório de Estoque Antigo';
----------------------------
ALTER TABLE SEG_EMPRESA ADD COLUMN UTILIZA_RESERVA_KITS BOOLEAN default TRUE;
INSERT INTO AFILIACAO('NOME', 'ATIVO')
VALUES('Consultório Legal', 'S');

UPDATE DOMINIO SET EXCLUIDO = 'S', DATA_EXCLUSAO = CURRENT_DATE
WHERE ID IN (
	SELECT
		D.ID
	FROM DOMINIO D
	WHERE D.OBJETO = 'planotratamento'
	  AND D.TIPO = 'justificativa'
	  AND D.EXCLUIDO = 'N'
	 ORDER BY D.NOME
);
INSERT INTO DOMINIO(OBJETO, TIPO, NOME, VALOR, ID_EMPRESA, EDITAVEL, EXCLUIDO)
VALUES('planotratamento', 'justificativa', 'Cadastro de plano de tratamento errado', 'CE', 41, TRUE, 'N');
INSERT INTO DOMINIO(OBJETO, TIPO, NOME, VALOR, ID_EMPRESA, EDITAVEL, EXCLUIDO)
VALUES('planotratamento', 'justificativa', 'Paciente desistiu do tratamento', 'PD', 41, TRUE, 'N');
INSERT INTO DOMINIO(OBJETO, TIPO, NOME, VALOR, ID_EMPRESA, EDITAVEL, EXCLUIDO)
VALUES('planotratamento', 'justificativa', 'Paciente optou por outro plano de tratamento', 'PP', 41, TRUE, 'N');
INSERT INTO DOMINIO(OBJETO, TIPO, NOME, VALOR, ID_EMPRESA, EDITAVEL, EXCLUIDO)
VALUES('planotratamento', 'justificativa', 'Plano de tratamento foi alterado', 'PA', 41, TRUE, 'N');

UPDATE DOMINIO SET EXCLUIDO = 'S', DATA_EXCLUSAO = CURRENT_DATE
WHERE ID IN (
	SELECT
		D.ID
	FROM DOMINIO D
	WHERE D.OBJETO = 'planotratamentoprocedimento'
	  AND D.TIPO = 'justificativa'
	  AND D.EXCLUIDO = 'N'
	 ORDER BY D.NOME
);
INSERT INTO DOMINIO(OBJETO, TIPO, NOME, VALOR, ID_EMPRESA, EDITAVEL, EXCLUIDO)
VALUES('planotratamentoprocedimento', 'justificativa', 'Cadastro de procedimento errado', 'PE', 41, TRUE, 'N');
INSERT INTO DOMINIO(OBJETO, TIPO, NOME, VALOR, ID_EMPRESA, EDITAVEL, EXCLUIDO)
VALUES('planotratamentoprocedimento', 'justificativa', 'Solicitação do dentista', 'SD', 41, TRUE, 'N');
INSERT INTO DOMINIO(OBJETO, TIPO, NOME, VALOR, ID_EMPRESA, EDITAVEL, EXCLUIDO)
VALUES('planotratamentoprocedimento', 'justificativa', 'Solicitação do paciente', 'SP', 41, TRUE, 'N');

ALTER TABLE SEG_EMPRESA ADD COLUMN ADICIONAR_LOGO_ORCAMENTO VARCHAR(1) NOT NULL DEFAULT 'N';

ALTER TABLE plano_tratamento RENAME finalizado TO status;

update plano_tratamento set status = 'E' where status = 'S' and justificativa is null



----------------------------
 insert into seg_perobjeto (per_int_cod,obj_int_cod)  
 select per_int_cod,149 from seg_perobjeto where obj_int_cod = 90


insert into objeto_profissional (obj_int_cod,id_profissional) 
  select 149,id_profissional from objeto_profissional where obj_int_cod = 90
-------------------------------

CREATE TABLE RECIBO_REPASSE_PROFISSIONAL (
	ID SERIAL PRIMARY KEY,
	DATA_CRIACAO TIMESTAMP(10) NOT NULL,
	DESCRICAO VARCHAR(255) NULL,
	OBSERVACAO TEXT NULL,

	ATIVO VARCHAR(1) NULL,
	APROVADO VARCHAR(1) NULL,
	DATA_APROVACAO TIMESTAMP(10) NULL,
	DATA_ALTERACAO_STATUS TIMESTAMP(10) NULL,
	
	APROVADO_POR BIGINT NULL REFERENCES PROFISSIONAL(ID),
	CRIADO_POR BIGINT NOT NULL REFERENCES PROFISSIONAL(ID),
	ALTERACAO_STATUS_ID BIGINT NULL REFERENCES PROFISSIONAL(ID),
	PROFISSIONAL_ID BIGINT NOT NULL REFERENCES PROFISSIONAL(ID)
);
CREATE TABLE RECIBO_REPASSE_PROFISSIONAL_LANCAMENTO (
	ID SERIAL PRIMARY KEY,
	VALIDADO_AO_APROVAR_RECIBO VARCHAR(1) NOT NULL,
	LANCAMENTO_ID BIGINT NOT NULL REFERENCES LANCAMENTO(ID),
	RECIBO_ID BIGINT NOT NULL REFERENCES RECIBO_REPASSE_PROFISSIONAL(ID)
);

INSERT INTO SEG_OBJETO(OBJ_INT_CODPAI, OBJ_STR_DES, OBJ_CHA_STS, OBJ_STR_CAMINHO,
                       SIS_INT_COD, OBJ_INT_ORDEM, OBJ_CHA_TIPO, OBJ_STR_ICON)
SELECT
	OBJ_INT_COD, 'Recibo de Repasses',
	'A', 'reciboRepasseProfissional.jsf',
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
		WHERE OBJ_STR_DES LIKE 'Recibo de Repasses'
		LIMIT 1
	),
	OP.ID_PROFISSIONAL
FROM OBJETO_PROFISSIONAL OP
LEFT JOIN SEG_OBJETO O
	ON O.OBJ_INT_COD = OP.OBJ_INT_COD
WHERE O.OBJ_STR_DES ILIKE 'REPASSE DE PROFISSIONAL'
  AND O.OBJ_CHA_STS = 'A';
  
  
  
  ALTER TABLE ORCAMENTO ADD COLUMN DESCRICAO VARCHAR(200);
  
  ALTER TABLE ORCAMENTO ADD COLUMN OBSERVACOES VARCHAR(1000);
  
  ALTER TABLE PLANO_TRATAMENTO_PROCEDIMENTO ADD COLUMN DENTISTA_EXECUTOR_ID BIGINT NULL REFERENCES PROFISSIONAL(ID);
  
  CREATE TABLE ORCAMENTO_PLANEJAMENTO (
	ID SERIAL PRIMARY KEY,
	VALOR DECIMAL(12,2) NOT NULL,
	N_PARCELA INTEGER NOT NULL,
	DATA_PAGAMENTO TIMESTAMP(10) NOT NULL,
	DATA_CREDITO TIMESTAMP(10) NOT NULL,
	
	ATIVO VARCHAR(1) NOT NULL DEFAULT 'S',
	DATA_ALTERACAO_STATUS TIMESTAMP(10),
	
	ORCAMENTO_ID BIGINT NOT NULL REFERENCES ORCAMENTO(ID),
	FORMA_PAGAMENTO_ID BIGINT NOT NULL REFERENCES DOMINIO(ID),
	ALTERACAO_STATUS_ID BIGINT NULL REFERENCES PROFISSIONAL(ID)
);
  ALTER TABLE PLANO_TRATAMENTO_PROCEDIMENTO ADD COLUMN DENTISTA_EXECUTOR_ID BIGINT NULL REFERENCES PROFISSIONAL(ID);
-------------------------------
UPDATE seg_objeto SET obj_int_ordem = 2 WHERE obj_str_des = 'Agenda';
UPDATE seg_objeto SET obj_int_ordem = 3 WHERE obj_str_des = 'Fila de Atendimento';
UPDATE seg_objeto SET obj_int_ordem = 4 WHERE obj_str_des = 'Retorno';
UPDATE seg_objeto SET obj_cha_sts = 'I' WHERE obj_str_des = 'Dashboard';
UPDATE seg_objeto SET obj_str_des = 'Consumo' WHERE obj_str_des = 'Empréstimo de Materiais';
UPDATE seg_objeto SET obj_cha_sts = 'I' WHERE obj_str_des = 'Devolução de Materiais';
UPDATE seg_objeto SET obj_int_ordem = 3 WHERE obj_str_des = 'Empréstimo Unitário';
UPDATE seg_objeto SET obj_int_ordem = 1 WHERE obj_str_des = 'Empréstimo de Kits';
UPDATE seg_objeto SET obj_int_codpai = 36, obj_int_ordem = 2 WHERE obj_str_des = 'Devolução de Kits';
UPDATE seg_objeto SET obj_int_codpai = 36, obj_int_ordem = 4 WHERE obj_str_des = 'Devolução Unitária';
UPDATE seg_objeto SET obj_int_ordem = 5 WHERE obj_str_des = 'Ajuste de Estoque';
UPDATE seg_objeto SET obj_int_ordem = 7 WHERE obj_str_des = 'Movimentação de Materiais';
UPDATE seg_objeto SET obj_int_ordem = 6 WHERE obj_str_des = 'Cadastros';
-------------------------------
INSERT INTO objeto_profissional (obj_int_cod,id_profissional) 
SELECT 166,id_profissional FROM objeto_profissional WHERE obj_int_cod = 149;
-------------------------------
ALTER TABLE CONVENIO ADD COLUMN observacao_convenio character varying(255);
-------------------------------
ALTER TABLE FATURA ADD COLUMN PROFISSIONAL_CRIACAO_ID BIGINT NULL REFERENCES PROFISSIONAL(ID);
ALTER TABLE REPASSE_FATURAS ADD COLUMN VALOR_CALCULO_MANUTENCAO NUMERIC(5, 2) NULL;
-------------------------------
INSERT INTO SEG_OBJETO(OBJ_INT_CODPAI, OBJ_STR_DES, OBJ_CHA_STS, OBJ_STR_CAMINHO,
                       SIS_INT_COD, OBJ_INT_ORDEM, OBJ_CHA_TIPO, OBJ_STR_ICON)
SELECT
	OBJ_INT_COD, 'Movimentações',
	'A', 'movimentacoes.jsf',
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
		WHERE OBJ_STR_DES LIKE 'Movimentações'
		LIMIT 1
	),
	OP.ID_PROFISSIONAL
FROM OBJETO_PROFISSIONAL OP
LEFT JOIN SEG_OBJETO O
	ON O.OBJ_INT_COD = OP.OBJ_INT_COD
WHERE O.OBJ_STR_DES ILIKE 'Pagamentos/Recebimentos';

ALTER TABLE LANCAMENTO ADD COLUMN PAGAMENTO_CONFERIDO VARCHAR(1) NOT NULL DEFAULT 'N';
ALTER TABLE LANCAMENTO ADD COLUMN CONFERIDO_POR BIGINT NULL REFERENCES PROFISSIONAL(ID);
ALTER TABLE LANCAMENTO ADD COLUMN DATA_CONFERIDO TIMESTAMP(10) NULL;
ALTER TABLE FATURA_ITEM ADD COLUMN OBSERVACAO TEXT NULL;
ALTER TABLE SEG_EMPRESA ADD COLUMN VALIDAR_REPASSE_CONFERE_CUSTO_DIRETO VARCHAR(1) NOT NULL DEFAULT 'N';
ALTER TABLE SEG_EMPRESA ADD COLUMN REPASSE_ADICIONA_TRIBUTOS VARCHAR(1) NOT NULL DEFAULT 'S';
ALTER TABLE REPASSE_FATURAS ADD COLUMN SUBTRAI_IMPOSTO_REPASSE VARCHAR(1) NOT NULL DEFAULT 'N';
ALTER TABLE REPASSE_FATURAS ADD COLUMN VALOR_IMPOSTO NUMERIC(11, 2) NULL;
ALTER TABLE PLANO_TRATAMENTO_PROCEDIMENTO ADD COLUMN CUSTO_DIRETO_VALIDO VARCHAR(1) NOT NULL DEFAULT 'N';
ALTER TABLE PLANO_TRATAMENTO_PROCEDIMENTO ADD COLUMN DATA_CUSTO_DIRETO_VALIDADO TIMESTAMP(10) NULL;
ALTER TABLE PLANO_TRATAMENTO_PROCEDIMENTO ADD COLUMN CUSTO_DIRETO_VALIDADO_POR BIGINT REFERENCES PROFISSIONAL(ID) NULL;

--//TODO ESSES LOCAIS DEVEM SER INSERIDOS PARA TODAS AS EMPRESAS E TAMBEM COMO DEFAULT NA CRIACAO DE NOVAS EMPRESAS
INSERT INTO local (descricao, tipo, id_empresa, excluido) SELECT distinct 'CANCELAMENTO_RESERVA','SI',emp_int_cod,'N' FROM seg_empresa;
INSERT INTO local (descricao, tipo, id_empresa, excluido) SELECT distinct 'EXCLUSAO_RESERVA','SI',emp_int_cod,'N' FROM seg_empresa;
INSERT INTO local (descricao, tipo, id_empresa, excluido) SELECT distinct 'EXCLUSAO_RESERVA_KIT','SI',emp_int_cod,'N' FROM seg_empresa;
INSERT INTO local (descricao, tipo, id_empresa, excluido) SELECT distinct 'DEVOLUCAO_KIT','SI',emp_int_cod,'N' FROM seg_empresa;
INSERT INTO local (descricao, tipo, id_empresa, excluido) SELECT distinct 'COMPRA','SI',emp_int_cod,'N' FROM seg_empresa;
INSERT INTO local (descricao, tipo, id_empresa, excluido) SELECT distinct 'KIT_NAO_UTILIZADO','SI',emp_int_cod,'N' FROM seg_empresa;
INSERT INTO local (descricao, tipo, id_empresa, excluido) SELECT distinct 'KIT_UTILIZADO_DEVOLVIDO','SI',emp_int_cod,'N' FROM seg_empresa;
INSERT INTO local (descricao, tipo, id_empresa, excluido) SELECT distinct 'DEVOLUCAO_KIT_LAVAGEM','SI',emp_int_cod,'N' FROM seg_empresa;
INSERT INTO local (descricao, tipo, id_empresa, excluido) SELECT distinct 'EMPRESTIMO_KIT','SI',emp_int_cod,'N' FROM seg_empresa;
INSERT INTO local (descricao, tipo, id_empresa, excluido) SELECT distinct 'CANCELAMENTO_EMPRESTIMO_KIT','SI',emp_int_cod,'N' FROM seg_empresa;
INSERT INTO local (descricao, tipo, id_empresa, excluido) SELECT distinct 'DEVOLUCAO_UNITARIA','SI',emp_int_cod,'N' FROM seg_empresa;
INSERT INTO local (descricao, tipo, id_empresa, excluido) SELECT distinct 'EMPRESTIMO_UNITARIO','SI',emp_int_cod,'N' FROM seg_empresa;
INSERT INTO local (descricao, tipo, id_empresa, excluido) SELECT distinct 'MATERIAL_ESTERELIZADO_DEVOLUCAO_LAVAGEM','SI',emp_int_cod,'N' FROM seg_empresa;
INSERT INTO local (descricao, tipo, id_empresa, excluido) SELECT distinct 'ENTREGA_LAVAGEM_MANUAL','SI',emp_int_cod,'N' FROM seg_empresa;
INSERT INTO local (descricao, tipo, id_empresa, excluido) SELECT distinct 'FINALIZACAO_DEVOLUCAO_LAVAGEM','SI',emp_int_cod,'N' FROM seg_empresa;
INSERT INTO local (descricao, tipo, id_empresa, excluido) SELECT distinct 'DESCARTE','SI',emp_int_cod,'N' FROM seg_empresa;
INSERT INTO local (descricao, tipo, id_empresa, excluido) SELECT distinct 'DEVOLUCAO_MATERIAL','SI',emp_int_cod,'N' FROM seg_empresa;

alter table material alter column id_local drop not null; 

alter table material alter column quantidade_unidade drop not null;

-------------------------------
ALTER TABLE FATURA ADD COLUMN PROFISSIONAL_CRIACAO_ID BIGINT NULL REFERENCES PROFISSIONAL(ID);
-------------------------------
INSERT INTO seg_objeto (obj_int_cod, obj_int_codpai, obj_str_des, obj_cha_sts, obj_str_caminho,sis_int_cod, obj_int_ordem, obj_cha_tipo, obj_str_icon)
			values (170, 11, 'Pagamentos e Recebimentos', 'A', 'pagamentoRecebimentoFatura.jsf',123, 6, 'T', null);
-------------------------------
INSERT INTO objeto_profissional (obj_int_cod,id_profissional) 
SELECT 170,id_profissional FROM objeto_profissional WHERE obj_int_cod = 149;
-------------------------------
ALTER TABLE fatura ADD COLUMN fornecedor_id bigint REFERENCES FORNECEDOR(ID);
-------------------------------
ALTER TABLE conta ADD COLUMN fornecedor_id bigint REFERENCES FORNECEDOR(ID);
-------------------------------
ALTER TABLE fatura ADD COLUMN origem_id bigint REFERENCES ORIGEM(ID);
-------------------------------
ALTER TABLE conta ADD COLUMN origem_id bigint REFERENCES ORIGEM(ID);
-------------------------------
ALTER TABLE fatura ADD COLUMN id_empresa bigint REFERENCES SEG_EMPRESA(EMP_INT_COD);
----

alter table SEG_USUARIO alter column USU_STR_SENHA drop not null; 

-------

update item set excluido = 'S',data_exclusao = current_timestamp where id_item_pai in (
select id from item where id_item_pai in (
select id from item where id_item_pai in (
select id from item where id_item_pai in (
select id from item
where id_item_pai in ( select id from item where 
descricao in
(	
'BIO MATERIAIS',
'MATERIAIS DE ACABAMENTO E POLIMENTO (Para Compósitos)',
'RESINA COMPOSTA',
'RESINA COMPOSTA INCISAL',
'RESINA COMPOSTA MICROPARTICULADA',
'RESINA COMPOSTA NANOPARTICULADA',
'RESINA COMPOSTA POSTERIOR',
'RESINA FLUIDA (FLOW)',
'EQUIPAMENTOS ELETRÔNICOS',
'COMPONENTES PROTÉTICOS E DE MOLDAGEM',
'ALVIM TI',
'DRIVE TI',
'DRIVE TI ACQUA',
'TITAMAX EX TI',
'TITAMAX EX TI ACQUA',
'TITAMAX TI CORTICAL',
'TITAMAX WS CORTICAL',
'TITAMAX WS MEDULAR',
'MINI IMPLANTES - ANCORAGEM ORTODÔNTICA',
'RECIPIENTES PLÁSTICOS',
'EMBALAGENS PLÁSTICAS',
'LUVAS CIRÚRGICAS',
'SORO FISIOLÓGICO',
'MATERIAIS DE LIMPEZA',
'MATERIAL ESTERILIZAÇÃO	',
'MATERIAL RAIO-X',
'FIOS ORTODONTICOS'
) and id_empresa = 41
	)))));
	
	
update item set excluido = 'S',data_exclusao = current_timestamp where id_item_pai in (
select id from item where id_item_pai in (
select id from item where id_item_pai in (
select id from item
where id_item_pai in ( select id from item where 
descricao in
(	
'BIO MATERIAIS',
'MATERIAIS DE ACABAMENTO E POLIMENTO (Para Compósitos)',
'RESINA COMPOSTA',
'RESINA COMPOSTA INCISAL',
'RESINA COMPOSTA MICROPARTICULADA',
'RESINA COMPOSTA NANOPARTICULADA',
'RESINA COMPOSTA POSTERIOR',
'RESINA FLUIDA (FLOW)',
'EQUIPAMENTOS ELETRÔNICOS',
'COMPONENTES PROTÉTICOS E DE MOLDAGEM',
'ALVIM TI',
'DRIVE TI',
'DRIVE TI ACQUA',
'TITAMAX EX TI',
'TITAMAX EX TI ACQUA',
'TITAMAX TI CORTICAL',
'TITAMAX WS CORTICAL',
'TITAMAX WS MEDULAR',
'MINI IMPLANTES - ANCORAGEM ORTODÔNTICA',
'RECIPIENTES PLÁSTICOS',
'EMBALAGENS PLÁSTICAS',
'LUVAS CIRÚRGICAS',
'SORO FISIOLÓGICO',
'MATERIAIS DE LIMPEZA',
'MATERIAL ESTERILIZAÇÃO	',
'MATERIAL RAIO-X',
'FIOS ORTODONTICOS'
) and id_empresa = 41
	))));
	
update item set excluido = 'S',data_exclusao = current_timestamp where id_item_pai in (
select id from item where id_item_pai in (
select id from item
where id_item_pai in ( select id from item where 
descricao in
(	
'BIO MATERIAIS',
'MATERIAIS DE ACABAMENTO E POLIMENTO (Para Compósitos)',
'RESINA COMPOSTA',
'RESINA COMPOSTA INCISAL',
'RESINA COMPOSTA MICROPARTICULADA',
'RESINA COMPOSTA NANOPARTICULADA',
'RESINA COMPOSTA POSTERIOR',
'RESINA FLUIDA (FLOW)',
'EQUIPAMENTOS ELETRÔNICOS',
'COMPONENTES PROTÉTICOS E DE MOLDAGEM',
'ALVIM TI',
'DRIVE TI',
'DRIVE TI ACQUA',
'TITAMAX EX TI',
'TITAMAX EX TI ACQUA',
'TITAMAX TI CORTICAL',
'TITAMAX WS CORTICAL',
'TITAMAX WS MEDULAR',
'MINI IMPLANTES - ANCORAGEM ORTODÔNTICA',
'RECIPIENTES PLÁSTICOS',
'EMBALAGENS PLÁSTICAS',
'LUVAS CIRÚRGICAS',
'SORO FISIOLÓGICO',
'MATERIAIS DE LIMPEZA',
'MATERIAL ESTERILIZAÇÃO	',
'MATERIAL RAIO-X',
'FIOS ORTODONTICOS'
) and id_empresa = 41
	)));	
	
update item set excluido = 'S',data_exclusao = current_timestamp where id_item_pai in (
select id from item
where id_item_pai in ( select id from item where 
descricao in
(	
'BIO MATERIAIS',
'MATERIAIS DE ACABAMENTO E POLIMENTO (Para Compósitos)',
'RESINA COMPOSTA',
'RESINA COMPOSTA INCISAL',
'RESINA COMPOSTA MICROPARTICULADA',
'RESINA COMPOSTA NANOPARTICULADA',
'RESINA COMPOSTA POSTERIOR',
'RESINA FLUIDA (FLOW)',
'EQUIPAMENTOS ELETRÔNICOS',
'COMPONENTES PROTÉTICOS E DE MOLDAGEM',
'ALVIM TI',
'DRIVE TI',
'DRIVE TI ACQUA',
'TITAMAX EX TI',
'TITAMAX EX TI ACQUA',
'TITAMAX TI CORTICAL',
'TITAMAX WS CORTICAL',
'TITAMAX WS MEDULAR',
'MINI IMPLANTES - ANCORAGEM ORTODÔNTICA',
'RECIPIENTES PLÁSTICOS',
'EMBALAGENS PLÁSTICAS',
'LUVAS CIRÚRGICAS',
'SORO FISIOLÓGICO',
'MATERIAIS DE LIMPEZA',
'MATERIAL ESTERILIZAÇÃO	',
'MATERIAL RAIO-X',
'FIOS ORTODONTICOS'
) and id_empresa = 41
	));		
	
	
update item set excluido = 'S',data_exclusao = current_timestamp where id_item_pai in ( select id from item where 
descricao in
(	
'BIO MATERIAIS',
'MATERIAIS DE ACABAMENTO E POLIMENTO (Para Compósitos)',
'RESINA COMPOSTA',
'RESINA COMPOSTA INCISAL',
'RESINA COMPOSTA MICROPARTICULADA',
'RESINA COMPOSTA NANOPARTICULADA',
'RESINA COMPOSTA POSTERIOR',
'RESINA FLUIDA (FLOW)',
'EQUIPAMENTOS ELETRÔNICOS',
'COMPONENTES PROTÉTICOS E DE MOLDAGEM',
'ALVIM TI',
'DRIVE TI',
'DRIVE TI ACQUA',
'TITAMAX EX TI',
'TITAMAX EX TI ACQUA',
'TITAMAX TI CORTICAL',
'TITAMAX WS CORTICAL',
'TITAMAX WS MEDULAR',
'MINI IMPLANTES - ANCORAGEM ORTODÔNTICA',
'RECIPIENTES PLÁSTICOS',
'EMBALAGENS PLÁSTICAS',
'LUVAS CIRÚRGICAS',
'SORO FISIOLÓGICO',
'MATERIAIS DE LIMPEZA',
'MATERIAL ESTERILIZAÇÃO	',
'MATERIAL RAIO-X',
'FIOS ORTODONTICOS'
) and id_empresa = 41
	);		
	
	
		
update item set excluido = 'S',data_exclusao = current_timestamp where
descricao in
(	
'BIO MATERIAIS',
'MATERIAIS DE ACABAMENTO E POLIMENTO (Para Compósitos)',
'RESINA COMPOSTA',
'RESINA COMPOSTA INCISAL',
'RESINA COMPOSTA MICROPARTICULADA',
'RESINA COMPOSTA NANOPARTICULADA',
'RESINA COMPOSTA POSTERIOR',
'RESINA FLUIDA (FLOW)',
'EQUIPAMENTOS ELETRÔNICOS',
'COMPONENTES PROTÉTICOS E DE MOLDAGEM',
'ALVIM TI',
'DRIVE TI',
'DRIVE TI ACQUA',
'TITAMAX EX TI',
'TITAMAX EX TI ACQUA',
'TITAMAX TI CORTICAL',
'TITAMAX WS CORTICAL',
'TITAMAX WS MEDULAR',
'MINI IMPLANTES - ANCORAGEM ORTODÔNTICA',
'RECIPIENTES PLÁSTICOS',
'EMBALAGENS PLÁSTICAS',
'LUVAS CIRÚRGICAS',
'SORO FISIOLÓGICO',
'MATERIAIS DE LIMPEZA',
'MATERIAL ESTERILIZAÇÃO	',
'MATERIAL RAIO-X',
'FIOS ORTODONTICOS'
) and id_empresa = 41;

update item set excluido = 'S',data_exclusao = current_timestamp 
where descricao IN ('1','2') and  id_empresa = 41
	
INSERT INTO local (descricao, tipo, id_empresa, excluido) SELECT distinct 'KIT_UTILIZADO_CONSUMIDO','SI',emp_int_cod,'N' FROM seg_empresa;	
INSERT INTO local (descricao, tipo, id_empresa, excluido) SELECT distinct 'DESCARTAR_LAVAGEM','SI',emp_int_cod,'N' FROM seg_empresa;
INSERT INTO local (descricao, tipo, id_empresa, excluido) SELECT distinct 'DESCARTAR_ESTERILIZACAO','SI',emp_int_cod,'N' FROM seg_empresa;
CREATE TABLE MATERIAL_CONSUMIDO (
	ID SERIAL PRIMARY KEY,
	MATERIAL_ID BIGINT NOT NULL REFERENCES MATERIAL(ID),
	PLANO_TRATAMENTO_ID BIGINT NOT NULL REFERENCES PLANO_TRATAMENTO(ID),
	PROCEDIMENTO_ID BIGINT NOT NULL REFERENCES PROCEDIMENTO(ID),
	PROFISSIONAL_ID BIGINT NOT NULL REFERENCES PROFISSIONAL(ID),
	EMPRESA_ID BIGINT NOT NULL REFERENCES SEG_EMPRESA(EMP_INT_COD),
	PACIENTE_ID BIGINT NOT NULL REFERENCES PACIENTE(ID),
	QUANTIDADE NUMERIC(10,2),
	DATA TIMESTAMP(10)
);

alter table MATERIAL_CONSUMIDO alter column MATERIAL_ID drop not null; 
alter table MATERIAL_CONSUMIDO alter column PLANO_TRATAMENTO_ID drop not null;
alter table MATERIAL_CONSUMIDO alter column PROCEDIMENTO_ID drop not null;
alter table MATERIAL_CONSUMIDO alter column PROFISSIONAL_ID drop not null;
alter table MATERIAL_CONSUMIDO alter column EMPRESA_ID drop not null;
alter table MATERIAL_CONSUMIDO alter column PACIENTE_ID drop not null;



                     
                    
                     