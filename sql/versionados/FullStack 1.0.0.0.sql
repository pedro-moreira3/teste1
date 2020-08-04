ALTER TABLE fatura_item ADD COLUMN VALOR_AJUSTE_MANUAL NUMERIC;

ALTER TABLE fatura ADD COLUMN VALOR_RESTANTE_IGNORADO_AJUSTE_MANUAL boolean;

CREATE TABLE log_importacao(
	id serial primary key,
	data_importacao timestamp without time zone DEFAULT now(),
	profissional_responsavel_id bigint NOT NULL REFERENCES PROFISSIONAL(id),
	id_empresa bigint NOT NULL DEFAULT 1,
	template text,
	registros_importados bigint,
	tipo_importacao varchar
);

CREATE TABLE log_erro_importacao(
	id serial primary key,
	numero_linha bigint,
	linha text,
	erro text,
	log_importacao_id bigint NOT NULL REFERENCES LOG_IMPORTACAO(id)
);

insert into seg_objeto (obj_int_codpai, obj_str_des, obj_cha_sts, obj_str_caminho, sis_int_cod, 
						obj_int_ordem, obj_cha_tipo, obj_str_icon)
	values (9,'Importação', 'A', 'importacao.jsf', 123, 9, 'T', null) commit
	
INSERT INTO OBJETO_PROFISSIONAL(OBJ_INT_COD, ID_PROFISSIONAL)
SELECT
	(
		SELECT OBJ_INT_COD FROM SEG_OBJETO
		WHERE OBJ_STR_DES = 'Importação'
		LIMIT 1
	),
	OP.ID_PROFISSIONAL
FROM OBJETO_PROFISSIONAL OP
LEFT JOIN SEG_OBJETO O
	ON O.OBJ_INT_COD = OP.OBJ_INT_COD
WHERE O.OBJ_STR_DES = 'Paciente';

ALTER TABLE paciente ADD COLUMN log_importacao_id bigint REFERENCES LOG_IMPORTACAO(id);

--acima ja rodado----

alter table recibo_repasse_profissional add column data_pagamento timestamp;