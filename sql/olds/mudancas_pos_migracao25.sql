UPDATE plano SET nome_paypal= 'intelidente_basico' where nome_paypal='Basico';
UPDATE plano SET nome_paypal= 'intelidente_inicial' where nome_paypal='Inicial';
UPDATE plano SET nome_paypal= 'intelidente_plus' where nome_paypal='Plus';
UPDATE plano SET nome_paypal= 'intelidente_pro' where nome_paypal='Pro';

ALTER TABLE SEG_EMPRESA DROP COLUMN EMP_STR_ASSINATURAID;

update seg_empresa set emp_cha_cep = '81630-180' where emp_cha_cep is null;


--alter table "inteli".plano_tratamento add column alterouvaloresdesconto bool default false;
--alter table "inteli".seg_empresa add column emp_bol_retirarmascaratelefone bool default false;