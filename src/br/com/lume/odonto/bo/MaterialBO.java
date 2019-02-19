package br.com.lume.odonto.bo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

import org.apache.log4j.Logger;
import org.eclipse.persistence.config.QueryHints;

import br.com.lume.common.connection.GenericListDAO;
import br.com.lume.common.exception.business.BusinessException;
import br.com.lume.common.exception.techinical.TechnicalException;
import br.com.lume.common.util.Status;
import br.com.lume.common.util.Utils;
import br.com.lume.odonto.dao.PersistenceUnitName;
import br.com.lume.odonto.entity.Item;
import br.com.lume.odonto.entity.Material;

public class MaterialBO extends BO<Material> {

    private Logger log = Logger.getLogger(Material.class);

    private static final long serialVersionUID = 1L;

    public MaterialBO() {
        super(PersistenceUnitName.ODONTO);
        this.setClazz(Material.class);
    }

    public BigDecimal somaQuantidadeAtual(List<Material> materiais) {
        BigDecimal soma = new BigDecimal(0);
        for (Material material : materiais) {
            soma = soma.add(material.getQuantidadeAtual());
        }
        return soma;
    }

    @Override
    public boolean refresh(Material entity) {
        try {
            return super.refresh(entity);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Material> listDescartePeriodo(Date inicio, Date fim) {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("idEmpresa", ProfissionalBO.getProfissionalLogado().getIdEmpresa());
            parametros.put("excluido", Status.NAO);
            parametros.put("status", "DE");
            if (inicio != null && fim != null) {
                parametros.put("o.dataCadastro between '" + Utils.dateToString(inicio, "yyyy-MM-dd HH:mm:ss") + "' and '" + Utils.dateToString(fim, "yyyy-MM-dd HH:mm:ss") + "' ",
                        GenericListDAO.FILTRO_GENERICO_QUERY);
            }
            return this.listByFields(parametros);
        } catch (Exception e) {
            log.error("Erro no listDescartePeriodo", e);
        }
        return null;
    }

    public List<Material> listForDistribuicao(Material material) throws Exception {
        try {
            Map<String, Object> parametros = new HashMap<>();
            // parametros.put("idEmpresa", ProfissionalBO.getProfissionalLogado().getIdEmpresa());
            // FIXME TIRAR ISSO
            parametros.put("idEmpresa", 41);
            parametros.put("status", "A");
            parametros.put("item", material.getItem());
            parametros.put("local", material.getLocal());
            parametros.put("excluido", Status.NAO);
            parametros.put("o.quantidadeAtual < o.quantidadeUnidade ", GenericListDAO.FILTRO_GENERICO_QUERY);
            parametros.put("distribuicaoAutomatica", Status.SIM);
            return this.listByFields(parametros, new String[] { "quantidadeUnidade desc", "dataUltimaUtilizacao desc" });
        } catch (Exception e) {
            log.error("Erro no listByEmpresa", e);
        }
        return null;
    }

    @Override
    public boolean remove(Material material) throws BusinessException, TechnicalException {
        material.setExcluido(Status.SIM);
        material.setDataExclusao(Calendar.getInstance().getTime());
        material.setExcluidoPorProfissional(ProfissionalBO.getProfissionalLogado().getId());
        material.setNotaFiscal(null);
        this.persist(material);
        return true;
    }

    @Override
    public List<Material> listAll() throws Exception {
        return this.listByEmpresa();
    }

    public List<Material> listByEmpresa() throws Exception {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("idEmpresa", ProfissionalBO.getProfissionalLogado().getIdEmpresa());
            parametros.put("excluido", Status.NAO);
            return this.listByFields(parametros);
        } catch (Exception e) {
            log.error("Erro no listByEmpresa", e);
        }
        return null;
    }

