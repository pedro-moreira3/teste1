package br.com.lume.odonto.bo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import br.com.lume.odonto.dao.PersistenceUnitName;
import br.com.lume.odonto.entity.AparelhoOrtodontico;

public class AparelhoOrtodonticoBO extends BO<AparelhoOrtodontico> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(AparelhoOrtodonticoBO.class);

    public AparelhoOrtodonticoBO() {
        super(PersistenceUnitName.ODONTO);
        this.setClazz(AparelhoOrtodontico.class);
    }

    @Override
    public List<AparelhoOrtodontico> listAll() throws Exception {
        Map<String, Object> parametros = new HashMap<>();
        return this.listByFields(parametros, new String[] { "descricao asc" });
    }
}
