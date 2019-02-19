package br.com.lume.odonto.bo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import br.com.lume.common.connection.GenericDAO;
import br.com.lume.odonto.dao.PersistenceUnitName;
import br.com.lume.odonto.entity.ObjetoProfissional;
import br.com.lume.odonto.entity.Profissional;
import br.com.lume.security.bo.EmpresaBO;
import br.com.lume.security.bo.ObjetoBO;
import br.com.lume.security.entity.Empresa;

public class ObjetoProfissionalBO extends BO<ObjetoProfissional> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(ObjetoProfissionalBO.class);

    public ObjetoProfissionalBO() {
        super(PersistenceUnitName.ODONTO);
        this.setClazz(ObjetoProfissional.class);
    }

    public List<ObjetoProfissional> listByProfissional(Profissional profissional) throws Exception {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("profissional", profissional);
            if (Empresa.ESTOQUE_SIMPLIFICADO.equals(EmpresaBO.getEmpresaLogada().getEmpStrEstoque())) {
                parametros.put("o.objeto.caminho not in (" + ObjetoBO.OBJETOS_ESTOQUE_COMPLETO + ") ", GenericDAO.FILTRO_GENERICO_QUERY);
            }
            return this.listByFields(parametros);
        } catch (Exception e) {
            log.error("Erro no listByProfissional", e);
        }
        return null;
    }
}
