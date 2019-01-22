package br.com.lume.security.entity;

import java.io.Serializable;
import java.util.Date;

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

import br.com.lume.common.util.Utils;

/**
 * The persistent class for the SEG_LOGACESSO database table.
 */
@Entity
@Table(name = "SEG_LOGACESSO")
public class LogAcesso implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "LOG_INT_COD")
    private long logIntCod;

    @Column(name = "LOG_DTM_ENTRADA")
    @Temporal(TemporalType.TIMESTAMP)
    private Date logDtmEntrada;

    @Column(name = "LOG_STR_IP")
    private String logStrIp;

    @Column(name = "LOG_STR_ACAO")
    private String logStrAcao;

    @ManyToOne
    @JoinColumn(name = "OBJ_INT_COD")
    private Objeto objeto;

    @ManyToOne
    @JoinColumn(name = "USU_INT_COD")
    private Usuario usuario;

    public LogAcesso() {
    }

    public long getLogIntCod() {
        return this.logIntCod;
    }

    public void setLogIntCod(long logIntCod) {
        this.logIntCod = logIntCod;
    }

    public Date getLogDtmEntrada() {
        return this.logDtmEntrada;
    }

    @Transient
    public String getLogDtmEntradaStr() {
        return Utils.dateToString(this.logDtmEntrada, "dd/MM/yyyy HH:mm:ss");
    }

    public void setLogDtmEntrada(Date logDtmEntrada) {
        this.logDtmEntrada = logDtmEntrada;
    }

    public String getLogStrIp() {
        return this.logStrIp;
    }

    public void setLogStrIp(String logStrIp) {
        this.logStrIp = logStrIp;
    }

    public Objeto getObjeto() {
        return this.objeto;
    }

    public void setObjeto(Objeto objeto) {
        this.objeto = objeto;
    }

    public Usuario getUsuario() {
        return this.usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public String getLogStrAcao() {
        return this.logStrAcao;
    }

    public void setLogStrAcao(String logStrAcao) {
        this.logStrAcao = logStrAcao;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (this.logIntCod ^ (this.logIntCod >>> 32));
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
        LogAcesso other = (LogAcesso) obj;
        if (this.logIntCod != other.logIntCod) {
            return false;
        }
        return true;
    }
}
