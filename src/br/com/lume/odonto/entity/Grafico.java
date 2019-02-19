package br.com.lume.odonto.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import br.com.lume.common.util.Status;

@Entity
@Table(name = "GRAFICO")
public class Grafico implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "DESCRICAO")
    private String descricao;

    @Column(name = "SQL")
    private String sql;

    @Column(name = "MINIMO")
    private int minimo;

    @Column(name = "MAXIMO")
    private int maximo;

    @Column(name = "INICIO")
    private int inicio;

    @Column(name = "FIM")
    private int fim;

    @Column(name = "VERDE")
    private int verde;

    @Column(name = "VERMELHO")
    private int vermelho;

    @Column(name = "AMARELO")
    private int amarelo;

    @Column(name = "ID_EMPRESA")
    private long idEmpresa;

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

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getDescricao() {
        return this.descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getSql() {
        return this.sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public long getIdEmpresa() {
        return this.idEmpresa;
    }

    public void setIdEmpresa(long idEmpresa) {
        this.idEmpresa = idEmpresa;
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
        Grafico other = (Grafico) obj;
        if (this.id != other.id) {
            return false;
        }
        return true;
    }

    public int getMinimo() {
        return this.minimo;
    }

    public void setMinimo(int minimo) {
        this.minimo = minimo;
    }

    public int getMaximo() {
        return this.maximo;
    }

    public void setMaximo(int maximo) {
        this.maximo = maximo;
    }

    public int getInicio() {
        return this.inicio;
    }

    public void setInicio(int inicio) {
        this.inicio = inicio;
    }

    public int getFim() {
        return this.fim;
    }

    public void setFim(int fim) {
        this.fim = fim;
    }

    public int getVerde() {
        return this.verde;
    }

    public void setVerde(int verde) {
        this.verde = verde;
    }

    public int getVermelho() {
        return this.vermelho;
    }

    public void setVermelho(int vermelho) {
        this.vermelho = vermelho;
    }

    public int getAmarelo() {
        return this.amarelo;
    }

    public void setAmarelo(int amarelo) {
        this.amarelo = amarelo;
    }

}
