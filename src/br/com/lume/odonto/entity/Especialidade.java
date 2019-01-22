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
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import br.com.lume.common.util.Status;
import br.com.lume.common.util.Utils;

/**
 * The persistent class for the ESPECIALIDADE database table.
 */
@Entity
@Table(name = "ESPECIALIDADE")
public class Especialidade implements Serializable, Comparable<Especialidade> {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "DESCRICAO")
    private String descricao;

    @Column(name = "RANGE_INI")
    private Integer rangeIni;

    @Column(name = "RANGE_FIM")
    private Integer rangeFim;

    private boolean anamnese;

    public Especialidade(Long idEmpresa, String descricao, Integer rangeIni, Integer rangeFim) {
        super();
        this.descricao = descricao;
        this.rangeIni = rangeIni;
        this.rangeFim = rangeFim;
        this.idEmpresa = idEmpresa;
    }

    @OneToMany(mappedBy = "especialidade")
    private List<Procedimento> procedimentos;

    @Column(name = "ID_EMPRESA")
    private Long idEmpresa;

    private String excluido = Status.NAO;

    @Column(name = "DATA_EXCLUSAO")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataExclusao;

    @Column(name = "EXCLUIDO_POR")
    private Long excluidoPorProfissional;

    @Transient
    private String descricaoId;

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

    public Especialidade() {
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

    public String getDescricaoLimpa() {
        return Utils.normalize(descricao);
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public List<Procedimento> getProcedimentos() {
        return procedimentos;
    }

    public void setProcedimentos(List<Procedimento> procedimentos) {
        this.procedimentos = procedimentos;
    }

    public Integer getRangeIni() {
        return rangeIni;
    }

    public void setRangeIni(Integer rangeIni) {
        this.rangeIni = rangeIni;
    }

    public Integer getRangeFim() {
        return rangeFim;
    }

    public void setRangeFim(Integer rangeFim) {
        this.rangeFim = rangeFim;
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
        Especialidade other = (Especialidade) obj;
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        return true;
    }

    public Long getIdEmpresa() {
        return idEmpresa;
    }

    public void setIdEmpresa(Long idEmpresa) {
        this.idEmpresa = idEmpresa;
    }

    @Override
    public int compareTo(Especialidade o) {
        if (descricao == null || o == null || o.getDescricao() == null) {
            return 1;
        } else {
            return Normalizer.normalize(this.getDescricao(), Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "").compareToIgnoreCase(Normalizer.normalize(o.getDescricao(), Normalizer.Form.NFD));
        }
    }

    public String getDescricaoId() {
        return descricao;
    }

    public void setDescricaoId(String descricaoId) {
        this.descricaoId = descricaoId;
    }

    public boolean isAnamnese() {
        return anamnese;
    }

    public void setAnamnese(boolean anamnese) {
        this.anamnese = anamnese;
    }
}
