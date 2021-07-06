ALTER TABLE MATERIAL ADD COLUMN VALOR_UNIDADE_INFORMADO DECIMAL(10);

ALTER TABLE CONFERENCIA_MATERIAL ADD COLUMN DATA_CADASTRO DATE;
alter table CONFERENCIA_MATERIAL add column MOTIVO CHAR(20);

ALTER TABLE ITEM ADD COLUMN APLICACAO CHAR(1) DEFAULT 'I';



-- ja executei em prod
alter table procedimento_kit add column QUANTIDADE INTEGER

INSERT INTO SEG_OBJETO (OBJ_INT_CODPAI,OBJ_STR_DES,OBJ_CHA_STS,OBJ_STR_CAMINHO,SIS_INT_COD,OBJ_INT_ORDEM,OBJ_CHA_TIPO,OBJ_STR_ICON) VALUES 
((select OBJ_INT_COD from SEG_OBJETO where OBJ_STR_DES = 'Administração' and SIS_INT_COD = (select SIS_INT_COD from seg_sistema where SIS_CHA_SIGLA = 'ODONTO')),'Log Acesso','A','logacesso.jsf',(select SIS_INT_COD from seg_sistema where SIS_CHA_SIGLA = 'ODONTO'),1,'T','fa fa-users');

insert into SEG_PEROBJETO (PER_INT_COD, OBJ_INT_COD) values ( (select PER_INT_COD from SEG_PERFIL where PER_STR_DES = 'Administradores' and SIS_INT_COD = (select SIS_INT_COD from seg_sistema where SIS_CHA_SIGLA = 'ODONTO')),(select OBJ_INT_COD from SEG_OBJETO where OBJ_STR_DES = 'Log Acesso' and SIS_INT_COD = (select SIS_INT_COD from seg_sistema where SIS_CHA_SIGLA = 'ODONTO')))


delete from SEG_PEROBJETO where OBJ_INT_COD = (select OBJ_INT_COD from SEG_OBJETO where OBJ_STR_CAMINHO = 'documentoFaturamento.jsf')

delete from SEG_LOGACESSO where OBJ_INT_COD = (select OBJ_INT_COD from SEG_OBJETO where OBJ_STR_CAMINHO = 'documentoFaturamento.jsf')

delete from SEG_OBJETO where OBJ_STR_CAMINHO = 'documentoFaturamento.jsf'