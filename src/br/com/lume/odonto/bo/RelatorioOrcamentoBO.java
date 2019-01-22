package br.com.lume.odonto.bo;

import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.apache.log4j.Logger;

import br.com.lume.odonto.dao.PersistenceUnitName;
import br.com.lume.odonto.entity.RelatorioOrcamento;

public class RelatorioOrcamentoBO extends BO<RelatorioOrcamento> {

    private Logger log = Logger.getLogger(RelatorioOrcamento.class);

    private static final long serialVersionUID = 1L;

    public RelatorioOrcamentoBO() {
        super(PersistenceUnitName.ODONTO);
        this.setClazz(RelatorioOrcamento.class);
    }

    public List<RelatorioOrcamento> listAll(Date inicio, Date fim) {
        try {
            StringBuffer sb = new StringBuffer();
            sb.append("SELECT ");
            sb.append("ROW_NUMBER() OVER () * EXTRACT(MICROSECONDS FROM NOW()) AS ID, ");
            sb.append("PT.ID,DB.NOME AS PACIENTE, DB2.NOME AS PROFISSIONAL, PT.DATA_HORA AS DATA, PT.VALOR_TOTAL, PT.VALOR_TOTAL_DESCONTO, ");
            sb.append("CASE ");
            sb.append("WHEN (PT.DATA_FINALIZADO IS NOT NULL) THEN 'Finalizado' ");
            sb.append(
                    "WHEN (PT.DATA_FINALIZADO IS NULL AND EXISTS (SELECT 1 FROM PLANO_TRATAMENTO_PROCEDIMENTO PTP WHERE PTP.ID_PLANO_TRATAMENTO = PT.ID AND PTP.DATA_FINALIZADO IS NOT NULL AND PTP.EXCLUIDO = 'N')) THEN 'Em Andamento' ");
            sb.append("ELSE 'Aberto' END AS STATUS ");
            sb.append("FROM ORCAMENTO O ");
            sb.append("JOIN PLANO_TRATAMENTO PT ON PT.EXCLUIDO = 'N' ");
            sb.append("                         AND O.ID_PLANO_TRATAMENTO = PT.ID ");
            sb.append("JOIN PACIENTE P ON PT.ID_PACIENTE = P.ID ");
            sb.append("JOIN DADOS_BASICOS DB ON  P.ID_DADOS_BASICOS = DB.ID ");
            sb.append("JOIN PROFISSIONAL PROF ON O.ID_PROFISSIONAL = PROF.ID ");
            sb.append("JOIN DADOS_BASICOS DB2 ON PROF.ID_DADOS_BASICOS = DB2.ID ");
            sb.append("WHERE ");
            sb.append("O.EXCLUIDO = 'N' AND ");
            sb.append("PROF.ID_EMPRESA = ?1 AND ");
            sb.append("DATE(O.DATA_APROVACAO) BETWEEN ?2 AND ?3 AND ");
            sb.append("PT.DATA_HORA BETWEEN ?2 AND ?3 ");
            sb.append("ORDER BY PT.DATA_HORA");
            Query query = this.getDao().createNativeQuery(sb.toString(), RelatorioOrcamento.class);
            query.setParameter(1, ProfissionalBO.getProfissionalLogado().getIdEmpresa());
            query.setParameter(2, inicio);
            query.setParameter(3, fim);
            return this.list(query);
        } catch (Exception e) {
            log.error("Erro no listAll", e);
        }
        return null;
    }

}
