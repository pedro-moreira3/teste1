CREATE TABLE CONFIGURACOES (
	VERSAO NUMERIC(25),
	DATA_VERSAO TIMESTAMP(10)	
);

insert into SEG_OBJETO (obj_str_des,obj_cha_sts,obj_str_caminho,sis_int_cod,obj_int_ordem,obj_cha_tipo,obj_str_icon)
values ('Clínicas','A','parceiroClinicas.jsf',123,1,'L','fa fa-users-md');

insert into seg_perfil (sis_int_cod,per_str_des,per_cha_sts) values (123,'Parceiro','A')

--INSERINDO O USUARIO DO FARUK COMO PARCEIRO
INSERT INTO seg_perusuario (PER_INT_COD,USU_INT_COD) 
	SELECT
	(
		SELECT PER_INT_COD FROM seg_perfil WHERE per_str_des = 'Parceiro'
	)
	,1036;
	
insert into OBJETO_PROFISSIONAL (obj_int_cod,id_profissional) 
	SELECT (
		select obj_int_cod from SEG_OBJETO where obj_str_caminho = 'parceiroClinicas.jsf'
	),
	994;

--ULTIMO SQL DO SCRIPT SEMPRE DEVE SER NA VERSAO DA CONFIGURAÇÃO, COM O MESMO NÚMERO DO ARQUIVO.

INSERT INTO CONFIGURACOES (VERSAO,DATA_VERSAO) VALUES (1,NOW())

ALTER TABLE paciente ADD COLUMN CARTEIRA_CONVENIO_TITULAR varchar(255);
ALTER TABLE dados_basicos ADD COLUMN EMPRESA_ONDE_TRABALHA varchar(255);
ALTER TABLE dados_basicos ADD COLUMN PROFISSAO varchar(255);

insert into seg_objeto (obj_int_codpai, obj_str_des, obj_cha_sts, obj_str_caminho, sis_int_cod, 
						obj_int_ordem, obj_cha_tipo, obj_str_icon)
values (172,'Aniversariantes','A','aniversariantes.jsf',123,1,'T',null);

update seg_objeto set obj_int_ordem = 2 where obj_int_cod = 158;

insert into objeto_profissional (obj_int_cod,id_profissional) 
select 173,id_profissional from objeto_profissional where obj_int_cod = 90;

insert into seg_perobjeto (per_int_cod,obj_int_cod)  
select per_int_cod,173 from seg_perobjeto where obj_int_cod = 90;
  