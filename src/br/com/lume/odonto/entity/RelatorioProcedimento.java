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
public class RelatorioProcedimento implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -7178620139295809933L;

    @Id
    @Column(name = "ID")
    private Long id;

    private String procedimento;

    private String profissional;

    private String paciente;

    private String filial;

    private String especialidade;

    private Long idTitular;

    private Long idPaciente;

    @Column(name = "DATA_FINALIZADO")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataFinalizado;

//    @Column(name = "VALOR_SERVICO")
//    private BigDecimal valorServico;

    private BigDecimal valor;

    @Column(name = "DATA_HORA")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataHora;

    @Transient
    private String titular;

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProcedimento() {
        return this.procedimento;
    }

    public void setProcedimento(String procedimento) {
        this.procedimento = procedimento;
    }

    public String getProfissional() {
        return this.profissional;
    }

    public void setProfissional(String profissional) {
        this.profissional = profissional;
    }

    public String getPaciente() {
        return this.paciente;
    }

    public void setPaciente(String paciente) {
        this.paciente = paciente;
    }

    public String getFilial() {
        return this.filial;
    }

    public void setFilial(String filial) {
        this.filial = filial;
    }

    public String getEspecialidade() {
        return this.especialidade;
    }

    public void setEspecialidade(String especialidade) {
        this.especialidade = especialidade;
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
        RelatorioProcedimento other = (RelatorioProcedimento) obj;
        if (this.id != other.id) {
            return false;
        }
        return true;
    }

    public String getTitular() {
        return this.titular;
    }

    public void setTitular(String titular) {
        this.titular = titular;
    }

    public Long getIdTitular() {
        return this.idTitular;
    }

    public void setIdTitular(Long idTitular) {
        this.idTitular = idTitular;
    }

    public Long getIdPaciente() {
        return this.idPaciente;
    }

    public void setIdPaciente(Long idPaciente) {
        this.idPaciente = idPaciente;
    }

    public Date getDataFinalizado() {
        return this.dataFinalizado;
    }

    public String getDataFinalizadoStrOrd() {
        return Utils.dateToString(this.dataFinalizado, "yyyy/MM/dd HH:mm");
    }

    public String getDataFinalizadoStr() {
        return Utils.dateToString(this.dataFinalizado);
    }

    public void setDataFinalizado(Date dataFinalizado) {
        this.dataFinalizado = dataFinalizado;
    }

//    public BigDecimal getValorServico() {
//        return valorServico;
//    }
//
//    public void setValorServico(BigDecimal valorServico) {
//        this.valorServico = valorServico;
//    }

    public Date getDataHora() {
        return this.dataHora;
    }

    public String getDataHoraStrOrd() {
        return Utils.dateToString(this.dataHora, "yyyy/MM/dd HH:mm");
    }

    public String getDataHoraStr() {
        return Utils.dateToString(this.dataHora);
    }

    public void setDataHora(Date dataHora) {
        this.dataHora = dataHora;
    }

    public BigDecimal getValor() {
        return this.valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }
}
