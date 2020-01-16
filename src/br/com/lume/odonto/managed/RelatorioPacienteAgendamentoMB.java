package br.com.lume.odonto.managed;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ActionEvent;

import org.apache.log4j.Logger;
import org.primefaces.component.datatable.DataTable;

import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.StatusAgendamentoUtil;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.convenio.ConvenioSingleton;
import br.com.lume.odonto.entity.Agendamento;
import br.com.lume.odonto.entity.Convenio;
import br.com.lume.odonto.entity.Paciente;
import br.com.lume.odonto.util.OdontoMensagens;
import br.com.lume.paciente.PacienteSingleton;

@ManagedBean
@ViewScoped
public class RelatorioPacienteAgendamentoMB extends LumeManagedBean<Paciente> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(RelatorioPacienteAgendamentoMB.class);

    private List<Paciente> pacientes = new ArrayList<>();

    private Date inicio, fim;

    private Paciente paciente;   
    
    private Convenio convenio;
    
    private String filtroPeriodo;
    
    private List<String> listaConvenios;
    
    private String filtroPorConvenio;
    
    private String filtroStatusPaciente;

    //EXPORTAÇÃO TABELA
    private DataTable tabelaRelatorio;
    
    private List<String> filtroAtendimento = new ArrayList<String>();
 
    private List<String> filtroStatusAgendamento; 
    
    public static final String SEM_ULTIMO_AGENDAMENTO = "SUA";
        
    public static final String SEM_AGENDAMENTO_FUTURO = "SAF";
        
    public static final String SEM_RETORNO_FUTURO = "SRR";
    
    private boolean checkFiltro = false;
    
    private boolean desabilitaStatusAgendamento = false;
   
    
    public RelatorioPacienteAgendamentoMB() {
        super(PacienteSingleton.getInstance().getBo());      
        this.setClazz(Paciente.class);
        
        if(this.listaConvenios == null)
            this.listaConvenios = new ArrayList<>();        
        this.sugestoesConvenios("todos");      
    }
    
    public List<Paciente> sugestoesPacientes(String query) {
        return PacienteSingleton.getInstance().getBo().listSugestoesComplete(query,UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
    }
    
    public void marcarFiltros() {
        if (this.checkFiltro) {
            this.filtroAtendimento.addAll(Arrays.asList("F", "A", "G", "C", "D", "I", "S", "O", "E", "H", "B", "N", "P", "R"));
        } else {
            this.filtroAtendimento.removeAll(Arrays.asList("F", "A", "G", "C", "D", "I", "S", "O", "E", "H", "B", "N", "P", "R"));
        }
    }

    public String getStatusDescricao(Agendamento agendamento) {
        try {
            return StatusAgendamentoUtil.findBySigla(agendamento.getStatusNovo()).getDescricao();
        } catch (Exception e) {

        }
        return "";
    }
    
    
    public void mostrarOsPacientes() {
        if(getFiltroStatusAgendamento().contains(SEM_ULTIMO_AGENDAMENTO)) {
            this.checkFiltro = false;
            filtroAtendimento = new ArrayList<String>();
            this.desabilitaStatusAgendamento = true;
        }else {
            this.desabilitaStatusAgendamento = false;
        }
    }
    public void actionFiltrar(ActionEvent event){
      
            
            try {
                if (this.inicio != null && this.fim != null && this.inicio.getTime() > this.fim.getTime()) {
                    this.addError(OdontoMensagens.getMensagem("afastamento.dtFim.menor.dtInicio"), "");
                }
                else {
                    this.pacientes = new ArrayList<Paciente>();
                    if(getFiltroStatusAgendamento().contains(SEM_ULTIMO_AGENDAMENTO)) {
                        this.pacientes = PacienteSingleton.getInstance().getBo().filtraRelatorioPacienteSemAgendamento
                                (UtilsFrontEnd.getProfissionalLogado().getIdEmpresa(), this.paciente,getConvenio(getFiltroPorConvenio()),this.filtroStatusPaciente);
                    }else {
                        this.pacientes = PacienteSingleton.getInstance().getBo().filtraRelatorioPacienteAgendamento
                                (this.inicio, this.fim, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa(), paciente,getConvenio(getFiltroPorConvenio()),this.filtroStatusPaciente);   
                        if(pacientes != null) {
                            List<Paciente> pacientesTemp = new ArrayList<Paciente>(pacientes);
                            for (Paciente paciente : pacientesTemp) {
                                if(paciente.getUltimoAgendamento() != null) {
                                    if(!filtroAtendimento.contains(paciente.getUltimoAgendamento().getStatusNovo())) {
                                        this.pacientes.remove(paciente);
                                    }
                                }                            
                            }  
                        }
                    }                 
                    
                    if(getFiltroStatusAgendamento().contains(SEM_AGENDAMENTO_FUTURO)) {
                        pacientes.removeIf(paciente -> paciente.getProximoAgendamento() != null);
                    }
                    if(getFiltroStatusAgendamento().contains(SEM_RETORNO_FUTURO)) {
                        pacientes.removeIf(paciente -> paciente.getProximoRetorno() != null);
                    }
                    
                }
                this.sugestoesConvenios("todos");
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            
            //TODO verificar se ja consigo pegar no sql somente os relatorios do filtro
            //agendamentos.removeIf(agendamento -> !filtroAtendimento.contains(agendamento.getStatusNovo()));
            
            //TODO pensar como filtrar os status direto na query - field status
   
    }

    public void actionTrocaDatasCriacao() {
        try {
            setInicio(getDataInicio(filtroPeriodo));
            setFim(getDataFim(filtroPeriodo));
          //  actionFiltrar(null);            
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
            }else if(filtro == null) { 
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
                if ("O".equals(filtro)) {
                c.add(Calendar.DAY_OF_MONTH, -1);                
                dataInicio = c.getTime(); 
            } else if ("H".equals(filtro)) { //Hoje                
                dataInicio = c.getTime(); 
            } else if ("S".equals(filtro)) { //Últimos 7 dias              
                c.add(Calendar.DAY_OF_MONTH, -7);
                dataInicio = c.getTime();
            } else if ("Q".equals(filtro)) { //Últimos 15 dias              
                c.add(Calendar.DAY_OF_MONTH, -15);
                dataInicio = c.getTime();
            } else if ("T".equals(filtro)) { //Últimos 30 dias                
                c.add(Calendar.DAY_OF_MONTH, -30);
                dataInicio = c.getTime();
            } else if ("M".equals(filtro)) { //Mês Atual              
                c.set(Calendar.DAY_OF_MONTH, 1);
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
    
    public void sugestoesConvenios(String query) {
        try {
            
            if(!this.getListaConvenios().contains("SEM CONVENIO"))
                this.getListaConvenios().add("Sem Convenio");
            
            List<Convenio> lista = ConvenioSingleton.getInstance().getBo().listSugestoesCompleteTodos(query, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa(),true);
            
            for(Convenio c : lista) {
                this.getListaConvenios().add(c.getDadosBasico().getNome());
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    private Convenio getConvenio(String nome) {
        if(nome != null && !(nome.toUpperCase().equals("TODOS"))) {
            
            List<Convenio> lista = ConvenioSingleton.getInstance().getBo().listSugestoesCompleteTodos(nome, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa(),true);
            
            if(nome.toUpperCase().equals("SEM CONVENIO") && lista.size() == 0)
                return new Convenio();
            
            return lista.get(0);
            
        }
        return null;
    }
    
    public String formatarData(Date data) {
        if (data != null) {
            return new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", new Locale("PT BR")).format(data);
        }
        return "";
    }
    
    public String formatarDataSemHora(Date data) {
        if (data != null) {
            return new SimpleDateFormat("dd/MM/yyyy", new Locale("PT BR")).format(data);
        }
        return "";
    }
    
    public void exportarTabela(String type) {
        exportarTabela("Relatório de agendamentos dos Pacientes", tabelaRelatorio, type);
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

    public Paciente getPaciente() {
        return paciente;
    }
    
    public void setPaciente(Paciente paciente) {
        this.paciente = paciente;
    }
    
    public Convenio getConvenio() {
        return convenio;
    }
    
    public void setConvenio(Convenio convenio) {
        this.convenio = convenio;
    }

    public DataTable getTabelaRelatorio() {
        return tabelaRelatorio;
    }

    public void setTabelaRelatorio(DataTable tabelaRelatorio) {
        this.tabelaRelatorio = tabelaRelatorio;
    }

    public List<String> getListaConvenios() {
        return listaConvenios;
    }

    public void setListaConvenios(List<String> listaConvenios) {
        this.listaConvenios = listaConvenios;
    }

    public String getFiltroPorConvenio() {
        return filtroPorConvenio;
    }

    public void setFiltroPorConvenio(String filtroPorConvenio) {
        this.filtroPorConvenio = filtroPorConvenio;
    }
    
    public List<String> getFiltroAtendimento() {
        return filtroAtendimento;
    }

    
    public void setFiltroAtendimento(List<String> filtroAtendimento) {
        this.filtroAtendimento = filtroAtendimento;
    }

    
    public List<Paciente> getPacientes() {
        return pacientes;
    }

    
    public void setPacientes(List<Paciente> pacientes) {
        this.pacientes = pacientes;
    }

    
    public boolean isCheckFiltro() {
        return checkFiltro;
    }

    
    public void setCheckFiltro(boolean checkFiltro) {
        this.checkFiltro = checkFiltro;
    }

    
    public String getFiltroStatusPaciente() {
        return filtroStatusPaciente;
    }
    
    public void setFiltroStatusPaciente(String filtroStatusPaciente) {
        this.filtroStatusPaciente = filtroStatusPaciente;
    }

    
    public List<String> getFiltroStatusAgendamento() {
        return filtroStatusAgendamento;
    }

    
    public void setFiltroStatusAgendamento(List<String> filtroStatusAgendamento) {
        this.filtroStatusAgendamento = filtroStatusAgendamento;
    }

    
    public boolean isDesabilitaStatusAgendamento() {
        return desabilitaStatusAgendamento;
    }

    
    public void setDesabilitaStatusAgendamento(boolean desabilitaStatusAgendamento) {
        this.desabilitaStatusAgendamento = desabilitaStatusAgendamento;
    }

}
