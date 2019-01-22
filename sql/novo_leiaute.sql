
COLOCAR NO TOMCAT.CONF

Dhttps.protocols=TLSv1.1,TLSv1.2

Mapear o images.xml tb no tomcat
---------------------- ODONTO ----------------------------

delete from dominio where id in (339,345,285)

ALTER TABLE retorno
alter COLUMN data_retorno set data type date

CALL SYSPROC.ADMIN_CMD('REORG TABLE retorno')


CREATE TABLE "ODONTO  "."HELP"  (
                  "ID" BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY (
                    START WITH +1
                    INCREMENT BY +1
                    MINVALUE +1
                    MAXVALUE +9223372036854775807
                    NO CYCLE
                    CACHE 20
                    NO ORDER ) ,
                  "TELA" CHAR(100 OCTETS) ,
                  "CONTEUDO" CLOB(307200 OCTETS) LOGGED NOT COMPACT ,
                  "LABEL" VARCHAR(80 OCTETS) )
                 IN "TBSP_DATA_4K"
                 ORGANIZE BY ROW;
  ALTER TABLE ODONTO.HELP
  ADD CONSTRAINT PK_HELP PRIMARY KEY  (ID);
  
  
CREATE TABLE ODONTO.PLANO (
   ID	BIGINT	NOT NULL	GENERATED ALWAYS
    AS IDENTITY (START WITH 1, INCREMENT BY 1, CACHE 20,
       NO MINVALUE, NO MAXVALUE, NO CYCLE, NO ORDER),
  NOME	CHAR(200),
  NOME_PAYPAL	CHAR(200),
  CONSULTAS	INTEGER,
  VALOR DOUBLE
  );

  ALTER TABLE ODONTO.PLANO
  ADD CONSTRAINT PK_PLANO PRIMARY KEY  (ID);


---------------------- ODONTO ----------------------------

---------------------- LUME SECURITY ----------------------------

CALL SYSPROC.ADMIN_CMD('REORG TABLE SEG_EMPRESA');

ALTER TABLE SEG_EMPRESA ALTER EMP_STR_RESPONSAVEL DROP NOT NULL;

ALTER TABLE SEG_EMPRESA ALTER EMP_STR_NMEFANTASIA DROP NOT NULL;
ALTER TABLE SEG_EMPRESA ALTER EMP_CHA_CNPJ DROP NOT NULL;
ALTER TABLE SEG_EMPRESA ALTER EMP_STR_ENDERECO DROP NOT NULL;
ALTER TABLE SEG_EMPRESA ALTER EMP_STR_BAIRRO DROP NOT NULL;
ALTER TABLE SEG_EMPRESA ALTER EMP_STR_CIDADE DROP NOT NULL;
ALTER TABLE SEG_EMPRESA ALTER EMP_CHA_UF DROP NOT NULL;
ALTER TABLE SEG_EMPRESA ALTER EMP_CHA_CEP DROP NOT NULL;
ALTER TABLE SEG_EMPRESA ALTER EMP_CHA_FONE DROP NOT NULL;


ALTER TABLE SEG_EMPRESA ADD COLUMN EMP_STR_EMAIL_PAG VARCHAR(100);

ALTER TABLE  SEG_EMPRESA ADD COLUMN EMP_STR_ASSINATURAID VARCHAR(20);

ALTER TABLE SEG_EMPRESA ADD COLUMN EMP_DTM_EXPIRACAO DATE;

ALTER TABLE SEG_EMPRESA ADD COLUMN EMP_DTM_PAGAMENTO DATE;

CALL SYSPROC.ADMIN_CMD('REORG TABLE SEG_EMPRESA')
  
delete from SEG_PERUSUARIO where PER_INT_COD = 286

insert into SEG_PERUSUARIO (PER_INT_COD,USU_INT_COD) values (286,1036)


insert into SEG_PEROBJETO (PER_INT_COD, OBJ_INT_COD) values ( (select PER_INT_COD from SEG_PERFIL where PER_STR_DES = 'Administradores' and SIS_INT_COD = 123),(select OBJ_INT_COD from SEG_OBJETO where OBJ_STR_DES = 'Help' and SIS_INT_COD = 123))

INSERT INTO SEG_OBJETO 
(OBJ_INT_CODPAI                                                                                ,OBJ_STR_DES,OBJ_CHA_STS,OBJ_STR_CAMINHO,SIS_INT_COD,OBJ_INT_ORDEM,OBJ_CHA_TIPO,OBJ_STR_ICON           ) VALUES
((select OBJ_INT_COD from SEG_OBJETO where OBJ_STR_DES = 'Administração' and SIS_INT_COD = 123),'Plano'     ,'A'        ,'plano.jsf'     ,123        ,1            ,'T'         ,'fa fa-money');


insert into SEG_PEROBJETO (PER_INT_COD, OBJ_INT_COD) values ( (select PER_INT_COD from SEG_PERFIL where PER_STR_DES = 'Administradores' and SIS_INT_COD = 123),(select OBJ_INT_COD from SEG_OBJETO where OBJ_STR_DES = 'Plano' and SIS_INT_COD = 123));

