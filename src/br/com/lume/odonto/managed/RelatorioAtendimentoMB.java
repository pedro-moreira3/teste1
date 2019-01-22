package br.com.lume.odonto.managed;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.apache.log4j.Logger;
import org.primefaces.model.chart.PieChartModel;

import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.Utils;
import br.com.lume.odonto.bo.AgendamentoBO;
import br.com.lume.odonto.bo.ReservaBO;
import br.com.lume.odonto.entity.Agendamento;
import br.com.lume.odonto.entity.StatusAgendamento;
import br.com.lume.security.bo.EmpresaBO;

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

    private ReservaBO reservaBO = new ReservaBO();

    private List<Integer> cadeiras;

    public RelatorioAtendimentoMB() {
        super(new AgendamentoBO());
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

                AgendamentoBO bo = ((AgendamentoBO) this.getbO());
                if (a.getStatus().equals(StatusAgendamento.CANCELADO.getSigla()) || a.getStatus().equals(StatusAgendamento.FALTA.getSigla())) {
                    reservaBO.cancelaReservas(a);
                }
                validacoes(a);
                bo.persist(a);
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
        if (a.getIniciouAs() == null && a.getChegouAs() != null && (!a.getStatus().equals(StatusAgendamento.CANCELADO.getSigla()) || !a.getStatus().equals(
                StatusAgendamento.REMARCADO.getSigla()) || !a.getStatus().equals(StatusAgendamento.ATENDIDO.getSigla()))) {
            a.setStatus(StatusAgendamento.CLIENTE_NA_CLINICA.getSigla());
        }
        if (a.getFinalizouAs() == null && a.getIniciouAs() != null && (!a.getStatus().equals(StatusAgendamento.CANCELADO.getSigla()) || !a.getStatus().equals(
                StatusAgendamento.REMARCADO.getSigla()) || !a.getStatus().equals(StatusAgendamento.ATENDIDO.getSigla()))) {
            a.setStatus(StatusAgendamento.EM_ATENDIMENTO.getSigla());
        }
        if (a.getFinalizouAs() != null) {
            a.setStatus(StatusAgendamento.ATENDIDO.getSigla());
        }
    }

    public void carregaLista() {
        try {
            AgendamentoBO bo = ((AgendamentoBO) this.getbO());
            agendamentos = bo.listAgendmantosValidosByDate(filtro);
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
        for (int i = 1; i <= EmpresaBO.getEmpresaLogada().getEmpIntCadeira(); i++) {
            cadeiras.add(i);
        }
    }

    public String getPacientesAgendamento() {
        if (filtro.equals("CURRENT_DATE")) {
            StringBuilder sb = new StringBuilder();

            AgendamentoBO bo = ((AgendamentoBO) getbO());
            Long countByAtendidos = bo.countByAtendidos();
            sb.append("['Atendidos', " + countByAtendidos + "],");

            Long countByEmAtendimento = bo.countByEmAtendimento();
            sb.append("['Em Atendimento', " + countByEmAtendimento + "],");

            Long countByAtrasado = bo.countByAtrasado();
            sb.append("['Atrasados', " + countByAtrasado + "],");

            Long countByClienteNaClinica = bo.countByClienteNaClinica();
            sb.append("['Na Clínica', " + countByClienteNaClinica + "],");

            Long countByPacienteNaoChegou = bo.countByPacienteNaoChegou();
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
