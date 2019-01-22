package br.com.lume.odonto.bo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import br.com.lume.odonto.dao.PersistenceUnitName;
import br.com.lume.odonto.entity.Material;
import br.com.lume.odonto.entity.MaterialLog;

public class MaterialLogBO extends BO<MaterialLog> {

    private Logger log = Logger.getLogger(MaterialLogBO.class);

    private static final long serialVersionUID = 1L;

    public MaterialLogBO() {
        super(PersistenceUnitName.ODONTO);
        this.setClazz(MaterialLog.class);
    }

    public List<MaterialLog> listByMaterial(Material material) throws Exception {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("material", material);
            return this.listByFields(parametros);
        } catch (Exception e) {
            log.error("Erro no listByMaterial", e);
        }
        return null;
    }
}
