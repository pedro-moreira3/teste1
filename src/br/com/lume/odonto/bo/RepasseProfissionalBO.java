package br.com.lume.odonto.bo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import br.com.lume.odonto.dao.PersistenceUnitName;
import br.com.lume.odonto.entity.RepasseItem;
import br.com.lume.odonto.entity.RepasseProfissional;

public class RepasseProfissionalBO extends BO<RepasseProfissional> {

    private Logger log = Logger.getLogger(RepasseProfissionalBO.class);

    private static final long serialVersionUID = 1L;

    public RepasseProfissionalBO() {
        super(PersistenceUnitName.ODONTO);
        this.setClazz(RepasseProfissional.class);
    }

    public List<RepasseProfissional> listByProfissional(Long idProfissional) throws Exception {
        Map<String, Object> parametros = new HashMap<>();
        parametros.put("profissional.id", idProfissional);
        return this.listByFields(parametros, new String[] { "data desc" });
    }

    public boolean existeRepasseEmAberto(Long idProfissional) throws Exception {
        Map<String, Object> parametros = new HashMap<>();
        parametros.put("profissional.id", idProfissional);
        parametros.put("status", RepasseItem.REPASSADO);
        List<RepasseProfissional> listByFields = this.listByFields(parametros, new String[] { "data desc" });
        return listByFields != null && !listByFields.isEmpty();
    }
}
