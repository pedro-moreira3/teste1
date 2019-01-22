package br.com.lume.odonto.entity;

import java.io.Serializable;
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

/**
 * The persistent class for the STATUS_DENTE database table.
 */

@Entity
@Table(name = "STATUS_DENTE")
public class StatusDente implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String descricao;

    private String excluido = Status.NAO;

    @Column(name = "DATA_EXCLUSAO")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataExclusao;

    @Column(name = "EXCLUIDO_POR")
    private Long excluidoPorProfissional;

    private String cor, label;

    @Transient
    private String corPF;

    @Column(name = "ID_EMPRESA")
    private Long idEmpresa;

    @Column(name = "TEM_FACE")
    private boolean temFace;

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

    public StatusDente() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getCor() {
        return cor;
    }

    public String getCorPF() {
        if (corPF == null && cor != null) {
            corPF = cor.replaceAll("#", "");
        }
        return corPF;
    }

    public void setCorPF(String corPF) {
        this.corPF = corPF;
    }

    public void setCor(String cor) {
        this.cor = cor;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Long getIdEmpresa() {
        return idEmpresa;
    }

    public void setIdEmpresa(Long idEmpresa) {
        this.idEmpresa = idEmpresa;
    }

    public String getStick() {
        return getStick("");
    }

    public String getStick(String extraStyle) {
        return getStick(extraStyle, "");
    }

    public String getStick(String extraStyle, String faces) {
        return "<div class='problema-odontograma' style='" + extraStyle + "background:" + getCor() + "'>" + faces + " " + getLabel() + "</div>";
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
        if (getClass() != obj.getClass()) {
            return false;
        }
        StatusDente other = (StatusDente) obj;
        if (id != other.id) {
            return false;
        }
        return true;
    }

    public boolean isTemFace() {
        return temFace;
    }

    public void setTemFace(boolean temFace) {
        this.temFace = temFace;
    }
}
