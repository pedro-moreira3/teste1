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
import java.util.List;
import java.util.Locale;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

import org.primefaces.PrimeFaces;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.StreamedContent;

import br.com.lume.categoriaMotivo.CategoriaMotivoSingleton;
import br.com.lume.common.OdontoPerfil;
import br.com.lume.common.bo.IEnumController;
import br.com.lume.common.exception.business.CancelarItemProcedimentoAtivo;
import br.com.lume.common.exception.business.CancelarItemProcedimentoExecutado;
import br.com.lume.common.log.LogIntelidenteSingleton;
import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.FormaPagamento;
import br.com.lume.common.util.JSFHelper;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.TooltipHelper;
import br.com.lume.common.util.Utils;
import br.com.lume.common.util.Utils.ValidacaoLancamento;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.common.util.UtilsPadraoRelatorio;
import br.com.lume.common.util.UtilsPadraoRelatorio.PeriodoBusca;
import br.com.lume.conta.ContaSingleton;
import br.com.lume.conta.ContaSingleton.TIPO_CONTA;
import br.com.lume.dadosBasico.DadosBasicoSingleton;
import br.com.lume.dadosBasico.DadosBasicoSingleton.TipoPessoa;
import br.com.lume.descontoOrcamento.DescontoOrcamentoSingleton;
import br.com.lume.documento.DocumentoSingleton;
import br.com.lume.documentoEmitido.DocumentoEmitidoSingleton;
import br.com.lume.dominio.DominioSingleton;
import br.com.lume.faturamento.FaturaItemSingleton;
import br.com.lume.faturamento.FaturaSingleton;
import br.com.lume.fornecedor.FornecedorSingleton;
import br.com.lume.lancamento.LancamentoSingleton;
import br.com.lume.lancamento.objects.LancamentoParcelaInfo;
import br.com.lume.lancamentoContabil.LancamentoContabilSingleton;
import br.com.lume.motivo.MotivoSingleton;
import br.com.lume.negociacao.NegociacaoFaturaSingleton;
import br.com.lume.odonto.entity.CategoriaMotivo;
import br.com.lume.odonto.entity.Conta;
import br.com.lume.odonto.entity.DadosBasico;
import br.com.lume.odonto.entity.DescontoOrcamento;
import br.com.lume.odonto.entity.Documento;
import br.com.lume.odonto.entity.DocumentoEmitido;
import br.com.lume.odonto.entity.Dominio;
import br.com.lume.odonto.entity.Fatura;
import br.com.lume.odonto.entity.Fatura.DirecaoFatura;
import br.com.lume.odonto.entity.Fatura.StatusFatura;
import br.com.lume.odonto.entity.Fatura.SubStatusFatura;
import br.com.lume.odonto.entity.Fatura.TipoFatura;
import br.com.lume.odonto.entity.FaturaItem;
import br.com.lume.odonto.entity.Lancamento;
import br.com.lume.odonto.entity.Lancamento.StatusLancamento;
import br.com.lume.odonto.entity.LancamentoContabil;
import br.com.lume.odonto.entity.Motivo;
import br.com.lume.odonto.entity.NegociacaoFatura;
import br.com.lume.odonto.entity.Paciente;
import br.com.lume.odonto.entity.PlanoTratamento;
import br.com.lume.odonto.entity.PlanoTratamentoProcedimento;
import br.com.lume.odonto.entity.Profissional;
import br.com.lume.odonto.entity.RepasseFaturasLancamento;
import br.com.lume.odonto.entity.Requisito;
import br.com.lume.odonto.entity.Tarifa;
import br.com.lume.odonto.entity.TipoCategoria;
import br.com.lume.odonto.util.OdontoMensagens;
import br.com.lume.paciente.PacienteSingleton;
import br.com.lume.planoTratamento.PlanoTratamentoSingleton;
import br.com.lume.planoTratamentoProcedimento.PlanoTratamentoProcedimentoSingleton;
import br.com.lume.profissional.ProfissionalSingleton;
import br.com.lume.repasse.RepasseFaturasItemSingleton;
import br.com.lume.repasse.RepasseFaturasLancamentoSingleton;
import br.com.lume.tarifa.TarifaSingleton;
import br.com.lume.tipoCategoria.TipoCategoriaSingleton;

/**
 * @author ricardo.poncio
 */
@ManagedBean
@ViewScoped
public class FaturaPagtoMB extends LumeManagedBean<Fatura> {

    private static final long serialVersionUID = 1L;
    //private Logger log = Logger.getLogger(FaturaPagtoMB.class);

    private String filtroPeriodo;
    private Date inicio, fim;
    private Paciente paciente;
    private List<Dominio> formasPagamento;
    private PlanoTratamento[] ptSelecionados = new PlanoTratamento[] {};
    private List<PlanoTratamento> listaPt;
    private boolean showLancamentosCancelados = false;
    private boolean showLancamentosEstorno = false;
    private boolean showItensCancelados = false;

    private StatusFatura status;
    private List<StatusFatura> listaStatus;
    private SubStatusFatura[] subStatusFatura;

    private FaturaItem itemSelecionado;
    private List<Profissional> profissionais;
    private Profissional profissionalTroca;
    private String observacao;

    private BigDecimal somaTotal = BigDecimal.ZERO, somaTotalPago = BigDecimal.ZERO, somaTotalNaoPago = BigDecimal.ZERO, somaTotalNaoPlanejado = BigDecimal.ZERO;

    private String faturaViewImportName;

    //Campos para 'Novo Lançamento'
    private boolean showProduto;
    private String formaPagamento;
    private BigDecimal valor;
    private Date dataPagamento, dataCredito;
    private Tarifa tarifa;
    private List<Tarifa> tarifas = new ArrayList<>();
    private List<Integer> parcelas;
    private Integer parcela;

    //Campos para 'Editar Lançamento'
    private Lancamento editarLancamento;
    private List<Tarifa> editarLancamentoFormasDisponiveis;

    //Campos para 'Ajustar Lançamento'    
    private Lancamento ajustarLancamento;
    private Lancamento extornarLancamento;

    //Campos novos para 'Novo Lançamento'
    private List<Integer> novoLancamentoParcelasDisponiveis;
    private List<Tarifa> novoLancamentoTarifasDisponiveis, novoLancamentoTarifasDisponiveisEEntrada;
    private Integer novoLancamentoQuantidadeParcelas;
    private Tarifa novoLancamentoFormaPagamento;
    private BigDecimal novoLancamentoValorTotal;
    private BigDecimal novoLancamentoValorDaParcela;
    private BigDecimal novoLancamentoValorDaPrimeiraParcela;
    private BigDecimal novoLancamentoValorDaPrimeiraParcelaDiferenca;
    private Date novoLancamentoDataPagamento, novoLancamentoDataCredito;
    private String novoLancamentoTooltipNegociacaoTarifasDisponiveis;
    private List<LancamentoParcelaInfo> novoLancamentoParcelas;
    private String novoLancamentoMensagemCalculo, novoLancamentoMensagemErroCalculo;

    //Campos para lançamentos a pagar e a receber da aba financeiro do paciente
    private List<Lancamento> lAPagar, lAReceber;

    //Campos para 'Nova negociação'
    private List<Tarifa> tarifasDisponiveis;
    private List<Integer> parcelasDisponiveis;
    private HashMap<Integer, DescontoOrcamento> descontosDisponiveis;
    private Integer negociacaoQuantidadeParcelas;
    private String negociacaoTipoDesconto;
    private BigDecimal negociacaoValorDesconto;
    private BigDecimal negociacaoValorDaPrimeiraParcela;
    private BigDecimal negociacaoValorDaPrimeiraParcelaDirefenca;
    private BigDecimal negociacaoValorDaParcela;
    private BigDecimal negociacaoValorTotal, negociacaoValorTotal1Parcela, negociacaoValorTotalDemaisParcelas;
    private String negociacaoObservacao;
    //Campos para 'Confirma negociação'
    private String tooltipNegociacaoTarifasDisponiveis;
    private List<Tarifa> negociacaoTarifasDisponiveis, negociacaoTarifasDisponiveisEEntrada;
    private Tarifa negociacaoFormaPagamento, negociacaoFormaPagamento1Parcela, negociacaoFormaPagamentoDemaisParcelas;
    private Date negociacaoDataPagamento, negociacaoDataPagamento1Parcela, negociacaoDataPagamentoDemaisParcelas;
    private Date negociacaoDataCredito, negociacaoDataCredito1Parcela, negociacaoDataCreditoDemaisParcelas;
    private NegociacaoFatura negociacaoConfirmacao;
    private List<LancamentoParcelaInfo> negociacaoParcelas;
    private String negociacaoMensagemCalculo, negociacaoMensagemErroCalculo;

    private BigDecimal valorAlteracaoItem;
    private FaturaItem itemAlteracao;

    //EXPORTAÇÃO TABELA
    private DataTable tabelaFatura;

    private DadosBasico origem;

    private LancamentoContabil lancamentoContabilEditarItem;
    
    private List<Fatura> listaFiltrada;

    private String tipo = "Débito";
    private TipoCategoria tipoCategoria;
    private List<CategoriaMotivo> categorias;
    // private Tarifa formaPagamentoDigitacao;
    // private List<Tarifa> tarifasDigitacao = new ArrayList<>();
    private CategoriaMotivo categoria;
    private List<Motivo> motivos;

    private List<TipoCategoria> tiposCategoria;
    private FaturaItem faturaItemEditar;

    // Tipo de impressão para Recibo
    private Lancamento lancamentoImpressao;
    private List<Lancamento> lancamentosImpressao;
    private BigDecimal valorTotalRecibo;
    private boolean incluirLogo;
    private TipoRecibo tipoReciboEscolhido;
    private StreamedContent reciboView;
    private String htmlReciboContent;

    public static enum TipoRecibo implements IEnumController {

        IMPRIMIR_SELECIONADO("IS", "Imprimir o recibo para esse recebimento"),
        IMPRIMIR_TODOS("IT", "Imprimir um recibo para todos os recebimentos dessa fatura"),
        ESCOLHER("ES", "Imprimir um recibo selecionando os recebimentos");

        private String rotulo, descricao;

        TipoRecibo(String rotulo, String descricao) {
            this.rotulo = rotulo;
            this.descricao = descricao;
        }

        public String getRotulo() {
            return this.rotulo;
        }

        public String getDescricao() {
            return this.descricao;
        }
    };

    public FaturaPagtoMB() {
        super(FaturaSingleton.getInstance().getBo());
        this.setClazz(Fatura.class);
        
        this.itemAlteracao = new FaturaItem();

        HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        String url = request.getRequestURL().toString();
        if (!url.contains("repasseComRecibo")) {

            try {
                //Calendar daysAgo = Calendar.getInstance();
                //daysAgo.add(Calendar.DAY_OF_MONTH, -7);
                //setInicio(daysAgo.getTime());
                //Calendar now = Calendar.getInstance();
                //setFim(now.getTime());

                setFormasPagamento(DominioSingleton.getInstance().getBo().listByEmpresaAndObjetoAndTipo("pagamento", "forma"));
                setListaStatus(Fatura.getStatusFaturaLista(DirecaoFatura.CREDITO));
                setShowLancamentosCancelados(false);
                carregarProfissionais();

                String idpaciente = JSFHelper.getRequest().getParameter("id");
                if (idpaciente != null && !idpaciente.isEmpty()) {
                    Paciente pac = PacienteSingleton.getInstance().getBo().find(Long.parseLong(idpaciente));
                    if (pac != null) {
                        this.setPaciente(pac);
                        pesquisar();
                    }
                }
            } catch (Exception e) {
                LogIntelidenteSingleton.getInstance().makeLog(e);
            }
        }

        updateWichScreenOpenForFaturaView();
        // System.out.println("FaturaPagtoMB" + new Timestamp(System.currentTimeMillis()));
    }

    public void setVideos() {
        getListaVideosTutorial().clear();
        getListaVideosTutorial().put("Como fazer o recebimento", "https://www.youtube.com/v/LeKXFc8hKgo?autoplay=1");
        getListaVideosTutorial().put("Paciente indeciso? Faça várias negociações!", "https://www.youtube.com/v/4KPlYGfPr50?autoplay=1");
    }

    public void updateWichScreenOpenForFaturaView() {
        try {
            if (getEntity() != null && getEntity().getId() != null && getEntity().getId().longValue() != 0l) {
                if ("PP".equals(getEntity().getTipoFatura().getRotulo())) {
                    this.faturaViewImportName = "faturaViewProfissional.xhtml";
                    return;
                }
            }
        } catch (Exception e) {
            LogIntelidenteSingleton.getInstance().makeLog(e);
        }
        this.faturaViewImportName = "faturaViewPaciente.xhtml";
    }

    public boolean hasRequisitosCumprir(Lancamento lancamentoRepasse) {
        return FaturaSingleton.getInstance().hasRequisitosCumprir(UtilsFrontEnd.getEmpresaLogada(), lancamentoRepasse);
    }

    public boolean isTodosRequisitosFeitos(Lancamento lancamentoRepasse) {
        return FaturaSingleton.getInstance().isTodosRequisitosFeitos(UtilsFrontEnd.getEmpresaLogada(), lancamentoRepasse);
    }

    public List<Requisito> getRequisitosValidaLancamentoFromRepasseFatura(Lancamento lancamentoRepasse) {
        return FaturaSingleton.getInstance().getRequisitosValidaLancamentoFromRepasseFatura(UtilsFrontEnd.getEmpresaLogada(), lancamentoRepasse);
    }

    private void carregarProfissionais() throws Exception {
        List<String> perfis = new ArrayList<>();
        perfis.add(OdontoPerfil.DENTISTA);
        perfis.add(OdontoPerfil.ADMINISTRADOR);
        if(UtilsFrontEnd.getProfissionalLogado() != null) {
            profissionais = ProfissionalSingleton.getInstance().getBo().listByEmpresa(perfis, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa()); 
        }
    }

    public void abreTrocaItemProfissional(FaturaItem item) {
        setItemSelecionado(item);
        PrimeFaces.current().executeScript("PF('dlgTrocaItemProfissional').show()");
    }

    public void actionPersistTrocaItemProfissional() {
        try {
            RepasseFaturasItemSingleton.getInstance().trocaItemRepasseProfissional(getItemSelecionado(), getObservacao(), getProfissionalTroca(), UtilsFrontEnd.getProfissionalLogado());
            setEntity(FaturaSingleton.getInstance().getBo().find(getEntity()));

            if (getEntity().getItensFiltered() == null || getEntity().getItensFiltered().isEmpty()) {
                PrimeFaces.current().executeScript("PF('dlgFaturaView').hide()");
                setEntity(null);
            }

            if (Profissional.FIXO.equals(getProfissionalTroca().getTipoRemuneracao())) {
                this.addInfo("Sucesso", "Item inativado somente, pois o profissional " + getProfissionalTroca().getDadosBasico().getNome() + " não possui comissionamento!", true);
            } else {
                this.addInfo("Sucesso", "Troca realizada com sucesso.<br />Verifique a nova fatura em nome de " + getProfissionalTroca().getDadosBasico().getNome() + "!", true);
            }
        } catch (Exception e) {
            LogIntelidenteSingleton.getInstance().makeLog(e);
            this.addError("Erro", "Falha ao realizar a troca!<br />" + e.getMessage(), true);
        }
    }

