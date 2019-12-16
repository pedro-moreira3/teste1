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
import org.primefaces.event.TabChangeEvent;

import br.com.lume.common.log.LogIntelidenteSingleton;
import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.lancamento.LancamentoSingleton;
import br.com.lume.odonto.entity.Lancamento;
import br.com.lume.odonto.entity.Profissional;
import br.com.lume.odonto.entity.ReciboRepasseProfissional;
import br.com.lume.odonto.entity.ReciboRepasseProfissional.StatusRecibo;
import br.com.lume.odonto.entity.RepasseFaturasLancamento;
import br.com.lume.planoTratamentoProcedimento.PlanoTratamentoProcedimentoSingleton;
import br.com.lume.profissional.ProfissionalSingleton;
import br.com.lume.repasse.ReciboRepasseProfissionalLancamentoSingleton;
import br.com.lume.repasse.ReciboRepasseProfissionalSingleton;
import br.com.lume.repasse.RepasseFaturasLancamentoSingleton;

@ManagedBean
@ViewScoped
public class ReciboRepasseProfissionalMB extends LumeManagedBean<ReciboRepasseProfissional> {

    private static final long serialVersionUID = 1L;

    private String filtroPeriodoRepasses = "M", filtroPeriodoRecibos = "M", descricao, observacao;
    private Date dataInicioRepasses, dataFimRepasses, dataInicioRecibos, dataFimRecibos;
    boolean lancSemRecibo = true, lancValidadosOnly = false, recibosFindCancelados = false;
    private Profissional profissionalRepasses, profissionalRecibos;
    private List<Lancamento> lancamentos;
    private Lancamento[] lancamentosSelecionados;
    private StatusRecibo filtroStatus;

    private List<Profissional> profissionaisRecibo;
    private HashMap<Profissional, Integer> profissionaisReciboLancamentos;
    private HashMap<Profissional, Double> profissionaisReciboValores;

    //EXPORTAÇÃO TABELA
    private DataTable tabelaLancamentos, tabelaRecibos;

    public ReciboRepasseProfissionalMB() {
        super(ReciboRepasseProfissionalSingleton.getInstance().getBo());
        this.setClazz(ReciboRepasseProfissional.class);
        try {
            setFiltroStatus(StatusRecibo.TODOS);
            actionTrocaDatasCriacaoRepasses();
            actionTrocaDatasCriacaoRecibos();
            pesquisarRepasses();
        } catch (Exception e) {
            LogIntelidenteSingleton.getInstance().makeLog(e);
        }
    }

    public void cancelarRecibo(ReciboRepasseProfissional r) {
        ReciboRepasseProfissionalSingleton.getInstance().inativaRecibo(r, UtilsFrontEnd.getProfissionalLogado());
        addInfo("Sucesso", Mensagens.getMensagemOffLine(Mensagens.REGISTRO_REMOVIDO_COM_SUCESSO));
        pesquisarRecibos();
    }

    public void aprovarRecibo(ReciboRepasseProfissional r) {
        ReciboRepasseProfissionalSingleton.getInstance().aprovarRecibo(r, UtilsFrontEnd.getProfissionalLogado());
        addInfo("Sucesso", Mensagens.getMensagemOffLine(Mensagens.REGISTRO_SALVO_COM_SUCESSO));
        pesquisarRecibos();
    }

    public void onTabChange(TabChangeEvent event) {
        if ("Repasses".equals(event.getTab().getTitle())) {
            pesquisarRepasses();
        } else if ("Histórico de Recibos".equals(event.getTab().getTitle())) {
            pesquisarRecibos();
        }
    }

    public void pesquisarRecibos() {
        try {
            setEntityList(ReciboRepasseProfissionalSingleton.getInstance().getBo().findRecibosFilter(UtilsFrontEnd.getEmpresaLogada(), getProfissionalRecibos(), getDataInicioRecibos(),
                    getDataFimRecibos(), getFiltroStatus(), isRecibosFindCancelados()));
        } catch (Exception e) {
            LogIntelidenteSingleton.getInstance().makeLog(e);
            addError("Erro", Mensagens.getMensagemOffLine(Mensagens.ERRO_AO_BUSCAR_REGISTROS));
        }
    }

