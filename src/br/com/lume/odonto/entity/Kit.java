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
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import br.com.lume.common.util.Status;
import br.com.lume.common.util.Utils;

@Entity
@Table(name = "KIT")
public class Kit implements Serializable, Comparable<Kit> {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "ID_KIT")
    private List<KitItem> kitItens;

    @Column(name = "ID_EMPRESA")
    private Long idEmpresa;

    @Column(name = "DATA")
    @Temporal(TemporalType.TIMESTAMP)
    private Date data;

    @Column(name = "DESCRICAO")
    private String descricao;

    @Column(name = "OBSERVACAO")
    private String observacao;

    private String excluido = Status.NAO;

    @Column(name = "DATA_EXCLUSAO")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataExclusao;

    private String tipo;

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

    public long getId() {
        return this.id;
    }

    @Transient
    public String getDataStr() {
        return Utils.dateToString(this.data, "dd/MM/yyyy");
    }

    @Transient
    public String getDataStrOrd() {
        return Utils.dateToString(this.data, "yyyy/MM/dd");
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

    public String getObservacao() {
        return this.observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }

    public List<KitItem> getKitItens() {
        return this.kitItens;
    }

    public void setKitItens(List<KitItem> kitItens) {
        this.kitItens = kitItens;
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
        Kit other = (Kit) obj;
        if (this.id != other.id) {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(Kit p) {
        if (this.getData().after(p.getData())) {
            return -1;
        } else {
            return 1;
        }
    }

    public String getTipo() {
        return this.tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }
}
