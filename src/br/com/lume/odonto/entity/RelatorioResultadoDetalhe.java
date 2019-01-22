package br.com.lume.odonto.entity;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class RelatorioResultadoDetalhe implements Serializable {

    private static final long serialVersionUID = -8798217110018223965L;

    @Id
    @Column(name = "ID")
    private Long id;

    private String descricao;

    @Column(name = "VALOR_UTILIZADO")
    private BigDecimal valorUtilizado;

    @Column(name = "VALOR")
    private BigDecimal valor;

    @Column(name = "QTD_PEDIDA")
    private Integer qtdPedida;

    @Column(name = "QTD_UTILIZADA")
    private Integer qtdUtilizada;

    @Column(name = "QTD_N_UTILIZADA")
    private Integer qtdNaoUtilizada;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getValorUtilizado() {
        return valorUtilizado;
    }

    public void setValorUtilizado(BigDecimal valorUtilizado) {
        this.valorUtilizado = valorUtilizado;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public Integer getQtdPedida() {
        return qtdPedida;
    }

    public void setQtdPedida(Integer qtdPedida) {
        this.qtdPedida = qtdPedida;
    }

    public Integer getQtdUtilizada() {
        return qtdUtilizada;
    }

    public void setQtdUtilizada(Integer qtdUtilizada) {
        this.qtdUtilizada = qtdUtilizada;
    }

    public Integer getQtdNaoUtilizada() {
        return qtdNaoUtilizada;
    }

    public void setQtdNaoUtilizada(Integer qtdNaoUtilizada) {
        this.qtdNaoUtilizada = qtdNaoUtilizada;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

}
