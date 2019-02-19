package br.com.lume.odonto.bo;

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
import br.com.lume.odonto.entity.KitItem;
import br.com.lume.odonto.entity.ReservaKit;
import br.com.lume.security.entity.Empresa;

public class KitItemBO extends BO<KitItem> {

    private Logger log = Logger.getLogger(KitItemBO.class);

    private static final long serialVersionUID = 1L;

    public KitItemBO() {
        super(PersistenceUnitName.ODONTO);
        this.setClazz(KitItem.class);
    }

    @Override
    public boolean remove(KitItem kitItem) throws BusinessException, TechnicalException {
        kitItem.setExcluido(Status.SIM);
        kitItem.setDataExclusao(Calendar.getInstance().getTime());
        kitItem.setExcluidoPorProfissional(ProfissionalBO.getProfissionalLogado().getId());
        this.persist(kitItem);
        return true;
    }

    @Override
    public List<KitItem> listAll() throws Exception {
        return this.listByEmpresa();
    }

    public List<KitItem> listByEmpresa() throws Exception {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("idEmpresa", ProfissionalBO.getProfissionalLogado().getIdEmpresa());
            parametros.put("excluido", Status.NAO);
            return this.listByFields(parametros);
        } catch (Exception e) {
            log.error("Erro no listByEmpresa", e);
        }
        return null;
    }

    public List<KitItem> listByEmpresa(Empresa empresa) throws Exception {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT ");
            sb.append("KI.* ");
            sb.append("FROM KIT_ITEM KI,KIT K,ITEM I ");
            sb.append("WHERE KI.ID_ITEM = I.ID ");
            sb.append("AND KI.ID_KIT = K.ID ");
            sb.append("AND K.ID_EMPRESA = ?1 ");
            sb.append("AND K.ID_EMPRESA = I.ID_EMPRESA ");
            sb.append("AND KI.EXCLUIDO = 'N' ");
            sb.append("AND K.EXCLUIDO = 'N' ");
            sb.append("AND I.EXCLUIDO = 'N' ");

            Query query = this.getDao().createNativeQuery(sb.toString(), KitItem.class);
            query.setParameter(1, empresa.getEmpIntCod());
            return query.getResultList();
        } catch (Exception e) {
            log.error("Erro no listByEmpresa", e);
        }
        return null;
    }

    public List<KitItem> listItensPendentesByReservaKit(ReservaKit reservaKit) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT ");
            sb.append("KI.* ");
            sb.append("FROM RESERVA_KIT RK,KIT K,KIT_ITEM KI, ITEM I ");
            sb.append("WHERE RK.ID_KIT = K.ID ");
            sb.append("AND K.ID = KI.ID_KIT ");
            sb.append("AND KI.EXCLUIDO = 'N' ");
            sb.append("AND KI.ID_ITEM = I.ID ");
            sb.append("AND ( ");
            sb.append(" (I.CATEGORIA = 'N' AND NOT EXISTS ( ");
            sb.append("        SELECT ");
            sb.append("        1 ");
            sb.append("        FROM MATERIAL_INDISPONIVEL MI,MATERIAL M2,ITEM I2 ");
            sb.append("        WHERE MI.ID_MATERIAL = M2.ID ");
            sb.append("        AND M2.ID_ITEM = I2.ID ");
            sb.append("        AND MI.ID_RESERVA_KIT = RK.ID ");
            sb.append("        AND I2.ID = KI.ID_ITEM ");
            sb.append("     ) ");
            sb.append(" ) ");
            sb.append(" OR ");
            sb.append(" (I.CATEGORIA = 'S' AND NOT EXISTS ( ");
            sb.append("        SELECT ");
            sb.append("        1 ");
            sb.append("        FROM MATERIAL_INDISPONIVEL MI,MATERIAL M2,ITEM I2, ITEM I3 ");
            sb.append("        WHERE MI.ID_MATERIAL = M2.ID ");
            sb.append("        AND M2.ID_ITEM = I2.ID ");
            sb.append("        AND I2.ID_ITEM_PAI = I3.ID ");
            sb.append("        AND MI.ID_RESERVA_KIT = RK.ID ");
            sb.append("        AND I3.ID = KI.ID_ITEM ");
            sb.append("     ) ");
            sb.append(" ) ");
            sb.append(") ");
            sb.append("AND RK.ID = ?1 ");
            Query q = this.getDao().createNativeQuery(sb.toString(), KitItem.class);
            q.setParameter(1, reservaKit.getId());
            return this.list(q);
        } catch (Exception e) {
            log.error("Erro no listByStatusAndReserva", e);
        }
        return null;
    }

}
