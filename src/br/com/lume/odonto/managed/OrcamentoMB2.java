package br.com.lume.odonto.managed;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ActionEvent;

import org.apache.log4j.Logger;
import org.primefaces.context.RequestContext;
import org.primefaces.event.CloseEvent;

import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.Utils;
import br.com.lume.configuracao.Configurar;
import br.com.lume.dominio.DominioSingleton;
import br.com.lume.lancamento.LancamentoSingleton;
import br.com.lume.odonto.entity.Dominio;
import br.com.lume.odonto.entity.Lancamento;
import br.com.lume.odonto.entity.Orcamento;
import br.com.lume.odonto.entity.PlanoTratamento;
import br.com.lume.odonto.entity.PlanoTratamentoProcedimento;
import br.com.lume.odonto.entity.Procedimento;
import br.com.lume.odonto.util.OdontoMensagens;
import br.com.lume.orcamento.OrcamentoSingleton;
import br.com.lume.planoTratamento.PlanoTratamentoSingleton;
import br.com.lume.planoTratamentoProcedimento.PlanoTratamentoProcedimentoSingleton;
import br.com.lume.security.bo.EmpresaBO;
import br.com.lume.security.entity.Empresa;

@ManagedBean
@ViewScoped
public class OrcamentoMB2 extends LumeManagedBean<Orcamento> {

    private Logger log = Logger.getLogger(OrcamentoMB2.class);

    private static final long serialVersionUID = 1L;

    private Date dataCredito;

    private int numeroParcelas;

    private int parcela;

    @ManagedProperty(value = "#{planoTratamentoMB}")
    private PlanoTratamentoMB planoTratamentoMB;

    private List<Lancamento> lancamentos = new ArrayList<>();

    private BigDecimal valorTotal, desconto, totalPago, valorTotalOriginal, porcentagem;

    private boolean porProcedimento, ciclico;

  

    private String nomeClinica;

    private String endTelefoneClinica;

    private String formaPagamento = "";

    private List<Dominio> formasPagamento;

