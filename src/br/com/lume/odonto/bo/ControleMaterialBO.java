package br.com.lume.odonto.bo;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

import org.apache.log4j.Logger;

import br.com.lume.common.connection.GenericListDAO;
import br.com.lume.common.exception.business.BusinessException;
import br.com.lume.common.exception.techinical.TechnicalException;
import br.com.lume.common.util.Status;
import br.com.lume.odonto.dao.PersistenceUnitName;
import br.com.lume.odonto.entity.Agendamento;
import br.com.lume.odonto.entity.ControleMaterial;
import br.com.lume.odonto.entity.ReservaKit;

public class ControleMaterialBO extends BO<ControleMaterial> {

    private Logger log = Logger.getLogger(ControleMaterial.class);

    private static final long serialVersionUID = 1L;

    private MaterialBO materialBO = new MaterialBO();

    public ControleMaterialBO() {
        super(PersistenceUnitName.ODONTO);
        this.setClazz(ControleMaterial.class);
    }

    @Override
    public boolean remove(ControleMaterial controleMaterial) throws BusinessException, TechnicalException {
        controleMaterial.setExcluido(Status.SIM);
        controleMaterial.setDataExclusao(Calendar.getInstance().getTime());
        controleMaterial.setExcluidoPorProfissional(ProfissionalBO.getProfissionalLogado().getId());
        materialBO.refresh(controleMaterial.getMaterial());
        controleMaterial.getMaterial().setQuantidadeAtual(controleMaterial.getMaterial().getQuantidadeAtual().add(controleMaterial.getQuantidade()));
        materialBO.persist(controleMaterial.getMaterial());
        controleMaterial.setQuantidade(new BigDecimal(0d));
        controleMaterial.setStatus(ControleMaterial.NAOUTILIZADO);

        this.persist(controleMaterial);
        return true;
    }

    @Override
    public List<ControleMaterial> listAll() throws Exception {
        return this.listByEmpresa();
    }

    public List<ControleMaterial> listByEmpresa() throws Exception {
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

    public List<ControleMaterial> listByEmpresaAndStatus() throws Exception {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("idEmpresa", ProfissionalBO.getProfissionalLogado().getIdEmpresa());
            parametros.put("o.status is null", GenericListDAO.FILTRO_GENERICO_QUERY);
            parametros.put("excluido", Status.NAO);
            parametros.put("reservaKit.status", "EN");
            return this.listByFields(parametros);
        } catch (Exception e) {
            log.error("Erro no listByEmpresa", e);
        }
        return null;
    }

    public List<ControleMaterial> listByReservaKitAndStatusEntregue(ReservaKit reservaKit) throws Exception {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("reservaKit", reservaKit);
            parametros.put("reservaKit.status", "EN");
            parametros.put("excluido", Status.NAO);
            parametros.put(" o.status is null ", GenericListDAO.FILTRO_GENERICO_QUERY);
            return this.listByFields(parametros);
        } catch (Exception e) {
            log.error("Erro no listByReservaKit", e);
        }
        return null;
    }

    public List<ControleMaterial> listByReservaKit(ReservaKit reservaKit) {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("reservaKit", reservaKit);
            parametros.put("excluido", Status.NAO);
            return this.listByFields(parametros);
        } catch (Exception e) {
            log.error("Erro no listByReservaKit", e);
        }
        return null;
    }

    public List<ControleMaterial> listByPlanoTratamentoProcedimento(Long id) throws Exception {
        try {
            String jpql = " select mi from ControleMaterial as mi, Material as m, ReservaKitAgendamentoPlanoTratamentoProcedimento as rkaptp, " + " AgendamentoPlanoTratamentoProcedimento aptp, Item as i" + " WHERE mi.material = m and m.item = i and mi.reservaKit = rkaptp.reservaKit " + " and rkaptp.agendamentoPlanoTratamentoProcedimento = aptp and aptp.planoTratamentoProcedimento.id = :id" + " and mi.idEmpresa = :empresa";
            Query q = this.getDao().createQuery(jpql);
            q.setParameter("empresa", ProfissionalBO.getProfissionalLogado().getIdEmpresa());
            q.setParameter("id", id);
            return this.list(q);
        } catch (Exception e) {
            log.error("Erro no listByProfissional", e);
        }
        return null;
    }

    public List<ControleMaterial> listParaExcluirReserva(Agendamento agendamento) throws Exception {
        try {
            StringBuffer sb = new StringBuffer();
            sb.append("SELECT ");
            sb.append("* ");
            sb.append("FROM MATERIAL_INDISPONIVEL ");
            sb.append("WHERE ID_RESERVA_KIT IN ");
            sb.append("( ");
            sb.append("   SELECT ");
            sb.append("   ID ");
            sb.append("   FROM RESERVA_KIT ");
            sb.append("   WHERE ID IN ");
            sb.append("   ( ");
            sb.append("      SELECT ");
            sb.append("      ID_RESERVA_KIT ");
            sb.append("      FROM RESERVA_KIT_AGENDAMENTO_PLANO_TRATAMENTO_PROCEDIMENTO ");
            sb.append("      WHERE ID_AGENDAMENTO_PLANO_TRATAMENTO_PROCEDIMENTO IN ");
            sb.append("      ( ");
            sb.append("         SELECT ");
            sb.append("         ID ");
            sb.append("         FROM AGENDAMENTO_PLANO_TRATAMENTO_PROCEDIMENTO ");
            sb.append("         WHERE ID_AGENDAMENTO = ?1 ");
            sb.append("      ) ");
            sb.append("   ) ");
            sb.append("   AND EXCLUIDO = 'N' ");
            sb.append(") ");
            sb.append("AND EXCLUIDO = 'N' ");
            Query query = this.getDao().createNativeQuery(sb.toString(), ControleMaterial.class);
            query.setParameter(1, agendamento.getId());
            return this.list(query);
        } catch (Exception e) {
            log.error("Erro no listParaExcluirReserva", e);
        }
        return null;
    }

}
