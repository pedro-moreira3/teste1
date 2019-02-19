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
import br.com.lume.odonto.entity.Atestado;
import br.com.lume.odonto.entity.Paciente;

public class AtestadoBO extends BO<Atestado> {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(AtestadoBO.class);

    public AtestadoBO() {
        super(PersistenceUnitName.ODONTO);
        this.setClazz(Atestado.class);
    }

    @Override
    public boolean remove(Atestado atestado) throws BusinessException, TechnicalException {
        atestado.setExcluido(Status.SIM);
        atestado.setDataExclusao(Calendar.getInstance().getTime());
        atestado.setExcluidoPorProfissional(ProfissionalBO.getProfissionalLogado().getId());
        this.persist(atestado);
        return true;
    }

    public List<Atestado> listByPaciente(Paciente paciente) throws Exception {
        Map<String, Object> parametros = new HashMap<>();
        parametros.put("paciente.id", paciente.getId());
        parametros.put("excluido", Status.NAO);
        return this.listByFields(parametros, new String[] { "dataHora desc" });
    }
}
