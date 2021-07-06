INSERT INTO SEG_OBJETO (OBJ_INT_CODPAI,OBJ_STR_DES,OBJ_CHA_STS,OBJ_STR_CAMINHO,SIS_INT_COD,OBJ_INT_ORDEM,OBJ_CHA_TIPO,OBJ_STR_ICON) VALUES (null,'Administração','A','LABEL_RAIZ',123,1,'L','');
INSERT INTO SEG_OBJETO (OBJ_INT_CODPAI,OBJ_STR_DES,OBJ_CHA_STS,OBJ_STR_CAMINHO,SIS_INT_COD,OBJ_INT_ORDEM,OBJ_CHA_TIPO,OBJ_STR_ICON) VALUES ((select OBJ_INT_COD from SEG_OBJETO where OBJ_STR_DES = 'Administração' and SIS_INT_COD = 123),'Help','A','help.jsf',123,1,'T','fa fa-question-circle');
INSERT INTO SEG_OBJETO (OBJ_INT_CODPAI,OBJ_STR_DES,OBJ_CHA_STS,OBJ_STR_CAMINHO,SIS_INT_COD,OBJ_INT_ORDEM,OBJ_CHA_TIPO,OBJ_STR_ICON) VALUES ((select OBJ_INT_COD from SEG_OBJETO where OBJ_STR_DES = 'Administração' and SIS_INT_COD = 123),'Plano'     ,'A'        ,'plano.jsf'     ,123        ,1            ,'T'         ,'fa fa-money');
insert into SEG_PEROBJETO (PER_INT_COD, OBJ_INT_COD) values ( (select PER_INT_COD from SEG_PERFIL where PER_STR_DES = 'Administradores' and SIS_INT_COD = 123),(select OBJ_INT_COD from SEG_OBJETO where OBJ_STR_DES = 'Plano' and SIS_INT_COD = 123));
INSERT INTO SEG_OBJETO (OBJ_INT_CODPAI,OBJ_STR_DES,OBJ_CHA_STS,OBJ_STR_CAMINHO,SIS_INT_COD,OBJ_INT_ORDEM,OBJ_CHA_TIPO,OBJ_STR_ICON) VALUES ((select OBJ_INT_COD from SEG_OBJETO where OBJ_STR_DES = 'Estoque' and OBJ_STR_CAMINHO = 'LABEL_RAIZ'),'Cadastros','A','LABEL_RAIZ',(select SIS_INT_COD from SEG_SISTEMA where SIS_STR_DES = 'Odonto'),5,'L','fa fa-caret-right');

delete from SEG_PEROBJETO where OBJ_INT_COD = (select OBJ_INT_COD from SEG_OBJETO where OBJ_STR_DES = 'Domínios');
delete from SEG_PEROBJETO where OBJ_INT_COD=(select OBJ_INT_COD from SEG_OBJETO where  OBJ_STR_DES = 'Desconto');
delete from SEG_PEROBJETO where OBJ_INT_COD=(select OBJ_INT_COD from SEG_OBJETO where  OBJ_STR_DES = 'Pedido de Compras');
delete from SEG_PEROBJETO where OBJ_INT_COD=(select OBJ_INT_COD from SEG_OBJETO where  OBJ_STR_DES = 'Cockpit');

----- UPDATE ----- 

