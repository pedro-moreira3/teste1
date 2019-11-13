UPDATE SEG_OBJETO SET OBJ_INT_ORDEM = 2 WHERE OBJ_STR_DES = 'Cadastro da Clínica';
UPDATE SEG_OBJETO SET OBJ_INT_ORDEM = 3 WHERE OBJ_STR_DES = 'Cadastro de Especialidades';
UPDATE SEG_OBJETO SET OBJ_INT_ORDEM = 4 WHERE OBJ_STR_DES = 'Cadastro de Procedimentos';
UPDATE SEG_OBJETO SET OBJ_INT_ORDEM = 5 WHERE OBJ_STR_DES = 'Cadastro de Convênios';
UPDATE SEG_OBJETO SET OBJ_INT_ORDEM = 6 WHERE OBJ_STR_DES = 'Convênio-Procedimento';
UPDATE SEG_OBJETO SET OBJ_INT_ORDEM = 7 WHERE OBJ_STR_DES = 'Cadastro de Profissionais';
UPDATE SEG_OBJETO SET OBJ_INT_ORDEM = 8 WHERE OBJ_STR_DES = 'Config. de Anamnese';

UPDATE SEG_OBJETO SET OBJ_INT_CODPAI = (
	SELECT OBJ_INT_COD FROM SEG_OBJETO
	WHERE SEG_OBJETO.OBJ_STR_DES = 'Emissão de Documentos'
) WHERE OBJ_STR_DES = 'Configuração de Documentos';

UPDATE SEG_OBJETO SET OBJ_STR_DES = 'Agenda' WHERE OBJ_STR_DES = 'Agendamento';
UPDATE SEG_OBJETO SET OBJ_STR_DES = 'Devolução Unitária' WHERE OBJ_STR_DES = 'Unitária';
UPDATE SEG_OBJETO SET OBJ_STR_DES = 'Relatório de Agendamento' WHERE OBJ_STR_DES = 'Relatório de Atendimento';

UPDATE SEG_OBJETO SET OBJ_INT_ORDEM = 2 WHERE OBJ_STR_DES = 'Fila de Atendimento';

UPDATE SEG_OBJETO SET OBJ_CHA_STS = 'I' WHERE OBJ_STR_DES = 'Repasse dos Profissionais' AND OBJ_STR_CAMINHO = 'faturamento.jsf';
UPDATE SEG_OBJETO SET OBJ_CHA_STS = 'I' WHERE OBJ_STR_DES = 'Rel. Atendimento' AND OBJ_STR_CAMINHO = 'filaAtendimento.jsf';
UPDATE SEG_OBJETO SET OBJ_CHA_STS = 'I' WHERE OBJ_STR_DES = 'Relatório de Avaliação de Atendimentos';
UPDATE SEG_OBJETO SET OBJ_CHA_STS = 'I' WHERE OBJ_STR_DES = 'Registro de Custos Diretos';
UPDATE SEG_OBJETO SET OBJ_CHA_STS = 'I' WHERE OBJ_STR_DES = 'Pagamentos/Recebimentos';
UPDATE SEG_OBJETO SET OBJ_CHA_STS = 'I' WHERE OBJ_STR_DES = 'Relatório de Resultados';
UPDATE SEG_OBJETO SET OBJ_CHA_STS = 'I' WHERE OBJ_STR_DES = 'Relatório de Bloqueios';
UPDATE SEG_OBJETO SET OBJ_CHA_STS = 'I' WHERE OBJ_STR_DES = 'Atualizações';
UPDATE SEG_OBJETO SET OBJ_CHA_STS = 'I' WHERE OBJ_STR_DES = 'Dashboard';

-------------------

ALTER TABLE AGENDAMENTO_PLANO_TRATAMENTO_PROCEDIMENTO ADD COLUMN ATIVO VARCHAR(1) NOT NULL DEFAULT 'S';
ALTER TABLE AGENDAMENTO_PLANO_TRATAMENTO_PROCEDIMENTO ADD COLUMN ALTERACAO_STATUS_ID BIGINT NULL REFERENCES PROFISSIONAL(ID);
ALTER TABLE AGENDAMENTO_PLANO_TRATAMENTO_PROCEDIMENTO ADD COLUMN DATA_ALTERACAO_STATUS TIMESTAMP(10) NULL;

ALTER TABLE SEG_EMPRESA ADD COLUMN EMP_STR_WPP_MESSAGE_AGENDAMENTO TEXT NULL;
ALTER TABLE SEG_EMPRESA ADD COLUMN EMP_STR_WPP_MESSAGE_PACIENTE TEXT NULL;
ALTER TABLE SEG_EMPRESA ADD COLUMN EMP_STR_WPP_MESSAGE_RETORNO TEXT NULL;

-----------------

insert into dominio (objeto,tipo,nome,valor,id_empresa,editavel,excluido) values ('indicacao','indicacao','Indicado por Paciente','0',0,false,'N');
insert into dominio (objeto,tipo,nome,valor,id_empresa,editavel,excluido) values ('indicacao','indicacao','Indicado por Profissional','1',0,false,'N');
insert into dominio (objeto,tipo,nome,valor,id_empresa,editavel,excluido) values ('indicacao','indicacao','Google','2',0,false,'N');
insert into dominio (objeto,tipo,nome,valor,id_empresa,editavel,excluido) values ('indicacao','indicacao','Facebook','3',0,false,'N');
insert into dominio (objeto,tipo,nome,valor,id_empresa,editavel,excluido) values ('indicacao','indicacao','Instagram','4',0,false,'N');
insert into dominio (objeto,tipo,nome,valor,id_empresa,editavel,excluido) values ('indicacao','indicacao','Propaganda','5',0,false,'N');
insert into dominio (objeto,tipo,nome,valor,id_empresa,editavel,excluido) values ('indicacao','indicacao','Outros','6',0,false,'N');

alter table PACIENTE ADD COLUMN INDICACAO_DOMINIO_ID BIGINT REFERENCES DOMINIO(ID);

alter table PACIENTE ADD COLUMN INDICACAO_PACIENTE_ID BIGINT REFERENCES PACIENTE(ID);
alter table PACIENTE ADD COLUMN INDICACAO_PROFISSIONAL_ID BIGINT REFERENCES PROFISSIONAL(ID);

UPDATE PACIENTE SET INDICACAO_DOMINIO_ID = (SELECT ID FROM dominio WHERE TIPO = 'indicacao' AND nome = 'Outros') WHERE INDICACAO IS NOT NULL AND INDICACAO <> '';

ALTER TABLE PERGUNTA ALTER COLUMN requerida DROP DEFAULT;
ALTER TABLE PERGUNTA ALTER requerida TYPE bool USING CASE WHEN requerida='false' THEN FALSE ELSE TRUE END;
ALTER TABLE PERGUNTA ALTER COLUMN requerida set DEFAULT false;


