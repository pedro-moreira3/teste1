package br.com.lume.odonto.managed;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.apache.log4j.Logger;
import org.primefaces.model.chart.PieChartModel;

import br.com.lume.agendamento.AgendamentoSingleton;
import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.StatusAgendamentoUtil;
import br.com.lume.common.util.Utils;
import br.com.lume.configuracao.Configurar;
import br.com.lume.odonto.entity.Agendamento;
import br.com.lume.reserva.ReservaSingleton;

//TODO esse menu é fila de atendimento e não um relatorio,
//necessario mudar o nome dessa classe, verificar permissoes e objetos, e demais relacoes.
@ManagedBean
@ViewScoped
public class RelatorioAtendimentoMB extends LumeManagedBean<Agendamento> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(AgendamentoMB.class);

    private PieChartModel pieModel;

    private List<Agendamento> agendamentos;

    private String filtro = "CURRENT_DATE";

    private Calendar c = Calendar.getInstance();

    private int dia;

    private HashSet<String> profissionaisAgendamento;

    private List<Integer> cadeiras;

    public RelatorioAtendimentoMB() {
        super(AgendamentoSingleton.getInstance().getBo());
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

             
                if (a.getStatus().equals(StatusAgendamentoUtil.CANCELADO.getSigla()) || a.getStatus().equals(StatusAgendamentoUtil.FALTA.getSigla())) {
                    ReservaSingleton.getInstance().getBo().cancelaReservas(a);
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
        if (a.getIniciouAs() == null && a.getChegouAs() != null && (!a.getStatus().equals(StatusAgendamentoUtil.CANCELADO.getSigla()) || !a.getStatus().equals(
                StatusAgendamentoUtil.REMARCADO.getSigla()) || !a.getStatus().equals(StatusAgendamentoUtil.ATENDIDO.getSigla()))) {
            a.setStatusNovo(StatusAgendamentoUtil.CLIENTE_NA_CLINICA.getSigla());
        }
        if (a.getFinalizouAs() == null && a.getIniciouAs() != null && (!a.getStatus().equals(StatusAgendamentoUtil.CANCELADO.getSigla()) || !a.getStatus().equals(
                StatusAgendamentoUtil.REMARCADO.getSigla()) || !a.getStatus().equals(StatusAgendamentoUtil.ATENDIDO.getSigla()))) {
            a.setStatusNovo(StatusAgendamentoUtil.EM_ATENDIMENTO.getSigla());
        }
        if (a.getFinalizouAs() != null) {
            a.setStatusNovo(StatusAgendamentoUtil.ATENDIDO.getSigla());
        }
    }

    public void carregaLista() {
        try {
           
            agendamentos = AgendamentoSingleton.getInstance().getBo().listAgendmantosValidosByDate(filtro);
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
        for (int i = 1; i <= Configurar.getInstance().getConfiguracao().getEmpresaLogada().getEmpIntCadeira(); i++) {
            cadeiras.add(i);
        }
    }

    public String getPacientesAgendamento() {
        if (filtro.equals("CURRENT_DATE")) {
            StringBuilder sb = new StringBuilder();
         
            Long countByAtendidos = AgendamentoSingleton.getInstance().getBo().countByAtendidos();
            sb.append("['Atendidos', " + countByAtendidos + "],");

            Long countByEmAtendimento = AgendamentoSingleton.getInstance().getBo().countByEmAtendimento();
            sb.append("['Em Atendimento', " + countByEmAtendimento + "],");

            Long countByAtrasado = AgendamentoSingleton.getInstance().getBo().countByAtrasado();
            sb.append("['Atrasados', " + countByAtrasado + "],");

            Long countByClienteNaClinica = AgendamentoSingleton.getInstance().getBo().countByClienteNaClinica();
            sb.append("['Na Clínica', " + countByClienteNaClinica + "],");

            Long countByPacienteNaoChegou = AgendamentoSingleton.getInstance().getBo().countByPacienteNaoChegou();
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

}