update SEG_OBJETO set OBJ_STR_DES = 'Administrativo' where OBJ_STR_DES = 'RH';
update SEG_OBJETO set OBJ_STR_DES = 'Financeiro' where OBJ_STR_DES = 'Contabilidade';
update SEG_OBJETO set OBJ_INT_CODPAI = (select OBJ_INT_COD from SEG_OBJETO where OBJ_STR_DES = 'Administrativo' )  where OBJ_STR_DES = 'Anamnese';
update SEG_OBJETO set OBJ_INT_CODPAI = (select OBJ_INT_COD from SEG_OBJETO where OBJ_STR_DES = 'Administrativo' )  where OBJ_STR_DES = 'Procedimento';
update SEG_OBJETO set OBJ_INT_CODPAI = (select OBJ_INT_COD from SEG_OBJETO where OBJ_STR_DES = 'Financeiro') where OBJ_STR_DES = 'Lançamentos';
update SEG_OBJETO set OBJ_STR_DES = 'Recebimentos' where OBJ_STR_DES = 'Lançamentos';
update SEG_OBJETO set OBJ_STR_DES = 'Empréstimo de Kits' where OBJ_STR_DES = 'Empréstimo de Materiais';
update SEG_OBJETO set OBJ_INT_CODPAI = (select OBJ_INT_COD from SEG_OBJETO where  OBJ_STR_DES = 'Relatório' and SIS_INT_COD = 123) where OBJ_STR_DES = 'Avaliações';
update SEG_OBJETO set OBJ_INT_CODPAI = (select OBJ_INT_COD from SEG_OBJETO where OBJ_STR_DES = 'Administrativo' )  where OBJ_STR_DES = 'Convênio';
update SEG_OBJETO set OBJ_INT_CODPAI = (select OBJ_INT_COD from SEG_OBJETO where OBJ_STR_DES = 'Administrativo' )  where OBJ_STR_DES =  'Convênio Procedimento';
update SEG_OBJETO set OBJ_STR_DES = 'Entrega para Lavagem', OBJ_INT_CODPAI = (select OBJ_INT_COD from SEG_OBJETO where OBJ_STR_DES = 'Esterilização' and OBJ_STR_CAMINHO = 'LABEL_RAIZ') where OBJ_STR_CAMINHO = 'lavagem.jsf'; 
update SEG_OBJETO set OBJ_STR_DES = 'Devolução da Lavagem', OBJ_INT_CODPAI = (select OBJ_INT_COD from SEG_OBJETO where OBJ_STR_DES = 'Esterilização' and OBJ_STR_CAMINHO = 'LABEL_RAIZ') where OBJ_STR_CAMINHO = 'devolucaoLavagem.jsf'; 
update SEG_OBJETO set OBJ_STR_DES = 'Devolução da Esterilização', OBJ_INT_CODPAI = (select OBJ_INT_COD from SEG_OBJETO where OBJ_STR_DES = 'Esterilização' and OBJ_STR_CAMINHO = 'LABEL_RAIZ') where OBJ_STR_CAMINHO = 'devolucaoesterilizacao.jsf'; 
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
update SEG_OBJETO set OBJ_STR_DES='Relatório de Avaliações dos Atendimentos' where OBJ_INT_COD = (select OBJ_INT_COD from SEG_OBJETO where OBJ_STR_DES = 'Avaliações');
update SEG_OBJETO set OBJ_STR_DES='Relatório de Planos de Tratamento' where OBJ_INT_COD = (select obj_int_cod from SEG_OBJETO where obj_str_caminho='relatorioPlanoTratamento.jsf');
update SEG_OBJETO set OBJ_STR_DES='Relatório de Procedimentos' where OBJ_INT_COD = (select obj_int_cod from SEG_OBJETO where obj_str_caminho='relatorioProcedimento.jsf');
update SEG_OBJETO set OBJ_STR_DES='Entrada de Materiais' where OBJ_INT_COD = (select obj_int_cod from SEG_OBJETO where obj_str_caminho='material.jsf');
update SEG_OBJETO set OBJ_STR_DES='Movimentação de Materiais' where OBJ_INT_COD = (select obj_int_cod from SEG_OBJETO where obj_str_caminho='movimentacao.jsf');
update SEG_OBJETO set OBJ_STR_DES='Conferência de Estoque' where OBJ_INT_COD = (select obj_int_cod from SEG_OBJETO where OBJ_STR_DES = 'Conferência' and OBJ_STR_CAMINHO = 'LABEL_RAIZ');
update SEG_OBJETO set OBJ_STR_DES='Relatório de Estoque' where OBJ_INT_COD = (select obj_int_cod from SEG_OBJETO where obj_str_caminho='relatorioMaterial.jsf');
update SEG_OBJETO set OBJ_STR_DES='Relatório de Esterilização' where OBJ_INT_COD = (select obj_int_cod from SEG_OBJETO where obj_str_caminho='relatorioEsterilizacao.jsf');
update SEG_OBJETO set OBJ_STR_DES='Recebimento dos Pacientes' where OBJ_INT_COD = (select obj_int_cod from SEG_OBJETO where obj_str_caminho='pagamento.jsf');
update SEG_OBJETO set OBJ_STR_DES='Registro de Custos Diretos' where OBJ_INT_COD = (select obj_int_cod from SEG_OBJETO where obj_str_caminho='custo.jsf');
update SEG_OBJETO set OBJ_STR_DES='Repasse dos Profissionais' where OBJ_INT_COD = (select obj_int_cod from SEG_OBJETO where obj_str_caminho='faturamento.jsf');
update SEG_OBJETO set OBJ_STR_DES='Cadastro de Tarifas de Cartões' where OBJ_INT_COD = (select obj_int_cod from SEG_OBJETO where obj_str_caminho='tarifa.jsf');
update SEG_OBJETO set OBJ_STR_DES='Relatório de Orçamentos' where OBJ_INT_COD = (select obj_int_cod from SEG_OBJETO where obj_str_caminho='relatorioOrcamento.jsf');
update SEG_OBJETO set OBJ_STR_DES='Relatório de Recebimentos' where OBJ_INT_COD = (select obj_int_cod from SEG_OBJETO where obj_str_caminho='relatorioLancamento.jsf');
update SEG_OBJETO set OBJ_STR_DES='Relatório de Repasses' where OBJ_INT_COD = (select obj_int_cod from SEG_OBJETO where obj_str_caminho='relatorioBalanco.jsf');
update SEG_OBJETO set OBJ_STR_DES='Configuração de Documentos' where OBJ_INT_COD = (select obj_int_cod from SEG_OBJETO where obj_str_caminho='configurarDocumento.jsf');
update SEG_OBJETO set OBJ_STR_DES='Emissão de Documentos' where OBJ_INT_COD = (select obj_int_cod from SEG_OBJETO where OBJ_STR_DES = 'Documento');
update SEG_OBJETO set OBJ_STR_DES='Cadastro de Retornos' where OBJ_INT_COD = (select obj_int_cod from SEG_OBJETO where obj_str_caminho='retorno.jsf');
update SEG_OBJETO set OBJ_STR_DES='Devolução de Materiais' where OBJ_INT_COD = (select obj_int_cod from SEG_OBJETO where OBJ_STR_DES = 'Devolução Material');
update SEG_OBJETO set OBJ_STR_DES='Devolução de Kits' where OBJ_INT_COD = (select obj_int_cod from SEG_OBJETO where obj_str_caminho='devolucaomaterial.jsf' and sis_int_cod = (select SIS_INT_COD from SEG_SISTEMA where SIS_STR_DES = 'Odonto'));
update SEG_OBJETO set OBJ_STR_DES='Empréstimo de Materiais' where OBJ_INT_COD = (select obj_int_cod from SEG_OBJETO where OBJ_STR_DES = 'Emprestimo');
update SEG_OBJETO set OBJ_STR_DES='Conferência de Materiais' where OBJ_INT_COD = (select obj_int_cod from SEG_OBJETO where obj_str_caminho='conferenciaMaterial.jsf');
update SEG_OBJETO set OBJ_STR_DES='Consulta Conferências' where OBJ_INT_COD = (select obj_int_cod from SEG_OBJETO where obj_str_caminho='conferencia.jsf');
update SEG_OBJETO set OBJ_STR_DES='Registro de Pagamentos/Recebimentos' where OBJ_INT_COD = (select obj_int_cod from SEG_OBJETO where obj_str_caminho='contaReceberPagar.jsf');
update SEG_OBJETO set OBJ_STR_DES='Cadastro de Especialidades' where OBJ_INT_COD = (select obj_int_cod from SEG_OBJETO where obj_str_caminho='especialidade.jsf');
update SEG_OBJETO set OBJ_STR_DES='Cadastro de Filiais' where OBJ_INT_COD = (select obj_int_cod from SEG_OBJETO where obj_str_caminho='filial.jsf');
update SEG_OBJETO set OBJ_STR_DES='Configuração de Anamnese' where OBJ_INT_COD = (select obj_int_cod from SEG_OBJETO where OBJ_STR_DES = 'Anamnese');
update SEG_OBJETO set OBJ_STR_DES='Cadastro de Perguntas' where OBJ_INT_COD = (select obj_int_cod from SEG_OBJETO where obj_str_caminho='pergunta.jsf');
update SEG_OBJETO set OBJ_STR_DES='Cadastro de Respostas' where OBJ_INT_COD = (select obj_int_cod from SEG_OBJETO where obj_str_caminho='resposta.jsf');
update SEG_OBJETO set OBJ_STR_DES='Cadastro de Procedimentos' where OBJ_INT_COD = (select obj_int_cod from SEG_OBJETO where obj_str_caminho='procedimento.jsf');
update SEG_OBJETO set OBJ_STR_DES='Cadastro de Convênios' where OBJ_INT_COD = (select obj_int_cod from SEG_OBJETO where obj_str_caminho='convenio.jsf');
update SEG_OBJETO set OBJ_STR_DES='Associação Convênio-Procedimento' where OBJ_INT_COD = (select obj_int_cod from SEG_OBJETO where obj_str_caminho='convenioProcedimento.jsf');
update SEG_OBJETO set OBJ_STR_DES='Cadastro de Profissionais' where OBJ_INT_COD = (select obj_int_cod from SEG_OBJETO where obj_str_caminho='profissional.jsf' and sis_int_cod = (
select SIS_INT_COD from SEG_SISTEMA where SIS_STR_DES = 'Odonto'));
update SEG_OBJETO set OBJ_STR_DES='Orçamento' where OBJ_INT_COD = (select obj_int_cod from SEG_OBJETO where obj_str_caminho='documentoOrcamento.jsf');
-- Clínica
update SEG_OBJETO set
OBJ_INT_CODPAI = (select OBJ_INT_COD from SEG_OBJETO where OBJ_STR_DES = 'Clínica' and OBJ_STR_CAMINHO = 'LABEL_RAIZ')
where obj_int_cod = (select obj_int_cod from SEG_OBJETO where OBJ_STR_DES = 'Relatório de Avaliações dos Atendimentos');
update SEG_OBJETO set
OBJ_INT_CODPAI = (select OBJ_INT_COD from SEG_OBJETO where OBJ_STR_DES = 'Clínica' and OBJ_STR_CAMINHO = 'LABEL_RAIZ')
where obj_int_cod = (select obj_int_cod from SEG_OBJETO where OBJ_STR_DES = 'Fila de Atendimento');
update SEG_OBJETO set
OBJ_INT_CODPAI = (select OBJ_INT_COD from SEG_OBJETO where OBJ_STR_DES = 'Clínica' and OBJ_STR_CAMINHO = 'LABEL_RAIZ')
where obj_int_cod = (select obj_int_cod from SEG_OBJETO where OBJ_STR_DES = 'Relatório de Planos de Tratamento');
update SEG_OBJETO set
OBJ_INT_CODPAI = (select OBJ_INT_COD from SEG_OBJETO where OBJ_STR_DES = 'Clínica' and OBJ_STR_CAMINHO = 'LABEL_RAIZ')
where obj_int_cod = (select obj_int_cod from SEG_OBJETO where OBJ_STR_DES = 'Relatório de Procedimentos');
-- Estoque
update SEG_OBJETO set
OBJ_INT_CODPAI = (select OBJ_INT_COD from SEG_OBJETO where OBJ_STR_DES = 'Cadastros' and OBJ_INT_CODPAI is not null)
where obj_int_cod = (select obj_int_cod from SEG_OBJETO where OBJ_STR_DES = 'Item');
update SEG_OBJETO set
OBJ_INT_CODPAI = (select OBJ_INT_COD from SEG_OBJETO where OBJ_STR_DES = 'Cadastros' and OBJ_INT_CODPAI is not null)
where obj_int_cod = (select obj_int_cod from SEG_OBJETO where OBJ_STR_DES = 'Local');
update SEG_OBJETO set
OBJ_INT_CODPAI = (select OBJ_INT_COD from SEG_OBJETO where OBJ_STR_DES = 'Cadastros' and OBJ_INT_CODPAI is not null)
where obj_int_cod = (select obj_int_cod from SEG_OBJETO where OBJ_STR_DES = 'Marca');
update SEG_OBJETO set
OBJ_INT_CODPAI = (select OBJ_INT_COD from SEG_OBJETO where OBJ_STR_DES = 'Cadastros' and OBJ_INT_CODPAI is not null)
where obj_int_cod = (select obj_int_cod from SEG_OBJETO where OBJ_STR_DES = 'Configuração de Kits');
update SEG_OBJETO set
OBJ_INT_CODPAI = (select OBJ_INT_COD from SEG_OBJETO where OBJ_STR_DES = 'Cadastros' and OBJ_INT_CODPAI is not null)
where obj_int_cod = (select obj_int_cod from SEG_OBJETO where OBJ_STR_DES = 'Fornecedor' AND OBJ_STR_CAMINHO = 'fornecedor.jsf');
update SEG_OBJETO set
OBJ_INT_CODPAI = (select OBJ_INT_COD from SEG_OBJETO where OBJ_STR_DES = 'Estoque' and OBJ_STR_CAMINHO = 'LABEL_RAIZ')
where obj_int_cod = (select obj_int_cod from SEG_OBJETO where OBJ_STR_DES = 'Entrada de Materiais');
update SEG_OBJETO set
OBJ_INT_CODPAI = (select OBJ_INT_COD from SEG_OBJETO where OBJ_STR_DES = 'Estoque' and OBJ_STR_CAMINHO = 'LABEL_RAIZ')
where obj_int_cod = (select obj_int_cod from SEG_OBJETO where OBJ_STR_DES = 'Movimentação de Materiais');
update SEG_OBJETO set
OBJ_INT_CODPAI = (select OBJ_INT_COD from SEG_OBJETO where OBJ_STR_DES = 'Estoque' and OBJ_STR_CAMINHO = 'LABEL_RAIZ')
where obj_int_cod = (select obj_int_cod from SEG_OBJETO where OBJ_STR_DES = 'Conferência de Estoque');
update SEG_OBJETO set
OBJ_INT_CODPAI = (select OBJ_INT_COD from SEG_OBJETO where OBJ_STR_DES = 'Estoque' and OBJ_STR_CAMINHO = 'LABEL_RAIZ')
where obj_int_cod = (select obj_int_cod from SEG_OBJETO where OBJ_STR_DES = 'Estoque Mínimo');
update SEG_OBJETO set
OBJ_INT_CODPAI = (select OBJ_INT_COD from SEG_OBJETO where OBJ_STR_DES = 'Estoque' and OBJ_STR_CAMINHO = 'LABEL_RAIZ')
where obj_int_cod = (select obj_int_cod from SEG_OBJETO where OBJ_STR_DES = 'Relatório de Estoque');
-- Esterilização
update SEG_OBJETO set
OBJ_INT_CODPAI = (select OBJ_INT_COD from SEG_OBJETO where OBJ_STR_DES = 'Esterilização' and OBJ_STR_CAMINHO = 'LABEL_RAIZ')
where obj_int_cod = (select obj_int_cod from SEG_OBJETO where OBJ_STR_DES = 'Relatório de Esterilização');
-- Financeiro
update SEG_OBJETO set
OBJ_INT_CODPAI = (select OBJ_INT_COD from SEG_OBJETO where OBJ_STR_DES = 'Financeiro' and OBJ_STR_CAMINHO = 'LABEL_RAIZ')
where obj_int_cod = (select obj_int_cod from SEG_OBJETO where OBJ_STR_DES = 'Relatório de Orçamentos');
update SEG_OBJETO set
OBJ_INT_CODPAI = (select OBJ_INT_COD from SEG_OBJETO where OBJ_STR_DES = 'Financeiro' and OBJ_STR_CAMINHO = 'LABEL_RAIZ')
where obj_int_cod = (select obj_int_cod from SEG_OBJETO where OBJ_STR_DES = 'Relatório de Recebimentos');
update SEG_OBJETO set
OBJ_INT_CODPAI = (select OBJ_INT_COD from SEG_OBJETO where OBJ_STR_DES = 'Financeiro' and OBJ_STR_CAMINHO = 'LABEL_RAIZ')
where obj_int_cod = (select obj_int_cod from SEG_OBJETO where OBJ_STR_DES = 'Relatório de Repasses');
update SEG_OBJETO set
OBJ_INT_CODPAI = (select OBJ_INT_COD from SEG_OBJETO where OBJ_STR_DES = 'Financeiro' and OBJ_STR_CAMINHO = 'LABEL_RAIZ')
where obj_int_cod = (select obj_int_cod from SEG_OBJETO where OBJ_STR_DES = 'Extrato Financeiro');
-- Administrativo
update SEG_OBJETO set
OBJ_INT_CODPAI = (select OBJ_INT_COD from SEG_OBJETO where OBJ_STR_DES = 'Administrativo' and OBJ_STR_CAMINHO = 'LABEL_RAIZ')
where obj_int_cod = (select obj_int_cod from SEG_OBJETO where OBJ_STR_DES = 'Configuração de Documentos');
update SEG_OBJETO set
OBJ_INT_CODPAI = (select OBJ_INT_COD from SEG_OBJETO where OBJ_STR_DES = 'Administrativo' and OBJ_STR_CAMINHO = 'LABEL_RAIZ')
where obj_int_cod = (select obj_int_cod from SEG_OBJETO where OBJ_STR_DES = 'Configuração de Anamnese');
update SEG_OBJETO set
OBJ_INT_CODPAI = (select OBJ_INT_COD from SEG_OBJETO where OBJ_STR_DES = 'Administrativo' and OBJ_STR_CAMINHO = 'LABEL_RAIZ')
where obj_int_cod = (select obj_int_cod from SEG_OBJETO where OBJ_STR_DES = 'Cadastro de Procedimentos');
update SEG_OBJETO set OBJ_INT_ORDEM = 1 where OBJ_INT_CODPAI = (select OBJ_INT_COD from SEG_OBJETO where OBJ_STR_DES = 'Clínica' and OBJ_STR_CAMINHO = 'LABEL_RAIZ');
update SEG_OBJETO set OBJ_INT_ORDEM = 1 where OBJ_STR_DES='Paciente' and OBJ_INT_CODPAI = (select OBJ_INT_COD from SEG_OBJETO where OBJ_STR_DES = 'Clínica' and OBJ_STR_CAMINHO = 'LABEL_RAIZ');
update SEG_OBJETO set OBJ_INT_ORDEM = 2 where OBJ_STR_DES='Plano Tratamento' and OBJ_INT_CODPAI = (select OBJ_INT_COD from SEG_OBJETO where OBJ_STR_DES = 'Clínica' and OBJ_STR_CAMINHO = 'LABEL_RAIZ');
update SEG_OBJETO set OBJ_INT_ORDEM = 3 where OBJ_STR_DES='Agendamento' and OBJ_INT_CODPAI = (select OBJ_INT_COD from SEG_OBJETO where OBJ_STR_DES = 'Clínica' and OBJ_STR_CAMINHO = 'LABEL_RAIZ');
update SEG_OBJETO set OBJ_INT_ORDEM = 4 where OBJ_STR_DES='Cadastro de Retornos' and OBJ_INT_CODPAI = (select OBJ_INT_COD from SEG_OBJETO where OBJ_STR_DES = 'Clínica' and OBJ_STR_CAMINHO = 'LABEL_RAIZ');
update SEG_OBJETO set OBJ_INT_ORDEM = 5 where OBJ_STR_DES='Relatório de Avaliações dos Atendimentos' and OBJ_INT_CODPAI = (select OBJ_INT_COD from SEG_OBJETO where OBJ_STR_DES = 'Clínica' and OBJ_STR_CAMINHO = 'LABEL_RAIZ');
update SEG_OBJETO set OBJ_INT_ORDEM = 6 where OBJ_STR_DES='Fila de Atendimento' and OBJ_INT_CODPAI = (select OBJ_INT_COD from SEG_OBJETO where OBJ_STR_DES = 'Clínica' and OBJ_STR_CAMINHO = 'LABEL_RAIZ');
update SEG_OBJETO set OBJ_INT_ORDEM = 7 where OBJ_STR_DES='Relatório de Planos de Tratamento' and OBJ_INT_CODPAI = (select OBJ_INT_COD from SEG_OBJETO where OBJ_STR_DES = 'Clínica' and OBJ_STR_CAMINHO = 'LABEL_RAIZ');
update SEG_OBJETO set OBJ_INT_ORDEM = 8 where OBJ_STR_DES='Relatório de Procedimentos' and OBJ_INT_CODPAI = (select OBJ_INT_COD from SEG_OBJETO where OBJ_STR_DES = 'Clínica' and OBJ_STR_CAMINHO = 'LABEL_RAIZ');
update SEG_OBJETO set OBJ_INT_ORDEM = 1 where OBJ_INT_CODPAI = (select OBJ_INT_COD from SEG_OBJETO where OBJ_STR_DES = 'Estoque' and OBJ_STR_CAMINHO = 'LABEL_RAIZ');
update SEG_OBJETO set OBJ_INT_ORDEM = 1 where OBJ_STR_DES='Entrada de Materiais' and OBJ_INT_CODPAI = (select OBJ_INT_COD from SEG_OBJETO where OBJ_STR_DES = 'Estoque' and OBJ_STR_CAMINHO = 'LABEL_RAIZ');
update SEG_OBJETO set OBJ_INT_ORDEM = 2 where OBJ_STR_DES='Reserva de Materiais' and OBJ_INT_CODPAI = (select OBJ_INT_COD from SEG_OBJETO where OBJ_STR_DES = 'Estoque' and OBJ_STR_CAMINHO = 'LABEL_RAIZ');
update SEG_OBJETO set OBJ_INT_ORDEM = 3 where OBJ_STR_DES='Empréstimo de Materiais' and OBJ_INT_CODPAI = (select OBJ_INT_COD from SEG_OBJETO where OBJ_STR_DES = 'Estoque' and OBJ_STR_CAMINHO = 'LABEL_RAIZ');
update SEG_OBJETO set OBJ_INT_ORDEM = 4 where OBJ_STR_DES='Devolução de Materiais' and OBJ_INT_CODPAI = (select OBJ_INT_COD from SEG_OBJETO where OBJ_STR_DES = 'Estoque' and OBJ_STR_CAMINHO = 'LABEL_RAIZ');
update SEG_OBJETO set OBJ_INT_ORDEM = 5 where OBJ_STR_DES='Movimentação de Materiais' and OBJ_INT_CODPAI = (select OBJ_INT_COD from SEG_OBJETO where OBJ_STR_DES = 'Estoque' and OBJ_STR_CAMINHO = 'LABEL_RAIZ');
update SEG_OBJETO set OBJ_INT_ORDEM = 6 where OBJ_STR_DES='Conferência de Estoque' and OBJ_INT_CODPAI = (select OBJ_INT_COD from SEG_OBJETO where OBJ_STR_DES = 'Estoque' and OBJ_STR_CAMINHO = 'LABEL_RAIZ');
update SEG_OBJETO set OBJ_INT_ORDEM = 7 where OBJ_STR_DES='Cadastros' and OBJ_INT_CODPAI = (select OBJ_INT_COD from SEG_OBJETO where OBJ_STR_DES = 'Estoque' and OBJ_STR_CAMINHO = 'LABEL_RAIZ');
update SEG_OBJETO set OBJ_INT_ORDEM = 8 where OBJ_STR_DES='Estoque Mínimo' and OBJ_INT_CODPAI = (select OBJ_INT_COD from SEG_OBJETO where OBJ_STR_DES = 'Estoque' and OBJ_STR_CAMINHO = 'LABEL_RAIZ');
update SEG_OBJETO set OBJ_INT_ORDEM = 9 where OBJ_STR_DES='Relatório de Estoque' and OBJ_INT_CODPAI = (select OBJ_INT_COD from SEG_OBJETO where OBJ_STR_DES = 'Estoque' and OBJ_STR_CAMINHO = 'LABEL_RAIZ');
update SEG_OBJETO set OBJ_CHA_STS = 'I' where OBJ_STR_DES = 'Nota Fiscal';
update SEG_OBJETO set OBJ_CHA_STS = 'I' where OBJ_STR_DES = 'Fragmentada';
update SEG_OBJETO set OBJ_INT_ORDEM = 1 where OBJ_STR_DES='Entrega para Lavagem' and OBJ_INT_CODPAI = (select OBJ_INT_COD from SEG_OBJETO where OBJ_STR_DES = 'Esterilização' and OBJ_STR_CAMINHO = 'LABEL_RAIZ');
update SEG_OBJETO set OBJ_INT_ORDEM = 2 where OBJ_STR_DES='Devolução da Lavagem' and OBJ_INT_CODPAI = (select OBJ_INT_COD from SEG_OBJETO where OBJ_STR_DES = 'Esterilização' and OBJ_STR_CAMINHO = 'LABEL_RAIZ');
update SEG_OBJETO set OBJ_INT_ORDEM = 3 where OBJ_STR_DES='Devolução da Esterilização' and OBJ_INT_CODPAI = (select OBJ_INT_COD from SEG_OBJETO where OBJ_STR_DES = 'Esterilização' and OBJ_STR_CAMINHO = 'LABEL_RAIZ');
update SEG_OBJETO set OBJ_INT_ORDEM = 4 where OBJ_STR_DES='Relatório de Esterilização' and OBJ_INT_CODPAI = (select OBJ_INT_COD from SEG_OBJETO where OBJ_STR_DES = 'Esterilização' and OBJ_STR_CAMINHO = 'LABEL_RAIZ');
update SEG_OBJETO set OBJ_CHA_STS = 'I' where OBJ_STR_DES = 'Bilhetagem';
update SEG_OBJETO set OBJ_INT_ORDEM = 1 where OBJ_STR_DES='Recebimento dos Pacientes' and OBJ_INT_CODPAI = (select OBJ_INT_COD from SEG_OBJETO where OBJ_STR_DES = 'Financeiro' and OBJ_STR_CAMINHO = 'LABEL_RAIZ');
update SEG_OBJETO set OBJ_INT_ORDEM = 2 where OBJ_STR_DES='Registro de Pagamentos/Recebimentos' and OBJ_INT_CODPAI = (select OBJ_INT_COD from SEG_OBJETO where OBJ_STR_DES = 'Financeiro' and OBJ_STR_CAMINHO = 'LABEL_RAIZ');
update SEG_OBJETO set OBJ_INT_ORDEM = 3 where OBJ_STR_DES='Registro de Custos Diretos' and OBJ_INT_CODPAI = (select OBJ_INT_COD from SEG_OBJETO where OBJ_STR_DES = 'Financeiro' and OBJ_STR_CAMINHO = 'LABEL_RAIZ');
update SEG_OBJETO set OBJ_INT_ORDEM = 4 where OBJ_STR_DES='Repasse dos Profissionais' and OBJ_INT_CODPAI = (select OBJ_INT_COD from SEG_OBJETO where OBJ_STR_DES = 'Financeiro' and OBJ_STR_CAMINHO = 'LABEL_RAIZ');
update SEG_OBJETO set OBJ_INT_ORDEM = 5 where OBJ_STR_DES='Cadastro de Tarifas de Cartões' and OBJ_INT_CODPAI = (select OBJ_INT_COD from SEG_OBJETO where OBJ_STR_DES = 'Financeiro' and OBJ_STR_CAMINHO = 'LABEL_RAIZ');
update SEG_OBJETO set OBJ_INT_ORDEM = 6 where OBJ_STR_DES='Relatório de Orçamentos' and OBJ_INT_CODPAI = (select OBJ_INT_COD from SEG_OBJETO where OBJ_STR_DES = 'Financeiro' and OBJ_STR_CAMINHO = 'LABEL_RAIZ');
update SEG_OBJETO set OBJ_INT_ORDEM = 7 where OBJ_STR_DES='Relatório de Recebimentos' and OBJ_INT_CODPAI = (select OBJ_INT_COD from SEG_OBJETO where OBJ_STR_DES = 'Financeiro' and OBJ_STR_CAMINHO = 'LABEL_RAIZ');
update SEG_OBJETO set OBJ_INT_ORDEM = 8 where OBJ_STR_DES='Relatório de Repasses' and OBJ_INT_CODPAI = (select OBJ_INT_COD from SEG_OBJETO where OBJ_STR_DES = 'Financeiro' and OBJ_STR_CAMINHO = 'LABEL_RAIZ');
update SEG_OBJETO set OBJ_INT_ORDEM = 9 where OBJ_STR_DES='Extrato Financeiro' and OBJ_INT_CODPAI = (select OBJ_INT_COD from SEG_OBJETO where OBJ_STR_DES = 'Financeiro' and OBJ_STR_CAMINHO = 'LABEL_RAIZ');
update SEG_OBJETO set OBJ_INT_ORDEM = 1 where OBJ_STR_DES='Cadastro de Especialidades' and OBJ_INT_CODPAI = (select OBJ_INT_COD from SEG_OBJETO where OBJ_STR_DES = 'Financeiro' and OBJ_STR_CAMINHO = 'LABEL_RAIZ');
update SEG_OBJETO set OBJ_INT_ORDEM = 2 where OBJ_STR_DES='Cadastro de Filiais' and OBJ_INT_CODPAI = (select OBJ_INT_COD from SEG_OBJETO where OBJ_STR_DES = 'Financeiro' and OBJ_STR_CAMINHO = 'LABEL_RAIZ');
update SEG_OBJETO set OBJ_INT_ORDEM = 4 where OBJ_STR_DES='Cadastro de Procedimentos' and OBJ_INT_CODPAI = (select OBJ_INT_COD from SEG_OBJETO where OBJ_STR_DES = 'Financeiro' and OBJ_STR_CAMINHO = 'LABEL_RAIZ');
update SEG_OBJETO set OBJ_INT_ORDEM = 5 where OBJ_STR_DES='Cadastro de Convênios' and OBJ_INT_CODPAI = (select OBJ_INT_COD from SEG_OBJETO where OBJ_STR_DES = 'Financeiro' and OBJ_STR_CAMINHO = 'LABEL_RAIZ');
update SEG_OBJETO set OBJ_INT_ORDEM = 6 where OBJ_STR_DES='Associação Convênio-Procedimento' and OBJ_INT_CODPAI = (select OBJ_INT_COD from SEG_OBJETO where OBJ_STR_DES = 'Financeiro' and OBJ_STR_CAMINHO = 'LABEL_RAIZ');
update SEG_OBJETO set OBJ_INT_ORDEM = 7 where OBJ_STR_DES='Cadastro de Profissionais' and OBJ_INT_CODPAI = (select OBJ_INT_COD from SEG_OBJETO where OBJ_STR_DES = 'Financeiro' and OBJ_STR_CAMINHO = 'LABEL_RAIZ');
update SEG_OBJETO set OBJ_INT_ORDEM = 8 where OBJ_STR_DES='Configuração de Documentos' and OBJ_INT_CODPAI = (select OBJ_INT_COD from SEG_OBJETO where OBJ_STR_DES = 'Financeiro' and OBJ_STR_CAMINHO = 'LABEL_RAIZ');
update SEG_OBJETO set OBJ_INT_ORDEM = 1 where OBJ_STR_DES='Cadastro de Especialidades' and OBJ_INT_CODPAI = (select OBJ_INT_COD from SEG_OBJETO where OBJ_STR_DES = 'Administrativo' and OBJ_STR_CAMINHO = 'LABEL_RAIZ');
update SEG_OBJETO set OBJ_INT_ORDEM = 2 where OBJ_STR_DES='Cadastro de Filiais' and OBJ_INT_CODPAI = (select OBJ_INT_COD from SEG_OBJETO where OBJ_STR_DES = 'Administrativo' and OBJ_STR_CAMINHO = 'LABEL_RAIZ');
update SEG_OBJETO set OBJ_INT_ORDEM = 3 where OBJ_STR_DES='Configuração de Anamnese' and OBJ_INT_CODPAI = (select OBJ_INT_COD from SEG_OBJETO where OBJ_STR_DES = 'Administrativo' and OBJ_STR_CAMINHO = 'LABEL_RAIZ');
update SEG_OBJETO set OBJ_INT_ORDEM = 4 where OBJ_STR_DES='Cadastro de Procedimentos' and OBJ_INT_CODPAI = (select OBJ_INT_COD from SEG_OBJETO where OBJ_STR_DES = 'Administrativo' and OBJ_STR_CAMINHO = 'LABEL_RAIZ');
update SEG_OBJETO set OBJ_INT_ORDEM = 5 where OBJ_STR_DES='Cadastro de Convênios' and OBJ_INT_CODPAI = (select OBJ_INT_COD from SEG_OBJETO where OBJ_STR_DES = 'Administrativo' and OBJ_STR_CAMINHO = 'LABEL_RAIZ');
update SEG_OBJETO set OBJ_INT_ORDEM = 6 where OBJ_STR_DES='Associação Convênio-Procedimento' and OBJ_INT_CODPAI = (select OBJ_INT_COD from SEG_OBJETO where OBJ_STR_DES = 'Administrativo' and OBJ_STR_CAMINHO = 'LABEL_RAIZ');
update SEG_OBJETO set OBJ_INT_ORDEM = 7 where OBJ_STR_DES='Cadastro de Profissionais' and OBJ_INT_CODPAI = (select OBJ_INT_COD from SEG_OBJETO where OBJ_STR_DES = 'Administrativo' and OBJ_STR_CAMINHO = 'LABEL_RAIZ');
update SEG_OBJETO set OBJ_INT_ORDEM = 8 where OBJ_STR_DES='Configuração de Documentos' and OBJ_INT_CODPAI = (select OBJ_INT_COD from SEG_OBJETO where OBJ_STR_DES = 'Administrativo' and OBJ_STR_CAMINHO = 'LABEL_RAIZ');
update SEG_OBJETO set OBJ_INT_ORDEM = 1 where OBJ_STR_DES='Receituário' and OBJ_INT_CODPAI = (select OBJ_INT_COD from SEG_OBJETO where OBJ_STR_DES = 'Emissão de Documentos' and OBJ_STR_CAMINHO = 'LABEL_RAIZ');
update SEG_OBJETO set OBJ_INT_ORDEM = 2 where OBJ_STR_DES='Atestado' and OBJ_INT_CODPAI = (select OBJ_INT_COD from SEG_OBJETO where OBJ_STR_DES = 'Emissão de Documentos' and OBJ_STR_CAMINHO = 'LABEL_RAIZ');
update SEG_OBJETO set OBJ_INT_ORDEM = 3 where OBJ_STR_DES='Termo de Consentimento' and OBJ_INT_CODPAI = (select OBJ_INT_COD from SEG_OBJETO where OBJ_STR_DES = 'Emissão de Documentos' and OBJ_STR_CAMINHO = 'LABEL_RAIZ');
update SEG_OBJETO set OBJ_INT_ORDEM = 4 where OBJ_STR_DES='Orçamento' and OBJ_INT_CODPAI = (select OBJ_INT_COD from SEG_OBJETO where OBJ_STR_DES = 'Emissão de Documentos' and OBJ_STR_CAMINHO = 'LABEL_RAIZ');
update SEG_OBJETO set OBJ_INT_ORDEM = 5 where OBJ_STR_DES='Faturamento' and OBJ_INT_CODPAI = (select OBJ_INT_COD from SEG_OBJETO where OBJ_STR_DES = 'Emissão de Documentos' and OBJ_STR_CAMINHO = 'LABEL_RAIZ');
update SEG_OBJETO set OBJ_INT_ORDEM = 6 where OBJ_STR_DES='Pedido de exames' and OBJ_INT_CODPAI = (select OBJ_INT_COD from SEG_OBJETO where OBJ_STR_DES = 'Emissão de Documentos' and OBJ_STR_CAMINHO = 'LABEL_RAIZ');
update SEG_OBJETO set OBJ_INT_ORDEM = 7 where OBJ_STR_DES='Outros Documentos' and OBJ_INT_CODPAI = (select OBJ_INT_COD from SEG_OBJETO where OBJ_STR_DES = 'Emissão de Documentos' and OBJ_STR_CAMINHO = 'LABEL_RAIZ');
update SEG_OBJETO set OBJ_CHA_STS = 'I' where OBJ_STR_CAMINHO = 'relatorioLavagem.jsf';
update SEG_OBJETO set OBJ_CHA_STS = 'I' where OBJ_STR_CAMINHO = 'relatorioPagamento.jsf';
update SEG_OBJETO set OBJ_INT_ORDEM = 1 where OBJ_STR_DES='Item' and OBJ_INT_CODPAI = (select OBJ_INT_COD from SEG_OBJETO where OBJ_STR_DES = 'Cadastros' and OBJ_STR_CAMINHO = 'LABEL_RAIZ' and OBJ_INT_CODPAI is not null);
update SEG_OBJETO set OBJ_INT_ORDEM = 2 where OBJ_STR_DES='Local' and OBJ_INT_CODPAI = (select OBJ_INT_COD from SEG_OBJETO where OBJ_STR_DES = 'Cadastros' and OBJ_STR_CAMINHO = 'LABEL_RAIZ' and OBJ_INT_CODPAI is not null);
update SEG_OBJETO set OBJ_INT_ORDEM = 3 where OBJ_STR_DES='Marca' and OBJ_INT_CODPAI = (select OBJ_INT_COD from SEG_OBJETO where OBJ_STR_DES = 'Cadastros' and OBJ_STR_CAMINHO = 'LABEL_RAIZ' and OBJ_INT_CODPAI is not null);
update SEG_OBJETO set OBJ_INT_ORDEM = 4 where OBJ_STR_DES='Configuração de Kits' and OBJ_INT_CODPAI = (select OBJ_INT_COD from SEG_OBJETO where OBJ_STR_DES = 'Cadastros' and OBJ_STR_CAMINHO = 'LABEL_RAIZ' and OBJ_INT_CODPAI is not null);
update SEG_OBJETO set OBJ_INT_ORDEM = 5 where OBJ_STR_DES='Fornecedor' and OBJ_INT_CODPAI = (select OBJ_INT_COD from SEG_OBJETO where OBJ_STR_DES = 'Cadastros' and OBJ_STR_CAMINHO = 'LABEL_RAIZ' and OBJ_INT_CODPAI is not null);
update SEG_OBJETO set OBJ_STR_DES='Relatório de Lavagem' where OBJ_INT_COD = (select obj_int_cod from SEG_OBJETO where obj_str_caminho='relatorioLavagem.jsf');
update SEG_OBJETO set
OBJ_INT_CODPAI = (select OBJ_INT_COD from SEG_OBJETO where OBJ_STR_DES = 'Esterilização' and OBJ_STR_CAMINHO = 'LABEL_RAIZ')
where obj_int_cod = (select obj_int_cod from SEG_OBJETO where OBJ_STR_DES = 'Relatório de Lavagem');
update SEG_OBJETO set OBJ_STR_DES='Rel Avaliação Atend.' where OBJ_INT_COD = (select OBJ_INT_COD from SEG_OBJETO where OBJ_STR_CAMINHO = 'relatorioAvaliacao.jsf');
update SEG_OBJETO set OBJ_STR_DES='Rel Plano Tratam.' where OBJ_INT_COD = (select OBJ_INT_COD from SEG_OBJETO where OBJ_STR_CAMINHO = 'relatorioPlanoTratamento.jsf');
update SEG_OBJETO set OBJ_STR_DES='Pagamentos/Recebimentos' where OBJ_INT_COD = (select OBJ_INT_COD from SEG_OBJETO where OBJ_STR_CAMINHO = 'contaReceberPagar.jsf');
update SEG_OBJETO set OBJ_STR_DES='Convênio-Procedimento' where OBJ_INT_COD = (select OBJ_INT_COD from SEG_OBJETO where OBJ_STR_CAMINHO = 'convenioProcedimento.jsf');
update SEG_OBJETO set OBJ_STR_DES='Ajuste de Estoque' where OBJ_INT_COD = (select OBJ_INT_COD from SEG_OBJETO where OBJ_STR_DES = 'Conferência de Estoque');
update SEG_OBJETO set OBJ_STR_DES='Ajuste de Materiais' where OBJ_INT_COD = (select OBJ_INT_COD from SEG_OBJETO where OBJ_STR_DES = 'Conferência de Materiais');
update SEG_OBJETO set OBJ_STR_DES='Consulta Ajustes' where OBJ_INT_COD = (select OBJ_INT_COD from SEG_OBJETO where OBJ_STR_DES = 'Consulta Conferências');
update SEG_OBJETO set OBJ_STR_DES='Fornecedor' where OBJ_INT_COD = (select OBJ_INT_COD from SEG_OBJETO where OBJ_STR_CAMINHO = 'fornecedor.jsf');
update SEG_OBJETO set OBJ_INT_CODPAI = 121 where OBJ_INT_COD = (select OBJ_INT_COD from SEG_OBJETO where OBJ_STR_CAMINHO = 'fornecedor.jsf');
update SEG_OBJETO set OBJ_STR_DES='Kits/Procedimentos' where OBJ_INT_COD = (select OBJ_INT_COD from SEG_OBJETO where OBJ_STR_DES = 'Kits-Procedimentos');
update SEG_OBJETO set OBJ_INT_CODPAI = 121 where OBJ_INT_COD = (select OBJ_INT_COD from SEG_OBJETO where OBJ_STR_DES = 'Kits/Procedimentos');
update SEG_OBJETO set OBJ_STR_DES='Cadastro de Tarifas' where OBJ_INT_COD = (select OBJ_INT_COD from SEG_OBJETO where OBJ_STR_DES = 'Cadastro de Tarifas de Cartões');