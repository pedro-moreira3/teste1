package br.com.lume.odonto.bo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

import org.apache.log4j.Logger;

import br.com.lume.configuracao.Configurar;
import br.com.lume.odonto.dao.PersistenceUnitName;
import br.com.lume.odonto.entity.Plano;
import br.com.lume.security.bo.EmpresaBO;

public class PlanoBO extends BO<Plano> {

    private Logger log = Logger.getLogger(PlanoBO.class);

    private static final long serialVersionUID = 1L;

    public PlanoBO() {
        super(PersistenceUnitName.ODONTO);
        this.setClazz(Plano.class);
    }

    @Override
    public List<Plano> listAll() {
        try {
            String jpql = "select p from Plano p order by p.id asc ";
            Query query = this.getDao().createQuery(jpql);
            return this.list(query);
        } catch (Exception e) {
            this.log.error("Erro no listAll", e);
        }
        return null;
    }

    public Plano findByUsuarioLogado() {
        try {
            return this.find(Configurar.getInstance().getConfiguracao().getEmpresaLogada().getIdPlano());
        } catch (Exception e) {
            this.log.error(e);
        }
        return null;
    }

    public Plano findByPaypal(String nomePaypal) throws Exception {
        Map<String, Object> filtros = new HashMap<>();
        filtros.put("nomePaypal", nomePaypal.trim());
        List<Plano> planos = super.listByFields(filtros);
        return planos != null && !planos.isEmpty() ? planos.get(0) : null;
    }
}
