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
import br.com.lume.odonto.entity.Paciente;
import br.com.lume.odonto.entity.Profissional;
import br.com.lume.odonto.entity.TermoConsentimento;

public class TermoConsentimentoBO extends BO<TermoConsentimento> {

    /**
     *
     */
    private static final long serialVersionUID = 3352115387523915966L;
    private Logger log = Logger.getLogger(TermoConsentimentoBO.class);

    public TermoConsentimentoBO() {
        super(PersistenceUnitName.ODONTO);
        this.setClazz(TermoConsentimento.class);
    }

    @Override
    public boolean remove(TermoConsentimento termoConsentimento) throws BusinessException, TechnicalException {
        termoConsentimento.setExcluido(Status.SIM);
        termoConsentimento.setDataExclusao(Calendar.getInstance().getTime());
        termoConsentimento.setExcluidoPorProfissional(ProfissionalBO.getProfissionalLogado().getId());
        this.persist(termoConsentimento);
        return true;
    }

    public List<TermoConsentimento> listByPacienteAndProfissional(Paciente paciente, Profissional profissional) {
        try {
            if (profissional != null && paciente != null) {
                String jpql = "select vo from TermoConsentimento as vo" + " WHERE vo.paciente.id = :paciente" + " AND vo.profissional.id = :profissional AND  vo.excluido= :excluido" + " ORDER BY vo.dataHora";
                Query q = this.getDao().createQuery(jpql);
                q.setParameter("paciente", paciente.getId());
                q.setParameter("profissional", profissional.getId());
                q.setParameter("excluido", Status.NAO);
                return this.list(q);
            }
        } catch (Exception e) {
            this.log.error("Erro no listByPacienteAndProfissional", e);
        }
        return null;
    }

    public List<TermoConsentimento> listByPaciente(Paciente paciente) throws Exception {

        Map<String, Object> parametros = new HashMap<>();
        parametros.put("paciente.id", paciente.getId());
        parametros.put("excluido", Status.NAO);
        return this.listByFields(parametros, new String[] { "dataHora desc" });
    }

}
