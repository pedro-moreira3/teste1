package br.com.lume.odonto.managed;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorEvent;
import javax.servlet.http.HttpServletRequest;

import org.primefaces.PrimeFaces;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.event.TabChangeEvent;

import br.com.lume.common.log.LogIntelidenteSingleton;
import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.convenioProcedimento.ConvenioProcedimentoSingleton;
import br.com.lume.faturamento.FaturaSingleton;
import br.com.lume.lancamento.LancamentoSingleton;
import br.com.lume.lancamentoContabil.LancamentoContabilSingleton;
import br.com.lume.odonto.entity.Lancamento;
import br.com.lume.odonto.entity.Profissional;
import br.com.lume.odonto.entity.ReciboRepasseProfissional;
import br.com.lume.odonto.entity.ReciboRepasseProfissional.StatusRecibo;
import br.com.lume.odonto.entity.ReciboRepasseProfissionalLancamento;
import br.com.lume.odonto.entity.ReciboRepasseProfissionalLancamentoDadosImutaveis;
import br.com.lume.odonto.entity.RepasseFaturas;
import br.com.lume.odonto.entity.RepasseFaturasLancamento;
import br.com.lume.planoTratamentoProcedimento.PlanoTratamentoProcedimentoSingleton;
import br.com.lume.planoTratamentoProcedimentoCusto.PlanoTratamentoProcedimentoCustoSingleton;
import br.com.lume.profissional.ProfissionalSingleton;
import br.com.lume.repasse.ReciboRepasseProfissionalLancamentoSingleton;
import br.com.lume.repasse.ReciboRepasseProfissionalSingleton;
import br.com.lume.repasse.RepasseFaturasLancamentoSingleton;
import br.com.lume.repasse.RepasseFaturasSingleton;

@ManagedBean
@ViewScoped
public class ReciboRepasseProfissionalMB extends LumeManagedBean<ReciboRepasseProfissional> {

    private static final long serialVersionUID = 1L;

    private String filtroPeriodoRepasses = "M", filtroPeriodoRecibos = "M", descricao, observacao;
    private Date dataInicioRepasses, dataFimRepasses, dataInicioRecibos, dataFimRecibos;
    boolean lancSemRecibo = true, lancValidadosOnly = false, recibosFindCancelados = false, lancMesesAnterioresRepasse = false;
    private Profissional profissionalRepasses, profissionalRecibos;
    private List<Lancamento> lancamentos;
    private Lancamento[] lancamentosSelecionados;
    private StatusRecibo filtroStatus;

    private List<Profissional> profissionaisRecibo;
    private HashMap<Profissional, Integer> profissionaisReciboLancamentos;
    private HashMap<Profissional, Double> profissionaisReciboValores;

    List<ReciboRepasseProfissionalLancamento> recibosAgrupados;

    //EXPORTA????O TABELA
    private DataTable tabelaLancamentos, tabelaRecibos, tabelaLancamentosRecibos;

    private int quantidadeRepasses = 0;
    
    private ReciboRepasseProfissional repasseParaAprovar;

    private Date dataRepassar = new Date();
    
    public ReciboRepasseProfissionalMB() {
        super(ReciboRepasseProfissionalSingleton.getInstance().getBo());
        this.setClazz(ReciboRepasseProfissional.class);

        HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        String url = request.getRequestURL().toString();
        if (!url.contains("repasseComRecibo")) {
            try {
                setFiltroStatus(StatusRecibo.TODOS);
                actionTrocaDatasCriacaoRepasses(null);
                actionTrocaDatasCriacaoRecibos(null);
                pesquisarRepasses();
            } catch (Exception e) {
                LogIntelidenteSingleton.getInstance().makeLog(e);
            }
        } else {
            setFiltroStatus(StatusRecibo.TODOS);
            actionTrocaDatasCriacaoRepasses(null);
            actionTrocaDatasCriacaoRecibos(null);
        }
        //System.out.println("ReciboRepasseProfissionalMB" + new Timestamp(System.currentTimeMillis()));
    }

    public void cancelarRecibo(ReciboRepasseProfissional r) {
        ReciboRepasseProfissionalSingleton.getInstance().inativaRecibo(r, UtilsFrontEnd.getProfissionalLogado());
        addInfo("Sucesso", Mensagens.getMensagemOffLine(Mensagens.REGISTRO_REMOVIDO_COM_SUCESSO));
        pesquisarRecibos();
    }

