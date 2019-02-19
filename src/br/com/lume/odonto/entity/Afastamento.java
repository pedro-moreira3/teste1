package br.com.lume.odonto.entity;

import java.io.Serializable;
import java.text.SimpleDateFormat;
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
import br.com.lume.odonto.bo.DominioBO;

@Entity
@Table(name = "AFASTAMENTO")
public class Afastamento implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    @JoinColumn(name = "ID_PROFISSIONAL")
    private Profissional profissional;

    @Temporal(TemporalType.TIMESTAMP)
    private Date inicio;

    @Temporal(TemporalType.TIMESTAMP)
    private Date fim;

    @Column(name = "TIPO")
    private String tipo;

    @Column(name = "OBSERVACAO")
    private String observacao;

    private String excluido = Status.NAO;

    @Column(name = "DATA_EXCLUSAO")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataExclusao;

    @Column(name = "EXCLUIDO_POR")
    private Long excluidoPorProfissional;

    @Transient
    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

    private String valido = Status.NAO;

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

    public Profissional getProfissional() {
        return this.profissional;
    }

    public void setProfissional(Profissional profissional) {
        this.profissional = profissional;
    }

    public Date getInicio() {
        return this.inicio;
    }

    public String getInicioStr() {
        return Utils.dateToString(this.inicio);
    }

    public String getInicioStrOrd() {
        return Utils.dateToString(this.inicio, "yyyy/MM/dd");
    }

    public void setInicio(Date inicio) {
        this.inicio = inicio;
    }

    public Date getFim() {
        return this.fim;
    }

    public String getFimStr() {
        return Utils.dateToString(this.fim);
    }

    public String getFimStrOrd() {
        return Utils.dateToString(this.fim, "yyyy/MM/dd");
    }

    public void setFim(Date fim) {
        this.fim = fim;
    }

    public String getTipo() {
        return this.tipo;
    }

    public String getTipoAfastamentoStr() {
        Dominio dominio = null;
        try {
            dominio = new DominioBO().listByTipoAndObjeto(this.tipo, "afastamento");
        } catch (Exception e) {
            return "";
        }
        return dominio != null ? dominio.getNome() : "";
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public static long getSerialversionuid() {
        return serialVersionUID;
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
        Afastamento other = (Afastamento) obj;
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

    public String getObservacao() {
        return this.observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }

    public String getValido() {
        return this.valido;
    }

    public String getValidoStr() {
        return "S".equals(this.valido) ? "Sim" : "Não";
    }

    public void setValido(String valido) {
        this.valido = valido;
    }
}
