package br.com.lume.odonto.entity;

import java.io.Serializable;
import java.text.Normalizer;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import br.com.lume.odonto.bo.ParceiroBO;

@Entity
@Table(name = "PORTALID.CERTIFICADO")
public class Certificado implements Serializable, Comparable<Certificado> {

    private static final long serialVersionUID = 1L;

    public Certificado() {
    }

    public Certificado(
            String descricao) {
        this.descricao = descricao;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "DESCRICAO")
    private String descricao;

    @Column(name = "ID_PARCEIRO")
    private long parceiro;

    @Transient
    public Parceiro getParceiroStr() {
        try {
            return new ParceiroBO().find(this.getParceiro());
        } catch (Exception e) {
            return new Parceiro();
        }
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

    public long getParceiro() {
        return this.parceiro;
    }

    public void setParceiro(long parceiro) {
        this.parceiro = parceiro;
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
        Certificado other = (Certificado) obj;
        if (this.id != other.id) {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(Certificado m) {
        if (this.descricao == null || m == null || m.getDescricao() == null) {
            return 1;
        } else {
            return Normalizer.normalize(this.getDescricao(), Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "").compareToIgnoreCase(Normalizer.normalize(m.getDescricao(), Normalizer.Form.NFD));
        }
    }
}
