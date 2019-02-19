package br.com.lume.security.bo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import br.com.lume.common.bo.BO;
import br.com.lume.common.connection.GenericDAO;
import br.com.lume.security.entity.Objeto;
import br.com.lume.security.entity.Perfil;
import br.com.lume.security.entity.PerfilUsuario;
import br.com.lume.security.entity.Restricao;
import br.com.lume.security.entity.Usuario;

public class RestricaoBO extends BO<Restricao> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(RestricaoBO.class);

    public RestricaoBO() {
        super(GenericDAO.LUME_SECURITY);
        this.setClazz(Restricao.class);
    }

    public List<Restricao> listByPerfilUsuario(PerfilUsuario perfilUsuario) throws Exception {
        Map<String, Object> filtros = new HashMap<>();
        filtros.put("perfilUsuario.peuIntCod", perfilUsuario.getPeuIntCod());
        return this.listByFields(filtros);
    }

    public List<Restricao> listByPerfilAndObjeto(Perfil perfil, Objeto objeto) throws Exception {
        if (objeto != null) {
            Map<String, Object> filtros = new HashMap<>();
            filtros.put("perfil", perfil);
            filtros.put("objeto.objIntCod", objeto.getObjIntCod());
            return this.listByFields(filtros);
        } else {
            return new ArrayList<>();
        }
    }

    public boolean isRestrito(String acao, Usuario usuario, Objeto objeto) throws Exception {
        List<Restricao> restricoes = new ArrayList<>();
        for (Perfil perfil : usuario.getPerfisUsuarios()) {
            restricoes.addAll(this.listByPerfilAndObjeto(perfil, objeto));
        }
        for (Restricao restricao : restricoes) {
            if (restricao.getAcao().getAcaStrDes().equalsIgnoreCase(acao)) {
                return true;
            }
        }
        return false;
    }
}