-- Remover dominio de todos os perfis
delete from SEG_PEROBJETO where OBJ_INT_COD = (select OBJ_INT_COD from SEG_OBJETO where OBJ_STR_DES = 'Domínios')



update SEG_OBJETO set OBJ_STR_DES = 'Administrativo' where OBJ_STR_DES = 'RH';
update SEG_OBJETO set OBJ_STR_DES = 'Financeiro' where OBJ_STR_DES = 'Contabilidade';
update SEG_OBJETO set OBJ_INT_CODPAI = (select OBJ_INT_COD from SEG_OBJETO where OBJ_STR_DES = 'Administrativo' )  where OBJ_STR_DES = 'Anamnese';
update SEG_OBJETO set OBJ_INT_CODPAI = (select OBJ_INT_COD from SEG_OBJETO where OBJ_STR_DES = 'Administrativo' )  where OBJ_STR_DES = 'Procedimento';
update SEG_OBJETO set OBJ_INT_CODPAI = (select OBJ_INT_COD from SEG_OBJETO where OBJ_STR_DES = 'Financeiro') where OBJ_STR_DES = 'Lançamentos';
update SEG_OBJETO set OBJ_STR_DES = 'Recebimentos' where OBJ_STR_DES = 'Lançamentos';

delete from SEG_PEROBJETO where OBJ_INT_COD=(select OBJ_INT_COD from SEG_OBJETO where  OBJ_STR_DES = 'Desconto');
update SEG_OBJETO set OBJ_STR_DES = 'Empréstimo de Kits' where OBJ_STR_DES = 'Empréstimo de Materiais';
delete from SEG_PEROBJETO where OBJ_INT_COD=(select OBJ_INT_COD from SEG_OBJETO where  OBJ_STR_DES = 'Pedido de Compras');
update SEG_OBJETO set OBJ_INT_CODPAI = (select OBJ_INT_COD from SEG_OBJETO where  OBJ_STR_DES = 'Relatório' and SIS_INT_COD = 123) where OBJ_STR_DES = 'Avaliações';
update SEG_OBJETO set OBJ_INT_CODPAI = (select OBJ_INT_COD from SEG_OBJETO where OBJ_STR_DES = 'Administrativo' )  where OBJ_STR_DES = 'Convênio';
update SEG_OBJETO set OBJ_INT_CODPAI = (select OBJ_INT_COD from SEG_OBJETO where OBJ_STR_DES = 'Administrativo' )  where OBJ_STR_DES =  'Convênio Procedimento'; 


update SEG_OBJETO set OBJ_STR_DES = 'Entrega para Lavagem', OBJ_INT_CODPAI = (select OBJ_INT_COD from SEG_OBJETO where OBJ_STR_DES = 'Esterilização' and OBJ_STR_CAMINHO = 'LABEL_RAIZ') where OBJ_STR_CAMINHO = 'lavagem.jsf'; 
update SEG_OBJETO set OBJ_STR_DES = 'Devolução da Lavagem', OBJ_INT_CODPAI = (select OBJ_INT_COD from SEG_OBJETO where OBJ_STR_DES = 'Esterilização' and OBJ_STR_CAMINHO = 'LABEL_RAIZ') where OBJ_STR_CAMINHO = 'devolucaoLavagem.jsf'; 
update SEG_OBJETO set OBJ_STR_DES = 'Devolução da Esterilização', OBJ_INT_CODPAI = (select OBJ_INT_COD from SEG_OBJETO where OBJ_STR_DES = 'Esterilização' and OBJ_STR_CAMINHO = 'LABEL_RAIZ') where OBJ_STR_CAMINHO = 'devolucaoesterilizacao.jsf'; 
delete from SEG_PEROBJETO where OBJ_INT_COD=(select OBJ_INT_COD from SEG_OBJETO where  OBJ_STR_DES = 'Cockpit');

