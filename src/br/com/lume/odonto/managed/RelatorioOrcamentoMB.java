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

import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Utils;
import br.com.lume.odonto.bo.RelatorioOrcamentoBO;
import br.com.lume.odonto.entity.RelatorioOrcamento;
import br.com.lume.odonto.util.OdontoMensagens;

@ManagedBean
@ViewScoped
public class RelatorioOrcamentoMB extends LumeManagedBean<RelatorioOrcamento> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(RelatorioOrcamentoMB.class);

    private Date inicio, fim;

    private List<RelatorioOrcamento> relatorioOrcamentos = new ArrayList<>();

    private BigDecimal somaValorTotal = new BigDecimal(0), somaValorTotalDesconto = new BigDecimal(0);

    private RelatorioOrcamentoBO relatorioOrcamentoBO;

    public RelatorioOrcamentoMB() {
        super(new RelatorioOrcamentoBO());
        this.relatorioOrcamentoBO = new RelatorioOrcamentoBO();
        this.setClazz(RelatorioOrcamento.class);
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
     
            this.fim = Utils.getLastHourOfDate(this.fim);
            
            this.somaValorTotal = new BigDecimal(0);
            this.somaValorTotalDesconto = new BigDecimal(0);
            this.relatorioOrcamentos = this.relatorioOrcamentoBO.listAll(this.inicio, this.fim);
            if (this.relatorioOrcamentos != null && !this.relatorioOrcamentos.isEmpty()) {
                for (RelatorioOrcamento relatorioOrcamento : this.relatorioOrcamentos) {
                    this.somaValorTotal = this.somaValorTotal.add(relatorioOrcamento.getValorTotal());
                    this.somaValorTotalDesconto = this.somaValorTotalDesconto.add(relatorioOrcamento.getValorTotalDesconto());
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

    public String getVigencia() {
        return "Orçamento_" + Utils.dateToString(this.inicio, "dd/MM/yyyy") + "_" + Utils.dateToString(this.fim, "dd/MM/yyyy");
    }

    public List<RelatorioOrcamento> getRelatorioOrcamentos() {
        return this.relatorioOrcamentos;
    }

    public void setRelatorioOrcamentos(List<RelatorioOrcamento> relatorioOrcamentos) {
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
}
