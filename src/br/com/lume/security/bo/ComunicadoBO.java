package br.com.lume.security.bo;

import java.util.List;

import javax.persistence.Query;

import org.apache.log4j.Logger;

import br.com.lume.common.bo.BO;
import br.com.lume.common.connection.GenericDAO;
import br.com.lume.security.entity.Comunicado;
import br.com.lume.security.entity.Sistema;

public class ComunicadoBO extends BO<Comunicado> {

    private static final long serialVersionUID = 1L;
    private Logger log = Logger.getLogger(ComunicadoBO.class);

    public ComunicadoBO() {
        super(GenericDAO.LUME_SECURITY);
        this.setClazz(Comunicado.class);
    }

    public List<Comunicado> getAllComunicadosBySistema(Sistema sistema) {
        List<Comunicado> comunicados = null;
        try {
            if (sistema != null) {
                StringBuilder jpql = new StringBuilder();
                jpql.append("select c from Comunicado c ");
                jpql.append("where c.sistema.sisIntCod=:sisIntCod ");
                jpql.append("order by c.cmnDtmIni, c.cmnDtmFim");
                Query query = this.getDao().createQuery(jpql.toString());
                query.setParameter("sisIntCod", sistema.getSisIntCod());
                comunicados = this.list(query);
            }
        } catch (Exception e) {
            this.log.error("Erro no getAllComunicadosBySistema", e);
        }
        return comunicados;
    }
}
