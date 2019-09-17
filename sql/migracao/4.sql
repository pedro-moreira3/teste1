MIGRACAO EM MATERIAL MB

ALTER TABLE MATERIAL_INDISPONIVEL RENAME TO EMPRESTIMO_KIT;

ALTER TABLE EMPRESTIMO_KIT DROP COLUMN VALOR;

ALTER TABLE EMPRESTIMO_KIT DROP COLUMN DATA_EXCLUSAO;

ALTER TABLE EMPRESTIMO_KIT DROP COLUMN EXCLUIDO_POR;

ALTER TABLE EMPRESTIMO_KIT DROP COLUMN EXCLUIDO;

ALTER TABLE ESTERILIZACAO_KIT RENAME COLUMN ID_MATERIAL_INDISPONIVEL TO EMPRESTIMO_KIT_ID;

ALTER TABLE ABASTECIMENTO RENAME TO EMPRESTIMO_UNITARIO;

ALTER TABLE EMPRESTIMO_UNITARIO DROP COLUMN VALOR;

ALTER TABLE EMPRESTIMO_UNITARIO DROP COLUMN DATA_EXCLUSAO;

ALTER TABLE EMPRESTIMO_UNITARIO DROP COLUMN EXCLUIDO_POR;

ALTER TABLE EMPRESTIMO_UNITARIO DROP COLUMN EXCLUIDO;

ALTER TABLE LAVAGEM_KIT RENAME COLUMN ID_ABASTECIMENTO TO EMPRESTIMO_UNITARIO_ID;

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
insert into local (descricao, tipo, id_empresa, excluido) values ('EM LAVAGEM','EM',41,'N');
insert into local (descricao, tipo, id_empresa, excluido) values ('EM ESTERILIZACAO','EM',41,'N');
insert into local (descricao, tipo, id_empresa, excluido) values ('EMPRESTADO KIT','EM',41,'N');
insert into local (descricao, tipo, id_empresa, excluido) values ('EMPRESTADO UNITARIO','EM',41,'N');

insert into local (descricao, tipo, id_empresa, excluido) values ('DESCARTE','DE',41,'N');

insert into local (descricao, tipo, id_empresa, excluido) values ('AJUSTE','AJ',41,'N');


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