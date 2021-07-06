alter table PLANO_TRATAMENTO_PROCEDIMENTO add column tributo decimal(11,2);

alter table PLANO_TRATAMENTO_PROCEDIMENTO drop column custo;

alter table PLANO_TRATAMENTO_PROCEDIMENTO add column custo decimal(11,2);

alter table PROFISSIONAL add column remuneracao_manutencao integer;

alter table PROCEDIMENTO add column tipo char;

 ALTER TABLE LANCAMENTO ADD COLUMN DATA_FATURAMENTO TIMESTAMP;
 
 ALTER TABLE LANCAMENTO ADD COLUMN DATA_PAGAMENTO_FATURAMENTO TIMESTAMP;
 
 alter table lancamento add column tributo decimal;
 

CREATE TABLE ODONTO.DOCUMENTO_FATURAMENTO ( 
ID BIGINT  GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1) PRIMARY KEY NOT NULL, 
ID_PROFISSIONAL BIGINT  REFERENCES ODONTO.PROFISSIONAL (ID) NOT NULL , 
DATA_HORA TIMESTAMP  NOT NULL , 
DOCUMENTO_GERADO CLOB  (1 M )  NOT NULL  LOGGED  NOT  COMPACT , 
EXCLUIDO CHARACTER (1) , 
DATA_EXCLUSAO TIMESTAMP , 
EXCLUIDO_POR BIGINT ) ;

create table PLANO_TRATAMENTO_PROCEDIMENTO_CUSTO(
ID BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1 INCREMENT BY 1) PRIMARY KEY,
ID_PLANO_TRATAMENTO_PROCEDIMENTO BIGINT  NOT NULL REFERENCES PLANO_TRATAMENTO_PROCEDIMENTO(ID),
DATA_FATURAMENTO TIMESTAMP NOT NULL,
DESCRICAO VARCHAR(100)  NOT NULL,
VALOR DECIMAL(11,2) NOT NULL,
EXCLUIDO CHAR(1),
DATA_EXCLUSAO TIMESTAMP,
EXCLUIDO_POR BIGINT);

update profissional set remuneracao_manutencao = PERCENTUAL_REMUNERACAO;

update Procedimento set tipo = 'N';

insert into dominio (objeto,tipo,nome,valor,id_empresa,editavel) values ('documento','tipo','Faturamento','F',0,0);
insert into dominio (objeto,tipo,nome,valor,editavel,excluido,id_empresa) values ('faturamento','tributo','7.93','TR',1,'N',41);
insert into dominio (objeto,tipo,nome,valor,editavel,excluido) values ('faturamento','tributo','0','TR',0,'N');