CREATE TABLE STATUS_AGENDAMENTO (
	ID SERIAL PRIMARY KEY,
	DESCRICAO VARCHAR(200) NOT NULL,
	SIGLA VARCHAR(2) NOT NULL,
	COR VARCHAR(10)	
);

INSERT INTO STATUS_AGENDAMENTO (DESCRICAO, SIGLA, COR) VALUES ('Atendido','A','');
INSERT INTO STATUS_AGENDAMENTO (DESCRICAO, SIGLA, COR) VALUES ('Não Atendido','N','');
INSERT INTO STATUS_AGENDAMENTO (DESCRICAO, SIGLA, COR) VALUES ('Agendado','A','');


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

    private StatusAgendamento(String descricao, String sigla, String styleCss) {
        this.descricao = descricao;
        this.sigla = sigla;
        this.styleCss = styleCss;
    }