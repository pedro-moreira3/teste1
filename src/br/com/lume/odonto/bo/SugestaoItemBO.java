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
import br.com.lume.odonto.entity.Profissional;
import br.com.lume.odonto.entity.SugestaoItem;

public class SugestaoItemBO extends BO<SugestaoItem> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(SugestaoItemBO.class);

    public SugestaoItemBO() {
        super(PersistenceUnitName.ODONTO);
        this.setClazz(SugestaoItem.class);
    }

    @Override
    public boolean remove(SugestaoItem sugestaoItem) throws BusinessException, TechnicalException {
        sugestaoItem.setExcluido(Status.SIM);
        sugestaoItem.setDataExclusao(Calendar.getInstance().getTime());
        sugestaoItem.setExcluidoPorProfissional(ProfissionalBO.getProfissionalLogado().getId());
        this.persist(sugestaoItem);
        return true;
    }

    public List<SugestaoItem> listByProfissional(Profissional profissional) throws Exception {
        Map<String, Object> parametros = new HashMap<>();
        parametros.put("profissional.id", profissional.getId());
        parametros.put("excluido", Status.NAO);
        return this.listByFields(parametros);
    }
}
