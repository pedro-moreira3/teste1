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

import br.com.lume.common.connection.GenericListDAO;
import br.com.lume.common.exception.business.BusinessException;
import br.com.lume.common.exception.techinical.TechnicalException;
import br.com.lume.common.util.Status;
import br.com.lume.common.util.Utils;
import br.com.lume.odonto.dao.PersistenceUnitName;
import br.com.lume.odonto.entity.Lancamento;
import br.com.lume.odonto.entity.Orcamento;
import br.com.lume.odonto.entity.Paciente;
import br.com.lume.odonto.entity.PlanoTratamento;
import br.com.lume.odonto.entity.PlanoTratamentoProcedimento;

public class LancamentoBO extends BO<Lancamento> {

    /**
     *
     */
    private static final long serialVersionUID = 3446396992517527567L;
    private Logger log = Logger.getLogger(LancamentoBO.class);

    public LancamentoBO() {
        super(PersistenceUnitName.ODONTO);
        this.setClazz(Lancamento.class);
    }

    @Override
    public boolean remove(Lancamento lancamento) throws BusinessException, TechnicalException {
        lancamento.setExcluido(Status.SIM);
        lancamento.setDataExclusao(Calendar.getInstance().getTime());
        lancamento.setExcluidoPorProfissional(ProfissionalBO.getProfissionalLogado().getId());
        this.persist(lancamento);
        return true;
    }

    // public List<Lancamento> listByPlanoTratamento(PlanoTratamento planoTratamento) {
    // try {
    // if (planoTratamento != null ) {
    // String jpql = "select vo from Lancamento as vo" +
    // " WHERE vo.orcamento.planoTratamento.id = :planoTratamento AND vo.excluido= :excluido "
    // + " ";
    // Query q = getDao().createQuery(jpql);
    // q.setParameter("planoTratamento", planoTratamento.getId());
    // q.setParameter("excluido", Status.NAO);
    // return list(q);
    // }
    // } catch (Exception e) {
    // log.error("Erro no listByDataAndProfissional", e);
    // }
    // return null;
    // }
    public List<Lancamento> listByPlanoTratamento(PlanoTratamento planoTratamento) {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("orcamento.planoTratamento", planoTratamento);
            parametros.put("excluido", Status.NAO);
            return this.listByFields(parametros);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Lancamento> listByPagamentoPacienteNaoValidado() throws Exception {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("orcamento.planoTratamento.profissional.idEmpresa", ProfissionalBO.getProfissionalLogado().getIdEmpresa());
            parametros.put("excluido", Status.NAO);
            parametros.put("validado", Status.NAO);
            parametros.put("o.formaPagamento in ('CD','CC','BO','CH') ", GenericListDAO.FILTRO_GENERICO_QUERY);
            parametros.put("o.dataCredito is not null", GenericListDAO.FILTRO_GENERICO_QUERY);
            return this.listByFields(parametros, new String[] { "dataCredito asc " });
        } catch (Exception e) {
            log.error("Erro no listByEmpresa", e);
        }
        return null;
    }

    public Long findParcelaMaximaByOrcamento(long idOrcamento, BigDecimal valor, String formaPagamento) {
        try {
            StringBuffer sb = new StringBuffer();

//            sb.append("SELECT ");
//            sb.append("NUMERO_PARCELA ");
//            sb.append("FROM LANCAMENTO M WHERE ID_ORCAMENTO = ?1 ");
//            sb.append("ORDER BY NUMERO_PARCELA DESC ");
//            sb.append("FETCH FIRST 1 ROWS ONLY ");

            sb.append("SELECT ");
            sb.append("COUNT(1) ");
            sb.append("FROM LANCAMENTO M WHERE ");
            sb.append("ID_ORCAMENTO = ?1 AND ");
            sb.append("FORMA_PAGAMENTO = ?2 AND ");
            sb.append("EXCLUIDO = 'N' ");
            // "AND ");
         //   sb.append("VALOR = ?3 ");

            Query query = this.getDao().createNativeQuery(sb.toString());
            query.setParameter(1, idOrcamento);
            query.setParameter(2, formaPagamento);
            query.setParameter(3, valor);
            return (Long) query.getSingleResult();
        } catch (Exception e) {
            log.error("Erro no findParcelaMaximaByOrcamento", e);
        }
        return 0L;
    }

