alter table negociacao_fatura add column indice_reajuste_id bigint references indice_reajuste(id);
alter table indice_reajuste add column desconto_reajuste numeric;
alter table orcamento_item add column valor_reajustado numeric;