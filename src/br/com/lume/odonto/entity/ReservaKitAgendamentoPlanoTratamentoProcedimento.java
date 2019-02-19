package br.com.lume.odonto.entity;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "RESERVA_KIT_AGENDAMENTO_PLANO_TRATAMENTO_PROCEDIMENTO")
public class ReservaKitAgendamentoPlanoTratamentoProcedimento implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_RESERVA_KIT")
    private ReservaKit reservaKit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_AGENDAMENTO_PLANO_TRATAMENTO_PROCEDIMENTO")
    private AgendamentoPlanoTratamentoProcedimento agendamentoPlanoTratamentoProcedimento;

    public ReservaKitAgendamentoPlanoTratamentoProcedimento() {
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
        result = prime * result + ((this.reservaKit == null) ? 0 : this.reservaKit.hashCode());
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
        ReservaKitAgendamentoPlanoTratamentoProcedimento other = (ReservaKitAgendamentoPlanoTratamentoProcedimento) obj;
        if (this.id != other.id) {
            return false;
        }
        if (this.reservaKit == null) {
            if (other.reservaKit != null) {
                return false;
            }
        } else if (!this.agendamentoPlanoTratamentoProcedimento.equals(other.agendamentoPlanoTratamentoProcedimento)) {
            return false;
        }
        return true;
    }

    public ReservaKit getReservaKit() {

        return this.reservaKit;
    }

    public void setReservaKit(ReservaKit reservaKit) {

        this.reservaKit = reservaKit;
    }

    public AgendamentoPlanoTratamentoProcedimento getAgendamentoPlanoTratamentoProcedimento() {

        return this.agendamentoPlanoTratamentoProcedimento;
    }

    public void setAgendamentoPlanoTratamentoProcedimento(AgendamentoPlanoTratamentoProcedimento agendamentoPlanoTratamentoProcedimento) {

        this.agendamentoPlanoTratamentoProcedimento = agendamentoPlanoTratamentoProcedimento;
    }

}
