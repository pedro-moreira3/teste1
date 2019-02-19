package br.com.lume.odonto.bo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import br.com.lume.odonto.dao.PersistenceUnitName;
import br.com.lume.odonto.entity.DiagnosticoOrtodontico;

public class DiagnosticoOrtodonticoBO extends BO<DiagnosticoOrtodontico> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(DiagnosticoOrtodonticoBO.class);

    public DiagnosticoOrtodonticoBO() {
        super(PersistenceUnitName.ODONTO);
        this.setClazz(DiagnosticoOrtodontico.class);
    }

    @Override
    public List<DiagnosticoOrtodontico> listAll() throws Exception {
        Map<String, Object> parametros = new HashMap<>();
        return this.listByFields(parametros, new String[] { "descricao asc" });
    }
}