    public void pesquisarRepasses() {
        try {
            setLancamentos(LancamentoSingleton.getInstance().getBo().listLancamentosRepasse(UtilsFrontEnd.getEmpresaLogada(), getProfissionalRepasses(), isLancSemRecibo(), isLancValidadosOnly(),
                    getDataInicioRepasses(), getDataFimRepasses()));
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
        } catch (Exception e) {
            LogIntelidenteSingleton.getInstance().makeLog(e);
            addError("Erro", Mensagens.getMensagemOffLine(Mensagens.ERRO_AO_BUSCAR_REGISTROS));
        }
    }

    public Integer getQtdeLancamentosFromProfissional(Profissional profissional) {
        return this.profissionaisReciboLancamentos.get(profissional);
    }

    public Double getValorLancamentosFromProfissional(Profissional profissional) {
        return this.profissionaisReciboValores.get(profissional);
    }

    public void preparaVisualizacao(ReciboRepasseProfissional recibo) {
        setEntity(recibo);
        if (getEntity().getReciboLancamentos() != null && !getEntity().getReciboLancamentos().isEmpty()) {
            getEntity().getReciboLancamentos().forEach(repasseRecibo -> {
                try {
                    RepasseFaturasLancamento repasse = RepasseFaturasLancamentoSingleton.getInstance().getBo().getFaturaRepasseLancamentoFromLancamentoRepasse(repasseRecibo.getLancamento());
                    repasseRecibo.getLancamento().setPtp(PlanoTratamentoProcedimentoSingleton.getInstance().getProcedimentoFromFaturaItem(repasse.getFaturaItem()));
                } catch (Exception e) {
                    repasseRecibo.getLancamento().setPtp(null);
                }
            });
        }
        PrimeFaces.current().executeScript("PF('dlgVisualizarRecibo').show()");
    }

