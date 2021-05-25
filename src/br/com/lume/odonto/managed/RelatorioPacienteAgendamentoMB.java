package br.com.lume.odonto.managed;

import java.sql.Timestamp;
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
import org.primefaces.PrimeFaces;
import org.primefaces.component.datatable.DataTable;

import br.com.lume.common.lazy.models.PacienteLazyModel;
import br.com.lume.common.lazy.models.PlanoTratamentoLazyModel;
import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.StatusAgendamentoUtil;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.convenio.ConvenioSingleton;
import br.com.lume.odonto.entity.Agendamento;
import br.com.lume.odonto.entity.Convenio;
import br.com.lume.odonto.entity.Paciente;
import br.com.lume.odonto.entity.Profissional;
import br.com.lume.odonto.entity.Retorno;
import br.com.lume.odonto.entity.Retorno.StatusRetorno;
import br.com.lume.odonto.util.OdontoMensagens;
import br.com.lume.paciente.PacienteSingleton;
import br.com.lume.planoTratamento.PlanoTratamentoSingleton;
import br.com.lume.profissional.ProfissionalSingleton;
import br.com.lume.retorno.RetornoSingleton;

@ManagedBean
@ViewScoped
public class RelatorioPacienteAgendamentoMB extends LumeManagedBean<Paciente> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(RelatorioPacienteAgendamentoMB.class);

    private List<Paciente> pacientes = new ArrayList<>();
    
    private PacienteLazyModel pacientesLazy;

    private Date inicio, fim;

    private Paciente paciente;   
    
    private Convenio convenio;
    
    private String filtroPeriodo;
    
    private List<String> listaConvenios;
    
    private String filtroPorConvenio;
    
    private String filtroStatusPaciente;
    
    private Profissional filtroPorProfissional;

    //EXPORTAÇÃO TABELA
    private DataTable tabelaRelatorio;
    private DataTable tabelaAniversariantes;
    
    private List<String> filtroAtendimento = new ArrayList<String>();
 
    private List<String> filtroStatusAgendamento;
    
    private List<Paciente> aniversariantes;
    
    private String filtroAniversariantes;
    
    public static final String SEM_ULTIMO_AGENDAMENTO = "SUA";
        
    public static final String SEM_AGENDAMENTO_FUTURO = "SAF";
        
    public static final String SEM_RETORNO_FUTURO = "SRR";
        
    public Retorno retorno;
    
    private Date dataInicio, dataFim;
    
    private boolean checkFiltro = false;
    private boolean desabilitaStatusAgendamento = false;
    private boolean imprimirCabecalho = true;
   
    
    public RelatorioPacienteAgendamentoMB() {
        super(PacienteSingleton.getInstance().getBo());      
        this.setClazz(Paciente.class);
        
        if(this.listaConvenios == null)
            this.listaConvenios = new ArrayList<>();        
        this.sugestoesConvenios("todos");
        
        this.filtroAniversariantes = "A";
        this.setDataInicio(new Date());
        this.setDataFim(new Date());
        actionTrocaDatasCriacaoAniversariantes(this.filtroAniversariantes);
        this.carregarAniversariantes();
        
    }
    
    public List<Paciente> sugestoesPacientes(String query) {
        try {
            return PacienteSingleton.getInstance().listSugestoesComplete(query, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            e.printStackTrace();
        }
        return null;
    }
    
    public List<Profissional> sugestoesProfissionais(String query) {
        return ProfissionalSingleton.getInstance().getBo().listSugestoesCompleteDentista(query, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa(), true);
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
    
    public void carregarAniversariantes() {
        try {
            
            this.aniversariantes = PacienteSingleton.getInstance().getBo().listAniversariantes(dataInicio, dataFim, 
                    UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
            
        } catch (Exception e) {
            this.log.error(e);
            e.printStackTrace();
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "Não foi possível carregar os aniversariantes");
        }
    }
    
    public void limparDlgAniversariantes() {
        this.aniversariantes = null;
        this.filtroAniversariantes = "";
    }
    
    public void exportarTabelaAniversariantes(String type) {
        this.exportarTabela("Aniversariantes", tabelaAniversariantes, type);
    }
    
    public void mostrarOsPacientes() {
        if(getFiltroStatusAgendamento().contains(SEM_ULTIMO_AGENDAMENTO) || getFiltroStatusAgendamento().contains(SEM_AGENDAMENTO_FUTURO)
                || getFiltroStatusAgendamento().contains(SEM_RETORNO_FUTURO)) {
            this.checkFiltro = false;
            filtroAtendimento = new ArrayList<String>();
            this.desabilitaStatusAgendamento = true;
        }else {
            this.desabilitaStatusAgendamento = false;
        }
    }
    
    public List<StatusRetorno> getStatusPossiveis() {
        return Arrays.asList(StatusRetorno.values());
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
                    }
                    
                    if(getFiltroStatusAgendamento().contains(SEM_AGENDAMENTO_FUTURO)) {
                        if(pacientes.isEmpty() && !getFiltroStatusAgendamento().contains(SEM_ULTIMO_AGENDAMENTO)) {
                            this.pacientes = PacienteSingleton.getInstance().getBo().filtraRelatorioPacienteAgendamentoFuturo
                                    (this.inicio, this.fim, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa(), paciente,getConvenio(getFiltroPorConvenio()),this.filtroStatusPaciente
                                            ,this.filtroPorProfissional);   
                        }
                       // pacientes.removeIf(paciente -> paciente.getProximoAgendamento() != null || paciente.getUltimoAgendamento() == null);
                    }
                    
                    if(getFiltroStatusAgendamento().contains(SEM_RETORNO_FUTURO)) {
                        if(pacientes.isEmpty() && !getFiltroStatusAgendamento().contains(SEM_ULTIMO_AGENDAMENTO) && !getFiltroStatusAgendamento().contains(SEM_AGENDAMENTO_FUTURO)) {
                            this.pacientes = PacienteSingleton.getInstance().getBo().filtraRelatorioPacienteAgendamento
                                    (this.inicio, this.fim, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa(), paciente,getConvenio(getFiltroPorConvenio()),this.filtroStatusPaciente
                                            ,this.filtroPorProfissional);   
                        }
                        pacientes.removeIf(paciente -> paciente.getProximoRetorno() != null);
                    }
                    
                    if(!getFiltroStatusAgendamento().contains(SEM_ULTIMO_AGENDAMENTO) && !getFiltroStatusAgendamento().contains(SEM_AGENDAMENTO_FUTURO)
                            && !getFiltroStatusAgendamento().contains(SEM_RETORNO_FUTURO)){
                      //  System.out.println(new Timestamp(System.currentTimeMillis()));
                        this.pacientes = PacienteSingleton.getInstance().getBo().filtraRelatorioPacienteAgendamento
                                (this.inicio, this.fim, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa(), paciente,getConvenio(getFiltroPorConvenio()),this.filtroStatusPaciente
                                        ,this.filtroPorProfissional);   
                        //System.out.println(new Timestamp(System.currentTimeMillis()));
                        
                        pacientes.removeIf(p -> p.getUltimoAgendamento() != null && (!filtroAtendimento.contains(p.getUltimoAgendamento().getStatusNovo()) || p.getUltimoAgendamento().getInicio().after(this.fim)));
                        
//                        if(pacientes != null) {
//                            List<Paciente> pacientesTemp = new ArrayList<Paciente>(pacientes);
//                            for (Paciente paciente : pacientesTemp) {
//                                if(paciente.getUltimoAgendamento() != null) {
//                                    if(!filtroAtendimento.contains(paciente.getUltimoAgendamento().getStatusNovo())) {
//                                        this.pacientes.remove(paciente);
//                                    }
//                                }  
//                                if(paciente.getUltimoAgendamento().getInicio().after(this.fim)) {
//                                    this.pacientes.remove(paciente);
//                                }
//                            }  
//                        }
                        System.out.println(new Timestamp(System.currentTimeMillis()));
                    }
                    
                 //   if(this.pacientes != null && !this.pacientes.isEmpty()) {
                   //     pacientesLazy = new PacienteLazyModel(pacientes);
                  //  }
                 
                                    
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

    public String descricaoStatusPaciente(String status) {
        
        if(status.equals("T"))
            return "Todos";
        if(status.equals("A"))
            return "Ativos";
        if(status.equals("I"))
            return "Inativos";
        
        return"";
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
    
    public String nomeClinica() {
        return UtilsFrontEnd.getEmpresaLogada().getEmpStrNmefantasia();
    }

    public String telefoneClinica() {
        return UtilsFrontEnd.getEmpresaLogada().getEmpChaFone();
    }
    
    public void agendarRetorno(Paciente p) {
        retorno = new Retorno();
        retorno.setPaciente(paciente);
        if(p.getProximoAgendamento() != null) {
            retorno.setAgendamento(p.getProximoAgendamento());    
        }        
        
        if(p.getProximoAgendamento()!= null && p.getProximoAgendamento().getPlanoTratamento() != null) {
            retorno.setPlanoTratamento( p.getProximoAgendamento().getPlanoTratamento());
        }      
    }
    
    public void actionPersistRetorno(ActionEvent event) {
        try {
            RetornoSingleton.getInstance().getBo().persist(retorno);          
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            e.printStackTrace();
        }
    
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
    
    public void actionTrocaDatasCriacaoAniversariantes(String filtro) {
        try {
            Calendar c = Calendar.getInstance();
            switch(this.filtroAniversariantes) {
                case "A":
                    //c.set(Calendar.HOUR_OF_DAY, 1);
                    this.dataInicio = c.getTime();
                    
                    //c.set(Calendar.HOUR_OF_DAY, 23);
                    dataFim = c.getTime();
                    break;
                
                case "B":
                    c.add(Calendar.DAY_OF_MONTH, 1);
                    //c.set(Calendar.HOUR_OF_DAY, 1);
                    this.dataInicio = c.getTime();
                    
                    //c.set(Calendar.HOUR_OF_DAY, 23);
                    dataFim = c.getTime();
                    break;
                    
                case "C":
                    //c.set(Calendar.HOUR_OF_DAY, 1);
                    this.dataInicio = c.getTime();
                    
                    c.add(Calendar.DAY_OF_MONTH, 7);
                    //c.set(Calendar.HOUR_OF_DAY, 23);
                    dataFim = c.getTime();
                    break;
                    
                case "D":
                    //c.set(Calendar.HOUR_OF_DAY, 1);
                    this.dataInicio = c.getTime();
                    
                    c.add(Calendar.DAY_OF_MONTH, 15);
                    //c.set(Calendar.HOUR_OF_DAY, 23);
                    dataFim = c.getTime();
                    break;
                    
                case "O":
                    c.add(Calendar.MONTH, 1);
                    c.set(Calendar.DAY_OF_MONTH, 1);
                    this.dataInicio = c.getTime();
                    
                    c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH));
                    dataFim = c.getTime();
                    break;
                
                case "M":
                    c.set(Calendar.DAY_OF_MONTH, 1);
                    dataInicio = c.getTime();
                    
                    c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH));
                    dataFim = c.getTime();
                    break;
                
                case "S":
                    dataFim = c.getTime();
                    
                    c.add(Calendar.DAY_OF_MONTH, -7);
                    dataInicio = c.getTime();
                    break;
                    
                case "Q":
                    dataFim = c.getTime();
                    
                    c.add(Calendar.DAY_OF_MONTH, -15);
                    dataInicio = c.getTime();
                    break;
                    
                case "T":
                    c.add(Calendar.MONTH, -1);
                    c.set(Calendar.DAY_OF_MONTH, 1);
                    dataInicio = c.getTime();
                    
                    c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH));
                    dataFim = c.getTime();
                    break;
                    
                case "":
                    c.set(Calendar.DAY_OF_MONTH, 1);
                    dataInicio = c.getTime();
                    
                    c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH));
                    dataFim = c.getTime();
                    break;
            }
        } catch (Exception e) {
            log.error("Erro no getDataInicio", e);
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

    public boolean isImprimirCabecalho() {
        return imprimirCabecalho;
    }

    public void setImprimirCabecalho(boolean imprimirCabecalho) {
        this.imprimirCabecalho = imprimirCabecalho;
    }

    
    public Retorno getRetorno() {
        return retorno;
    }

    
    public void setRetorno(Retorno retorno) {
        this.retorno = retorno;
    }

    
    public Profissional getFiltroPorProfissional() {
        return filtroPorProfissional;
    }

    
    public void setFiltroPorProfissional(Profissional filtroPorProfissional) {
        this.filtroPorProfissional = filtroPorProfissional;
    }

    public DataTable getTabelaAniversariantes() {
        return tabelaAniversariantes;
    }

    public void setTabelaAniversariantes(DataTable tabelaAniversariantes) {
        this.tabelaAniversariantes = tabelaAniversariantes;
    }

    public List<Paciente> getAniversariantes() {
        return aniversariantes;
    }

    public void setAniversariantes(List<Paciente> aniversariantes) {
        this.aniversariantes = aniversariantes;
    }

    public String getFiltroAniversariantes() {
        return filtroAniversariantes;
    }

    public void setFiltroAniversariantes(String filtroAniversariantes) {
        this.filtroAniversariantes = filtroAniversariantes;
    }

    public Date getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(Date dataInicio) {
        this.dataInicio = dataInicio;
    }

    public Date getDataFim() {
        return dataFim;
    }

    public void setDataFim(Date dataFim) {
        this.dataFim = dataFim;
    }

    public PacienteLazyModel getPacientesLazy() {
        return pacientesLazy;
    }

    public void setPacientesLazy(PacienteLazyModel pacientesLazy) {
        this.pacientesLazy = pacientesLazy;
    }
}
