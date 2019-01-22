ALTER TABLE seg_empresa ADD COLUMN emp_str_estado_conselho varchar	(2);

update seg_empresa set emp_str_estado_conselho  = 'PR';

CREATE TABLE inteli."tipo_categoria"
(
    "id" serial,
    "descricao" character varying(200),
    PRIMARY KEY ("id")
);

ALTER TABLE inteli.categoria_motivo
    ADD COLUMN id_tipo bigint;
	
ALTER TABLE ONLY categoria_motivo
    ADD CONSTRAINT tipo_categoria FOREIGN KEY (id_tipo) REFERENCES tipo_categoria(id);
    
INSERT INTO tipo_categoria (DESCRICAO) VALUES('Gastos Gerais');
INSERT INTO tipo_categoria (DESCRICAO) VALUES('Gastos Odontológicos');
INSERT INTO tipo_categoria (DESCRICAO) VALUES('Gastos Operacionais');
INSERT INTO tipo_categoria (DESCRICAO) VALUES('Receita Bruta');

UPDATE categoria_motivo SET ID_TIPO = (SELECT ID FROM TIPO_CATEGORIA WHERE DESCRICAO = 'Gastos Gerais') WHERE DESCRICAO = 'Captação de Recursos Terceiros';
UPDATE categoria_motivo SET ID_TIPO = (SELECT ID FROM TIPO_CATEGORIA WHERE DESCRICAO = 'Gastos Gerais') WHERE DESCRICAO = 'Captação de Recursos Terceiros';
UPDATE categoria_motivo SET ID_TIPO = (SELECT ID FROM TIPO_CATEGORIA WHERE DESCRICAO = 'Gastos Gerais') WHERE DESCRICAO = 'Comunicação';
UPDATE categoria_motivo SET ID_TIPO = (SELECT ID FROM TIPO_CATEGORIA WHERE DESCRICAO = 'Gastos Gerais') WHERE DESCRICAO = 'Despesas Gerais';
UPDATE categoria_motivo SET ID_TIPO = (SELECT ID FROM TIPO_CATEGORIA WHERE DESCRICAO = 'Gastos Gerais') WHERE DESCRICAO = 'Despesas Transitórias';
UPDATE categoria_motivo SET ID_TIPO = (SELECT ID FROM TIPO_CATEGORIA WHERE DESCRICAO = 'Gastos Gerais') WHERE DESCRICAO = 'Despesas Transitórias';
UPDATE categoria_motivo SET ID_TIPO = (SELECT ID FROM TIPO_CATEGORIA WHERE DESCRICAO = 'Gastos Gerais') WHERE DESCRICAO = 'Festas, Eventos e Patrocínios';
UPDATE categoria_motivo SET ID_TIPO = (SELECT ID FROM TIPO_CATEGORIA WHERE DESCRICAO = 'Gastos Gerais') WHERE DESCRICAO = 'Financeira';
UPDATE categoria_motivo SET ID_TIPO = (SELECT ID FROM TIPO_CATEGORIA WHERE DESCRICAO = 'Gastos Gerais') WHERE DESCRICAO = 'Financeiras';
UPDATE categoria_motivo SET ID_TIPO = (SELECT ID FROM TIPO_CATEGORIA WHERE DESCRICAO = 'Gastos Gerais') WHERE DESCRICAO = 'Financeiras';
UPDATE categoria_motivo SET ID_TIPO = (SELECT ID FROM TIPO_CATEGORIA WHERE DESCRICAO = 'Gastos Gerais') WHERE DESCRICAO = 'Financeiras';
UPDATE categoria_motivo SET ID_TIPO = (SELECT ID FROM TIPO_CATEGORIA WHERE DESCRICAO = 'Gastos Gerais') WHERE DESCRICAO = 'Imobilizado';
UPDATE categoria_motivo SET ID_TIPO = (SELECT ID FROM TIPO_CATEGORIA WHERE DESCRICAO = 'Gastos Gerais') WHERE DESCRICAO = 'Imobilizado';
UPDATE categoria_motivo SET ID_TIPO = (SELECT ID FROM TIPO_CATEGORIA WHERE DESCRICAO = 'Gastos Gerais') WHERE DESCRICAO = 'Imobilizado';
UPDATE categoria_motivo SET ID_TIPO = (SELECT ID FROM TIPO_CATEGORIA WHERE DESCRICAO = 'Gastos Gerais') WHERE DESCRICAO = 'Impostos';
UPDATE categoria_motivo SET ID_TIPO = (SELECT ID FROM TIPO_CATEGORIA WHERE DESCRICAO = 'Gastos Gerais') WHERE DESCRICAO = 'Impostos';
UPDATE categoria_motivo SET ID_TIPO = (SELECT ID FROM TIPO_CATEGORIA WHERE DESCRICAO = 'Gastos Gerais') WHERE DESCRICAO = 'Impostos';
UPDATE categoria_motivo SET ID_TIPO = (SELECT ID FROM TIPO_CATEGORIA WHERE DESCRICAO = 'Gastos Gerais') WHERE DESCRICAO = 'Impostos';
UPDATE categoria_motivo SET ID_TIPO = (SELECT ID FROM TIPO_CATEGORIA WHERE DESCRICAO = 'Gastos Gerais') WHERE DESCRICAO = 'Impostos';
UPDATE categoria_motivo SET ID_TIPO = (SELECT ID FROM TIPO_CATEGORIA WHERE DESCRICAO = 'Gastos Gerais') WHERE DESCRICAO = 'Impostos';
UPDATE categoria_motivo SET ID_TIPO = (SELECT ID FROM TIPO_CATEGORIA WHERE DESCRICAO = 'Gastos Gerais') WHERE DESCRICAO = 'Impostos';
UPDATE categoria_motivo SET ID_TIPO = (SELECT ID FROM TIPO_CATEGORIA WHERE DESCRICAO = 'Gastos Gerais') WHERE DESCRICAO = 'Mão de Obra';
UPDATE categoria_motivo SET ID_TIPO = (SELECT ID FROM TIPO_CATEGORIA WHERE DESCRICAO = 'Gastos Gerais') WHERE DESCRICAO = 'Mão de Obra';
UPDATE categoria_motivo SET ID_TIPO = (SELECT ID FROM TIPO_CATEGORIA WHERE DESCRICAO = 'Gastos Gerais') WHERE DESCRICAO = 'Mão de Obra';
UPDATE categoria_motivo SET ID_TIPO = (SELECT ID FROM TIPO_CATEGORIA WHERE DESCRICAO = 'Gastos Gerais') WHERE DESCRICAO = 'Padrão Sistema';
UPDATE categoria_motivo SET ID_TIPO = (SELECT ID FROM TIPO_CATEGORIA WHERE DESCRICAO = 'Gastos Gerais') WHERE DESCRICAO = 'Padrão Sistema';
UPDATE categoria_motivo SET ID_TIPO = (SELECT ID FROM TIPO_CATEGORIA WHERE DESCRICAO = 'Gastos Gerais') WHERE DESCRICAO = 'Pagamento de Recursos Terceiros';
UPDATE categoria_motivo SET ID_TIPO = (SELECT ID FROM TIPO_CATEGORIA WHERE DESCRICAO = 'Gastos Gerais') WHERE DESCRICAO = 'Pagamento de Recursos Terceiros';
UPDATE categoria_motivo SET ID_TIPO = (SELECT ID FROM TIPO_CATEGORIA WHERE DESCRICAO = 'Gastos Gerais') WHERE DESCRICAO = 'Receitas Transitórias';
UPDATE categoria_motivo SET ID_TIPO = (SELECT ID FROM TIPO_CATEGORIA WHERE DESCRICAO = 'Gastos Odontológicos') WHERE DESCRICAO = 'Aluguel';
UPDATE categoria_motivo SET ID_TIPO = (SELECT ID FROM TIPO_CATEGORIA WHERE DESCRICAO = 'Gastos Odontológicos') WHERE DESCRICAO = 'Despesas Diretas com a Operacional';
UPDATE categoria_motivo SET ID_TIPO = (SELECT ID FROM TIPO_CATEGORIA WHERE DESCRICAO = 'Gastos Odontológicos') WHERE DESCRICAO = 'Despesas Diretas com a Operacional';
UPDATE categoria_motivo SET ID_TIPO = (SELECT ID FROM TIPO_CATEGORIA WHERE DESCRICAO = 'Gastos Odontológicos') WHERE DESCRICAO = 'Despesas Diretas com a Operacional';
UPDATE categoria_motivo SET ID_TIPO = (SELECT ID FROM TIPO_CATEGORIA WHERE DESCRICAO = 'Gastos Odontológicos') WHERE DESCRICAO = 'Despesas Diretas com a Operacional';
UPDATE categoria_motivo SET ID_TIPO = (SELECT ID FROM TIPO_CATEGORIA WHERE DESCRICAO = 'Gastos Odontológicos') WHERE DESCRICAO = 'Padrão Sistema';
UPDATE categoria_motivo SET ID_TIPO = (SELECT ID FROM TIPO_CATEGORIA WHERE DESCRICAO = 'Gastos Odontológicos') WHERE DESCRICAO = 'Padrão Sistema';
UPDATE categoria_motivo SET ID_TIPO = (SELECT ID FROM TIPO_CATEGORIA WHERE DESCRICAO = 'Gastos Odontológicos') WHERE DESCRICAO = 'Padrão Sistema';
UPDATE categoria_motivo SET ID_TIPO = (SELECT ID FROM TIPO_CATEGORIA WHERE DESCRICAO = 'Gastos Odontológicos') WHERE DESCRICAO = 'Padrão Sistema';
UPDATE categoria_motivo SET ID_TIPO = (SELECT ID FROM TIPO_CATEGORIA WHERE DESCRICAO = 'Gastos Operacionais') WHERE DESCRICAO = 'Aluguel';
UPDATE categoria_motivo SET ID_TIPO = (SELECT ID FROM TIPO_CATEGORIA WHERE DESCRICAO = 'Gastos Operacionais') WHERE DESCRICAO = 'Comunicação';
UPDATE categoria_motivo SET ID_TIPO = (SELECT ID FROM TIPO_CATEGORIA WHERE DESCRICAO = 'Gastos Operacionais') WHERE DESCRICAO = 'Comunicação';
UPDATE categoria_motivo SET ID_TIPO = (SELECT ID FROM TIPO_CATEGORIA WHERE DESCRICAO = 'Gastos Operacionais') WHERE DESCRICAO = 'Comunicação';
UPDATE categoria_motivo SET ID_TIPO = (SELECT ID FROM TIPO_CATEGORIA WHERE DESCRICAO = 'Gastos Operacionais') WHERE DESCRICAO = 'Despesas Gerais';
UPDATE categoria_motivo SET ID_TIPO = (SELECT ID FROM TIPO_CATEGORIA WHERE DESCRICAO = 'Gastos Operacionais') WHERE DESCRICAO = 'Despesas Gerais';
UPDATE categoria_motivo SET ID_TIPO = (SELECT ID FROM TIPO_CATEGORIA WHERE DESCRICAO = 'Gastos Operacionais') WHERE DESCRICAO = 'Despesas Gerais';
UPDATE categoria_motivo SET ID_TIPO = (SELECT ID FROM TIPO_CATEGORIA WHERE DESCRICAO = 'Gastos Operacionais') WHERE DESCRICAO = 'Despesas Gerais';
UPDATE categoria_motivo SET ID_TIPO = (SELECT ID FROM TIPO_CATEGORIA WHERE DESCRICAO = 'Gastos Operacionais') WHERE DESCRICAO = 'Despesas Gerais';
UPDATE categoria_motivo SET ID_TIPO = (SELECT ID FROM TIPO_CATEGORIA WHERE DESCRICAO = 'Gastos Operacionais') WHERE DESCRICAO = 'Despesas Gerais';
UPDATE categoria_motivo SET ID_TIPO = (SELECT ID FROM TIPO_CATEGORIA WHERE DESCRICAO = 'Gastos Operacionais') WHERE DESCRICAO = 'Despesas Gerais';
UPDATE categoria_motivo SET ID_TIPO = (SELECT ID FROM TIPO_CATEGORIA WHERE DESCRICAO = 'Gastos Operacionais') WHERE DESCRICAO = 'Despesas Gerais';
UPDATE categoria_motivo SET ID_TIPO = (SELECT ID FROM TIPO_CATEGORIA WHERE DESCRICAO = 'Gastos Operacionais') WHERE DESCRICAO = 'Despesas Gerais';
UPDATE categoria_motivo SET ID_TIPO = (SELECT ID FROM TIPO_CATEGORIA WHERE DESCRICAO = 'Gastos Operacionais') WHERE DESCRICAO = 'Despesas Gerais';
UPDATE categoria_motivo SET ID_TIPO = (SELECT ID FROM TIPO_CATEGORIA WHERE DESCRICAO = 'Gastos Operacionais') WHERE DESCRICAO = 'Despesas Gerais';
UPDATE categoria_motivo SET ID_TIPO = (SELECT ID FROM TIPO_CATEGORIA WHERE DESCRICAO = 'Gastos Operacionais') WHERE DESCRICAO = 'Despesas Gerais';
UPDATE categoria_motivo SET ID_TIPO = (SELECT ID FROM TIPO_CATEGORIA WHERE DESCRICAO = 'Gastos Operacionais') WHERE DESCRICAO = 'Despesas Gerais';
UPDATE categoria_motivo SET ID_TIPO = (SELECT ID FROM TIPO_CATEGORIA WHERE DESCRICAO = 'Gastos Operacionais') WHERE DESCRICAO = 'Despesas Gerais';
UPDATE categoria_motivo SET ID_TIPO = (SELECT ID FROM TIPO_CATEGORIA WHERE DESCRICAO = 'Gastos Operacionais') WHERE DESCRICAO = 'Despesas Gerais';
UPDATE categoria_motivo SET ID_TIPO = (SELECT ID FROM TIPO_CATEGORIA WHERE DESCRICAO = 'Gastos Operacionais') WHERE DESCRICAO = 'Despesas Gerais';
UPDATE categoria_motivo SET ID_TIPO = (SELECT ID FROM TIPO_CATEGORIA WHERE DESCRICAO = 'Gastos Operacionais') WHERE DESCRICAO = 'Despesas Gerais';
UPDATE categoria_motivo SET ID_TIPO = (SELECT ID FROM TIPO_CATEGORIA WHERE DESCRICAO = 'Gastos Operacionais') WHERE DESCRICAO = 'Despesas Gerais';
UPDATE categoria_motivo SET ID_TIPO = (SELECT ID FROM TIPO_CATEGORIA WHERE DESCRICAO = 'Gastos Operacionais') WHERE DESCRICAO = 'Despesas Gerais';
UPDATE categoria_motivo SET ID_TIPO = (SELECT ID FROM TIPO_CATEGORIA WHERE DESCRICAO = 'Gastos Operacionais') WHERE DESCRICAO = 'Encargos';
UPDATE categoria_motivo SET ID_TIPO = (SELECT ID FROM TIPO_CATEGORIA WHERE DESCRICAO = 'Gastos Operacionais') WHERE DESCRICAO = 'Encargos';
UPDATE categoria_motivo SET ID_TIPO = (SELECT ID FROM TIPO_CATEGORIA WHERE DESCRICAO = 'Gastos Operacionais') WHERE DESCRICAO = 'Encargos';
UPDATE categoria_motivo SET ID_TIPO = (SELECT ID FROM TIPO_CATEGORIA WHERE DESCRICAO = 'Gastos Operacionais') WHERE DESCRICAO = 'Encargos';
UPDATE categoria_motivo SET ID_TIPO = (SELECT ID FROM TIPO_CATEGORIA WHERE DESCRICAO = 'Gastos Operacionais') WHERE DESCRICAO = 'Entidades de Classe';
UPDATE categoria_motivo SET ID_TIPO = (SELECT ID FROM TIPO_CATEGORIA WHERE DESCRICAO = 'Gastos Operacionais') WHERE DESCRICAO = 'Entidades de Classe';
UPDATE categoria_motivo SET ID_TIPO = (SELECT ID FROM TIPO_CATEGORIA WHERE DESCRICAO = 'Gastos Operacionais') WHERE DESCRICAO = 'Festas, Eventos e Patrocínios';
UPDATE categoria_motivo SET ID_TIPO = (SELECT ID FROM TIPO_CATEGORIA WHERE DESCRICAO = 'Gastos Operacionais') WHERE DESCRICAO = 'Financeiras';
UPDATE categoria_motivo SET ID_TIPO = (SELECT ID FROM TIPO_CATEGORIA WHERE DESCRICAO = 'Gastos Operacionais') WHERE DESCRICAO = 'Financeiras';
UPDATE categoria_motivo SET ID_TIPO = (SELECT ID FROM TIPO_CATEGORIA WHERE DESCRICAO = 'Gastos Operacionais') WHERE DESCRICAO = 'Honorários Profissionais';
UPDATE categoria_motivo SET ID_TIPO = (SELECT ID FROM TIPO_CATEGORIA WHERE DESCRICAO = 'Gastos Operacionais') WHERE DESCRICAO = 'Honorários Profissionais';
UPDATE categoria_motivo SET ID_TIPO = (SELECT ID FROM TIPO_CATEGORIA WHERE DESCRICAO = 'Gastos Operacionais') WHERE DESCRICAO = 'Honorários Profissionais';
UPDATE categoria_motivo SET ID_TIPO = (SELECT ID FROM TIPO_CATEGORIA WHERE DESCRICAO = 'Gastos Operacionais') WHERE DESCRICAO = 'Impostos';
UPDATE categoria_motivo SET ID_TIPO = (SELECT ID FROM TIPO_CATEGORIA WHERE DESCRICAO = 'Gastos Operacionais') WHERE DESCRICAO = 'Impostos';
UPDATE categoria_motivo SET ID_TIPO = (SELECT ID FROM TIPO_CATEGORIA WHERE DESCRICAO = 'Gastos Operacionais') WHERE DESCRICAO = 'Manutenção';
UPDATE categoria_motivo SET ID_TIPO = (SELECT ID FROM TIPO_CATEGORIA WHERE DESCRICAO = 'Gastos Operacionais') WHERE DESCRICAO = 'Manutenção';
UPDATE categoria_motivo SET ID_TIPO = (SELECT ID FROM TIPO_CATEGORIA WHERE DESCRICAO = 'Gastos Operacionais') WHERE DESCRICAO = 'Manutenção';
UPDATE categoria_motivo SET ID_TIPO = (SELECT ID FROM TIPO_CATEGORIA WHERE DESCRICAO = 'Gastos Operacionais') WHERE DESCRICAO = 'Mão de Obra';
UPDATE categoria_motivo SET ID_TIPO = (SELECT ID FROM TIPO_CATEGORIA WHERE DESCRICAO = 'Gastos Operacionais') WHERE DESCRICAO = 'Mão de Obra';
UPDATE categoria_motivo SET ID_TIPO = (SELECT ID FROM TIPO_CATEGORIA WHERE DESCRICAO = 'Gastos Operacionais') WHERE DESCRICAO = 'Mão de Obra';
UPDATE categoria_motivo SET ID_TIPO = (SELECT ID FROM TIPO_CATEGORIA WHERE DESCRICAO = 'Gastos Operacionais') WHERE DESCRICAO = 'Mão de Obra';
UPDATE categoria_motivo SET ID_TIPO = (SELECT ID FROM TIPO_CATEGORIA WHERE DESCRICAO = 'Gastos Operacionais') WHERE DESCRICAO = 'Mão de Obra';
UPDATE categoria_motivo SET ID_TIPO = (SELECT ID FROM TIPO_CATEGORIA WHERE DESCRICAO = 'Gastos Operacionais') WHERE DESCRICAO = 'Mão de Obra';
UPDATE categoria_motivo SET ID_TIPO = (SELECT ID FROM TIPO_CATEGORIA WHERE DESCRICAO = 'Gastos Operacionais') WHERE DESCRICAO = 'Mão de Obra';
UPDATE categoria_motivo SET ID_TIPO = (SELECT ID FROM TIPO_CATEGORIA WHERE DESCRICAO = 'Gastos Operacionais') WHERE DESCRICAO = 'Mão de Obra';
UPDATE categoria_motivo SET ID_TIPO = (SELECT ID FROM TIPO_CATEGORIA WHERE DESCRICAO = 'Gastos Operacionais') WHERE DESCRICAO = 'Mão de Obra';
UPDATE categoria_motivo SET ID_TIPO = (SELECT ID FROM TIPO_CATEGORIA WHERE DESCRICAO = 'Gastos Operacionais') WHERE DESCRICAO = 'Mão de Obra';
UPDATE categoria_motivo SET ID_TIPO = (SELECT ID FROM TIPO_CATEGORIA WHERE DESCRICAO = 'Gastos Operacionais') WHERE DESCRICAO = 'Mão de Obra';
UPDATE categoria_motivo SET ID_TIPO = (SELECT ID FROM TIPO_CATEGORIA WHERE DESCRICAO = 'Gastos Operacionais') WHERE DESCRICAO = 'Mão de Obra';
UPDATE categoria_motivo SET ID_TIPO = (SELECT ID FROM TIPO_CATEGORIA WHERE DESCRICAO = 'Gastos Operacionais') WHERE DESCRICAO = 'Padrão Sistema';
UPDATE categoria_motivo SET ID_TIPO = (SELECT ID FROM TIPO_CATEGORIA WHERE DESCRICAO = 'Gastos Operacionais') WHERE DESCRICAO = 'Reembolsos';
UPDATE categoria_motivo SET ID_TIPO = (SELECT ID FROM TIPO_CATEGORIA WHERE DESCRICAO = 'Gastos Operacionais') WHERE DESCRICAO = 'Reembolsos';
UPDATE categoria_motivo SET ID_TIPO = (SELECT ID FROM TIPO_CATEGORIA WHERE DESCRICAO = 'Receita Bruta') WHERE DESCRICAO = 'Aporte';
UPDATE categoria_motivo SET ID_TIPO = (SELECT ID FROM TIPO_CATEGORIA WHERE DESCRICAO = 'Receita Bruta') WHERE DESCRICAO = 'Aporte';
UPDATE categoria_motivo SET ID_TIPO = (SELECT ID FROM TIPO_CATEGORIA WHERE DESCRICAO = 'Receita Bruta') WHERE DESCRICAO = 'Aporte';
UPDATE categoria_motivo SET ID_TIPO = (SELECT ID FROM TIPO_CATEGORIA WHERE DESCRICAO = 'Receita Bruta') WHERE DESCRICAO = 'Financeira';
UPDATE categoria_motivo SET ID_TIPO = (SELECT ID FROM TIPO_CATEGORIA WHERE DESCRICAO = 'Receita Bruta') WHERE DESCRICAO = 'Padrão Sistema';
UPDATE categoria_motivo SET ID_TIPO = (SELECT ID FROM TIPO_CATEGORIA WHERE DESCRICAO = 'Receita Bruta') WHERE DESCRICAO = 'Padrão Sistema';

