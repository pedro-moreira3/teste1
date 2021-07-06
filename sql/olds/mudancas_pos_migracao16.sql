alter table inteli.seg_empresa add column emp_str_estoque char(1) default 'C';

ALTER TABLE inteli.material ALTER COLUMN consignacao DROP NOT NULL;

ALTER TABLE inteli.material ALTER COLUMN lote DROP NOT NULL;

ALTER TABLE inteli.material ALTER COLUMN id_marca DROP NOT NULL;

ALTER TABLE inteli.material ALTER COLUMN valor DROP NOT NULL;

commit;