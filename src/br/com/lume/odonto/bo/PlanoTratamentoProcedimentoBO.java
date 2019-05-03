package br.com.lume.odonto.bo;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

import org.apache.log4j.Logger;

import br.com.lume.common.connection.GenericDAO;
import br.com.lume.common.exception.business.BusinessException;
import br.com.lume.common.exception.techinical.TechnicalException;
import br.com.lume.common.util.Status;
import br.com.lume.common.util.Utils;
import br.com.lume.configuracao.Configurar;
import br.com.lume.odonto.dao.PersistenceUnitName;
import br.com.lume.odonto.entity.ConvenioProcedimento;
import br.com.lume.odonto.entity.Dente;
import br.com.lume.odonto.entity.Especialidade;
import br.com.lume.odonto.entity.Paciente;
import br.com.lume.odonto.entity.PlanoTratamento;
import br.com.lume.odonto.entity.PlanoTratamentoProcedimento;
import br.com.lume.odonto.entity.Procedimento;
import br.com.lume.odonto.entity.Profissional;
import br.com.lume.odonto.entity.ProfissionalEspecialidade;
import br.com.lume.odonto.entity.RepasseItem;
import br.com.lume.odonto.entity.RepasseLancamento;
import br.com.lume.odonto.entity.RepasseProfissional;
//import br.com.lume.security.bo.EmpresaBO;

