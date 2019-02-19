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
@Table(name = "SEG_PERUSUARIO")
public class PerfilUsuario implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PEU_INT_COD")
    private long peuIntCod;

    @ManyToOne
    @JoinColumn(name = "PER_INT_COD")
    private Perfil perfil;

    @ManyToOne
    @JoinColumn(name = "USU_INT_COD")
    private Usuario usuario;

    public long getPeuIntCod() {
        return this.peuIntCod;
    }

    public void setPeuIntCod(long peuIntCod) {
        this.peuIntCod = peuIntCod;
    }

    public Perfil getPerfil() {
        return this.perfil;
    }

    public void setPerfil(Perfil perfil) {
        this.perfil = perfil;
    }

    public Usuario getUsuario() {
        return this.usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (this.peuIntCod ^ (this.peuIntCod >>> 32));
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
        PerfilUsuario other = (PerfilUsuario) obj;
        if (this.peuIntCod != other.peuIntCod) {
            return false;
        }
        return true;
    }
}
