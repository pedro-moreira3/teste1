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
import br.com.lume.odonto.entity.Evolucao;
import br.com.lume.odonto.entity.Paciente;
import br.com.lume.odonto.entity.PlanoTratamentoProcedimento;

public class EvolucaoBO extends BO<Evolucao> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(EvolucaoBO.class);

    public EvolucaoBO() {
        super(PersistenceUnitName.ODONTO);
        this.setClazz(Evolucao.class);
    }

    @Override
    public boolean remove(Evolucao evolucao) throws BusinessException, TechnicalException {
        evolucao.setExcluido(Status.SIM);
        evolucao.setDataExclusao(Calendar.getInstance().getTime());
        evolucao.setExcluidoPorProfissional(ProfissionalBO.getProfissionalLogado().getId());
        this.persist(evolucao);
        return true;
    }

    @Override
    public List<Evolucao> listAll() throws Exception {
        return this.listByEmpresa();
    }

    @Override
    public boolean persist(Evolucao evolucao) throws BusinessException, TechnicalException {
        evolucao.setProfissional(ProfissionalBO.getProfissionalLogado());
        return super.persist(evolucao);
    }

    public List<Evolucao> listByEmpresa() throws Exception {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("planoTratamentoProcedimento.planoTratamento.profissional.idEmpresa", ProfissionalBO.getProfissionalLogado().getIdEmpresa());
            parametros.put("excluido", Status.NAO);
            return this.listByFields(parametros);
        } catch (Exception e) {
            this.log.error("Erro no listByEmpresa", e);
        }
        return null;
    }

    public List<Evolucao> listByPlanosTratamentoProcedimentos(List<PlanoTratamentoProcedimento> ptps) throws Exception {
        try {
            if (ptps.size() <= 0) {
                ptps = null;
            }
            String jpql = "select e from Evolucao as e WHERE e.planoTratamentoProcedimento in :planoTratamentoProcedimento  AND  e.excluido= :excluido";
            Query q = this.getDao().createQuery(jpql);
            q.setParameter("planoTratamentoProcedimento", ptps);
            q.setParameter("excluido", Status.NAO);
            return this.list(q);
        } catch (Exception e) {
            this.log.error("Erro no getByPlanoTratamento", e);
        }
        return null;
    }

    public List<Evolucao> listByPaciente(Paciente paciente) {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("paciente", paciente);
//            parametros.put("excluido", Status.NAO);
            return this.listByFields(parametros, new String[] { "data desc" });
        } catch (Exception e) {
            this.log.error("Erro no listByEmpresa", e);
        }
        return null;
    }
}
