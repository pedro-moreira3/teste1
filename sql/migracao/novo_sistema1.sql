
CREATE TABLE STATUS_AGENDAMENTO
(
   id serial PRIMARY KEY NOT NULL, 
	descricao varchar(200),
	sigla varchar(2),
	cor varchar(10)
)

  AFASTAMENTO("Bloqueio", "F", "agendamentoAfastamento"),
    ATENDIDO("Atendido", "A", "agendamentoAtendido"),
    CANCELADO("Cancelado", "C", "agendamentoCancelado"),
    CLIENTE_NA_CLINICA("Cliente na ClÃ­nica", "I", "clienteNaClinica"),
    CONFIRMADO("Confirmado", "S", "agendamentoConfirmado"),
    EM_ATENDIMENTO("Em Atendimento", "O", "emAtendimento"),
    ENCAIXE("Encaixe", "E", "agendamentoEncaixe"),
    FALTA("Falta", "B", "agendamentoFalta"),
    NAO_CONFIRMADO("NÃ£o Confirmado", "N", "agendamentoNaoConfirmado"),
    PRE_AGENDADO("Agendado pelo Paciente", "P", "agendamentoPrecadastro"),
    ERRO_AGENDAMENTO("Erro de Agendamento", "D", "agendamentoErroAgendamento"),
    CONSULTA_INICIAL("Consulta Inicial", "G", "agendamentoConsultaInicial"),
    ENCAIXE_ATENDIDO("Encaixe Atendido", "H", "agendamentoEncaixeAtendido"),
    REMARCADO("Remarcado", "R", "agendamentoRemarcado");