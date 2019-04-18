package br.com.lume.odonto.managed;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;

import org.apache.log4j.Logger;
import org.primefaces.context.RequestContext;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.odonto.bo.AgendamentoBO;
import br.com.lume.odonto.entity.Agendamento;
import br.com.lume.odonto.entity.AgendamentoAgenda;

@ManagedBean
@ViewScoped
public class ExportarAgendaMB extends LumeManagedBean<Agendamento> {

    private static final long serialVersionUID = -4445481223043257997L;

    private Logger log = Logger.getLogger(ExportarAgendaMB.class);

    private List<Agendamento> agendamentos;

    private String periodo = "";

    private Date inicio, fim;

    @ManagedProperty(value = "#{agendamentoMB}")
    private AgendamentoMB agendamentoMB;

    public ExportarAgendaMB() {
        super(new AgendamentoBO());
        this.setClazz(Agendamento.class);
    }

    public void carregarAgendamentos() {
        try {
            calcularDatas();

            agendamentos = ((AgendamentoBO) getbO()).listByDataAndProfissional(agendamentoMB.getProfissional(), inicio, fim, null);
            if (RequestContext.getCurrentInstance() != null) {
                if (agendamentos != null && !agendamentos.isEmpty()) {
                    List<AgendamentoAgenda> agendamentosAgenda = new ArrayList<>();
                    for (Agendamento a : agendamentos) {
                        agendamentosAgenda.add(new AgendamentoAgenda(a.getId(), a.getDescricaoAgenda(), a.getDescricao(), a.getEnderecoEmpresa(), a.getInicio(), a.getFim()));
                    }
                    Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").create();
                    //
                    RequestContext.getCurrentInstance().addCallbackParam("agendamentos", gson.toJson(agendamentosAgenda));
                    RequestContext.getCurrentInstance().addCallbackParam("valido", true);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void calcularDatas() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        inicio = cal.getTime();
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        fim = cal.getTime();
        if ("AMANHA".equals(periodo)) {
            cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_MONTH, 1);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            inicio = cal.getTime();
            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
            fim = cal.getTime();
        } else if ("ESTA_SEMANA".equals(periodo)) {
            cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            inicio = cal.getTime();
            cal.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
            fim = cal.getTime();
        } else if ("PROXIMA_SEMANA".equals(periodo)) {
            cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_WEEK, 1);
            cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            inicio = cal.getTime();
            cal.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
            fim = cal.getTime();
        }

    }

    public List<Agendamento> getAgendamentos() {
        return agendamentos;
    }

    public void setAgendamentos(List<Agendamento> agendamentos) {
        this.agendamentos = agendamentos;
    }

    public String getPeriodo() {
        return periodo;
    }

    public void setPeriodo(String periodo) {
        this.periodo = periodo;
    }

    public AgendamentoMB getAgendamentoMB() {
        return agendamentoMB;
    }

    public void setAgendamentoMB(AgendamentoMB agendamentoMB) {
        this.agendamentoMB = agendamentoMB;
    }

}
