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
import javax.faces.event.ActionEvent;

import org.apache.log4j.Logger;
import org.primefaces.PrimeFaces;
import org.primefaces.event.CloseEvent;
import org.primefaces.event.SelectEvent;

import br.com.lume.common.exception.business.BusinessException;
import br.com.lume.common.exception.techinical.TechnicalException;
import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.JSFHelper;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.Status;
import br.com.lume.configuracao.Configurar;
import br.com.lume.dadosBasico.DadosBasicoSingleton;
import br.com.lume.desconto.DescontoSingleton;
import br.com.lume.dominio.DominioSingleton;
import br.com.lume.lancamento.LancamentoSingleton;
import br.com.lume.lancamentoContabil.LancamentoContabilSingleton;
import br.com.lume.motivo.MotivoSingleton;
import br.com.lume.odonto.datamodel.LancamentoDataModel;
import br.com.lume.odonto.entity.DadosBasico;
import br.com.lume.odonto.entity.Desconto;
import br.com.lume.odonto.entity.Dominio;
import br.com.lume.odonto.entity.Lancamento;
import br.com.lume.odonto.entity.LancamentoContabil;
import br.com.lume.odonto.entity.Motivo;
import br.com.lume.odonto.entity.Orcamento;
import br.com.lume.odonto.entity.Paciente;
import br.com.lume.odonto.entity.PlanoTratamento;
import br.com.lume.odonto.entity.Tarifa;
import br.com.lume.odonto.util.OdontoMensagens;
import br.com.lume.paciente.PacienteSingleton;
import br.com.lume.tarifa.TarifaSingleton;

