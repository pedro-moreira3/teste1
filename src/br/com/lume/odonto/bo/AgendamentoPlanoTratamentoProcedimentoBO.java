package br.com.lume.odonto.bo;

import java.text.SimpleDateFormat;
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
import br.com.lume.odonto.entity.Agendamento;
import br.com.lume.odonto.entity.AgendamentoPlanoTratamentoProcedimento;
import br.com.lume.odonto.entity.PlanoTratamentoProcedimento;
import br.com.lume.odonto.entity.Profissional;

public class AgendamentoPlanoTratamentoProcedimentoBO extends BO<AgendamentoPlanoTratamentoProcedimento> {

    private Logger log = Logger.getLogger(AgendamentoPlanoTratamentoProcedimento.class);

    private static final long serialVersionUID = 1L;

    public AgendamentoPlanoTratamentoProcedimentoBO() {
        super(PersistenceUnitName.ODONTO);
        this.setClazz(AgendamentoPlanoTratamentoProcedimento.class);
    }

    @Override
    public List<AgendamentoPlanoTratamentoProcedimento> listAll() throws Exception {
        return this.listByEmpresa();
    }

    @Override
    public boolean remove(AgendamentoPlanoTratamentoProcedimento AgendamentoPlanoTratamentoProcedimento) throws BusinessException, TechnicalException {
        AgendamentoPlanoTratamentoProcedimento.setExcluido(Status.SIM);
        AgendamentoPlanoTratamentoProcedimento.setDataExclusao(Calendar.getInstance().getTime());
        AgendamentoPlanoTratamentoProcedimento.setExcluidoPorProfissional(ProfissionalBO.getProfissionalLogado().getId());
        this.persist(AgendamentoPlanoTratamentoProcedimento);
        return true;
    }

    public List<AgendamentoPlanoTratamentoProcedimento> listRelatorioResultado(Date inicio, Date fim, Profissional profissional) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        StringBuilder sb = new StringBuilder();
        sb.append("SELECT A_PTP.* ");
        sb.append("FROM ");
        sb.append("AGENDAMENTO_PLANO_TRATAMENTO_PROCEDIMENTO A_PTP,AGENDAMENTO AGE, PROFISSIONAL P ");
        sb.append("WHERE ");
        sb.append("A_PTP.ID_AGENDAMENTO = AGE.ID ");
        sb.append("AND A_PTP.EXCLUIDO = 'N' ");
        sb.append("AND AGE.DATA_AGENDAMENTO::DATE BETWEEN '" + sdf.format(inicio) + "' AND '" + sdf.format(fim) + "' ");
        sb.append("AND AGE.ID_DENTISTA = P.ID ");
        sb.append("AND P.ID_EMPRESA = ?1 ");
        if (profissional != null) {
            sb.append("AND AGE.ID_DENTISTA = " + profissional.getId() + " ");
        }

        Query query = this.getDao().createNativeQuery(sb.toString(), AgendamentoPlanoTratamentoProcedimento.class);
        query.setParameter(1, ProfissionalBO.getProfissionalLogado().getIdEmpresa());
        return (query.getResultList());
    }

    @Override
    public AgendamentoPlanoTratamentoProcedimento find(AgendamentoPlanoTratamentoProcedimento aptp) {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("planoTratamentoProcedimento", aptp.getPlanoTratamentoProcedimento());
            parametros.put("agendamento", aptp.getAgendamento());
            return this.findByFields(parametros);
        } catch (Exception e) {
            log.error("Erro no hasAgendamentoPlanoTratamentoProcedimento", e);
        }
        return null;
    }

    public List<AgendamentoPlanoTratamentoProcedimento> listByEmpresa() throws Exception {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("excluido", Status.NAO);
            return this.listByFields(parametros);
        } catch (Exception e) {
            log.error("Erro no listByEmpresa", e);
        }
        return null;
    }

    public List<AgendamentoPlanoTratamentoProcedimento> listByPlanoTratamentoProcedimento(List<PlanoTratamentoProcedimento> ptps) throws Exception {
        if (ptps != null && !ptps.isEmpty()) {
            String jpql = "select a from AgendamentoPlanoTratamentoProcedimento as a" + " WHERE  a.planoTratamentoProcedimento in :ptps AND  " + "  a.agendamento.excluido= :excluido AND a.agendamento.status in('P','S','N','E','I','O')";
            Query q = this.getDao().createQuery(jpql);
            q.setParameter("ptps", ptps);
            q.setParameter("excluido", Status.NAO);
            return this.list(q);
        }
        return null;
    }

    public List<AgendamentoPlanoTratamentoProcedimento> listByPlanoTratamentoProcedimento(Long id) throws Exception {
        String jpql = "select a from AgendamentoPlanoTratamentoProcedimento as a" + " WHERE  a.planoTratamentoProcedimento.id = :id ";
        Query q = this.getDao().createQuery(jpql);
        q.setParameter("id", id);
        q.setParameter("excluido", Status.NAO);
        return this.list(q);
    }

    public List<AgendamentoPlanoTratamentoProcedimento> listByPlanoTratamentoProcedimento(PlanoTratamentoProcedimento ptps) throws Exception {
        if (ptps != null) {
            String jpql = "select a from AgendamentoPlanoTratamentoProcedimento as a" + " WHERE  a.planoTratamentoProcedimento = :ptps AND  " + "  a.excluido= :excluido AND a.agendamento.status in('P','S','N','E','A','I','O')";
            Query q = this.getDao().createQuery(jpql);
            q.setParameter("ptps", ptps);
            q.setParameter("excluido", Status.NAO);
            return this.list(q);
        }
        return null;
    }

    public List<AgendamentoPlanoTratamentoProcedimento> listByAgendamento(Agendamento agendamento) {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("excluido", Status.NAO);
            parametros.put("agendamento", agendamento);
            return this.listByFields(parametros);
        } catch (Exception e) {
            log.error("Erro no listByEmpresa", e);
        }
        return null;
    }
}
