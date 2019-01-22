package br.com.lume.security.bo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import br.com.lume.common.bo.BO;
import br.com.lume.common.connection.GenericDAO;
import br.com.lume.security.entity.PerfilUsuario;
import br.com.lume.security.entity.Sistema;
import br.com.lume.security.entity.Usuario;

public class PerfilUsuarioBO extends BO<PerfilUsuario> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(PerfilUsuarioBO.class);

    public PerfilUsuarioBO() {
        super(GenericDAO.LUME_SECURITY);
        this.setClazz(PerfilUsuario.class);
    }

    public List<PerfilUsuario> listByUsuarioAndSistema(Usuario usuario, Sistema sistema) throws Exception {
        Map<String, Object> filtros = new HashMap<>();
        filtros.put("usuario.usuIntCod", usuario.getUsuIntCod());
        filtros.put("perfil.sistema.sisIntCod", sistema.getSisIntCod());
        return this.listByFields(filtros);
    }
}
