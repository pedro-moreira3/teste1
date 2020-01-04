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
import org.primefaces.component.datatable.DataTable;

import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.Utils;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.odonto.entity.PlanoTratamentoProcedimento;
import br.com.lume.odonto.entity.Profissional;
import br.com.lume.odonto.entity.RelatorioRepasse;
import br.com.lume.planoTratamentoProcedimento.PlanoTratamentoProcedimentoSingleton;
import br.com.lume.profissional.ProfissionalSingleton;
import br.com.lume.relatorioRepasse.RelatorioRepasseSingleton;

@ManagedBean
@ViewScoped
public class RelatorioRepasseMB extends LumeManagedBean<RelatorioRepasse> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(RelatorioRepasseMB.class);

    private Date inicio, fim;

    private List<PlanoTratamentoProcedimento> planoTratamentoProcedimentos;

    private Profissional profissional;

    private String statusPagamento;

    //EXPORTAÇÃO TABELA
    private DataTable tabelaRelatorio;

    public RelatorioRepasseMB() {
        super(RelatorioRepasseSingleton.getInstance().getBo());
        this.setClazz(RelatorioRepasse.class);
        this.inicio = Utils.getPrimeiroDiaMesCorrente();
        this.fim = Calendar.getInstance().getTime();
    }

    public void actionCarregarProcedimentos() {
        try {
            this.planoTratamentoProcedimentos = PlanoTratamentoProcedimentoSingleton.getInstance().getBo().listByRelatoriosBalanco(this.inicio, this.fim, this.profissional, this.statusPagamento);
        } catch (Exception e) {
            this.log.error("Erro no carregarProcedimentos : ", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
        }
    }

    public List<Profissional> geraSugestoesProfissional(String query) {
        List<Profissional> sugestoes = new ArrayList<>();
        List<Profissional> profissionais = new ArrayList<>();
        try {
            profissionais = ProfissionalSingleton.getInstance().getBo().listByEmpresa(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
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

    public void exportarTabela(String type) {
        exportarTabela("Relatório de repasses", tabelaRelatorio, type);
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

    public Profissional getProfissional() {
        return this.profissional;
    }

    public void setProfissional(Profissional profissional) {
        this.profissional = profissional;
    }

    public String getStatusPagamento() {
        return this.statusPagamento;
    }

    public void setStatusPagamento(String statusPagamento) {
        this.statusPagamento = statusPagamento;
    }

    public List<PlanoTratamentoProcedimento> getPlanoTratamentoProcedimentos() {
        return planoTratamentoProcedimentos;
    }

    public void setPlanoTratamentoProcedimentos(List<PlanoTratamentoProcedimento> planoTratamentoProcedimentos) {
        this.planoTratamentoProcedimentos = planoTratamentoProcedimentos;
    }

    public DataTable getTabelaRelatorio() {
        return tabelaRelatorio;
    }

    public void setTabelaRelatorio(DataTable tabelaRelatorio) {
        this.tabelaRelatorio = tabelaRelatorio;
    }
}
