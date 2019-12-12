package br.com.lume.odonto.managed;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.primefaces.PrimeFaces;
import org.primefaces.component.datatable.DataTable;

import br.com.lume.common.log.LogIntelidenteSingleton;
import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.lancamento.LancamentoSingleton;
import br.com.lume.odonto.entity.Lancamento;
import br.com.lume.odonto.entity.Profissional;
import br.com.lume.odonto.entity.ReciboRepasseProfissional;
import br.com.lume.odonto.entity.RepasseFaturasLancamento;
import br.com.lume.planoTratamentoProcedimento.PlanoTratamentoProcedimentoSingleton;
import br.com.lume.profissional.ProfissionalSingleton;
import br.com.lume.repasse.ReciboRepasseProfissionalSingleton;
import br.com.lume.repasse.RepasseFaturasLancamentoSingleton;

@ManagedBean
@ViewScoped
public class ReciboRepasseProfissionalMB extends LumeManagedBean<ReciboRepasseProfissional> {

    private static final long serialVersionUID = 1L;

    private String filtroPeriodo = "M", descricao, observacao;
    private Date dataInicio, dataFim;
    boolean lancSemRecibo = true, lancValidadosOnly = false;
    private Profissional profissional;
    private List<Lancamento> lancamentos;
    private Lancamento[] lancamentosSelecionados;

    private List<Profissional> profissionaisRecibo;
    private HashMap<Profissional, Integer> profissionaisReciboLancamentos;
    private HashMap<Profissional, Double> profissionaisReciboValores;

    //EXPORTAÇÃO TABELA
    private DataTable tabelaLancamentos;

    public ReciboRepasseProfissionalMB() {
        super(ReciboRepasseProfissionalSingleton.getInstance().getBo());
        this.setClazz(ReciboRepasseProfissional.class);
        try {
            actionTrocaDatasCriacao();
            pesquisar();
        } catch (Exception e) {
            LogIntelidenteSingleton.getInstance().makeLog(e);
        }
    }

    public void pesquisar() {
        setLancamentos(LancamentoSingleton.getInstance().getBo().listLancamentosRepasse(UtilsFrontEnd.getEmpresaLogada(), this.profissional, this.lancSemRecibo, this.lancValidadosOnly,
                this.dataInicio, this.dataFim));
        if (getLancamentos() != null && !getLancamentos().isEmpty()) {
            getLancamentos().forEach(lancamento -> {
                try {
                    RepasseFaturasLancamento repasse = RepasseFaturasLancamentoSingleton.getInstance().getBo().getFaturaRepasseLancamentoFromLancamentoRepasse(lancamento);
                    lancamento.setPtp(PlanoTratamentoProcedimentoSingleton.getInstance().getProcedimentoFromFaturaItem(repasse.getFaturaItem()));
                } catch (Exception e) {
                    lancamento.setPtp(null);
                }
            });
        }
    }

    public Integer getQtdeLancamentosFromProfissional(Profissional profissional) {
        return this.profissionaisReciboLancamentos.get(profissional);
    }

    public Double getValorLancamentosFromProfissional(Profissional profissional) {
        return this.profissionaisReciboValores.get(profissional);
    }

    public void prepararRecibo() {
        this.profissionaisReciboLancamentos = new HashMap<>();
        this.profissionaisReciboValores = new HashMap<>();
        this.profissionaisRecibo = new ArrayList<>();

        for (int i = 0; i < lancamentos.size(); i++) {
            Lancamento lancamento = lancamentos.get(i);
            Profissional donoLancamento = lancamento.getFatura().getProfissional();
            Double valor = (lancamento.getValorComDesconto() == null || lancamento.getValorComDesconto().doubleValue() == 0d ? lancamento.getValor().doubleValue() : lancamento.getValorComDesconto().doubleValue());
            if (getProfissionaisRecibo() == null || getProfissionaisRecibo().indexOf(donoLancamento) < 0) {
                this.profissionaisReciboLancamentos.put(donoLancamento, new Integer(1));
                this.profissionaisReciboValores.put(donoLancamento, valor);
                this.profissionaisRecibo.add(donoLancamento);
            } else {
                this.profissionaisReciboLancamentos.put(donoLancamento, this.profissionaisReciboLancamentos.get(donoLancamento) + 1);
                this.profissionaisReciboValores.put(donoLancamento, this.profissionaisReciboValores.get(donoLancamento) + valor);
            }
        }
        PrimeFaces.current().executeScript("PF('dlgGerarRecibo').show()");
    }

    public void gerarRecibo() {
        try {
            if (!ReciboRepasseProfissionalSingleton.getInstance().gerarRecibo(Arrays.asList(this.lancamentosSelecionados), this.descricao, this.observacao))
                throw new Exception();
            addInfo("Sucesso", Mensagens.getMensagemOffLine(Mensagens.ERRO_AO_SALVAR_REGISTRO));
        } catch (Exception e) {
            addError("Erro", Mensagens.getMensagemOffLine(Mensagens.ERRO_AO_SALVAR_REGISTRO));
        }
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
            this.addError("Erro", Mensagens.ERRO_AO_BUSCAR_REGISTROS, true);
        }
        return sugestoes;
    }

    public void actionTrocaDatasCriacao() {
        try {
            this.dataInicio = getDataInicio(filtroPeriodo);
            this.dataFim = getDataFim(filtroPeriodo);
        } catch (Exception e) {
            LogIntelidenteSingleton.getInstance().makeLog(e);
            this.addError("Erro", Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS));
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
            LogIntelidenteSingleton.getInstance().makeLog(e);
            this.addError("Erro", Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS));
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
            LogIntelidenteSingleton.getInstance().makeLog(e);
            this.addError("Erro", Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS));
            return null;
        }
    }

    public boolean isLancSemRecibo() {
        return lancSemRecibo;
    }

    public void setLancSemRecibo(boolean lancSemRecibo) {
        this.lancSemRecibo = lancSemRecibo;
    }

    public Profissional getProfissional() {
        return profissional;
    }

    public void setProfissional(Profissional profissional) {
        this.profissional = profissional;
    }

    public Lancamento[] getLancamentosSelecionados() {
        return lancamentosSelecionados;
    }

    public void setLancamentosSelecionados(Lancamento[] lancamentosSelecionados) {
        this.lancamentosSelecionados = lancamentosSelecionados;
    }

    public DataTable getTabelaLancamentos() {
        return tabelaLancamentos;
    }

    public void setTabelaLancamentos(DataTable tabelaLancamentos) {
        this.tabelaLancamentos = tabelaLancamentos;
    }

    public List<Lancamento> getLancamentos() {
        return lancamentos;
    }

    public void setLancamentos(List<Lancamento> lancamentos) {
        this.lancamentos = lancamentos;
    }

    public String getFiltroPeriodo() {
        return filtroPeriodo;
    }

    public void setFiltroPeriodo(String filtroPeriodo) {
        this.filtroPeriodo = filtroPeriodo;
    }

    public Date getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(Date dataInicio) {
        this.dataInicio = dataInicio;
    }

    public Date getDataFim() {
        return dataFim;
    }

    public void setDataFim(Date dataFim) {
        this.dataFim = dataFim;
    }

    public boolean isLancValidadosOnly() {
        return lancValidadosOnly;
    }

    public void setLancValidadosOnly(boolean lancValidadosOnly) {
        this.lancValidadosOnly = lancValidadosOnly;
    }

    public List<Profissional> getProfissionaisRecibo() {
        return this.profissionaisRecibo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }

}
