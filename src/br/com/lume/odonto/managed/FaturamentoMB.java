package br.com.lume.odonto.managed;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ActionEvent;

import org.apache.log4j.Logger;
import org.primefaces.PrimeFaces;
import org.primefaces.event.ToggleSelectEvent;

import br.com.lume.common.exception.business.BusinessException;
import br.com.lume.common.exception.techinical.TechnicalException;
import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.custo.CustoSingleton;
import br.com.lume.lancamento.LancamentoSingleton;
import br.com.lume.lancamentoContabil.LancamentoContabilSingleton;
import br.com.lume.motivo.MotivoSingleton;
//import br.com.lume.odonto.bo.CustoBO;
//import br.com.lume.odonto.bo.LancamentoBO;
//import br.com.lume.odonto.bo.LancamentoContabilBO;
//import br.com.lume.odonto.bo.MotivoBO;
//import br.com.lume.odonto.bo.PlanoTratamentoProcedimentoBO;
//import br.com.lume.odonto.bo.ProfissionalBO;
//import br.com.lume.odonto.bo.RepasseItemBO;
//import br.com.lume.odonto.bo.RepasseLancamentoBO;
//import br.com.lume.odonto.bo.RepasseProfissionalBO;
import br.com.lume.odonto.entity.Lancamento;
import br.com.lume.odonto.entity.LancamentoContabil;
import br.com.lume.odonto.entity.Motivo;
import br.com.lume.odonto.entity.PlanoTratamentoProcedimento;
import br.com.lume.odonto.entity.PlanoTratamentoProcedimentoCusto;
import br.com.lume.odonto.entity.Profissional;
import br.com.lume.odonto.entity.RepasseItem;
import br.com.lume.odonto.entity.RepasseLancamento;
import br.com.lume.odonto.entity.RepasseProfissional;
import br.com.lume.planoTratamentoProcedimento.PlanoTratamentoProcedimentoSingleton;
import br.com.lume.profissional.ProfissionalSingleton;
import br.com.lume.repasseItem.RepasseItemSingleton;
import br.com.lume.repasseLancamento.RepasseLancamentoSingleton;
import br.com.lume.repasseProfissional.RepasseProfissionalSingleton;
import br.com.lume.security.entity.Empresa;

