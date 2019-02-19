--ALTER TABLE agendamento ADD COLUMN auxiliar boolean default false;

INSERT INTO dominio (objeto,tipo,nome,valor,id_empresa,editavel,excluido,data_exclusao,excluido_por) VALUES ('documento','tipo','Recibo','RC',41,true,'N',null,null);

CREATE TABLE "inteli"."recibo"
(
   id serial PRIMARY KEY NOT NULL,
   data_hora timestamp NOT NULL,
   id_paciente bigint NOT NULL,
   id_profissional bigint NOT NULL,
   documento_gerado text NOT NULL,
   excluido char(1) DEFAULT 'N'::bpchar,
   data_exclusao timestamp,
   excluido_por bigint
)
;
CREATE UNIQUE INDEX pk_recibo ON "inteli"."recibo"(id)
;
