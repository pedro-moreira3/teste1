package br.com.lume.odonto.bo;

import java.math.BigDecimal;
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
import br.com.lume.odonto.entity.LancamentoContabil;
import br.com.lume.odonto.entity.LancamentoContabilRelatorio;
import br.com.lume.odonto.entity.Motivo;
import br.com.lume.odonto.entity.NotaFiscal;

public class LancamentoContabilBO extends BO<LancamentoContabil> {

    private Logger log = Logger.getLogger(LancamentoContabil.class);

    private static final long serialVersionUID = 1L;

    public LancamentoContabilBO() {
        super(PersistenceUnitName.ODONTO);
        this.setClazz(LancamentoContabil.class);
    }

    @Override
    public List<LancamentoContabil> listAll() throws Exception {
        return this.listByEmpresa();
    }

    @Override
    public boolean remove(LancamentoContabil lancamentoContabil) throws BusinessException, TechnicalException {
        lancamentoContabil.setExcluido(Status.SIM);
        lancamentoContabil.setDataExclusao(Calendar.getInstance().getTime());
        lancamentoContabil.setExcluidoPorProfissional(ProfissionalBO.getProfissionalLogado().getId());
        this.persist(lancamentoContabil);
        return true;
    }

    public static void main(String[] args) {
        try {
            LancamentoContabilBO lBo = new LancamentoContabilBO();
            List<LancamentoContabil> listByEmpresa2 = lBo.listByEmpresa2();
            System.out.println(listByEmpresa2.size());
            BigDecimal valorFinal = new BigDecimal(0);
            for (LancamentoContabil lc : listByEmpresa2) {
                BigDecimal valor = lc.getValor();
                if (lc.getTipo().equals("Pagar") && valor.intValue() > 0) {
                    valor = valor.multiply(new BigDecimal(-1));
                }
                System.out.println(valor);
                valorFinal = valorFinal.add(valor);
            }
            System.out.println(valorFinal);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<LancamentoContabil> listLancamentosContify(long idEmpresa) throws Exception {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.MONTH, -1);
        c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH));
        Date fim = c.getTime();
        c.set(Calendar.DAY_OF_MONTH, 1);
        Date inicio = c.getTime();
        return new LancamentoContabilBO().listAllByPeriodo(inicio, fim, idEmpresa);
    }

    public List<LancamentoContabil> listByEmpresa2() throws Exception {
        try {
            String jpql = "select lc from LancamentoContabil lc where lc.excluido = 'N' and (lc.lancamento is null or lc.lancamento.validado = 'S') and lc.idEmpresa=:idEmpresa ";
            Query query = this.getDao().createQuery(jpql);
            query.setParameter("idEmpresa", 41);
            return this.list(query);
        } catch (Exception e) {
            log.error("Erro no listByEmpresa", e);
        }
        return null;
    }

    public List<LancamentoContabil> listByEmpresa() throws Exception {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("idEmpresa", ProfissionalBO.getProfissionalLogado().getIdEmpresa());
            parametros.put("excluido", Status.NAO);
            return this.listByFieldsHint(parametros, new String[] { "o.lancamento", "o.dadosBasico" });
        } catch (Exception e) {
            log.error("Erro no listByEmpresa", e);
        }
        return null;
    }

    public LancamentoContabil findByTipoInicial(Date inicio, Date fim) {
        try {
            StringBuffer sb = new StringBuffer();
            sb.append("SELECT ");
            sb.append("L.* ");
            sb.append("FROM ");
            sb.append("LANCAMENTO_CONTABIL L ");
            sb.append("WHERE L.EXCLUIDO = 'N' ");
            // sb.append("AND DATE(L.DATA) BETWEEN ?1 AND ?2 ");
            sb.append("AND L.ID_EMPRESA = ?3 AND TIPO = 'Inicial' ");
            sb.append("ORDER BY L.DATA DESC ");
            Query query = this.getDao().createNativeQuery(sb.toString(), LancamentoContabil.class);
            query.setParameter(1, inicio);
            query.setParameter(2, fim);
            query.setParameter(3, ProfissionalBO.getProfissionalLogado().getIdEmpresa());
            List<LancamentoContabil> lancamentos = query.getResultList();
            return lancamentos != null && !lancamentos.isEmpty() ? lancamentos.get(0) : null;
        } catch (Exception e) {
            log.error("Erro no listComCredito", e);
        }
        return null;
    }

    public LancamentoContabil findByTipoInicial() {
        try {
            StringBuffer sb = new StringBuffer();
            sb.append("SELECT ");
            sb.append("L.* ");
            sb.append("FROM ");
            sb.append("LANCAMENTO_CONTABIL L ");
            sb.append("WHERE L.EXCLUIDO = 'N' ");
            sb.append("AND L.ID_EMPRESA = ?3 AND TIPO = 'Inicial' ");
            sb.append("ORDER BY L.DATA DESC ");
            Query query = this.getDao().createNativeQuery(sb.toString(), LancamentoContabil.class);
            query.setParameter(3, ProfissionalBO.getProfissionalLogado().getIdEmpresa());
            List<LancamentoContabil> lancamentos = query.getResultList();
            return lancamentos != null && !lancamentos.isEmpty() ? lancamentos.get(0) : null;
        } catch (Exception e) {
            log.error("Erro no listComCredito", e);
        }
        return null;
    }

    public List<LancamentoContabil> listByLancamento(Lancamento lancamento) {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("idEmpresa", ProfissionalBO.getProfissionalLogado().getIdEmpresa());
            parametros.put("excluido", Status.NAO);
            parametros.put("lancamento", lancamento);
            return this.listByFields(parametros);
        } catch (Exception e) {
            log.error("Erro no listByLancamento", e);
        }
        return null;
    }

    public List<LancamentoContabil> listAllByPeriodo(Date inicio, Date fim) throws Exception {
        return listAllByPeriodo(inicio, fim, ProfissionalBO.getProfissionalLogado().getIdEmpresa());
    }

    public List<LancamentoContabil> listAllByPeriodo(Date inicio, Date fim, long idEmpresa) throws Exception {
        try {
            StringBuffer sb = new StringBuffer();
            sb.append("SELECT ");
            sb.append("L.* ");
            sb.append("FROM LANCAMENTO_CONTABIL L ");
            sb.append("WHERE L.EXCLUIDO = 'N' ");
            sb.append("AND DATE(L.DATA) BETWEEN ?1 AND ?2 ");
            sb.append("AND L.ID_EMPRESA = ?3 ");
            sb.append("AND L.TIPO <> 'Inicial' ");

            sb.append("ORDER BY DATA ");
            Query query = this.getDao().createNativeQuery(sb.toString(), LancamentoContabil.class);
            query.setParameter(1, inicio);
            query.setParameter(2, fim);
            query.setParameter(3, idEmpresa);
            List<LancamentoContabil> lista = query.getResultList();
            return lista;
        } catch (Exception e) {
            log.error("Erro no LancamentoContabil", e);
        }
        return null;
    }

    public List<LancamentoContabilRelatorio> listAllByPeriodoAndTipo(Date inicio, Date fim) throws Exception {
        try {
            //super.getDao().clearEntityManager();
            StringBuffer sb = new StringBuffer();
            sb.append("SELECT ");
            sb.append("L.ID,L.VALOR,D.NOME,M.DESCRICAO,L.DATA,M.TIPO ");
            sb.append("FROM LANCAMENTO_CONTABIL L ");
            sb.append("LEFT OUTER JOIN DADOS_BASICOS D ON L.ID_DADOS_BASICOS = D.ID ");
            sb.append("LEFT OUTER JOIN MOTIVO M ON L.ID_MOTIVO = M.ID ");
            sb.append("WHERE L.EXCLUIDO = 'N' ");
            sb.append("AND DATE(L.DATA) BETWEEN ?1 AND ?2 ");
            sb.append("AND L.ID_EMPRESA = ?3 ");
            sb.append("AND L.TIPO <> 'Inicial' ");
            sb.append("AND L.ID_LANCAMENTO IS NULL ");
            sb.append("AND L.ID_MOTIVO != 8 ");

            sb.append("UNION ");

            sb.append("SELECT ");
            sb.append("L.ID,L.VALOR,D.NOME,M.DESCRICAO,L.DATA,M.TIPO ");
            sb.append("FROM LANCAMENTO_CONTABIL L ");
            sb.append("LEFT OUTER JOIN DADOS_BASICOS D ON L.ID_DADOS_BASICOS = D.ID ");
            sb.append("INNER JOIN LANCAMENTO LAN ON L.ID_LANCAMENTO = LAN.ID AND LAN.VALIDADO = 'S' ");
            sb.append("LEFT OUTER JOIN MOTIVO M ON L.ID_MOTIVO = M.ID ");
            sb.append("WHERE L.EXCLUIDO = 'N' ");
            sb.append("AND DATE(L.DATA) BETWEEN ?1 AND ?2 ");
            sb.append("AND L.ID_EMPRESA = ?3 ");
            sb.append("AND L.TIPO <> 'Inicial' ");
            sb.append("AND L.ID_MOTIVO != 3 ");
            sb.append("AND L.ID_MOTIVO != 8 ");

            sb.append("UNION ");

            sb.append("SELECT ");
            sb.append("MAX(L.ID) * extract(epoch from now()),SUM(L.VALOR),D.NOME,M.DESCRICAO,DATE(DATA),M.TIPO ");
            sb.append("FROM LANCAMENTO_CONTABIL L ");
            sb.append("LEFT OUTER JOIN DADOS_BASICOS D ON L.ID_DADOS_BASICOS = D.ID ");
            sb.append("LEFT OUTER JOIN MOTIVO M ON L.ID_MOTIVO = M.ID ");
            sb.append("WHERE L.EXCLUIDO = 'N' ");
            sb.append("AND DATE(L.DATA) BETWEEN ?1 AND ?2 ");
            sb.append("AND L.ID_EMPRESA = ?3 ");
            sb.append("AND L.TIPO <> 'Inicial' ");
            sb.append("AND L.ID_LANCAMENTO IS NULL ");
            sb.append("AND L.ID_MOTIVO = 8 ");
            sb.append("GROUP BY DATE(DATA),D.NOME,M.DESCRICAO,M.TIPO ");

            sb.append("ORDER BY DATA ");
            Query query = this.getDao().createNativeQuery(sb.toString(), LancamentoContabilRelatorio.class);
            query.setParameter(1, inicio);
            query.setParameter(2, fim);
            query.setParameter(3, ProfissionalBO.getProfissionalLogado().getIdEmpresa());
            List<LancamentoContabilRelatorio> lista = query.getResultList();
            for (LancamentoContabilRelatorio lancamentoContabilRelatorio : lista) {
                LancamentoContabil lc = this.find(lancamentoContabilRelatorio.getId());
                if (lc != null && lc.getLancamento() != null) {
                    lancamentoContabilRelatorio.setLancamento(lc.getLancamento());
                }
            }
            return lista;
        } catch (Exception e) {
            log.error("Erro no listComCredito", e);
        }
        return null;
    }

    public List<LancamentoContabil> listAllByPeriodoAndTipo_(Date inicio, Date fim) throws Exception {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("idEmpresa", ProfissionalBO.getProfissionalLogado().getIdEmpresa());
            parametros.put("excluido", Status.NAO);
            if (inicio != null && fim != null) {
                parametros.put("o.data between '" + Utils.dateToString(inicio, "yyyy-MM-dd HH:mm:ss") + "' and '" + Utils.dateToString(fim, "yyyy-MM-dd HH:mm:ss") + "' ",
                        GenericListDAO.FILTRO_GENERICO_QUERY);
            }
            return this.listByFields(parametros, new String[] { "data" }, new String[] { "o.lancamento", "o.dadosBasico", "o.notaFiscal" });
            // return listByFields(parametros, new String[] { "data" });
        } catch (Exception e) {
            log.error("Erro no listAllByPeriodoAndTipo", e);
        }
        return null;
    }

    public LancamentoContabil findByLancamentoCartao(Lancamento lancamento) {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("idEmpresa", ProfissionalBO.getProfissionalLogado().getIdEmpresa());
            parametros.put("excluido", Status.NAO);
            parametros.put("lancamento", lancamento);
            parametros.put("motivo.sigla", Motivo.PAGAMENTO_CARTAO);
            List<LancamentoContabil> list = this.listByFields(parametros);
            return list != null && !list.isEmpty() ? list.get(0) : null;
        } catch (Exception e) {
            log.error("Erro no findByNotafiscal", e);
        }
        return null;
    }

    public LancamentoContabil findByNotafiscal(NotaFiscal notaFiscal) throws Exception {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("idEmpresa", ProfissionalBO.getProfissionalLogado().getIdEmpresa());
            parametros.put("excluido", Status.NAO);
            parametros.put("notaFiscal", notaFiscal);
            return this.findByFields(parametros);
        } catch (Exception e) {
            log.error("Erro no findByNotafiscal", e);
        }
        return null;
    }

    public List<LancamentoContabilRelatorio> listByPeriodoAnterior(Date inicio, String forma) {
        try {
            StringBuffer sb = new StringBuffer();
            sb.append("SELECT ");
            sb.append("L.ID,L.VALOR,D.NOME,M.DESCRICAO,L.DATA,M.TIPO ");
            sb.append("FROM LANCAMENTO_CONTABIL L ");
            sb.append("LEFT OUTER JOIN DADOS_BASICOS D ON L.ID_DADOS_BASICOS = D.ID ");
            sb.append("LEFT OUTER JOIN MOTIVO M ON L.ID_MOTIVO = M.ID ");
            sb.append("WHERE L.EXCLUIDO = 'N' ");
            sb.append("AND DATE(L.DATA) < ?1 ");
            sb.append("AND L.ID_EMPRESA = ?2 ");
            sb.append("AND L.TIPO <> 'Inicial' ");
            sb.append("AND L.ID_LANCAMENTO IS NULL ");
            sb.append("UNION ");
            sb.append("SELECT ");
            sb.append("L.ID,L.VALOR,D.NOME,M.DESCRICAO,L.DATA,M.TIPO ");
            sb.append("FROM LANCAMENTO_CONTABIL L ");
            sb.append("LEFT OUTER JOIN DADOS_BASICOS D ON L.ID_DADOS_BASICOS = D.ID ");
            sb.append("INNER JOIN LANCAMENTO LAN ON L.ID_LANCAMENTO = LAN.ID AND LAN.VALIDADO = 'S' ");
            sb.append("LEFT OUTER JOIN MOTIVO M ON L.ID_MOTIVO = M.ID ");
            sb.append("WHERE L.EXCLUIDO = 'N' ");
            sb.append("AND DATE(L.DATA) < ?1 ");
            sb.append("AND L.ID_EMPRESA = ?2 ");
            sb.append("AND L.TIPO <> 'Inicial' ");
            sb.append("AND L.ID_MOTIVO != 3 ");
            sb.append("ORDER BY DATA ");
            Query query = this.getDao().createNativeQuery(sb.toString(), LancamentoContabilRelatorio.class);
            query.setParameter(1, inicio);
            query.setParameter(2, ProfissionalBO.getProfissionalLogado().getIdEmpresa());
            return query.getResultList();
        } catch (Exception e) {
            log.error("Erro no listByPeriodoAnterior", e);
        }
        return null;
    }
}