public class PlanoTratamentoProcedimentoBO extends BO<PlanoTratamentoProcedimento> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(PlanoTratamentoProcedimentoBO.class);

    public PlanoTratamentoProcedimentoBO() {
        super(PersistenceUnitName.ODONTO);
        this.setClazz(PlanoTratamentoProcedimento.class);
    }

    public PlanoTratamentoProcedimento carregaProcedimento(PlanoTratamento planoTratamento, Procedimento procedimento, Paciente paciente) {
        BigDecimal valorComDesconto = new BigDecimal(0);
        ConvenioProcedimentoBO convenioProcedimentoBO = new ConvenioProcedimentoBO();
        if (paciente.getConvenio() != null && paciente.getConvenio().getExcluido().equals(Status.NAO) && planoTratamento.isBconvenio()) {
            try {
                ConvenioProcedimento cp = convenioProcedimentoBO.findByConvenioAndProcedimento(paciente.getConvenio(), procedimento);
                if (cp != null) {
                    valorComDesconto = cp.getValor();
                } else {
                    valorComDesconto = procedimento.getValor();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            valorComDesconto = procedimento.getValor();
        }

        PlanoTratamentoProcedimento planoTratamentoProcedimento = new PlanoTratamentoProcedimento(valorComDesconto, valorComDesconto, planoTratamento, procedimento);
        planoTratamentoProcedimento.setValorDescontoLabel(valorComDesconto);
        planoTratamentoProcedimento.setValorLabel(procedimento.getValor());
        planoTratamentoProcedimento.setQtdConsultas(0);
        planoTratamentoProcedimento.setDenteObj(null);
        planoTratamentoProcedimento.setStatusDente(null);
        planoTratamentoProcedimento.setPlanoTratamentoProcedimentoFaces(null);
        planoTratamentoProcedimento.setStatus(null);
        planoTratamentoProcedimento.setStatusNovo(null);
        if (paciente.getConvenio() != null && paciente.getConvenio().getExcluido().equals(Status.NAO)) {
            planoTratamentoProcedimento.setConvenio(paciente.getConvenio());
        }
        planoTratamentoProcedimento.setCodigoConvenio(procedimento.getCodigoConvenio());
        return planoTratamentoProcedimento;
    }

    public void carregarRepasses() {
        try {
            StringBuffer sb = new StringBuffer();
            sb.append("select ");
            sb.append("* ");
            sb.append("from plano_tratamento_procedimento ");
            sb.append("where valor_repassado > 0 ");
            sb.append("and valor_repassado is not null ");
            sb.append("and valor_repasse is not null ");
            sb.append("and status_pagamento = 'G' ");
            Query query = this.getDao().createNativeQuery(sb.toString(), PlanoTratamentoProcedimento.class);
            List<PlanoTratamentoProcedimento> resultado = query.getResultList();
            Profissional profissional = new ProfissionalBO().find(436L);
            RepasseProfissionalBO repasseProfissionalBO = new RepasseProfissionalBO();
            for (PlanoTratamentoProcedimento ptp : resultado) {
                Date data = Calendar.getInstance().getTime();
                RepasseProfissional rp = new RepasseProfissional(data, data, ptp.getFinalizadoPorProfissional(), profissional, RepasseItem.PAGO_COMPLETO);
                RepasseItem ri = new RepasseItem(rp, ptp, RepasseItem.PAGO_COMPLETO, ptp.getValorRepassado());
                List<RepasseItem> riLista = new ArrayList<>();
                riLista.add(ri);
                rp.setRepasseItens(riLista);
                repasseProfissionalBO.persist(rp);
            }

        } catch (Exception e) {
            log.error("Erro no listProcedimentosPickListAgendamento", e);
        }
    }

    public void carregarRepasses2() {
        try {
            StringBuffer sb = new StringBuffer();
            sb.append("select ");
            sb.append("* ");
            sb.append("from plano_tratamento_procedimento ");
            sb.append("where valor_repassado > 0 ");
            sb.append("and valor_repassado is not null ");
            sb.append("and valor_repasse is not null ");
            sb.append("and status_pagamento != 'G' ");
            Query query = this.getDao().createNativeQuery(sb.toString(), PlanoTratamentoProcedimento.class);
            List<PlanoTratamentoProcedimento> resultado = query.getResultList();
            RepasseLancamentoBO repasseLancamentoBO = new RepasseLancamentoBO();
            for (PlanoTratamentoProcedimento ptp : resultado) {
                Date data = Calendar.getInstance().getTime();
                RepasseLancamento rl = new RepasseLancamento(null, ptp, ptp.getValorRepassado(), null, RepasseItem.REPASSADO);
                repasseLancamentoBO.persist(rl);
//                RepasseProfissional rp = new RepasseProfissional(data, data, ptp.getFinalizadoPorProfissional(),  profissional, RepasseItem.PAGO_COMPLETO);
//                RepasseItem ri = new RepasseItem(rp, ptp, RepasseItem.PAGO_COMPLETO, ptp.getValorRepassado());
//                List<RepasseItem> riLista = new ArrayList<>();
//                riLista.add(ri);
//                rp.setRepasseItens(riLista);
//                repasseProfissionalBO.persist(rp);
            }

        } catch (Exception e) {
            log.error("Erro no listProcedimentosPickListAgendamento", e);
        }
    }

    @Override
    public boolean remove(PlanoTratamentoProcedimento planoTratamentoProcedimento) throws BusinessException, TechnicalException {
        planoTratamentoProcedimento.setExcluido(Status.SIM);
        planoTratamentoProcedimento.setDataExclusao(Calendar.getInstance().getTime());
        planoTratamentoProcedimento.setExcluidoPorProfissional(ProfissionalBO.getProfissionalLogado().getId());
        this.persist(planoTratamentoProcedimento);
        return true;
    }

    public List<PlanoTratamentoProcedimento> listByComRegiaoAndPlanoTratamento(PlanoTratamento planoTratamento) throws Exception {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("o.regiao is not null", GenericDAO.FILTRO_GENERICO_QUERY);
            parametros.put("planoTratamento", planoTratamento);
            parametros.put("excluido", Status.NAO);
            return this.listByFields(parametros);
        } catch (Exception e) {
            log.error("Erro no listByDenteAndPlanoTratamento", e);
        }
        return null;
    }

    public List<PlanoTratamentoProcedimento> listByDenteAndPlanoTratamento(Dente dente, PlanoTratamento planoTratamento) throws Exception {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("denteObj", dente);
            parametros.put("planoTratamento", planoTratamento);
            parametros.put("excluido", Status.NAO);
            return this.listByFields(parametros);
        } catch (Exception e) {
            log.error("Erro no listByDenteAndPlanoTratamento", e);
        }
        return null;
    }

    public List<PlanoTratamentoProcedimento> listByPlanoTratamento(long id) throws Exception {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("planoTratamento.id", id);
            parametros.put("excluido", Status.NAO);
            return this.listByFields(parametros);
        } catch (Exception e) {
            log.error("Erro no listByPlanoTratamento", e);
        }
        return null;
    }

    public List<PlanoTratamentoProcedimento> listByPaciente(Paciente paciente) throws Exception {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("planoTratamento.paciente.id", paciente.getId());
            parametros.put("excluido", Status.NAO);
            return this.listByFields(parametros);
        } catch (Exception e) {
            log.error("Erro no List<PlanoTratamentoProcedimento> listByPaciente", e);
        }
        return null;
    }

    public List<PlanoTratamentoProcedimento> listByPaciente(Paciente paciente, List<ProfissionalEspecialidade> profissionalEspecialidades) {
        try {
            List<Especialidade> especialidades = new ArrayList<>();
            for (ProfissionalEspecialidade pe : profissionalEspecialidades) {
                especialidades.add(pe.getEspecialidade());
            }
            if (especialidades.size() <= 0) {
                especialidades = null;
            }
            if (paciente != null) {
                String jpql = "select distinct ptp from PlanoTratamentoProcedimento as ptp " + "WHERE ptp.planoTratamento.paciente.id = :paciente " + "AND ptp.procedimento.especialidade in :especialidades AND  ptp.excluido= :excluido";
                Query q = this.getDao().createQuery(jpql);
                q.setParameter("paciente", paciente.getId());
                q.setParameter("especialidades", especialidades);
                q.setParameter("excluido", Status.NAO);
                return this.list(q);
            }
        } catch (Exception e) {
            log.error("Erro no listByPlanoTratamento", e);
        }
        return null;
    }

    public List<PlanoTratamentoProcedimento> listByPlanoTratamentoAtivos(PlanoTratamento planoTratamento) {
        try {
            if (planoTratamento != null) {
                String jpql = " select distinct ptp from PlanoTratamentoProcedimento as ptp , PlanoTratamento pt " + " WHERE ptp.planoTratamento.id = :planoTratamento AND  ptp.excluido= :excluido AND ptp.status = null  " + " and pt = ptp.planoTratamento " + " and pt.finalizado = 'N' " + " ORDER BY ptp.prioridade";
                Query q = this.getDao().createQuery(jpql);
                q.setParameter("planoTratamento", planoTratamento.getId());
                q.setParameter("excluido", Status.NAO);
                return this.list(q);
            }
        } catch (Exception e) {
            log.error("Erro no listByPlanoTratamento", e);
        }
        return null;
    }

    public List<PlanoTratamentoProcedimento> listByPlanoTratamento(PlanoTratamento planoTratamento) {
        try {
            if (planoTratamento != null) {
                String jpql = " select distinct ptp from PlanoTratamentoProcedimento as ptp , PlanoTratamento pt " + " WHERE ptp.planoTratamento.id = :planoTratamento AND  ptp.excluido= :excluido " + " and pt = ptp.planoTratamento " + " and ( (ptp.status = 'F' and pt.finalizado = 'S') or (pt.finalizado = 'N')) " + " ORDER BY ptp.prioridade ";
                Query q = this.getDao().createQuery(jpql);
                q.setParameter("planoTratamento", planoTratamento.getId());
                q.setParameter("excluido", Status.NAO);
                return this.list(q);
            }
        } catch (Exception e) {
            log.error("Erro no listByPlanoTratamento", e);
        }
        return null;
    }

    public StringBuffer createSQL(Paciente paciente, Profissional profissional, Date inicio, Date fim, Procedimento procedimento, String status) throws Exception {
        StringBuffer sb = new StringBuffer();
        Procedimento procedimentoInicial = new ProcedimentoBO().findByProcedimentoInicial();
        sb.append(" select ptp.* ");
        sb.append(" from PLANO_TRATAMENTO_PROCEDIMENTO ptp, PLANO_TRATAMENTO pt, PROCEDIMENTO po, paciente pa, DADOS_BASICOS d1 ");
        sb.append(", profissional p, DADOS_BASICOS d ");
        if (status.equals("Pagos") || status.equals("Parcialmente")) {
            sb.append("  , repasse r where r.ID_PLANO_TRATAMENTO_PROCEDIMENTO = ptp.id and r.excluido = 'N' and ");
        } else {
            if (status.equals("")) {
                sb.append(" where  ");
            } else {
                sb.append("  where ptp.id not in (  select ID_PLANO_TRATAMENTO_PROCEDIMENTO from REPASSE r where r.id_plano_tratamento_procedimento = ptp.id and r.excluido = 'N') and ");
                if (status.equals("Faturados")) {
                    sb.append(" ptp.DATA_FATURAMENTO is not null and ");
                } else if (status.equals("Pendentes")) {
                    sb.append(" ptp.DATA_FATURAMENTO is null and ");
                }
            }
        }
        sb.append(" pa.id_dados_basicos = d1.id ");
        sb.append(" and d.id = p.ID_DADOS_BASICOS ");
        sb.append(" and pt.id_paciente = pa.id ");
        sb.append(" and ptp.ID_PLANO_TRATAMENTO = pt.id ");
        sb.append(" and ptp.ID_PROCEDIMENTO = po.id ");
        sb.append(" and ptp.FINALIZADO_POR = p.id and ptp.STATUS = 'F'");
        if (profissional != null) {
            sb.append(" and ptp.FINALIZADO_POR = " + profissional.getId()); // profissional
        }
        if (procedimento != null) {
            sb.append(" and ptp.ID_PROCEDIMENTO = " + procedimento.getId());
        }
        if (paciente != null) {
            sb.append(" and pt.ID_PACIENTE = " + paciente.getId());
        }
        if (inicio != null) {
            if (fim != null) {
                sb.append(" and ptp.DATA_FINALIZADO between '" + Utils.dateToString(inicio, "yyyy-MM-dd HH:mm:ss") + "' and '" + Utils.dateToString(fim, "yyyy-MM-dd HH:mm:ss") + " '");
            } else {
                sb.append(" and ptp.DATA_FINALIZADO >= '" + Utils.dateToString(inicio, "yyyy-MM-dd HH:mm:ss") + "'");
            }
        } else if (fim != null) {
            Calendar c = Calendar.getInstance();
            c.setTime(fim);
            c.add(Calendar.DAY_OF_YEAR, 1);
            sb.append(" and ptp.DATA_FINALIZADO <= '" + Utils.dateToString(c.getTime(), "yyyy-MM-dd HH:mm:ss") + "'");
        }
        // Retirando consulta inicial
        if (procedimentoInicial != null) {
            sb.append(" and po.id != " + procedimentoInicial.getId());
        }
        return sb;
    }

    public List<PlanoTratamentoProcedimento> listAllByPacienteAndProfissionalAndPeriodoAndProcedimento(Paciente paciente, Profissional profissional, Date inicio, Date fim, Procedimento procedimento,
            String status) {
        try {
            StringBuffer sb = this.createSQL(paciente, profissional, inicio, fim, procedimento, status);
            Query query = this.getDao().createNativeQuery(sb.toString(), PlanoTratamentoProcedimento.class);
            List<PlanoTratamentoProcedimento> ptps = this.list(query);
            return ptps;
        } catch (Exception e) {
            log.error("Erro no listAllByPacienteAndProfissionalAndPeriodoAndProcedimento", e);
        }
        return null;
    }

    public static void main(String[] args) {
        try {
            PlanoTratamentoProcedimentoBO ptpBO = new PlanoTratamentoProcedimentoBO();
            List<PlanoTratamentoProcedimento> listErro = ptpBO.listErro2();
            DecimalFormat df = new DecimalFormat("0.##");
            for (PlanoTratamentoProcedimento ptp : listErro) {
                BigDecimal valorRepasse = ptp.getValorRepasse();
                if (valorRepasse != null) {

                    BigDecimal valorAtual = ptpBO.findValorRepasse(ptp);
                    if (valorRepasse.doubleValue() != valorAtual.doubleValue()) {
                        System.out.println(ptp.getId() + ";ERROS;" + df.format(valorRepasse) + ";" + df.format(
                                valorAtual) + ";" + ptp.getPlanoTratamento().getPaciente().getDadosBasico().getNome() + ";" + ptp.getDataFinalizadoStr());
                    } else {
                        System.out.println(ptp.getId() + ";CERTOS;" + df.format(valorRepasse) + ";" + df.format(
                                valorAtual) + ";" + ptp.getPlanoTratamento().getPaciente().getDadosBasico().getNome() + ";" + ptp.getDataFinalizadoStr());
                    }
                }
            }

        } catch (

        Exception e) {
            e.printStackTrace();
        }
    }

    public static void main2(String[] args) {
        try {
            PlanoTratamentoProcedimentoBO planoTratamentoProcedimentoBO = new PlanoTratamentoProcedimentoBO();
            RepasseLancamentoBO repasseLancamentoBO = new RepasseLancamentoBO();
            List<PlanoTratamentoProcedimento> listErro = planoTratamentoProcedimentoBO.listErro();

            List<PlanoTratamentoProcedimento> antigoMaiorNovo = new ArrayList<>();
            List<PlanoTratamentoProcedimento> novoMaiorAntigo = new ArrayList<>();

            int total = 0;
            DecimalFormat df = new DecimalFormat("0.##");

            for (PlanoTratamentoProcedimento ptp : listErro) {
                BigDecimal repasseNovo = planoTratamentoProcedimentoBO.findValorRepasse(ptp);

                BigDecimal repasseAntigo = ptp.getValorRepasse();
                BigDecimal repasseAntigoOriginal = repasseAntigo;

                List<RepasseLancamento> repasseLancamentos = ptp.getRepasseLancamentos();
                BigDecimal valorDesconto = new BigDecimal(0d);
                String erros = "";
                if (repasseLancamentos != null && !repasseLancamentos.isEmpty()) {
                    for (RepasseLancamento repasseLancamento : repasseLancamentos) {
                        if (repasseLancamento.getLancamento() != null && repasseLancamento.getLancamento().getFormaPagamento().equals(
                                "DI") && repasseLancamento.getValorDesconto() != null && repasseLancamento.getValorDesconto().doubleValue() > 0d) {
                            erros += "Pago em dinheiro, mas colocou taxa;";
                        }
                        if (repasseLancamento.getLancamento() != null && !repasseLancamento.getLancamento().getFormaPagamento().equals(
                                "DI") && (repasseLancamento.getValorDesconto() == null || repasseLancamento.getValorDesconto().doubleValue() == 0d)) {
                            erros += "Pago em " + repasseLancamento.getLancamento().getFormaPagamento() + ", e NÃO colocou taxa;";
                        }
                        if (repasseLancamento.getLancamento() == null) {
                            erros += "Registros inseridos manualmente durante a transição;";
                        }
                        if (repasseLancamento.getValorDesconto() != null) {
                            valorDesconto = valorDesconto.add(repasseLancamento.getValorDesconto());
                        }

                    }
                } else {
                    erros += "Não tem Repasse Lancamento;";
                }
                repasseAntigo = repasseAntigo.add(valorDesconto);

                repasseNovo = repasseNovo.setScale(2, BigDecimal.ROUND_HALF_UP);
                repasseAntigo = repasseAntigo.setScale(2, BigDecimal.ROUND_HALF_UP);
                if (!repasseAntigo.equals(repasseNovo)) {
                    if (repasseAntigo.doubleValue() > repasseNovo.doubleValue()) {
                        antigoMaiorNovo.add(ptp);
                        System.out.println(
                                "PAGOU MAIS;" + ptp.getId() + ";" + ptp.getDataFinalizadoStrOrd() + ";" + ptp.getFinalizadoPorProfissional().getDadosBasico().getNome() + ";" + ptp.getPlanoTratamento().getPaciente().getDadosBasico().getNome() + ";" + Utils.split(
                                        ptp.getPlanoTratamento().getDescricao(), 15) + ";" + Utils.split(ptp.getProcedimento().getDescricao(), 15) + ";" + df.format(
                                                repasseAntigo) + ";" + df.format(repasseAntigoOriginal) + ";" + df.format(repasseNovo) + ";" + df.format(valorDesconto) + ";" + erros);
                    } else {
                        novoMaiorAntigo.add(ptp);
                        System.out.println(
                                "PAGOU MENOS;" + ptp.getId() + ";" + ptp.getDataFinalizadoStrOrd() + ";" + ptp.getFinalizadoPorProfissional().getDadosBasico().getNome() + ";" + ptp.getPlanoTratamento().getPaciente().getDadosBasico().getNome() + ";" + Utils.split(
                                        ptp.getPlanoTratamento().getDescricao(), 15) + ";" + Utils.split(ptp.getProcedimento().getDescricao(), 15) + ";" + df.format(
                                                repasseAntigo) + ";" + df.format(repasseAntigoOriginal) + ";" + df.format(repasseNovo) + ";" + df.format(valorDesconto) + ";" + erros);
                    }
                }
            }

            //System.out.println(total);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public BigDecimal findValorTotalDescontoByPT(PlanoTratamento pt) {
        try {
            StringBuffer sb = new StringBuffer();
            sb.append("   SELECT SUM(VALOR_DESCONTO) FROM PLANO_TRATAMENTO_PROCEDIMENTO PTP WHERE PTP.ID_PLANO_TRATAMENTO = ?1 AND EXCLUIDO = 'N' ");
            Query query = this.getDao().createNativeQuery(sb.toString());
            query.setParameter(1, pt.getId());
            BigDecimal valor = (BigDecimal) query.getSingleResult();
            return valor;
        } catch (Exception e) {
            log.error("Erro no findValorTotalDescontoByPT", e);
        }
        return new BigDecimal(0);
    }

    public BigDecimal findValorDescontoOriginal(PlanoTratamentoProcedimento ptp) {
        try {
            StringBuffer sb = new StringBuffer();
            sb.append("   SELECT VALOR_DESCONTO FROM PLANO_TRATAMENTO_PROCEDIMENTO PTP WHERE PTP.ID = ?1 ");
            Query query = this.getDao().createNativeQuery(sb.toString());
            query.setParameter(1, ptp.getId());
            BigDecimal valor = (BigDecimal) query.getSingleResult();
            return valor;
        } catch (Exception e) {
            log.error("Erro no findValorDescontoOriginal", e);
        }
        return new BigDecimal(0);
    }

    public List<PlanoTratamentoProcedimento> listProcedimentosPickListAgendamento(Long idPlanoTratamento, Long idAgendamento, boolean semFinalizados) {
        try {
            StringBuffer sb = new StringBuffer();
            sb.append("SELECT ");
            sb.append("* ");
            sb.append("FROM PLANO_TRATAMENTO_PROCEDIMENTO PTP ");
            sb.append("WHERE PTP.ID_PLANO_TRATAMENTO = ?1 ");
            sb.append("AND PTP.EXCLUIDO = 'N' ");
            if (semFinalizados) {
                sb.append("AND PTP.STATUS IS NULL ");
            }
            sb.append("AND NOT EXISTS ");
            sb.append("( ");
            sb.append("   SELECT ");
            sb.append("   1 ");
            sb.append("   FROM AGENDAMENTO_PLANO_TRATAMENTO_PROCEDIMENTO APTP ");
            sb.append("   WHERE APTP.ID_PLANO_TRATAMENTO_PROCEDIMENTO = PTP.ID AND APTP.ID_AGENDAMENTO = ?2 ");
            sb.append(") ");
            Query query = this.getDao().createNativeQuery(sb.toString(), PlanoTratamentoProcedimento.class);
            query.setParameter(1, idPlanoTratamento);
            query.setParameter(2, idAgendamento);
            return query.getResultList();
        } catch (Exception e) {
            log.error("Erro no listProcedimentosPickListAgendamento", e);
        }
        return null;
    }

    public List<PlanoTratamentoProcedimento> listPTPOrtodontia(long idPlanoTratamento) {
        try {
            StringBuffer sb = new StringBuffer();
            sb.append("SELECT * FROM PLANO_TRATAMENTO_PROCEDIMENTO PTP WHERE ");
            sb.append("ID_PLANO_TRATAMENTO = ?1 AND ");
            sb.append("EXCLUIDO = 'N' ");
            sb.append("ORDER BY SEQUENCIAL,DATA_FINALIZADO ASC ");
            Query query = this.getDao().createNativeQuery(sb.toString(), PlanoTratamentoProcedimento.class);
            query.setHint("javax.persistence.cache.storeMode", "REFRESH");
            query.setParameter(1, idPlanoTratamento);
            return query.getResultList();
        } catch (Exception e) {
            log.error("Erro no listPTPOrtodontia", e);
        }
        return null;
    }

    public Long findQtdFinalizadosPTPOrtodontia(long idPlanoTratamento) {
        try {
            StringBuffer sb = new StringBuffer();
            sb.append("SELECT COUNT(1) FROM PLANO_TRATAMENTO_PROCEDIMENTO PTP WHERE ");
            sb.append("ID_PLANO_TRATAMENTO = ?1 AND ");
            sb.append("EXCLUIDO = 'N' AND DATA_FINALIZADO IS NOT NULL AND ORTODONTICO = TRUE ");
            Query query = this.getDao().createNativeQuery(sb.toString());
            query.setParameter(1, idPlanoTratamento);
            return (Long) query.getSingleResult();
        } catch (Exception e) {
            log.error("Erro no listPTPOrtodontia", e);
        }
        return 0l;
    }

    public Long findQtdNaoFinalizadosPTPOrtodontia(long idPlanoTratamento) {
        try {
            StringBuffer sb = new StringBuffer();
            sb.append("SELECT COUNT(1) FROM PLANO_TRATAMENTO_PROCEDIMENTO PTP WHERE ");
            sb.append("ID_PLANO_TRATAMENTO = ?1 AND ");
            sb.append("EXCLUIDO = 'N' AND DATA_FINALIZADO IS NULL AND ORTODONTICO = TRUE ");
            Query query = this.getDao().createNativeQuery(sb.toString());
            query.setParameter(1, idPlanoTratamento);
            return (Long) query.getSingleResult();
        } catch (Exception e) {
            log.error("Erro no listPTPOrtodontia", e);
        }
        return 0l;
    }

    public List<PlanoTratamentoProcedimento> listNaoPagosTotalmente(long idprofissional, Integer mes, boolean mesesAnteriores) {
        try {
            StringBuffer sb = new StringBuffer();
            sb.append("SELECT * FROM PLANO_TRATAMENTO_PROCEDIMENTO PTP WHERE ");
            sb.append("VALOR_REPASSE > COALESCE(VALOR_REPASSADO,0) ");
            sb.append("AND FINALIZADO_POR = ?1 ");
            if (mesesAnteriores) {
                Calendar c = Calendar.getInstance();
                c.set(Calendar.MONTH, mes - 1);
                c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH));
                sb.append("AND DATA_FINALIZADO::DATE <= '" + new SimpleDateFormat("yyyy-MM-dd").format(c.getTime()) + "' ");
            } else {
                sb.append("AND EXTRACT(MONTH FROM DATA_FINALIZADO) = " + mes + " AND EXTRACT(YEAR FROM DATA_FINALIZADO) <= " + Calendar.getInstance().get(Calendar.YEAR) + " ");
            }
            sb.append(
                    "AND NOT EXISTS (SELECT 1 FROM REPASSE_ITEM RI WHERE RI.ID_PLANO_TRATAMENTO_PROCEDIMENTO = PTP.ID AND STATUS IN('" + RepasseItem.REPASSADO + "','" + RepasseItem.PAGO_COMPLETO + "')) ");
            sb.append("AND NOT EXISTS (SELECT 1 FROM REPASSE_LANCAMENTO RL WHERE Rl.ID_PLANO_TRATAMENTO_PROCEDIMENTO = PTP.ID AND STATUS = '" + RepasseItem.REPASSADO + "' ) ");
            sb.append(" ORDER BY DATA_FINALIZADO ASC ");
            Query query = this.getDao().createNativeQuery(sb.toString(), PlanoTratamentoProcedimento.class);
            query.setParameter(1, idprofissional);
            return query.getResultList();
        } catch (Exception e) {
            log.error("Erro no findValorRepasse", e);
        }
        return null;
    }

