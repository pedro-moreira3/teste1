package br.com.lume.odonto.managed;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.AjaxBehaviorEvent;

import org.primefaces.PrimeFaces;
import org.primefaces.component.datatable.DataTable;

import br.com.lume.common.log.LogIntelidenteSingleton;
import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.common.util.UtilsPadraoRelatorio.PeriodoBusca;
import br.com.lume.convenioProcedimento.ConvenioProcedimentoSingleton;
import br.com.lume.faturamento.FaturaItemSingleton;
import br.com.lume.faturamento.FaturaSingleton;
import br.com.lume.lancamentoContabil.LancamentoContabilSingleton;
import br.com.lume.odonto.entity.Fatura;
import br.com.lume.odonto.entity.Fatura.TipoLancamentos;
import br.com.lume.odonto.entity.FaturaItem;
import br.com.lume.odonto.entity.Lancamento;
import br.com.lume.odonto.entity.PlanoTratamentoProcedimento;
import br.com.lume.odonto.entity.Profissional;
import br.com.lume.odonto.entity.ProfissionalDiaria;
import br.com.lume.odonto.entity.ReciboRepasseProfissional;
import br.com.lume.odonto.entity.ReciboRepasseProfissionalLancamento;
import br.com.lume.odonto.entity.RepasseFaturas;
import br.com.lume.odonto.entity.RepasseFaturasItem;
import br.com.lume.odonto.entity.RepasseFaturasLancamento;
import br.com.lume.orcamento.OrcamentoSingleton;
import br.com.lume.planoTratamentoProcedimento.PlanoTratamentoProcedimentoSingleton;
import br.com.lume.planoTratamentoProcedimentoCusto.PlanoTratamentoProcedimentoCustoSingleton;
import br.com.lume.profissional.ProfissionalDiariaSingleton;
import br.com.lume.profissional.ProfissionalSingleton;
import br.com.lume.repasse.ReciboRepasseProfissionalDiariaSingleton;
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
/**
 * @author ricardo.poncio
 *
 */
@ManagedBean
@ViewScoped
public class RepasseProfissionalComReciboMB extends LumeManagedBean<PlanoTratamentoProcedimento> {

    private static final long serialVersionUID = 1L;
    //private Logger log = Logger.getLogger(FaturaPagtoMB.class);

    private boolean semPendencias = false, procedimentosNaoExecutados = false, mostrarRepasseAntigo = false;

    //FILTROS
    private Date dataInicio, dataInicioDiaria;
    private Date dataFim, dataFimDiaria;
    private Profissional profissional, profissionalDiaria;

    private String filtroPeriodo = "MA";
    private PeriodoBusca filtroPeriodoDiaria = PeriodoBusca.MES_ATUAL;

    private List<String> pendencias = new ArrayList<String>();

    private boolean validaPagamentoPacienteOrtodontico;
    private boolean validaPagamentoPaciente;
    private boolean validaConferenciaCustosDiretos;
    private boolean validaExecucaoProcedimento;

    private List<PlanoTratamentoProcedimento> ptpsSelecionados;
    
    private List<ProfissionalDiaria> diarias, diariasSelecionadas;

    private String descricao, observacao;

    //EXPORTAÇÃO TABELA
    private DataTable tabelaRepasse, tabelaRepasseDiaria;

    private List<Profissional> profissionaisRecibo;
    private HashMap<Profissional, Integer> profissionaisReciboLancamentos;
    private HashMap<Profissional, Double> profissionaisReciboValores;
    private HashMap<PlanoTratamentoProcedimento, List<Lancamento>> ptpsValidosComLancamentos = new HashMap<PlanoTratamentoProcedimento, List<Lancamento>>();
    private List<Lancamento> lancamentosCalculados;

    private ReciboRepasseProfissional reciboRepasseProfissional;
    private List<ReciboRepasseProfissional> listaRecibos;

    private List<Lancamento> lancamentoParaRecibo;
    private List<ProfissionalDiaria> diariaParaRecibo;
    private BigDecimal valorTotalFatura = new BigDecimal(0);
    private BigDecimal valorProcedimento = new BigDecimal(0);
    private BigDecimal valorBaseRepasse = new BigDecimal(0);
    private List<Lancamento> lancamentosDeOrigem;

    private BigDecimal valorRepassar = new BigDecimal(0);

    private boolean agendarParaData = false;

    private Date dataValorRestante;

    private boolean ignorarRestante = false;

    private String justificativa;
    private RepasseFaturas repasseFatura;

    private Lancamento lancamentoDeducoes;

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
            validaPagamentoPacienteOrtodontico = empresa.getValidarRepasseLancamentoOrigemValidadoOrtodonticoBool();
            validaConferenciaCustosDiretos = empresa.getValidarRepasseConfereCustoDiretoBool();
            validaExecucaoProcedimento = empresa.getValidarRepasseProcedimentoFinalizadoBool();

            actionTrocaDatas(null);
            actionTrocaDatasDiaria(null);

