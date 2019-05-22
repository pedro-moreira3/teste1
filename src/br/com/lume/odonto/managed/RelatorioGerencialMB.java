package br.com.lume.odonto.managed;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ActionEvent;

import org.apache.log4j.Logger;

import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.StatusAgendamentoUtil;
import br.com.lume.configuracao.Configurar;
import br.com.lume.odonto.entity.KeyValue;
import br.com.lume.odonto.entity.Paciente;
import br.com.lume.odonto.entity.StatusAgendamento;
import br.com.lume.relatorioGerencial.RelatorioGerencialSingleton;
import br.com.lume.security.entity.Empresa;

@ManagedBean
@ViewScoped
public class RelatorioGerencialMB extends LumeManagedBean<Paciente> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(RelatorioGerencialMB.class);

    private Date inicio, fim;

    private String filtroPeriodo = "S";

    private BigDecimal recebimentoPaciente, recebimento, pagamentosConsultorio, saldoDoDia;

    private Long pacientesAgendados, pacientesAtendidos, pacientesNaoVieram, pacientesRemarcaram, pacientesNovos, pacientesAntigos, pacientesConvenio, pacientesSemConvenio, pacientesCancelados,
            pacientesAtivos;

    private List<Paciente> pacientesInativosDetalhe;

    private List<Object[]> orcamentosAprovados;

    private Double minutosAgendados, minutosUtilizados, minutosOciosos;

    private Integer taxaDeOcupacao, pacientesInativos;

    private List<KeyValue> profissionaisMaisRentaveis;

    private List<KeyValue> agendamentosHorario;

    public RelatorioGerencialMB() {
        super(RelatorioGerencialSingleton.getInstance().getBo());
        this.setClazz(Paciente.class);
        actionTrocaDatas();
    }

    public void actionTrocaDatas() {
        try {
            Calendar c = Calendar.getInstance();

            if ("O".equals(filtroPeriodo)) {
                c.add(Calendar.DAY_OF_MONTH, -1);
                inicio = c.getTime();
                fim = c.getTime();
            } else if ("H".equals(filtroPeriodo)) { //Hoje
                inicio = c.getTime();
                fim = c.getTime();
            } else if ("S".equals(filtroPeriodo)) { //Últimos 7 dias
                fim = c.getTime();
                c.add(Calendar.DAY_OF_MONTH, -7);
                inicio = c.getTime();
            } else if ("Q".equals(filtroPeriodo)) { //Últimos 15 dias
                fim = c.getTime();
                c.add(Calendar.DAY_OF_MONTH, -15);
                inicio = c.getTime();
            } else if ("T".equals(filtroPeriodo)) { //Últimos 30 dias
                fim = c.getTime();
                c.add(Calendar.DAY_OF_MONTH, -30);
                inicio = c.getTime();
            } else if ("M".equals(filtroPeriodo)) { //Mês Atual
                fim = c.getTime();
                c.set(Calendar.DAY_OF_MONTH, 1);
                inicio = c.getTime();
            } else if ("I".equals(filtroPeriodo)) { //Mês Atual
                fim = c.getTime();
                c.add(Calendar.MONTH, -6);
                inicio = c.getTime();
            }
            actionFiltrar(null);
        } catch (Exception e) {
            log.error("Erro no actionTrocaDatas", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
        }
    }

    public String getObjectArrayToString(List<Object[]> lista) {
        StringBuilder sb = new StringBuilder();
        if (lista != null && !lista.isEmpty()) {
            for (Object[] o : lista) {
                sb.append("[");
                sb.append("'");
                sb.append(o[0]);
                sb.append("'");
                sb.append(",");
                sb.append(o[1]);
                sb.append("]");
                sb.append(",");
            }
        } else {
            sb.append("['',0]");
        }

        return sb.toString();
    }

    public String getKeyValueToString(List<KeyValue> lista) {
        StringBuilder sb = new StringBuilder();
        if (lista != null && !lista.isEmpty()) {
            for (KeyValue kv : lista) {
                sb.append("[");
                sb.append("'");
                sb.append(kv.getKey());
                sb.append("'");
                sb.append(",");
                sb.append(kv.getValue());
                sb.append("]");
                sb.append(",");
            }
        } else {
            sb.append("['',0]");
        }

        return sb.toString();
    }

    public String getProfissionaisMaisRentaveisStr() {
        return getKeyValueToString(profissionaisMaisRentaveis);
    }

    public String getAgendamentosHorarioStr() {
        return getKeyValueToString(agendamentosHorario);
    }

    public void actionFiltrar(ActionEvent event) {
        try {
            Empresa empresa = Configurar.getInstance().getConfiguracao().getEmpresaLogada();
           // RelatorioGerencialBO bo = ((RelatorioGerencialBO) getbO());
            recebimento = RelatorioGerencialSingleton.getInstance().getBo().findRecebimento(inicio, fim, empresa);
            pagamentosConsultorio = RelatorioGerencialSingleton.getInstance().getBo().findPagamentosConsultorio(inicio, fim, empresa);
            pagamentosConsultorio = pagamentosConsultorio.abs();
            saldoDoDia = recebimento.subtract(getPagamentosConsultorio());
            pacientesAtendidos = RelatorioGerencialSingleton.getInstance().getBo().findTotalAgendamentoStatus(inicio, fim, StatusAgendamentoUtil.ATENDIDO.getSigla(), empresa);
            pacientesCancelados = RelatorioGerencialSingleton.getInstance().getBo().findTotalAgendamentoStatus(inicio, fim, StatusAgendamentoUtil.CANCELADO.getSigla(), empresa);
            pacientesNaoVieram = RelatorioGerencialSingleton.getInstance().getBo().findTotalAgendamentoStatus(inicio, fim, StatusAgendamentoUtil.FALTA.getSigla(), empresa);
            pacientesRemarcaram = RelatorioGerencialSingleton.getInstance().getBo().findTotalAgendamentoStatus(inicio, fim, StatusAgendamentoUtil.REMARCADO.getSigla(), empresa);
            pacientesAgendados = pacientesAtendidos + pacientesNaoVieram + pacientesRemarcaram + pacientesCancelados;
            pacientesAtivos = RelatorioGerencialSingleton.getInstance().getBo().findPacientesAtivos(inicio, fim, empresa);
            pacientesInativosDetalhe = RelatorioGerencialSingleton.getInstance().getBo().listPacientesInativos(inicio, fim, empresa);
            pacientesInativos = pacientesInativosDetalhe.size();
            pacientesConvenio = RelatorioGerencialSingleton.getInstance().getBo().findTotalAgendamentoConvenioOuSem(inicio, fim, true, empresa);
            pacientesSemConvenio = RelatorioGerencialSingleton.getInstance().getBo().findTotalAgendamentoConvenioOuSem(inicio, fim, false, empresa);
            minutosAgendados = RelatorioGerencialSingleton.getInstance().getBo().findTotalMinutosAgendamento(inicio, fim, false, empresa);
            minutosUtilizados = RelatorioGerencialSingleton.getInstance().getBo().findTotalMinutosAgendamento(inicio, fim, true, empresa);
            orcamentosAprovados = RelatorioGerencialSingleton.getInstance().getBo().listStatutsPTP(inicio, fim, empresa);
            profissionaisMaisRentaveis = RelatorioGerencialSingleton.getInstance().getBo().findProfissionaisMaisRentaveis(inicio, fim, empresa);
            agendamentosHorario = RelatorioGerencialSingleton.getInstance().getBo().findAgendamentosHorario(inicio, fim, empresa);
            minutosOciosos = minutosAgendados - minutosUtilizados;
            taxaDeOcupacao = (int) (minutosUtilizados / 60) * 100 / Configurar.getInstance().getConfiguracao().getEmpresaLogada().getCapacidadeInstalada();
        } catch (Exception e) {
            log.error("Erro no actionFiltrar", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
        }
    }

    public Date getInicio() {
        return inicio;
    }

    public void setInicio(Date inicio) {
        this.inicio = inicio;
    }

    public Date getFim() {
        return fim;
    }

    public void setFim(Date fim) {
        this.fim = fim;
    }

    public String getFiltroPeriodo() {
        return filtroPeriodo;
    }

    public void setFiltroPeriodo(String filtroPeriodo) {
        this.filtroPeriodo = filtroPeriodo;
    }

    public BigDecimal getRecebimentoPaciente() {
        return recebimentoPaciente;
    }

    public void setRecebimentoPaciente(BigDecimal recebimentoPaciente) {
        this.recebimentoPaciente = recebimentoPaciente;
    }

    public BigDecimal getPagamentosConsultorio() {
        return pagamentosConsultorio;
    }

    public void setPagamentosConsultorio(BigDecimal pagamentosConsultorio) {
        this.pagamentosConsultorio = pagamentosConsultorio;
    }

    public BigDecimal getSaldoDoDia() {
        return saldoDoDia;
    }

    public void setSaldoDoDia(BigDecimal saldoDoDia) {
        this.saldoDoDia = saldoDoDia;
    }

    public Long getPacientesAtendidos() {
        return pacientesAtendidos;
    }

    public void setPacientesAtendidos(Long pacientesAtendidos) {
        this.pacientesAtendidos = pacientesAtendidos;
    }

    public Long getPacientesNaoVieram() {
        return pacientesNaoVieram;
    }

    public void setPacientesNaoVieram(Long pacientesNaoVieram) {
        this.pacientesNaoVieram = pacientesNaoVieram;
    }

    public Long getPacientesRemarcaram() {
        return pacientesRemarcaram;
    }

    public void setPacientesRemarcaram(Long pacientesRemarcaram) {
        this.pacientesRemarcaram = pacientesRemarcaram;
    }

    public Long getPacientesNovos() {
        return pacientesNovos;
    }

    public void setPacientesNovos(Long pacientesNovos) {
        this.pacientesNovos = pacientesNovos;
    }

    public Long getPacientesAntigos() {
        return pacientesAntigos;
    }

    public void setPacientesAntigos(Long pacientesAntigos) {
        this.pacientesAntigos = pacientesAntigos;
    }

    public Long getPacientesConvenio() {
        return pacientesConvenio;
    }

    public void setPacientesConvenio(Long pacientesConvenio) {
        this.pacientesConvenio = pacientesConvenio;
    }

    public Long getPacientesSemConvenio() {
        return pacientesSemConvenio;
    }

    public void setPacientesSemConvenio(Long pacientesSemConvenio) {
        this.pacientesSemConvenio = pacientesSemConvenio;
    }

    public Double getMinutosAgendados() {
        return minutosAgendados;
    }

    public void setMinutosAgendados(Double minutosAgendados) {
        this.minutosAgendados = minutosAgendados;
    }

    public Double getMinutosUtilizados() {
        return minutosUtilizados;
    }

    public void setMinutosUtilizados(Double minutosUtilizados) {
        this.minutosUtilizados = minutosUtilizados;
    }

    public Double getMinutosOciosos() {
        return minutosOciosos;
    }

    public void setMinutosOciosos(Double minutosOciosos) {
        this.minutosOciosos = minutosOciosos;
    }

    public BigDecimal getRecebimento() {
        return recebimento;
    }

    public void setRecebimento(BigDecimal recebimento) {
        this.recebimento = recebimento;
    }

    public Long getPacientesAgendados() {
        return pacientesAgendados;
    }

    public void setPacientesAgendados(Long pacientesAgendados) {
        this.pacientesAgendados = pacientesAgendados;
    }

    public Long getPacientesCancelados() {
        return pacientesCancelados;
    }

    public void setPacientesCancelados(Long pacientesCancelados) {
        this.pacientesCancelados = pacientesCancelados;
    }

    public Integer getTaxaDeOcupacao() {
        return taxaDeOcupacao;
    }

    public void setTaxaDeOcupacao(Integer taxaDeOcupacao) {
        this.taxaDeOcupacao = taxaDeOcupacao;
    }

    public String getOrcamentosAprovadosStr() {
        return getObjectArrayToString(orcamentosAprovados);
    }

    public List<Object[]> getOrcamentosAprovados() {
        return orcamentosAprovados;
    }

    public void setOrcamentosAprovados(List<Object[]> orcamentosAprovados) {
        this.orcamentosAprovados = orcamentosAprovados;
    }

    public Long getPacientesAtivos() {
        return pacientesAtivos;
    }

    public void setPacientesAtivos(Long pacientesAtivos) {
        this.pacientesAtivos = pacientesAtivos;
    }

    public List<KeyValue> getProfissionaisMaisRentaveis() {
        return profissionaisMaisRentaveis;
    }

    public void setProfissionaisMaisRentaveis(List<KeyValue> profissionaisMaisRentaveis) {
        this.profissionaisMaisRentaveis = profissionaisMaisRentaveis;
    }

    public List<Paciente> getPacientesInativosDetalhe() {
        return pacientesInativosDetalhe;
    }

    public void setPacientesInativosDetalhe(List<Paciente> pacientesInativosDetalhe) {
        this.pacientesInativosDetalhe = pacientesInativosDetalhe;
    }

    public Integer getPacientesInativos() {
        return pacientesInativos;
    }

    public void setPacientesInativos(Integer pacientesInativos) {
        this.pacientesInativos = pacientesInativos;
    }

}
