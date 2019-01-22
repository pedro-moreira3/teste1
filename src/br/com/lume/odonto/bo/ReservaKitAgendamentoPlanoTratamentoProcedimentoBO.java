package br.com.lume.odonto.bo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import br.com.lume.common.util.Status;
import br.com.lume.odonto.dao.PersistenceUnitName;
import br.com.lume.odonto.entity.Agendamento;
import br.com.lume.odonto.entity.AgendamentoPlanoTratamentoProcedimento;
import br.com.lume.odonto.entity.ReservaKit;
import br.com.lume.odonto.entity.ReservaKitAgendamentoPlanoTratamentoProcedimento;

public class ReservaKitAgendamentoPlanoTratamentoProcedimentoBO extends BO<ReservaKitAgendamentoPlanoTratamentoProcedimento> {

    private Logger log = Logger.getLogger(ReservaKitAgendamentoPlanoTratamentoProcedimento.class);

    private static final long serialVersionUID = 1L;

    public ReservaKitAgendamentoPlanoTratamentoProcedimentoBO() {
        super(PersistenceUnitName.ODONTO);
        this.setClazz(ReservaKitAgendamentoPlanoTratamentoProcedimento.class);
    }

    public List<ReservaKitAgendamentoPlanoTratamentoProcedimento> listByAgendamento(Agendamento agendamento) {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("agendamentoPlanoTratamentoProcedimento.agendamento", agendamento);
            // parametros.put("excluido", Status.NAO);
            return this.listByFields(parametros);
        } catch (Exception e) {
            log.error("Erro no listByAgendamento", e);
        }
        return null;
    }

    public ReservaKitAgendamentoPlanoTratamentoProcedimento find(ReservaKit rk, AgendamentoPlanoTratamentoProcedimento aptp) {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("agendamentoPlanoTratamentoProcedimento", aptp);
            parametros.put("reservaKit", rk);
            return this.findByFields(parametros);
        } catch (Exception e) {
            log.error("Erro no find de ReservaKitAgendamentoPlanoTratamentoProcedimento", e);
        }
        return null;
    }

    public void removeByReservaKist(ReservaKit reservaKit) {
        try {
            List<ReservaKitAgendamentoPlanoTratamentoProcedimento> mats = this.listByReservaKit(reservaKit);
            for (ReservaKitAgendamentoPlanoTratamentoProcedimento mat : mats) {
                if (mat != null) {
                    super.remove(mat);
                }
            }
        } catch (Exception e) {
            log.error("Erro no removeByReservaKist", e);
        }
    }

    public List<ReservaKitAgendamentoPlanoTratamentoProcedimento> listByReservaKit(ReservaKit reservaKit) throws Exception {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("reservaKit", reservaKit);
            return this.listByFields(parametros);
        } catch (Exception e) {
            log.error("Erro no listByReservaKit", e);
        }
        return null;
    }

    public boolean existeReserva(AgendamentoPlanoTratamentoProcedimento aptp) {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("agendamentoPlanoTratamentoProcedimento.planoTratamentoProcedimento", aptp.getPlanoTratamentoProcedimento());
            parametros.put("agendamentoPlanoTratamentoProcedimento.agendamento.id", aptp.getAgendamento().getId());
            parametros.put("reservaKit.reserva.excluido", Status.NAO);
            //parametros.put("reservaKit.status", "PE");
            List<ReservaKitAgendamentoPlanoTratamentoProcedimento> listByFields = this.listByFields(parametros);
            return listByFields != null && !listByFields.isEmpty() ? true : false;
        } catch (Exception e) {
            log.error("Erro existeReserva", e);
        }
        return true;
    }
}