            ////List<ReciboRepasseProfissionalLancamento> rrpls = ReciboRepasseProfissionalLancamentoSingleton.getInstance().getBo().findReciboLancamentoInvalido();
            //List<ReciboRepasseProfissionalLancamento> rrpls = ReciboRepasseProfissionalLancamentoSingleton.getInstance().getBo().listAll();
            //for (ReciboRepasseProfissionalLancamento rrpl : rrpls) {
            //    rrpl.setDados(new ReciboRepasseProfissionalLancamentoDadosImutaveis(rrpl));
            //    ReciboRepasseProfissionalLancamentoDadosImutaveisSingleton.getInstance().getBo().persist(rrpl.getDados());
            //}
            //ReciboRepasseProfissionalLancamentoSingleton.getInstance().getBo().mergeBatch(rrpls);
        } catch (Exception e) {
            LogIntelidenteSingleton.getInstance().makeLog(e);
            this.addError("Erro", "Não foi possivel carregar a tela.", true);
        }
        //System.out.println("RepasseProfissionalComReciboMB" + new Timestamp(System.currentTimeMillis()));
    }
    
    public void prepararReciboDiaria() {
        this.descricao = null;
        this.observacao = null;

        this.profissionaisReciboLancamentos = new HashMap<>();
        this.profissionaisReciboValores = new HashMap<>();
        this.profissionaisRecibo = new ArrayList<>();
//
        if (diariasSelecionadas == null || diariasSelecionadas.size() == 0) {
            addError("Erro", "É necessário escolher ao menos uma diária.");
            return;
        }
        diariaParaRecibo = new ArrayList<>();
        for (ProfissionalDiaria diaria : diariasSelecionadas) {
            Profissional dentista = diaria.getProfissional();
            if (getProfissionaisRecibo() == null || getProfissionaisRecibo().indexOf(dentista) < 0) {
                this.profissionaisReciboLancamentos.put(dentista, new Integer(1));
                this.profissionaisReciboValores.put(dentista, diaria.getValorDiaria().doubleValue());
                this.profissionaisRecibo.add(dentista);
            } else {
                this.profissionaisReciboLancamentos.put(dentista, this.profissionaisReciboLancamentos.get(dentista) + 1);
                this.profissionaisReciboValores.put(dentista, this.profissionaisReciboValores.get(dentista) + diaria.getValorDiaria().doubleValue());
            }
            diariaParaRecibo.add(diaria);
        }

        PrimeFaces.current().executeScript("PF('dlgGerarRecibo').show()");
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
            if (!existemPendencias(ptp) || ptp.getFatura().getTipoLancamentos() == TipoLancamentos.MANUAL) {
                //   Fatura fatura = ptp.getFatura();
                //  if (fatura != null) {
                //  List<Lancamento> lancamentos = fatura.getLancamentos();
                Profissional dentistaExecutor = ptp.getDentistaExecutor();
                for (Lancamento lancamento : ptpsValidosComLancamentos.get(ptp)) {
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
                //   }
            }
        }

        PrimeFaces.current().executeScript("PF('dlgGerarRecibo').show()");
    }

    public void cancelarRecibo() {
        setPtpsSelecionados(null);
        setLancamentoParaRecibo(null);
        setDiariaParaRecibo(null);
    }
    
    public void gerarRecibo() {
        if(getLancamentoParaRecibo() != null)
            gerarReciboLancamento();
        else if(getDiariaParaRecibo() != null)
            gerarReciboDiaria();
    }

    private void gerarReciboDiaria() {
        try {
            for(ProfissionalDiaria diaria: diariaParaRecibo) {
                if (ReciboRepasseProfissionalDiariaSingleton.getInstance().getBo().findReciboValidoForDiaria(diaria) != null) {
                    addError("Erro", "Existem lançamentos na lista de repasse que já estão em outros recibos!");
                    return;
                }
            }
            if (!ReciboRepasseProfissionalSingleton.getInstance().gerarReciboForDiaria(diariaParaRecibo, this.descricao, this.observacao, UtilsFrontEnd.getProfissionalLogado()))
                throw new Exception();
            setDiariasSelecionadas(null);
            PrimeFaces.current().executeScript("PF('dlgGerarRecibo').hide()");
            addInfo("Sucesso", Mensagens.getMensagemOffLine(Mensagens.REGISTRO_SALVO_COM_SUCESSO));
            
            pesquisarDiaria();
        } catch (Exception e) {
            addError("Erro", Mensagens.getMensagemOffLine(Mensagens.ERRO_AO_SALVAR_REGISTRO));
        }
    }
    
    private void gerarReciboLancamento() {
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
                // pesquisar();
                setPtpsSelecionados(null);
                PrimeFaces.current().executeScript("PF('dlgGerarRecibo').hide()");
                addInfo("Sucesso", Mensagens.getMensagemOffLine(Mensagens.REGISTRO_SALVO_COM_SUCESSO));
            }
        } catch (Exception e) {
            addError("Erro", Mensagens.getMensagemOffLine(Mensagens.ERRO_AO_SALVAR_REGISTRO));
        }
    }

