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

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.AjaxBehaviorEvent;

import org.primefaces.PrimeFaces;
import org.primefaces.component.datatable.DataTable;

import br.com.lume.common.log.LogIntelidenteSingleton;
import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.convenioProcedimento.ConvenioProcedimentoSingleton;
import br.com.lume.faturamento.FaturaSingleton;
import br.com.lume.lancamentoContabil.LancamentoContabilSingleton;
import br.com.lume.odonto.entity.Fatura;
import br.com.lume.odonto.entity.Lancamento;
import br.com.lume.odonto.entity.PlanoTratamentoProcedimento;
import br.com.lume.odonto.entity.Profissional;
import br.com.lume.odonto.entity.ReciboRepasseProfissional;
import br.com.lume.odonto.entity.RepasseFaturas;
import br.com.lume.odonto.entity.RepasseFaturasItem;
import br.com.lume.odonto.entity.RepasseFaturasLancamento;
import br.com.lume.planoTratamentoProcedimento.PlanoTratamentoProcedimentoSingleton;
import br.com.lume.planoTratamentoProcedimentoCusto.PlanoTratamentoProcedimentoCustoSingleton;
import br.com.lume.profissional.ProfissionalSingleton;
import br.com.lume.repasse.ReciboRepasseProfissionalLancamentoSingleton;
import br.com.lume.repasse.ReciboRepasseProfissionalSingleton;
import br.com.lume.repasse.RepasseFaturasItemSingleton;
import br.com.lume.repasse.RepasseFaturasLancamentoSingleton;
import br.com.lume.repasse.RepasseFaturasSingleton;
import br.com.lume.security.EmpresaSingleton;
import br.com.lume.security.entity.Empresa;

/**
 * @author ariel.pires
 */
@ManagedBean
@ViewScoped
public class RepasseProfissionalComReciboMB extends LumeManagedBean<PlanoTratamentoProcedimento> {

    private static final long serialVersionUID = 1L;
    //private Logger log = Logger.getLogger(FaturaPagtoMB.class);

    private boolean mesesAnteriores = false, semPendencias = true, procedimentosNaoExecutados = false, mostrarRepasseAntigo = false;

    //FILTROS
    private Date dataInicio;
    private Date dataFim;
    private Profissional profissional;

    private String filtroPeriodo = "MA";

    private List<String> pendencias = new ArrayList<String>();

    private boolean validaPagamentoPaciente;
    private boolean validaConferenciaCustosDiretos;
    private boolean validaExecucaoProcedimento;

    private List<PlanoTratamentoProcedimento> ptpsSelecionados;

    private String descricao, observacao;

    //EXPORTAÇÃO TABELA
    private DataTable tabelaRepasse;

    private List<Profissional> profissionaisRecibo;
    private HashMap<Profissional, Integer> profissionaisReciboLancamentos;
    private HashMap<Profissional, Double> profissionaisReciboValores;

    private List<Lancamento> lancamentosCalculados;

    private ReciboRepasseProfissional reciboRepasseProfissional;
    private List<ReciboRepasseProfissional> listaRecibos;

    private List<Lancamento> lancamentoParaRecibo;

    public Integer getQtdeLancamentosFromProfissional(Profissional profissional) {
        return this.profissionaisReciboLancamentos.get(profissional);
    }

    public Double getValorLancamentosFromProfissional(Profissional profissional) {
        return this.profissionaisReciboValores.get(profissional);
    }

