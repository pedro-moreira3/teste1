package br.com.lume.odonto.entity;

import java.io.Serializable;
import java.text.Normalizer;
import java.util.Date;

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

import br.com.lume.common.util.Status;

@Entity
@Table(name = "MOTIVO")
public class Motivo implements Serializable, Comparable<Motivo> {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    @JoinColumn(name = "ID_CATEGORIA")
    private CategoriaMotivo categoria;

    private String tipo;

    @Column(name = "DESCRICAO")
    private String descricao;

    @Column(name = "SIGLA")
    private String sigla;

    @Column(name = "CODIGO_CONTIFY")
    private Integer codigoContify;

    private String excluido = Status.NAO;

    @Column(name = "DATA_EXCLUSAO")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataExclusao;

    @Column(name = "EXCLUIDO_POR")
    private Long excluidoPorProfissional;

    @Column(name = "CENTRO_CUSTO")
    private String centroCusto;

    private String grupo;

    private boolean ir;

    public static final String COMPRA_MATERIAIS = "CM";

    public static final String PAGAMENTO_PACIENTE = "PX";

    public static final String PAGAMENTO_CARTAO = "PT";

    public static final String DEVOLUCAO_PACIENTE = "DP";

    public static final String DESPESAS_DIVERSAS = "DD";

    public static final String PAGAMENTO_PROFISSIONAL = "PP";

    public static final String PAGAMENTO_LABORATORIO = "PL";

    public static final String SALDO_INICIAL = "SI";

    public Long getExcluidoPorProfissional() {
        return excluidoPorProfissional;
    }

    public static String getPagamentoProfissional() {
        return PAGAMENTO_PROFISSIONAL;
    }

    public static String getSaldoInicial() {
        return SALDO_INICIAL;
    }

    public void setExcluidoPorProfissional(Long excluidoPorProfissional) {
        this.excluidoPorProfissional = excluidoPorProfissional;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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
        Motivo other = (Motivo) obj;
        if (id != other.id) {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(Motivo m) {
        if (descricao == null || m == null || m.getDescricao() == null) {
            return 1;
        } else {
            return Normalizer.normalize(this.getDescricao(), Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "").compareToIgnoreCase(Normalizer.normalize(m.getDescricao(), Normalizer.Form.NFD));
        }
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getSigla() {
        return sigla;
    }

    public void setSigla(String sigla) {
        this.sigla = sigla;
    }

    public CategoriaMotivo getCategoria() {
        return categoria;
    }

    public void setCategoria(CategoriaMotivo categoria) {
        this.categoria = categoria;
    }

    public String getCentroCusto() {
        return centroCusto;
    }

    public void setCentroCusto(String centroCusto) {
        this.centroCusto = centroCusto;
    }

    public String getGrupo() {
        return grupo;
    }

    public void setGrupo(String grupo) {
        this.grupo = grupo;
    }

    public boolean isIr() {
        return ir;
    }

    public void setIr(boolean ir) {
        this.ir = ir;
    }

    public Integer getCodigoContify() {
        return codigoContify;
    }

    public void setCodigoContify(Integer codigoContify) {
        this.codigoContify = codigoContify;
    }

}
