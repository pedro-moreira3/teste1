package br.com.lume.odonto.managed;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

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
import br.com.lume.common.util.Utils.Mes;
import br.com.lume.common.util.Utils.ValidacaoLancamento;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.conta.ContaSingleton;
import br.com.lume.conta.ContaSingleton.TIPO_CONTA;
import br.com.lume.dominio.DominioSingleton;
import br.com.lume.faturamento.FaturaSingleton;
import br.com.lume.lancamento.LancamentoSingleton;
import br.com.lume.odonto.entity.Dominio;
import br.com.lume.odonto.entity.Fatura;
import br.com.lume.odonto.entity.Fatura.DirecaoFatura;
import br.com.lume.odonto.entity.Fatura.StatusFatura;
import br.com.lume.odonto.entity.Fatura.SubStatusFatura;
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
        // System.out.println("FaturaPagtoMB" + new Timestamp(System.currentTimeMillis()));
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
            LancamentoSingleton.getInstance().inativaLancamento(l, UtilsFrontEnd.getProfissionalLogado());
            this.addInfo("Sucesso", "Lançamento cancelado com sucesso!", true);
            updateValues(getEntity());
        } catch (Exception e) {
            LogIntelidenteSingleton.getInstance().makeLog(e);
            this.addError("Erro", "Falha ao cancelar o lançamento!", true);
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
        updateValues(fatura);
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
                    (fim == null ? null : fim.getTime()), this.status, Arrays.asList(this.subStatusFatura)));
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
        if (fatura.getTipoFatura() == Fatura.TipoFatura.PAGAMENTO_PROFISSIONAL)
            fatura.setDadosTabelaRepassePlanoTratamento(fatura.getDadosTabelaPT());

        //fatura.setDadosTabelaStatusFatura("A Receber");
        //if (fatura.getDadosTabelaRepasseTotalFatura().subtract(fatura.getDadosTabelaRepasseTotalPago()).doubleValue() <= 0)
        //fatura.setDadosTabelaStatusFatura("Recebido");

        this.somaTotal = this.somaTotal.add(fatura.getDadosTabelaRepasseTotalFatura());
        this.somaTotalPago = this.somaTotalPago.add(fatura.getDadosTabelaRepasseTotalPago());
        this.somaTotalNaoPago = this.somaTotalNaoPago.add(fatura.getDadosTabelaRepasseTotalNaoPago());
        this.somaTotalNaoPlanejado = this.somaTotalNaoPlanejado.add(fatura.getDadosTabelaRepasseTotalNaoPlanejado());
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
                        LancamentoSingleton.getInstance().novoLancamento(getEntity(), valorOriginalDividio, getFormaPagamento(), getParcela(), now.getTime(), data.getTime(), getTarifa(),
                                UtilsFrontEnd.getProfissionalLogado(), null);
                        //persistLancamento(getParcela(), getEntity(), valorOriginalDividio, getFormaPagamento(), Calendar.getInstance().getTime(), data.getTime(), getTarifa());
                    } else {
                        LancamentoSingleton.getInstance().novoLancamento(getEntity(), valorOriginalDividio, getFormaPagamento(), getParcela(), now.getTime(), data.getTime(), getTarifa(),
                                UtilsFrontEnd.getProfissionalLogado(), null);
                        now.add(Calendar.MONTH, 1);
                    }
                    data.add(Calendar.MONTH, 1);
                }
            } else {
                LancamentoSingleton.getInstance().novoLancamento(getEntity(), getValor(), getFormaPagamento(), getParcela(), now.getTime(), data.getTime(), getTarifa(),
                        UtilsFrontEnd.getProfissionalLogado(), null);
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
            if (existeCadastroTarifa()) {
                BigDecimal valorOriginalDividio = getValor().divide(new BigDecimal(getParcela()), 2, RoundingMode.HALF_UP);
                BigDecimal diferenca = valorOriginalDividio.multiply(new BigDecimal(getParcela())).subtract(getValor());
                for (int i = 1; i <= getParcela(); i++) {
                    if (i == getParcela()) {
                        valorOriginalDividio = valorOriginalDividio.subtract(diferenca);
                    }

                    if ("CC".equals(getFormaPagamento())) {
                        LancamentoSingleton.getInstance().novoLancamentoGenerico(getEntity(), valorOriginalDividio, getFormaPagamento(), getParcela(), null, now.getTime(), data.getTime(), getTarifa(),
                                UtilsFrontEnd.getProfissionalLogado());
                    } else {
                        LancamentoSingleton.getInstance().novoLancamentoGenerico(getEntity(), valorOriginalDividio, getFormaPagamento(), getParcela(), null, now.getTime(), data.getTime(), getTarifa(),
                                UtilsFrontEnd.getProfissionalLogado());
                        now.add(Calendar.MONTH, 1);
                    }

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
        lancamentosSearch.addAll(LancamentoSingleton.getInstance().getBo().listLancamentosFromFatura(getEntity(), true));
        lancamentosSearch.addAll(LancamentoSingleton.getInstance().getBo().listLancamentosFromFatura(getEntity(), false));
        if (showLancamentosCancelados) {
            lancamentosSearch.addAll(LancamentoSingleton.getInstance().getBo().listLancamentosFromFatura(getEntity(), true, false));
            lancamentosSearch.addAll(LancamentoSingleton.getInstance().getBo().listLancamentosFromFatura(getEntity(), false, false));
        }
        return lancamentosSearch;
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

}
