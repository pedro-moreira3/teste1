package br.com.lume.odonto.managed;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.primefaces.PrimeFaces;

import br.com.lume.common.log.LogIntelidenteSingleton;
import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.faturamento.FaturaItemSingleton;
import br.com.lume.odonto.entity.FaturaItem;
import br.com.lume.odonto.entity.Paciente;
import br.com.lume.odonto.entity.PlanoTratamento;
import br.com.lume.odonto.entity.PlanoTratamentoProcedimento;
import br.com.lume.odonto.entity.Profissional;
import br.com.lume.paciente.PacienteSingleton;
import br.com.lume.planoTratamento.PlanoTratamentoSingleton;
import br.com.lume.planoTratamentoProcedimento.PlanoTratamentoProcedimentoSingleton;
import br.com.lume.planoTratamentoProcedimento.bo.PlanoTratamentoProcedimentoBO;
import br.com.lume.profissional.ProfissionalSingleton;
import br.com.lume.repasse.RepasseFaturasItemSingleton;
import br.com.lume.repasse.RepasseFaturasLancamentoSingleton;

@ManagedBean
@ViewScoped
public class RepasseItensProfissionalMB extends LumeManagedBean<PlanoTratamentoProcedimento> {

    private static final long serialVersionUID = 1L;
    //private Logger log = Logger.getLogger(FaturaPagtoMB.class);

    private int mes;
    private boolean mesesAnteriores, procSemRepasse, filtroSemRepasse;
    private Paciente paciente;
    private Profissional profissional, profissionalTroca;
    private List<PlanoTratamento> planosTratamento;
    private PlanoTratamento[] planosTratamentoSelecionados;
    private FaturaItem itemTroca;

    public RepasseItensProfissionalMB() {
        super(PlanoTratamentoProcedimentoSingleton.getInstance().getBo());
        this.setClazz(PlanoTratamentoProcedimento.class);
        try {
            Calendar now = Calendar.getInstance();
            setMes(now.get(Calendar.MONTH) + 1);
        } catch (Exception e) {
            LogIntelidenteSingleton.getInstance().makeLog(e);
        }
    }

    public BigDecimal getValorRepassado(PlanoTratamentoProcedimento ptp) {
        return RepasseFaturasLancamentoSingleton.getInstance().getTotalLancamentoFromItemRepasse(getFaturaItemFromPTP(ptp), false);
    }

    public BigDecimal getValorRepassadoEValidado(PlanoTratamentoProcedimento ptp) {
        return RepasseFaturasLancamentoSingleton.getInstance().getTotalLancamentoFromItemRepasse(getFaturaItemFromPTP(ptp), true);
    }

    public BigDecimal getValorRepasseFromPTP(PlanoTratamentoProcedimento ptp) {
        FaturaItem faturaItem = getFaturaItemFromPTP(ptp);
        if (faturaItem != null)
            return faturaItem.getValorComDesconto();
        return BigDecimal.ZERO;
    }

    public FaturaItem getFaturaItemFromPTP(PlanoTratamentoProcedimento ptp) {
        return FaturaItemSingleton.getInstance().getBo().faturaItensRepasseFromPTP(ptp);
    }

    public void abreTrocaItemProfissional(PlanoTratamentoProcedimento ptp) {
        setItemTroca(itemTroca);
        if (itemTroca == null) {
            this.addError("Erro!", "Profissional de origem não possui comissionamento! Função ainda não implementada.");
        } else {
            PrimeFaces.current().executeScript("PF('dlgTrocaItemProfissional').show()");
        }
    }