//    public void corrigeRepasse2() throws Exception {
//        // Long[] ids = new
//        // Long[]{19544L,20317L,20348L,20349L,20448L,20456L,20545L,20546L,20548L,20543L,20544L,20481L,20561L,20614L,20547L,20616L,20685L,
//        // 20615L,20747L,20741L,20731L,20732L,20805L,20845L,19201L,20748L,20749L,20638L,20480L,20449L};
//        Long[] ids = new Long[] { 14046L, 17814L, 17807L, 17810L, 18568L, 19084L, 20045L, 20074L, 20079L, 20140L, 20194L, 20195L, 20208L, 20302L, 20351L, 20340L, 20338L, 20092L, 20094L, 20096L, 19647L, 20095L, 20447L, 20456L, 20431L, 20453L, 20454L, 20445L, 20452L, 20068L, 20056L, 20054L, 17818L, 19933L, 20057L, 20061L, 20062L, 20063L, 20091L, 20097L, 20117L, 20561L, 20549L, 20618L, 20637L, 20551L, 20679L, 20684L, 19085L, 19086L, 19087L, 19088L, 20729L, 20736L, 20743L, 20737L, 20744L, 20747L, 20711L, 20733L, 20774L, 20781L, 20811L, 20817L, 20789L, 20962L, 19190L, 19191L, 19192L, 19198L, 19200L, 20749L, 20638L, 20748L, 20931L, 20940L, 20944L, 20961L, 19934L };
//        for (Long id : ids) {
//            PlanoTratamentoProcedimento ptp = this.find(id);
//            BigDecimal valor = this.findValorRepasse(id);
//            ptp.setValorRepasse(valor);
//            this.merge(ptp);
//            System.out.println(ptp.getId() + " " + ptp.getValorRepasse() + " " + valor);
//        }
//    }

