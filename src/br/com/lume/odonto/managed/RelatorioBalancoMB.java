package br.com.lume.odonto.managed;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.apache.log4j.Logger;

import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.Utils;
import br.com.lume.odonto.bo.PlanoTratamentoProcedimentoBO;
import br.com.lume.odonto.bo.ProfissionalBO;
import br.com.lume.odonto.bo.RelatorioBalancoBO;
import br.com.lume.odonto.entity.PlanoTratamentoProcedimento;
import br.com.lume.odonto.entity.Profissional;
import br.com.lume.odonto.entity.RelatorioBalanco;

@ManagedBean
@ViewScoped
public class RelatorioBalancoMB extends LumeManagedBean<RelatorioBalanco> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(RelatorioBalancoMB.class);

    private Date inicio, fim;

    private boolean finalizado, repassado;

    private List<PlanoTratamentoProcedimento> planoTratamentoProcedimentos;

    private PlanoTratamentoProcedimentoBO planoTratamentoProcedimentoBO = new PlanoTratamentoProcedimentoBO();

    private Profissional profissional;

    private ProfissionalBO profissionalBO = new ProfissionalBO();

    private List<RelatorioBalanco> relatoriosSelecionados;

    private String statusPagamento;

    public RelatorioBalancoMB() {
        super(new RelatorioBalancoBO());
        this.setClazz(RelatorioBalanco.class);
        this.inicio = Utils.getPrimeiroDiaMesCorrente();
        this.fim = Calendar.getInstance().getTime();
    }

    public void actionCarregarProcedimentos() {
        try {
            this.planoTratamentoProcedimentos = this.planoTratamentoProcedimentoBO.listByRelatoriosBalanco(this.inicio, this.fim, this.profissional, this.statusPagamento);
        } catch (Exception e) {
            this.log.error("Erro no carregarProcedimentos : ", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
        }
    }

    public List<Profissional> geraSugestoesProfissional(String query) {
        List<Profissional> sugestoes = new ArrayList<>();
        List<Profissional> profissionais = new ArrayList<>();
        try {
            profissionais = this.profissionalBO.listByEmpresa();
            for (Profissional p : profissionais) {
                if (Normalizer.normalize(p.getDadosBasico().getNome().toLowerCase(), Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "").toLowerCase().contains(
                        Normalizer.normalize(query, Normalizer.Form.NFD).toLowerCase())) {
                    sugestoes.add(p);
                }
            }
            Collections.sort(sugestoes);
        } catch (Exception e) {
            this.log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
            this.addError(Mensagens.ERRO_AO_BUSCAR_REGISTROS, "");
        }
        return sugestoes;
    }

    public void actionLimpar() {
        this.profissional = new Profissional();
        this.inicio = null;
        this.fim = null;
        this.setEntityList(null);
        this.setPlanoTratamentoProcedimentos(null);
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

    public boolean isFinalizado() {
        return this.finalizado;
    }

    public void setFinalizado(boolean finalizado) {
        this.finalizado = finalizado;
    }

    public boolean isRepassado() {
        return this.repassado;
    }

    public void setRepassado(boolean repassado) {
        this.repassado = repassado;
    }

    public List<PlanoTratamentoProcedimento> getPlanoTratamentoProcedimentos() {
        return this.planoTratamentoProcedimentos;
    }

    public void setPlanoTratamentoProcedimentos(List<PlanoTratamentoProcedimento> planoTratamentoProcedimentos) {
        this.planoTratamentoProcedimentos = planoTratamentoProcedimentos;
    }

    public Profissional getProfissional() {
        return this.profissional;
    }

    public void setProfissional(Profissional profissional) {
        this.profissional = profissional;
    }

    public List<RelatorioBalanco> getRelatoriosSelecionados() {
        return this.relatoriosSelecionados;
    }

    public void setRelatoriosSelecionados(List<RelatorioBalanco> relatoriosSelecionados) {
        this.relatoriosSelecionados = relatoriosSelecionados;
    }

    public String getStatusPagamento() {
        return this.statusPagamento;
    }

    public void setStatusPagamento(String statusPagamento) {
        this.statusPagamento = statusPagamento;
    }
}
