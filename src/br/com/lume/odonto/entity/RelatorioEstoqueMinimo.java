package br.com.lume.odonto.entity;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;

import br.com.lume.odonto.bo.DominioBO;

@Entity
public class RelatorioEstoqueMinimo implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 8613568423187964008L;

    @Id
    @Column(name = "ID")
    private long id;

    @Column(name = "ITEM")
    private String item;

    @Column(name = "TIPO")
    private String tipo;

    @Column(name = "ESTOQUE_MINIMO")
    private Long estoqueMinimo;

    @Column(name = "SOMA_QUANTIDADE_ATUAL")
    private Long somaQuantidadeAtual;

    @Column(name = "VALOR_MEDIO")
    private Long valorMedio;

    @Column(name = "SOMA_VALOR_TOTAL")
    private Long somaValorTotal;

    @Column(name = "UNIDADE_MEDIDA")
    private String unidadeMedida;

    @Transient
    private BigDecimal valorMedioUlt6Meses, valorUltimaCompra;

    @Transient
    private String unidadeMedidaStr;

    @Column(name = "LOCAL_ESTOQUE")
    private String localEstoque;

    public String getUnidadeMedidaStr() {
        try {
            Dominio dominio = new DominioBO().findByEmpresaAndObjetoAndTipoAndValor("item", "unidade_medida", unidadeMedida);
            if (dominio != null) {
                unidadeMedidaStr = " " + dominio.getNome();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return unidadeMedidaStr;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (id ^ (id >>> 32));
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
        RelatorioEstoqueMinimo other = (RelatorioEstoqueMinimo) obj;
        if (id != other.id) {
            return false;
        }
        return true;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getItem() {
        return item;
    }

    public void setItem(String item) {
        this.item = item;
    }

    public Long getValorMedio() {
        return valorMedio;
    }

    public void setValorMedio(Long valorMedio) {
        this.valorMedio = valorMedio;
    }

    public Long getSomaQuantidadeAtual() {
        return somaQuantidadeAtual;
    }

    public void setSomaQuantidadeAtual(Long somaQuantidadeAtual) {
        this.somaQuantidadeAtual = somaQuantidadeAtual;
    }

    public Long getSomaValorTotal() {
        return somaValorTotal;
    }

    public void setSomaValorTotal(Long somaValorTotal) {
        this.somaValorTotal = somaValorTotal;
    }

    public String getUnidadeMedida() {
        return unidadeMedida;
    }

    public void setUnidadeMedida(String unidadeMedida) {
        this.unidadeMedida = unidadeMedida;
    }

    public Long getEstoqueMinimo() {
        return estoqueMinimo;
    }

    public void setEstoqueMinimo(Long estoqueMinimo) {
        this.estoqueMinimo = estoqueMinimo;
    }

    public String getLocalEstoque() {
        return localEstoque;
    }

    public void setLocalEstoque(String localEstoque) {
        this.localEstoque = localEstoque;
    }

    public BigDecimal getValorMedioUlt6Meses() {
        return valorMedioUlt6Meses;
    }

    public void setValorMedioUlt6Meses(BigDecimal valorMedioUlt6Meses) {
        this.valorMedioUlt6Meses = valorMedioUlt6Meses;
    }

    public BigDecimal getValorUltimaCompra() {
        return valorUltimaCompra;
    }

    public void setValorUltimaCompra(BigDecimal valorUltimaCompra) {
        this.valorUltimaCompra = valorUltimaCompra;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getTipoStr() {
        return tipo == null ? "" : tipo.equals("I") ? "Instrumental" : "Consumo";
    }
}
