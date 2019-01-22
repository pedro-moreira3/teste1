
ALTER TABLE LANCAMENTO ADD COLUMN validado CHAR(1) DEFAULT 'S';

ALTER TABLE LANCAMENTO ADD COLUMN data_validado timestamp;

ALTER TABLE LANCAMENTO ADD COLUMN validado_por int8;

update lancamento l
set validado = 'S'; 

update lancamento l set validado = 'N' where l.data_credito >= '2017-06-01' and l.forma_pagamento in ('CC','CD');

ALTER TABLE PLANO_TRATAMENTO_PROCEDIMENTO_CUSTO ADD COLUMN ID_ABASTECIMENTO INT8;