    public OrcamentoMB2() {
        super(OrcamentoSingleton.getInstance().getBo());
     
        this.setClazz(Orcamento.class);
        Empresa empresalogada = Configurar.getInstance().getConfiguracao().getEmpresaLogada();
        nomeClinica = empresalogada.getEmpStrNme() != null ? empresalogada.getEmpStrNme() : "";
        endTelefoneClinica = (empresalogada.getEmpStrEndereco() != null ? empresalogada.getEmpStrEndereco() + " - " : "") + (empresalogada.getEmpStrCidade() != null ? empresalogada.getEmpStrCidade() + "/" : "") + (empresalogada.getEmpChaUf() != null ? empresalogada.getEmpChaUf() + " - " : "") + (empresalogada.getEmpChaFone() != null ? empresalogada.getEmpChaFone() : "");
        try {
            porcentagem = new BigDecimal(0);
            formasPagamento = DominioSingleton.getInstance().getBo().listByEmpresaAndObjetoAndTipo("pagamento", "forma");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void calculaDesconto() {
        BigDecimal valorTotalComPago = valorTotalOriginal.subtract(totalPago);
        valorTotal = valorTotalComPago.multiply(porcentagem);
        valorTotal = valorTotalComPago.subtract(valorTotal.divide(new BigDecimal(100), MathContext.DECIMAL32).setScale(2, BigDecimal.ROUND_HALF_UP));
    }

    public void handleClose(CloseEvent event) {
        this.setEntity(new Orcamento());
        numeroParcelas = 1;
        dataCredito = null;
        lancamentos = new ArrayList<>();
        valorTotal = valorTotalOriginal;
        planoTratamentoMB.zeraTotais();
        porcentagem = new BigDecimal(0);
    }

    public void carregaTela() {
        try {
            lancamentos = new ArrayList<>();
            this.setEntity(null);
            PlanoTratamento pt = planoTratamentoMB.getEntity();
            planoTratamentoMB.setEntity(PlanoTratamentoSingleton.getInstance().getBo().find(pt.getId()));

            valorTotal = planoTratamentoMB.getEntity().getValorTotalDesconto();
            valorTotalOriginal = planoTratamentoMB.getEntity().isAlterouvaloresdesconto() ? planoTratamentoMB.getEntity().getValorTotalDesconto() : planoTratamentoMB.getEntity().getValorTotal();

            Orcamento orcamento = OrcamentoSingleton.getInstance().getBo().findUltimoOrcamentoEmAberto(planoTratamentoMB.getEntity().getId());

            this.setEntity(orcamento);
            porcentagem = planoTratamentoMB.getEntity().getDesconto();

            if (orcamento != null) {
                valorTotal = orcamento.getValorTotal();

            }
            totalPago = new BigDecimal(0);
            for (Lancamento lan : LancamentoSingleton.getInstance().getBo().listByPlanoTratamentoOrcamentoNaoExcluido(planoTratamentoMB.getEntity())) {
                if (lan.getDataPagamento() != null) {
                    totalPago = totalPago.add(lan.getValorOriginal());
                }
            }
            valorTotal = valorTotal.subtract(totalPago);
            valorTotal = (valorTotal.compareTo(BigDecimal.ZERO)) < 0 ? BigDecimal.ZERO : valorTotal;
            desconto = valorTotal;
            if (this.getEntity() != null && this.getEntity().getId() != 0) {
                lancamentos = LancamentoSingleton.getInstance().getBo().listLancamentosNaoPagos(this.getEntity());
                numeroParcelas = lancamentos.size();
                if (!lancamentos.isEmpty()) {
                    dataCredito = lancamentos.get(0).getDataCredito();
                }
            } else {
                this.setEntity(new Orcamento());
                numeroParcelas = 1;
                dataCredito = null;
                lancamentos = new ArrayList<>();
            }
            dataCredito = new Date();
        } catch (Exception e) {
            log.error("Erro no carregaTela", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
        }
    }

    public void simulaLancamento() {
        if (porcentagem != null) {
            if (!isDentistaAdmin() && Configurar.getInstance().getConfiguracao().getProfissionalLogado().getDesconto() == null || Configurar.getInstance().getConfiguracao().getProfissionalLogado().getDesconto().doubleValue() < porcentagem.doubleValue()) {
                porcentagem = Configurar.getInstance().getConfiguracao().getProfissionalLogado().getDesconto() != null ? Configurar.getInstance().getConfiguracao().getProfissionalLogado().getDesconto() : new BigDecimal(0);
                this.addError(OdontoMensagens.getMensagem("erro.orcamento.desconto.maior"), "");
                calculaDesconto();
                lancamentos = new ArrayList<>();
                return;
            } else {
                calculaDesconto();
            }
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.DAY_OF_MONTH, -1);
        this.getEntity().setDataAprovacao(new java.util.Date());
        this.getEntity().setPlanoTratamento(planoTratamentoMB.getEntity());
        this.getEntity().setQuantidadeParcelas(numeroParcelas);
        this.getEntity().setValorTotal(valorTotal);
        lancamentos = new ArrayList<>();
        Lancamento lancamento;
        Calendar instanceData = Calendar.getInstance();
        instanceData.setTime(dataCredito);
        parcela = 1;
        if (porProcedimento) {
            for (PlanoTratamentoProcedimento ptp : planoTratamentoMB.getPlanoTratamentoProcedimentos()) {
                if (!ptp.getProcedimento().isCiclicoBoo()) {
                    if (!this.checkMinMaxOptions(planoTratamentoMB.getPlanoTratamentoProcedimentos().size(), ptp.getValor())) {
                        return;
                    }
                }
            }
            BigDecimal descontoTotal = valorTotal.add(totalPago).divide(valorTotalOriginal, 5, BigDecimal.ROUND_UP).multiply(new BigDecimal(100)).subtract(new BigDecimal(100)).negate();
            BigDecimal valor;
            for (PlanoTratamentoProcedimento ptp : planoTratamentoMB.getPlanoTratamentoProcedimentos()) {
                valor = ptp.getValorDesconto().subtract(ptp.getValorDesconto().multiply(descontoTotal).divide(new BigDecimal(100), 5, BigDecimal.ROUND_UP));
                if (parcela != 1) {
                    instanceData.add(Calendar.MONTH, 1);
                }
                lancamento = new Lancamento();
                lancamento.setNumeroParcela(parcela);
                lancamento.setDataCredito(instanceData.getTime());
                lancamento.setValor(valor);
                lancamento.setValorOriginal(valor);
                lancamento.setPlanoTratamentoProcedimento(ptp);
                if (formaPagamento != null && !formaPagamento.isEmpty()) {
                    lancamento.setFormaPagamento(formaPagamento);
                }
                Lancamento lanca = LancamentoSingleton.getInstance().getBo().findByPlanoTratamentoProcedimento(ptp);
                if (lanca == null) {
                    lancamentos.add(lancamento);
                    parcela++;
                }
            }
        } else {
            if (this.getEntity().getPlanoTratamento().getPlanoTratamentoProcedimentos().get(0).getProcedimento().isCiclicoBoo()) {
                // if (lancamentos == null || lancamentos.isEmpty())
                lancamentos = LancamentoSingleton.getInstance().getBo().listLancamentosNaoPagos(this.getEntity());
            }
            parcela = lancamentos.size() + 1;
            for (; parcela <= numeroParcelas; parcela++) {
                if (parcela != 1) {
                    instanceData.add(Calendar.MONTH, 1);
                    dataCredito = instanceData.getTime();
                }
                BigDecimal valor = new BigDecimal(0);
                if (this.isCiclico()) {
                    valor = this.checkTotal();
                } else if ((this.getEntity().getValorTotal().setScale(3, BigDecimal.ROUND_HALF_DOWN).abs().multiply((new BigDecimal(100)))).floatValue() % numeroParcelas == 0) {
                    valor = (this.getEntity().getValorTotal().divide(new BigDecimal(numeroParcelas), 2, BigDecimal.ROUND_UP));
                    if (valor.multiply(new BigDecimal(numeroParcelas)).compareTo(this.getEntity().getValorTotal().setScale(3, BigDecimal.ROUND_HALF_DOWN).abs()) > 0) {
                        valor = (this.getEntity().getValorTotal().divide(new BigDecimal(numeroParcelas), 2, BigDecimal.ROUND_DOWN));
                    }
                } else {
                    valor = (this.getEntity().getValorTotal().divide(new BigDecimal(numeroParcelas), 2, BigDecimal.ROUND_DOWN));
                }
                if (!this.getEntity().getPlanoTratamento().getPlanoTratamentoProcedimentos().get(0).getProcedimento().isCiclicoBoo()) {
                    if (!this.checkMinMaxOptions(numeroParcelas, valor)) {
                        return;
                    }
                }
                lancamento = new Lancamento();
                lancamento.setNumeroParcela(parcela);
                lancamento.setDataCredito(dataCredito);
                lancamento.setValor(valor);
                lancamento.setValorOriginal(valor);
                if (formaPagamento != null && !formaPagamento.isEmpty()) {
                    lancamento.setFormaPagamento(formaPagamento);
                }
                lancamentos.add(lancamento);
            }
            dataCredito = lancamentos.get(0).getDataCredito();
        }
    }

    public BigDecimal checkTotal() {
        BigDecimal total = BigDecimal.ZERO;
        List<Procedimento> ps = new ArrayList<>();
        for (PlanoTratamentoProcedimento ptp : this.getEntity().getPlanoTratamento().getPlanoTratamentoProcedimentos()) {
            if (!ps.contains(ptp.getProcedimento())) {
                total = total.add(ptp.getValorDesconto());
                ps.add(ptp.getProcedimento());
            }
        }
        return total;
    }

    public boolean checkMinMaxOptions(int parcela, BigDecimal valor) {
        try {
            int quantidadeParcelaMaxima = 0;
            if (isAdministrador()) {
                quantidadeParcelaMaxima = Integer.parseInt(DominioSingleton.getInstance().getBo().findByEmpresaAndObjetoAndTipoAndNome("orcamento", "parcela", "quantidadeMaxima").getValor());
            } else {
                quantidadeParcelaMaxima = Integer.parseInt(DominioSingleton.getInstance().getBo().findByEmpresaAndObjetoAndTipoAndNome("orcamento", "parcela", "quantidadeMaximaNormal").getValor());
            }
            BigDecimal valorMinimo = new BigDecimal(DominioSingleton.getInstance().getBo().findByEmpresaAndObjetoAndTipoAndValor("orcamento", "parcela", "VM").getNome());
            if (quantidadeParcelaMaxima < parcela) {
                log.error(OdontoMensagens.getMensagem("erro.orcamento.parcelaMaxima").replaceFirst("\\{1\\}", String.valueOf(quantidadeParcelaMaxima)));
                this.addError(OdontoMensagens.getMensagem("erro.orcamento.parcelaMaxima").replaceFirst("\\{1\\}", String.valueOf(quantidadeParcelaMaxima)), "");
                return false;
            } else if (parcela > 1 && !porProcedimento) {
                if (valorMinimo.compareTo(valor) > 0) {
                    log.error(OdontoMensagens.getMensagem("erro.orcamento.valorMinimo").replaceFirst("\\{1\\}", String.valueOf(valorMinimo)));
                    this.addError(OdontoMensagens.getMensagem("erro.orcamento.valorMinimo").replaceFirst("\\{1\\}", String.valueOf(valorMinimo)), "");
                    return false;
                }
            }
        } catch (Exception e) {
            log.error(OdontoMensagens.getMensagem("erro.orcamento.configuracao"), e);
            this.addError(OdontoMensagens.getMensagem("erro.orcamento.configuracao"), "");
            return false;
        }
        return true;
    }

    private boolean validaTotalLancamento() {

        BigDecimal valorTotalLancamento = new BigDecimal(0);
        for (Lancamento lancamento : lancamentos) {
            if (lancamento.getDataPagamento() == null) {
                valorTotalLancamento = valorTotalLancamento.add(lancamento.getValor().setScale(2, BigDecimal.ROUND_HALF_UP));
            }
        }

//        if (totalPago.doubleValue() != 0) {
//            this.addError("Não é possivel mudar as parcelas quando já existe pagamentos efetuados.", "");
//            return false;
//        }

        if (valorTotalLancamento.doubleValue() == valorTotal.doubleValue()) {
            return true;
        }
        BigDecimal result = valorTotal.subtract(valorTotalLancamento);
        valorTotalLancamento = valorTotalLancamento.add(result);
        lancamentos.get(0).setValor(lancamentos.get(0).getValor().add(result));
        lancamentos.get(0).setValorOriginal(lancamentos.get(0).getValor());
        log.error(OdontoMensagens.getMensagem("erro.orcamento.lancamento.valor2"));
        this.addError(OdontoMensagens.getMensagem("erro.orcamento.lancamento.valor2"), "");
        return false;
    }

    private void calculaRepasses(PlanoTratamento pt) {
        for (PlanoTratamentoProcedimento ptp : planoTratamentoMB.getPlanoTratamentoProcedimentos()) {
            System.out.println(ptp.getValorDesconto().doubleValue() + " != " + ptp.getValorAnterior().doubleValue());
            if (ptp.getStatus() != null && ptp.getStatus().equals("F") && ptp.getValorDesconto().doubleValue() != ptp.getValorAnterior().doubleValue()) {
                ptp.setValorRepasse(PlanoTratamentoProcedimentoSingleton.getInstance().getBo().findValorRepasse(ptp));
                try {
                    PlanoTratamentoProcedimentoSingleton.getInstance().getBo().persist(ptp);
                } catch (Exception e) {
                    log.error("Erro no calculaRepasses", e);
                    this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
                }
            }
        }
    }

    @Override
    public void actionPersist(ActionEvent event) {
        BigDecimal valorTotalPlano = planoTratamentoMB.getEntity().getValorTotal();
        BigDecimal valorTotalDesconto = planoTratamentoMB.getEntity().getValorTotalDesconto();
        try {
            if (this.validaTotalLancamento()) {
                PlanoTratamentoProcedimento ptp = planoTratamentoMB.getPlanoTratamentoProcedimentos().get(0);
                this.getEntity().setDataAprovacao(new Date());
                this.getEntity().setPlanoTratamento(planoTratamentoMB.getEntity());
                this.getEntity().setQuantidadeParcelas(numeroParcelas);
                BigDecimal desconto = valorTotalPlano.subtract(valorTotal.add(totalPago));
                this.getEntity().setValorTotal(valorTotalPlano.subtract(desconto));
                this.getEntity().setProfissional(Configurar.getInstance().getConfiguracao().getProfissionalLogado());
                for (Lancamento l : lancamentos) {
                    if (l.getValor().compareTo(l.getValorOriginal()) != 0) {
                        l.setValorOriginal(l.getValor());
                    }
                }
                this.getbO().persist(this.getEntity());
                List<Lancamento> ls = LancamentoSingleton.getInstance().getBo().listLancamentosNaoPagos(this.getEntity());
                if (ls != null) {
                    for (Lancamento l : ls) {
                        LancamentoSingleton.getInstance().getBo().remove(l);
                    }
                }
                for (Lancamento lancamento : lancamentos) {
                    lancamento.setOrcamento(this.getEntity());
                    LancamentoSingleton.getInstance().getBo().persist(lancamento);
                }
                PlanoTratamento pt = PlanoTratamentoSingleton.getInstance().getBo().find(this.getEntity().getPlanoTratamento());
                pt.setValorTotal(valorTotalPlano);
                pt.setValorTotalDesconto(valorTotalDesconto);
                pt.setDesconto(porcentagem);
                PlanoTratamentoSingleton.getInstance().getBo().persist(pt);
                List<PlanoTratamentoProcedimento> planoTratamentoProcedimentos = planoTratamentoMB.getPlanoTratamentoProcedimentos();
                for (PlanoTratamentoProcedimento planoTratamentoProcedimento : planoTratamentoProcedimentos) {
                    planoTratamentoProcedimento.setValorAnterior(planoTratamentoProcedimento.getValorDesconto());
                    if (planoTratamentoMB.getEntity().isAlterouvaloresdesconto()) {
                        planoTratamentoProcedimento.setValorDesconto(
                                planoTratamentoProcedimento.getValorDesconto().subtract(planoTratamentoProcedimento.getValorDesconto().multiply(this.getPorcentagem().divide(new BigDecimal(100)))));
                    } else {
                        planoTratamentoProcedimento.setValorDesconto(
                                planoTratamentoProcedimento.getValor().subtract(planoTratamentoProcedimento.getValor().multiply(this.getPorcentagem().divide(new BigDecimal(100)))));
                    }
                    PlanoTratamentoProcedimentoSingleton.getInstance().getBo().merge(planoTratamentoProcedimento);
                }
                calculaRepasses(pt);
                planoTratamentoMB.actionNew(null);
                this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");
                RequestContext.getCurrentInstance().addCallbackParam("orcamento", true);
            }
        } catch (Exception e) {
            log.error("Erro no actionPersist OrcamentoMB", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
        }
    }

    @Override
    public void actionRemove(ActionEvent arg0) {
        if (totalPago.doubleValue() != 0) {
            this.addError("Não é possivel excluir as parecelas quando já existe pagamentos efetuados.", "");
        } else {
            super.actionRemove(arg0);
            planoTratamentoMB.actionNew(null);
            RequestContext.getCurrentInstance().addCallbackParam("orcamento", true);
        }
    }

    public int checkQuantidade(PlanoTratamentoProcedimento ptp, List<PlanoTratamentoProcedimento> ptps) {
        int qtd = 0;
        if (ptps != null) {
            for (PlanoTratamentoProcedimento ptpAux : ptps) {
                if (ptpAux.getProcedimento().equals(ptp.getProcedimento())) {
                    qtd++;
                }
            }
        }
        return qtd;
    }

    public void atualizaTela() {
    }

    public int getNumeroParcelas() {
        return numeroParcelas == 0 ? 1 : numeroParcelas;
    }

    public void setNumeroParcelas(int numeroParcelas) {
        this.numeroParcelas = numeroParcelas;
    }

    public List<Lancamento> getLancamentos() {
        return lancamentos;
    }

    public void setLancamentos(List<Lancamento> lancamentos) {
        this.lancamentos = lancamentos;
    }

    public PlanoTratamentoMB getPlanoTratamentoMB() {
        return planoTratamentoMB;
    }

    public void setPlanoTratamentoMB(PlanoTratamentoMB planoTratamentoMB) {
        this.planoTratamentoMB = planoTratamentoMB;
    }

    public int getParcela() {
        return parcela;
    }

    public void setParcela(int parcela) {
        this.parcela = parcela;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(BigDecimal valorTotal) {
        this.valorTotal = valorTotal;
    }

    public Date getDataCredito() {
        return dataCredito;
    }

    public void setDataCredito(Date dataCredito) {
        this.dataCredito = dataCredito;
    }

    public BigDecimal getDesconto() {
        return desconto;
    }

    public void setDesconto(BigDecimal desconto) {
        this.desconto = desconto;
    }

    public BigDecimal getTotalPago() {
        return totalPago;
    }

    public void setTotalPago(BigDecimal totalPago) {
        this.totalPago = totalPago;
    }

    public BigDecimal getValorTotalOriginal() {
        return valorTotalOriginal;
    }

    public void setValorTotalOriginal(BigDecimal valorTotalOriginal) {
        this.valorTotalOriginal = valorTotalOriginal;
    }

    public BigDecimal getPorcentagem() {
        return porcentagem;
    }

    public void setPorcentagem(BigDecimal porcentagem) {
        this.porcentagem = porcentagem;
    }

    public boolean getPorProcedimento() {
        return porProcedimento;
    }

    public void setPorProcedimento(boolean porProcedimento) {
        this.porProcedimento = porProcedimento;
    }

    public boolean isCiclico() {
//        return planoTratamentoMB.getEntity().getPlanoTratamentoProcedimentos() != null ? planoTratamentoMB.getEntity().getPlanoTratamentoProcedimentos().get(
//                0).getProcedimento().isCiclicoBoo() : ciclico;
        return false;
    }

    public void setCiclico(boolean ciclico) {
        this.ciclico = ciclico;
    }

    public boolean isDesabilitaGerarDocumento() {
        return lancamentos == null || lancamentos.isEmpty();
    }

    public String getDataOrcamento() {
        return Utils.dateToString(Calendar.getInstance().getTime(), "dd/MM/yyyy HH:mm");
    }

    public String getProfissionalOrcamento() {
        return Configurar.getInstance().getConfiguracao().getProfissionalLogado().getDadosBasico().getNome();
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

    public String getFormaPagamento() {
        return formaPagamento;
    }

    public void setFormaPagamento(String formaPagamento) {
        this.formaPagamento = formaPagamento;
    }

    public List<Dominio> getFormasPagamento() {
        return formasPagamento;
    }

    public void setFormasPagamento(List<Dominio> formasPagamento) {
        this.formasPagamento = formasPagamento;
    }

}
