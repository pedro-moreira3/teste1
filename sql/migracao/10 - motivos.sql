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


