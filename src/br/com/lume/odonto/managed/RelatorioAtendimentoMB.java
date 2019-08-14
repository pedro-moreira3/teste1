package br.com.lume.odonto.managed;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import org.apache.log4j.Logger;
import org.primefaces.model.chart.PieChartModel;

import br.com.lume.agendamento.AgendamentoSingleton;
import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.StatusAgendamentoUtil;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.common.util.UtilsPrimefaces;
import br.com.lume.convenio.ConvenioSingleton;
import br.com.lume.odonto.entity.Agendamento;
import br.com.lume.odonto.entity.Convenio;
import br.com.lume.odonto.entity.Paciente;
import br.com.lume.odonto.entity.Profissional;
import br.com.lume.paciente.PacienteSingleton;
import br.com.lume.profissional.ProfissionalSingleton;

@ManagedBean
@ViewScoped
public class RelatorioAtendimentoMB extends LumeManagedBean<Agendamento> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(AgendamentoMB.class);

    private PieChartModel pieModel;

    private List<String> filtroAtendimento;
    
    private List<Agendamento> listaAtendimentos;

    private List<Agendamento> agendamentos;
    
    private String filtro = "CURRENT_DATE";

    private int dia;

    private HashSet<String> profissionaisAgendamento;

    private List<Integer> cadeiras;
    
    // ATRIBUTOS USADOS COMO FILTRO PARA PESQUISA DOS AGENDAMENTOS
    private Profissional filtroPorProfissional;
    private Profissional filtroPorProfissionalUltAlteracao;
    private Paciente filtroPorPaciente;
    private Date dataInicio, dataFim;
    private Convenio filtroPorConvenio;
    
    private boolean checkFiltro = false;
    private boolean imprimirCabecalho = true;
    private boolean novoAgendamento = true;

    public RelatorioAtendimentoMB() {
        super(AgendamentoSingleton.getInstance().getBo());
        pieModel = new PieChartModel();
        this.setClazz(Agendamento.class);
        
        if(filtroAtendimento == null) {
            this.filtroAtendimento = new ArrayList<String>();
        }
        
        filtroAtendimento.addAll(Arrays.asList("F", "A", "I", "S", "O", "E", "B", "N", "P", "G", "H"));
        
        if(getDataInicio() == null && getDataFim() == null) {
            Calendar calendario = Calendar.getInstance();
            this.setDataFim(calendario.getTime());
            calendario.add(Calendar.DAY_OF_MONTH, -7);
            this.setDataInicio(calendario.getTime());
        }
        
        this.popularLista();
    }
    
    public void popularLista() {
        try{
            
            this.setListaAtendimentos(AgendamentoSingleton.getInstance().getBo().listByDataAndPacientesAndProfissionais(getDataInicio(), getDataFim(),
                    getFiltroPorProfissional(), getFiltroPorProfissionalUltAlteracao(), getFiltroPorPaciente(), getFiltroPorConvenio(),
                    UtilsFrontEnd.getProfissionalLogado().getIdEmpresa()));
            
            this.removerFiltrosAgendamento(this.getListaAtendimentos());
            
        }catch(Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
        }
    }
    
    public String formatarData(Date data) {
        if(data != null) {
            return new SimpleDateFormat("dd/MM/yyyy HH:mm:ss",new Locale("PT BR")).format(data);
        }
        return "";
    }
    
    public List<Paciente> sugestoesPacientes(String query) {
        return PacienteSingleton.getInstance().getBo().listSugestoesComplete(query,UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
    }
    
    public List<Profissional> sugestoesProfissionais(String query) {
        return ProfissionalSingleton.getInstance().getBo().listSugestoesComplete(query,UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
    }
    
    public List<Profissional> sugestoesProfissionalUltAlteracao(String query) {
        return ProfissionalSingleton.getInstance().getBo().listSugestoesComplete(query,UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
    }
    
    public List<Convenio> sugestoesConvenios(String query) {
        return ConvenioSingleton.getInstance().getBo().listSugestoesComplete(query,UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
    }
    
    private void removerFiltrosAgendamento(List<Agendamento> agendamentos) {
        List<Agendamento> agentamentoAux = new ArrayList<>(agendamentos);
        for (Agendamento agendamento : agentamentoAux) {
            if(!filtroAtendimento.contains(agendamento.getStatusNovo())) {
                agendamentos.remove(agendamento);
            }
        }
    }
    
    public String verificarSituacaoAgendamento(Agendamento agendamento) {
        if(agendamento.getStatusNovo().equals("O")) {
            return "Em atendimento";
        }else if(agendamento.getStatusNovo().equals("I")) {
            return "Na clinica";
        }else if(agendamento.getStatusNovo().equals("S")) {
            return "Confirmado";
        }
        return "N/A";
    }
    
    public String nomeClinica() {
        return UtilsFrontEnd.getEmpresaLogada().getEmpStrNmefantasia();
    }
    
    public String telefoneClinica() {
        return UtilsFrontEnd.getEmpresaLogada().getEmpChaFone();
    }
    
    public void marcarFiltros() {
        if(this.checkFiltro) {
            this.filtroAtendimento.addAll(Arrays.asList("F", "A", "G", "C", "D", "I", "S", "O", "E", "H", "B", "N", "P", "R"));
        }else {
            this.filtroAtendimento.removeAll(Arrays.asList("F", "A", "G", "C", "D", "I", "S", "O", "E", "H", "B", "N", "P", "R"));
        }
    }
    
    public String getStatusDescricao(Agendamento agendamento) {
        try {
            return StatusAgendamentoUtil.findBySigla(agendamento.getStatusNovo()).getDescricao();
        }catch(Exception e) {
            
        }
        return "";
    }
    
    public void carregarTelaAgendamento(Agendamento agendamento) {
        FacesContext.getCurrentInstance().getExternalContext().getFlash().put("agendamento", agendamento);
    }
    
    public void verificarNovoAgendamento() {
        if(this.novoAgendamento) {
            UtilsPrimefaces.readOnlyUIComponent(":lume:pnDlgAgendamento",false);
        }else {
            UtilsPrimefaces.readOnlyUIComponent(":lume:pnDlgAgendamento",true);
        }
    }
    
    public boolean verificarStatusAgendamentoFuturo(Agendamento agendamento) {
        if(agendamento.getInicio().after(Calendar.getInstance().getTime())) {
            return false;
        }else if(agendamento.getProximoAgendamentoPaciente() != null) {
            return false;
        }
        return true;
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

    public List<Agendamento> getListaAtendimentos() {
        return listaAtendimentos;
    }

    public void setListaAtendimentos(List<Agendamento> listAtendimentos) {
        this.listaAtendimentos = listAtendimentos;
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

    public List<String> getFiltroAtendimento() {
        return filtroAtendimento;
    }

    public void setFiltroAtendimento(List<String> filtroAtendimento) {
        this.filtroAtendimento = filtroAtendimento;
    }

    public Profissional getFiltroPorProfissional() {
        return filtroPorProfissional;
    }

    public void setFiltroPorProfissional(Profissional filtroPorProfissional) {
        this.filtroPorProfissional = filtroPorProfissional;
    }

    public Profissional getFiltroPorProfissionalUltAlteracao() {
        return filtroPorProfissionalUltAlteracao;
    }

    public void setFiltroPorProfissionalUltAlteracao(Profissional filtroPorProfissionalUltAlteracao) {
        this.filtroPorProfissionalUltAlteracao = filtroPorProfissionalUltAlteracao;
    }

    public Paciente getFiltroPorPaciente() {
        return filtroPorPaciente;
    }

    public void setFiltroPorPaciente(Paciente filtroPorPaciente) {
        this.filtroPorPaciente = filtroPorPaciente;
    }

    public Convenio getFiltroPorConvenio() {
        return filtroPorConvenio;
    }

    public void setFiltroPorConvenio(Convenio filtroPorConvenio) {
        this.filtroPorConvenio = filtroPorConvenio;
    }

    public boolean isCheckFiltro() {
        return checkFiltro;
    }

    public void setCheckFiltro(boolean checkFiltro) {
        this.checkFiltro = checkFiltro;
    }

    public boolean isImprimirCabecalho() {
        return imprimirCabecalho;
    }

    public void setImprimirCabecalho(boolean imprimirCabecalho) {
        this.imprimirCabecalho = imprimirCabecalho;
    }

    public boolean isNovoAgendamento() {
        return novoAgendamento;
    }

    public void setNovoAgendamento(boolean novoAgendamento) {
        this.novoAgendamento = novoAgendamento;
    }
}
