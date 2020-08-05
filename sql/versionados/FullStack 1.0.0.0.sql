ALTER TABLE fatura_item ADD COLUMN VALOR_AJUSTE_MANUAL NUMERIC;

ALTER TABLE fatura ADD COLUMN VALOR_RESTANTE_IGNORADO_AJUSTE_MANUAL boolean;


alter table recibo_repasse_profissional add column data_pagamento timestamp;

CREATE TABLE USUARIO_AFILIACAO (
	ID SERIAL PRIMARY KEY,
	USUARIO_ID bigint references SEG_USUARIO(USU_INT_COD),	
	AFILIACAO_ID bigint references AFILIACAO(ID),
	DATA_AFILIACAO timestamp NOT NULL,	
	CRIADO_POR bigint REFERENCES SEG_USUARIO(USU_INT_COD),
	STATUS character DEFAULT 'A'
);

CREATE TABLE log_importacao(
	id serial primary key,
	data_importacao timestamp without time zone DEFAULT now(),
	profissional_responsavel_id bigint NOT NULL REFERENCES PROFISSIONAL(id),
	id_empresa bigint NOT NULL DEFAULT 1,
	template text,
	registros_importados bigint,
	tipo_importacao varchar
);

CREATE TABLE log_erro_importacao(
	id serial primary key,
	numero_linha bigint,
	linha text,
	erro text,
	log_importacao_id bigint NOT NULL REFERENCES LOG_IMPORTACAO(id)
);

insert into seg_objeto (obj_int_codpai, obj_str_des, obj_cha_sts, obj_str_caminho, sis_int_cod, 
						obj_int_ordem, obj_cha_tipo, obj_str_icon)
	values (9,'Importação', 'A', 'importacao.jsf', 123, 9, 'T', null) commit
	
INSERT INTO OBJETO_PROFISSIONAL(OBJ_INT_COD, ID_PROFISSIONAL)
SELECT
	(
		SELECT OBJ_INT_COD FROM SEG_OBJETO
		WHERE OBJ_STR_DES = 'Importação'
		LIMIT 1
	),
	OP.ID_PROFISSIONAL
FROM OBJETO_PROFISSIONAL OP
LEFT JOIN SEG_OBJETO O
	ON O.OBJ_INT_COD = OP.OBJ_INT_COD
WHERE O.OBJ_STR_DES = 'Paciente';

ALTER TABLE paciente ADD COLUMN log_importacao_id bigint REFERENCES LOG_IMPORTACAO(id);

alter table seg_empresa add column id_iugu VARCHAR(100);
alter table seg_empresa add column assinatura_iugu VARCHAR(100);
alter table seg_empresa add column suspenso_iugu character DEFAULT 'N';

alter table seg_empresa add column BLOQUEADO character DEFAULT 'N';



ALTER TABLE plano add COLUMN nome_iugu  VARCHAR(500);

CREATE TABLE LOG_EMPRESA (
	ID SERIAL PRIMARY KEY,
	USUARIO_ID bigint references SEG_USUARIO(USU_INT_COD),	
	EMPRESA_ID bigint references SEG_EMPRESA(EMP_INT_COD),
	DATA timestamp NOT NULL,
	LOG VARCHAR(500) NOT NULL
);


alter table afiliacao add column valor_mensal NUMERIC(16, 4) DEFAULT 90;


--acima ja rodado----


--INSERINDO A GENTE E ALVARO POR ENQUANTO
INSERT INTO USUARIO_AFILIACAO (USUARIO_ID,AFILIACAO_ID,DATA_AFILIACAO,CRIADO_POR) VALUES 
(1036,1,current_timestamp,1036);

INSERT INTO USUARIO_AFILIACAO (USUARIO_ID,AFILIACAO_ID,DATA_AFILIACAO,CRIADO_POR) VALUES 
(1036,2,current_timestamp,1036);

INSERT INTO USUARIO_AFILIACAO (USUARIO_ID,AFILIACAO_ID,DATA_AFILIACAO,CRIADO_POR) VALUES 
(3701,2,current_timestamp,1036);

