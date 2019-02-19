package br.com.lume.odonto.bo;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import br.com.lume.odonto.dao.PersistenceUnitName;
import br.com.lume.odonto.entity.Profissional;
import br.com.lume.odonto.entity.ProfissionalPonto;

public class ProfissionalPontoBO extends BO<ProfissionalPonto> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(ProfissionalPontoBO.class);

    public ProfissionalPontoBO() {
        super(PersistenceUnitName.ODONTO);
        this.setClazz(ProfissionalPonto.class);
    }

    public List<ProfissionalPonto> listPontosByProfissional(Profissional profissional) throws Exception {
        Map<String, Object> parametros = new HashMap<>();
        parametros.put("profissional", profissional);
        return this.listByFields(parametros, new String[] { "data desc" });
    }

    public ProfissionalPonto findByData(Profissional profissional, Date data) throws Exception {
        Map<String, Object> parametros = new HashMap<>();
        parametros.put("profissional", profissional);
        parametros.put("data", data);
        List<ProfissionalPonto> pontos = this.listByFields(parametros, new String[] { "data desc" });
        return pontos != null && !pontos.isEmpty() ? pontos.get(0) : new ProfissionalPonto();
    }
}
