package br.com.lume.odonto.entity;

import java.io.Serializable;
import java.text.Normalizer;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import br.com.lume.common.util.Status;
import br.com.lume.odonto.bo.DominioBO;

@Entity
@Table(name = "ITEM")
public class Item implements Serializable, Comparable<Item> {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    @JoinColumn(name = "ID_ITEM_PAI")
    private Item idItemPai;

    @OneToMany
    @JoinColumn(name = "ID_ITEM")
    private List<PedidoItem> pedidoItens;

    @Column(name = "DESCRICAO")
    private String descricao;

    @Column(name = "TIPO")
    private String tipo;

    @Column(name = "ESTOQUE_MINIMO")
    private Long estoqueMinimo;

    @Column(name = "ESTOQUE_MAXIMO")
    private Long estoqueMaximo;

    @Column(name = "ESTERILIZAVEL")
    private Character esterilizavel;

    @Column(name = "CONTROLADO")
    private Character controlado;

    @Column(name = "ESTERIL")
    private Character esteril;

    @Column(name = "CATEGORIA")
    private String categoria;

    @Column(name = "UTILIZACAO")
    private String utilizacao;

    @Transient
    private String utilizacaoString;

    @Column(name = "FORMA_ARMAZENAMENTO")
    private String formaArmazenamento;

    @Transient
    private String formaArmazenamentoString;

    @Column(name = "ID_EMPRESA")
    private Long idEmpresa;

    @Column(name = "FRACAO_UNITARIA")
    private String fracaoUnitaria;

    @Transient
    private String fracaoUnitariaString;

    @Column(name = "UNIDADE_MEDIDA")
    private String unidadeMedida;

    @Transient
    private String unidadeMedidaString;

    @Transient
    private String fracaoUnitariaStr;

    private String excluido;

    @Column(name = "DATA_EXCLUSAO")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataExclusao;

    @Column(name = "EXCLUIDO_POR")
    private Long excluidoPorProfissional;

    @Column(name = "DISTRIBUICAO_AUTOMATICA")
    private String distribuicaoAutomatica;

    @OneToMany(mappedBy = "idItemPai")
    private List<Item> itensFilho;

    @Column(name = "APLICACAO")
    private String aplicacao = APLICACAO_INDIRETA;

    public static final String APLICACAO_DIRETA = "D";

    public static final String APLICACAO_INDIRETA = "I";

    public Long getExcluidoPorProfissional() {
        return this.excluidoPorProfissional;
    }

    public void setExcluidoPorProfissional(Long excluidoPorProfissional) {
        this.excluidoPorProfissional = excluidoPorProfissional;
    }

    public String getExcluido() {
        return this.excluido;
    }

    public void setExcluido(String excluido) {
        this.excluido = excluido;
    }

    public Date getDataExclusao() {
        return this.dataExclusao;
    }

    public void setDataExclusao(Date dataExclusao) {
        this.dataExclusao = dataExclusao;
    }

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Item getIdItemPai() {
        return this.idItemPai;
    }

    public void setIdItemPai(Item idItemPai) {
        this.idItemPai = idItemPai;
    }

