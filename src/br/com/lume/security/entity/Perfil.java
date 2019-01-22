package br.com.lume.security.entity;

import java.io.Serializable;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;

import br.com.lume.common.util.Status;

/**
 * The persistent class for the SEG_PERFIL database table.
 */
@Entity
@Table(name = "SEG_PERFIL")
public class Perfil implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PER_INT_COD")
    private long perIntCod;

    @Column(name = "PER_CHA_STS")
    private String perChaSts;

    @Column(name = "PER_STR_DES")
    private String perStrDes;

    @ManyToOne
    @JoinColumn(name = "SIS_INT_COD")
    private Sistema sistema;

    @ManyToMany
    @JoinTable(name = "SEG_PEROBJETO", joinColumns = { @JoinColumn(name = "PER_INT_COD") }, inverseJoinColumns = { @JoinColumn(name = "OBJ_INT_COD") })
    @OrderBy(value = "objStrDes asc")
    private List<Objeto> perfisObjeto;

    @ManyToMany(mappedBy = "perfisUsuarios")
    private List<Usuario> usuariosPerfil;

    @OneToMany(mappedBy = "perfil", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Restricao> restricoes;

    public static final String PACIENTE = "Paciente";

    public Perfil() {
    }

    public List<Restricao> getRestricoes() {
        return this.restricoes;
    }

    public void setRestricoes(List<Restricao> restricoes) {
        this.restricoes = restricoes;
    }

    public long getPerIntCod() {
        return this.perIntCod;
    }

    public void setPerIntCod(long perIntCod) {
        this.perIntCod = perIntCod;
    }

    public List<Usuario> getUsuariosPerfil() {
        return this.usuariosPerfil;
    }

    public void setUsuariosPerfil(List<Usuario> usuariosPerfil) {
        this.usuariosPerfil = usuariosPerfil;
    }

    public String getPerChaSts() {
        return Status.INATIVO.equalsIgnoreCase(this.perChaSts) ? Boolean.TRUE.toString() : Boolean.FALSE.toString();
    }

    public void setPerChaSts(String perChaSts) {
        if (Status.ATIVO.equalsIgnoreCase(perChaSts) || Status.INATIVO.equalsIgnoreCase(perChaSts)) {
            this.perChaSts = perChaSts;
        } else {
            this.perChaSts = Boolean.TRUE.toString().equalsIgnoreCase(perChaSts) ? Status.INATIVO : Status.ATIVO;
        }
    }

    public String getPerStrDes() {
        return this.perStrDes;
    }

    public void setPerStrDes(String perStrDes) {
        this.perStrDes = perStrDes;
    }

    public Sistema getSistema() {
        return this.sistema;
    }

    public void setSistema(Sistema segSistema) {
        this.sistema = segSistema;
    }

    @Override
    public String toString() {
        return this.perStrDes;
    }

    public List<Objeto> getPerfisObjeto() {
        return this.perfisObjeto;
    }

    public void setPerfisObjeto(List<Objeto> perfisObjeto) {
        this.perfisObjeto = perfisObjeto;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (this.perIntCod ^ (this.perIntCod >>> 32));
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
        Perfil other = (Perfil) obj;
        if (this.perIntCod != other.perIntCod) {
            return false;
        }
        return true;
    }
}
