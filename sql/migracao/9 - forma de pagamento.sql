

alter table TARIFA add column periodicidade integer;
alter table TARIFA add column receber_automaticamente varchar(1) default 'N';
alter table TARIFA add column conta varchar(100);

alter table TARIFA alter column bandeira drop not null;
alter table TARIFA alter column taxa drop not null;
alter table TARIFA alter column tarifa drop not null;
alter table TARIFA alter column banco drop not null;
alter table TARIFA alter column agencia drop not null;

-----acima ja rodado -----------
update SEG_OBJETO set OBJ_STR_DES = 'Cadastro de Formas de Pagamento' where OBJ_STR_DES = 'Cadastro de Tarifas';

insert into tarifa (id_empresa,produto,prazo,parcela_minima,parcela_maxima,receber_automaticamente,status,tipo,excluido)
select emp_int_cod,'Dinheiro',0,1,1,'S','A','OU','N' from seg_empresa;

insert into tarifa (id_empresa,produto,prazo,parcela_minima,parcela_maxima,receber_automaticamente,status,tipo,excluido,periodicidade)
select emp_int_cod,'Cheque',0,1,36,'S','A','CH','N',30 from seg_empresa
