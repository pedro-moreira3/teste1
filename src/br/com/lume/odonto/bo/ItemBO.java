package br.com.lume.odonto.bo;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

import org.apache.log4j.Logger;

import br.com.lume.common.connection.GenericListDAO;
import br.com.lume.common.exception.business.BusinessException;
import br.com.lume.common.exception.techinical.TechnicalException;
import br.com.lume.common.util.Status;
import br.com.lume.odonto.dao.PersistenceUnitName;
import br.com.lume.odonto.entity.Item;
import br.com.lume.security.entity.Empresa;

public class ItemBO extends BO<Item> {

    private Logger log = Logger.getLogger(ItemBO.class);

    private static final long serialVersionUID = 1L;

    public ItemBO() {
        super(PersistenceUnitName.ODONTO);
        this.setClazz(Item.class);
    }

    @Override
    public boolean remove(Item item) throws BusinessException, TechnicalException {
        item.setExcluido(Status.SIM);
        item.setDataExclusao(Calendar.getInstance().getTime());
        item.setExcluidoPorProfissional(ProfissionalBO.getProfissionalLogado().getId());
        this.persist(item);
        return true;
    }

    @Override
    public List<Item> listAll() throws Exception {
        return this.listByEmpresa();
    }

    public List<Item> listByEmpresa() throws Exception {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT ");
            sb.append("* ");
            sb.append("FROM ITEM ");
            sb.append("WHERE ID_EMPRESA = ?1 ");
            sb.append("AND (ID_ITEM_PAI IS NULL OR ID_ITEM_PAI IN (SELECT ID FROM ITEM WHERE ID_EMPRESA = ?1 AND EXCLUIDO = 'N')) AND EXCLUIDO = 'N' ");
            Query query = this.getDao().createNativeQuery(sb.toString(), Item.class);
            query.setParameter(1, ProfissionalBO.getProfissionalLogado().getIdEmpresa());
            return query.getResultList();
        } catch (Exception e) {
            log.error("Erro no listByEmpresa", e);
        }
        return null;
    }

    public List<Item> listByEmpresaAndTipo(String tipo) throws Exception {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT ");
            sb.append("* ");
            sb.append("FROM ITEM ");
            sb.append("WHERE ID_EMPRESA = ?1 ");
            sb.append("AND (ID_ITEM_PAI IS NULL OR ID_ITEM_PAI IN (SELECT ID FROM ITEM WHERE ID_EMPRESA = ?1 AND EXCLUIDO = 'N')) AND EXCLUIDO = 'N' AND (TIPO = '" + tipo + "' OR TIPO = '')");
            Query query = this.getDao().createNativeQuery(sb.toString(), Item.class);
            query.setParameter(1, ProfissionalBO.getProfissionalLogado().getIdEmpresa());
            return query.getResultList();
        } catch (Exception e) {
            log.error("Erro no listByEmpresa", e);
        }
        return null;
    }

    public List<Item> listCategoriasByEmpresa() throws Exception {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT ");
            sb.append("* ");
            sb.append("FROM ITEM ");
            sb.append("WHERE ID_EMPRESA = ?1 ");
            sb.append("AND (ID_ITEM_PAI IS NULL OR ID_ITEM_PAI IN (SELECT ID FROM ITEM WHERE ID_EMPRESA = ?1 AND EXCLUIDO = 'N')) AND EXCLUIDO = 'N' AND CATEGORIA = 'S' ");
            Query query = this.getDao().createNativeQuery(sb.toString(), Item.class);
            query.setParameter(1, ProfissionalBO.getProfissionalLogado().getIdEmpresa());
            return query.getResultList();
        } catch (Exception e) {
            log.error("Erro no listByEmpresa", e);
        }
        return null;
    }

    public List<Item> listByPai(long idPai) throws Exception {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("idItemPai.id", idPai);
            parametros.put("idEmpresa", ProfissionalBO.getProfissionalLogado().getIdEmpresa());
            parametros.put("excluido", Status.NAO);
            return this.listByFields(parametros);
        } catch (Exception e) {
            log.error("Erro no listByEmpresa", e);
        }
        return null;
    }

    public List<Item> listByEmpresaAndDescricaoParcial(String digitacao) throws Exception {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT ");
            sb.append("* ");
            sb.append("FROM ITEM ");
            sb.append("WHERE ID_EMPRESA = ?1 ");
            sb.append("AND TRANSLATE(UPPER(DESCRICAO),'ÁÀÄÂÃÉÈËÊÍÌÏÎÓÒÖÔÕÚÙÜÇ', 'AAAAAEEEEIIIIOOOOOUUUC') LIKE '%" + digitacao.toUpperCase() + "%' ");
            sb.append("AND (ID_ITEM_PAI IS NULL OR ID_ITEM_PAI IN (SELECT ID FROM ITEM WHERE ID_EMPRESA = ?1 AND EXCLUIDO = 'N')) AND EXCLUIDO = 'N' AND CATEGORIA = 'N'");
            Query query = this.getDao().createNativeQuery(sb.toString(), Item.class);
            query.setParameter(1, ProfissionalBO.getProfissionalLogado().getIdEmpresa());
            return query.getResultList();
        } catch (Exception e) {
            log.error("Erro no listByEmpresaAndDescricaoParcial", e);
        }
        return null;
    }

    public List<Item> listByEmpresaAndDescricaoParcialAndTipo(String digitacao, String tipo) throws Exception {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT ");
            sb.append("* ");
            sb.append("FROM ITEM ");
            sb.append("WHERE ID_EMPRESA = ?1 ");
            sb.append("AND TRANSLATE(UPPER(DESCRICAO),'ÁÀÄÂÃÉÈËÊÍÌÏÎÓÒÖÔÕÚÙÜÇ', 'AAAAAEEEEIIIIOOOOOUUUC') LIKE '%" + digitacao.toUpperCase() + "%' ");
            sb.append("AND (ID_ITEM_PAI IS NULL OR ID_ITEM_PAI IN (SELECT ID FROM ITEM WHERE ID_EMPRESA = ?1 AND EXCLUIDO = 'N')) AND EXCLUIDO = 'N' AND (TIPO = '" + tipo + "' OR TIPO = '')");
            Query query = this.getDao().createNativeQuery(sb.toString(), Item.class);
            query.setParameter(1, ProfissionalBO.getProfissionalLogado().getIdEmpresa());
            return query.getResultList();
        } catch (Exception e) {
            log.error("Erro no listByEmpresaAndDescricaoParcial", e);
        }
        return null;
    }

    public List<Item> listByEmpresaAndDescricaoParcialAndCategoria(String digitacao) throws Exception {
        List<Item> itens = new ArrayList<>();
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT ");
            sb.append("* ");
            sb.append("FROM ITEM ");
            sb.append("WHERE ID_EMPRESA = ?1 ");
            sb.append("AND TRANSLATE(UPPER(DESCRICAO),'ÁÀÄÂÃÉÈËÊÍÌÏÎÓÒÖÔÕÚÙÜÇ', 'AAAAAEEEEIIIIOOOOOUUUC') LIKE '%" + digitacao.toUpperCase() + "%' ");
            sb.append("AND (ID_ITEM_PAI IS NULL OR ID_ITEM_PAI IN (SELECT ID FROM ITEM WHERE ID_EMPRESA = ?1 AND EXCLUIDO = 'N')) AND EXCLUIDO = 'N' ");
            sb.append("UNION ");
            sb.append("SELECT ");
            sb.append("* ");
            sb.append("FROM ITEM ");
            sb.append("WHERE ID_EMPRESA = ?1 ");
            sb.append("AND EXCLUIDO = 'N' ");
            sb.append("AND CATEGORIA = 'S' ");
            sb.append("AND ");
            sb.append("( ");
            sb.append("   ID_ITEM_PAI IS NULL OR ID_ITEM_PAI IN (SELECT ID FROM ITEM WHERE ID_EMPRESA = ?1 AND EXCLUIDO = 'N') ");
            sb.append(") ");

            Query query = this.getDao().createNativeQuery(sb.toString(), Item.class);
            query.setParameter(1, ProfissionalBO.getProfissionalLogado().getIdEmpresa());
            return query.getResultList();
        } catch (Exception e) {
            log.error("Erro no listByEmpresaAndDescricaoParcial", e);
        }
        return itens;
    }

    public List<Item> listByEmpresaAndDescricaoParcialAndCategoria2(String digitacao) throws Exception {
        List<Item> itens = new ArrayList<>();
        try {
            String jpql = "SELECT i FROM Item AS i " + "WHERE (UPPER(i.descricao) LIKE :descricao AND i.idEmpresa = :empresa OR i.categoria = :categoria) AND  i.excluido= :excluido";
            Query q = this.getDao().createQuery(jpql);
            q.setParameter("empresa", ProfissionalBO.getProfissionalLogado().getIdEmpresa());
            q.setParameter("descricao", "%" + digitacao.toUpperCase() + "%");
            q.setParameter("excluido", Status.NAO);
            q.setParameter("categoria", Status.SIM);
            itens = this.list(q);
        } catch (Exception e) {
            log.error("Erro no listByEmpresaAndDescricaoParcial", e);
        }
        return itens;
    }

    public List<Item> listCategoriasByEmpresaAndDescricaoParcial(String digitacao) throws Exception {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT ");
            sb.append("* ");
            sb.append("FROM ITEM ");
            sb.append("WHERE ID_EMPRESA = ?1 ");
            sb.append("AND TRANSLATE(UPPER(DESCRICAO),'ÁÀÄÂÃÉÈËÊÍÌÏÎÓÒÖÔÕÚÙÜÇ', 'AAAAAEEEEIIIIOOOOOUUUC') LIKE '%" + digitacao.toUpperCase() + "%' ");
            sb.append("AND (ID_ITEM_PAI IS NULL OR ID_ITEM_PAI IN (SELECT ID FROM ITEM WHERE ID_EMPRESA = ?1 AND EXCLUIDO = 'N')) AND EXCLUIDO = 'N' AND CATEGORIA = 'S' ");
            Query query = this.getDao().createNativeQuery(sb.toString(), Item.class);
            query.setParameter(1, ProfissionalBO.getProfissionalLogado().getIdEmpresa());
            return query.getResultList();

        } catch (Exception e) {
            log.error("Erro no listByEmpresaAndDescricaoParcial", e);
        }
        return null;
    }

    public List<Item> listProcurarItemComplete(String digitacao) throws Exception {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT ");
            sb.append("* ");
            sb.append("FROM ITEM I ");
            sb.append("WHERE I.ID_EMPRESA = ?1 ");
            if (digitacao != null && !digitacao.equals("")) {
                sb.append("AND TRANSLATE(UPPER(I.DESCRICAO),'ÁÀÄÂÃÉÈËÊÍÌÏÎÓÒÖÔÕÚÙÜÇ', 'AAAAAEEEEIIIIOOOOOUUUC') LIKE '%" + digitacao.toUpperCase() + "%'  ");
            }
            sb.append("AND I.EXCLUIDO = 'N' ");
            sb.append("AND I.TIPO = 'I' ");
            sb.append("AND EXISTS ");
            sb.append("( ");
            sb.append("   SELECT ");
            sb.append("   1 ");
            sb.append("   FROM MATERIAL M,LOCAL L ");
            sb.append("   WHERE M.ID_ITEM = I.ID ");
            sb.append("   AND M.EXCLUIDO = 'N' ");
            sb.append("   AND M.STATUS = 'A' ");
            sb.append("   AND M.ID_LOCAL = L.ID ");
            sb.append("   AND L.TIPO = 'SM' ");
            sb.append("   AND M.QUANTIDADE_ATUAL > 0 ");
            sb.append(") ORDER BY I.DESCRICAO ");
            Query query = this.getDao().createNativeQuery(sb.toString(), Item.class);
            query.setParameter(1, ProfissionalBO.getProfissionalLogado().getIdEmpresa());
            return query.getResultList();

        } catch (Exception e) {
            log.error("Erro no listProcurarItemComplete", e);
        }
        return null;
    }

    public void clonarDadosEmpresaDefault(Empresa modelo, Empresa destino) {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("idEmpresa", modelo.getEmpIntCod());
            parametros.put("excluido", Status.NAO);
            parametros.put("o.idItemPai is null", GenericListDAO.FILTRO_GENERICO_QUERY);

            List<Item> itens = this.listByFields(parametros);

            for (Item item : itens) {
                item.setId(0L);
                item.setIdEmpresa(destino.getEmpIntCod());
                detach(item);
                persist(item);
            }

            parametros = new HashMap<>();
            parametros.put("idEmpresa", modelo.getEmpIntCod());
            parametros.put("excluido", Status.NAO);
            parametros.put("o.idItemPai is not null", GenericListDAO.FILTRO_GENERICO_QUERY);

            itens = this.listByFields(parametros, new String[] { "idItemPai desc" });

            for (Item item : itens) {
                item.setId(0L);
                item.setIdEmpresa(destino.getEmpIntCod());
                detach(item);
                persist(item);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Item findByDescricaoAndEmpresa(String descricao, Empresa empresa) {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("idEmpresa", empresa.getEmpIntCod());
            parametros.put("descricao", descricao);
            parametros.put("excluido", Status.NAO);
            List<Item> listByFields = this.listByFields(parametros, new String[] { "id asc" });
            return listByFields != null && !listByFields.isEmpty() ? listByFields.get(0) : null;
        } catch (Exception e) {
            log.error("Erro no findByDescricaoAndEmpresa", e);
        }
        return null;
    }

}
