package br.com.lume.odonto.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import br.com.lume.common.util.Status;
import br.com.lume.common.util.Utils;
import br.com.lume.odonto.bo.DominioBO;
import br.com.lume.odonto.bo.LancamentoBO;

@Entity
@Table(name = "LANCAMENTO")
public class Lancamento implements Serializable, Comparable<Lancamento> {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "VALOR")
    private BigDecimal valor;

    @Column(name = "VALOR_REPASSADO")
    private BigDecimal valorRepassado;

    @Column(name = "VALOR_ORIGINAL")
    private BigDecimal valorOriginal;

    @Column(name = "DATA_CREDITO")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataCredito;

    @Column(name = "DATA_PAGAMENTO")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataPagamento;

    @Column(name = "NUMERO_PARCELA")
    private int numeroParcela;

    @Column(name = "FORMA_PAGAMENTO")
    private String formaPagamento;

    @Column(name = "RECIBO")
    private String recibo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_PLANO_TRATAMENTO_PROCEDIMENTO")
    private PlanoTratamentoProcedimento planoTratamentoProcedimento;

    @ManyToOne
    @JoinColumn(name = "ID_ORCAMENTO")
    private Orcamento orcamento;

    private String excluido = Status.NAO;

    @Column(name = "DATA_EXCLUSAO")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataExclusao;

    @Column(name = "EXCLUIDO_POR")
    private Long excluidoPorProfissional;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "ID_LANCAMENTO")
    private List<LancamentoContabil> lancamentosContabeis;

    @Column(name = "DATA_FATURAMENTO")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataFaturamento;

    @Column(name = "DATA_PAGAMENTO_FATURAMENTO")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataPagamentoFaturamento;

    @Column(name = "TRIBUTO")
    private BigDecimal tributo;

    @Transient
    private BigDecimal valorDesconto;

    @Transient
    private BigDecimal credito;

    @ManyToOne
    @JoinColumn(name = "ID_TARIFA")
    private Tarifa tarifa;

    @Transient
    private boolean used;

    @Column(name = "DATA_VALIDADO")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataValidado;

    @Column(name = "VALIDADO_POR")
    private Long validadoPorProfissional;

    private String validado = Status.SIM;

    public Long getExcluidoPorProfissional() {
        return excluidoPorProfissional;
    }

    public void setExcluidoPorProfissional(Long excluidoPorProfissional) {
        this.excluidoPorProfissional = excluidoPorProfissional;
    }

    public String getExcluido() {
        return excluido;
    }

    public void setExcluido(String excluido) {
        this.excluido = excluido;
    }

    public Date getDataExclusao() {
        return dataExclusao;
    }

    public void setDataExclusao(Date dataExclusao) {
        this.dataExclusao = dataExclusao;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public BigDecimal getValorComDesconto() {
        if (this.getTarifa() != null && this.getTarifa().getTaxa() != null) {
            return valor.subtract(valor.multiply(this.getTarifa().getTaxa().divide(new BigDecimal(100))));
        }
        if (this.getTarifa() != null && this.getTarifa().getTarifa() != null) {
            return valor.subtract(this.getTarifa().getTarifa());
        }
        return new BigDecimal(0);
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public Date getDataCredito() {
        return dataCredito;
    }

    @Transient
    public String getDataCreditoStrOrd() {
        return Utils.dateToString(dataCredito, "yyyy/MM/dd");
    }

    @Transient
    public String getDataCreditoStr() {
        return Utils.dateToString(dataCredito, "dd/MM/yyyy");
    }

    public void setDataCredito(Date dataCredito) {
        this.dataCredito = dataCredito;
    }

    public Date getDataPagamento() {
        return dataPagamento;
    }

    @Transient
    public String getDataPagamentoStr() {
        return Utils.dateToString(dataPagamento, "dd/MM/yyyy");
    }

    @Transient
    public String getDataPagamentoStrOrd() {
        return Utils.dateToString(dataPagamento, "yyyy/MM/dd");
    }

    @Transient
    public String getExcluidoStr() {
        return excluido.equals(Status.SIM) ? "Sim" : "Não";
    }

    @Transient
    public String getStatus() {
        String retorno = ATIVO;
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(dataCredito);
        cal2.set(Calendar.HOUR_OF_DAY, 0);
        cal2.set(Calendar.MINUTE, 0);
        cal2.set(Calendar.SECOND, 0);
        cal2.set(Calendar.MILLISECOND, 0);
        dataCredito = cal2.getTime();

        if (excluido.equals(Status.SIM)) {
            retorno = CANCELADO;
        } else if (dataPagamento != null) {
            if (dataCredito.after(cal.getTime())) {
                retorno = AGENDADO;
            } else {
                retorno = PAGO;
            }
        } else {
            cal.add(Calendar.DAY_OF_MONTH, -1);
            if (dataCredito != null && dataCredito.getTime() < cal.getTime().getTime()) {
                retorno = ATRASADO;
            } else if (orcamento != null && orcamento.getPlanoTratamento().getFinalizado().equals(Status.SIM)) {
                retorno = PENDENTE;
            }
        }
        return retorno;
    }

    @Transient
    public String getTipo() {
        Dominio d = null;
        try {
            d = new DominioBO().findByEmpresaAndObjetoAndTipoAndValor("pagamento", "forma", formaPagamento);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return d != null ? d.getNome() : "";
    }

    public void setDataPagamento(Date dataPagamento) {
        this.dataPagamento = dataPagamento;
    }

    public Orcamento getOrcamento() {
        return orcamento;
    }

    public void setOrcamento(Orcamento orcamento) {
        this.orcamento = orcamento;
    }

    public String getFormaPagamento() {
        return formaPagamento;
    }

    public String getFormaPagamentoStr() throws Exception {
        String formaStr = "";
        Dominio dominio = new DominioBO().listByTipoAndObjeto(formaPagamento, "pagamento");
        if (dominio != null) {
            formaStr = dominio.getNome();
        }
        if ("CC".equals(formaPagamento) || "CD".equals(formaPagamento)) {
            formaStr += " (" + getParcelaMaxima() + "x)";
        }
        return formaStr;
    }

    public void setFormaPagamento(String formaPagamento) {
        this.formaPagamento = formaPagamento;
    }

    public int getNumeroParcela() {
        return numeroParcela;
    }

    public void setNumeroParcela(int numeroParcela) {
        this.numeroParcela = numeroParcela;
    }

    public BigDecimal getValorOriginal() {
        return valorOriginal;
    }

    public void setValorOriginal(BigDecimal valorOriginal) {
        this.valorOriginal = valorOriginal;
    }

    public String getRecibo() {
        return recibo;
    }

    public void setRecibo(String recibo) {
        this.recibo = recibo;
    }

    public PlanoTratamentoProcedimento getPlanoTratamentoProcedimento() {
        return planoTratamentoProcedimento;
    }

    public void setPlanoTratamentoProcedimento(PlanoTratamentoProcedimento planoTratamentoProcedimento) {
        this.planoTratamentoProcedimento = planoTratamentoProcedimento;
    }

    public List<LancamentoContabil> getLancamentosContabeis() {
        return lancamentosContabeis;
    }

    public void setLancamentosContabeis(List<LancamentoContabil> lancamentosContabeis) {
        this.lancamentosContabeis = lancamentosContabeis;
    }

    public Date getDataFaturamento() {
        return dataFaturamento;
    }

    public void setDataFaturamento(Date dataFaturamento) {
        this.dataFaturamento = dataFaturamento;
    }

    public Date getDataPagamentoFaturamento() {
        return dataPagamentoFaturamento;
    }

    public void setDataPagamentoFaturamento(Date dataPagamentoFaturamento) {
        this.dataPagamentoFaturamento = dataPagamentoFaturamento;
    }

    public BigDecimal getTributo() {
        return tributo;
    }

    public void setTributo(BigDecimal tributo) {
        this.tributo = tributo;
    }

    public BigDecimal getValorDesconto() {
        return valorDesconto != null ? valorDesconto : new BigDecimal(0);
    }

    public void setValorDesconto(BigDecimal valorDesconto) {
        this.valorDesconto = valorDesconto;
    }

    public Tarifa getTarifa() {
        return tarifa;
    }

    public void setTarifa(Tarifa tarifa) {
        this.tarifa = tarifa;
    }

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }

    public static final String ATIVO = "Ativo";

    public static final String CANCELADO = "Cancelado";

    public static final String PAGO = "Pago";

    public static final String AGENDADO = "Agendado";

    public static final String ATRASADO = "Atrasado";

    public static final String PENDENTE = "Pendente";

    public static List<String> getStatusCombo() {
        List<String> ls = new ArrayList<>();
        ls.add(AGENDADO);
        ls.add(ATIVO);
        ls.add(ATRASADO);
        ls.add(CANCELADO);
        ls.add(PAGO);
        ls.add(PENDENTE);
        return ls;
    }

    @Override
    public int compareTo(Lancamento o) {
        return (String.valueOf(this.getId()).concat(String.valueOf(this.getNumeroParcela())).compareTo(String.valueOf(o.getId()).concat(String.valueOf(o.getNumeroParcela()))));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (id ^ (id >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        Lancamento other = (Lancamento) obj;
        if (id != other.id) {
            return false;
        }
        return true;
    }

    public BigDecimal getValorRepassado() {
        return valorRepassado != null ? valorRepassado : new BigDecimal(0);
    }

    public void setValorRepassado(BigDecimal valorRepassado) {
        this.valorRepassado = valorRepassado;
    }

    public BigDecimal getCredito() {
        return this.getValor().subtract(this.getValorRepassado());
    }

    public void setCredito(BigDecimal credito) {
        this.credito = credito;
    }

    public Date getDataValidado() {
        return dataValidado;
    }

    public void setDataValidado(Date dataValidado) {
        this.dataValidado = dataValidado;
    }

    public Long getValidadoPorProfissional() {
        return validadoPorProfissional;
    }

    public void setValidadoPorProfissional(Long validadoPorProfissional) {
        this.validadoPorProfissional = validadoPorProfissional;
    }

    public String getValidado() {
        return validado;
    }

    public void setValidado(String validado) {
        this.validado = validado;
    }

    @Transient
    public Long getParcelaMaxima() {
        return new LancamentoBO().findParcelaMaximaByOrcamento(orcamento.getId(), valor, formaPagamento);
    }
}
