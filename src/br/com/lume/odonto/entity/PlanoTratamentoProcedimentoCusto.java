package br.com.lume.odonto.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import br.com.lume.common.util.Status;
import br.com.lume.common.util.Utils;

@Entity
@Table(name = "PLANO_TRATAMENTO_PROCEDIMENTO_CUSTO")
public class PlanoTratamentoProcedimentoCusto implements Serializable, Comparable<PlanoTratamentoProcedimentoCusto> {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_PLANO_TRATAMENTO_PROCEDIMENTO")
    private PlanoTratamentoProcedimento planoTratamentoProcedimento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_ABASTECIMENTO")
    private Abastecimento abastecimento;

    @Column(name = "DATA_FATURAMENTO")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataFaturamento;

    @Column(name = "DATA_REGISTRO")
    @Temporal(TemporalType.DATE)
    private Date dataRegistro;

    @Column(name = "VALOR")
    private BigDecimal valor;

    @Column
    private String descricao;

    private String excluido = Status.NAO;

    @Column(name = "DATA_EXCLUSAO")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataExclusao;

    @Column(name = "EXCLUIDO_POR")
    private Long excluidoPorProfissional;

    public PlanoTratamentoProcedimentoCusto(
            Abastecimento abastecimento,
            PlanoTratamentoProcedimento planoTratamentoProcedimento,
            Date dataFaturamento,
            Date dataRegistro,
            BigDecimal valor,
            String descricao) {
        super();
        this.abastecimento = abastecimento;
        this.planoTratamentoProcedimento = planoTratamentoProcedimento;
        this.dataFaturamento = dataFaturamento;
        this.dataRegistro = dataRegistro;
        this.valor = valor;
        this.descricao = descricao;
    }

    public PlanoTratamentoProcedimentoCusto() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getValor() {
        return this.valor == null ? new BigDecimal(0) : this.valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.id == null) ? 0 : this.id.hashCode());
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
        PlanoTratamentoProcedimentoCusto other = (PlanoTratamentoProcedimentoCusto) obj;
        if (this.id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!this.id.equals(other.id)) {
            return false;
        }
        return true;
    }

    public PlanoTratamentoProcedimento getPlanoTratamentoProcedimento() {
        return this.planoTratamentoProcedimento;
    }

    public void setPlanoTratamentoProcedimento(PlanoTratamentoProcedimento planoTratamentoProcedimento) {
        this.planoTratamentoProcedimento = planoTratamentoProcedimento;
    }

    public Date getDataFaturamento() {
        return this.dataFaturamento;
    }

    public void setDataFaturamento(Date dataFaturamento) {
        this.dataFaturamento = dataFaturamento;
    }

    @Transient
    public String getDataFaturamentoStr() {
        return Utils.dateToString(this.dataFaturamento, "dd/MM/yyyy HH:mm:ss");
    }

    @Transient
    public String getDataRegistroStr() {
        return Utils.dateToString(this.dataRegistro, "dd/MM/yyyy");
    }

    public String getDescricao() {
        return this.descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    @Override
    public int compareTo(PlanoTratamentoProcedimentoCusto o) {
        if (o == null || this == null || o.getDescricao() == null || this.getDescricao() == null) {
            return -1;
        }
        return Normalizer.normalize(this.descricao, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "").compareToIgnoreCase(Normalizer.normalize(o.getDescricao(), Normalizer.Form.NFD));
    }

    public String getExcluido() {
        return this.excluido;
    }

    public void setExcluido(String excluido) {
        this.excluido = excluido;
    }

    public Date getDataExclusao() {
        return this.dataExclusao;
    }

    public void setDataExclusao(Date dataExclusao) {
        this.dataExclusao = dataExclusao;
    }

    public Date getDataRegistro() {
        return this.dataRegistro;
    }

    public void setDataRegistro(Date dataRegistro) {
        this.dataRegistro = dataRegistro;
    }

    public Long getExcluidoPorProfissional() {
        return this.excluidoPorProfissional;
    }

    public void setExcluidoPorProfissional(Long excluidoPorProfissional) {
        this.excluidoPorProfissional = excluidoPorProfissional;
    }

    public Abastecimento getAbastecimento() {
        return this.abastecimento;
    }

    public void setAbastecimento(Abastecimento abastecimento) {
        this.abastecimento = abastecimento;
    }
}
