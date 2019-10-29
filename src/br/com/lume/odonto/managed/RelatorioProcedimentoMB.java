package br.com.lume.odonto.managed;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.apache.log4j.Logger;
import org.primefaces.component.datatable.DataTable;

import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.convenio.ConvenioSingleton;
import br.com.lume.odonto.entity.Convenio;
import br.com.lume.odonto.entity.Paciente;
import br.com.lume.odonto.entity.PlanoTratamento;
import br.com.lume.odonto.entity.PlanoTratamentoProcedimento;
import br.com.lume.odonto.entity.Profissional;
import br.com.lume.paciente.PacienteSingleton;
import br.com.lume.planoTratamento.PlanoTratamentoSingleton;
import br.com.lume.planoTratamentoProcedimento.PlanoTratamentoProcedimentoSingleton;
import br.com.lume.profissional.ProfissionalSingleton;

@ManagedBean
@ViewScoped
public class RelatorioProcedimentoMB extends LumeManagedBean<PlanoTratamentoProcedimento> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(RelatorioProcedimentoMB.class);
    
    private List<PlanoTratamentoProcedimento> listaProcedimentos;
    
    private List<String> filtroProcedimento;

    
    // ATRIBUTOS USADOS COMO FILTRO PARA PESQUISA DOS PROCEDIMENTOS
    private Profissional filtroPorProfissional;
    private PlanoTratamento filtroPorPlanoTratamento;
    private Paciente filtroPorPaciente;
    private Date dataInicio, dataFim;
    private Convenio filtroPorConvenio;
    private Profissional filtroPorProfissionalUltAlteracao;
    
    private boolean imprimirCabecalho = true;
    
    private DataTable tabelaProcedimento;

    public RelatorioProcedimentoMB() {
        super(PlanoTratamentoProcedimentoSingleton.getInstance().getBo());
        this.setClazz(PlanoTratamentoProcedimento.class);
        
        if(filtroProcedimento == null) {
            this.filtroProcedimento = new ArrayList<String>();
        }
        
        Calendar calendario = Calendar.getInstance();
        this.setDataFim(calendario.getTime());
        calendario.add(Calendar.DAY_OF_MONTH, -7);
        this.setDataInicio(calendario.getTime());
        
        popularLista();
    }
    
    public void popularLista() {
        try{            
            this.listaProcedimentos = PlanoTratamentoProcedimentoSingleton.getInstance().getBo().listByDataAndProfissionalAndPaciente(this.dataInicio, this.dataFim, this.filtroPorPaciente, 
                    this.filtroPorProfissional, this.filtroPorPlanoTratamento, this.filtroPorConvenio, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
            
            if(!this.filtroProcedimento.isEmpty())
                removerFiltrosProcedimento(this.listaProcedimentos);
            
        }catch(Exception e){
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
        }
    }
    
    public String formatarData(Date data) {
        if(data != null) {
            return new SimpleDateFormat("dd/MM/yyyy").format(data);
        }
        return "";
    }
    
    public List<Paciente> sugestoesPacientes(String query) {
        return PacienteSingleton.getInstance().getBo().listSugestoesComplete(query,UtilsFrontEnd.getProfissionalLogado().getIdEmpresa(),true);
    }
    
    public List<Profissional> sugestoesProfissionais(String query) {
        return ProfissionalSingleton.getInstance().getBo().listSugestoesCompletePaciente(query,UtilsFrontEnd.getProfissionalLogado().getIdEmpresa(),true);
    }
    
    public List<Profissional> sugestoesProfissionalUltAlteracao(String query) {
        return ProfissionalSingleton.getInstance().getBo().listSugestoesCompletePaciente(query,UtilsFrontEnd.getProfissionalLogado().getIdEmpresa(),true);
    }
    
    public List<Convenio> sugestoesConvenios(String query) {
        return ConvenioSingleton.getInstance().getBo().listSugestoesComplete(query,UtilsFrontEnd.getProfissionalLogado().getIdEmpresa(),true);
    }
    
    public String nomeClinica() {
        return UtilsFrontEnd.getEmpresaLogada().getEmpStrNmefantasia();
    }
    
    public String telefoneClinica() {
        return UtilsFrontEnd.getEmpresaLogada().getEmpChaFone();
    }
    
    public List<PlanoTratamento> sugestoesPlanoTratamento(String query){
        return PlanoTratamentoSingleton.getInstance().getBo().listSugestoesComplete(query);
    }
    
    private void removerFiltrosProcedimento(List<PlanoTratamentoProcedimento> procedimentos) {     
        List<PlanoTratamentoProcedimento> listaAux = new ArrayList<>(procedimentos);
        for(PlanoTratamentoProcedimento procedimento : listaAux) {
            boolean isCancelado = procedimento.isCancelado();
            boolean isFinalizado = procedimento.isFinalizado();
            
            if(!(this.filtroProcedimento.contains("C") && isCancelado) && !(this.filtroProcedimento.contains("F") && isFinalizado))
                procedimentos.remove(procedimento);
        }
    }
    
    public String statusPagamento(Character status) {
        switch(status) {
            case 'P':
                return "Pendente";
            case 'G':
                return "Pago";
            case 'R':
                return "Reservado";
        }
        return "";
    }
    
    public void exportarTabela(String type) {
        exportarTabela("Relatório de Procedimentos", tabelaProcedimento, type);
    }
    
    public String statusProcedimento(String status) {
        return (status.equals("F") ? "Finalizado" : "Não finalizado");
    }
    
    public Profissional getFiltroPorProfissional() {
        return filtroPorProfissional;
    }

    public void setFiltroPorProfissional(Profissional filtroPorProfissional) {
        this.filtroPorProfissional = filtroPorProfissional;
    }

    public Paciente getFiltroPorPaciente() {
        return filtroPorPaciente;
    }

    public void setFiltroPorPaciente(Paciente filtroPorPaciente) {
        this.filtroPorPaciente = filtroPorPaciente;
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

    public Convenio getFiltroPorConvenio() {
        return filtroPorConvenio;
    }

    public void setFiltroPorConvenio(Convenio filtroPorConvenio) {
        this.filtroPorConvenio = filtroPorConvenio;
    }

    public List<PlanoTratamentoProcedimento> getListaProcedimentos() {
        return listaProcedimentos;
    }

    public void setListaProcedimentos(List<PlanoTratamentoProcedimento> listaProcedimentos) {
        this.listaProcedimentos = listaProcedimentos;
    }

    public PlanoTratamento getFiltroPorPlanoTratamento() {
        return filtroPorPlanoTratamento;
    }

    public void setFiltroPorPlanoTratamento(PlanoTratamento filtroPorPlanoTratamento) {
        this.filtroPorPlanoTratamento = filtroPorPlanoTratamento;
    }

    public List<String> getFiltroProcedimento() {
        return filtroProcedimento;
    }

    public void setFiltroProcedimento(List<String> filtroProcedimento) {
        this.filtroProcedimento = filtroProcedimento;
    }

    public boolean isImprimirCabecalho() {
        return imprimirCabecalho;
    }

    public void setImprimirCabecalho(boolean imprimirCabecalho) {
        this.imprimirCabecalho = imprimirCabecalho;
    }

    public Profissional getFiltroPorProfissionalUltAlteracao() {
        return filtroPorProfissionalUltAlteracao;
    }

    public void setFiltroPorProfissionalUltAlteracao(Profissional filtroPorProfissionalUltAlteracao) {
        this.filtroPorProfissionalUltAlteracao = filtroPorProfissionalUltAlteracao;
    }

    public DataTable getTabelaProcedimento() {
        return tabelaProcedimento;
    }

    public void setTabelaProcedimento(DataTable tabelaProcedimento) {
        this.tabelaProcedimento = tabelaProcedimento;
    }

    

}