    public void prepararRecibo() {
        this.descricao = null;
        this.observacao = null;

        this.profissionaisReciboLancamentos = new HashMap<>();
        this.profissionaisReciboValores = new HashMap<>();
        this.profissionaisRecibo = new ArrayList<>();

        if (lancamentosSelecionados == null || Arrays.asList(lancamentosSelecionados).size() == 0) {
            addError("Erro", "É necessário escolher ao menos um repasse.");
            return;
        }

        for (int i = 0; i < Arrays.asList(lancamentosSelecionados).size(); i++) {
            Lancamento lancamento = Arrays.asList(lancamentosSelecionados).get(i);
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

    public void cancelarRecibo() {
        setLancamentosSelecionados(new Lancamento[] {});
        PrimeFaces.current().executeScript("PF('dlgGerarRecibo').hide()");
    }

    public void gerarRecibo() {
        try {
            for (Lancamento l : Arrays.asList(lancamentosSelecionados)) {
                if (ReciboRepasseProfissionalLancamentoSingleton.getInstance().getBo().findReciboValidoForLancamento(l) != null) {
                    addError("Erro", "Existem lançamentos na lista de repasse que já estão em outros recibos!");
                    return;
                }
            }

            if (!ReciboRepasseProfissionalSingleton.getInstance().gerarRecibo(Arrays.asList(this.lancamentosSelecionados), this.descricao, this.observacao, UtilsFrontEnd.getProfissionalLogado()))
                throw new Exception();
            pesquisarRepasses();
            setLancamentosSelecionados(new Lancamento[] {});
            PrimeFaces.current().executeScript("PF('dlgGerarRecibo').hide()");
            addInfo("Sucesso", Mensagens.getMensagemOffLine(Mensagens.REGISTRO_SALVO_COM_SUCESSO));
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

    public void actionTrocaDatasCriacaoRecibos() {
        try {
            this.dataInicioRecibos = getDataInicio(filtroPeriodoRecibos);
            this.dataFimRecibos = getDataFim(filtroPeriodoRecibos);
        } catch (Exception e) {
            LogIntelidenteSingleton.getInstance().makeLog(e);
            this.addError("Erro", Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS));
        }
    }

    public void actionTrocaDatasCriacaoRepasses() {
        try {
            this.dataInicioRepasses = getDataInicio(filtroPeriodoRepasses);
            this.dataFimRepasses = getDataFim(filtroPeriodoRepasses);
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

    public List<StatusRecibo> getStatusRecibo() {
        return Arrays.asList(ReciboRepasseProfissional.StatusRecibo.values());
    }

    private Date aplicaHoraInicial(Date data) {
        return aplicaHora(data, 0, 0, 0, 0);
    }

    private Date aplicaHoraFinal(Date data) {
        return aplicaHora(data, 23, 59, 59, 999);
    }

    private Date aplicaHora(Date data, int hora, int minuto, int segundo, int millis) {
        if (data == null)
            return null;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(data);
        calendar.set(Calendar.HOUR, hora);
        calendar.set(Calendar.MINUTE, minuto);
        calendar.set(Calendar.SECOND, segundo);
        calendar.set(Calendar.MILLISECOND, millis);
        return calendar.getTime();
    }

    public boolean isLancSemRecibo() {
        return lancSemRecibo;
    }

    public void setLancSemRecibo(boolean lancSemRecibo) {
        this.lancSemRecibo = lancSemRecibo;
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

    public String getFiltroPeriodoRepasses() {
        return filtroPeriodoRepasses;
    }

    public void setFiltroPeriodoRepasses(String filtroPeriodoRepasses) {
        this.filtroPeriodoRepasses = filtroPeriodoRepasses;
    }

    public String getFiltroPeriodoRecibos() {
        return filtroPeriodoRecibos;
    }

    public void setFiltroPeriodoRecibos(String filtroPeriodoRecibos) {
        this.filtroPeriodoRecibos = filtroPeriodoRecibos;
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

    public Date getDataInicioRepasses() {
        return aplicaHoraInicial(dataInicioRepasses);
    }

    public void setDataInicioRepasses(Date dataInicioRepasses) {
        this.dataInicioRepasses = dataInicioRepasses;
    }

    public Date getDataFimRepasses() {
        return aplicaHoraFinal(dataFimRepasses);
    }

    public void setDataFimRepasses(Date dataFimRepasses) {
        this.dataFimRepasses = dataFimRepasses;
    }

    public Date getDataInicioRecibos() {
        return aplicaHoraInicial(dataInicioRecibos);
    }

    public void setDataInicioRecibos(Date dataInicioRecibos) {
        this.dataInicioRecibos = dataInicioRecibos;
    }

    public Date getDataFimRecibos() {
        return aplicaHoraFinal(dataFimRecibos);
    }

    public void setDataFimRecibos(Date dataFimRecibos) {
        this.dataFimRecibos = dataFimRecibos;
    }

    public Profissional getProfissionalRepasses() {
        return profissionalRepasses;
    }

    public void setProfissionalRepasses(Profissional profissionalRepasses) {
        this.profissionalRepasses = profissionalRepasses;
    }

    public Profissional getProfissionalRecibos() {
        return profissionalRecibos;
    }

    public void setProfissionalRecibos(Profissional profissionalRecibos) {
        this.profissionalRecibos = profissionalRecibos;
    }

    public HashMap<Profissional, Integer> getProfissionaisReciboLancamentos() {
        return profissionaisReciboLancamentos;
    }

    public void setProfissionaisReciboLancamentos(HashMap<Profissional, Integer> profissionaisReciboLancamentos) {
        this.profissionaisReciboLancamentos = profissionaisReciboLancamentos;
    }

    public HashMap<Profissional, Double> getProfissionaisReciboValores() {
        return profissionaisReciboValores;
    }

    public void setProfissionaisReciboValores(HashMap<Profissional, Double> profissionaisReciboValores) {
        this.profissionaisReciboValores = profissionaisReciboValores;
    }

    public void setProfissionaisRecibo(List<Profissional> profissionaisRecibo) {
        this.profissionaisRecibo = profissionaisRecibo;
    }

    public StatusRecibo getFiltroStatus() {
        return filtroStatus;
    }

    public void setFiltroStatus(StatusRecibo filtroStatus) {
        this.filtroStatus = filtroStatus;
    }

    public boolean isRecibosFindCancelados() {
        return recibosFindCancelados;
    }

    public void setRecibosFindCancelados(boolean recibosFindCancelados) {
        this.recibosFindCancelados = recibosFindCancelados;
    }

    public DataTable getTabelaRecibos() {
        return tabelaRecibos;
    }

    public void setTabelaRecibos(DataTable tabelaRecibos) {
        this.tabelaRecibos = tabelaRecibos;
    }

}