@ManagedBean
@ViewScoped
public class FaturamentoMB extends LumeManagedBean<PlanoTratamentoProcedimento> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(FaturamentoMB.class);

    private Profissional profissional;

  //  private ProfissionalBO profissionalBO;

    private List<PlanoTratamentoProcedimento> planoTratamentoProcedimentos = new ArrayList<>();

    private List<PlanoTratamentoProcedimento> procedimentosOutrosProfissionais = new ArrayList<>();

    private List<PlanoTratamentoProcedimento> procedimentosRepasse = new ArrayList<>();

    private List<PlanoTratamentoProcedimento> procedimentosRepasseSelecionados = new ArrayList<>();

    private List<PlanoTratamentoProcedimento> procedimentosSemValor = new ArrayList<>();

    private List<PlanoTratamentoProcedimentoCusto> custos = new ArrayList<>();

    private List<Lancamento> lancamentos = new ArrayList<>();

   // private LancamentoBO lancamentoBO;

   // private CustoBO custoBO;

    private Lancamento lancamento;

    private boolean reservar, pagar;

    private boolean mesesAnteriores;

    private Integer mes;

   // private LancamentoContabilBO lancamentoContabilBO;

  //  private MotivoBO motivoBO;

    private String nomeClinica = "", endTelefoneClinica = "";

    private BigDecimal tarifa;

    private BigDecimal taxa;

    private BigDecimal valorParcial;

 //   private RepasseProfissionalBO repasseProfissionalBO = new RepasseProfissionalBO();

    private RepasseProfissional repasseSelecionado;

    private List<RepasseProfissional> repasses;

   // private RepasseLancamentoBO repasseLancamentoBO = new RepasseLancamentoBO();

    private boolean existeRepasseEmAberto = true;

    private List<RepasseItem> repasseItens;

  //  private RepasseItemBO repasseItemBO = new RepasseItemBO();

  //  private PlanoTratamentoProcedimentoBO planoTratamentoProcedimentoBO = new PlanoTratamentoProcedimentoBO();

    public FaturamentoMB() {
        super(PlanoTratamentoProcedimentoSingleton.getInstance().getBo());
       // profissionalBO = new ProfissionalBO();
      //  lancamentoBO = new LancamentoBO();
      //  lancamentoContabilBO = new LancamentoContabilBO();
     //   custoBO = new CustoBO();
     //   motivoBO = new MotivoBO();
        this.setClazz(PlanoTratamentoProcedimento.class);
        this.carregarPlanoTratamentoProcedimentos();
        mes = Calendar.getInstance().get(Calendar.MONTH) + 1;
        Empresa empresalogada = UtilsFrontEnd.getEmpresaLogada();
        nomeClinica = empresalogada.getEmpStrNme() != null ? empresalogada.getEmpStrNme() : "";
        endTelefoneClinica = (empresalogada.getEmpStrEndereco() != null ? empresalogada.getEmpStrEndereco() + " - " : "") + (empresalogada.getEmpStrCidade() != null ? empresalogada.getEmpStrCidade() + "/" : "") + (empresalogada.getEmpChaUf() != null ? empresalogada.getEmpChaUf() + " - " : "") + (empresalogada.getEmpChaFone() != null ? empresalogada.getEmpChaFone() : "");
    }

    public void carregarRepasseItens() {
        try {
            repasseItens = RepasseItemSingleton.getInstance().getBo().listByRepasseProfissional(repasseSelecionado);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void actionBaixaProcedimento(PlanoTratamentoProcedimento ptp) {
        try {
            ptp.setValorRepassado(ptp.getValorRepasse());
            ptp.setStatusPagamento('G');
            ptp.setDataRepasse(new Date());
            PlanoTratamentoProcedimentoSingleton.getInstance().getBo().persist(ptp);
            carregarPlanoTratamentoProcedimentos();
            addInfo("Procedimento baixado com sucesso.", "");
        } catch (Exception e) {
            log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
            this.addError(Mensagens.ERRO_AO_BUSCAR_REGISTROS, "");
        }
    }

    public void carregarReservarParcial() {
        valorParcial = getEntity().getValorRestante();
    }

    public void actionReservarParcial() {
        if (valorParcial.doubleValue() > getEntity().getValorRestante().doubleValue()) {
            PrimeFaces.current().ajax().addCallbackParam("dlgReservaParcial", false);
            addError("Valor maior que o valor devido.", "");
        } else if (valorParcial.doubleValue() > lancamento.getCredito().doubleValue()) {
            PrimeFaces.current().ajax().addCallbackParam("dlgReservaParcial", false);
            addError("Valor maior que o valor em credito no lançamento. ", "");
        } else {
            try {
                PrimeFaces.current().ajax().addCallbackParam("dlgReservaParcial", true);
                System.out.println(valorParcial);
                PlanoTratamentoProcedimento planoTratamentoProcedimento = this.getEntity();

                BigDecimal valorRepassar = this.retiraTaxa(valorParcial, planoTratamentoProcedimento);
                planoTratamentoProcedimento.setValorRepassado(planoTratamentoProcedimento.getValorRepassado().add(valorRepassar));
                BigDecimal taxaDebitar = this.calcularTaxaDebitar(valorParcial, planoTratamentoProcedimento);
                lancamento.setValorRepassado(lancamento.getValorRepassado().add(valorRepassar));
                if (taxaDebitar != null) {
                    this.addError("Valor retirado do Repasse : " + taxaDebitar, "");
                    planoTratamentoProcedimento.setValorRepasse(planoTratamentoProcedimento.getValorRepasse().subtract(taxaDebitar));
                }
                planoTratamentoProcedimento.setStatusPagamento(PlanoTratamentoProcedimento.PAGAMENTO_RESERVADO);
                planoTratamentoProcedimento.addRepasseLancamento(new RepasseLancamento(lancamento, planoTratamentoProcedimento, valorRepassar, taxaDebitar, RepasseItem.REPASSADO));
                planoTratamentoProcedimento.setDataRepasse(Calendar.getInstance().getTime());
                LancamentoSingleton.getInstance().getBo().merge(lancamento);
                PlanoTratamentoProcedimentoSingleton.getInstance().getBo().merge(planoTratamentoProcedimento);
                this.carregarPlanoTratamentoProcedimentos();
                this.carregarLancamentos();
                if (this.getEntity().getValorRestante().doubleValue() == 0) {
                    this.setEntity(null);
                    lancamentos = null;
                }
                lancamento = null;
            } catch (Exception e) {
                log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
                this.addError(Mensagens.ERRO_AO_BUSCAR_REGISTROS, "");
            }
        }
    }

    public void actionReservar(ActionEvent event) {
        if (this.getEntity() != null && lancamento != null) {
            try {
                PlanoTratamentoProcedimento planoTratamentoProcedimento = this.getEntity();
                BigDecimal faltaRepassar = planoTratamentoProcedimento.getValorRestante();
                BigDecimal creditoLancamento = lancamento.getCredito();
                BigDecimal taxaDebitar = null;
                BigDecimal valorRepassar = null;
                if (creditoLancamento.doubleValue() < faltaRepassar.doubleValue()) {
                    valorRepassar = this.retiraTaxa(creditoLancamento, planoTratamentoProcedimento);
                    planoTratamentoProcedimento.setValorRepassado(planoTratamentoProcedimento.getValorRepassado().add(valorRepassar));
                    taxaDebitar = this.calcularTaxaDebitar(creditoLancamento, planoTratamentoProcedimento);
                    lancamento.setValorRepassado(lancamento.getValorRepassado().add(creditoLancamento));
                } else {
                    valorRepassar = this.retiraTaxa(faltaRepassar, planoTratamentoProcedimento);
                    planoTratamentoProcedimento.setValorRepassado(planoTratamentoProcedimento.getValorRepassado().add(valorRepassar));
                    taxaDebitar = this.calcularTaxaDebitar(faltaRepassar, planoTratamentoProcedimento);
                    lancamento.setValorRepassado(lancamento.getValorRepassado().add(faltaRepassar));
                }
                if (taxaDebitar != null) {
                    this.addError("Valor retirado do Repasse : " + taxaDebitar, "");
                    planoTratamentoProcedimento.setValorRepasse(planoTratamentoProcedimento.getValorRepasse().subtract(taxaDebitar));
                }
                if (planoTratamentoProcedimento.getValorRepassado().doubleValue() == planoTratamentoProcedimento.getValorRepasse().doubleValue()) {
                    planoTratamentoProcedimento.setStatusPagamento(PlanoTratamentoProcedimento.PAGAMENTO_RESERVADO);
                }
                planoTratamentoProcedimento.addRepasseLancamento(new RepasseLancamento(lancamento, planoTratamentoProcedimento, valorRepassar, taxaDebitar, RepasseItem.REPASSADO));
                planoTratamentoProcedimento.setDataRepasse(Calendar.getInstance().getTime());
                LancamentoSingleton.getInstance().getBo().merge(lancamento);
                PlanoTratamentoProcedimentoSingleton.getInstance().getBo().merge(planoTratamentoProcedimento);
                this.carregarPlanoTratamentoProcedimentos();
                this.carregarLancamentos();
                if (this.getEntity().getValorRestante().doubleValue() == 0) {
                    this.setEntity(null);
                    lancamentos = null;
                }
                lancamento = null;
            } catch (Exception e) {
                log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
                this.addError(Mensagens.ERRO_AO_BUSCAR_REGISTROS, "");
            }
        }
    }

    public void actionCarregarProcedimentosSemValor(ActionEvent event) {
        try {
            procedimentosSemValor = PlanoTratamentoProcedimentoSingleton.getInstance().getBo().listProcedimentosSemValor(profissional, mes, mesesAnteriores);
        } catch (Exception e) {
            log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
            this.addError(Mensagens.ERRO_AO_BUSCAR_REGISTROS, "");
        }
    }

    public void checkOrUncheckAllRepassar(ToggleSelectEvent event) {
        if (event.isSelected()) {
            procedimentosRepasseSelecionados = procedimentosRepasse;
        } else {
            procedimentosRepasseSelecionados = new ArrayList<>();
        }
    }

    public void actionRemoverReservaRepasse(PlanoTratamentoProcedimento ptp) {
        this.setEntity(ptp);
        this.actionRemoverReserva(null);
    }

    public void actionRemoverReserva(ActionEvent event) {
        try {
            List<RepasseLancamento> lancamentos = RepasseLancamentoSingleton.getInstance().getBo().listByPlanoTratamentoProcedimento(this.getEntity().getId());
            if (lancamentos != null && !lancamentos.isEmpty()) {
                for (RepasseLancamento rl : lancamentos) {
                    BigDecimal valorVoltar = rl.getValor().add(rl.getValorDesconto() != null ? rl.getValorDesconto() : new BigDecimal(0));
                    Lancamento l = LancamentoSingleton.getInstance().getBo().find(rl.getLancamento().getId());
                    // lancamentoBO.refresh(l);
                    l.setValorRepassado(l.getValorRepassado().subtract(valorVoltar));
                    this.getEntity().setValorRepassado(this.getEntity().getValorRepassado().subtract(rl.getValor()));
                    if (rl.getValorDesconto() != null) {
                        this.getEntity().setValorRepasse(this.getEntity().getValorRepasse().add(rl.getValorDesconto()));
                    }
                    if (this.getEntity().getRepasseLancamentos() != null && !this.getEntity().getRepasseLancamentos().isEmpty()) {
                        this.getEntity().getRepasseLancamentos().remove(rl);
                    }
                    LancamentoSingleton.getInstance().getBo().persist(l);
                    RepasseLancamentoSingleton.getInstance().getBo().remove(rl);
                }
                this.getbO().persist(this.getEntity());
                this.getbO().refresh(this.getEntity());
                this.actionFiltrar(event);
                this.carregarLancamentos();
                this.addInfo("Reserva removida com sucesso.", "");
            }
        } catch (Exception e) {
            log.error(Mensagens.ERRO_AO_REMOVER_REGISTRO, e);
            this.addError(Mensagens.ERRO_AO_REMOVER_REGISTRO, "");
        }
    }

    private BigDecimal calcularTaxaDebitar(BigDecimal valor, PlanoTratamentoProcedimento planoTratamentoProcedimento) {
        if (planoTratamentoProcedimento.getTipoRemuneracaoProfissionalCalc() != null && planoTratamentoProcedimento.getTipoRemuneracaoProfissionalCalc().equals(Profissional.PORCENTAGEM)) {
            if (taxa != null && taxa.doubleValue() != 0d) {
                BigDecimal multiply = valor.multiply(taxa.divide(new BigDecimal(100)));
                multiply = multiply.setScale(2, BigDecimal.ROUND_HALF_UP);
                return multiply;
            } else if (tarifa != null && tarifa.doubleValue() != 0d) {
                return tarifa;
            }
        }
        return null;
    }

    private BigDecimal retiraTaxa(BigDecimal valor, PlanoTratamentoProcedimento planoTratamentoProcedimento) {
        if (planoTratamentoProcedimento.getTipoRemuneracaoProfissionalCalc() != null && planoTratamentoProcedimento.getTipoRemuneracaoProfissionalCalc().equals(Profissional.PORCENTAGEM)) {
            if (taxa != null && taxa.doubleValue() != 0d) {
                BigDecimal multiply = valor.multiply(new BigDecimal(1).subtract(taxa.divide(new BigDecimal(100))));
                multiply = multiply.setScale(2, BigDecimal.ROUND_HALF_UP);
                return multiply;
            } else if (tarifa != null && tarifa.doubleValue() != 0d) {
                return valor.subtract(tarifa);
            }
        }
        return valor;
    }

    public void actionRepassar(ActionEvent event) {
        try {
            RepasseProfissional repasseProfissional = new RepasseProfissional(Calendar.getInstance().getTime(), profissional, RepasseItem.REPASSADO);
            List<RepasseItem> repasseItens = new ArrayList<>();
            for (PlanoTratamentoProcedimento ptp : procedimentosRepasseSelecionados) {
                repasseItens.add(new RepasseItem(repasseProfissional, ptp, RepasseItem.REPASSADO, ptp.getValorReservado()));
            }
            repasseProfissional.setRepasseItens(repasseItens);
            RepasseProfissionalSingleton.getInstance().getBo().persist(repasseProfissional);
            procedimentosRepasseSelecionados = null;
            this.actionFiltrar(event);
        } catch (Exception e) {
            log.error(Mensagens.ERRO_AO_SALVAR_REGISTRO, e);
            this.addError(Mensagens.ERRO_AO_SALVAR_REGISTRO, "");
        }
    }

    public void actionPagar(ActionEvent event) {
        try {
            if (repasseSelecionado != null && repasseSelecionado.getRepasseItens() != null && !repasseSelecionado.getRepasseItens().isEmpty()) {
                for (RepasseItem ri : repasseSelecionado.getRepasseItens()) {
                    this.geraLancamentoContabil(ri.getPlanoTratamentoProcedimento().getValorReservado());
                    String status = RepasseItem.PAGO_PARCIAL;
                    if (ri.getPlanoTratamentoProcedimento().getValorRepassado().doubleValue() == ri.getPlanoTratamentoProcedimento().getValorRepasse().doubleValue()) {
                        status = RepasseItem.PAGO_COMPLETO;
                    }
                    ri.setStatus(status);
                    for (RepasseLancamento rl : ri.getPlanoTratamentoProcedimento().getRepasseLancamentos()) {
                        rl.setStatus(RepasseItem.PAGO_COMPLETO);
                    }
                    this.getbO().merge(ri.getPlanoTratamentoProcedimento());
                }
                repasseSelecionado.setDataPagamento(Calendar.getInstance().getTime());
                repasseSelecionado.setProfissionalPagou(Configurar.getInstance().getConfiguracao().getProfissionalLogado());
                repasseSelecionado.setStatus(RepasseItem.PAGO_COMPLETO);
                RepasseProfissionalSingleton.getInstance().getBo().merge(repasseSelecionado);
                this.actionFiltrar(event);
                this.addInfo("Procedimentos Pagos com sucesso! Para conferir acessar o relatório de repasses.", "");
            }
        } catch (Exception e) {
            log.error(Mensagens.ERRO_AO_SALVAR_REGISTRO, e);
            this.addError(Mensagens.ERRO_AO_SALVAR_REGISTRO, "");
        }
    }

    public void actionVerificaTarifa() {
        if (this.getLancamento().getTarifa() != null) {
            tarifa = this.getLancamento().getTarifa().getTarifa();
            taxa = this.getLancamento().getTarifa().getTaxa();
            if (tarifa != null && tarifa.doubleValue() > 0) {
                this.addError("Valor descontado de tarifa: " + tarifa.doubleValue(), "");
            }
            if (taxa != null && taxa.doubleValue() > 0) {
                this.addError("Valor descontado de taxa: " + taxa.doubleValue(), "");
            }
        } else {
            tarifa = null;
            taxa = null;
        }
    }

    @Override
    public void setEntity(PlanoTratamentoProcedimento planoTratamentoProcedimento) {
        this.carregarProcedimentosOutrosProfissioanais(planoTratamentoProcedimento);
        super.setEntity(planoTratamentoProcedimento);
    }

    private void carregarProcedimentosOutrosProfissioanais(PlanoTratamentoProcedimento planoTratamentoProcedimento) {
        if (planoTratamentoProcedimento != null) {
            procedimentosOutrosProfissionais = new ArrayList<>();
            Profissional procedimentoSelecionadoProfissional = planoTratamentoProcedimento.getFinalizadoPorProfissional();
            List<PlanoTratamentoProcedimento> ptp = planoTratamentoProcedimento.getPlanoTratamento().getPlanoTratamentoProcedimentos();
            for (PlanoTratamentoProcedimento p : ptp) {
                if (p.getFinalizadoPorProfissional() != null && !p.getFinalizadoPorProfissional().equals(procedimentoSelecionadoProfissional)) {
                    // System.out.println(p.getProcedimento().getDescricao() + " FINALIZADO POR : " +
                    // p.getFinalizadoPorProfissional().getDadosBasico().getNome());
                    procedimentosOutrosProfissionais.add(p);
                }
            }
        }
    }

    private void geraLancamentoContabil(BigDecimal valor) throws Exception, BusinessException, TechnicalException {
        LancamentoContabil lc = new LancamentoContabil();
        Motivo motivo = MotivoSingleton.getInstance().getBo().findBySigla(Motivo.PAGAMENTO_PROFISSIONAL);
        lc.setIdEmpresa(idEmpresa);
        lc.setTipo(motivo.getTipo());
        lc.setDadosBasico(profissional.getDadosBasico());
        lc.setMotivo(motivo);
        lc.setValor(valor);
        lc.setNotaFiscal(null);
        // lc.setLancamento(lancamento);
        lc.setData(Calendar.getInstance().getTime());
        LancamentoContabilSingleton.getInstance().getBo().persist(lc);
    }

    private void carregarPlanoTratamentoProcedimentos() {
        if (profissional != null) {
            repasseItens = null;
            repasseSelecionado = null;
            planoTratamentoProcedimentos = PlanoTratamentoProcedimentoSingleton.getInstance().getBo().listNaoPagosTotalmente(profissional.getId(), mes, mesesAnteriores);
            if (planoTratamentoProcedimentos == null || planoTratamentoProcedimentos.isEmpty()) {
                this.setEntity(null);
            }
            this.carregarRepassar();
            this.carregarRepassesProfissional();
            this.verificaExisteRepasseEmAberto();
        }
    }

    private void verificaExisteRepasseEmAberto() {
        try {
            existeRepasseEmAberto = RepasseProfissionalSingleton.getInstance().getBo().existeRepasseEmAberto(profissional.getId());
            if (existeRepasseEmAberto) {
                this.addError("Enquanto não for pago os repasses antigos não é possível fazer novas reservas.", "");
            }
        } catch (Exception e) {
            log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
            this.addError(Mensagens.ERRO_AO_BUSCAR_REGISTROS, "");
        }
    }

    private void carregarRepassesProfissional() {
        try {
            repasses = RepasseProfissionalSingleton.getInstance().getBo().listByProfissional(profissional.getId());
        } catch (Exception e) {
            log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
            this.addError(Mensagens.ERRO_AO_BUSCAR_REGISTROS, "");
        }
    }

    public void actionCarregarReservados() {
        repasseSelecionado.getRepasseItens();
    }

    private void carregarRepassar() {
        procedimentosRepasse = PlanoTratamentoProcedimentoSingleton.getInstance().getBo().listRepassar(profissional.getId(), mes, mesesAnteriores);
    }

    public void actionFiltrar(ActionEvent event) {
        this.carregarPlanoTratamentoProcedimentos();
    }

    public void carregarLancamentos() {
        try {
            if (this.getEntity().getPlanoTratamento() != null) {
                lancamentos = LancamentoSingleton.getInstance().getBo().listComCredito(this.getEntity().getPlanoTratamento().getId());
                lancamento = null;
            }
        } catch (Exception e) {
            log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
            this.addError(Mensagens.ERRO_AO_BUSCAR_REGISTROS, "");
        }
    }

    public void carregaCustos(PlanoTratamentoProcedimento ptp) throws Exception {
        custos = new ArrayList<>();
        custos = CustoSingleton.getInstance().getBo().listbyIdCusto(ptp.getId());
    }

    public List<Profissional> geraSugestoesProfissional(String query) {
        List<Profissional> sugestoes = new ArrayList<>();
        List<Profissional> profissionais = new ArrayList<>();
        try {
            profissionais = ProfissionalSingleton.getInstance().getBo().listDentistasByEmpresa(idEmpresa);
            for (Profissional p : profissionais) {
                if (Normalizer.normalize(p.getDadosBasico().getNome().toLowerCase(), Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "").toLowerCase().contains(
                        Normalizer.normalize(query, Normalizer.Form.NFD).toLowerCase())) {
                    sugestoes.add(p);
                }
            }
            Collections.sort(sugestoes);
        } catch (Exception e) {
            log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
            this.addError(Mensagens.ERRO_AO_BUSCAR_REGISTROS, "");
        }
        return sugestoes;
    }

    public Profissional getProfissional() {
        return profissional;
    }

    public void setProfissional(Profissional profissional) {
        this.profissional = profissional;
    }

    public List<PlanoTratamentoProcedimento> getPlanoTratamentoProcedimentos() {
        return planoTratamentoProcedimentos;
    }

    public void setPlanoTratamentoProcedimentos(List<PlanoTratamentoProcedimento> planoTratamentoProcedimentos) {
        this.planoTratamentoProcedimentos = planoTratamentoProcedimentos;
    }

    public List<Lancamento> getLancamentos() {
        return lancamentos;
    }

    public void setLancamentos(List<Lancamento> lancamentos) {
        this.lancamentos = lancamentos;
    }

    public Lancamento getLancamento() {
        return lancamento;
    }

    public void setLancamento(Lancamento lancamento) {
        this.lancamento = lancamento;
    }

    public boolean isReservar() {
        if (lancamento != null && lancamento.getId() != 0 && this.getEntity() != null && this.getEntity().getId() != 0) {
            reservar = true;
        } else {
            reservar = false;
        }
        return reservar;
    }

    public Integer getMes() {
        return mes;
    }

    public void setMes(Integer mes) {
        this.mes = mes;
    }

    public List<PlanoTratamentoProcedimento> getProcedimentosOutrosProfissionais() {
        return procedimentosOutrosProfissionais;
    }

    public void setProcedimentosOutrosProfissionais(List<PlanoTratamentoProcedimento> procedimentosOutrosProfissionais) {
        this.procedimentosOutrosProfissionais = procedimentosOutrosProfissionais;
    }

    public boolean isMesesAnteriores() {
        return mesesAnteriores;
    }

    public void setMesesAnteriores(boolean mesesAnteriores) {
        this.mesesAnteriores = mesesAnteriores;
    }

    public boolean isPagar() {
        if (this.isRecibo() && RepasseItem.REPASSADO.equals(repasseSelecionado.getStatus())) {
            pagar = true;
        } else {
            pagar = false;
        }
        return pagar;
    }

    public boolean isRecibo() {
        if (repasseSelecionado != null && repasseSelecionado.getRepasseItens() != null && !repasseSelecionado.getRepasseItens().isEmpty()) {
            return true;
        } else {
            return false;
        }
    }

    public void setPagar(boolean pagar) {
        this.pagar = pagar;
    }

    public void setReservar(boolean reservar) {
        this.reservar = reservar;
    }

    public BigDecimal getValorRepassadoTotal() {
        BigDecimal valorRepassadoTotal = new BigDecimal(0);
        if (repasseSelecionado != null && repasseSelecionado.getRepasseItens() != null && !repasseSelecionado.getRepasseItens().isEmpty()) {
            for (RepasseItem ri : repasseSelecionado.getRepasseItens()) {
                valorRepassadoTotal = valorRepassadoTotal.add(ri.getValor());
            }
        }
        return valorRepassadoTotal;
    }

    public BigDecimal getValorDebitoTotal() {
        BigDecimal valorDebitoTotal = new BigDecimal(0);
        if (repasseSelecionado != null && repasseSelecionado.getRepasseItens() != null && !repasseSelecionado.getRepasseItens().isEmpty()) {
            for (RepasseItem ri : repasseSelecionado.getRepasseItens()) {
                valorDebitoTotal = valorDebitoTotal.add(ri.getPlanoTratamentoProcedimento().getValorRestante());
            }
        }
        return valorDebitoTotal;
    }

    public String getNomeClinica() {
        return nomeClinica;
    }

    public void setNomeClinica(String nomeClinica) {
        this.nomeClinica = nomeClinica;
    }

    public String getEndTelefoneClinica() {
        return endTelefoneClinica;
    }

    public void setEndTelefoneClinica(String endTelefoneClinica) {
        this.endTelefoneClinica = endTelefoneClinica;
    }

    public String getMesStr() {
        if (mes != null && mes > 0) {
            Calendar c = Calendar.getInstance();
            c.set(Calendar.MONTH, mes - 1);
            SimpleDateFormat sdf = new SimpleDateFormat("MMMM");
            return sdf.format(c.getTime());
        }
        return "";
    }

    public List<PlanoTratamentoProcedimentoCusto> getCustos() {
        return custos;
    }

    public void setCustos(List<PlanoTratamentoProcedimentoCusto> custos) {
        this.custos = custos;
    }

    public List<PlanoTratamentoProcedimento> getProcedimentosRepasse() {
        return procedimentosRepasse;
    }

    public void setProcedimentosRepasse(List<PlanoTratamentoProcedimento> procedimentosRepasse) {
        this.procedimentosRepasse = procedimentosRepasse;
    }

    public List<PlanoTratamentoProcedimento> getProcedimentosRepasseSelecionados() {
        return procedimentosRepasseSelecionados;
    }

    public void setProcedimentosRepasseSelecionados(List<PlanoTratamentoProcedimento> procedimentosRepasseSelecionados) {
        this.procedimentosRepasseSelecionados = procedimentosRepasseSelecionados;
    }

    public RepasseProfissional getRepasseSelecionado() {
        return repasseSelecionado;
    }

    public void setRepasseSelecionado(RepasseProfissional repasseSelecionado) {
        this.repasseSelecionado = repasseSelecionado;
    }

    public List<RepasseProfissional> getRepasses() {
        return repasses;
    }

    public void setRepasses(List<RepasseProfissional> repasses) {
        this.repasses = repasses;
    }

    public boolean isExisteRepasseEmAberto() {
        return existeRepasseEmAberto;
    }

    public void setExisteRepasseEmAberto(boolean existeRepasseEmAberto) {
        this.existeRepasseEmAberto = existeRepasseEmAberto;
    }

    public List<RepasseItem> getRepasseItens() {
        return repasseItens;
    }

    public void setRepasseItens(List<RepasseItem> repasseItens) {
        this.repasseItens = repasseItens;
    }

    public BigDecimal getValorParcial() {
        return valorParcial;
    }

    public void setValorParcial(BigDecimal valorParcial) {
        this.valorParcial = valorParcial;
    }

    public List<PlanoTratamentoProcedimento> getProcedimentosSemValor() {
        return procedimentosSemValor;
    }

    public void setProcedimentosSemValor(List<PlanoTratamentoProcedimento> procedimentosSemValor) {
        this.procedimentosSemValor = procedimentosSemValor;
    }

}