    public void aprovarRecibo() {
        try {          
            ReciboRepasseProfissionalSingleton.getInstance().aprovarRecibo(dataRepassar,repasseParaAprovar, UtilsFrontEnd.getProfissionalLogado());
            addInfo("Sucesso", Mensagens.getMensagemOffLine(Mensagens.REGISTRO_SALVO_COM_SUCESSO));
            pesquisarRecibos();
           // PrimeFaces.current().executeScript("PF('dtPrincipal').filter()");
           // PrimeFaces.current().executeScript("PF('dtRecibos').filter()");
        } catch (Exception e) {
            addInfo("Erro", Mensagens.getMensagemOffLine(Mensagens.ERRO_AO_SALVAR_REGISTRO));
            e.printStackTrace();
        }
    }
    
    public boolean showIfCalculo(ReciboRepasseProfissional r) {
        return r.getReciboLancamentos() != null && !r.getReciboLancamentos().isEmpty();
    }
    public boolean showIfDiaria(ReciboRepasseProfissional r) {
        return r.getReciboDiarias() != null && !r.getReciboDiarias().isEmpty();
    }
    
    public String getMensagemAprovacaoRecibo(ReciboRepasseProfissional r) {
        if(r == null)
            return "";
        
        if(showIfCalculo(r)) {
            return "Voc?? tem certeza que deseja aprovar o pagamento este recibo?  Os lan??amentos n??o pagos " + 
            "ser??o pagos neste momento, completando o repasse.";
        } else if(showIfDiaria(r)) {
            return "Voc?? tem certeza que deseja aprovar o pagamento este recibo?  Ser?? criada uma fatura de " + 
                    "pagamento para o profissional, completando o repasse.";
        } else
            return "";
    }
    
    public void preparaAprovarRecibo(ReciboRepasseProfissional r) {
        dataRepassar = new Date();
        repasseParaAprovar = r;
    }

