package br.com.lume.odonto.entity;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import br.com.lume.odonto.bo.DominioBO;

@Entity
public class LancamentoContabilRelatorio {

    @Id
    private long id;

    private BigDecimal valor;

    private String nome;

    private String descricao;

    @Temporal(TemporalType.DATE)
    private Date data;

    private String tipo;

    @Transient
    private Motivo motivo;

    @Transient
    private Lancamento lancamento;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        if (lancamento != null && lancamento.getFormaPagamento() != null && !lancamento.getFormaPagamento().isEmpty()) {
            try {
                String formaPagamento = lancamento.getFormaPagamento();
                Dominio dominio = new DominioBO().listByTipoAndObjeto(formaPagamento, "pagamento");
                formaPagamento = dominio != null ? dominio.getNome() : "";
                return descricao + "(" + formaPagamento + ")";
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Date getData() {
        return lancamento != null ? lancamento.getDataCredito() : data;
    }

    public void setData(Date data) {
        this.data = data;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public Lancamento getLancamento() {
        return lancamento;
    }

    public void setLancamento(Lancamento lancamento) {
        this.lancamento = lancamento;
    }

    public Motivo getMotivo() {
        return motivo;
    }

    public void setMotivo(Motivo motivo) {
        this.motivo = motivo;
    }

}
