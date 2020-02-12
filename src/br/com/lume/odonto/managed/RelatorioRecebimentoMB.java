package br.com.lume.odonto.managed;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ActionEvent;

import org.apache.log4j.Logger;
import org.primefaces.component.datatable.DataTable;

import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.lancamento.LancamentoSingleton;
import br.com.lume.odonto.entity.Lancamento;
import br.com.lume.odonto.entity.RelatorioRecebimento;
import br.com.lume.odonto.util.OdontoMensagens;
import br.com.lume.relatorioRecebimento.RelatorioRecebimentoSingleton;

@ManagedBean
@ViewScoped
public class RelatorioRecebimentoMB extends LumeManagedBean<RelatorioRecebimento> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(RelatorioRecebimentoMB.class);

    // private List<Lancamento> Lancamentos = new ArrayList<>();

    private List<Lancamento> lancamentos = new ArrayList<>();

    private Date inicio, fim;

    private String status;

    private BigDecimal somaValor = new BigDecimal(0);

    public final List<String> statuss;

    //EXPORTAÇÃO TABELA
    private DataTable tabelaRelatorio;

    public RelatorioRecebimentoMB() {
        super(RelatorioRecebimentoSingleton.getInstance().getBo());

        this.setClazz(RelatorioRecebimento.class);
        this.statuss = new ArrayList<>();
        this.statuss.add(Lancamento.AGENDADO);
        this.statuss.add(Lancamento.ATIVO);
        this.statuss.add(Lancamento.ATRASADO);
        this.statuss.add(Lancamento.CANCELADO);
        this.statuss.add(Lancamento.PAGO);
        this.statuss.add(Lancamento.PENDENTE);
        //this.filtra();
    }

    public void filtra() {
        try {
            if (this.inicio != null && this.fim != null && this.inicio.getTime() > this.fim.getTime()) {
                this.addError(OdontoMensagens.getMensagem("afastamento.dtFim.menor.dtInicio"), "");
            } else {
                this.lancamentos = new ArrayList<>();
                this.somaValor = new BigDecimal(0);
                List<Lancamento> lancamentosFiltrados = LancamentoSingleton.getInstance().getBo().filterRelatorioRecebimentos(UtilsFrontEnd.getEmpresaLogada(), inicio, fim);
                for (Lancamento l : lancamentosFiltrados) {
                    this.somaValor = this.somaValor.add(l.getValor()).setScale(2, BigDecimal.ROUND_HALF_UP);
                    if ((this.status == null || this.status.isEmpty() || l.getStatus().equals(this.status)) && "RP".equals(l.getFatura().getTipoFatura().getRotulo())) {
                        this.lancamentos.add(l);
                    }
                }

                if (this.lancamentos == null || this.lancamentos.isEmpty()) {
                    this.addError(OdontoMensagens.getMensagem("relatorio.procedimento.vazio"), "");
                    this.log.error(OdontoMensagens.getMensagem("relatorio.procedimento.vazio"));
                }
            }
        } catch (Exception e) {
            this.log.error(e);
        }
    }

    @Override
    public void actionNew(ActionEvent arg0) {
        this.inicio = null;
        this.fim = null;
        this.status = null;
        super.actionNew(arg0);
    }
    
    public String planoTratamentoFromLancamento(Lancamento lancamento) {
        
        String planoTratamento = lancamento.getFatura().getItens().get(0).getOrigemOrcamento().getOrcamentoItem().getOrigemProcedimento().getPlanoTratamentoProcedimento().getPlanoTratamento().getDescricao(); 
        
        if(planoTratamento != null)
            return planoTratamento;
        return null;
        
    }

    public void exportarTabela(String type) {
        exportarTabela("Relatório de recebimentos", tabelaRelatorio, type);
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

    public List<Lancamento> getLancamentos() {
        return this.lancamentos;
    }

    public void setLancamentos(List<Lancamento> lancamentos) {
        this.lancamentos = lancamentos;
    }

    public BigDecimal getSomaValor() {
        return this.somaValor;
    }

    public void setSomaValor(BigDecimal somaValor) {
        this.somaValor = somaValor;
    }

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<String> getStatuss() {
        return statuss;
    }

    public DataTable getTabelaRelatorio() {
        return tabelaRelatorio;
    }

    public void setTabelaRelatorio(DataTable tabelaRelatorio) {
        this.tabelaRelatorio = tabelaRelatorio;
    }
}
