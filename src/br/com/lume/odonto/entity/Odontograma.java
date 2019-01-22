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

import br.com.lume.common.util.Status;
import br.com.lume.common.util.Utils;

/**
 * The persistent class for the ODONTOGRAMA database table.
 */
@Entity
@Table(name = "ODONTOGRAMA")
public class Odontograma implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    private long id;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "DATA_CADASTRO")
    private Date dataCadastro;

    @ManyToOne
    @JoinColumn(name = "ID_PACIENTE")
    private Paciente paciente;

    private String observacoes;

    private String excluido = Status.NAO;

    @Column(name = "DATA_EXCLUSAO")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataExclusao;

    @Column(name = "EXCLUIDO_POR")
    private Long excluidoPorProfissional;

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

    public Odontograma() {
    }

    public Odontograma(Date dataCadastro, Paciente paciente, String observacoes) {
        this.dataCadastro = dataCadastro;
        this.paciente = paciente;
        this.observacoes = observacoes;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Date getDataCadastro() {
        return dataCadastro;
    }

    public void setDataCadastro(Date dataCadastro) {
        this.dataCadastro = dataCadastro;
    }

    public Paciente getPaciente() {
        return paciente;
    }

    public void setPaciente(Paciente paciente) {
        this.paciente = paciente;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    @Transient
    public String getDataCadastroStr() {
        return Utils.dateToString(dataCadastro, "dd/MM/yyyy hh:mm");
    }

    @Transient
    public String getDataCadastroStrE() {
        return ("S").equals(excluido) ? "[EXCLUIDO] " + getDataCadastroStr() : getDataCadastroStr();
    }

    @Transient
    public String getDataCadastroStrOrd() {
        return Utils.dateToString(dataCadastro, "yyyy/MM/dd");
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
        Odontograma other = (Odontograma) obj;
        if (id != other.id) {
            return false;
        }
        return true;
    }

}
