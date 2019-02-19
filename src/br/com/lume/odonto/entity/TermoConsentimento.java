package br.com.lume.odonto.entity;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import br.com.lume.common.util.Status;
import br.com.lume.common.util.Utils;

@Entity
@Table(name = "TERMO_CONSENTIMENTO")
public class TermoConsentimento implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "DATA_HORA")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataHora = Calendar.getInstance().getTime();

    @ManyToOne
    @JoinColumn(name = "ID_PACIENTE")
    private Paciente paciente;

    @ManyToOne
    @JoinColumn(name = "ID_PROFISSIONAL")
    private Profissional profissional;

    @Lob()
    @Column(name = "TERMO_GERADO")
    private String termoGerado;

    @ManyToOne
    @JoinColumn(name = "ID_PLANO_TRATAMENTO")
    private PlanoTratamento planoTratamento;

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

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getDataHora() {
        return this.dataHora;
    }

    @Transient
    public String getDataHoraStr() {
        return Utils.dateToString(this.dataHora);
    }

    @Transient
    public String getDataHoraStrOrd() {
        return Utils.dateToString(this.dataHora, "yyyy/MM/dd HH:mm:ss");
    }

    public void setDataHora(Date dataHora) {
        this.dataHora = dataHora;
    }

    public Paciente getPaciente() {
        return this.paciente;
    }

    public void setPaciente(Paciente paciente) {
        this.paciente = paciente;
    }

    public Profissional getProfissional() {
        return this.profissional;
    }

    public void setProfissional(Profissional profissional) {
        this.profissional = profissional;
    }

    public String getTermoGerado() {
        return this.termoGerado;
    }

    public void setTermoGerado(String termoGerado) {
        this.termoGerado = termoGerado;
    }

    public PlanoTratamento getPlanoTratamento() {
        return this.planoTratamento;
    }

    public void setPlanoTratamento(PlanoTratamento planoTratamento) {
        this.planoTratamento = planoTratamento;
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
        TermoConsentimento other = (TermoConsentimento) obj;
        if (this.id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!this.id.equals(other.id)) {
            return false;
        }
        return true;
    }
}
