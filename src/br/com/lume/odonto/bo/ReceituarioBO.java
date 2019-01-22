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
import br.com.lume.odonto.entity.Paciente;
import br.com.lume.odonto.entity.Receituario;

public class ReceituarioBO extends BO<Receituario> {

    /**
     *
     */
    private static final long serialVersionUID = 6620122986198234217L;
    private Logger log = Logger.getLogger(ReceituarioBO.class);

    public ReceituarioBO() {
        super(PersistenceUnitName.ODONTO);
        this.setClazz(Receituario.class);
    }

    @Override
    public boolean remove(Receituario receituario) throws BusinessException, TechnicalException {
        receituario.setExcluido(Status.SIM);
        receituario.setDataExclusao(Calendar.getInstance().getTime());
        receituario.setExcluidoPorProfissional(ProfissionalBO.getProfissionalLogado().getId());
        this.persist(receituario);
        return true;
    }

    public List<Receituario> listByPaciente(Paciente paciente) throws Exception {
        Map<String, Object> parametros = new HashMap<>();
        parametros.put("paciente.id", paciente.getId());
        parametros.put("excluido", Status.NAO);
        return this.listByFields(parametros, new String[] { "dataHora desc" });
    }
}
