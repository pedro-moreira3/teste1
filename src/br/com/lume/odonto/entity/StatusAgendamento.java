package br.com.lume.odonto.entity;

public enum StatusAgendamento {

    AFASTAMENTO("Bloqueio", "F", "agendamentoAfastamento"),
    ATENDIDO("Atendido", "A", "agendamentoAtendido"),
    CANCELADO("Cancelado", "C", "agendamentoCancelado"),
    CLIENTE_NA_CLINICA("Cliente na Clínica", "I", "clienteNaClinica"),
    CONFIRMADO("Confirmado", "S", "agendamentoConfirmado"),
    EM_ATENDIMENTO("Em Atendimento", "O", "emAtendimento"),
    ENCAIXE("Encaixe", "E", "agendamentoEncaixe"),
    FALTA("Falta", "B", "agendamentoFalta"),
    NAO_CONFIRMADO("Não Confirmado", "N", "agendamentoNaoConfirmado"),
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

    private String descricao;

    private String sigla;

    private String styleCss;

    public String getDescricao() {
        return descricao;
    }

    public String getSigla() {
        return sigla;
    }

    public String getStyleCss() {
        return styleCss;
    }

    public static StatusAgendamento findBySigla(String sigla) {
        StatusAgendamento[] values = StatusAgendamento.values();
        for (StatusAgendamento statusAgendamento : values) {
            if (statusAgendamento.getSigla().equals(sigla)) {
                return statusAgendamento;
            }
        }
        return null;
    }
}
