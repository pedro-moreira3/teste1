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
import br.com.lume.common.util.Utils;
import br.com.lume.odonto.dao.PersistenceUnitName;
import br.com.lume.odonto.entity.PlanoTratamento;
import br.com.lume.odonto.entity.Retorno;

public class RetornoBO extends BO<Retorno> {

    private Logger log = Logger.getLogger(Retorno.class);

    private static final long serialVersionUID = 1L;

    public RetornoBO() {
        super(PersistenceUnitName.ODONTO);
        this.setClazz(Retorno.class);
    }

    @Override
    public boolean remove(Retorno entity) throws BusinessException, TechnicalException {
        entity.setExcluido(Status.SIM);
        entity.setDataExclusao(Calendar.getInstance().getTime());
        entity.setExcluidoPorProfissional(ProfissionalBO.getProfissionalLogado().getId());
        this.persist(entity);
        return true;
    }

    public List<Retorno> listByPlano(PlanoTratamento pt) throws Exception {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("planoTratamento", pt);
            parametros.put("excluido", Status.NAO);
            return this.listByFields(parametros);
        } catch (Exception e) {
            this.log.error("Erro no listByPlano", e);
        }
        return null;
    }

    public List<Retorno> listRetornoHoje() throws Exception {
        try {
            StringBuffer sb = new StringBuffer();
            sb.append("SELECT ");
            sb.append("RET.* ");
            sb.append("FROM RETORNO RET,PACIENTE P ");
            sb.append("WHERE RET.ID_PACIENTE = P.ID ");
            sb.append("AND P.ID_EMPRESA = ?1 ");
            sb.append("AND RET.DATA_RETORNO = CURRENT_DATE ");
            Query query = this.getDao().createNativeQuery(sb.toString(), Retorno.class);
            query.setParameter(1, ProfissionalBO.getProfissionalLogado().getIdEmpresa());
            return this.list(query);
        } catch (Exception e) {
            this.log.error("Erro no listByPlano", e);
        }
        return null;
    }

    public List<Retorno> listByDate(Date dataIni, Date dataFim) throws Exception {
        try {
            StringBuffer sb = new StringBuffer();
            sb.append("SELECT ");
            sb.append("RET.* ");
            sb.append("FROM RETORNO RET,PACIENTE P ");
            sb.append("WHERE RET.ID_PACIENTE = P.ID ");
            sb.append("AND RET.EXCLUIDO = ?1 ");
            sb.append("AND P.ID_EMPRESA = ?2 ");
            sb.append("AND DATE(RET.DATA_RETORNO) BETWEEN '" + Utils.dateToString(dataIni, "yyyy-MM-dd") + "' AND '" + Utils.dateToString(dataFim, "yyyy-MM-dd") + "' ORDER BY RET.DATA_RETORNO ASC ");
            Query query = this.getDao().createNativeQuery(sb.toString(), Retorno.class);
            query.setParameter(1, Status.NAO);
            query.setParameter(2, ProfissionalBO.getProfissionalLogado().getIdEmpresa());
            return this.list(query);
        } catch (Exception e) {
            this.log.error("Erro no listByPlano", e);
        }
        return null;
    }
}
