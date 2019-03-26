package br.com.lume.odonto.bo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

import org.apache.log4j.Logger;

import br.com.lume.common.exception.business.BusinessException;
import br.com.lume.common.exception.techinical.TechnicalException;
import br.com.lume.common.util.Status;
import br.com.lume.odonto.dao.PersistenceUnitName;
import br.com.lume.odonto.entity.ControleMaterial;
import br.com.lume.odonto.entity.MaterialLog;
import br.com.lume.odonto.entity.Reserva;
import br.com.lume.odonto.entity.ReservaKit;

public class ReservaKitBO extends BO<ReservaKit> {

    private Logger log = Logger.getLogger(ReservaKitBO.class);

    private static final long serialVersionUID = 1L;

    public ReservaKitBO() {
        super(PersistenceUnitName.ODONTO);
        this.setClazz(ReservaKit.class);
    }

    @Override
    public boolean remove(ReservaKit reservaKit) throws BusinessException, TechnicalException {
        reservaKit.setExcluido(Status.SIM);
        reservaKit.setDataExclusao(Calendar.getInstance().getTime());
        reservaKit.setExcluidoPorProfissional(ProfissionalBO.getProfissionalLogado().getId());

        ControleMaterialBO controleMaterialBO = new ControleMaterialBO();
        MaterialLogBO materialLogBO = new MaterialLogBO();

        List<ControleMaterial> cms = controleMaterialBO.listByReservaKit(reservaKit);

        if (cms != null && !cms.isEmpty()) {
            MaterialBO materialBO = new MaterialBO();
            for (ControleMaterial cm : cms) {
                materialBO.refresh(cm.getMaterial());
                cm.getMaterial().setQuantidadeAtual(cm.getMaterial().getQuantidadeAtual().add(cm.getQuantidade()));
                materialLogBO.persist(new MaterialLog(cm, null, cm.getMaterial(), ProfissionalBO.getProfissionalLogado(), cm.getQuantidade(), cm.getMaterial().getQuantidadeAtual(),
                        MaterialLog.RESERVA_KIT_EXCLUIDA));
                materialBO.persist(cm.getMaterial());
                cm.setQuantidade(new BigDecimal(0d));
                cm.setStatus("UK");
                controleMaterialBO.persist(cm);
            }
        }

        this.persist(reservaKit);
        return true;
    }

    public void removeByReserva(Reserva reserva) {
        try {
            ControleMaterialBO controleMaterialBO = new ControleMaterialBO();
            ReservaKitAgendamentoPlanoTratamentoProcedimentoBO agendamentoPlanoTratamentoProcedimentoBO = new ReservaKitAgendamentoPlanoTratamentoProcedimentoBO();
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("reserva", reserva);
            List<ReservaKit> rks = this.listByFields(parametros);
            for (ReservaKit rk : rks) {
                if (rk != null) {
                    agendamentoPlanoTratamentoProcedimentoBO.removeByReservaKist(rk);
                    //controleMaterialBO.removeByReservaKist(rk);
                }
            }
            for (ReservaKit rk : rks) {
                if (rk != null) {
                    super.remove(rk);
                }
            }

        } catch (Exception e) {
            log.error("Erro no removeByReserva", e);
        }
    }

    public List<ReservaKit> listKitsDevolucao() throws Exception {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT ");
            sb.append("DISTINCT RKI.* ");
            sb.append("FROM ");
            sb.append("RESERVA RES,RESERVA_KIT RKI, MATERIAL_INDISPONIVEL MAT ");
            sb.append("WHERE ");
            sb.append("RES.ID = RKI.ID_RESERVA ");
            sb.append("AND RES.ID_EMPRESA = ?1 ");
            sb.append("AND RES.EXCLUIDO = 'N' ");
            sb.append("AND RKI.STATUS = 'EN' ");
            sb.append("AND RKI.ID = MAT.ID_RESERVA_KIT ");
            sb.append("AND MAT.STATUS IS NULL ");
            sb.append("AND MAT.EXCLUIDO = 'N' ");
            Query query = this.getDao().createNativeQuery(sb.toString());
            query = this.getDao().createNativeQuery(sb.toString(), ReservaKit.class);
            query.setParameter(1, ProfissionalBO.getProfissionalLogado().getIdEmpresa());
            return query.getResultList();
        } catch (Exception e) {
            log.error("Erro no listKitsDevolucao", e);
        }
        return null;
    }

    public List<ReservaKit> listByStatusAndReserva(String status) throws Exception {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("select ");
            sb.append("rk ");
            sb.append("from ReservaKit AS rk , Reserva AS r ");
            sb.append("WHERE rk.status = :status ");
            sb.append("AND rk.reserva.id = r.id ");
            sb.append("AND rk.excluido= :excluido ");
            sb.append("AND r.idEmpresa = :empresa ");
            sb.append("AND r.excluido= :excluido ");
            sb.append("ORDER BY r.prazo ");
            //BY r.agendamento.inicio ");
            Query q = this.getDao().createQuery(sb.toString());
            q.setParameter("status", status);
            q.setParameter("empresa", ProfissionalBO.getProfissionalLogado().getIdEmpresa());
            q.setParameter("excluido", Status.NAO);
            
            return this.list(q);
        } catch (Exception e) {
            log.error("Erro no listByStatusAndReserva", e);
        }
        return null;
    }

    public List<ReservaKit> listReservasDia() {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT ");
            sb.append("t1.* ");
            sb.append("FROM RESERVA t0, RESERVA t2, RESERVA_KIT t1 ");
            sb.append("WHERE ");
            sb.append("t1.STATUS = 'PE' AND ");
            sb.append("t2.ID = t0.ID AND ");
            sb.append("t1.EXCLUIDO = 'N' AND ");
            sb.append("t0.ID_EMPRESA = ? AND ");
            sb.append("t0.EXCLUIDO = 'N' AND ");
            sb.append("DATE(t0.PRAZO) = CURRENT_DATE AND ");
            sb.append("t2.ID = t1.ID_RESERVA ");
            sb.append("ORDER BY t0.PRAZO ");
            Query query = this.getDao().createNativeQuery(sb.toString(), ReservaKit.class);
            query.setParameter(1, ProfissionalBO.getProfissionalLogado().getIdEmpresa());
            return query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }
}