//    public void corrigeRepasse() {
//        try {
//            StringBuffer sb = new StringBuffer();
//            sb.append("select * from plano_tratamento_procedimento ");
//            sb.append("where ");
//            sb.append("excluido = 'N' and ");
//            sb.append("status = 'F' ");
//            sb.append("and valor_repasse is null and ");
//            sb.append("EXTRACT(year FROM DATA_FINALIZADO) = 2017 and ");
//            sb.append("EXTRACT(month FROM DATA_FINALIZADO) = 5 ");
//            Query query = this.getDao().createNativeQuery(sb.toString(), PlanoTratamentoProcedimento.class);
//            List<PlanoTratamentoProcedimento> ptps = query.getResultList();
//            for (PlanoTratamentoProcedimento ptp : ptps) {
//                ptp.setValorRepasse(this.findValorRepasse(ptp.getId()));
//                System.out.println(this.findValorRepasse(ptp.getId()));
//                // System.out.println(ptp.getId());
//                this.merge(ptp);
//            }
//        } catch (Exception e) {
//            log.error("Erro no findValorRepasse", e);
//        }
//    }

    public static void main3(String[] args) {
        try {
            PlanoTratamentoProcedimentoBO ptpbo = new PlanoTratamentoProcedimentoBO();
            PlanoTratamentoProcedimento ptp = ptpbo.find(20339L);
            BigDecimal valor = ptpbo.findValorRepasse(ptp);
            System.out.println(valor);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public BigDecimal findValorRepasse(PlanoTratamentoProcedimento ptp) {
        try {

            if (ptp.getFinalizadoPorProfissional() != null) {
                String tipoRemuneracao = ptp.getTipoRemuneracaoProfissionalCalc() != null && !ptp.getTipoRemuneracaoProfissionalCalc().equals(
                        "") ? ptp.getTipoRemuneracaoProfissionalCalc() : ptp.getFinalizadoPorProfissional().getTipoRemuneracao();

                if (tipoRemuneracao.equals(Profissional.PORCENTAGEM)) {
                    atualizaTributo(ptp);
                    StringBuffer sb = new StringBuffer();
                    // ((valor bruto - 22,5) - custodireto)*40%
                    // VALOR DO PROFISSIONAL JA TRIBUTADO DO TOTAL - CUSTOS
                    sb.append("SELECT ");
                    sb.append("( ");
                    sb.append(
                            " (PTP.VALOR_DESCONTO * (1-(PTP.TRIBUTO/100)) - COALESCE((SELECT SUM(PTC.VALOR) FROM PLANO_TRATAMENTO_PROCEDIMENTO_CUSTO PTC WHERE PTC.ID_PLANO_TRATAMENTO_PROCEDIMENTO = PTP.ID AND PTC.EXCLUIDO = 'N'),0 ) ) ");
                    sb.append(" * (COALESCE( CAST((SELECT PERCENTUAL_REMUNERACAO FROM PROFISSIONAL P WHERE P.ID = PTP.FINALIZADO_POR) AS DOUBLE PRECISION),0)/100) ");
                    sb.append(") ");
                    sb.append("AS VALOR_REPASSE, ");

                    sb.append("PTP.VALOR_DESCONTO, ");
                    sb.append("PTP.TRIBUTO, ");
                    sb.append("COALESCE((SELECT SUM(PTC.VALOR) FROM PLANO_TRATAMENTO_PROCEDIMENTO_CUSTO PTC WHERE PTC.ID_PLANO_TRATAMENTO_PROCEDIMENTO = PTP.ID AND PTC.EXCLUIDO = 'N'),0 ) , ");
                    sb.append("COALESCE( (SELECT PERCENTUAL_REMUNERACAO FROM PROFISSIONAL P WHERE P.ID = PTP.FINALIZADO_POR),0) ");

                    sb.append("FROM PLANO_TRATAMENTO_PROCEDIMENTO PTP ");
                    sb.append("WHERE PTP.ID = ?1 ");
                    Query query = this.getDao().createNativeQuery(sb.toString());
                    query.setParameter(1, ptp.getId());
                    List<Object[]> repasse = query.getResultList();

                    if (repasse != null && !repasse.isEmpty() && repasse.get(0)[0] != null) {
                        ptp.setValorCalc((BigDecimal) repasse.get(0)[1]);
                        ptp.setTributoCalc((BigDecimal) repasse.get(0)[2]);
                        ptp.setCustoCalc((BigDecimal) repasse.get(0)[3]);
                        ptp.setPercentualCalc((Integer) repasse.get(0)[4]);
                        ptp.setTipoRemuneracaoProfissionalCalc(Profissional.PORCENTAGEM);
                        return new BigDecimal((Double) repasse.get(0)[0]);
                    }
                } else if (tipoRemuneracao.equals(Profissional.PROCEDIMENTO)) {
                    BigDecimal valorRepasse = null;
                    if (ptp.getPlanoTratamento().getConvenio() != null) {
                        ConvenioProcedimento cp = new ConvenioProcedimentoBO().findByConvenioAndProcedimento(ptp.getPlanoTratamento().getConvenio(), ptp.getProcedimento());
                        if (cp != null) {
                            ptp.setValorCalc(cp.getValorRepasse());
                            valorRepasse = cp.getValorRepasse();
                        } else {
                            ptp.setValorCalc(ptp.getProcedimento().getValorRepasse());
                            valorRepasse = ptp.getProcedimento().getValorRepasse();
                        }
                    } else {
                        ptp.setValorCalc(ptp.getProcedimento().getValorRepasse());
                        valorRepasse = ptp.getProcedimento().getValorRepasse();
                    }
                    ptp.setTributoCalc(new BigDecimal(0));
                    ptp.setCustoCalc(new BigDecimal(0));
                    ptp.setPercentualCalc(0);
                    ptp.setTipoRemuneracaoProfissionalCalc(Profissional.PROCEDIMENTO);
                    return valorRepasse;
                } else if (tipoRemuneracao.equals(Profissional.FIXO)) {
                    ptp.setValorRepasse(new BigDecimal(0));
                    ptp.setValorRepassado(new BigDecimal(0));
                    ptp.setTipoRemuneracaoProfissionalCalc(Profissional.FIXO);
                }
            }
            return new BigDecimal(0);

            //return repasse != null && !repasse.isEmpty() ? new BigDecimal((Double) repasse.get(0)) : new BigDecimal(0);
        } catch (Exception e) {
            log.error("Erro no findValorRepasse", e);
        }
        return new BigDecimal(0);
    }

    private void atualizaTributo(PlanoTratamentoProcedimento ptp) {
        try {
            ptp.setTributo(new BigDecimal(Configurar.getInstance().getConfiguracao().getEmpresaLogada().getEmpFltImposto()));
            persist(ptp);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<PlanoTratamentoProcedimento> listRelatorioProcedimento(Profissional p, Date inicio, Date fim) {
        try {
            StringBuffer sb = new StringBuffer();
            sb.append("SELECT ");
            sb.append("PTP.* ");
            sb.append("FROM PLANO_TRATAMENTO_PROCEDIMENTO PTP, PLANO_TRATAMENTO PT, PACIENTE PAC ");
            sb.append("WHERE ");
            if (p != null) {
                sb.append("PTP.FINALIZADO_POR = " + p.getId() + " AND ");
            }
            sb.append("DATE(PTP.DATA_FINALIZADO) BETWEEN ?1 AND ?2 AND ");
            sb.append("PTP.ID_PLANO_TRATAMENTO = PT.ID AND ");
            sb.append("PT.ID_PACIENTE = PAC.ID AND ");
            sb.append("PAC.ID_EMPRESA = ?3 ");
            Query query = this.getDao().createNativeQuery(sb.toString(), PlanoTratamentoProcedimento.class);
            query.setParameter(1, inicio);
            query.setParameter(2, fim);
            query.setParameter(3, ProfissionalBO.getProfissionalLogado().getIdEmpresa());
            return query.getResultList();
        } catch (Exception e) {
            log.error("Erro no findValorRepasse", e);
        }
        return null;
    }

    public List<PlanoTratamentoProcedimento> listPagos(Long idprofissional, Integer mes, boolean mesesAnteriores) {
        try {
            StringBuffer sb = new StringBuffer();
            sb.append("SELECT * FROM PLANO_TRATAMENTO_PROCEDIMENTO WHERE ");
            sb.append("(VALOR_REPASSE = COALESCE(VALOR_REPASSADO,0) OR VALOR_REPASSE < 0) ");
            sb.append("AND FINALIZADO_POR = ?1 ");
            if (mesesAnteriores) {
                Calendar c = Calendar.getInstance();
                c.set(Calendar.MONTH, mes - 1);
                c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH));
                sb.append("AND DATA_FINALIZADO::DATE <= '" + new SimpleDateFormat("yyyy-MM-dd").format(c.getTime()) + "' ");
            } else {
                sb.append("AND EXTRACT(MONTH FROM DATA_FINALIZADO) = " + mes + " AND EXTRACT(YEAR FROM DATA_FINALIZADO) <= " + Calendar.getInstance().get(Calendar.YEAR) + " ");
            }
            Query query = this.getDao().createNativeQuery(sb.toString(), PlanoTratamentoProcedimento.class);
            query.setParameter(1, idprofissional);
            query.setParameter(2, mes);
            return query.getResultList();
        } catch (Exception e) {
            log.error("Erro no findValorRepasse", e);
        }
        return null;
    }

    public List<PlanoTratamentoProcedimento> listErro2() {
        try {
            StringBuffer sb = new StringBuffer();
            sb.append("SELECT ");
            sb.append("distinct PTP.* ");
            sb.append("FROM ");
            sb.append("plano_tratamento_procedimento PTP ");
            sb.append("WHERE ");
            //sb.append("PTP.id in (17908,19177,18271,17807,3422,20339,14046,18269,20830,20367,17810,21007,18588,18273) ");
            sb.append("PTP.id in (20339,21007,20367) ");
            Query query = this.getDao().createNativeQuery(sb.toString(), PlanoTratamentoProcedimento.class);

            return query.getResultList();
        } catch (Exception e) {
            log.error("Erro no findValorRepasse", e);
        }
        return null;
    }

    public List<PlanoTratamentoProcedimento> listErro() {
        try {
            StringBuffer sb = new StringBuffer();
            sb.append("SELECT ");
            sb.append("distinct PTP.* ");
            sb.append("FROM ");
            sb.append("plano_tratamento_procedimento PTP, ");
            sb.append("repasse_profissional RP, ");
            sb.append("REPASSE_ITEM RI ");
            sb.append("WHERE ");
            sb.append("RP.ID = RI.id_repasse AND ");
            sb.append("PTP.id = RI.id_plano_tratamento_procedimento AND ");
            sb.append("RP.data_pagamento BETWEEN '2017-06-01' AND '2017-11-09' and PTP.ID = 20338");
            Query query = this.getDao().createNativeQuery(sb.toString(), PlanoTratamentoProcedimento.class);

            return query.getResultList();
        } catch (Exception e) {
            log.error("Erro no findValorRepasse", e);
        }
        return null;
    }

    public List<PlanoTratamentoProcedimento> listReservados(Long idprofissional, Integer mes, boolean mesesAnteriores) {
        try {
            StringBuffer sb = new StringBuffer();
            sb.append("SELECT * FROM PLANO_TRATAMENTO_PROCEDIMENTO WHERE STATUS_PAGAMENTO = 'R' AND ");
            sb.append("(VALOR_REPASSE = COALESCE(VALOR_REPASSADO,0) OR VALOR_REPASSE < 0) ");
            sb.append("AND FINALIZADO_POR = ?1 ");
            if (mesesAnteriores) {
                Calendar c = Calendar.getInstance();
                c.set(Calendar.MONTH, mes - 1);
                c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH));
                sb.append("AND DATA_FINALIZADO::DATE <= '" + new SimpleDateFormat("yyyy-MM-dd").format(c.getTime()) + "' ");
            } else {
                sb.append("AND EXTRACT(MONTH FROM DATA_FINALIZADO) = " + mes + " AND EXTRACT(YEAR FROM DATA_FINALIZADO) <= " + Calendar.getInstance().get(Calendar.YEAR) + " ");
            }
            Query query = this.getDao().createNativeQuery(sb.toString(), PlanoTratamentoProcedimento.class);
            query.setParameter(1, idprofissional);
            query.setParameter(2, mes);
            return query.getResultList();
        } catch (Exception e) {
            log.error("Erro no findValorRepasse", e);
        }
        return null;
    }

    public List<PlanoTratamentoProcedimento> listRepassar(Long idprofissional, Integer mes, boolean mesesAnteriores) {
        try {
            StringBuffer sb = new StringBuffer();
            sb.append("SELECT * FROM PLANO_TRATAMENTO_PROCEDIMENTO PTP WHERE ");
            sb.append("VALOR_REPASSADO > 0 ");
            sb.append("AND FINALIZADO_POR = ?1 ");
            if (mesesAnteriores) {
                Calendar c = Calendar.getInstance();
                c.set(Calendar.MONTH, mes - 1);
                c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH));
                sb.append("AND DATA_FINALIZADO::DATE <= '" + new SimpleDateFormat("yyyy-MM-dd").format(c.getTime()) + "' ");
            } else {
                sb.append("AND EXTRACT(MONTH FROM DATA_FINALIZADO) = " + mes + " AND EXTRACT(YEAR FROM DATA_FINALIZADO) <= " + Calendar.getInstance().get(Calendar.YEAR) + " ");
            }
            sb.append(
                    "AND NOT EXISTS (SELECT 1 FROM REPASSE_ITEM RI WHERE RI.ID_PLANO_TRATAMENTO_PROCEDIMENTO = PTP.ID AND STATUS IN('" + RepasseItem.REPASSADO + "','" + RepasseItem.PAGO_COMPLETO + "'))");
            sb.append("AND EXISTS (SELECT 1 FROM REPASSE_LANCAMENTO RL WHERE Rl.ID_PLANO_TRATAMENTO_PROCEDIMENTO = PTP.ID AND STATUS = '" + RepasseItem.REPASSADO + "' ) ");
            Query query = this.getDao().createNativeQuery(sb.toString(), PlanoTratamentoProcedimento.class);
            query.setParameter(1, idprofissional);
            query.setParameter(2, mes);
            return query.getResultList();
        } catch (Exception e) {
            log.error("Erro no listRepassar", e);
        }
        return null;
    }

    public List<PlanoTratamentoProcedimento> listByRelatoriosBalanco(Date inicio, Date fim, Profissional profissional, String statusPagamento) {
        try {
            StringBuffer sb = new StringBuffer();
            sb.append("SELECT DISTINCT(PTP.*) FROM PLANO_TRATAMENTO_PROCEDIMENTO PTP WHERE ");
            sb.append("VALOR_REPASSE > 0 ");
            sb.append("AND FINALIZADO_POR = ?1 ");
            sb.append("AND DATE(DATA_REPASSE) BETWEEN ?2 AND ?3 ");

            if (statusPagamento.equals(RepasseItem.PAGO_COMPLETO)) {
                sb.append("AND EXISTS (SELECT 1 FROM REPASSE_ITEM RI WHERE RI.ID_PLANO_TRATAMENTO_PROCEDIMENTO = PTP.ID AND RI.STATUS = '" + RepasseItem.PAGO_COMPLETO + "') ");
            } else if (statusPagamento.equals(RepasseItem.PAGO_PARCIAL)) {
                sb.append("AND EXISTS (SELECT 1 FROM REPASSE_ITEM RI WHERE RI.ID_PLANO_TRATAMENTO_PROCEDIMENTO = PTP.ID AND RI.STATUS = '" + RepasseItem.PAGO_PARCIAL + "') ");
                sb.append("AND NOT EXISTS (SELECT 1 FROM REPASSE_ITEM RI WHERE RI.ID_PLANO_TRATAMENTO_PROCEDIMENTO = PTP.ID AND RI.STATUS = '" + RepasseItem.PAGO_COMPLETO + "') ");
            }
            Query query = this.getDao().createNativeQuery(sb.toString(), PlanoTratamentoProcedimento.class);
            query.setParameter(1, profissional.getId());
            query.setParameter(2, inicio);
            query.setParameter(3, fim);
            query.setParameter(4, statusPagamento);
            return query.getResultList();
        } catch (Exception e) {
            log.error("Erro no listByRelatoriosBalanco", e);
        }
        return null;
    }

    public List<PlanoTratamentoProcedimento> listRelatorioResultado(Date inicio, Date fim, Profissional profissional) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        StringBuilder sb = new StringBuilder();

        sb.append("SELECT PTP.* ");
        sb.append("FROM ");
        sb.append("PLANO_TRATAMENTO_PROCEDIMENTO PTP, PLANO_TRATAMENTO PT, PROFISSIONAL P ");
        sb.append("WHERE ");
        sb.append("PTP.ID_PLANO_TRATAMENTO = PT.ID ");
        sb.append("AND PT.DATA_HORA::DATE BETWEEN '" + sdf.format(inicio) + "' AND '" + sdf.format(fim) + "' ");
        sb.append("AND PTP.EXCLUIDO = 'N' ");
        sb.append("AND PT.ID_PROFISSIONAL = P.ID ");
        sb.append("AND P.ID_EMPRESA = ? ");
        if (profissional != null) {
            sb.append("AND PTP.FINALIZADO_POR = ? ");
        }

        Query query = this.getDao().createNativeQuery(sb.toString(), PlanoTratamentoProcedimento.class);
        query.setParameter(1, ProfissionalBO.getProfissionalLogado().getIdEmpresa());
        return (query.getResultList());
    }

    public List<PlanoTratamentoProcedimento> listProcedimentosFinalizadosByPaciente(Paciente paciente) throws Exception {
        StringBuilder sb = new StringBuilder();

        sb.append("SELECT ");
        sb.append("PTP.* ");
        sb.append("FROM PLANO_TRATAMENTO_PROCEDIMENTO PTP, PLANO_TRATAMENTO PT ");
        sb.append("WHERE ");
        sb.append("PTP.ID_PLANO_TRATAMENTO = PT.ID AND ");
        sb.append("PTP.EXCLUIDO = 'N' AND ");
        sb.append("PT.ID_PACIENTE = ?1 AND ");
        sb.append("PTP.STATUS = 'F' ");
        sb.append("ORDER BY PTP.DATA_FINALIZADO DESC ");

        Query query = this.getDao().createNativeQuery(sb.toString(), PlanoTratamentoProcedimento.class);
        query.setParameter(1, paciente.getId());
        return (query.getResultList());
    }

    public Long findUltimoSequencial(Long idPlanoTratamento) {
        try {
            StringBuffer sb = new StringBuffer();
            sb.append("SELECT SEQUENCIAL FROM PLANO_TRATAMENTO_PROCEDIMENTO PTP WHERE ");
            sb.append("ID_PLANO_TRATAMENTO = ?1 AND ");
            sb.append("EXCLUIDO = 'N' ORDER BY SEQUENCIAL DESC FETCH FIRST 1 ROW ONLY ");
            Query query = this.getDao().createNativeQuery(sb.toString());
            query.setParameter(1, idPlanoTratamento);
            List<Object> resultList = query.getResultList();
            return resultList != null && !resultList.isEmpty() ? (Long) resultList.get(0) : 0L;
        } catch (Exception e) {
            log.error("Erro no findUltimoSequencial", e);
        }
        return 0L;
    }

    public List<PlanoTratamentoProcedimento> listPTPOrtodontiaOrcamento(Long idPlanoTratamento) {
        try {
            StringBuffer sb = new StringBuffer();

            sb.append("SELECT ID_PROCEDIMENTO,SUM(VALOR_DESCONTO),COUNT(1),AVG(VALOR_DESCONTO) FROM PLANO_TRATAMENTO_PROCEDIMENTO PTP WHERE ");
            sb.append("ID_PLANO_TRATAMENTO = ?1 AND ");
            sb.append("EXCLUIDO = 'N' AND ");
            sb.append("DATA_FINALIZADO IS NULL ");
            sb.append("GROUP BY ID_PROCEDIMENTO ");

            Query query = this.getDao().createNativeQuery(sb.toString());

//            query.setHint("javax.persistence.cache.storeMode", "REFRESH");
            query.setParameter(1, idPlanoTratamento);
            List<PlanoTratamentoProcedimento> retorno = new ArrayList<>();
            List<Object[]> resultList = query.getResultList();
            ProcedimentoBO procedimentoBO = new ProcedimentoBO();
            if (resultList != null && !resultList.isEmpty()) {
                for (Object[] objects : resultList) {
                    PlanoTratamentoProcedimento ptp = new PlanoTratamentoProcedimento();
                    ptp.setProcedimento(procedimentoBO.find(objects[0]));
                    ptp.setValorDesconto((BigDecimal) objects[1]);
                    ptp.setQtdProcedimentosRelatorio((Long) objects[2]);
                    ptp.setValor((BigDecimal) objects[3]);
                    retorno.add(ptp);
                }
            }
            return retorno;
        } catch (Exception e) {
            log.error("Erro no listPTPOrtodontiaOrcamento", e);
        }
        return null;
    }

    public List<PlanoTratamentoProcedimento> listProcedimentosSemValor(Profissional profissional, Integer mes, boolean mesesAnteriores) throws Exception {
        StringBuilder sb = new StringBuilder();

        sb.append("SELECT ");
        sb.append("PTP.* ");
        sb.append("FROM PLANO_TRATAMENTO_PROCEDIMENTO PTP ");
        sb.append("WHERE ");
        sb.append("FINALIZADO_POR IS NOT NULL AND ");
        sb.append("VALOR_REPASSE IS NULL AND ");
        sb.append("FINALIZADO_POR = ?1 ");
        if (mesesAnteriores) {
            Calendar c = Calendar.getInstance();
            c.set(Calendar.MONTH, mes - 1);
            c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH));
            sb.append("AND DATA_FINALIZADO::DATE <= '" + new SimpleDateFormat("yyyy-MM-dd").format(c.getTime()) + "' ");
        } else {
            sb.append("AND EXTRACT(MONTH FROM DATA_FINALIZADO) = " + mes + " AND EXTRACT(YEAR FROM DATA_FINALIZADO) <= " + Calendar.getInstance().get(Calendar.YEAR) + " ");
        }
        sb.append("ORDER BY DATA_FINALIZADO DESC ");

        Query query = this.getDao().createNativeQuery(sb.toString(), PlanoTratamentoProcedimento.class);
        query.setParameter(1, profissional.getId());
        return (query.getResultList());
    }
}
