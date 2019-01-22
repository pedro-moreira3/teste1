package br.com.lume.odonto.bo;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

import org.apache.log4j.Logger;

import br.com.lume.common.exception.business.BusinessException;
import br.com.lume.common.exception.techinical.TechnicalException;
import br.com.lume.common.util.Status;
import br.com.lume.odonto.dao.PersistenceUnitName;
import br.com.lume.odonto.entity.Afastamento;
import br.com.lume.odonto.entity.Profissional;

public class AfastamentoBO extends BO<Afastamento> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(AfastamentoBO.class);

    public AfastamentoBO() {
        super(PersistenceUnitName.ODONTO);
        this.setClazz(Afastamento.class);
    }

    @Override
    public boolean remove(Afastamento afastamento) throws BusinessException, TechnicalException {
        afastamento.setExcluido(Status.SIM);
        afastamento.setDataExclusao(Calendar.getInstance().getTime());
        afastamento.setExcluidoPorProfissional(ProfissionalBO.getProfissionalLogado().getId());
        this.persist(afastamento);
        return true;
    }

    public List<Afastamento> listByProfissional(Profissional profissional) throws Exception {
        Map<String, Object> parametros = new HashMap<>();
        parametros.put("profissional.id", profissional.getId());
        parametros.put("excluido", Status.NAO);
        return this.listByFields(parametros, new String[] { "inicio desc" });
    }

    public List<Afastamento> listByDataValidos(Date inicio, Date fim) throws Exception {
        try {
            if (inicio != null && fim != null) {

                StringBuilder sb = new StringBuilder();

                sb.append("SELECT ");
                sb.append("* ");
                sb.append("FROM AFASTAMENTO ");
                sb.append("WHERE EXCLUIDO = 'N' ");
                sb.append("AND VALIDO = 'S' ");
                sb.append("AND FIM >= 1 ");
                sb.append("ORDER BY INICIO,FIM ");
                Query query = this.getDao().createNativeQuery(sb.toString(), Afastamento.class);
                query.setParameter(1, inicio);

                return query.getResultList();
            }
        } catch (Exception e) {
            log.error("Erro no listByProfissionalAndValido", e);
        }
        return null;
    }

    public List<Afastamento> listByDataAndProfissionalValidos(Profissional profissional, Date inicio, Date fim) throws Exception {
        try {
            if (inicio != null && fim != null) {
                StringBuilder sb = new StringBuilder();

                sb.append("SELECT ");
                sb.append("* ");
                sb.append("FROM AFASTAMENTO ");
                sb.append("WHERE ID_PROFISSIONAL = ?1 ");
                sb.append("AND EXCLUIDO = 'N' ");
                sb.append("AND VALIDO = 'S' ");
                sb.append("AND FIM >= ?2 ");
                sb.append("ORDER BY INICIO,FIM ");
                Query query = this.getDao().createNativeQuery(sb.toString(), Afastamento.class);
                query.setParameter(1, profissional.getId());
                query.setParameter(2, inicio);
                return query.getResultList();
            }
        } catch (Exception e) {
            log.error("Erro no listByProfissionalAndValido", e);
        }
        return null;
    }

    public List<Afastamento> listAdm() throws Exception {
        Map<String, Object> parametros = new HashMap<>();
        parametros.put("profissional.idEmpresa", ProfissionalBO.getProfissionalLogado().getIdEmpresa());
        parametros.put("excluido", Status.NAO);
        return this.listByFields(parametros, null);
    }

}
