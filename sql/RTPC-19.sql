-- Alteração dos nomes de telas


Mudar esses nomes das telas
update seg_objeto set obj_str_des = 'Empréstimo de kits' where obj_str_des = 'Empréstimo de Materiais'

update seg_objeto set obj_str_des = 'Kit (Individual)' where obj_str_des = 'Fragmentada'


Mudar esses para Administrador
Ivaldo Sa Barreto Filho 
Adm 
Administrador 
Administrador 
Adriana Erbs 


--Alterar os perfis de Administrador para Administradores

--update seg_perusuario set per_int_cod = 286 where USU_INT_COD in (926,1300,1321,1322,1492)

--alter table lavagem_kit add column id_material_indisponivel bigint;

--alter table lavagem_kit add column id_reserva_kit bigint;

--alter table lavagem_kit add column id_abastecimento bigint;

--ALTER TABLE ODONTO.LAVAGEM_KIT 
--ADD CONSTRAINT LAVAGEM_KIT_MATERIAL_INDISPONIVEL_FK FOREIGN KEY (ID_MATERIAL_INDISPONIVEL) REFERENCES ODONTO.MATERIAL_INDISPONIVEL (ID)  ON DELETE NO ACTION ON UPDATE NO ACTION ENFORCED  ENABLE QUERY OPTIMIZATION 
--ADD CONSTRAINT LAVAGEM_KIT_RESERVA_KIT_FK FOREIGN KEY (ID_RESERVA_KIT) REFERENCES ODONTO.RESERVA_KIT (ID)  ON DELETE NO ACTION ON UPDATE NO ACTION ENFORCED  ENABLE QUERY OPTIMIZATION 
--ADD CONSTRAINT LAVAGEM_KIT_ABASTECIMENTO_FK FOREIGN KEY (ID_ABASTECIMENTO) REFERENCES ODONTO.ABASTECIMENTO (ID)  ON DELETE NO ACTION ON UPDATE NO ACTION ENFORCED  ENABLE QUERY OPTIMIZATION ;


--ALTER TABLE ODONTO.ESTERILIZACAO_KIT 
--ADD COLUMN ID_MATERIAL_INDISPONIVEL BIGINT 
--ADD COLUMN ID_RESERVA_KIT BIGINT 
--ADD COLUMN ID_ABASTECIMENTO BIGINT 
--ADD CONSTRAINT CC1432739954413 FOREIGN KEY (ID_ABASTECIMENTO) REFERENCES ODONTO.ABASTECIMENTO (ID)  ON DELETE NO ACTION ON UPDATE NO ACTION ENFORCED  ENABLE QUERY OPTIMIZATION 
--ADD CONSTRAINT CC1432739962597 FOREIGN KEY (ID_MATERIAL_INDISPONIVEL) REFERENCES ODONTO.MATERIAL_INDISPONIVEL (ID)  ON DELETE NO ACTION ON UPDATE NO ACTION ENFORCED  ENABLE QUERY OPTIMIZATION 
--ADD CONSTRAINT CC1432739973296 FOREIGN KEY (ID_RESERVA_KIT) REFERENCES ODONTO.RESERVA_KIT (ID)  ON DELETE NO ACTION ON UPDATE NO ACTION ENFORCED  ENABLE QUERY OPTIMIZATION ;

--alter table ESTERILIZACAO
--alter column DESCRICAO drop not null;

--alter table lavagem
--add column clinica integer;

--alter table ESTERILIZACAO
--add column clinica integer;

--insert into dominio (objeto,tipo,nome,valor,id_empresa,editavel,excluido) values ('abastecimento', 'validades', 'dias','30',41,0,'N');
--insert into dominio (objeto,tipo,nome,valor,id_empresa,editavel,excluido) values ('descarte', 'justificativa', 'Estragado','ES',41,0,'N');

--alter table PLANO_TRATAMENTO_PROCEDIMENTO
--add column VALOR_REPASSADO decimal(11,2);


--MOVER ADMINISTRADOR PARA ADMINISTRADORES

--REORGS (lavagem_kit, esterilizacao, lavagem)