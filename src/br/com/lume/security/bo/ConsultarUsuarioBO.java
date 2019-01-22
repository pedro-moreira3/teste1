package br.com.lume.security.bo;

import java.util.List;

import javax.persistence.Query;

import org.apache.log4j.Logger;

import br.com.lume.common.bo.BO;
import br.com.lume.common.connection.GenericDAO;
import br.com.lume.security.entity.Sistema;
import br.com.lume.security.entity.Usuario;

public class ConsultarUsuarioBO extends BO<Usuario> {

    private static final long serialVersionUID = 1L;
    private Logger log = Logger.getLogger(ConsultarUsuarioBO.class);

    public ConsultarUsuarioBO() {
        super(GenericDAO.LUME_SECURITY);
        this.setClazz(Usuario.class);
    }

    public List<Usuario> getAllMensagensBySistema(final Sistema sistema) {
        List<Usuario> modulos = null;

        //TODO: change the query
        if (sistema != null) {
            try {
                String jpql = "select m from Mensagem m where m.sistema.sisIntCod=:sisIntCod order by m.msgIntCod";
                Query query = this.getDao().createQuery(jpql);
                query.setParameter("sisIntCod", sistema.getSisIntCod());
                modulos = this.list(query);
            } catch (Exception e) {
                this.log.error("Erro no getAllMensagensBySistema", e);
            }
        }
        return modulos;
    }
}
