package br.com.lume.odonto.bo;

import java.util.List;

import javax.persistence.Query;

import org.apache.log4j.Logger;

import br.com.lume.odonto.dao.PersistenceUnitName;
import br.com.lume.odonto.entity.TipoCategoria;

public class TipoCategoriaBO extends BO<TipoCategoria> {

    private Logger log = Logger.getLogger(TipoCategoria.class);

    private static final long serialVersionUID = 1L;

    public TipoCategoriaBO() {
        super(PersistenceUnitName.ODONTO);
        this.setClazz(TipoCategoria.class);
    }

    @Override
    public List<TipoCategoria> listAll() throws Exception {
        try {
            String jpql = "select c from TipoCategoria as c order by c.descricao";
            Query q = this.getDao().createQuery(jpql);
            return this.list(q);
        } catch (Exception e) {
            this.log.error("Erro no listAll", e);
        }
        return null;
    }
}
