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

@Entity
@Table(name = "PLANO_TRATAMENTO_PROCEDIMENTO_FACE")
public class PlanoTratamentoProcedimentoFace implements Serializable {

    private static final long serialVersionUID = 1L;

    public PlanoTratamentoProcedimentoFace() {
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_PLANO_TRATAMENTO_PROCEDIMENTO")
    private PlanoTratamentoProcedimento planoTratamentoProcedimento;

    @Column(name = "FACE")
    private String face;

    @Column(name = "DATA_EXCLUSAO")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataExclusao;

    private String excluido = Status.NAO;

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public PlanoTratamentoProcedimento getPlanoTratamentoProcedimento() {
        return this.planoTratamentoProcedimento;
    }

    public void setPlanoTratamentoProcedimento(PlanoTratamentoProcedimento planoTratamentoProcedimento) {
        this.planoTratamentoProcedimento = planoTratamentoProcedimento;
    }

    public String getFace() {
        return this.face;
    }

    public void setFace(String face) {
        this.face = face;
    }

    public Date getDataExclusao() {
        return this.dataExclusao;
    }

    public void setDataExclusao(Date dataExclusao) {
        this.dataExclusao = dataExclusao;
    }

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
        PlanoTratamentoProcedimentoFace other = (PlanoTratamentoProcedimentoFace) obj;
        if (this.id != other.id) {
            return false;
        }
        return true;
    }

    public String getExcluido() {
        return this.excluido;
    }

    public void setExcluido(String excluido) {
        this.excluido = excluido;
    }
}
