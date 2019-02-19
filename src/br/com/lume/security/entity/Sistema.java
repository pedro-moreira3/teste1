package br.com.lume.security.entity;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import br.com.lume.common.util.Status;

/**
 * The persistent class for the SEG_SISTEMA database table.
 */
@Entity
@Table(name = "SEG_SISTEMA")
public class Sistema implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SIS_INT_COD")
    private long sisIntCod;

    @Column(name = "SIS_CHA_STS")
    private String sisChaSts;

    @Column(name = "SIS_STR_DES")
    private String sisStrDes;

    @Column(name = "SIS_CHA_SIGLA")
    private String sisChaSigla;

    @OneToMany(mappedBy = "sistema")
    private List<Perfil> perfils;

    @OneToMany(mappedBy = "sistema")
    private List<Objeto> objetos;

    public Sistema() {
    }

    public long getSisIntCod() {
        return this.sisIntCod;
    }

    public void setSisIntCod(long sisIntCod) {
        this.sisIntCod = sisIntCod;
    }

    public String getSisChaSts() {
        return Status.INATIVO.equalsIgnoreCase(this.sisChaSts) ? Boolean.TRUE.toString() : Boolean.FALSE.toString();
    }

    public void setSisChaSts(String sisChaSts) {
        if (Status.ATIVO.equalsIgnoreCase(sisChaSts) || Status.INATIVO.equalsIgnoreCase(sisChaSts)) {
            this.sisChaSts = sisChaSts;
        } else {
            this.sisChaSts = Boolean.TRUE.toString().equalsIgnoreCase(sisChaSts) ? Status.INATIVO : Status.ATIVO;
        }
    }

    public String getSisStrDes() {
        return this.sisStrDes;
    }

    public void setSisStrDes(String sisStrDes) {
        this.sisStrDes = sisStrDes;
    }

    public List<Perfil> getPerfils() {
        return this.perfils;
    }

    public void setPerfils(List<Perfil> perfils) {
        this.perfils = perfils;
    }

    public String getSisChaSigla() {
        return this.sisChaSigla;
    }

    public void setSisChaSigla(String sisChaSigla) {
        this.sisChaSigla = sisChaSigla;
    }

    @Override
    public String toString() {
        return this.sisStrDes;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (this.sisIntCod ^ (this.sisIntCod >>> 32));
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
        Sistema other = (Sistema) obj;
        if (this.sisIntCod != other.sisIntCod) {
            return false;
        }
        return true;
    }

    public List<Objeto> getObjetos() {
        return this.objetos;
    }

    public void setObjetos(List<Objeto> objetos) {
        this.objetos = objetos;
    }
}
