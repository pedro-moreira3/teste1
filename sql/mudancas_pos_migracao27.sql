create table "inteli".profissional_ponto
(
    "id" bigserial PRIMARY KEY NOT NULL,
    "id_profissional" bigserial,
    "data" date,
    "entrada" time,
    "saida" time
);

grant all on "inteli".profissional_ponto to inteli;


alter table "inteli".seg_empresa add column emp_str_token_contify varchar (300)