package br.com.lume.odonto.managed;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.component.UIData;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;
import javax.faces.model.SelectItemGroup;

import org.apache.log4j.Logger;
import org.primefaces.PrimeFaces;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.model.DualListModel;

import br.com.lume.agendamentoPlanoTratamentoProcedimento.AgendamentoPlanoTratamentoProcedimentoSingleton;
import br.com.lume.common.OdontoPerfil;
import br.com.lume.common.exception.business.CancelarItemProcedimentoAtivo;
import br.com.lume.common.exception.business.CancelarItemProcedimentoExecutado;
import br.com.lume.common.exception.business.FaturaIrregular;
import br.com.lume.common.exception.business.RepasseNaoPossuiRecebimentoException;
import br.com.lume.common.lazy.models.PlanoTratamentoLazyModel;
import br.com.lume.common.log.LogIntelidenteSingleton;
import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.FormaPagamento;
import br.com.lume.common.util.JSFHelper;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.Status;
import br.com.lume.common.util.Utils;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.convenio.ConvenioSingleton;
import br.com.lume.convenioProcedimento.ConvenioProcedimentoSingleton;
import br.com.lume.dente.DenteSingleton;
import br.com.lume.descontoOrcamento.DescontoOrcamentoSingleton;
import br.com.lume.dominio.DominioSingleton;
import br.com.lume.evolucao.EvolucaoSingleton;
import br.com.lume.faturamento.FaturaItemSingleton;
import br.com.lume.faturamento.FaturaSingleton;
import br.com.lume.lancamento.LancamentoSingleton;
import br.com.lume.negociacao.NegociacaoOrcamentoSingleton;
import br.com.lume.odonto.entity.AgendamentoPlanoTratamentoProcedimento;
import br.com.lume.odonto.entity.Convenio;
import br.com.lume.odonto.entity.ConvenioProcedimento;
import br.com.lume.odonto.entity.Dente;
import br.com.lume.odonto.entity.Desconto;
import br.com.lume.odonto.entity.DescontoOrcamento;
import br.com.lume.odonto.entity.Dominio;
import br.com.lume.odonto.entity.Fatura;
import br.com.lume.odonto.entity.FaturaItem;
import br.com.lume.odonto.entity.Lancamento;
import br.com.lume.odonto.entity.NegociacaoOrcamento;
import br.com.lume.odonto.entity.Odontograma;
import br.com.lume.odonto.entity.Orcamento;
import br.com.lume.odonto.entity.OrcamentoItem;
import br.com.lume.odonto.entity.OrcamentoPlanejamento;
import br.com.lume.odonto.entity.Paciente;
import br.com.lume.odonto.entity.PlanoTratamento;
import br.com.lume.odonto.entity.PlanoTratamentoProcedimento;
import br.com.lume.odonto.entity.PlanoTratamentoProcedimentoFace;
import br.com.lume.odonto.entity.PlanoTratamentoProcedimentoRegiao;
import br.com.lume.odonto.entity.Procedimento;
import br.com.lume.odonto.entity.Profissional;
import br.com.lume.odonto.entity.RegiaoDente;
import br.com.lume.odonto.entity.RegiaoRegiao;
import br.com.lume.odonto.entity.RepasseFaturas;
import br.com.lume.odonto.entity.RepasseFaturasItem;
import br.com.lume.odonto.entity.Retorno;
import br.com.lume.odonto.entity.StatusDente;
import br.com.lume.odonto.entity.Tarifa;
import br.com.lume.odonto.entity.Fatura.SubStatusFatura;
import br.com.lume.odonto.entity.PlanoTratamentoProcedimentoRegiao.TipoRegiao;
import br.com.lume.odonto.util.OdontoMensagens;
import br.com.lume.odontograma.OdontogramaSingleton;
import br.com.lume.orcamento.OrcamentoSingleton;
import br.com.lume.paciente.PacienteSingleton;
import br.com.lume.planoTratamento.PlanoTratamentoSingleton;
import br.com.lume.planoTratamento.bo.PlanoTratamentoBO;
import br.com.lume.planoTratamentoProcedimento.PlanoTratamentoProcedimentoRegiaoSingleton;
import br.com.lume.planoTratamentoProcedimento.PlanoTratamentoProcedimentoSingleton;
import br.com.lume.planoTratamentoProcedimento.bo.PlanoTratamentoProcedimentoBO;
import br.com.lume.procedimento.ProcedimentoSingleton;
import br.com.lume.profissional.ProfissionalSingleton;
import br.com.lume.regiaoDente.RegiaoDenteSingleton;
import br.com.lume.regiaoRegiao.RegiaoRegiaoSingleton;
import br.com.lume.repasse.RepasseFaturasItemSingleton;
import br.com.lume.repasse.RepasseFaturasSingleton;
import br.com.lume.retorno.RetornoSingleton;
import br.com.lume.security.entity.Empresa;
import br.com.lume.statusDente.StatusDenteSingleton;
import br.com.lume.tarifa.TarifaSingleton;

@ManagedBean
@ViewScoped
public class PlanoTratamentoMB extends LumeManagedBean<PlanoTratamento> {

    private static final long serialVersionUID = 4784490064723477535L;
    private Logger log = Logger.getLogger(PlanoTratamentoMB.class);

    private Date retorno;
    private String observacoesRetorno;

    private Dominio justificativa;
    private List<Dominio> justificativasCancelamento;
    private String justificativaCancelamento;

    private List<Profissional> profissionais;

    private List<PlanoTratamento> planosTratamento;
    private PlanoTratamentoLazyModel ptLazyModel;
    private List<PlanoTratamentoProcedimento> planoTratamentoProcedimentos;
    private List<PlanoTratamentoProcedimento> planoTratamentoProcedimentosExcluidos;

    private List<Odontograma> odontogramas;

    /// ORCAMENTO ///

    private List<Orcamento> orcamentos;
    private Orcamento orcamentoSelecionado;
    private OrcamentoPlanejamento planejamentoAtual;
    private boolean showProcedimentosCancelados;
    private Orcamento orcamentoCancelamento;
    private List<Orcamento> orcamentosPendentes;
    private List<Orcamento> orcamentosParaReprovar;

    // Alteracao Data Aprovacao
    private Orcamento orcamentoDataAprovacao;
    private Date novaDataAprovacao;

    private List<Dominio> formasPagamentoNewPlanejamento;
    private List<Tarifa> tarifasNewPlanejamento;
    private List<Integer> parcelasNewPlanejamento;
    private List<OrcamentoItem> procedimentosOrcSelecionado;

    private BigDecimal valorDescontoAlterar;

    private String nomeClinica;
    private String endTelefoneClinica;

    private List<SelectItem> dentes = new ArrayList<SelectItem>();

    private PlanoTratamentoProcedimento planoTratamentoProcedimentoSelecionado = new PlanoTratamentoProcedimento();
    private Procedimento procedimentoSelecionado;

    private List<Dominio> justificativas;

    private boolean imprimirSemValores = false;

    private boolean incluirObservacoesCobranca = false;

    private boolean omitirProcedimentosNaoInclusos = false;

    private boolean omitirDadosEmpresa = false;

    private boolean omitirLogo = false;

    private boolean encerrarPlano;

    private String filtroStatus = "T";

    private String filtroStatusProcedimento = "N";

    private String filtroTipo = "T";

    private String mensagemErroExclusaoProcedimento;

    private DualListModel<PlanoTratamentoProcedimento> ptProcedimentosDisponiveis = new DualListModel<>();

    private boolean novoPtDialogAberto = false;

    //Dentes
    private List<Integer> grupoDente1 = new ArrayList<>();
    private List<Integer> grupoDente2 = new ArrayList<>();
    private List<Integer> grupoDente3 = new ArrayList<>();
    private List<Integer> grupoDente4 = new ArrayList<>();
    private List<Integer> grupoDente5 = new ArrayList<>();
    private List<Integer> grupoDente6 = new ArrayList<>();
    private List<Integer> grupoDente7 = new ArrayList<>();
    private List<Integer> grupoDente8 = new ArrayList<>();

    private List<String> regioesPossiveisOdontograma;
    private List<String> regioesEscolhidasOdontograma;
    private List<String> dentesPossiveisOdontograma;
    private List<String> dentesEscolhidasOdontograma;

    //Regiao/Dente Procedimento
    private boolean enableRegioes = false;
    private Dente denteSelecionado;
    private List<PlanoTratamentoProcedimento> procedimentosDente;
    private String regiaoSelecionada;

    // Regi??es/Dentes em Detalhes e Diagnostico do PTP
    private List<Dente> dentesSelecionados;
    private List<String> regioesSelecionadas;
    private List<String> dentesRegioesEscolhidas;
    private List<String> facesEscolhidas;
    private List<StatusDente> diagnosticosSelecionados;
    private List<PlanoTratamentoProcedimentoRegiao> diagnosticos;
    private PlanoTratamentoProcedimentoRegiao ptprSelecionado;
    private List<PlanoTratamentoProcedimento> procedimentosDiferentes;

    //Personalizar
    private List<StatusDente> statusDente;
    private List<StatusDente> statusDenteEmpresa;
    private StatusDente statusDenteSelecionado = new StatusDente();
    private StatusDente statusDenteDenteSelecionado = new StatusDente();

    //EXPORTACAO TABELA
    private DataTable tabelaPlanoTratamento;

    private Odontograma odontogramaSelecionado;
    private List<PlanoTratamentoProcedimento> ptps2Finalizar;
    private String descricaoEvolucao, denteRegiaoEscolhida;

    @ManagedProperty(value = "#{pacienteMB}")
    private PacienteMB pacienteMB;
    private boolean renderizarValoresProcedimentos = true;
    //private boolean renderizarObservacoesCobranca = false;
    private List<Profissional> profissionaisFinalizarNovamente;
    private Profissional profissionalFinalizarNovamente;

    private PlanoTratamentoProcedimento ptpMudarExecutor;

    private String observacoesCobrancaOrcamento;

    private Integer quantidadeVezesNegociacaoOrcamento;
    private List<Integer> parcelasDisponiveis;
    private BigDecimal valorPrimeiraParcelaOrcamento = new BigDecimal(0);
    private Map<Integer, DescontoOrcamento> descontosDisponiveis = new HashMap<Integer, DescontoOrcamento>();
    private String mensagemCalculoOrcamento = "";
    private BigDecimal numeroParcelaOrcamento = new BigDecimal(0);

    // private String mensagemCalculoOrcamentoDiferenca;
    private BigDecimal valorParcela;
    private BigDecimal diferencaCalculoParcelas = new BigDecimal(0);

    private List<Convenio> conveniosDisponiveis;

    private Boolean ptpInserirMuitasVezes;
    private Integer ptpInserirQuantasVezes;

    private String motivoReativacao;

    private BigDecimal valorProc = new BigDecimal(0);

    private String observacoesDetalhes;

    private String observacoesAdicionarProcedimento;
    private RegiaoDente regiaSomenteFaces;

    public PlanoTratamentoMB() {
        super(PlanoTratamentoSingleton.getInstance().getBo());
        setClazz(PlanoTratamento.class);
        if (UtilsFrontEnd.getProfissionalLogado() != null) {
            try {
                setFormasPagamentoNewPlanejamento(DominioSingleton.getInstance().getBo().listByEmpresaAndObjetoAndTipo("pagamento", "forma"));
                justificativas = DominioSingleton.getInstance().getBo().listByEmpresaAndObjetoAndTipo("planotratamento", "justificativa");
                atualizaTela();

                justificativasCancelamento = DominioSingleton.getInstance().getBo().listByEmpresaAndObjetoAndTipo("planotratamentoprocedimento", "justificativa");
            } catch (Exception e) {
                e.printStackTrace();
                log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
                addError(Mensagens.ERRO_AO_BUSCAR_REGISTROS, "");
            }

            loadDentesCombo();
            loadDentesERegioesOdontograma();
            loadGruposDentes();
            carregarStatusDente();
        }
    }

    public void actionNaoAprovaOrcamento() {

    }

    public void imprimirSemValoresListener() {
        renderizarValoresProcedimentos = true;
        if (imprimirSemValores) {
            renderizarValoresProcedimentos = false;
        }
    }

    public void setVideos() {
        getListaVideosTutorial().clear();
        getListaVideosTutorial().put("Como utilizar o plano de tratamento", "https://www.youtube.com/v/SFJ3XrvCbt4?autoplay=1");
        getListaVideosTutorial().put("Encerrar e reativar planos de tratamento", "https://www.youtube.com/v/fIzAePpEQQs?autoplay=1");
    }

    public void setVideosOrcamento() {
        getListaVideosTutorial().clear();
        getListaVideosTutorial().put("Como gerar um or??amento", "https://www.youtube.com/v/MWZH_lOR6xI?autoplay=1");
        getListaVideosTutorial().put("Realizando negocia????es", "https://www.youtube.com/v/3dPXE4cXt6M?autoplay=1");
    }

    //  public void incluirObservacoesCobrancaListener() {
    //   renderizarObservacoesCobranca = false;
    //    if (incluirObservacoesCobranca) {
    //        renderizarObservacoesCobranca = true;
    //      }
    //   }

    public BigDecimal valorTotal(PlanoTratamento pt) {
        BigDecimal total = new BigDecimal(0);
        for (PlanoTratamentoProcedimento ptp : pt.getPlanoTratamentoProcedimentos()) {
            FaturaItem faturaItem = FaturaItemSingleton.getInstance().getBo().faturaItensOrigemFromPTP(ptp);
            if (faturaItem != null) {
                total = FaturaSingleton.getInstance().getTotal(faturaItem.getFatura());
            }
        }
        return total;
    }

    public String getDateNowFormat() {
        return Utils.dateToString(new Date());
    }

    // ========================================= PLANO DE TRATAMENTO ========================================= //
    @Override
    public void actionPersist(ActionEvent event) {
        try {
            if (getEntity().getPlanoTratamentoProcedimentos() != null)
                for (PlanoTratamentoProcedimento ptp : getEntity().getPlanoTratamentoProcedimentos())
                    if (!validarAlteracao(ptp))
                        return;

            if (getEntity().getId() != null && getEntity().getId().longValue() != 0) {

                if (getPaciente() == null) {
                    //   setaFinalizacoesProcedimentos();
                    addError(OdontoMensagens.getMensagem("plano.paciente.vazio"), "");
                    return;
                }
                if (getEntity().getDescricao() == null) {
                    addError(OdontoMensagens.getMensagem("erro.plano.descricao.vazio"), "");
                    return;
                }
                if (planoTratamentoProcedimentos == null || planoTratamentoProcedimentos.isEmpty()) {
                    //setaFinalizacoesProcedimentos();
                    //addError(OdontoMensagens.getMensagem("plano.procedimentos.vazio"), "");
                    //return;
                }
                if (!verificaAgendamento()) {
                    log.error(OdontoMensagens.getMensagem("erro.plano.utilizado"));
                    //   setaFinalizacoesProcedimentos();
                    return;
                }

                for (PlanoTratamentoProcedimento ptp : planoTratamentoProcedimentos) {
                    if (ptp.getValorAnterior() != null && ptp.getValorDesconto() != null && ptp.getValorAnterior().doubleValue() != ptp.getValorDesconto().doubleValue()) {
                        getEntity().setAlterouvaloresdesconto(true);
                    }
                }

                setPlanoTratamentoProcedimentosEntity(planoTratamentoProcedimentos, planoTratamentoProcedimentosExcluidos);
                if (finalizaProcedimento()) {
                    for (PlanoTratamentoProcedimento ptp : planoTratamentoProcedimentos) {
                        PlanoTratamentoProcedimentoSingleton.getInstance().getBo().persist(ptp);
                    }
                    for (PlanoTratamentoProcedimento ptp : planoTratamentoProcedimentosExcluidos) {
                        PlanoTratamentoProcedimentoSingleton.getInstance().getBo().persist(ptp);
                    }
                    PlanoTratamentoSingleton.getInstance().getBo().persist(getEntity());
                    validaRepasse();
                    addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");

                    PrimeFaces.current().executeScript("PF('dlgEditaPlanoTratamento').hide()");
                }
            } else {
                getEntity().setPaciente(getPaciente());
                getEntity().setProfissional(UtilsFrontEnd.getProfissionalLogado());
                if (getEntity().isBconvenio() && getPaciente().getConvenio() != null) {
                    getEntity().setConvenio(getPaciente().getConvenio());
                }
                getEntity().setValorTotal(new BigDecimal(0));
                getEntity().setValorTotalDesconto(new BigDecimal(0));

                Odontograma recente = OdontogramaSingleton.getInstance().odontogramaRecente(this.getPaciente());
                if (recente != null) {
                    /*
                     * for (Dente dente : recente.getDentes()) { dente.setId(0l); dente.setOdontograma(recente); for (RegiaoDente regiao : dente.getRegioes()) { regiao.setId(0l);
                     * regiao.setDente(dente); } } for (RegiaoRegiao regiao : recente.getRegioesRegiao()) { regiao.setId(0l); regiao.setOdontograma(recente); }
                     */
                    recente.setRegioesRegiao(null);
                    recente.setDentes(null);
                    recente.setId(0l);
                    recente.setDataCadastro(new Date());
                    OdontogramaSingleton.getInstance().getBo().persist(recente);
                    recente = OdontogramaSingleton.getInstance().getBo().find(recente);
                    getEntity().setOdontograma(recente);
                } else {
                    Odontograma odontograma = new Odontograma(Calendar.getInstance().getTime(), this.getPaciente(), "");
                    OdontogramaSingleton.getInstance().getBo().persist(odontograma);
                    getEntity().setOdontograma(odontograma);
                }

                PlanoTratamentoSingleton.getInstance().getBo().persist(getEntity());

                addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");
                PrimeFaces.current().executeScript("PF('dlgPt').hide()");
                fechaNovoPtDialog();

                setEntity(PlanoTratamentoSingleton.getInstance().getBo().find(getEntity()));
                carregaDlgProcedimentos(getEntity());
                PrimeFaces.current().executeScript("PF('dlgViewPlanoTratamento').show()");
            }

            if (getEntity().getConvenio() != null && getEntity().getPaciente() != null) {
                getEntity().getPaciente().setConvenio(getEntity().getConvenio());
                PacienteSingleton.getInstance().getBo().persist(getEntity().getPaciente());
            }

            carregarPlanosTratamento();
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Erro no actionPersist", e);
            addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), e.getMessage());
        }
    }

    @Override
    public void actionNew(ActionEvent event) {
        planoTratamentoProcedimentos = new ArrayList<>();
        planoTratamentoProcedimentosExcluidos = new ArrayList<>();
        setEntity(null);
        carregarPlanosTratamento();
        retorno = null;
        observacoesRetorno = null;
        if (!this.getEntity().isBconvenio())
            this.getEntity().setBconvenio(true);
        abreNovoPtDialog();
        carregaConvenios();
        getEntity().setDescricao("PT " + Utils.dateToStringSomenteDataBrasil(new Date()));
    }

    public boolean validaPermissaoExclusaoPTP(PlanoTratamentoProcedimento ptp) {
        if (OrcamentoSingleton.getInstance().isProcedimentoTemOrcamentoAprovado(ptp))
            return true;
        return false;
    }

