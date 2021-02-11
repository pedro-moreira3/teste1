alter table negociacao_fatura add column indice_reajuste_id bigint references indice_reajuste(id);
alter table indice_reajuste add column desconto_reajuste numeric;
alter table orcamento_item add column valor_reajustado numeric;
alter table plano_tratamento add column usa_igpm varchar(1) default 'S';
alter table indice_reajuste add column tipo_reajuste varchar(1) default 'P';