package br.com.lume.security.entity;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import br.com.lume.common.util.Status;

/**
 * The persistent class for the SEG_COMUNICADO database table.
 */
@Entity
@Table(name = "SEG_COMUNICADO")
public class Comunicado implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CMN_INT_COD")
    private long cmnIntCod;

    @Column(name = "CMN_CHA_STS")
    private String cmnChaSts = Status.ATIVO;

    @Column(name = "CMN_DTM_CADASTRO")
    @Temporal(TemporalType.TIMESTAMP)
    private Date cmnDtmCadastro = Calendar.getInstance().getTime();

    @Column(name = "CMN_DTM_INI")
    @Temporal(TemporalType.TIMESTAMP)
    private Date cmnDtmIni;

    @Column(name = "CMN_DTM_FIM")
    @Temporal(TemporalType.TIMESTAMP)
    private Date cmnDtmFim;

    @Column(name = "CMN_STR_DES")
    private String cmnStrDes;

    @ManyToOne
    @JoinColumn(name = "SIS_INT_COD")
    private Sistema sistema;

    @ManyToMany
    @JoinTable(name = "SEG_EMPCOMUNICADO", joinColumns = { @JoinColumn(name = "CMN_INT_COD") }, inverseJoinColumns = { @JoinColumn(name = "EMP_INT_COD") })
    private List<Empresa> empresas;

    public Comunicado() {
    }

    public long getCmnIntCod() {
        return this.cmnIntCod;
    }

    public void setCmnIntCod(long cmnIntCod) {
        this.cmnIntCod = cmnIntCod;
    }

    public String getCmnChaSts() {
        return this.cmnChaSts;
    }

    public void setCmnChaSts(String cmnChaSts) {
        this.cmnChaSts = cmnChaSts;
    }

    public Date getCmnDtmCadastro() {
        return this.cmnDtmCadastro;
    }

    public void setCmnDtmCadastro(Date cmnDtmCadastro) {
        this.cmnDtmCadastro = cmnDtmCadastro;
    }

    public Date getCmnDtmIni() {
        return this.cmnDtmIni;
    }

    public void setCmnDtmIni(Date cmnDtmIni) {
        this.cmnDtmIni = cmnDtmIni;
    }

    public Date getCmnDtmFim() {
        return this.cmnDtmFim;
    }

    public void setCmnDtmFim(Date cmnDtmFim) {
        this.cmnDtmFim = cmnDtmFim;
    }

    public String getCmnStrDes() {
        return this.cmnStrDes;
    }

    public void setCmnStrDes(String cmnStrDes) {
        this.cmnStrDes = cmnStrDes;
    }

    public Sistema getSistema() {
        return this.sistema;
    }

    public void setSistema(Sistema segSistema) {
        this.sistema = segSistema;
    }

    public List<Empresa> getEmpresas() {
        return this.empresas;
    }

    public void setEmpresas(List<Empresa> segEmpcomunicados) {
        this.empresas = segEmpcomunicados;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (this.cmnIntCod ^ (this.cmnIntCod >>> 32));
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
        Comunicado other = (Comunicado) obj;
        if (this.cmnIntCod != other.cmnIntCod) {
            return false;
        }
        return true;
    }
}
