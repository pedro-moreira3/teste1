package br.com.lume.odonto.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import br.com.lume.common.util.Utils;

@Entity
public class RelatorioResultado implements Serializable {

    private static final long serialVersionUID = -8798217110018223965L;

    @Id
    @Column(name = "ID")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "ID_DENTISTA")
    private Profissional profissional;

    @ManyToOne
    @JoinColumn(name = "ID_PACIENTE")
    private Paciente paciente;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "DATA")
    private Date dataAgendamento;

    @ManyToOne
    @JoinColumn(name = "ID_PROCEDIMENTO")
    private Procedimento procedimento;

    @ManyToOne
    @JoinColumn(name = "ID_PLANO_TRATAMENTO")
    private PlanoTratamento planoTratamento;

    @Column(name = "VALOR_MATERIAL")
    private BigDecimal valorMaterial;

    @Column(name = "VALOR")
    private BigDecimal valor;

    @Column(name = "VALOR_REPASSADO")
    private BigDecimal valorRepassado;

    @Column(name = "RESULTADO")
    private BigDecimal resultado;

    public RelatorioResultado() {
    }

    public RelatorioResultado(Profissional profissional, Paciente paciente, Date dataAgendamento, Procedimento procedimento, BigDecimal valor, BigDecimal valorRepassado,
            PlanoTratamento planoTratamento) {
        super();
        this.profissional = profissional;
        this.paciente = paciente;
        this.dataAgendamento = dataAgendamento;
        this.procedimento = procedimento;
        this.valor = valor;
        this.valorRepassado = valorRepassado;
        this.planoTratamento = planoTratamento;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Profissional getProfissional() {
        return profissional;
    }

    public void setProfissional(Profissional profissional) {
        this.profissional = profissional;
    }

    public Paciente getPaciente() {
        return paciente;
    }

    public void setPaciente(Paciente paciente) {
        this.paciente = paciente;
    }

    public Date getDataAgendamento() {
        return dataAgendamento;
    }

    public void setDataAgendamento(Date dataAgendamento) {
        this.dataAgendamento = dataAgendamento;
    }

    public Procedimento getProcedimento() {
        return procedimento;
    }

    public void setProcedimento(Procedimento procedimento) {
        this.procedimento = procedimento;
    }

    public BigDecimal getValorMaterial() {
        return valorMaterial;
    }

    public void setValorMaterial(BigDecimal valorMaterial) {
        this.valorMaterial = valorMaterial;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public BigDecimal getValorRepassado() {
        return valorRepassado;
    }

    public void setValorRepassado(BigDecimal valorRepassado) {
        this.valorRepassado = valorRepassado;
    }

    public BigDecimal getResultado() {
        //(Valor procedimento - repassado ) - material
        BigDecimal valor_ = getValor() != null ? getValor() : new BigDecimal(0);
        BigDecimal valorRepassado_ = getValorRepassado() != null ? getValorRepassado() : new BigDecimal(0);
        BigDecimal valorMaterial_ = getValorMaterial() != null ? getValorMaterial() : new BigDecimal(0);

        return valor_.subtract(valorRepassado_).subtract(valorMaterial_);
    }

    public void setResultado(BigDecimal resultado) {
        this.resultado = resultado;
    }

    @Transient
    public String getDataStr() {
        return Utils.dateToString(dataAgendamento);
    }

    public PlanoTratamento getPlanoTratamento() {
        return planoTratamento;
    }

    public void setPlanoTratamento(PlanoTratamento planoTratamento) {
        this.planoTratamento = planoTratamento;
    }

}
