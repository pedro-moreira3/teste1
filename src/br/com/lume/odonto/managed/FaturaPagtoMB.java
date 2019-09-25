package br.com.lume.odonto.managed;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.primefaces.PrimeFaces;

import br.com.lume.common.exception.business.BusinessException;
import br.com.lume.common.exception.techinical.TechnicalException;
import br.com.lume.common.log.LogIntelidenteSingleton;
import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.Status;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.conta.ContaSingleton;
import br.com.lume.dadosBasico.DadosBasicoSingleton;
import br.com.lume.dominio.DominioSingleton;
import br.com.lume.faturamento.FaturaSingleton;
import br.com.lume.lancamento.LancamentoSingleton;
import br.com.lume.lancamentoContabil.LancamentoContabilSingleton;
import br.com.lume.motivo.MotivoSingleton;
import br.com.lume.odonto.entity.DadosBasico;
import br.com.lume.odonto.entity.Dominio;
import br.com.lume.odonto.entity.Fatura;
import br.com.lume.odonto.entity.FaturaItem;
import br.com.lume.odonto.entity.Lancamento;
import br.com.lume.odonto.entity.LancamentoContabil;
import br.com.lume.odonto.entity.Motivo;
import br.com.lume.odonto.entity.Paciente;
import br.com.lume.odonto.entity.Tarifa;
import br.com.lume.paciente.PacienteSingleton;
import br.com.lume.tarifa.TarifaSingleton;

@ManagedBean
@ViewScoped
public class FaturaPagtoMB extends LumeManagedBean<Fatura> {

    private static final long serialVersionUID = 1L;
    //private Logger log = Logger.getLogger(FaturaPagtoMB.class);

    private Date inicio, fim;
    private Paciente paciente;
    private List<Dominio> formasPagamento;
    //private PlanoTratamento[] ptSelecionados;
    //private List<PlanoTratamento> listaPt;
    private String status;
    private List<String> listaStatus;
    private boolean showLancamentosCancelados = false;

    //Campos para 'Novo Lançamento'
    private boolean showProduto;
    private String formaPagamento;
    private BigDecimal valor;
    private Date dataPagamento, dataCredito;
    private Tarifa tarifa;
    private List<Tarifa> tarifas = new ArrayList<>();
    private List<Integer> parcelas;
    private Integer parcela;

    public FaturaPagtoMB() {
        super(FaturaSingleton.getInstance().getBo());
        this.setClazz(Fatura.class);
        try {
            Calendar daysAgo = Calendar.getInstance();
            daysAgo.add(Calendar.DAY_OF_MONTH, -7);
            setInicio(daysAgo.getTime());
            Calendar now = Calendar.getInstance();
            setFim(now.getTime());
            setFormasPagamento(DominioSingleton.getInstance().getBo().listByEmpresaAndObjetoAndTipo("pagamento", "forma"));
            setListaStatus(new ArrayList<>());
            getListaStatus().add(Lancamento.PAGO);
            getListaStatus().add(Lancamento.PENDENTE);
        } catch (Exception e) {
            LogIntelidenteSingleton.getInstance().makeLog(e);
        }
    }

    public void cancelaLancamento(Lancamento l) {
        try {
            LancamentoSingleton.getInstance().inativaLancamento(l, UtilsFrontEnd.getProfissionalLogado());
            this.addInfo("Sucesso", "Lançamento cancelado com sucesso!", true);
        } catch (Exception e) {
            LogIntelidenteSingleton.getInstance().makeLog(e);
            this.addError("Erro na busca de dados!", "Falha ao buscar os lançamentos.", true);
        }
    }

    public void visualizaFatura(Fatura fatura) {
        setEntity(fatura);
        setShowLancamentosCancelados(false);
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
            }
            Calendar fim = null;
            if (getFim() != null) {
                fim = Calendar.getInstance();
                fim.setTime(getFim());
                fim.set(Calendar.HOUR_OF_DAY, 23);
                fim.set(Calendar.MINUTE, 59);
                fim.set(Calendar.SECOND, 59);
            }

