package br.com.lume.odonto.bo;

import java.math.BigDecimal;
import java.util.List;

import javax.persistence.Query;

import org.apache.log4j.Logger;

import br.com.lume.odonto.dao.PersistenceUnitName;
import br.com.lume.odonto.entity.RelatorioEstoqueMinimo;

public class RelatorioEstoqueMinimoBO extends BO<RelatorioEstoqueMinimo> {

    private Logger log = Logger.getLogger(RelatorioEstoqueMinimo.class);

    private static final long serialVersionUID = 1L;

    public RelatorioEstoqueMinimoBO() {
        super(PersistenceUnitName.ODONTO);
        this.setClazz(RelatorioEstoqueMinimo.class);
    }

    public List<RelatorioEstoqueMinimo> listAllByFilterToReportGroupByItem() {
        try {
            String sql = "select i.ID, i.DESCRICAO AS ITEM , i.UNIDADE_MEDIDA as UNIDADE_MEDIDA, i.ESTOQUE_MINIMO as ESTOQUE_MINIMO, " + "sum(m.QUANTIDADE_TOTAL) AS SOMA_QUANTIDADE_ATUAL, " + "sum(m.QUANTIDADE*m.TAMANHO_UNIDADE*m.valor)/sum(m.QUANTIDADE*m.TAMANHO_UNIDADE) AS VALOR_MEDIO, " + "sum(m.QUANTIDADE*m.TAMANHO_UNIDADE*m.valor) AS SOMA_VALOR_TOTAL" + " from Material as m, Item as i " + "where i.ID = m.ID_ITEM " + " and m.STATUS = 'A'" + "and m.EXCLUIDO = 'N' and m.ID_EMPRESA = " + ProfissionalBO.getProfissionalLogado().getIdEmpresa() + "i.ID_EMPRESA = " + ProfissionalBO.getProfissionalLogado().getIdEmpresa() + " group by i.ID,  i.DESCRICAO, i.UNIDADE_MEDIDA ,i.ESTOQUE_MINIMO ";
            Query query = this.getDao().createNativeQuery(sql, RelatorioEstoqueMinimo.class);
            return this.list(query);
        } catch (Exception e) {
            log.error("Erro no listAllByFilterToReport", e);
        }
        return null;
    }

    public BigDecimal findValorMedioUlt6Meses(long idItem) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT ");
            sb.append("AVG(M2.VALOR) ");
            sb.append("FROM MATERIAL M2 ");
            sb.append("WHERE M2.ID_ITEM = ?1 ");
            sb.append("AND M2.STATUS = 'A' ");
            sb.append("AND M2.EXCLUIDO ='N' ");
            sb.append("AND M2.DATA_CADASTRO BETWEEN CURRENT_DATE - interval '6 month' ");
            sb.append("AND CURRENT_DATE ");

            Query query = this.getDao().createNativeQuery(sb.toString());
            query.setParameter(1, idItem);
            List<Object> list = query.getResultList();

            return list != null && !list.isEmpty() ? (BigDecimal) list.get(0) : new BigDecimal(0);
        } catch (Exception e) {
            log.error("Erro no findValorMedioUlt6Meses", e);
        }
        return null;
    }

    public BigDecimal findValorUltimaCompra(long idItem) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT ");
            sb.append("VALOR ");
            sb.append("FROM MATERIAL ");
            sb.append("WHERE ID_ITEM = ?1 ");
            sb.append("AND STATUS = 'A' ");
            sb.append("AND EXCLUIDO ='N' ");
            sb.append("ORDER BY ID DESC ");
            sb.append("FETCH FIRST 1 ROWS ONLY ");

            Query query = this.getDao().createNativeQuery(sb.toString());
            query.setParameter(1, idItem);
            List<Object> list = query.getResultList();

            return list != null && !list.isEmpty() ? (BigDecimal) list.get(0) : new BigDecimal(0);
        } catch (Exception e) {
            log.error("Erro no findValorUltimaCompra", e);
        }
        return null;
    }

    public List<RelatorioEstoqueMinimo> listAllByFilterToReportGroupByItemFiltrado() {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT ");
            sb.append("I.ID, ");
            sb.append("I.DESCRICAO AS ITEM , ");
            sb.append("I.TIPO AS TIPO , ");
            sb.append("I.UNIDADE_MEDIDA AS UNIDADE_MEDIDA, ");
            sb.append("I.ESTOQUE_MINIMO AS ESTOQUE_MINIMO, ");
            sb.append("SUM(M.QUANTIDADE_ATUAL) AS SOMA_QUANTIDADE_ATUAL, ");
            sb.append("SUM(M.QUANTIDADE*M.TAMANHO_UNIDADE*M.VALOR)/NULLIF(SUM(M.QUANTIDADE*M.TAMANHO_UNIDADE),0) AS VALOR_MEDIO, ");
            sb.append("SUM(M.QUANTIDADE*M.TAMANHO_UNIDADE*M.VALOR) AS SOMA_VALOR_TOTAL ");
            sb.append("FROM MATERIAL AS M, ITEM AS I ");
            sb.append("WHERE I.ID = M.ID_ITEM ");
            sb.append("AND I.ESTOQUE_MINIMO > 0 ");
            sb.append("AND M.STATUS = 'A' ");
            sb.append("AND M.EXCLUIDO = 'N' ");
            sb.append("AND I.ID_EMPRESA = " + ProfissionalBO.getProfissionalLogado().getIdEmpresa() + " ");
            sb.append("GROUP BY I.ID, ");
            sb.append("I.DESCRICAO, ");
            sb.append("I.UNIDADE_MEDIDA , ");
            sb.append("I.ESTOQUE_MINIMO, ");
            sb.append("I.TIPO ");
            sb.append("HAVING SUM(M.QUANTIDADE_ATUAL) <= I.ESTOQUE_MINIMO ");

            Query query = this.getDao().createNativeQuery(sb.toString(), RelatorioEstoqueMinimo.class);
            List<RelatorioEstoqueMinimo> list = this.list(query);
            for (RelatorioEstoqueMinimo relatorioEstoqueMinimo : list) {
                relatorioEstoqueMinimo.setValorMedioUlt6Meses(this.findValorMedioUlt6Meses(relatorioEstoqueMinimo.getId()));
                relatorioEstoqueMinimo.setValorUltimaCompra(this.findValorUltimaCompra(relatorioEstoqueMinimo.getId()));
            }
            return list;
        } catch (Exception e) {
            log.error("Erro no listAllByFilterToReport", e);
        }
        return null;
    }
}
