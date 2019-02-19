package br.com.lume.odonto.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.CascadeType;
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
import br.com.lume.odonto.bo.DominioBO;

@Entity
@Table(name = "SUGESTAO_ITEM")
public class SugestaoItem implements Serializable, Comparable<SugestaoItem> {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "ID_SUGESTAO")
    private Sugestao sugestao;

    @ManyToOne
    @JoinColumn(name = "ID_ITEM")
    private Item item;

    private Integer quantidade;

    private String status;

    @Column(name = "DATA_STATUS")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataStatus;

    private String excluido = Status.NAO;

    @Column(name = "DATA_EXCLUSAO")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataExclusao;

    private String justificativa;

    @Column(name = "NOVO_ITEM")
    private String novoItem;

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
        SugestaoItem other = (SugestaoItem) obj;
        if (this.id != other.id) {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(SugestaoItem cm) {
        if (this.getId() > cm.getId()) {
            return 1;
        } else {
            return -1;
        }
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

    public Sugestao getSugestao() {
        return this.sugestao;
    }

    public void setSugestao(Sugestao sugestao) {
        this.sugestao = sugestao;
    }

    public Item getItem() {
        return this.item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public Integer getQuantidade() {
        return this.quantidade;
    }

    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
    }

    public String getStatus() {
        return this.status;
    }

    @Transient
    public String getStatusStr() {
        String aux = "";
        try {
            aux = new DominioBO().findByEmpresaAndObjetoAndTipoAndValor("sugestao", "status", this.status).getNome();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return aux;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getDataStatus() {
        return this.dataStatus;
    }

    public void setDataStatus(Date dataStatus) {
        this.dataStatus = dataStatus;
    }

    public String getJustificativa() {
        return this.justificativa;
    }

    public void setJustificativa(String justificativa) {
        this.justificativa = justificativa;
    }

    public String getNovoItem() {
        return this.novoItem;
    }

    public void setNovoItem(String novoItem) {
        this.novoItem = novoItem;
    }
}
