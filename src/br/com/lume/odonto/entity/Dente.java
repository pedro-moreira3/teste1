package br.com.lume.odonto.entity;

import java.io.Serializable;
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
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import br.com.lume.common.util.Status;

/**
 * The persistent class for the DENTE database table.
 */
@Entity
@Table(name = "DENTE")
public class Dente implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String descricao;

    @ManyToOne
    @JoinColumn(name = "ID_ODONTOGRAMA")
    private Odontograma odontograma;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "ID_DENTE")
    private List<RegiaoDente> regioes;

    private String excluido = Status.NAO;

    @Column(name = "DATA_EXCLUSAO")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataExclusao;

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

    public Dente() {
    }

    public Dente(String descricao, Odontograma odontograma) {
        this.descricao = descricao;
        this.odontograma = odontograma;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Odontograma getOdontograma() {
        return odontograma;
    }

    public void setOdontograma(Odontograma odontograma) {
        this.odontograma = odontograma;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((descricao == null) ? 0 : descricao.hashCode());
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
        Dente other = (Dente) obj;
        if (descricao == null) {
            if (other.descricao != null) {
                return false;
            }
        } else if (!descricao.equals(other.descricao)) {
            return false;
        }
        return true;
    }

//    public void setRegioesObj(List<StatusDente> regioesObj) {
//        try {
//            regioes = new ArrayList<>();
//            for (StatusDente statusDente : regioesObj) {
//                regioes.add(new RegiaoDente(statusDente, 'C', this));
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    public List<StatusDente> getRegioesObj() {
//        List<StatusDente> regioesObj = new ArrayList<>();
//        for (RegiaoDente rd : regioes) {
//            if (!regioesObj.contains(rd.getStatusDente())) {
//                regioesObj.add(rd.getStatusDente());
//            }
//        }
//        return regioesObj;
//    }

    public List<RegiaoDente> getRegioes() {
        return regioes;
    }

    public void setRegioes(List<RegiaoDente> regioes) {
        this.regioes = regioes;
    }

}
