package br.com.lume.odonto.managed;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.primefaces.PrimeFaces;
import org.primefaces.component.datatable.DataTable;

import br.com.lume.common.OdontoPerfil;
import br.com.lume.common.log.LogIntelidenteSingleton;
import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.JSFHelper;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.Utils.Mes;
import br.com.lume.common.util.Utils.ValidacaoLancamento;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.conta.ContaSingleton;
import br.com.lume.conta.ContaSingleton.TIPO_CONTA;
import br.com.lume.convenioProcedimento.ConvenioProcedimentoSingleton;
import br.com.lume.dominio.DominioSingleton;
import br.com.lume.faturamento.FaturaSingleton;
import br.com.lume.lancamento.LancamentoSingleton;
import br.com.lume.lancamentoContabil.LancamentoContabilSingleton;
import br.com.lume.odonto.entity.Dominio;
import br.com.lume.odonto.entity.Fatura;
import br.com.lume.odonto.entity.Fatura.TipoFatura;
import br.com.lume.odonto.entity.FaturaItem;
import br.com.lume.odonto.entity.Lancamento;
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
import br.com.lume.planoTratamentoProcedimentoCusto.PlanoTratamentoProcedimentoCustoSingleton;
import br.com.lume.profissional.ProfissionalSingleton;
import br.com.lume.repasse.RepasseFaturasItemSingleton;
import br.com.lume.repasse.RepasseFaturasLancamentoSingleton;
import br.com.lume.tarifa.TarifaSingleton;

@ManagedBean
@ViewScoped
public class FaturaPagtoMB extends LumeManagedBean<Fatura> {

    private static final long serialVersionUID = 1L;
    //private Logger log = Logger.getLogger(FaturaPagtoMB.class);

    private Date inicio, fim;
    private Paciente paciente;
    private List<Dominio> formasPagamento;
    private PlanoTratamento[] ptSelecionados = new PlanoTratamento[] {};
    private List<PlanoTratamento> listaPt;
    private String status;
    private List<String> listaStatus;
    private boolean showLancamentosCancelados = false;

    private FaturaItem itemSelecionado;
    private List<Profissional> profissionais;
    private Profissional profissionalTroca;
    private String observacao;

    //Campos para 'Novo Lançamento'
    private boolean showProduto;
    private String formaPagamento;
    private BigDecimal valor;
    private Date dataPagamento, dataCredito;
    private Tarifa tarifa;
    private List<Tarifa> tarifas = new ArrayList<>();
    private List<Integer> parcelas;
    private Integer parcela;

    //Campos para lançamentos a pagar e a receber da aba financeiro do paciente
    private List<Lancamento> lAPagar, lAReceber;

    //EXPORTAÇÃO TABELA
    private DataTable tabelaFatura;

