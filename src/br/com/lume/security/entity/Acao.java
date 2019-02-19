package br.com.lume.security.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "SEG_ACAO")
public class Acao implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ACA_INT_COD")
    private long acaIntCod;

    @Column(name = "ACA_STR_DES")
    private String acaStrDes;

    public long getAcaIntCod() {
        return this.acaIntCod;
    }

    public void setAcaIntCod(long acaIntCod) {
        this.acaIntCod = acaIntCod;
    }

    public String getAcaStrDes() {
        return this.acaStrDes;
    }

    public void setAcaStrDes(String acaStrDes) {
        this.acaStrDes = acaStrDes;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (this.acaIntCod ^ (this.acaIntCod >>> 32));
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
        Acao other = (Acao) obj;
        if (this.acaIntCod != other.acaIntCod) {
            return false;
        }
        return true;
    }
}
