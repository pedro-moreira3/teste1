package br.com.lume.odonto.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import br.com.lume.common.util.Utils;

/**
 * The persistent class for the SEG_EMPRESA database table.
 */
@Entity
public class RelatorioUsuario implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "EMP_INT_COD")
    private long empIntCod;

    @Column(name = "EMP_STR_NME")
    private String empStrNme;

    private String admin;

    @Column(name = "EMP_DTM_CRIACAO")
    @Temporal(TemporalType.DATE)
    private Date empDtmCriacao;

    @Column(name = "EMP_DTM_EXPIRACAO")
    @Temporal(TemporalType.DATE)
    private Date empDtmExpiracao;

    @Column(name = "ULTACESSO")
    @Temporal(TemporalType.DATE)
    private Date ultacesso;

    @Column(name = "EMP_CHA_FONE")
    private String empChaFone;

    private String celular;

    private String email;

    public long getEmpIntCod() {
        return this.empIntCod;
    }

    public void setEmpIntCod(long empIntCod) {
        this.empIntCod = empIntCod;
    }

    public String getEmpStrNme() {
        return this.empStrNme;
    }

    public void setEmpStrNme(String empStrNme) {
        this.empStrNme = empStrNme;
    }

    public String getAdmin() {
        return this.admin;
    }

    public void setAdmin(String admin) {
        this.admin = admin;
    }

    public Date getEmpDtmCriacao() {
        return this.empDtmCriacao;
    }

    public String getEmpDtmCriacaoStr() {
        return this.empDtmCriacao != null ? Utils.dateToString(this.empDtmCriacao, "dd/MM/yyyy") : "";
    }

    public void setEmpDtmCriacao(Date empDtmCriacao) {
        this.empDtmCriacao = empDtmCriacao;
    }

    public Date getEmpDtmExpiracao() {
        return this.empDtmExpiracao;
    }

    public String getEmpDtmExpiracaoStr() {
        return this.empDtmExpiracao != null ? Utils.dateToString(this.empDtmExpiracao, "dd/MM/yyyy") : "";
    }

    public void setEmpDtmExpiracao(Date empDtmExpiracao) {
        this.empDtmExpiracao = empDtmExpiracao;
    }

    public String getEmpChaFone() {
        return this.empChaFone;
    }

    public void setEmpChaFone(String empChaFone) {
        this.empChaFone = empChaFone;
    }

    public String getCelular() {
        return this.celular;
    }

    public void setCelular(String celular) {
        this.celular = celular;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Date getUltacesso() {
        return this.ultacesso;
    }

    public void setUltacesso(Date ultacesso) {
        this.ultacesso = ultacesso;
    }

    public String getUltacessoStr() {
        return this.ultacesso != null ? Utils.dateToString(this.ultacesso, "dd/MM/yyyy") : "";
    }

}
