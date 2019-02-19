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
@Table(name = "CONTRATO")
public class Contrato implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "DATA_HORA")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataHora = Calendar.getInstance().getTime();

    @ManyToOne
    @JoinColumn(name = "ID_PROFISSIONAL_CONTRATADO")
    private Profissional profissionalContratado;

    @ManyToOne
    @JoinColumn(name = "ID_PROFISSIONAL_CONTRATANTE")
    private Profissional profissionalContratante;

    @Lob()
    @Column(name = "CONTRATO_GERADO")
    private String contratoGerado;

    private String excluido = Status.NAO;

    @Column(name = "DATA_EXCLUSAO")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataExclusao;

    @Column(name = "DATA_INICIAL")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataInicial;

    @Column(name = "DATA_FINAL")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataFinal;

    @ManyToOne
    @JoinColumn(name = "FORMA_CONTRATACAO")
    private Dominio formaContratacao;

    @Column(name = "EXCLUIDO_POR")
    private Long excluidoPorProfissional;

    public Long getExcluidoPorProfissional() {
        return this.excluidoPorProfissional;
    }

    public void setExcluidoPorProfissional(Long excluidoPorProfissional) {
        this.excluidoPorProfissional = excluidoPorProfissional;
    }

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
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

    public Profissional getProfissionalContratado() {
        return this.profissionalContratado;
    }

    public void setProfissionalContratado(Profissional profissionalContratado) {
        this.profissionalContratado = profissionalContratado;
    }

    public Profissional getProfissionalContratante() {
        return this.profissionalContratante;
    }

    public void setProfissionalContratante(Profissional profissionalContratante) {
        this.profissionalContratante = profissionalContratante;
    }

    public String getContratoGerado() {
        return this.contratoGerado;
    }

    public void setContratoGerado(String contratoGerado) {
        this.contratoGerado = contratoGerado;
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
        Contrato other = (Contrato) obj;
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

    public Date getDataExclusao() {
        return this.dataExclusao;
    }

    public void setDataExclusao(Date dataExclusao) {
        this.dataExclusao = dataExclusao;
    }

    public Date getDataInicial() {
        return this.dataInicial;
    }

    public void setDataInicial(Date dataInicial) {
        this.dataInicial = dataInicial;
    }

    public Date getDataFinal() {
        return this.dataFinal;
    }

    public void setDataFinal(Date dataFinal) {
        this.dataFinal = dataFinal;
    }

    public Dominio getFormaContratacao() {
        return this.formaContratacao;
    }

    public void setFormaContratacao(Dominio formaContratacao) {
        this.formaContratacao = formaContratacao;
    }

    @Transient
    public String getDataInicialStr() {
        return Utils.dateToString(this.dataInicial, "dd/MM/yy");
    }

    @Transient
    public String getDataFinalStr() {
        return Utils.dateToString(this.dataFinal, "dd/MM/yy");
    }

    @Transient
    public String getDataFinalStrOrd() {
        return Utils.dateToString(this.dataFinal, "yyyy/MM/dd HH:mm:ss");
    }

    @Transient
    public String getDataInicialStrOrd() {
        return Utils.dateToString(this.dataInicial, "yyyy/MM/dd HH:mm:ss");
    }
}
