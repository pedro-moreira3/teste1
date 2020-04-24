package br.com.lume.odonto.managed;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ActionEvent;

import org.apache.log4j.Logger;
import org.primefaces.PrimeFaces;
import org.primefaces.component.datatable.DataTable;

import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.Utils;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.odonto.entity.Orcamento;
import br.com.lume.odonto.entity.Paciente;
import br.com.lume.odonto.entity.Profissional;
import br.com.lume.odonto.util.OdontoMensagens;
import br.com.lume.orcamento.OrcamentoSingleton;
import br.com.lume.paciente.PacienteSingleton;
import br.com.lume.profissional.ProfissionalSingleton;

@ManagedBean
@ViewScoped
public class RelatorioOrcamentoMB extends LumeManagedBean<Orcamento> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(RelatorioOrcamentoMB.class);

    private Date inicio = new Date(), fim = new Date(), aprovacaoInicio, aprovacaoFim;

   // private List<RelatorioOrcamento> relatorioOrcamentos = new ArrayList<>();
    private List<Orcamento> relatorioOrcamentos = new ArrayList<>();

    private BigDecimal somaValorTotal = new BigDecimal(0), somaValorTotalDesconto = new BigDecimal(0), somaValorTotalPago = new BigDecimal(0);
    
    private String filtroPeriodo;
    private String filtroPeriodoAprovacao;
    private String filtroStatusPagamento;
    
    private List<String> filtroOrcamento = new ArrayList<String>();
    
    private Profissional filtroPorProfissional;
    
    private Paciente pacienteSelecionado;
    
    private boolean filtrandoAprovacao = false;
    
    //EXPORTAÇÃO TABELA
    private DataTable tabelaRelatorio;

    public RelatorioOrcamentoMB() {
        super(OrcamentoSingleton.getInstance().getBo());
        this.setClazz(Orcamento.class);
        Calendar c = Calendar.getInstance();
       // this.fim = c.getTime();
        c.add(Calendar.MONTH, -1);
        this.inicio = c.getTime();
        c = Calendar.getInstance();
        this.fim = c.getTime(); 
        this.filtra();
    }

    public void filtra() {
        if ((this.inicio != null && this.fim != null) && (this.inicio.getTime() > this.fim.getTime())) {
            this.addError(OdontoMensagens.getMensagem("afastamento.dtFim.menor.dtInicio"), "");
        } else {
            //this.fim = Utils.getLastHourOfDate(this.fim);
            if(validarIntervaloDatas()) {
                
                if(inicio != null && fim != null) {
                    this.inicio = Utils.setFirstHourDate(this.inicio);
                    this.fim = Utils.setLastHourDate(this.fim);
                }
                
                this.somaValorTotal = new BigDecimal(0);
                this.somaValorTotalDesconto = new BigDecimal(0);
                this.somaValorTotalPago = new BigDecimal(0);
                
                this.relatorioOrcamentos = OrcamentoSingleton.getInstance().getBo().listByData(this.inicio, this.fim, this.aprovacaoInicio, this.aprovacaoFim, this.filtroPorProfissional,
                        this.getPacienteSelecionado(), UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
                
                if(this.relatorioOrcamentos != null)
                    validarFiltro(relatorioOrcamentos);
                
                if (this.relatorioOrcamentos != null && !this.relatorioOrcamentos.isEmpty()) {
                    for (Orcamento relatorioOrcamento : this.relatorioOrcamentos) {                        
                        this.somaValorTotal = this.somaValorTotal.add(relatorioOrcamento.getValorTotalSemDesconto());
                        this.somaValorTotalDesconto = this.somaValorTotalDesconto.add(relatorioOrcamento.getValorTotalComDesconto());
                        this.somaValorTotalPago = this.somaValorTotalPago.add((relatorioOrcamento.getValorPago() == null ? new BigDecimal(0) : relatorioOrcamento.getValorPago()));
                    }
                }
            }
            
        }
    }

    
    public List<Profissional> sugestoesProfissionais(String query) {
        return ProfissionalSingleton.getInstance().getBo().listSugestoesCompleteProfissional(query, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa(), true);
    }
    
    public List<Paciente> sugestoesPacientes(String query){
        return PacienteSingleton.getInstance().getBo().listSugestoesComplete(query, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
    }
    
    public String origemOrcamento(Orcamento orcamento) {
        
        if(!orcamento.getItens().isEmpty())
            return orcamento.getItens().get(0).getOrigemProcedimento().getPlanoTratamentoProcedimento().getPlanoTratamento().getDescricao();
        
        return "";
    }
    
    @Override
    public void actionNew(ActionEvent arg0) {
        this.inicio = null;
        this.fim = null;
        super.actionNew(arg0);
    }

    public void actionLimpar() {
        this.inicio = null;
        this.fim = null;
    }
    
    public void actionTrocaDatasCriacao() {
        try {

            this.inicio = getDataInicio(getFiltroPeriodo());
            this.fim = getDataFim(getFiltroPeriodo());

            PrimeFaces.current().ajax().update(":lume:inicio");
            PrimeFaces.current().ajax().update(":lume:fim");

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

    public void actionTrocaDatasAprovacao() {
        try {

            if(filtroPeriodoAprovacao == null || filtroPeriodoAprovacao.equals("")) {
                filtrandoAprovacao = false;              
                this.aprovacaoInicio = null;
                this.aprovacaoFim = null;                
            }else {
                filtrandoAprovacao = true;
                this.aprovacaoInicio = getDataInicio(getFiltroPeriodoAprovacao());
                this.aprovacaoFim = getDataFim(getFiltroPeriodoAprovacao());
                filtroOrcamento = new ArrayList<String>();
                filtroOrcamento.add("A");
            }
            PrimeFaces.current().ajax().update(":lume:aprovacaoInicio");
            PrimeFaces.current().ajax().update(":lume:aprovacaoFim");
            PrimeFaces.current().ajax().update(":lume:checkbox");
    
        } catch (Exception e) {
            log.error("Erro no actionTrocaDatasAprovacao", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
        }
    }
    
    private boolean validarIntervaloDatas() {

        if ((inicio != null && fim != null) && inicio.getTime() > fim.getTime()) {
            this.addError("Intervalo de datas", "A data inicial deve preceder a data final.", true);
            return false;
        }
        
        if ((aprovacaoInicio != null && aprovacaoFim != null) && aprovacaoInicio.getTime() > aprovacaoFim.getTime()) {
            this.addError("Intervalo de datas", "A data inicial deve preceder a data final.", true);
            return false;
        }
        return true;
    }
    
    public String statusPagamento(Orcamento orcamento) {    
        if(orcamento.isAtivo()) {
            if( (orcamento.isAprovado() && orcamento.getValorPago().intValue() < orcamento.getValorTotal().intValue()) ) {
                return "A Receber";
            }else if( (orcamento.getValorPago().intValue() == orcamento.getValorTotal().intValue()) ) {
                return "Recebidos";
            }
        }
        
        return "";
    }
    
    private void validarFiltro(List<Orcamento> orc) {
        
        List<Orcamento> orcamentos = new ArrayList(orc);
        
        for(Orcamento orcamento : orcamentos) {
            OrcamentoSingleton.getInstance().recalculaValores(orcamento);
            
            if(this.filtroStatusPagamento != null) {
                if( (this.filtroStatusPagamento.equals("P") && (orcamento.getValorPago().intValue() < orcamento.getValorTotal().intValue())) ) 
                    this.relatorioOrcamentos.remove(orcamento);
                else if( (this.filtroStatusPagamento.equals("N") && (orcamento.getValorPago().intValue() == orcamento.getValorTotal().intValue())) )
                    this.relatorioOrcamentos.remove(orcamento);
            }
            
            if(this.filtroOrcamento != null && !this.filtroOrcamento.isEmpty()) {
                if( (this.filtroOrcamento.contains("A")) ) {
                    if( !orcamento.isAprovado() || !orcamento.isAtivo())
                        this.relatorioOrcamentos.remove(orcamento);
                }
                
                if( (this.filtroOrcamento.contains("I")) ) {
                    if( orcamento.isAprovado() || !orcamento.isAtivo())
                        this.relatorioOrcamentos.remove(orcamento);
                }
            }
            
        }
        
    }
    
    public void exportarTabela(String type) {
        exportarTabela("Relatório de Orçamentos", tabelaRelatorio, type);
    }

    public String getVigencia() {
        return "Orçamento_" + Utils.dateToString(this.inicio, "dd/MM/yyyy") + "_" + Utils.dateToString(this.fim, "dd/MM/yyyy");
    }

    public List<Orcamento> getRelatorioOrcamentos() {
        return this.relatorioOrcamentos;
    }

    public void setRelatorioOrcamentos(List<Orcamento> relatorioOrcamentos) {
        this.relatorioOrcamentos = relatorioOrcamentos;
    }

    public Date getInicio() {
        return this.inicio;
    }

    public void setInicio(Date inicio) {
        this.inicio = inicio;
    }

    public Date getFim() {
        return this.fim;
    }

    public void setFim(Date fim) {
        this.fim = fim;
    }

    public BigDecimal getSomaValorTotal() {
        return this.somaValorTotal;
    }

    public void setSomaValorTotal(BigDecimal somaValorTotal) {
        this.somaValorTotal = somaValorTotal;
    }

    public BigDecimal getSomaValorTotalDesconto() {
        return this.somaValorTotalDesconto;
    }

    public void setSomaValorTotalDesconto(BigDecimal somaValorTotalDesconto) {
        this.somaValorTotalDesconto = somaValorTotalDesconto;
    }

    public DataTable getTabelaRelatorio() {
        return tabelaRelatorio;
    }

    public void setTabelaRelatorio(DataTable tabelaRelatorio) {
        this.tabelaRelatorio = tabelaRelatorio;
    }

    public String getFiltroPeriodo() {
        return filtroPeriodo;
    }

    public void setFiltroPeriodo(String filtroPeriodo) {
        this.filtroPeriodo = filtroPeriodo;
    }

    public String getFiltroPeriodoAprovacao() {
        return filtroPeriodoAprovacao;
    }

    public void setFiltroPeriodoAprovacao(String filtroPeriodoAprovacao) {
        this.filtroPeriodoAprovacao = filtroPeriodoAprovacao;
    }

    public Date getAprovacaoInicio() {
        return aprovacaoInicio;
    }

    public void setAprovacaoInicio(Date aprovacaoInicio) {
        this.aprovacaoInicio = aprovacaoInicio;
    }

    public Date getAprovacaoFim() {
        return aprovacaoFim;
    }

    public void setAprovacaoFim(Date aprovacaoFim) {
        this.aprovacaoFim = aprovacaoFim;
    }

    public BigDecimal getSomaValorTotalPago() {
        return somaValorTotalPago;
    }

    public void setSomaValorTotalPago(BigDecimal somaValorTotalPago) {
        this.somaValorTotalPago = somaValorTotalPago;
    }

    public List<String> getFiltroOrcamento() {
        return filtroOrcamento;
    }

    public void setFiltroOrcamento(List<String> filtroOrcamento) {
        this.filtroOrcamento = filtroOrcamento;
    }

    public Profissional getFiltroPorProfissional() {
        return filtroPorProfissional;
    }

    public void setFiltroPorProfissional(Profissional filtroPorProfissional) {
        this.filtroPorProfissional = filtroPorProfissional;
    }

    public String getFiltroStatusPagamento() {
        return filtroStatusPagamento;
    }

    public void setFiltroStatusPagamento(String filtroStatusPagamento) {
        this.filtroStatusPagamento = filtroStatusPagamento;
    }

    
    public boolean isFiltrandoAprovacao() {
        return filtrandoAprovacao;
    }

    
    public void setFiltrandoAprovacao(boolean filtrandoAprovacao) {
        this.filtrandoAprovacao = filtrandoAprovacao;
    }

    /**
     * @return the pacienteSelecionado
     */
    public Paciente getPacienteSelecionado() {
        return pacienteSelecionado;
    }

    /**
     * @param pacienteSelecionado the pacienteSelecionado to set
     */
    public void setPacienteSelecionado(Paciente pacienteSelecionado) {
        this.pacienteSelecionado = pacienteSelecionado;
    }
}