    public String getDescricao() {
        return this.descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Long getEstoqueMinimo() {
        return this.estoqueMinimo;
    }

    public void setEstoqueMinimo(Long estoqueMinimo) {
        this.estoqueMinimo = estoqueMinimo;
    }

    public Long getEstoqueMaximo() {
        return this.estoqueMaximo;
    }

    public void setEstoqueMaximo(Long estoqueMaximo) {
        this.estoqueMaximo = estoqueMaximo;
    }

    public Character getEsterilizavel() {
        return this.esterilizavel;
    }

    public void setEsterilizavel(Character esterilizavel) {
        this.esterilizavel = esterilizavel;
    }

    public Character getControlado() {
        return this.controlado;
    }

    public void setControlado(Character controlado) {
        this.controlado = controlado;
    }

    public String getCategoria() {
        return this.categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public Character getEsteril() {
        return this.esteril;
    }

    public void setEsteril(Character esteril) {
        this.esteril = esteril;
    }

    public String getFormaArmazenamento() {
        return this.formaArmazenamento;
    }

    public void setFormaArmazenamento(String formaArmazenamento) {
        this.formaArmazenamento = formaArmazenamento;
    }

    public String getUtilizacao() {
        return this.utilizacao;
    }

    public void setUtilizacao(String utilizacao) {
        this.utilizacao = utilizacao;
    }

    public String getFormaArmazenamentoString() {
        if (this.formaArmazenamento != null) {
            if (this.formaArmazenamento.equals("CO")) {
                this.formaArmazenamentoString = "Armazenamento Comum";
            } else if (this.formaArmazenamento.equals("FR")) {
                this.formaArmazenamentoString = "Freezer";
            } else if (this.formaArmazenamento.equals("GE")) {
                this.formaArmazenamentoString = "Geladeira";
            } else if (this.formaArmazenamento.equals("LP")) {
                this.formaArmazenamentoString = "Local Protegido";
            } else if (this.formaArmazenamento.equals("IN")) {
                this.formaArmazenamentoString = "Sala para inflamáveis";
            }
        }
        return this.formaArmazenamentoString;
    }

    public void setFormaArmazenamentoString(String formaArmazenamentoString) {
        this.formaArmazenamentoString = formaArmazenamentoString;
    }

    public String getUtilizacaoString() {
        return this.utilizacaoString;
    }

    public void setUtilizacaoString(String utilizacaoString) {
        this.utilizacaoString = utilizacaoString;
    }

    public Long getIdEmpresa() {
        return this.idEmpresa;
    }

    public void setIdEmpresa(Long idEmpresa) {
        this.idEmpresa = idEmpresa;
    }

    public List<PedidoItem> getPedidoItens() {
        return this.pedidoItens;
    }

    public void setPedidoItens(List<PedidoItem> pedidoItens) {
        this.pedidoItens = pedidoItens;
    }

    public String getFracaoUnitaria() {
        return this.fracaoUnitaria;
    }

    public void setFracaoUnitaria(String fracaoUnitaria) {
        this.fracaoUnitaria = fracaoUnitaria;
    }

    public String getFracaoUnitariaString() {
        return this.fracaoUnitariaString;
    }

    public void setFracaoUnitariaString(String fracaoUnitariaString) {
        this.fracaoUnitariaString = fracaoUnitariaString;
    }

    public String getUnidadeMedida() {
        return this.unidadeMedida;
    }

    public void setUnidadeMedida(String unidadeMedida) {
        this.unidadeMedida = unidadeMedida;
    }

    public String getUnidadeMedidaString() {
        try {
            Dominio dominio = new DominioBO().findByEmpresaAndObjetoAndTipoAndValor("item", "unidade_medida", this.unidadeMedida);
            if (dominio != null) {
                this.unidadeMedidaString = " " + dominio.getNome();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this.unidadeMedidaString;
    }

    public String getFracaoUnitariaStr() {
        try {
            Dominio fracao = new DominioBO().findByEmpresaAndObjetoAndTipoAndValor("item", "fracao_unitaria", this.fracaoUnitaria);
            if (fracao != null) {
                this.fracaoUnitariaStr = fracao.getNome() + " de  ";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this.fracaoUnitariaStr != null ? " - " + this.fracaoUnitariaStr : " - ";
    }

    public void setUnidadeMedidaString(String unidadeMedidaString) {
        this.unidadeMedidaString = unidadeMedidaString;
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
        Item other = (Item) obj;
        if (this.id != other.id) {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(Item l) {
        if (this.descricao == null) {
            return 1;
        } else {
            return this.getDescricao().compareTo(l.getDescricao());
        }
    }

    public String getDescricaoLimpa() {
        return Normalizer.normalize(this.getDescricao(), Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "");
    }

    public String getTipo() {
        return this.tipo;
    }

    public String getTipoStr() {
        return this.tipo == null ? "" : this.tipo.equals("I") ? "Instrumental" : "Consumo";
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    /*
     * public String getDistribuicaoAutomatica() { return distribuicaoAutomatica; }
     */
    public void setDistribuicaoAutomatica(String distribuicaoAutomatica) {
        this.distribuicaoAutomatica = distribuicaoAutomatica;
    }

    public String getDistribuicaoAutomatica() {
        return this.distribuicaoAutomatica != null && this.distribuicaoAutomatica.equals(Status.SIM) ? Status.SIM : Status.NAO;
    }

    public String getDistribuicaoAutomaticaStr() {
        return this.distribuicaoAutomatica == null ? "" : this.distribuicaoAutomatica.equals("N") ? "Não" : "Sim";
    }

    /*
     * public boolean isDistribuicaoAutomatica() { return (distribuicaoAutomatica != null && Status.SIM.equals(distribuicaoAutomatica)) ? true : false; } public void setDistribuicaoAutomatica(boolean
     * bdistribuicaoAutomatica) { this.distribuicaoAutomatica = bdistribuicaoAutomatica ? Status.SIM : Status.NAO; }
     */
    public String getAplicacao() {
        return this.aplicacao;
    }

    public String getAplicacaoStr() {
        return this.aplicacao.equals("I") ? "Indireta" : "Direta";
    }

    public void setAplicacao(String aplicacao) {
        this.aplicacao = aplicacao;
    }

    public List<Item> getItensFilho() {
        return this.itensFilho;
    }

    public void setItensFilho(List<Item> itensFilho) {
        this.itensFilho = itensFilho;
    }

}
