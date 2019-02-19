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
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import br.com.lume.common.util.Status;
import br.com.lume.common.util.Utils;

@Entity
@Table(name = "ABASTECIMENTO")
public class Abastecimento implements Serializable, Comparable<Abastecimento> {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    @JoinColumn(name = "ID_MATERIAL")
    private Material material;

    @ManyToOne
    @JoinColumn(name = "ID_PROFISSIONAL")
    private Profissional profissional;

    @Column(name = "QUANTIDADE")
    private BigDecimal quantidade;

    @Column(name = "STATUS")
    private String status;

    @Column(name = "DATA_ENTREGA")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataEntrega;

    @Column(name = "DATA_FINALIZADO")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataFinalizado;

    private String excluido = Status.NAO;

    @Column(name = "DATA_EXCLUSAO")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataExclusao;

    @Column(name = "EXCLUIDO_POR")
    private Long excluidoPorProfissional;

    @Column(name = "ID_EMPRESA")
    private Long idEmpresa;

    @ManyToOne
    @JoinColumn(name = "ID_AGENDAMENTO")
    private Agendamento agendamento;

    @ManyToMany(cascade = CascadeType.MERGE)
    @JoinTable(name = "ABASTECIMENTO_AGENDAMENTO_PLANO_TRATAMENTO_PROCEDIMENTO", joinColumns = { @JoinColumn(name = "ID_ABASTECIMENTO") }, inverseJoinColumns = { @JoinColumn(name = "ID_AGENDAMENTO_PLANO_TRATAMENTO_PROCEDIMENTO") })
    private List<AgendamentoPlanoTratamentoProcedimento> planoTratamentoProcedimentosAgendamentos;

    @Transient
    private String procedimentoStr;

    public Long getExcluidoPorProfissional() {
        return this.excluidoPorProfissional;
    }

    public void setExcluidoPorProfissional(Long excluidoPorProfissional) {
        this.excluidoPorProfissional = excluidoPorProfissional;
    }

    public Material getMaterial() {
        return this.material;
    }

    public void setMaterial(Material material) {
        this.material = material;
    }

    public BigDecimal getQuantidade() {
        return this.quantidade;
    }

    public Integer getQuantidadeInt() {
        return this.quantidade != null ? this.quantidade.intValue() : 0;
    }

    public void setQuantidade(BigDecimal quantidade) {
        this.quantidade = quantidade;
    }

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
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
        Abastecimento other = (Abastecimento) obj;
        if (this.id != other.id) {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(Abastecimento cm) {
        if (this.getId() > cm.getId()) {
            return 1;
        } else {
            return -1;
        }
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

    public Profissional getProfissional() {
        return this.profissional;
    }

    public void setProfissional(Profissional profissional) {
        this.profissional = profissional;
    }

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Transient
    public String getDataEntregaStr() {
        return Utils.dateToString(this.dataEntrega, "dd/MM/yyyy");
    }

    @Transient
    public String getDataEntregaStrOrd() {
        return Utils.dateToString(this.dataEntrega, "yyyy/MM/dd");
    }

    public Date getDataEntrega() {
        return this.dataEntrega;
    }

    public void setDataEntrega(Date dataEntrega) {
        this.dataEntrega = dataEntrega;
    }

    @Transient
    public String getDataFinalizadoStr() {
        return Utils.dateToString(this.dataFinalizado, "dd/MM/yyyy");
    }

    @Transient
    public String getDataFinalizadoStrOrd() {
        return Utils.dateToString(this.dataFinalizado, "yyyy/MM/dd");
    }

    public Date getDataFinalizado() {
        return this.dataFinalizado;
    }

    public void setDataFinalizado(Date dataFinalizado) {
        this.dataFinalizado = dataFinalizado;
    }

    public Long getIdEmpresa() {
        return this.idEmpresa;
    }

    public void setIdEmpresa(Long idEmpresa) {
        this.idEmpresa = idEmpresa;
    }

    public Agendamento getAgendamento() {
        return this.agendamento;
    }

    public void setAgendamento(Agendamento agendamento) {
        this.agendamento = agendamento;
    }

    public String getProcedimentoStr() {
        for (AgendamentoPlanoTratamentoProcedimento aptp : this.getPlanoTratamentoProcedimentosAgendamentos()) {
            this.procedimentoStr += aptp.getPlanoTratamentoProcedimento().getProcedimento().getDescricao() + " / ";
        }
        if (this.procedimentoStr == null) {
            return null;
        }
        return this.procedimentoStr.substring(0, this.procedimentoStr.length() - 3);
    }

    public List<AgendamentoPlanoTratamentoProcedimento> getPlanoTratamentoProcedimentosAgendamentos() {
        return this.planoTratamentoProcedimentosAgendamentos;
    }

    public void setPlanoTratamentoProcedimentosAgendamentos(List<AgendamentoPlanoTratamentoProcedimento> planoTratamentoProcedimentosAgendamentos) {
        this.planoTratamentoProcedimentosAgendamentos = planoTratamentoProcedimentosAgendamentos;
    }
}
