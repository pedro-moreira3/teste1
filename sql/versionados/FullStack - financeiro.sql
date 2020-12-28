alter table seg_empresa add column 	QUANTIDADE_MESES_FATURA_RECORRENTE INTEGER default 12;

ALTER TABLE FATURA ADD COLUMN RECORRENTE VARCHAR(1) DEFAULT 'N';

ALTER TABLE FATURA ADD COLUMN OBSERVACAO VARCHAR(500);

alter table FATURA add column QUANTIDADE_RECORRENCIA INTEGER;

CREATE TABLE FATURA_RECORRENTE (
	ID SERIAL PRIMARY KEY,
	FATURA_ID bigint references FATURA(ID),	
	SALDO VARCHAR(50),	
	TIPO_PESSOA VARCHAR(50),	
	TARIFA_ID bigint references TARIFA(ID),
	MOTIVO_ID bigint references MOTIVO(ID),
	DESCRICAO VARCHAR(500),	
	QUANTIDADE_RECORRENCIA int,
	INTERVALO_RECORRENCIA int,
	REFERENCIA_RECORRENCIA bigint,	
	VALOR DECIMAL(11,2),
	STATUS character DEFAULT 'A'	
);

---acima ja executado---

alter table afiliacao drop column PLANO_IUGU;

INSERT INTO SEG_OBJETO(OBJ_INT_CODPAI, OBJ_STR_DES, OBJ_CHA_STS, OBJ_STR_CAMINHO,
                       SIS_INT_COD, OBJ_INT_ORDEM, OBJ_CHA_TIPO, OBJ_STR_ICON)
SELECT
	OBJ_INT_COD, 'Relatório de Pagamentos',
	'A', 'relatorioPagamento.jsf',
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
		WHERE OBJ_STR_DES = 'Relatório de Pagamentos'
		LIMIT 1
	),
	OP.ID_PROFISSIONAL
FROM OBJETO_PROFISSIONAL OP
LEFT JOIN SEG_OBJETO O
	ON O.OBJ_INT_COD = OP.OBJ_INT_COD
WHERE O.OBJ_STR_DES = 'Pagamentos/Recebimentos';

alter table TARIFA add column FORMA_PAGAMENTO char(1) default 'R'

alter table LANCAMENTO_CONTABIL add column DESCRICAO varchar(200);

alter table LANCAMENTO_CONTABIL add column OBSERVACAO varchar(200);

update seg_objeto set obj_int_codpai = 9 ,obj_int_ordem = 9,obj_str_des = 'Cadastro de Fornecedores' where obj_str_caminho ilike '%fornecedor%'

--TODO MIGRAR ORIGENS PARA FORNECEDORES, ESTA NO FORNECEDORESMB
--APOS MIGRAR REMOVER ORIGEM, ORIGEMBO E ORIGEMSINGLETON E TIRAR AS REFERENCIAS DISSO DO SISTEMA

-- verificar o codigo certo para inserir nesse caso
insert into seg_perobjeto (per_int_cod,obj_int_cod)
 select per_int_cod,180 from seg_perobjeto where obj_int_cod = 90
 
 
 alter table TIPO_CATEGORIA add column tipo varchar(200);
 
 update TIPO_CATEGORIA set tipo = 'Pagar' where descricao = 'Gastos Gerais';
 update TIPO_CATEGORIA set tipo = 'Pagar' where descricao = 'Gastos Operacionais';
 update TIPO_CATEGORIA set tipo = 'Pagar' where descricao = 'Gastos Odontológicos';
 update TIPO_CATEGORIA set tipo = 'Receber' where descricao = 'Receita Bruta';
 
 update seg_objeto set obj_int_codpai = 9 ,obj_int_ordem = 9,obj_str_des = 'Cadastro de Fornecedores' where obj_str_caminho ilike '%fornecedor%'

 update seg_objeto set obj_str_des = 'Configurações' where obj_str_des = 'Administrativo'
 
 
update seg_objeto set obj_int_codpai = 9 where obj_str_caminho = 'tarifa.jsf'


update seg_objeto set obj_int_ordem = 1  where obj_str_des = 'Recebimento dos Pacientes' and obj_cha_sts = 'A';
update seg_objeto set obj_int_ordem = 2  where obj_str_des = 'Registro de Custos Diretos' and obj_cha_sts = 'A';
update seg_objeto set obj_int_ordem = 3   where obj_str_des = 'Pagamentos/Recebimentos' and obj_cha_sts = 'A';
update seg_objeto set obj_int_ordem = 4  where obj_str_des = 'Repasse dos Profissionais' and obj_cha_sts = 'A';
update seg_objeto set obj_int_ordem = 5  where obj_str_des = 'Movimentações' and obj_cha_sts = 'A';
update seg_objeto set obj_int_ordem = 6  where obj_str_des = 'Mensalidades do Sistema' and obj_cha_sts = 'A';
update seg_objeto set obj_int_ordem = 7  where obj_str_des = 'Relatório de Pagamentos' and obj_cha_sts = 'A';
update seg_objeto set obj_int_ordem = 8  where obj_str_des = 'Relatório de Recebimentos' and obj_cha_sts = 'A';
update seg_objeto set obj_int_ordem = 9  where obj_str_des = 'Relatório de Repasses' and obj_cha_sts = 'A';
update seg_objeto set obj_int_ordem = 10  where obj_str_des = 'Relatório de Faturas' and obj_cha_sts = 'A';


update motivo set id_categoria = 34 where id = 3
update CATEGORIA_MOTIVO set id_tipo = 7 where id = 44

commit
 
 
 
 
 

