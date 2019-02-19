package br.com.lume.odonto.entity;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "REPASSE_LANCAMENTO")
public class RepasseLancamento implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1097663037020699507L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_LANCAMENTO")
    private Lancamento lancamento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_PLANO_TRATAMENTO_PROCEDIMENTO")
    private PlanoTratamentoProcedimento planoTratamentoProcedimento;

    private BigDecimal valor;

    public String status;

    @Column(name = "VALOR_DESCONTO")
    private BigDecimal valorDesconto;

    public RepasseLancamento() {
    }

    public RepasseLancamento(
            Lancamento lancamento,
            PlanoTratamentoProcedimento planoTratamentoProcedimento,
            BigDecimal valor,
            BigDecimal valorDesconto,
            String status) {
        super();
        this.lancamento = lancamento;
        this.planoTratamentoProcedimento = planoTratamentoProcedimento;
        this.valor = valor;
        this.valorDesconto = valorDesconto;
        this.status = status;
    }

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Lancamento getLancamento() {
        return this.lancamento;
    }

    public void setLancamento(Lancamento lancamento) {
        this.lancamento = lancamento;
    }

    public PlanoTratamentoProcedimento getPlanoTratamentoProcedimento() {
        return this.planoTratamentoProcedimento;
    }

    public void setPlanoTratamentoProcedimento(PlanoTratamentoProcedimento planoTratamentoProcedimento) {
        this.planoTratamentoProcedimento = planoTratamentoProcedimento;
    }

    public BigDecimal getValor() {
        return this.valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public BigDecimal getValorDesconto() {
        return this.valorDesconto;
    }

    public void setValorDesconto(BigDecimal valorDesconto) {
        this.valorDesconto = valorDesconto;
    }

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
