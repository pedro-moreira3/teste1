package br.com.lume.odonto.managed;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
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

import br.com.lume.common.OdontoPerfil;
import br.com.lume.common.log.LogIntelidenteSingleton;
import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.JSFHelper;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.Status;
import br.com.lume.common.util.TooltipHelper;
import br.com.lume.common.util.Utils.ValidacaoLancamento;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.common.util.UtilsPadraoRelatorio;
import br.com.lume.common.util.UtilsPadraoRelatorio.PeriodoBusca;
import br.com.lume.conta.ContaSingleton;
import br.com.lume.conta.ContaSingleton.TIPO_CONTA;
import br.com.lume.descontoOrcamento.DescontoOrcamentoSingleton;
import br.com.lume.dominio.DominioSingleton;
import br.com.lume.faturamento.FaturaSingleton;
import br.com.lume.lancamento.LancamentoSingleton;
import br.com.lume.lancamento.objects.LancamentoParcelaInfo;
import br.com.lume.lancamentoContabil.LancamentoContabilSingleton;
import br.com.lume.negociacao.NegociacaoFaturaSingleton;
import br.com.lume.odonto.entity.DescontoOrcamento;
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
import br.com.lume.paciente.PacienteSingleton;
import br.com.lume.planoTratamento.PlanoTratamentoSingleton;
import br.com.lume.planoTratamentoProcedimento.PlanoTratamentoProcedimentoSingleton;
import br.com.lume.profissional.ProfissionalSingleton;
import br.com.lume.repasse.RepasseFaturasItemSingleton;
import br.com.lume.repasse.RepasseFaturasLancamentoSingleton;
import br.com.lume.tarifa.TarifaSingleton;

/**
 * @author ricardo.poncio
 */
