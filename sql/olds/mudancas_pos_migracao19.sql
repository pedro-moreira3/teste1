
drop table "inteli"."dente_periograma";
drop table "inteli"."periograma";

CREATE TABLE "inteli"."periograma"
(
   id serial PRIMARY KEY NOT NULL,
   id_paciente bigint NOT NULL,
   observacoes varchar(200),
   data_cadastro timestamp,
   excluido char(1) DEFAULT 'N'::bpchar,
   data_exclusao timestamp,
   excluido_por bigint
)
;
CREATE UNIQUE INDEX "periograma_idx" ON "inteli"."periograma"(id)
;

CREATE TABLE "inteli"."dente_periograma"
(
   id bigserial PRIMARY KEY NOT NULL,
   dente int4,
   id_periograma bigint NOT NULL,
   face varchar(20),
   mobilidade integer,
   implante bool,
   furca integer,
   sangramento_1 bool,
   sangramento_2 bool,
   sangramento_3 bool,
   placa_1 bool,
   placa_2 bool,
   placa_3 bool,
   margem_gengival_1 integer,
   margem_gengival_2 integer,
   margem_gengival_3 integer,
   profundidade_sondagem_1 integer,
   profundidade_sondagem_2 integer,
   profundidade_sondagem_3 integer
);

ALTER TABLE "inteli"."dente_periograma" ADD CONSTRAINT "dente_periograma" FOREIGN KEY (id_periograma) REFERENCES "inteli"."periograma"(id);

CREATE UNIQUE INDEX "dente_periograma_idx" ON "inteli"."dente_periograma"(id);