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

/**
 * The persistent class for the SEG_SENUSADA database table.
 */
@Entity
@Table(name = "SEG_SENUSADA")
public class SenhaUsada implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SUS_INT_COD")
    private long susIntCod;

    @Column(name = "SUS_STR_SENHA")
    private String susStrSenha;

    // bi-directional many-to-one association to Usuario
    @ManyToOne
    @JoinColumn(name = "USU_INT_COD")
    private Usuario usuario;

    public SenhaUsada() {
    }

    public long getSusIntCod() {
        return this.susIntCod;
    }

    public void setSusIntCod(long susIntCod) {
        this.susIntCod = susIntCod;
    }

    public String getSusStrSenha() {
        return this.susStrSenha;
    }

    public void setSusStrSenha(String susStrSenha) {
        this.susStrSenha = susStrSenha;
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
        result = prime * result + (int) (this.susIntCod ^ (this.susIntCod >>> 32));
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
        SenhaUsada other = (SenhaUsada) obj;
        if (this.susIntCod != other.susIntCod) {
            return false;
        }
        return true;
    }
}
