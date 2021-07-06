create table "material_log"
(
	"id" serial,
	"id_material_indisponivel" int8,
	"id_abastecimento" int8,
	"id_material" int8,
	"quantidade_alterada" int8,
	"quantidade_atual" int8,
	"acao" varchar(50),
	"id_profissional" int8,
	"data" timestamp,
    primary key ("id")
);


alter table only "material_log"
    add constraint material_log_mi foreign key ("id_material_indisponivel") references material_indisponivel("id");

alter table only "material_log"
    add constraint material_log_abastecimento foreign key ("id_abastecimento") references abastecimento("id");
    
alter table only "material_log"
    add constraint material_log_material foreign key ("id_material") references material("id");
    
alter table only "material_log"
    add constraint material_log_profissional foreign key ("id_profissional") references profissional("id");
    
grant all on material_log to inteli;

GRANT USAGE, SELECT ON SEQUENCE material_log_id_seq TO inteli;