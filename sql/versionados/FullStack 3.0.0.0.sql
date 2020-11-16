create table TAG(
	ID SERIAL PRIMARY KEY,
	ENTIDADE VARCHAR(255),
	ENTIDADE_ID BIGINT REFERENCES TAG(id),
	DESCRICAO VARCHAR(255)
);
create table TAG_DOCUMENTO(
	ID SERIAL PRIMARY KEY,
	ENTIDADE_ID BIGINT REFERENCES TAG(id),
	TIPO_DOCUMENTO_ID BIGINT REFERENCES DOMINIO(id)
);
create table TAG_ENTIDADE (
	ID SERIAL PRIMARY KEY,
	ATRIBUTO VARCHAR(255),
	DESCRICAO_CAMPO VARCHAR(255),
	INSERIR_DADO VARCHAR(2),
	GERA_SUGESTOES VARCHAR(2),
	ENTIDADE_ID BIGINT REFERENCES TAG(id)
);

create table tag_documento_modelo(
	id serial PRIMARY KEY,
	documento_modelo_id bigint references documento(id),
	tag_entidade_id bigint references tag_entidade(id)
);

create table documento_emitido(
	id serial primary key,
	path_documento varchar(255),
	documento_modelo_id bigint references documento(id),
	emitido_por bigint references profissional(id),
	data_emissao timestamp
);

alter table documento add column layout varchar(10);
alter table documento add column path_documento varchar(255);
alter table documento add column orientacao varchar(2);
alter table documento add column criado_por bigint references PROFISSIONAL(id);
alter table documento add column data_criacao timestamp without time zone;
alter table documento_emitido add column emitido_para bigint references Paciente(id);
alter table documento add column ativo varchar(2);
alter table documento add column DATA_ALTERACAO_STATUS timestamp;
alter table documento add column ALTERACAO_STATUS_ID bigint references profissional(id);


----CLINICA
insert into tag (entidade,descricao)
  values ('clinica','Dados da clinica');
insert into tag_entidade (atributo,descricao_campo,entidade_id)
	values ('empChaCnpj','CNPJ',1);
insert into tag_entidade (atributo,descricao_campo,entidade_id)
	values ('empChaFone','Telefone',1);
insert into tag_entidade (atributo,descricao_campo,entidade_id)
	values ('empStrNme','Clínica',1);
insert into tag_entidade (atributo,descricao_campo,entidade_id)
	values ('empChaCnpj','CNPJ',1);
insert into tag_entidade (atributo,descricao_campo,entidade_id)
	values ('empChaFone','Telefone',1);
insert into tag_entidade (atributo,descricao_campo,entidade_id)
	values ('empStrNme','Clínica',1);
insert into tag_entidade (atributo,descricao_campo,entidade_id)
	values ('empChaCep','CEP',1);
insert into tag_entidade (atributo,descricao_campo,entidade_id)
	values ('empStrEndereco','Endereço',1);
insert into tag_entidade (atributo,descricao_campo,entidade_id)
	values ('empStrBairro','Bairro',1);
insert into tag_entidade (atributo,descricao_campo,entidade_id)
	values ('empStrCidade','Cidade',1);
insert into tag_entidade (atributo,descricao_campo,entidade_id)
	values ('empChaNumEndereco','Numero',1);
insert into tag_entidade (atributo,descricao_campo,entidade_id)
	values ('empChaUf','UF',1);
insert into tag_entidade (atributo,descricao_campo,entidade_id)
	values ('empStrResponsavel','Responsável',1);
insert into tag_documento (TIPO_DOCUMENTO_ID,ENTIDADE_ID)
	values (155,1);
	
-----PACIENTE
insert into tag (entidade,descricao)
  values ('Paciente','Paciente');
insert into tag_entidade (atributo,descricao_campo,entidade_id)
	values ('nome','Nome',2);
insert into tag_entidade (atributo,descricao_campo,entidade_id)
	values ('rg','RG',2);
insert into tag_entidade (atributo,descricao_campo,entidade_id)
	values ('documento','CPF',2);
insert into tag_entidade (atributo,descricao_campo,entidade_id)
	values ('dataNascimento','Data de nascimento',2);
insert into tag_entidade (atributo,descricao_campo,entidade_id)
	values ('email','E-mail',2);
insert into tag_entidade (atributo,descricao_campo,entidade_id)
	values ('celular','Celular',2);
insert into tag_entidade (atributo,descricao_campo,entidade_id)
	values ('bairro','Bairro',2);
insert into tag_entidade (atributo,descricao_campo,entidade_id)
	values ('cep','CEP',2);
insert into tag_entidade (atributo,descricao_campo,entidade_id)
	values ('cidade','Cidade',2);
insert into tag_entidade (atributo,descricao_campo,entidade_id)
	values ('cpfresponsavel','CPF responsável',2);
insert into tag_entidade (atributo,descricao_campo,entidade_id)
	values ('rgResponsavel','RG responsável',2);
insert into tag_entidade (atributo,descricao_campo,entidade_id)
	values ('estadoCivil','Estado civil',2);
insert into tag_entidade (atributo,descricao_campo,entidade_id)
	values ('endereco','Endereço',2);
insert into tag_entidade (atributo,descricao_campo,entidade_id)
	values ('uf','UF',2);
insert into tag_entidade (atributo,descricao_campo,entidade_id)
	values ('complemento','Complemento',2);
insert into tag_entidade (atributo,descricao_campo,entidade_id)
	values ('numero','Numero residência',2);
insert into tag_entidade (atributo,descricao_campo,entidade_id)
	values ('sexo','Gênero',2);