    public void pesquisar() {
        try {
            this.filtroSemRepasse = this.procSemRepasse;
            //Fatura.TipoFatura tipoFatura = (getTipoFatura() == null || getTipoFatura().isEmpty() ? null : Fatura.TipoFatura.getTipoFromRotulo(getTipoFatura()));
            setEntityList(((PlanoTratamentoProcedimentoBO) this.getbO()).getPTPForRepasseFilters(getPaciente(), Arrays.asList(getPlanosTratamentoSelecionados()), getProfissional(), getMes(),
                    isMesesAnteriores(), isProcSemRepasse()));
        } catch (Exception e) {
            LogIntelidenteSingleton.getInstance().makeLog(e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
        }
    }

    public List<Profissional> geraSugestoesProfissional(String query) {
        List<Profissional> profissionais = new ArrayList<>();
        try {
            profissionais = ProfissionalSingleton.getInstance().getBo().listDentistasByEmpresa(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
            profissionais.removeIf(profissional -> {
                if (!Normalizer.normalize(profissional.getDadosBasico().getNome().toLowerCase(), Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "").toLowerCase().contains(
                        Normalizer.normalize(query, Normalizer.Form.NFD).toLowerCase()))
                    return true;
                return false;
            });
            Collections.sort(profissionais);
        } catch (Exception e) {
            LogIntelidenteSingleton.getInstance().makeLog(e);
            this.addError(Mensagens.ERRO_AO_BUSCAR_REGISTROS, "");
        }
        return profissionais;
    }

    public List<Paciente> geraSugestoesPaciente(String query) {
        return PacienteSingleton.getInstance().getBo().listSugestoesComplete(query, UtilsFrontEnd.getEmpresaLogada().getEmpIntCod());
    }

    public void actionPersistTrocaItemProfissional() {
        try {
            if (!this.filtroSemRepasse)
                RepasseFaturasItemSingleton.getInstance().trocaItemRepasseProfissional(getItemTroca(), null, getProfissionalTroca(), UtilsFrontEnd.getProfissionalLogado());
            else
                System.out.println("");
            //criar repasse
            this.pesquisar();
            this.addInfo("Sucesso", "Troca realizada com sucesso.<br />Verifique a nova fatura em nome de " + getProfissionalTroca().getDadosBasico().getNome() + "!", true);
            PrimeFaces.current().executeScript("PF('dlgTrocaItemProfissional').hide();");
        } catch (Exception e) {
            LogIntelidenteSingleton.getInstance().makeLog(e);
            this.addError("Erro", "Falha ao realizar a troca!<br />" + e.getMessage(), true);
        }
    }

    public int getMes() {
        return mes;
    }

    public void setMes(int mes) {
        this.mes = mes;
    }

    public boolean isMesesAnteriores() {
        return mesesAnteriores;
    }

    public void setMesesAnteriores(boolean mesesAnteriores) {
        this.mesesAnteriores = mesesAnteriores;
    }

    public Paciente getPaciente() {
        return paciente;
    }

    public void setPaciente(Paciente paciente) {
        this.paciente = paciente;
        setPlanosTratamento(PlanoTratamentoSingleton.getInstance().getBo().listAtivosByPaciente(getPaciente()));
        setPlanosTratamentoSelecionados(new PlanoTratamento[] {});
    }

    public Profissional getProfissional() {
        return profissional;
    }

    public void setProfissional(Profissional profissional) {
        this.profissional = profissional;
    }

    public boolean isProcSemRepasse() {
        return procSemRepasse;
    }

    public void setProcSemRepasse(boolean procSemRepasse) {
        this.procSemRepasse = procSemRepasse;
    }

    public List<PlanoTratamento> getPlanosTratamento() {
        return planosTratamento;
    }

    public void setPlanosTratamento(List<PlanoTratamento> planosTratamento) {
        this.planosTratamento = planosTratamento;
    }

    public PlanoTratamento[] getPlanosTratamentoSelecionados() {
        return planosTratamentoSelecionados;
    }

    public void setPlanosTratamentoSelecionados(PlanoTratamento[] planosTratamentoSelecionados) {
        this.planosTratamentoSelecionados = planosTratamentoSelecionados;
    }

    public FaturaItem getItemTroca() {
        return itemTroca;
    }

    public void setItemTroca(FaturaItem itemTroca) {
        this.itemTroca = itemTroca;
    }

    public Profissional getProfissionalTroca() {
        return profissionalTroca;
    }

    public void setProfissionalTroca(Profissional profissionalTroca) {
        this.profissionalTroca = profissionalTroca;
    }

    public boolean isFiltroSemRepasse() {
        return filtroSemRepasse;
    }

    public void setFiltroSemRepasse(boolean filtroSemRepasse) {
        this.filtroSemRepasse = filtroSemRepasse;
    }

}
