package br.com.lume.odonto.entity;

import java.io.Serializable;
import java.text.Normalizer;
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
import br.com.lume.odonto.bo.DominioBO;

@Entity
public class RelatorioEntradaSaidaMaterial implements Serializable, Comparable<RelatorioEntradaSaidaMaterial> {

    /**
     *
     */
    private static final long serialVersionUID = 5467687427965173055L;

    @Id
    @Column(name = "ID")
    private String id;

    @Column(name = "ITEM")
    private String item;

    @Column(name = "UNIDADE_MEDIDA")
    private String unidadeMedida;

    @Column(name = "ESTOQUE_MINIMO")
    private Long estoqueMinimo;

    @Column(name = "ID_EMPRESA")
    private long idEmpresa;

    @ManyToOne
    @JoinColumn(name = "LOCAL_MATERIAL")
    private Local localMaterial;

    @Column(name = "QUANTIDADE_TOTAL")
    private Long quantidadeTotal;

    @Column(name = "VALOR_MEDIO")
    private Long valorMedio;

    @Column(name = "VALOR_TOTAL")
    private Long valorTotal;

    @Column(name = "STATUS")
    private String status;

    @Column(name = "ENTREGA")
    @Temporal(TemporalType.TIMESTAMP)
    private Date entrega;

    @Column(name = "ENTRADA")
    @Temporal(TemporalType.TIMESTAMP)
    private Date entrada;

    @ManyToOne
    @JoinColumn(name = "PROCEDIMENTO")
    private Procedimento procedimento;

    @Column(name = "TAMANHO_UNIDADE")
    private Long tamanhoUnidade;

    @Column(name = "QUANTIDADE")
    private Long quantidade;

    @Column(name = "VALIDADE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date validade;

    @ManyToOne
    @JoinColumn(name = "ID_PROFISSIONAL")
    private Profissional profissional;

    @ManyToOne
    @JoinColumn(name = "LOCAL_RESERVA")
    private Local localReserva;

    @Column(name = "ORDENACAO")
    @Temporal(TemporalType.TIMESTAMP)
    private Date ordenacao;

    @Transient
    private String unidadeMedidaStr;

    public String getUnidadeMedidaStr() {
        try {
            Dominio dominio = new DominioBO().findByEmpresaAndObjetoAndTipoAndValor("item", "unidade_medida", this.unidadeMedida);
            if (dominio != null) {
                this.unidadeMedidaStr = " " + dominio.getNome();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this.unidadeMedidaStr;
    }

    public String getItem() {
        return this.item;
    }

    public void setItem(String item) {
        this.item = item;
    }

    public String getUnidadeMedida() {
        return this.unidadeMedida;
    }

    public void setUnidadeMedida(String unidadeMedida) {
        this.unidadeMedida = unidadeMedida;
    }

    public Long getEstoqueMinimo() {
        return this.estoqueMinimo;
    }

    public void setEstoqueMinimo(Long estoqueMinimo) {
        this.estoqueMinimo = estoqueMinimo;
    }

    public long getIdEmpresa() {
        return this.idEmpresa;
    }

    public void setIdEmpresa(long idEmpresa) {
        this.idEmpresa = idEmpresa;
    }

    public Local getLocalMaterial() {
        return this.localMaterial;
    }

    public void setLocalMaterial(Local localMaterial) {
        this.localMaterial = localMaterial;
    }

    public Long getQuantidadeTotal() {
        return this.quantidadeTotal;
    }

    public void setQuantidadeTotal(Long quantidadeTotal) {
        this.quantidadeTotal = quantidadeTotal;
    }

    public Long getValorMedio() {
        return this.valorMedio;
    }

    public void setValorMedio(Long valorMedio) {
        this.valorMedio = valorMedio;
    }

    public Long getValorTotal() {
        return this.valorTotal;
    }

    public void setValorTotal(Long valorTotal) {
        this.valorTotal = valorTotal;
    }

    public String getStatus() {
        return this.status;
    }

    public String getStatusStr() {
        return this.status.equals("A") ? "Ativo" : this.status.equals("D") ? "Devolvido" : " ";
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getEntrega() {
        return this.entrega;
    }

    public void setEntrega(Date entrega) {
        this.entrega = entrega;
    }

    public Date getEntrada() {
        return this.entrada;
    }

    public void setEntrada(Date entrada) {
        this.entrada = entrada;
    }

    public Procedimento getProcedimento() {
        return this.procedimento;
    }

    public void setProcedimento(Procedimento procedimento) {
        this.procedimento = procedimento;
    }

    public Long getTamanhoUnidade() {
        return this.tamanhoUnidade;
    }

    public void setTamanhoUnidade(Long tamanhoUnidade) {
        this.tamanhoUnidade = tamanhoUnidade;
    }

    public Long getQuantidade() {
        return this.quantidade;
    }

    public void setQuantidade(Long quantidade) {
        this.quantidade = quantidade;
    }

    public Date getValidade() {
        return this.validade;
    }

    public void setValidade(Date validade) {
        this.validade = validade;
    }

    public Profissional getProfissional() {
        return this.profissional;
    }

    public void setProfissional(Profissional profissional) {
        this.profissional = profissional;
    }

    public Local getLocalReserva() {
        return this.localReserva;
    }

    public void setLocalReserva(Local localReserva) {
        this.localReserva = localReserva;
    }

    public String getEntradaStr() {
        return Utils.dateToString(this.entrada);
    }

    public String getEntregaStr() {
        return Utils.dateToString(this.entrega);
    }

    public String getEntradaStrOrd() {
        return Utils.dateToString(this.entrada, "yyyy/MM/dd HH:mm");
    }

    public String getEntregaStrOrd() {
        return Utils.dateToString(this.entrega, "yyyy/MM/dd HH:mm");
    }

    public String getValidadeStrOrd() {
        return Utils.dateToString(this.validade, "yyyy/MM/dd HH:mm");
    }

    public String getValidadeStr() {
        return Utils.dateToString(this.validade);
    }

    public Date getOrdenacao() {
        return this.ordenacao;
    }

    public void setOrdenacao(Date ordenacao) {
        this.ordenacao = ordenacao;
    }

    public String getOrdenacaoStrOrd() {
        return Utils.dateToString(this.ordenacao, "yyyy/MM/dd HH:mm");
    }

    public String getOrdenacaoStr() {
        return Utils.dateToString(this.ordenacao);
    }

    @Override
    public int compareTo(RelatorioEntradaSaidaMaterial o) {
        if (this.item == null || o == null || o.getItem() == null) {
            return 1;
        } else {
            return Normalizer.normalize(this.getItem(), Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "").compareToIgnoreCase(Normalizer.normalize(o.getItem(), Normalizer.Form.NFD));
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.id == null) ? 0 : this.id.hashCode());
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
        RelatorioEntradaSaidaMaterial other = (RelatorioEntradaSaidaMaterial) obj;
        if (this.id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!this.id.equals(other.id)) {
            return false;
        }
        return true;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getItemLimpo() {
        return Normalizer.normalize(this.getItem(), Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "");
    }
}
