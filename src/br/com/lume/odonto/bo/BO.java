package br.com.lume.odonto.bo;

import java.io.Serializable;
import java.util.List;

import org.apache.log4j.Logger;

import br.com.lume.common.exception.business.BusinessException;
import br.com.lume.common.exception.techinical.TechnicalException;
import br.com.lume.odonto.entity.RelatorioBalanco;
import br.com.lume.odonto.exception.RemoverPlanoTratamComAgendException;

public class BO<E extends Serializable> extends br.com.lume.common.bo.BO<E> {

    /**
     *
     */
    private static final long serialVersionUID = -6001186535368103890L;
    private Logger log = Logger.getLogger(PlanoTratamentoBO.class);

    public BO(String arg0) {
        super(arg0);
    }

    protected String tratarCamposInString(List<String> strigs) {
        String out = "";
        for (String s : strigs) {
            out += ",'" + s + "'";
        }
        out = out.replaceFirst(",", "");
        return out;
    }

    @Override
    public boolean remove(E arg0) throws BusinessException, TechnicalException {
        try {
            return super.remove(arg0);
        } catch (Exception e) {
            this.tratarException(e);
            throw new TechnicalException(e);
        }
    }

    public void detach(Object o) {
        this.getDao().detachObject(o);
    }

    @Override
    public boolean persist(E arg0) throws BusinessException, TechnicalException {
        try {
            return super.persist(arg0);
        } catch (Exception e) {
            this.tratarException(e);
            throw new TechnicalException(e);
        }
    }

    @Override
    public boolean merge(E arg0) throws BusinessException, TechnicalException {
        try {
            return super.merge(arg0);
        } catch (Exception e) {
            this.tratarException(e);
            throw new TechnicalException(e);
        }
    }

    protected String tratarCamposInRelatorioBalanco(List<RelatorioBalanco> valores) {
        if (valores != null && !valores.isEmpty()) {
            String out = "";
            for (RelatorioBalanco l : valores) {
                out += ",'" + l.getId() + "'";
            }
            out = out.replaceFirst(",", "");
            return out;
        }
        return "";
    }

    private void tratarException(Exception e) throws BusinessException {
        if (e.getMessage().toUpperCase().contains(RemoverPlanoTratamComAgendException.ERRO_PREVISTO.toUpperCase())) {
            throw new RemoverPlanoTratamComAgendException(e);
        }
        throw new BusinessException(e);
    }
}
