
alter table "inteli".plano_tratamento add column inicio date;

alter table "inteli".plano_tratamento add column fim date;

alter table "inteli".plano_tratamento add column meses integer;

alter table "inteli".orcamento add column valor_procedimento_ortodontico numeric(11,2);

alter table "inteli".plano_tratamento add column ortodontico bool default false;


------------------------------------------------------

INSERT INTO "inteli".dominio (objeto,tipo,nome,valor,id_empresa,editavel,excluido,data_exclusao,excluido_por) VALUES 
('ortodontia','procedimento_padrao','86000357','PP',0,false,'N',null,null);

create table "inteli".diagnostico_ortodontico
(
    "id" bigserial PRIMARY KEY NOT NULL,
    "descricao" varchar(100)
);

grant all on "inteli".diagnostico_ortodontico to inteli;

-------------------------------------------------------------------

create table "inteli".aparelho_ortodontico
(
    "id" bigserial PRIMARY KEY NOT NULL,
    "descricao" varchar(100)
);

grant all on "inteli".aparelho_ortodontico to inteli;

GRANT USAGE, SELECT ON SEQUENCE "inteli".aparelho_ortodontico_id_seq TO inteli;

-------------------------------------------------------------------
create table "inteli".plano_tratamento_diagnostico
(
	"id" bigserial PRIMARY KEY NOT NULL,
	"id_plano_tratamento" int8,
	"id_diagnostico" int8
);

alter table only "inteli".plano_tratamento_diagnostico
    add constraint plano_tratamento_diagnostico_plano_tratamento foreign key ("id_plano_tratamento") references "inteli".plano_tratamento("id");

alter table only "inteli".plano_tratamento_diagnostico
    add constraint plano_tratamento_diagnostico_diagnostico foreign key ("id_diagnostico") references "inteli".diagnostico_ortodontico("id");
    
grant all on "inteli".plano_tratamento_diagnostico to inteli;

diagnostico_ortodontico_id_seq

GRANT USAGE, SELECT ON SEQUENCE "inteli".diagnostico_ortodontico_id_seq TO inteli;


-------------------------------------------------------------------

create table "inteli".plano_tratamento_aparelho
(
	"id" bigserial PRIMARY KEY NOT NULL,
	"id_plano_tratamento" int8,
	"id_aparelho" int8
);


alter table only "inteli".plano_tratamento_aparelho
    add constraint plano_tratamento_aparelho_plano_tratamento foreign key ("id_plano_tratamento") references "inteli".plano_tratamento("id");
    
alter table only "inteli".plano_tratamento_aparelho
    add constraint plano_tratamento_aparelho_aparelho foreign key ("id_aparelho") references "inteli".aparelho_ortodontico("id");
        
grant all on "inteli".plano_tratamento_aparelho to inteli;


GRANT USAGE, SELECT ON SEQUENCE "inteli".plano_tratamento_aparelho_id_seq TO inteli;




-----------------------------------------------------------------------

insert into diagnostico_ortodontico (descricao) values('Classe I de angle');
insert into diagnostico_ortodontico (descricao) values('Classe II de angle');
insert into diagnostico_ortodontico (descricao) values('Classe II Divisao primeira');
insert into diagnostico_ortodontico (descricao) values('Classe II Divisao segunda');
insert into diagnostico_ortodontico (descricao) values('Classe II Subdivisão direita');
insert into diagnostico_ortodontico (descricao) values('Classe II Subdivisao esquerda');
insert into diagnostico_ortodontico (descricao) values('Classe III de angle');
insert into diagnostico_ortodontico (descricao) values('Classe III Subdivisão esquerda');
insert into diagnostico_ortodontico (descricao) values('Classe III Subdivisão direita');
insert into diagnostico_ortodontico (descricao) values('Atresia Maxilar');
insert into diagnostico_ortodontico (descricao) values('Mordida aberta posterior');
insert into diagnostico_ortodontico (descricao) values('Mordida aberta anterior');
insert into diagnostico_ortodontico (descricao) values('Mordida profunda');
insert into diagnostico_ortodontico (descricao) values('Biprotruso');
insert into diagnostico_ortodontico (descricao) values('Biretruso');
insert into diagnostico_ortodontico (descricao) values('Mordida cruzada posterior');
insert into diagnostico_ortodontico (descricao) values('Mordida cruzada posterior esquerda');
insert into diagnostico_ortodontico (descricao) values('Mordida cruzada posterior direita');
insert into diagnostico_ortodontico (descricao) values('Mordida cruzada posterior bilateral');
insert into diagnostico_ortodontico (descricao) values('Mordida cruzada anterior');
insert into diagnostico_ortodontico (descricao) values('Apinhamento dental');
insert into diagnostico_ortodontico (descricao) values('Diastema');
insert into diagnostico_ortodontico (descricao) values('Dente incluso com necessidade de tracionamento');


insert into aparelho_ortodontico (descricao) values('Aparelho fixo metálico');
insert into aparelho_ortodontico (descricao) values('Aparelho fixo metálico autoligável');
insert into aparelho_ortodontico (descricao) values('Aparelho fixo porcelana');
insert into aparelho_ortodontico (descricao) values('Aparelho fixo safira');
insert into aparelho_ortodontico (descricao) values('Aparelho fixo estético autoligável');
insert into aparelho_ortodontico (descricao) values('Aparelho fixo safira autoligável');
insert into aparelho_ortodontico (descricao) values('Aparatologia removível');
insert into aparelho_ortodontico (descricao) values('Placa de hawley com expansor mediano');
insert into aparelho_ortodontico (descricao) values('Mantenedor de espaço');
insert into aparelho_ortodontico (descricao) values('Niveladores estéticos');
insert into aparelho_ortodontico (descricao) values('Aparelhos ortopédicos');
insert into aparelho_ortodontico (descricao) values('Disjuntor maxilar');
insert into aparelho_ortodontico (descricao) values('Aparelho de Protração Mandibular');