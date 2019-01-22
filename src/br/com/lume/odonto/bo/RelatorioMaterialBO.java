package br.com.lume.odonto.bo;

import java.util.List;

import javax.persistence.Query;

import org.apache.log4j.Logger;

import br.com.lume.odonto.dao.PersistenceUnitName;
import br.com.lume.odonto.entity.RelatorioMaterial;

public class RelatorioMaterialBO extends BO<RelatorioMaterial> {

    private Logger log = Logger.getLogger(RelatorioMaterial.class);

    private static final long serialVersionUID = 1L;

    public RelatorioMaterialBO() {
        super(PersistenceUnitName.ODONTO);
        this.setClazz(RelatorioMaterial.class);
    }

    public List<RelatorioMaterial> listAllByFilterToReport() {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT ");
            sb.append("ROW_NUMBER() OVER () * EXTRACT(MICROSECONDS FROM NOW()) AS ID, ");
            sb.append("ITEM, ");
            sb.append("TIPO, ");
            sb.append("SUM(QUANTIDADE_ATUAL) AS QUANTIDADE_ATUAL, ");
            sb.append("SUM(VALOR_UNITARIO) AS VALOR_UNITARIO, ");
            sb.append("SUM(VALOR_TOTAL) AS VALOR_TOTAL, ");
            sb.append("UNIDADE_MEDIDA AS UNIDADE_MEDIDA , ");
            sb.append("LOCAL AS LOCAL, ");
            sb.append("MARCA AS MARCA, ");
            sb.append("MAX(DATA_ULTIMA_UTILIZACAO) AS DATA_ULTIMA_UTILIZACAO ");
            sb.append("FROM ( ");
            sb.append("SELECT ");
            sb.append("I.DESCRICAO AS ITEM, ");
            sb.append("I.TIPO AS TIPO, ");
            sb.append("M.QUANTIDADE_ATUAL AS QUANTIDADE_ATUAL, ");
            sb.append("(SELECT M2.VALOR FROM MATERIAL M2 WHERE M2.ID_ITEM = M.ID_ITEM ORDER BY ID DESC FETCH FIRST 1 ROWS ONLY) AS VALOR_UNITARIO, ");
            sb.append("(M.QUANTIDADE_ATUAL * (SELECT M2.VALOR FROM MATERIAL M2 WHERE M2.ID_ITEM = M.ID_ITEM ORDER BY ID DESC FETCH FIRST 1 ROWS ONLY) ) AS VALOR_TOTAL, ");
            sb.append("I.UNIDADE_MEDIDA AS UNIDADE_MEDIDA, ");
            sb.append("LOC.DESCRICAO AS LOCAL, ");
            sb.append("MA.NOME AS MARCA, ");
            sb.append("M.DATA_ULTIMA_UTILIZACAO AS DATA_ULTIMA_UTILIZACAO ");
            sb.append("FROM MATERIAL M,ITEM I,MARCA MA,LOCAL LOC ");
            sb.append("WHERE I.ID = M.ID_ITEM ");
            sb.append("AND MA.ID = M.ID_MARCA ");
            sb.append("AND M.EXCLUIDO = 'N' ");
            sb.append("AND M.STATUS = 'A' ");
            sb.append("AND M.QUANTIDADE_ATUAL > 0 ");
            sb.append("AND M.ID_LOCAL = LOC.ID ");
            sb.append("AND M.ID_EMPRESA = ?1 ");
            sb.append("ORDER BY 1) AS QUERY ");
            sb.append("GROUP BY ITEM,TIPO,MARCA,LOCAL,UNIDADE_MEDIDA ");
            sb.append("ORDER BY ITEM ASC ");
            Query query = this.getDao().createNativeQuery(sb.toString(), RelatorioMaterial.class);
            query.setParameter(1, ProfissionalBO.getProfissionalLogado().getIdEmpresa());
            return this.list(query);
        } catch (Exception e) {
            log.error("Erro no listAllByFilterToReport", e);
        }
        return null;
    }
}
