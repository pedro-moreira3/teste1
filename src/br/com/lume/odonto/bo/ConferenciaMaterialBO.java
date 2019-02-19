package br.com.lume.odonto.bo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import br.com.lume.odonto.dao.PersistenceUnitName;
import br.com.lume.odonto.entity.Conferencia;
import br.com.lume.odonto.entity.ConferenciaMaterial;

public class ConferenciaMaterialBO extends BO<ConferenciaMaterial> {

    private Logger log = Logger.getLogger(ConferenciaMaterial.class);

    private static final long serialVersionUID = 1L;

    public ConferenciaMaterialBO() {
        super(PersistenceUnitName.ODONTO);
        this.setClazz(ConferenciaMaterial.class);
    }

    @Override
    public List<ConferenciaMaterial> listAll() throws Exception {
        return this.listByEmpresa();
    }

    public List<ConferenciaMaterial> listByEmpresa() throws Exception {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("idEmpresa", ProfissionalBO.getProfissionalLogado().getIdEmpresa());
            return this.listByFields(parametros);
        } catch (Exception e) {
            this.log.error("Erro no listByEmpresa", e);
        }
        return null;
    }

    public List<ConferenciaMaterial> listByConferencia(Conferencia conferencia) throws Exception {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("idEmpresa", ProfissionalBO.getProfissionalLogado().getIdEmpresa());
            parametros.put("conferencia", conferencia);
            return this.listByFields(parametros);
        } catch (Exception e) {
            this.log.error("Erro no listByEmpresa", e);
        }
        return null;
    }

}
