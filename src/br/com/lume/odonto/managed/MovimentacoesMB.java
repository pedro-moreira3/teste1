package br.com.lume.odonto.managed;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.primefaces.event.TabChangeEvent;

import br.com.lume.common.log.LogIntelidenteSingleton;
import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.Utils.Mes;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.lancamento.LancamentoSingleton;
import br.com.lume.odonto.entity.Lancamento;

@ManagedBean
@ViewScoped
public class MovimentacoesMB extends LumeManagedBean<Lancamento> {

    private static final long serialVersionUID = 1L;
    private List<Lancamento> aPagar, aReceber;
    private Mes mesAPagar, mesAReceber;
    private boolean consideraLancamentosValidadosAPagar, consideraLancamentosValidadosAReceber;

    public MovimentacoesMB() {
        super(LancamentoSingleton.getInstance().getBo());
        this.setClazz(Lancamento.class);
        this.mesAPagar = this.mesAReceber = getMeses().get(Calendar.getInstance().get(Calendar.MONTH) - 1);
        this.consideraLancamentosValidadosAPagar = this.consideraLancamentosValidadosAReceber = false;
        this.carregaListaAReceber();
    }

    public void onTabChange(TabChangeEvent event) {
        if ("Contas a Receber".equals(event.getTab().getTitle())) {
            carregaListaAReceber();
        } else if ("Contas a Pagar".equals(event.getTab().getTitle())) {
            carregaListaAPagar();
        }
    }

    public void carregaListaAPagar() {
        try {
            this.aPagar = LancamentoSingleton.getInstance().getBo().listContasAPagar(UtilsFrontEnd.getEmpresaLogada().getConta(), this.mesAPagar, this.consideraLancamentosValidadosAPagar);
        } catch (Exception e) {
            LogIntelidenteSingleton.getInstance().makeLog(e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
        }
    }

    public void carregaListaAReceber() {
        try {
            this.aReceber = LancamentoSingleton.getInstance().getBo().listContasAReceber(UtilsFrontEnd.getEmpresaLogada().getConta(), this.mesAReceber, this.consideraLancamentosValidadosAReceber);
        } catch (Exception e) {
            LogIntelidenteSingleton.getInstance().makeLog(e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
        }
    }

    public String getOrigemFromLancamento(Lancamento l) {
        if (l == null || l.getFatura() == null || l.getFatura().getId() == null)
            return "";
        return "Fatura " + l.getFatura().getId();
    }

    public List<Lancamento> getaPagar() {
        return aPagar;
    }

    public void setaPagar(List<Lancamento> aPagar) {
        this.aPagar = aPagar;
    }

    public List<Lancamento> getaReceber() {
        return aReceber;
    }

    public void setaReceber(List<Lancamento> aReceber) {
        this.aReceber = aReceber;
    }

    public Mes getMesAPagar() {
        return mesAPagar;
    }

    public void setMesAPagar(Mes mesAPagar) {
        this.mesAPagar = mesAPagar;
    }

    public Mes getMesAReceber() {
        return mesAReceber;
    }

    public void setMesAReceber(Mes mesAReceber) {
        this.mesAReceber = mesAReceber;
    }

    public boolean isConsideraLancamentosValidadosAPagar() {
        return consideraLancamentosValidadosAPagar;
    }

    public void setConsideraLancamentosValidadosAPagar(boolean consideraLancamentosValidadosAPagar) {
        this.consideraLancamentosValidadosAPagar = consideraLancamentosValidadosAPagar;
    }

    public boolean isConsideraLancamentosValidadosAReceber() {
        return consideraLancamentosValidadosAReceber;
    }

    public void setConsideraLancamentosValidadosAReceber(boolean consideraLancamentosValidadosAReceber) {
        this.consideraLancamentosValidadosAReceber = consideraLancamentosValidadosAReceber;
    }

    public List<Mes> getMeses() {
        return Arrays.asList(Mes.values());
    }

}
