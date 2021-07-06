alter table plano_tratamento_procedimento add column id_dente int8

alter table only plano_tratamento_procedimento
    add constraint ptp_dente foreign key (id_dente) references dente(id);

    
alter table plano_tratamento add column id_odontograma int8;

alter table plano_tratamento add column desconto numeric (11,2);

alter table only plano_tratamento
    add constraint pt_odontograma foreign key (id_odontograma) references odontograma(id);1

alter table plano_tratamento_procedimento add column regiao varchar(40);    
    
alter table paciente add column numero_plano varchar(50);    

commit;