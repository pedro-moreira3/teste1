package br.com.lume.odonto.bo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

import org.apache.log4j.Logger;

import br.com.lume.odonto.dao.PersistenceUnitName;
import br.com.lume.odonto.entity.CategoriaMotivo;
import br.com.lume.odonto.entity.TipoCategoria;

public class CategoriaMotivoBO extends BO<CategoriaMotivo> {

    private Logger log = Logger.getLogger(CategoriaMotivo.class);

    private static final long serialVersionUID = 1L;

    public CategoriaMotivoBO() {
        super(PersistenceUnitName.ODONTO);
        this.setClazz(CategoriaMotivo.class);
    }

    @Override
    public List<CategoriaMotivo> listAll() throws Exception {
        try {
            String jpql = "select c from CategoriaMotivo as c order by c.descricao";
            Query q = this.getDao().createQuery(jpql);
            return this.list(q);
        } catch (Exception e) {
            this.log.error("Erro no listAll", e);
        }
        return null;
    }

    public List<CategoriaMotivo> listByTipoCategoria(TipoCategoria tipoCategoria) {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("tipoCategoria", tipoCategoria);
            return this.listByFields(parametros, new String[] { "descricao" });
        } catch (Exception e) {
            this.log.error("Erro no listByTipoCategoria", e);
        }
        return null;
    }
}
