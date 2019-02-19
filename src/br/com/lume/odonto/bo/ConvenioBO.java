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
import br.com.lume.odonto.entity.Convenio;

public class ConvenioBO extends BO<Convenio> {

    private static final long serialVersionUID = 1L;
    private Logger log = Logger.getLogger(ConvenioBO.class);

    public ConvenioBO() {
        super(PersistenceUnitName.ODONTO);
        this.setClazz(Convenio.class);
    }

    @Override
    public boolean remove(Convenio convenio) throws BusinessException, TechnicalException {
        convenio.setExcluido(Status.SIM);
        convenio.setDataExclusao(Calendar.getInstance().getTime());
        convenio.setExcluidoPorProfissional(ProfissionalBO.getProfissionalLogado().getId());
        this.persist(convenio);
        return true;
    }

    @Override
    public List<Convenio> listAll() throws Exception {
        return this.listByEmpresa();
    }

    public List<Convenio> listByEmpresa() throws Exception {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("idEmpresa", ProfissionalBO.getProfissionalLogado().getIdEmpresa());
            parametros.put("excluido", Status.NAO);
            return this.listByFields(parametros);
        } catch (Exception e) {
            this.log.error("Erro no listByEmpresa", e);
        }
        return null;
    }
}
