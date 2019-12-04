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

ALTER TABLE PACIENTE ADD COLUMN CRIADO_POR BIGINT NULL;
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
-----------------

UPDATE PLANO_TRATAMENTO SET ID_ODONTOGRAMA = (
	SELECT
		CASE WHEN UP."ODONTOGRAMA" IS NOT NULL
			THEN UP."ODONTOGRAMA"
		ELSE (
			SELECT ODO.ID
			FROM PLANO_TRATAMENTO PT
			LEFT JOIN PLANO_TRATAMENTO_PROCEDIMENTO PTP ON PTP.ID_PLANO_TRATAMENTO = PT.ID
			LEFT JOIN DENTE D ON D.ID = PTP.ID_DENTE
			LEFT JOIN ODONTOGRAMA ODO ON ODO.ID = D.ID_ODONTOGRAMA
			WHERE PT.ID = UP."PT"
			ORDER BY PTP.ID DESC, D.ID DESC, ODO.ID DESC
			LIMIT 1
		) END
	FROM (
		SELECT DISTINCT PT.ID AS "PT", (
			SELECT O.ID FROM ODONTOGRAMA O
			WHERE O.DATA_CADASTRO < PT.DATA_HORA
			  AND O.ID_PACIENTE = PT.ID_PACIENTE
			ORDER BY ID DESC LIMIT 1
		) AS "ODONTOGRAMA" FROM PLANO_TRATAMENTO PT
	) AS UP
	WHERE UP."PT" = PLANO_TRATAMENTO.ID
) WHERE ID IN (
	SELECT ID FROM PLANO_TRATAMENTO WHERE ID_ODONTOGRAMA IS NULL
);
CREATE TABLE REGIAO_REGIAO (
	ID SERIAL PRIMARY KEY,
	REGIAO VARCHAR(255) NOT NULL,
	EXCLUIDO VARCHAR(1) NOT NULL DEFAULT 'N',
	DATA_EXCLUSAO TIMESTAMP(10) NULL,
	
	EXCLUIDO_POR BIGINT NULL REFERENCES PROFISSIONAL(ID),
	ODONTOGRAMA_ID BIGINT NULL REFERENCES ODONTOGRAMA(ID),
	ID_STATUS_DENTE BIGINT NOT NULL REFERENCES STATUS_DENTE(ID)
);
-------------------------
CREATE TABLE AFILIACAO (
	ID SERIAL PRIMARY KEY,
	NOME VARCHAR(255) NOT NULL,
	EMAIL VARCHAR(255) NULL,
	TELEFONE VARCHAR(50) NULL,
	
	ATIVO VARCHAR(1) NOT NULL DEFAULT 'N',
	DATA_ALTERACAO_STATUS TIMESTAMP(10) NULL,
	ALTERACAO_STATUS_ID BIGINT NULL REFERENCES PROFISSIONAL(ID)
);
INSERT INTO AFILIACAO(NOME, ATIVO)
VALUES('Grupo QS - Qualidade e Saúde', 'S');
ALTER TABLE SEG_EMPRESA ADD COLUMN AFILIACAO_ID BIGINT NULL REFERENCES AFILIACAO(ID);
-------------------------
ALTER TABLE TRANSFERENCIA_CONTA
   DROP CONSTRAINT transferencia_conta_id_conta_origem_fkey
 , ADD  CONSTRAINT transferencia_conta_id_conta_origem_fkey FOREIGN KEY(ID_CONTA_ORIGEM)
      REFERENCES CONTA(ID) DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE TRANSFERENCIA_CONTA
   DROP CONSTRAINT transferencia_conta_id_conta_destino_fkey
 , ADD  CONSTRAINT transferencia_conta_id_conta_destino_fkey FOREIGN KEY(ID_CONTA_DESTINO)
      REFERENCES CONTA(ID) DEFERRABLE INITIALLY DEFERRED;
      
----------------------------
update ESPECIALIDADE set descricao = 'GENERICAS' where descricao ilike 'GEN%'      
ALTER TABLE SEG_EMPRESA ADD COLUMN VALIDAR_REPASSE_PROCEDIMENTO_FINALIZADO VARCHAR(1) NOT NULL DEFAULT 'S';
ALTER TABLE SEG_EMPRESA ADD COLUMN VALIDAR_REPASSE_LANCAMENTO_ORIGEM_VALIDADO VARCHAR(1) NOT NULL DEFAULT 'S';
----------------------------
CREATE TABLE EVOLUCAO_PLANO_TRATAMENTO_PROCEDIMENTO (
	ID SERIAL PRIMARY KEY,
	PLANO_TRATAMENTO VARCHAR(255) NULL,
	PLANO_TRATAMENTO_PROCEDIMENTO TEXT NULL,
	ACAO_EVOLUCAO VARCHAR(255) NULL,
	REGIAO_DENTE_FACE VARCHAR(255) NULL,	
	EVOLUCAO_ID BIGINT NOT NULL REFERENCES EVOLUCAO(ID),
	PLANO_TRATAMENTO_PROCEDIMENTO_ID BIGINT NULL REFERENCES PLANO_TRATAMENTO_PROCEDIMENTO(ID)
);


----------------------------
UPDATE SEG_OBJETO SET OBJ_STR_DES = 'Relatório de Estoque Antigo' WHERE OBJ_STR_DES = 'Relatório de Estoque';
UPDATE SEG_OBJETO SET OBJ_STR_DES = 'Relatório de Estoque' WHERE OBJ_STR_DES = 'Estoque Mínimo';
UPDATE SEG_OBJETO SET OBJ_CHA_STS = 'I' WHERE OBJ_STR_DES = 'Relatório de Estoque Antigo';

