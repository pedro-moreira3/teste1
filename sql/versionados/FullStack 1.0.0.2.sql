INSERT INTO SEG_OBJETO(OBJ_INT_CODPAI, OBJ_STR_DES, OBJ_CHA_STS, OBJ_STR_CAMINHO,
                       SIS_INT_COD, OBJ_INT_ORDEM, OBJ_CHA_TIPO, OBJ_STR_ICON)
SELECT
	OBJ_INT_COD, 'Gerenciamento Iugu',
	'A', 'gerenciamentoIugu.xhtml',
	123, MAX(OBJ_INT_ORDEM) + 1,
	'T', NULL
FROM SEG_OBJETO
WHERE OBJ_STR_DES = 'Plano'
GROUP BY OBJ_INT_COD, OBJ_INT_ORDEM
LIMIT 1;

insert into seg_perobjeto (obj_int_cod,per_int_cod)  
SELECT
	(
		SELECT OBJ_INT_COD FROM SEG_OBJETO
		WHERE OBJ_STR_DES = 'Gerenciamento Iugu'
		LIMIT 1
	), 286
	
	
update SEG_OBJETO set obj_int_codpai = 1
		WHERE OBJ_STR_DES = 'Gerenciamento Iugu'		


update SEG_OBJETO set obj_str_caminho = 'gerenciamentoIugu.jsf' WHERE OBJ_STR_DES = 'Gerenciamento Iugu'	

insert into plano (nome,nome_iugu,consultas,preco) values
('Plano Promocional (Comissão)','intelidente_plano_promocional',240,87) 

---acima ja executado---

--TODO inserir o id do plano nos clientes do alvaro.