update SEG_OBJETO set OBJ_INT_ORDEM = 1 WHERE OBJ_STR_DES='Item';
update SEG_OBJETO set OBJ_INT_ORDEM = 2 WHERE OBJ_STR_DES='Local';
update SEG_OBJETO set OBJ_INT_ORDEM = 3 WHERE OBJ_STR_DES='Marca';
update SEG_OBJETO set OBJ_INT_ORDEM = 4 WHERE OBJ_STR_DES='Material';
update SEG_OBJETO set OBJ_INT_ORDEM = 5 WHERE OBJ_STR_DES='Fornecedor';
update SEG_OBJETO set OBJ_INT_ORDEM = 6 WHERE OBJ_STR_DES='Pedido de Compras';
update SEG_OBJETO set OBJ_INT_ORDEM = 7 WHERE OBJ_STR_DES='Nota Fiscal';
update SEG_OBJETO set OBJ_INT_ORDEM = 8 WHERE OBJ_STR_DES='Configuração de Kits';
update SEG_OBJETO set OBJ_INT_ORDEM = 9 WHERE OBJ_STR_DES='Reserva de Materiais';
update SEG_OBJETO set OBJ_INT_ORDEM = 10 WHERE OBJ_STR_DES='Emprestimo';
update SEG_OBJETO set OBJ_INT_ORDEM = 11 WHERE OBJ_STR_DES='Devolução de Materiais';
update SEG_OBJETO set OBJ_INT_ORDEM = 12 WHERE OBJ_STR_DES='Devolução Material';
update SEG_OBJETO set OBJ_INT_ORDEM = 13 WHERE OBJ_STR_DES='Movimentação';
update SEG_OBJETO set OBJ_INT_ORDEM = 14 WHERE OBJ_STR_DES='Conferência';
update SEG_OBJETO set OBJ_INT_ORDEM = 15 WHERE OBJ_STR_DES='Sugestão';
update SEG_OBJETO set OBJ_INT_ORDEM = 1 WHERE OBJ_STR_DES='Paciente';
update SEG_OBJETO set OBJ_INT_ORDEM = 1 WHERE OBJ_STR_DES='Pré-Agendamento';
update SEG_OBJETO set OBJ_INT_ORDEM = 2 WHERE OBJ_STR_DES='Plano Tratamento';
update SEG_OBJETO set OBJ_INT_ORDEM = 3 WHERE OBJ_STR_DES='Retorno';
update SEG_OBJETO set OBJ_INT_ORDEM = 4 WHERE OBJ_STR_DES='Avaliação';
update SEG_OBJETO set OBJ_INT_ORDEM = 5 WHERE OBJ_STR_DES='Agendamento';
update SEG_OBJETO set OBJ_INT_ORDEM = 1 WHERE OBJ_STR_DES='Clínica' and OBJ_STR_CAMINHO = 'LABEL_RAIZ' and SIS_INT_COD = 123  and OBJ_INT_CODPAI is null;
update SEG_OBJETO set OBJ_INT_ORDEM = 2 WHERE OBJ_STR_DES='Estoque' and OBJ_STR_CAMINHO = 'LABEL_RAIZ' and SIS_INT_COD = 123  and OBJ_INT_CODPAI is null;
update SEG_OBJETO set OBJ_INT_ORDEM = 3 WHERE OBJ_STR_DES='Esterilização' and OBJ_STR_CAMINHO = 'LABEL_RAIZ' and SIS_INT_COD = 123  and OBJ_INT_CODPAI is null;
update SEG_OBJETO set OBJ_INT_ORDEM = 4 WHERE OBJ_STR_DES='Financeiro' and OBJ_STR_CAMINHO = 'LABEL_RAIZ' and SIS_INT_COD = 123  and OBJ_INT_CODPAI is null;
update SEG_OBJETO set OBJ_INT_ORDEM = 5 WHERE OBJ_STR_DES='Administrativo' and OBJ_STR_CAMINHO = 'LABEL_RAIZ' and SIS_INT_COD = 123  and OBJ_INT_CODPAI is null;
update SEG_OBJETO set OBJ_INT_ORDEM = 6 WHERE OBJ_STR_DES='Documento' and OBJ_STR_CAMINHO = 'LABEL_RAIZ' and SIS_INT_COD = 123  and OBJ_INT_CODPAI is null;
update SEG_OBJETO set OBJ_INT_ORDEM = 7 WHERE OBJ_STR_DES='Relatório' and OBJ_STR_CAMINHO = 'LABEL_RAIZ' and SIS_INT_COD = 123  and OBJ_INT_CODPAI is null;
update SEG_OBJETO set OBJ_INT_ORDEM = 8 WHERE OBJ_STR_DES='Opções' and OBJ_STR_CAMINHO = 'LABEL_RAIZ' and SIS_INT_COD = 123  and OBJ_INT_CODPAI is null;
update SEG_OBJETO set OBJ_INT_CODPAI = (select OBJ_INT_COD from SEG_OBJETO where  OBJ_STR_DES = 'Clínica' )  where OBJ_STR_CAMINHO = 'pacienteExterno.jsf';
update SEG_OBJETO set OBJ_INT_ORDEM = 1 WHERE OBJ_STR_DES='Especialidade';
update SEG_OBJETO set OBJ_INT_ORDEM = 2 WHERE OBJ_STR_DES='Filial';
update SEG_OBJETO set OBJ_INT_ORDEM = 3 WHERE OBJ_STR_DES='Anamnese';
update SEG_OBJETO set OBJ_INT_ORDEM = 4 WHERE OBJ_STR_DES='Procedimento';
update SEG_OBJETO set OBJ_INT_ORDEM = 5 WHERE OBJ_STR_DES='Convênio';
update SEG_OBJETO set OBJ_INT_ORDEM = 6 WHERE OBJ_STR_DES='Convênio Procedimento';
update SEG_OBJETO set OBJ_INT_ORDEM = 7 WHERE OBJ_STR_DES='Profissional';

ALTER TABLE SEG_EMPRESA ADD COLUMN EMP_DTM_CRIACAO DATE DEFAULT CURRENT_DATE

ALTER TABLE SEG_EMPRESA ADD COLUMN EMP_CHA_TRIAL	CHAR(1) DEFAULT 'N'

---------------------- LUME SECURITY ----------------------------