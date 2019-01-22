package br.com.lume.odonto.bo;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import br.com.lume.common.exception.business.BusinessException;
import br.com.lume.common.exception.techinical.TechnicalException;
import br.com.lume.common.util.Status;
import br.com.lume.odonto.dao.PersistenceUnitName;
import br.com.lume.odonto.entity.Exame;
import br.com.lume.odonto.entity.Paciente;

public class ExameBO extends BO<Exame> {

    /**
     *
     */
    private static final long serialVersionUID = -9041186698923437093L;
    private Logger log = Logger.getLogger(ExameBO.class);

    public ExameBO() {
        super(PersistenceUnitName.ODONTO);
        this.setClazz(Exame.class);
    }

    @Override
    public boolean remove(Exame exame) throws BusinessException, TechnicalException {
        exame.setExcluido(Status.SIM);
        exame.setDataExclusao(Calendar.getInstance().getTime());
        exame.setExcluidoPorProfissional(ProfissionalBO.getProfissionalLogado().getId());
        this.persist(exame);
        return true;
    }

    public List<Exame> listByPaciente(Paciente paciente) {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("paciente.id", paciente.getId());
            parametros.put("excluido", Status.NAO);
            return this.listByFields(parametros, new String[] { "dataHora desc" });
        } catch (Exception e) {
            this.log.error("Erro no listByPaciente", e);
        }
        return null;
    }
}
