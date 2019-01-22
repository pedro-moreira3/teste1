package br.com.lume.odonto.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.CascadeType;
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

import br.com.lume.odonto.bo.CertificadoBO;
import br.com.lume.odonto.bo.UserBO;

@Entity
@Table(name = "PORTALID.CERTIFICACOES")
public class Certificacao implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "ID_CERTIFICADO")
    private Certificado certificado;

    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "ID_USUARIO")
    private User usuario;

    @Column(name = "DATA_INICIAL")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataInicial;

    @Column(name = "DATA_FINAL")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataFinal;

    @Transient
    public User getUsuarioStr() {
        try {
            return new UserBO().find(this.getUsuario());
        } catch (Exception e) {
            return new User();
        }
    }

    @Transient
    public Certificado getCertificadoStr() {
        try {
            return new CertificadoBO().find(this.getCertificado());
        } catch (Exception e) {
            return new Certificado();
        }
    }

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Date getDataInicial() {
        return this.dataInicial;
    }

    public void setDataInicial(Date dataInicial) {
        this.dataInicial = dataInicial;
    }

    public Date getDataFinal() {
        return this.dataFinal;
    }

    public void setDataFinal(Date dataFinal) {
        this.dataFinal = dataFinal;
    }

    public Certificado getCertificado() {
        return this.certificado;
    }

    public void setCertificado(Certificado certificado) {
        this.certificado = certificado;
    }

    public User getUsuario() {
        return this.usuario;
    }

    public void setUsuario(User usuario) {
        this.usuario = usuario;
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
        Certificacao other = (Certificacao) obj;
        if (this.id != other.id) {
            return false;
        }
        return true;
    }
}
