package br.com.lume.odonto.entity;

import java.io.Serializable;
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
@Table(name = "CONFERENCIA")
public class Conferencia implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "ALTERACAO")
    private String alteracao;

    @Column(name = "ID_EMPRESA")
    private long idEmpresa;

    @Column(name = "DATA")
    @Temporal(TemporalType.TIMESTAMP)
    private Date data;

    @ManyToOne
    @JoinColumn(name = "ID_PROFISSIONAL")
    private Profissional profissional;

    @Transient
    public String getDataStr() {
        return Utils.dateToString(this.data);
    }

    @Transient
    public String getDataStrOrd() {
        return Utils.dateToString(this.data, "yyyy/MM/dd");
    }

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
        Conferencia other = (Conferencia) obj;
        if (this.id != other.id) {
            return false;
        }
        return true;
    }

    public String getAlteracao() {
        return this.alteracao;
    }

    public void setAlteracao(String alteracao) {
        this.alteracao = alteracao;
    }

    public Date getData() {
        return this.data;
    }

    public void setData(Date data) {
        this.data = data;
    }

    public Profissional getProfissional() {
        return this.profissional;
    }

    public void setProfissional(Profissional profissional) {
        this.profissional = profissional;
    }

    public long getIdEmpresa() {
        return this.idEmpresa;
    }

    public void setIdEmpresa(long idEmpresa) {
        this.idEmpresa = idEmpresa;
    }
}
