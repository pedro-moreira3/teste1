package br.com.lume.odonto.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
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

import org.eclipse.persistence.annotations.Cache;
import org.eclipse.persistence.annotations.CacheType;

import br.com.lume.common.util.Status;
import br.com.lume.common.util.Utils;
import br.com.lume.odonto.bo.DominioBO;
import br.com.lume.odonto.bo.MaterialBO;

@Entity
@Cache(type = CacheType.NONE)
@Table(name = "MATERIAL")
public class Material implements Serializable, Comparable<Material> {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    @JoinColumn(name = "ID_ITEM")
    private Item item;

    @ManyToOne
    @JoinColumn(name = "ID_MARCA")
    private Marca marca;

    @ManyToOne
    @JoinColumn(name = "ID_LOCAL")
    private Local local;

    @Column(name = "ID_EMPRESA")
    private long idEmpresa;

    @Column(name = "DATA_CADASTRO")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataCadastro;

    @Column(name = "QUANTIDADE")
    private BigDecimal quantidade;

    // ESTE EH O VALOR UNITÁRIO, por ex: Quanto custa cada unidade de algodão dentro do pacote
    @Column(name = "VALOR")
    private BigDecimal valor;

    @Column(name = "VALOR_UNIDADE_INFORMADO")
    private BigDecimal valorUnidadeInformado;

    @Column(name = "LOTE")
    private String lote;

    // ESTE EH A QUANTIDADE DENTRO DO PACOTE, por ex: Quantos algodões tem dentro de uma embalagem
    @Column(name = "TAMANHO_UNIDADE")
    private BigDecimal tamanhoUnidade = new BigDecimal(1);

    // NÃO USAR MAIS ESTE CAMPO!
    @Deprecated
    @Column(name = "QUANTIDADE_UNIDADE")
    private BigDecimal quantidadeUnidade = new BigDecimal(1);

