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
import javax.persistence.Transient;

import br.com.lume.common.util.Status;
import br.com.lume.common.util.Utils;

@Entity
@Table(name = "ESTERILIZACAO")
public class Esterilizacao implements Serializable, Comparable<Esterilizacao> {

    private static final long serialVersionUID = 1L;

    public static final String ABERTO = "A";

    public static final String DEVOLVIDO = "D";

    public static final String ESTERILIZADO = "E";

    public static final String EMPACOTADO = "P";

    public static final String DESCARTADO = "X";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    @JoinColumn(name = "ID_PROFISSIONAL")
    private Profissional profissional;

    @ManyToOne
    @JoinColumn(name = "ID_SOLICITANTE")
    private Profissional solicitante;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "ID_ESTERILIZACAO")
    private List<EsterilizacaoKit> esterilizacaoKits;

    @Column(name = "ID_EMPRESA")
    private Long idEmpresa;

    @Column(name = "CLINICA")
    private Boolean clinica;

    @Column(name = "DATA")
    @Temporal(TemporalType.TIMESTAMP)
    private Date data;

    @Column(name = "DATA_DEVOLUCAO")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataDevolucao;

    @Column(name = "DATA_VALIDADE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataValidade;

    @Column(name = "STATUS")
    private String status;

    @Column(name = "OBSERVACAO")
    private String observacao;

    @Column(name = "DESCRICAO")
    private String descricao;

    private String excluido = Status.NAO;

    @Column(name = "DATA_EXCLUSAO")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataExclusao;

    @ManyToOne
    @JoinColumn(name = "DEVOLVIDO_POR")
    private Profissional devolvidoPorProfissional;

    @Column(name = "EXCLUIDO_POR")
    private Long excluidoPorProfissional;

    @Transient
    private String resumo;

    public String getResumo() {
        String string = "";
        if (esterilizacaoKits != null && !esterilizacaoKits.isEmpty()) {
            for (EsterilizacaoKit lk : esterilizacaoKits) {
                string += ", ";
                if (lk.getQuantidade() > 0) {
                    if (lk.getItem() != null) {
                        string += lk.getItem().getDescricao();
                    }
                }
            }
        }
        string = string.replaceFirst(", ", "");
        return string.length() > 110 ? string.substring(0, 110).concat("...") : string;
    }

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

    @Transient
    public String getDataStr() {
        return Utils.dateToString(data, "dd/MM/yyyy");
    }

    @Transient
    public String getDataStrOrd() {
        return Utils.dateToString(data, "yyyy/MM/dd");
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Long getIdEmpresa() {
        return idEmpresa;
    }

    public void setIdEmpresa(Long idEmpresa) {
        this.idEmpresa = idEmpresa;
    }

    public Date getData() {
        return data;
    }

    public void setData(Date data) {
        this.data = data;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }

    public Profissional getProfissional() {
        return profissional;
    }

    public void setProfissional(Profissional profissional) {
        this.profissional = profissional;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Date getDataDevolucao() {
        return dataDevolucao;
    }

    @Transient
    public String getDataDevolucaoStr() {
        return Utils.dateToString(dataDevolucao, "dd/MM/yyyy");
    }

    @Transient
    public String getDataDevolucaoStrOrd() {
        return Utils.dateToString(dataDevolucao, "yyyy/MM/dd");
    }

    public void setDataDevolucao(Date dataDevolucao) {
        this.dataDevolucao = dataDevolucao;
    }

    public String getStatus() {
        return status.equals(DEVOLVIDO) ? "Devolvido" : status.equals(ABERTO) ? "Aberto" : status.equals(ESTERILIZADO) ? "Em Esterilização" : status.equals(EMPACOTADO) ? "Empacotado" : "";
    }

    public void setStatus(String status) {
        this.status = status;
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
        Esterilizacao other = (Esterilizacao) obj;
        if (id != other.id) {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(Esterilizacao p) {
        if (this.getData().after(p.getData())) {
            return -1;
        } else {
            return 1;
        }
    }

    public List<EsterilizacaoKit> getEsterilizacaoKits() {
        return esterilizacaoKits;
    }

    public void setEsterilizacaoKits(List<EsterilizacaoKit> esterilizacaoKits) {
        this.esterilizacaoKits = esterilizacaoKits;
    }

    public Profissional getDevolvidoPorProfissional() {
        return devolvidoPorProfissional;
    }

    public void setDevolvidoPorProfissional(Profissional devolvidoPorProfissional) {
        this.devolvidoPorProfissional = devolvidoPorProfissional;
    }

    public Profissional getSolicitante() {
        return solicitante;
    }

    public void setSolicitante(Profissional solicitante) {
        this.solicitante = solicitante;
    }

    public Date getDataValidade() {
        return dataValidade;
    }

    public void setDataValidade(Date dataValidade) {
        this.dataValidade = dataValidade;
    }

    @Transient
    public String getDataValidadeStr() {
        return Utils.dateToString(dataValidade, "dd/MM/yyyy");
    }

    @Transient
    public String getDataValidadeStrOrd() {
        return Utils.dateToString(dataValidade, "yyyy/MM/dd");
    }

    public Boolean getClinica() {
        return clinica;
    }

    public void setClinica(Boolean clinica) {
        this.clinica = clinica;
    }
}
