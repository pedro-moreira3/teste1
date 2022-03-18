package br.com.lume.odonto.managed;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.push.Push;
import javax.faces.push.PushContext;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.primefaces.PrimeFaces;

import br.com.lume.avisos.AvisosSingleton;
import br.com.lume.common.iugu.Iugu;
import br.com.lume.common.iugu.exceptions.IuguDadosObrigatoriosException;
import br.com.lume.common.log.LogIntelidenteSingleton;
import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.EnviaEmail;
import br.com.lume.common.util.JSFHelper;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.Status;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.faturamento.FaturaSingleton;
import br.com.lume.lancamento.LancamentoSingleton;
import br.com.lume.lancamentoContabil.LancamentoContabilSingleton;
import br.com.lume.odonto.entity.Avisos;
import br.com.lume.odonto.entity.Avisos.TIPO_AVISO;
import br.com.lume.odonto.entity.Fatura;
import br.com.lume.odonto.entity.FaturaItem;
import br.com.lume.odonto.entity.Lancamento;
import br.com.lume.odonto.entity.PlanoTratamentoProcedimento;
import br.com.lume.odonto.entity.PlanoTratamentoProcedimentoCusto;
import br.com.lume.odonto.entity.Profissional;
import br.com.lume.odonto.entity.ReciboRepasseProfissionalLancamento;
import br.com.lume.odonto.entity.RepasseFaturas;
import br.com.lume.planoTratamentoProcedimento.PlanoTratamentoProcedimentoSingleton;
import br.com.lume.planoTratamentoProcedimentoCusto.PlanoTratamentoProcedimentoCustoSingleton;
import br.com.lume.profissional.ProfissionalSingleton;
import br.com.lume.repasse.ReciboRepasseProfissionalLancamentoSingleton;
import br.com.lume.repasse.RepasseFaturasSingleton;
import br.com.lume.security.EmpresaSingleton;
import br.com.lume.security.UsuarioSingleton;
import br.com.lume.security.entity.Empresa;
import co.elastic.apm.opentracing.ElasticApmTracer;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.Tracer;

@ManagedBean
@ViewScoped
public class AvisosMB extends LumeManagedBean<Avisos> {

    private static final long serialVersionUID = 1L;
    private Logger log = Logger.getLogger(AvisosMB.class);

    @Inject
    @Push
    private PushContext atualizarAvisos;

    private String idEmpresaParaSocket;
    
    final static Tracer tracer = new ElasticApmTracer();

    public AvisosMB() {
        super(AvisosSingleton.getInstance().getBo());
        this.setClazz(Avisos.class);
        if (UtilsFrontEnd.getProfissionalLogado() != null) {
            idEmpresaParaSocket = "" + UtilsFrontEnd.getProfissionalLogado().getIdEmpresa();
        }
        
        
        Span span = tracer.buildSpan("TESTE SPAN").start();
        try(Scope scope = tracer.scopeManager().activate(span)){
            carregarAvisos();
            System.out.println("Teste print profissional: "
                    + UtilsFrontEnd.getProfissionalLogado().getDadosBasico().getNome());
        }finally {
            span.finish();
        }
    }

    public void testeEmail() {
        try {
            EnviaEmail.envioResetSenha(UsuarioSingleton.getInstance().getBo().find(UtilsFrontEnd.getProfissionalLogado().getIdUsuario()));
        } catch (Exception e) {
            e.printStackTrace();
            this.addError("Erro", e.getMessage());
        }
    }