    public void onTabChange(TabChangeEvent event) {
        if ("Repasses".equals(event.getTab().getTitle())) {
            pesquisarRepasses();
        } else if ("Hist??rico de Recibos".equals(event.getTab().getTitle())) {
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
                    isLancMesesAnterioresRepasse()));
            if (getLancamentos() != null && !getLancamentos().isEmpty()) {
                getLancamentos().forEach(lancamento -> {
                    try {
                        // Setando Objetos Gerais
                        RepasseFaturasLancamento repasse = RepasseFaturasLancamentoSingleton.getInstance().getBo().getFaturaRepasseLancamentoFromLancamentoRepasse(lancamento);
                        lancamento.setRfl(repasse);
                        lancamento.setPtp(PlanoTratamentoProcedimentoSingleton.getInstance().getProcedimentoFromFaturaItem(repasse.getFaturaItem()));

                        // Calculos necess??rios para mostragem na tabela
                        lancamento.setDadosTabelaValorTotalFatura(FaturaSingleton.getInstance().getTotal(repasse.getFaturaItem().getFatura()));
                        lancamento.setDadosTabelaPercItem(
                                repasse.getFaturaItem().getValorComDesconto().divide(lancamento.getDadosTabelaValorTotalFatura(), 4, BigDecimal.ROUND_HALF_UP).multiply(BigDecimal.valueOf(100)));
                        lancamento.setDadosTabelaValorProcComPercItem(String.format("R$ %.2f", repasse.getFaturaItem().getValorComDesconto().doubleValue()) + " (" + String.format("%.2f%%",
                                lancamento.getDadosTabelaPercItem().doubleValue()) + ")");
                        lancamento.setDadosTabelaValorTotalCustosDiretos(
                                PlanoTratamentoProcedimentoCustoSingleton.getInstance().getCustoDiretoFromPTP(lancamento.getPtp()).setScale(2, BigDecimal.ROUND_HALF_UP));
                        lancamento.setDadosTabelaValorRecebidoComFormaPagto(
                                String.format("R$ %.2f", repasse.getLancamentoOrigem().getValor()) + " " + repasse.getLancamentoOrigem().getFormaPagamentoStr());
                        lancamento.setDadosTabelaValorTaxasETarifas(
                                LancamentoContabilSingleton.getInstance().getTaxasAndTarifasForLancamentoRecebimento(repasse.getLancamentoOrigem()).setScale(2, BigDecimal.ROUND_HALF_UP));
                        if (Profissional.PORCENTAGEM.equals(repasse.getRepasseFaturas().getTipoCalculo())) {
                            lancamento.setDadosTabelaValorRepasse(String.format("%.2f%%", repasse.getRepasseFaturas().getValorCalculo()));
                            lancamento.setDadosTabelaMetodoRepasse("POR");
                        } else if (Profissional.PROCEDIMENTO.equals(repasse.getRepasseFaturas().getTipoCalculo())) {
                            BigDecimal valorRepasse = ConvenioProcedimentoSingleton.getInstance().getCheckValorConvenio(lancamento.getPtp());
                            lancamento.setDadosTabelaValorRepasse(String.format("R$ %.2f", valorRepasse.doubleValue()));
                            lancamento.setDadosTabelaMetodoRepasse("PRO");
                        }
                        lancamento.setDadosTabelaMetodoRepasseComValor(lancamento.getDadosTabelaMetodoRepasse() + " - " + lancamento.getDadosTabelaValorRepasse());

                        // Calculos necess??rios para mostragem da explica????o do c??lculo
                        lancamento.setDadosCalculoPercLancamentoFatura(repasse.getLancamentoOrigem().getValor().divide(lancamento.getDadosTabelaValorTotalFatura(), 4, BigDecimal.ROUND_HALF_UP));
                        lancamento.setDadosCalculoPercTaxa(
                                (repasse.getLancamentoOrigem().getTarifa() != null ? repasse.getLancamentoOrigem().getTarifa().getTaxa().divide(BigDecimal.valueOf(100)) : BigDecimal.ZERO));
                        lancamento.setDadosCalculoValorTaxa(lancamento.getDadosCalculoPercTaxa().multiply(repasse.getLancamentoOrigem().getValor()));
                        lancamento.setDadosCalculoValorTarifa((repasse.getLancamentoOrigem().getTarifa() != null ? repasse.getLancamentoOrigem().getTarifa().getTarifa() : BigDecimal.ZERO));
                        lancamento.setDadosCalculoPercTributo(repasse.getLancamentoOrigem().getTributoPerc());
                        lancamento.setDadosCalculoValorTributo(repasse.getLancamentoOrigem().getTributoPerc().multiply(repasse.getLancamentoOrigem().getValor()).setScale(2, BigDecimal.ROUND_HALF_UP));
                        lancamento.setDadosCalculoPercCustoDireto(lancamento.getDadosTabelaValorTotalCustosDiretos().divide(lancamento.getDadosTabelaValorTotalFatura(), 4, BigDecimal.ROUND_HALF_UP));
                        lancamento.setDadosCalculoValorCustoDiretoRateado(
                                lancamento.getDadosTabelaValorTotalCustosDiretos().divide(lancamento.getDadosTabelaValorTotalFatura(), 4, BigDecimal.ROUND_HALF_UP).multiply(
                                        repasse.getLancamentoOrigem().getValor()));
                        lancamento.setDadosCalculoTotalReducao(lancamento.getDadosCalculoValorTaxa().add(lancamento.getDadosCalculoValorTarifa()).add(lancamento.getDadosCalculoValorTributo()));
                        lancamento.setDadosCalculoRecebidoMenosReducao(
                                repasse.getLancamentoOrigem().getValor().subtract(lancamento.getDadosCalculoTotalReducao()).setScale(2, BigDecimal.ROUND_HALF_UP));

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
            //List<Long> idsPtps = new ArrayList<Long>();
            
            getEntity().getReciboLancamentos().removeIf(l -> !l.getLancamento().isAtivo());
            
            Map<Long, ReciboRepasseProfissionalLancamento> recibosMap = new HashMap<Long, ReciboRepasseProfissionalLancamento>();
            getEntity().getReciboLancamentos().forEach(repasseRecibo -> {
                try {
                    
                    RepasseFaturasLancamento repasse = RepasseFaturasLancamentoSingleton.getInstance().getBo().getFaturaRepasseLancamentoFromLancamentoRepasse(repasseRecibo.getLancamento());
                    if (repasse != null) {
                        repasseRecibo.getLancamento().setPtp(PlanoTratamentoProcedimentoSingleton.getInstance().getProcedimentoFromFaturaItem(repasse.getFaturaItem()));
                    }
                    //quando nao tem repasse de origem    
                    if (repasse == null) {
                        repasse = RepasseFaturasLancamentoSingleton.getInstance().getBo().getFaturaRepasseLancamentoFromLancamentoRepasseDestino(repasseRecibo.getLancamento());
                        
                        if(repasse == null) {
                            RepasseFaturas rf = RepasseFaturasSingleton.getInstance().getBo().getFaturaRepasseFromFaturaProfissional(repasseRecibo.getLancamento().getFatura());                                                        
                            repasseRecibo.getLancamento().setPtp(rf.getPlanoTratamentoProcedimento());
                            repasseRecibo.getDados().setPaciente(rf.getFaturaOrigem().getPaciente().getDadosBasico().getNome());
                            repasseRecibo.getDados().setPlanoTratamento(rf.getPlanoTratamentoProcedimento().getDescricao());
                            repasseRecibo.getDados().setPlanoTratamentoProcedimento(rf.getPlanoTratamentoProcedimento().getDescricaoCompletaComFace());
                            repasseRecibo.getDados().setDataExecucao(rf.getPlanoTratamentoProcedimento().getDataFinalizado());
                            
                        }
                        
                        if (repasse != null) {                         
                            repasseRecibo.getLancamento().setPtp(repasse.getRepasseFaturas().getPlanoTratamentoProcedimento());
                            repasseRecibo.getDados().setPaciente(repasse.getRepasseFaturas().getPlanoTratamentoProcedimento().getPlanoTratamento().getPaciente().getDadosBasico().getNome());
                            repasseRecibo.getDados().setPlanoTratamento(repasse.getRepasseFaturas().getPlanoTratamentoProcedimento().getPlanoTratamento().getDescricao());
                            repasseRecibo.getDados().setPlanoTratamentoProcedimento(repasse.getRepasseFaturas().getPlanoTratamentoProcedimento().getDescricaoCompletaComFace());
                            repasseRecibo.getDados().setDataExecucao(repasse.getRepasseFaturas().getPlanoTratamentoProcedimento().getDataFinalizado());
                          
                        }
                        
                        //TODO - Corrige situa????es em que n??o existe dados na tabela ReciboRepasseProfissionalLancamentoDadosImutaveis
                        if (repasseRecibo.getDados().getValorTotal() == null && repasseRecibo.getDados().getValorJaPago() == null) {
                            repasseRecibo.getDados().setLancamentoPago(repasseRecibo.getLancamento().getValidadoStr());
                            repasseRecibo.getDados().setValorTotal(FaturaSingleton.getInstance().getTotal(repasseRecibo.getLancamento().getFatura()));
                            
                            List<Lancamento> lancamentosAnteriores = findLancamentosImutaveis(repasseRecibo.getDados());
                            BigDecimal valorPago = BigDecimal.ZERO;
                            
                            if(!lancamentosAnteriores.isEmpty())
                                for(Lancamento l : lancamentosAnteriores)
                                    valorPago = valorPago.add(l.getValor());
                            
                            repasseRecibo.getDados().setValorJaPago(valorPago);
                            repasseRecibo.getDados().setValorAPagar(repasseRecibo.getLancamento().getValorComDesconto());
                            ReciboRepasseProfissionalLancamentoSingleton.getInstance().getBo().persist(repasseRecibo);
                        }
                        
                        if (repasseRecibo.getLancamento().getValorComDesconto() == null
                                || repasseRecibo.getLancamento().getValorComDesconto().compareTo(BigDecimal.ZERO) == 0)
                            repasseRecibo.getDados().setValorAPagar(repasseRecibo.getLancamento().getValor());
                    }
                    repasseRecibo.setValorTotalRepassar(FaturaSingleton.getInstance().getTotal(repasseRecibo.getLancamento().getFatura()));
                    if (recibosMap.containsKey(repasseRecibo.getLancamento().getPtp().getId())) {
                        // BigDecimal soma = repasseRecibo.getLancamento().getValor().add(recibosMap.get(repasseRecibo.getLancamento().getPtp().getId()).getLancamento().getValor());
                        //BigDecimal somaApagar = repasseRecibo.getDados().getValorAPagar().add(recibosMap.get(repasseRecibo.getLancamento().getPtp().getId()).getDados().getValorAPagar());
                        //   {reciboLancamento.dados.valorAPagar                        
                        //  repasseRecibo.getLancamento().setValor(soma);
                        //repasseRecibo.getDados().setValorAPagar(somaApagar);
                        recibosMap.put(repasseRecibo.getLancamento().getPtp().getId(), repasseRecibo);
                    } else {
                        recibosMap.put(repasseRecibo.getLancamento().getPtp().getId(), repasseRecibo);
                    }

                } catch (Exception e) {
                    repasseRecibo.getLancamento().setPtp(null);
                }
            });
            recibosAgrupados = new ArrayList<ReciboRepasseProfissionalLancamento>();
            for (Map.Entry<Long, ReciboRepasseProfissionalLancamento> entry : recibosMap.entrySet())
                recibosAgrupados.add(entry.getValue());

            quantidadeRepasses = recibosAgrupados.size();
            getEntity().getReciboLancamentos().sort((l1,l2) -> l1.getDados().getPaciente().compareTo(l2.getDados().getPaciente()));
        }
        
        if(showIfCalculo(recibo))
            PrimeFaces.current().executeScript("PF('dtReciboRepasseLancamentos').filter();");
        else if(showIfDiaria(recibo))
            PrimeFaces.current().executeScript("PF('dtReciboRepasseDiarias').filter();");
        PrimeFaces.current().executeScript("PF('dlgVisualizarRecibo').show()");
    }
    
    public List<Lancamento> findLancamentosImutaveis(ReciboRepasseProfissionalLancamentoDadosImutaveis lancamentoImutavel){
        List<Lancamento> lancamentosAnteriores = new ArrayList<Lancamento>();
        
        lancamentoImutavel.getReciboLancamento().getLancamento().getFatura().getLancamentos().forEach(l -> {
            if(l.getId() < lancamentoImutavel.getReciboLancamento().getLancamento().getId()
                    && l.getDataConferido() != null && l.getDataValidado() != null)
                lancamentosAnteriores.add(l);
        });
        
        return lancamentosAnteriores;
    }

    public void prepararRecibo() {
        this.descricao = null;
        this.observacao = null;

        this.profissionaisReciboLancamentos = new HashMap<>();
        this.profissionaisReciboValores = new HashMap<>();
        this.profissionaisRecibo = new ArrayList<>();

        if (lancamentosSelecionados == null || Arrays.asList(lancamentosSelecionados).size() == 0) {
            addError("Erro", "?? necess??rio escolher ao menos um repasse.");
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
                    addError("Erro", "Existem lan??amentos na lista de repasse que j?? est??o em outros recibos!");
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

    public void actionTrocaDatasCriacaoRecibos(AjaxBehaviorEvent event) {
        try {
            this.dataInicioRecibos = getDataInicio(filtroPeriodoRecibos);
            this.dataFimRecibos = getDataFim(filtroPeriodoRecibos);
        } catch (Exception e) {
            LogIntelidenteSingleton.getInstance().makeLog(e);
            this.addError("Erro", Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS));
        }
    }

    public void actionTrocaDatasCriacaoRepasses(AjaxBehaviorEvent event) {
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
            } else if ("S".equals(filtro)) { //??ltimos 7 dias              
                c.add(Calendar.DAY_OF_MONTH, -7);
                dataInicio = c.getTime();
            } else if ("Q".equals(filtro)) { //??ltimos 15 dias              
                c.add(Calendar.DAY_OF_MONTH, -15);
                dataInicio = c.getTime();
            } else if ("T".equals(filtro)) { //??ltimos 30 dias                
                c.add(Calendar.DAY_OF_MONTH, -30);
                dataInicio = c.getTime();
            } else if ("M".equals(filtro)) { //M??s Atual              
                c.set(Calendar.DAY_OF_MONTH, 1);
                dataInicio = c.getTime();
            } else if ("I".equals(filtro)) { //M??s Atual             
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

    public void exportarTabela(String type) {
        exportarTabela("Recibo dos profissionais", tabelaLancamentosRecibos, type);
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

    public boolean isLancMesesAnterioresRepasse() {
        return lancMesesAnterioresRepasse;
    }

    public void setLancMesesAnterioresRepasse(boolean lancMesesAnterioresRepasse) {
        this.lancMesesAnterioresRepasse = lancMesesAnterioresRepasse;
    }

    public List<ReciboRepasseProfissionalLancamento> getRecibosAgrupados() {
        return recibosAgrupados;
    }

    public void setRecibosAgrupados(List<ReciboRepasseProfissionalLancamento> recibosAgrupados) {
        this.recibosAgrupados = recibosAgrupados;
    }

    public int getQuantidadeRepasses() {
        return quantidadeRepasses;
    }

    public void setQuantidadeRepasses(int quantidadeRepasses) {
        this.quantidadeRepasses = quantidadeRepasses;
    }

    
    public ReciboRepasseProfissional getRepasseParaAprovar() {
        return repasseParaAprovar;
    }

    
    public void setRepasseParaAprovar(ReciboRepasseProfissional repasseParaAprovar) {
        this.repasseParaAprovar = repasseParaAprovar;
    }
    
    public Date getDataRepassar() {
        return dataRepassar;
    }

    public void setDataRepassar(Date dataRepassar) {
        this.dataRepassar = dataRepassar;
    }

    public DataTable getTabelaLancamentosRecibos() {
        return tabelaLancamentosRecibos;
    }

    public void setTabelaLancamentosRecibos(DataTable tabelaLancamentosRecibos) {
        this.tabelaLancamentosRecibos = tabelaLancamentosRecibos;
    }

}
