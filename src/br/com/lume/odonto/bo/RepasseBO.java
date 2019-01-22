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
import br.com.lume.odonto.entity.Repasse;

public class RepasseBO extends BO<Repasse> {

    private Logger log = Logger.getLogger(Repasse.class);

    private static final long serialVersionUID = 1L;

    public RepasseBO() {
        super(PersistenceUnitName.ODONTO);
        this.setClazz(Repasse.class);
    }

    @Override
    public List<Repasse> listAll() throws Exception {
        return this.listByEmpresa();
    }

    @Override
    public boolean remove(Repasse repasse) throws BusinessException, TechnicalException {
        repasse.setExcluido(Status.SIM);
        repasse.setDataExclusao(Calendar.getInstance().getTime());
        repasse.setExcluidoPorProfissional(ProfissionalBO.getProfissionalLogado().getId());
        this.persist(repasse);
        return true;
    }

    public List<Repasse> listByEmpresa() throws Exception {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("excluido", Status.NAO);
            return this.listByFields(parametros);
        } catch (Exception e) {
            this.log.error("Erro no listByEmpresa", e);
        }
        return null;
    }
}
