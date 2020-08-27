alter table SEG_EMPRESA add column DATA_ATIVACAO timestamp;
	
alter table SEG_EMPRESA add column DATA_INATIVACAO timestamp;


update afiliacao set modalidade_contrato = 'Pré' where id = 3;
update afiliacao set modalidade_contrato = 'Mensal' where id = 1;
update afiliacao set modalidade_contrato = 'Comissao' where id = 2;


alter table afiliacao add column plano_iugu VARCHAR(50);
update afiliacao set plano_iugu = 'intelidente_plano_basico' where id = 3;

update afiliacao set plano_iugu = 'intelidente_plano_promocional' where id = 2;


alter table lancamento add column lancamento_extornado char(1) default 'N'
alter table seg_empresa add column FATURA_IUGU varchar(250);
update motivo set id_categoria = 40 where id = 247;



ALTER TABLE AFILIACAO ADD COLUMN PERCENTUAL  NUMERIC(16, 4) ;



--------ACIMA JA RODADO ------------

insert into seg_objeto (obj_int_codpai, obj_str_des, obj_cha_sts, obj_str_caminho, sis_int_cod, 
						obj_int_ordem, obj_cha_tipo, obj_str_icon)
	values (9,'Configuração de Anamnese', 'A', 'configuracaoAnamnese.jsf', 123, 9, 'T', null)
	
INSERT INTO OBJETO_PROFISSIONAL(OBJ_INT_COD, ID_PROFISSIONAL)
SELECT
	(
		SELECT OBJ_INT_COD FROM SEG_OBJETO
		WHERE OBJ_STR_DES = 'Configuração de Anamnese'
		LIMIT 1
	),
	OP.ID_PROFISSIONAL
FROM OBJETO_PROFISSIONAL OP
LEFT JOIN SEG_OBJETO O
	ON O.OBJ_INT_COD = OP.OBJ_INT_COD
WHERE O.OBJ_STR_DES = 'Cadastro de Perguntas';

update seg_objeto set obj_cha_sts = 'I' where obj_str_des = 'Cadastro de Perguntas';
update seg_objeto set obj_cha_sts = 'I' where obj_str_des = 'Cadastro de Respostas';
update seg_objeto set obj_cha_sts = 'I' where obj_str_des = 'Config. de Anamnese';


CREATE TABLE CONFIGURACAO_ANAMNESE (
	ID SERIAL PRIMARY KEY,
	ID_ESPECIALIDADE bigint references ESPECIALIDADE(ID)	
);

ALTER TABLE PERGUNTA ADD COLUMN ID_CONFIGURACAO_ANAMNESE bigint references CONFIGURACAO_ANAMNESE(ID);
COMMIT
INSERT INTO CONFIGURACAO_ANAMNESE (ID_ESPECIALIDADE)  
SELECT DISTINCT ID_ESPECIALIDADE FROM PERGUNTA WHERE ID_ESPECIALIDADE IN (
SELECT ID FROM ESPECIALIDADE
);

 --   SELECT 'UPDATE inteli.PERGUNTA SET ID_CONFIGURACAO_ANAMNESE = ' || CAST(C.ID AS VARCHAR) || ' WHERE ID_ESPECIALIDADE = ' || CAST(C.ID_ESPECIALIDADE AS VARCHAR) FROM inteli.PERGUNTA P INNER JOIN inteli.CONFIGURACAO_ANAMNESE C ON P.ID_ESPECIALIDADE = C.ID_ESPECIALIDADE GROUP BY 'UPDATE inteli.PERGUNTA SET ID_CONFIGURACAO_ANAMNESE = ' || CAST(C.ID AS VARCHAR) || ' WHERE ID_ESPECIALIDADE = ' || CAST(C.ID_ESPECIALIDADE AS VARCHAR);
​
   -- Essa query vai gerar uma porrada de update que ajusta o que vc precisa. Em dev tem alguns códigos sem referencia, para verificar segue query

  --  SELECT P.ID_ESPECIALIDADE, C.ID_ESPECIALIDADE, COUNT(*) FROM inteli.PERGUNTA P LEFT JOIN inteli.CONFIGURACAO_ANAMNESE C ON P.ID_ESPECIALIDADE = C.ID_ESPECIALIDADE WHERE C.ID_ESPECIALIDADE IS NULL GROUP BY P.ID_ESPECIALIDADE, C.ID_ESPECIALIDADE;


--TODO DEPOIS EXCLUIR ID_ESPECIALIDADE DA PERGUNTA



