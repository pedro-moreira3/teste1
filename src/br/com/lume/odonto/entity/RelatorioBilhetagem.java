package br.com.lume.odonto.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import br.com.lume.common.util.Utils;

@Entity
public class RelatorioBilhetagem implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 9045566340252702956L;

    @Id
    @Column(name = "ID")
    private Long id;

    @Column(name = "PROFISSIONAL")
    private String profissional;

    private String paciente;

    @Temporal(TemporalType.TIMESTAMP)
    private Date inicio;

    @Temporal(TemporalType.TIMESTAMP)
    private Date fim;

    @Column(name = "STATUS")
    private String status;

    @Column(name = "QUANTIDADE_DE_PROCEDIMENTOS")
    private Integer quantidade;

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProfissional() {
        return this.profissional;
    }

    public void setProfissional(String profissional) {
        this.profissional = profissional;
    }

    public String getPaciente() {
        return this.paciente;
    }

    public void setPaciente(String paciente) {
        this.paciente = paciente;
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
        RelatorioBilhetagem other = (RelatorioBilhetagem) obj;
        if (this.id != other.id) {
            return false;
        }
        return true;
    }

    @Transient
    public String getInicioStr() {
        return Utils.dateToString(this.inicio);
    }

    @Transient
    public String getInicioStrOrd() {
        return Utils.dateToString(this.inicio, "yyyy/MM/dd");
    }

    @Transient
    public String getFimStr() {
        return Utils.dateToString(this.fim);
    }

    @Transient
    public String getFimStrOrd() {
        return Utils.dateToString(this.fim, "yyyy/MM/dd");
    }

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getInicio() {
        return this.inicio;
    }

    public void setInicio(Date inicio) {
        this.inicio = inicio;
    }

    public Date getFim() {
        return this.fim;
    }

    public void setFim(Date fim) {
        this.fim = fim;
    }

    public Integer getQuantidade() {
        return this.quantidade;
    }

    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
    }
}
