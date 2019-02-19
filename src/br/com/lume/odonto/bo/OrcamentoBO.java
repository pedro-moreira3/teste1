package br.com.lume.odonto.bo;

import java.math.BigDecimal;
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
import br.com.lume.odonto.entity.Lancamento;
import br.com.lume.odonto.entity.Orcamento;
import br.com.lume.odonto.entity.PlanoTratamento;

public class OrcamentoBO extends BO<Orcamento> {

    /**
     *
     */
    private static final long serialVersionUID = 1085766932824688606L;
    private Logger log = Logger.getLogger(OrcamentoBO.class);

    public OrcamentoBO() {
        super(PersistenceUnitName.ODONTO);
        this.setClazz(Orcamento.class);
    }

    @Override
    public boolean remove(Orcamento orcamento) throws BusinessException, TechnicalException {
        for (Lancamento l : orcamento.getLancamentos()) {
            if (!l.getStatus().equals("Pago")) {
                new LancamentoBO().remove(l);
            }
        }
        orcamento.setExcluido(Status.SIM);
        orcamento.setDataExclusao(Calendar.getInstance().getTime());
        orcamento.setExcluidoPorProfissional(ProfissionalBO.getProfissionalLogado().getId());
        this.persist(orcamento);
        return true;
    }

    public BigDecimal valorTotalAPagar(PlanoTratamento planoTratamento, boolean ortodontico) {
        try {
            StringBuffer sb = new StringBuffer();

            sb.append("SELECT ");
            sb.append("COALESCE(SUM(L.VALOR),0) ");
            sb.append("FROM LANCAMENTO L ");
            sb.append("WHERE L.ID_ORCAMENTO IN ");
            sb.append("( ");
            sb.append("   SELECT ");
            sb.append("   ID ");
            sb.append("   FROM ORCAMENTO ");
            sb.append("   WHERE ID_PLANO_TRATAMENTO = ?1 ");

            if (!ortodontico) {
                sb.append("ORDER BY ID DESC FETCH FIRST 1 ROW ONLY ");
            }

            sb.append(") ");
            sb.append("AND L.EXCLUIDO = 'N' ");
            sb.append("AND L.DATA_PAGAMENTO IS NULL ");

            Query query = this.getDao().createNativeQuery(sb.toString());
            query.setParameter(1, planoTratamento.getId());
            List<Object> resultList = query.getResultList();
            BigDecimal apagar = resultList != null && !resultList.isEmpty() ? (BigDecimal) resultList.get(0) : new BigDecimal(0);
            return apagar;
            //return valorTotal(planoTratamento, ortodontico).subtract(pagos);
        } catch (Exception e) {
            log.error("Erro no findUltimoOrcamentoEmAberto", e);
        }
        return null;
    }

    public BigDecimal valorTotal(PlanoTratamento planoTratamento, boolean ortodontico) {
        try {
            StringBuffer sb = new StringBuffer();

            if (!ortodontico) {
                sb.append("SELECT COALESCE(SUM(VALOR_DESCONTO),0) FROM PLANO_TRATAMENTO_PROCEDIMENTO PTP ");
                sb.append("WHERE PTP.ID_PLANO_TRATAMENTO = ?1 AND EXCLUIDO = 'N' ");
            } else {
                sb.append("SELECT ");
                sb.append("COALESCE(SUM(O.VALOR_TOTAL),0) ");
                sb.append("FROM ORCAMENTO O ");
                sb.append("WHERE O.ID_PLANO_TRATAMENTO = ?1 ");
            }

            Query query = this.getDao().createNativeQuery(sb.toString());
            query.setParameter(1, planoTratamento.getId());
            List<Object> resultList = query.getResultList();
            return resultList != null && !resultList.isEmpty() ? (BigDecimal) resultList.get(0) : new BigDecimal(0);
        } catch (Exception e) {
            log.error("Erro no findUltimoOrcamentoEmAberto", e);
        }
        return null;
    }

    public BigDecimal valorTotalDesconto(PlanoTratamento planoTratamento) {
        BigDecimal desconto = new BigDecimal(0);
        Orcamento orcamento = this.findByPlanoTratamento(planoTratamento);
        if (orcamento != null) {
            desconto = orcamento.getValorTotal().subtract(planoTratamento.getValorTotalDesconto()).negate();
        }
        return desconto;
    }

    public Orcamento findByPlanoTratamento(PlanoTratamento planoTratamento) {
        try {
            StringBuffer sb = new StringBuffer();
            sb.append("SELECT ");
            sb.append("* ");
            sb.append("FROM ORCAMENTO O ");
            sb.append("WHERE O.ID_PLANO_TRATAMENTO = ?1 ");
            sb.append("AND O.EXCLUIDO = 'N' ORDER BY O.ID DESC FETCH FIRST 1 ROW ONLY ");

            Query query = this.getDao().createNativeQuery(sb.toString(), Orcamento.class);
            query.setParameter(1, planoTratamento.getId());
            List<Orcamento> resultList = query.getResultList();
            return resultList != null && !resultList.isEmpty() ? resultList.get(0) : null;
        } catch (Exception e) {
            log.error("Erro no findByPlanoTratamento", e);
        }
        return null;
    }

