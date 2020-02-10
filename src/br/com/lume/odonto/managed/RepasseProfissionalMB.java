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
import br.com.lume.odonto.entity.Paciente;
import br.com.lume.odonto.entity.PlanoTratamentoProcedimento;
import br.com.lume.odonto.entity.Profissional;
import br.com.lume.odonto.entity.RepasseFaturas;
import br.com.lume.odonto.entity.RepasseFaturasItem;
import br.com.lume.odonto.entity.RepasseFaturasLancamento;
import br.com.lume.odonto.entity.Requisito;
import br.com.lume.paciente.PacienteSingleton;
import br.com.lume.planoTratamentoProcedimento.PlanoTratamentoProcedimentoSingleton;
import br.com.lume.planoTratamentoProcedimentoCusto.PlanoTratamentoProcedimentoCustoSingleton;
import br.com.lume.profissional.ProfissionalSingleton;
import br.com.lume.repasse.ReciboRepasseProfissionalLancamentoSingleton;
import br.com.lume.repasse.ReciboRepasseProfissionalSingleton;
import br.com.lume.repasse.RepasseFaturasItemSingleton;
import br.com.lume.repasse.RepasseFaturasLancamentoSingleton;
import br.com.lume.repasse.RepasseFaturasSingleton;

/**
 * @author ricardo.poncio
 */
@ManagedBean
@ViewScoped
public class RepasseProfissionalMB extends LumeManagedBean<Fatura> {

    private static final long serialVersionUID = 1L;
    //private Logger log = Logger.getLogger(FaturaPagtoMB.class);

    private int mes;
    private boolean mesesAnteriores, pagoTotalmente;

    //FILTROS
    private Date dataInicio;
    private Date dataFim;
    private Profissional profissional;
    private Paciente paciente;

    private Fatura[] faturasSelecionadas;
    
    private String filtroPeriodo = "M",descricao, observacao;
    
    private String status;

    private List<Profissional> profissionaisRecibo;
    private HashMap<Profissional, Integer> profissionaisReciboLancamentos;
    private HashMap<Profissional, Double> profissionaisReciboValores;
    
    //EXPORTAÇÃO TABELA
    private DataTable tabelaRepasse;
    private int quantidadeLancamentos;

