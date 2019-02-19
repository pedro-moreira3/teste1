package br.com.lume.odonto.bo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import br.com.lume.odonto.dao.PersistenceUnitName;
import br.com.lume.odonto.entity.RepasseItem;
import br.com.lume.odonto.entity.RepasseProfissional;

public class RepasseItemBO extends BO<RepasseItem> {

    private Logger log = Logger.getLogger(RepasseItemBO.class);

    private static final long serialVersionUID = 1L;

    public RepasseItemBO() {
        super(PersistenceUnitName.ODONTO);
        this.setClazz(RepasseItem.class);
    }

    public List<RepasseItem> listByRepasseProfissional(RepasseProfissional repasse) throws Exception {
        Map<String, Object> parametros = new HashMap<>();
        parametros.put("repasse", repasse);
        return this.listByFields(parametros, new String[] { "planoTratamentoProcedimento.dataFinalizado desc" });
    }

}
