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
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import br.com.lume.common.util.Status;
import br.com.lume.common.util.Utils;
import br.com.lume.odonto.bo.KitItemBO;

@Entity()
@Table(name = "RESERVA_KIT")
public class ReservaKit implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "ID_KIT")
    private Kit kit;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "ID_RESERVA")
    private Reserva reserva;

    @Column(name = "QUANTIDADE")
    private Long quantidade;

    @Column(name = "STATUS")
    private String status;

    private String excluido = Status.NAO;

    @Column(name = "DATA_EXCLUSAO")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataExclusao;

    @Column(name = "DATA_ENTREGA")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataEntrega;

    @Column(name = "DATA_FINALIZADO")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataFinalizado;

    private String justificativa;

    @ManyToOne
    @JoinColumn(name = "DEVOLVIDO_POR")
    private Profissional devolvidoPorProfissional;

    @ManyToMany(cascade = CascadeType.MERGE)
    @JoinTable(name = "RESERVA_KIT_AGENDAMENTO_PLANO_TRATAMENTO_PROCEDIMENTO", joinColumns = { @JoinColumn(name = "ID_RESERVA_KIT") }, inverseJoinColumns = { @JoinColumn(name = "ID_AGENDAMENTO_PLANO_TRATAMENTO_PROCEDIMENTO") })
    private List<AgendamentoPlanoTratamentoProcedimento> planoTratamentoProcedimentosAgendamentos;

    @Column(name = "EXCLUIDO_POR")
    private Long excluidoPorProfissional;

    @Transient
    private String procedimentoStr;

    public Long getExcluidoPorProfissional() {
        return this.excluidoPorProfissional;
    }

    public void setExcluidoPorProfissional(Long excluidoPorProfissional) {
        this.excluidoPorProfissional = excluidoPorProfissional;
    }

    public boolean isDisponiblizado() {
        List<KitItem> listItensPendentesByReservaKit = new KitItemBO().listItensPendentesByReservaKit(this);
        return listItensPendentesByReservaKit == null || listItensPendentesByReservaKit.isEmpty();
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

    public Kit getKit() {
        return this.kit;
    }

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setKit(Kit kit) {
        this.kit = kit;
    }

    public Reserva getReserva() {
        return this.reserva;
    }

    public void setReserva(Reserva reserva) {
        this.reserva = reserva;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getQuantidade() {
        return this.quantidade;
    }

    public void setQuantidade(Long quantidade) {
        this.quantidade = quantidade;
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
        ReservaKit other = (ReservaKit) obj;
        if (this.id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!this.id.equals(other.id)) {
            return false;
        }
        return true;
    }

    public Date getDataEntrega() {
        return this.dataEntrega;
    }

    @Transient
    public String getDataEntregaStr() {
        return Utils.dateToString(this.dataEntrega, "dd/MM/yyyy");
    }

    @Transient
    public String getDataEntregaStrOrd() {
        return Utils.dateToString(this.dataEntrega, "yyyy/MM/dd");
    }

    public void setDataEntrega(Date dataEntrega) {
        this.dataEntrega = dataEntrega;
    }

    public Date getDataFinalizado() {
        return this.dataFinalizado;
    }

    public void setDataFinalizado(Date dataFinalizado) {
        this.dataFinalizado = dataFinalizado;
    }

    public String getJustificativa() {
        return this.justificativa;
    }

    public void setJustificativa(String justificativa) {
        this.justificativa = justificativa;
    }

    public Profissional getDevolvidoPorProfissional() {
        return this.devolvidoPorProfissional;
    }

    public void setDevolvidoPorProfissional(Profissional devolvidoPorProfissional) {
        this.devolvidoPorProfissional = devolvidoPorProfissional;
    }

    public String getProcedimentoStr() {
        String procedimentosStr = "";
        for (AgendamentoPlanoTratamentoProcedimento aptp : this.getPlanoTratamentoProcedimentosAgendamentos()) {
            procedimentosStr += aptp.getPlanoTratamentoProcedimento().getProcedimento().getDescricao() + " / ";
        }
        return procedimentosStr.equals("") ? " " : procedimentosStr.substring(0, procedimentosStr.length() - 3);
    }

    public List<AgendamentoPlanoTratamentoProcedimento> getPlanoTratamentoProcedimentosAgendamentos() {
        return this.planoTratamentoProcedimentosAgendamentos;
    }

    public void setPlanoTratamentoProcedimentosAgendamentos(List<AgendamentoPlanoTratamentoProcedimento> planoTratamentoProcedimentosAgendamentos) {
        this.planoTratamentoProcedimentosAgendamentos = planoTratamentoProcedimentosAgendamentos;
    }

    public void setProcedimentoStr(String procedimentoStr) {
        this.procedimentoStr = procedimentoStr;
    }
}