    public List<Lancamento> listLancamentosAbertosByPlanoTratamento(long idPlanoTratamento) {
        try {
            StringBuffer sb = new StringBuffer();
            sb.append("SELECT ");
            sb.append("* ");
            sb.append("FROM LANCAMENTO L,ORCAMENTO O ");
            sb.append("WHERE ");
            sb.append("O.ID_PLANO_TRATAMENTO = ?1 AND ");
            sb.append("L.ID_ORCAMENTO = O.ID AND ");
            sb.append("L.EXCLUIDO = 'N' AND ");
            sb.append("L.DATA_PAGAMENTO IS NULL ");
            Query query = this.getDao().createNativeQuery(sb.toString(), Lancamento.class);
            query.setParameter(1, idPlanoTratamento);
            return query.getResultList();
        } catch (Exception e) {
            log.error("Erro no listLancamentosAbertosByPlanoTratamento", e);
        }
        return null;
    }

    public List<Lancamento> listComCredito(long idplanotratamento) {
        try {
            StringBuffer sb = new StringBuffer();
            sb.append("SELECT ");
            sb.append("* ");
            sb.append("FROM LANCAMENTO LAN,ORCAMENTO ORC ");
            sb.append("WHERE ");
            sb.append("LAN.EXCLUIDO = 'N' AND ");
            sb.append("LAN.ID_ORCAMENTO = ORC.ID AND ");
            sb.append("ORC.ID_PLANO_TRATAMENTO = ?1 AND ");
            sb.append("COALESCE(LAN.VALOR_REPASSADO,0) < COALESCE(LAN.VALOR,0) AND ");
            sb.append("LAN.DATA_PAGAMENTO IS NOT NULL AND ");
            sb.append("LAN.DATA_CREDITO IS NOT NULL AND LAN.DATA_CREDITO <= CURRENT_DATE AND LAN.VALIDADO = 'S' ");
            sb.append("ORDER BY LAN.NUMERO_PARCELA ASC ");
            Query query = this.getDao().createNativeQuery(sb.toString(), Lancamento.class);
            query.setParameter(1, idplanotratamento);
            return query.getResultList();
        } catch (Exception e) {
            log.error("Erro no listComCredito", e);
        }
        return null;
    }

