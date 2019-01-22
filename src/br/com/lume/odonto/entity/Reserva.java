package br.com.lume.odonto.entity;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import br.com.lume.common.util.Status;
import br.com.lume.common.util.Utils;

@Entity
@Table(name = "RESERVA")
public class Reserva implements Serializable, Comparable<Reserva> {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    @JoinColumn(name = "ID_PROFISSIONAL")
    private Profissional profissional;

    @ManyToOne
    @JoinColumn(name = "ID_LOCAL")
    private Local local;

    @ManyToOne
    @JoinColumn(name = "ID_AGENDAMENTO")
    private Agendamento agendamento;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "ID_RESERVA")
    private List<ReservaKit> reservaKits;

    @Column(name = "ID_EMPRESA")
    private Long idEmpresa;

    @Column(name = "DATA")
    @Temporal(TemporalType.TIMESTAMP)
    private Date data;

    @Column(name = "PRAZO")
    @Temporal(TemporalType.TIMESTAMP)
    private Date prazo;

    @Column(name = "OBSERVACAO")
    private String observacao;

    @Column(name = "DESCRICAO")
    private String descricao;

    private String excluido = Status.NAO;

    @Column(name = "DATA_EXCLUSAO")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataExclusao;

    @Transient
    private SimpleDateFormat sdf;

    @Column(name = "EXCLUIDO_POR")
    private Long excluidoPorProfissional;

    public static final String PENDENTE = "PE";

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

    @Transient
    public String getDataStr() {
        return Utils.dateToString(this.data, "dd/MM/yyyy");
    }

    @Transient
    public String getPrazoStr() {
        return Utils.dateToString(this.prazo, "dd/MM/yyyy");
    }

    @Transient
    public String getPrazoStrOrd() {
        return Utils.dateToString(this.prazo, "yyyy/MM/dd");
    }

    @Transient
    public String getDataStrOrd() {
        return Utils.dateToString(this.data, "yyyy/MM/dd");
    }

    public List<ReservaKit> getReservaKits() {
        return this.reservaKits;
    }

    public void setReservaKits(List<ReservaKit> reservaKits) {
        this.reservaKits = reservaKits;
    }

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Local getLocal() {
        return this.local;
    }

    public void setLocal(Local local) {
        this.local = local;
    }

    public Long getIdEmpresa() {
        return this.idEmpresa;
    }

    public void setIdEmpresa(Long idEmpresa) {
        this.idEmpresa = idEmpresa;
    }

    public Date getData() {
        return this.data;
    }

    public void setData(Date data) {
        this.data = data;
    }

    public Date getPrazo() {
        return this.prazo;
    }

    public void setPrazo(Date prazo) {
        this.prazo = prazo;
    }

    public String getObservacao() {
        return this.observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }

    public Profissional getProfissional() {
        return this.profissional;
    }

    public void setProfissional(Profissional profissional) {
        this.profissional = profissional;
    }

    public String getDescricao() {
        return this.descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
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
        Reserva other = (Reserva) obj;
        if (this.id != other.id) {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(Reserva p) {
        if (this.getData().after(p.getData())) {
            return -1;
        } else {
            return 1;
        }
    }

    public Agendamento getAgendamento() {
        return this.agendamento;
    }

    public void setAgendamento(Agendamento agendamento) {
        this.agendamento = agendamento;
    }
}