    public RepasseProfissionalMB() {
        super(FaturaSingleton.getInstance().getBo());
        this.setClazz(Fatura.class);
        try {
            Calendar now = Calendar.getInstance();
            setMes(now.get(Calendar.MONTH) + 1);

            Calendar inicio = Calendar.getInstance();
            inicio.setTime(new Date());
            inicio.set(Calendar.DAY_OF_MONTH, 1);
            inicio.set(Calendar.HOUR_OF_DAY, 0);
            inicio.set(Calendar.MINUTE, 0);
            inicio.set(Calendar.SECOND, 0);
            inicio.set(Calendar.MILLISECOND, 0);
            this.dataInicio = inicio.getTime();
            Calendar fim = Calendar.getInstance();
            fim.setTime(new Date());
            fim.set(Calendar.DAY_OF_MONTH, fim.getActualMaximum(Calendar.DAY_OF_MONTH));
            fim.set(Calendar.HOUR_OF_DAY, 23);
            fim.set(Calendar.MINUTE, 59);
            fim.set(Calendar.SECOND, 59);
            fim.set(Calendar.MILLISECOND, 999);
            this.dataFim = fim.getTime();

            setStatus("A pagar");
         //   pesquisar();
        } catch (Exception e) {
            LogIntelidenteSingleton.getInstance().makeLog(e);
            this.addError("Erro", "Não foi possivel carregar a tela.", true);
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

    public void pesquisar() {
        try {
            Calendar inicio = null;
            if (getDataInicio() != null) {
                inicio = Calendar.getInstance();
                inicio.setTime(getDataInicio());
                inicio.set(Calendar.HOUR_OF_DAY, 0);
                inicio.set(Calendar.MINUTE, 0);
                inicio.set(Calendar.SECOND, 0);
                inicio.set(Calendar.MILLISECOND, 0);
            }
            Calendar fim = null;
            if (getDataFim() != null) {
                fim = Calendar.getInstance();
                fim.setTime(getDataFim());
                fim.set(Calendar.HOUR_OF_DAY, 23);
                fim.set(Calendar.MINUTE, 59);
                fim.set(Calendar.SECOND, 59);
                fim.set(Calendar.MILLISECOND, 999);
            }

          //  if (this.status == null)
                setEntityList(FaturaSingleton.getInstance().getBo().listFaturasRepasse(UtilsFrontEnd.getEmpresaLogada(), getProfissional(),
                        (inicio != null ? inicio.getTime() : null), (fim != null ? fim.getTime() : null), status));
           // else if (this.status == 1)
           //     setEntityList(FaturaSingleton.getInstance().getBo().findFaturasRepasseFilter(UtilsFrontEnd.getEmpresaLogada(), getProfissional(), getPaciente(), null,
           //             (inicio != null ? inicio.getTime() : null), (fim != null ? fim.getTime() : null), isMesesAnteriores(), false, true));
          //  else if (this.status == 2)
          //      setEntityList(FaturaSingleton.getInstance().getBo().findFaturasRepasseFilter(UtilsFrontEnd.getEmpresaLogada(), getProfissional(), getPaciente(), null,
             //           (inicio != null ? inicio.getTime() : null), (fim != null ? fim.getTime() : null), isMesesAnteriores(), true, false));
            if (isPagoTotalmente())
                getEntityList().removeIf(fatura -> {
                    if (FaturaSingleton.getInstance().getTotalRestante(fatura).compareTo(BigDecimal.ZERO) <= 0)
                        return true;
                    return false;
                });

            if (getEntityList() != null) {
              
                getEntityList().forEach(fatura -> { 
                    RepasseFaturas repasseObject = null;
                    try {
                        repasseObject = RepasseFaturasSingleton.getInstance().getBo().getFaturaOrigemFromRepasse(fatura);
                        RepasseFaturasItem repasseItem = RepasseFaturasItemSingleton.getInstance().getBo().getItemOrigemFromRepasse(fatura.getItensFiltered().get(0));
                        PlanoTratamentoProcedimento ptp = PlanoTratamentoProcedimentoSingleton.getInstance().getProcedimentoFromFaturaItem(repasseItem.getFaturaItemOrigem());
                        fatura.setDadosTabelaRepassePaciente(repasseObject.getFaturaOrigem().getPaciente());
                        fatura.setDadosTabelaRepassePTP(ptp);
                        fatura.setDadosTabelaRepassePlanoTratamento(repasseObject.getFaturaOrigem().getItensFiltered().get(
                                0).getOrigemOrcamento().getOrcamentoItem().getOrigemProcedimento().getPlanoTratamentoProcedimento().getPlanoTratamento());
                        fatura.setDadosTabelaRepasseTotalFatura(FaturaSingleton.getInstance().getTotal(fatura).setScale(2, BigDecimal.ROUND_HALF_UP));
                        fatura.setDadosTabelaRepasseTotalPago(FaturaSingleton.getInstance().getTotalPago(fatura).setScale(2, BigDecimal.ROUND_HALF_UP));
                        fatura.setDadosTabelaRepasseTotalNaoPago(FaturaSingleton.getInstance().getTotalNaoPago(fatura).setScale(2, BigDecimal.ROUND_HALF_UP));
                        fatura.setDadosTabelaRepasseTotalNaoPlanejado(FaturaSingleton.getInstance().getTotalNaoPlanejado(fatura).setScale(2, BigDecimal.ROUND_HALF_UP));
                           
                        fatura.setDadosTabelaRepasseTotalRestante(new BigDecimal(0)); 
                        
                      //  fatura.setDadosTabelaRepasseTotalRestante(FaturaSingleton.getInstance().getTotalRestante(fatura).setScale(2, BigDecimal.ROUND_HALF_UP));    
                        
                    } catch (Exception e) {
                        // TODO: handle exception
                    }
                    List<Lancamento> lancamentosCalculados = new ArrayList<Lancamento>();
                    fatura.setDadosTabelaStatusFatura("Paga");
                    boolean  faturaAPagar = false;
                  //  boolean  validaParaPagamento = true;
                    BigDecimal total = new BigDecimal(0);
                    for (Lancamento lancamento : fatura.getLancamentos()) {
                        try {                             
                            // Setando Objetos Gerais
                            RepasseFaturasLancamento repasse = RepasseFaturasLancamentoSingleton.getInstance().getBo().getFaturaRepasseLancamentoFromLancamentoRepasseDestino(lancamento);   
                            
                            lancamento.setRfl(repasse);
                            lancamento.setPtp(PlanoTratamentoProcedimentoSingleton.getInstance().getProcedimentoFromFaturaItem(repasse.getFaturaItem()));
                            lancamento.setPlanoTratamentoProcedimento(PlanoTratamentoProcedimentoSingleton.getInstance().getProcedimentoFromFaturaItem(repasse.getFaturaItem()));
                            //Só vamos deixar valor a pagar para lancamentos validos de acordo com a regra da empresa
                            List<Requisito> todosRequisitos = RepasseFaturasLancamentoSingleton.getInstance().getRequisitosValidaLancamentoFromRepasseFatura(UtilsFrontEnd.getEmpresaLogada(), lancamento);
                            boolean requisitoFalhou = false;
                            if(todosRequisitos != null && todosRequisitos.size() > 0) {
                                for (Requisito req : todosRequisitos) {
                                    if(!req.isRequisitoFeito()) {
                                        requisitoFalhou = true;
                                    }
                               }
                            } 
//                            if(lancamento.getPtp().getPlanoTratamento().getPaciente().getDadosBasico().getNome().equals("Jayne Lima")) {
//                                System.out.println(lancamento.getValor());   
//                                System.out.println(requisitoFalhou);    
//                            }
                            
                            if(!requisitoFalhou) {  
//                                for (Lancamento lancamentoOrigem : repasseObject.getFaturaOrigem().getLancamentos()) {
//                                    if(lancamentoOrigem.getValidado().equals("S") && lancamentoOrigem.getConferidoPorProfissional() != null) {
//                                        total = total.add(lancamento.getValor());                 
//                                        fatura.setDadosTabelaRepasseTotalRestante(total);           
//                                    }
//                                }
                                if(lancamento.getConferidoPorProfissional() == null) {
                                    total = total.add(lancamento.getValor());                 
                                    fatura.setDadosTabelaRepasseTotalRestante(total);    
                                }
                                     
                            }
                          //  else {
                           //     fatura.setDadosTabelaRepasseTotalRestante(new BigDecimal(0)); 
                           // }
                            //else {
                           //     validaParaPagamento = false;
                           // }
                                
                             
                             
                            
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
                                                        
                            if(!faturaAPagar && lancamento.getValidado() != null && lancamento.getPagamentoConferido() != null) {
                                if(!lancamento.getValidado().equals("S") || !lancamento.getPagamentoConferido().equals("S")) {
                                    fatura.setDadosTabelaStatusFatura("A Pagar");   
                                    faturaAPagar = true;    
                                }                                 
                            }     
                        } catch (Exception e) {
                            lancamento.setPtp(null);
                        }
                        lancamentosCalculados.add(lancamento);    
                    }
                    
                    fatura.setLancamentos(lancamentosCalculados);
                    
                });
            }
        } catch (Exception e) {
            LogIntelidenteSingleton.getInstance().makeLog(e);
            this.addError("Erro", Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), true);
        }
    }
    
  //  public Integer getQtdeLancamentosFromProfissional(Profissional profissional) {
   //     return this.profissionaisReciboLancamentos.get(profissional);
   // }

    public Double getValorLancamentosFromProfissional(Profissional profissional) {
        return this.profissionaisReciboValores.get(profissional);
    }
    
    public void prepararRecibo() {
        this.descricao = null;
        this.observacao = null;

        this.profissionaisReciboLancamentos = new HashMap<>();
        this.profissionaisReciboValores = new HashMap<>();
        this.profissionaisRecibo = new ArrayList<>();
        
        List<Lancamento> lancamentos = new ArrayList<Lancamento>();
        for (Fatura fatura : faturasSelecionadas) {
            for (Lancamento l : fatura.getLancamentos()) {
                lancamentos.add(l);
                if(l.getValorDesconto().compareTo(new BigDecimal(0)) == 0) {
                    if(UtilsFrontEnd.getEmpresaLogada().getValidarGeraReciboValorZerado().equals("S") || l.getConferidoPorProfissional() == null) {
                        lancamentos.remove(l);
                    }
                }
                
                List<Requisito> todosRequisitos = RepasseFaturasLancamentoSingleton.getInstance().getRequisitosValidaLancamentoFromRepasseFatura(UtilsFrontEnd.getEmpresaLogada(), l);
                boolean requisitoFalhou = false;
                if(todosRequisitos != null && todosRequisitos.size() > 0) {
                    for (Requisito req : todosRequisitos) {
                        if(!req.isRequisitoFeito()) {
                            requisitoFalhou = true;
                        }
                   }
                }
                if(requisitoFalhou) {
                    lancamentos.remove(l);
                }
                
            }
               if (fatura.getLancamentos() == null || Arrays.asList(fatura.getLancamentos()).size() == 0) {
                 addError("Erro", "É necessário escolher ao menos uma fatura.");
                 return;
             }
            
           // if(lancamentos == null || lancamentos.size() == 0) {
            //    addError("Erro", "Apenas lançamentos com valores zerados encontrados! Se deseja gerar recibo mesmo assim, muda a configuração de sua empresa");
           //     return;
           // }
        }

     //   for (Fatura fatura : faturasSelecionadas) {
            
         //   if (fatura.getLancamentos() == null || Arrays.asList(fatura.getLancamentos()).size() == 0) {
           //     addError("Erro", "É necessário escolher ao menos um repasse.");
          //      return;
         //   }
            this.quantidadeLancamentos = 0;
            for (Lancamento lancamento : lancamentos) {
                this.quantidadeLancamentos++;              
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
        
             
      //  }



        PrimeFaces.current().executeScript("PF('dlgGerarRecibo').show()");
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
    
    public void cancelarRecibo() {
      //  setLancamentosSelecionados(new Lancamento[] {});
        setEntityList(new ArrayList<Fatura>());
        PrimeFaces.current().executeScript("PF('dlgGerarRecibo').hide()");
    }

    public void gerarRecibo() {       
        try {            
          List<Lancamento> lancamentos = new ArrayList<Lancamento>();
            for (Fatura fatura : faturasSelecionadas) {
              
                for (Lancamento l : fatura.getLancamentos()) {
                    
                    lancamentos.add(l);
                    List<Requisito> todosRequisitos = RepasseFaturasLancamentoSingleton.getInstance().getRequisitosValidaLancamentoFromRepasseFatura(UtilsFrontEnd.getEmpresaLogada(), l);
                    boolean requisitoFalhou = false;
                    if(todosRequisitos != null && todosRequisitos.size() > 0) {
                        for (Requisito req : todosRequisitos) {
                            if(!req.isRequisitoFeito()) {
                                requisitoFalhou = true;
                            }
                       }
                    }
                    if(requisitoFalhou) {
                        lancamentos.remove(l);
                    }
                    if(l.getConferidoPorProfissional() != null) {
                        lancamentos.remove(l);
                    }
                    
                }
//                for (Lancamento l : lancamentos) {
//                    if (ReciboRepasseProfissionalLancamentoSingleton.getInstance().getBo().findReciboValidoForLancamento(l) != null ) {
//                        addError("Erro", "Existem lançamentos na lista de repasse que já estão em outros recibos!");
//                        return;
//                    }
//                }
                
            }
            
            if (!ReciboRepasseProfissionalSingleton.getInstance().gerarRecibo(lancamentos, this.descricao, this.observacao, UtilsFrontEnd.getProfissionalLogado()))
                throw new Exception();
            
            //setLancamentosSelecionados(new Lancamento[] {});
            PrimeFaces.current().executeScript("PF('dlgGerarRecibo').hide()");
            addInfo("Sucesso", Mensagens.getMensagemOffLine(Mensagens.REGISTRO_SALVO_COM_SUCESSO));                 

        } catch (Exception e) {
            addError("Erro", Mensagens.getMensagemOffLine(Mensagens.ERRO_AO_SALVAR_REGISTRO));
        }
    }

    public List<Paciente> geraSugestoesPaciente(String query) {

        List<Paciente> pacientes = new ArrayList<Paciente>();

        try {
            pacientes = PacienteSingleton.getInstance().getBo().listSugestoesComplete(query, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
            for (Paciente p : pacientes) {
                if (!Normalizer.normalize(p.getDadosBasico().getNome().toLowerCase(), Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "").toLowerCase().contains(
                        Normalizer.normalize(query, Normalizer.Form.NFD).toLowerCase())) {
                    pacientes.remove(p);
                }
            }
            Collections.sort(pacientes);
        } catch (Exception e) {
            LogIntelidenteSingleton.getInstance().makeLog(e);
            this.addError("Erro", Mensagens.ERRO_AO_BUSCAR_REGISTROS, true);
        }
        return pacientes;

    }

    public void exportarTabela(String type) {
        exportarTabela("Repasse dos profissionais", tabelaRepasse, type);
    }

    public int getMes() {
        return mes;
    }

    public void setMes(int mes) {
        this.mes = mes;
    }

    public Profissional getProfissional() {
        return profissional;
    }

    public void setProfissional(Profissional profissional) {
        this.profissional = profissional;
    }

    public boolean isMesesAnteriores() {
        return mesesAnteriores;
    }

    public void setMesesAnteriores(boolean mesesAnteriores) {
        this.mesesAnteriores = mesesAnteriores;
    }

    public boolean isPagoTotalmente() {
        return pagoTotalmente;
    }

    public void setPagoTotalmente(boolean pagoTotalmente) {
        this.pagoTotalmente = pagoTotalmente;
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

    public Paciente getPaciente() {
        return paciente;
    }

    public void setPaciente(Paciente paciente) {
        this.paciente = paciente;
    }

    public DataTable getTabelaRepasse() {
        return tabelaRepasse;
    }

    public void setTabelaRepasse(DataTable tabelaRepasse) {
        this.tabelaRepasse = tabelaRepasse;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    
    public String getFiltroPeriodo() {
        return filtroPeriodo;
    }

    
    public void setFiltroPeriodo(String filtroPeriodo) {
        this.filtroPeriodo = filtroPeriodo;
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

    
    public int getQuantidadeLancamentos() {
        return quantidadeLancamentos;
    }

    
    public void setQuantidadeLancamentos(int quantidadeLancamentos) {
        this.quantidadeLancamentos = quantidadeLancamentos;
    }

    
    public Fatura[] getFaturasSelecionadas() {
        return faturasSelecionadas;
    }

    
    public void setFaturasSelecionadas(Fatura[] faturasSelecionadas) {
        this.faturasSelecionadas = faturasSelecionadas;
    }

}
