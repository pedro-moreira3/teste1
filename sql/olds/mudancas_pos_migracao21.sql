
--ALTER TABLE "inteli".STATUS_DENTE DROP COLUMN SIGLA;

alter table "inteli".regiao_dente add column faces varchar(100); 

alter table "inteli".status_dente add column tem_face bool default false;  

alter table "inteli".convenio_procedimento add column valor_repasse decimal(11,2);

update status_dente set excluido = 'S' where id = 19;

-- 

INSERT INTO status_dente (descricao,sigla,excluido,label,cor) VALUES ('Implante','IM','N','Implante','#696969');
INSERT INTO status_dente (descricao,sigla,excluido,label,cor) VALUES ('Macha Branca Ativa ','MB','N','Mancha','#FF7256');
INSERT INTO status_dente (descricao,sigla,excluido,label,cor) VALUES ('Mobilidade ','MO','N','Mobil.','#6E8B3D');

UPDATE status_dente SET label='Cáries' WHERE id=11;
UPDATE status_dente SET label='Cálculo' WHERE id=14;
UPDATE status_dente SET label='Rest. A.' WHERE id=12;
UPDATE status_dente SET label='Rest. def.' WHERE id=16;
UPDATE status_dente SET label='Raiz R.' WHERE id=13;
UPDATE status_dente SET label='Prótese' WHERE id=18;
UPDATE status_dente SET label='Corrigir Item' WHERE id=19;
UPDATE status_dente SET label='Rec. G.' WHERE id=15;
UPDATE status_dente SET label='Dente A.' WHERE id=17;



UPDATE status_dente SET tem_face = true WHERE label='Cáries';
UPDATE status_dente SET tem_face = true WHERE label='Rest. A.';
UPDATE status_dente SET tem_face = true WHERE label='Rest. def.';
UPDATE status_dente SET tem_face = true WHERE label='Mancha';

