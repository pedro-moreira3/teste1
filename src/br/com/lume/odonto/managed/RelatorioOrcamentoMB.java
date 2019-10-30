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
import org.primefaces.component.datatable.DataTable;

import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Utils;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.odonto.entity.Orcamento;
import br.com.lume.odonto.entity.RelatorioOrcamento;
import br.com.lume.odonto.util.OdontoMensagens;
import br.com.lume.orcamento.OrcamentoSingleton;
import br.com.lume.relatorioOrcamento.RelatorioOrcamentoSingleton;

@ManagedBean
@ViewScoped
public class RelatorioOrcamentoMB extends LumeManagedBean<Orcamento> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(RelatorioOrcamentoMB.class);

    private Date inicio, fim;

   // private List<RelatorioOrcamento> relatorioOrcamentos = new ArrayList<>();
    private List<Orcamento> relatorioOrcamentos = new ArrayList<>();

    private BigDecimal somaValorTotal = new BigDecimal(0), somaValorTotalDesconto = new BigDecimal(0);
    
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
            this.inicio = Utils.setFirstHourDate(this.inicio);
            this.fim = Utils.setLastHourDate(this.fim);
            
            this.somaValorTotal = new BigDecimal(0);
            this.somaValorTotalDesconto = new BigDecimal(0); 
            this.relatorioOrcamentos = OrcamentoSingleton.getInstance().getBo().listByData(this.inicio, this.fim, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
            if (this.relatorioOrcamentos != null && !this.relatorioOrcamentos.isEmpty()) {
                for (Orcamento relatorioOrcamento : this.relatorioOrcamentos) {
                    this.somaValorTotal = this.somaValorTotal.add(relatorioOrcamento.getValorTotalSemDesconto());
                    this.somaValorTotalDesconto = this.somaValorTotalDesconto.add(relatorioOrcamento.getValorTotalComDesconto());
                }
            }
        }
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
}
