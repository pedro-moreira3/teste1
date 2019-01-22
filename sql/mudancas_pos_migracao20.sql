alter table inteli.plano_tratamento add column id_convenio int8;

ALTER TABLE "inteli".plano_tratamento ADD CONSTRAINT plano_tratamento_convenio FOREIGN KEY (id_convenio) REFERENCES "inteli"."convenio"(id);

alter table "inteli".plano_tratamento add column bconvenio bool default false;

alter table "inteli".seg_empresa add column emp_bol_dentistaadmin bool default false;