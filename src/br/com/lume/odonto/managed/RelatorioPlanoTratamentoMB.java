package br.com.lume.odonto.managed;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ActionEvent;

import org.apache.log4j.Logger;

import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.odonto.entity.PlanoTratamento;
import br.com.lume.odonto.util.OdontoMensagens;
import br.com.lume.planoTratamento.PlanoTratamentoSingleton;

@ManagedBean
@ViewScoped
public class RelatorioPlanoTratamentoMB extends LumeManagedBean<PlanoTratamento> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(RelatorioPlanoTratamentoMB.class);

    private List<PlanoTratamento> planoTratamentos = new ArrayList<>();

    private Date inicio, fim;

    private String filtroPeriodo = "Q";

    private String status = "N";

    public RelatorioPlanoTratamentoMB() {
        super(PlanoTratamentoSingleton.getInstance().getBo());      
        this.setClazz(PlanoTratamento.class);
        actionTrocaDatas();
    }

    public void actionFiltrar(ActionEvent event) {
        if (inicio != null && fim != null && inicio.getTime() > fim.getTime()) {
            this.addError(OdontoMensagens.getMensagem("afastamento.dtFim.menor.dtInicio"), "");
        } else {
            planoTratamentos = PlanoTratamentoSingleton.getInstance().getBo().listPTByDateAndStatus(inicio, fim, status);
        }
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

    @Override
    public void actionNew(ActionEvent arg0) {
        inicio = null;
        fim = null;
        super.actionNew(arg0);
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

    public List<PlanoTratamento> getPlanoTratamentos() {
        return planoTratamentos;
    }

    public void setPlanoTratamentos(List<PlanoTratamento> planoTratamentos) {
        this.planoTratamentos = planoTratamentos;
    }

    public String getFiltroPeriodo() {
        return filtroPeriodo;
    }

    public void setFiltroPeriodo(String filtroPeriodo) {
        this.filtroPeriodo = filtroPeriodo;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}
