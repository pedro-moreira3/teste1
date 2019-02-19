package br.com.lume.security.bo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

import org.apache.log4j.Logger;

import br.com.lume.common.bo.BO;
import br.com.lume.common.connection.GenericDAO;
import br.com.lume.security.entity.Perfil;
import br.com.lume.security.entity.Sistema;
import br.com.lume.security.entity.Usuario;

public class PerfilBO extends BO<Perfil> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(PerfilBO.class);

    public PerfilBO() {
        super(GenericDAO.LUME_SECURITY);
        this.setClazz(Perfil.class);
    }

    public static void main(String[] args) {
        // new PerfilBO().getAllPerfisBySistema(JSFHelper.getSistemaAtual());
        Sistema sistema = new Sistema();
        sistema.setSisIntCod(123);
        List<Perfil> allPerfisBySistema = new PerfilBO().getAllPerfisBySistema(sistema);
        for (Perfil perfil : allPerfisBySistema) {
            System.out.println(perfil.getPerStrDes());
        }
    }

    public List<Perfil> getAllPerfisBySistema(Sistema sistema) {
        List<Perfil> perfils = new ArrayList<>();
        if (sistema != null) {
            try {
                StringBuilder jpql = new StringBuilder();
                jpql.append("select p from Perfil p where p.sistema.sisIntCod=:sisIntCod and p.perChaSts='A' ");
                jpql.append("order by p.perStrDes");
                Query query = this.getDao().createQuery(jpql.toString());
                query.setParameter("sisIntCod", sistema.getSisIntCod());
                perfils = this.list(query);
            } catch (Exception e) {
                log.error("Erro no getAllPerfisBySistema", e);
            }
        }
        return perfils;
    }

    public List<Perfil> getAllPerfisByUsuarioAndSistema(Usuario usuario, Sistema sistema) {
        List<Perfil> perfils = new ArrayList<>();
        if (usuario != null && sistema != null) {
            try {
                StringBuilder jpql = new StringBuilder();
                jpql.append("select p from Perfil p JOIN p.usuariosPerfil pu where ");
                jpql.append(" pu.usuIntCod=:usuIntCod and p.sistema.sisIntCod=:sisIntCod ");
                jpql.append("order by p.perStrDes");
                Query query = this.getDao().createQuery(jpql.toString());
                query.setParameter("usuIntCod", usuario.getUsuIntCod());
                query.setParameter("sisIntCod", sistema.getSisIntCod());
                perfils = this.list(query);
            } catch (Exception e) {
                log.error("Erro no getAllPerfisByUsuarioAndSistema", e);
            }
        }
        return perfils;
    }

    /**
     * @deprecated use getPerfilbyDescricaoAndSistema
     */
    @Deprecated
    public Perfil getPerfilbyDescricao(String perStrDes) {
        Perfil perfil = new Perfil();
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("perStrDes", perStrDes);
            return this.findByFields(parametros);
        } catch (Exception e) {
            log.error("Erro no getPerfilbyDescricao", e);
        }
        return perfil;
    }

    public Perfil getPerfilbyDescricaoAndSistema(String perStrDes, Sistema sistema) {
        Perfil perfil = null;
        if (perStrDes != null && sistema != null) {
            try {
                Map<String, Object> parametros = new HashMap<>();
                parametros.put("perStrDes", perStrDes);
                parametros.put("sistema.sisIntCod", sistema.getSisIntCod());
                return this.findByFields(parametros);
            } catch (Exception e) {
                log.error("Erro no getPerfilbyDescricao", e);
            }
        }
        return perfil;
    }
}
