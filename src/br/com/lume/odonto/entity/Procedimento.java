package br.com.lume.odonto.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import br.com.lume.common.util.Status;

@Entity
@Table(name = "PROCEDIMENTO")
public class Procedimento implements Serializable, Comparable<Procedimento> {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "DESCRICAO")
    private String descricao;

    @Column(name = "VALOR")
    private BigDecimal valor;

    @Column(name = "VALOR_REPASSE")
    private BigDecimal valorRepasse;

    @Column(name = "CODIGO_CFO")
    private Integer codigoCfo;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ID_ESPECIALIDADE")
    private Especialidade especialidade;

    private String excluido = Status.NAO;

    @Column(name = "DATA_EXCLUSAO")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataExclusao;

    // @Column(name = "VALOR_SERVICO")
    // private BigDecimal valorServico;
    @Column(name = "IDADE_MINIMA")
    private Integer idadeMinima;

    @Column(name = "IDADE_MAXIMA")
    private Integer idadeMaxima;

    @Column(name = "QUANTIDADE_FACES")
    private Integer quantidadeFaces;

    @Column(name = "DINAMICO")
    private String dinamico = Status.NAO;

    @Column(name = "CICLICO")
    private String ciclico = Status.NAO;

    @Column(name = "TIPO")
    private String tipo = "N";

    @Column(name = "ID_EMPRESA")
    private Long idEmpresa;

    @Transient
    private String descricaoId;

    @Transient
    private String codigoConvenio;

    @Transient
    private boolean dinamicoBoo;

    @Transient
    private long total;

    // public BigDecimal getValorServico() {
    // return valorServico;
    // }
    //
    // public void setValorServico(BigDecimal valorServico) {
    // this.valorServico = valorServico;
    // }
    @Column(name = "EXCLUIDO_POR")
    private Long excluidoPorProfissional;

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

    public Procedimento() {
    }

    public Procedimento(Long idEmpresa, String descricao, double valor, Integer codigoCfo, Especialidade especialidade) {
        super();
        this.descricao = descricao;
        this.valor = new BigDecimal(valor);
        this.codigoCfo = codigoCfo;
        this.especialidade = especialidade;
        this.idEmpresa = idEmpresa;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getDescricaoId() {
        return descricao + " (Código: " + codigoCfo + ")";
    }

    public String getDescricaoIdLimpo() {
        return Normalizer.normalize(descricao, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "") + " (Código: " + codigoCfo + ")";
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Especialidade getEspecialidade() {
        return especialidade;
    }

    public void setEspecialidade(Especialidade especialidade) {
        this.especialidade = especialidade;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public Integer getCodigoCfo() {
        return codigoCfo;
    }

    public void setCodigoCfo(Integer codigoCfo) {
        this.codigoCfo = codigoCfo;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
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
        Procedimento other = (Procedimento) obj;
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(Procedimento o) {
        if (descricao == null || o == null || o.getDescricao() == null) {
            return 1;
        } else {
            return Normalizer.normalize(this.getDescricao(), Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "").compareToIgnoreCase(Normalizer.normalize(o.getDescricao(), Normalizer.Form.NFD));
        }
    }

    public String getDescricaoLimpa() {
        return Normalizer.normalize(this.getDescricao(), Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "");
    }

    public boolean isDinamicoBoo() {
        return dinamico.equals(Status.SIM);
    }

    public boolean isCiclicoBoo() {
        ciclico = "N";
        return ciclico.equals(Status.SIM);
    }

    public void setDinamicoBoo(boolean dinamicoBoo) {
        this.dinamicoBoo = dinamicoBoo;
    }

    public Integer getQuantidadeFaces() {
        return quantidadeFaces;
    }

    public void setQuantidadeFaces(Integer quantidadeFaces) {
        this.quantidadeFaces = quantidadeFaces;
    }

    public Integer getIdadeMinima() {
        return idadeMinima;
    }

    public void setIdadeMinima(Integer idadeMinima) {
        this.idadeMinima = idadeMinima;
    }

    public Integer getIdadeMaxima() {
        return idadeMaxima;
    }

    public void setIdadeMaxima(Integer idadeMaxima) {
        this.idadeMaxima = idadeMaxima;
    }

    public void setDescricaoId(String descricaoId) {
        this.descricaoId = descricaoId;
    }

    public String getDinamico() {
        return dinamico;
    }

    public void setDinamico(String dinamico) {
        this.dinamico = dinamico;
    }

    public Long getIdEmpresa() {
        return idEmpresa;
    }

    public void setIdEmpresa(Long idEmpresa) {
        this.idEmpresa = idEmpresa;
    }

    public String getCiclico() {
        return ciclico;
    }

    public void setCiclico(String ciclico) {
        this.ciclico = ciclico;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getCodigoConvenio() {
        return codigoConvenio;
    }

    public void setCodigoConvenio(String codigoConvenio) {
        this.codigoConvenio = codigoConvenio;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public BigDecimal getValorRepasse() {
        return valorRepasse;
    }

    public void setValorRepasse(BigDecimal valorRepasse) {
        this.valorRepasse = valorRepasse;
    }

}
