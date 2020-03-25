package br.com.lume.odonto.managed;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import org.apache.log4j.Logger;
import org.primefaces.PrimeFaces;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.model.chart.PieChartModel;

import br.com.lume.afastamento.AfastamentoSingleton;
import br.com.lume.agendamento.AgendamentoSingleton;
import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.StatusAgendamentoUtil;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.common.util.UtilsPrimefaces;
import br.com.lume.convenio.ConvenioSingleton;
import br.com.lume.dominio.DominioSingleton;
import br.com.lume.odonto.entity.Afastamento;
import br.com.lume.odonto.entity.Agendamento;
import br.com.lume.odonto.entity.Convenio;
import br.com.lume.odonto.entity.Dominio;
import br.com.lume.odonto.entity.Paciente;
import br.com.lume.odonto.entity.Profissional;
import br.com.lume.paciente.PacienteSingleton;
import br.com.lume.profissional.ProfissionalSingleton;

@ManagedBean
@ViewScoped
public class AgendamentoRapidoMB extends LumeManagedBean<Agendamento> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(AgendamentoMB.class);


    // ATRIBUTOS USADOS COMO FILTRO PARA PESQUISA DOS AGENDAMENTOS
    private Profissional filtroPorProfissional;

    private Date data;

    private List<Agendamento> listaAgendamentos;

    public AgendamentoRapidoMB() {
        super(AgendamentoSingleton.getInstance().getBo());   
        this.setClazz(Agendamento.class);
    }

    public void popularLista() {
        try {            

        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
        }
    } 
 
    public List<Profissional> sugestoesProfissionais(String query) {
        return ProfissionalSingleton.getInstance().getBo().listSugestoesCompleteDentista(query, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa(), false);
    }
    
    public void abreNovoAgendamento() {     
        UtilsPrimefaces.readOnlyUIComponent(":lume:pnDlgAgendamento", false);      
    }
    
    public void abreAgendamentoExistente() {       
        UtilsPrimefaces.readOnlyUIComponent(":lume:pnDlgAgendamento", true);      
    }
    
    public String getStatusDescricao(Agendamento agendamento) {
        try {
            return StatusAgendamentoUtil.findBySigla(agendamento.getStatusNovo()).getDescricao();
        } catch (Exception e) {

        }
        return "";
    }
    
    public String[] getHolidays() { 
        String[] xs = new String[] {"'2020-03-22T00:00:00'","'2020-03-24T00:00:00'","'2020-03-25T00:00:00'"};
        return xs;
      }

      public String[] getReportedDays() {
          String[] xs = new String[] {"'2020-03-10T00:00:00'","'2020-03-11T00:00:00'"};
          return xs;
      } 

    public Profissional getFiltroPorProfissional() {
        return filtroPorProfissional;
    }

    public void setFiltroPorProfissional(Profissional filtroPorProfissional) {
        this.filtroPorProfissional = filtroPorProfissional;
    }

    
    public List<Agendamento> getListaAgendamentos() {
        return listaAgendamentos;
    }

    
    public void setListaAgendamentos(List<Agendamento> listaAgendamentos) {
        this.listaAgendamentos = listaAgendamentos;
    }

    
    public Date getData() {
        return data;
    }

    
    public void setData(Date data) {
        this.data = data;
    }
}
