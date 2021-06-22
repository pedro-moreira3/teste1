package br.com.lume.odonto.managed;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import org.apache.log4j.Logger;
import org.primefaces.PrimeFaces;
import org.primefaces.component.datatable.DataTable;

import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.JSFHelper;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.Utils;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.convenio.ConvenioSingleton;
import br.com.lume.especialidade.EspecialidadeSingleton;
import br.com.lume.odonto.entity.Convenio;
import br.com.lume.odonto.entity.Especialidade;
import br.com.lume.odonto.entity.Paciente;
import br.com.lume.odonto.entity.PlanoTratamento;
import br.com.lume.odonto.entity.PlanoTratamentoProcedimento;
import br.com.lume.odonto.entity.Procedimento;
import br.com.lume.odonto.entity.Profissional;
import br.com.lume.paciente.PacienteSingleton;
import br.com.lume.planoTratamento.PlanoTratamentoSingleton;
import br.com.lume.planoTratamentoProcedimento.PlanoTratamentoProcedimentoSingleton;
import br.com.lume.procedimento.ProcedimentoSingleton;
import br.com.lume.profissional.ProfissionalSingleton;

@ManagedBean
@ViewScoped
public class RelatorioProcedimentoMB extends LumeManagedBean<PlanoTratamentoProcedimento> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(RelatorioProcedimentoMB.class);
    
    private List<PlanoTratamentoProcedimento> listaProcedimentos;
    
    private List<String> filtroProcedimento;

    private List<String> listaConvenios;
    
    
    // ATRIBUTOS USADOS COMO FILTRO PARA PESQUISA DOS PROCEDIMENTOS
    private Profissional filtroPorProfissional;
    private Profissional filtroPorProfissionalUltAlteracao;
    
    private PlanoTratamento filtroPorPlanoTratamento;
    
    private Paciente filtroPorPaciente;
    
    private Date dataInicio, dataFim;
    private Date dataFinalizacaoInicio, dataFinalizacaoFim;
    
    private String filtroPorConvenio;
    private String filtroPeriodo;
    private String filtroPeriodoFinalizacao;
    
    private List<Especialidade> especialidades;
    private Especialidade filtroPorEspecialidade;
   
    private Procedimento filtroPorProcedimento;
    
    private boolean imprimirCabecalho = true;
    
    private DataTable tabelaProcedimento;

    public RelatorioProcedimentoMB() {
        super(PlanoTratamentoProcedimentoSingleton.getInstance().getBo());
        this.setClazz(PlanoTratamentoProcedimento.class);
        
        if(filtroProcedimento == null) {
            this.filtroProcedimento = new ArrayList<String>();
        }
        filtroPeriodo = "S";
        this.dataInicio = getDataInicio(filtroPeriodo);
        this.dataFim = getDataFim(filtroPeriodo);
        
        if(this.listaConvenios == null)
            this.listaConvenios = new ArrayList<String>();
        
        this.sugestoesConvenios("todos");
        
       // popularLista();
        
        try {
            this.especialidades = EspecialidadeSingleton.getInstance().getBo().listByEmpresa(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
        } catch (Exception e) {
            // TODO: handle exception
        }
    }
    
    public void popularLista() {
        try{
            
            if(validarIntervaloDatas()) {
                
                this.listaProcedimentos = PlanoTratamentoProcedimentoSingleton.getInstance().getBo().
                        listByDataAndProfissionalAndPaciente(Utils.formataComecoDia(this.dataInicio), Utils.formataFimDia(this.dataFim), Utils.formataComecoDia(this.dataFinalizacaoInicio),
                                Utils.formataFimDia(this.dataFinalizacaoFim), this.filtroPorPaciente, 
                        this.filtroPorProfissional, this.filtroPorPlanoTratamento, this.getConvenio(filtroPorConvenio), this.filtroPorProcedimento,UtilsFrontEnd.getProfissionalLogado().getIdEmpresa(),
                        this.filtroPorEspecialidade);
                
                if(!this.filtroProcedimento.isEmpty())
                    removerFiltrosProcedimento(this.listaProcedimentos);
                
            }
            
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
        try {
            return PacienteSingleton.getInstance().listSugestoesComplete(query, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            e.printStackTrace();
        }
        return null;
    }
    
    public List<Profissional> sugestoesProfissionais(String query) {
        return ProfissionalSingleton.getInstance().getBo().listSugestoesCompleteDentista(query,UtilsFrontEnd.getProfissionalLogado().getIdEmpresa(),true);
    }
    
    public List<Profissional> sugestoesProfissionalUltAlteracao(String query) {
        return ProfissionalSingleton.getInstance().getBo().listSugestoesCompleteProfissional(query,UtilsFrontEnd.getProfissionalLogado().getIdEmpresa(),true);
    }
    
    public List<Procedimento> sugestoesProcedimento(String query){
        return ProcedimentoSingleton.getInstance().getBo().listSugestoesComplete(query, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
    }
    
//    public List<Convenio> sugestoesConvenios(String query) {
//        return ConvenioSingleton.getInstance().getBo().listSugestoesComplete(query,UtilsFrontEnd.getProfissionalLogado().getIdEmpresa(),true);
//    }
    
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
            boolean mostraCancelado = this.filtroProcedimento.contains("C") && procedimento.isCancelado();
            boolean mostraFinalizado = this.filtroProcedimento.contains("F") && procedimento.isFinalizado();
            boolean mostraNFinalizado = this.filtroProcedimento.contains("N") && !procedimento.isFinalizado();
              
            if(!mostraCancelado && !mostraFinalizado && !mostraNFinalizado) {
                procedimentos.remove(procedimento);
            }
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
    
    public void actionTrocaDatasCriacao() {
        try {
            
            this.dataInicio = getDataInicio(filtroPeriodo);
            this.dataFim = getDataFim(filtroPeriodo);
            
            PrimeFaces.current().ajax().update(":lume:dataInicial");
            PrimeFaces.current().ajax().update(":lume:dataFinal");
            
        } catch (Exception e) {
            log.error("Erro no actionTrocaDatasCriacao", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
        }
    }
    
    public void actionTrocaDatasFinal() {
        try {
            
            this.dataFinalizacaoInicio = getDataInicio(filtroPeriodoFinalizacao);
            this.dataFinalizacaoFim =  getDataFim(filtroPeriodoFinalizacao);
            
            PrimeFaces.current().ajax().update(":lume:dataFinalizacaoInicial");
            PrimeFaces.current().ajax().update(":lume:dataFinalizacaoFinal");
            
        } catch (Exception e) {
            log.error("Erro no actionTrocaDatasFinal", e);
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
    
    private boolean validarIntervaloDatas() {
        
        if((dataInicio != null && dataFim != null) && dataInicio.getTime() > dataFim.getTime()) {
            this.addError("Intervalo de datas", "A data inicial deve preceder a data final.", true);
            return false;
        }else if((dataFinalizacaoInicio != null && dataFinalizacaoFim != null) && dataFinalizacaoInicio.getTime() > dataFinalizacaoFim.getTime()) {
            this.addError("Intervalo de datas", "A data inicial deve preceder a data final.", true);
            return false;
        }
        
        return true;
    }
    
    public String dataCriacaoProcediemento(PlanoTratamentoProcedimento ptp) {
        
        if(ptp.getDataCriado() != null)
            return Utils.dateToString(ptp.getDataCriado());
        else
            return Utils.dateToString(ptp.getPlanoTratamento().getDataHora());        
    }
    
    public void exportarTabela(String type) {
        exportarTabela("Relatório de Procedimentos", tabelaProcedimento, type);
    }
    
    public String statusProcedimento(String status) {
        return (status.equals("F") ? "Executado" : "Não executado");
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

    public String getFiltroPorConvenio() {
        return filtroPorConvenio;
    }

    public void setFiltroPorConvenio(String filtroPorConvenio) {
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

    public List<String> getListaConvenios() {
        return listaConvenios;
    }

    public void setListaConvenios(List<String> listaConvenios) {
        this.listaConvenios = listaConvenios;
    }

    public Procedimento getFiltroPorProcedimento() {
        return filtroPorProcedimento;
    }

    public void setFiltroPorProcedimento(Procedimento filtroPorProcedimento) {
        this.filtroPorProcedimento = filtroPorProcedimento;
    }

    public String getFiltroPeriodo() {
        return filtroPeriodo;
    }

    public void setFiltroPeriodo(String filtroPeriodo) {
        this.filtroPeriodo = filtroPeriodo;
    }

    public Date getDataFinalizacaoInicio() {
        return dataFinalizacaoInicio;
    }

    public void setDataFinalizacaoInicio(Date dataFinalizacaoInicio) {
        this.dataFinalizacaoInicio = dataFinalizacaoInicio;
    }

    public Date getDataFinalizacaoFim() {
        return dataFinalizacaoFim;
    }

    public void setDataFinalizacaoFim(Date dataFinalizacaoFim) {
        this.dataFinalizacaoFim = dataFinalizacaoFim;
    }

    public String getFiltroPeriodoFinalizacao() {
        return filtroPeriodoFinalizacao;
    }

    public void setFiltroPeriodoFinalizacao(String filtroPeriodoFinalizacao) {
        this.filtroPeriodoFinalizacao = filtroPeriodoFinalizacao;
    }
    
    public List<Especialidade> getEspecialidades() {
        return especialidades;
    }
    
    public void setEspecialidades(List<Especialidade> especialidades) {
        this.especialidades = especialidades;
    }
    
    public Especialidade getFiltroPorEspecialidade() {
        return filtroPorEspecialidade;
    }

    public void setFiltroPorEspecialidade(Especialidade filtroPorEspecialidade) {
        this.filtroPorEspecialidade = filtroPorEspecialidade;
    }
    
}
