package br.com.lume.odonto.bo;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

import org.apache.log4j.Logger;
import org.eclipse.persistence.config.HintValues;
import org.eclipse.persistence.config.QueryHints;

import br.com.lume.common.connection.GenericListDAO;
import br.com.lume.common.exception.business.BusinessException;
import br.com.lume.common.exception.techinical.TechnicalException;
import br.com.lume.common.util.Status;
import br.com.lume.common.util.Utils;
import br.com.lume.odonto.dao.PersistenceUnitName;
import br.com.lume.odonto.entity.Dominio;
import br.com.lume.odonto.entity.Odontograma;
import br.com.lume.odonto.entity.Paciente;
import br.com.lume.odonto.entity.PlanoTratamento;
import br.com.lume.odonto.entity.PlanoTratamentoProcedimento;
import br.com.lume.odonto.entity.Procedimento;
import br.com.lume.odonto.entity.Profissional;
import br.com.lume.odonto.util.OdontoMensagens;

public class PlanoTratamentoBO extends BO<PlanoTratamento> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(PlanoTratamentoBO.class);

    public PlanoTratamentoBO() {
        super(PersistenceUnitName.ODONTO);
        this.setClazz(PlanoTratamento.class);
    }

    @Override
    public PlanoTratamento find(Object id) throws Exception {

        try {
            StringBuffer sb = new StringBuffer();
            sb.append("SELECT * FROM PLANO_TRATAMENTO PTP WHERE ");
            sb.append("ID = ?1");
            Query query = this.getDao().createNativeQuery(sb.toString(), PlanoTratamento.class);
            query.setHint("javax.persistence.cache.storeMode", "REFRESH");
            query.setParameter(1, id);
            List<PlanoTratamento> resultList = query.getResultList();
            return resultList != null && !resultList.isEmpty() ? resultList.get(0) : null;
        } catch (Exception e) {
            log.error("Erro no find", e);
        }
        return null;
    }

    public void finalizaPlanoBatch() {
        List<Dominio> dominios = new DominioBO().listByObjetoAndTipoAndNome("planotratamento", "finalizabach", "finalizabach");
        if (dominios != null && dominios.isEmpty()) {
            for (Dominio dominio : dominios) {
                List<PlanoTratamento> planos = this.listByData(dominio);
                if (planos != null && !planos.isEmpty()) {
                    for (PlanoTratamento plano : planos) {
                        plano.setJustificativa(OdontoMensagens.getMensagemOffLine("planotratamento.finaliza.bach"));
                        plano.setFinalizado(Status.SIM);
                        plano.setDataFinalizado(new Date());
                        try {
                            new PlanoTratamentoBO().persist(plano);
                        } catch (Exception e) {
                            log.error("ERRO FINALIZA PLANO finalizaPlanoBatch", e);
                        }
                    }
                }
            }
        }
    }

    public PlanoTratamento persistPlano(Paciente paciente, Profissional profissional) throws Exception {
        ProcedimentoBO procedimentoBO = new ProcedimentoBO();
        PlanoTratamentoBO planoTratamentoBO = new PlanoTratamentoBO();
        PlanoTratamentoProcedimentoBO planoTratamentoProcedimentoBO = new PlanoTratamentoProcedimentoBO();
        Procedimento procedimentoInicial = procedimentoBO.findByProcedimentoInicial();
        if (procedimentoInicial != null) {
            PlanoTratamento pt = new PlanoTratamento();
            pt.setDescricao("PT Exame Clínico Inicial");
            pt.setFinalizado(Status.NAO);
            pt.setDataHora(Calendar.getInstance().getTime());
            pt.setPaciente(paciente);
            pt.setProfissional(profissional);
            PlanoTratamentoProcedimento ptp = new PlanoTratamentoProcedimento();
            ptp.setPlanoTratamento(pt);
            ptp.setProcedimento(procedimentoInicial);
            ptp.setValor(procedimentoInicial.getValor());
            ptp.setValorDesconto(procedimentoInicial.getValor());
            pt.setValorTotal(ptp.getValor());
            pt.setValorTotalDesconto(ptp.getValorDesconto());
            planoTratamentoBO.persist(pt);
            planoTratamentoProcedimentoBO.persist(ptp);
            return pt;
        }
        return null;
    }

    public List<PlanoTratamento> listByData(Dominio dominio) {
        try {
            String jpql = " select vo from PlanoTratamento as vo" + " where vo.dataHora between :ini and :fim" + " and vo.finalizado = 'N' AND  vo.excluido= :excluido" + " AND vo.profissional.idEmpresa = :idEmpresa";
            Query q = this.getDao().createQuery(jpql);
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_MONTH, -Integer.parseInt(dominio.getValor()));
            cal.set(Calendar.AM_PM, Calendar.AM);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            Date ini = cal.getTime();
            cal.set(Calendar.AM_PM, Calendar.PM);
            cal.set(Calendar.HOUR_OF_DAY, 11);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
            Date fim = cal.getTime();
            q.setParameter("ini", ini);
            q.setParameter("fim", fim);
            q.setParameter("excluido", Status.NAO);
            q.setParameter("idEmpresa", dominio.getIdEmpresa());
            log.warn("Hora Inicio : " + ini);
            log.warn("Hora Fim : " + fim);
            return this.list(q);
        } catch (Exception e) {
            log.error("ERRO FINALIZA PLANO listByData: ", e);
        }
        return null;
    }

    @Override
    public boolean remove(PlanoTratamento planoTratamento) throws BusinessException, TechnicalException {
        planoTratamento.setExcluido(Status.SIM);
        planoTratamento.setDataExclusao(Calendar.getInstance().getTime());
        planoTratamento.setExcluidoPorProfissional(ProfissionalBO.getProfissionalLogado().getId());
        this.persist(planoTratamento);
        return true;
    }

    public List<PlanoTratamento> listByPaciente(Paciente paciente) {
        try {
            if (paciente != null) {
                final Query readQuery = getDao().createQuery("select pt from PlanoTratamento pt where pt.paciente.id = :id and pt.excluido = 'N' order by pt.dataHora desc ");
                readQuery.setParameter("id", paciente.getId());
                readQuery.setHint(QueryHints.REFRESH, HintValues.TRUE);
                return readQuery.getResultList();
            }
        } catch (Exception e) {
            log.error("Erro no listByPaciente", e);
        }
        return null;
    }

    public List<PlanoTratamento> listAtivosByPaciente(Paciente paciente) {
        try {
            if (paciente != null) {
                Map<String, Object> parametros = new HashMap<>();
                parametros.put("paciente.id", paciente.getId());
                parametros.put("excluido", Status.NAO);
                parametros.put("finalizado", Status.NAO);
                return this.listByFields(parametros, new String[] { "dataHora desc" });
            }
        } catch (Exception e) {
            log.error("Erro no listByPaciente", e);
        }
        return null;
    }

    public List<PlanoTratamento> listByPacienteAndValorTotalDesconto(Paciente paciente) {
        try {
            if (paciente != null) {
                Map<String, Object> parametros = new HashMap<>();
                parametros.put("paciente.id", paciente.getId());
                parametros.put("excluido", Status.NAO);
                parametros.put("o.valorTotalDesconto >= 0", GenericListDAO.FILTRO_GENERICO_QUERY);
                return this.listByFields(parametros, new String[] { "dataHora desc" });
            }
        } catch (Exception e) {
            log.error("Erro no listByPaciente", e);
        }
        return null;
    }

    public List<PlanoTratamento> listPTByDateAndStatus(Date inicio, Date fim, String status) {
        try {
            StringBuffer sb = new StringBuffer();
            sb.append("SELECT ");
            sb.append("PT.* ");
            sb.append("FROM ");
            sb.append("PLANO_TRATAMENTO PT, PROFISSIONAL P ");
            sb.append("WHERE ");
            sb.append("PT.EXCLUIDO = 'N' AND ");
            sb.append("PT.ID_PROFISSIONAL = P.ID AND ");
            sb.append("P.ID_EMPRESA = ?1 AND ");
            sb.append("DATE(PT.DATA_HORA) BETWEEN '" + Utils.dateToString(inicio, "yyyy-MM-dd") + "' AND '" + Utils.dateToString(fim, "yyyy-MM-dd") + "' AND ");
            sb.append("PT.FINALIZADO = ?2 ");
            sb.append("ORDER BY PT.DATA_HORA DESC ");
            Query query = this.getDao().createNativeQuery(sb.toString(), PlanoTratamento.class);
            query.setParameter(1, ProfissionalBO.getProfissionalLogado().getIdEmpresa());
            query.setParameter(2, status);

            return this.list(query);
        } catch (Exception e) {
            log.error("Erro no listAllByFilterToReport", e);
        }
        return null;
    }

    public BigDecimal findValorPagoByPlanoTratamento(Long idPlanoTratamento) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT SUM(LAN.VALOR) FROM PLANO_TRATAMENTO PT, ORCAMENTO ORC, LANCAMENTO LAN ");
        sb.append("WHERE ");
        sb.append("PT.ID = ORC.ID_PLANO_TRATAMENTO AND ");
        sb.append("ORC.ID = LAN.ID_ORCAMENTO AND ");
        sb.append("LAN.EXCLUIDO = 'N' AND ");
        sb.append("LAN.DATA_PAGAMENTO IS NOT NULL AND ");
        sb.append("PT.ID = ?1 ");

        Query query = this.getDao().createNativeQuery(sb.toString());
        query.setParameter(1, idPlanoTratamento);
        Object r = query.getSingleResult();
        return r != null ? (BigDecimal) r : null;
    }

    public Integer findProcedimentosEmAbertoByPlanoTratamento(Long idPlanoTratamento) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT COUNT(PTT.ID)::integer FROM PLANO_TRATAMENTO PT, PLANO_TRATAMENTO_PROCEDIMENTO PTT ");
        sb.append("WHERE ");
        sb.append("PT.ID = PTT.ID_PLANO_TRATAMENTO AND ");
        sb.append("PTT.STATUS IS NULL AND ");
        sb.append("PT.ID = ?1 ");

        Query query = this.getDao().createNativeQuery(sb.toString());
        query.setParameter(1, idPlanoTratamento);
        Object r = query.getSingleResult();
        return r != null ? (Integer) r : null;
    }

    public static void main(String[] args) {
        try {
            System.out.println(new PlanoTratamentoBO().findProcedimentosEmAbertoByPlanoTratamento(4101L));
            //System.out.println(new PlanoTratamentoBO().findValorPagarByPlanoTratamento(14101L));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public Integer findDescontoByPlanoTratamento(Long idPlanoTratamento) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT ");
        sb.append("100-((O.VALOR_TOTAL * 100) / PT.VALOR_TOTAL)::INTEGER ");
        sb.append("FROM PLANO_TRATAMENTO PT, ORCAMENTO O ");
        sb.append("WHERE PT.ID = ?1 AND PT.ID = O.ID_PLANO_TRATAMENTO ");

        Query query = this.getDao().createNativeQuery(sb.toString());
        query.setParameter(1, idPlanoTratamento);
        Object r = query.getSingleResult();
        return r != null ? (Integer) r : null;
    }

    public BigDecimal findValorPagarByPlanoTratamento(Long idPlanoTratamento) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT SUM(PTT.VALOR_DESCONTO) FROM PLANO_TRATAMENTO PT, PLANO_TRATAMENTO_PROCEDIMENTO PTT ");
        sb.append("WHERE ");
        sb.append("PT.ID = PTT.ID_PLANO_TRATAMENTO AND ");
        sb.append("PTT.STATUS = 'F' AND ");
        sb.append("PT.ID = ?1 ");

        Query query = this.getDao().createNativeQuery(sb.toString());
        query.setParameter(1, idPlanoTratamento);
        Object r = query.getSingleResult();
        return r != null ? (BigDecimal) r : null;
    }

    public List<PlanoTratamento> listByOdontograma(Odontograma odontograma) throws Exception {
        Map<String, Object> parametros = new HashMap<>();
        parametros.put("odontograma.id", odontograma.getId());
        parametros.put("excluido", Status.NAO);
        return this.listByFields(parametros, new String[] { "dataHora desc" });
    }

    public List<PlanoTratamento> listByPacienteOrtodontia(Paciente paciente) throws Exception {
        try {
            if (paciente != null) {
                Map<String, Object> parametros = new HashMap<>();
                parametros.put("paciente.id", paciente.getId());
                parametros.put("excluido", Status.NAO);
                parametros.put("ortodontico", true);
                return this.listByFields(parametros, new String[] { "dataHora desc" });
            }
        } catch (Exception e) {
            log.error("Erro no listByPaciente", e);
        }
        return null;

    }

    public void carregarValoresOrtodontia(PlanoTratamento pt) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT ");
        sb.append("VALOR_PAGO, ");
        sb.append("VALOR_TOTAL-VALOR_PAGO AS VALOR_ORCAMENTO_RESTANTE, ");
        sb.append("VALOR_PT - VALOR_PAGO AS VALOR_PT_RESTANTE ");
        sb.append("FROM ( ");
        sb.append(" SELECT ");
        sb.append(" (SELECT SUM(O.VALOR_TOTAL) ");
        sb.append(" FROM ORCAMENTO O ");
        sb.append(" WHERE ");
        sb.append(" O.ID_PLANO_TRATAMENTO = ?1 AND ");
        sb.append(" O.EXCLUIDO = 'N') AS VALOR_TOTAL, ");
        sb.append(" (SELECT COALESCE(SUM(L.VALOR),0) AS VALOR_PAGO ");
        sb.append(" FROM LANCAMENTO L, ORCAMENTO O ");
        sb.append(" WHERE ");
        sb.append(" L.ID_ORCAMENTO = O.ID AND ");
        sb.append(" O.ID_PLANO_TRATAMENTO = ?1 AND ");
        sb.append(" O.EXCLUIDO = 'N' AND ");
        sb.append(" L.EXCLUIDO = 'N' AND ");
        sb.append(" L.DATA_PAGAMENTO IS NOT NULL) AS VALOR_PAGO, ");
        sb.append(" (SELECT VALOR_TOTAL AS VALOR_PT FROM PLANO_TRATAMENTO WHERE ID = ?1) AS VALOR_PT ");
        sb.append(") AS CONTA ");

        Query query = this.getDao().createNativeQuery(sb.toString());
        query.setParameter(1, pt.getId());
        List<Object[]> resultList = query.getResultList();
        if (resultList != null && !resultList.isEmpty()) {
            pt.setValorPago((BigDecimal) resultList.get(0)[0]);
            pt.setValorOrcamentoRestante((BigDecimal) resultList.get(0)[1]);
            pt.setValorTotalRestante((BigDecimal) resultList.get(0)[2]);
        }

    }

    public void carregarNovoValorTotal(PlanoTratamento pt, BigDecimal novoValor) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT ");
        sb.append("((PT.MESES - (SELECT SUM(O.QUANTIDADE_PARCELAS) FROM ORCAMENTO O WHERE O.ID_PLANO_TRATAMENTO = PT.ID)) * ");
        sb.append("" + novoValor.doubleValue() + ") + (SELECT SUM(O.VALOR_TOTAL) FROM ORCAMENTO O WHERE O.ID_PLANO_TRATAMENTO = PT.ID) ");
        sb.append("FROM ");
        sb.append("PLANO_TRATAMENTO PT ");
        sb.append("WHERE PT.ID = ?1 ");

        Query query = this.getDao().createNativeQuery(sb.toString());
        query.setParameter(1, pt.getId());
        pt.setValorTotal((BigDecimal) query.getSingleResult());
        pt.setValorTotalDesconto((BigDecimal) query.getSingleResult());
    }

}
