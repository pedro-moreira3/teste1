INSERT INTO especialidade (descricao,range_ini,range_fim,id_empresa,excluido,data_exclusao,excluido_por) VALUES ('GENÉRICAS',0,0,41,'N',null,null);

update pergunta 
set id_especialidade = (select ID from especialidade where descricao = 'GENÉRICAS')
where id_especialidade is null and excluido = 'N';

alter table especialidade add column anamnese bool default true;

update especialidade set anamnese = false where id in (102,100,106,96,98,97,114);
