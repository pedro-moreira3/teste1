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
import br.com.lume.odonto.entity.Contrato;
import br.com.lume.odonto.entity.Profissional;

public class ContratoBO extends BO<Contrato> {

    /**
     *
     */
    private static final long serialVersionUID = 494861803987228031L;
    private Logger log = Logger.getLogger(ContratoBO.class);

    public ContratoBO() {
        super(PersistenceUnitName.ODONTO);
        this.setClazz(Contrato.class);
    }

    @Override
    public boolean remove(Contrato contrato) throws BusinessException, TechnicalException {
        contrato.setExcluido(Status.SIM);
        contrato.setDataExclusao(Calendar.getInstance().getTime());
        contrato.setExcluidoPorProfissional(ProfissionalBO.getProfissionalLogado().getId());
        this.persist(contrato);
        return true;
    }

    public List<Contrato> listByProfissional(Profissional profissional) throws Exception {
        Map<String, Object> parametros = new HashMap<>();
        parametros.put("profissionalContratado.id", profissional.getId());
        parametros.put("excluido", Status.NAO);
        return this.listByFields(parametros, new String[] { "dataHora desc" });
    }
}
