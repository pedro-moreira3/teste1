INSERT INTO SEG_OBJETO(OBJ_INT_CODPAI, OBJ_STR_DES, OBJ_CHA_STS, OBJ_STR_CAMINHO,
                       SIS_INT_COD, OBJ_INT_ORDEM, OBJ_CHA_TIPO, OBJ_STR_ICON)
SELECT
	OBJ_INT_COD, 'Agendamento Rápido',
	'A', 'agendamentoRapido.jsf',
	123, MAX(OBJ_INT_ORDEM) + 1,
	'T', NULL
FROM SEG_OBJETO
WHERE OBJ_STR_DES = 'Clínica'
GROUP BY OBJ_INT_COD, OBJ_INT_ORDEM
LIMIT 1;
INSERT INTO OBJETO_PROFISSIONAL(OBJ_INT_COD, ID_PROFISSIONAL)
SELECT
	(
		SELECT OBJ_INT_COD FROM SEG_OBJETO
		WHERE OBJ_STR_DES = 'Agendamento Rápido'
		LIMIT 1
	),
	OP.ID_PROFISSIONAL
FROM OBJETO_PROFISSIONAL OP
LEFT JOIN SEG_OBJETO O
	ON O.OBJ_INT_COD = OP.OBJ_INT_COD
WHERE O.OBJ_STR_DES = 'Paciente';

alter table horas_uteis_profissional add column HORA_INI_TARDE TIME;
alter table horas_uteis_profissional add column HORA_FIM_TARDE TIME;
alter table convenio_procedimento add column status varchar(1);
update convenio_procedimento set status = 'A' where excluido = 'N';
update convenio_procedimento set status = 'I' where excluido = 'S';