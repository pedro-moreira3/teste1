package br.com.lume.odonto.entity;

import java.io.Serializable;
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

import br.com.lume.common.util.Status;

/**
 * The persistent class for the PLANO_TRATAMENTO_PROCEDIMENTO database table.
 */
@Entity
@Table(name = "PROFISSIONAL_ESPECIALIDADE")
public class ProfissionalEspecialidade implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_PROFISSIONAL")
    private Profissional profissional;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_ESPECIALIDADE")
    private Especialidade especialidade;

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

    public ProfissionalEspecialidade() {
    }

    public ProfissionalEspecialidade(
            Profissional profissional,
            Especialidade especialidade) {
        this.profissional = profissional;
        this.especialidade = especialidade;
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
        result = prime * result + ((this.profissional == null) ? 0 : this.profissional.hashCode());
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
        ProfissionalEspecialidade other = (ProfissionalEspecialidade) obj;
        if (this.id != other.id) {
            return false;
        }
        if (this.profissional == null) {
            if (other.profissional != null) {
                return false;
            }
        } else if (!this.especialidade.equals(other.especialidade)) {
            return false;
        }
        return true;
    }

    public Profissional getProfissional() {

        return this.profissional;
    }

    public void setProfissional(Profissional profissional) {

        this.profissional = profissional;
    }

    public Especialidade getEspecialidade() {

        return this.especialidade;
    }

    public void setEspecialidade(Especialidade especialidade) {

        this.especialidade = especialidade;
    }

}