//    public boolean validaPermissao(PlanoTratamento pt) {
//        List<Orcamento> orcs = OrcamentoSingleton.getInstance().getBo().findOrcamentosAtivosByPT(pt);
//        for (Orcamento o : orcs) {
//            if (o.isAprovado())
//                return true;
//        }
//        return false;
//    }

    public void carregarPlanosTratamento() {
        try {
            planosTratamento = new ArrayList<>();
            if (getPaciente() != null) {
                //planosTratamento = PlanoTratamentoSingleton.getInstance().getBo().listByPacienteAndStatusAndTipo(getPaciente(), filtroStatus, filtroTipo);
                planosTratamento = PlanoTratamentoSingleton.getInstance().getBo().listPlanosByPacienteAndStatusAndTipo(getPaciente(), filtroStatus, filtroTipo);
                ptLazyModel = new PlanoTratamentoLazyModel(planosTratamento);
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogIntelidenteSingleton.getInstance().makeLog(e);
            this.addError("", Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS));
        }
    }

    public void carregaConvenios() {
        try {
            if (conveniosDisponiveis == null || conveniosDisponiveis.isEmpty())
                conveniosDisponiveis = ConvenioSingleton.getInstance().getBo().listByEmpresa(UtilsFrontEnd.getEmpresaLogada().getEmpIntCod());
        } catch (Exception e) {
            log.error(e);
        }
    }

    public boolean temProcedimentosAbertos() throws Exception {
        if (this.planoTratamentoProcedimentos == null)
            return false;
        for (PlanoTratamentoProcedimento ptp : this.planoTratamentoProcedimentos)
            if ("N".equals(ptp.getExcluido()) && !"F".equals(ptp.getStatus()))
                return true;
        return false;
    }

    public void actionFinalizar(PlanoTratamento planoTratamento) throws Exception {
        if (planoTratamento != null) {
            setEntity(planoTratamento);
            //carregarPlanoTratamentoProcedimentos();
        }

        actionFinalizar((ActionEvent) null);
    }

    public void actionReativar(PlanoTratamento planoTratamento) throws Exception {
        if (planoTratamento != null) {
            setEntity(planoTratamento);
            PrimeFaces.current().executeScript("PF('reativar').show()");
        }
    }

    public void setEntityFromRelatorio(PlanoTratamento planoTratamento) {
        try {
            if (this.pacienteMB == null) {
                this.pacienteMB = new PacienteMB();
            }
            this.pacienteMB.setEntity(planoTratamento.getPaciente());

            this.actionFinalizar(planoTratamento);
        } catch (Exception e) {
            this.addError("Erro", "Erro ao encerrar.");
            e.printStackTrace();
        }
    }

    public void actionReativar(ActionEvent event) {
        try {
            getEntity().setJustificativaReativacao(this.motivoReativacao);
            getEntity().setStatus("N");
            PlanoTratamentoSingleton.getInstance().getBo().persist(getEntity());
            PrimeFaces.current().executeScript("PF('reativar').hide()");
            this.addInfo("Sucesso", Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO));
            this.motivoReativacao = null;
        } catch (Exception e) {
            this.addError("Erro", Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), true);
            e.printStackTrace();
        }

    }

    public void actionSuspender(ActionEvent event) {
        boolean retorno = false;
        retorno = PlanoTratamentoSingleton.getInstance().suspenderPlanoTratamento(getEntity(), profissionalFinalizarNovamente);

        if (retorno)
            this.addInfo("Sucesso", Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO));
        else
            this.addError("Erro", Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), true);

    }

    public void actionExecutarPlanoTratamento(PlanoTratamento planoTratamento) {
        try {
            if (!temProcedimentosAbertos()) {
                PlanoTratamentoSingleton.getInstance().executarPlanoTratamento(planoTratamento, UtilsFrontEnd.getProfissionalLogado());
                this.addInfo("Sucesso", Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO));
                atualizaTela();
            }
        } catch (Exception e) {
            e.printStackTrace();
            this.addError("Erro", Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), true);
        }
    }

    public void actionFinalizar(ActionEvent event) {
        try {
            List<Fatura> faturas = FaturaSingleton.getInstance().getBo().getFaturasFromPT(getEntity());

            if (faturas != null && !faturas.isEmpty()) {
                for (Fatura f : faturas)
                    FaturaSingleton.getInstance().permiteRegularizacaoFatura(f);
            }

            if (this.planoTratamentoProcedimentos == null || this.planoTratamentoProcedimentos.isEmpty() || temProcedimentosAbertos()) {
                if (this.justificativa == null) {
                    PrimeFaces.current().executeScript("PF('devolver').show()");
                    return;
                }

                this.criaRetorno();
                PrimeFaces.current().executeScript("PF('devolver').hide()");
            }
            PlanoTratamentoSingleton.getInstance().encerrarPlanoTratamento(getEntity(), this.justificativa, UtilsFrontEnd.getProfissionalLogado());

            if (faturas != null && !faturas.isEmpty())
                FaturaSingleton.getInstance().regularizarFatura(faturas, UtilsFrontEnd.getProfissionalLogado());

            this.addInfo("Sucesso", Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO));
            atualizaTela();
        } catch (CancelarItemProcedimentoExecutado e) {
            System.out.println(e.getMessage());
            this.addError("Falha ao cancelar item de fatura", "N??o ?? poss??vel cancelar um item referente a um procedimento executado.");
        } catch (CancelarItemProcedimentoAtivo e) {
            System.out.println(e.getMessage());
            this.addError("Falha ao cancelar item de fatura", "N??o ?? poss??vel cancelar um item referente a um procedimento ativo, deve estar cancelado.");
        } catch (FaturaIrregular e) {
            System.out.println(e.getMessage());
            this.addError("Pend??ncia financeira", "?? necess??rio regularizar as faturas originadas por esse plano de tratamento.");
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Erro no actionFinalizar", e);
            this.addError("Erro", Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), true);
        } finally {
            this.encerrarPlano = false;
            this.justificativa = null;
        }
    }

    public void actionCancelFinalizar() {
        this.justificativa = null;
        PrimeFaces.current().executeScript("PF('devolver').hide()");
    }

    public void actionCancelReativar() {
        this.motivoReativacao = null;
        PrimeFaces.current().executeScript("PF('reativar').hide()");
    }

    public void actionPTInicial() {
        try {
            PlanoTratamento pt = PlanoTratamentoSingleton.getInstance().getBo().persistPlano(getPaciente(), UtilsFrontEnd.getProfissionalLogado());
            if (pt != null) {
                setEntity(PlanoTratamentoSingleton.getInstance().getBo().find(pt.getId()));
                atualizaTela();
                this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Erro no actionPTInicial", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
        }
    }

    public boolean setaFinalizacoesProcedimentos() {
        for (PlanoTratamentoProcedimento ptp : planoTratamentoProcedimentos) {
            if (((ptp.getStatus() != null) && ptp.getStatus().equals(ptp.getStatusNovo())) || ((ptp.getStatusNovo() != null) && (ptp.getStatusNovo().equals(ptp.getStatus())))) {
                ptp.setFinalizado(true);
            } else {
                ptp.setFinalizado(false);
            }
        }
        return true;
    }

    private void validaRepasse() throws Exception {
        for (PlanoTratamentoProcedimento ptp : planoTratamentoProcedimentos) {
            if (ptp.getStatus() != null && ptp.getStatus().equals(
                    "F") && ptp.getValorAnterior() != null && ptp.getValorDesconto() != null && ptp.getValorAnterior().doubleValue() != ptp.getValorDesconto().doubleValue()) {
                ptp.setValorRepasse(PlanoTratamentoProcedimentoSingleton.getInstance().getBo().findValorRepasse(ptp, UtilsFrontEnd.getEmpresaLogada().getEmpFltImposto()));
                PlanoTratamentoProcedimentoSingleton.getInstance().getBo().persist(ptp);
            }
        }
    }

    private void criaRetorno() {
        Retorno r = new Retorno();
        try {
            if (retorno != null) {
                r.setDataRetorno(retorno);
                r.setPlanoTratamento(this.getEntity());
                r.setPaciente(getPaciente());
                r.setObservacoes(observacoesRetorno);
                RetornoSingleton.getInstance().getBo().persist(r);
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error(OdontoMensagens.getMensagem("erro.plano.cria.retorno"), e);
        }
    }

    /**
     * Mostra confirma????o caso o proc esteja sendo executado mas n??o est?? or??ado
     * 
     * @return boolean
     */
    private boolean validaProcOrcado(PlanoTratamentoProcedimento ptp) {
        List<Orcamento> orcs = OrcamentoSingleton.getInstance().getBo().listOrcamentosFromPT(this.getEntity());
        if (orcs != null && !orcs.isEmpty()) {
            if (ptp.getOrcamentoProcedimentos() == null || ptp.getOrcamentoProcedimentos().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private boolean finalizaProcedimento() throws Exception {
        this.ptps2Finalizar = new ArrayList<>();
        for (PlanoTratamentoProcedimento ptp : planoTratamentoProcedimentos) {
            if (ptp.getStatus() == null || !"F".equals(ptp.getStatus())) {
                ptp.setDataFinalizado(null);
                ptp.setFinalizadoPorProfissional(null);
            } else {
                //verifica se ptp ja estava finalizado anteriormente
                PlanoTratamentoProcedimento ptpBanco = PlanoTratamentoProcedimentoSingleton.getInstance().getBo().find(ptp.getId());
                if (!"F".equals(ptpBanco.getStatus())) {
                    this.ptps2Finalizar.add(ptp);
                }
            }
        }
        if (this.ptps2Finalizar != null && !this.ptps2Finalizar.isEmpty()) {
            this.descricaoEvolucao = null;
            PrimeFaces.current().executeScript("PF('evolucao').show()");
            FacesContext.getCurrentInstance().getPartialViewContext().getRenderIds().add("lume:tabViewPaciente:evolucaoView");
            return false;
        }
        return true;
    }

    public void salvaProcedimento(PlanoTratamentoProcedimento ptp) throws Exception {
        actionPersistFaces(ptp);
        PlanoTratamentoProcedimentoSingleton.getInstance().getBo().persist(ptp);
    }

    public void abreJustificativaRemove(PlanoTratamentoProcedimento planoTratamentoProcedimentoRemove) {
        planoTratamentoProcedimentoSelecionado = planoTratamentoProcedimentoRemove;
        mensagemErroExclusaoProcedimento = "";
        PrimeFaces.current().executeScript("PF('dlgJustificativaRemove').show()");
    }

    public void salvaJustificativaRemove() throws Exception {
        if (planoTratamentoProcedimentoSelecionado.getJustificativaExclusaoDominio() == null) {
            addInfo("?? preciso selecionar uma justificativa!", "");
            return;
        }
        onProcedimentoRemove(planoTratamentoProcedimentoSelecionado);
        carregarPlanoTratamentoProcedimentos();
        PrimeFaces.current().executeScript("PF('dlgJustificativaRemove').hide()");
    }

    public void fechaJustifivaticaRemove() {
        planoTratamentoProcedimentoSelecionado = new PlanoTratamentoProcedimento();
    }

    public void onProcedimentoRemove(PlanoTratamentoProcedimento planoTratamentoProcedimentoRemove) throws Exception {
        if (planoTratamentoProcedimentoRemove.getId() == 0) {
            planoTratamentoProcedimentos.remove(planoTratamentoProcedimentoRemove);
            return;
        }

        if (planoTratamentoProcedimentoRemove.getPlanoTratamento().getValorTotalOrcamento().doubleValue() > 0 && planoTratamentoProcedimentoRemove.getPlanoTratamento().getValor().doubleValue() == 0 && !isAdmin()) {
            mensagemErroExclusaoProcedimento = "Apenas perfis administrativos podem remover procedimentos totalmente j?? pagos.";
            addError("Apenas perfis administrativos podem remover procedimentos totalmente j?? pagos.", "");
            return;
        }
        List<AgendamentoPlanoTratamentoProcedimento> agenda = AgendamentoPlanoTratamentoProcedimentoSingleton.getInstance().getBo().listByPlanoTratamentoProcedimento(
                planoTratamentoProcedimentoRemove);
        if (agenda == null || agenda.isEmpty()) {
            planoTratamentoProcedimentoRemove.setExcluido(Status.SIM);
            planoTratamentoProcedimentoRemove.setDataExclusao(new Date());
            planoTratamentoProcedimentoRemove.setExcluidoPorProfissional(UtilsFrontEnd.getProfissionalLogado().getId());
            planoTratamentoProcedimentoRemove.setStatus("C");
            planoTratamentoProcedimentosExcluidos.add(planoTratamentoProcedimentoRemove);
            planoTratamentoProcedimentos.remove(planoTratamentoProcedimentoRemove);
            ordenaListas();
            actionPersist(null);
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            String erro = "\n";
            for (AgendamentoPlanoTratamentoProcedimento a : agenda) {
                erro += a.getAgendamento().getProfissional().getDadosBasico().getNome() + "\nAgendado: " + sdf.format(a.getAgendamento().getInicio()) + "\n";
            }
            log.error("Erro no actionRemove" + OdontoMensagens.getMensagem("erro.procedimento.utilizado"));
            addError(OdontoMensagens.getMensagem("erro.procedimento.utilizado") + erro, "");

            mensagemErroExclusaoProcedimento = "Esse procedimento est?? em um agendamento, portanto n??o pode ser excluido.";

        }
    }

    public void editaPlanoTratamentoProcedimento(PlanoTratamentoProcedimento ptp) {
        setPlanoTratamentoProcedimentoSelecionado(ptp);
        cleanDetalhesProcedimento();

        handleDenteRegiaoSelected();
        handleDentesRegioesSelects();

        valorProc = ptp.getValorDesconto();

        //seta dentes/regioes
        this.diagnosticos = new ArrayList<>();
        if (ptp.getRegioes() != null)
            this.diagnosticos = new ArrayList<>(ptp.getRegioes());

        //if (ptp.getDenteObj() != null)
        //    this.odontogramaSelecionado = ptp.getDenteObj().getOdontograma();
        this.procedimentoSelecionado = ptp.getProcedimento();
        this.denteRegiaoEscolhida = (ptp.getDenteObj() == null ? ptp.getRegiao() : "Dente " + ptp.getDenteObj().getDescricao());

        List<String> faces = new ArrayList<>();
        for (PlanoTratamentoProcedimentoFace ptpf : ptp.getPlanoTratamentoProcedimentoFaces()) {
            if (!faces.contains(ptpf.getFace())) {
                faces.add(ptpf.getFace());
            }
        }
        ptp.setFacesSelecionadas(faces);

        observacoesDetalhes = "";
        if (ptp.getProcedimento().getQuantidadeFaces() == null || ptp.getProcedimento().getQuantidadeFaces() == 0) {
            observacoesDetalhes = "Observa????o: Procedimento selecionado n??o possui faces configuradas em seu cadastro.";
        }
    }

    public PlanoTratamentoProcedimentoRegiao.TipoRegiao isDenteOrRegiao(String denteOuRegiao) {
        if (denteOuRegiao != null && denteOuRegiao.trim().startsWith("Dente ")) {
            return PlanoTratamentoProcedimentoRegiao.TipoRegiao.DENTE;
        } else if (denteOuRegiao != null && !denteOuRegiao.trim().isEmpty()) {
            return PlanoTratamentoProcedimentoRegiao.TipoRegiao.REGIAO;
        }
        return null;
    }

    public boolean isMostraMensagemConvenioNaoVigente() {
        PlanoTratamentoProcedimento ptp = new PlanoTratamentoProcedimento();
        ptp.setProcedimento(procedimentoSelecionado);
        ptp.setPlanoTratamento(getEntity());
        return isMostraMensagemConvenioNaoVigente(ptp);
    }

    public boolean isMostraMensagemConvenioNaoVigente(PlanoTratamentoProcedimento ptp) {
        try {
            if (ptp.getProcedimento() != null)
                return ConvenioProcedimentoSingleton.getInstance().isUsingConvenio(ptp.getPlanoTratamento(),
                        ptp.getProcedimento()) && !ConvenioProcedimentoSingleton.getInstance().isConvenioAtivoEVigente(ptp.getPlanoTratamento(), ptp.getProcedimento());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public void salvar(ActionEvent event) {
        actionAdicionarProcedimento(event);
        PrimeFaces.current().executeScript("PF('dlgNovoProcedimento').hide()");
        PrimeFaces.current().executeScript("PF('dlgEditarProcedimento').hide()");
    }

    public void salvarContinuar(ActionEvent event) {
        actionAdicionarProcedimento(event);

        this.planoTratamentoProcedimentoSelecionado = new PlanoTratamentoProcedimento();
        this.procedimentoSelecionado = null;
        this.denteRegiaoEscolhida = null;
        this.valorProc = new BigDecimal(0);
        this.ptpInserirQuantasVezes = 1;
        ptpInserirMuitasVezes = false;
        handleDenteRegiaoSelected();
    }

    //Monta PTP para persistencia
    private void populaPtp() {
        try {
            PlanoTratamentoProcedimentoRegiao.TipoRegiao local = isDenteOrRegiao(this.denteRegiaoEscolhida);

            if (this.planoTratamentoProcedimentoSelecionado.getProcedimento() == null || this.planoTratamentoProcedimentoSelecionado.getProcedimento().getId() != this.procedimentoSelecionado.getId())
                atualizaPlanoTratamentoProcedimento(this.planoTratamentoProcedimentoSelecionado, getEntity(), this.procedimentoSelecionado, getPaciente());

            if (local != null) {
                if (local == PlanoTratamentoProcedimentoRegiao.TipoRegiao.DENTE) {
                    if (getEntity().getOdontograma() == null) {
                        this.addInfo("Escolha um odontograma antes de adicionar um procedimento ?? um dente.", "");
                        return;
                    }

                    String denteDescricao = this.denteRegiaoEscolhida.trim().split("Dente ")[1];
                    Dente d = DenteSingleton.getInstance().getBo().findByDescAndOdontograma(denteDescricao, getEntity().getOdontograma());
                    if (d == null) {
                        d = new Dente(denteDescricao, getEntity().getOdontograma());
                        DenteSingleton.getInstance().getBo().persist(d);
                    }
                    this.planoTratamentoProcedimentoSelecionado.setDenteObj(d);
                } else if (local == PlanoTratamentoProcedimentoRegiao.TipoRegiao.REGIAO) {
                    this.planoTratamentoProcedimentoSelecionado.setRegiao(this.denteRegiaoEscolhida);
                }
                actionPersistFaces(planoTratamentoProcedimentoSelecionado);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void actionAdicionarProcedimento(ActionEvent event) {
        try {
            if (this.procedimentoSelecionado == null) {
                this.addInfo("Escolha um procedimento antes de salvar.", "");
                return;
            }

            List<PlanoTratamentoProcedimento> ptpsPersist = new ArrayList<PlanoTratamentoProcedimento>();
            boolean persist = false;

            if (planoTratamentoProcedimentoSelecionado.getId() != 0) {
                populaPtp();
            } else if (planoTratamentoProcedimentoSelecionado.getId() == 0) {
                persist = true;
                int sequencial = 1;
                for (int i = 0; i < (getPtpInserirMuitasVezes() != null && getPtpInserirMuitasVezes().booleanValue() && getPtpInserirQuantasVezes() != null && getPtpInserirQuantasVezes().intValue() > 0 ? getPtpInserirQuantasVezes().intValue() : 1); i++) {

                    populaPtp();

                    planoTratamentoProcedimentoSelecionado.setDataCriado(new Date());
                    if (planoTratamentoProcedimentoSelecionado.getPlanoTratamento().isOrtodontico()) {
                        planoTratamentoProcedimentoSelecionado.setSequencial(sequencial);
                        sequencial++;
                    }

                    ptpsPersist.add(planoTratamentoProcedimentoSelecionado);
                    planoTratamentoProcedimentoSelecionado = new PlanoTratamentoProcedimento();
                }
            }

            if (persist) {
                PlanoTratamentoProcedimentoSingleton.getInstance().getBo().persistBatch(ptpsPersist);
                this.planoTratamentoProcedimentos.addAll(ptpsPersist);
            } else {
                PlanoTratamentoProcedimentoSingleton.getInstance().getBo().merge(planoTratamentoProcedimentoSelecionado);
            }

            if (getEntity().getPlanoTratamentoProcedimentos() == null)
                getEntity().setPlanoTratamentoProcedimentos(new ArrayList<>());
            getEntity().setPlanoTratamentoProcedimentos(this.planoTratamentoProcedimentos);

            PlanoTratamentoSingleton.getInstance().getBo().persist(getEntity());

            this.planoTratamentoProcedimentoSelecionado = new PlanoTratamentoProcedimento();
            this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");
        } catch (Exception e) {
            e.printStackTrace();
            log.error("actionAdicionarProcedimento", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
        }
    }

    public void atualizaPlanoTratamentoProcedimento(PlanoTratamentoProcedimento ptp, PlanoTratamento planoTratamento, Procedimento procedimento, Paciente paciente) {
        ptp.setPlanoTratamento(planoTratamento);
        ptp.setProcedimento(procedimento);

        BigDecimal valorProcedimento = new BigDecimal(this.valorProc.doubleValue());

        ptp.setValorDescontoLabel(valorProcedimento);
        ptp.setValorLabel(procedimento.getValor());
        ptp.setQtdConsultas(0);
        ptp.setDenteObj(null);
        ptp.setStatusDente(null);
        ptp.setPlanoTratamentoProcedimentoFaces(null);
        ptp.setStatus(null);
        ptp.setStatusNovo(null);
        ptp.setDataCriado(new Date());
        if (planoTratamento.isBconvenio() && paciente.getConvenio() != null && paciente.getConvenio().getExcluido().equals(Status.NAO))
            ptp.setConvenio(paciente.getConvenio());

        BigDecimal valorConvenio = new BigDecimal(0);
        if (!valorProc.equals(new BigDecimal(0))) {
            valorConvenio = valorProc;
        } else {
            valorConvenio = Utils.resolveDescontoConvenio(ptp);
        }

        if (valorConvenio != null) {
            ptp.setValor(valorConvenio);
            ptp.setCodigoConvenio(procedimento.getCodigoConvenio());
            ptp.setValorConvenio(true);
        } else {
            ptp.setValor(procedimento.getValor());
            ptp.setValorConvenio(false);
        }
        ptp.setValorDesconto(valorProcedimento);
    }

    public void populaValorProc() {
        if (procedimentoSelecionado != null) {
            BigDecimal valorConvenio = Utils.resolveDescontoConvenio(procedimentoSelecionado, getEntity());
            if (valorConvenio != null && this.getEntity().getPaciente().getConvenio() != null && this.getEntity().getPaciente().getConvenio().getExcluido().equals(Status.NAO)) {
                this.valorProc = valorConvenio;
            } else {
                this.valorProc = procedimentoSelecionado.getValor();
            }
        }
    }

    // public ConvenioProcedimento findByConvenioAndProcedimento(Convenio convenio, Procedimento procedimento) throws Exception {
    //    try {
    //  Map<String, Object> parametros = new HashMap<>();
    //  parametros.put("convenio", convenio);
    // parametros.put("procedimento", procedimento);
    //  parametros.put("idEmpresa", convenio.getIdEmpresa());
    //parametros.put("idEmpresa", 41);
    //  parametros.put("excluido", Status.NAO);
    //  parametros.put("status", Status.ATIVO);
    // return this.findByFields(parametros);

//            StringBuilder sb = new StringBuilder();
//            sb.append("SELECT * FROM CONVENIO_PROCEDIMENTO WHERE ID_CONVENIO = ?1 AND ID_PROCEDIMENTO = ?2 AND ID_EMPRESA = ?3 AND EXCLUIDO = 'N' AND STATUS='A' ");
//            Query query = ConvenioProcedimentoSingleton.getInstance().getBo().getDao().createNativeQuery(sb.toString(), ConvenioProcedimento.class);
//            query.setParameter(1, convenio.getId());
//            query.setParameter(2, procedimento.getId());
//            query.setParameter(3, convenio.getIdEmpresa());
//           
//           if(query.getResultList() != null && !query.getResultList().isEmpty()) {
//               ConvenioProcedimento cp = (ConvenioProcedimento) query.getResultList().get(0);
//               return cp;
//           }
//        } catch (Exception e) {
//            log.error("Erro no findByConvenioAndProcedimento", e);
//        }
//        return null;
//    }

    private void calculaValorTotal() {
        if (getEntity().getPlanoTratamentoProcedimentos() != null) {
            BigDecimal valorTotal = new BigDecimal(0);
            for (PlanoTratamentoProcedimento ptp : getEntity().getPlanoTratamentoProcedimentos()) {
                if ("N".equals(ptp.getExcluido()))
                    valorTotal = valorTotal.add(ptp.getValor());
            }
            getEntity().setValorTotal(valorTotal);
            getEntity().setValorTotalDesconto(valorTotal);
        }

    }

    private void carregarProcedimentosComRegiao() throws Exception {
        procedimentosDente = PlanoTratamentoProcedimentoSingleton.getInstance().getBo().listByComRegiaoAndPlanoTratamento(getEntity());
        for (PlanoTratamentoProcedimento ptp : procedimentosDente) {
            List<String> faces = new ArrayList<>();
            for (PlanoTratamentoProcedimentoFace ptpf : ptp.getPlanoTratamentoProcedimentoFaces()) {
                if (!faces.contains(ptpf.getFace())) {
                    faces.add(ptpf.getFace());
                }
            }
            ptp.setFacesSelecionadas(faces);
        }
    }

    public void actionAdicionarProcedimentoDiagnostico(ActionEvent event) {
        try {

            if (getEntity() != null) {

                PlanoTratamentoProcedimento ptp = PlanoTratamentoProcedimentoSingleton.getInstance().carregaProcedimento(getEntity(), procedimentoSelecionado, getPaciente());
                if (enableRegioes) {
                    ptp.setRegiao(regiaoSelecionada);
                } else {
                    ptp.setDenteObj(denteSelecionado);
                }
                PlanoTratamentoProcedimentoSingleton.getInstance().getBo().persist(ptp);
                if (getEntity().getPlanoTratamentoProcedimentos() == null) {
                    getEntity().setPlanoTratamentoProcedimentos(new ArrayList<>());
                }
                getEntity().getPlanoTratamentoProcedimentos().add(ptp);
                calculaValorTotal();
                PlanoTratamentoSingleton.getInstance().getBo().persist(getEntity());

                for (RegiaoDente rd : getDiagnosticosDentes()) {
                    PlanoTratamentoProcedimentoRegiao ptpr = PlanoTratamentoProcedimentoRegiaoSingleton.getInstance().getBo().findByRegiaoDente(rd);
                    if (ptpr == null) {
                        ptpr = new PlanoTratamentoProcedimentoRegiao();
                        ptpr.setRegiaoDente(rd);
                        ptpr.setAtivo(true);
                        ptpr.setTipoRegiao(TipoRegiao.DENTE);
                    }
                    ptpr.setPlanoTratamentoProcedimento(ptp);
                    PlanoTratamentoProcedimentoRegiaoSingleton.getInstance().getBo().persist(ptpr);
                }

                if (enableRegioes) {
                    carregarProcedimentosComRegiao();
                } else {
                    carregarProcedimentos(denteSelecionado);
                }

                //carregarPlanoTratamentoProcedimentos();
                getEntity().setPlanoTratamentoProcedimentos(PlanoTratamentoProcedimentoSingleton.getInstance().getBo().listByPlanoTratamento(getEntity().getId()));
                procedimentoSelecionado = null;
            } else {
                this.addError("Selecione um plano de tratamento.", "");
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("actionAdicionarProcedimento", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
        }
    }

    public void ordenaListas() {
        Collections.sort(planoTratamentoProcedimentos, new Comparator<PlanoTratamentoProcedimento>() {

            @Override
            public int compare(PlanoTratamentoProcedimento o1, PlanoTratamentoProcedimento o2) {
                return o1.getProcedimento().getDescricao().compareTo(o2.getProcedimento().getDescricao());
            }
        });
    }

    public void carregaDlgProcedimentos(PlanoTratamento planoTratamento) throws Exception {
        justificativa = null;
        carregaConvenios();
        setEntity(planoTratamento);
        atualizaTela(true);
    }

    public void atualizaTela() throws Exception {
        atualizaTela(false);
    }

    @SuppressWarnings("unused")
    public void atualizaTela(boolean validaOrcamento) throws Exception {
        carregarProfissionais();
        carregarPlanoTratamentoProcedimentos();
        carregarOdontogramas();
        carregarPlanosTratamento();
        carregarDadosCabecalho();
        if (validaOrcamento && false)
            validaValoresOrcamentoPlanoTratamento();
    }

    private boolean contemPlanoTratamentoProcedimentoAberto(List<PlanoTratamentoProcedimento> ptp) {
        for (PlanoTratamentoProcedimento planoTratamentoProcedimento : ptp) {
            if (planoTratamentoProcedimento.isFinalizado() == false) {
                return true;
            }
        }
        return false;
    }

    public void actionFinalizarNovamente(PlanoTratamentoProcedimento ptp) {

        try {
            if (ptp.getStatus() != null && ptp.getStatus().equals("F")) {
                List<String> perfis = new ArrayList<>();
                perfis.add(OdontoPerfil.DENTISTA);
                perfis.add(OdontoPerfil.ADMINISTRADOR);
                profissionaisFinalizarNovamente = ProfissionalSingleton.getInstance().getBo().listByEmpresa(perfis, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
                ptpMudarExecutor = ptp;
                profissionalFinalizarNovamente = null;
                PrimeFaces.current().executeScript("PF('dlgFinalizarNovamente').show()");
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogIntelidenteSingleton.getInstance().makeLog("Erro no actionFinalizarNovamente", e);
            addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
        }

    }

    public void actionFinalizarSalvar(ActionEvent event) {
        try {
            ptpMudarExecutor.setFinalizadoPorProfissional(profissionalFinalizarNovamente);
            ptpMudarExecutor.setDentistaExecutor(profissionalFinalizarNovamente);
            calculaRepasse(ptpMudarExecutor);
            PlanoTratamentoProcedimentoSingleton.getInstance().getBo().persist(ptpMudarExecutor);
            PrimeFaces.current().executeScript("PF('dlgFinalizarNovamente').hide()");
            RepasseFaturasSingleton.getInstance().recalculaRepasse(ptpMudarExecutor, profissionalFinalizarNovamente, UtilsFrontEnd.getProfissionalLogado(), null, UtilsFrontEnd.getEmpresaLogada());
            this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");
        } catch (RepasseNaoPossuiRecebimentoException e) {
            addError("Aten????o", e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            LogIntelidenteSingleton.getInstance().makeLog("Erro no actionFinalizarSalvar", e);
            addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
        }

    }

    private void calculaRepasse(PlanoTratamentoProcedimento ptp) {
        try {
            if (ptp.getStatus() != null && ptp.getStatus().equals("F")) {
                ptp.setValorRepasse(PlanoTratamentoProcedimentoSingleton.getInstance().getBo().findValorRepasse(ptp, UtilsFrontEnd.getEmpresaLogada().getEmpFltImposto()));
                PlanoTratamentoProcedimentoSingleton.getInstance().getBo().persist(ptp);
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogIntelidenteSingleton.getInstance().makeLog("Erro no calculaRepasses", e);
            addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
        }

    }

    private void calculaRepasses() {
        for (PlanoTratamentoProcedimento ptp : planoTratamentoProcedimentos)
            calculaRepasse(ptp);
    }

    public boolean isEncerrado() {
        if (getEntity().getStatus().equals(Status.SIM))
            return true;
        return false;
    }

    public boolean isExecutado() {
        if (getEntity().getStatus().equals(Status.EXECUTADO))
            return true;
        return false;
    }

    public List<String> getFacesDisponiveis() {
        return planoTratamentoProcedimentoSelecionado.getFaces(null, this.denteRegiaoEscolhida);
    }

    public void actionPersistFaces(PlanoTratamentoProcedimento ptp) {
        try {
            List<PlanoTratamentoProcedimentoFace> ptpfList = new ArrayList<>();

            if (ptp.getFacesSelecionadas() != null && !ptp.getFacesSelecionadas().isEmpty()) {
                for (String face : ptp.getFacesSelecionadas()) {
                    PlanoTratamentoProcedimentoFace ptpf = new PlanoTratamentoProcedimentoFace();
                    ptpf.setPlanoTratamentoProcedimento(ptp);
                    ptpf.setFace(face);
                    ptpfList.add(ptpf);
                }
            }

            ptp.setPlanoTratamentoProcedimentoFaces(ptpfList);
            PlanoTratamentoProcedimentoSingleton.getInstance().getBo().persist(ptp);
            //carregarPlanoTratamentoProcedimentos();
        } catch (Exception e) {
            e.printStackTrace();
            log.error("actionPersistFaces", e);
            this.addInfo(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
        }
    }

    public void actionNewProcedimento() {
        this.ptpInserirMuitasVezes = false;
        this.ptpInserirQuantasVezes = 1;
        this.valorProc = new BigDecimal(0);
        this.planoTratamentoProcedimentoSelecionado = new PlanoTratamentoProcedimento();
        this.procedimentoSelecionado = null;
        this.denteRegiaoEscolhida = null;
        handleDenteRegiaoSelected();

        observacoesAdicionarProcedimento = "";
        if (getEntity() != null && getEntity().isBconvenio() && getEntity().getConvenio() != null) {
            Long totalProcsConvenio = ProcedimentoSingleton.getInstance().getBo().countByConveio(getEntity().getConvenio().getId());
            if (totalProcsConvenio == 0l) {
                observacoesAdicionarProcedimento = "Observa????o: Esse conv??nio est?? sem procedimentos vinculados! Acesse o menu Configura??oes -> " + "Conv??nio procedimento para vincular os procedimentos nesse conv??nio.";
            }
        }
        
        try {
        	resetTable();
            carregarPlanoTratamentoProcedimentos();
        }catch (Exception e) {
        	addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
		}

    }

    public void atualizaObjetoLista(PlanoTratamentoProcedimento planoTratamentoProcedimento) {
        //planoTratamentoProcedimentos.remove(planoTratamentoProcedimento);
        //planoTratamentoProcedimentos.add(planoTratamentoProcedimento);
    }
    
    public boolean isDisableFaces() {
        return planoTratamentoProcedimentoSelecionado == null || this.procedimentoSelecionado == null || this.procedimentoSelecionado.getQuantidadeFaces() == null || this.procedimentoSelecionado.getQuantidadeFaces() <= 0 || this.denteRegiaoEscolhida == null || this.denteRegiaoEscolhida.isEmpty() || !this.denteRegiaoEscolhida.startsWith(
                "Dente ");
    }

    public boolean escondeEditFaces(PlanoTratamentoProcedimentoRegiao regiao) {
        boolean procedimentoTemFaces = false;
        if (regiao.getPlanoTratamentoProcedimento().getProcedimento() != null && regiao.getPlanoTratamentoProcedimento().getProcedimento().getQuantidadeFaces() != null && regiao.getPlanoTratamentoProcedimento().getProcedimento().getQuantidadeFaces() == 0) {
            procedimentoTemFaces = true;
        }
        return regiao.getTipoRegiao() == null || regiao.getTipoRegiao() != TipoRegiao.DENTE || procedimentoTemFaces;
    }

    public boolean isDisableFacesMulti() {
        if (planoTratamentoProcedimentoSelecionado == null || this.procedimentoSelecionado == null || this.procedimentoSelecionado.getQuantidadeFaces() == null || this.procedimentoSelecionado.getQuantidadeFaces() <= 0)
            return true;
        return dentesSelecionados != null && !dentesSelecionados.isEmpty();
    }
    // ========================================= PLANO DE TRATAMENTO ========================================= //

    // =============================================== EVOLUCAO ============================================== //
    public void saveEvolucao(ActionEvent event) {
        //if (this.finalizaAutomatico())
        actionPersistEvolucao();
    }

    public void actionPersistEvolucao() {
        try {
            EvolucaoSingleton.getInstance().criaEvolucao(getPaciente(), getDescricaoEvolucao(), UtilsFrontEnd.getProfissionalLogado(), getPtps2Finalizar());

            for (PlanoTratamentoProcedimento ptp : planoTratamentoProcedimentos) {
                if (ptp.isFinalizado() && ptp.getFinalizadoPorProfissional() == null) {
                    ptp.setDataFinalizado(new Date());
                    ptp.setFinalizadoPorProfissional(UtilsFrontEnd.getProfissionalLogado());
                    ptp.setDentistaExecutor(UtilsFrontEnd.getProfissionalLogado());
                    ptp.setStatuss(true);
                    PlanoTratamentoProcedimentoSingleton.getInstance().getBo().persist(ptp);
                    this.calculaRepasse(ptp);
                    salvaProcedimento(ptp);

                    try {
                        System.out.println("PROCEDIMENTO EXECUTADO: " + ptp.getId());
                        
                        RepasseFaturasSingleton.getInstance().verificaPlanoTratamentoProcedimentoRepasse(ptp, UtilsFrontEnd.getProfissionalLogado(), UtilsFrontEnd.getProfissionalLogado(),
                                UtilsFrontEnd.getEmpresaLogada());
                    } catch (RepasseNaoPossuiRecebimentoException e) {
                        addError("Aten????o", e.getMessage());
                    } catch (Exception e) {
                        e.printStackTrace();
                        addError("Erro ao salvar registro", e.getMessage());
                    }
                }
            }
            //carregarPlanoTratamentoProcedimentos();
            this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");
            PrimeFaces.current().ajax().addCallbackParam("descEvolucao", true);
            actionExecutarPlanoTratamento(getEntity());
            carregarPlanoTratamentoProcedimentos();
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Erro no actionPersistEvolucao", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
        }
    }

    public String getNomeProfissionalLogado() {
        return UtilsFrontEnd.getProfissionalLogado().getDadosBasico().getNome();
    }

    public void closeEvolucao() throws Exception {
        //carregarPlanoTratamentoProcedimentos();
    }
    // =============================================== EVOLUCAO ============================================== //

    // ============================================= AGENDAMENTOS ============================================ //
    public void carregaAgendamentosProcedimento(PlanoTratamentoProcedimento ptp) {
        setPlanoTratamentoProcedimentoSelecionado(ptp);
    }

    public void limpaAgendamentosProcedimento() {
        setPlanoTratamentoProcedimentoSelecionado(null);
    }

//    public void cancelaAgendamentos() {
//        try {
//            List<AgendamentoPlanoTratamentoProcedimento> aptps = AgendamentoPlanoTratamentoProcedimentoSingleton.getInstance().getBo().listByPlanoTratamentoProcedimento(planoTratamentoProcedimentos);
//            if (aptps != null && !aptps.isEmpty()) {
//                for (AgendamentoPlanoTratamentoProcedimento aptp : aptps) {
//                    aptp.getAgendamento().setStatusNovo(StatusAgendamentoUtil.CANCELADO.getSigla());
//                    aptp.getAgendamento().setDescricao("Cancelado automaticamente pela finaliza????o do plano de tratamento.");
//                    AgendamentoSingleton.getInstance().getBo().persist(aptp.getAgendamento());
//                }
//            }
//        } catch (Exception e) {
//            log.error(OdontoMensagens.getMensagem("plano.cancela.agendamento"), e);
//        }
//    }

    public boolean verificaAgendamento() {
        List<PlanoTratamentoProcedimento> ptpsFinalizados = new ArrayList<>();
        for (PlanoTratamentoProcedimento ptp : planoTratamentoProcedimentos) {
            if (ptp.getStatus() != null && ptp.getStatus().equals("F") && ptp.getDataFinalizado() == null) {
                ptpsFinalizados.add(ptp);
            }
        }
        try {
            List<AgendamentoPlanoTratamentoProcedimento> aptps = AgendamentoPlanoTratamentoProcedimentoSingleton.getInstance().getBo().listByPlanoTratamentoProcedimento(ptpsFinalizados);
            if (aptps != null && !aptps.isEmpty()) {
                addError("N??o ?? possivel finalizar procedimentos com agendamentos pendentes. ", "");
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Erro listByPlanoTratamentoProcedimento ", e);
            addError(OdontoMensagens.getMensagem("erro.plano.agendamento"), "");
            return false;
        }
        return true;
    }
    // ============================================= AGENDAMENTOS ============================================ //

    //============================================== FINANCEIRO ============================================== //
    public BigDecimal calculaTotalOrcamento() {
        return OrcamentoSingleton.getInstance().getTotalOrcamento(this.orcamentoSelecionado);
    }

    public BigDecimal getDescontoFrom(OrcamentoItem oi) {
        try {
            return oi.getValorOriginal().subtract(oi.getValor()).divide(oi.getValorOriginal(), BigDecimal.ROUND_HALF_UP);
        } catch (Exception e) {
            e.printStackTrace();
            return BigDecimal.ZERO;
        }
    }

    public void actionNewOrcamento() {
        try {
            if (this.fazOrcamento()) {
                this.orcamentoSelecionado = OrcamentoSingleton.getInstance().preparaOrcamentoFromPT(getEntity());
                this.orcamentoSelecionado.setProfissionalCriacao(UtilsFrontEnd.getProfissionalLogado());

                observacoesCobrancaOrcamento = null;

                quantidadeVezesNegociacaoOrcamento = null;
                parcelasDisponiveis = null;
                valorPrimeiraParcelaOrcamento = new BigDecimal(0);
                descontosDisponiveis = new HashMap<Integer, DescontoOrcamento>();
                mensagemCalculoOrcamento = "";
                numeroParcelaOrcamento = new BigDecimal(0);
                //    mensagemCalculoOrcamentoDiferenca = "";
                valorParcela = new BigDecimal(0);

                populaDescontos();

                PrimeFaces.current().executeScript("PF('dlgViewOrcamento').show()");
            } else {
                this.addError("PERMISS??O NEGADA",
                        "Para fazer o or??amento, " + "voc?? precisa ativar a caixa de sele????o na tela de \"Configura????o de Descontos\", " + "menu \"Cadastro de profissionais\".");
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Erro no actionNewOrcamento", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), e.getMessage());
        }

    }

    public void fazNada() {

    }

    public String getDescontoFromParcela(Integer parcela) {
        String percent = null;
        NumberFormat percformat = NumberFormat.getPercentInstance(new Locale("pt", "BR"));
        if (parcela == null || this.descontosDisponiveis.get(parcela) == null) {
            percent = percformat.format(BigDecimal.ZERO);
        } else {
            if (this.descontosDisponiveis.get(parcela).getDesconto() != null) {
                percent = percformat.format(this.descontosDisponiveis.get(parcela).getDesconto().divide(BigDecimal.valueOf(100), 4, BigDecimal.ROUND_HALF_UP));
            }

        }
        return percent;
    }

    private boolean validaOrcamentoMaiorPermitido() {
        BigDecimal valorDesconto = new BigDecimal(0);
        DescontoOrcamento descontoCadastrado = null;
        if (!descontosDisponiveis.isEmpty()) {
            if (numeroParcelaOrcamento == null) {
                return false;
            }
            descontoCadastrado = descontosDisponiveis.get(numeroParcelaOrcamento.intValue());
            if (descontoCadastrado != null) {
                valorDesconto = descontoCadastrado.getDesconto();
            }
        }
        if (orcamentoSelecionado.getDescontoTipo().equals("P") && orcamentoSelecionado.getDescontoValor().compareTo(valorDesconto) == 1) {
            return true;
        } else if (orcamentoSelecionado.getDescontoTipo().equals("V")) {
            //orcamentoSelecionado.getValorTotal();
            //orcamentoSelecionado.getValorTotalSemDesconto();
            double descontoEmPorcentagem = (orcamentoSelecionado.getDescontoValor().doubleValue() * 100) / orcamentoSelecionado.getValorTotalSemDesconto().doubleValue();

            if (descontoCadastrado != null) {
                valorDesconto = descontosDisponiveis.get(numeroParcelaOrcamento.intValue()).getDesconto();
            } else {
                valorDesconto = new BigDecimal(0);
            }

            if (descontoEmPorcentagem > valorDesconto.doubleValue()) {
                return true;
            }
        }
        return false;
    }

    public void validaDescontos() {
        mensagemCalculoOrcamento = "";
        //   mensagemCalculoOrcamentoDiferenca = "";

        if (quantidadeVezesNegociacaoOrcamento == null && orcamentoSelecionado.getDescontoValor().compareTo(new BigDecimal(0)) != 0) {
            this.addError("Erro", "Selecione a quantidade de parcelas para a negocia????o");
            orcamentoSelecionado.setDescontoValor(new BigDecimal(0));
        } else if (quantidadeVezesNegociacaoOrcamento != null) {
            numeroParcelaOrcamento = new BigDecimal(quantidadeVezesNegociacaoOrcamento);

            if (orcamentoSelecionado.getDescontoValor() == null) {
                orcamentoSelecionado.setDescontoValor(new BigDecimal(0));
            }

            if (descontosDisponiveis.containsKey(numeroParcelaOrcamento.intValue())) {

                if (validaOrcamentoMaiorPermitido()) {
                    this.addError("Erro", "Desconto maior que o permitido.");
                    orcamentoSelecionado.setDescontoValor(new BigDecimal(0));
                }

            } else {
                if (orcamentoSelecionado.getDescontoValor().compareTo(new BigDecimal(0)) != 0) {
                    this.addError("Erro", "O n??mero de parcelas escolhido n??o permite desconto.");
                    orcamentoSelecionado.setDescontoValor(new BigDecimal(0));
                }
            }
            if (valorPrimeiraParcelaOrcamento == null) {
                valorPrimeiraParcelaOrcamento = new BigDecimal(0);
            }
            if (valorPrimeiraParcelaOrcamento.compareTo(orcamentoSelecionado.getValorTotalComDesconto()) > 0) {
                this.addError("Erro", "O valor da primeira parcela n??o pode ser maior que o valor do or??amento");
                valorPrimeiraParcelaOrcamento = new BigDecimal(0);
            } else if (valorPrimeiraParcelaOrcamento.compareTo(new BigDecimal(0)) < 0) {
                this.addError("Erro", "O valor da primeira parcela n??o pode ser negativo");
                valorPrimeiraParcelaOrcamento = new BigDecimal(0);
            }

            String valortotalFormatado = Utils.formataValor(orcamentoSelecionado.getValorTotalComDesconto());
            if (numeroParcelaOrcamento.compareTo(new BigDecimal(1)) == 0) {
                mensagemCalculoOrcamento = "Pagamento a vista, valor de R$ " + valortotalFormatado;
                valorPrimeiraParcelaOrcamento = new BigDecimal(0);
            } else {

                valorParcela = new BigDecimal(0);
                if (valorPrimeiraParcelaOrcamento.compareTo(new BigDecimal(0)) != 0) {
                    valorParcela = (orcamentoSelecionado.getValorTotalComDesconto().subtract(valorPrimeiraParcelaOrcamento)).divide(numeroParcelaOrcamento.subtract(new BigDecimal(1)), 2,
                            RoundingMode.HALF_UP);

                    String valorParcelaFormatado = Utils.formataValor(valorParcela);

                    mensagemCalculoOrcamento = "Entrada de " + Utils.formataValor(
                            valorPrimeiraParcelaOrcamento) + " mais " + (numeroParcelaOrcamento.subtract(new BigDecimal(1))) + "x de " + valorParcelaFormatado + ". Total de " + valortotalFormatado;
                } else {
                    valorParcela = orcamentoSelecionado.getValorTotalComDesconto().divide(numeroParcelaOrcamento, 2, RoundingMode.HALF_UP);
                    String valorParcelaFormatado = Utils.formataValor(valorParcela);
                    mensagemCalculoOrcamento = "Pagamento em " + numeroParcelaOrcamento + "x de " + valorParcelaFormatado + ". Total de " + valortotalFormatado;

                }
                BigDecimal valorComparar;
                if (valorPrimeiraParcelaOrcamento.compareTo(new BigDecimal(0)) == 0) {
                    valorComparar = valorParcela.multiply(numeroParcelaOrcamento);
                } else {
                    valorComparar = valorParcela.multiply(numeroParcelaOrcamento.subtract(new BigDecimal(1))).add(valorPrimeiraParcelaOrcamento);
                }
                diferencaCalculoParcelas = null;
                if (valorComparar.compareTo(orcamentoSelecionado.getValorTotalComDesconto()) != 0) {
                    if (valorPrimeiraParcelaOrcamento != null && valorPrimeiraParcelaOrcamento.compareTo(new BigDecimal(0)) != 0) {
                        diferencaCalculoParcelas = orcamentoSelecionado.getValorTotalComDesconto().subtract(
                                valorPrimeiraParcelaOrcamento.add(valorParcela.multiply(numeroParcelaOrcamento.subtract(new BigDecimal(1)))));
                    } else {
                        diferencaCalculoParcelas = orcamentoSelecionado.getValorTotalComDesconto().subtract((valorParcela.multiply(numeroParcelaOrcamento)));
                    }

                    //  mensagemCalculoOrcamentoDiferenca = "Aten????o! Diferen??a de " + Utils.formataValor(
                    //      diferencaCalculoParcelas) + " na soma das parcelas. " + "Essa diferen??a ser?? somada na primeira parcela automaticamente.";
                }
            }
        }
    }

    private void populaDescontos() {
        parcelasDisponiveis = new ArrayList<Integer>();

        List<DescontoOrcamento> descontos = new ArrayList<DescontoOrcamento>();
        descontos = DescontoOrcamentoSingleton.getInstance().getBo().listByProfissional(UtilsFrontEnd.getProfissionalLogado(), "A");

        if (!descontos.isEmpty()) {
            //VERIFICA QUANDO PROFISSIONAL N??O TEM DESCONTO CADASTRADO CORRETAMENTE
            for (DescontoOrcamento desconto : descontos) {
                if (desconto.getQuantidadeParcelas() == null) {
                    addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "No cadastro do profissional, informe a quantidade de parcelas para o(s) desconto(s) cadastrado(s).");
                    return;
                }
            }
        } else if (descontos.isEmpty()) {
            descontos = DescontoOrcamentoSingleton.getInstance().getBo().listByClinica(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa(), "A");

            for (DescontoOrcamento desconto : descontos) {
                if (desconto.getQuantidadeParcelas() == null) {
                    addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "No cadastro da clinica, informe a quantidade de parcelas para o(s) desconto(s) cadastrado(s).");
                    return;
                }
            }
        }

        descontos.sort((d1, d2) -> d1.getQuantidadeParcelas().compareTo(d2.getQuantidadeParcelas()));
        int quantidadeParcelasInseridas = 1;
        for (DescontoOrcamento descontoOrcamento : descontos) {
            for (int i = 1; i <= descontoOrcamento.getQuantidadeParcelas().intValue(); i++) {
                if (i <= descontoOrcamento.getQuantidadeParcelas().intValue()) {
                    if (parcelasDisponiveis.size() < descontoOrcamento.getQuantidadeParcelas().intValue()) {
                        descontosDisponiveis.put(quantidadeParcelasInseridas, descontoOrcamento);
                        parcelasDisponiveis.add(quantidadeParcelasInseridas);
                        quantidadeParcelasInseridas++;
                    }

                }
            }
        }
        while (quantidadeParcelasInseridas <= 60) {
            parcelasDisponiveis.add(quantidadeParcelasInseridas);
            quantidadeParcelasInseridas++;

        }

    }

    public BigDecimal getValorRestanteOrcamento() {
        BigDecimal totalAPagar = this.getValorOrcamentoAPagar();
        if (totalAPagar == null || getOrcamentoSelecionado() == null)
            return BigDecimal.ZERO;
        BigDecimal totalPlanejado = getOrcamentoSelecionado().getTotalPlanejado();
        if (totalPlanejado == null)
            return BigDecimal.ZERO;
        return totalAPagar.subtract(totalPlanejado);
    }

    public void newPlanejamento() {
        Dominio pagtoDinheiro;
        try {
            pagtoDinheiro = this.formasPagamentoNewPlanejamento.stream().filter(forma -> "DI".equals(forma.getValor())).collect(Collectors.toList()).get(0);
        } catch (Exception e) {
            e.printStackTrace();
            pagtoDinheiro = null;
        }

        this.planejamentoAtual = new OrcamentoPlanejamento();
        this.planejamentoAtual.setDataCriacao(new Date());
        this.planejamentoAtual.setCriadoPor(UtilsFrontEnd.getProfissionalLogado());
        this.planejamentoAtual.setOrcamento(getOrcamentoSelecionado());
        this.planejamentoAtual.setDataPagamento(new Date());
        this.planejamentoAtual.setDataCredito(new Date());
        this.planejamentoAtual.setFormaPagamento(pagtoDinheiro);
        this.planejamentoAtual.setValor(0d);
    }

    public void atualizaDataCreditoNParcelasNewPlanejamento() {
        if (this.getPlanejamentoAtual().getTarifa() != null) {
            this.setParcelasNewPlanejamento(new ArrayList<>());
            for (int i = this.getPlanejamentoAtual().getTarifa().getParcelaMinima(); i <= this.getPlanejamentoAtual().getTarifa().getParcelaMaxima(); i++)
                this.getParcelasNewPlanejamento().add(i);

            Calendar c = Calendar.getInstance();
            if (this.getPlanejamentoAtual().getDataPagamento() == null)
                this.getPlanejamentoAtual().setDataPagamento(c.getTime());
            c.setTime(this.getPlanejamentoAtual().getDataPagamento());
            c.add(Calendar.DAY_OF_MONTH, this.getPlanejamentoAtual().getTarifa().getPrazo());
            this.getPlanejamentoAtual().setDataCredito(c.getTime());
        } else {
            this.getPlanejamentoAtual().setDataCredito(this.getPlanejamentoAtual().getDataPagamento());
        }
    }

    public boolean existeCadastroTarifa() {
        return existeCadastroTarifa(getPlanejamentoAtual().getFormaPagamento().getValor());
    }

    public boolean existeCadastroTarifa(String formaPagamento) {
        List<String> formasPagamentoComProduto = Arrays.asList(new String[] { "CC", "CD", "BO" });
        if (formasPagamentoComProduto.indexOf(formaPagamento) != -1)
            return true;
        return false;
    }

    public boolean showProdutoNewPlanejamento() {
        if (getPlanejamentoAtual() != null && getPlanejamentoAtual().getFormaPagamento() != null) {
            if (existeCadastroTarifa()) {
                setTarifasNewPlanejamento(TarifaSingleton.getInstance().getBo().listByForma(getPlanejamentoAtual().getFormaPagamento().getValor(), UtilsFrontEnd.getProfissionalLogado().getIdEmpresa(),
                        FormaPagamento.RECEBIMENTO));
                return true;
            } else {
                getPlanejamentoAtual().setnParcela(1);
                return false;
            }
        } else
            return false;
    }

    public void removePlanejamento(OrcamentoPlanejamento planejamento) {
        planejamento.setAtivo(false);
        planejamento.setDataAlteracaoStatus(new Date());
        planejamento.setAlteradoPor(UtilsFrontEnd.getProfissionalLogado());
    }

    public void addPlanejamentoContinuando() {
        try {
            BigDecimal valorFuturo = getOrcamentoSelecionado().getTotalPlanejado().add(BigDecimal.valueOf(planejamentoAtual.getValor()));
            if (getOrcamentoSelecionado().getValorComDesconto().compareTo(valorFuturo) == -1)
                throw new Exception("Valor do or??amento excedido!");
            if (getOrcamentoSelecionado().getPlanejamento() == null)
                getOrcamentoSelecionado().setPlanejamento(new ArrayList<>());
            planejamentoAtual.setOrcamento(getOrcamentoSelecionado());
            getOrcamentoSelecionado().getPlanejamento().add(planejamentoAtual);
            planejamentoAtual = null;

            newPlanejamento();
            this.addInfo("Sucesso", "Planejamento inserido com sucesso.");
        } catch (Exception e) {
            e.printStackTrace();
            this.addError("Erro", Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO) + " " + e.getMessage());
        }
    }

    public void addPlanejamento() {
        addPlanejamentoContinuando();
        PrimeFaces.current().executeScript("PF('dlgNewPlanejamento').hide()");
    }

    public void cancelaLancamentos(Orcamento o) throws Exception {
        OrcamentoSingleton.getInstance().inativaOrcamento(o, UtilsFrontEnd.getProfissionalLogado());
    }

    private void validaValoresOrcamentoPlanoTratamento() {
        if (getEntity() != null && getEntity().getId() != null) {
            BigDecimal valorUltimoOrcamento = OrcamentoSingleton.getInstance().getBo().findValorUltimoOrcamento(getEntity().getId());
            if (valorUltimoOrcamento != null) {
                if (isTemPermissaoExtra() && PlanoTratamentoProcedimentoSingleton.getInstance().getBo().findValorTotalDescontoByPT(
                        getEntity()).doubleValue() != valorUltimoOrcamento.doubleValue() && !getEntity().isOrtodontico()) {
                    this.addError("Valor do plano est?? diferente do valor do or??amento, ?? preciso refazer o or??amento!", "");
                }
            } else {
                if (isTemPermissaoExtra() && getEntity().getStatus().equals("N")) {
                    this.addError("Este plano ?? novo e ainda n??o tem or??amento, ?? preciso fazer o or??amento!", "");
                } else {
                    this.addError("Este plano j?? foi finalizado!", "");
                }
            }
        }
    }

    private void carregarDadosCabecalho() {
        Empresa empresalogada = UtilsFrontEnd.getEmpresaLogada();
        if (empresalogada != null) {
            nomeClinica = empresalogada.getEmpStrNme() != null ? empresalogada.getEmpStrNme() : "";
            endTelefoneClinica = (empresalogada.getEmpStrEndereco() != null ? empresalogada.getEmpStrEndereco() + " - " : "") + (empresalogada.getEmpStrCidade() != null ? empresalogada.getEmpStrCidade() + "/" : "") + (empresalogada.getEmpChaUf() != null ? empresalogada.getEmpChaUf() + " - " : "") + (empresalogada.getEmpChaFone() != null ? empresalogada.getEmpChaFone() : "");
        }
    }

    public BigDecimal getTotalPago() {
        return LancamentoSingleton.getInstance().getTotalLancamentoPorOrcamento(this.orcamentoSelecionado, true);
    }

    public void actionEditaDataAprovacaoOrcamento(Orcamento o) {
        this.orcamentoDataAprovacao = o;
        this.novaDataAprovacao = o.getDataAprovacao();
    }

    public void actionSalvarDataAprovacaoOrcamento() {
        try {
            OrcamentoSingleton.getInstance().atualizaDataAprovacao(orcamentoDataAprovacao, novaDataAprovacao, UtilsFrontEnd.getProfissionalLogado());
            carregaOrcamentos();

            PrimeFaces.current().executeScript("PF('alterarDataAprovacao').hide()");
            this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
        }
    }

    public void carregaTelaOrcamento(PlanoTratamento planoTratamento) {
        try {
            setEntity(planoTratamento);
            carregaOrcamentos();
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Erro no carregaTela", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
        }
    }

    public void carregaOrcamentos() {
        orcamentos = OrcamentoSingleton.getInstance().getBo().listOrcamentosFromPT(getEntity());
        try {
        	resetTable();
            carregarPlanoTratamentoProcedimentos();
        } catch (Exception e) {
            log.error("Erro no carregaTela", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
        }
    }

    public BigDecimal getValorOrcamentoAPagar() {
        if (this.orcamentoSelecionado == null)
            return new BigDecimal(0);

        BigDecimal valorAPagar = OrcamentoSingleton.getInstance().getTotalOrcamentoDesconto(this.orcamentoSelecionado);
        BigDecimal valorPago = getTotalPago();
        if (valorAPagar.subtract(valorPago).doubleValue() > 0d)
            return valorAPagar.subtract(valorPago);
        return new BigDecimal(0);
    }

    public void naoAprovarOrcamento(Orcamento orcamento) {
        if (!orcamento.getStatus().equals("Cancelado") && !orcamento.getStatus().equals("Aprovado")) {
            try {
                orcamento.setStatus("Reprovado");
                Date hoje = Calendar.getInstance().getTime();
                orcamento.setDataAlteracaoStatus(hoje);
                orcamento.setDataAprovacao(hoje);
                orcamento.setProfissionalAprovacao(UtilsFrontEnd.getProfissionalLogado());
                OrcamentoSingleton.getInstance().getBo().persist(orcamento);
                this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");
                carregaOrcamentos();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            this.addError("Erro ao atualizar status", "Or??amento j?? foi Aprovado/Cancelado");
        }

    }

    public void actionRemoverOrcamento() {
        try {
            List<Fatura> faturas = FaturaSingleton.getInstance().getBo().findFaturaByOrcamento(orcamentoCancelamento);

            if (faturas != null && !faturas.isEmpty()) {
                for (Fatura f : faturas)
                    if (f.getSubStatusFatura().contains(SubStatusFatura.VALIDADO))
                        throw new FaturaIrregular();
            }
            orcamentoCancelamento.setStatus("Cancelado");
            orcamentoCancelamento.setJustificativaCancelamento(justificativaCancelamento);

            OrcamentoSingleton.getInstance().inativaOrcamento(orcamentoCancelamento, UtilsFrontEnd.getProfissionalLogado(), this.getEntity());
            carregaOrcamentos();

            if (orcamentoCancelamento.getItens() != null && orcamentoCancelamento.getItens().size() > 0 && orcamentoCancelamento.getItens().get(
                    0).getOrigemProcedimento() != null && orcamentoCancelamento.getItens().get(
                            0).getOrigemProcedimento().getPlanoTratamentoProcedimento().getPlanoTratamento() != null && orcamentoCancelamento.getItens().get(
                                    0).getOrigemProcedimento().getPlanoTratamentoProcedimento().getPlanoTratamento().getValorReceber() != null) {
                orcamentoCancelamento.getItens().get(0).getOrigemProcedimento().getPlanoTratamentoProcedimento().getPlanoTratamento().setValorReceber(
                        orcamentoCancelamento.getItens().get(0).getOrigemProcedimento().getPlanoTratamentoProcedimento().getPlanoTratamento().getValorReceber().subtract(
                                orcamentoCancelamento.getValorTotal()));
                PlanoTratamentoSingleton.getInstance().getBo().persist(orcamentoCancelamento.getItens().get(0).getOrigemProcedimento().getPlanoTratamentoProcedimento().getPlanoTratamento());
            }

            List<Fatura> faturasOrcamento = FaturaSingleton.getInstance().getBo().findFaturaByOrcamento(orcamentoCancelamento);
            for (Fatura f : faturasOrcamento) {
                FaturaSingleton.getInstance().cancelarFatura(f, UtilsFrontEnd.getProfissionalLogado());
            }

            this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");
            PrimeFaces.current().executeScript("PF('justificativaCancelamento').hide();");
        } catch (FaturaIrregular e) {
            System.out.println(e.getMessage());
            this.addError("Or??amento possui faturas validadas", "?? necess??rio regularizar as faturas originadas por esse or??amento.");
        } catch (Exception e) {
            e.printStackTrace();
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_REMOVER_REGISTRO), "");
            LogIntelidenteSingleton.getInstance().makeLog(e);
        }
    }

    public void actionReprovarOrcamentos() {
        try {
//      Reprova os or??amentos selecionados na tabela antes de aprovar o or??amento
            if (orcamentosParaReprovar != null && orcamentosParaReprovar.size() > 0) {
                for (Orcamento o : orcamentosParaReprovar) {
                    o.setStatus("Reprovado");
                    OrcamentoSingleton.getInstance().getBo().persist(o);
                }
                orcamentosParaReprovar = new ArrayList<Orcamento>();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void actionSimulaLancamento() {
        try {
            List<OrcamentoItem> itemsJaAprovados = OrcamentoSingleton.getInstance().itensAprovadosNoOrcamento(orcamentoSelecionado);

            if (validaOrcamentoMaiorPermitido()) {
                this.addError("Erro", "Desconto maior que o permitido.");
                return;
            }

            //verificando se os procedimentos ja estao aprovados em outro orcamento       
            if (itemsJaAprovados != null && itemsJaAprovados.size() > 0) {
                List<String> procedimentos = new ArrayList<String>();
                for (OrcamentoItem orcamentoItem : itemsJaAprovados) {
                    procedimentos.add(orcamentoItem.getOrigemProcedimento().getPlanoTratamentoProcedimento().getProcedimento().getDescricao());
                }
                String valoresSeparados = String.join(", ", procedimentos);

                this.addError("O(s) procedimento(s): " + valoresSeparados + " j?? est??(??o) aprovado(s) em outro or??amento.", "");
                return;
            }

            BigDecimal orcamentoPerc = new BigDecimal(0);
            if ("P".equals(orcamentoSelecionado.getDescontoTipo()))
                orcamentoPerc = orcamentoSelecionado.getDescontoValor();
            else if ("V".equals(orcamentoSelecionado.getDescontoTipo())) {
                orcamentoPerc = orcamentoSelecionado.getDescontoValor().divide(orcamentoSelecionado.getValorTotalSemDesconto(), 10, BigDecimal.ROUND_HALF_DOWN).multiply(new BigDecimal(100));
            }
            orcamentoPerc = orcamentoPerc.setScale(2, BigDecimal.ROUND_HALF_DOWN);

//            if (!isDentistaAdmin() && UtilsFrontEnd.getProfissionalLogado().getDesconto() == null || UtilsFrontEnd.getProfissionalLogado().getDesconto().doubleValue() < orcamentoPerc.doubleValue()) {
//                if (UtilsFrontEnd.getProfissionalLogado().getDesconto() != null) {
//                    if ("P".equals(orcamentoSelecionado.getDescontoTipo())) {
//                        orcamentoSelecionado.setDescontoValor(UtilsFrontEnd.getProfissionalLogado().getDesconto());
//                    } else if ("V".equals(orcamentoSelecionado.getDescontoTipo())) {
//                        orcamentoSelecionado.setDescontoValor(orcamentoSelecionado.getValorTotal().multiply(UtilsFrontEnd.getProfissionalLogado().getDesconto()));
//                    }
//                }
//                //sem desconto na tela, nao precisa validar se profissional tem desconto...
//                if (!(orcamentoSelecionado.getDescontoValor().compareTo(new BigDecimal(0)) == 0)) {
//                    addError(OdontoMensagens.getMensagem("erro.orcamento.desconto.maior"), "");
//                    return;
//                }
//
//            }

            orcamentoSelecionado.setValorTotal(OrcamentoSingleton.getInstance().getTotalOrcamento(orcamentoSelecionado));
            orcamentoSelecionado.setQuantidadeParcelas(1);
            orcamentoSelecionado.setStatus("Aprovado");
            OrcamentoSingleton.getInstance().aprovaOrcamento(orcamentoSelecionado, UtilsFrontEnd.getProfissionalLogado());
            addInfo("Sucesso", "Aprova????o com " + orcamentoPerc + "% de desconto aplicado!");

            carregaOrcamentos();

            orcamentoSelecionado.setQuantidadeParcelas(1);

            itemsJaAprovados = OrcamentoSingleton.getInstance().itensAprovadosNoOrcamento(orcamentoSelecionado);

            if (itemsJaAprovados != null && itemsJaAprovados.size() > 0) {
                //recalculaRepasseAsync(orcamentoItem.getOrigemProcedimento().getPlanoTratamentoProcedimento(),UtilsFrontEnd.getProfissionalLogado(), UtilsFrontEnd.getEmpresaLogada());
                recalculaRepasseAsync(itemsJaAprovados, UtilsFrontEnd.getProfissionalLogado(), UtilsFrontEnd.getEmpresaLogada());
            }

            orcamentosPendentes = new ArrayList<Orcamento>();
            for (Orcamento o : orcamentos) {
                if (o.getStatus().equals("Pendente Aprova????o") && o.getId() != orcamentoSelecionado.getId()) {
                    orcamentosPendentes.add(o);
                }
            }

            if (orcamentosPendentes.size() > 0) {
                PrimeFaces.current().executeScript("PF('reprovarOrcamento').show();");
                PrimeFaces.current().executeScript("PF('dlgViewOrcamento').hide();");
            }

        } catch (Exception e) {
            e.printStackTrace();
            LogIntelidenteSingleton.getInstance().makeLog("Erro no actionPersist OrcamentoMB", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), e.getMessage());
            return;
        }
    }

    private void recalculaRepasseAsync(List<OrcamentoItem> itemsJaAprovados, Profissional profissional, Empresa empresa) {

        Thread th = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    for (OrcamentoItem orcamentoItem : itemsJaAprovados) {
                        Thread.sleep(3000);
                        PlanoTratamentoProcedimento ptp = orcamentoItem.getOrigemProcedimento().getPlanoTratamentoProcedimento();

                        if (ptp.getRepasseFaturas() != null && ptp.getRepasseFaturas().size() > 0) {
                            RepasseFaturas repasseFaturas = RepasseFaturasSingleton.getInstance().getRepasseFaturasComFaturaAtiva(ptp);
                            if (repasseFaturas != null && repasseFaturas.getFaturaRepasse() != null) {
                                ptp.setFatura(repasseFaturas.getFaturaRepasse());
                            }
                        } else {
                            //repasse antigo, quando ainda nao tinha ptp no repasse fatura
                            RepasseFaturasItem repasseFaturasItem = RepasseFaturasItemSingleton.getInstance().getBo().getItemOrigemFromRepasse(ptp);
                            if (repasseFaturasItem != null && repasseFaturasItem.getFaturaItemRepasse() != null && repasseFaturasItem.getFaturaItemRepasse().getFatura() != null) {
                                ptp.setFatura(repasseFaturasItem.getFaturaItemRepasse().getFatura());
                            }
                        }

                        RepasseFaturasSingleton.getInstance().recalculaRepasse(ptp, ptp.getDentistaExecutor(), profissional, ptp.getFatura(), empresa);

                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });

        th.start();

    }

    public void actionEditOrcamento(Orcamento orcamento) {
        if (orcamento != null) {
            this.imprimirSemValores = false;
            this.orcamentoSelecionado = orcamento;
            orcamentoSelecionado.setValorPago(getTotalPago());

            orcamentoSelecionado.getItens().sort((p1, p2) -> {
                Integer i = p1.getOrigemProcedimento().getPlanoTratamentoProcedimento().getSequencial();
                return i.compare(i, p2.getOrigemProcedimento().getPlanoTratamentoProcedimento().getSequencial());
            });

            //procedimentosOrcSelecionado = orcamentoSelecionado.getItens();

            NegociacaoOrcamento negociacaOrcamento = NegociacaoOrcamentoSingleton.getInstance().getBo().getNegociacaoFromOrcamento(orcamentoSelecionado);
            populaDescontos();
            if (negociacaOrcamento != null) {
                numeroParcelaOrcamento = new BigDecimal(negociacaOrcamento.getQuantidadeParcelas());
                valorPrimeiraParcelaOrcamento = negociacaOrcamento.getValorPrimeiraParcela();
                valorParcela = negociacaOrcamento.getValorParcela();
                observacoesCobrancaOrcamento = negociacaOrcamento.getObservacao();

                for (Integer quantidadeVezes : parcelasDisponiveis) {
                    if (new BigDecimal(quantidadeVezes).compareTo(numeroParcelaOrcamento) == 0) {
                        quantidadeVezesNegociacaoOrcamento = quantidadeVezes.intValue();
                    }
                }

                validaDescontos();
            } else {
                numeroParcelaOrcamento = null;
                valorPrimeiraParcelaOrcamento = null;
                valorParcela = null;
                observacoesCobrancaOrcamento = null;

            }

        }
    }

    public void updateListaProcedimentos() {
        if (this.procedimentosOrcSelecionado != null) {
            this.procedimentosOrcSelecionado = orcamentoSelecionado.getItens();
            if (!this.showProcedimentosCancelados) {
                procedimentosOrcSelecionado.removeIf((proc) -> !proc.isAtivo());
            }
        }
    }

    public void actionRemoveOrcamento(ActionEvent event) {
        try {
            if (isDentistaAdmin()) {
                cancelaLancamentos(this.orcamentoSelecionado);
                OrcamentoSingleton.getInstance().inativaOrcamento(this.orcamentoSelecionado, UtilsFrontEnd.getProfissionalLogado());

                this.addError(Mensagens.getMensagem(Mensagens.REGISTRO_REMOVIDO_COM_SUCESSO), "");
                carregaTelaOrcamento(getEntity());
                this.orcamentoSelecionado = null;
            } else {
                this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "Somente o administrador do sistema pode cancelar o or??amento.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogIntelidenteSingleton.getInstance().makeLog(e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_REMOVER_REGISTRO), "");
        }
    }

    public void atualizaValoresOrcamento() {
        orcamentoSelecionado.setValorTotal(OrcamentoSingleton.getInstance().getTotalOrcamentoDesconto(orcamentoSelecionado));
    }

    public void actionPersistOrcamento(ActionEvent event) {
        try {
            if (orcamentoSelecionado.getDataCriacao() == null) {
                orcamentoSelecionado.setProfissionalCriacao(UtilsFrontEnd.getProfissionalLogado());
                orcamentoSelecionado.setDataCriacao(new Date());
            }
            orcamentoSelecionado.setPlanoTratamento(getEntity());
            orcamentoSelecionado.setStatus("Pendente Aprova????o");
            atualizaValoresOrcamento();
            setOrcamentoSelecionado(OrcamentoSingleton.getInstance().salvaOrcamento(orcamentoSelecionado));

            //   valorPrimeiraParcelaOrcamento = null;
            valorParcela = null;
            //  if (diferencaCalculoParcelas != null && diferencaCalculoParcelas.compareTo(new BigDecimal(0)) != 0) {
            // if (valorPrimeiraParcelaOrcamento == null || valorPrimeiraParcelaOrcamento.compareTo(new BigDecimal(0)) == 0) {
            // valorPrimeiraParcelaOrcamento = valorPrimeiraParcelaOrcamento.add(diferencaCalculoParcelas);
            //  } else {
            //    valorParcela = orcamentoSelecionado.getValorTotalComDesconto().divide(numeroParcelaOrcamento, 2, RoundingMode.HALF_UP);
            // valorPrimeiraParcelaOrcamento = valorParcela.add(diferencaCalculoParcelas);
            //     valorParcela = (orcamentoSelecionado.getValorTotalComDesconto()).divide(numeroParcelaOrcamento.subtract(new BigDecimal(1)), 2,
            //            RoundingMode.HALF_UP);
            //  }
            //   }

            if (valorPrimeiraParcelaOrcamento != null && valorPrimeiraParcelaOrcamento.compareTo(new BigDecimal(0)) != 0) {
                valorParcela = orcamentoSelecionado.getValorTotalComDesconto().divide(numeroParcelaOrcamento.add(new BigDecimal(1)), 2, RoundingMode.HALF_UP);
            } else {
                if (numeroParcelaOrcamento.compareTo(new BigDecimal(0)) == 0)
                    numeroParcelaOrcamento = new BigDecimal(1);
                valorParcela = orcamentoSelecionado.getValorTotalComDesconto().divide(numeroParcelaOrcamento, 2, RoundingMode.HALF_UP);
            }

            validaDescontos();
            NegociacaoOrcamentoSingleton.getInstance().criaNovaNegociacao(getOrcamentoSelecionado(), descontosDisponiveis.get(numeroParcelaOrcamento.intValue()), numeroParcelaOrcamento.intValue(),
                    getOrcamentoSelecionado().getDescontoTipo(), getOrcamentoSelecionado().getDescontoValor(), valorPrimeiraParcelaOrcamento, valorParcela, diferencaCalculoParcelas,
                    getOrcamentoSelecionado().getValorTotal(), getOrcamentoSelecionado().getValorTotalComDesconto(), observacoesCobrancaOrcamento, UtilsFrontEnd.getProfissionalLogado());

            try {
                RepasseFaturasSingleton.getInstance().verificaPlanoTratamentoProcedimentoDeOrcamentoRecemAprovado(orcamentoSelecionado, UtilsFrontEnd.getProfissionalLogado(),
                        UtilsFrontEnd.getEmpresaLogada());
            } catch (RepasseNaoPossuiRecebimentoException e) {
                this.addWarn("Aten????o", "N??o foi gerado repasse para os procedimentos executados devido ?? pend??ncia financeira");
            } catch (Exception e) {
                this.addError("Erro", "N??o foi poss??vel calcular o repasse.");
            }
            calculaRepasses();
            //actionNew(event);
            this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");
            PrimeFaces.current().executeScript("PF('dlg').hide()");
            carregaTelaOrcamento(getEntity());
            //this.orcamentoSelecionado = null;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Erro no actionPersist OrcamentoMB", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
        }
    }

    public String getHeaderProcedimentoEdit() {
        String result;
        //if (enableRegioes)
        //   result = "Procedimentos p/ Regi??o";
        //else if (denteSelecionado != null && denteSelecionado.getDescricao() != null && !denteSelecionado.getDescricao().isEmpty())
        //    result = "Procedimentos do Dente " + denteSelecionado.getDescricao().trim();
        //else
        result = "Adicionar procedimento";

        if (pacienteMB != null && pacienteMB.getEntity() != null && pacienteMB.getEntity().getDadosBasico() != null && pacienteMB.getEntity().getDadosBasico().getNome() != null && !pacienteMB.getEntity().getDadosBasico().getNome().trim().isEmpty())
            if (getEntity() != null && getEntity().getDescricao() != null)
                result = "P.T. '" + getEntity().getDescricao().trim() + "' do Paciente " + pacienteMB.getEntity().getDadosBasico().getNome().trim() + (result != null && !result.trim().isEmpty() ? " | " + result : "");
            else
                result = "P.T. do Paciente " + pacienteMB.getEntity().getDadosBasico().getNome().trim() + (result != null && !result.trim().isEmpty() ? " | " + result : "");
        return result;
    }
    //============================================== FINANCEIRO ============================================== //

    //============================================= ODONTOGRAMA ============================================== //
    public void actionAlterarRegiao(PlanoTratamentoProcedimento ptp) {
        try {
            PlanoTratamentoProcedimentoSingleton.getInstance().getBo().persist(ptp);
            this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Erro no actionAlterarRegiao", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
        }
    }

    public void carregaTelaOdontograma() {
        try {
            procedimentosDente = new ArrayList<>();
            setPlanoTratamentoProcedimentosEntity(planoTratamentoProcedimentos, planoTratamentoProcedimentosExcluidos);

            this.odontogramaSelecionado = getEntity().getOdontograma();
            PrimeFaces.current().executeScript("PF('dlgOdontogramaPT').show()");
        } catch (Exception e) {
            e.printStackTrace();
            LogIntelidenteSingleton.getInstance().makeLog(e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
        }
    }

    public String diagnosticosDente(String dente, String extraStyle) {
        try {
            Dente d = DenteSingleton.getInstance().getBo().findByDescAndOdontograma(dente, getEntity().getOdontograma());
            if (d != null) {
                //List<RegiaoDente> regioes = regiaoDenteBO.listByOdontogramaAndDente(getEntity(), d);
                List<RegiaoDente> regioes = d.getRegioes();
                StringBuilder aux = new StringBuilder();
                if (regioes != null && !regioes.isEmpty()) {
                    for (RegiaoDente regiaoDente : regioes) {
                        aux.append(regiaoDente.getStick(extraStyle));
                    }
                    return aux.toString();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
        }
        return "";
    }

    public String diagnosticosDente(PlanoTratamentoProcedimento ptp, String extraStyle) {

        StringBuilder aux = new StringBuilder();

        List<PlanoTratamentoProcedimentoRegiao> listRegioes = new ArrayList<>(ptp.getRegioes());
        for (PlanoTratamentoProcedimentoRegiao planoTratamentoProcedimentoRegiao : listRegioes) {
            if (planoTratamentoProcedimentoRegiao.isAtivo()) {
                if (planoTratamentoProcedimentoRegiao.get() != null && planoTratamentoProcedimentoRegiao.get().getStatusDente() != null && planoTratamentoProcedimentoRegiao.get().getStatusDente().getExcluido().equals(
                        "N")) {
                    if (planoTratamentoProcedimentoRegiao != null && planoTratamentoProcedimentoRegiao.getRegiaoDente() != null) {
                        aux.append(planoTratamentoProcedimentoRegiao.getRegiaoDente().getStick(extraStyle));
                    }

                }

            }
        }
        return aux.toString();

    }

    public void carregaProcedimentos() {
        String strDente = JSFHelper.getRequestParameterMap("dente");
        try {
            enableRegioes = false;
            denteSelecionado = DenteSingleton.getInstance().getBo().findByDescAndOdontograma(strDente, getEntity().getOdontograma());
            if (denteSelecionado == null) {
                denteSelecionado = new Dente(strDente, getEntity().getOdontograma());
                DenteSingleton.getInstance().getBo().persist(denteSelecionado);
            }
            this.dentesEscolhidasOdontograma = new ArrayList<>(Arrays.asList(denteSelecionado.getDescricaoCompleta()));
            carregarProcedimentos(denteSelecionado);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Erro no carregaProcedimentos", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
        }
    }

    private void carregarProcedimentos(Dente dente) throws Exception {
        procedimentosDente = null;
        if (denteSelecionado != null && getEntity().getId() != null) {
            procedimentosDente = PlanoTratamentoProcedimentoSingleton.getInstance().getBo().listByDenteAndPlanoTratamento(denteSelecionado, getEntity());
            for (PlanoTratamentoProcedimento ptp : procedimentosDente) {
                List<String> faces = new ArrayList<>();
                for (PlanoTratamentoProcedimentoFace ptpf : ptp.getPlanoTratamentoProcedimentoFaces()) {
                    if (!faces.contains(ptpf.getFace())) {
                        faces.add(ptpf.getFace());
                    }
                }
                ptp.setFacesSelecionadas(faces);
            }
        }
    }

    public void actionNewOdontograma() {
        try {
            Odontograma odontograma = new Odontograma(Calendar.getInstance().getTime(), this.getPaciente(), "");
            OdontogramaSingleton.getInstance().getBo().persist(odontograma);
            getEntity().setOdontograma(odontograma);
            carregarOdontogramas();
        } catch (Exception e) {
            e.printStackTrace();
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
        }
    }

    public void removeOdontograma() {
        try {
            OdontogramaSingleton.getInstance().excluiOdontograma(getEntity().getOdontograma(), UtilsFrontEnd.getProfissionalLogado());
            getEntity().setOdontograma(null);
            if (odontogramas != null && !odontogramas.isEmpty()) {
                // Pega o mais atual
                getEntity().setOdontograma(odontogramas.get(0));
                List<Odontograma> odontogramasFilter = odontogramas.stream().filter(odontograma -> odontograma.getDataCadastro().before(getEntity().getDataHora())).collect(Collectors.toList());
                // se existir, pega o mais atual considerando como data limite a cria????o do p.t.
                if (odontogramasFilter != null && !odontogramasFilter.isEmpty())
                    getEntity().setOdontograma(odontogramasFilter.get(0));
            }
            carregarOdontogramas();
            this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_REMOVIDO_COM_SUCESSO), "");
        } catch (Exception e) {
            e.printStackTrace();
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_REMOVER_REGISTRO), "");
        }
    }

    public void actionPersistStatusDente(ActionEvent event) {
        try {
            statusDenteSelecionado.setCor("#" + statusDenteSelecionado.getCorPF());
            statusDenteSelecionado.setIdEmpresa(UtilsFrontEnd.getEmpresaLogada().getEmpIntCod());
            StatusDenteSingleton.getInstance().getBo().persist(statusDenteSelecionado);
            statusDenteSelecionado = new StatusDente();
            carregarStatusDente();
            this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");
        } catch (Exception e) {
            e.printStackTrace();
            log.error("actionPersistStatusDente", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
        }

    }

    public void actionNewStatusDente() {
        statusDenteSelecionado = new StatusDente();
    }

    public void editaFacesDiagnostico(PlanoTratamentoProcedimentoRegiao ptpr) {
        this.ptprSelecionado = ptpr;
    }

    public void removeDiagnostico(PlanoTratamentoProcedimentoRegiao ptpr) {
        ptpr.setAtivo(false);
        ptpr.setDataAlteracaoStatus(new Date());
        ptpr.setAlteradoPor(UtilsFrontEnd.getProfissionalLogado());
        ptpr.get().setExcluido("S");
        ptpr.get().setDataExclusao(new Date());
        ptpr.get().setExcluidoPorProfissional(UtilsFrontEnd.getProfissionalLogado().getId());
    }

    public void fechaTelaFacesDiagnostico() {
        this.ptprSelecionado = null;
    }

    public void actionPersistFacesDiagnostico() {
        PrimeFaces.current().executeScript("PF('dlgFacesDetalhesProcedimento').hide()");
    }

    public void handleDenteRegiaoSelected() {
        PlanoTratamentoProcedimentoRegiao.TipoRegiao local = isDenteOrRegiao(denteRegiaoEscolhida);

        try {
            this.denteSelecionado = null;
            this.regiaoSelecionada = null;
            this.enableRegioes = false;

            if (local == PlanoTratamentoProcedimentoRegiao.TipoRegiao.DENTE) {
                String denteDescricao = this.denteRegiaoEscolhida.trim().split("Dente ")[1];
                this.denteSelecionado = DenteSingleton.getInstance().getBo().findByDescAndOdontograma(denteDescricao, getEntity().getOdontograma());
                if (this.denteSelecionado == null)
                    this.denteSelecionado = new Dente(denteDescricao, getEntity().getOdontograma());
            } else if (local == PlanoTratamentoProcedimentoRegiao.TipoRegiao.REGIAO) {
                this.regiaoSelecionada = this.denteRegiaoEscolhida;
                this.enableRegioes = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogIntelidenteSingleton.getInstance().makeLog(e);
            addError("Erro", "Erro ao adicionar diagn??stico!");
        }
    }

    public void cleanDetalhesProcedimento() {
        this.diagnosticos = null;
        this.dentesRegioesEscolhidas = null;
        this.facesEscolhidas = null;
        this.diagnosticosSelecionados = null;
    }

    public void handleDentesRegioesSelects() {
        handleDentesRegioesSelects(false);
    }

    public void handleDentesRegioesSelects(boolean forcePersist) {
        this.dentesSelecionados = new ArrayList<>();
        this.regioesSelecionadas = new ArrayList<>();
        if (dentesRegioesEscolhidas == null)
            return;

        try {
            for (String denteOuRegiao : dentesRegioesEscolhidas) {
                PlanoTratamentoProcedimentoRegiao.TipoRegiao local = isDenteOrRegiao(denteOuRegiao);

                if (local == PlanoTratamentoProcedimentoRegiao.TipoRegiao.DENTE) {
                    String denteDescricao = denteOuRegiao.trim().split("Dente ")[1];
                    Dente denteEscolhido = DenteSingleton.getInstance().getBo().findByDescAndOdontograma(denteDescricao, getEntity().getOdontograma());
                    if (denteEscolhido == null) {
                        denteEscolhido = new Dente(denteDescricao, getEntity().getOdontograma());
                        DenteSingleton.getInstance().getBo().persist(denteEscolhido);
                    }
                    this.dentesSelecionados.add(denteEscolhido);
                } else if (local == PlanoTratamentoProcedimentoRegiao.TipoRegiao.REGIAO) {
                    String regiaoEscolhida = denteOuRegiao;
                    this.regioesSelecionadas.add(regiaoEscolhida);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogIntelidenteSingleton.getInstance().makeLog(e);
            addError("Erro", "Erro ao adicionar diagn??sticos!");
        }
    }

    public void actionAdicionaDiagnosticoDetalhesProcedimento() {
        handleDentesRegioesSelects(true);
        if (this.dentesSelecionados != null) {
            for (int i = 0; i < this.dentesSelecionados.size(); i++) {
                if (this.dentesSelecionados.get(i) == null)
                    continue;
                Dente dente = this.dentesSelecionados.get(i);
                if (dente.getRegioes() != null && !dente.getRegioes().isEmpty() && this.diagnosticosSelecionados != null && !this.diagnosticosSelecionados.isEmpty()) {
                    if (dente.getRegioes().stream().filter(regiao -> !"S".equals(regiao.getExcluido())).filter(regiao -> regiao.getStatusDente() != null).filter(
                            regiao -> this.diagnosticosSelecionados.stream().anyMatch(diag -> diag.getId() == regiao.getStatusDente().getId())).count() > 0) {
                        this.addError("Diagn??stico j?? selecionado para o " + dente.getDescricao(), "");
                        return;
                    }
                    if (dente.getRegioes().stream().filter(regiao -> !"S".equals(regiao.getExcluido())).filter(regiao -> regiao.getStatusDente() != null).count() >= 4) {
                        this.addError("M??ximo 4 Diagn??stico por dente. (" + dente.getDescricao() + ")", "");
                        return;
                    }

                } else {
                    dente.setRegioes(new ArrayList<>());
                }

                if (this.diagnosticosSelecionados != null && !this.diagnosticosSelecionados.isEmpty()) {
                    for (StatusDente sd : this.diagnosticosSelecionados)
                        this.diagnosticos.add(new PlanoTratamentoProcedimentoRegiao(new RegiaoDente(sd, 'C', dente, this.facesEscolhidas), this.planoTratamentoProcedimentoSelecionado));
                } else {
                    this.diagnosticos.add(new PlanoTratamentoProcedimentoRegiao(new RegiaoDente(null, 'C', dente, this.facesEscolhidas), this.planoTratamentoProcedimentoSelecionado));
                }
            }
        }
        if (this.regioesSelecionadas != null) {
            for (int i = 0; i < this.regioesSelecionadas.size(); i++) {
                if (this.regioesSelecionadas.get(i) == null || this.regioesSelecionadas.get(i).trim().isEmpty())
                    continue;
                String regiaoEscolhida = this.regioesSelecionadas.get(i);
                if (getEntity().getOdontograma().getRegioesRegiao() != null && !getEntity().getOdontograma().getRegioesRegiao().isEmpty()) {
                    if (getEntity().getOdontograma().getRegioesRegiao().stream().filter(regiao -> regiao.getRegiao().equals(regiaoEscolhida)).filter(
                            regiao -> !"S".equals(regiao.getExcluido())).filter(regiao -> regiao.getStatusDente() != null).filter(
                                    regiao -> Long.valueOf(regiao.getStatusDente().getId()) == Long.valueOf(statusDenteDenteSelecionado.getId())).count() > 0) {
                        this.addError("Diagn??stico j?? selecionado.", "Regi??o " + regiaoEscolhida);
                        return;
                    }
                    if (getEntity().getOdontograma().getRegioesRegiao().stream().filter(regiao -> regiao.getRegiao().equals(regiaoEscolhida)).filter(
                            regiao -> !"S".equals(regiao.getExcluido())).filter(regiao -> regiao.getStatusDente() != null).count() >= 4) {
                        this.addError("M??ximo 4 Diagn??stico por regi??o.", "Regi??o " + regiaoEscolhida);
                        return;
                    }
                } else {
                    getEntity().getOdontograma().setRegioesRegiao(new ArrayList<>());
                }

                if (this.diagnosticosSelecionados != null && !this.diagnosticosSelecionados.isEmpty()) {
                    for (StatusDente sd : this.diagnosticosSelecionados)
                        this.diagnosticos.add(new PlanoTratamentoProcedimentoRegiao(new RegiaoRegiao(sd, regiaoEscolhida, getEntity().getOdontograma()), this.planoTratamentoProcedimentoSelecionado));
                } else {
                    this.diagnosticos.add(new PlanoTratamentoProcedimentoRegiao(new RegiaoRegiao(null, regiaoEscolhida, getEntity().getOdontograma()), this.planoTratamentoProcedimentoSelecionado));
                }
            }
        } else {
            addError("", "?? necess??rio selecionar um Dente/Regi??o para adicionar!");
        }
    }

    public void actionPersistDetalhesProcedimento() {
        try {
            this.planoTratamentoProcedimentoSelecionado.setRegioes(this.diagnosticos);
            actionPersist(null);

            PrimeFaces.current().executeScript("PF('dlgDetalhesProcedimento').hide()");
            addInfo("", "Registro salvo com sucesso!");
        } catch (Exception e) {
            addError("Erro ao salvar registros!", e.getLocalizedMessage());
        }
    }

    public void actionAdicionarStatusDente() {
        if (statusDenteDenteSelecionado == null) {
            this.addError("Erro", "?? necess??rio escolher um diagn??stico!");
            return;
        }

        try {
            if (!enableRegioes && dentesEscolhidasOdontograma != null && !dentesEscolhidasOdontograma.isEmpty()) {
                for (String denteStr : dentesEscolhidasOdontograma) {
                    String denteDescricao = denteStr.split("Dente ")[1];
                    Dente denteEscolhido = DenteSingleton.getInstance().getBo().findByDescAndOdontograma(denteDescricao, getEntity().getOdontograma());
                    if (denteEscolhido == null) {
                        denteEscolhido = new Dente(denteDescricao, getEntity().getOdontograma());
                    }

                    if (denteEscolhido.getRegioes() != null && !denteEscolhido.getRegioes().isEmpty()) {
                        if (denteEscolhido.getRegioes().stream().filter(regiao -> !"S".equals(regiao.getExcluido())).filter(regiao -> regiao.getStatusDente() != null).filter(
                                regiao -> Long.valueOf(regiao.getStatusDente().getId()) == Long.valueOf(statusDenteDenteSelecionado.getId())).count() > 0) {
                            this.addError("Diagn??stico j?? selecionado.", denteEscolhido.getDescricaoCompleta());
                            return;
                        }
                        if (denteEscolhido.getRegioes().stream().filter(regiao -> !"S".equals(regiao.getExcluido())).filter(regiao -> regiao.getStatusDente() != null).count() >= 4) {
                            this.addError("M??ximo 4 Diagn??stico por dente.", denteEscolhido.getDescricaoCompleta());
                            return;
                        }
                    } else {
                        denteEscolhido.setRegioes(new ArrayList<>());
                    }
                    denteEscolhido.getRegioes().add(new RegiaoDente(statusDenteDenteSelecionado, 'C', denteEscolhido));
                    DenteSingleton.getInstance().getBo().persist(denteEscolhido);
                }
            } else if (!enableRegioes && (dentesEscolhidasOdontograma == null || dentesEscolhidasOdontograma.isEmpty())) {
                this.addError("Escolha ao menos um dente para inserir um diagn??stico.", "");
                return;
            } else if (enableRegioes && regioesEscolhidasOdontograma != null && !regioesEscolhidasOdontograma.isEmpty()) {
                for (String regiaoEscolhida : regioesEscolhidasOdontograma) {
                    if (getEntity().getOdontograma().getRegioesRegiao() != null && !getEntity().getOdontograma().getRegioesRegiao().isEmpty()) {
                        if (getEntity().getOdontograma().getRegioesRegiao().stream().filter(regiao -> regiao.getRegiao().equals(regiaoEscolhida)).filter(
                                regiao -> !"S".equals(regiao.getExcluido())).filter(regiao -> regiao.getStatusDente() != null).filter(
                                        regiao -> Long.valueOf(regiao.getStatusDente().getId()) == Long.valueOf(statusDenteDenteSelecionado.getId())).count() > 0) {
                            this.addError("Diagn??stico j?? selecionado.", "Regi??o " + regiaoEscolhida);
                            return;
                        }
                        if (getEntity().getOdontograma().getRegioesRegiao().stream().filter(regiao -> regiao.getRegiao().equals(regiaoEscolhida)).filter(
                                regiao -> !"S".equals(regiao.getExcluido())).filter(regiao -> regiao.getStatusDente() != null).count() >= 4) {
                            this.addError("M??ximo 4 Diagn??stico por regi??o.", "Regi??o " + regiaoEscolhida);
                            return;
                        }
                    } else {
                        getEntity().getOdontograma().setRegioesRegiao(new ArrayList<>());
                    }
                    getEntity().getOdontograma().getRegioesRegiao().add(new RegiaoRegiao(statusDenteDenteSelecionado, regiaoEscolhida, getEntity().getOdontograma()));
                    OdontogramaSingleton.getInstance().getBo().persist(getEntity().getOdontograma());
                    getEntity().setOdontograma(OdontogramaSingleton.getInstance().getBo().find(getEntity().getOdontograma()));
                }
            } else if (enableRegioes && (regioesEscolhidasOdontograma == null || regioesEscolhidasOdontograma.isEmpty())) {
                this.addError("Escolha ao menos uma regi??o para inserir um diagn??stico.", "");
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Erro no actionPersistRegioes", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
        }
    }

    public void abreSomenteFaces(RegiaoDente rd) {
        this.regiaSomenteFaces = rd;
    }

    public void actionPersistFacesStatusDente() {
        try {
            RegiaoDenteSingleton.getInstance().getBo().persist(regiaSomenteFaces);
            PlanoTratamentoProcedimentoRegiao ptpr = PlanoTratamentoProcedimentoRegiaoSingleton.getInstance().getBo().findByRegiaoDente(regiaSomenteFaces);
            if (ptpr == null) {
                ptpr = new PlanoTratamentoProcedimentoRegiao();
                ptpr.setRegiaoDente(regiaSomenteFaces);
                ptpr.setAtivo(true);
                ptpr.setTipoRegiao(TipoRegiao.DENTE);
            }

            ptpr.setRegiaoDente(regiaSomenteFaces);
            PlanoTratamentoProcedimentoRegiaoSingleton.getInstance().getBo().persist(ptpr);

        } catch (Exception e) {
            e.printStackTrace();
            log.error("Erro no actionPersistFacesStatusDente", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
        }
    }

    public void actionRemoverStatusDente(RegiaoRegiao rd) {
        try {
            if (enableRegioes) {
                rd.setExcluido("S");
                rd.setExcluidoPorProfissional(UtilsFrontEnd.getProfissionalLogado().getId());
                rd.setDataExclusao(new Date());
                RegiaoRegiaoSingleton.getInstance().getBo().persist(rd);
                getEntity().setOdontograma(OdontogramaSingleton.getInstance().getBo().find(getEntity().getOdontograma()));
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Erro no actionRemoverStatusDente", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
        }
    }

    public void actionRemoverStatusDente(RegiaoDente rd) {
        try {
            if (denteSelecionado != null) {
                rd.setExcluido("S");
                rd.setExcluidoPorProfissional(UtilsFrontEnd.getProfissionalLogado().getId());
                rd.setDataExclusao(new Date());
                RegiaoDenteSingleton.getInstance().getBo().persist(rd);
                denteSelecionado = DenteSingleton.getInstance().getBo().find(denteSelecionado);
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Erro no actionRemoverStatusDente", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
        }
    }

    public List<RegiaoRegiao> getDiagnosticosRegiao() {
        try {
            if (getEntity().getOdontograma().getRegioesRegiao() != null && !getEntity().getOdontograma().getRegioesRegiao().isEmpty() && getRegiaoSelecionada() != null && !getRegiaoSelecionada().isEmpty())
                return RegiaoRegiaoSingleton.getInstance().getBo().findByDescAndOdontograma(getRegiaoSelecionada(), getEntity().getOdontograma());
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Erro no getDiagnosticosRegiao", e);
        }
        return null;
    }

    public List<RegiaoDente> getDiagnosticosDentes() {
        try {
            if (this.dentesEscolhidasOdontograma != null)
                return dentesEscolhidasOdontograma.stream().map(dente -> {
                    try {
                        return DenteSingleton.getInstance().getBo().findByDescAndOdontograma(dente.split("Dente ")[1], getEntity().getOdontograma()).getRegioes();
                    } catch (Exception e) {
                        return null;
                    }
                }).filter(regioesDente -> regioesDente != null).flatMap(regioesDente -> regioesDente.stream()).filter(regiaoDente -> !"S".equals(regiaoDente.getExcluido())).collect(
                        Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Erro no getDiagnosticosRegiao", e);
        }
        return null;
    }

    public List<RegiaoRegiao> getDiagnosticosRegioes() {
        try {
            if (this.regioesEscolhidasOdontograma != null)
                return regioesEscolhidasOdontograma.stream().map(regiao -> {
                    try {
                        return RegiaoRegiaoSingleton.getInstance().getBo().findByDescAndOdontograma(regiao, getEntity().getOdontograma());
                    } catch (Exception e) {
                        return new ArrayList<RegiaoRegiao>();
                    }
                }).flatMap(listRegiao -> listRegiao.stream()).collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Erro no getDiagnosticosRegiao", e);
        }
        return null;
    }

    //============================================= ODONTOGRAMA ============================================== //

    //============================================== PERMISSOES ============================================== //
    public boolean isTemPermissaoExtra() {
        if (UtilsFrontEnd.getProfissionalLogado() != null) {
            return temPermissaoExtra(true);
        } else {
            return false;
        }
    }

    public boolean isTemPermissaoTrocarValor() {

        if (UtilsFrontEnd.getProfissionalLogado().isFazOrcamento()) {
            return true;
        }

        if (this.getEntity().isBconvenio() && this.getEntity().getConvenio() != null)
            //  if (this.getEntity().getConvenio().getTipo().equals(Convenio.CONVENIO_PLANO_SAUDE) || UtilsFrontEnd.getProfissionalLogado().getTipoRemuneracao().equals(Profissional.FIXO))

            if (UtilsFrontEnd.getProfissionalLogado().getTipoRemuneracao().equals(Profissional.FIXO))
                return false;
        return temPermissaoExtra(false);
    }

    private boolean temPermissaoExtra(boolean orcamento) {
        if (isDentistaAdmin() || UtilsFrontEnd.getProfissionalLogado().getPerfil().equals(OdontoPerfil.ORCAMENTADOR) || UtilsFrontEnd.getProfissionalLogado().getPerfil().equals(
                OdontoPerfil.ADMINISTRADOR) || UtilsFrontEnd.getProfissionalLogado().getPerfil().equals(OdontoPerfil.RESPONSAVEL_TECNICO) || UtilsFrontEnd.getProfissionalLogado().getPerfil().equals(
                        OdontoPerfil.ADMINISTRADOR_CLINICA) || (orcamento && UtilsFrontEnd.getProfissionalLogado().isFazOrcamento()))
            return true;
        return false;
    }

    public BigDecimal valorAReceber(PlanoTratamento pt) {
        return FaturaItemSingleton.getInstance().valorAReceberDoPacienteFromPT(pt);
    }

    public BigDecimal valorAConferir(PlanoTratamento pt) {
        return FaturaItemSingleton.getInstance().valorAConferirDoPacienteFromPT(pt);
    }

    //============================================== PERMISSOES ============================================== //

    // ================================================= TELA ================================================ //
    @SuppressWarnings("unused")
    private void atualizaRowTable(UIData tabela) {
        atualizaRowTable(tabela, null);
    }

    private void atualizaRowTable(UIData tabela, String idExtra) {
        if (tabela == null)
            return;
        String elementoId = tabela.getClientId() + ":" + tabela.getRowIndex() + (idExtra != null && !idExtra.isEmpty() ? ":" + idExtra : "");
        atualizaJSFElm(elementoId);
    }

    private void atualizaJSFElm(String id) {
        FacesContext.getCurrentInstance().getPartialViewContext().getRenderIds().add(id);
    }

    public String convertValor(BigDecimal v) {
        return String.valueOf(v.doubleValue());
    }

    public boolean validarAlteracao(PlanoTratamentoProcedimento ptp) {
        try {

            if (this.getEntity().isBconvenio()) {

                //   if(!this.getEntity().getConvenio().getTipo().equals(Convenio.CONVENIO_PLANO_SAUDE)) {

                BigDecimal diferenca = new BigDecimal(0);
                BigDecimal valorDesconto = new BigDecimal(0);
                BigDecimal valor = new BigDecimal(0);

                if (ptp.getConvenio() != null && ptp.getProcedimento() != null && ConvenioProcedimentoSingleton.getInstance().isConvenioAtivoEVigente(this.getEntity(), ptp.getProcedimento()))
                    valor = ConvenioProcedimentoSingleton.getInstance().getBo().findByConvenioAndProcedimento(ptp.getConvenio(), ptp.getProcedimento()).getValor();
                else
                    valor = ptp.getProcedimento().getValor();

                diferenca = valor.subtract(ptp.getValorDesconto());

                // if (diferenca.doubleValue() > 0) {
                //    valorDesconto = (diferenca.divide(valor, BigDecimal.ROUND_HALF_UP)).multiply(new BigDecimal(100));

                //  if (valorDesconto.doubleValue() > UtilsFrontEnd.getProfissionalLogado().getDesconto().doubleValue()) {
                //    this.addError("Erro ao salvar registro", "N??o ?? poss??vel dar desconto maior que " + UtilsFrontEnd.getProfissionalLogado().getDesconto().doubleValue() + "% no item " + ptp.getProcedimento().getDescricao());
                //     ptp.setValorDesconto(valor);

                //     return false;
                //  }
                // }

                //   }else {
                //       this.addWarn("Erro ao salvar registro", "N??o ?? poss??vel alterar o valor.");
                //   }
                //    atualizaValorTotal();
                //   this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");

            }

        } catch (Exception e) {
            e.printStackTrace();
            this.addError(Mensagens.ERRO_AO_SALVAR_REGISTRO, "");
        }

        return true;
    }

    public void validarAlteracao(OrcamentoItem oi) {
        try {

            if (this.getEntity().isBconvenio() && this.getEntity().getConvenio() != null) {

                if (!this.getEntity().getConvenio().getTipo().equals(Convenio.CONVENIO_PLANO_SAUDE)) {

                    BigDecimal diferenca = new BigDecimal(0);
                    BigDecimal valorDesconto = new BigDecimal(0);
                    BigDecimal valor = new BigDecimal(0);

                    if (ConvenioProcedimentoSingleton.getInstance().isConvenioAtivoEVigente(this.getEntity(), oi.getOrigemProcedimento().getPlanoTratamentoProcedimento().getProcedimento()))
                        valor = ConvenioProcedimentoSingleton.getInstance().getBo().findByConvenioAndProcedimento(oi.getOrigemProcedimento().getPlanoTratamentoProcedimento().getConvenio(),
                                oi.getOrigemProcedimento().getPlanoTratamentoProcedimento().getProcedimento()).getValor();

                    diferenca = valor.subtract(oi.getValor());

                    if (diferenca.doubleValue() > 0) {
                        valorDesconto = (diferenca.divide(valor, BigDecimal.ROUND_HALF_UP)).multiply(new BigDecimal(100));

                        if (valorDesconto.doubleValue() > UtilsFrontEnd.getProfissionalLogado().getDesconto().doubleValue()) {
                            this.addError("Erro ao salvar registro", "N??o ?? poss??vel dar desconto maior que " + UtilsFrontEnd.getProfissionalLogado().getDesconto().doubleValue() + "%");
                            oi.setValor(valor);

                            return;
                        }
                    }

                } else {
                    this.addWarn("Erro ao salvar registro", "N??o ?? poss??vel alterar o valor.");
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
            this.addError(Mensagens.ERRO_AO_SALVAR_REGISTRO, "");
        }
    }

    @SuppressWarnings("unused")
    private void atualizaRowsTable(UIData tabela) {
        if (tabela == null)
            return;
        for (int i = 0; i < tabela.getRowCount(); i++) {
            String elementoId = tabela.getClientId() + ":" + i;
            FacesContext.getCurrentInstance().getPartialViewContext().getRenderIds().add(elementoId);
        }
    }

    private void carregarProfissionais() throws Exception {
        List<String> perfis = new ArrayList<>();
        perfis.add(OdontoPerfil.DENTISTA);
        perfis.add(OdontoPerfil.ADMINISTRADOR);
        if (UtilsFrontEnd.getProfissionalLogado() != null && UtilsFrontEnd.getProfissionalLogado().getIdEmpresa() != null) {
            profissionais = ProfissionalSingleton.getInstance().getBo().listByEmpresa(perfis, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
        }
    }

    public void carregarPlanoTratamentoProcedimentos() throws Exception {
        if (getEntity() != null && getEntity().getId() != null) {
            this.planoTratamentoProcedimentosExcluidos = new ArrayList<>();
            this.planoTratamentoProcedimentos = PlanoTratamentoProcedimentoSingleton.getInstance().getBo().listByPlanoTratamentoStatus(getEntity().getId(), filtroStatusProcedimento);
            getEntity().setPlanoTratamentoProcedimentos(this.planoTratamentoProcedimentos);

            PrimeFaces.current().ajax().update(":lume:tabViewPaciente:dtProcedimentosSelecionadospt");
        }
    }

    public void validaFecharTela() {
        try {
            setProcedimentosDiferentes(new ArrayList<PlanoTratamentoProcedimento>());
            for (PlanoTratamentoProcedimento ptp : this.getEntity().getPlanoTratamentoProcedimentos()) {
                if (ptp.getStatus() != null) {
                    if ("F".equals(ptp.getStatus())) {
                        PlanoTratamentoProcedimento ptpBanco;
                        ptpBanco = PlanoTratamentoProcedimentoSingleton.getInstance().getBo().find(ptp.getId());
                        if (ptpBanco.getStatus() != null) {
                            if (!(ptp.getStatus().equals(ptpBanco.getStatus()) && ptpBanco.getDentistaExecutor() != null)) {
                                getProcedimentosDiferentes().add(ptp);
                            }
                        } else {
                            getProcedimentosDiferentes().add(ptp);
                        }
                    }
                }
            }
            if (getProcedimentosDiferentes().size() > 0) {
                PrimeFaces.current().executeScript("PF('confirmDialog').show();");
            } else {
                PrimeFaces.current().executeScript("PF('dlgViewPlanoTratamento').hide();");
                carregarPlanosTratamento();
                resetTable();
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    public void resetTable() {
    	DataTable table = (DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent(":lume:tabViewPaciente:dtProcedimentosSelecionadospt");
        table.reset();
    }

    public void resetProcedimentos() {
        try {
            for (PlanoTratamentoProcedimento ptp : this.getProcedimentosDiferentes()) {
                ptp.setStatus(null);
                PlanoTratamentoProcedimentoSingleton.getInstance().getBo().persist(ptp);
            }
            this.setProcedimentosDiferentes(new ArrayList<PlanoTratamentoProcedimento>());
            carregarPlanosTratamento();
            resetTable();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void enableRegioes(boolean enable) {
        try {
            enableRegioes = enable;
            denteSelecionado = null;
            regiaoSelecionada = null;
            carregarProcedimentosComRegiao();
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Erro no carregaProcedimentos", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
        }
    }

    public void carregarOdontogramas() throws Exception {
        if (getPaciente() != null) {
            odontogramas = OdontogramaSingleton.getInstance().getBo().listByPaciente(getPaciente());
        }
    }

    private String carregaDenteCartesiano(int i, int j) {
        try {
            String descricao = String.valueOf(i) + String.valueOf(j);
            Dente dente = DenteSingleton.getInstance().getBo().findByDescAndOdontograma(descricao, getEntity().getOdontograma());
            if (dente == null)
                dente = new Dente(descricao, getEntity().getOdontograma());
            return dente.getDescricaoCompleta();
        } catch (Exception e) {
            return null;
        }
    }

    private void loadDentesERegioesOdontograma() {
        dentesEscolhidasOdontograma = null;
        dentesPossiveisOdontograma = new ArrayList<>();
        for (int i = 1; i <= 4; i++)
            for (int j = 1; j <= 8; j++)
                dentesPossiveisOdontograma.add(carregaDenteCartesiano(i, j));
        for (int i = 5; i <= 8; i++)
            for (int j = 1; j <= 5; j++)
                dentesPossiveisOdontograma.add(carregaDenteCartesiano(i, j));

        regioesEscolhidasOdontograma = null;
        regioesPossiveisOdontograma = new ArrayList<>();
        regioesPossiveisOdontograma.add("Arcada Superior/Inferior");
        regioesPossiveisOdontograma.add("Arcada Superior");
        regioesPossiveisOdontograma.add("Arcada Inferior");
        regioesPossiveisOdontograma.add("Segmento Superior Direito");
        regioesPossiveisOdontograma.add("Segmento Superior Anterior");
        regioesPossiveisOdontograma.add("Segmento Superior Esquerdo");
        regioesPossiveisOdontograma.add("Segmento Inferior Direito");
        regioesPossiveisOdontograma.add("Segmento Inferior Anterior");
        regioesPossiveisOdontograma.add("Segmento Inferior Esquerdo");
        regioesPossiveisOdontograma.add("Seio Maxilar Direito");
        regioesPossiveisOdontograma.add("Seio Maxila Esquerdo");
        regioesPossiveisOdontograma.add("Seio Maxilar Bilateral");
    }

    private void loadDentesCombo() {
        dentes = new ArrayList<SelectItem>();

        SelectItemGroup regioes = new SelectItemGroup("Regi??es");
        regioes.setSelectItems(new SelectItem[] { new SelectItem("Arcada Superior/Inferior"), new SelectItem("Arcada Superior"), new SelectItem("Arcada Inferior"), new SelectItem(
                "Segmento Superior Direito"), new SelectItem("Segmento Superior Anterior"), new SelectItem("Segmento Superior Esquerdo"), new SelectItem(
                        "Segmento Inferior Direito"), new SelectItem("Segmento Inferior Anterior"), new SelectItem(
                                "Segmento Inferior Esquerdo"), new SelectItem("Seio Maxilar Direito"), new SelectItem("Seio Maxila Esquerdo"), new SelectItem("Seio Maxilar Bilateral") });

        SelectItemGroup dentes1118 = new SelectItemGroup("Sequ??ncia de dentes 11 - 18");
        dentes1118.setSelectItems(new SelectItem[] { new SelectItem("Dente 11"), new SelectItem("Dente 12"), new SelectItem("Dente 13"), new SelectItem("Dente 14"), new SelectItem(
                "Dente 15"), new SelectItem("Dente 16"), new SelectItem("Dente 17"), new SelectItem("Dente 18") });

        SelectItemGroup dentes2128 = new SelectItemGroup("Sequ??ncia de dentes 21 - 28");
        dentes2128.setSelectItems(new SelectItem[] { new SelectItem("Dente 21"), new SelectItem("Dente 22"), new SelectItem("Dente 23"), new SelectItem("Dente 24"), new SelectItem(
                "Dente 25"), new SelectItem("Dente 26"), new SelectItem("Dente 27"), new SelectItem("Dente 28") });

        SelectItemGroup dentes3138 = new SelectItemGroup("Sequ??ncia de dentes 31 - 38");
        dentes3138.setSelectItems(new SelectItem[] { new SelectItem("Dente 31"), new SelectItem("Dente 32"), new SelectItem("Dente 33"), new SelectItem("Dente 34"), new SelectItem(
                "Dente 35"), new SelectItem("Dente 36"), new SelectItem("Dente 37"), new SelectItem("Dente 38") });

        SelectItemGroup dentes4148 = new SelectItemGroup("Sequ??ncia de dentes 41 - 48");
        dentes4148.setSelectItems(new SelectItem[] { new SelectItem("Dente 41"), new SelectItem("Dente 42"), new SelectItem("Dente 43"), new SelectItem("Dente 44"), new SelectItem(
                "Dente 45"), new SelectItem("Dente 46"), new SelectItem("Dente 47"), new SelectItem("Dente 48") });

        SelectItemGroup dentes5155 = new SelectItemGroup("Sequ??ncia de dentes 51 - 55");
        dentes5155.setSelectItems(new SelectItem[] { new SelectItem("Dente 51"), new SelectItem("Dente 52"), new SelectItem("Dente 53"), new SelectItem("Dente 54"), new SelectItem("Dente 55") });

        SelectItemGroup dentes6165 = new SelectItemGroup("Sequ??ncia de dentes 61 - 65");
        dentes6165.setSelectItems(new SelectItem[] { new SelectItem("Dente 61"), new SelectItem("Dente 62"), new SelectItem("Dente 63"), new SelectItem("Dente 64"), new SelectItem("Dente 65") });

        SelectItemGroup dentes7175 = new SelectItemGroup("Sequ??ncia de dentes 71 - 75");
        dentes7175.setSelectItems(new SelectItem[] { new SelectItem("Dente 71"), new SelectItem("Dente 72"), new SelectItem("Dente 73"), new SelectItem("Dente 74"), new SelectItem("Dente 75") });

        SelectItemGroup dentes8185 = new SelectItemGroup("Sequ??ncia de dentes 81 - 85");
        dentes8185.setSelectItems(new SelectItem[] { new SelectItem("Dente 81"), new SelectItem("Dente 82"), new SelectItem("Dente 83"), new SelectItem("Dente 84"), new SelectItem("Dente 85") });

        dentes.add(dentes1118);
        dentes.add(dentes2128);
        dentes.add(dentes3138);
        dentes.add(dentes4148);
        dentes.add(dentes5155);
        dentes.add(dentes6165);
        dentes.add(dentes7175);
        dentes.add(dentes8185);
        dentes.add(regioes);
    }

    private void loadGruposDentes() {
        // 18 a 11
        grupoDente1 = this.populaList(11, 18);
        grupoDente2 = this.populaList(21, 28);
        grupoDente3 = this.populaList(31, 38);
        grupoDente4 = this.populaList(41, 48);
        grupoDente5 = this.populaList(51, 55);
        grupoDente6 = this.populaList(61, 65);
        grupoDente7 = this.populaList(71, 75);
        grupoDente8 = this.populaList(81, 85);

        Collections.sort(grupoDente1, Collections.reverseOrder());
        Collections.sort(grupoDente4, Collections.reverseOrder());
        Collections.sort(grupoDente5, Collections.reverseOrder());
        Collections.sort(grupoDente8, Collections.reverseOrder());
    }

    private List<Integer> populaList(int i, int j) {
        List<Integer> l = new ArrayList<>();
        for (Integer x = i; x <= j; x++) {
            l.add(x);
        }
        return l;
    }

    public List<Procedimento> geraSugestoesProcedimento(String query) {
        if (getEntity() != null && getEntity().isBconvenio() && getEntity().getConvenio() != null) {
            return ProcedimentoSingleton.getInstance().getBo().listSugestoesCompleteConvenio(getEntity().getConvenio().getId(), query);
        } else {
            return ProcedimentoSingleton.getInstance().getBo().listSugestoesComplete(query, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
        }
    }

    public void carregarStatusDente() {
        if (UtilsFrontEnd.getProfissionalLogado() != null) {
            HashSet<StatusDente> hashSet = new HashSet<>();
            statusDente = StatusDenteSingleton.getInstance().getBo().listAllTemplate(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
            hashSet.addAll(statusDente);
            statusDenteEmpresa = StatusDenteSingleton.getInstance().getBo().listByEmpresa(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
            hashSet.addAll(statusDenteEmpresa);
            statusDente = new ArrayList<>(hashSet);
            statusDente.sort((o1, o2) -> o1.getDescricao().compareTo(o2.getDescricao()));
        }

    }

    public List<StatusDente> sugestoesStatusDente(String query) {
        return StatusDenteSingleton.getInstance().getBo().listSugestoesStatusDente(query, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
    }

    public void setOrcamentoExclusao(Orcamento orcamento) {
        this.setOrcamentoCancelamento(orcamento);
    }
    // ================================================= TELA ================================================ //

    public void exportarTabela(String type) {
        exportarTabela("Planos de Tratamento", tabelaPlanoTratamento, type);
    }

    public Date getRetorno() {
        return retorno;
    }

    public void setRetorno(Date retorno) {
        this.retorno = retorno;
    }

    public List<Profissional> getProfissionais() {
        return profissionais;
    }

    public void setProfissionais(List<Profissional> profissionais) {
        this.profissionais = profissionais;
    }

    public List<PlanoTratamentoProcedimento> getPlanoTratamentoProcedimentos() {
        return planoTratamentoProcedimentos;
    }

    public void setPlanoTratamentoProcedimentos(List<PlanoTratamentoProcedimento> planoTratamentoProcedimentos) {
        this.planoTratamentoProcedimentos = planoTratamentoProcedimentos;
    }

    public List<Odontograma> getOdontogramas() {
        return odontogramas;
    }

    public void setOdontogramas(List<Odontograma> odontogramas) {
        this.odontogramas = odontogramas;
    }

    public Dominio getJustificativa() {
        return justificativa;
    }

    public void setJustificativa(Dominio justificativa) {
        this.justificativa = justificativa;
    }

    public List<PlanoTratamento> getPlanosTratamento() {
        return planosTratamento;
    }

    public void setPlanosTratamento(List<PlanoTratamento> planosTratamento) {
        this.planosTratamento = planosTratamento;
    }

    public String getNomeClinica() {
        return nomeClinica;
    }

    public void setNomeClinica(String nomeClinica) {
        this.nomeClinica = nomeClinica;
    }

    public String getEndTelefoneClinica() {
        return endTelefoneClinica;
    }

    public void setEndTelefoneClinica(String endTelefoneClinica) {
        this.endTelefoneClinica = endTelefoneClinica;
    }

    public String getDataOrcamento() {
        return Utils.dateToString(Calendar.getInstance().getTime(), "dd/MM/yyyy HH:mm");
    }

    public String getObservacoesRetorno() {
        return observacoesRetorno;
    }

    public void setObservacoesRetorno(String observacoesRetorno) {
        this.observacoesRetorno = observacoesRetorno;
    }

    public PacienteMB getPacienteMB() {
        return this.pacienteMB;
    }

    public void setPacienteMB(PacienteMB pacienteMB) {
        this.pacienteMB = pacienteMB;
    }

    public Paciente getPaciente() {
        if (this.pacienteMB != null)
            return this.pacienteMB.getEntity();
        return null;
    }

    public List<Orcamento> getOrcamentos() {
        return orcamentos;
    }

    public void setOrcamentos(List<Orcamento> orcamentos) {
        this.orcamentos = orcamentos;
    }

    public Orcamento getOrcamentoSelecionado() {
        return orcamentoSelecionado;
    }

    public void setOrcamentoSelecionado(Orcamento orcamentoSelecionado) {
        this.orcamentoSelecionado = orcamentoSelecionado;
    }

    public List<SelectItem> getDentes() {
        return dentes;
    }

    public void setDentes(List<SelectItem> dentes) {
        this.dentes = dentes;
    }

    public void setJustificativas(List<Dominio> justificativas) {
        this.justificativas = justificativas;
    }

    public List<Dominio> getJustificativas() {
        return justificativas;
    }

    public DualListModel<PlanoTratamentoProcedimento> getPtProcedimentosDisponiveis() {
        return ptProcedimentosDisponiveis;
    }

    public void setPtProcedimentosDisponiveis(DualListModel<PlanoTratamentoProcedimento> ptProcedimentosDisponiveis) {
        this.ptProcedimentosDisponiveis = ptProcedimentosDisponiveis;
    }

    public PlanoTratamentoProcedimento getPlanoTratamentoProcedimentoSelecionado() {
        return planoTratamentoProcedimentoSelecionado;
    }

    public void setPlanoTratamentoProcedimentoSelecionado(PlanoTratamentoProcedimento planoTratamentoProcedimentoSelecionado) {
        this.planoTratamentoProcedimentoSelecionado = planoTratamentoProcedimentoSelecionado;
    }

    public List<Integer> getGrupoDente1() {
        return grupoDente1;
    }

    public void setGrupoDente1(List<Integer> grupoDente1) {
        this.grupoDente1 = grupoDente1;
    }

    public List<Integer> getGrupoDente2() {
        return grupoDente2;
    }

    public void setGrupoDente2(List<Integer> grupoDente2) {
        this.grupoDente2 = grupoDente2;
    }

    public List<Integer> getGrupoDente3() {
        return grupoDente3;
    }

    public void setGrupoDente3(List<Integer> grupoDente3) {
        this.grupoDente3 = grupoDente3;
    }

    public List<Integer> getGrupoDente4() {
        return grupoDente4;
    }

    public void setGrupoDente4(List<Integer> grupoDente4) {
        this.grupoDente4 = grupoDente4;
    }

    public List<Integer> getGrupoDente5() {
        return grupoDente5;
    }

    public void setGrupoDente5(List<Integer> grupoDente5) {
        this.grupoDente5 = grupoDente5;
    }

    public List<Integer> getGrupoDente6() {
        return grupoDente6;
    }

    public void setGrupoDente6(List<Integer> grupoDente6) {
        this.grupoDente6 = grupoDente6;
    }

    public List<Integer> getGrupoDente7() {
        return grupoDente7;
    }

    public void setGrupoDente7(List<Integer> grupoDente7) {
        this.grupoDente7 = grupoDente7;
    }

    public List<Integer> getGrupoDente8() {
        return grupoDente8;
    }

    public void setGrupoDente8(List<Integer> grupoDente8) {
        this.grupoDente8 = grupoDente8;
    }

    public boolean isEnableRegioes() {
        return enableRegioes;
    }

    public void setEnableRegioes(boolean enableRegioes) {
        this.enableRegioes = enableRegioes;
    }

    public Dente getDenteSelecionado() {
        return denteSelecionado;
    }

    public void setDenteSelecionado(Dente denteSelecionado) {
        this.denteSelecionado = denteSelecionado;
    }

    public List<PlanoTratamentoProcedimento> getProcedimentosDente() {
        return procedimentosDente;
    }

    public void setProcedimentosDente(List<PlanoTratamentoProcedimento> procedimentosDente) {
        this.procedimentosDente = procedimentosDente;
    }

    public Procedimento getProcedimentoSelecionado() {
        return procedimentoSelecionado;
    }

    public void setProcedimentoSelecionado(Procedimento procedimentoSelecionado) {
        this.procedimentoSelecionado = procedimentoSelecionado;
    }

    public List<StatusDente> getStatusDenteEmpresa() {
        return statusDenteEmpresa;
    }

    public void setStatusDenteEmpresa(List<StatusDente> statusDenteEmpresa) {
        this.statusDenteEmpresa = statusDenteEmpresa;
    }

    public StatusDente getStatusDenteSelecionado() {
        return statusDenteSelecionado;
    }

    public void setStatusDenteSelecionado(StatusDente statusDenteSelecionado) {
        this.statusDenteSelecionado = statusDenteSelecionado;
    }

    public List<StatusDente> getStatusDente() {
        return statusDente;
    }

    public void setStatusDente(List<StatusDente> statusDente) {
        this.statusDente = statusDente;
    }

    public StatusDente getStatusDenteDenteSelecionado() {
        return statusDenteDenteSelecionado;
    }

    public void setStatusDenteDenteSelecionado(StatusDente statusDenteDenteSelecionado) {
        this.statusDenteDenteSelecionado = statusDenteDenteSelecionado;
    }

    public String getRegiaoSelecionada() {
        return regiaoSelecionada;
    }

    public void setRegiaoSelecionada(String regiaoSelecionada) {
        this.regiaoSelecionada = regiaoSelecionada;
    }

    public void setPlanoTratamentoProcedimentosEntity(List<PlanoTratamentoProcedimento> procedimentos, List<PlanoTratamentoProcedimento> excluidos) {
        List<PlanoTratamentoProcedimento> procedimentosTotais = new ArrayList<>();
        for (PlanoTratamentoProcedimento ptp : procedimentos)
            procedimentosTotais.add(ptp);
        for (PlanoTratamentoProcedimento ptp : excluidos)
            procedimentosTotais.add(ptp);
        getEntity().setPlanoTratamentoProcedimentos(procedimentosTotais);
    }

    public List<Dominio> getJustificativasCancelamento() {
        return justificativasCancelamento;
    }

    public void setJustificativasCancelamento(List<Dominio> justificativasCancelamento) {
        this.justificativasCancelamento = justificativasCancelamento;
    }

    public DataTable getTabelaPlanoTratamento() {
        return tabelaPlanoTratamento;
    }

    public void setTabelaPlanoTratamento(DataTable tabelaPlanoTratamento) {
        this.tabelaPlanoTratamento = tabelaPlanoTratamento;
    }

    public Odontograma getOdontogramaSelecionado() {
        return odontogramaSelecionado;
    }

    public void setOdontogramaSelecionado(Odontograma odontogramaSelecionado) {
        this.odontogramaSelecionado = odontogramaSelecionado;
    }

    public String getDescricaoEvolucao() {
        return descricaoEvolucao;
    }

    public void setDescricaoEvolucao(String descricaoEvolucao) {
        this.descricaoEvolucao = descricaoEvolucao;
    }

    public List<PlanoTratamentoProcedimento> getPtps2Finalizar() {
        return ptps2Finalizar;
    }

    public void setPtps2Finalizar(List<PlanoTratamentoProcedimento> ptps2Finalizar) {
        this.ptps2Finalizar = ptps2Finalizar;
    }

    public String getDenteRegiaoEscolhida() {
        return denteRegiaoEscolhida;
    }

    public void setDenteRegiaoEscolhida(String denteRegiaoEscolhida) {
        this.denteRegiaoEscolhida = denteRegiaoEscolhida;
    }

    public String getFiltroStatus() {
        return filtroStatus;
    }

    public void setFiltroStatus(String filtroStatus) {
        this.filtroStatus = filtroStatus;
    }

    public boolean isNovoPtDialogAberto() {
        return novoPtDialogAberto;
    }

    public void abreNovoPtDialog() {
        this.novoPtDialogAberto = true;
    }

    public void fechaNovoPtDialog() {
        this.novoPtDialogAberto = true;
    }

    public String getFiltroTipo() {
        return filtroTipo;
    }

    public void setFiltroTipo(String filtroTipo) {
        this.filtroTipo = filtroTipo;
    }

    public String getFiltroStatusProcedimento() {
        return filtroStatusProcedimento;
    }

    public void setFiltroStatusProcedimento(String filtroStatusProcedimento) {
        this.filtroStatusProcedimento = filtroStatusProcedimento;
    }

    public OrcamentoPlanejamento getPlanejamentoAtual() {
        return planejamentoAtual;
    }

    public void setPlanejamentoAtual(OrcamentoPlanejamento planejamentoAtual) {
        this.planejamentoAtual = planejamentoAtual;
    }

    public List<Dominio> getFormasPagamentoNewPlanejamento() {
        return formasPagamentoNewPlanejamento;
    }

    public void setFormasPagamentoNewPlanejamento(List<Dominio> formasPagamentoNewPlanejamento) {
        this.formasPagamentoNewPlanejamento = formasPagamentoNewPlanejamento;
    }

    public List<Tarifa> getTarifasNewPlanejamento() {
        return tarifasNewPlanejamento;
    }

    public void setTarifasNewPlanejamento(List<Tarifa> tarifasNewPlanejamento) {
        this.tarifasNewPlanejamento = tarifasNewPlanejamento;
    }

    public List<Integer> getParcelasNewPlanejamento() {
        return parcelasNewPlanejamento;
    }

    public void setParcelasNewPlanejamento(List<Integer> parcelasNewPlanejamento) {
        this.parcelasNewPlanejamento = parcelasNewPlanejamento;
    }

    public BigDecimal getValorDescontoAlterar() {
        return valorDescontoAlterar;
    }

    public void setValorDescontoAlterar(BigDecimal valorDescontoAlterar) {
        this.valorDescontoAlterar = valorDescontoAlterar;
    }

    public boolean isImprimirSemValores() {
        return imprimirSemValores;
    }

    public void setImprimirSemValores(boolean imprimirSemValores) {
        this.imprimirSemValores = imprimirSemValores;
    }

    public boolean isRenderizarValoresProcedimentos() {
        return renderizarValoresProcedimentos;
    }

    public void setRenderizarValoresProcedimentos(boolean renderizarValoresProcedimentos) {
        this.renderizarValoresProcedimentos = renderizarValoresProcedimentos;
    }

    public List<Profissional> getProfissionaisFinalizarNovamente() {
        return profissionaisFinalizarNovamente;
    }

    public void setProfissionaisFinalizarNovamente(List<Profissional> profissionaisFinalizarNovamente) {
        this.profissionaisFinalizarNovamente = profissionaisFinalizarNovamente;
    }

    public Profissional getProfissionalFinalizarNovamente() {
        return profissionalFinalizarNovamente;
    }

    public void setProfissionalFinalizarNovamente(Profissional profissionalFinalizarNovamente) {
        this.profissionalFinalizarNovamente = profissionalFinalizarNovamente;
    }

    public PlanoTratamentoProcedimento getPtpMudarExecutor() {
        return ptpMudarExecutor;
    }

    public void setPtpMudarExecutor(PlanoTratamentoProcedimento ptpMudarExecutor) {
        this.ptpMudarExecutor = ptpMudarExecutor;
    }

    public String getObservacoesCobrancaOrcamento() {
        return observacoesCobrancaOrcamento;
    }

    public void setObservacoesCobrancaOrcamento(String observacoesCobrancaOrcamento) {
        this.observacoesCobrancaOrcamento = observacoesCobrancaOrcamento;
    }

    public Integer getQuantidadeVezesNegociacaoOrcamento() {
        return quantidadeVezesNegociacaoOrcamento;
    }

    public void setQuantidadeVezesNegociacaoOrcamento(Integer quantidadeVezesNegociacaoOrcamento) {
        this.quantidadeVezesNegociacaoOrcamento = quantidadeVezesNegociacaoOrcamento;
    }

    public BigDecimal getValorPrimeiraParcelaOrcamento() {
        return valorPrimeiraParcelaOrcamento;
    }

    public void setValorPrimeiraParcelaOrcamento(BigDecimal valorPrimeiraParcelaOrcamento) {
        this.valorPrimeiraParcelaOrcamento = valorPrimeiraParcelaOrcamento;
    }

    public String getMensagemCalculoOrcamento() {
        return mensagemCalculoOrcamento;
    }

    public void setMensagemCalculoOrcamento(String mensagemCalculoOrcamento) {
        this.mensagemCalculoOrcamento = mensagemCalculoOrcamento;
    }

    public BigDecimal getNumeroParcelaOrcamento() {
        return numeroParcelaOrcamento;
    }

    public void setNumeroParcelaOrcamento(BigDecimal numeroParcelaOrcamento) {
        this.numeroParcelaOrcamento = numeroParcelaOrcamento;
    }

//    public String getMensagemCalculoOrcamentoDiferenca() {
//        return mensagemCalculoOrcamentoDiferenca;
//    }
//
//    public void setMensagemCalculoOrcamentoDiferenca(String mensagemCalculoOrcamentoDiferenca) {
//        this.mensagemCalculoOrcamentoDiferenca = mensagemCalculoOrcamentoDiferenca;
//    }

    public BigDecimal getValorParcela() {
        return valorParcela;
    }

    public void setValorParcela(BigDecimal valorParcela) {
        this.valorParcela = valorParcela;
    }

    public List<Integer> getParcelasDisponiveis() {
        return parcelasDisponiveis;
    }

    public void setParcelasDisponiveis(List<Integer> parcelasDisponiveis) {
        this.parcelasDisponiveis = parcelasDisponiveis;
    }

    public Map<Integer, DescontoOrcamento> getDescontosDisponiveis() {
        return descontosDisponiveis;
    }

    public void setDescontosDisponiveis(Map<Integer, DescontoOrcamento> descontosDisponiveis) {
        this.descontosDisponiveis = descontosDisponiveis;
    }

    public BigDecimal getDiferencaCalculoParcelas() {
        return diferencaCalculoParcelas;
    }

    public void setDiferencaCalculoParcelas(BigDecimal diferencaCalculoParcelas) {
        this.diferencaCalculoParcelas = diferencaCalculoParcelas;
    }

    public boolean isIncluirObservacoesCobranca() {
        return incluirObservacoesCobranca;
    }

    public void setIncluirObservacoesCobranca(boolean incluirObservacoesCobranca) {
        this.incluirObservacoesCobranca = incluirObservacoesCobranca;
    }

    public boolean isOmitirProcedimentosNaoInclusos() {
        return omitirProcedimentosNaoInclusos;
    }

    public void setOmitirProcedimentosNaoInclusos(boolean omitirProcedimentosNaoInclusos) {
        this.omitirProcedimentosNaoInclusos = omitirProcedimentosNaoInclusos;
    }

    public PlanoTratamentoLazyModel getPtLazyModel() {
        return ptLazyModel;
    }

    public void setPtLazyModel(PlanoTratamentoLazyModel ptLazyModel) {
        this.ptLazyModel = ptLazyModel;
    }

    public boolean isOmitirDadosEmpresa() {
        return omitirDadosEmpresa;
    }

    public void setOmitirDadosEmpresa(boolean omitirDadosEmpresa) {
        this.omitirDadosEmpresa = omitirDadosEmpresa;
    }

    public boolean isOmitirLogo() {
        return omitirLogo;
    }

    public void setOmitirLogo(boolean omitirLogo) {
        this.omitirLogo = omitirLogo;
    }

    public List<Convenio> getConveniosDisponiveis() {
        return conveniosDisponiveis;
    }

    public void setConveniosDisponiveis(List<Convenio> conveniosDisponiveis) {
        this.conveniosDisponiveis = conveniosDisponiveis;
    }

    public Boolean getPtpInserirMuitasVezes() {
        return ptpInserirMuitasVezes;
    }

    public void setPtpInserirMuitasVezes(Boolean ptpInserirMuitasVezes) {
        this.ptpInserirMuitasVezes = ptpInserirMuitasVezes;
    }

    public Integer getPtpInserirQuantasVezes() {
        return ptpInserirQuantasVezes;
    }

    public void setPtpInserirQuantasVezes(Integer ptpInserirQuantasVezes) {
        this.ptpInserirQuantasVezes = ptpInserirQuantasVezes;
    }

    public List<Dente> getDentesSelecionados() {
        return dentesSelecionados;
    }

    public void setDentesSelecionados(List<Dente> dentesSelecionados) {
        this.dentesSelecionados = dentesSelecionados;
    }

    public List<String> getRegioesSelecionadas() {
        return regioesSelecionadas;
    }

    public void setRegioesSelecionadas(List<String> regioesSelecionadas) {
        this.regioesSelecionadas = regioesSelecionadas;
    }

    public List<String> getDentesRegioesEscolhidas() {
        return dentesRegioesEscolhidas;
    }

    public void setDentesRegioesEscolhidas(List<String> dentesRegioesEscolhidas) {
        this.dentesRegioesEscolhidas = dentesRegioesEscolhidas;
    }

    public List<StatusDente> getDiagnosticosSelecionados() {
        return diagnosticosSelecionados;
    }

    public void setDiagnosticosSelecionados(List<StatusDente> diagnosticosSelecionados) {
        this.diagnosticosSelecionados = diagnosticosSelecionados;
    }

    public List<PlanoTratamentoProcedimentoRegiao> getDiagnosticos() {
        return diagnosticos;
    }

    public List<PlanoTratamentoProcedimentoRegiao> getDiagnosticosLimpo() {
        if (diagnosticos == null)
            return diagnosticos;
        return diagnosticos.stream().sorted().filter(diag -> diag.isAtivo()).collect(Collectors.toList());
    }

    public void setDiagnosticos(List<PlanoTratamentoProcedimentoRegiao> diagnosticos) {
        this.diagnosticos = diagnosticos;
    }

    public List<String> getFacesEscolhidas() {
        return facesEscolhidas;
    }

    public void setFacesEscolhidas(List<String> facesEscolhidas) {
        this.facesEscolhidas = facesEscolhidas;
    }

    public PlanoTratamentoProcedimentoRegiao getPtprSelecionado() {
        return ptprSelecionado;
    }

    public void setPtprSelecionado(PlanoTratamentoProcedimentoRegiao ptprSelecionado) {
        this.ptprSelecionado = ptprSelecionado;
    }

    public List<String> getRegioesPossiveisOdontograma() {
        return regioesPossiveisOdontograma;
    }

    public void setRegioesPossiveisOdontograma(List<String> regioesPossiveisOdontograma) {
        this.regioesPossiveisOdontograma = regioesPossiveisOdontograma;
    }

    public List<String> getRegioesEscolhidasOdontograma() {
        return regioesEscolhidasOdontograma;
    }

    public void setRegioesEscolhidasOdontograma(List<String> regioesEscolhidasOdontograma) {
        this.regioesEscolhidasOdontograma = regioesEscolhidasOdontograma;
    }

    public List<String> getDentesPossiveisOdontograma() {
        return dentesPossiveisOdontograma;
    }

    public void setDentesPossiveisOdontograma(List<String> dentesPossiveisOdontograma) {
        this.dentesPossiveisOdontograma = dentesPossiveisOdontograma;
    }

    public List<String> getDentesEscolhidasOdontograma() {
        return dentesEscolhidasOdontograma;
    }

    public void setDentesEscolhidasOdontograma(List<String> dentesEscolhidasOdontograma) {
        this.dentesEscolhidasOdontograma = dentesEscolhidasOdontograma;
    }

    public String getMotivoReativacao() {
        return motivoReativacao;
    }

    public void setMotivoReativacao(String motivoReativacao) {
        this.motivoReativacao = motivoReativacao;
    }

    public BigDecimal getValorProc() {
        return valorProc;
    }

    public void setValorProc(BigDecimal valorProc) {
        this.valorProc = valorProc;
    }

    public String getObservacoesDetalhes() {
        return observacoesDetalhes;
    }

    public void setObservacoesDetalhes(String observacoesDetalhes) {
        this.observacoesDetalhes = observacoesDetalhes;
    }

    public String getObservacoesAdicionarProcedimento() {
        return observacoesAdicionarProcedimento;
    }

    public void setObservacoesAdicionarProcedimento(String observacoesAdicionarProcedimento) {
        this.observacoesAdicionarProcedimento = observacoesAdicionarProcedimento;
    }

    public boolean isShowProcedimentosCancelados() {
        return showProcedimentosCancelados;
    }

    public void setShowProcedimentosCancelados(boolean showProcedimentosCancelados) {
        this.showProcedimentosCancelados = showProcedimentosCancelados;
    }

    public List<OrcamentoItem> getProcedimentosOrcSelecionado() {
        return procedimentosOrcSelecionado;
    }

    public void setProcedimentosOrcSelecionado(List<OrcamentoItem> procedimentosOrcSelecionado) {
        this.procedimentosOrcSelecionado = procedimentosOrcSelecionado;
    }

    public String getMensagemErroExclusaoProcedimento() {
        return mensagemErroExclusaoProcedimento;
    }

    public void setMensagemErroExclusaoProcedimento(String mensagemErroExclusaoProcedimento) {
        this.mensagemErroExclusaoProcedimento = mensagemErroExclusaoProcedimento;
    }

    public Orcamento getOrcamentoDataAprovacao() {
        return orcamentoDataAprovacao;
    }

    public void setOrcamentoDataAprovacao(Orcamento orcamentoDataAprovacao) {
        this.orcamentoDataAprovacao = orcamentoDataAprovacao;
    }

    public Date getNovaDataAprovacao() {
        return novaDataAprovacao;
    }

    public void setNovaDataAprovacao(Date novaDataAprovacao) {
        this.novaDataAprovacao = novaDataAprovacao;
    }

    public RegiaoDente getRegiaSomenteFaces() {
        return regiaSomenteFaces;
    }

    public void setRegiaSomenteFaces(RegiaoDente regiaSomenteFaces) {
        this.regiaSomenteFaces = regiaSomenteFaces;
    }

    public boolean isEncerrarPlano() {
        return encerrarPlano;
    }

    public void setEncerrarPlano(boolean encerrarPlano) {
        this.encerrarPlano = encerrarPlano;
    }

    public Orcamento getOrcamentoCancelamento() {
        return orcamentoCancelamento;
    }

    public void setOrcamentoCancelamento(Orcamento orcamentoCancelamento) {
        this.orcamentoCancelamento = orcamentoCancelamento;
    }

    public String getJustificativaCancelamento() {
        return justificativaCancelamento;
    }

    public void setJustificativaCancelamento(String justificativaCancelamento) {
        this.justificativaCancelamento = justificativaCancelamento;
    }

    public List<Orcamento> getOrcamentosParaReprovar() {
        return orcamentosParaReprovar;
    }

    public void setOrcamentosParaReprovar(List<Orcamento> orcamentosParaReprovar) {
        this.orcamentosParaReprovar = orcamentosParaReprovar;
    }

    public List<Orcamento> getOrcamentosPendentes() {
        return orcamentosPendentes;
    }

    public void setOrcamentosPendentes(List<Orcamento> orcamentosPendentes) {
        this.orcamentosPendentes = orcamentosPendentes;
    }

	public List<PlanoTratamentoProcedimento> getProcedimentosDiferentes() {
		return procedimentosDiferentes;
	}

	public void setProcedimentosDiferentes(List<PlanoTratamentoProcedimento> procedimentosDiferentes) {
		this.procedimentosDiferentes = procedimentosDiferentes;
	}
}