    public List<Material> listAtivosByEmpresa_() throws Exception {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("idEmpresa", ProfissionalBO.getProfissionalLogado().getIdEmpresa());
            parametros.put("status", "A");
            parametros.put("excluido", Status.NAO);
            return this.listByFields(parametros);
        } catch (Exception e) {
            log.error("Erro no listByEmpresa", e);
        }
        return null;
    }

    public List<Material> listAtivosByEmpresa() throws Exception {
        // try {
        // Map<String, Object> parametros = new HashMap<String, Object>();
        // parametros.put("idEmpresa", ProfissionalBO.getProfissionalLogado().getIdEmpresa());
        // parametros.put("status", "A");
        // parametros.put("o.quantidadeAtual > 0 ", GenericListDAO.FILTRO_GENERICO_QUERY);
        // parametros.put("excluido", Status.NAO);
        // return listByFields(parametros);
        //
        // query.setHint(QueryHints.BATCH_TYPE, "JOIN");
        // query.setHint(QueryHints.LEFT_FETCH, "a.funcionario");
        //
        // } catch (Exception e) {
        // log.error("Erro no listByEmpresa", e);
        // }
        // return null;
        //
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("select m from Material m ");
            sb.append("where m.idEmpresa = :idEmpresa and m.status = 'A' and m.excluido = 'N'");
            Query query = this.getDao().createQuery(sb.toString());
            query.setParameter("idEmpresa", ProfissionalBO.getProfissionalLogado().getIdEmpresa());
            query.setHint(QueryHints.BATCH_TYPE, "JOIN");
            query.setHint(QueryHints.LEFT_FETCH, "m.item");
            query.setHint(QueryHints.LEFT_FETCH, "m.marca");
            query.setHint(QueryHints.LEFT_FETCH, "m.local");
            return query.getResultList();
        } catch (Exception e) {
            log.error("Erro no listAtivosByEmpresaMaiorZero", e);
        }
        return null;
    }

    public List<Material> listByItem(Item item) throws Exception {
        Map<String, Object> parametros = new HashMap<>();
        parametros.put("item", item);
        parametros.put("excluido", Status.NAO);
        return this.listByFields(parametros);
    }

    public List<Material> listAtivosByEmpresaAndItemAndQuantidade(List<Item> itens, Integer quantidade) throws Exception {
        try {
            List<Material> materiais = new ArrayList<>();
            for (Item item : itens) {
                materiais.addAll(this.listAtivosByEmpresaAndItemAndQuantidade(item, quantidade));
            }
            return materiais;
        } catch (Exception e) {
            log.error("Erro no listByEmpresa", e);
        }
        return null;
    }

    public List<Material> listAtivosByEmpresaAndItemAndQuantidade(Item item, Integer quantidade) throws Exception {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("idEmpresa", ProfissionalBO.getProfissionalLogado().getIdEmpresa());
            parametros.put("status", "A");
            parametros.put("item", item);
            parametros.put(" o.quantidadeAtual >= 1 ", GenericListDAO.FILTRO_GENERICO_QUERY);
            parametros.put("excluido", Status.NAO);
            parametros.put("local.tipo", "SM");
            return this.listByFields(parametros, new String[] { "quantidadeAtual asc" });
        } catch (Exception e) {
            log.error("Erro no listByEmpresa", e);
        }
        return null;
    }

    @Override
    public boolean persist(Material entity) throws BusinessException, TechnicalException {
        boolean persist = super.persist(entity);
        try {
            getDao().refresh(entity);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return persist;
    }

    public List<Material> listAtivosByEmpresaAndItem(Item item) throws Exception {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("idEmpresa", ProfissionalBO.getProfissionalLogado().getIdEmpresa());
            parametros.put("status", "A");
            parametros.put("item", item);
            parametros.put(" o.quantidadeAtual > 0", GenericListDAO.FILTRO_GENERICO_QUERY);
            parametros.put("excluido", Status.NAO);
            parametros.put("local.tipo", "SM");
            return this.listByFields(parametros, new String[] { "quantidadeAtual asc" });
        } catch (Exception e) {
            log.error("Erro no listByEmpresa", e);
        }
        return null;
    }

    public List<Material> listAllAtivosByEmpresaAndItem(Item item) throws Exception {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("idEmpresa", ProfissionalBO.getProfissionalLogado().getIdEmpresa());
            parametros.put("status", "A");
            parametros.put("item", item);
            parametros.put("excluido", Status.NAO);
            parametros.put("local.tipo", "SM");
            return this.listByFields(parametros);
        } catch (Exception e) {
            log.error("Erro no listByEmpresa", e);
        }
        return null;
    }

    public List<Material> listAtivosByEmpresaWithoutNotaFiscal() throws Exception {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("idEmpresa", ProfissionalBO.getProfissionalLogado().getIdEmpresa());
            parametros.put("status", "A");
            parametros.put("excluido", Status.NAO);
            parametros.put(" o.notaFiscal is null ", GenericListDAO.FILTRO_GENERICO_QUERY);
            parametros.put("brinde", Status.NAO);
            return this.listByFields(parametros);
        } catch (Exception e) {
            log.error("Erro no listByEmpresa", e);
        }
        return null;
    }

    public BigDecimal atualizaQuantidade_(long id, BigDecimal quantidade) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("UPDATE MATERIAL CLI ");
        if (quantidade.doubleValue() < 0) {
            sb.append("SET QUANTIDADE_ATUAL = QUANTIDADE_ATUAL - " + quantidade.abs().intValue());
        } else {
            sb.append("SET QUANTIDADE_ATUAL = QUANTIDADE_ATUAL + " + quantidade.abs().intValue());
        }
        sb.append(" WHERE ID = " + id);
        Query query = this.getDao().createNativeQuery(sb.toString());
        this.getDao().executeQuery(query);

        sb = new StringBuilder();
        sb.append("SELECT QUANTIDADE_ATUAL FROM MATERIAL WHERE ID = " + id);
        query = this.getDao().createNativeQuery(sb.toString());
        List<Object[]> resultList = query.getResultList();
        return resultList != null && !resultList.isEmpty() ? (BigDecimal) resultList.get(0)[0] : null;

    }

    public BigDecimal findQuantidadeAtual(long id) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT QUANTIDADE_ATUAL FROM MATERIAL WHERE ID = " + id);
            Query query = this.getDao().createNativeQuery(sb.toString());
            List<BigDecimal> resultList = query.getResultList();
            return resultList != null && !resultList.isEmpty() ? resultList.get(0) : null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new BigDecimal(0);

    }

    public void carregarQuantidadeEmprestada(Material material) {
        material.setQuantidadeEmprestada(findQuantidadeEmprestada(material.getId()));
    }

    public BigDecimal findQuantidadeEmprestada(long id) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT ");
            sb.append("SUM(QTD) ");
            sb.append("FROM ");
            sb.append("( ");
            sb.append("   SELECT ");
            sb.append("   SUM(MI.QUANTIDADE) AS QTD ");
            sb.append("   FROM MATERIAL_INDISPONIVEL MI,RESERVA_KIT RK,RESERVA R ");
            sb.append("   WHERE MI.STATUS IS NULL ");
            sb.append("   AND MI.EXCLUIDO = 'N' ");
            sb.append("   AND MI.ID_RESERVA_KIT = RK.ID ");
            sb.append("   AND RK.EXCLUIDO = 'N' ");
            sb.append("   AND RK.ID_RESERVA = R.ID ");
            sb.append("   AND R.EXCLUIDO = 'N' ");
            sb.append("   AND RK.STATUS IN ('PE','EN') ");
            sb.append("   AND MI.ID_MATERIAL = ?1 ");
            sb.append("   UNION ALL ");
            sb.append("   SELECT ");
            sb.append("   SUM(QUANTIDADE) AS QTD ");
            sb.append("   FROM ABASTECIMENTO ");
            sb.append("   WHERE ID_MATERIAL = ?1 ");
            sb.append("   AND STATUS = 'EN' ");
            sb.append("   AND EXCLUIDO = 'N' ");

            sb.append("   UNION ALL ");
            sb.append("   SELECT ");
            sb.append("   SUM(LK.QUANTIDADE) AS QTD ");
            sb.append("   FROM LAVAGEM_KIT LK,LAVAGEM L, MATERIAL_INDISPONIVEL MI,RESERVA_KIT RK,RESERVA R ");
            sb.append("   WHERE LK.ID_LAVAGEM = L.ID ");
            sb.append("   AND LK.ID_MATERIAL_INDISPONIVEL = MI.ID ");
            sb.append("   AND MI.ID_MATERIAL = ?1 ");
            sb.append("   AND L.STATUS = 'A' ");
            sb.append("   AND L.EXCLUIDO = 'N' ");
            sb.append("   AND MI.EXCLUIDO = 'N' ");
            sb.append("   AND MI.ID_RESERVA_KIT = RK.ID ");
            sb.append("   AND RK.ID_RESERVA = R.ID ");
            sb.append("   UNION ALL ");
            sb.append("   SELECT ");
            sb.append("   SUM(LK.QUANTIDADE) AS QTD ");
            sb.append("   FROM LAVAGEM_KIT LK,LAVAGEM L,ABASTECIMENTO A ");
            sb.append("   WHERE LK.ID_LAVAGEM = L.ID ");
            sb.append("   AND LK.ID_ABASTECIMENTO = A.ID ");
            sb.append("   AND A.ID_MATERIAL = ?1 ");
            sb.append("   AND L.STATUS = 'A' ");
            sb.append("   AND L.EXCLUIDO = 'N' ");
            sb.append("   UNION ALL ");
            sb.append("   SELECT ");
            sb.append("   SUM(LK.QUANTIDADE) AS QTD ");
            sb.append("   FROM ESTERILIZACAO_KIT LK,ESTERILIZACAO L, MATERIAL_INDISPONIVEL MI,RESERVA_KIT RK,RESERVA R ");
            sb.append("   WHERE LK.ID_ESTERILIZACAO = L.ID ");
            sb.append("   AND LK.ID_MATERIAL_INDISPONIVEL = MI.ID ");
            sb.append("   AND MI.ID_MATERIAL = ?1 ");
            sb.append("   AND L.STATUS IN ('A','E','P') ");
            sb.append("   AND L.EXCLUIDO = 'N' ");
            sb.append("   AND MI.EXCLUIDO = 'N' ");
            sb.append("   AND MI.ID_RESERVA_KIT = RK.ID ");
            sb.append("   AND RK.ID_RESERVA = R.ID ");
            sb.append("   UNION ALL ");
            sb.append("   SELECT ");
            sb.append("   SUM(LK.QUANTIDADE) AS QTD ");
            sb.append("   FROM ESTERILIZACAO_KIT LK,ESTERILIZACAO L,ABASTECIMENTO A ");
            sb.append("   WHERE LK.ID_ESTERILIZACAO = L.ID ");
            sb.append("   AND LK.ID_ABASTECIMENTO = A.ID ");
            sb.append("   AND A.ID_MATERIAL = ?1 ");
            sb.append("   AND L.STATUS IN ('A','E','P') ");
            sb.append("   AND L.EXCLUIDO = 'N' ");
            sb.append(") ");
            sb.append("AS QTD ");
            Query query = this.getDao().createNativeQuery(sb.toString());
            query.setParameter(1, id);

            List<BigDecimal> resultList = query.getResultList();
            return resultList != null && !resultList.isEmpty() && resultList.get(0) != null ? resultList.get(0) : new BigDecimal(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new BigDecimal(0);

    }
}
