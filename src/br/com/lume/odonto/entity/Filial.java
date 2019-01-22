package br.com.lume.odonto.entity;

import java.io.Serializable;
import java.text.Normalizer;
import java.util.Date;

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

import br.com.lume.common.util.Status;

/**
 * The persistent class for the FILIAL database table.
 */
@Entity
@Table(name = "FILIAL")
public class Filial implements Serializable, Comparable<Filial> {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // bi-directional many-to-one association to DadosBasico
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "ID_DADOS_BASICOS")
    private DadosBasico dadosBasico = new DadosBasico();

    @Column(name = "ID_EMPRESA")
    private Long idEmpresa;

    private String excluido = Status.NAO;

    @Column(name = "DATA_EXCLUSAO")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataExclusao;

    @Column(name = "DATA_ULTIMA_ALTERACAO")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataUltimaAlteracao;

    @ManyToOne
    @JoinColumn(name = "ALTERADO_POR")
    private Profissional alteradoPor;

    private String status = Status.ATIVO;

    @ManyToOne
    @JoinColumn(name = "JUSTIFICATIVA")
    private Dominio justificativa;

    @Column(name = "EXCLUIDO_POR")
    private Long excluidoPorProfissional;

    @Column(name = "DATA_CADASTRO")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataCadastro;

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

    public Filial() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public DadosBasico getDadosBasico() {
        return this.dadosBasico;
    }

    public void setDadosBasico(DadosBasico dadosBasico) {
        this.dadosBasico = dadosBasico;
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
        Filial other = (Filial) obj;
        if (this.id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!this.id.equals(other.id)) {
            return false;
        }
        return true;
    }

    public Long getIdEmpresa() {
        return this.idEmpresa;
    }

    public void setIdEmpresa(Long idEmpresa) {
        this.idEmpresa = idEmpresa;
    }

    @Override
    public int compareTo(Filial o) {
        if (this.dadosBasico == null || this.dadosBasico.getNome() == null || o == null || o.getDadosBasico() == null || o.getDadosBasico().getNome() == null) {
            return 1;
        } else {
            return Normalizer.normalize(this.dadosBasico.getNome(), Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "").compareToIgnoreCase(
                    Normalizer.normalize(o.dadosBasico.getNome(), Normalizer.Form.NFD));
        }
    }

    public Date getDataUltimaAlteracao() {
        return this.dataUltimaAlteracao;
    }

    public void setDataUltimaAlteracao(Date dataUltimaAlteracao) {
        this.dataUltimaAlteracao = dataUltimaAlteracao;
    }

    public Profissional getAlteradoPor() {
        return this.alteradoPor;
    }

    public void setAlteradoPor(Profissional alteradoPor) {
        this.alteradoPor = alteradoPor;
    }

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Dominio getJustificativa() {
        return this.justificativa;
    }

    public void setJustificativa(Dominio justificativa) {
        this.justificativa = justificativa;
    }

    public Date getDataCadastro() {
        return this.dataCadastro;
    }

    public void setDataCadastro(Date dataCadastro) {
        this.dataCadastro = dataCadastro;
    }
}
