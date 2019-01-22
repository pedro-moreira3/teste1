package br.com.lume.security.bo;

import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.apache.log4j.Logger;
import org.primefaces.model.chart.PieChartModel;

import br.com.lume.common.bo.BO;
import br.com.lume.common.connection.GenericDAO;
import br.com.lume.security.entity.LogAcesso;
import br.com.lume.security.entity.LogAcessoSumarizado;
import br.com.lume.security.entity.Usuario;

public class LogAcessoBO extends BO<LogAcesso> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(LogAcessoBO.class);

    public LogAcessoBO() {
        super(GenericDAO.LUME_SECURITY);
        this.setClazz(LogAcesso.class);
    }

    public List<LogAcesso> listarPorSistemaEmpresaUsuarioPeriodo(long idSistema, long idEmpresa, long idUsuario, Date dataIni, Date dataFinal) {
        List<LogAcesso> logs = null;
        try {
            String jpql = "select o from LogAcesso o where ";
            if (idSistema > 0) {
                jpql += "and o.objeto.sistema.sisIntCod = :idSistema ";
            }
            if (idUsuario > 0) {
                jpql += "and o.usuario.usuIntCod = :idUsuario ";
            } else if (idEmpresa > 0) {
                jpql += "and o.usuario.empresa.empIntCod = :idEmpresa ";
            }
            if (dataIni != null) {
                jpql += "and o.logDtmEntrada >= :dataIni ";
            }
            if (dataFinal != null) {
                jpql += "and o.logDtmEntrada <= :dataFinal ";
            }
            jpql += "order by o.logDtmEntrada";
            jpql = jpql.replaceFirst("and", "");
            Query query = this.getDao().createQuery(jpql.toString());
            if (idSistema > 0) {
                query.setParameter("idSistema", idSistema);
            }
            if (idUsuario > 0) {
                query.setParameter("idUsuario", idUsuario);
            } else if (idEmpresa > 0) {
                query.setParameter("idEmpresa", idEmpresa);
            }
            if (dataIni != null) {
                query.setParameter("dataIni", dataIni);
            }
            if (dataFinal != null) {
                query.setParameter("dataFinal", dataFinal);
            }
            logs = this.list(query);
        } catch (Exception e) {
            this.log.error("Erro no listarPorSistemaEmpresaUsuarioPeriodo", e);
        }
        return logs;
    }

    public List<Usuario> listUsuariosByEmpresa(long idEmpresa) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT USU.* ");
            sb.append("FROM SEG_USUARIO USU ");
            sb.append("JOIN PROFISSIONAL PRO ON PRO.ID_USUARIO = USU.USU_INT_COD ");
            sb.append("JOIN SEG_EMPRESA EMP ON EMP.EMP_INT_COD = PRO.ID_EMPRESA ");
            sb.append("WHERE PRO.ID_EMPRESA = ?1");

            Query query = this.getDao().createNativeQuery(sb.toString(), Usuario.class);
            query.setParameter(1, idEmpresa);
            return query.getResultList();
        } catch (Exception e) {
            this.log.error("Erro no listUsuariosByEmpresa", e);
        }
        return null;
    }

    public List<LogAcesso> listByEmpresaAndData(Long idEmpresa, Long idUsuario, Date dataIni, Date dataFinal) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT LOG.* ");
            sb.append("FROM SEG_LOGACESSO LOG ");
            sb.append("JOIN SEG_USUARIO USU ON USU.USU_INT_COD = LOG.USU_INT_COD ");
            sb.append("JOIN PROFISSIONAL PRO ON PRO.ID_USUARIO = USU.USU_INT_COD  ");
            sb.append("WHERE PRO.ID_EMPRESA = ?1 AND ");
            if (idUsuario != null) {
                sb.append("PRO.ID_USUARIO = " + idUsuario.toString() + " AND ");
            }
            sb.append("LOG.LOG_DTM_ENTRADA BETWEEN ?2 AND ?3");
            Query query = this.getDao().createNativeQuery(sb.toString(), LogAcesso.class);
            query.setParameter(1, idEmpresa);
            query.setParameter(2, dataIni);
            query.setParameter(3, dataFinal);
            return query.getResultList();
        } catch (Exception e) {
            this.log.error("Erro no listByEmpresaAndData", e);
        }
        return null;
    }

    public void doPieGraficoAcessos(PieChartModel model, long idEmpresa, Date dataIni, Date dataFinal) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT ");
        sb.append("OBJ.OBJ_STR_DES, COUNT(OBJ.OBJ_STR_DES) AS TOTAL ");
        sb.append("FROM SEG_LOGACESSO LOG, SEG_OBJETO OBJ, SEG_USUARIO USU, PROFISSIONAL PROF ");
        sb.append("WHERE ");
        sb.append("LOG.OBJ_INT_COD = OBJ.OBJ_INT_COD AND ");
        sb.append("LOG.USU_INT_COD = USU.USU_INT_COD AND ");
        sb.append("USU.USU_INT_COD = PROF.ID_USUARIO AND ");
        sb.append("PROF.ID_EMPRESA = ?1 AND ");
        sb.append("LOG.LOG_DTM_ENTRADA BETWEEN ?2 AND ?3 ");
        sb.append("GROUP BY OBJ.OBJ_STR_DES ");
        sb.append("ORDER BY TOTAL DESC ");
        sb.append("FETCH FIRST 10 ROW ONLY");

        Query query = this.getDao().createNativeQuery(sb.toString());
        query.setParameter(1, idEmpresa);
        query.setParameter(2, dataIni);
        query.setParameter(3, dataFinal);

        List<Object[]> resultList = query.getResultList();

        for (Object object[] : resultList) {
            model.set("" + object[0], Integer.parseInt("" + object[1]));
        }
    }

    public List<LogAcessoSumarizado> listLogAcessoSumarizado(long idEmpresa, Date dataIni, Date dataFinal) throws Exception {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT ");
            sb.append("ROW_NUMBER() OVER () * EXTRACT(MICROSECONDS FROM CURRENT_TIMESTAMP) AS ID, ");
            sb.append("OBJ.OBJ_STR_DES as objNome, COUNT(OBJ.OBJ_STR_DES) as qtdAcessos ");
            sb.append("FROM SEG_LOGACESSO LOG, SEG_OBJETO OBJ, SEG_USUARIO USU, PROFISSIONAL PROF ");
            sb.append("WHERE ");
            sb.append("LOG.OBJ_INT_COD = OBJ.OBJ_INT_COD AND ");
            sb.append("LOG.USU_INT_COD = USU.USU_INT_COD AND ");
            sb.append("USU.USU_INT_COD = PROF.ID_USUARIO AND ");
            sb.append("PROF.ID_EMPRESA = ?1 AND ");
            sb.append("LOG.LOG_DTM_ENTRADA BETWEEN ?2 AND ?3 ");
            sb.append("GROUP BY OBJ.OBJ_STR_DES ");
            sb.append("ORDER BY qtdAcessos DESC ");

            Query query = this.getDao().createNativeQuery(sb.toString(), LogAcessoSumarizado.class);
            query.setParameter(1, idEmpresa);
            query.setParameter(2, dataIni);
            query.setParameter(3, dataFinal);

            return query.getResultList();

        } catch (Exception e) {
            this.log.error("Erro no listLogAcessoSumarizado", e);
        }
        return null;

    }

}
