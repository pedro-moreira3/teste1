package br.com.lume.security.bo;

import java.util.HashMap;
import java.util.List;

import javax.persistence.Query;

import org.apache.log4j.Logger;

import br.com.lume.common.bo.BO;
import br.com.lume.common.connection.GenericDAO;
import br.com.lume.security.entity.Sistema;

public class SistemaBO extends BO<Sistema> {

    private static final long serialVersionUID = 1L;
    private Logger log = Logger.getLogger(SistemaBO.class);
    private static HashMap<String, Sistema> sistemasHash = new HashMap<>();

    public SistemaBO() {
        super(GenericDAO.LUME_SECURITY);
        this.setClazz(Sistema.class);
    }

    public Sistema getSistemaById(final int sisIntCod) {
        Sistema sistema = null;
        try {
            sistema = this.find(sisIntCod);
        } catch (Exception e) {
            this.log.error("Erro no getSistemaById", e);
        }
        return sistema;
    }

    public Sistema getSistemaBySigla(String sigla) {
        Sistema sistema = null;
        if (sigla != null) {
            try {
                if (!sistemasHash.containsKey(sigla)) {
                    StringBuilder jpql = new StringBuilder();
                    jpql.append("select s from");
                    jpql.append("  Sistema s ");
                    jpql.append("where");
                    jpql.append("  s.sisChaSigla = :sisChaSigla ");
                    Query query = this.getDao().createQuery(jpql.toString());
                    query.setParameter("sisChaSigla", sigla);
                    List<Sistema> sistemas = this.list(query);
                    sistema = sistemas != null && sistemas.size() > 0 ? sistemas.get(0) : null;
                    sistemasHash.put(sigla, sistema);
                } else {
                    sistema = sistemasHash.get(sigla);
                }
            } catch (Exception e) {
                this.log.error("Erro no getSistemaBySigla", e);
            }
        }
        return sistema;
    }

    public static void main(String[] args) {
        new SistemaBO().getAllSistemas();
    }

    public List<Sistema> getAllSistemas() {
        List<Sistema> sistemas = null;
        try {
            // FIXME : Fazer um listALL no GenericListDAO passando a coluna que deseja ordenar
            sistemas = this.list("select s from Sistema s order by s.sisStrDes");
            //sistemas = listAll();
        } catch (Exception e) {
            this.log.error("Erro no getAllSistemas", e);
        }
        return sistemas;
    }
}
