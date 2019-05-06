package br.com.lume.odonto.managed;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ActionEvent;

import org.apache.log4j.Logger;

import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.lancamento.LancamentoSingleton;
import br.com.lume.odonto.entity.Lancamento;
import br.com.lume.odonto.util.OdontoMensagens;

@ManagedBean
@ViewScoped
public class RelatorioLancamentoMB extends LumeManagedBean<Lancamento> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(LancamentoMB.class);

    private List<Lancamento> Lancamentos = new ArrayList<>();

    private Date inicio, fim;

    private String status;

    private BigDecimal somaValor = new BigDecimal(0);

   

    public final List<String> statuss;

    public RelatorioLancamentoMB() {
        super(LancamentoSingleton.getInstance().getBo());
      
        this.setClazz(Lancamento.class);
        this.statuss = new ArrayList<>();
        this.statuss.add(Lancamento.AGENDADO);
        this.statuss.add(Lancamento.ATIVO);
        this.statuss.add(Lancamento.ATRASADO);
        this.statuss.add(Lancamento.CANCELADO);
        this.statuss.add(Lancamento.PAGO);
        this.statuss.add(Lancamento.PENDENTE);
        this.filtra();
    }

    public void filtra() {
        try {
            if (this.status != null) {
                if (this.inicio != null && this.fim != null && this.inicio.getTime() > this.fim.getTime()) {
                    this.addError(OdontoMensagens.getMensagem("afastamento.dtFim.menor.dtInicio"), "");
                } else {
                    this.Lancamentos = LancamentoSingleton.getInstance().getBo().listAllByPeriodoAndStatus(this.inicio, this.fim, this.status);
                    this.somaValor = new BigDecimal(0);
                    for (Lancamento l : this.Lancamentos) {
                        this.somaValor = this.somaValor.add(l.getValor()).setScale(2, BigDecimal.ROUND_HALF_UP);
                    }
                    if (this.Lancamentos == null || this.Lancamentos.isEmpty()) {
                        this.addError(OdontoMensagens.getMensagem("relatorio.procedimento.vazio"), "");
                        this.log.error(OdontoMensagens.getMensagem("relatorio.procedimento.vazio"));
                    }
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
        return this.Lancamentos;
    }

    public void setLancamentos(List<Lancamento> lancamentos) {
        this.Lancamentos = lancamentos;
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
}
