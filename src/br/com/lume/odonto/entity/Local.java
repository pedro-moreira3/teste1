package br.com.lume.odonto.entity;

import java.io.Serializable;
import java.text.Normalizer;
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

import br.com.lume.common.util.Status;

@Entity
@Table(name = "LOCAL")
public class Local implements Serializable, Comparable<Local> {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    @JoinColumn(name = "ID_LOCAL")
    private Local idLocal;

    @Column(name = "DESCRICAO")
    private String descricao;

    @Column(name = "TIPO")
    private String tipo;

    @Transient
    private String descricaoTipo;

    @Column(name = "ID_EMPRESA")
    private long idEmpresa;

    private String excluido = Status.NAO;

    @Column(name = "DATA_EXCLUSAO")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataExclusao;

    @Column(name = "EXCLUIDO_POR")
    private Long excluidoPorProfissional;

    public Local() {
    }

    public Local(String descricao, String tipo, long idEmpresa) {
        super();
        this.descricao = descricao;
        this.tipo = tipo;
        this.idEmpresa = idEmpresa;
    }

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

    public String getDescricaoTipo() {
        return descricaoTipo;
    }

    public void setDescricaoTipo(String descricaoTipo) {
        this.descricaoTipo = descricaoTipo;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public Local getIdLocal() {
        return idLocal;
    }

    public void setIdLocal(Local idLocal) {
        this.idLocal = idLocal;
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

    public long getIdEmpresa() {
        return idEmpresa;
    }

    public void setIdEmpresa(long idEmpresa) {
        this.idEmpresa = idEmpresa;
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
        Local other = (Local) obj;
        if (id != other.id) {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(Local l) {
        if (descricao == null || l == null || l.getDescricao() == null) {
            return 1;
        } else {
            return Normalizer.normalize(this.getDescricao(), Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "").compareToIgnoreCase(Normalizer.normalize(l.getDescricao(), Normalizer.Form.NFD));
        }
    }

    public String getDescricaoLimpa() {
        return Normalizer.normalize(this.getDescricao(), Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "");
    }
}
