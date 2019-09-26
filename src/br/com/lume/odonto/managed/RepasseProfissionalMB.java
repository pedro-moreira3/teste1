package br.com.lume.odonto.managed;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import br.com.lume.common.log.LogIntelidenteSingleton;
import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.faturamento.FaturaSingleton;
import br.com.lume.lancamento.LancamentoSingleton;
import br.com.lume.odonto.entity.Fatura;
import br.com.lume.odonto.entity.FaturaItem;
import br.com.lume.odonto.entity.Profissional;
import br.com.lume.profissional.ProfissionalSingleton;

@ManagedBean
@ViewScoped
public class RepasseProfissionalMB extends LumeManagedBean<Fatura> {

    private static final long serialVersionUID = 1L;
    //private Logger log = Logger.getLogger(FaturaPagtoMB.class);

    private int mes;
    private Profissional profissional;
    private boolean mesesAnteriores, pagoTotalmente;

    public RepasseProfissionalMB() {
        super(FaturaSingleton.getInstance().getBo());
        this.setClazz(Fatura.class);
        try {
            Calendar now = Calendar.getInstance();
            setMes(now.get(Calendar.MONTH) + 1);
        } catch (Exception e) {
            LogIntelidenteSingleton.getInstance().makeLog(e);
        }
    }

    public void pesquisar() {
        try {
            setEntityList(FaturaSingleton.getInstance().getBo().findFaturasFilter(getProfissional(), getMes(), isMesesAnteriores()));
            if (isPagoTotalmente())
                getEntityList().removeIf(fatura -> {
                    if (FaturaSingleton.getInstance().getTotalRestante(fatura).compareTo(BigDecimal.ZERO) <= 0)
                        return true;
                    return false;
                });
        } catch (Exception e) {
            LogIntelidenteSingleton.getInstance().makeLog(e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
        }
    }

    public BigDecimal getTotalPago(Fatura fatura) {
        return LancamentoSingleton.getInstance().getTotalLancamentoPorFatura(fatura, true);
    }

    public BigDecimal getTotalRestante(Fatura fatura) {
        return getTotal(fatura).subtract(getTotalPago(fatura));
    }

    public BigDecimal getTotal(Fatura fatura) {
        BigDecimal total = BigDecimal.ZERO;
        if (fatura == null || fatura.getItens() == null)
            return total;
        for (FaturaItem item : fatura.getItens()) {
            if ("E".equals(item.getTipoSaldo()))
                total = total.add(new BigDecimal(item.getValorItem()));
            else if ("S".equals(item.getTipoSaldo()))
                total = total.subtract(new BigDecimal(item.getValorItem()));
        }
        return total;
    }

    public List<Profissional> geraSugestoesProfissional(String query) {
        List<Profissional> sugestoes = new ArrayList<>();
        List<Profissional> profissionais = new ArrayList<>();
        try {
            profissionais = ProfissionalSingleton.getInstance().getBo().listDentistasByEmpresa(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
            for (Profissional p : profissionais) {
                if (Normalizer.normalize(p.getDadosBasico().getNome().toLowerCase(), Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "").toLowerCase().contains(
                        Normalizer.normalize(query, Normalizer.Form.NFD).toLowerCase())) {
                    sugestoes.add(p);
                }
            }
            Collections.sort(sugestoes);
        } catch (Exception e) {
            LogIntelidenteSingleton.getInstance().makeLog(e);
            this.addError(Mensagens.ERRO_AO_BUSCAR_REGISTROS, "");
        }
        return sugestoes;
    }

    public int getMes() {
        return mes;
    }

    public void setMes(int mes) {
        this.mes = mes;
    }

    public Profissional getProfissional() {
        return profissional;
    }

    public void setProfissional(Profissional profissional) {
        this.profissional = profissional;
    }

    public boolean isMesesAnteriores() {
        return mesesAnteriores;
    }

    public void setMesesAnteriores(boolean mesesAnteriores) {
        this.mesesAnteriores = mesesAnteriores;
    }

    public boolean isPagoTotalmente() {
        return pagoTotalmente;
    }

    public void setPagoTotalmente(boolean pagoTotalmente) {
        this.pagoTotalmente = pagoTotalmente;
    }

}