insert into tag_entidade (atributo,descricao_campo,entidade_id)
	values ('telefone','Telefone',2);
insert into tag_entidade (atributo,descricao_campo,entidade_id)
	values ('telefoneResponsavel','Telefone responsável',2);
insert into tag_entidade (atributo,descricao_campo,entidade_id)
	values ('responsavel','Nome responsável',2);

----ATESTADO
insert into tag (entidade,descricao)
  values ('Atestado','Atestado');
 insert into tag_entidade (atributo,descricao_campo,entidade_id, inserir_dado)
	values ('cid','CID',3,'S');
insert into tag_entidade (atributo,descricao_campo,entidade_id, inserir_dado, tipo_atributo)
	values ('inicio','Inicio',3,'S','data');
insert into tag_entidade (atributo,descricao_campo,entidade_id, inserir_dado, tipo_atributo)
	values ('fim','Fim',3,'S','data');

----SISTEMA
insert into tag (entidade,descricao)
  values ('Sistema','Sistema')
 
insert into tag_entidade (atributo,descricao_campo,entidade_id)
	values ('profissionalLogado','Profissional logado',4);
insert into tag_entidade (atributo,descricao_campo,entidade_id)
	values ('dataAtual','Data atual',4);
insert into tag_entidade (atributo,descricao_campo,entidade_id)
	values ('horaAtual','Hora atual',4);
	
insert into tag_documento (TIPO_DOCUMENTO_ID,ENTIDADE_ID)
	values (152,1);
insert into tag_documento (TIPO_DOCUMENTO_ID,ENTIDADE_ID)
	values (153,1);
insert into tag_documento (TIPO_DOCUMENTO_ID,ENTIDADE_ID)
	values (155,1);
insert into tag_documento (TIPO_DOCUMENTO_ID,ENTIDADE_ID)
	values (151,1);
insert into tag_documento (TIPO_DOCUMENTO_ID,ENTIDADE_ID)
	values (154,1);
insert into tag_documento (TIPO_DOCUMENTO_ID,ENTIDADE_ID)
	values (187,1);
insert into tag_documento (TIPO_DOCUMENTO_ID,ENTIDADE_ID)
	values (207,1);
insert into tag_documento (TIPO_DOCUMENTO_ID,ENTIDADE_ID)
	values (172,1);
insert into tag_documento (TIPO_DOCUMENTO_ID,ENTIDADE_ID)
	values (238,1);

insert into tag_documento (TIPO_DOCUMENTO_ID,ENTIDADE_ID)
	values (152,2);
insert into tag_documento (TIPO_DOCUMENTO_ID,ENTIDADE_ID)
	values (153,2);
insert into tag_documento (TIPO_DOCUMENTO_ID,ENTIDADE_ID)
	values (155,2);
insert into tag_documento (TIPO_DOCUMENTO_ID,ENTIDADE_ID)
	values (151,2);
insert into tag_documento (TIPO_DOCUMENTO_ID,ENTIDADE_ID)
	values (154,2);
insert into tag_documento (TIPO_DOCUMENTO_ID,ENTIDADE_ID)
	values (187,2);
insert into tag_documento (TIPO_DOCUMENTO_ID,ENTIDADE_ID)
	values (207,2);
insert into tag_documento (TIPO_DOCUMENTO_ID,ENTIDADE_ID)
	values (172,2);
insert into tag_documento (TIPO_DOCUMENTO_ID,ENTIDADE_ID)
	values (238,2);

insert into tag_documento (TIPO_DOCUMENTO_ID,ENTIDADE_ID)
	values (155,3);
	
insert into tag_documento (TIPO_DOCUMENTO_ID,ENTIDADE_ID)
	values (152,4);
insert into tag_documento (TIPO_DOCUMENTO_ID,ENTIDADE_ID)
	values (153,4);
insert into tag_documento (TIPO_DOCUMENTO_ID,ENTIDADE_ID)
	values (155,4);
insert into tag_documento (TIPO_DOCUMENTO_ID,ENTIDADE_ID)
	values (151,4);
insert into tag_documento (TIPO_DOCUMENTO_ID,ENTIDADE_ID)
	values (154,4);
insert into tag_documento (TIPO_DOCUMENTO_ID,ENTIDADE_ID)
	values (187,4);
insert into tag_documento (TIPO_DOCUMENTO_ID,ENTIDADE_ID)
	values (207,4);
insert into tag_documento (TIPO_DOCUMENTO_ID,ENTIDADE_ID)
	values (172,4);
insert into tag_documento (TIPO_DOCUMENTO_ID,ENTIDADE_ID)
	values (238,4);
	
insert into seg_objeto (obj_int_codpai, obj_str_des, obj_cha_sts, obj_str_caminho, sis_int_cod, 
						obj_int_ordem, obj_cha_tipo, obj_str_icon)
	values (12,'Emissão de documentos', 'A', 'emissaoDocumento.jsf', 123, 9, 'T', null);

INSERT INTO OBJETO_PROFISSIONAL(OBJ_INT_COD, ID_PROFISSIONAL)
SELECT
	(
		SELECT OBJ_INT_COD FROM SEG_OBJETO
		WHERE OBJ_STR_DES = 'Emissão de documentos'
		LIMIT 1
	),
	OP.ID_PROFISSIONAL
FROM OBJETO_PROFISSIONAL OP
LEFT JOIN SEG_OBJETO O
	ON O.OBJ_INT_COD = OP.OBJ_INT_COD
WHERE O.OBJ_STR_DES = 'Paciente';