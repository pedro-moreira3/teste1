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
import br.com.lume.odonto.entity.Odontograma;
import br.com.lume.odonto.entity.Paciente;

public class OdontogramaBO extends BO<Odontograma> {

    /**
     *
     */
    private static final long serialVersionUID = 3570441204697690821L;
    private Logger log = Logger.getLogger(Odontograma.class);

    public OdontogramaBO() {
        super(PersistenceUnitName.ODONTO);
        this.setClazz(Odontograma.class);
    }

    @Override
    public boolean remove(Odontograma odontograma) throws BusinessException, TechnicalException {
        odontograma.setExcluido(Status.SIM);
        odontograma.setDataExclusao(Calendar.getInstance().getTime());
        odontograma.setExcluidoPorProfissional(ProfissionalBO.getProfissionalLogado().getId());
        this.persist(odontograma);
        return true;
    }

    public List<Odontograma> listByPaciente(Paciente paciente) throws Exception {
        if (paciente != null) {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("paciente.id", paciente.getId());
            parametros.put("excluido", Status.NAO);
            return this.listByFields(parametros, new String[] { "dataCadastro DESC" });
        }
        return null;
    }
}
