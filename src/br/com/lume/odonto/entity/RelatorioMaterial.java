package br.com.lume.odonto.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import br.com.lume.common.util.Utils;
import br.com.lume.odonto.bo.DominioBO;

@Entity
public class RelatorioMaterial implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 6867054901180685935L;

    @Id
    @Column(name = "ID")
    private long id;

    @Column(name = "ITEM")
    private String item;

    @Column(name = "MARCA")
    private String marca;

    @Column(name = "QUANTIDADE_TOTAL")
    private BigDecimal quantidadeTotal;

    @Column(name = "QUANTIDADE_ATUAL")
    private BigDecimal quantidadeAtual;

    @Column(name = "VALOR_UNITARIO")
    private BigDecimal valor;

    @Column(name = "VALOR_TOTAL")
    private BigDecimal valorTotal;

    @Column(name = "VALOR_TOTAL_ATUAL")
    private BigDecimal valorTotalAtual;

    @Column(name = "VALIDADE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date validade;

    @Column(name = "DATA_ULTIMA_UTILIZACAO")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataUltimaUtilizacao;

    @Column(name = "UNIDADE_MEDIDA")
    private String unidadeMedida;

    @Column(name = "LOTE")
    private String lote;

    @Column(name = "LOCAL")
    private String local;

    private String tipo;

    @Transient
    private String unidadeMedidaStr;

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

    public BigDecimal getQuantidadeTotal() {
        return quantidadeTotal;
    }

    public void setQuantidadeTotal(BigDecimal quantidadeTotal) {
        this.quantidadeTotal = quantidadeTotal;
    }

    public BigDecimal getQuantidadeAtual() {
        return quantidadeAtual;
    }

    public void setQuantidadeAtual(BigDecimal quantidadeAtual) {
        this.quantidadeAtual = quantidadeAtual;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(BigDecimal valorTotal) {
        this.valorTotal = valorTotal;
    }

    public BigDecimal getValorTotalAtual() {
        return valorTotalAtual;
    }

    public void setValorTotalAtual(BigDecimal valorTotalAtual) {
        this.valorTotalAtual = valorTotalAtual;
    }

    public Date getValidade() {
        return validade;
    }

    public void setValidade(Date validade) {
        this.validade = validade;
    }

    public Date getDataUltimaUtilizacao() {
        return dataUltimaUtilizacao;
    }

    public Date getDataUltimaUtilizacaoSort() {
        if (dataUltimaUtilizacao == null) {
            Calendar c = Calendar.getInstance();
            c.set(Calendar.YEAR, 1970);
            return c.getTime();
        }
        return dataUltimaUtilizacao;
    }

    public void setDataUltimaUtilizacao(Date dataUltimaUtilizacao) {
        this.dataUltimaUtilizacao = dataUltimaUtilizacao;
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
        RelatorioMaterial other = (RelatorioMaterial) obj;
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

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    @Transient
    public String getDataUltimaUtilizacaoStr() {
        return Utils.dateToString(dataUltimaUtilizacao, "dd/MM/yyyy");
    }

    @Transient
    public String getDataUltimaUtilizacaoStrOrd() {
        return Utils.dateToString(dataUltimaUtilizacao, "yyyy/MM/dd");
    }

    @Transient
    public String getValidadeStr() {
        return Utils.dateToString(validade, "dd/MM/yyyy");
    }

    @Transient
    public String getValidadeStrOrd() {
        return Utils.dateToString(validade, "yyyy/MM/dd");
    }

    public String getUnidadeMedida() {
        return unidadeMedida;
    }

    public void setUnidadeMedida(String unidadeMedida) {
        this.unidadeMedida = unidadeMedida;
    }

    public String getLote() {
        return lote;
    }

    public void setLote(String lote) {
        this.lote = lote;
    }

    public String getLocal() {
        return local;
    }

    public void setLocal(String local) {
        this.local = local;
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