//    public void aprovarRecibo(ReciboRepasseProfissional r) {
//        try {
//            ReciboRepasseProfissionalSingleton.getInstance().aprovarRecibo(r, UtilsFrontEnd.getProfissionalLogado());
//            addInfo("Sucesso", Mensagens.getMensagemOffLine(Mensagens.REGISTRO_SALVO_COM_SUCESSO));
//        } catch (Exception e) {
//            addError("Erro!", e.getMessage());
//        }
//        // pesquisar();
//    }

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
    
    public void actionTrocaDatasDiaria(AjaxBehaviorEvent event) {
        try {
            this.dataInicioDiaria = getDataInicio(filtroPeriodoDiaria.getRotulo());
            this.dataFimDiaria = getDataFim(filtroPeriodoDiaria.getRotulo());
        } catch (Exception e) {
            LogIntelidenteSingleton.getInstance().makeLog(e);
            this.addError("Erro", Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS));
        }
    }

    public void pesquisarDiaria() {
        try {
            setDiarias(ProfissionalDiariaSingleton.getInstance()
                    .getBo().listByFields(UtilsFrontEnd.getEmpresaLogada(), 
                            profissionalDiaria, dataInicioDiaria, dataFimDiaria));
            getDiarias().stream().forEach(diaria -> diaria.loadPTPsDiaria());
        } catch (Exception e) {
            e.printStackTrace();
            LogIntelidenteSingleton.getInstance().makeLog(e);
            this.addError("Erro", Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), true);
        }
    }
    
    public void pesquisar() {
        try {
            setEntityList(new ArrayList<PlanoTratamentoProcedimento>());
            setEntityList(PlanoTratamentoProcedimentoSingleton.getInstance().getBo().listParaRepasseProfissionais(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa(), dataInicio, dataFim,
                    profissional, procedimentosNaoExecutados, mostrarRepasseAntigo));

//            if (semPendencias && getEntityList() != null && getEntityList().size() > 0) {
//                //getEntityList().removeIf(ptp -> (existemPendencias(ptp) || valorDisponivel(ptp).compareTo(new BigDecimal(0)) == 0));
//                List<PlanoTratamentoProcedimento> semPendencias = new ArrayList<PlanoTratamentoProcedimento>();
//                semPendencias.addAll(getEntityList());
//                for (PlanoTratamentoProcedimento ptp : getEntityList()) {
//                    if (existemPendencias(ptp) || valorDisponivel(ptp).compareTo(new BigDecimal(0)) == 0) {
//                        semPendencias.remove(ptp);
//                    }
//                }
//                setEntityList(semPendencias);
//            }            

            //novaLista.addAll(getEntityList());
            if (getEntityList() != null && getEntityList().size() > 0) {

                List<PlanoTratamentoProcedimento> removerPtp = new ArrayList<PlanoTratamentoProcedimento>();
                List<PlanoTratamentoProcedimento> novaLista = new ArrayList<PlanoTratamentoProcedimento>();
                for (PlanoTratamentoProcedimento ptp : getEntityList()) {
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
                    if (ptp.getRepasseFaturas() != null) {
                        BigDecimal valorTotal = new BigDecimal(0);
                        BigDecimal valorPago = new BigDecimal(0);
                        for (RepasseFaturas repasse : ptp.getRepasseFaturas()) {
                            if (repasse.getFaturaRepasse().isAtivo()) {
                                valorTotal = FaturaSingleton.getInstance().getTotal(repasse.getFaturaRepasse());
                                valorPago = FaturaSingleton.getInstance().getTotalPago(repasse.getFaturaRepasse());
                            }
                        }

                        if ((valorTotal.compareTo(valorPago) == 0 && valorTotal.compareTo(new BigDecimal(0)) != 0) || (ptp.getDentistaExecutor().getTipoRemuneracao().equals(Profissional.FIXO))) {
                            removerPtp.add(ptp);
                            continue;
                        }

                    }
                    if (ptp.getFatura() != null) {
                        ptp.setValorTotal(FaturaSingleton.getInstance().getTotal(ptp.getFatura()));

                        FaturaSingleton.getInstance().getTotalPago(ptp.getFatura());

                        ptp.setValorPago(FaturaSingleton.getInstance().getTotalPago(ptp.getFatura()));

                        //TODO ESSA CONTA DO VALOR DISPONIVEL DEVE ESTAR EM UM SINGLETON, NAO AQUI
                        if (ptp.getFatura().getTipoLancamentos() == TipoLancamentos.AUTOMATICO) {
                            //ptp.setValorDisponivel(ptp.getValorTotal().subtract(ptp.getValorPago()));  
                            // ptp.setValorDisponivel(new BigDecimal(FaturaSingleton.getInstance().getValorNaoPagoDisponivel(ptp.getFatura())));
                            BigDecimal valorDisponivel = new BigDecimal(0);
                            ptpsValidosComLancamentos.put(ptp, new ArrayList<Lancamento>());
                            for (Lancamento lancamento : ptp.getFatura().getLancamentos()) {
                                if ((validaPagamentoPaciente && !ptp.getPlanoTratamento().isOrtodontico()) || (this.validaPagamentoPacienteOrtodontico && ptp.getPlanoTratamento().isOrtodontico())) {
                                    if (lancamento.isAtivo() && lancamento.getValidado().equals("N")) {
                                        RepasseFaturasLancamento rfl = RepasseFaturasLancamentoSingleton.getInstance().getBo().getFaturaRepasseLancamentoFromLancamentoRepasse(lancamento);
                                        if (rfl != null && rfl.getLancamentoOrigem() != null) {
                                            Lancamento lancamentoDeOrigem = rfl.getLancamentoOrigem();
                                            if (lancamentoDeOrigem != null && lancamentoDeOrigem.getValidadoPorProfissional() != null) {
                                                valorDisponivel = valorDisponivel.add(lancamento.getValor());
                                                ptpsValidosComLancamentos.get(ptp).add(lancamento);
                                            }
                                        }

                                    }
                                } else {
                                    if (lancamento.isAtivo() && lancamento.getValidado().equals("N")) {
                                        valorDisponivel = valorDisponivel.add(lancamento.getValor());

                                        ptpsValidosComLancamentos.get(ptp).add(lancamento);
                                    }
                                }

                            }
                            ptp.setValorDisponivel(valorDisponivel);
                        } else {
                            ptpsValidosComLancamentos.put(ptp, new ArrayList<Lancamento>());
                            for (Lancamento lancamento : ptp.getFatura().getLancamentos()) {
                                ptpsValidosComLancamentos.get(ptp).add(lancamento);
                            }
                            ptp.setValorDisponivel(FaturaSingleton.getInstance().getTotalNaoPago(ptp.getFatura()));
                        }
                    }
                    if (ptp.getValorTotal() == null) {
                        ptp.setValorTotal(new BigDecimal(0));
                    }
                    if (ptp.getValorDisponivel() == null) {
                        ptp.setValorDisponivel(new BigDecimal(0));
                    }
                    if (ptp.getFatura() != null && ptp.getFatura().getTipoLancamentos() == TipoLancamentos.MANUAL && (ptp.getValorDisponivel() == null || ptp.getValorDisponivel().compareTo(
                            new BigDecimal(0)) == 0) && ptp.getFatura().isValorRestanteIgnoradoAjusteManual()) {
                        removerPtp.add(ptp);
                        continue;
                    } else if (ptp.getFatura() != null && ptp.getFatura().getTipoLancamentos() == TipoLancamentos.MANUAL && ptp.getValorPago().compareTo(ptp.getValorTotal()) >= 0) {
                        //nesse caso provavelmente de problema no valor do calculo, entao estou mostrando pra nao perder informacao
                        if (ptp.getValorTotal().compareTo(new BigDecimal(0)) != 0) {
                            removerPtp.add(ptp);
                        }
                        continue;
                    }

                    novaLista.add(ptp);
                }

                this.getEntityList().removeIf(p -> removerPtp.contains(p));

                //precisa deixar apenas ptp sem pendencias
                //pensar uma maneira melhor de tratar isso
                for (PlanoTratamentoProcedimento ptp : novaLista) {
                    if (semPendencias && existemPendencias(ptp)) {
                        getEntityList().remove(ptp);
                    }
                    if (semPendencias && ptp.getValorDisponivel().equals(new BigDecimal(0))) {
                        getEntityList().remove(ptp);
                    }
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
            LogIntelidenteSingleton.getInstance().makeLog(e);
            this.addError("Erro", Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), true);
        }
    }

    public boolean verificaRepasseNaoPago(Profissional profissionalRecibo) {
        return ReciboRepasseProfissionalSingleton.getInstance().getBo().existeReciboEmAberto(profissionalRecibo);
    }

    public void faturaManual(PlanoTratamentoProcedimento ptp) {
        try {

            if (ptp.getPlanoTratamento().getRegistroAntigo() != null && ptp.getPlanoTratamento().getRegistroAntigo().equals("S")) {
                this.addError("Erro", "Plano de tratamento criado no modelo antigo. realizar o repasse na aba de Repasse Antigo.", true);
                return;
            }
            if (ptp.getDentistaExecutor() != null && Profissional.FIXO.equals(ptp.getDentistaExecutor().getTipoRemuneracao())) {
                this.addError("Erro", "Dentista executor com tipo de remuneração fixa");
                return;
            }
            if (ptp.getFatura() != null && ptp.getFatura().getLancamentos() != null) {
                for (Lancamento l : ptp.getFatura().getLancamentos()) {
                    ReciboRepasseProfissionalLancamento rrpl = ReciboRepasseProfissionalLancamentoSingleton.getInstance().getBo().findReciboValidoForLancamento(l);
                    if (rrpl != null && rrpl.getRecibo().getAprovado().equals("S")) {
                        this.addError("Erro", "Não é permitido o recálculo para procedimentos com recibos já aprovados.");
                        return;
                    }
                }
            }

            RepasseFaturasSingleton.getInstance().recalculaRepasse(ptp, ptp.getDentistaExecutor(), UtilsFrontEnd.getProfissionalLogado(), ptp.getFatura());
            addInfo("Sucesso", "Repasse recalculado!");
            this.pesquisar();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void mostraAjusteManual(PlanoTratamentoProcedimento ptp) {
        try {
            setEntity(ptp);

            ignorarRestante = false;
            if (ptp.getFatura() != null && ptp.getFatura().isValorRestanteIgnoradoAjusteManual()) {
                ignorarRestante = true;
            }

            agendarParaData = false;
            dataValorRestante = null;
            justificativa = null;
            Fatura fatura = ptp.getFatura();
            // FaturaItem faturaItem = null;

            //  faturaItem = FaturaItemSingleton.getInstance().getBo().faturaItensFromPTP(ptp);

            //se a fatura ainda nao tem lancamentos, mas o profissional recebe por procedimento, ja sabemos o valor de repasse base.
            if (fatura == null || fatura.getLancamentos() == null || fatura.getLancamentos().size() == 0) {
                if (Profissional.PROCEDIMENTO.equals(ptp.getDentistaExecutor().getTipoRemuneracao())) {
                    setValorBaseRepasse(ConvenioProcedimentoSingleton.getInstance().getCheckValorConvenio(ptp));
                }
            }

            RepasseFaturasLancamento repasse = null;
            lancamentosDeOrigem = null;
            repasseFatura = null;
            if (fatura != null) {

                RepasseFaturas repasseFaturas = RepasseFaturasSingleton.getInstance().getRepasseFaturasComFaturaAtiva(ptp);

                if (fatura.getLancamentos() != null)
                    fatura.getLancamentos().sort(new Comparator<Lancamento>() {

                        @Override
                        public int compare(Lancamento o1, Lancamento o2) {
                            return Long.compare(o1.getId(), o2.getId());
                        }
                    });
                int cont = 0;
                for (Lancamento lancamento : fatura.getLancamentos()) {
                    repasse = RepasseFaturasLancamentoSingleton.getInstance().getBo().getFaturaRepasseLancamentoFromLancamentoRepasseDestino(lancamento);
                    if (repasse == null) {
                        repasse = RepasseFaturasLancamentoSingleton.getInstance().getBo().getFaturaRepasseFromRepasseFaturas(repasseFaturas);
                    }
                    if (repasseFaturas != null && repasseFaturas.getFaturaOrigem() != null) {
                        lancamentosDeOrigem = repasseFaturas.getFaturaOrigem().getLancamentos();
                    }

                    valorTotalFatura = new BigDecimal(0);
                    if (repasse != null && repasseFaturas != null && repasseFaturas.getFaturaOrigem() != null) {
                        valorTotalFatura = FaturaSingleton.getInstance().getTotal(repasseFaturas.getFaturaOrigem());
                    } else if (repasseFaturas.getFaturaOrigem() == null) {
                        valorTotalFatura = ptp.getPlanoTratamento().getValor();
                    } else {
                        valorTotalFatura = ptp.getOrcamentoProcedimentos().get(0).getOrcamentoItem().getOrcamento().getValorTotalComDesconto();
                    }

                    if (repasse != null && repasse.getFaturaItem() != null && repasse.getFaturaItem().getValorComDesconto() != null) {
                        valorProcedimento = repasse.getFaturaItem().getValorComDesconto();
                    } else {
                        valorProcedimento = ptp.getProcedimento().getValor();
                    }

                    repasseFatura = repasse.getRepasseFaturas();
                    if (Profissional.PORCENTAGEM.equals(ptp.getDentistaExecutor().getTipoRemuneracao())) {
                        setValorBaseRepasse(repasse.getRepasseFaturas().getValorCalculo());
                    } else if (Profissional.PROCEDIMENTO.equals(ptp.getDentistaExecutor().getTipoRemuneracao())) {
                        setValorBaseRepasse(ConvenioProcedimentoSingleton.getInstance().getCheckValorConvenio(ptp));
                    }

                    if (lancamentosDeOrigem != null)
                        lancamentosDeOrigem.sort(new Comparator<Lancamento>() {

                            @Override
                            public int compare(Lancamento o1, Lancamento o2) {
                                return Long.compare(o1.getId(), o2.getId());
                            }
                        });

                    if (lancamentosDeOrigem != null && cont < lancamentosDeOrigem.size()) {

                        lancamentosDeOrigem.get(cont).setDadosCalculoValorARepassarSemCusto(lancamento.getValor());

                        lancamentosDeOrigem.get(cont).setDadosCalculoValorTaxa(new BigDecimal(0));
                        lancamentosDeOrigem.get(cont).setDadosCalculoValorTarifa(new BigDecimal(0));
                        lancamentosDeOrigem.get(cont).setDadosCalculoValorTributo(new BigDecimal(0));

                        lancamentosDeOrigem.get(cont).setDadosCalculoPercTaxa(
                                (lancamentosDeOrigem.get(cont).getTarifa() != null && lancamentosDeOrigem.get(cont).getTarifa().getTaxa() != null ? lancamentosDeOrigem.get(
                                        cont).getTarifa().getTaxa().divide(BigDecimal.valueOf(100)) : BigDecimal.ZERO));
                        //  BigDecimal valorTarifa = lancamentosDeOrigem.get(cont).getDadosCalculoPercTaxa().multiply(lancamento.getValor());
                        lancamentosDeOrigem.get(cont).setDadosCalculoValorTaxa(lancamentosDeOrigem.get(cont).getDadosCalculoPercTaxa().multiply(lancamentosDeOrigem.get(cont).getValor()));
                        if (lancamentosDeOrigem.get(cont).getTarifa() != null && lancamentosDeOrigem.get(cont).getTarifa().getTarifa() != null) {
                            lancamentosDeOrigem.get(cont).setDadosCalculoValorTarifa(lancamentosDeOrigem.get(cont).getTarifa().getTarifa());
                        }
                        lancamentosDeOrigem.get(cont).setDadosCalculoPercTributo(lancamentosDeOrigem.get(cont).getTributoPerc());
                        lancamentosDeOrigem.get(cont).setDadosCalculoValorTributo(
                                lancamentosDeOrigem.get(cont).getTributoPerc().multiply(lancamentosDeOrigem.get(cont).getValor()).setScale(2, BigDecimal.ROUND_HALF_UP));

                        lancamentosDeOrigem.get(cont).setDadosTabelaValorTotalCustosDiretos(ptp.getCustos().setScale(2, BigDecimal.ROUND_HALF_UP));

                        if (lancamentosDeOrigem.get(cont).getDadosTabelaValorTotalCustosDiretos() != null && lancamentosDeOrigem.get(cont).getDadosTabelaValorTotalCustosDiretos().compareTo(
                                new BigDecimal(0)) != 0) {
                            lancamentosDeOrigem.get(cont).setDadosCalculoPercCustoDireto(
                                    lancamentosDeOrigem.get(cont).getDadosTabelaValorTotalCustosDiretos().divide(valorTotalFatura, 4, BigDecimal.ROUND_HALF_UP));
                            lancamentosDeOrigem.get(cont).setDadosCalculoValorCustoDiretoRateado(
                                    lancamentosDeOrigem.get(cont).getDadosTabelaValorTotalCustosDiretos().divide(valorTotalFatura, 4, BigDecimal.ROUND_HALF_UP).multiply(
                                            lancamentosDeOrigem.get(cont).getValor()));
                        } else {
                            lancamentosDeOrigem.get(cont).setDadosCalculoPercCustoDireto(new BigDecimal(0));
                            lancamentosDeOrigem.get(cont).setDadosCalculoValorCustoDiretoRateado(new BigDecimal(0));
                        }

                        lancamentosDeOrigem.get(cont).setDadosCalculoTotalReducao(
                                lancamentosDeOrigem.get(cont).getDadosCalculoValorTaxa().add(lancamentosDeOrigem.get(cont).getDadosCalculoValorTarifa()).add(
                                        lancamentosDeOrigem.get(cont).getDadosCalculoValorTributo()).add(lancamentosDeOrigem.get(cont).getDadosCalculoValorCustoDiretoRateado()));

                        lancamentosDeOrigem.get(cont).setDadosCalculoRecebidoMenosReducao(
                                lancamentosDeOrigem.get(cont).getValor().subtract(lancamentosDeOrigem.get(cont).getDadosCalculoTotalReducao()));

                    }
                    cont++;

                }
            }
            if (lancamentosDeOrigem != null) {
                for (Lancamento lancamento : lancamentosDeOrigem) {
                    lancamento.setDadosCalculoPercPagtoFaturaStr(String.format("R$ %.2f", lancamento.getValor()) + " (" + String.format("%.2f%%",
                            lancamento.getValor().divide(valorTotalFatura, 2).multiply(BigDecimal.valueOf(100))) + ")");
                }
            }

            setValorRepassar(ptp.getValorDisponivel());
            PrimeFaces.current().executeScript("PF('dlgAjusteManual').show();");
        } catch (Exception e) {
            e.printStackTrace();
            LogIntelidenteSingleton.getInstance().makeLog(e);
            this.addError("Erro", Mensagens.ERRO_AO_BUSCAR_REGISTROS, true);
        }
    }

    public void loadDeducoes(Lancamento lancamento) {
        this.lancamentoDeducoes = lancamento;
    }

    public void actionAjusteManual() {
        if (this.justificativa == null || this.justificativa.trim().equals("")) {
            this.addError("Erro", "Informe a justificativa", true);
        } else if (agendarParaData && dataValorRestante == null) {
            this.addError("Erro", "Informe a data do valor restante", true);
        } else {

            //TODO aqui criar a fatura manual quando essa nao existir
            //  criaRepasseManual

            if (getEntity().getFatura() == null) {
                Fatura novaFatura = RepasseFaturasSingleton.getInstance().criaRepasseManual(getEntity(), getEntity().getDentistaExecutor(), profissional);
                getEntity().setFatura(novaFatura);
                try {
                    PlanoTratamentoProcedimentoSingleton.getInstance().getBo().persist(getEntity());
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

            try {
                if (ignorarRestante) {
                    getEntity().getFatura().setValorRestanteIgnoradoAjusteManual(true);
                } else {
                    getEntity().getFatura().setValorRestanteIgnoradoAjusteManual(false);
                }

                FaturaSingleton.getInstance().novoLancamentoManualRepasse(getEntity(), getEntity().getFatura(), valorRepassar, new Date(), agendarParaData, dataValorRestante, ignorarRestante,
                        justificativa, UtilsFrontEnd.getProfissionalLogado());
                addInfo("Sucesso", "Ajuste manual executado!");
                pesquisar();
                PrimeFaces.current().executeScript("PF('dlgJustificativa').hide();");
                PrimeFaces.current().executeScript("PF('dlgAjusteManual').hide();");
                pesquisar();
            } catch (Exception e) {
                e.printStackTrace();
                this.addError("Erro", "Erro ao efetuar ajuste manual", true);
            }

        }

    }

    public void mostraCalculoRepasse(PlanoTratamentoProcedimento ptp) {

        //TODO hoje so mostra calculo quando tem  lancamento de origem, ou seja, quando empresa validar pagamento do paciente antes de receber.
        //if(validaPagamentoPaciente) {
        setEntity(ptp);
        Fatura fatura = ptp.getFatura();
        FaturaItem faturaItem = null;

        try {
            faturaItem = FaturaItemSingleton.getInstance().getBo().faturaItensFromPTP(ptp);
        } catch (Exception e) {
            // TODO: handle exception
        }

        List<Lancamento> lancamentos = null;
        if (fatura != null) {
            lancamentos = fatura.getLancamentos();
        }
        List<Lancamento> lancamentosTemp = new ArrayList<Lancamento>();
        lancamentosTemp.addAll(lancamentos);

        lancamentosCalculados = new ArrayList<Lancamento>();
        if (lancamentos != null && lancamentos.size() > 0) {

            for (Lancamento lancamento : lancamentos) {
                try {
                    Lancamento lancamentoCalculado = lancamento;
                    RepasseFaturasLancamento repasse = null;
                    FaturaItem itemOrigem = FaturaItemSingleton.getInstance().getBo().faturaItensOrigemFromPTP(ptp);

                    if (itemOrigem == null) {
                        addError("Cálculo", "Cálculo disponível somente após pagamento do paciente.");
                        return;
                    }

//                    ********** Código Removido pq esse getFaturaRepasseFromRepasseFaturas ta fazendo besteira
//                    ********** Dúvida fala com o poncio                    
//                    fatura.getRepassesOriginamEstaFatura();
//                    //verificando se conseguimos pegar pelo  ptp
//                    RepasseFaturas repasseFaturas = RepasseFaturasSingleton.getInstance().getBo().findByPtp(ptp);
//                    if (repasseFaturas != null) {
//                        repasse = RepasseFaturasLancamentoSingleton.getInstance().getBo().getFaturaRepasseFromRepasseFaturas(repasseFaturas);
//                    }

                    //se nao tem, vamos do jeito antigo:
                    //tentando pegar do lancamento de origem:                    
                    if (repasse == null) {
                        repasse = RepasseFaturasLancamentoSingleton.getInstance().getBo().getFaturaRepasseLancamentoFromLancamentoRepasseDestino(lancamentoCalculado);
                    }
                    //nao conseguimos achar repasse
                    if (repasse != null) {

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

                        if (repasse.getLancamentoOrigem() != null && repasse.getLancamentoOrigem().getFatura() != null) {
                            lancamentoCalculado.setDadosTabelaValorTotalFaturaOrigem(FaturaSingleton.getInstance().getTotal(repasse.getLancamentoOrigem().getFatura()));
                            lancamentoCalculado.setDadosTabelaValorTotalFatura(FaturaSingleton.getInstance().getTotal(fatura));
                        } else {
                            lancamentoCalculado.setDadosTabelaValorTotalFatura(FaturaSingleton.getInstance().getTotal(fatura));
                            addError("Cálculo", "Cálculo disponível somente após pagamento do paciente.");
                            return;
                        }

                        lancamentoCalculado.setDadosTabelaPercItem(repasse.getRepasseFaturas().getFaturaRepasse().getItensFiltered().get(0).getValorComDesconto().divide(
                                lancamentoCalculado.getDadosTabelaValorTotalFatura(), 4, BigDecimal.ROUND_HALF_UP).multiply(BigDecimal.valueOf(100)));
                        if (faturaItem != null)
                            lancamentoCalculado.setDadosTabelaValorProcComPercItem(String.format("R$ %.2f", faturaItem.getValorComDesconto().doubleValue()) + " (" + String.format("%.2f%%",
                                    faturaItem.getValorComDesconto().divide(lancamentoCalculado.getDadosTabelaValorTotalFaturaOrigem(), 2, BigDecimal.ROUND_HALF_UP)) + ")");
                        lancamentoCalculado.setDadosTabelaValorTotalCustosDiretos(
                                PlanoTratamentoProcedimentoCustoSingleton.getInstance().getCustoDiretoFromPTP(lancamentoCalculado.getPtp()).setScale(2, BigDecimal.ROUND_HALF_UP));
                        lancamentoCalculado.setDadosTabelaValorRecebidoComFormaPagto(String.format("R$ %.2f", lancamentoParaCalculo.getValor()) + " " + lancamentoParaCalculo.getFormaPagamentoStr());
                        lancamentoCalculado.setDadosTabelaValorTaxasETarifas(
                                LancamentoContabilSingleton.getInstance().getTaxasAndTarifasForLancamentoRecebimento(lancamentoParaCalculo).setScale(2, BigDecimal.ROUND_HALF_UP));
                        if (Profissional.PORCENTAGEM.equals(ptp.getDentistaExecutor().getTipoRemuneracao())) {
                            lancamentoCalculado.setDadosTabelaValorRepasse(String.format("%.2f%%", repasse.getRepasseFaturas().getValorCalculo()));
                            lancamentoCalculado.setDadosTabelaMetodoRepasse("POR");
                        } else if (Profissional.PROCEDIMENTO.equals(ptp.getDentistaExecutor().getTipoRemuneracao())) {
                            BigDecimal valorRepasse = ConvenioProcedimentoSingleton.getInstance().getCheckValorConvenio(lancamentoCalculado.getPtp());
                            lancamentoCalculado.setDadosTabelaValorRepasse(String.format("R$ %.2f", valorRepasse.doubleValue()));
                            lancamentoCalculado.setDadosTabelaMetodoRepasse("PRO");
                        }
                        lancamentoCalculado.setDadosTabelaMetodoRepasseComValor(lancamentoCalculado.getDadosTabelaMetodoRepasse() + " - " + lancamentoCalculado.getDadosTabelaValorRepasse());

                        // Calculos necessários para mostragem da explicação do cálculo
                        if (repasse.getLancamentoOrigem() != null && repasse.getLancamentoOrigem().getFatura() != null) {
                            lancamentoCalculado.setDadosCalculoPercLancamentoFatura(
                                    lancamentoParaCalculo.getValor().divide(lancamentoCalculado.getDadosTabelaValorTotalFaturaOrigem(), 4, BigDecimal.ROUND_HALF_UP));
                        } else {
                            lancamentoCalculado.setDadosCalculoPercLancamentoFatura(new BigDecimal(100));
                        }

                        lancamentoCalculado.setDadosCalculoPercTaxa(
                                (lancamentoParaCalculo.getTarifa() != null && lancamentoParaCalculo.getTarifa().getTaxa() != null ? lancamentoParaCalculo.getTarifa().getTaxa().divide(
                                        BigDecimal.valueOf(100)) : BigDecimal.ZERO));
                        lancamentoCalculado.setDadosCalculoValorTaxa(lancamentoCalculado.getDadosCalculoPercTaxa().multiply(lancamentoParaCalculo.getValor()));
                        lancamentoCalculado.setDadosCalculoValorTarifa((lancamentoParaCalculo.getTarifa() != null ? lancamentoParaCalculo.getTarifa().getTarifa() : BigDecimal.ZERO));
                        lancamentoCalculado.setDadosCalculoPercTributo(lancamentoParaCalculo.getTributoPerc());
                        lancamentoCalculado.setDadosCalculoValorTributo(lancamentoParaCalculo.getTributoPerc().multiply(lancamentoParaCalculo.getValor()).setScale(2, BigDecimal.ROUND_HALF_UP));
                        if (lancamentoCalculado.getDadosTabelaValorTotalCustosDiretos() != null && lancamentoCalculado.getDadosTabelaValorTotalCustosDiretos().compareTo(new BigDecimal(0)) != 0) {
                            lancamentoCalculado.setDadosCalculoPercCustoDireto(
                                    lancamentoCalculado.getDadosTabelaValorTotalCustosDiretos().divide(lancamentoCalculado.getDadosTabelaValorTotalFatura(), 4, BigDecimal.ROUND_HALF_UP));
                            lancamentoCalculado.setDadosCalculoValorCustoDiretoRateado(
                                    lancamentoCalculado.getDadosTabelaValorTotalCustosDiretos().divide(lancamentoCalculado.getDadosTabelaValorTotalFatura(), 4, BigDecimal.ROUND_HALF_UP).multiply(
                                            lancamentoParaCalculo.getValor()));
                            lancamentoCalculado.setDadosCalculoValorItemSemCusto(
                                    itemOrigem.getValorComDesconto().subtract(lancamentoCalculado.getDadosTabelaValorTotalCustosDiretos()).setScale(2, BigDecimal.ROUND_HALF_UP));
                        } else {
                            lancamentoCalculado.setDadosCalculoValorItemSemCusto(itemOrigem.getValorComDesconto().setScale(2, BigDecimal.ROUND_HALF_UP));
                        }

                        if (lancamentoCalculado.getDadosCalculoValorTarifa() != null && lancamentoCalculado.getDadosCalculoValorTarifa().compareTo(new BigDecimal(0)) != 0) {
                            lancamentoCalculado.setDadosCalculoTotalReducao(
                                    lancamentoCalculado.getDadosCalculoValorTaxa().add(lancamentoCalculado.getDadosCalculoValorTarifa()).add(lancamentoCalculado.getDadosCalculoValorTributo()));
                        } else {
                            lancamentoCalculado.setDadosCalculoTotalReducao(new BigDecimal(0));
                        }

                        lancamentoCalculado.setDadosCalculoRecebidoMenosReducao(
                                lancamentoParaCalculo.getValor().subtract(lancamentoCalculado.getDadosCalculoTotalReducao()).setScale(2, BigDecimal.ROUND_HALF_UP));

                        if (lancamentoCalculado.getDadosTabelaValorTotalCustosDiretos() == null || !(lancamentoCalculado.getDadosTabelaValorTotalCustosDiretos().compareTo(new BigDecimal(0)) != 0)) {

                            lancamentoCalculado.setDadosCalculoPercItemSemCusto(new BigDecimal(0));
                            lancamentoCalculado.setDadosCalculoInvPercItemSemCusto(new BigDecimal(0));
                            lancamentoCalculado.setDadosCalculoReducaoCustoDireto(new BigDecimal(0));

                        }else {
                            lancamentoCalculado.setDadosCalculoPercItemSemCusto(
                                    lancamentoCalculado.getDadosCalculoValorItemSemCusto().divide(lancamentoCalculado.getDadosTabelaValorTotalFaturaOrigem(), 6, BigDecimal.ROUND_HALF_UP));
                            
                            lancamentoCalculado.setDadosCalculoInvPercItemSemCusto(BigDecimal.valueOf(1).subtract(lancamentoCalculado.getDadosCalculoPercItemSemCusto()));

                            lancamentoCalculado.setDadosCalculoReducaoCustoDireto(
                                    lancamentoCalculado.getDadosCalculoRecebidoMenosReducao().multiply(lancamentoCalculado.getDadosCalculoInvPercItemSemCusto()).setScale(2, BigDecimal.ROUND_HALF_UP));
                        }
                        
                        lancamentoCalculado.setDadosCalculoValorARepassarSemCusto(
                                lancamentoCalculado.getDadosCalculoRecebidoMenosReducao().subtract(lancamentoCalculado.getDadosCalculoReducaoCustoDireto()).setScale(2, BigDecimal.ROUND_HALF_UP));

                        lancamentoCalculado.setDadosCalculoValorARepassarSemCusto(
                                lancamentoCalculado.getDadosCalculoValorARepassarSemCusto().subtract(lancamentoCalculado.getDadosCalculoValorTaxa()).setScale(2, BigDecimal.ROUND_HALF_UP));

                        lancamentoCalculado.setDadosCalculoPercItemFaturaStr(String.format("R$ %.2f", repasse.getFaturaItem().getValorComDesconto()) + " (" + String.format("%.2f%%",
                                lancamentoCalculado.getDadosCalculoPercItemSemCusto().multiply(BigDecimal.valueOf(100))) + ")");
                        lancamentoCalculado.setDadosCalculoPercPagtoFaturaStr(String.format("R$ %.2f", repasse.getLancamentoOrigem().getValor()) + " (" + String.format("%.2f%%",
                                repasse.getLancamentoOrigem().getValor().divide(lancamentoCalculado.getDadosTabelaValorTotalFaturaOrigem(), 2, BigDecimal.ROUND_HALF_UP).multiply(
                                        BigDecimal.valueOf(100))) + ")");

                        lancamentosCalculados.add(lancamentoCalculado);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }

        if (lancamentosCalculados != null && lancamentosCalculados.size() > 0) {
            PrimeFaces.current().executeScript("PF('dlgCalculoRepasse').show();");
        } else {
            this.addError("Erro", "Erro ao buscar cálculo - verifique as pendências e recalcule o repasse.", true);
        }

    }

    public void mudaCheckbox(String checkbox) {
        if (checkbox.equals("semPendencias") && semPendencias == true) {
            semPendencias = true;
            procedimentosNaoExecutados = false;
            mostrarRepasseAntigo = false;
        } else if (checkbox.equals("procedimentosNaoExecutados") && procedimentosNaoExecutados == true) {
            procedimentosNaoExecutados = true;
            semPendencias = false;
            mostrarRepasseAntigo = false;
        } else if (checkbox.equals("mostrarRepasseAntigo") && mostrarRepasseAntigo == true) {
            mostrarRepasseAntigo = true;
            procedimentosNaoExecutados = false;
            semPendencias = false;
        }
        if (procedimentosNaoExecutados || mostrarRepasseAntigo) {
            semPendencias = false;
        }
    }

    public void mudaCheckboxAjuste(String checkbox) {
        if (checkbox.equals("agendarParaData")) {
            ignorarRestante = false;
        } else if (checkbox.equals("ignorarRestante")) {
            agendarParaData = false;
        }
    }

    public void verificaPendencias(PlanoTratamentoProcedimento ptp) {
        pendencias = new ArrayList<String>();

        if (this.validaPagamentoPaciente && !ptp.getPlanoTratamento().isOrtodontico()) {
            if (!OrcamentoSingleton.getInstance().isProcedimentoTemOrcamentoAprovado(ptp)) {
                pendencias.add("Não existe orçamento aprovado para o paciente;");
            }
        }
        if (this.validaPagamentoPacienteOrtodontico && ptp.getPlanoTratamento().isOrtodontico()) {
            if (!OrcamentoSingleton.getInstance().isProcedimentoTemOrcamentoAprovado(ptp)) {
                pendencias.add("Não existe orçamento aprovado para o paciente;");
            }
        }
        if (ptp.getPlanoTratamento().getRegistroAntigo() != null && ptp.getPlanoTratamento().getRegistroAntigo().equals("S")) {
            pendencias.add("Plano de tratamento criado no modelo antigo. realizar o repasse na aba de Repasse Antigo;");
        }
        if (ptp.getDentistaExecutor() != null && Profissional.FIXO.equals(ptp.getDentistaExecutor().getTipoRemuneracao())) {
            pendencias.add("Dentista executor com tipo de remuneração fixa;");

        }
        if (this.validaExecucaoProcedimento && (ptp.getDataFinalizado() == null || ptp.getDentistaExecutor() == null)) {
            pendencias.add("Procedimento ainda não executado;");
        }
        //if (this.validaPagamentoPaciente && ptp.getFatura() != null && (ptp.getFatura().getLancamentos() == null || ptp.getFatura().getLancamentos().size() == 0 )) {    
        //    pendencias.add("Paciente ainda não pagou o procedimento;");
        // }

        if (validaPagamentoPaciente && !ptp.getPlanoTratamento().isOrtodontico()) {
            if ((ptp.getValorDisponivel() == null || ptp.getValorDisponivel().compareTo(
                    new BigDecimal(0)) == 0) && (ptp.getProcedimento().getValorRepasse() == null || ptp.getProcedimento().getValorRepasse().compareTo(new BigDecimal(0)) != 0)) {
                pendencias.add("Paciente ainda não pagou o procedimento - verifique os recebimentos;");
            } else if (ptp.getValorDisponivel().compareTo(new BigDecimal(0)) == 0 && ptp.getProcedimento().getValorRepasse().compareTo(new BigDecimal(0)) == 0) {
                pendencias.add("Procedimento com valor de repasse zerado;");
            }
        }

        if (validaPagamentoPacienteOrtodontico && ptp.getPlanoTratamento().isOrtodontico()) {
            if (ptp.getValorDisponivel().compareTo(new BigDecimal(0)) == 0) {
                pendencias.add("Paciente ainda não pagou o procedimento - verifique os recebimentos;");
            }
        }

        if (this.validaConferenciaCustosDiretos && (ptp.getCustoDiretoValido() == null || ptp.getCustoDiretoValido().equals("N"))) {
            pendencias.add("Custos diretos ainda não conferidos;");
        }

        if (pendencias == null || pendencias.size() == 0) {
            pendencias.add("Sem pendência!");
        }

    }

    public boolean existemPendencias(PlanoTratamentoProcedimento ptp) {
        verificaPendencias(ptp);
        if (pendencias != null && !pendencias.isEmpty() && pendencias.get(0).equals("Sem pendência!")) {
            return false;
        }
        return true;
    }

//    public Fatura getFaturaFromPtp(PlanoTratamentoProcedimento ptp) {
//        if (ptp.getRepasseFaturas() != null && ptp.getRepasseFaturas().size() > 0) {
//            RepasseFaturas repasseFaturas = RepasseFaturasSingleton.getInstance().getRepasseFaturasComFaturaAtiva(ptp);
//            if(repasseFaturas != null && repasseFaturas.getFaturaRepasse() != null) {
//                return repasseFaturas.getFaturaRepasse();
//            }
//        } else {
//            //repasse antigo, quando ainda nao tinha ptp no repasse fatura
//            RepasseFaturasItem repasseFaturasItem = RepasseFaturasItemSingleton.getInstance().getBo().getItemOrigemFromRepasse(ptp);
//            if (repasseFaturasItem == null || repasseFaturasItem.getFaturaItemRepasse() == null || repasseFaturasItem.getFaturaItemRepasse().getFatura() == null) {
//                return null;
//            }
//            return repasseFaturasItem.getFaturaItemRepasse().getFatura();
//        }
//        return null;
//    }

    public List<Profissional> geraSugestoesProfissional(String query) {
        List<Profissional> sugestoes = new ArrayList<>();
        List<Profissional> profissionais = new ArrayList<>();
        try {
            profissionais = ProfissionalSingleton.getInstance().getBo()
                    .listDentistasByEmpresaERemuneracao(UtilsFrontEnd.getEmpresaLogada(), new String[] { Profissional.PORCENTAGEM, Profissional.PROCEDIMENTO });
            for (Profissional p : profissionais) {
                if (!p.getTipoRemuneracao().equals(Profissional.FIXO)) {
                    if (Normalizer.normalize(p.getDadosBasico().getNome().toLowerCase(), Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "").toLowerCase().contains(
                            Normalizer.normalize(query, Normalizer.Form.NFD).toLowerCase())) {
                        sugestoes.add(p);
                    }
                }
            }
            Collections.sort(sugestoes);
        } catch (Exception e) {
            LogIntelidenteSingleton.getInstance().makeLog(e);
            this.addError("Erro", Mensagens.ERRO_AO_BUSCAR_REGISTROS, true);
        }
        return sugestoes;
    }
    
    public List<Profissional> geraSugestoesProfissionalDiaria(String query) {
        try {
            List<Profissional> profissionais = ProfissionalSingleton.getInstance()
                    .getBo().listProfissionaisByRemuneracaoDiaria(UtilsFrontEnd.getEmpresaLogada());
            if(query == null || query.trim().isEmpty())
                return profissionais;
            return profissionais.stream()
                .filter(profissional -> 
                        Normalizer.normalize(profissional.getDadosBasico().getPrefixoNome(), Normalizer.Form.NFD)
                            .replaceAll("[^\\p{ASCII}]", "")
                            .toLowerCase().contains(
                                    Normalizer.normalize(query, Normalizer.Form.NFD)
                                        .replaceAll("[^\\p{ASCII}]", "")
                                        .toLowerCase())
                )
                .sorted()
                .collect(Collectors.toList());
        } catch (Exception e) {
            LogIntelidenteSingleton.getInstance().makeLog(e);
            this.addError("Erro", Mensagens.ERRO_AO_BUSCAR_REGISTROS, true);
            return null;
        }
    }

    public void exportarTabela(String type) {
        exportarTabela("Repasse dos profissionais", tabelaRepasse, type);
    }
    
    public void exportarTabelaDiaria(String type) {
        exportarTabela("Repasse dos profissionais por Diária", tabelaRepasseDiaria, type);
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

    public boolean isMostrarRepasseAntigo() {
        return mostrarRepasseAntigo;
    }

    public void setMostrarRepasseAntigo(boolean mostrarRepasseAntigo) {
        this.mostrarRepasseAntigo = mostrarRepasseAntigo;
    }

    public Object getOneValue() {
        return Arrays.asList(new Object());
    }

    public boolean isValidaPagamentoPacienteOrtodontico() {
        return validaPagamentoPacienteOrtodontico;
    }

    public void setValidaPagamentoPacienteOrtodontico(boolean validaPagamentoPacienteOrtodontico) {
        this.validaPagamentoPacienteOrtodontico = validaPagamentoPacienteOrtodontico;
    }

    public HashMap<PlanoTratamentoProcedimento, List<Lancamento>> getPtpsValidosComLancamentos() {
        return ptpsValidosComLancamentos;
    }

    public void setPtpsValidosComLancamentos(HashMap<PlanoTratamentoProcedimento, List<Lancamento>> ptpsValidosComLancamentos) {
        this.ptpsValidosComLancamentos = ptpsValidosComLancamentos;
    }

    public BigDecimal getValorTotalFatura() {
        return valorTotalFatura;
    }

    public void setValorTotalFatura(BigDecimal valorTotalFatura) {
        this.valorTotalFatura = valorTotalFatura;
    }

    public BigDecimal getValorBaseRepasse() {
        return valorBaseRepasse;
    }

    public void setValorBaseRepasse(BigDecimal valorBaseRepasse) {
        this.valorBaseRepasse = valorBaseRepasse;
    }

    public List<Lancamento> getLancamentosDeOrigem() {
        return lancamentosDeOrigem;
    }

    public void setLancamentosDeOrigem(List<Lancamento> lancamentosDeOrigem) {
        this.lancamentosDeOrigem = lancamentosDeOrigem;
    }

    public BigDecimal getValorRepassar() {
        return valorRepassar;
    }

    public void setValorRepassar(BigDecimal valorRepassar) {
        this.valorRepassar = valorRepassar;
    }

    public boolean isAgendarParaData() {
        return agendarParaData;
    }

    public void setAgendarParaData(boolean agendarParaData) {
        this.agendarParaData = agendarParaData;
    }

    public Date getDataValorRestante() {
        return dataValorRestante;
    }

    public void setDataValorRestante(Date dataValorRestante) {
        this.dataValorRestante = dataValorRestante;
    }

    public boolean isIgnorarRestante() {
        return ignorarRestante;
    }

    public void setIgnorarRestante(boolean ignorarRestante) {
        this.ignorarRestante = ignorarRestante;
    }

    public String getJustificativa() {
        return justificativa;
    }

    public void setJustificativa(String justificativa) {
        this.justificativa = justificativa;
    }

    public RepasseFaturas getRepasseFatura() {
        return repasseFatura;
    }

    public void setRepasseFatura(RepasseFaturas repasseFatura) {
        this.repasseFatura = repasseFatura;
    }

    public BigDecimal getValorProcedimento() {
        return valorProcedimento;
    }

    public void setValorProcedimento(BigDecimal valorProcedimento) {
        this.valorProcedimento = valorProcedimento;
    }

    public Lancamento getLancamentoDeducoes() {
        return lancamentoDeducoes;
    }

    public void setLancamentoDeducoes(Lancamento lancamentoDeducoes) {
        this.lancamentoDeducoes = lancamentoDeducoes;
    }
    
    public Date getDataInicioDiaria() {
        return dataInicioDiaria;
    }

    public void setDataInicioDiaria(Date dataInicioDiaria) {
        this.dataInicioDiaria = dataInicioDiaria;
    }    
    
    public Date getDataFimDiaria() {
        return dataFimDiaria;
    }
    
    public void setDataFimDiaria(Date dataFimDiaria) {
        this.dataFimDiaria = dataFimDiaria;
    }
       
    public PeriodoBusca getFiltroPeriodoDiaria() {
        return filtroPeriodoDiaria;
    }
    
    public void setFiltroPeriodoDiaria(PeriodoBusca filtroPeriodoDiaria) {
        this.filtroPeriodoDiaria = filtroPeriodoDiaria;
    }

    public Profissional getProfissionalDiaria() {
        return profissionalDiaria;
    }
   
    public void setProfissionalDiaria(Profissional profissionalDiaria) {
        this.profissionalDiaria = profissionalDiaria;
    }
    
    public DataTable getTabelaRepasseDiaria() {
        return tabelaRepasseDiaria;
    }
   
    public void setTabelaRepasseDiaria(DataTable tabelaRepasseDiaria) {
        this.tabelaRepasseDiaria = tabelaRepasseDiaria;
    }
    
    public List<ProfissionalDiaria> getDiarias() {
        return diarias;
    }
    
    public void setDiarias(List<ProfissionalDiaria> diarias) {
        this.diarias = diarias;
    }
    
    public List<ProfissionalDiaria> getDiariasSelecionadas() {
        return diariasSelecionadas;
    }
   
    public void setDiariasSelecionadas(List<ProfissionalDiaria> diariasSelecionadas) {
        this.diariasSelecionadas = diariasSelecionadas;
    }
    
    public List<ProfissionalDiaria> getDiariaParaRecibo() {
        return diariaParaRecibo;
    }
   
    public void setDiariaParaRecibo(List<ProfissionalDiaria> diariaParaRecibo) {
        this.diariaParaRecibo = diariaParaRecibo;
    }
            
}