    @Column(name = "PROCEDENCIA")
    private String procedencia;

    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "ID_NOTA_FISCAL")
    private NotaFiscal notaFiscal;

    @Transient
    private String procedenciaString;

    @Transient
    private Integer totalUnidade;

    @Column(name = "STATUS")
    private String status;

    @Column(name = "DEDUTIVEL")
    private String dedutivel = Status.NAO;

    private String excluido = Status.NAO;

    @Column(name = "DATA_EXCLUSAO")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataExclusao;

    @Column(name = "VALIDADE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date validade;

    private String justificativa;

    @Column(name = "QUANTIDADE_TOTAL")
    private BigDecimal quantidadeTotal;

    @Column(name = "QUANTIDADE_ATUAL")
    private BigDecimal quantidadeAtual;

    @Column(name = "CONSIGNACAO")
    private String consignacao = Status.NAO;

    @Column(name = "BRINDE")
    private String brinde = Status.NAO;

    @Column(name = "DATA_ULTIMA_UTILIZACAO")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataUltimaUtilizacao;

    @Column(name = "DATA_MOVIMENTACAO")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataMovimentacao;

    @ManyToOne
    @JoinColumn(name = "ID_FORNECEDOR")
    private Fornecedor fornecedor;

    @Transient
    private BigDecimal quantidadeRetirada;

    @Transient
    private BigDecimal quantidadeEmprestada;

    @Column(name = "EXCLUIDO_POR")
    private Long excluidoPorProfissional;

    public static final String MOVIMENTACAO_ENTRADA = "E";

    public static final String MOVIMENTACAO_SAIDA = "S";

    public Long getExcluidoPorProfissional() {
        return excluidoPorProfissional;
    }

    public void setExcluidoPorProfissional(Long excluidoPorProfissional) {
        this.excluidoPorProfissional = excluidoPorProfissional;
    }

    public String getExcluido() {
        return excluido;
    }

    public void setExcluido(String excluido) {
        this.excluido = excluido;
    }

    public Date getDataExclusao() {
        return dataExclusao;
    }

    public void setDataExclusao(Date dataExclusao) {
        this.dataExclusao = dataExclusao;
    }

    public long getId() {
        return id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public Marca getMarca() {
        return marca;
    }

    public void setMarca(Marca marca) {
        this.marca = marca;
    }

    public Local getLocal() {
        return local;
    }

    public void setLocal(Local local) {
        this.local = local;
    }

    public String getLote() {
        return lote;
    }

    public void setLote(String lote) {
        this.lote = lote;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getIdEmpresa() {
        return idEmpresa;
    }

    public void setIdEmpresa(long idEmpresa) {
        this.idEmpresa = idEmpresa;
    }

    public Date getDataCadastro() {
        return dataCadastro;
    }

    @Transient
    public String getDataCadastroStr() {
        return Utils.dateToString(dataCadastro, "dd/MM/yyyy");
    }

    @Transient
    public String getDataCadastroStrOrd() {
        return Utils.dateToString(dataCadastro, "yyyy/MM/dd");
    }

    public String getDataValidadeStr() {
        return Utils.dateToString(validade, "dd/MM/yyyy");
    }

    public void setDataCadastro(Date dataCadastro) {
        this.dataCadastro = dataCadastro;
    }

    public String getProcedencia() {
        return procedencia;
    }

    public void setProcedencia(String procedencia) {
        this.procedencia = procedencia;
    }

    public String getProcedenciaString() {
        if (procedenciaString == null) {
            try {
                List<Dominio> dominios = new DominioBO().listByEmpresaAndObjetoAndTipo("item", "procedencia");
                for (Dominio d : dominios) {
                    if (d.getValor().equals(this.getProcedencia())) {
                        procedenciaString = d.getNome();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return procedenciaString;
    }

    public void setProcedenciaString(String procedenciaString) {
        this.procedenciaString = procedenciaString;
    }

    public NotaFiscal getNotaFiscal() {
        return notaFiscal;
    }

    public void setNotaFiscal(NotaFiscal notaFiscal) {
        this.notaFiscal = notaFiscal;
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
        Material other = (Material) obj;
        if (id != other.id) {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(Material m) {
        return -1 * (this.getDataCadastro().compareTo(m.getDataCadastro()));// DECRESCENTE
    }

    public Date getValidade() {
        return validade;
    }

    public String getValidadeStr() {
        return Utils.dateToString(validade, "dd/MM/yyyy");
    }

    public void setValidade(Date validade) {
        this.validade = validade;
    }

    public String getJustificativa() {
        return justificativa;
    }

    public void setJustificativa(String justificativa) {
        this.justificativa = justificativa;
    }

    public Integer getTotalUnidade() {
        return totalUnidade;
    }

    public void setTotalUnidade(Integer totalUnidade) {
        this.totalUnidade = totalUnidade;
    }

    public BigDecimal getQuantidadeRetirada() {
        return quantidadeRetirada;
    }

    public void setQuantidadeRetirada(BigDecimal quantidadeRetirada) {
        this.quantidadeRetirada = quantidadeRetirada;
    }

    public String getConsignacao() {
        return consignacao;
    }

    public void setConsignacao(String consignacao) {
        this.consignacao = consignacao;
    }

    public Date getDataUltimaUtilizacao() {
        return dataUltimaUtilizacao;
    }

    public void setDataUltimaUtilizacao(Date dataUltimaUtilizacao) {
        this.dataUltimaUtilizacao = dataUltimaUtilizacao;
    }

    @Transient
    public String getDataUltimaUtilizacaoStr() {
        return Utils.dateToString(dataUltimaUtilizacao, "dd/MM/yyyy");
    }

    @Transient
    public String getDataUltimaUtilizacaoStrOrd() {
        return Utils.dateToString(dataUltimaUtilizacao, "yyyy/MM/dd");
    }

    public String getBrinde() {
        return brinde;
    }

    public void setBrinde(String brinde) {
        this.brinde = brinde;
    }

    public Date getDataMovimentacao() {
        return dataMovimentacao;
    }

    public void setDataMovimentacao(Date dataMovimentacao) {
        this.dataMovimentacao = dataMovimentacao;
    }

    @Transient
    public String getDataMovimentacaoStr() {
        return Utils.dateToString(dataMovimentacao, "dd/MM/yyyy");
    }

    @Transient
    public String getDataMovimentacaoStrOrd() {
        return Utils.dateToString(dataMovimentacao, "yyyy/MM/dd");
    }

    public BigDecimal getQuantidadeUnidade() {
        return quantidadeUnidade;
    }

    public void setQuantidadeUnidade(BigDecimal quantidadeUnidade) {
        this.quantidadeUnidade = quantidadeUnidade;
    }

    public BigDecimal getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(BigDecimal quantidade) {
        this.quantidade = quantidade;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public BigDecimal getTamanhoUnidade() {
        return tamanhoUnidade;
    }

    public void setTamanhoUnidade(BigDecimal tamanhoUnidade) {
        this.tamanhoUnidade = tamanhoUnidade;
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

    public BigDecimal getQuantidadeAtualBD() {
        return new MaterialBO().findQuantidadeAtual(id);
    }

    public void setQuantidadeAtual(BigDecimal quantidadeAtual) {
        this.quantidadeAtual = quantidadeAtual;
    }

    public String getDedutivel() {
        return dedutivel;
    }

    public void setDedutivel(String dedutivel) {
        this.dedutivel = dedutivel;
    }

    public BigDecimal getValorUnidadeInformado() {
        return valorUnidadeInformado;
    }

    public void setValorUnidadeInformado(BigDecimal valorUnidadeInformado) {
        this.valorUnidadeInformado = valorUnidadeInformado;
    }

    public Fornecedor getFornecedor() {
        return fornecedor;
    }

    public void setFornecedor(Fornecedor fornecedor) {
        this.fornecedor = fornecedor;
    }

    public BigDecimal getQuantidadeEmprestada() {
        return quantidadeEmprestada;
    }

    public void setQuantidadeEmprestada(BigDecimal quantidadeEmprestada) {
        this.quantidadeEmprestada = quantidadeEmprestada;
    }

    public BigDecimal getQuantidadeDisponivelEEmprestada() {
        BigDecimal quantidadeAtual_ = quantidadeAtual != null ? quantidadeAtual : new BigDecimal(0);
        BigDecimal quantidadeEmprestada_ = quantidadeEmprestada != null ? quantidadeEmprestada : new BigDecimal(0);
        return quantidadeAtual_.add(quantidadeEmprestada_);
    }
}
