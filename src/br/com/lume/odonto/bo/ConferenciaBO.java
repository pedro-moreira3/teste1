package br.com.lume.odonto.bo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import br.com.lume.odonto.dao.PersistenceUnitName;
import br.com.lume.odonto.entity.Conferencia;

public class ConferenciaBO extends BO<Conferencia> {

    private Logger log = Logger.getLogger(Conferencia.class);

    private static final long serialVersionUID = 1L;

    public ConferenciaBO() {
        super(PersistenceUnitName.ODONTO);
        this.setClazz(Conferencia.class);
    }

    @Override
    public List<Conferencia> listAll() throws Exception {
        return this.listByEmpresa();
    }

    public List<Conferencia> listByEmpresa() throws Exception {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("idEmpresa", ProfissionalBO.getProfissionalLogado().getIdEmpresa());
            return this.listByFields(parametros, new String[] { "data desc" });
        } catch (Exception e) {
            this.log.error("Erro no listByEmpresa", e);
        }
        return null;
    }

}