    public RepasseProfissionalComReciboMB() {
        super(PlanoTratamentoProcedimentoSingleton.getInstance().getBo());
        this.setClazz(PlanoTratamentoProcedimento.class);
        try {
            Empresa empresa = EmpresaSingleton.getInstance().getBo().find(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
            validaPagamentoPaciente = empresa.getValidarRepasseLancamentoOrigemValidadoBool();
            validaConferenciaCustosDiretos = empresa.getValidarRepasseConfereCustoDiretoBool();
            validaExecucaoProcedimento = empresa.getValidarRepasseProcedimentoFinalizadoBool();

            actionTrocaDatas(null);
        } catch (Exception e) {
            LogIntelidenteSingleton.getInstance().makeLog(e);
            this.addError("Erro", "Não foi possivel carregar a tela.", true);
        }
    }

    public void prepararRecibo() {
        this.descricao = null;
        this.observacao = null;

        this.profissionaisReciboLancamentos = new HashMap<>();
        this.profissionaisReciboValores = new HashMap<>();
        this.profissionaisRecibo = new ArrayList<>();
//
        if (ptpsSelecionados == null || ptpsSelecionados.size() == 0) {
            addError("Erro", "É necessário escolher ao menos um repasse.");
            return;
        }
        lancamentoParaRecibo = new ArrayList<Lancamento>();
        for (PlanoTratamentoProcedimento ptp : ptpsSelecionados) {
            if (!existemPendencias(ptp)) {
                Fatura fatura = getFaturaFromPtp(ptp);
                List<Lancamento> lancamentos = fatura.getLancamentos();
                Profissional dentistaExecutor = ptp.getDentistaExecutor();
                for (Lancamento lancamento : lancamentos) {
                    if (lancamento.getAtivoStr().equals("Sim") && lancamento.getConferidoPorProfissional() == null) {
                        Double valor = (lancamento.getValorComDesconto() == null || lancamento.getValorComDesconto().doubleValue() == 0d ? lancamento.getValor().doubleValue() : lancamento.getValorComDesconto().doubleValue());
                        if (getProfissionaisRecibo() == null || getProfissionaisRecibo().indexOf(dentistaExecutor) < 0) {
                            this.profissionaisReciboLancamentos.put(dentistaExecutor, new Integer(1));
                            this.profissionaisReciboValores.put(dentistaExecutor, valor);
                            this.profissionaisRecibo.add(dentistaExecutor);
                        } else {
                            this.profissionaisReciboLancamentos.put(dentistaExecutor, this.profissionaisReciboLancamentos.get(dentistaExecutor) + 1);
                            this.profissionaisReciboValores.put(dentistaExecutor, this.profissionaisReciboValores.get(dentistaExecutor) + valor);
                        }
                        lancamentoParaRecibo.add(lancamento);
                    }
                }
            }
        }

        PrimeFaces.current().executeScript("PF('dlgGerarRecibo').show()");
    }

    public void cancelarRecibo() {
        setPtpsSelecionados(null);
        PrimeFaces.current().executeScript("PF('dlgGerarRecibo').hide()");
    }

    public void gerarRecibo() {
        try {
            List<Lancamento> lancamentosParaGerarRecibo = new ArrayList<Lancamento>();
            String mensagem = "sem lancamentos válidos para repasse";
            // for (PlanoTratamentoProcedimento ptp : ptpsSelecionados) {
            // Fatura fatura = getFaturaFromPtp(ptp);
            // List<Lancamento> lancamentos = fatura.getLancamentos();
            for (Lancamento l : lancamentoParaRecibo) {
                if (ReciboRepasseProfissionalLancamentoSingleton.getInstance().getBo().findReciboValidoForLancamento(l) != null) {
                    mensagem = "Existem lançamentos na lista de repasse que já estão em outros recibos!";
                    //addError("Erro", "Existem lançamentos na lista de repasse que já estão em outros recibos!");
                    //return;
                } else {
                    lancamentosParaGerarRecibo.add(l);
                }
            }
            // }
            if (lancamentosParaGerarRecibo == null || lancamentosParaGerarRecibo.size() == 0) {
                addError("Erro", mensagem);
                return;
            } else {
                if (!ReciboRepasseProfissionalSingleton.getInstance().gerarRecibo(lancamentosParaGerarRecibo, this.descricao, this.observacao, UtilsFrontEnd.getProfissionalLogado()))
                    throw new Exception();
                pesquisar();
                setPtpsSelecionados(null);
                PrimeFaces.current().executeScript("PF('dlgGerarRecibo').hide()");
                addInfo("Sucesso", Mensagens.getMensagemOffLine(Mensagens.REGISTRO_SALVO_COM_SUCESSO));
            }
        } catch (Exception e) {
            addError("Erro", Mensagens.getMensagemOffLine(Mensagens.ERRO_AO_SALVAR_REGISTRO));
        }
    }

    public void aprovarRecibo(ReciboRepasseProfissional r) {
        ReciboRepasseProfissionalSingleton.getInstance().aprovarRecibo(r, UtilsFrontEnd.getProfissionalLogado());
        addInfo("Sucesso", Mensagens.getMensagemOffLine(Mensagens.REGISTRO_SALVO_COM_SUCESSO));
        pesquisar();
    }

    public void preparaVisualizacao(ReciboRepasseProfissional recibo) {
        reciboRepasseProfissional = recibo;
        if (reciboRepasseProfissional.getReciboLancamentos() != null && !reciboRepasseProfissional.getReciboLancamentos().isEmpty()) {
            reciboRepasseProfissional.getReciboLancamentos().forEach(repasseRecibo -> {
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

    public Date getDataFim(String filtro) {
        Date dataFim = null;
        try {
            Calendar c = Calendar.getInstance();
            if ("O".equals(filtro)) {
                c.add(Calendar.DAY_OF_MONTH, -1);
                dataFim = c.getTime();
            } else if ("MA".equals(filtro)) { //Mês Anterior
                c.add(Calendar.MONTH, -1);
                c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DATE));
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
            } else if ("MA".equals(filtro)) { //Mês Anterior            
                c.set(Calendar.DAY_OF_MONTH, 1);
                c.add(Calendar.MONTH, -1);
                dataInicio = c.getTime();
            } else if ("I".equals(filtro)) { //Ultimos 6 mêses         
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

    public void actionTrocaDatas(AjaxBehaviorEvent event) {
        try {
            this.dataInicio = getDataInicio(filtroPeriodo);
            this.dataFim = getDataFim(filtroPeriodo);
        } catch (Exception e) {
            LogIntelidenteSingleton.getInstance().makeLog(e);
            this.addError("Erro", Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS));
        }
    }

    public void mudaFiltroSemPendencia() {
        mesesAnteriores = false;
    }

    public void mudaFiltroComPendencia() {
        semPendencias = false;
    }

    public void pesquisar() {
        try {

            setEntityList(PlanoTratamentoProcedimentoSingleton.getInstance().getBo().listParaRepasseProfissionais(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa(), dataInicio, dataFim,
                    profissional, mesesAnteriores, procedimentosNaoExecutados, mostrarRepasseAntigo));

            //precisa deixar apenas ptp sem pendencias
            //pensar uma maneira melhor de tratar isso
            //preciso retirar se ja tiver recibo aprovado...
            //pensar uma maneira melhor de tratar isso            
            if (mesesAnteriores && getEntityList() != null && getEntityList().size() > 0) {
                List<PlanoTratamentoProcedimento> semMesesAnteriores = new ArrayList<PlanoTratamentoProcedimento>();
                semMesesAnteriores.addAll(getEntityList());
                for (PlanoTratamentoProcedimento ptp : getEntityList()) {
                    Fatura fatura = getFaturaFromPtp(ptp);
                    if (fatura != null && fatura.getLancamentos() != null) {
                        for (Lancamento lancamento : fatura.getLancamentos()) {
                            if (lancamento.getReciboRepasseAtivo() != null && lancamento.getReciboRepasseAtivo().getRecibo() != null && lancamento.getReciboRepasseAtivo().getRecibo().getAprovado() != null && lancamento.getReciboRepasseAtivo().getRecibo().getAprovado().equals(
                                    "S")) {
                                semMesesAnteriores.remove(ptp);
                            }

                        }
                    }
                }
                setEntityList(semMesesAnteriores);
            }

            //precisa deixar apenas ptp sem pendencias
            //pensar uma maneira melhor de tratar isso
            if (semPendencias && getEntityList() != null && getEntityList().size() > 0) {
                List<PlanoTratamentoProcedimento> semPendencias = new ArrayList<PlanoTratamentoProcedimento>();
                semPendencias.addAll(getEntityList());
                for (PlanoTratamentoProcedimento ptp : getEntityList()) {
                    if (existemPendencias(ptp) || valorDisponivel(ptp).compareTo(new BigDecimal(0)) == 0) {
                        semPendencias.remove(ptp);
                    }
                }
                setEntityList(semPendencias);
            }

        } catch (Exception e) {
            e.printStackTrace();
            LogIntelidenteSingleton.getInstance().makeLog(e);
            this.addError("Erro", Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), true);
        }
    }

    public void faturaManual(PlanoTratamentoProcedimento ptp) {
        try {

            if (ptp.getPlanoTratamento().getRegistroAntigo() != null && ptp.getPlanoTratamento().getRegistroAntigo().equals("S")) {
                this.addError("Erro", "Plano de tratamento criado no modelo antigo. realizar o repasse na aba de Repasse Antigo.", true);
            } else {
                this.addError("Erro", Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), true);
            }
            RepasseFaturasSingleton.getInstance().recalculaRepasse(ptp, ptp.getDentistaExecutor(), ptp.getDentistaExecutor());
        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    public void mostraCalculoRepasse(PlanoTratamentoProcedimento ptp) {

        //TODO hoje so mostra calculo quando tem  lancamento de origem, ou seja, quando empresa validar pagamento do paciente antes de receber.
        //if(validaPagamentoPaciente) {
        setEntity(ptp);
        Fatura fatura = getFaturaFromPtp(ptp);
        // List<Lancamento> lancamentos =  fatura.getLancamentos();  

        // Fatura faturaOrigem = fatura.getRepassesOriginamEstaFatura().get(0).getFaturaOrigem();
        List<Lancamento> lancamentos = null;
        if (fatura != null) {
            lancamentos = fatura.getLancamentos();
        }
        //TODO verificar como vamos mostrar quando tiver maid de um lançamento...
        //   for (Lancamento lancamento : lancamentos) {

        if (lancamentos != null && lancamentos.size() > 0) {
            lancamentosCalculados = new ArrayList<Lancamento>();
            for (Lancamento lancamento : lancamentos) {
                try {
                    Lancamento lancamentoCalculado = lancamento;

                    fatura.getRepassesOriginamEstaFatura();
                    //verificando se conseguimos pegar pelo  ptp
                    RepasseFaturas repasseFaturas = RepasseFaturasSingleton.getInstance().getBo().findByPtp(ptp);
                    RepasseFaturasLancamento repasse = null;
                    if (repasseFaturas != null) {
                        repasse = RepasseFaturasLancamentoSingleton.getInstance().getBo().getFaturaRepasseFromRepasseFaturas(repasseFaturas);
                    }
                    //se nao tem, vamos do jeito antigo:
                    //tentando pegar do lancamento de origem:
                    if (repasse == null) {
                        List<RepasseFaturasLancamento> repasseFaturaLancamentos = RepasseFaturasLancamentoSingleton.getInstance().getBo().getFaturaRepasseLancamentoFromLancamentoOrigem(
                                lancamentoCalculado);
                        if (repasseFaturaLancamentos != null && repasseFaturaLancamentos.size() != 0) {
                            repasse = repasseFaturaLancamentos.get(0);
                            repasseFaturas = repasseFaturaLancamentos.get(0).getRepasseFaturas();
                        }
                    }
                    //nao conseguimos achar repasse
                    if (repasse == null) {
                        return;
                    }

                    //PARA INATIVAR OS
                    //RepasseFaturasItemSingleton.getInstance().inativaItemRepasseProfissional(getFaturaFromPtp(ptp).getItens().get(0), 
                    //        repasseFaturas, ptp.getDentistaExecutor(), true);

                    lancamentoCalculado.setRfl(repasse);

                    //por prioridade pegamos o lancamento de origem, se nao tivermos é pq a fatura foi gerada
                    //sem ter orcamento nem fatura de cliente, portanto pegamos do destino.
                    Lancamento lancamentoParaCalculo = repasse.getLancamentoOrigem();
                    if (lancamentoParaCalculo == null) {
                        lancamentoParaCalculo = repasse.getLancamentoRepasse();
                    }

                    lancamentoCalculado.setPtp(ptp);

                    // Calculos necessários para mostragem na tabela
                    lancamentoCalculado.setDadosTabelaValorTotalFatura(FaturaSingleton.getInstance().getTotal(fatura));
                    lancamentoCalculado.setDadosTabelaPercItem(
                            fatura.getItensFiltered().get(0).getValorComDesconto().divide(lancamentoCalculado.getDadosTabelaValorTotalFatura(), 4, BigDecimal.ROUND_HALF_UP).multiply(
                                    BigDecimal.valueOf(100)));
                    lancamentoCalculado.setDadosTabelaValorProcComPercItem(String.format("R$ %.2f",
                            fatura.getItensFiltered().get(0).getValorComDesconto().doubleValue()) + " (" + String.format("%.2f%%", lancamentoCalculado.getDadosTabelaPercItem().doubleValue()) + ")");
                    lancamentoCalculado.setDadosTabelaValorTotalCustosDiretos(
                            PlanoTratamentoProcedimentoCustoSingleton.getInstance().getCustoDiretoFromPTP(lancamentoCalculado.getPtp()).setScale(2, BigDecimal.ROUND_HALF_UP));
                    lancamentoCalculado.setDadosTabelaValorRecebidoComFormaPagto(String.format("R$ %.2f", lancamentoParaCalculo.getValor()) + " " + lancamentoParaCalculo.getFormaPagamentoStr());
                    lancamentoCalculado.setDadosTabelaValorTaxasETarifas(
                            LancamentoContabilSingleton.getInstance().getTaxasAndTarifasForLancamentoRecebimento(lancamentoParaCalculo).setScale(2, BigDecimal.ROUND_HALF_UP));
                    if (Profissional.PORCENTAGEM.equals(ptp.getDentistaExecutor().getTipoRemuneracao())) {
                        lancamentoCalculado.setDadosTabelaValorRepasse(String.format("%.2f%%", repasseFaturas.getValorCalculo()));
                        lancamentoCalculado.setDadosTabelaMetodoRepasse("POR");
                    } else if (Profissional.PROCEDIMENTO.equals(ptp.getDentistaExecutor().getTipoRemuneracao())) {
                        BigDecimal valorRepasse = ConvenioProcedimentoSingleton.getInstance().getCheckValorConvenio(lancamentoCalculado.getPtp());
                        lancamentoCalculado.setDadosTabelaValorRepasse(String.format("R$ %.2f", valorRepasse.doubleValue()));
                        lancamentoCalculado.setDadosTabelaMetodoRepasse("PRO");
                    }
                    lancamentoCalculado.setDadosTabelaMetodoRepasseComValor(lancamentoCalculado.getDadosTabelaMetodoRepasse() + " - " + lancamentoCalculado.getDadosTabelaValorRepasse());

                    // Calculos necessários para mostragem da explicação do cálculo
                    lancamentoCalculado.setDadosCalculoPercLancamentoFatura(lancamentoParaCalculo.getValor().divide(lancamentoCalculado.getDadosTabelaValorTotalFatura(), 4, BigDecimal.ROUND_HALF_UP));
                    lancamentoCalculado.setDadosCalculoPercTaxa(
                            (lancamentoParaCalculo.getTarifa() != null ? lancamentoParaCalculo.getTarifa().getTaxa().divide(BigDecimal.valueOf(100)) : BigDecimal.ZERO));
                    lancamentoCalculado.setDadosCalculoValorTaxa(lancamentoCalculado.getDadosCalculoPercTaxa().multiply(lancamentoParaCalculo.getValor()));
                    lancamentoCalculado.setDadosCalculoValorTarifa((lancamentoParaCalculo.getTarifa() != null ? lancamentoParaCalculo.getTarifa().getTarifa() : BigDecimal.ZERO));
                    lancamentoCalculado.setDadosCalculoPercTributo(lancamentoParaCalculo.getTributoPerc());
                    lancamentoCalculado.setDadosCalculoValorTributo(lancamentoParaCalculo.getTributoPerc().multiply(lancamentoParaCalculo.getValor()).setScale(2, BigDecimal.ROUND_HALF_UP));
                    lancamentoCalculado.setDadosCalculoPercCustoDireto(
                            lancamentoCalculado.getDadosTabelaValorTotalCustosDiretos().divide(lancamentoCalculado.getDadosTabelaValorTotalFatura(), 4, BigDecimal.ROUND_HALF_UP));
                    lancamentoCalculado.setDadosCalculoValorCustoDiretoRateado(
                            lancamentoCalculado.getDadosTabelaValorTotalCustosDiretos().divide(lancamentoCalculado.getDadosTabelaValorTotalFatura(), 4, BigDecimal.ROUND_HALF_UP).multiply(
                                    lancamentoParaCalculo.getValor()));
                    lancamentoCalculado.setDadosCalculoTotalReducao(
                            lancamentoCalculado.getDadosCalculoValorTaxa().add(lancamentoCalculado.getDadosCalculoValorTarifa()).add(lancamentoCalculado.getDadosCalculoValorTributo()));
                    lancamentoCalculado.setDadosCalculoRecebidoMenosReducao(
                            lancamentoParaCalculo.getValor().subtract(lancamentoCalculado.getDadosCalculoTotalReducao()).setScale(2, BigDecimal.ROUND_HALF_UP));

                    lancamentoCalculado.setDadosCalculoValorItemSemCusto(
                            fatura.getItensFiltered().get(0).getValorComDesconto().subtract(lancamentoCalculado.getDadosTabelaValorTotalCustosDiretos()).setScale(2, BigDecimal.ROUND_HALF_UP));
                    lancamentoCalculado.setDadosCalculoPercItemSemCusto(
                            lancamentoCalculado.getDadosCalculoValorItemSemCusto().divide(lancamentoCalculado.getDadosTabelaValorTotalFatura(), 4, BigDecimal.ROUND_HALF_UP));
                    lancamentoCalculado.setDadosCalculoInvPercItemSemCusto(BigDecimal.valueOf(1).subtract(lancamentoCalculado.getDadosCalculoPercItemSemCusto()));
                    lancamentoCalculado.setDadosCalculoReducaoCustoDireto(
                            lancamentoCalculado.getDadosCalculoRecebidoMenosReducao().multiply(lancamentoCalculado.getDadosCalculoInvPercItemSemCusto()).setScale(2, BigDecimal.ROUND_HALF_UP));

                    lancamentoCalculado.setDadosCalculoValorARepassarSemCusto(
                            lancamentoCalculado.getDadosCalculoRecebidoMenosReducao().subtract(lancamentoCalculado.getDadosCalculoReducaoCustoDireto()).setScale(2, BigDecimal.ROUND_HALF_UP));
                    lancamentosCalculados.add(lancamentoCalculado);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }

        if (lancamentosCalculados != null && lancamentosCalculados.size() > 0) {
            PrimeFaces.current().executeScript("PF('dlgCalculoRepasse').show();");
        } else {
            this.addError("Erro", Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), true);
        }

    }

    public void verificaPendencias(PlanoTratamentoProcedimento ptp) {
        pendencias = new ArrayList<String>();

        if (ptp.getPlanoTratamento().getRegistroAntigo() != null && ptp.getPlanoTratamento().getRegistroAntigo().equals("S")) {
            pendencias.add("Plano de tratamento criado no modelo antigo. realizar o repasse na aba de Repasse Antigo.");
        }

        if (this.validaConferenciaCustosDiretos && (ptp.getCustoDiretoValido() == null || ptp.getCustoDiretoValido().equals("N"))) {
            pendencias.add("Custos diretos ainda não conferidos;");
        }
        if (this.validaExecucaoProcedimento && (ptp.getDataFinalizado() == null || ptp.getDentistaExecutor() == null)) {
            pendencias.add("Procedimento ainda não executado;");
        }
        if (this.validaPagamentoPaciente) {
            if (ptp.getOrcamentoProcedimentos() == null || ptp.getOrcamentoProcedimentos().size() == 0 || ptp.getOrcamentoProcedimentos().get(
                    0).getOrcamentoItem() == null || ptp.getOrcamentoProcedimentos().get(
                            0).getOrcamentoItem().getOrcamento() == null || !ptp.getOrcamentoProcedimentos().get(0).getOrcamentoItem().getOrcamento().isAprovado()) {
                pendencias.add("Não existe orçamento aprovado para o paciente;");
            }
        }
        if (this.validaPagamentoPaciente && getFaturaFromPtp(ptp) == null) {
            pendencias.add("Paciente ainda não pagou o procedimento;");
        }

        if (pendencias == null || pendencias.size() == 0) {
            pendencias.add("Sem pendência!");
        }

    }

    public boolean existemPendencias(PlanoTratamentoProcedimento ptp) {

        try {
            if (this.validaConferenciaCustosDiretos && (ptp.getCustoDiretoValido() == null || ptp.getCustoDiretoValido().equals("N"))) {
                return true;
            }
            if (this.validaExecucaoProcedimento && (ptp.getDataFinalizado() == null || ptp.getDentistaExecutor() == null)) {
                return true;
            }
            //TODO verificar esse caso
            if (this.validaPagamentoPaciente && getFaturaFromPtp(ptp) == null) {
                return true;
            }

        } catch (Exception e) {
            this.addError("Erro", Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), true);
            LogIntelidenteSingleton.getInstance().makeLog(e);
            e.printStackTrace();
        }

        return false;
    }

    public BigDecimal valorTotal(PlanoTratamentoProcedimento ptp) {
        return FaturaSingleton.getInstance().getTotal(getFaturaFromPtp(ptp));
    }

    public BigDecimal valorPago(PlanoTratamentoProcedimento ptp) {
        if (getFaturaFromPtp(ptp) != null) {
            return FaturaSingleton.getInstance().getTotalPago(getFaturaFromPtp(ptp));
        }
        return new BigDecimal(0);
    }

    public BigDecimal valorDisponivel(PlanoTratamentoProcedimento ptp) {
        if (!existemPendencias(ptp)) {
            return valorTotal(ptp).subtract(valorPago(ptp));
        }
        return new BigDecimal(0);
    }

    public Fatura getFaturaFromPtp(PlanoTratamentoProcedimento ptp) {
        if (ptp.getRepasseFatura() != null && ptp.getRepasseFatura().getFaturaRepasse() != null) {
            return ptp.getRepasseFatura().getFaturaRepasse();
        } else {
            //repasse antigo, quando ainda nao tinha ptp no repasse fatura
            RepasseFaturasItem repasseFaturasItem = RepasseFaturasItemSingleton.getInstance().getBo().getItemOrigemFromRepasse(ptp);
            if (repasseFaturasItem == null || repasseFaturasItem.getFaturaItemRepasse() == null || repasseFaturasItem.getFaturaItemRepasse().getFatura() == null) {
                return null;
            }
            return repasseFaturasItem.getFaturaItemRepasse().getFatura();
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

    public void exportarTabela(String type) {
        exportarTabela("Repasse dos profissionais", tabelaRepasse, type);
    }

    public Profissional getProfissional() {
        return profissional;
    }

    public void setProfissional(Profissional profissional) {
        this.profissional = profissional;
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

    public DataTable getTabelaRepasse() {
        return tabelaRepasse;
    }

    public void setTabelaRepasse(DataTable tabelaRepasse) {
        this.tabelaRepasse = tabelaRepasse;
    }

//    public String getStatus() {
//        return status;
//    }
//
//    public void setStatus(String status) {
//        this.status = status;
//    }

    public boolean isSemPendencias() {
        return semPendencias;
    }

    public void setSemPendencias(boolean semPendencias) {
        this.semPendencias = semPendencias;
    }

    public String getFiltroPeriodo() {
        return filtroPeriodo;
    }

    public void setFiltroPeriodo(String filtroPeriodo) {
        this.filtroPeriodo = filtroPeriodo;
    }

    public boolean isProcedimentosNaoExecutados() {
        return procedimentosNaoExecutados;
    }

    public void setProcedimentosNaoExecutados(boolean procedimentosNaoExecutados) {
        this.procedimentosNaoExecutados = procedimentosNaoExecutados;
    }

    public boolean isValidaPagamentoPaciente() {
        return validaPagamentoPaciente;
    }

    public void setValidaPagamentoPaciente(boolean validaPagamentoPaciente) {
        this.validaPagamentoPaciente = validaPagamentoPaciente;
    }

    public boolean isValidaConferenciaCustosDiretos() {
        return validaConferenciaCustosDiretos;
    }

    public void setValidaConferenciaCustosDiretos(boolean validaConferenciaCustosDiretos) {
        this.validaConferenciaCustosDiretos = validaConferenciaCustosDiretos;
    }

    public boolean isValidaExecucaoProcedimento() {
        return validaExecucaoProcedimento;
    }

    public void setValidaExecucaoProcedimento(boolean validaExecucaoProcedimento) {
        this.validaExecucaoProcedimento = validaExecucaoProcedimento;
    }

    public List<String> getPendencias() {
        return pendencias;
    }

    public void setPendencias(List<String> pendencias) {
        this.pendencias = pendencias;
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

    public List<PlanoTratamentoProcedimento> getPtpsSelecionados() {
        return ptpsSelecionados;
    }

    public void setPtpsSelecionados(List<PlanoTratamentoProcedimento> ptpsSelecionados) {
        this.ptpsSelecionados = ptpsSelecionados;
    }

    public List<Profissional> getProfissionaisRecibo() {
        return profissionaisRecibo;
    }

    public void setProfissionaisRecibo(List<Profissional> profissionaisRecibo) {
        this.profissionaisRecibo = profissionaisRecibo;
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

    public ReciboRepasseProfissional getReciboRepasseProfissional() {
        return reciboRepasseProfissional;
    }

    public void setReciboRepasseProfissional(ReciboRepasseProfissional reciboRepasseProfissional) {
        this.reciboRepasseProfissional = reciboRepasseProfissional;
    }

    public void setLancamentosCalculados(List<Lancamento> lancamentosCalculados) {
        this.lancamentosCalculados = lancamentosCalculados;
    }

    public List<Lancamento> getLancamentosCalculados() {
        return lancamentosCalculados;
    }

    public List<ReciboRepasseProfissional> getListaRecibos() {
        return listaRecibos;
    }

    public void setListaRecibos(List<ReciboRepasseProfissional> listaRecibos) {
        this.listaRecibos = listaRecibos;
    }

    public List<Lancamento> getLancamentoParaRecibo() {
        return lancamentoParaRecibo;
    }

    public void setLancamentoParaRecibo(List<Lancamento> lancamentoParaRecibo) {
        this.lancamentoParaRecibo = lancamentoParaRecibo;
    }

    public boolean isMesesAnteriores() {
        return mesesAnteriores;
    }

    public void setMesesAnteriores(boolean mesesAnteriores) {
        this.mesesAnteriores = mesesAnteriores;
    }

    public boolean isMostrarRepasseAntigo() {
        return mostrarRepasseAntigo;
    }

    public void setMostrarRepasseAntigo(boolean mostrarRepasseAntigo) {
        this.mostrarRepasseAntigo = mostrarRepasseAntigo;
    }

    public Object getOneValue() {
        return Arrays.asList(new Object());
    }

}