    public FaturaPagtoMB() {
        super(FaturaSingleton.getInstance().getBo());
        this.setClazz(Fatura.class);
        try {
            //Calendar daysAgo = Calendar.getInstance();
            //daysAgo.add(Calendar.DAY_OF_MONTH, -7);
            //setInicio(daysAgo.getTime());
            //Calendar now = Calendar.getInstance();
            //setFim(now.getTime());

            setFormasPagamento(DominioSingleton.getInstance().getBo().listByEmpresaAndObjetoAndTipo("pagamento", "forma"));
            setListaStatus(new ArrayList<>());
            getListaStatus().add(Fatura.StatusFatura.PAGO.getRotulo());
            getListaStatus().add(Fatura.StatusFatura.PENDENTE.getRotulo());
            getListaStatus().add(Fatura.StatusFatura.TODOS.getRotulo());
            setStatus(Fatura.StatusFatura.PENDENTE.getRotulo());
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

    public boolean hasRequisitosCumprir(Lancamento lancamentoRepasse) {
        List<Requisito> todosRequisitos = getRequisitosValidaLancamentoFromRepasseFatura(lancamentoRepasse);
        return todosRequisitos != null && todosRequisitos.size() > 0;
    }

    public boolean isTodosRequisitosFeitos(Lancamento lancamentoRepasse) {
        List<Requisito> todosRequisitos = getRequisitosValidaLancamentoFromRepasseFatura(lancamentoRepasse);
        if (todosRequisitos != null && todosRequisitos.size() > 0) {
            List<Requisito> requisitosNaoFeitos = todosRequisitos.stream().filter(requisito -> !requisito.isRequisitoFeito()).collect(Collectors.toList());
            return !(requisitosNaoFeitos != null && requisitosNaoFeitos.size() > 0);
        } else
            return true;
    }

    public List<Requisito> getRequisitosValidaLancamentoFromRepasseFatura(Lancamento lancamentoRepasse) {
        return RepasseFaturasLancamentoSingleton.getInstance().getRequisitosValidaLancamentoFromRepasseFatura(UtilsFrontEnd.getEmpresaLogada(), lancamentoRepasse);
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
            LancamentoSingleton.getInstance().inativaLancamento(l, UtilsFrontEnd.getProfissionalLogado());
            this.addInfo("Sucesso", "Lançamento cancelado com sucesso!", true);
            updateValues(getEntity());
        } catch (Exception e) {
            LogIntelidenteSingleton.getInstance().makeLog(e);
            this.addError("Erro", "Falha ao cancelar o lançamento!", true);
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
        return "N/A";
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

            setEntityList(FaturaSingleton.getInstance().getBo().findFaturasOrigemFilter(UtilsFrontEnd.getEmpresaLogada(), getPaciente(), (inicio == null ? null : inicio.getTime()),
                    (fim == null ? null : fim.getTime()), Fatura.StatusFatura.getTipoFromRotulo(getStatus())));
            getEntityList().forEach(fatura -> {
                updateValues(fatura);
            });
        } catch (Exception e) {
            LogIntelidenteSingleton.getInstance().makeLog(e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
        }
    }

    private void updateValues(Fatura fatura) {
        fatura.setDadosTabelaRepasseTotalFatura(FaturaSingleton.getInstance().getTotal(fatura));
        fatura.setDadosTabelaRepasseTotalPago(FaturaSingleton.getInstance().getTotalPago(fatura));
        fatura.setDadosTabelaRepasseTotalNaoPago(FaturaSingleton.getInstance().getTotalNaoPago(fatura));
        fatura.setDadosTabelaRepasseTotalNaoPlanejado(FaturaSingleton.getInstance().getTotalNaoPlanejado(fatura));
        fatura.setDadosTabelaRepasseTotalRestante(FaturaSingleton.getInstance().getTotalRestante(fatura));
        fatura.setDadosTabelaPT(PlanoTratamentoSingleton.getInstance().getPlanoTratamentoFromFaturaOrigem(fatura));

        fatura.setDadosTabelaStatusFatura("A Receber");
        if (fatura.getDadosTabelaRepasseTotalFatura().subtract(fatura.getDadosTabelaRepasseTotalPago()).doubleValue() <= 0)
            fatura.setDadosTabelaStatusFatura("Recebido");
    }

    public void changePaciente() {
        setListaPt(PlanoTratamentoSingleton.getInstance().getBo().listAtivosByPaciente(getPaciente()));
    }

    public void atualizaProduto() {
        if ("CC".equals(getFormaPagamento()) || "CD".equals(getFormaPagamento()) || "BO".equals(getFormaPagamento())) {
            setTarifas(TarifaSingleton.getInstance().getBo().listByForma(getFormaPagamento(), UtilsFrontEnd.getProfissionalLogado().getIdEmpresa()));
            setShowProduto(Boolean.TRUE);
        } else {
            setShowProduto(Boolean.FALSE);
            setParcela(1);
        }

        handleSelectPagamento();
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
            if ("CC".equals(getFormaPagamento())) {
                BigDecimal valorOriginalDividio = getValor().divide(new BigDecimal(getParcela()), 2, RoundingMode.HALF_UP);
                BigDecimal diferenca = valorOriginalDividio.multiply(new BigDecimal(getParcela())).subtract(getValor());
                for (int i = 1; i <= getParcela(); i++) {
                    if (i == getParcela()) {
                        valorOriginalDividio = valorOriginalDividio.subtract(diferenca);
                    }
                    LancamentoSingleton.getInstance().novoLancamento(getEntity(), valorOriginalDividio, getFormaPagamento(), getParcela(), now.getTime(), data.getTime(), getTarifa(),
                            UtilsFrontEnd.getProfissionalLogado());
                    //persistLancamento(getParcela(), getEntity(), valorOriginalDividio, getFormaPagamento(), Calendar.getInstance().getTime(), data.getTime(), getTarifa());
                    data.add(Calendar.MONTH, 1);
                }
            } else {
                LancamentoSingleton.getInstance().novoLancamento(getEntity(), getValor(), getFormaPagamento(), getParcela(), now.getTime(), data.getTime(), getTarifa(),
                        UtilsFrontEnd.getProfissionalLogado());
                //persistLancamento(getParcela(), getEntity(), getValor(), getFormaPagamento(), Calendar.getInstance().getTime(), data.getTime(), getTarifa());
            }
            this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");
            PrimeFaces.current().executeScript("PF('dlgNewLancamento').hide()");
            updateValues(getEntity());
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
            if ("CC".equals(getFormaPagamento())) {
                BigDecimal valorOriginalDividio = getValor().divide(new BigDecimal(getParcela()), 2, RoundingMode.HALF_UP);
                BigDecimal diferenca = valorOriginalDividio.multiply(new BigDecimal(getParcela())).subtract(getValor());
                for (int i = 1; i <= getParcela(); i++) {
                    if (i == getParcela()) {
                        valorOriginalDividio = valorOriginalDividio.subtract(diferenca);
                    }
                    LancamentoSingleton.getInstance().novoLancamentoGenerico(getEntity(), valorOriginalDividio, getFormaPagamento(), getParcela(), null, now.getTime(), data.getTime(), getTarifa(),
                            UtilsFrontEnd.getProfissionalLogado());

                    data.add(Calendar.MONTH, 1);
                }
            } else {
                LancamentoSingleton.getInstance().novoLancamentoGenerico(getEntity(), getValor(), getFormaPagamento(), getParcela(), null, now.getTime(), data.getTime(), getTarifa(),
                        UtilsFrontEnd.getProfissionalLogado());

            }

        } catch (Exception e) {
            LogIntelidenteSingleton.getInstance().makeLog(e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
        }

        this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");
        PrimeFaces.current().executeScript("PF('dlgNewLancamento').hide()");

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
        lancamentosSearch.addAll(LancamentoSingleton.getInstance().getBo().listLancamentosFromFatura(getEntity(), true));
        lancamentosSearch.addAll(LancamentoSingleton.getInstance().getBo().listLancamentosFromFatura(getEntity(), false));
        if (showLancamentosCancelados) {
            lancamentosSearch.addAll(LancamentoSingleton.getInstance().getBo().listLancamentosFromFatura(getEntity(), true, false));
            lancamentosSearch.addAll(LancamentoSingleton.getInstance().getBo().listLancamentosFromFatura(getEntity(), false, false));
        }
        
        List<Lancamento> lancamentos = new ArrayList<Lancamento>(); 
        lancamentosSearch.forEach(lancamento -> {
            try {
                // Setando Objetos Gerais
                RepasseFaturasLancamento repasse = RepasseFaturasLancamentoSingleton.getInstance().getBo().getFaturaRepasseLancamentoFromLancamentoRepasseDestino(lancamento);   
                
                lancamento.setRfl(repasse);
                lancamento.setPtp(PlanoTratamentoProcedimentoSingleton.getInstance().getProcedimentoFromFaturaItem(repasse.getFaturaItem()));

                // Calculos necessários para mostragem na tabela
                lancamento.setDadosTabelaValorTotalFatura(FaturaSingleton.getInstance().getTotal(repasse.getFaturaItem().getFatura()));
                lancamento.setDadosTabelaPercItem(
                        repasse.getFaturaItem().getValorComDesconto().divide(lancamento.getDadosTabelaValorTotalFatura(), 4, BigDecimal.ROUND_HALF_UP).multiply(BigDecimal.valueOf(100)));
                lancamento.setDadosTabelaValorProcComPercItem(String.format("R$ %.2f", repasse.getFaturaItem().getValorComDesconto().doubleValue()) + " (" + String.format("%.2f%%",
                        lancamento.getDadosTabelaPercItem().doubleValue()) + ")");
                lancamento.setDadosTabelaValorTotalCustosDiretos(
                        PlanoTratamentoProcedimentoCustoSingleton.getInstance().getCustoDiretoFromPTP(lancamento.getPtp()).setScale(2, BigDecimal.ROUND_HALF_UP));
                lancamento.setDadosTabelaValorRecebidoComFormaPagto(
                        String.format("R$ %.2f", repasse.getLancamentoRepasse().getValor()) + " " + repasse.getLancamentoRepasse().getFormaPagamentoStr());
                lancamento.setDadosTabelaValorTaxasETarifas(
                        LancamentoContabilSingleton.getInstance().getTaxasAndTarifasForLancamentoRecebimento(repasse.getLancamentoRepasse()).setScale(2, BigDecimal.ROUND_HALF_UP));
                if (Profissional.PORCENTAGEM.equals(repasse.getRepasseFaturas().getTipoCalculo())) {
                    lancamento.setDadosTabelaValorRepasse(String.format("%.2f%%", repasse.getRepasseFaturas().getValorCalculo()));
                    lancamento.setDadosTabelaMetodoRepasse("POR");
                } else if (Profissional.PROCEDIMENTO.equals(repasse.getRepasseFaturas().getTipoCalculo())) {
                    BigDecimal valorRepasse = ConvenioProcedimentoSingleton.getInstance().getCheckValorConvenio(lancamento.getPtp());
                    lancamento.setDadosTabelaValorRepasse(String.format("R$ %.2f", valorRepasse.doubleValue()));
                    lancamento.setDadosTabelaMetodoRepasse("PRO");
                }
                lancamento.setDadosTabelaMetodoRepasseComValor(lancamento.getDadosTabelaMetodoRepasse() + " - " + lancamento.getDadosTabelaValorRepasse());

                // Calculos necessários para mostragem da explicação do cálculo
                lancamento.setDadosCalculoPercLancamentoFatura(repasse.getLancamentoRepasse().getValor().divide(lancamento.getDadosTabelaValorTotalFatura(), 4, BigDecimal.ROUND_HALF_UP));
                lancamento.setDadosCalculoPercTaxa(
                        (repasse.getLancamentoRepasse().getTarifa() != null ? repasse.getLancamentoRepasse().getTarifa().getTaxa().divide(BigDecimal.valueOf(100)) : BigDecimal.ZERO));
                lancamento.setDadosCalculoValorTaxa(lancamento.getDadosCalculoPercTaxa().multiply(repasse.getLancamentoRepasse().getValor()));
                lancamento.setDadosCalculoValorTarifa((repasse.getLancamentoRepasse().getTarifa() != null ? repasse.getLancamentoRepasse().getTarifa().getTarifa() : BigDecimal.ZERO));
                lancamento.setDadosCalculoPercTributo(repasse.getLancamentoRepasse().getTributoPerc());
                lancamento.setDadosCalculoValorTributo(repasse.getLancamentoRepasse().getTributoPerc().multiply(repasse.getLancamentoRepasse().getValor()).setScale(2, BigDecimal.ROUND_HALF_UP));
                lancamento.setDadosCalculoPercCustoDireto(lancamento.getDadosTabelaValorTotalCustosDiretos().divide(lancamento.getDadosTabelaValorTotalFatura(), 4, BigDecimal.ROUND_HALF_UP));
                lancamento.setDadosCalculoValorCustoDiretoRateado(
                        lancamento.getDadosTabelaValorTotalCustosDiretos().divide(lancamento.getDadosTabelaValorTotalFatura(), 4, BigDecimal.ROUND_HALF_UP).multiply(
                                repasse.getLancamentoRepasse().getValor()));
                lancamento.setDadosCalculoTotalReducao(lancamento.getDadosCalculoValorTaxa().add(lancamento.getDadosCalculoValorTarifa()).add(lancamento.getDadosCalculoValorTributo()));
                lancamento.setDadosCalculoRecebidoMenosReducao(
                        repasse.getLancamentoRepasse().getValor().subtract(lancamento.getDadosCalculoTotalReducao()).setScale(2, BigDecimal.ROUND_HALF_UP));

                lancamento.setDadosCalculoValorItemSemCusto(
                        repasse.getFaturaItem().getValorComDesconto().subtract(lancamento.getDadosTabelaValorTotalCustosDiretos()).setScale(2, BigDecimal.ROUND_HALF_UP));
                lancamento.setDadosCalculoPercItemSemCusto(lancamento.getDadosCalculoValorItemSemCusto().divide(lancamento.getDadosTabelaValorTotalFatura(), 4, BigDecimal.ROUND_HALF_UP));
                lancamento.setDadosCalculoInvPercItemSemCusto(BigDecimal.valueOf(1).subtract(lancamento.getDadosCalculoPercItemSemCusto()));
                lancamento.setDadosCalculoReducaoCustoDireto(
                        lancamento.getDadosCalculoRecebidoMenosReducao().multiply(lancamento.getDadosCalculoInvPercItemSemCusto()).setScale(2, BigDecimal.ROUND_HALF_UP));

                lancamento.setDadosCalculoValorARepassarSemCusto(
                        lancamento.getDadosCalculoRecebidoMenosReducao().subtract(lancamento.getDadosCalculoReducaoCustoDireto()).setScale(2, BigDecimal.ROUND_HALF_UP));
            } catch (Exception e) {
                lancamento.setPtp(null);
            }
            lancamentos.add(lancamento);
        });
        
        return lancamentos;
    }

    public List<Paciente> sugestoesPacientes(String query) {
        return PacienteSingleton.getInstance().getBo().listSugestoesComplete(query, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
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
            this.lAPagar = LancamentoSingleton.getInstance().getBo().listContasAPagar(ContaSingleton.getInstance().getContaFromOrigem(this.paciente), Mes.getMesAtual(),
                    ValidacaoLancamento.NAO_VALIDADO);
            BigDecimal vAPagar = BigDecimal.valueOf(LancamentoSingleton.getInstance().sumLancamentos(this.lAPagar));
            this.lAReceber = LancamentoSingleton.getInstance().getBo().listContasAReceber(ContaSingleton.getInstance().getContaFromOrigem(this.paciente), Mes.getMesAtual(),
                    ValidacaoLancamento.NAO_VALIDADO);
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<String> getListaStatus() {
        return listaStatus;
    }

    public void setListaStatus(List<String> listaStatus) {
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

}
