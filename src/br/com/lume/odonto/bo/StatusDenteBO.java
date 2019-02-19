package br.com.lume.odonto.bo;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.persistence.Query;

import org.apache.log4j.Logger;

import br.com.lume.common.exception.business.BusinessException;
import br.com.lume.common.exception.techinical.TechnicalException;
import br.com.lume.common.util.Status;
import br.com.lume.odonto.dao.PersistenceUnitName;
import br.com.lume.odonto.entity.StatusDente;
import br.com.lume.security.bo.EmpresaBO;
import br.com.lume.security.entity.Empresa;

public class StatusDenteBO extends BO<StatusDente> {

    /**
     *
     */
    private static final long serialVersionUID = 3355154353249940703L;
    private Logger log = Logger.getLogger(StatusDente.class);

    public StatusDenteBO() {
        super(PersistenceUnitName.ODONTO);
        this.setClazz(StatusDente.class);
    }

    public List<StatusDente> listSemLimpar() throws Exception {
        try {
            String jpql = "select p from StatusDente p where p.excluido='N'";
            Query q = this.getDao().createQuery(jpql);
            List<StatusDente> status = this.list(q);
            return status;
        } catch (Exception e) {
            log.error("Erro no listSemLimpar", e);
        }
        return null;
    }

    @Override
    public boolean remove(StatusDente statusDente) throws BusinessException, TechnicalException {
        statusDente.setExcluido(Status.SIM);
        statusDente.setDataExclusao(Calendar.getInstance().getTime());
        statusDente.setExcluidoPorProfissional(ProfissionalBO.getProfissionalLogado().getId());
        this.persist(statusDente);
        return true;
    }

    public List<StatusDente> listAllTemplate() {
        try {
            long idEmpresaTemplate = Empresa.ID_EMPRESA_TEMPLATE;
            long idEmpresaLogada = EmpresaBO.getEmpresaLogada().getEmpIntCod();
            String jpql = "select p from StatusDente p where p.excluido='N' and (p.idEmpresa=" + idEmpresaTemplate + " or p.idEmpresa=" + idEmpresaLogada + ") order by p.descricao";
            Query q = this.getDao().createQuery(jpql);
            List<StatusDente> status = this.list(q);
            return status;
        } catch (Exception e) {
            log.error("Erro no listAllTemplate", e);
        }
        return null;
    }

    public List<StatusDente> listByEmpresa() {
        try {
            String jpql = "select p from StatusDente p where p.excluido='N' and p.idEmpresa=" + EmpresaBO.getEmpresaLogada().getEmpIntCod();
            Query q = this.getDao().createQuery(jpql);
            List<StatusDente> status = this.list(q);
            return status;
        } catch (Exception e) {
            log.error("Erro no listByEmpresa", e);
        }
        return new ArrayList<>();
    }
}
