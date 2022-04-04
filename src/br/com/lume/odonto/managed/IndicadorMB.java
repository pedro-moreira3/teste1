package br.com.lume.odonto.managed;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.faces.view.ViewScoped;
import javax.inject.Named;

import org.apache.log4j.Logger;

import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.indicador.bo.IndicadorBO;
import br.com.lume.odonto.entity.Indicador;

@Named
@ViewScoped
public class IndicadorMB extends LumeManagedBean<Indicador> implements Serializable {

    private static final long serialVersionUID = 1L;
    private static Logger log = Logger.getLogger(IndicadorMB.class);

    private String filtroPeriodo;
    private Date inicio, fim;
    private String tabela;
    private List<Indicador> indicadoresAgendamento, indicadoresFinanceiro, indicadoresOutros;
    

    public IndicadorMB() {
        super(new IndicadorBO());
        this.setClazz(Indicador.class);

    }

    public void actionTrocaDatasCriacao() {
        try {
            setInicio(getDataInicio(filtroPeriodo));
            setFim(getDataFim(filtroPeriodo));
        } catch (Exception e) {
            log.error("Erro no actionTrocaDatasCriacao", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
        }
    }
    
    public Date getDataFim(String filtro) {
        Date dataFim = null;
        try {
            Calendar c = Calendar.getInstance();
            if ("O".equals(filtro)) {
                c.add(Calendar.DAY_OF_MONTH, -1);
                dataFim = c.getTime();
            } else if (filtro == null) {
                dataFim = null;
            } else {
                dataFim = c.getTime();
            }
            return dataFim;
        } catch (Exception e) {
            log.error("Erro no getDataFim", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            return null;
        }
    }

    public Date getDataInicio(String filtro) {
        Date dataInicio = null;
        try {
            Calendar c = Calendar.getInstance();
            if ("S".equals(filtro)) { //Últimos 7 dias              
                c.add(Calendar.DAY_OF_MONTH, -7);
                dataInicio = c.getTime();
            } else if ("Q".equals(filtro)) { //Últimos 15 dias              
                c.add(Calendar.DAY_OF_MONTH, -15);
                dataInicio = c.getTime();
            } else if ("T".equals(filtro)) { //Últimos 30 dias                
                c.add(Calendar.DAY_OF_MONTH, -30);
                dataInicio = c.getTime();
            } else if ("I".equals(filtro)) { //Mês Atual             
                c.add(Calendar.MONTH, -6);
                dataInicio = c.getTime();
            }
            return dataInicio;
        } catch (Exception e) {
            log.error("Erro no getDataInicio", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            return null;
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

    public String getTabela() {
        return tabela;
    }

    public void setTabela(String tabela) {
        this.tabela = tabela;
    }

    
    public String getFiltroPeriodo() {
        return filtroPeriodo;
    }

    
    public void setFiltroPeriodo(String filtroPeriodo) {
        this.filtroPeriodo = filtroPeriodo;
    }

    
    public List<Indicador> getIndicadoresAgendamento() {
        return indicadoresAgendamento;
    }

    
    public void setIndicadoresAgendamento(List<Indicador> indicadoresAgendamento) {
        this.indicadoresAgendamento = indicadoresAgendamento;
    }

    
    public List<Indicador> getIndicadoresFinanceiro() {
        return indicadoresFinanceiro;
    }

    
    public void setIndicadoresFinanceiro(List<Indicador> indicadoresFinanceiro) {
        this.indicadoresFinanceiro = indicadoresFinanceiro;
    }

    
    public List<Indicador> getIndicadoresOutros() {
        return indicadoresOutros;
    }

    
    public void setIndicadoresOutros(List<Indicador> indicadoresOutros) {
        this.indicadoresOutros = indicadoresOutros;
    }

}
