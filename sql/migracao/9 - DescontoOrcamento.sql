CREATE TABLE DESCONTO_ORCAMENTO(
	ID serial primary key,
	DATA_CRIACAO timestamp NOT NULL,
	DATA_ULTIMA_ALTERACAO timestamp NOT NULL,
	CRIADO_POR bigint REFERENCES PROFISSIONAL(ID),
	ALTERADO_POR bigint REFERENCES PROFISSIONAL(ID),
	DESCONTO bigint,
	QUANTIDADE_PARCELAS bigint,
	ID_EMPRESA bigint,
	STATUS character DEFAULT 'A'
);

alter table desconto_orcamento add column ID_PROFISSIONAL bigint REFERENCES PROFISSIONAL(ID);
alter table desconto_orcamento add column TIPO_DESCONTO character;