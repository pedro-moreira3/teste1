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
import br.com.lume.odonto.bo.ProfissionalBO;
import br.com.lume.odonto.bo.ReservaBO;
import br.com.lume.security.bo.EmpresaBO;

/**
 * The persistent class for the AGENDAMENTO database table.
 */
@Entity
@Table(name = "AGENDAMENTO")
public class Agendamento implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "INICIOU_AS")
    @Temporal(TemporalType.TIMESTAMP)
    private Date iniciouAs;

    @Column(name = "FINALIZOU_AS")
    @Temporal(TemporalType.TIMESTAMP)
    private Date finalizouAs;

    @Column(name = "CHEGOU_AS")
    @Temporal(TemporalType.TIMESTAMP)
    private Date chegouAs;

    private String status = StatusAgendamento.NAO_CONFIRMADO.getSigla();

    @Temporal(TemporalType.TIMESTAMP)
    private Date inicio;

    @Temporal(TemporalType.TIMESTAMP)
    private Date fim;

    @Column(name = "DATA_ULT_ALTERACAO")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataUltAlteracao;

    @ManyToOne
    @JoinColumn(name = "ID_PACIENTE")
    private Paciente paciente;

    @ManyToOne
    @JoinColumn(name = "ID_FILIAL")
    private Filial filial;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "ID_AGENDAMENTO")
    private List<AgendamentoPlanoTratamentoProcedimento> planoTratamentoProcedimentosAgendamento;

    @ManyToOne
    @JoinColumn(name = "ID_DENTISTA")
    private Profissional profissional;

    @ManyToOne
    @JoinColumn(name = "ID_PLANO_TRATAMENTO")
    private PlanoTratamento planoTratamento;

    @ManyToOne
    @JoinColumn(name = "ID_AGENDADOR")
    private Profissional agendador;

    @ManyToOne
    @JoinColumn(name = "ID_ULT_ALTERACAO")
    private Profissional profissionalUltAlteracao;

    @Column(name = "DATA_AGENDAMENTO")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataAgendamento;

    @Column(name = "DESCRICAO")
    private String descricao;

    @Column(name = "JUSTIFICATIVA")
    private String justificativa;

    private String excluido = Status.NAO;

    @Column(name = "DATA_EXCLUSAO")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataExclusao;

    @Column(name = "EXCLUIDO_POR")
    private Long excluidoPorProfissional;

    private String encaixe;

    private boolean auxiliar;

    private int cadeira = 1;

    public Long getExcluidoPorProfissional() {
        return excluidoPorProfissional;
    }

    public void setExcluidoPorProfissional(Long excluidoPorProfissional) {
        this.excluidoPorProfissional = excluidoPorProfissional;
    }

    private String hash;

    public Agendamento() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Date getIniciouAs() {
        return iniciouAs;
    }

    @Transient
    public String getIniciouAsStr() {
        return Utils.dateToString(iniciouAs);
    }

    @Transient
    public String getIniciouAsStrOrd() {
        return Utils.dateToString(iniciouAs, "yyyy/MM/dd");
    }

    public void setIniciouAs(Date iniciouAs) {
        this.iniciouAs = iniciouAs;
    }

    public Date getFinalizouAs() {
        return finalizouAs;
    }

    @Transient
    public String getFinalizouAsStr() {
        return Utils.dateToString(finalizouAs);
    }

    @Transient
    public String getFinalizouAsStrOrd() {
        return Utils.dateToString(finalizouAs, "yyyy/MM/dd");
    }

    public void setFinalizouAs(Date finalizouAs) {
        this.finalizouAs = finalizouAs;
    }

    public Date getChegouAs() {
        return chegouAs;
    }

    @Transient
    public String getChegouAsStr() {
        return Utils.dateToString(chegouAs);
    }

    @Transient
    public String getChegouAsStrOrd() {
        return Utils.dateToString(chegouAs, "yyyy/MM/dd");
    }

    public void setChegouAs(Date chegouAs) {
        this.chegouAs = chegouAs;
    }

    public String getStatus() {
        return status;
    }

    @Transient
    public String getStatusDescricao() {
        StatusAgendamento findBySigla = StatusAgendamento.findBySigla(status);
        return findBySigla != null ? findBySigla.getDescricao() : "";
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getInicio() {
        return inicio;
    }

    @Transient
    public String getInicioStr() {
        return Utils.dateToString(inicio);
    }

    @Transient
    public String getInicioStrH() {
        return Utils.dateToString(inicio, "HH:mm");
    }

    @Transient
    public String getInicioStrOrd() {
        return Utils.dateToString(inicio, "dd/MM/yyyy");
    }

    @Transient
    public String getInicioHoraStr() {
        return Utils.dateToString(inicio, "HH:mm:ss");
    }

    public void setInicio(Date inicio) {
        this.inicio = inicio;
    }

    public Date getFim() {
        return fim;
    }

    public void setFim(Date fim) {
        this.fim = fim;
    }

    @Transient
    public String getFimStr() {
        return Utils.dateToString(fim);
    }

    @Transient
    public String getFimStrH() {
        return Utils.dateToString(fim, "HH:mm");
    }

    @Transient
    public String getFimStrOrd() {
        return Utils.dateToString(fim, "yyyy/MM/dd");
    }

    public Paciente getPaciente() {
        return paciente;
    }

    public void setPaciente(Paciente paciente) {
        this.paciente = paciente;
    }

    public Profissional getProfissional() {
        return profissional;
    }

    public void setProfissional(Profissional profissional) {
        this.profissional = profissional;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public List<AgendamentoPlanoTratamentoProcedimento> getPlanoTratamentoProcedimentosAgendamento() {
        return planoTratamentoProcedimentosAgendamento;
    }

    public void setPlanoTratamentoProcedimentosAgendamento(List<AgendamentoPlanoTratamentoProcedimento> planoTratamentoProcedimentosAgendamento) {
        this.planoTratamentoProcedimentosAgendamento = planoTratamentoProcedimentosAgendamento;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((fim == null) ? 0 : fim.hashCode());
        result = prime * result + (int) (id ^ (id >>> 32));
        result = prime * result + ((inicio == null) ? 0 : inicio.hashCode());
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
        Agendamento other = (Agendamento) obj;
        if (fim == null) {
            if (other.fim != null) {
                return false;
            }
        } else if (!fim.equals(other.fim)) {
            return false;
        }
        if (id != other.id) {
            return false;
        }
        if (inicio == null) {
            if (other.inicio != null) {
                return false;
            }
        } else if (!inicio.equals(other.inicio)) {
            return false;
        }
        return true;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public boolean isExcluido() {
        return Status.SIM.equals(excluido);
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

    public Profissional getAgendador() {
        return agendador;
    }

    public void setAgendador(Profissional agendador) {
        this.agendador = agendador;
    }

    public Date getDataAgendamento() {
        return dataAgendamento;
    }

    public void setDataAgendamento(Date dataAgendamento) {
        this.dataAgendamento = dataAgendamento;
    }

    @Transient
    public String getDataAgendamentoStr() {
        return Utils.dateToString(dataAgendamento);
    }

    @Transient
    public String getDataUltAlteracaoStr() {
        return Utils.dateToString(dataUltAlteracao);
    }

    @Transient
    public String getDataAgendamentoStrOrd() {
        return Utils.dateToString(dataAgendamento, "yyyy/MM/dd");
    }

    @Transient
    public String getDataPaciente() {
        return Utils.dateToString(inicio) + "  -  " + paciente.getDadosBasico().getNome();
    }

    public String getJustificativa() {
        return justificativa;
    }

    public void setJustificativa(String justificativa) {
        this.justificativa = justificativa;
    }

    public Filial getFilial() {
        return filial;
    }

    public void setFilial(Filial filial) {
        this.filial = filial;
    }

    public String getEncaixe() {
        return encaixe;
    }

    public void setEncaixe(String encaixe) {
        this.encaixe = encaixe;
    }

    public boolean isReserva() {
        try {
            List<Reserva> reservas = new ReservaBO().listByAgendamento(this);
            return reservas != null && !reservas.isEmpty();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean isConfirmado() {
        return status != null && status.equals(StatusAgendamento.CONFIRMADO.getSigla()) ? true : false;
    }

    public PlanoTratamento getPlanoTratamento() {
        return planoTratamento;
    }

    public void setPlanoTratamento(PlanoTratamento planoTratamento) {
        this.planoTratamento = planoTratamento;
    }

    public boolean isAuxiliar() {
        return auxiliar;
    }

    public String getAuxiliarStr() {
        return auxiliar ? "Sim" : "Não";
    }

    public void setAuxiliar(boolean auxiliar) {
        this.auxiliar = auxiliar;
    }

    public String getDescricaoAgenda() {
        String descricao = getPaciente().getDadosBasico().getNome();

        if ((!ProfissionalBO.getProfissionalLogado().getPerfil().equals(OdontoPerfil.DENTISTA)) || (ProfissionalBO.getProfissionalLogado().getPerfil().equals(
                OdontoPerfil.DENTISTA) && EmpresaBO.getEmpresaLogada().isEmpBolDentistaAdmin() == true)) {
            if (getPaciente().getDadosBasico().getCelular() != null && !getPaciente().getDadosBasico().getCelular().equals("")) {
                descricao += " - " + getPaciente().getDadosBasico().getCelular();
            } else {
                if (getPaciente().getDadosBasico().getTelefone() != null && !getPaciente().getDadosBasico().getTelefone().equals("")) {
                    descricao += " - " + getPaciente().getDadosBasico().getTelefone();
                }
            }
        }
        return descricao;
    }

    public String getEnderecoEmpresa() {
        try {
            return new EmpresaBO().find(getProfissional().getIdEmpresa()).getEnderecoCompleto();
        } catch (Exception e) {
            // TODO: handle exception
        }
        return "";

    }

    public int getCadeira() {
        return cadeira;
    }

    public void setCadeira(int cadeira) {
        this.cadeira = cadeira;
    }

    public String getWhatsURL() {
        if (paciente != null && paciente.getDadosBasico() != null && paciente.getDadosBasico().getCelular() != null) {
            return "https://api.whatsapp.com/send?phone=55" + paciente.getDadosBasico().getCelularSemMascara() + "&text=Favor confirmar consulta!";
        } else {
            return null;
        }
    }

    public Date getDataUltAlteracao() {
        return dataUltAlteracao;
    }

    public void setDataUltAlteracao(Date dataUltAlteracao) {
        this.dataUltAlteracao = dataUltAlteracao;
    }

    public Profissional getProfissionalUltAlteracao() {
        return profissionalUltAlteracao;
    }

    public void setProfissionalUltAlteracao(Profissional profissionalUltAlteracao) {
        this.profissionalUltAlteracao = profissionalUltAlteracao;
    }

}