insert into SEG_OBJETO (obj_str_des,obj_cha_sts,obj_str_caminho,sis_int_cod,obj_int_ordem,obj_cha_tipo,obj_str_icon) values
('Clínicas','A','clinicas.jsf',123,1,'T','fa fa-user-md')	

--rodar pra pegar o id de cima
select * from SEG_OBJETO order by obj_int_cod desc

--insert into seg_perobjeto (per_int_cod,obj_int_cod)
--values(266,164);

insert into seg_perobjeto (per_int_cod,obj_int_cod)
values(328,164);


INSERT INTO SEG_OBJETO(OBJ_INT_CODPAI, OBJ_STR_DES, OBJ_CHA_STS, OBJ_STR_CAMINHO,
                       SIS_INT_COD, OBJ_INT_ORDEM, OBJ_CHA_TIPO, OBJ_STR_ICON)
SELECT
	OBJ_INT_COD, 'Relatório de Patrocinadores',
	'A', 'relatorioPatrocinador.jsf',
	123, MAX(OBJ_INT_ORDEM) + 1,
	'T', NULL
FROM SEG_OBJETO
WHERE OBJ_STR_DES = 'Plano'
GROUP BY OBJ_INT_COD, OBJ_INT_ORDEM
LIMIT 1;

insert into seg_perobjeto (obj_int_cod,per_int_cod)  
SELECT
	(
		SELECT OBJ_INT_COD FROM SEG_OBJETO
		WHERE OBJ_STR_DES = 'Relatório de Patrocinadores'
		LIMIT 1
	), 286
	
	
update SEG_OBJETO set obj_int_codpai = 1
		WHERE OBJ_STR_DES = 'Relatório de Patrocinadores'		
		
		
INSERT INTO SEG_OBJETO(OBJ_INT_CODPAI, OBJ_STR_DES, OBJ_CHA_STS, OBJ_STR_CAMINHO,
                       SIS_INT_COD, OBJ_INT_ORDEM, OBJ_CHA_TIPO, OBJ_STR_ICON)
SELECT
	OBJ_INT_COD, 'Mensalidades do Sistema',
	'A', 'mensal.jsf',
	123, MAX(OBJ_INT_ORDEM) + 1,
	'T', NULL
FROM SEG_OBJETO
WHERE OBJ_STR_DES = 'Movimentações'
GROUP BY OBJ_INT_COD, OBJ_INT_ORDEM
LIMIT 1;

INSERT INTO OBJETO_PROFISSIONAL(OBJ_INT_COD, ID_PROFISSIONAL)
SELECT
	(
		SELECT OBJ_INT_COD FROM SEG_OBJETO
		WHERE OBJ_STR_DES = 'Mensalidades do Sistema'
		LIMIT 1
	),
	OP.ID_PROFISSIONAL
FROM OBJETO_PROFISSIONAL OP
LEFT JOIN SEG_OBJETO O
	ON O.OBJ_INT_COD = OP.OBJ_INT_COD
WHERE O.OBJ_STR_DES = 'Movimentações';

update SEG_OBJETO set obj_int_codpai = 11 where OBJ_STR_DES = 'Mensalidades do Sistema'

--TODO verificarquem do QS para inserir em USUARIO_AFILIACAO

INSERT INTO AFILIACAO(NOME, ATIVO) VALUES('Lume Tecnologia', 'S');

--empresas do alvaro
update seg_empresa set afiliacao_id = 2 where
emp_int_cod in (283,89,190,99,188,121,304,217,160,
				530,215,194,473,212,191,269,139,
				366,105,515,443,540)

update seg_empresa set afiliacao_id = 3 where afiliacao_id is null

	
delete from plano where id <> 1;


update plano set nome_iugu = 'intelidente_plano_basico';



--ATENCAO, TOKEN DO PRODUCAO

--insert into iugu_config (token) values ('15dfd95f7455dbf568807ed46239a8f3')


