

ALTER TABLE MATERIAL_INDISPONIVEL RENAME TO EMPRESTIMO_KIT;

ALTER TABLE EMPRESTIMO_KIT DROP COLUMN VALOR;

ALTER TABLE EMPRESTIMO_KIT DROP COLUMN DATA_EXCLUSAO;

ALTER TABLE EMPRESTIMO_KIT DROP COLUMN EXCLUIDO_POR;

ALTER TABLE EMPRESTIMO_KIT DROP COLUMN EXCLUIDO;

ALTER TABLE ESTERILIZACAO_KIT RENAME COLUMN ID_MATERIAL_INDISPONIVEL TO EMPRESTIMO_KIT_ID;

ALTER TABLE ABASTECIMENTO RENAME TO EMPRESTIMO_UNITARIO;

ALTER TABLE ABASTECIMENTO DROP COLUMN VALOR;

ALTER TABLE ABASTECIMENTO DROP COLUMN DATA_EXCLUSAO;

ALTER TABLE ABASTECIMENTO DROP COLUMN EXCLUIDO_POR;

ALTER TABLE ABASTECIMENTO DROP COLUMN EXCLUIDO;

update seg_objeto set obj_str_caminho = 'emprestimokit.jsf' where obj_str_caminho ilike '%controlema%'

--alterar queries de local para nao pegar os locais do tipo sistema

--ALTER TABLE MATERIAL_LOG ADD COLUMN ID_LOCAL_ORIGEM  BIGINT REFERENCES LOCAL(ID);
--ALTER TABLE MATERIAL_LOG ADD COLUMN ID_LOCAL_DESTINO BIGINT REFERENCES LOCAL(ID);
--ALTER TABLE MATERIAL_LOG ADD COLUMN ID_LOCAL_DESTINO BIGINT REFERENCES LOCAL(ID);

insert into local (descricao, tipo, id_empresa, excluido) values ('ENTREGUE','SI',41,'N');
insert into local (descricao, tipo, id_empresa, excluido) values ('PENDENTE','SI',41,'N');
insert into local (descricao, tipo, id_empresa, excluido) values ('FINALIZADO','SI',41,'N');
insert into local (descricao, tipo, id_empresa, excluido) values ('UTILIZADO_KIT','SI',41,'N');
insert into local (descricao, tipo, id_empresa, excluido) values ('UTILIZADO_UNITARIO','SI',41,'N');
insert into local (descricao, tipo, id_empresa, excluido) values ('LAVAGEM_UNITARIO','SI',41,'N');
insert into local (descricao, tipo, id_empresa, excluido) values ('LAVAGEM_KIT','SI',41,'N');
insert into local (descricao, tipo, id_empresa, excluido) values ('DESCARTE','SI',41,'N');
insert into local (descricao, tipo, id_empresa, excluido) values ('NAOUTILIZADO','SI',41,'N');
insert into local (descricao, tipo, id_empresa, excluido) values ('NAOUTILIZADO','SI',41,'N');

insert into local (descricao, tipo, id_empresa, excluido) values ('COMPRA','SI',41,'N');
insert into local (descricao, tipo, id_empresa, excluido) values ('DEVOLUCAO','SI',41,'N');
insert into local (descricao, tipo, id_empresa, excluido) values ('DESCARTE','SI',41,'N');
insert into local (descricao, tipo, id_empresa, excluido) values ('DESCARTE','SI',41,'N');

CREATE TABLE ESTOQUE (
	ID SERIAL PRIMARY KEY,
	ID_LOCAL BIGINT REFERENCES LOCAL(ID),
	ID_MATERIAL BIGINT REFERENCES MATERIAL(ID),
	QUANTIDADE BIGINT;
);