            //setEntityList(FaturaSingleton.getInstance().getBo().findFaturasFilter(getPaciente(), getListaPt(), getInicio(), getFim()));
            setEntityList(FaturaSingleton.getInstance().getBo().findFaturasFilter(getPaciente(), null, (inicio == null ? null : inicio.getTime()), (fim == null ? null : fim.getTime())));
            getEntityList().removeIf(fatura -> {
                if (Lancamento.PAGO.equals(getStatus()) && this.getTotalRestante(fatura).compareTo(BigDecimal.ZERO) > 0)
                    return true;
                else if (Lancamento.PENDENTE.equals(getStatus()) && this.getTotalRestante(fatura).compareTo(BigDecimal.ZERO) == 0)
                    return true;
                else
                    return false;
            });
        } catch (Exception e) {
            LogIntelidenteSingleton.getInstance().makeLog(e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
        }
    }

    //public void changePaciente() {
    //    setListaPt(PlanoTratamentoSingleton.getInstance().getBo().listAtivosByPaciente(getPaciente()));
    //}

    public void atualizaProduto() {
        if ("CC".equals(getFormaPagamento()) || "CD".equals(getFormaPagamento()) || "BO".equals(getFormaPagamento())) {
            setTarifas(TarifaSingleton.getInstance().getBo().listByForma(getFormaPagamento(), UtilsFrontEnd.getProfissionalLogado().getIdEmpresa()));
            setShowProduto(Boolean.TRUE);
        } else {
            if (getDataPagamento() == null) {
                getDataPagamento();
            }
            setShowProduto(Boolean.FALSE);
            setParcela(1);
        }
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
        setValor(getTotalRestante(getEntity()));
        setDataPagamento(new Date());
    }

    public void persistLancamento(Integer numParcela, Fatura fatura, BigDecimal valor, String formaPagamento, Date dataPagamento, Date dataCredito, Tarifa tarifa) throws Exception {
        Lancamento l = new Lancamento();
        l.setNumeroParcela(numParcela);
        l.setFatura(fatura);
        l.setValor(valor);
        l.setValorOriginal(valor);
        l.setFormaPagamento(formaPagamento);
        l.setRecibo("");
        l.setDataPagamento(dataPagamento);
        l.setDataCredito(dataCredito);
        l.setTributo(DominioSingleton.getInstance().getBo().getTributo(UtilsFrontEnd.getEmpresaLogada().getEmpFltImposto()));
        l.setDataCriacao(Calendar.getInstance().getTime());
        if (tarifa != null && tarifa.getId() != 0) {
            l.setTarifa(tarifa);
        }
        l.setValidado("N");
        LancamentoSingleton.getInstance().getBo().persist(l);

        setValor(BigDecimal.ZERO);
        if (l.getFormaPagamento().equals("CC") || l.getFormaPagamento().equals("CD") || l.getFormaPagamento().equals("BO") || l.getFormaPagamento().equals("CH")) {
            l.setValidado(Status.NAO);
            Motivo motivo = MotivoSingleton.getInstance().getBo().findBySigla(Motivo.PAGAMENTO_CARTAO);
            DadosBasico dadosBasico = DadosBasicoSingleton.getInstance().getBo().findByNome(l.getTarifa().getProduto());
            if (l.getTarifa() != null && l.getTarifa().getTaxa() != null && l.getTarifa().getTaxa().doubleValue() > 0) {
                setValor(l.getValor().multiply(l.getTarifa().getTaxa()).divide(new BigDecimal(100), MathContext.DECIMAL32));
                persistLancamentoContabil(l, getValor().negate(), dadosBasico, motivo);
            }
            if (l.getTarifa() != null && l.getTarifa().getTarifa() != null && l.getTarifa().getTarifa().doubleValue() > 0) {
                persistLancamentoContabil(l, l.getTarifa().getTarifa().negate(), dadosBasico, motivo);
            }
        }

        Motivo motivo = MotivoSingleton.getInstance().getBo().findBySigla(Motivo.PAGAMENTO_PACIENTE);
        persistLancamentoContabil(l, l.getValor(), getEntity().getPaciente().getDadosBasico(), motivo);
    }

    private void persistLancamentoContabil(Lancamento l, BigDecimal valorLC, DadosBasico dadosBasico, Motivo motivo) throws Exception, BusinessException, TechnicalException {
        LancamentoContabil lc = new LancamentoContabil();
        lc.setIdEmpresa(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
        lc.setTipo(motivo.getTipo());
        lc.setDadosBasico(dadosBasico);
        lc.setMotivo(motivo);
        lc.setValor(valorLC);
        lc.setData(l.getDataCredito());
        lc.setLancamento(l);
        lc.setDataCriacao(Calendar.getInstance().getTime());
        LancamentoContabilSingleton.getInstance().getBo().persist(lc);
    }

    public void actionPersistLancamento() {
        try {
            if (getValor().compareTo(getTotalRestante(getEntity())) > 0) {
                this.addError("Informe um valor menor que o total restante!", "");
                return;
            }

            Calendar data = Calendar.getInstance();
            data.setTime(getDataCredito());
            if ("CC".equals(getFormaPagamento())) {
                BigDecimal valorOriginalDividio = getValor().divide(new BigDecimal(getParcela()), 2, RoundingMode.HALF_UP);
                BigDecimal diferenca = valorOriginalDividio.multiply(new BigDecimal(getParcela())).subtract(getValor());
                for (int i = 1; i <= getParcela(); i++) {
                    if (i == getParcela()) {
                        valorOriginalDividio = valorOriginalDividio.subtract(diferenca);
                    }
                    persistLancamento(getParcela(), getEntity(), valorOriginalDividio, getFormaPagamento(), Calendar.getInstance().getTime(), data.getTime(), getTarifa());
                    data.add(Calendar.MONTH, 1);
                }
            } else {
                persistLancamento(getParcela(), getEntity(), getValor(), getFormaPagamento(), Calendar.getInstance().getTime(), data.getTime(), getTarifa());

            }
            this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");
            PrimeFaces.current().executeScript("PF('dlgNewLancamento').hide()");
        } catch (Exception e) {
            LogIntelidenteSingleton.getInstance().makeLog(e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
        }
    }

    public String getStyleBySaldoPaciente() {
        if (getPaciente() == null || getPaciente().getId() == null || getPaciente().getId().longValue() == 0)
            return "";
        BigDecimal saldo = getPaciente().getConta().getSaldo();
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
        return lancamentosSearch;
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
                total = total.add(item.getValorComDesconto());
            else if ("S".equals(item.getTipoSaldo()))
                total = total.subtract(item.getValorComDesconto());
        }
        return total;
    }

    public List<Paciente> sugestoesPacientes(String query) {
        return PacienteSingleton.getInstance().getBo().listSugestoesComplete(query, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
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

    public void setPaciente(Paciente paciente) {
        this.paciente = paciente;
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

    //public PlanoTratamento[] getPtSelecionados() {
    //    return ptSelecionados;
    //}

    //public void setPtSelecionados(PlanoTratamento[] ptSelecionados) {
    //    this.ptSelecionados = ptSelecionados;
    //}

    //public List<PlanoTratamento> getListaPt() {
    //    return listaPt;
    //}

    //public void setListaPt(List<PlanoTratamento> listaPt) {
    //    this.listaPt = listaPt;
    //}

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

}
