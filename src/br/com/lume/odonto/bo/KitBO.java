package br.com.lume.odonto.bo;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

import org.apache.log4j.Logger;

import br.com.lume.common.exception.business.BusinessException;
import br.com.lume.common.exception.techinical.TechnicalException;
import br.com.lume.common.util.Status;
import br.com.lume.odonto.dao.PersistenceUnitName;
import br.com.lume.odonto.entity.Item;
import br.com.lume.odonto.entity.Kit;
import br.com.lume.odonto.entity.KitItem;
import br.com.lume.security.entity.Empresa;

public class KitBO extends BO<Kit> {

    private Logger log = Logger.getLogger(KitBO.class);

    private static final long serialVersionUID = 1L;

    public KitBO() {
        super(PersistenceUnitName.ODONTO);
        this.setClazz(Kit.class);
    }

    @Override
    public boolean remove(Kit kit) throws BusinessException, TechnicalException {
        kit.setExcluido(Status.SIM);
        kit.setDataExclusao(Calendar.getInstance().getTime());
        kit.setExcluidoPorProfissional(ProfissionalBO.getProfissionalLogado().getId());
        this.persist(kit);
        return true;
    }

    @Override
    public List<Kit> listAll() throws Exception {
        return this.listByEmpresa();
    }

    public List<Kit> listByEmpresa() throws Exception {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("idEmpresa", ProfissionalBO.getProfissionalLogado().getIdEmpresa());
            parametros.put("excluido", Status.NAO);
            return this.listByFields(parametros, new String[] { "descricao" });
        } catch (Exception e) {
            this.log.error("Erro no listByEmpresa", e);
        }
        return null;
    }

    public List<Kit> listByEmpresaAndTipo() throws Exception {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("idEmpresa", ProfissionalBO.getProfissionalLogado().getIdEmpresa());
            parametros.put("excluido", Status.NAO);
            parametros.put("tipo", "Instrumental");
            return this.listByFields(parametros);
        } catch (Exception e) {
            this.log.error("Erro no listByEmpresa", e);
        }
        return null;
    }

    public List<Kit> listByEmpresaAndDescricaoParcial(String digitacao) throws Exception {
        try {
            String jpql = "SELECT i FROM Kit AS i " + "WHERE UPPER(i.descricao) LIKE :descricao AND i.idEmpresa = :empresa AND  i.excluido= :excluido";
            Query q = this.getDao().createQuery(jpql);
            q.setParameter("empresa", ProfissionalBO.getProfissionalLogado().getIdEmpresa());
            q.setParameter("descricao", "%" + digitacao.toUpperCase() + "%");
            q.setParameter("excluido", Status.NAO);
            return this.list(q);
        } catch (Exception e) {
            this.log.error("Erro no listByEmpresaAndDescricaoParcial", e);
        }
        return null;
    }

    public List<Kit> listByEmpresaAndDescricaoParcialAndTipo(String digitacao) throws Exception {
        try {
            String jpql = "SELECT i FROM Kit AS i " + "WHERE UPPER(i.descricao) LIKE :descricao AND i.idEmpresa = :empresa AND  i.excluido= :excluido AND  i.tipo= :tipo";
            Query q = this.getDao().createQuery(jpql);
            q.setParameter("empresa", ProfissionalBO.getProfissionalLogado().getIdEmpresa());
            q.setParameter("descricao", "%" + digitacao.toUpperCase() + "%");
            q.setParameter("excluido", Status.NAO);
            q.setParameter("tipo", "Instrumental");
            return this.list(q);
        } catch (Exception e) {
            this.log.error("Erro no listByEmpresaAndDescricaoParcial", e);
        }
        return null;
    }

    public Kit findByDescricaoAndEmpresaTipo(String descricao, Empresa empresa, String tipo) {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("idEmpresa", empresa.getEmpIntCod());
            parametros.put("descricao", descricao);
            parametros.put("excluido", Status.NAO);
            parametros.put("tipo", tipo);
            List<Kit> listByFields = this.listByFields(parametros, new String[] { "id asc" });
            return listByFields != null && !listByFields.isEmpty() ? listByFields.get(0) : null;
        } catch (Exception e) {
            this.log.error("Erro no findByDescricaoAndEmpresaTipo", e);
        }
        return null;
    }

    public void clonarDadosEmpresaDefault(Empresa modelo, Empresa destino) {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("idEmpresa", modelo.getEmpIntCod());
            parametros.put("excluido", Status.NAO);
            List<Kit> kits = this.listByFields(parametros);
            KitItemBO kitItemBO = new KitItemBO();
            ItemBO itemBO = new ItemBO();

            for (Kit kit : kits) {
                kit.setId(0L);
                kit.setIdEmpresa(destino.getEmpIntCod());
                kit.setKitItens(null);
                detach(kit);
                persist(kit);
            }

            List<KitItem> kitItens = kitItemBO.listByEmpresa(modelo);

            for (KitItem kitItem : kitItens) {
                Item item = itemBO.findByDescricaoAndEmpresa(kitItem.getItem().getDescricao(), destino);
                Kit kit = findByDescricaoAndEmpresaTipo(kitItem.getKit().getDescricao(), destino, kitItem.getKit().getTipo());
                kitItem.setKit(kit);
                kitItem.setItem(item);
                kitItem.setId(0L);
                kitItemBO.detach(kitItem);
                kitItemBO.persist(kitItem);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