@ManagedBean
@ViewScoped
public class LancamentoMB extends LumeManagedBean<Lancamento> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(LancamentoMB.class);

    private List<Lancamento> lancamentos = new ArrayList<>();

    private Lancamento[] lancamentosSelecionados;

    private List<Lancamento> lancamentosSplit;

    public List<Paciente> pacientes;

    public Paciente paciente;

    public PlanoTratamento planoTratamento;

    public Orcamento orcamento;

    public boolean pago = false, produto = false, admin, acerto;

    private Tarifa tarifa;

    private List<Tarifa> tarifas = new ArrayList<>();

    private List<Dominio> dominios;

    private List<Desconto> descontos = new ArrayList<>();

    private Desconto desconto;

    private BigDecimal valorDesconto, valor, novoValor, somaValores, valorAgregado, valorAgregadoProporcional, valorAgregadoExcedido, valorPago, valorRestante;

    private String recibo;

    private LancamentoDataModel lancamentoDataModel;

    private Boolean disable = true;

    private Date dataPagamento, inicio, fim;

    private String formaPagamento = "DI";

    private String status;

    public final List<String> statuss;

    private List<PlanoTratamento> listPt, listPtSelecionados;

    private Date dataSalva;

    private Tarifa tarifaCred, tarifaDeb, tarifaBoleto;

    private boolean liberaPagamento = false;

    private List<Integer> parcelas;

    private Integer parcela;

    private BigDecimal valorAPagar;

    public LancamentoMB() {
        super(LancamentoSingleton.getInstance().getBo());       
        statuss = new ArrayList<>();
        statuss.add(Lancamento.AGENDADO);
        statuss.add(Lancamento.ATIVO);
        statuss.add(Lancamento.ATRASADO);
        statuss.add(Lancamento.CANCELADO);
        statuss.add(Lancamento.PAGO);
        statuss.add(Lancamento.PENDENTE);
        dataPagamento = Calendar.getInstance().getTime();
        tarifa = new Tarifa();
        formaPagamento = "DI";
        desconto = new Desconto();
        recibo = "";
        valorDesconto = BigDecimal.ZERO;
        valorAgregado = BigDecimal.ZERO;
        novoValor = BigDecimal.ZERO;
        valorRestante = BigDecimal.ZERO;
        listPt = new ArrayList<>();
        listPtSelecionados = new ArrayList<>();
        this.setLancamentosSplit(new ArrayList<Lancamento>());
        this.setClazz(Lancamento.class);
        this.setValorAgregado(BigDecimal.ZERO);
        this.setValorAgregadoProporcional(BigDecimal.ZERO);
        pago = false;
        try {
            String idpaciente = JSFHelper.getRequest().getParameter("id");
            if (idpaciente != null && !idpaciente.isEmpty()) {
                Paciente pac = PacienteSingleton.getInstance().getBo().find(Long.parseLong(idpaciente));
                if (pac != null) {
                    Configurar.getInstance().getConfiguracao().setPacienteLogado(pac);
                }
            }
            // descontos = descontoBO.listByEmpresa();
            dominios = DominioSingleton.getInstance().getBo().listByEmpresaAndObjetoAndTipo("pagamento", "forma");
            pacientes = PacienteSingleton.getInstance().getBo().listByEmpresa(Configurar.getInstance().getConfiguracao().getProfissionalLogado().getIdEmpresa());
            this.setPaciente(Configurar.getInstance().getConfiguracao().getPacienteSelecionado());
            this.setValorAgregadoExcedido(BigDecimal.ZERO);
            if (paciente == null) {
                this.addError(OdontoMensagens.getMensagem("plano.paciente.vazio"), "");
            }
            lancamentos = new ArrayList<>();
            if (paciente != null && paciente.getId() != null) {
                this.carregaListaLancamento();
            }
            if (this.getPaciente() != null) {
                this.carregaCombos(this.getPaciente());
                this.carregaTela();
            }
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
        }
    }

    public void actionLimpar() {
        listPtSelecionados = new ArrayList<>();
        paciente = null;
        inicio = null;
        fim = null;
        status = null;
        lancamentoDataModel = new LancamentoDataModel(null);
        this.limpaSelection();
    }

    public void carregarValorAPagar() {
        valorAPagar = getEntity().getValor();
    }

    @Override
    public void actionRemove(ActionEvent event) {
        try {
            // for (Lancamento l : lancamentosSelecionados) {
            this.getEntity().setExcluido(Status.SIM);
            this.getEntity().setDataPagamento(null);
            this.getEntity().setFormaPagamento(null);
            this.getEntity().setExcluidoPorProfissional(Configurar.getInstance().getConfiguracao().getProfissionalLogado().getId());
            this.getbO().persist(this.getEntity());
            List<LancamentoContabil> lancamentosContabeis = LancamentoContabilSingleton.getInstance().getBo().listByLancamento(this.getEntity());
            for (LancamentoContabil lc : lancamentosContabeis) {
                lc.setExcluido(Status.SIM);
                lc.setDataExclusao(Calendar.getInstance().getTime());
                lc.setExcluidoPorProfissional(Configurar.getInstance().getConfiguracao().getProfissionalLogado().getId());
                LancamentoContabilSingleton.getInstance().getBo().persist(lc);
            }
            // }
            this.actionNew(event);
            this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_REMOVIDO_COM_SUCESSO), "");
        } catch (Exception e) {
            log.error("Erro no actionRemove", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_REMOVER_REGISTRO), "");
        }
    }

    @Override
    public void setEntity(Lancamento entity) {
        super.setEntity(entity);
        produto = false;
        if (entity != null) {
            formaPagamento = entity.getFormaPagamento();
            if (formaPagamento != null && !formaPagamento.isEmpty()) {
                atualizaProduto();
            }
        }
        if (this.getEntity().getDataPagamento() != null) {
            liberaPagamento = false;
        } else {
            liberaPagamento = true;
        }
    }

    private void abatimentoCartao(Lancamento l) throws Exception {
        valor = new BigDecimal(0);
        if (l.getFormaPagamento().equals("CC") || l.getFormaPagamento().equals("CD") || l.getFormaPagamento().equals("BO") || l.getFormaPagamento().equals("CH")) {
            l.setValidado(Status.NAO);
            if (l.getTarifa() != null && l.getTarifa().getTaxa() != null && l.getTarifa().getTaxa().doubleValue() > 0) {
                valor = l.getValor().multiply(l.getTarifa().getTaxa()).divide(new BigDecimal(100), MathContext.DECIMAL32);
                this.persistLancamentoContabil(l, valor);
            }
            if (l.getTarifa() != null && l.getTarifa().getTarifa() != null && l.getTarifa().getTarifa().doubleValue() > 0) {
                this.persistLancamentoContabil(l, l.getTarifa().getTarifa());
            }
            this.getbO().persist(l);
        }
    }

    private void persistLancamentoContabil(Lancamento l, BigDecimal valorLC) throws Exception, BusinessException, TechnicalException {
        LancamentoContabil lc = new LancamentoContabil();
        Motivo motivo = MotivoSingleton.getInstance().getBo().findBySigla(Motivo.PAGAMENTO_CARTAO);
        DadosBasico dadosBasico = DadosBasicoSingleton.getInstance().getBo().findByNome(l.getTarifa().getProduto());
        lc.setIdEmpresa(Configurar.getInstance().getConfiguracao().getProfissionalLogado().getIdEmpresa());
        lc.setTipo(motivo.getTipo());
        lc.setDadosBasico(dadosBasico);
        lc.setMotivo(motivo);
        lc.setValor(valorLC.negate());
        lc.setData(l.getDataCredito());
        lc.setLancamento(l);
        LancamentoContabilSingleton.getInstance().getBo().persist(lc);
    }

    public void atualizaDataCredito() {
        if (this.getTarifa() != null) {
            parcelas = new ArrayList<>();
            for (int i = getTarifa().getParcelaMinima(); i <= getTarifa().getParcelaMaxima(); i++) {
                parcelas.add(i);
            }
            this.getEntity().setDataCredito(this.adicionaDias(dataPagamento, this.getTarifa().getPrazo()));
        } else {
            this.getEntity().setDataCredito(dataPagamento);
        }
        if (formaPagamento.equals("CC")) {
            tarifaCred = tarifa;
        } else if (formaPagamento.equals("CD")) {
            tarifaDeb = tarifa;
        } else if (formaPagamento.equals("BO")) {
            tarifaBoleto = tarifa;
        }
    }

    private Date adicionaDias(Date data, int dias) {
        Calendar c = Calendar.getInstance();
        c.setTime(data);
        c.add(Calendar.DAY_OF_MONTH, dias);
        return c.getTime();
    }

    public void atualizaProduto() {
        if (formaPagamento.equals("CC") || formaPagamento.equals("CD") || formaPagamento.equals("BO")) {
            tarifas = TarifaSingleton.getInstance().getBo().listByForma(formaPagamento);
            produto = true;
        } else {
            if (this.getEntity().getDataPagamento() == null) {
                this.getEntity().setDataCredito(dataPagamento);
            }
            produto = false;
        }
        try {
            descontos = DescontoSingleton.getInstance().getBo().listByEmpresaAndForma(formaPagamento);
            valorDesconto = null;
            desconto = null;
            this.atualizaDesconto();
            // novoValor = somaValores;
            novoValor = valorRestante;
            if (formaPagamento.equals("CC") && tarifaCred != null) {
                tarifa = tarifaCred;
                this.atualizaDataCredito();
            } else if (formaPagamento.equals("CD") && tarifaDeb != null) {
                tarifa = tarifaDeb;
                this.atualizaDataCredito();
            } else if (formaPagamento.equals("BO") && tarifaBoleto != null) {
                tarifa = tarifaBoleto;
                this.atualizaDataCredito();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void atualizaDesconto() {
        valorRestante = somaValores != null ? somaValores.subtract(valorAgregadoProporcional != null ? valorAgregadoProporcional : BigDecimal.ZERO) : BigDecimal.ZERO;
        if (desconto != null) {
            valorRestante = valorRestante.subtract(valorRestante.multiply(desconto.getValorDesconto()).divide(new BigDecimal(100), MathContext.DECIMAL32));
            valorDesconto = valorRestante;
        } else {
            valorDesconto = null;
        }
        novoValor = valorRestante;
    }

    private void geraLancamentoContabil(Lancamento l) throws Exception {
        LancamentoContabil lc = new LancamentoContabil();
        Motivo motivo = MotivoSingleton.getInstance().getBo().findBySigla(Motivo.PAGAMENTO_PACIENTE);
        lc.setIdEmpresa(Configurar.getInstance().getConfiguracao().getProfissionalLogado().getIdEmpresa());
        lc.setTipo(motivo.getTipo());
        lc.setDadosBasico(paciente.getDadosBasico());
        lc.setMotivo(motivo);
        lc.setValor(l.getValor());
        // if (l.getFormaPagamento().equals("CC") || l.getFormaPagamento().equals("CD") || l.getFormaPagamento().equals("BO"))
        // lc.setData(adicionaDias(l.getDataPagamento(), 40));
        // else
        lc.setData(l.getDataCredito());
        lc.setLancamento(l);
        LancamentoContabilSingleton.getInstance().getBo().persist(lc);
    }

    public void handleSelectPagamento(SelectEvent event) {
        if (this.getTarifa().getProduto() != null) {
            this.getEntity().setDataCredito(this.adicionaDias(dataPagamento, this.getTarifa().getPrazo()));
        } else {
            this.getEntity().setDataCredito(dataPagamento);
        }

    }

    @Override
    public void actionPersist(ActionEvent event) {
        try {
            if (valorAPagar.doubleValue() <= this.getEntity().getValor().doubleValue()) {

                if (valorAPagar.doubleValue() == this.getEntity().getValor().doubleValue()) {
                    this.pagarParcela(valorAPagar);
                } else {
                    BigDecimal novoValor = this.getEntity().getValor().subtract(valorAPagar);
                    criaParcelaComSaldoRestante(novoValor);
                    this.pagarParcela(valorAPagar);
                }
                this.carregaTela();
                PrimeFaces.current().ajax().addCallbackParam("pagar", true);
            } else {
                this.addError("Valor maior que a parcela!", "");
            }
            pago = true;
        } catch (Exception e) {
            log.error("Erro no actionPersist", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
        }
    }

    private void criaParcelaComSaldoRestante(BigDecimal novoValor) throws BusinessException, TechnicalException, Exception {
        Lancamento lancamento = new Lancamento();
        lancamento = new Lancamento();
        lancamento.setNumeroParcela(this.getEntity().getNumeroParcela());
        lancamento.setDataCredito(this.getEntity().getDataCredito());
        lancamento.setValor(novoValor);
        lancamento.setValorOriginal(novoValor);
        lancamento.setPlanoTratamentoProcedimento(this.getEntity().getPlanoTratamentoProcedimento());
        lancamento.setOrcamento(this.getEntity().getOrcamento());
        LancamentoSingleton.getInstance().getBo().persist(lancamento);
    }

    private void pagarParcela(BigDecimal valorAPagar) throws Exception {
        if ("CC".equals(formaPagamento)) {
            BigDecimal valorOriginalDividio = valorAPagar.divide(new BigDecimal(parcela), 2, RoundingMode.HALF_UP);
            BigDecimal diferenca = valorOriginalDividio.multiply(new BigDecimal(parcela)).subtract(valorAPagar);
            Calendar data = Calendar.getInstance();
            data.setTime(getEntity().getDataCredito());
            for (int i = 1; i <= parcela; i++) {
                if (i == parcela) {
                    valorOriginalDividio = valorOriginalDividio.subtract(diferenca);
                }
                Lancamento l = copiaLancamento(valorOriginalDividio);
                l.setFormaPagamento(formaPagamento);
                l.setRecibo(recibo);
                l.setDataPagamento(Calendar.getInstance().getTime());
                l.setDataCredito(data.getTime());
                l.setFormaPagamento(this.getFormaPagamento());
                l.setTributo(DominioSingleton.getInstance().getBo().getTributo());
                persist(l);
                data.add(Calendar.MONTH, 1);
            }
            getEntity().setExcluido("S");
            LancamentoSingleton.getInstance().getBo().persist(getEntity());
        } else {
            pagarParcelaUnitaria(valorAPagar, dataPagamento);
        }
    }

    private void pagarParcelaUnitaria(BigDecimal valor, Date data) throws Exception {
        this.getEntity().setValor(valor);
        this.getEntity().setFormaPagamento(formaPagamento);
        this.getEntity().setRecibo(recibo);
        this.getEntity().setDataPagamento(data);
        this.getEntity().setFormaPagamento(this.getFormaPagamento());
        this.getEntity().setTributo(DominioSingleton.getInstance().getBo().getTributo());
        if (this.getTarifa() != null && this.getTarifa().getId() != 0) {
            this.getEntity().setTarifa(this.getTarifa());
        }
        this.persist(this.getEntity());
    }

    public boolean isRendererProximo() {
        return novoValor != null && novoValor.doubleValue() != 0;
    }

    public Lancamento copiaLancamento(BigDecimal valor) {
        Lancamento l = new Lancamento();
        l.setDataCredito(this.getEntity().getDataCredito());
        l.setFormaPagamento(this.getEntity().getFormaPagamento());
        l.setDataPagamento(this.getEntity().getDataPagamento());
        l.setNumeroParcela(this.getEntity().getNumeroParcela());
        l.setOrcamento(this.getEntity().getOrcamento());
        l.setPlanoTratamentoProcedimento(this.getEntity().getPlanoTratamentoProcedimento());
        l.setRecibo(this.getEntity().getRecibo());
        l.setTributo(this.getEntity().getTributo());
        l.setValor(valor);
        l.setValorOriginal(valor);
        if (this.getTarifa() != null && this.getTarifa().getId() != 0) {
            l.setTarifa(this.getTarifa());
        }
        l.setValidado("N");
        return l;
    }

    public void persist(Lancamento l) throws Exception {
        this.getbO().persist(l);
        this.abatimentoCartao(l);
        this.geraLancamentoContabil(l);
    }

    public int getProximaParcela(Lancamento lancamento) {
        int ultimaParcela = 0;
        for (Lancamento l : lancamento.getOrcamento().getLancamentos()) {
            if (ultimaParcela < l.getNumeroParcela()) {
                ultimaParcela = l.getNumeroParcela();
            }
        }
        return ultimaParcela + 1;
    }

    public void atualizaTela() {
        this.setEntity(lancamentosSelecionados[0]);
        novoValor = new BigDecimal(0);
        for (Lancamento l : lancamentosSelecionados) {
            novoValor = novoValor.add(l.getValor());
        }
        somaValores = novoValor;
        valorRestante = somaValores;
        disable = this.desabilitaBotoes();
    }

    public boolean desabilitaBotoes() {
        if (lancamentosSelecionados == null) {
            return true;
        }
        for (Lancamento l : lancamentosSelecionados) {
            if (l.getDataPagamento() != null || !l.getExcluido().equals("N") || !l.getOrcamento().getExcluido().equals("N")) {
                return true;
            }
        }
        return false;
    }

    public void handleClose(CloseEvent event) {
        tarifaCred = null;
        tarifaDeb = null;
        tarifaBoleto = null;
        liberaPagamento = false;
        dataPagamento = Calendar.getInstance().getTime();
    }

    public void fechaDialog() {
        if (!pago) {
            this.getEntity().setDataCredito(dataSalva);
        } else {
            pago = false;
        }
        this.limpaSelection();
    }

    public void openDialog() {
        dataSalva = this.getEntity().getDataCredito();
        this.getEntity().setDataCredito(Calendar.getInstance().getTime());
    }

    private void limpaSelection() {
        this.setValorAgregado(BigDecimal.ZERO);
        this.setValorDesconto(BigDecimal.ZERO);
        this.setLancamentosSplit(new ArrayList<Lancamento>());
        this.setLancamentosSelecionados(null);
        this.setEntity(new Lancamento());
        tarifa = new Tarifa();
        formaPagamento = "DI";
        desconto = new Desconto();
        recibo = "";
        valorDesconto = BigDecimal.ZERO;
        valorAgregado = BigDecimal.ZERO;
        novoValor = BigDecimal.ZERO;
        valorRestante = BigDecimal.ZERO;
    }

    public List<Paciente> geraSugestoes(String query) {
        return PacienteSingleton.getInstance().getBo().listSugestoesComplete(query,Configurar.getInstance().getConfiguracao().getProfissionalLogado().getIdEmpresa());
    }

    public void handleSelect(SelectEvent event) {
        Object object = event.getObject();
        this.carregaCombos(object);
        this.carregaTela();
    }

    private void carregaCombos(Object object) {
        paciente = (Paciente) object;
        if (paciente == null) {
            this.addError(OdontoMensagens.getMensagem("plano.paciente.vazio"), "");
        }
        Configurar.getInstance().getConfiguracao().setPacienteSelecionado(paciente);
        this.carregaListaLancamento();
        this.carregarFiltros();
    }

    public void carregarFiltros() {
        boolean encontrou = false;
        listPt = new ArrayList<>();
        for (Lancamento l : lancamentos) {
            encontrou = false;
            for (PlanoTratamento lpt : listPt) {
                if (l.getOrcamento().getPlanoTratamento() == lpt) {
                    encontrou = true;
                }
            }
            if (!encontrou) {
                listPt.add(l.getOrcamento().getPlanoTratamento());
            }
        }
    }

    public void carregaTela() {
        try {
            this.limpaSelection();
            lancamentos = LancamentoSingleton.getInstance().getBo().listByFiltros(paciente, listPtSelecionados, inicio, fim, status);
            lancamentoDataModel = new LancamentoDataModel(lancamentos);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void filtra() {
        try {
            lancamentosSelecionados = new Lancamento[10];
            lancamentos = LancamentoSingleton.getInstance().getBo().listAllByPeriodoAndStatus(inicio, fim, status);
            // lancamentoDataModel = new LancamentoDataModel(lancamentos);
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
        }
    }

    public void carregaListaLancamento() {
        this.setEntity(null);
        try {
            lancamentosSelecionados = new Lancamento[10];
            lancamentos = LancamentoSingleton.getInstance().getBo().listByPacienteAndDependentes(paciente);
            lancamentoDataModel = new LancamentoDataModel(lancamentos);
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
        }
    }

    public boolean getHasRecibo() {
        if (this.getEntity().getDataPagamento() != null && (this.getEntity().getRecibo() == null || (this.getEntity().getRecibo().equals("")))) {
            return false;
        } else {
            return true;
        }
    }

    public List<Lancamento> getLancamentos() {
        return lancamentos;
    }

    public void setLancamentos(List<Lancamento> lancamentos) {
        this.lancamentos = lancamentos;
    }

    public List<Paciente> getPacientes() {
        return pacientes;
    }

    public void setPacientes(List<Paciente> pacientes) {
        this.pacientes = pacientes;
    }

    public Paciente getPaciente() {
        return paciente;
    }

    public void setPaciente(Paciente paciente) {
        this.paciente = paciente;
    }

    public PlanoTratamento getPlanoTratamento() {
        return planoTratamento;
    }

    public void setPlanoTratamento(PlanoTratamento planoTratamento) {
        this.planoTratamento = planoTratamento;
    }

    public Orcamento getOrcamento() {
        return orcamento;
    }

    public void setOrcamento(Orcamento orcamento) {
        this.orcamento = orcamento;
    }

    public boolean isPago() {
        return pago;
    }

    public void setPago(boolean pago) {
        this.pago = pago;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
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

    public boolean isProduto() {
        return produto;
    }

    public void setProduto(boolean produto) {
        this.produto = produto;
    }

    public void setDominios(List<Dominio> dominios) {
        this.dominios = dominios;
    }

    public List<Dominio> getDominios() {
        return dominios;
    }

    public List<Desconto> getDescontos() {
        return descontos;
    }

    public void setDescontos(List<Desconto> descontos) {
        this.descontos = descontos;
    }

    public Desconto getDesconto() {
        return desconto;
    }

    public void setDesconto(Desconto desconto) {
        this.desconto = desconto;
    }

    public BigDecimal getValorDesconto() {
        return valorDesconto;
    }

    public void setValorDesconto(BigDecimal valorDesconto) {
        this.valorDesconto = valorDesconto;
    }

    public String getRecibo() {
        return recibo;
    }

    public void setRecibo(String recibo) {
        this.recibo = recibo;
    }

    public BigDecimal getNovoValor() {
        return novoValor != null ? valorRestante.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : novoValor : novoValor;
    }

    public void setNovoValor(BigDecimal novoValor) {
        this.novoValor = novoValor;
    }

    public LancamentoDataModel getLancamentoDataModel() {
        return lancamentoDataModel;
    }

    public void setLancamentoDataModel(LancamentoDataModel lancamentoDataModel) {
        this.lancamentoDataModel = lancamentoDataModel;
    }

    public Lancamento[] getLancamentosSelecionados() {
        return lancamentosSelecionados;
    }

    public void setLancamentosSelecionados(Lancamento[] lancamentosSelecionados) {
        this.lancamentosSelecionados = lancamentosSelecionados;
    }

    public Boolean getDisable() {
        return disable;
    }

    public void setDisable(Boolean disable) {
        this.disable = disable;
    }

    public BigDecimal getSomaValores() {
        return somaValores;
    }

    public void setSomaValores(BigDecimal somaValores) {
        this.somaValores = somaValores;
    }

    public Date getDataPagamento() {
        return dataPagamento;
    }

    public void setDataPagamento(Date dataPagamento) {
        this.dataPagamento = dataPagamento;
    }

    public String getFormaPagamento() {
        return formaPagamento;
    }

    public void setFormaPagamento(String formaPagamento) {
        this.formaPagamento = formaPagamento;
    }

    public BigDecimal getValorAgregado() {
        return valorAgregado;
    }

    public void setValorAgregado(BigDecimal valorAgregado) {
        this.valorAgregado = valorAgregado;
    }

    public List<Lancamento> getLancamentosSplit() {
        return lancamentosSplit;
    }

    public void setLancamentosSplit(List<Lancamento> lancamentosSplit) {
        this.lancamentosSplit = lancamentosSplit;
    }

    public BigDecimal getValorAgregadoProporcional() {
        return valorAgregadoProporcional;
    }

    public void setValorAgregadoProporcional(BigDecimal valorAgregadoProporcional) {
        this.valorAgregadoProporcional = valorAgregadoProporcional;
    }

    public BigDecimal getValorAgregadoExcedido() {
        return valorAgregadoExcedido;
    }

    public void setValorAgregadoExcedido(BigDecimal valorAgregadoExcedido) {
        this.valorAgregadoExcedido = valorAgregadoExcedido;
    }

    public BigDecimal getValorRestante() {
        return valorRestante != null ? valorRestante.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : valorRestante : valorRestante;
    }

    public void setValorRestante(BigDecimal valorRestante) {
        this.valorRestante = valorRestante;
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

    public List<String> getStatuss() {
        return statuss;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<PlanoTratamento> getListPt() {
        return listPt;
    }

    public void setListPt(List<PlanoTratamento> listPt) {
        this.listPt = listPt;
    }

    public List<PlanoTratamento> getListPtSelecionados() {
        return listPtSelecionados;
    }

    public void setListPtSelecionados(List<PlanoTratamento> listPtSelecionados) {
        this.listPtSelecionados = listPtSelecionados;
    }

    public boolean isLiberaPagamento() {
        return liberaPagamento;
    }

    public void setLiberaPagamento(boolean liberaPagamento) {
        this.liberaPagamento = liberaPagamento;
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

    public BigDecimal getValorAPagar() {
        return valorAPagar;
    }

    public void setValorAPagar(BigDecimal valorAPagar) {
        this.valorAPagar = valorAPagar;
    }

}
