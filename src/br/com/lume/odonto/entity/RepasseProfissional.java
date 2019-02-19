package br.com.lume.odonto.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
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

import br.com.lume.common.util.Utils;

@Entity
@Table(name = "REPASSE_PROFISSIONAL")
public class RepasseProfissional implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 7813591723435640374L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "DATA")
    @Temporal(TemporalType.TIMESTAMP)
    private Date data;

    @Column(name = "DATA_PAGAMENTO")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataPagamento;

    @OneToMany(mappedBy = "repasse", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RepasseItem> repasseItens;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_PROFISSIONAL")
    private Profissional profissional;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_PROFISSIONAL_PAGAMENTO")
    private Profissional profissionalPagou;

    private String status;

    public RepasseProfissional() {
    }

    public RepasseProfissional(
            Date data, Profissional profissional, String status) {
        super();
        this.data = data;
        this.profissional = profissional;
        this.status = status;
    }

    public RepasseProfissional(
            Date data, Date dataPagamento, Profissional profissional, Profissional profissionalPagou, String status) {
        super();
        this.data = data;
        this.dataPagamento = dataPagamento;
        this.profissional = profissional;
        this.profissionalPagou = profissionalPagou;
        this.status = status;
    }

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Date getData() {
        return this.data;
    }

    @Transient
    public String getDataStr() {
        return Utils.dateToString(this.data, "dd/MM/yyyy HH:mm:ss");
    }

    public void setData(Date data) {
        this.data = data;
    }

    public List<RepasseItem> getRepasseItens() {
        return this.repasseItens;
    }

    public void setRepasseItens(List<RepasseItem> repasseItens) {
        this.repasseItens = repasseItens;
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
        RepasseProfissional other = (RepasseProfissional) obj;
        if (this.id != other.id) {
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

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getDataPagamento() {
        return this.dataPagamento;
    }

    @Transient
    public String getDataPagamentoStr() {
        return Utils.dateToString(this.dataPagamento, "dd/MM/yyyy HH:mm:ss");
    }

    public void setDataPagamento(Date dataPagamento) {
        this.dataPagamento = dataPagamento;
    }

    public Profissional getProfissionalPagou() {
        return this.profissionalPagou;
    }

    public void setProfissionalPagou(Profissional profissionalPagou) {
        this.profissionalPagou = profissionalPagou;
    }
}
