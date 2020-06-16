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

alter table paciente add column anotacoes text;
INSERT INTO motivo (id,descricao,tipo,excluido,data_exclusao,excluido_por,sigla,id_categoria,centro_custo,grupo,ir,id_conta,id_empresa,codigo_contify)
VALUES (247,'Extorno de recebimento','Pagar','N',null,null,'EP',29,'Variável','Custo Variável',false,null,null,null);

INSERT INTO motivo (id,descricao,tipo,excluido,data_exclusao,excluido_por,sigla,id_categoria,centro_custo,grupo,ir,id_conta,id_empresa,codigo_contify)
VALUES (248,'Extorno de pagamento','Receber','N',null,null,'ER',29,'Variável','Receita',true,null,null,null);

create table log_repasse(
	id serial PRIMARY KEY,
	data timestamp,
	tipo_operacao char VARYING(1),
	profissional_id bigint references profissional(id),
	repasse_faturas_id bigint references repasse_faturas(id)
)

insert into seg_perobjeto (per_int_cod,obj_int_cod)
values(326,167);

insert into seg_perobjeto (per_int_cod,obj_int_cod)
values(146,167);

insert into seg_perobjeto (per_int_cod,obj_int_cod)
values(326,171);

insert into seg_perobjeto (per_int_cod,obj_int_cod)
values(146,171);

delete from seg_perobjeto where obj_int_cod = 46;

CREATE TABLE DESCONTO_ORCAMENTO(
	ID serial primary key,
	DATA_CRIACAO timestamp NOT NULL,
	DATA_ULTIMA_ALTERACAO timestamp NOT NULL,
	CRIADO_POR bigint REFERENCES PROFISSIONAL(ID),
	ALTERADO_POR bigint REFERENCES PROFISSIONAL(ID),
	DESCONTO bigint,
	QUANTIDADE_PARCELAS bigint,
	ID_EMPRESA bigint,
	STATUS character DEFAULT 'A'
);

alter table desconto_orcamento add column ID_PROFISSIONAL bigint REFERENCES PROFISSIONAL(ID);
alter table desconto_orcamento add column TIPO_DESCONTO character;

insert into seg_objeto (obj_int_cod, obj_str_des, obj_cha_sts, obj_str_caminho, sis_int_cod, 
						obj_int_ordem, obj_cha_tipo, obj_str_icon)
values (172,'Relacionamento','A','LABEL_RAIZ',123,2,'L','fa fa-globe');

update seg_objeto set obj_int_ordem = 2 where obj_int_cod = 158;

insert into objeto_profissional (obj_int_cod,id_profissional) 
select 173,id_profissional from objeto_profissional where obj_int_cod = 90;

insert into seg_perobjeto (per_int_cod,obj_int_cod)  
select per_int_cod,173 from seg_perobjeto where obj_int_cod = 90;

update seg_objeto set obj_int_ordem = 3 where obj_str_des = 'Estoque';
update seg_objeto set obj_int_ordem = 4 where obj_str_des = 'Esterilização';
update seg_objeto set obj_int_ordem = 5 where obj_str_des = 'Financeiro';
update seg_objeto set obj_int_ordem = 6 where obj_str_des = 'Administrativo';
update seg_objeto set obj_int_ordem = 7 where obj_str_des = 'Emissão de Documentos';

update seg_objeto set obj_int_codpai = 172 and obj_int_ordem = 2 where obj_str_des = 'Relatório de Relacionamento';

alter table desconto_orcamento add column ID_PROFISSIONAL bigint REFERENCES PROFISSIONAL(ID);
alter table desconto_orcamento add column TIPO_DESCONTO character;

alter table paciente add column anotacoes text;
