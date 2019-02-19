package br.com.lume.security.entity;

import java.beans.Transient;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import br.com.lume.common.util.JSFHelper;
import br.com.lume.common.util.Status;
import br.com.lume.common.util.Utils;

/**
 * The persistent class for the SEG_USUARIO database table.
 */
@Entity
@Table(name = "SEG_USUARIO")
public class Usuario implements Serializable {

    public static final Character LOG_ACESSO_ATIVO = 'S';

    public static final Character LOG_ACESSO_INATIVO = 'N';

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "USU_INT_COD")
    private long usuIntCod;

    @Column(name = "USU_CHA_CPF")
    private String usuChaCpf;

    @Column(name = "USU_CHA_INLOGACESSO")
    private String usuChaInlogacesso;

    @Column(name = "USU_CHA_STS")
    private String usuChaSts;

    @Column(name = "USU_CHA_ADM")
    private String usuChaAdm;

    @Column(name = "USU_CHA_TUTORIAL")
    private String usuChaTutorial;

    @Column(name = "USU_DTM_ULTTROCASENHA")
    private Timestamp usuDtmUlttrocasenha = new Timestamp(Calendar.getInstance().getTimeInMillis());

    @Column(name = "USU_INT_DIASTROCASENHA")
    private int usuIntDiastrocasenha;

    @Column(name = "USU_STR_EML")
    private String usuStrEml;

    @Column(name = "USU_STR_LOGIN", unique = true)
    private String usuStrLogin;

    @Column(name = "USU_STR_NME")
    private String usuStrNme;

    @Column(name = "USU_STR_SENHA")
    private String usuStrSenha;

    @OneToMany(mappedBy = "usuario")
    private List<LogAcesso> logAcessos;

    @ManyToMany
    @JoinTable(name = "SEG_PERUSUARIO", joinColumns = { @JoinColumn(name = "USU_INT_COD") }, inverseJoinColumns = { @JoinColumn(name = "PER_INT_COD") })
    private List<Perfil> perfisUsuarios = new ArrayList<>();
    // @OneToMany(mappedBy = "usuario")
    // private List<SenhaUsada> senhasUsadas;

    // @ManyToOne(fetch=FetchType.EAGER)
    // @JoinColumn(name = "EMP_INT_COD")
    // private Empresa empresa;
    public Usuario() {
        this.usuIntDiastrocasenha = 30;
    }

    public long getUsuIntCod() {
        return this.usuIntCod;
    }

    public void setUsuIntCod(long usuIntCod) {
        this.usuIntCod = usuIntCod;
    }

    public String getUsuChaCpf() {
        return this.usuChaCpf;
    }

    public void setUsuChaCpf(String usuChaCpf) {
        this.usuChaCpf = usuChaCpf;
    }

    public String getUsuChaInlogacesso() {
        return Status.SIM.equalsIgnoreCase(this.usuChaInlogacesso) ? Boolean.TRUE.toString() : Boolean.FALSE.toString();
    }

    public void setUsuChaInlogacesso(String usuChaInlogacesso) {
        if (Status.SIM.equalsIgnoreCase(usuChaInlogacesso) || Status.NAO.equalsIgnoreCase(usuChaInlogacesso)) {
            this.usuChaInlogacesso = usuChaInlogacesso;
        } else {
            this.usuChaInlogacesso = Boolean.TRUE.toString().equalsIgnoreCase(usuChaInlogacesso) ? Status.SIM : Status.NAO;
        }
    }

    public String getUsuChaSts() {
        return Status.INATIVO.equalsIgnoreCase(this.usuChaSts) ? Boolean.TRUE.toString() : Boolean.FALSE.toString();
    }

    public void setUsuChaSts(String usuChaSts) {
        // FIXME REVER ISSO
        if (Status.ATIVO.equalsIgnoreCase(usuChaSts) || Status.INATIVO.equalsIgnoreCase(usuChaSts)) {
            this.usuChaSts = usuChaSts;
        } else {
            this.usuChaSts = Boolean.TRUE.toString().equalsIgnoreCase(usuChaSts) ? Status.INATIVO : Status.ATIVO;
        }
    }

    public boolean isAdmin() {
        return Status.SIM.equalsIgnoreCase(this.usuChaAdm) ? Boolean.TRUE : Boolean.FALSE;
    }

    public String getUsuChaAdm() {
        return Status.SIM.equalsIgnoreCase(this.usuChaAdm) ? Boolean.TRUE.toString() : Boolean.FALSE.toString();
    }

    public void setUsuChaAdm(String usuChaAdm) {
        if (Status.SIM.equalsIgnoreCase(usuChaAdm) || Status.NAO.equalsIgnoreCase(usuChaAdm)) {
            this.usuChaAdm = usuChaAdm;
        } else {
            this.usuChaAdm = Boolean.TRUE.toString().equalsIgnoreCase(usuChaAdm) ? Status.SIM : Status.NAO;
        }
    }

    public Timestamp getUsuDtmUlttrocasenha() {
        return this.usuDtmUlttrocasenha;
    }

    public void setUsuDtmUlttrocasenha(Timestamp usuDtmUlttrocasenha) {
        this.usuDtmUlttrocasenha = usuDtmUlttrocasenha;
    }

    public int getUsuIntDiastrocasenha() {
        return this.usuIntDiastrocasenha;
    }

    public void setUsuIntDiastrocasenha(int usuIntDiastrocasenha) {
        this.usuIntDiastrocasenha = usuIntDiastrocasenha;
    }

    public String getUsuStrEml() {
        return this.usuStrEml;
    }

    public void setUsuStrEml(String usuStrEml) {
        this.usuStrEml = usuStrEml;
    }

    public String getUsuStrLogin() {
        return this.usuStrLogin;
    }

    public void setUsuStrLogin(String usuStrLogin) {
        if (usuStrLogin != null && !usuStrLogin.equals("")) {
            this.usuStrLogin = usuStrLogin.toUpperCase();
        }
    }

    public String getUsuStrNme() {
        return this.usuStrNme;
    }

    public void setUsuStrNme(String usuStrNme) {
        this.usuStrNme = Utils.toFirstUpperCase(usuStrNme);
    }

    public String getUsuStrSenha() {
        return this.usuStrSenha;
    }

    public void setUsuStrSenha(String usuStrSenha) {
        this.usuStrSenha = usuStrSenha;
    }

    public List<LogAcesso> getLogAcessos() {
        return this.logAcessos;
    }

    public void setLogAcessos(List<LogAcesso> logAcessos) {
        this.logAcessos = logAcessos;
    }
    //
    // public List<SenhaUsada> getSenhasUsadas() {
    // return this.senhasUsadas;
    // }
    //
    // public void setSenhasUsadas(List<SenhaUsada> senhasUsadas) {
    // this.senhasUsadas = senhasUsadas;
    // }

    @Transient
    public Empresa getEmpresa() {
        return (Empresa) JSFHelper.getSession().getAttribute("EMPRESA_LOGADA");
    }
    //
    // public void setEmpresa(Empresa empresa) {
    // this.empresa = empresa;
    // }

    @Override
    public String toString() {
        return this.usuStrNme;
    }

    public List<Perfil> getPerfisUsuarios() {
        return this.perfisUsuarios;
    }

    public void setPerfisUsuarios(List<Perfil> perfisUsuarios) {
        this.perfisUsuarios = perfisUsuarios;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (this.usuIntCod ^ (this.usuIntCod >>> 32));
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
        Usuario other = (Usuario) obj;
        if (this.usuIntCod != other.usuIntCod) {
            return false;
        }
        return true;
    }

    public boolean temPerfil(String perfil) {
        if (this.perfisUsuarios != null) {
            for (Perfil perfilAtual : this.perfisUsuarios) {
                if (perfilAtual.getPerStrDes().trim().equals(perfil)) {
                    return true;
                }
            }
        }
        return false;
    }

    public String getUsuChaTutorial() {
        return Status.SIM.equalsIgnoreCase(this.usuChaTutorial) ? Boolean.TRUE.toString() : Boolean.FALSE.toString();
    }

    public void setUsuChaTutorial(String usuChaTutorial) {
        if (Status.SIM.equalsIgnoreCase(usuChaTutorial) || Status.NAO.equalsIgnoreCase(usuChaTutorial)) {
            this.usuChaTutorial = usuChaTutorial;
        } else {
            this.usuChaTutorial = Boolean.TRUE.toString().equalsIgnoreCase(usuChaTutorial) ? Status.SIM : Status.NAO;
        }
    }
}