    public void cancelarItem(FaturaItem item) {
        try {
            FaturaItemSingleton.getInstance().cancelarItem(Arrays.asList(item), UtilsFrontEnd.getProfissionalLogado());
            addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");
            updateValues(this.getEntity());
        } catch (CancelarItemProcedimentoExecutado e) {
            System.out.println(e.getMessage());
            this.addError("Falha ao cancelar item de fatura", "Não é possível cancelar um item referente a um procedimento executado.");
        } catch (CancelarItemProcedimentoAtivo e) {
            System.out.println(e.getMessage());
            this.addError("Falha ao cancelar item de fatura", "Não é possível cancelar um item referente a um procedimento ativo, deve estar cancelado.");
        } catch (Exception e) {
            e.printStackTrace();
            addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "Falha ao inativar item da fatura.");
        }
    }

    public void carregaItem(FaturaItem item) {
        this.itemAlteracao = item;
    }

    public void persistAlteracaoValorItem(FaturaItem item) {
        try {
            FaturaItem itemBanco = FaturaItemSingleton.getInstance().getBo().find(item);

            if (itemBanco.getValorAlterado() == null) {
                item.setValorAlterado(itemBanco.getValorComDesconto());
            }

            item.setAlteradoPor(UtilsFrontEnd.getProfissionalLogado());
            item.setDataAlteracaoStatus(new Date());

            FaturaItemSingleton.getInstance().getBo().merge(item);
            this.updateValues(getEntity());

            addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");
        } catch (Exception e) {
            addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "Falha ao alterar valor do item da fatura.");
            e.printStackTrace();
        }
    }

    public void alterarItem() {
        try {
            FaturaItemSingleton.getInstance().getBo().persist(itemAlteracao);
            addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");
            updateValues(this.getEntity());
        } catch (Exception e) {
            addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "Falha ao alterar item.");
            e.printStackTrace();
        }
    }

    public void ativarItem(FaturaItem item) {
        try {
            FaturaItemSingleton.getInstance().ativaFaturaItem(item, UtilsFrontEnd.getProfissionalLogado());
        } catch (Exception e) {
            addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "Falha ao ativar fatura.");
            e.printStackTrace();
        }
    }

    public void cancelaLancamento(Lancamento l) {
        try {

            if (l.getValidadoPorProfissional() == null) {
                LancamentoSingleton.getInstance().inativaLancamento(l, UtilsFrontEnd.getProfissionalLogado());
                //FaturaSingleton.getInstance().ajustarFaturaGenerica(l.getFatura(), UtilsFrontEnd.getProfissionalLogado());
                this.addInfo("Sucesso", "Lançamento cancelado com sucesso!", true);
            } else {
                if (isAdmin()) {
                    cancelaLancamentoValidado(l);
                    //  FaturaSingleton.getInstance().ajustarFaturaGenerica(l.getFatura(), UtilsFrontEnd.getProfissionalLogado());
                } else {
                    this.addError("Permissão negada !", Mensagens.getMensagem(Mensagens.PERMISSAO_NEGADA));
                }
            }

            updateValues(getEntity(), true, false);

        } catch (Exception e) {
            e.printStackTrace();
            LogIntelidenteSingleton.getInstance().makeLog(e);
            this.addError("Erro", "Falha ao cancelar o lançamento!\\r\\n" + e.getMessage(), true);
        }
    }

    private void cancelaLancamentoValidado(Lancamento l) {
        try {
            LancamentoSingleton.getInstance().gerarEstornoRecebimento(l, UtilsFrontEnd.getProfissionalLogado());
            this.addInfo("Sucesso", "Lançamento cancelado com sucesso!", true);
        } catch (Exception e) {
            e.printStackTrace();
            LogIntelidenteSingleton.getInstance().makeLog(e);
            this.addError("Erro", "Falha ao cancelar o lançamento!\\r\\n" + e.getMessage(), true);
        }
    }

    public void carregarAjuste(Lancamento lancamento) {
        try {
            if (isAdmin() && lancamento.getValidadoPorProfissional() != null) {
                this.ajustarLancamento = new Lancamento();
                this.extornarLancamento = lancamento;

                this.ajustarLancamento.setFormaPagamento(lancamento.getFormaPagamento());
                this.ajustarLancamento.setValor(lancamento.getValor());
                this.ajustarLancamento.setTarifa(lancamento.getTarifa());
                this.ajustarLancamento.setValorDesconto(lancamento.getValorDesconto());
                this.ajustarLancamento.setDataPagamento(lancamento.getDataPagamento());
                this.ajustarLancamento.setDataCredito(lancamento.getDataCredito());
                this.ajustarLancamento.setId(lancamento.getId());
                editarLancamentoAtualizarFormasPagamentoDisponiveis(lancamento.getFatura().getTipoFaturaSigla());
            } else {
                this.addError("Permissão negada", Mensagens.getMensagem(Mensagens.PERMISSAO_NEGADA));
            }
        } catch (Exception e) {
            this.addError("Falha ao carregar dados de ajuste", Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS));
        }
    }

    public void ajustarLancamento() {
        try {

            Lancamento lancamento = LancamentoSingleton.getInstance().getBo().find(ajustarLancamento.getId());

//            //SE FOR FATURA GENERICA DE RECEBIMENTO OU PAGAMENTO, NAO PRECISA FAZER EXTORNO, POIS PODEMOS ALTERAR O VALOR
//            //TOTAL DA FATURA, VISTO QUE A ORIGEM É ELA MESMO.

//                if (lancamento.getFatura().getTipoFatura().equals(Fatura.TipoFatura.FATURA_GENERICA_RECEBIMENTO) ||
//                        lancamento.getFatura().getTipoFatura().equals(Fatura.TipoFatura.FATURA_GENERICA_PAGAMENTO)    
//                        ) {                    
//                    lancamento.setFormaPagamento(ajustarLancamento.getFormaPagamento());
//                    lancamento.setValor(ajustarLancamento.getValor());
//                    lancamento.setTarifa(ajustarLancamento.getTarifa());
//                    lancamento.setValorDesconto(ajustarLancamento.getValorDesconto());
//                    lancamento.setDataPagamento(ajustarLancamento.getDataPagamento());
//                    lancamento.setDataCredito(ajustarLancamento.getDataCredito());
//                   FaturaSingleton.getInstance().ajustarFaturaGenerica(lancamento,UtilsFrontEnd.getProfissionalLogado());
//                    
//                }else {
            //EXTORNA O VALOR JÁ CONFERIDO E VALIDADO
            this.cancelaLancamentoValidado(extornarLancamento);

            //CRIA UM NOVO LANÇAMENTO
            Lancamento l = LancamentoSingleton.getInstance().novoLancamento(null, extornarLancamento.getFatura(), ajustarLancamento.getValor(), ajustarLancamento.getFormaPagamento(),
                    extornarLancamento.getNumeroParcela(), extornarLancamento.getParcelaTotal(), ajustarLancamento.getDataPagamento(), ajustarLancamento.getDataCredito(),
                    extornarLancamento.getTarifa(), UtilsFrontEnd.getProfissionalLogado(), false, null, null, null);
            //  }

            setEntity(FaturaSingleton.getInstance().getBo().find(lancamento.getFatura().getId()));
            getEntity().setDadosTabelaRepasseTotalFatura(FaturaSingleton.getInstance().getTotal(lancamento.getFatura()));

            this.addInfo("Sucesso", "Lançamento ajustado com sucesso!", true);

        } catch (Exception e) {
            LogIntelidenteSingleton.getInstance().makeLog(e);
            e.printStackTrace();
            this.addError("Erro", "Falha ao ajustar o lançamento!");
        }
    }

    public void actionTrocaDatasCriacao() {
        try {
            setInicio(getDataInicio(filtroPeriodo));
            setFim(getDataFim(filtroPeriodo));
        } catch (Exception e) {
            LogIntelidenteSingleton.getInstance().makeLog(e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
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
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
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
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            return null;
        }
    }

    public void visualizaFatura(Fatura fatura) {
        /*
         * List<FaturaItem> itens = new ArrayList<>(fatura.getItens()); if (fatura.getTipoFatura() == Fatura.TipoFatura.RECEBIMENTO_PACIENTE) { itens.forEach(item -> { try { String pt =
         * PlanoTratamentoSingleton.getInstance().getPlanoTratamentoFromFaturaItemOrigem(item).getDescricao(); item.setDescricaoItem(item.getDescricaoItem() + " [" + pt + "]"); } catch (Exception e) {
         * LogIntelidenteSingleton.getInstance().makeLog(e); } }); }
         */
        Fatura aux = null;
        try {
//            String idFatura = (String) FacesContext.getCurrentInstance()
//                    .getExternalContext().getRequestParameterMap().get("fatura_selecionada");
            aux = FaturaSingleton.getInstance().getBo().find(Long.valueOf(fatura.getId()));
            
            if(aux != null)
                fatura = aux;
            
            setEntity(fatura);
            fatura.setNegociacoes(NegociacaoFaturaSingleton.getInstance().getBo().getNegociacaoFromFatura(fatura));
            setShowLancamentosCancelados(false);
            setShowLancamentosEstorno(false);
            updateValues(fatura, true, false);

            updateWichScreenOpenForFaturaView();
        }catch (Exception e) {
            e.printStackTrace();
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
        }
    }

    public void visualizaFaturaSimples(Fatura fatura) {
        setEntity(fatura);
        setShowLancamentosCancelados(false);
        setShowLancamentosEstorno(false);
        updateValues(fatura, true, false);
        updateWichScreenOpenForFaturaView();
        if (fatura.getProfissional() != null)
            origem = fatura.getProfissional().getDadosBasico();
        if (fatura.getPaciente() != null)
            origem = fatura.getPaciente().getDadosBasico();
        if (fatura.getFornecedor() != null)
            origem = fatura.getFornecedor().getDadosBasico();
    }

    public void visualizaFaturaSimples(Lancamento l) {

        setEntity(l.getFatura());
        setShowLancamentosCancelados(false);
        setShowLancamentosEstorno(false);
        updateValues(l.getFatura(), true, false);
        updateWichScreenOpenForFaturaView();
        if (l.getFatura().getPaciente() != null) {
            origem = l.getFatura().getPaciente().getDadosBasico();
//        }else if (l.getFatura().getOrigem() != null) {
//            origem = l.getFatura().getOrigem().getDadosBasico();
        } else if (l.getFatura().getFornecedor() != null) {
            origem = l.getFatura().getFornecedor().getDadosBasico();
        } else if (l.getFatura().getProfissional() != null) {
            origem = l.getFatura().getProfissional().getDadosBasico();
        } else {
            origem = LancamentoContabilSingleton.getInstance().getBo().findByLancamento(l).getDadosBasico();
        }

    }

    public PlanoTratamentoProcedimento buscaPTPOrigemRepasse(Lancamento l) {
        try {
            RepasseFaturasLancamento repasse = RepasseFaturasLancamentoSingleton.getInstance().getBo().getFaturaRepasseLancamentoFromLancamentoRepasse(l);
            if (repasse != null)
                return PlanoTratamentoProcedimentoSingleton.getInstance().getProcedimentoFromFaturaItem(repasse.getFaturaItem());
        } catch (Exception e) {
            // TODO: handle exception
        }
        return null;
    }

    public String buscaProcedimentoOrigemRepasse(Lancamento l) {
        RepasseFaturasLancamento repasse = RepasseFaturasLancamentoSingleton.getInstance().getBo().getFaturaRepasseLancamentoFromLancamentoRepasse(l);
        if (repasse != null)
            return repasse.getFaturaItem().getDescricaoItem();
        return "";
    }

    public boolean mostraProcedimentoOrigemRepasse(Lancamento l) {
        RepasseFaturasLancamento repasse = RepasseFaturasLancamentoSingleton.getInstance().getBo().getFaturaRepasseLancamentoFromLancamentoRepasse(l);
        if (repasse != null)
            return (repasse.getFaturaItem().getDescricaoItem() != null && !repasse.getFaturaItem().getDescricaoItem().isEmpty());
        return false;
    }

    public PlanoTratamento getPTFromFaturaRepasse(Fatura fatura) {
        return PlanoTratamentoSingleton.getInstance().getPlanoTratamentoFromFaturaRepasse(fatura);
    }

    public boolean isPermiteExcluirLancamento() {
        return isAdmin() || isAdministrador();
    }

    public void validaLancamentoRepasse(Lancamento l) {
        try {
            RepasseFaturasLancamentoSingleton.getInstance().validaLancamentoRepasse(l, UtilsFrontEnd.getProfissionalLogado());
            addInfo("Sucesso", "Sucesso ao salvar o registro", true);
        } catch (Exception e) {
            LogIntelidenteSingleton.getInstance().makeLog(e);
            this.addError("Erro", "Falha ao validar o lançamento! " + e.getMessage().replace("'", "\\'"), true);
        }
    }

    public void confereLancamentoRepasse(Lancamento l) {
        try {
            LancamentoSingleton.getInstance().confereLancamento(l, UtilsFrontEnd.getProfissionalLogado());
            updateValues(getEntity(), true, false);
            addInfo("Sucesso", "Sucesso ao salvar o registro", true);
        } catch (Exception e) {
            LogIntelidenteSingleton.getInstance().makeLog(e);
            this.addError("Erro", "Falha ao conferir o lançamento! " + e.getMessage().replace("'", "\\'"), true);
        }
    }

    public void pesquisar() {
        try {
            Calendar inicio = null;
            if (getInicio() != null) {
                inicio = Calendar.getInstance();
                inicio.setTime(getInicio());
                inicio.set(Calendar.HOUR_OF_DAY, 0);
                inicio.set(Calendar.MINUTE, 0);
                inicio.set(Calendar.SECOND, 0);
                inicio.set(Calendar.MILLISECOND, 0);
            }
            Calendar fim = null;
            if (getFim() != null) {
                fim = Calendar.getInstance();
                fim.setTime(getFim());
                fim.set(Calendar.HOUR_OF_DAY, 23);
                fim.set(Calendar.MINUTE, 59);
                fim.set(Calendar.SECOND, 59);
                fim.set(Calendar.MILLISECOND, 999);
            }

            this.somaTotal = BigDecimal.ZERO;
            this.somaTotalPago = BigDecimal.ZERO;
            this.somaTotalNaoPago = BigDecimal.ZERO;
            this.somaTotalNaoPlanejado = BigDecimal.ZERO;

            setEntityList(FaturaSingleton.getInstance().getBo().findFaturasOrigemFilter(UtilsFrontEnd.getEmpresaLogada(), getPaciente(), (inicio == null ? null : inicio.getTime()),
                    (fim == null ? null : fim.getTime()), this.status, (this.subStatusFatura != null ? Arrays.asList(this.subStatusFatura) : null)));
            getEntityList().forEach(fatura -> {
                updateValues(fatura, true, true);
            });
        } catch (Exception e) {
            e.printStackTrace();
            LogIntelidenteSingleton.getInstance().makeLog(e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
        }
    }

    @SuppressWarnings("unused")
    private void updateValues(Fatura fatura) {
        updateValues(fatura, false, false);
    }

    private void updateValues(Fatura fatura, boolean updateAllValues, boolean updateTableValues) {
        fatura.setDadosTabelaRepasseTotalFatura(FaturaSingleton.getInstance().getTotal(fatura));
        fatura.setDadosTabelaRepasseTotalPago(FaturaSingleton.getInstance().getTotalPago(fatura));
        fatura.setDadosTabelaRepasseTotalNaoPago(FaturaSingleton.getInstance().getTotalNaoPago(fatura));
        fatura.setDadosTabelaRepasseTotalNaoPlanejado(FaturaSingleton.getInstance().getTotalNaoPlanejado(fatura));
        fatura.setDadosTabelaRepasseTotalRestante(FaturaSingleton.getInstance().getTotalRestante(fatura));
        fatura.setDadosTabelaPT(PlanoTratamentoSingleton.getInstance().getPlanoTratamentoFromFaturaOrigem(fatura));
            fatura.setDadosTabelaRepassePlanoTratamento(PlanoTratamentoSingleton.getInstance().getPlanoTratamentoFromFaturaRepasse(fatura));
            fatura.setDadosTabelaTotalPagoFromPaciente(FaturaSingleton.getInstance().getTotalPagoFromPaciente(fatura));
            fatura.setDadosTabelaTotalNaoPagoFromPaciente(FaturaSingleton.getInstance().getTotalNaoPagoFromPaciente(fatura));
            fatura.setDadosTabelaRepasseTotalRestante(FaturaSingleton.getInstance().getTotalRestanteFromPaciente(fatura));

            // Ajustando para corrigir valores na tabela de Recebimento da tela FaturaPagto
            fatura.setDadosTabelaRepasseTotalNaoPago(fatura.getDadosTabelaTotalNaoPagoFromPaciente());
            fatura.setDadosTabelaRepasseTotalPago(fatura.getDadosTabelaTotalPagoFromPaciente());

            NumberFormat ptFormat = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
            NumberFormat percformat = NumberFormat.getPercentInstance(new Locale("pt", "BR"));
            String totalDesconto = ptFormat.format(FaturaSingleton.getInstance().getTotalDesconto(fatura).doubleValue());
            totalDesconto += " (" + percformat.format(FaturaSingleton.getInstance().getPercentualDesconto(fatura)) + ")";
            fatura.setDadosTabelaTotalDesconto(totalDesconto);

        //fatura.setDadosTabelaStatusFatura("A Receber");
        //if (fatura.getDadosTabelaRepasseTotalFatura().subtract(fatura.getDadosTabelaRepasseTotalPago()).doubleValue() <= 0)
        //fatura.setDadosTabelaStatusFatura("Recebido");
        if (updateTableValues) {
            this.somaTotal = this.somaTotal.add(fatura.getDadosTabelaRepasseTotalFatura());
            this.somaTotalPago = this.somaTotalPago.add(fatura.getDadosTabelaRepasseTotalPago());
            this.somaTotalNaoPago = this.somaTotalNaoPago.add(fatura.getDadosTabelaRepasseTotalNaoPago());
            this.somaTotalNaoPlanejado = this.somaTotalNaoPlanejado.add(fatura.getDadosTabelaRepasseTotalNaoPlanejado());
        }
    }
    
    public void updateSomatorio(){
        somaTotal = new BigDecimal(0);
        somaTotalPago = new BigDecimal(0);
        somaTotalNaoPago = new BigDecimal(0);
        somaTotalNaoPlanejado = new BigDecimal(0);
        for (Fatura fatura : listaFiltrada) {
            this.somaTotal = this.somaTotal.add(fatura.getDadosTabelaRepasseTotalFatura());
            this.somaTotalPago = this.somaTotalPago.add(fatura.getDadosTabelaRepasseTotalPago());
            this.somaTotalNaoPago = this.somaTotalNaoPago.add(fatura.getDadosTabelaRepasseTotalNaoPago());
            this.somaTotalNaoPlanejado = this.somaTotalNaoPlanejado.add(fatura.getDadosTabelaRepasseTotalNaoPlanejado());
        }
        
        System.out.println(somaTotal);
        System.out.println(somaTotalPago);
        System.out.println(somaTotalNaoPago);
        System.out.println(somaTotalNaoPlanejado);
    }

    public void changePaciente() {
        setListaPt(PlanoTratamentoSingleton.getInstance().getBo().listAtivosByPaciente(getPaciente()));
    }

    public void atualizaProduto() {
        if (existeCadastroTarifa()) {
            setTarifas(TarifaSingleton.getInstance().getBo().listByForma(getFormaPagamento(), UtilsFrontEnd.getProfissionalLogado().getIdEmpresa(), FormaPagamento.RECEBIMENTO));
            setShowProduto(Boolean.TRUE);
        } else {
            setShowProduto(Boolean.FALSE);
            setParcela(1);
        }

        handleSelectPagamento();
    }

    public boolean existeCadastroTarifa() {
        return existeCadastroTarifa(getFormaPagamento());
    }

    public boolean existeCadastroTarifa(String formaPagamento) {
        if (Arrays.asList(new String[] { "CC", "CD", "BO" }).indexOf(formaPagamento) != -1)
            return true;
        return false;
    }

    public void atualizaDataCredito() {
        if (this.getTarifa() != null) {
            setParcelas(new ArrayList<>());
            for (int i = getTarifa().getParcelaMinima(); i <= getTarifa().getParcelaMaxima(); i++)
                getParcelas().add(i);
            setDataCredito(this.adicionaDias(getDataPagamento(), this.getTarifa().getPrazo()));
        } else {
            setDataCredito(getDataPagamento());
        }
    }

    private Date adicionaDias(Date data, int dias) {
        Calendar c = Calendar.getInstance();
        if (data == null)
            data = c.getTime();
        c.setTime(data);
        c.add(Calendar.DAY_OF_MONTH, dias);
        return c.getTime();
    }

    public void handleSelectPagamento() {
        if (this.getTarifa() != null && this.getTarifa().getProduto() != null) {
            setDataCredito(this.adicionaDias(getDataPagamento(), this.getTarifa().getPrazo()));
        } else {
            setDataCredito(getDataPagamento());
        }
    }

    public void actionNewLancamento() {
        setParcela(1);
        setShowProduto(false);
        setFormaPagamento("DI");
        setTarifa(null);
        setValor(getEntity().getDadosTabelaRepasseTotalNaoPlanejado());
        setDataPagamento(new Date());
        handleSelectPagamento();
    }

    public void handleSelectEditarItem(SelectEvent event) {
        Object object = event.getObject();
        lancamentoContabilEditarItem.setDadosBasico((DadosBasico) object);
//        Motivo ultimoMotivo = MotivoSingleton.getInstance().getBo().findUltimoMotivoByDadosBasicos(getEntity().getDadosBasico());
//        if (ultimoMotivo != null) {
//            tipoCategoria = ultimoMotivo.getCategoria().getTipoCategoria();
//            categoria = ultimoMotivo.getCategoria();
//            this.getEntity().setMotivo(ultimoMotivo);
//        }
        lancamentoContabilEditarItem.setData(new Date());
        if (lancamentoContabilEditarItem.getDadosBasico() == null) {
            this.addError(OdontoMensagens.getMensagem("lancamentoContabil.dadosbasico.vazio"), "");
        }
    }

    public List<DadosBasico> geraSugestoesEditarItem(String query) {

        List<DadosBasico> dadosBasicos = null;
        try {
            dadosBasicos = Utils.geraListaSugestoesOrigens(UtilsFrontEnd.getEmpresaLogada().getEmpIntCod());
        } catch (Exception e) {
            e.printStackTrace();
        }

        List<DadosBasico> suggestions = new ArrayList<>();
        if (query.length() >= 3) {
            for (DadosBasico d : dadosBasicos) {
                if (Utils.normalize(d.getNome()).toLowerCase().contains(query.toLowerCase())) {
                    suggestions.add(d);
                }
            }
        } else if (query.equals("")) {
            suggestions = dadosBasicos;
        }
        // Collections.sort(suggestions);
        return suggestions;
    }

    public void actionPersistLancamento() {
        try {
            if (getValor().compareTo(getEntity().getDadosTabelaRepasseTotalFatura()) <= 0) {
                if (getValor().compareTo(getEntity().getDadosTabelaRepasseTotalNaoPlanejado()) > 0) {
                    this.addError("Informe um valor menor que o total restante de planejamento!", "");
                    return;
                }

                if (this.dataCredito == null || this.dataPagamento == null) {
                    this.addError("Não foi possível gerar o lançamento", "Preencha todos os campos corretamente !");
                    return;
                }

                Calendar now = Calendar.getInstance();
                now.setTime(getDataPagamento());
                Calendar data = Calendar.getInstance();
                data.setTime(getDataCredito());
                if (existeCadastroTarifa()) {
                    BigDecimal valorOriginalDividio = getValor().divide(new BigDecimal(getParcela()), 2, RoundingMode.HALF_UP);
                    BigDecimal diferenca = valorOriginalDividio.multiply(new BigDecimal(getParcela())).subtract(getValor());
                    for (int i = 1; i <= getParcela(); i++) {
                        if (i == getParcela()) {
                            valorOriginalDividio = valorOriginalDividio.subtract(diferenca);
                        }

                        if ("CC".equals(getFormaPagamento())) {
                            LancamentoSingleton.getInstance().novoLancamento(getEntity(), valorOriginalDividio, getFormaPagamento(), i, getParcela(), now.getTime(), data.getTime(), getTarifa(),
                                    UtilsFrontEnd.getProfissionalLogado(), null);
                            //persistLancamento(getParcela(), getEntity(), valorOriginalDividio, getFormaPagamento(), Calendar.getInstance().getTime(), data.getTime(), getTarifa());
                        } else {
                            LancamentoSingleton.getInstance().novoLancamento(getEntity(), valorOriginalDividio, getFormaPagamento(), i, getParcela(), now.getTime(), data.getTime(), getTarifa(),
                                    UtilsFrontEnd.getProfissionalLogado(), null);
                            now.add(Calendar.MONTH, 1);
                        }
                        data.add(Calendar.MONTH, 1);
                    }
                } else {
                    LancamentoSingleton.getInstance().novoLancamento(getEntity(), getValor(), getFormaPagamento(), getParcela(), getParcela(), now.getTime(), data.getTime(), getTarifa(),
                            UtilsFrontEnd.getProfissionalLogado(), null);
                    //persistLancamento(getParcela(), getEntity(), getValor(), getFormaPagamento(), Calendar.getInstance().getTime(), data.getTime(), getTarifa());
                }
                this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");
                PrimeFaces.current().executeScript("PF('dlgNewLancamento').hide()");
                updateValues(getEntity(), true, false);
            }
        } catch (Exception e) {
            LogIntelidenteSingleton.getInstance().makeLog(e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
        }
    }

    public void actionPersistLancamentoGenerico() {
        try {
            if (getValor().compareTo(getEntity().getDadosTabelaRepasseTotalNaoPlanejado()) > 0) {
                this.addError("Informe um valor menor que o total restante de planejamento!", "");
                return;
            }

            if (this.dataCredito == null || this.dataPagamento == null) {
                this.addError("Não foi possível gerar o lançamento", "Preencha todos os campos corretamente !");
                return;
            }

            Calendar now = Calendar.getInstance();
            Calendar data = Calendar.getInstance();
            data.setTime(getDataCredito());
            if (existeCadastroTarifa()) {
                BigDecimal valorOriginalDividio = getValor().divide(new BigDecimal(getParcela()), 2, RoundingMode.HALF_UP);
                BigDecimal diferenca = valorOriginalDividio.multiply(new BigDecimal(getParcela())).subtract(getValor());
                for (int i = 1; i <= getParcela(); i++) {
                    if (i == getParcela()) {
                        valorOriginalDividio = valorOriginalDividio.subtract(diferenca);
                    }

                    if ("CC".equals(getFormaPagamento())) {
                        LancamentoSingleton.getInstance().novoLancamento(null, getEntity(), valorOriginalDividio, getFormaPagamento(), i, getParcela(), now.getTime(), data.getTime(), getTarifa(),
                                UtilsFrontEnd.getProfissionalLogado(), false, "", "", null);
                    } else {
                        LancamentoSingleton.getInstance().novoLancamento(null, getEntity(), valorOriginalDividio, getFormaPagamento(), i, getParcela(), now.getTime(), data.getTime(), getTarifa(),
                                UtilsFrontEnd.getProfissionalLogado(), false, "", "", null);
                        now.add(Calendar.MONTH, 1);
                    }

                    data.add(Calendar.MONTH, 1);
                }
            } else {
                LancamentoSingleton.getInstance().novoLancamento(null, getEntity(), getValor(), getFormaPagamento(), getParcela(), getParcela(), now.getTime(), data.getTime(), getTarifa(),
                        UtilsFrontEnd.getProfissionalLogado(), false, "", "", null);

            }

        } catch (Exception e) {
            LogIntelidenteSingleton.getInstance().makeLog(e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
        }

        this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");
        PrimeFaces.current().executeScript("PF('dlgNewLancamento').hide()");

    }

    public void carregarCategorias() {
        try {
            if (tipoCategoria != null) {
                categorias = CategoriaMotivoSingleton.getInstance().getBo().listByTipoCategoria(tipoCategoria);
            } else {
                categorias = CategoriaMotivoSingleton.getInstance().getBo().listAll();
            }
        } catch (Exception e) {
            e.printStackTrace();
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
        }
    }

    public void carregarMotivos() {
        try {
            if (categoria != null) {
                motivos = MotivoSingleton.getInstance().getBo().listByCategoria(categoria, tipo);
            } else {
                motivos = MotivoSingleton.getInstance().getBo().listByTipo(tipo);
            }
        } catch (Exception e) {
            e.printStackTrace();
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
        }
    }

    public void carregarListasEditaritem() {

        try {

            tiposCategoria = TipoCategoriaSingleton.getInstance().getBo().listAll();

            if (tipoCategoria != null) {
                categorias = CategoriaMotivoSingleton.getInstance().getBo().listByTipoCategoria(tipoCategoria);
            } else {
                categorias = CategoriaMotivoSingleton.getInstance().getBo().listAll();
            }

            if (lancamentoContabilEditarItem != null && lancamentoContabilEditarItem.getMotivo() != null) {
                categoria = lancamentoContabilEditarItem.getMotivo().getCategoria();
                tipoCategoria = lancamentoContabilEditarItem.getMotivo().getCategoria().getTipoCategoria();
            }

            if (categoria != null) {
                motivos = MotivoSingleton.getInstance().getBo().listByCategoria(categoria, tipo);
            } else {
                motivos = MotivoSingleton.getInstance().getBo().listByTipo(tipo);
            }

        } catch (Exception e) {
            e.printStackTrace();
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
        }
    }

    public void actionEditarItem(FaturaItem faturaItem) {
        try {

            Lancamento lancamento = faturaItem.getFatura().getLancamentosFiltered().get(faturaItem.getFatura().getLancamentosFiltered().size() - 1);
            lancamentoContabilEditarItem = LancamentoContabilSingleton.getInstance().getBo().findByLancamento(lancamento);

            if (faturaItem.getTipoSaldo().equals("S")) {
                this.tipo = "Débito";
                //  lancamentoContabilEditarItem.setValor(lancamentoContabilEditarItem.getValor().negate());
            } else {
                this.tipo = "Crédito";
            }

            carregarListasEditaritem();

            if (faturaItem.getDescricaoItem() != null) {
                lancamentoContabilEditarItem.setDescricao(faturaItem.getDescricaoItem().replace(lancamentoContabilEditarItem.getMotivo().getDescricao() + " - ", ""));
            }

            faturaItemEditar = faturaItem;

            tipoCategoria = faturaItem.getMotivo().getCategoria().getTipoCategoria();
            categoria = faturaItem.getMotivo().getCategoria();
            carregarMotivos();

        } catch (Exception e) {
            e.printStackTrace();
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_REMOVER_REGISTRO), "");
        }
    }

    public void actionPersistEditarItem() {

        BigDecimal valorTotalExistenteLancamentos = LancamentoSingleton.getInstance().getTotalLancamentoPorFatura(getEntity(), ValidacaoLancamento.TODOS_ATIVOS);
        BigDecimal valorEditado = new BigDecimal(faturaItemEditar.getValorItem());
        valorEditado = valorEditado.setScale(2, BigDecimal.ROUND_HALF_EVEN);
        if (FaturaSingleton.getInstance().getValorTotal(faturaItemEditar.getFatura()).compareTo(new BigDecimal(0)) != 0 && valorTotalExistenteLancamentos.compareTo(valorEditado) > 0) {
            this.addError("Erro!", "O valor dos lancamentos não pode exceder o valor do item");
            return;
        }

        try {
            faturaItemEditar.setValorComDesconto(new BigDecimal(faturaItemEditar.getValorItem()));
            faturaItemEditar.setDataCriacao(lancamentoContabilEditarItem.getData());

            lancamentoContabilEditarItem.setMotivo(faturaItemEditar.getMotivo());
            faturaItemEditar.setDescricaoItem(Utils.descricaoItemFaturaGenerica(lancamentoContabilEditarItem));

            if (faturaItemEditar.getTipoSaldo().equals("S")) {
                lancamentoContabilEditarItem.setValor(lancamentoContabilEditarItem.getValor().negate());
            }
            lancamentoContabilEditarItem.setMotivo(lancamentoContabilEditarItem.getMotivo());
            lancamentoContabilEditarItem.setDescricao(faturaItemEditar.getDescricaoItem().replace(lancamentoContabilEditarItem.getMotivo().getDescricao() + " - ", ""));
            FaturaItemSingleton.getInstance().getBo().persist(faturaItemEditar);
            LancamentoContabilSingleton.getInstance().getBo().persist(lancamentoContabilEditarItem);

            Fatura fatura = faturaItemEditar.getFatura();
            TipoPessoa tipoPessoa = DadosBasicoSingleton.getTipoPessoa(lancamentoContabilEditarItem.getDadosBasico());
            if (tipoPessoa.equals(TipoPessoa.FORNECEDOR)) {
                fatura.setFornecedor(FornecedorSingleton.getInstance().getBo().findByDadosBasicos(lancamentoContabilEditarItem.getDadosBasico()));
                fatura.setPaciente(null);
                fatura.setProfissional(null);
            } else if (tipoPessoa.equals(TipoPessoa.PACIENTE)) {
                fatura.setPaciente(PacienteSingleton.getInstance().getBo().findByDadosBasicos(lancamentoContabilEditarItem.getDadosBasico()));
                fatura.setProfissional(null);
                fatura.setFornecedor(null);
            } else if (tipoPessoa.equals(TipoPessoa.PROFISSIONAL)) {
                fatura.setProfissional(ProfissionalSingleton.getInstance().getBo().findByDadosBasicos(lancamentoContabilEditarItem.getDadosBasico()));
                fatura.setPaciente(null);
                fatura.setFornecedor(null);
            }
            origem = lancamentoContabilEditarItem.getDadosBasico();
            FaturaSingleton.getInstance().getBo().persist(fatura);
            setEntity(fatura);

            //TODO depois de colocar a recorrencia verificar aqui

            addInfo("Sucesso", "Sucesso ao salvar o registro", true);
            PrimeFaces.current().executeScript("PF('dlgEditarPagamentoRecebimento').hide()");
        } catch (Exception e) {
            this.addError("Erro!", Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO));
            e.printStackTrace();
        }
    }

    //-------------------------------- EDITAR LANÇAMENTO --------------------------------

    public void actionStartEditarLancamento(Lancamento l) {
        if (this.editarLancamentoFormasDisponiveis == null)
            editarLancamentoAtualizarFormasPagamentoDisponiveis(l.getFatura().getTipoFaturaSigla());
        this.editarLancamento = l;
    }

    public void actionPersistEditarLancamento() {
        try {
            BigDecimal totalFatura = FaturaSingleton.getInstance().getTotal(getEntity());
            if (editarLancamento.getValor().compareTo(totalFatura) > 0) {
                this.addError("Erro!", "O valor do lançamento não pode exceder o total da fatura !");
                return;
            }

            LancamentoSingleton.getInstance().getBo().persist(editarLancamento);

            LancamentoContabil lancamentoContabilEditar = LancamentoContabilSingleton.getInstance().getBo().findByLancamento(editarLancamento);
            if (lancamentoContabilEditar != null) {
                lancamentoContabilEditar.setData(editarLancamento.getDataCredito());
                if (editarLancamento.getFatura().getTipoFatura().equals(Fatura.TipoFatura.FATURA_GENERICA_PAGAMENTO)) {
                    lancamentoContabilEditar.setValor(editarLancamento.getValor().negate());
                } else {
                    lancamentoContabilEditar.setValor(editarLancamento.getValor());
                }

                LancamentoContabilSingleton.getInstance().getBo().persist(lancamentoContabilEditar);
            }

            //  FaturaItemSingleton.getInstance().recalculaItemsFaturasGenericas(editarLancamento.getFatura());

            //TODO esse ajuste sera chamado automaticamente na tela de conferencia
            // FaturaSingleton.getInstance().ajustarFaturaGenerica(editarLancamento.getFatura(), UtilsFrontEnd.getProfissionalLogado());

            setEntity(FaturaSingleton.getInstance().getBo().find(getEntity()));
            updateValues(getEntity(), true, true);

            PrimeFaces.current().executeScript("PF('dlgEditarLancamento').hide()");
        } catch (Exception e) {
            LogIntelidenteSingleton.getInstance().makeLog(e);
            this.addError("Erro!", Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO));
        }
    }

    public void actionEditarLancamentoAlteraFormaPagamento() {
        if (editarLancamento == null)
            return;

        editarLancamento.setFormaPagamento(editarLancamento.getTarifa().getTipo());
        actionEditarLancamentoAlteraDataPagamento();
    }

    public void actionAjustarLancamentoAlteraFormaPagamento() {
        if (ajustarLancamento == null)
            return;

        ajustarLancamento.setFormaPagamento(ajustarLancamento.getTarifa().getTipo());
        actionAjustarLancamentoAlteraDataPagamento();
    }

    public void actionAjustarLancamentoAlteraDataPagamento() {
        if (ajustarLancamento == null)
            return;

        if (ajustarLancamento.getTarifa() != null && ajustarLancamento.getDataPagamento() != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(ajustarLancamento.getDataPagamento());
            cal.add(Calendar.DAY_OF_MONTH, ajustarLancamento.getTarifa().getPrazo());
            ajustarLancamento.setDataCredito(cal.getTime());
        } else
            ajustarLancamento.setDataCredito(null);
    }

    public void actionEditarLancamentoAlteraDataPagamento() {
        if (editarLancamento == null)
            return;

        if (editarLancamento.getTarifa() != null && editarLancamento.getDataPagamento() != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(editarLancamento.getDataPagamento());
            cal.add(Calendar.DAY_OF_MONTH, editarLancamento.getTarifa().getPrazo());
            editarLancamento.setDataCredito(cal.getTime());
        } else
            editarLancamento.setDataCredito(null);
    }

    private void editarLancamentoAtualizarFormasPagamentoDisponiveis(String formaPagamentoSigla) {
        try {
            this.editarLancamentoFormasDisponiveis = TarifaSingleton.getInstance().getBo().listByEmpresa(UtilsFrontEnd.getEmpresaLogada().getEmpIntCod(), formaPagamentoSigla);
            System.out.println();
        } catch (Exception e) {
            LogIntelidenteSingleton.getInstance().makeLog(e);
        }
    }

    //-------------------------------- EDITAR LANÇAMENTO --------------------------------

    //--------------------------------- IMPRIMIR RECIBO ---------------------------------

    public void actionIniciaImpressaoRecibo(Lancamento lancamento) {
        this.lancamentoImpressao = lancamento;
        this.tipoReciboEscolhido = TipoRecibo.IMPRIMIR_SELECIONADO;
        this.incluirLogo = false;
    }

    public List<Lancamento> getLancamentosAEscolher() {
        return getLancamentos();
    }

    public void actionPrintReciboLancamento() {
        try {
            if (tipoReciboEscolhido == TipoRecibo.ESCOLHER) {
                PrimeFaces.current().executeScript("PF('dlgImprimirReciboEscolhaLancamento').show()");
            } else {
                if (tipoReciboEscolhido == TipoRecibo.IMPRIMIR_SELECIONADO) {
                    this.lancamentosImpressao = new ArrayList<>();
                    this.lancamentosImpressao.add(this.lancamentoImpressao);
                } else if (tipoReciboEscolhido == TipoRecibo.IMPRIMIR_TODOS) {
                    this.lancamentosImpressao = new ArrayList<>();
                    this.lancamentosImpressao.addAll(getLancamentosAEscolher());
                }
                this.valorTotalRecibo = lancamentosImpressao.stream().map(l -> l.getValor()).reduce(BigDecimal.ZERO, BigDecimal::add);
                PrimeFaces.current().executeScript("PF('dlgPrintReciboFatura').show()");
                // Imprimi mesmo

                addInfo("Sucesso!", "Recibo gerado");
                PrimeFaces.current().executeScript("PF('dlgImprimirRecibo').hide()");
            }
        } catch (Exception e) {
            addError("Erro!", "Falha ao gerar o Recibo!");
        }
    }

    public void actionPrintReciboLancamentoEscolha() {
        try {
            if (lancamentosImpressao != null && !lancamentosImpressao.isEmpty()) {
                this.valorTotalRecibo = lancamentosImpressao.stream().map(l -> l.getValor()).reduce(BigDecimal.ZERO, BigDecimal::add);
                PrimeFaces.current().executeScript("PF('dlgPrintReciboFatura').show()");

                // Imprimi mesmo
                addInfo("Sucesso!", "Recibos gerados");
                PrimeFaces.current().executeScript("PF('dlgImprimirReciboEscolhaLancamento').hide()");
                PrimeFaces.current().executeScript("PF('dlgImprimirRecibo').hide()");
            } else
                addError("Erro!", "Selecione ao menos um Lançamento!");
        } catch (Exception e) {
            addError("Erro!", "Falha ao gerar o Recibo!");
        }
    }

    public void salvaHtmlReciboContent() {
        try {

            Documento documento = DocumentoSingleton.getInstance().getBo().findByDescricao("Recibo com valor do recebimento");

            Dominio dominio = DominioSingleton.getInstance().getBo().findByEmpresaAndObjetoAndTipoAndNome("documento", "tipo", "Recibo");

            //TODO arrumar construtor pra isso
            if (documento == null) {
                documento = new Documento();
                documento.setAtivo("S");
                documento.setExcluido("N");
                documento.setDataCriacao(new Date());
                documento.setTipo(dominio);
                documento.setModelo("");
                documento.setDescricao("Recibo com valor do recebimento");
                documento.setIdEmpresa(UtilsFrontEnd.getEmpresaLogada().getEmpIntCod());
                DocumentoSingleton.getInstance().getBo().persist(documento);
            }

            DocumentoEmitido de = new DocumentoEmitido(this.htmlReciboContent, documento, dominio, UtilsFrontEnd.getProfissionalLogado(), getEntity().getPaciente(), null, new Date());
            de.setValor(valorTotalRecibo.doubleValue());
            DocumentoEmitidoSingleton.getInstance().getBo().persist(de);

        } catch (Exception e) {
            e.printStackTrace();
            addError("Erro!", "Falha ao salvar o Recibo!");
        }
    }

    //--------------------------------- IMPRIMIR RECIBO ---------------------------------

    //-------------------------------- NOVO - NOVO LANÇAMENTO --------------------------------    

    public void actionPersistNovoNovoLancamento() {
        actionPersistNovoNovoLancamento(true);
    }

    public void actionPersistNovoNovoLancamento(boolean forcaFechamentoDialog) {
        try {
            if (novoLancamentoQuantidadeParcelas == null || novoLancamentoValorDaPrimeiraParcela == null || novoLancamentoFormaPagamento == null || novoLancamentoDataPagamento == null || novoLancamentoDataCredito == null) {
                this.addError("Erro!", "Preencha todos os campos!");
                return;
            }
            BigDecimal valorTotalExistente = LancamentoSingleton.getInstance().getTotalLancamentoPorFatura(getEntity(), ValidacaoLancamento.TODOS_ATIVOS);
            valorTotalExistente = valorTotalExistente.add(novoLancamentoValorTotal);
            if (valorTotalExistente.compareTo(FaturaSingleton.getInstance().getTotal(getEntity())) > 0) {
                this.addError("Erro!", "O valor do lançamento não pode exceder o total da fatura !");
                return;
            }

            //List<BigDecimal> demaisParcelas = new ArrayList<>();
            //for (int parcela = 1; parcela <= novoLancamentoQuantidadeParcelas - 1; parcela++)
            //    demaisParcelas.add(novoLancamentoValorDaParcela);
            //LancamentoSingleton.getInstance().novoParcelamento(getEntity(), novoLancamentoQuantidadeParcelas, novoLancamentoValorDaPrimeiraParcela, demaisParcelas, novoLancamentoFormaPagamento,
            //        novoLancamentoDataPagamento, novoLancamentoDataCredito, UtilsFrontEnd.getProfissionalLogado());

            //if (novoLancamentoParcelas != null && novoLancamentoParcelas.size() > 0 && novoLancamentoValorDaPrimeiraParcelaDiferenca != null)
            //    novoLancamentoParcelas.get(0).setValor(novoLancamentoParcelas.get(0).getValor().add(novoLancamentoValorDaPrimeiraParcelaDiferenca));
            LancamentoSingleton.getInstance().novoParcelamento(getEntity(), novoLancamentoParcelas, UtilsFrontEnd.getProfissionalLogado());

            setEntity(FaturaSingleton.getInstance().getBo().find(getEntity()));
            //  updateValues(getEntity(), true, true);

            pesquisar();

            if (forcaFechamentoDialog)
                PrimeFaces.current().executeScript("PF('dlgNovoLancamento').hide()");
            else
                actionNovoLancamentoNew();
        } catch (Exception e) {
            LogIntelidenteSingleton.getInstance().makeLog(e);
            e.printStackTrace();
            this.addError("Erro!", Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO));
        }
    }

    private void novoLancamentoLimpaCampos() {
        novoLancamentoQuantidadeParcelas = null;
        novoLancamentoFormaPagamento = null;
        novoLancamentoDataPagamento = new Date();
        novoLancamentoDataCredito = null;
    }

    private void novoLancamentoZeraValores() {
        novoLancamentoValorDaPrimeiraParcela = null;
        novoLancamentoValorDaParcela = null;
    }

    public void actionNovoLancamentoNew() {
        if (novoLancamentoParcelasDisponiveis == null)
            novoLancamentoAtualizaPossibilidadesParcelas();
        novoLancamentoValorTotal = getEntity().getDadosTabelaRepasseTotalNaoPlanejado();
        novoLancamentoMensagemCalculo = null;
        novoLancamentoMensagemErroCalculo = null;
        this.novoLancamentoParcelas = new ArrayList<>();
        novoLancamentoLimpaCampos();
        novoLancamentoZeraValores();
        novoLancamentoAtualizaTooltipTarifasDisponiveis();
    }

    public void actionNovoLancamentoAtualizaQuantidadeDeParcelas() {
        novoLancamentoAtualizaPossibilidadesTarifa();
        novoLancamentoZeraValores();
        novoLancamentoRefazCalculos();
    }

    public void actionNovoLancamentoAlteraFormaPagamento() {
        actionNovoLancamentoAlteraDataPagamento();
        novoLancamentoAtualizaListaParcelas();
    }

    public void actionNovoLancamentoAlteraDataPagamento() {
        if (novoLancamentoFormaPagamento != null && novoLancamentoDataPagamento != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(novoLancamentoDataPagamento);
            cal.add(Calendar.DAY_OF_MONTH, novoLancamentoFormaPagamento.getPrazo());
            novoLancamentoDataCredito = cal.getTime();
        } else
            novoLancamentoDataCredito = null;
    }

    public void novoLancamentoAtualizaPossibilidadesTarifa() {
        try {
            novoLancamentoTarifasDisponiveis = new ArrayList<>();
            novoLancamentoTarifasDisponiveisEEntrada = new ArrayList<>();
            if (novoLancamentoQuantidadeParcelas == null)
                return;

            List<Tarifa> listaFormasDePagamento = TarifaSingleton.getInstance().getBo().listByEmpresa(UtilsFrontEnd.getEmpresaLogada().getEmpIntCod(), FormaPagamento.RECEBIMENTO);

            novoLancamentoTarifasDisponiveis.addAll(listaFormasDePagamento);
            novoLancamentoTarifasDisponiveisEEntrada.addAll(listaFormasDePagamento);
            /*
             * if (listaFormasDePagamento != null) { for (Tarifa formaPagamento : listaFormasDePagamento) { if (formaPagamento.getParcelaMinima() != null && formaPagamento.getParcelaMaxima() != null)
             * { if (novoLancamentoQuantidadeParcelas >= formaPagamento.getParcelaMinima() && novoLancamentoQuantidadeParcelas <= formaPagamento.getParcelaMaxima() &&
             * novoLancamentoTarifasDisponiveis.indexOf( formaPagamento) == -1) novoLancamentoTarifasDisponiveis.add(formaPagamento); if
             * (novoLancamentoTarifasDisponiveisEEntrada.indexOf(formaPagamento) == -1) novoLancamentoTarifasDisponiveisEEntrada.add(formaPagamento); } if (formaPagamento.getParcelaMinima() == 1 &&
             * novoLancamentoTarifasDisponiveisEEntrada.indexOf(formaPagamento) == -1) novoLancamentoTarifasDisponiveisEEntrada.add(formaPagamento); } }
             */

            novoLancamentoAtualizaTooltipTarifasDisponiveis();
        } catch (Exception e) {
            // TODO Auto-generated catch block
        }
    }

    public void novoLancamentoAtualizaPossibilidadesParcelas() {
        try {
            novoLancamentoParcelasDisponiveis = new ArrayList<>();

            List<Tarifa> listaFormasDePagamento = TarifaSingleton.getInstance().getBo().listByEmpresa(UtilsFrontEnd.getEmpresaLogada().getEmpIntCod(), FormaPagamento.RECEBIMENTO);
            if (listaFormasDePagamento != null)
                for (Tarifa formaPagamento : listaFormasDePagamento)
                    if (formaPagamento.getParcelaMinima() != null && formaPagamento.getParcelaMaxima() != null)
                        for (int parcela = formaPagamento.getParcelaMinima(); parcela <= formaPagamento.getParcelaMaxima(); parcela++)
                            if (novoLancamentoParcelasDisponiveis.indexOf(parcela) == -1)
                                novoLancamentoParcelasDisponiveis.add(parcela);

            Collections.sort(novoLancamentoParcelasDisponiveis);
        } catch (Exception e) {
            // TODO Auto-generated catch block
        }
    }

    public void novoLancamentoRefazCalculos() {
        if (novoLancamentoQuantidadeParcelas == null)
            return;
        novoLancamentoValorDaPrimeiraParcelaDiferenca = BigDecimal.ZERO;

        if (novoLancamentoValorDaPrimeiraParcela != null) {
            if (novoLancamentoValorDaPrimeiraParcela.compareTo(novoLancamentoValorTotal) > 0 && novoLancamentoQuantidadeParcelas != 1) {
                this.addError("Erro", "Insira uma primeira parcela menor que o total!");
                return;
            } else if (novoLancamentoQuantidadeParcelas > 1 && novoLancamentoValorDaPrimeiraParcela.compareTo(novoLancamentoValorTotal) >= 0) {
                this.addError("Erro", "Primeira parcela não pode ser igual ao valor total!");
                return;
            }

            if (novoLancamentoQuantidadeParcelas > 1) {
                BigDecimal quantidadeParcelasBD = BigDecimal.valueOf(novoLancamentoQuantidadeParcelas - 1);
                novoLancamentoValorDaParcela = novoLancamentoValorTotal.subtract(novoLancamentoValorDaPrimeiraParcela).divide(quantidadeParcelasBD, 2, BigDecimal.ROUND_HALF_UP);

                BigDecimal totalParcelado = quantidadeParcelasBD.multiply(novoLancamentoValorDaParcela).add(novoLancamentoValorDaPrimeiraParcela);
                novoLancamentoValorDaPrimeiraParcelaDiferenca = novoLancamentoValorTotal.subtract(totalParcelado);

                atualizaInfoParcelamentoNovoLancamento(TipoNegociacao.PARCELADO_PRIMEIRA_PARCELA_DIFERENTE);
            } else {
                novoLancamentoValorDaPrimeiraParcelaDiferenca = BigDecimal.ZERO;
                novoLancamentoValorDaPrimeiraParcela = novoLancamentoValorTotal;
                novoLancamentoValorDaParcela = null;

                atualizaInfoParcelamentoNovoLancamento(TipoNegociacao.A_VISTA);
            }
        } else {
            if (novoLancamentoQuantidadeParcelas > 1) {
                BigDecimal quantidadeParcelasBD = BigDecimal.valueOf(novoLancamentoQuantidadeParcelas);
                novoLancamentoValorDaParcela = novoLancamentoValorTotal.divide(quantidadeParcelasBD, 2, BigDecimal.ROUND_HALF_UP);
                novoLancamentoValorDaPrimeiraParcela = novoLancamentoValorDaParcela;

                BigDecimal totalParcelado = quantidadeParcelasBD.multiply(novoLancamentoValorDaParcela);
                novoLancamentoValorDaPrimeiraParcelaDiferenca = novoLancamentoValorTotal.subtract(totalParcelado);

                atualizaInfoParcelamentoNovoLancamento(TipoNegociacao.PARCELADO_TODAS_PARCELAS_IGUAIS);
            } else {
                novoLancamentoValorDaPrimeiraParcelaDiferenca = BigDecimal.ZERO;
                novoLancamentoValorDaPrimeiraParcela = novoLancamentoValorTotal;
                novoLancamentoValorDaParcela = null;

                atualizaInfoParcelamentoNovoLancamento(TipoNegociacao.A_VISTA);
            }
        }

        novoLancamentoAtualizaListaParcelas();
    }

    private void novoLancamentoAtualizaListaParcelas() {
        this.novoLancamentoParcelas = atualizaListaParcelas(novoLancamentoDataPagamento, novoLancamentoDataCredito, novoLancamentoQuantidadeParcelas, novoLancamentoValorDaPrimeiraParcela,
                novoLancamentoValorDaParcela, novoLancamentoValorDaPrimeiraParcelaDiferenca, novoLancamentoFormaPagamento);
    }

    private void novoLancamentoAtualizaTooltipTarifasDisponiveis() {
        List<String[]> linhas = new ArrayList<>();
        if (novoLancamentoTarifasDisponiveis != null && !novoLancamentoTarifasDisponiveis.isEmpty()) {
            for (Tarifa tarifa : novoLancamentoTarifasDisponiveis)
                linhas.add(new String[] { tarifa.getProdutoStr(), String.valueOf(tarifa.getParcelaMinima()), String.valueOf(tarifa.getParcelaMaxima()) });
        } else {
            linhas.add(new String[] { "Nenhum registro disponível!", "Nenhum registro disponível!", "Nenhum registro disponível!" });
        }
        novoLancamentoTooltipNegociacaoTarifasDisponiveis = TooltipHelper.getInstance().montaTabela(new String[] { "Produto", "Parcela Mínima", "Parcela Máxima" }, linhas);
    }

    private void atualizaInfoParcelamentoNovoLancamento(TipoNegociacao tipoNegociacao) {
        NumberFormat ptFormat = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

        if (tipoNegociacao == TipoNegociacao.PARCELADO_PRIMEIRA_PARCELA_DIFERENTE) {
            novoLancamentoMensagemCalculo = "Entrada de " + ptFormat.format(novoLancamentoValorDaPrimeiraParcela) + " mais " + String.valueOf(
                    novoLancamentoQuantidadeParcelas - 1) + "x de " + ptFormat.format(novoLancamentoValorDaParcela) + ". Total de " + ptFormat.format(novoLancamentoValorTotal);
        } else if (tipoNegociacao == TipoNegociacao.PARCELADO_TODAS_PARCELAS_IGUAIS) {
            novoLancamentoMensagemCalculo = "Pagamento em " + String.valueOf(novoLancamentoQuantidadeParcelas) + "x de " + ptFormat.format(
                    novoLancamentoValorDaParcela) + ". Total de " + ptFormat.format(novoLancamentoValorTotal);
        } else if (tipoNegociacao == TipoNegociacao.A_VISTA) {
            novoLancamentoMensagemCalculo = "Pagamento a vista, valor de R$ " + ptFormat.format(novoLancamentoValorTotal);
        }

        if (novoLancamentoValorDaPrimeiraParcelaDiferenca != null && novoLancamentoValorDaPrimeiraParcelaDiferenca.compareTo(BigDecimal.ZERO) != 0) {
            //novoLancamentoMensagemErroCalculo = "Atenção! Diferença de " + ptFormat.format(
            //        novoLancamentoValorDaPrimeiraParcelaDiferenca) + " na soma das parcelas. " + "Essa diferença será somada na primeira parcela automaticamente.";
        } else {
            novoLancamentoMensagemErroCalculo = null;
        }
    }

    //-------------------------------- NOVO - NOVO LANÇAMENTO --------------------------------

    //-------------------------------- NEGOCIACAO --------------------------------

    public void atualizaTipoDesconto() {

    }

    public void actionAlteraDescontoValor() {
        this.negociacaoValorDaPrimeiraParcela = null;
    }

    public void actionAlteraDataPagamentoNegociacao(int campo) {
        if (campo == 1) {
            if (negociacaoFormaPagamento != null && negociacaoDataPagamento != null) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(negociacaoDataPagamento);
                cal.add(Calendar.DAY_OF_MONTH, negociacaoFormaPagamento.getPrazo());
                negociacaoDataCredito = cal.getTime();
            } else
                negociacaoDataCredito = null;

            negociacaoAtualizaListaParcelas();
        } else if (campo == 2) {
            if (negociacaoFormaPagamento1Parcela != null && negociacaoFormaPagamento1Parcela != null) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(negociacaoDataPagamento1Parcela);
                cal.add(Calendar.DAY_OF_MONTH, negociacaoFormaPagamento1Parcela.getPrazo());
                negociacaoDataCredito1Parcela = cal.getTime();
            } else
                negociacaoDataCredito1Parcela = null;

            negociacaoAtualizaListaParcelas();
        } else if (campo == 3) {
            if (negociacaoFormaPagamentoDemaisParcelas != null && negociacaoDataPagamentoDemaisParcelas != null) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(negociacaoDataPagamentoDemaisParcelas);
                cal.add(Calendar.DAY_OF_MONTH, negociacaoFormaPagamentoDemaisParcelas.getPrazo());
                negociacaoDataCreditoDemaisParcelas = cal.getTime();
            } else
                negociacaoDataCreditoDemaisParcelas = null;

            negociacaoAtualizaListaParcelas();
        }
    }

    public void actionAlteraDataCreditoNegociacao(int campo) {
        negociacaoAtualizaListaParcelas();
    }

    public void actionAlteraFormaPagamentoNegociacao(int campo) {
        if (campo == 1) {
            if (this.negociacaoDataPagamento == null)
                this.negociacaoDataPagamento = new Date();
        } else if (campo == 2) {
            if (this.negociacaoDataPagamento1Parcela == null)
                this.negociacaoDataPagamento1Parcela = new Date();
        } else if (campo == 3) {
            if (this.negociacaoDataPagamentoDemaisParcelas == null)
                this.negociacaoDataPagamentoDemaisParcelas = new Date();
        }
        actionAlteraDataPagamentoNegociacao(campo);

        // Ja chama no metodo anterior
        // negociacaoAtualizaListaParcelas();
    }

    public void actionNovaNegociacao() {
        if (parcelasDisponiveis == null)
            atualizaPossibilidadesNegociacao();
        negociacaoQuantidadeParcelas = null;
        negociacaoTipoDesconto = "P";
        negociacaoObservacao = null;
        zeraValores();
        atualizaTooltipTarifasDisponiveis();
    }

    public void actionPersistNegociacao() {
        try {
            if (negociacaoQuantidadeParcelas == null || negociacaoValorTotal == null) {
                addError("Erro!", "Preencha todos os itens!");
                return;
            }

            BigDecimal valorDeDesconto = (negociacaoValorDesconto == null ? BigDecimal.ZERO : negociacaoValorDesconto);
            if ("P".equals(negociacaoTipoDesconto) && negociacaoValorDesconto != null)
                valorDeDesconto = negociacaoValorDesconto.divide(BigDecimal.valueOf(100)).multiply(negociacaoValorTotal);
            BigDecimal totalSemDesconto = negociacaoValorTotal.subtract(valorDeDesconto);

            //if (negociacaoValorDaPrimeiraParcela != null && negociacaoValorDaPrimeiraParcelaDirefenca != null)
            //    negociacaoValorDaPrimeiraParcela = negociacaoValorDaPrimeiraParcela.add(negociacaoValorDaPrimeiraParcelaDirefenca);

            //uma parcela só, nao tem entrada
            if (negociacaoQuantidadeParcelas == 1) {
                negociacaoValorDaParcela = negociacaoValorDaPrimeiraParcela;
                negociacaoValorDaPrimeiraParcela = new BigDecimal(0);
            }

            NegociacaoFaturaSingleton.getInstance().criaNovaNegociacao(getEntity(), getDescontoObjFromParcela(negociacaoQuantidadeParcelas), negociacaoQuantidadeParcelas, negociacaoTipoDesconto,
                    negociacaoValorDesconto, negociacaoValorDaPrimeiraParcela, negociacaoValorDaParcela, negociacaoValorDaPrimeiraParcelaDirefenca, negociacaoValorTotal, totalSemDesconto,
                    negociacaoObservacao, UtilsFrontEnd.getProfissionalLogado());

            setEntity(FaturaSingleton.getInstance().getBo().find(getEntity()));
            updateValues(getEntity(), true, false);

            PrimeFaces.current().executeScript("PF('dlgNovaNegociacao').hide()");
        } catch (Exception e) {
            LogIntelidenteSingleton.getInstance().makeLog(e);
            this.addError("Erro!", e.getMessage());
        }
    }

    public void actionCancelaNegociacao(NegociacaoFatura negociacao) {
        try {
            NegociacaoFaturaSingleton.getInstance().cancelarNegociacao(negociacao, UtilsFrontEnd.getProfissionalLogado());

            setEntity(FaturaSingleton.getInstance().getBo().find(getEntity()));
            updateValues(getEntity(), true, false);
        } catch (Exception e) {
            LogIntelidenteSingleton.getInstance().makeLog(e);
            this.addError("Erro!", e.getMessage());
        }
    }

    public void actionConfirmaNegociacao() {
        try {
            if ((!temPrimeiraParcelaDiferente() && (negociacaoFormaPagamento == null || negociacaoDataPagamento == null || negociacaoDataCredito == null)) || (temPrimeiraParcelaDiferente() && (negociacaoFormaPagamento1Parcela == null || negociacaoFormaPagamentoDemaisParcelas == null || negociacaoDataPagamento1Parcela == null || negociacaoDataPagamentoDemaisParcelas == null || negociacaoDataCredito1Parcela == null || negociacaoDataCreditoDemaisParcelas == null))) {
                addError("Erro!", "Preencha todos os itens!");
                return;
            }
            NegociacaoFaturaSingleton.getInstance().aprovarNegociacao(negociacaoConfirmacao, negociacaoParcelas, UtilsFrontEnd.getProfissionalLogado());

            setEntity(FaturaSingleton.getInstance().getBo().find(getEntity()));
            updateValues(getEntity(), true, false);

            PrimeFaces.current().executeScript("PF('dlgAprovaNegociacao').hide()");
        } catch (Exception e) {
            LogIntelidenteSingleton.getInstance().makeLog(e);
            this.addError("Erro!", e.getMessage());
        }
    }

    public boolean temPrimeiraParcelaDiferente() {
        if (this.negociacaoValorDaParcela == null || this.negociacaoValorDaPrimeiraParcela == null || new BigDecimal(0).compareTo(this.negociacaoValorDaPrimeiraParcela) == 0) {
            return false;
        }
        return true;
//        if (this.negociacaoValorDaParcela != null && this.negociacaoValorDaParcela.compareTo(
//                BigDecimal.ZERO) != 0 && this.negociacaoValorDaPrimeiraParcela != null && this.negociacaoValorDaPrimeiraParcela.compareTo(
//                        BigDecimal.ZERO) != 0 && this.negociacaoValorDaParcela.compareTo(this.negociacaoValorDaPrimeiraParcela) != 0)
//            return true;
//        return false;
    }

    public void actionStartConfirmacaNegociacao(NegociacaoFatura negociacaoFatura) {
        this.negociacaoFormaPagamento = null;
        this.negociacaoFormaPagamento1Parcela = null;
        this.negociacaoFormaPagamentoDemaisParcelas = null;
        this.negociacaoDataPagamento = null;
        this.negociacaoDataPagamento1Parcela = null;
        this.negociacaoDataPagamentoDemaisParcelas = null;
        this.negociacaoDataCredito = null;
        this.negociacaoDataCredito1Parcela = null;
        this.negociacaoDataCreditoDemaisParcelas = null;

        negociacaoConfirmacao = negociacaoFatura;
        if (parcelasDisponiveis == null)
            atualizaPossibilidadesNegociacao();
        this.negociacaoQuantidadeParcelas = negociacaoFatura.getQuantidadeParcelas();
        this.negociacaoTipoDesconto = negociacaoFatura.getTipoDesconto();
        this.negociacaoValorDesconto = negociacaoFatura.getValorDesconto();
        this.negociacaoValorDaPrimeiraParcela = negociacaoFatura.getValorPrimeiraParcela();
        this.negociacaoValorDaParcela = negociacaoFatura.getValorParcela();
        this.negociacaoValorDaPrimeiraParcelaDirefenca = negociacaoFatura.getCorrecaoValor();
        this.negociacaoObservacao = negociacaoFatura.getObservacao();
        this.negociacaoMensagemCalculo = null;
        this.negociacaoMensagemErroCalculo = null;

        atualizaPossibilidadesTarifaNegociacao();
        atualizaTooltipTarifasDisponiveis();
        //refazCalculos();

        this.negociacaoValorTotal = negociacaoFatura.getValorTotal();
        if (negociacaoFatura.getResultadoDesconto() != null)
            this.negociacaoValorTotal = negociacaoFatura.getValorTotal().subtract(negociacaoFatura.getResultadoDesconto());

        if (this.negociacaoQuantidadeParcelas == 1) {
            BigDecimal valorPago = FaturaSingleton.getInstance().getTotalPago(getEntity());
            this.negociacaoValorDaParcela = this.negociacaoValorTotal.subtract(valorPago);
        }

        if ((this.negociacaoValorDaParcela == null || this.negociacaoValorDaParcela.compareTo(
                BigDecimal.ZERO) == 0) && (this.negociacaoValorDaPrimeiraParcela == null || this.negociacaoValorDaPrimeiraParcela.compareTo(BigDecimal.ZERO) == 0))
            this.negociacaoValorDaPrimeiraParcela = this.negociacaoValorTotal;

        if (negociacaoQuantidadeParcelas == 1)
            atualizaInfoParcelamentoNegociacao(Boolean.TRUE, TipoNegociacao.A_VISTA);
        else if (negociacaoValorDaPrimeiraParcela != null && negociacaoValorDaPrimeiraParcela.compareTo(BigDecimal.ZERO) != 0 && negociacaoValorDaParcela.compareTo(
                negociacaoValorDaPrimeiraParcela) != 0)
            atualizaInfoParcelamentoNegociacao(Boolean.TRUE, TipoNegociacao.PARCELADO_PRIMEIRA_PARCELA_DIFERENTE);
        else {
            //negociacaoValorDaPrimeiraParcela = negociacaoValorDaParcela;
            atualizaInfoParcelamentoNegociacao(Boolean.TRUE, TipoNegociacao.PARCELADO_TODAS_PARCELAS_IGUAIS);
        }

        if (temPrimeiraParcelaDiferente()) {
            this.negociacaoValorTotal1Parcela = this.negociacaoValorDaPrimeiraParcela;
            this.negociacaoValorTotalDemaisParcelas = this.negociacaoValorDaParcela;
        }

        negociacaoAtualizaListaParcelas();
    }
    
    public void atualizaQuantidadeDeParcelas() {
        zeraValores();
        negociacaoTipoDesconto = "P";
        if (getDescontoObjFromParcela(negociacaoQuantidadeParcelas) != null) {
            negociacaoValorDesconto = getDescontoObjFromParcela(negociacaoQuantidadeParcelas).getDesconto();
        } else {
            negociacaoValorDesconto = null;
        }

        negociacaoValorDesconto = BigDecimal.ZERO;

        refazCalculos();
        atualizaPossibilidadesTarifaNegociacao();

        try {
            atualizaListaTarifasDisponiveis();
        } catch (Exception e) {
        }
    }

    public boolean isPagamentoAVista() {
        return negociacaoQuantidadeParcelas != null && negociacaoQuantidadeParcelas == 1;
    }

    private void zeraValores() {
        negociacaoValorDesconto = null;
        negociacaoValorDaPrimeiraParcela = null;
        negociacaoValorDaParcela = null;
        negociacaoValorTotal = null;
    }

    public void refazCalculos() { 
        negociacaoValorTotal = FaturaSingleton.getInstance().getTotal(getEntity());
        BigDecimal valorDeDesconto = (negociacaoValorDesconto == null ? BigDecimal.ZERO : negociacaoValorDesconto);
        DescontoOrcamento descontoCadQtdeParcelas = descontosDisponiveis.get(negociacaoQuantidadeParcelas);
        BigDecimal desconto;
        if(negociacaoTipoDesconto.equals("V") ) {
            desconto = negociacaoValorTotal.multiply(descontoCadQtdeParcelas.getDesconto()).divide(new BigDecimal(100));
        }else {
            desconto = descontoCadQtdeParcelas.getDesconto();
        }
        if ((descontoCadQtdeParcelas == null && valorDeDesconto.compareTo(BigDecimal.ZERO) > 0) || (descontoCadQtdeParcelas != null && valorDeDesconto.compareTo(
                desconto) > 0)) {
            negociacaoValorDesconto = new BigDecimal(0);
            PrimeFaces.current().ajax().update("lume:tabViewPaciente:negociacaoValorDescPorcentagem");
            PrimeFaces.current().ajax().update("lume:tabViewPaciente:negociacaoValorDesc");
            this.addError("Erro!", "Desconto dado maior que o máximo permitido.");
            return;
        }

        BigDecimal valorPago = FaturaSingleton.getInstance().getTotalPago(getEntity());
        
        if ("P".equals(negociacaoTipoDesconto) && negociacaoValorDesconto != null)
            valorDeDesconto = negociacaoValorDesconto.divide(BigDecimal.valueOf(100)).multiply(negociacaoValorTotal);
        BigDecimal totalSemDesconto = negociacaoValorTotal.subtract(valorDeDesconto);

        totalSemDesconto = totalSemDesconto.subtract(valorPago);
        
        if (negociacaoValorDaPrimeiraParcela != null && negociacaoValorDaPrimeiraParcela.compareTo(BigDecimal.ZERO) != 0) {
            if (negociacaoValorDaPrimeiraParcela.compareTo(totalSemDesconto) > 0) {
                this.addError("Erro", "Insira uma primeira parcela menor que o total!");
                return;
            }

            if (negociacaoQuantidadeParcelas > 1) {
                BigDecimal quantidadeParcelasBD = BigDecimal.valueOf(negociacaoQuantidadeParcelas - 1);
                negociacaoValorDaParcela = totalSemDesconto.subtract(negociacaoValorDaPrimeiraParcela).divide(quantidadeParcelasBD, 2, BigDecimal.ROUND_HALF_UP);

                BigDecimal totalParcelado = quantidadeParcelasBD.multiply(negociacaoValorDaParcela).add(negociacaoValorDaPrimeiraParcela);
                negociacaoValorDaPrimeiraParcelaDirefenca = totalSemDesconto.subtract(totalParcelado);

                atualizaInfoParcelamentoNegociacao(TipoNegociacao.PARCELADO_PRIMEIRA_PARCELA_DIFERENTE);
            } else {
                negociacaoValorDaPrimeiraParcelaDirefenca = BigDecimal.ZERO;
                negociacaoValorDaPrimeiraParcela = totalSemDesconto;
                negociacaoValorDaParcela = null;

                atualizaInfoParcelamentoNegociacao(TipoNegociacao.A_VISTA);
            }
        } else {
            if (negociacaoQuantidadeParcelas > 1) {
                BigDecimal quantidadeParcelasBD = BigDecimal.valueOf(negociacaoQuantidadeParcelas);
                negociacaoValorDaParcela = totalSemDesconto.divide(quantidadeParcelasBD, 2, BigDecimal.ROUND_HALF_UP);
                //negociacaoValorDaPrimeiraParcela = negociacaoValorDaParcela;

                BigDecimal totalParcelado = quantidadeParcelasBD.multiply(negociacaoValorDaParcela);
                negociacaoValorDaPrimeiraParcelaDirefenca = totalSemDesconto.subtract(totalParcelado);

                atualizaInfoParcelamentoNegociacao(TipoNegociacao.PARCELADO_TODAS_PARCELAS_IGUAIS);
            } else {
                negociacaoValorDaPrimeiraParcelaDirefenca = BigDecimal.ZERO;
                negociacaoValorDaPrimeiraParcela = totalSemDesconto;
                negociacaoValorDaParcela = null;

                atualizaInfoParcelamentoNegociacao(TipoNegociacao.A_VISTA);
            }
        }
    }

    private void atualizaInfoParcelamentoNegociacao(TipoNegociacao tipoNegociacao) {
        atualizaInfoParcelamentoNegociacao(false, tipoNegociacao);
    }

    private void atualizaInfoParcelamentoNegociacao(boolean totalJaComDesconto, TipoNegociacao tipoNegociacao) {
        NumberFormat ptFormat = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
        
        BigDecimal valorDeDesconto = (negociacaoValorDesconto == null ? BigDecimal.ZERO : negociacaoValorDesconto);
        if ("P".equals(negociacaoTipoDesconto) && negociacaoValorDesconto != null)
            valorDeDesconto = negociacaoValorDesconto.divide(BigDecimal.valueOf(100)).multiply(negociacaoValorTotal);
        BigDecimal totalSemDesconto = null;

        totalSemDesconto = (totalJaComDesconto ? negociacaoValorTotal : negociacaoValorTotal.subtract(valorDeDesconto));
        
//        if(negociacaoConfirmacao != null && negociacaoConfirmacao.getFatura() != null) {
//            if(FaturaSingleton.getInstance().getTotalPago(negociacaoConfirmacao.getFatura()).compareTo(BigDecimal.ZERO) > 0) {
//                List<Lancamento> lancamentos = negociacaoConfirmacao.getFatura().getLancamentosFiltered();
//                lancamentos.removeIf((lan) -> !(lan.getValidadoPorProfissional() != null && lan.getConferidoPorProfissional() != null));
//                lancamentos.forEach((lan) -> {
//                    negociacaoMensagemCalculo = "Parcela paga: " + lan.getValor() + "\n";
//                });
//            }
//        }
        
        if (tipoNegociacao == TipoNegociacao.PARCELADO_PRIMEIRA_PARCELA_DIFERENTE) {
            negociacaoMensagemCalculo = "Entrada de " + ptFormat.format(negociacaoValorDaPrimeiraParcela) + " mais " + String.valueOf(negociacaoQuantidadeParcelas - 1) + "x de " + ptFormat.format(
                    negociacaoValorDaParcela) + ". Total de " + ptFormat.format(totalSemDesconto);
        } else if (tipoNegociacao == TipoNegociacao.PARCELADO_TODAS_PARCELAS_IGUAIS) {
            negociacaoMensagemCalculo = "Pagamento em " + String.valueOf(negociacaoQuantidadeParcelas) + "x de " + ptFormat.format(negociacaoValorDaParcela) + ". Total de " + ptFormat.format(
                    totalSemDesconto);
        } else if (tipoNegociacao == TipoNegociacao.A_VISTA) {
            BigDecimal valorPago = FaturaSingleton.getInstance().getTotalPago(getEntity());
            if(valorPago.compareTo(BigDecimal.ZERO) > 0)
                negociacaoMensagemCalculo = "Pagamento a vista, valor de R$ " + ptFormat.format(totalSemDesconto.subtract(valorPago)) 
                    + ". Total de R$ " + ptFormat.format(totalSemDesconto);
            else
                negociacaoMensagemCalculo = "Pagamento a vista, valor de R$ " + ptFormat.format(totalSemDesconto);
        }

        if (negociacaoValorDaPrimeiraParcelaDirefenca != null && negociacaoValorDaPrimeiraParcelaDirefenca.compareTo(BigDecimal.ZERO) != 0) {
            //negociacaoMensagemErroCalculo = "Atenção! Diferença de " + ptFormat.format(
            //        negociacaoValorDaPrimeiraParcelaDirefenca) + " na soma das parcelas. " + "Essa diferença será somada na primeira parcela automaticamente.";
        } else {
            negociacaoMensagemErroCalculo = null;
        }
    }

    public void atualizaPossibilidadesTarifaNegociacao() {
        try {
            negociacaoTarifasDisponiveis = new ArrayList<>();
            negociacaoTarifasDisponiveisEEntrada = new ArrayList<>();
            if (negociacaoQuantidadeParcelas == null)
                return;

            List<Tarifa> listaFormasDePagamento = TarifaSingleton.getInstance().getBo().listByEmpresa(UtilsFrontEnd.getEmpresaLogada().getEmpIntCod(), FormaPagamento.RECEBIMENTO);

            negociacaoTarifasDisponiveis.addAll(listaFormasDePagamento);
            negociacaoTarifasDisponiveisEEntrada.addAll(listaFormasDePagamento);
            /*
             * if (listaFormasDePagamento != null) { for (Tarifa formaPagamento : listaFormasDePagamento) { if (formaPagamento.getParcelaMinima() != null && formaPagamento.getParcelaMaxima() != null)
             * { if (negociacaoQuantidadeParcelas >= formaPagamento.getParcelaMinima() && negociacaoQuantidadeParcelas <= formaPagamento.getParcelaMaxima() && negociacaoTarifasDisponiveis.indexOf(
             * formaPagamento) == -1) { negociacaoTarifasDisponiveis.add(formaPagamento); if (negociacaoTarifasDisponiveisEEntrada.indexOf(formaPagamento) == -1)
             * negociacaoTarifasDisponiveisEEntrada.add(formaPagamento); } } if (formaPagamento.getParcelaMinima() == 1 && negociacaoTarifasDisponiveisEEntrada.indexOf(formaPagamento) == -1)
             * negociacaoTarifasDisponiveisEEntrada.add(formaPagamento); } }
             */

        } catch (Exception e) {
            // TODO Auto-generated catch block
        }
    }

    public void atualizaPossibilidadesNegociacao() {
        try {
            parcelasDisponiveis = new ArrayList<>();

            List<Tarifa> listaFormasDePagamento = TarifaSingleton.getInstance().getBo().listByEmpresa(UtilsFrontEnd.getEmpresaLogada().getEmpIntCod(), FormaPagamento.RECEBIMENTO);
            if (listaFormasDePagamento != null)
                for (Tarifa formaPagamento : listaFormasDePagamento)
                    if (formaPagamento.getParcelaMinima() != null && formaPagamento.getParcelaMaxima() != null)
                        for (int parcela = formaPagamento.getParcelaMinima(); parcela <= formaPagamento.getParcelaMaxima(); parcela++)
                            if (parcelasDisponiveis.indexOf(parcela) == -1)
                                parcelasDisponiveis.add(parcela);

            Collections.sort(parcelasDisponiveis);

            descontosDisponiveis = new HashMap<>();
            for (Integer parcela : parcelasDisponiveis)
                descontosDisponiveis.put(parcela,
                        DescontoOrcamentoSingleton.getInstance().getBo().getMaiorDescontoFromParcela(UtilsFrontEnd.getProfissionalLogado(), UtilsFrontEnd.getEmpresaLogada(), parcela));
        } catch (Exception e) {
            // TODO Auto-generated catch block
        }
    }

    public DescontoOrcamento getDescontoObjFromParcela(Integer parcela) {
        if (parcela == null)
            return null;
        return this.descontosDisponiveis.get(parcela);
    }

    public String getDescontoFromParcela(Integer parcela) {
        String percent = null;
        NumberFormat percformat = NumberFormat.getPercentInstance(new Locale("pt", "BR"));
        if (parcela == null || this.descontosDisponiveis.get(parcela) == null) {
            percent = percformat.format(BigDecimal.ZERO);
        } else {
            percent = percformat.format(this.descontosDisponiveis.get(parcela).getDesconto().divide(BigDecimal.valueOf(100), 4, BigDecimal.ROUND_HALF_UP));
        }
        return percent;
    }

    private void atualizaListaTarifasDisponiveis() throws Exception {
        if (negociacaoQuantidadeParcelas == null)
            return;
        tarifasDisponiveis = new ArrayList<>();

        List<Tarifa> listaFormasDePagamento = TarifaSingleton.getInstance().getBo().listByEmpresa(UtilsFrontEnd.getEmpresaLogada().getEmpIntCod(), FormaPagamento.RECEBIMENTO);
        if (listaFormasDePagamento != null)
            for (Tarifa formaPagamento : listaFormasDePagamento)
                if (formaPagamento.getParcelaMinima() != null && formaPagamento.getParcelaMaxima() != null)
                    if (formaPagamento.getParcelaMinima() <= negociacaoQuantidadeParcelas && formaPagamento.getParcelaMaxima() >= negociacaoQuantidadeParcelas)
                        tarifasDisponiveis.add(formaPagamento);

        atualizaTooltipTarifasDisponiveis();
    }

    private void atualizaTooltipTarifasDisponiveis() {
        List<String[]> linhas = new ArrayList<>();
        if (tarifasDisponiveis != null && !tarifasDisponiveis.isEmpty()) {
            for (Tarifa tarifa : tarifasDisponiveis)
                linhas.add(new String[] { tarifa.getProdutoStr(), String.valueOf(tarifa.getParcelaMinima()), String.valueOf(tarifa.getParcelaMaxima()) });
        } else {
            linhas.add(new String[] { "Nenhum registro disponível!", "Nenhum registro disponível!", "Nenhum registro disponível!" });
        }
        tooltipNegociacaoTarifasDisponiveis = TooltipHelper.getInstance().montaTabela(new String[] { "Produto", "Parcela Mínima", "Parcela Máxima" }, linhas);
    }

    private void negociacaoAtualizaListaParcelas() {        
        this.negociacaoParcelas = atualizaListaParcelas((negociacaoDataPagamento1Parcela != null ? negociacaoDataPagamento1Parcela : negociacaoDataPagamento), negociacaoDataPagamentoDemaisParcelas,
                (negociacaoDataCredito1Parcela != null ? negociacaoDataCredito1Parcela : negociacaoDataCredito), negociacaoDataCreditoDemaisParcelas, negociacaoQuantidadeParcelas,
                negociacaoValorDaPrimeiraParcela, negociacaoValorDaParcela, negociacaoValorDaPrimeiraParcelaDirefenca,
                (negociacaoFormaPagamento1Parcela != null ? negociacaoFormaPagamento1Parcela : negociacaoFormaPagamento), negociacaoFormaPagamentoDemaisParcelas);
    }

    public String returnStrParcela(int seq) {
        if (temPrimeiraParcelaDiferente() && seq == 1)
            return "Entrada";
        else if (temPrimeiraParcelaDiferente())
            return (seq - 1) + "/" + (this.negociacaoParcelas.size() - 1);
        return seq + "/" + this.negociacaoParcelas.size();
    }

    //-------------------------------- NEGOCIACAO --------------------------------

    //-------------------------------- NEGOCIACAO E NOVO LANCAMENTO --------------------------------

    private enum TipoNegociacao {
        A_VISTA,
        PARCELADO_TODAS_PARCELAS_IGUAIS,
        PARCELADO_PRIMEIRA_PARCELA_DIFERENTE
    };

    private List<LancamentoParcelaInfo> atualizaListaParcelas(Date dataPagamento, Date dataCredito, Integer quantidadeParcelas, BigDecimal valorPrimeiraParcela, BigDecimal valorParcela,
            BigDecimal correcaoValor, Tarifa formaPagamento) {
        return atualizaListaParcelas(dataPagamento, null, dataCredito, null, quantidadeParcelas, valorPrimeiraParcela, valorParcela, correcaoValor, formaPagamento, null);
    }

    private List<LancamentoParcelaInfo> atualizaListaParcelas(Date dataPagamento, Date dataPagamentoDemaisParcelas, Date dataCredito, Date dataCreditoDemaisParcelas, Integer quantidadeParcelas,
            BigDecimal valorPrimeiraParcela, BigDecimal valorParcela, BigDecimal correcaoValor, Tarifa formaPagamento, Tarifa formaPagamentoDemaisParcelas) {
        List<LancamentoParcelaInfo> lancamentos = new ArrayList<>();

        Calendar now = null, data = null;
        boolean dataCredito1PD = false, dataPagamento1PD = false;
        if (dataPagamento != null) {
            now = Calendar.getInstance();
            dataPagamento1PD = dataPagamentoDemaisParcelas != null;
            now.setTime((dataPagamento1PD ? dataPagamentoDemaisParcelas : dataPagamento));
        }
        if (dataCredito != null) {
            data = Calendar.getInstance();
            dataCredito1PD = dataCreditoDemaisParcelas != null;
            data.setTime((dataCredito1PD ? dataCreditoDemaisParcelas : dataCredito));
        }

        for (int parcela = 1; parcela <= quantidadeParcelas; parcela++) {
            if (parcela == 1) {
                Date dataPagamento1P = (dataPagamento1PD ? (dataPagamento != null ? dataPagamento : null) : (now != null ? now.getTime() : null));
                Date dataCredito1P = (dataCredito1PD ? (dataCredito != null ? dataCredito : null) : (data != null ? data.getTime() : null));
                int parcelaAtual = (formaPagamentoDemaisParcelas == null ? parcela : 1);
                int totalParcela = (formaPagamentoDemaisParcelas == null ? quantidadeParcelas : 1);
                BigDecimal valorParcelaAtual = (valorPrimeiraParcela == null || valorPrimeiraParcela.compareTo(BigDecimal.ZERO) == 0 ? valorParcela : valorPrimeiraParcela);
                lancamentos.add(new LancamentoParcelaInfo(parcela, parcelaAtual, totalParcela, valorParcelaAtual, formaPagamento, dataPagamento1P, dataCredito1P));
            } else {
                BigDecimal valorParcelaAtual = valorParcela;
                if (correcaoValor != null && parcela == quantidadeParcelas)
                    valorParcelaAtual = valorParcelaAtual.add(correcaoValor);
                int parcelaAtual = (formaPagamentoDemaisParcelas == null ? parcela : parcela - 1);
                int totalParcela = (formaPagamentoDemaisParcelas == null ? quantidadeParcelas : quantidadeParcelas - 1);
                lancamentos.add(new LancamentoParcelaInfo(parcela, parcelaAtual, totalParcela, valorParcelaAtual,
                        (formaPagamentoDemaisParcelas != null ? formaPagamentoDemaisParcelas : formaPagamento), (now != null ? now.getTime() : null), (data != null ? data.getTime() : null)));
            }

            if ((parcela == 1 && formaPagamentoDemaisParcelas == null) || parcela != 1) {
                Tarifa tarifa = (formaPagamentoDemaisParcelas == null ? formaPagamento : formaPagamentoDemaisParcelas);
                if (tarifa != null && "CC".equals(tarifa.getTipo())) {
                    if (data != null)
                        data.add(Calendar.DAY_OF_MONTH, tarifa.getPrazo());
                } else if (tarifa != null && !"CC".equals(tarifa.getTipo())) {
                    if (now != null)
                        now.add(Calendar.MONTH, 1);
                    if (data != null)
                        data.add(Calendar.MONTH, 1);
                }
            }
        }

        return lancamentos;
    }

    //-------------------------------- NEGOCIACAO E NOVO LANCAMENTO --------------------------------

    public double tributoLancamento(Lancamento lancamento) {

        return 0;
    }

    public String getStyleBySaldoPaciente() throws Exception {
        if (getPaciente() == null || getPaciente().getId() == null || getPaciente().getId().longValue() == 0)
            return "";
        if (ContaSingleton.getInstance().getContaFromOrigem(getPaciente()) == null)
            ContaSingleton.getInstance().criaConta(TIPO_CONTA.PACIENTE, UtilsFrontEnd.getProfissionalLogado(), BigDecimal.ZERO, getPaciente(), null, null, null, null);
        BigDecimal saldo = ContaSingleton.getInstance().getContaFromOrigem(getPaciente()).getSaldo();
        String color = (saldo.compareTo(BigDecimal.ZERO) == 0 ? "#17a2b8" : (saldo.compareTo(BigDecimal.ZERO) < 0 ? "#dc3545" : "#28a745"));
        return (color != null && !color.isEmpty() ? "color: " + color : "");
    }

    public List<Lancamento> getLancamentos() {
        if (getEntity() == null || getEntity().getId() == null || getEntity().getId() == 0)
            return null;
        List<Lancamento> lancamentosSearch = new ArrayList<>();
        lancamentosSearch.addAll(LancamentoSingleton.getInstance().getBo().listLancamentosFromFatura(getEntity(), null, ValidacaoLancamento.VALIDADO, true));
        lancamentosSearch.addAll(LancamentoSingleton.getInstance().getBo().listLancamentosFromFatura(getEntity(), null, ValidacaoLancamento.NAO_VALIDADO, true));
        lancamentosSearch.addAll(LancamentoSingleton.getInstance().getBo().listLancamentosFromFatura(getEntity(), null, ValidacaoLancamento.FALHA_OPERACAO, true));
        if (showLancamentosCancelados) {
            lancamentosSearch.addAll(LancamentoSingleton.getInstance().getBo().listLancamentosFromFatura(getEntity(), null, ValidacaoLancamento.VALIDADO, false));
            lancamentosSearch.addAll(LancamentoSingleton.getInstance().getBo().listLancamentosFromFatura(getEntity(), null, ValidacaoLancamento.NAO_VALIDADO, false));
            lancamentosSearch.addAll(LancamentoSingleton.getInstance().getBo().listLancamentosFromFatura(getEntity(), null, ValidacaoLancamento.FALHA_OPERACAO, false));
        }

        if (showLancamentosEstorno) {
            lancamentosSearch.addAll(LancamentoSingleton.getInstance().getBo().listLancamentosFromFatura(getEntity(), null, ValidacaoLancamento.VALIDADO, true, true));
            lancamentosSearch.addAll(LancamentoSingleton.getInstance().getBo().listLancamentosFromFatura(getEntity(), null, ValidacaoLancamento.NAO_VALIDADO, true, true));
            lancamentosSearch.addAll(LancamentoSingleton.getInstance().getBo().listLancamentosFromFatura(getEntity(), null, ValidacaoLancamento.FALHA_OPERACAO, true, true));
        }

        if (lancamentosSearch != null) {
            lancamentosSearch.sort(new Comparator<Lancamento>() {

                @Override
                public int compare(Lancamento o1, Lancamento o2) {
                    return Long.compare(o1.getId(), o2.getId());
                }

            });

            List<Lancamento> lancamentosFatura = new ArrayList<Lancamento>();
            lancamentosFatura.addAll(lancamentosSearch);

            for (Lancamento lan : lancamentosFatura) {
                for (LancamentoContabil lc : lan.getLancamentosContabeis()) {
                    if (lc.getMotivo().getSigla() != null && (lc.getMotivo().getSigla().equals(Motivo.EXTORNO_PACIENTE) || lc.getMotivo().getSigla().equals(Motivo.EXTORNO_PROFISSIONAL)))
                        lancamentosSearch.remove(lan);
                }

            }
        }

        return lancamentosSearch;
    }
    
    public List<FaturaItem> getItens(){
        if (getEntity() == null || getEntity().getId() == null || getEntity().getId() == 0)
            return null;
        
        if(showItensCancelados)
            return getEntity().getItens();
        else
            return getEntity().getItensFiltered();
    }

    public List<Paciente> sugestoesPacientes(String query) {
        try {
            return PacienteSingleton.getInstance().listSugestoesComplete(query, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            e.printStackTrace();
        }
        return null;
    }

    public void exportarTabela(String type) {
        exportarTabela("Faturas", tabelaFatura, type);
    }

    public Date getInicio() {
        return inicio;
    }

    public void setInicio(Date inicio) {
        this.inicio = inicio;
    }

    public Date getFim() {
        return fim;
    }

    public void setFim(Date fim) {
        this.fim = fim;
    }

    public Paciente getPaciente() {
        return paciente;
    }

    public BigDecimal getTotalNaoPlanejado() {
        BigDecimal valor = BigDecimal.ZERO;
        try {
            List<Fatura> faturas = FaturaSingleton.getInstance().getBo().findFaturaIrregularByPaciente(TipoFatura.RECEBIMENTO_PACIENTE, this.paciente);
            if (faturas != null) {
                for (Fatura fatura : faturas) {
                    valor = valor.add(FaturaSingleton.getInstance().getTotalNaoPlanejado(fatura));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            addError("Erro", Mensagens.getMensagemOffLine(Mensagens.ERRO_AO_BUSCAR_REGISTROS));
        }
        return valor;
    }

    public void setPaciente(Paciente paciente) {
        this.paciente = paciente;
        try {
            this.lAPagar = LancamentoSingleton.getInstance().getBo().listContasAPagar(ContaSingleton.getInstance().getContaFromOrigem(this.paciente), null,
                    UtilsPadraoRelatorio.getDataFim(PeriodoBusca.MES_ATUAL), null, null, StatusLancamento.A_PAGAR, null);
            BigDecimal vAPagar = BigDecimal.valueOf(LancamentoSingleton.getInstance().sumLancamentos(this.lAPagar));
            this.lAReceber = LancamentoSingleton.getInstance().getBo().listContasAReceber(ContaSingleton.getInstance().getContaFromOrigem(this.paciente), null,
                    UtilsPadraoRelatorio.getDataFim(PeriodoBusca.MES_ATUAL), null, null, StatusLancamento.A_RECEBER, null);
            BigDecimal vAReceber = BigDecimal.valueOf(LancamentoSingleton.getInstance().sumLancamentos(this.lAReceber));
            if (this.paciente != null) {
                Conta conta = ContaSingleton.getInstance().getContaFromOrigem(this.paciente);
                
                if(conta != null)
                    conta.setSaldo(vAReceber.subtract(vAPagar));
            }
        } catch (Exception e) {
            e.printStackTrace();
            addError("Erro", Mensagens.getMensagemOffLine(Mensagens.ERRO_AO_BUSCAR_REGISTROS));
        }
    }

    public String getNaoPlanejadoCor() {
        BigDecimal saldo = getTotalNaoPlanejado();
        if (saldo != null && saldo.compareTo(BigDecimal.ZERO) != 0) {
            if (saldo.compareTo(BigDecimal.ZERO) > 0)
                return "#dc3545";
        }
        return "black";
    }

    public String getSaldoCor() {
        try {
            if (ContaSingleton.getInstance().getContaFromOrigem(this.paciente) != null) {
                BigDecimal saldo = ContaSingleton.getInstance().getContaFromOrigem(this.paciente).getSaldo();
                if (saldo != null && saldo.compareTo(BigDecimal.ZERO) != 0) {
                    if (saldo.compareTo(BigDecimal.ZERO) < 0)
                        return "#ffc107";
                    else if (saldo.compareTo(BigDecimal.ZERO) > 0)
                        return "#007bff";
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            addError("Erro", Mensagens.getMensagemOffLine(Mensagens.ERRO_AO_BUSCAR_REGISTROS));
        }

        return "black";
    }

    public boolean verificaFaturaInterrompida() {
        if (this.getEntity() != null && this.getEntity().getId() > 0) {
            BigDecimal valorLancamentos = new BigDecimal(0);
            BigDecimal valorItens = new BigDecimal(0);

            List<Lancamento> lancamentos = LancamentoSingleton.getInstance().getBo().listLancamentosFromFatura(this.getEntity(), null, ValidacaoLancamento.VALIDADO, true);
            lancamentos.addAll(LancamentoSingleton.getInstance().getBo().listLancamentosFromFatura(this.getEntity(), null, ValidacaoLancamento.NAO_VALIDADO, true));

            for (Lancamento lancamento : lancamentos) {
                if (lancamento.isAtivo()) {
                    valorLancamentos = valorLancamentos.add(lancamento.getValor());
                }
            }

            for (FaturaItem item : this.getEntity().getItens()) {
                if (item.isAtivo()) {
                    PlanoTratamentoProcedimento ptp = item.getOrigemOrcamento().getOrcamentoItem().getOrigemProcedimento().getPlanoTratamentoProcedimento();
                    if (ptp.isFinalizado()) {
                        valorItens = valorItens.add(item.getValorComDesconto());
                    }
                }
            }

            if (valorLancamentos.compareTo(valorItens) >= 0) {
                return true;
            }
        }
        return false;
    }

    public List<Dominio> getFormasPagamento() {
        return formasPagamento;
    }

    public void setFormasPagamento(List<Dominio> formasPagamento) {
        this.formasPagamento = formasPagamento;
    }

    public boolean isShowProduto() {
        return showProduto;
    }

    public void setShowProduto(boolean showProduto) {
        this.showProduto = showProduto;
    }

    public Tarifa getTarifa() {
        return tarifa;
    }

    public void setTarifa(Tarifa tarifa) {
        this.tarifa = tarifa;
    }

    public List<Tarifa> getTarifas() {
        return tarifas;
    }

    public void setTarifas(List<Tarifa> tarifas) {
        this.tarifas = tarifas;
    }

    public List<Integer> getParcelas() {
        return parcelas;
    }

    public void setParcelas(List<Integer> parcelas) {
        this.parcelas = parcelas;
    }

    public Integer getParcela() {
        return parcela;
    }

    public void setParcela(Integer parcela) {
        this.parcela = parcela;
    }

    public String getFormaPagamento() {
        return formaPagamento;
    }

    public void setFormaPagamento(String formaPagamento) {
        this.formaPagamento = formaPagamento;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public Date getDataPagamento() {
        return dataPagamento;
    }

    public void setDataPagamento(Date dataPagamento) {
        this.dataPagamento = dataPagamento;
    }

    public Date getDataCredito() {
        return dataCredito;
    }

    public void setDataCredito(Date dataCredito) {
        this.dataCredito = dataCredito;
    }

    public PlanoTratamento[] getPtSelecionados() {
        return ptSelecionados;
    }

    public void setPtSelecionados(PlanoTratamento[] ptSelecionados) {
        this.ptSelecionados = ptSelecionados;
    }

    public List<PlanoTratamento> getListaPt() {
        return listaPt;
    }

    public void setListaPt(List<PlanoTratamento> listaPt) {
        this.listaPt = listaPt;
    }

    public StatusFatura getStatus() {
        return status;
    }

    public void setStatus(StatusFatura status) {
        this.status = status;
    }

    public List<StatusFatura> getListaStatus() {
        return listaStatus;
    }

    public void setListaStatus(List<StatusFatura> listaStatus) {
        this.listaStatus = listaStatus;
    }

    public boolean isShowLancamentosCancelados() {
        return showLancamentosCancelados;
    }

    public void setShowLancamentosCancelados(boolean showLancamentosCancelados) {
        this.showLancamentosCancelados = showLancamentosCancelados;
    }

    public FaturaItem getItemSelecionado() {
        return itemSelecionado;
    }

    public void setItemSelecionado(FaturaItem itemSelecionado) {
        this.itemSelecionado = itemSelecionado;
    }

    public Profissional getProfissionalTroca() {
        return profissionalTroca;
    }

    public void setProfissionalTroca(Profissional profissionalTroca) {
        this.profissionalTroca = profissionalTroca;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }

    public List<Profissional> getProfissionais() {
        return profissionais;
    }

    public void setProfissionais(List<Profissional> profissionais) {
        this.profissionais = profissionais;
    }

    public DataTable getTabelaFatura() {
        return tabelaFatura;
    }

    public void setTabelaFatura(DataTable tabelaFatura) {
        this.tabelaFatura = tabelaFatura;
    }

    public List<Lancamento> getlAPagar() {
        return lAPagar;
    }

    public void setlAPagar(List<Lancamento> lAPagar) {
        this.lAPagar = lAPagar;
    }

    public List<Lancamento> getlAReceber() {
        return lAReceber;
    }

    public void setlAReceber(List<Lancamento> lAReceber) {
        this.lAReceber = lAReceber;
    }

    public SubStatusFatura[] getSubStatusFatura() {
        return subStatusFatura;
    }

    public void setSubStatusFatura(SubStatusFatura[] subStatusFatura) {
        this.subStatusFatura = subStatusFatura;
    }

    public List<SubStatusFatura> getListaSubStatusFatura() {
        List<SubStatusFatura> listAll = Arrays.asList(SubStatusFatura.values());
        List<SubStatusFatura> list = new ArrayList<Fatura.SubStatusFatura>();
        for(SubStatusFatura s : listAll) {
            if(!s.getDescricao().equals("Estornado") && !s.getDescricao().equals("Cancelado")) {
                list.add(s);
            }
        }
        return list;
    }

    public String getFiltroPeriodo() {
        return filtroPeriodo;
    }

    public void setFiltroPeriodo(String filtroPeriodo) {
        this.filtroPeriodo = filtroPeriodo;
    }

    public BigDecimal getSomaTotal() {
        return somaTotal;
    }

    public void setSomaTotal(BigDecimal somaTotal) {
        this.somaTotal = somaTotal;
    }

    public BigDecimal getSomaTotalPago() {
        return somaTotalPago;
    }

    public void setSomaTotalPago(BigDecimal somaTotalPago) {
        this.somaTotalPago = somaTotalPago;
    }

    public BigDecimal getSomaTotalNaoPago() {
        return somaTotalNaoPago;
    }

    public void setSomaTotalNaoPago(BigDecimal somaTotalNaoPago) {
        this.somaTotalNaoPago = somaTotalNaoPago;
    }

    public BigDecimal getSomaTotalNaoPlanejado() {
        return somaTotalNaoPlanejado;
    }

    public void setSomaTotalNaoPlanejado(BigDecimal somaTotalNaoPlanejado) {
        this.somaTotalNaoPlanejado = somaTotalNaoPlanejado;
    }

    public String getFaturaViewImportName() {
        return faturaViewImportName;
    }

    public void setFaturaViewImportName(String faturaViewImportName) {
        this.faturaViewImportName = faturaViewImportName;
    }

    //-------------------------------- NEGOCIACAO --------------------------------

    public List<Integer> getParcelasDisponiveis() {
        return parcelasDisponiveis;
    }

    public void setParcelasDisponiveis(List<Integer> parcelasDisponiveis) {
        this.parcelasDisponiveis = parcelasDisponiveis;
    }

    public Integer getNegociacaoQuantidadeParcelas() {
        return negociacaoQuantidadeParcelas;
    }

    public void setNegociacaoQuantidadeParcelas(Integer negociacaoQuantidadeParcelas) {
        this.negociacaoQuantidadeParcelas = negociacaoQuantidadeParcelas;
    }

    public String getNegociacaoTipoDesconto() {
        return negociacaoTipoDesconto;
    }

    public void setNegociacaoTipoDesconto(String negociacaoTipoDesconto) {
        this.negociacaoTipoDesconto = negociacaoTipoDesconto;
    }

    public BigDecimal getNegociacaoValorDesconto() {
        return negociacaoValorDesconto;
    }

    public void setNegociacaoValorDesconto(BigDecimal negociacaoValorDesconto) {
        this.negociacaoValorDesconto = negociacaoValorDesconto;
    }

    public BigDecimal getNegociacaoValorDaPrimeiraParcela() {
        return negociacaoValorDaPrimeiraParcela;
    }

    public void setNegociacaoValorDaPrimeiraParcela(BigDecimal negociacaoValorDaPrimeiraParcela) {
        this.negociacaoValorDaPrimeiraParcela = negociacaoValorDaPrimeiraParcela;
    }

    public BigDecimal getNegociacaoValorDaParcela() {
        return negociacaoValorDaParcela;
    }

    public void setNegociacaoValorDaParcela(BigDecimal negociacaoValorDaParcela) {
        this.negociacaoValorDaParcela = negociacaoValorDaParcela;
    }

    public BigDecimal getNegociacaoValorTotal() {
        return negociacaoValorTotal;
    }

    public void setNegociacaoValorTotal(BigDecimal negociacaoValorTotal) {
        this.negociacaoValorTotal = negociacaoValorTotal;
    }

    public String getNegociacaoObservacao() {
        return negociacaoObservacao;
    }

    public void setNegociacaoObservacao(String negociacaoObservacao) {
        this.negociacaoObservacao = negociacaoObservacao;
    }

    public List<Tarifa> getNegociacaoTarifasDisponiveis() {
        return negociacaoTarifasDisponiveis;
    }

    public void setNegociacaoTarifasDisponiveis(List<Tarifa> negociacaoTarifasDisponiveis) {
        this.negociacaoTarifasDisponiveis = negociacaoTarifasDisponiveis;
    }

    public Tarifa getNegociacaoFormaPagamento() {
        return negociacaoFormaPagamento;
    }

    public void setNegociacaoFormaPagamento(Tarifa negociacaoFormaPagamento) {
        this.negociacaoFormaPagamento = negociacaoFormaPagamento;
    }

    public Date getNegociacaoDataPagamento() {
        return negociacaoDataPagamento;
    }

    public void setNegociacaoDataPagamento(Date negociacaoDataPagamento) {
        this.negociacaoDataPagamento = negociacaoDataPagamento;
    }

    public Date getNegociacaoDataCredito() {
        return negociacaoDataCredito;
    }

    public void setNegociacaoDataCredito(Date negociacaoDataCredito) {
        this.negociacaoDataCredito = negociacaoDataCredito;
    }

    public NegociacaoFatura getNegociacaoConfirmacao() {
        return negociacaoConfirmacao;
    }

    public void setNegociacaoConfirmacao(NegociacaoFatura negociacaoConfirmacao) {
        this.negociacaoConfirmacao = negociacaoConfirmacao;
    }

    public String getTooltipNegociacaoTarifasDisponiveis() {
        return tooltipNegociacaoTarifasDisponiveis;
    }

    public void setTooltipNegociacaoTarifasDisponiveis(String tooltipNegociacaoTarifasDisponiveis) {
        this.tooltipNegociacaoTarifasDisponiveis = tooltipNegociacaoTarifasDisponiveis;
    }

    public List<LancamentoParcelaInfo> getNegociacaoParcelas() {
        return negociacaoParcelas;
    }

    public void setNegociacaoParcelas(List<LancamentoParcelaInfo> negociacaoParcelas) {
        this.negociacaoParcelas = negociacaoParcelas;
    }

    public String getNegociacaoMensagemCalculo() {
        return negociacaoMensagemCalculo;
    }

    public void setNegociacaoMensagemCalculo(String negociacaoMensagemCalculo) {
        this.negociacaoMensagemCalculo = negociacaoMensagemCalculo;
    }

    public String getNegociacaoMensagemErroCalculo() {
        return negociacaoMensagemErroCalculo;
    }

    public void setNegociacaoMensagemErroCalculo(String negociacaoMensagemErroCalculo) {
        this.negociacaoMensagemErroCalculo = negociacaoMensagemErroCalculo;
    }

    public List<Tarifa> getNegociacaoTarifasDisponiveisEEntrada() {
        return negociacaoTarifasDisponiveisEEntrada;
    }

    public void setNegociacaoTarifasDisponiveisEEntrada(List<Tarifa> negociacaoTarifasDisponiveisEEntrada) {
        this.negociacaoTarifasDisponiveisEEntrada = negociacaoTarifasDisponiveisEEntrada;
    }

    public BigDecimal getNegociacaoValorTotal1Parcela() {
        return negociacaoValorTotal1Parcela;
    }

    public void setNegociacaoValorTotal1Parcela(BigDecimal negociacaoValorTotal1Parcela) {
        this.negociacaoValorTotal1Parcela = negociacaoValorTotal1Parcela;
    }

    public BigDecimal getNegociacaoValorTotalDemaisParcelas() {
        return negociacaoValorTotalDemaisParcelas;
    }

    public void setNegociacaoValorTotalDemaisParcelas(BigDecimal negociacaoValorTotalDemaisParcelas) {
        this.negociacaoValorTotalDemaisParcelas = negociacaoValorTotalDemaisParcelas;
    }

    public Tarifa getNegociacaoFormaPagamento1Parcela() {
        return negociacaoFormaPagamento1Parcela;
    }

    public void setNegociacaoFormaPagamento1Parcela(Tarifa negociacaoFormaPagamento1Parcela) {
        this.negociacaoFormaPagamento1Parcela = negociacaoFormaPagamento1Parcela;
    }

    public Tarifa getNegociacaoFormaPagamentoDemaisParcelas() {
        return negociacaoFormaPagamentoDemaisParcelas;
    }

    public void setNegociacaoFormaPagamentoDemaisParcelas(Tarifa negociacaoFormaPagamentoDemaisParcelas) {
        this.negociacaoFormaPagamentoDemaisParcelas = negociacaoFormaPagamentoDemaisParcelas;
    }

    public Date getNegociacaoDataPagamento1Parcela() {
        return negociacaoDataPagamento1Parcela;
    }

    public void setNegociacaoDataPagamento1Parcela(Date negociacaoDataPagamento1Parcela) {
        this.negociacaoDataPagamento1Parcela = negociacaoDataPagamento1Parcela;
    }

    public Date getNegociacaoDataPagamentoDemaisParcelas() {
        return negociacaoDataPagamentoDemaisParcelas;
    }

    public void setNegociacaoDataPagamentoDemaisParcelas(Date negociacaoDataPagamentoDemaisParcelas) {
        this.negociacaoDataPagamentoDemaisParcelas = negociacaoDataPagamentoDemaisParcelas;
    }

    public Date getNegociacaoDataCredito1Parcela() {
        return negociacaoDataCredito1Parcela;
    }

    public void setNegociacaoDataCredito1Parcela(Date negociacaoDataCredito1Parcela) {
        this.negociacaoDataCredito1Parcela = negociacaoDataCredito1Parcela;
    }

    public Date getNegociacaoDataCreditoDemaisParcelas() {
        return negociacaoDataCreditoDemaisParcelas;
    }

    public void setNegociacaoDataCreditoDemaisParcelas(Date negociacaoDataCreditoDemaisParcelas) {
        this.negociacaoDataCreditoDemaisParcelas = negociacaoDataCreditoDemaisParcelas;
    }

    //-------------------------------- NEGOCIACAO --------------------------------

    //-------------------------------- NOVO - NOVO LANÇAMENTO --------------------------------
    public Integer getNovoLancamentoQuantidadeParcelas() {
        return novoLancamentoQuantidadeParcelas;
    }

    public void setNovoLancamentoQuantidadeParcelas(Integer novoLancamentoQuantidadeParcelas) {
        this.novoLancamentoQuantidadeParcelas = novoLancamentoQuantidadeParcelas;
    }

    public Tarifa getNovoLancamentoFormaPagamento() {
        return novoLancamentoFormaPagamento;
    }

    public void setNovoLancamentoFormaPagamento(Tarifa novoLancamentoFormaPagamento) {
        this.novoLancamentoFormaPagamento = novoLancamentoFormaPagamento;
    }

    public BigDecimal getNovoLancamentoValorTotal() {
        return novoLancamentoValorTotal;
    }

    public void setNovoLancamentoValorTotal(BigDecimal novoLancamentoValorTotal) {
        this.novoLancamentoValorTotal = novoLancamentoValorTotal;
    }

    public BigDecimal getNovoLancamentoValorDaPrimeiraParcela() {
        return novoLancamentoValorDaPrimeiraParcela;
    }

    public void setNovoLancamentoValorDaPrimeiraParcela(BigDecimal novoLancamentoValorDaPrimeiraParcela) {
        this.novoLancamentoValorDaPrimeiraParcela = novoLancamentoValorDaPrimeiraParcela;
    }

    public Date getNovoLancamentoDataPagamento() {
        return novoLancamentoDataPagamento;
    }

    public void setNovoLancamentoDataPagamento(Date novoLancamentoDataPagamento) {
        this.novoLancamentoDataPagamento = novoLancamentoDataPagamento;
    }

    public Date getNovoLancamentoDataCredito() {
        return novoLancamentoDataCredito;
    }

    public void setNovoLancamentoDataCredito(Date novoLancamentoDataCredito) {
        this.novoLancamentoDataCredito = novoLancamentoDataCredito;
    }

    public BigDecimal getNovoLancamentoValorDaParcela() {
        return novoLancamentoValorDaParcela;
    }

    public void setNovoLancamentoValorDaParcela(BigDecimal novoLancamentoValorDaParcela) {
        this.novoLancamentoValorDaParcela = novoLancamentoValorDaParcela;
    }

    public List<Integer> getNovoLancamentoParcelasDisponiveis() {
        return novoLancamentoParcelasDisponiveis;
    }

    public void setNovoLancamentoParcelasDisponiveis(List<Integer> novoLancamentoParcelasDisponiveis) {
        this.novoLancamentoParcelasDisponiveis = novoLancamentoParcelasDisponiveis;
    }

    public List<Tarifa> getNovoLancamentoTarifasDisponiveis() {
        return novoLancamentoTarifasDisponiveis;
    }

    public void setNovoLancamentoTarifasDisponiveis(List<Tarifa> novoLancamentoTarifasDisponiveis) {
        this.novoLancamentoTarifasDisponiveis = novoLancamentoTarifasDisponiveis;
    }

    public String getNovoLancamentoTooltipNegociacaoTarifasDisponiveis() {
        return novoLancamentoTooltipNegociacaoTarifasDisponiveis;
    }

    public void setNovoLancamentoTooltipNegociacaoTarifasDisponiveis(String novoLancamentoTooltipNegociacaoTarifasDisponiveis) {
        this.novoLancamentoTooltipNegociacaoTarifasDisponiveis = novoLancamentoTooltipNegociacaoTarifasDisponiveis;
    }

    public List<LancamentoParcelaInfo> getNovoLancamentoParcelas() {
        return novoLancamentoParcelas;
    }

    public void setNovoLancamentoParcelas(List<LancamentoParcelaInfo> novoLancamentoParcelas) {
        this.novoLancamentoParcelas = novoLancamentoParcelas;
    }

    public String getNovoLancamentoMensagemCalculo() {
        return novoLancamentoMensagemCalculo;
    }

    public void setNovoLancamentoMensagemCalculo(String novoLancamentoMensagemCalculo) {
        this.novoLancamentoMensagemCalculo = novoLancamentoMensagemCalculo;
    }

    public String getNovoLancamentoMensagemErroCalculo() {
        return novoLancamentoMensagemErroCalculo;
    }

    public void setNovoLancamentoMensagemErroCalculo(String novoLancamentoMensagemErroCalculo) {
        this.novoLancamentoMensagemErroCalculo = novoLancamentoMensagemErroCalculo;
    }

    public List<Tarifa> getNovoLancamentoTarifasDisponiveisEEntrada() {
        return novoLancamentoTarifasDisponiveisEEntrada;
    }

    public void setNovoLancamentoTarifasDisponiveisEEntrada(List<Tarifa> novoLancamentoTarifasDisponiveisEEntrada) {
        this.novoLancamentoTarifasDisponiveisEEntrada = novoLancamentoTarifasDisponiveisEEntrada;
    }

    //-------------------------------- NOVO - NOVO LANÇAMENTO --------------------------------

    //-------------------------------- EDITAR LANÇAMENTO --------------------------------

    public Lancamento getEditarLancamento() {
        return editarLancamento;
    }

    public void setEditarLancamento(Lancamento editarLancamento) {
        this.editarLancamento = editarLancamento;
    }

    public List<Tarifa> getEditarLancamentoFormasDisponiveis() {
        return editarLancamentoFormasDisponiveis;
    }

    public void setEditarLancamentoFormasDisponiveis(List<Tarifa> editarLancamentoFormasDisponiveis) {
        this.editarLancamentoFormasDisponiveis = editarLancamentoFormasDisponiveis;
    }

    public Lancamento getAjustarLancamento() {
        return ajustarLancamento;
    }

    public void setAjustarLancamento(Lancamento ajustarLancamento) {
        this.ajustarLancamento = ajustarLancamento;
    }

    public Lancamento getExtornarLancamento() {
        return extornarLancamento;
    }

    public void setExtornarLancamento(Lancamento extornarLancamento) {
        this.extornarLancamento = extornarLancamento;
    }

    public boolean isShowLancamentosEstorno() {
        return showLancamentosEstorno;
    }

    public void setShowLancamentosEstorno(boolean showLancamentosEstorno) {
        this.showLancamentosEstorno = showLancamentosEstorno;
    }

    public DadosBasico getOrigem() {
        return origem;
    }

    public void setOrigem(DadosBasico origem) {
        this.origem = origem;
    }

    public TipoCategoria getTipoCategoria() {
        return tipoCategoria;
    }

    public void setTipoCategoria(TipoCategoria tipoCategoria) {
        this.tipoCategoria = tipoCategoria;
    }

    public List<CategoriaMotivo> getCategorias() {
        return categorias;
    }

    public void setCategorias(List<CategoriaMotivo> categorias) {
        this.categorias = categorias;
    }

    public CategoriaMotivo getCategoria() {
        return categoria;
    }

    public void setCategoria(CategoriaMotivo categoria) {
        this.categoria = categoria;
    }

    public List<Motivo> getMotivos() {
        return motivos;
    }

    public void setMotivos(List<Motivo> motivos) {
        this.motivos = motivos;
    }

    public List<TipoCategoria> getTiposCategoria() {
        return tiposCategoria;
    }

    public void setTiposCategoria(List<TipoCategoria> tiposCategoria) {
        this.tiposCategoria = tiposCategoria;
    }

    public FaturaItem getFaturaItemEditar() {
        return faturaItemEditar;
    }

    public void setFaturaItemEditar(FaturaItem faturaItemEditar) {
        this.faturaItemEditar = faturaItemEditar;
    }

    public LancamentoContabil getLancamentoContabilEditarItem() {
        return lancamentoContabilEditarItem;
    }

    public void setLancamentoContabilEditarItem(LancamentoContabil lancamentoContabilEditarItem) {
        this.lancamentoContabilEditarItem = lancamentoContabilEditarItem;
    }

    public BigDecimal getValorAlteracaoItem() {
        return valorAlteracaoItem;
    }

    public void setValorAlteracaoItem(BigDecimal valorAlteracaoItem) {
        this.valorAlteracaoItem = valorAlteracaoItem;
    }

    public FaturaItem getItemAlteracao() {
        return itemAlteracao;
    }

    public void setItemAlteracao(FaturaItem itemAlteracao) {
        this.itemAlteracao = itemAlteracao;
    }

    //-------------------------------- EDITAR LANÇAMENTO --------------------------------

    //--------------------------------- IMPRIMIR RECIBO ---------------------------------

    public Lancamento getLancamentoImpressao() {
        return lancamentoImpressao;
    }

    public void setLancamentoImpressao(Lancamento lancamentoImpressao) {
        this.lancamentoImpressao = lancamentoImpressao;
    }

    public List<Lancamento> getLancamentosImpressao() {
        return lancamentosImpressao;
    }

    public void setLancamentosImpressao(List<Lancamento> lancamentosImpressao) {
        this.lancamentosImpressao = lancamentosImpressao;
    }

    public boolean isIncluirLogo() {
        return incluirLogo;
    }

    public void setIncluirLogo(boolean incluirLogo) {
        this.incluirLogo = incluirLogo;
    }

    public TipoRecibo getTipoReciboEscolhido() {
        return tipoReciboEscolhido;
    }

    public void setTipoReciboEscolhido(TipoRecibo tipoReciboEscolhido) {
        this.tipoReciboEscolhido = tipoReciboEscolhido;
    }

    public List<TipoRecibo> getTiposRecibo() {
        return Arrays.asList(TipoRecibo.values());
    }

    public StreamedContent getReciboView() {
        return reciboView;
    }

    public void setReciboView(StreamedContent reciboView) {
        this.reciboView = reciboView;
    }

    public BigDecimal getValorTotalRecibo() {
        return valorTotalRecibo;
    }

    public void setValorTotalRecibo(BigDecimal valorTotalRecibo) {
        this.valorTotalRecibo = valorTotalRecibo;
    }

    public String getNowDate() {
        return new SimpleDateFormat("dd/MM/yyyy").format(new Date());
    }

    public String getHtmlReciboContent() {
        return htmlReciboContent;
    }

    public void setHtmlReciboContent(String htmlReciboContent) {
        this.htmlReciboContent = htmlReciboContent;
    }

    public boolean isShowItensCancelados() {
        return showItensCancelados;
    }

    public void setShowItensCancelados(boolean showItensCancelados) {
        this.showItensCancelados = showItensCancelados;
    }

    
    public List<Fatura> getListaFiltrada() {
        return listaFiltrada;
    }

    
    public void setListaFiltrada(List<Fatura> listaFiltrada) {
        this.listaFiltrada = listaFiltrada;
    }

    //--------------------------------- IMPRIMIR RECIBO ---------------------------------

}