/**
 * @author ricardo.poncio
 */
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

    //EXPORTAÇÃO TABELA
    private DataTable tabelaFatura;

    public FaturaPagtoMB() {
        super(FaturaSingleton.getInstance().getBo());
        this.setClazz(Fatura.class);

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
                setStatus(Fatura.StatusFatura.A_RECEBER);
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
        profissionais = ProfissionalSingleton.getInstance().getBo().listByEmpresa(perfis, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
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

    public void cancelaLancamento(Lancamento l) {
        try {
            if(l.getConferidoPorProfissional() == null) {
                LancamentoSingleton.getInstance().inativaLancamento(l, UtilsFrontEnd.getProfissionalLogado());
                this.addInfo("Sucesso", "Lançamento cancelado com sucesso!", true);
            }else {
                if(isAdmin()) {
                    cancelaLancamentoValidado(l);
                }else {
                    this.addError("Permissão negada !",Mensagens.getMensagem(Mensagens.PERMISSAO_NEGADA));
                }
            }
            
            updateValues(getEntity(), true, false);
        } catch (Exception e) {
            LogIntelidenteSingleton.getInstance().makeLog(e);
            this.addError("Erro", "Falha ao cancelar o lançamento!\\r\\n" + e.getMessage(), true);
        }
    }
    
    private void cancelaLancamentoValidado(Lancamento l) {
        try {
            Profissional profissionalLogado = UtilsFrontEnd.getProfissionalLogado();
            Tarifa tarifa = TarifaSingleton.getInstance().getBo().findByProdutoAndEmpresa("Dinheiro", profissionalLogado.getIdEmpresa());
            
            Lancamento lancamento = LancamentoSingleton.getInstance().novoLancamento(l.getFatura(), l.getValor(), tarifa.getTipo(),
                    l.getNumeroParcela(), l.getParcelaTotal(), new Date(), new Date(),
                    tarifa, UtilsFrontEnd.getProfissionalLogado(), null, true, null);
            
            l.setLancamentoExtornado(Status.SIM);
            LancamentoSingleton.getInstance().getBo().persist(l);
            
            LancamentoSingleton.getInstance().cancelaRepasseFromExtornoRecebimento(l, profissionalLogado);
            
            LancamentoContabilSingleton.getInstance().validaEConfereLancamentos(lancamento, profissionalLogado);
            lancamento.calculaStatusESubStatus();
            
            this.addInfo("Sucesso", "Lançamento cancelado com sucesso!", true);
        } catch (Exception e) {
            LogIntelidenteSingleton.getInstance().makeLog(e);
            this.addError("Erro", "Falha ao cancelar o lançamento!\\r\\n" + e.getMessage(), true);
        }
    }
    
    public void carregarAjuste(Lancamento lancamento) {
        if(isAdmin() && lancamento.getValidadoPorProfissional() != null) {
            this.ajustarLancamento = new Lancamento();
            this.extornarLancamento = lancamento;
            
            this.ajustarLancamento.setFormaPagamento(lancamento.getFormaPagamento());
            this.ajustarLancamento.setValor(lancamento.getValor());
            this.ajustarLancamento.setTarifa(lancamento.getTarifa());
            this.ajustarLancamento.setValorDesconto(lancamento.getValorDesconto());
            this.ajustarLancamento.setDataPagamento(lancamento.getDataPagamento());
            this.ajustarLancamento.setDataCredito(lancamento.getDataCredito());
            
            editarLancamentoAtualizarFormasPagamentoDisponiveis();
        }else {
            this.addError("Permissão negada",Mensagens.getMensagem(Mensagens.PERMISSAO_NEGADA));
        }
    }
    
    public void ajustarLancamento() {
        try {
            //EXTORNA O VALOR JÁ CONFERIDO E VALIDADO
            this.cancelaLancamentoValidado(extornarLancamento);
            
            //CRIA UM NOVO LANÇAMENTO
            Lancamento l = LancamentoSingleton.getInstance().novoLancamento(extornarLancamento.getFatura(), ajustarLancamento.getValor(), ajustarLancamento.getFormaPagamento(),
                    extornarLancamento.getNumeroParcela(), extornarLancamento.getParcelaTotal(), ajustarLancamento.getDataPagamento(), ajustarLancamento.getDataCredito(),
                    extornarLancamento.getTarifa(), UtilsFrontEnd.getProfissionalLogado(), null, false, null);
            
            this.addInfo("Sucesso", "Lançamento ajustado com sucesso!", true);
            
        }catch (Exception e) {
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
        setEntity(fatura);
        setShowLancamentosCancelados(false);
        updateValues(fatura, true, false);

        updateWichScreenOpenForFaturaView();
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
        if (fatura.getTipoFatura() == Fatura.TipoFatura.PAGAMENTO_PROFISSIONAL)
            fatura.setDadosTabelaRepassePlanoTratamento(PlanoTratamentoSingleton.getInstance().getPlanoTratamentoFromFaturaRepasse(fatura));
        if (fatura.getTipoFatura() == Fatura.TipoFatura.RECEBIMENTO_PACIENTE && updateAllValues) {
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
        }

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

    public void changePaciente() {
        setListaPt(PlanoTratamentoSingleton.getInstance().getBo().listAtivosByPaciente(getPaciente()));
    }

    public void atualizaProduto() {
        if (existeCadastroTarifa()) {
            setTarifas(TarifaSingleton.getInstance().getBo().listByForma(getFormaPagamento(), UtilsFrontEnd.getProfissionalLogado().getIdEmpresa()));
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

    public void actionPersistLancamento() {
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
                        LancamentoSingleton.getInstance().novoLancamentoGenerico(getEntity(), valorOriginalDividio, getFormaPagamento(), i, getParcela(), null, now.getTime(), data.getTime(),
                                getTarifa(), UtilsFrontEnd.getProfissionalLogado());
                    } else {
                        LancamentoSingleton.getInstance().novoLancamentoGenerico(getEntity(), valorOriginalDividio, getFormaPagamento(), i, getParcela(), null, now.getTime(), data.getTime(),
                                getTarifa(), UtilsFrontEnd.getProfissionalLogado());
                        now.add(Calendar.MONTH, 1);
                    }

                    data.add(Calendar.MONTH, 1);
                }
            } else {
                LancamentoSingleton.getInstance().novoLancamentoGenerico(getEntity(), getValor(), getFormaPagamento(), getParcela(), getParcela(), null, now.getTime(), data.getTime(), getTarifa(),
                        UtilsFrontEnd.getProfissionalLogado());

            }

        } catch (Exception e) {
            LogIntelidenteSingleton.getInstance().makeLog(e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
        }

        this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");
        PrimeFaces.current().executeScript("PF('dlgNewLancamento').hide()");

    }

    //-------------------------------- EDITAR LANÇAMENTO --------------------------------

    public void actionStartEditarLancamento(Lancamento l) {
        this.editarLancamento = l;
        if (this.editarLancamentoFormasDisponiveis == null)
            editarLancamentoAtualizarFormasPagamentoDisponiveis();
    }

    public void actionPersistEditarLancamento() {
        try {
            LancamentoSingleton.getInstance().getBo().persist(editarLancamento);

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

    private void editarLancamentoAtualizarFormasPagamentoDisponiveis() {
        try {
            this.editarLancamentoFormasDisponiveis = TarifaSingleton.getInstance().getBo().listByEmpresa(UtilsFrontEnd.getEmpresaLogada().getEmpIntCod());
        } catch (Exception e) {
            LogIntelidenteSingleton.getInstance().makeLog(e);
        }
    }

    //-------------------------------- EDITAR LANÇAMENTO --------------------------------

    //-------------------------------- NOVO - NOVO LANÇAMENTO --------------------------------    

    public void actionPersistNovoNovoLancamento() {
        try {
            if (novoLancamentoQuantidadeParcelas == null || novoLancamentoValorDaPrimeiraParcela == null || novoLancamentoFormaPagamento == null || novoLancamentoDataPagamento == null || novoLancamentoDataCredito == null) {
                this.addError("Erro!", "Preencha todos os campos!");
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
            updateValues(getEntity(), true, true);

            PrimeFaces.current().executeScript("PF('dlgNovoLancamento').hide()");
        } catch (Exception e) {
            LogIntelidenteSingleton.getInstance().makeLog(e);
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

            List<Tarifa> listaFormasDePagamento = TarifaSingleton.getInstance().getBo().listByEmpresa(UtilsFrontEnd.getEmpresaLogada().getEmpIntCod());

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

            List<Tarifa> listaFormasDePagamento = TarifaSingleton.getInstance().getBo().listByEmpresa(UtilsFrontEnd.getEmpresaLogada().getEmpIntCod());
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
            if (novoLancamentoValorDaPrimeiraParcela.compareTo(novoLancamentoValorTotal) > 0) {
                this.addError("Erro", "Insira uma primeira parcela menor que o total!");
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

        if(this.negociacaoQuantidadeParcelas == 1) {
            this.negociacaoValorDaParcela = this.negociacaoValorTotal;    
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
        negociacaoValorTotal = FaturaSingleton.getInstance().getValorTotal(getEntity());
        BigDecimal valorDeDesconto = (negociacaoValorDesconto == null ? BigDecimal.ZERO : negociacaoValorDesconto);
        DescontoOrcamento descontoCadQtdeParcelas = descontosDisponiveis.get(negociacaoQuantidadeParcelas);
        if ((descontoCadQtdeParcelas == null && valorDeDesconto.compareTo(BigDecimal.ZERO) > 0) || (descontoCadQtdeParcelas != null && valorDeDesconto.compareTo(
                descontoCadQtdeParcelas.getDesconto()) > 0)) {
            negociacaoValorDesconto = null;
            this.addError("Erro!", "Desconto dado maior que o máximo permitido.");
            return;
        }

        if ("P".equals(negociacaoTipoDesconto) && negociacaoValorDesconto != null)
            valorDeDesconto = negociacaoValorDesconto.divide(BigDecimal.valueOf(100)).multiply(negociacaoValorTotal);
        BigDecimal totalSemDesconto = negociacaoValorTotal.subtract(valorDeDesconto);

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

        if (tipoNegociacao == TipoNegociacao.PARCELADO_PRIMEIRA_PARCELA_DIFERENTE) {
            negociacaoMensagemCalculo = "Entrada de " + ptFormat.format(negociacaoValorDaPrimeiraParcela) + " mais " + String.valueOf(negociacaoQuantidadeParcelas - 1) + "x de " + ptFormat.format(
                    negociacaoValorDaParcela) + ". Total de " + ptFormat.format(totalSemDesconto);
        } else if (tipoNegociacao == TipoNegociacao.PARCELADO_TODAS_PARCELAS_IGUAIS) {
            negociacaoMensagemCalculo = "Pagamento em " + String.valueOf(negociacaoQuantidadeParcelas) + "x de " + ptFormat.format(negociacaoValorDaParcela) + ". Total de " + ptFormat.format(
                    totalSemDesconto);
        } else if (tipoNegociacao == TipoNegociacao.A_VISTA) {
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

            List<Tarifa> listaFormasDePagamento = TarifaSingleton.getInstance().getBo().listByEmpresa(UtilsFrontEnd.getEmpresaLogada().getEmpIntCod());

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

            List<Tarifa> listaFormasDePagamento = TarifaSingleton.getInstance().getBo().listByEmpresa(UtilsFrontEnd.getEmpresaLogada().getEmpIntCod());
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

        List<Tarifa> listaFormasDePagamento = TarifaSingleton.getInstance().getBo().listByEmpresa(UtilsFrontEnd.getEmpresaLogada().getEmpIntCod());
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
        
        if (lancamentosSearch != null) {
            lancamentosSearch.sort(new Comparator<Lancamento>() {

                @Override
                public int compare(Lancamento o1, Lancamento o2) {
                    return Long.compare(o1.getId(), o2.getId());
                }

            });
            
            List<Lancamento> lancamentosFatura = lancamentosSearch;
            
            for(Lancamento lan : lancamentosFatura) {
                for(LancamentoContabil lc : lan.getLancamentosContabeis()) {
                    if(lc.getMotivo().getSigla().equals(Motivo.EXTORNO_PACIENTE))
                        lancamentosSearch.remove(lan);
                }
            }
        }

        return lancamentosSearch;
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
            if (this.paciente != null)
                ContaSingleton.getInstance().getContaFromOrigem(this.paciente).setSaldo(vAReceber.subtract(vAPagar));
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
        return Arrays.asList(SubStatusFatura.values());
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

    //-------------------------------- EDITAR LANÇAMENTO --------------------------------    

}