    public Orcamento findUltimoOrcamentoEmAberto(long idPlanoTratamento) {
        try {
            StringBuffer sb = new StringBuffer();
            sb.append("SELECT ");
            sb.append("* ");
            sb.append("FROM ORCAMENTO O ");
            sb.append("WHERE O.ID_PLANO_TRATAMENTO = ?1 ");
            sb.append("AND EXISTS ");
            sb.append("( ");
            sb.append("   SELECT ");
            sb.append("   1 ");
            sb.append("   FROM LANCAMENTO L ");
            sb.append("   WHERE L.ID_ORCAMENTO = O.ID ");
            sb.append("   AND L.DATA_PAGAMENTO IS NULL ");
            sb.append("   AND L.EXCLUIDO = 'N' ");
            sb.append(") ORDER BY O.ID DESC FETCH FIRST 1 ROW ONLY ");

            Query query = this.getDao().createNativeQuery(sb.toString(), Orcamento.class);
            query.setParameter(1, idPlanoTratamento);
            List<Orcamento> resultList = query.getResultList();
            return resultList != null && !resultList.isEmpty() ? resultList.get(0) : null;
        } catch (Exception e) {
            log.error("Erro no findUltimoOrcamentoEmAberto", e);
        }
        return null;
    }

    public Orcamento findUltimoOrcamentoOrtodontico(long idPlanoTratamento) {
        try {
            StringBuffer sb = new StringBuffer();
            sb.append("SELECT ");
            sb.append("* ");
            sb.append("FROM ORCAMENTO O ");
            sb.append("WHERE O.ID_PLANO_TRATAMENTO = ?1 AND ORTODONTICO = TRUE ");
            sb.append("ORDER BY O.ID DESC FETCH FIRST 1 ROW ONLY ");

            Query query = this.getDao().createNativeQuery(sb.toString(), Orcamento.class);
            query.setParameter(1, idPlanoTratamento);
            List<Orcamento> resultList = query.getResultList();
            return resultList != null && !resultList.isEmpty() ? resultList.get(0) : null;
        } catch (Exception e) {
            log.error("Erro no findUltimoOrcamentoOrtodontico", e);
        }
        return null;
    }

    public BigDecimal findValorUltimoOrcamento(long idPlanoTratamento) {
        try {
            StringBuffer sb = new StringBuffer();
            sb.append("SELECT SUM(VALOR) FROM LANCAMENTO WHERE ID_ORCAMENTO = ( ");
            sb.append("SELECT ");
            sb.append("ID ");
            sb.append("FROM ORCAMENTO O ");
            sb.append("WHERE O.ID_PLANO_TRATAMENTO = ?1 AND EXCLUIDO = 'N' ");
            sb.append("ORDER BY O.ID DESC FETCH FIRST 1 ROW ONLY ) AND EXCLUIDO = 'N' ");

            Query query = this.getDao().createNativeQuery(sb.toString());
            query.setParameter(1, idPlanoTratamento);
            List<Object> resultList = query.getResultList();
            return resultList != null && !resultList.isEmpty() ? (BigDecimal) resultList.get(0) : null;
        } catch (Exception e) {
            log.error("Erro no findValorUltimoOrcamento", e);
        }
        return null;
    }

    public List<Orcamento> listByPlanoTratamento(PlanoTratamento planoTratamento) {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("excluido", Status.NAO);
            parametros.put("planoTratamento", planoTratamento);
            return this.listByFields(parametros, new String[] { "dataAprovacao desc" });
        } catch (Exception e) {
            log.error("Erro no ListByPlanoTratamento", e);
        }
        return null;
    }

    public List<Orcamento> listByPlanoTratamentoOrtodontico(PlanoTratamento planoTratamento) {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("excluido", Status.NAO);
            parametros.put("ortodontico", true);
            parametros.put("planoTratamento", planoTratamento);
            return this.listByFields(parametros, new String[] { "dataAprovacao asc" });
        } catch (Exception e) {
            log.error("Erro no ListByPlanoTratamento", e);
        }
        return null;
    }

    public List<Orcamento> ListAllByPlanoTratamento(PlanoTratamento planoTratamento) {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("planoTratamento", planoTratamento);
            return this.listByFields(parametros, new String[] { "dataAprovacao desc" });
        } catch (Exception e) {
            log.error("Erro no ListByPlanoTratamento", e);
        }
        return null;
    }

    public List<Orcamento> ListByPlanoTratamentoExcluido(PlanoTratamento planoTratamento) {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("planoTratamento", planoTratamento);
            return this.listByFields(parametros, new String[] { "dataAprovacao desc" });
        } catch (Exception e) {
            log.error("Erro no ListByPlanoTratamento", e);
        }
        return null;
    }

    public BigDecimal findValorPago(Orcamento o) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT COALESCE(SUM(L.VALOR),0) AS VALOR_PAGO ");
        sb.append("FROM LANCAMENTO L, ORCAMENTO O ");
        sb.append("WHERE ");
        sb.append("L.ID_ORCAMENTO = O.ID AND ");
        sb.append("O.ID = ?1 AND ");
        sb.append("O.EXCLUIDO = 'N' AND ");
        sb.append("L.EXCLUIDO = 'N' AND ");
        sb.append("L.DATA_PAGAMENTO IS NOT NULL ");

        Query query = this.getDao().createNativeQuery(sb.toString());
        query.setParameter(1, o.getId());
        return (BigDecimal) query.getSingleResult();
    }
}
