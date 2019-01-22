ALTER TABLE profissional ADD COLUMN estado_conselho varchar	(2);

ALTER TABLE paciente ADD COLUMN data_criacao timestamp default current_timestamp;

ALTER TABLE paciente ADD COLUMN legado boolean default false;

ALTER TABLE SEG_EMPRESA ADD COLUMN EMP_INT_CADEIRA INT4;

ALTER TABLE SEG_EMPRESA ADD COLUMN EMP_FLT_IMPOSTO NUMERIC(11,2);

ALTER TABLE SEG_EMPRESA ADD COLUMN EMP_STR_LOGO VARCHAR(100);

update seg_empresa set emp_flt_imposto = 22.5;

update profissional set estado_conselho  = 'PR';

update seg_objeto set obj_str_des = 'Cadastro da Clínica' where obj_str_caminho = 'filial.jsf';

update seg_objeto set obj_str_caminho = 'cadastroempresa.jsf' where obj_str_caminho = 'filial.jsf';

update seg_empresa set emp_cha_tipo = 'J' where emp_int_cod = 41;
