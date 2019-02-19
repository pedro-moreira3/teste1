CREATE TABLE inteli."categoria_motivo"
(
    "id" serial,
    "descricao" character varying(200),
    PRIMARY KEY ("id")
);
	
ALTER TABLE inteli.motivo
    ADD COLUMN id_categoria bigint;
	
ALTER TABLE ONLY motivo
    ADD CONSTRAINT motivo_categoria FOREIGN KEY (id_categoria) REFERENCES categoria_motivo(id); 	
	
	
INSERT INTO motivo (id_conta,id_empresa,descricao,tipo,excluido,data_exclusao,excluido_por,sigla,id_categoria) VALUES (2,0,'Viagens ','Pagar','N',null,null,null,(select id from categoria_motivo where descricao = 'Reembolsos'));

INSERT INTO motivo (id_conta,id_empresa,descricao,tipo,excluido,data_exclusao,excluido_por,sigla,id_categoria) VALUES (2,0,'Energia Elétrica','Pagar','N',null,null,null,(select id from categoria_motivo where descricao = 'Despesas Gerais'));
	
insert into categoria_motivo (descricao) values('Padrão Sistema');

update motivo set id_categoria = (select id from categoria_motivo where descricao = 'Padrão Sistema') where sigla is not null;


insert into categoria_motivo (descricao) values('Reembolsos');
INSERT INTO motivo (id_conta,id_empresa,descricao,tipo,excluido,data_exclusao,excluido_por,sigla,id_categoria) VALUES (2,0,'Viagens','Pagar','N',null,null,null,(select id from categoria_motivo where descricao = 'Reembolsos'));
INSERT INTO motivo (id_conta,id_empresa,descricao,tipo,excluido,data_exclusao,excluido_por,sigla,id_categoria) VALUES (2,0,'Diversos','Pagar','N',null,null,null,(select id from categoria_motivo where descricao = 'Reembolsos'));
insert into categoria_motivo (descricao) values('Financeira');
INSERT INTO motivo (id_conta,id_empresa,descricao,tipo,excluido,data_exclusao,excluido_por,sigla,id_categoria) VALUES (2,0,'Juros e Multas sem recebimentos em atraso','Pagar','N',null,null,null,(select id from categoria_motivo where descricao = 'Financeira'));
INSERT INTO motivo (id_conta,id_empresa,descricao,tipo,excluido,data_exclusao,excluido_por,sigla,id_categoria) VALUES (2,0,'Rendimentos de Aplicações Financeiras','Pagar','N',null,null,null,(select id from categoria_motivo where descricao = 'Financeira'));
insert into categoria_motivo (descricao) values('Receitas Transitórias');
INSERT INTO motivo (id_conta,id_empresa,descricao,tipo,excluido,data_exclusao,excluido_por,sigla,id_categoria) VALUES (2,0,'Reapresentação de Cheques','Pagar','N',null,null,null,(select id from categoria_motivo where descricao = 'Receitas Transitórias'));
insert into categoria_motivo (descricao) values('Captação de Recursos Terceiros');
INSERT INTO motivo (id_conta,id_empresa,descricao,tipo,excluido,data_exclusao,excluido_por,sigla,id_categoria) VALUES (2,0,'Diretores / Administradores','Pagar','N',null,null,null,(select id from categoria_motivo where descricao = 'Captação de Recursos Terceiros'));
INSERT INTO motivo (id_conta,id_empresa,descricao,tipo,excluido,data_exclusao,excluido_por,sigla,id_categoria) VALUES (2,0,'Instituições Financeiras','Pagar','N',null,null,null,(select id from categoria_motivo where descricao = 'Captação de Recursos Terceiros'));
insert into categoria_motivo (descricao) values('Impostos');
INSERT INTO motivo (id_conta,id_empresa,descricao,tipo,excluido,data_exclusao,excluido_por,sigla,id_categoria) VALUES (2,0,'ISS','Pagar','N',null,null,null,(select id from categoria_motivo where descricao = 'Impostos'));
INSERT INTO motivo (id_conta,id_empresa,descricao,tipo,excluido,data_exclusao,excluido_por,sigla,id_categoria) VALUES (2,0,'ISS Retido de terceirizados','Pagar','N',null,null,null,(select id from categoria_motivo where descricao = 'Impostos'));
INSERT INTO motivo (id_conta,id_empresa,descricao,tipo,excluido,data_exclusao,excluido_por,sigla,id_categoria) VALUES (2,0,'Pis','Pagar','N',null,null,null,(select id from categoria_motivo where descricao = 'Impostos'));
INSERT INTO motivo (id_conta,id_empresa,descricao,tipo,excluido,data_exclusao,excluido_por,sigla,id_categoria) VALUES (2,0,'Cofins','Pagar','N',null,null,null,(select id from categoria_motivo where descricao = 'Impostos'));
INSERT INTO motivo (id_conta,id_empresa,descricao,tipo,excluido,data_exclusao,excluido_por,sigla,id_categoria) VALUES (2,0,'Contrib Social','Pagar','N',null,null,null,(select id from categoria_motivo where descricao = 'Impostos'));
INSERT INTO motivo (id_conta,id_empresa,descricao,tipo,excluido,data_exclusao,excluido_por,sigla,id_categoria) VALUES (2,0,'IRPJ','Pagar','N',null,null,null,(select id from categoria_motivo where descricao = 'Impostos'));
INSERT INTO motivo (id_conta,id_empresa,descricao,tipo,excluido,data_exclusao,excluido_por,sigla,id_categoria) VALUES (2,0,'IPTU','Pagar','N',null,null,null,(select id from categoria_motivo where descricao = 'Impostos'));
INSERT INTO motivo (id_conta,id_empresa,descricao,tipo,excluido,data_exclusao,excluido_por,sigla,id_categoria) VALUES (2,0,'CRO-PR','Pagar','N',null,null,null,(select id from categoria_motivo where descricao = 'Impostos'));
INSERT INTO motivo (id_conta,id_empresa,descricao,tipo,excluido,data_exclusao,excluido_por,sigla,id_categoria) VALUES (2,0,'Taxas','Pagar','N',null,null,null,(select id from categoria_motivo where descricao = 'Impostos'));
insert into categoria_motivo (descricao) values('Aluguel');
INSERT INTO motivo (id_conta,id_empresa,descricao,tipo,excluido,data_exclusao,excluido_por,sigla,id_categoria) VALUES (2,0,'Máquinas e Equipamentos','Pagar','N',null,null,null,(select id from categoria_motivo where descricao = 'Aluguel'));
INSERT INTO motivo (id_conta,id_empresa,descricao,tipo,excluido,data_exclusao,excluido_por,sigla,id_categoria) VALUES (2,0,'Imóvel','Pagar','N',null,null,null,(select id from categoria_motivo where descricao = 'Aluguel'));
insert into categoria_motivo (descricao) values('Manutenção');
INSERT INTO motivo (id_conta,id_empresa,descricao,tipo,excluido,data_exclusao,excluido_por,sigla,id_categoria) VALUES (2,0,'Máquinas, Equipamentos','Pagar','N',null,null,null,(select id from categoria_motivo where descricao = 'Manutenção'));
INSERT INTO motivo (id_conta,id_empresa,descricao,tipo,excluido,data_exclusao,excluido_por,sigla,id_categoria) VALUES (2,0,'Moveis e Utensílios','Pagar','N',null,null,null,(select id from categoria_motivo where descricao = 'Manutenção'));
INSERT INTO motivo (id_conta,id_empresa,descricao,tipo,excluido,data_exclusao,excluido_por,sigla,id_categoria) VALUES (2,0,'Instalações, Imóveis e Elevador','Pagar','N',null,null,null,(select id from categoria_motivo where descricao = 'Manutenção'));
insert into categoria_motivo (descricao) values('Comunicação');
INSERT INTO motivo (id_conta,id_empresa,descricao,tipo,excluido,data_exclusao,excluido_por,sigla,id_categoria) VALUES (2,0,'Telefone e Internet','Pagar','N',null,null,null,(select id from categoria_motivo where descricao = 'Comunicação'));
INSERT INTO motivo (id_conta,id_empresa,descricao,tipo,excluido,data_exclusao,excluido_por,sigla,id_categoria) VALUES (2,0,'Provedor Internet e Domínios','Pagar','N',null,null,null,(select id from categoria_motivo where descricao = 'Comunicação'));
INSERT INTO motivo (id_conta,id_empresa,descricao,tipo,excluido,data_exclusao,excluido_por,sigla,id_categoria) VALUES (2,0,'Correio','Pagar','N',null,null,null,(select id from categoria_motivo where descricao = 'Comunicação'));
INSERT INTO motivo (id_conta,id_empresa,descricao,tipo,excluido,data_exclusao,excluido_por,sigla,id_categoria) VALUES (2,0,'Marketing e Propaganda','Pagar','N',null,null,null,(select id from categoria_motivo where descricao = 'Comunicação'));
insert into categoria_motivo (descricao) values('Festas, Eventos e Patrocínios');
INSERT INTO motivo (id_conta,id_empresa,descricao,tipo,excluido,data_exclusao,excluido_por,sigla,id_categoria) VALUES (2,0,'Confraternizações Colaboradores','Pagar','N',null,null,null,(select id from categoria_motivo where descricao = 'Festas, Eventos e Patrocínios'));
INSERT INTO motivo (id_conta,id_empresa,descricao,tipo,excluido,data_exclusao,excluido_por,sigla,id_categoria) VALUES (2,0,'Eventos e Patrocínios','Pagar','N',null,null,null,(select id from categoria_motivo where descricao = 'Festas, Eventos e Patrocínios'));
insert into categoria_motivo (descricao) values('Despesas Gerais');
INSERT INTO motivo (id_conta,id_empresa,descricao,tipo,excluido,data_exclusao,excluido_por,sigla,id_categoria) VALUES (2,0,'Energia Elétrica','Pagar','N',null,null,null,(select id from categoria_motivo where descricao = 'Despesas Gerais'));
INSERT INTO motivo (id_conta,id_empresa,descricao,tipo,excluido,data_exclusao,excluido_por,sigla,id_categoria) VALUES (2,0,'Água e Esgoto','Pagar','N',null,null,null,(select id from categoria_motivo where descricao = 'Despesas Gerais'));
INSERT INTO motivo (id_conta,id_empresa,descricao,tipo,excluido,data_exclusao,excluido_por,sigla,id_categoria) VALUES (2,0,'Vigilância e Segurança-Alarme','Pagar','N',null,null,null,(select id from categoria_motivo where descricao = 'Despesas Gerais'));
INSERT INTO motivo (id_conta,id_empresa,descricao,tipo,excluido,data_exclusao,excluido_por,sigla,id_categoria) VALUES (2,0,'Reembolso Combustíveis','Pagar','N',null,null,null,(select id from categoria_motivo where descricao = 'Despesas Gerais'));
INSERT INTO motivo (id_conta,id_empresa,descricao,tipo,excluido,data_exclusao,excluido_por,sigla,id_categoria) VALUES (2,0,'Despesas de Viagem','Pagar','N',null,null,null,(select id from categoria_motivo where descricao = 'Despesas Gerais'));
INSERT INTO motivo (id_conta,id_empresa,descricao,tipo,excluido,data_exclusao,excluido_por,sigla,id_categoria) VALUES (2,0,'Taxi / Estacionamentos','Pagar','N',null,null,null,(select id from categoria_motivo where descricao = 'Despesas Gerais'));
INSERT INTO motivo (id_conta,id_empresa,descricao,tipo,excluido,data_exclusao,excluido_por,sigla,id_categoria) VALUES (2,0,'Materiais de Escritório/Expediente','Pagar','N',null,null,null,(select id from categoria_motivo where descricao = 'Despesas Gerais'));
INSERT INTO motivo (id_conta,id_empresa,descricao,tipo,excluido,data_exclusao,excluido_por,sigla,id_categoria) VALUES (2,0,'Matérias de Limpeza e Higiene','Pagar','N',null,null,null,(select id from categoria_motivo where descricao = 'Despesas Gerais'));
INSERT INTO motivo (id_conta,id_empresa,descricao,tipo,excluido,data_exclusao,excluido_por,sigla,id_categoria) VALUES (2,0,'Cantina e Refeições','Pagar','N',null,null,null,(select id from categoria_motivo where descricao = 'Despesas Gerais'));
INSERT INTO motivo (id_conta,id_empresa,descricao,tipo,excluido,data_exclusao,excluido_por,sigla,id_categoria) VALUES (2,0,'Suprimento Maquina Café','Pagar','N',null,null,null,(select id from categoria_motivo where descricao = 'Despesas Gerais'));
INSERT INTO motivo (id_conta,id_empresa,descricao,tipo,excluido,data_exclusao,excluido_por,sigla,id_categoria) VALUES (2,0,'Xerox Encadernações','Pagar','N',null,null,null,(select id from categoria_motivo where descricao = 'Despesas Gerais'));
INSERT INTO motivo (id_conta,id_empresa,descricao,tipo,excluido,data_exclusao,excluido_por,sigla,id_categoria) VALUES (2,0,'Lavanderia','Pagar','N',null,null,null,(select id from categoria_motivo where descricao = 'Despesas Gerais'));
INSERT INTO motivo (id_conta,id_empresa,descricao,tipo,excluido,data_exclusao,excluido_por,sigla,id_categoria) VALUES (2,0,'Uniformes /Jalecos','Pagar','N',null,null,null,(select id from categoria_motivo where descricao = 'Despesas Gerais'));
INSERT INTO motivo (id_conta,id_empresa,descricao,tipo,excluido,data_exclusao,excluido_por,sigla,id_categoria) VALUES (2,0,'Transporte de Resíduos','Pagar','N',null,null,null,(select id from categoria_motivo where descricao = 'Despesas Gerais'));
INSERT INTO motivo (id_conta,id_empresa,descricao,tipo,excluido,data_exclusao,excluido_por,sigla,id_categoria) VALUES (2,0,'Fretes e Carretos/Motoboy','Pagar','N',null,null,null,(select id from categoria_motivo where descricao = 'Despesas Gerais'));
INSERT INTO motivo (id_conta,id_empresa,descricao,tipo,excluido,data_exclusao,excluido_por,sigla,id_categoria) VALUES (2,0,'Seguro Imóvel','Pagar','N',null,null,null,(select id from categoria_motivo where descricao = 'Despesas Gerais'));
INSERT INTO motivo (id_conta,id_empresa,descricao,tipo,excluido,data_exclusao,excluido_por,sigla,id_categoria) VALUES (2,0,'Jornais Revistas e Periódicos','Pagar','N',null,null,null,(select id from categoria_motivo where descricao = 'Despesas Gerais'));
INSERT INTO motivo (id_conta,id_empresa,descricao,tipo,excluido,data_exclusao,excluido_por,sigla,id_categoria) VALUES (2,0,'Cartório/Certidões','Pagar','N',null,null,null,(select id from categoria_motivo where descricao = 'Despesas Gerais'));
INSERT INTO motivo (id_conta,id_empresa,descricao,tipo,excluido,data_exclusao,excluido_por,sigla,id_categoria) VALUES (2,0,'Taxas/Anuidades Diversas','Pagar','N',null,null,null,(select id from categoria_motivo where descricao = 'Despesas Gerais'));
INSERT INTO motivo (id_conta,id_empresa,descricao,tipo,excluido,data_exclusao,excluido_por,sigla,id_categoria) VALUES (2,0,'Despesas Particulares','Pagar','N',null,null,null,(select id from categoria_motivo where descricao = 'Despesas Gerais'));
insert into categoria_motivo (descricao) values('Despesas Diretas com a Operacional');
INSERT INTO motivo (id_conta,id_empresa,descricao,tipo,excluido,data_exclusao,excluido_por,sigla,id_categoria) VALUES (2,0,'Materiais Odontológicos','Pagar','N',null,null,null,(select id from categoria_motivo where descricao = 'Despesas Diretas com a Operacional'));
INSERT INTO motivo (id_conta,id_empresa,descricao,tipo,excluido,data_exclusao,excluido_por,sigla,id_categoria) VALUES (2,0,'Laboratório e Materiais de Prótese','Pagar','N',null,null,null,(select id from categoria_motivo where descricao = 'Despesas Diretas com a Operacional'));
INSERT INTO motivo (id_conta,id_empresa,descricao,tipo,excluido,data_exclusao,excluido_por,sigla,id_categoria) VALUES (2,0,'Manutenção de Material Odontológico','Pagar','N',null,null,null,(select id from categoria_motivo where descricao = 'Despesas Diretas com a Operacional'));
INSERT INTO motivo (id_conta,id_empresa,descricao,tipo,excluido,data_exclusao,excluido_por,sigla,id_categoria) VALUES (2,0,'Instrumentos Odontológico','Pagar','N',null,null,null,(select id from categoria_motivo where descricao = 'Despesas Diretas com a Operacional'));
insert into categoria_motivo (descricao) values('Mão de Obra');
INSERT INTO motivo (id_conta,id_empresa,descricao,tipo,excluido,data_exclusao,excluido_por,sigla,id_categoria) VALUES (2,0,'Salários/Insalubridade','Pagar','N',null,null,null,(select id from categoria_motivo where descricao = 'Mão de Obra'));
INSERT INTO motivo (id_conta,id_empresa,descricao,tipo,excluido,data_exclusao,excluido_por,sigla,id_categoria) VALUES (2,0,'Salario Gestor','Pagar','N',null,null,null,(select id from categoria_motivo where descricao = 'Mão de Obra'));
INSERT INTO motivo (id_conta,id_empresa,descricao,tipo,excluido,data_exclusao,excluido_por,sigla,id_categoria) VALUES (2,0,'Adiantamento de Salario','Pagar','N',null,null,null,(select id from categoria_motivo where descricao = 'Mão de Obra'));
INSERT INTO motivo (id_conta,id_empresa,descricao,tipo,excluido,data_exclusao,excluido_por,sigla,id_categoria) VALUES (2,0,'13o Salario','Pagar','N',null,null,null,(select id from categoria_motivo where descricao = 'Mão de Obra'));
INSERT INTO motivo (id_conta,id_empresa,descricao,tipo,excluido,data_exclusao,excluido_por,sigla,id_categoria) VALUES (2,0,'Ferias','Pagar','N',null,null,null,(select id from categoria_motivo where descricao = 'Mão de Obra'));
INSERT INTO motivo (id_conta,id_empresa,descricao,tipo,excluido,data_exclusao,excluido_por,sigla,id_categoria) VALUES (2,0,'Rescisões','Pagar','N',null,null,null,(select id from categoria_motivo where descricao = 'Mão de Obra'));
INSERT INTO motivo (id_conta,id_empresa,descricao,tipo,excluido,data_exclusao,excluido_por,sigla,id_categoria) VALUES (2,0,'Medicina do Trabalho','Pagar','N',null,null,null,(select id from categoria_motivo where descricao = 'Mão de Obra'));
INSERT INTO motivo (id_conta,id_empresa,descricao,tipo,excluido,data_exclusao,excluido_por,sigla,id_categoria) VALUES (2,0,'Vale Transporte','Pagar','N',null,null,null,(select id from categoria_motivo where descricao = 'Mão de Obra'));
INSERT INTO motivo (id_conta,id_empresa,descricao,tipo,excluido,data_exclusao,excluido_por,sigla,id_categoria) VALUES (2,0,'Convenio Médico/Farmácia','Pagar','N',null,null,null,(select id from categoria_motivo where descricao = 'Mão de Obra'));
INSERT INTO motivo (id_conta,id_empresa,descricao,tipo,excluido,data_exclusao,excluido_por,sigla,id_categoria) VALUES (2,0,'Auxilio Alimentação','Pagar','N',null,null,null,(select id from categoria_motivo where descricao = 'Mão de Obra'));
INSERT INTO motivo (id_conta,id_empresa,descricao,tipo,excluido,data_exclusao,excluido_por,sigla,id_categoria) VALUES (2,0,'Convenio Auxilio Funeral','Pagar','N',null,null,null,(select id from categoria_motivo where descricao = 'Mão de Obra'));
INSERT INTO motivo (id_conta,id_empresa,descricao,tipo,excluido,data_exclusao,excluido_por,sigla,id_categoria) VALUES (2,0,'Cursos e Treinamentos','Pagar','N',null,null,null,(select id from categoria_motivo where descricao = 'Mão de Obra'));
INSERT INTO motivo (id_conta,id_empresa,descricao,tipo,excluido,data_exclusao,excluido_por,sigla,id_categoria) VALUES (2,0,'Profissionais Liberais Contratados','Pagar','N',null,null,null,(select id from categoria_motivo where descricao = 'Mão de Obra'));
INSERT INTO motivo (id_conta,id_empresa,descricao,tipo,excluido,data_exclusao,excluido_por,sigla,id_categoria) VALUES (2,0,'Profissionais Liberais Eventuais','Pagar','N',null,null,null,(select id from categoria_motivo where descricao = 'Mão de Obra'));
INSERT INTO motivo (id_conta,id_empresa,descricao,tipo,excluido,data_exclusao,excluido_por,sigla,id_categoria) VALUES (2,0,'Limpeza e Conservação','Pagar','N',null,null,null,(select id from categoria_motivo where descricao = 'Mão de Obra'));
insert into categoria_motivo (descricao) values('Encargos');
INSERT INTO motivo (id_conta,id_empresa,descricao,tipo,excluido,data_exclusao,excluido_por,sigla,id_categoria) VALUES (2,0,'INSS','Pagar','N',null,null,null,(select id from categoria_motivo where descricao = 'Encargos'));
INSERT INTO motivo (id_conta,id_empresa,descricao,tipo,excluido,data_exclusao,excluido_por,sigla,id_categoria) VALUES (2,0,'FGTS','Pagar','N',null,null,null,(select id from categoria_motivo where descricao = 'Encargos'));
INSERT INTO motivo (id_conta,id_empresa,descricao,tipo,excluido,data_exclusao,excluido_por,sigla,id_categoria) VALUES (2,0,'Contribuição Sindical/Patronal','Pagar','N',null,null,null,(select id from categoria_motivo where descricao = 'Encargos'));
INSERT INTO motivo (id_conta,id_empresa,descricao,tipo,excluido,data_exclusao,excluido_por,sigla,id_categoria) VALUES (2,0,'Contribuição Assistencial','Pagar','N',null,null,null,(select id from categoria_motivo where descricao = 'Encargos'));
insert into categoria_motivo (descricao) values('Honorários Profissionais');
INSERT INTO motivo (id_conta,id_empresa,descricao,tipo,excluido,data_exclusao,excluido_por,sigla,id_categoria) VALUES (2,0,'Assessoria Jurídica','Pagar','N',null,null,null,(select id from categoria_motivo where descricao = 'Honorários Profissionais'));
INSERT INTO motivo (id_conta,id_empresa,descricao,tipo,excluido,data_exclusao,excluido_por,sigla,id_categoria) VALUES (2,0,'Assessoria Contábil','Pagar','N',null,null,null,(select id from categoria_motivo where descricao = 'Honorários Profissionais'));
INSERT INTO motivo (id_conta,id_empresa,descricao,tipo,excluido,data_exclusao,excluido_por,sigla,id_categoria) VALUES (2,0,'Taxas Serviços Extras Contabilide','Pagar','N',null,null,null,(select id from categoria_motivo where descricao = 'Honorários Profissionais'));
insert into categoria_motivo (descricao) values('Financeiras');
INSERT INTO motivo (id_conta,id_empresa,descricao,tipo,excluido,data_exclusao,excluido_por,sigla,id_categoria) VALUES (2,0,'Taxa Adm de Cartões','Pagar','N',null,null,null,(select id from categoria_motivo where descricao = 'Financeiras'));
INSERT INTO motivo (id_conta,id_empresa,descricao,tipo,excluido,data_exclusao,excluido_por,sigla,id_categoria) VALUES (2,0,'Tarifa Docs/Teds','Pagar','N',null,null,null,(select id from categoria_motivo where descricao = 'Financeiras'));
INSERT INTO motivo (id_conta,id_empresa,descricao,tipo,excluido,data_exclusao,excluido_por,sigla,id_categoria) VALUES (2,0,'Tarifa Manutenção de Conta','Pagar','N',null,null,null,(select id from categoria_motivo where descricao = 'Financeiras'));
INSERT INTO motivo (id_conta,id_empresa,descricao,tipo,excluido,data_exclusao,excluido_por,sigla,id_categoria) VALUES (2,0,'Juros e Encargos Uso Limite','Pagar','N',null,null,null,(select id from categoria_motivo where descricao = 'Financeiras'));
INSERT INTO motivo (id_conta,id_empresa,descricao,tipo,excluido,data_exclusao,excluido_por,sigla,id_categoria) VALUES (2,0,'Descontos Concedidos','Pagar','N',null,null,null,(select id from categoria_motivo where descricao = 'Financeiras'));
insert into categoria_motivo (descricao) values('Entidades de Classe');
INSERT INTO motivo (id_conta,id_empresa,descricao,tipo,excluido,data_exclusao,excluido_por,sigla,id_categoria) VALUES (2,0,'Sindicato da Classe','Pagar','N',null,null,null,(select id from categoria_motivo where descricao = 'Entidades de Classe'));
INSERT INTO motivo (id_conta,id_empresa,descricao,tipo,excluido,data_exclusao,excluido_por,sigla,id_categoria) VALUES (2,0,'SINDESC','Pagar','N',null,null,null,(select id from categoria_motivo where descricao = 'Entidades de Classe'));
insert into categoria_motivo (descricao) values('Pagamento de Recursos Terceiros');
INSERT INTO motivo (id_conta,id_empresa,descricao,tipo,excluido,data_exclusao,excluido_por,sigla,id_categoria) VALUES (2,0,'Diretores/Administradores','Pagar','N',null,null,null,(select id from categoria_motivo where descricao = 'Pagamento de Recursos Terceiros'));
INSERT INTO motivo (id_conta,id_empresa,descricao,tipo,excluido,data_exclusao,excluido_por,sigla,id_categoria) VALUES (2,0,'Instituições Financeiras','Pagar','N',null,null,null,(select id from categoria_motivo where descricao = 'Pagamento de Recursos Terceiros'));
insert into categoria_motivo (descricao) values('Despesas Transitórias');
INSERT INTO motivo (id_conta,id_empresa,descricao,tipo,excluido,data_exclusao,excluido_por,sigla,id_categoria) VALUES (2,0,'Devolução de Cheques','Pagar','N',null,null,null,(select id from categoria_motivo where descricao = 'Despesas Transitórias'));
INSERT INTO motivo (id_conta,id_empresa,descricao,tipo,excluido,data_exclusao,excluido_por,sigla,id_categoria) VALUES (2,0,'Cancelamento de Tratamentos','Pagar','N',null,null,null,(select id from categoria_motivo where descricao = 'Despesas Transitórias'));
insert into categoria_motivo (descricao) values('Imobilizado');
INSERT INTO motivo (id_conta,id_empresa,descricao,tipo,excluido,data_exclusao,excluido_por,sigla,id_categoria) VALUES (2,0,'Máquinas e Equipamentos','Pagar','N',null,null,null,(select id from categoria_motivo where descricao = 'Imobilizado'));
INSERT INTO motivo (id_conta,id_empresa,descricao,tipo,excluido,data_exclusao,excluido_por,sigla,id_categoria) VALUES (2,0,'Moveis e Utensílios','Pagar','N',null,null,null,(select id from categoria_motivo where descricao = 'Imobilizado'));
INSERT INTO motivo (id_conta,id_empresa,descricao,tipo,excluido,data_exclusao,excluido_por,sigla,id_categoria) VALUES (2,0,'Imóveis','Pagar','N',null,null,null,(select id from categoria_motivo where descricao = 'Imobilizado'));
