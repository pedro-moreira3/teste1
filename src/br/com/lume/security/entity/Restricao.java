package br.com.lume.security.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "SEG_RESTRICAO")
public class Restricao implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "RES_INT_COD")
    private long resIntCod;

    @ManyToOne
    @JoinColumn(name = "PER_INT_COD")
    private Perfil perfil;

    @ManyToOne
    @JoinColumn(name = "ACA_INT_COD")
    private Acao acao;

    @ManyToOne
    @JoinColumn(name = "OBJ_INT_COD")
    private Objeto objeto;

    public Restricao() {
    }

    public Restricao(
            Perfil perfil,
            Acao acao,
            Objeto objeto) {
        this.perfil = perfil;
        this.acao = acao;
        this.objeto = objeto;
    }

    public long getResIntCod() {
        return this.resIntCod;
    }

    public void setResIntCod(long resIntCod) {
        this.resIntCod = resIntCod;
    }

    public Perfil getPerfil() {
        return this.perfil;
    }

    public void setPerfil(Perfil perfil) {
        this.perfil = perfil;
    }

    public Acao getAcao() {
        return this.acao;
    }

    public void setAcao(Acao acao) {
        this.acao = acao;
    }

    public Objeto getObjeto() {
        return this.objeto;
    }

    public void setObjeto(Objeto objeto) {
        this.objeto = objeto;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (this.resIntCod ^ (this.resIntCod >>> 32));
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
        Restricao other = (Restricao) obj;
        if (this.resIntCod != other.resIntCod) {
            return false;
        }
        return true;
    }
}