    public void redireciona(Avisos aviso) {
        if (aviso.getTipoAviso().equals(TIPO_AVISO.CONTRATACAO)) {

            if (UtilsFrontEnd.getEmpresaLogada().getEmpChaCep() == null || UtilsFrontEnd.getEmpresaLogada().getEmpChaCep().equals("")) {
                this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "Antes de contratar o serviço, favor preencher o CEP da empresa em Administrativo, Cadastro da clínica");
                return;
            }

            PrimeFaces.current().executeScript("PF('dlgContratacao').show();");
        } else {
            JSFHelper.redirect(aviso.getLink());
        }
    }

    public void carregarAvisos() {
        try {
            this.setEntityList(AvisosSingleton.getInstance().carregarAvisos(UtilsFrontEnd.getEmpresaLogada(), UtilsFrontEnd.getProfissionalLogado()));
            final Empresa emp = UtilsFrontEnd.getEmpresaLogada();
            //  final Empresa emp2 = emp;
            Thread th = new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
                        EmpresaSingleton.getInstance().carregarStatusFaturas(emp);

                        if (Iugu.getInstance().isSuspenso(emp.getAssinaturaIuguBanco())) {
                            Avisos aviso = new Avisos();
                            aviso.setTitulo("Regularização de assinatura");
                            aviso.setAviso("Entre em contato com o suporte para regularização.");
                            aviso.setTipoAviso(TIPO_AVISO.INFORMACAO);
                            aviso.setLink("mensal.jsf");
                            getEntityList().add(aviso);

                            atualizarAvisos.send(emp.getEmpIntCod());
                        }

                    } catch (Exception e) {
                        LogIntelidenteSingleton.getInstance().makeLog(e);
                        e.printStackTrace();
                    }
                }
            });

            if (emp.getAssinaturaIuguBanco() != null && emp.getIdIugu() != null && !emp.getIdIugu().equals("")) {
                th.start();
            }

        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "Falha ao carregar avisos.");
            e.printStackTrace();
        }
    }

    public void contratarServico() {
        try {
            if (isAdmin()) {
                boolean status = EmpresaSingleton.getInstance().criarUsuarioAssinaturaIugu(UtilsFrontEnd.getEmpresaLogada());
                carregarAvisos();
                if (status) {
                    this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "Serviço contratado ! Para visualizar as faturas, acesse o menu Financeiro-Mensalidades.");
                    JSFHelper.redirect("mensal.jsf");
                } else {
                    this.addWarn(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "Não foi possível contratar o serviço.");
                }
            } else {
                this.addError(Mensagens.getMensagem(Mensagens.USUARIO_SEM_PERFIL), "Permissão negada.");
            }
        } catch (IuguDadosObrigatoriosException e) {
            this.addWarn(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "Dados da empresa insuficientes para contratação.");
        } catch (Exception e) {
            this.addWarn(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "Não foi possível contratar o serviço. Verifique o CEP da sua empresa em administrativo, cadastro da clínica");
            e.printStackTrace();
        }
    }

    public PushContext getAtualizarAvisos() {
        return atualizarAvisos;
    }

    public void setAtualizarAvisos(PushContext atualizarAvisos) {
        this.atualizarAvisos = atualizarAvisos;
    }

    public String getIdEmpresaParaSocket() {
        return idEmpresaParaSocket;
    }

    public void setIdEmpresaParaSocket(String idEmpresaParaSocket) {
        this.idEmpresaParaSocket = idEmpresaParaSocket;
    }

    public void scriptRepasse() {
        try {
            Empresa empresa = EmpresaSingleton.getInstance().getBo().find(139l);
            Profissional adminLume = ProfissionalSingleton.getInstance().getBo().find(994l);

            List<Fatura> faturas = FaturaSingleton.getInstance().getBo().listByEmpresa(empresa.getEmpIntCod(), Fatura.TipoFatura.RECEBIMENTO_PACIENTE);
            BigDecimal totalRecebido = BigDecimal.ZERO;
            BigDecimal totalReceber = BigDecimal.ZERO;
            BigDecimal totalRepassado = BigDecimal.ZERO;
            BigDecimal totalRepassar = BigDecimal.ZERO;
            BigDecimal totalFaturaRepasse = BigDecimal.ZERO;
            BigDecimal disparidadeAjuste = BigDecimal.ZERO;

            Lancamento lanRepasse = null;

            System.out.println("--- INICIANDO ANALISES REPASSE ---");

            for (Fatura origem : faturas) {
                List<PlanoTratamentoProcedimento> procedimentos = PlanoTratamentoProcedimentoSingleton.getInstance().getBo().listFromOrigem(origem);
                totalRecebido = FaturaSingleton.getInstance().getTotalPagoFromPaciente(origem);
                totalReceber = FaturaSingleton.getInstance().getTotal(origem);

                // SE NÃO EXISTIR REPASSE, PRECISA SER RECALCULADO O PTP QUANDO JÁ EXISTE VALOR RECEBIDO
                if (totalRecebido.compareTo(BigDecimal.ZERO) > 0) {
                    procedimentos.removeIf((ptp) -> !ptp.getStatuss());

                    for (PlanoTratamentoProcedimento ptp : procedimentos) {
                        if (FaturaSingleton.getInstance().getBo().getFaturasOrigemFromPTP(ptp, empresa.getEmpIntCod()).size() == 1) {

                            List<RepasseFaturas> repasses = RepasseFaturasSingleton.getInstance().getBo().getFaturaRepasseFromOrigem(origem, true);

                            if (repasses != null && !repasses.isEmpty())
                                repasses.removeIf((rf) -> !rf.getFaturaRepasse().isAtivo());

                            // SE NÃO TEM REPASSE RECALCULA
                            if (repasses == null || repasses.isEmpty()) {
                                try {
                                    RepasseFaturasSingleton.getInstance().recalculaRepasse(ptp, ptp.getDentistaExecutor(), adminLume, null, empresa);
                                    System.out.println("PTP ID: " + ptp.getId() + " DATA EXEC: " + ptp.getDataFinalizadoStr());
                                } catch (Exception e) {
                                    System.out.println("ERRO AO RECALCULAR REPASSE. PTP: " + ptp.getId());
                                    e.printStackTrace();
                                }
                            } else {
                                //totalRepassado = LancamentoSingleton.getInstance().getTotalLancamentoPorFatura(Arrays.asList(repasses.get(0).getFaturaRepasse()), true, ValidacaoLancamento.VALIDADO);
                                totalRepassado = LancamentoSingleton.getInstance().getBo().listLancamentosFromFaturaValor(Arrays.asList(repasses.get(0).getFaturaRepasse()), null, null, true);
                                totalRepassar = valorRepasse(repasses.get(0), repasses.get(0).getItens().get(0).getFaturaItemOrigem(),
                                        repasses.get(0).getPlanoTratamentoProcedimento().getDentistaExecutor());

                                totalRepassado = totalRepassado.setScale(2, BigDecimal.ROUND_HALF_UP);
                                totalRepassar = totalRepassar.setScale(2, BigDecimal.ROUND_HALF_UP);

                                // SE FALTA REPASSAR E NÃO TEM MAIS O QUE RECEBER, PRECISA SER CORRIGIDO
                                if ((totalRepassado.compareTo(totalRepassar) < 0) && (totalRecebido.compareTo(totalReceber) == 0)) {
                                    RepasseFaturas rf = repasses.get(0);
                                    boolean reciboAprovado = false;

                                    // VERIFICA SE TEM RECIBO APROVADO
                                    if (ptp.getFatura() != null && ptp.getFatura().getLancamentos() != null) {
                                        for (Lancamento l : ptp.getFatura().getLancamentos()) {
                                            ReciboRepasseProfissionalLancamento rrpl = ReciboRepasseProfissionalLancamentoSingleton.getInstance().getBo().findReciboValidoForLancamento(l);
                                            if (rrpl != null && rrpl.getRecibo().getAprovado().equals("S")) {
                                                reciboAprovado = true;
                                                break;
                                            }
                                        }
                                    }

                                    List<ReciboRepasseProfissionalLancamento> recibos = new ArrayList<ReciboRepasseProfissionalLancamento>();

                                    rf.getFaturaRepasse().getLancamentosFiltered().forEach((l) -> {
                                        ReciboRepasseProfissionalLancamento l2 = ReciboRepasseProfissionalLancamentoSingleton.getInstance().getBo().findReciboValidoForLancamento(l);
                                        recibos.add(l2);
                                    });

                                    try {
                                        RepasseFaturasSingleton.getInstance().recalculaRepasse(ptp, ptp.getDentistaExecutor(), adminLume, null, empresa);
                                        System.out.println("PTP ID: " + ptp.getId() + " DATA EXEC: " + ptp.getDataFinalizadoStr());

                                        // PARA OS RECIBOS EXISTENTES, ASSOCIA-OS AO REPASSE RECALCULADO
                                        if (reciboAprovado) {
                                            List<RepasseFaturas> repasseNovo = RepasseFaturasSingleton.getInstance().getBo().getRepasseFaturasFromPTP(ptp);
                                            repasseNovo.removeIf((r) -> !r.getFaturaRepasse().isAtivo());

                                            for (ReciboRepasseProfissionalLancamento rrpl : recibos) {
                                                rrpl.setLancamento(repasseNovo.get(0).getFaturaRepasse().getLancamentos().get(0));
                                            }

                                            ReciboRepasseProfissionalLancamentoSingleton.getInstance().getBo().mergeBatch(recibos);
                                        }
                                    } catch (Exception e) {
                                        System.out.println("ERRO AO RECALCULAR REPASSE. PTP: " + ptp.getId());
                                        e.printStackTrace();
                                    }
                                } else if (totalRepassado.compareTo(FaturaSingleton.getInstance().getTotal(repasses.get(0).getFaturaRepasse())) > 0) {
                                    totalFaturaRepasse = FaturaSingleton.getInstance().getTotal(repasses.get(0).getFaturaRepasse());
                                    disparidadeAjuste = totalRepassado.subtract(totalFaturaRepasse);

                                    totalFaturaRepasse = totalFaturaRepasse.setScale(2, BigDecimal.ROUND_HALF_UP);
                                    disparidadeAjuste = disparidadeAjuste.setScale(2, BigDecimal.ROUND_HALF_UP);

                                    repasses.get(0).getFaturaRepasse().getLancamentos().removeIf((l) -> !l.isAtivo());

                                    repasses.get(0).getFaturaRepasse().getLancamentos().sort((lan1, lan2) -> {
                                        BigDecimal i = lan1.getValor();
                                        return i.compareTo(lan2.getValor());
                                    });

                                    if (repasses.get(0).getFaturaRepasse().getLancamentos() != null && !repasses.get(0).getFaturaRepasse().getLancamentos().isEmpty()) {
                                        lanRepasse = repasses.get(0).getFaturaRepasse().getLancamentos().get(0);
                                        lanRepasse.setValor(lanRepasse.getValor().subtract(disparidadeAjuste));

                                        LancamentoSingleton.getInstance().getBo().merge(lanRepasse);
                                        System.out.println("REAJUSTE DISPARIDADE PTP ID: " + ptp.getId() + " DATA EXEC: " + ptp.getDataFinalizadoStr() + " LANC ID: " + lanRepasse.getId());
                                    }

                                }
                            }

                        }
                    }
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public BigDecimal valorRepasse(RepasseFaturas repasseFaturas, FaturaItem faturaItem, Profissional profissional) throws Exception {

        BigDecimal percentual = repasseFaturas.getValorCalculo().divide(BigDecimal.valueOf(100), 4, BigDecimal.ROUND_HALF_UP);
        PlanoTratamentoProcedimento ptp = PlanoTratamentoProcedimentoSingleton.getInstance().getProcedimentoFromFaturaItem(faturaItem);

        BigDecimal valorRepasseOrigem = faturaItem.getValorComDesconto();
        valorRepasseOrigem = valorRepasseOrigem.setScale(4, BigDecimal.ROUND_HALF_UP);
        BigDecimal valorRepasse = BigDecimal.valueOf(valorRepasseOrigem.doubleValue());

        BigDecimal percentualTributo = BigDecimal.valueOf(repasseFaturas.getValorImposto()).divide(BigDecimal.valueOf(100), 4, BigDecimal.ROUND_HALF_UP);
        BigDecimal valorDescontoTributos = valorRepasse.multiply(percentualTributo);
        valorDescontoTributos = valorDescontoTributos.setScale(4, BigDecimal.ROUND_HALF_UP);
        BigDecimal valorRepasseSemTributo = valorRepasse.subtract(valorDescontoTributos);
        if (repasseFaturas != null && Status.SIM.equals(repasseFaturas.getSubtraiImpostoRepasse()))
            valorRepasse = BigDecimal.valueOf(valorRepasseSemTributo.doubleValue());
        BigDecimal taxasAndTarifas = LancamentoContabilSingleton.getInstance().getTaxaAndTarifaRateadaForFaturaItem(faturaItem);
        BigDecimal valorRepasseSemTaxasAndTarifas = valorRepasse.subtract(taxasAndTarifas);
        valorRepasse = BigDecimal.valueOf(valorRepasseSemTaxasAndTarifas.doubleValue());

        BigDecimal valorCustoDireto = BigDecimal.ZERO;
        if (ptp != null) {
            List<PlanoTratamentoProcedimentoCusto> custos = PlanoTratamentoProcedimentoCustoSingleton.getInstance().getBo().listFromPlanoTratamentoProcedimento(ptp);
            for (PlanoTratamentoProcedimentoCusto custo : custos)
                valorCustoDireto = valorCustoDireto.add(custo.getValor());
        }
        valorRepasse = valorRepasse.subtract(valorCustoDireto);
        valorRepasse = percentual.multiply(valorRepasse);

        return valorRepasse;
    }

}
