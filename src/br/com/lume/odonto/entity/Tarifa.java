package br.com.lume.odonto.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import br.com.lume.common.util.Status;

@Entity
@Table(name = "TARIFA")
public class Tarifa implements Serializable, Comparable<Tarifa> {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "PRODUTO")
    private String produto;

    @Column(name = "BANDEIRA")
    private String bandeira;

    @Column(name = "TAXA")
    private BigDecimal taxa = new BigDecimal(0d);

    @Column(name = "PRAZO")
    private Integer prazo;

    @Column(name = "TARIFA")
    private BigDecimal tarifa = new BigDecimal(0d);

    @Column(name = "PARCELA_MINIMA")
    private Integer parcelaMinima;

    @Column(name = "PARCELA_MAXIMA")
    private Integer parcelaMaxima;

    @Column(name = "BANCO")
    private String banco;

    @Column(name = "AGENCIA")
    private String agencia;

    @Column(name = "ID_EMPRESA")
    private long idEmpresa;

    private String tipo = "CC";

    private String excluido = Status.NAO;

    @Column(name = "DATA_EXCLUSAO")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataExclusao;

    @Column(name = "EXCLUIDO_POR")
    private Long excluidoPorProfissional;

    public Long getExcluidoPorProfissional() {
        return this.excluidoPorProfissional;
    }

    public void setExcluidoPorProfissional(Long excluidoPorProfissional) {
        this.excluidoPorProfissional = excluidoPorProfissional;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (this.id ^ (this.id >>> 32));
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
        Tarifa other = (Tarifa) obj;
        if (this.id != other.id) {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(Tarifa m) {
        if (this.produto == null || m == null || m.getProduto() == null) {
            return 1;
        } else {
            return Normalizer.normalize(this.getProduto(), Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "").compareToIgnoreCase(Normalizer.normalize(m.getProduto(), Normalizer.Form.NFD));
        }
    }

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getProduto() {
        return this.produto;
    }

    @Transient
    public String getProdutoStr() {
        return this.produto + "-(Até " + this.parcelaMaxima + ")";
    }

    public void setProduto(String produto) {
        this.produto = produto;
    }

    public String getBandeira() {
        return this.bandeira;
    }

    public void setBandeira(String bandeira) {
        this.bandeira = bandeira;
    }

    public BigDecimal getTaxa() {
        return this.taxa;
    }

    public void setTaxa(BigDecimal taxa) {
        this.taxa = taxa;
    }

    public Integer getPrazo() {
        return this.prazo;
    }

    public void setPrazo(Integer prazo) {
        this.prazo = prazo;
    }

    public BigDecimal getTarifa() {
        return this.tarifa;
    }

    public void setTarifa(BigDecimal tarifa) {
        this.tarifa = tarifa;
    }

    public Integer getParcelaMinima() {
        return this.parcelaMinima;
    }

    public void setParcelaMinima(Integer parcelaMinima) {
        this.parcelaMinima = parcelaMinima;
    }

    public Integer getParcelaMaxima() {
        return this.parcelaMaxima;
    }

    public void setParcelaMaxima(Integer parcelaMaxima) {
        this.parcelaMaxima = parcelaMaxima;
    }

    public String getBanco() {
        return this.banco;
    }

    public void setBanco(String banco) {
        this.banco = banco;
    }

    public String getAgencia() {
        return this.agencia;
    }

    public void setAgencia(String agencia) {
        this.agencia = agencia;
    }

    public long getIdEmpresa() {
        return this.idEmpresa;
    }

    public void setIdEmpresa(long idEmpresa) {
        this.idEmpresa = idEmpresa;
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

    public String getTipoStr() {
        switch (this.tipo) {
            case "CC":
                return "Cartão de Crédito";
            case "CD":
                return "Cartão de Débito";
            case "BO":
                return "Boleto";
            default:
                return this.tipo;
        }
    }

    public String getTipo() {
        return this.tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }
}
