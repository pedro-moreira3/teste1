UPDATE SEG_OBJETO SET OBJ_INT_ORDEM = 2 WHERE OBJ_STR_DES = 'Cadastro da Clínica';
UPDATE SEG_OBJETO SET OBJ_INT_ORDEM = 3 WHERE OBJ_STR_DES = 'Cadastro de Especialidades';
UPDATE SEG_OBJETO SET OBJ_INT_ORDEM = 4 WHERE OBJ_STR_DES = 'Cadastro de Procedimentos';
UPDATE SEG_OBJETO SET OBJ_INT_ORDEM = 5 WHERE OBJ_STR_DES = 'Cadastro de Convênios';
UPDATE SEG_OBJETO SET OBJ_INT_ORDEM = 6 WHERE OBJ_STR_DES = 'Convênio-Procedimento';
UPDATE SEG_OBJETO SET OBJ_INT_ORDEM = 7 WHERE OBJ_STR_DES = 'Cadastro de Profissionais';
UPDATE SEG_OBJETO SET OBJ_INT_ORDEM = 8 WHERE OBJ_STR_DES = 'Config. de Anamnese';

UPDATE SEG_OBJETO SET OBJ_INT_CODPAI = (
	SELECT OBJ_INT_COD FROM SEG_OBJETO
	WHERE SEG_OBJETO.OBJ_STR_DES = 'Emissão de Documentos'
) WHERE OBJ_STR_DES = 'Configuração de Documentos';

UPDATE SEG_OBJETO SET OBJ_STR_DES = 'Agenda' WHERE OBJ_STR_DES = 'Agendamento';
UPDATE SEG_OBJETO SET OBJ_STR_DES = 'Devolução Unitária' WHERE OBJ_STR_DES = 'Unitária';
UPDATE SEG_OBJETO SET OBJ_STR_DES = 'Relatório de Agendamento' WHERE OBJ_STR_DES = 'Relatório de Atendimento';

UPDATE SEG_OBJETO SET OBJ_CHA_STS = 'I' WHERE OBJ_STR_DES = 'Rel. Atendimento' AND OBJ_STR_CAMINHO = 'filaAtendimento.jsf';

UPDATE SEG_OBJETO SET OBJ_INT_ORDEM = 2 WHERE OBJ_STR_DES = 'Fila de Atendimento';