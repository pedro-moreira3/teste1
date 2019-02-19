package br.com.lume.odonto.entity;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "REPASSE_ITEM")
public class RepasseItem implements Serializable, Comparable<RepasseItem> {

    /**
     *
     */
    private static final long serialVersionUID = -1363713571219493059L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_REPASSE")
    private RepasseProfissional repasse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_PLANO_TRATAMENTO_PROCEDIMENTO")
    private PlanoTratamentoProcedimento planoTratamentoProcedimento;

    private String status;

    private BigDecimal valor;

    public static final String REPASSADO = "R";

    public static final String PAGO_COMPLETO = "P";

    public static final String PAGO_PARCIAL = "L";

    public RepasseItem() {
    }

    public RepasseItem(
            RepasseProfissional repasse, PlanoTratamentoProcedimento planoTratamentoProcedimento, String status, BigDecimal valor) {
        super();
        this.repasse = repasse;
        this.planoTratamentoProcedimento = planoTratamentoProcedimento;
        this.status = status;
        this.valor = valor;
    }

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public RepasseProfissional getRepasse() {
        return this.repasse;
    }

    public void setRepasse(RepasseProfissional repasse) {
        this.repasse = repasse;
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

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public int compareTo(RepasseItem o) {
        return o.getPlanoTratamentoProcedimento().getDataFinalizado().compareTo(getPlanoTratamentoProcedimento().getDataFinalizado());

    }
}
