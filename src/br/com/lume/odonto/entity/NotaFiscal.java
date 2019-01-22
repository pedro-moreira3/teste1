package br.com.lume.odonto.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
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

import br.com.lume.common.util.Utils;

@Entity
@Table(name = "NOTA_FISCAL")
public class NotaFiscal implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer numero;

    @ManyToOne
    @JoinColumn(name = "ID_FORNECEDOR", referencedColumnName = "ID", nullable = false, insertable = true, updatable = true)
    private Fornecedor fornecedor;

    @Column(name = "DATA_ENTRADA")
    @Temporal(TemporalType.DATE)
    private Date dataEntrada;

    @Column(name = "DATA_EXPEDICAO")
    @Temporal(TemporalType.DATE)
    private Date dataExpedicao;

    @Column(name = "VALOR_TOTAL")
    private BigDecimal valorTotal;

    @Column(name = "ID_EMPRESA")
    private Long idEmpresa;

    @OneToMany(mappedBy = "notaFiscal", cascade = CascadeType.MERGE)
    private List<Material> materiais;

    public NotaFiscal() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getNumero() {
        return this.numero;
    }

    public void setNumero(Integer numero) {
        this.numero = numero;
    }

    public Fornecedor getFornecedor() {
        return this.fornecedor;
    }

    public void setFornecedor(Fornecedor fornecedor) {
        this.fornecedor = fornecedor;
    }

    public Date getDataEntrada() {
        return this.dataEntrada;
    }

    public void setDataEntrada(Date dataEntrada) {
        this.dataEntrada = dataEntrada;
    }

    public Date getDataExpedicao() {
        return this.dataExpedicao;
    }

    public void setDataExpedicao(Date dataExpedicao) {
        this.dataExpedicao = dataExpedicao;
    }

    public BigDecimal getValorTotal() {
        return this.valorTotal;
    }

    public void setValorTotal(BigDecimal valorTotal) {
        this.valorTotal = valorTotal;
    }

    @Transient
    public String getDataExpedicaoStr() {
        return Utils.dateToString(this.dataExpedicao, "dd/MM/yyyy");
    }

    @Transient
    public String getDataExpedicaoStrOrd() {
        return Utils.dateToString(this.dataExpedicao, "yyyy/MM/dd");
    }

    @Transient
    public String getDataEntradaStr() {
        return Utils.dateToString(this.dataEntrada, "dd/MM/yyyy");
    }

    @Transient
    public String getDataEntradaStrOrd() {
        return Utils.dateToString(this.dataEntrada, "yyyy/MM/dd");
    }

    public Long getIdEmpresa() {
        return this.idEmpresa;
    }

    public void setIdEmpresa(Long idEmpresa) {
        this.idEmpresa = idEmpresa;
    }

    public List<Material> getMateriais() {
        return this.materiais;
    }

    public void setMateriais(List<Material> materiais) {
        this.materiais = materiais;
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
        NotaFiscal other = (NotaFiscal) obj;
        if (this.id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!this.id.equals(other.id)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "NotaFiscal [id=" + this.id + "]";
    }
}