    public List<Lancamento> listByPlanoTratamentoOrcamentoNaoExcluido(PlanoTratamento planoTratamento) {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("orcamento.planoTratamento", planoTratamento);
            parametros.put("orcamento.excluido", Status.NAO);
            parametros.put("excluido", Status.NAO);
            return this.listByFields(parametros);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private String tratarCamposInPlanoTratamento(List<PlanoTratamento> planos) {
        String out = "";
        for (PlanoTratamento p : planos) {
            out += "," + p.getId();
        }
        out = out.replaceFirst(",", "");
        return out;
    }

    private String tratarCamposIn(List<Paciente> pacientes) {
        String out = "";
        for (Paciente p : pacientes) {
            out += "," + p.getId();
        }
        out = out.replaceFirst(",", "");
        return out;
    }

    public List<Lancamento> listByPacienteAndDependentes(Paciente paciente) throws Exception {
        Map<String, Object> parametros = new HashMap<>();
        List<Paciente> pacientes = new PacienteBO().listByTitular(paciente);
        pacientes.add(paciente);
        parametros.put("o.orcamento.planoTratamento.paciente.id in (" + this.tratarCamposIn(pacientes) + ")", GenericListDAO.FILTRO_GENERICO_QUERY);
        parametros.put("orcamento.planoTratamento.excluido", Status.NAO);
        parametros.put("orcamento.excluido", Status.NAO);
        parametros.put("excluido", Status.NAO);
        return this.listByFields(parametros, new String[] { "orcamento.planoTratamento.id desc ", "numeroParcela desc " });
    }

    public List<Lancamento> listByFiltros(Paciente paciente, List<PlanoTratamento> listPt, Date inicio, Date fim, String status) throws Exception {
        Map<String, Object> parametros = new HashMap<>();
        List<Paciente> pacientes = new PacienteBO().listByTitular(paciente);
        pacientes.add(paciente);
        parametros.put("o.orcamento.planoTratamento.paciente.id in (" + this.tratarCamposIn(pacientes) + ")", GenericListDAO.FILTRO_GENERICO_QUERY);
        parametros.put("orcamento.planoTratamento.excluido", Status.NAO);
        parametros.put("orcamento.excluido", Status.NAO);
        parametros.put("excluido", Status.NAO);
        if (listPt != null && !listPt.isEmpty()) {
            parametros.put("o.orcamento.planoTratamento.id in (" + this.tratarCamposInPlanoTratamento(listPt) + ")", GenericListDAO.FILTRO_GENERICO_QUERY);
        }
        this.findByPeriodoStatus(inicio, fim, status, parametros);
        return this.filtraStatus(this.listByFields(parametros, new String[] { "dataCredito asc", "orcamento.planoTratamento.id desc ", "numeroParcela desc " }), status);
    }

    public List<Lancamento> listByOrcamento(Orcamento orcamento) {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("orcamento", orcamento);
            // parametros.put("excluido", Status.NAO);
            return this.listByFields(parametros, new String[] { "id desc" });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Lancamento> listByOrcamentoPagosAndAtivos(Orcamento orcamento) {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("orcamento", orcamento);
            parametros.put("excluido", Status.NAO);
            return this.listByFields(parametros);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Lancamento> listByOrcamentoAndPagoAndMesmoTipo(Orcamento orcamento, String formaPagamento) {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("orcamento", orcamento);
            parametros.put("o.dataPagamento is null", GenericListDAO.FILTRO_GENERICO_QUERY);
            parametros.put("excluido", Status.NAO);
            parametros.put("formaPagamento", formaPagamento);
            return this.listByFields(parametros);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Lancamento> listLancamentosNaoPagos(Orcamento orcamento) {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("orcamento", orcamento);
            parametros.put("o.dataPagamento is null", GenericListDAO.FILTRO_GENERICO_QUERY);
            parametros.put("excluido", Status.NAO);
            return this.listByFields(parametros);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Lancamento> listByEmpresa() {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("orcamento.planoTratamento.profissional.idEmpresa", ProfissionalBO.getProfissionalLogado().getIdEmpresa());
            parametros.put("excluido", Status.NAO);
            return this.listByFields(parametros);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Lancamento> listByPlanoTratamentoAndData(PlanoTratamento planoTratamento) {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("orcamento.planoTratamento", planoTratamento);
            parametros.put("o.dataPagamento is not null", GenericListDAO.FILTRO_GENERICO_QUERY);
            parametros.put("excluido", Status.NAO);
            return this.listByFields(parametros);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Lancamento> listAllByPeriodoAndStatus(Date inicio, Date fim, String s) throws Exception {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("orcamento.profissional.idEmpresa", ProfissionalBO.getProfissionalLogado().getIdEmpresa());
            this.findByPeriodoStatus(inicio, fim, s, parametros);
            return this.filtraStatus(this.listByFields(parametros), s);
        } catch (Exception e) {
            log.error("Erro no listAllByPeriodoAndStatus", e);
        }
        return null;
    }

    private List<Lancamento> filtraStatus(List<Lancamento> lancamentos, String s) {
        if (lancamentos != null && !lancamentos.isEmpty() && s != null && !s.equals("") && !s.equals("T")) {
            List<Lancamento> retorno = new ArrayList<>();
            for (Lancamento lancamento : lancamentos) {
                if (lancamento.getStatus() != null && lancamento.getStatus().equals(s)) {
                    retorno.add(lancamento);
                }
            }
            return retorno;
        } else {
            return lancamentos;
        }
    }

    private void findByPeriodoStatus(Date inicio, Date fim, String s, Map<String, Object> parametros) {
        if (inicio != null && fim != null) {
            Calendar c = Calendar.getInstance();
            c.setTime(fim);
            c.set(Calendar.HOUR_OF_DAY, 23);
            c.set(Calendar.MINUTE, 59);
            c.set(Calendar.SECOND, 59);
            c.set(Calendar.MILLISECOND, 999);
            fim = c.getTime();
            parametros.put("o.dataCredito between '" + Utils.dateToString(inicio, "yyyy-MM-dd HH:mm:ss") + "' and '" + Utils.dateToString(fim, "yyyy-MM-dd HH:mm:ss") + "' ",
                    GenericListDAO.FILTRO_GENERICO_QUERY);
        }
    }

    public Lancamento findByPlanoTratamentoProcedimento(PlanoTratamentoProcedimento ptp) {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("planoTratamentoProcedimento", ptp);
            parametros.put("excluido", Status.NAO);
            parametros.put("o.dataPagamento is not null", GenericListDAO.FILTRO_GENERICO_QUERY);
            return this.findByFields(parametros);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
