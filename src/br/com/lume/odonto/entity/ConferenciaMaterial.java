package br.com.lume.odonto.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import br.com.lume.common.util.Utils;

@Entity
@Table(name = "CONFERENCIA_MATERIAL")
public class ConferenciaMaterial implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    @JoinColumn(name = "ID_MATERIAL")
    private Material material;

    @Column(name = "ID_EMPRESA")
    private long idEmpresa;

    @ManyToOne
    @JoinColumn(name = "ID_CONFERENCIA")
    private Conferencia conferencia;

    @Column(name = "VALOR_ORIGINAL")
    private BigDecimal valorOriginal;

    @Column(name = "VALOR_ALTERADO")
    private BigDecimal valorAlterado = new BigDecimal(0);

    @Column(name = "DATA_CADASTRO")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataCadastro;

    @Column(name = "MOTIVO")
    private String motivo;

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
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
        ConferenciaMaterial other = (ConferenciaMaterial) obj;
        if (this.id != other.id) {
            return false;
        }
        return true;
    }

    public Material getMaterial() {
        return this.material;
    }

    public void setMaterial(Material material) {
        this.material = material;
    }

    public Conferencia getConferencia() {
        return this.conferencia;
    }

    public void setConferencia(Conferencia conferencia) {
        this.conferencia = conferencia;
    }

    public BigDecimal getValorOriginal() {
        return this.valorOriginal;
    }

    public void setValorOriginal(BigDecimal valorOriginal) {
        this.valorOriginal = valorOriginal;
    }

    public BigDecimal getValorAlterado() {
        return this.valorAlterado;
    }

    public void setValorAlterado(BigDecimal valorAlterado) {
        this.valorAlterado = valorAlterado;
    }

    public long getIdEmpresa() {
        return this.idEmpresa;
    }

    public void setIdEmpresa(long idEmpresa) {
        this.idEmpresa = idEmpresa;
    }

    public Date getDataCadastro() {
        return this.dataCadastro;
    }

    public void setDataCadastro(Date dataCadastro) {
        this.dataCadastro = dataCadastro;
    }

    @Transient
    public String getDataCadastroStr() {
        return Utils.dateToString(this.dataCadastro, "dd/MM/yyyy HH:mm:ss");
    }

    public String getMotivo() {
        return this.motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }
}
