ALTER TABLE fatura_item ADD COLUMN VALOR_AJUSTE_MANUAL NUMERIC;

ALTER TABLE fatura ADD COLUMN VALOR_RESTANTE_IGNORADO_AJUSTE_MANUAL boolean;

--acima ja rodado----

alter table recibo_repasse_profissional add column data_pagamento timestamp;