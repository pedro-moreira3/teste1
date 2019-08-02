package br.com.lume.odonto.managed;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.apache.commons.lang3.time.DateUtils;
import org.apache.log4j.Logger;
import org.primefaces.model.chart.PieChartModel;

import br.com.lume.agendamento.AgendamentoSingleton;
import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.StatusAgendamentoUtil;
import br.com.lume.common.util.Utils;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.odonto.entity.Agendamento;
import br.com.lume.reserva.ReservaSingleton;

// TODO esse menu é fila de atendimento e não um relatorio,
// necessario mudar o nome dessa classe, verificar permissoes e objetos, e demais relacoes.
@ManagedBean
@ViewScoped
public class FilaAtendimentoMB extends LumeManagedBean<Agendamento> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(AgendamentoMB.class);

    private PieChartModel pieModel;

    private List<Agendamento> agendamentos;

    private String filtro = "CURRENT_DATE";
    private Date dateFilter;
    private SimpleDateFormat dateFilterFormat = new SimpleDateFormat("yyyy-MM-dd");

    private Calendar c = Calendar.getInstance();

    private int dia;

    private HashSet<String> profissionaisAgendamento;

    private List<Integer> cadeiras;

    public FilaAtendimentoMB() {
        super(AgendamentoSingleton.getInstance().getBo());
        this.dateFilter = new Date();
        pieModel = new PieChartModel();
        this.setClazz(Agendamento.class);
        this.carregaLista();
        dia = c.get(Calendar.DAY_OF_WEEK);
    }

    public void persistAgendamento(Agendamento a) {
        try {
            if (a.getInicio().before(a.getFim())) {

                a.setChegouAs(Utils.getDataAtual(a.getChegouAs()));
                a.setIniciouAs(Utils.getDataAtual(a.getIniciouAs()));
                a.setFinalizouAs(Utils.getDataAtual(a.getFinalizouAs()));

                if (a.getStatusNovo().equals(StatusAgendamentoUtil.CANCELADO.getSigla()) || a.getStatusNovo().equals(StatusAgendamentoUtil.FALTA.getSigla())) {
                    ReservaSingleton.getInstance().getBo().cancelaReservas(a, UtilsFrontEnd.getProfissionalLogado());
                }
                validacoes(a);
                AgendamentoSingleton.getInstance().getBo().persist(a);
                this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");
            } else {
                getbO().refresh(a);
            }
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
            log.error(Mensagens.ERRO_AO_SALVAR_REGISTRO, e);
        }
    }

    public void validacoes(Agendamento a) {
        if (a.getIniciouAs() == null && a.getChegouAs() != null && (!a.getStatusNovo().equals(StatusAgendamentoUtil.CANCELADO.getSigla()) || !a.getStatusNovo().equals(
                StatusAgendamentoUtil.REMARCADO.getSigla()) || !a.getStatusNovo().equals(StatusAgendamentoUtil.ATENDIDO.getSigla()))) {
            a.setStatusNovo(StatusAgendamentoUtil.CLIENTE_NA_CLINICA.getSigla());
        }
        if (a.getFinalizouAs() == null && a.getIniciouAs() != null && (!a.getStatusNovo().equals(StatusAgendamentoUtil.CANCELADO.getSigla()) || !a.getStatusNovo().equals(
                StatusAgendamentoUtil.REMARCADO.getSigla()) || !a.getStatusNovo().equals(StatusAgendamentoUtil.ATENDIDO.getSigla()))) {
            a.setStatusNovo(StatusAgendamentoUtil.EM_ATENDIMENTO.getSigla());
        }
        if (a.getFinalizouAs() != null) {
            a.setStatusNovo(StatusAgendamentoUtil.ATENDIDO.getSigla());
        }
    }

    public void carregaLista() {
        try {
            filtro = "'" + this.dateFilterFormat.format((dateFilter == null ? new Date() : dateFilter)) + "'";
            agendamentos = AgendamentoSingleton.getInstance().getBo().listAgendmantosValidosByDate(filtro, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
            carregarCadeiras();
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
        }
    }

    public boolean isLiberaEdicao() {
        return isAdmin() || isSecretaria();
    }

    private void carregarCadeiras() {
        cadeiras = new ArrayList<>();
        for (int i = 1; i <= UtilsFrontEnd.getEmpresaLogada().getEmpIntCadeira(); i++) {
            cadeiras.add(i);
        }
    }

    public String getPacientesAgendamento() {
        if (filtro.equals("CURRENT_DATE")) {

            StringBuilder sb = new StringBuilder();
            long idEmpresaLogada = UtilsFrontEnd.getProfissionalLogado().getIdEmpresa();

            Long countByAtendidos = AgendamentoSingleton.getInstance().getBo().countByAtendidos(idEmpresaLogada);
            sb.append("['Atendidos', " + countByAtendidos + "],");

            Long countByEmAtendimento = AgendamentoSingleton.getInstance().getBo().countByEmAtendimento(idEmpresaLogada);
            sb.append("['Em Atendimento', " + countByEmAtendimento + "],");

            Long countByAtrasado = AgendamentoSingleton.getInstance().getBo().countByAtrasado(idEmpresaLogada);
            sb.append("['Atrasados', " + countByAtrasado + "],");

            Long countByClienteNaClinica = AgendamentoSingleton.getInstance().getBo().countByClienteNaClinica(idEmpresaLogada);
            sb.append("['Na Clínica', " + countByClienteNaClinica + "],");

            Long countByPacienteNaoChegou = AgendamentoSingleton.getInstance().getBo().countByPacienteNaoChegou(idEmpresaLogada);
            sb.append("['Consultas restantes', " + countByPacienteNaoChegou + "],");
            return sb.toString();
        } else {
            return "['','']";
        }

    }

    public String getAtendimentosChart() {
        StringBuilder sb = new StringBuilder();
        profissionaisAgendamento = new HashSet<>();
        if (agendamentos != null && !agendamentos.isEmpty()) {
            for (Agendamento agendamento : agendamentos) {
                Calendar c = Calendar.getInstance();
                c.setTime(agendamento.getInicio());
                String hora1 = c.get(Calendar.HOUR_OF_DAY) + "";
                hora1 += "," + c.get(Calendar.MINUTE);

                c.setTime(agendamento.getFim());
                String hora2 = c.get(Calendar.HOUR_OF_DAY) + "";
                hora2 += "," + c.get(Calendar.MINUTE);
                sb.append(
                        "[ '" + agendamento.getProfissional().getDadosBasico().getNome() + "', '" + agendamento.getPaciente().getDadosBasico().getNome() + "', new Date(0,0,0," + hora1 + ",0), new Date(0,0,0," + hora2 + ",0) ],");
                profissionaisAgendamento.add(agendamento.getProfissional().getDadosBasico().getNome());
            }
            return sb.toString();
        }
        return "[]";
    }

    public PieChartModel getPieModel() {
        return pieModel;
    }

    public void setPieModel(PieChartModel pieModel) {
        this.pieModel = pieModel;
    }

    public List<Agendamento> getAgendamentos() {
        return agendamentos;
    }

    public void setAgendamentos(List<Agendamento> agendamentos) {
        this.agendamentos = agendamentos;
    }

    public String getFiltro() {
        return filtro;
    }

    public void setFiltro(String filtro) {
        this.filtro = filtro;
    }

    public int getDia() {
        return dia;
    }

    public void setDia(int dia) {
        this.dia = dia;
    }

    public int getTotalProfissionaisAgendamento() {
        return profissionaisAgendamento != null ? profissionaisAgendamento.toArray().length : 0;
    }

    public List<Integer> getCadeiras() {
        return cadeiras;
    }

    public void setCadeiras(List<Integer> cadeiras) {
        this.cadeiras = cadeiras;
    }

    public Date getDateFilter() {
        return this.dateFilter;
    }

    public void setDateFilter(Date dateFilter) {
        this.dateFilter = dateFilter;
    }

    public boolean isFiltroTomorrow() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 1);
        return DateUtils.isSameDay(getDateFilter(), cal.getTime());
    }

    public StatusAgendamentoUtil[] getStatusAgendamentoUtil() {
        List<StatusAgendamentoUtil> status = Arrays.asList(StatusAgendamentoUtil.values());
        List<StatusAgendamentoUtil> result = new ArrayList<>();
        for(StatusAgendamentoUtil statusUtil: status)
            if(!"P".equals(statusUtil.getSigla()) && !"F".equals(statusUtil.getSigla()))
                result.add(statusUtil);
        return  result.toArray(new StatusAgendamentoUtil[result.size()]);
    }

}
