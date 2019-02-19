alter table profissional add column tipo_remuneracao bpchar(3) default 'PRO';
alter table profissional add column valor_remuneracao numeric(11,2);
alter table profissional add column dia_remuneracao int4;
alter table procedimento add column valor_repasse  numeric(11,2);
update profissional set tipo_remuneracao = 'POR';
alter table plano_tratamento_procedimento add column tipo_remuneracao_prof_calc bpchar(3);
update plano_tratamento_procedimento set tipo_remuneracao_prof_calc='POR' where finalizado_por is not null;