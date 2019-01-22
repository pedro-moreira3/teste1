package br.com.lume.odonto.entity;

import java.io.Serializable;
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
@Table(name = "PEDIDO")
public class Pedido implements Serializable, Comparable<Pedido> {

    private static final long serialVersionUID = 1L;

    public static String PENDENTE = "PE";

    public static String EM_TRANSITO = "ET";

    public static String FINALIZADO = "FI";

    public static String CANCELADO = "CA";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    @JoinColumn(name = "ID_PROFISSIONAL")
    private Profissional profissional;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "ID_PEDIDO")
    private List<PedidoItem> pedidoItens;

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

    @Column(name = "STATUS")
    private String status;

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

    @Transient
    public String getDataStr() {
        return Utils.dateToString(this.data, "dd/MM/yyyy");
    }

    @Transient
    public String getDataStrOrd() {
        return Utils.dateToString(this.data, "yyyy/MM/dd");
    }

    @Transient
    public String getPrazoStr() {
        return Utils.dateToString(this.prazo, "dd/MM/yyyy");
    }

    @Transient
    public String getPrazoStrOrd() {
        return Utils.dateToString(this.prazo, "yyyy/MM/dd");
    }

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
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

    public List<PedidoItem> getPedidoItens() {
        return this.pedidoItens;
    }

    public void setPedidoItens(List<PedidoItem> pedidoItens) {
        this.pedidoItens = pedidoItens;
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
        Pedido other = (Pedido) obj;
        if (this.id != other.id) {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(Pedido p) {
        if (this.getData().after(p.getData())) {
            return -1;
        } else {
            return 1;
        }
    }

    public String getStatus() {
        return this.status;
    }

    public String getStatusStr() {
        return this.status != null ? this.status.equals(
                PENDENTE) ? "Pendente" : this.status.equals(CANCELADO) ? "Cancelado" : this.status.equals(EM_TRANSITO) ? "Em Transito" : this.status.equals(FINALIZADO) ? "Finalizado" : "" : "";
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
