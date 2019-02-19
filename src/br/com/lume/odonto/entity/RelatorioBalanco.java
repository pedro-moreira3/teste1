package br.com.lume.odonto.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import br.com.lume.common.util.Utils;

@Entity
public class RelatorioBalanco implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 4040381317740257822L;

    @Id
    @Column(name = "ID")
    private long id;

    @Column(name = "DESCRICAO_PLANO")
    private String plano;

    @Column(name = "NOME_PACIENTE")
    private String paciente;

    @Column(name = "NOME_PROFISSIONAL")
    private String profissional;

    @Column(name = "VALOR_TOTAL_DESCONTO")
    private BigDecimal valorPlano;

    @Column(name = "VALOR_PAGO")
    private BigDecimal valorRecebido;

    @Column(name = "VALOR_CUSTO")
    private BigDecimal valorCusto;

    @Column(name = "VALOR_REPASSE")
    private BigDecimal valorRepasse;

    @Column(name = "VALOR_REPASSADO")
    private BigDecimal valorRepassado;

    @Column(name = "VALOR_PAGO_TRIBUTADO")
    private BigDecimal valorPagoTributado;

    @Column(name = "RESULTADO")
    private BigDecimal resultado;

    @Column(name = "DATA_FINALIZADO")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataFinalizacaoPlano;

    @Column(name = "DATA_HORA")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataHora;

    @Transient
    private boolean repasseFinalizado;

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getPlano() {
        return this.plano;
    }

    public void setPlano(String plano) {
        this.plano = plano;
    }

    public String getPaciente() {
        return this.paciente;
    }

    public void setPaciente(String paciente) {
        this.paciente = paciente;
    }

    public String getProfissional() {
        return this.profissional;
    }

    public void setProfissional(String profissional) {
        this.profissional = profissional;
    }

    public BigDecimal getValorPlano() {
        return this.valorPlano;
    }

    public void setValorPlano(BigDecimal valorPlano) {
        this.valorPlano = valorPlano;
    }

    public BigDecimal getValorRecebido() {
        return this.valorRecebido;
    }

    public void setValorRecebido(BigDecimal valorRecebido) {
        this.valorRecebido = valorRecebido;
    }

    public BigDecimal getValorRepassado() {
        return this.valorRepassado;
    }

    public void setValorRepassado(BigDecimal valorRepassado) {
        this.valorRepassado = valorRepassado;
    }

    public BigDecimal getResultado() {
        return this.resultado;
    }

    public void setResultado(BigDecimal resultado) {
        this.resultado = resultado;
    }

    public Date getDataFinalizacaoPlano() {
        return this.dataFinalizacaoPlano;
    }

    public void setDataFinalizacaoPlano(Date dataFinalizacaoPlano) {
        this.dataFinalizacaoPlano = dataFinalizacaoPlano;
    }

    public boolean isRepasseFinalizado() {
        if (this.getValorPlano() != null && this.getValorRecebido() != null && this.getValorPlano().doubleValue() == this.getValorRecebido().doubleValue()) {
            return true;
        } else {
            return false;
        }
    }

    public void setRepasseFinalizado(boolean repasseFinalizado) {
        this.repasseFinalizado = repasseFinalizado;
    }

    public BigDecimal getValorRepasse() {
        return this.valorRepasse;
    }

    public void setValorRepasse(BigDecimal valorRepasse) {
        this.valorRepasse = valorRepasse;
    }

    public BigDecimal getValorPagoTributado() {
        return this.valorPagoTributado;
    }

    public void setValorPagoTributado(BigDecimal valorPagoTributado) {
        this.valorPagoTributado = valorPagoTributado;
    }

    public BigDecimal getValorCusto() {
        return this.valorCusto;
    }

    public void setValorCusto(BigDecimal valorCusto) {
        this.valorCusto = valorCusto;
    }

    public Date getDataHora() {
        return this.dataHora;
    }

    public void setDataHora(Date dataHora) {
        this.dataHora = dataHora;
    }

    @Transient
    public String getDataHoraStr() {
        return Utils.dateToString(this.dataHora, "dd/MM/yyyy");
    }
}
