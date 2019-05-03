package br.com.lume.odonto.bo;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

import org.apache.log4j.Logger;

import br.com.lume.common.exception.business.BusinessException;
import br.com.lume.common.exception.techinical.TechnicalException;
import br.com.lume.common.util.Status;
import br.com.lume.common.util.Utils;
import br.com.lume.configuracao.Configurar;
import br.com.lume.odonto.dao.PersistenceUnitName;
import br.com.lume.odonto.entity.Agendamento;
import br.com.lume.odonto.entity.AgendamentoPlanoTratamentoProcedimento;
import br.com.lume.odonto.entity.Paciente;
import br.com.lume.odonto.entity.Profissional;
import br.com.lume.odonto.entity.StatusAgendamento;
import br.com.lume.odonto.util.OdontoMensagens;
import br.com.lume.security.entity.Empresa;

public class AgendamentoBO extends BO<Agendamento> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(AgendamentoBO.class);

    public AgendamentoBO() {
        super(PersistenceUnitName.ODONTO);
        this.setClazz(Agendamento.class);
    }

    @Override
    public boolean persist(Agendamento agendamento) throws BusinessException, TechnicalException {
        ReservaBO reservaBO = new ReservaBO();
//        try {
        agendamento.setDataUltAlteracao(Calendar.getInstance().getTime());
        agendamento.setProfissionalUltAlteracao(ProfissionalBO.getProfissionalLogado());
        super.persist(agendamento);
        if (Empresa.ESTOQUE_COMPLETO.equals(Configurar.getInstance().getConfiguracao().getEmpresaLogada().getEmpStrEstoque())) {
            if (agendamento.getExcluido().equals(
                    Status.NAO) && !agendamento.getStatus().equals(StatusAgendamento.CANCELADO.getSigla()) && !agendamento.getStatus().equals(StatusAgendamento.REMARCADO.getSigla())) {
                reservaBO.persistByAgendamento(agendamento);
            }
        }
//        } catch (Exception e) {
//            log.error("Erro no persist", e);
//            System.out.println(e.getMessage());
//            System.out.println(e.getLocalizedMessage());
//            return false;
//        }
        return true;
    }

    private boolean isAgendamentoProcedimentoDiferente(Agendamento agendamento) {
        try {
            if (agendamento.getPlanoTratamentoProcedimentosAgendamento() != null && agendamento.getPlanoTratamentoProcedimentosAgendamento().size() > 0) {
                int totalInserido = agendamento.getPlanoTratamentoProcedimentosAgendamento().size();
                List<AgendamentoPlanoTratamentoProcedimento> agendamentoProcedimentoBanco = new AgendamentoPlanoTratamentoProcedimentoBO().listByAgendamento(agendamento);
                int totalBanco = agendamentoProcedimentoBanco != null ? agendamentoProcedimentoBanco.size() : 0;
                return totalInserido != totalBanco;
            }
        } catch (Exception e) {
            log.error("Erro no isAgendamentoProcedimentoDiferente", e);
        }
        return false;
    }

    @Override
    public boolean remove(Agendamento agendamento) throws BusinessException, TechnicalException {
        agendamento.setExcluido(Status.SIM);
        agendamento.setDataExclusao(Calendar.getInstance().getTime());
        agendamento.setExcluidoPorProfissional(ProfissionalBO.getProfissionalLogado().getId());
        this.persist(agendamento);
        return true;
    }

    public List<Agendamento> listByDataAndProfissional(Profissional profissional, Date inicio, Date fim, List<String> filtroAgendamento) {
        try {
            if (profissional != null && inicio != null && fim != null) {
                StringBuilder sb = new StringBuilder();
                sb.append("select ");
                sb.append("vo ");
                sb.append("from Agendamento as vo ");
                sb.append("WHERE vo.profissional.id = :profissional ");
                sb.append("AND (vo.inicio between :inicio and :fim OR vo.fim between :inicio and :fim) ");
                if (filtroAgendamento != null) {
                    sb.append("AND vo.status in (" + tratarCamposInString(filtroAgendamento) + ") ");
                }
                sb.append("AND vo.excluido= :excluido ");
                sb.append("ORDER BY vo.inicio,vo.fim ");
                Query q = this.getDao().createQuery(sb.toString());
                q.setParameter("profissional", profissional.getId());
                q.setParameter("inicio", inicio);
                q.setParameter("fim", fim);
                q.setParameter("excluido", Status.NAO);
                return this.list(q);
            }
        } catch (Exception e) {
            log.error("Erro no listByDataAndProfissional", e);
        }
        return null;
    }

    public List<Agendamento> listByDataTodosProfissionais(Date inicio, Date fim, List<String> filtroAgendamento) {
        try {
            if (inicio != null && fim != null) {
                StringBuilder sb = new StringBuilder();
                sb.append("select ");
                sb.append("vo ");
                sb.append("from Agendamento as vo ");
                sb.append("WHERE vo.profissional.idEmpresa = :idEmpresa ");
                sb.append("AND (vo.inicio between :inicio and :fim OR vo.fim between :inicio and :fim) ");
                sb.append("AND vo.excluido= :excluido ");
                sb.append("AND vo.status in (" + tratarCamposInString(filtroAgendamento) + ") ");
                sb.append("ORDER BY vo.inicio,vo.fim ");
                Query q = this.getDao().createQuery(sb.toString());
                q.setParameter("idEmpresa", ProfissionalBO.getProfissionalLogado().getIdEmpresa());
                q.setParameter("inicio", inicio);
                q.setParameter("fim", fim);
                q.setParameter("excluido", Status.NAO);
                return this.list(q);
            }
        } catch (Exception e) {
            log.error("Erro no listByDataAndProfissional", e);
        }
        return null;
    }

    public List<Agendamento> listByDataAndPaciente(Paciente paciente, Date inicio, Date fim, List<String> filtroAgendamento) {
        try {
            if (paciente != null && inicio != null && fim != null) {
                StringBuilder sb = new StringBuilder();
                sb.append("select ");
                sb.append("vo ");
                sb.append("from Agendamento as vo ");
                sb.append("WHERE vo.paciente.id = :paciente ");
                sb.append("AND (vo.inicio between :inicio and :fim OR vo.fim between :inicio and :fim) ");
                sb.append("AND vo.excluido= :excluido ");
                sb.append("AND vo.status in (" + tratarCamposInString(filtroAgendamento) + ") ");
                sb.append("ORDER BY vo.inicio,vo.fim ");
                Query q = this.getDao().createQuery(sb.toString());
                q.setParameter("paciente", paciente.getId());
                q.setParameter("inicio", inicio);
                q.setParameter("fim", fim);
                q.setParameter("excluido", Status.NAO);
                return this.list(q);
            }
        } catch (Exception e) {
            log.error("Erro no listByDataAndPaciente", e);
        }
        return null;
    }

    public List<Agendamento> listByRealizadasAndPaciente(Paciente paciente) {
        try {
            if (paciente != null) {
                String jpql = "select vo from Agendamento as vo WHERE vo.paciente.id = :paciente AND vo.fim < :fim AND  vo.excluido= :excluido ORDER BY vo.inicio, vo.fim";
                Query q = this.getDao().createQuery(jpql);
                q.setParameter("paciente", paciente.getId());
                q.setParameter("fim", Calendar.getInstance().getTime());
                q.setParameter("excluido", Status.NAO);
                return this.list(q);
            }
        } catch (Exception e) {
            log.error("Erro no listByRealizadasAndPaciente", e);
        }
        return null;
    }

    public List<Agendamento> listByData() {
        try {
            String jpql = " select vo from Agendamento as vo" + " where vo.inicio between :inicio and :fim" + " and vo.status in ('P','N') AND  vo.excluido= :excluido" + " ORDER BY vo.inicio";
            Query q = this.getDao().createQuery(jpql);
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_MONTH, 2);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            Date ini = cal.getTime();
            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
            Date fim = cal.getTime();
            q.setParameter("inicio", ini);
            q.setParameter("fim", fim);
            q.setParameter("excluido", Status.NAO);
            return this.list(q);
        } catch (Exception e) {
            log.error("Erro no listByData", e);
        }
        return null;
    }

    public String getUrlConfirmacao(Agendamento agendamento) {
        try {
            return OdontoMensagens.getMensagemOffLine(
                    new DominioBO().listByEmpresaAndObjetoAndTipo("agendamento", "urlConfirmacao").get(0).getNome()) + "?hash=" + agendamento.getHash() + "&agendamento=" + agendamento.getId();
        } catch (Exception e) {
            log.error("getUrlConfirmacao: " + e);
        }
        return null;
    }

    public Agendamento findByHash(String id, String hash) throws Exception {
        Map<String, Object> parametros = new HashMap<>();
        parametros.put("id", Long.parseLong(id));
        parametros.put("hash", hash);
        parametros.put("excluido", Status.NAO);
        return this.findByFields(parametros);
    }

    public void enviaEmailBatch() {
        // List<Agendamento> agendamentos;
        // try {
        // agendamentos = listByData();
        // if (agendamentos != null) {
        // for (Agendamento agendamento : agendamentos) {
        // if (agendamento != null) {
        // String de = OdontoMensagens.getMensagemOffLine("odonto.email.from");
        // String para = agendamento.getPaciente().getDadosBasico().getEmail();
        // String assunto = OdontoMensagens.getMensagemOffLine("agendamento.titulo.email.batch");
        // String corpo = OdontoMensagens.getMensagemOffLine("agendamento.corpo.email.batch");
        // Empresa empresa = new EmpresaBO().find(agendamento.getProfissional().getIdEmpresa());
        // corpo = corpo.replaceFirst("\\{0\\}", getUrlConfirmacao(agendamento));
        // corpo = corpo.replaceFirst("\\{1\\}", agendamento.getPaciente().getDadosBasico().getNome());
        // corpo = corpo.replaceFirst("\\{2\\}", agendamento.getInicioStr());
        // corpo = corpo.replaceFirst("\\{3\\}", empresa.getEmpStrEndereco());
        // corpo = corpo.replaceFirst("\\{4\\}", ProfissionalBO.getProfissionalLogado().getDadosBasico().getNome());
        // corpo = corpo.replaceFirst("\\{5\\}", empresa.getEmpChaFone());
        // corpo = corpo.replaceFirst("\\{6\\}", empresa.getEmpStrNmefantasia());
        // corpo = corpo.replaceFirst("\\{7\\}", agendamento.getPaciente().getDadosBasico().getEmail());
        // EnviaEmail.enviaEmailOffLine(de, para, assunto, EnviaEmail.getTemplate(null, corpo));
        // }
        // }
        // }
        // } catch (Exception e) {
        // log.error("Erro no enviaEmailBatch", e);
        // }
    }

    public List<Agendamento> listByProfissional(Profissional profissional) {
        try {
            if (profissional != null) {
                String jpql = "select vo from Agendamento as vo" + " WHERE vo.profissional.id = :profissional AND  vo.excluido= :excluido " + " ORDER BY vo.inicio, vo.fim";
                Query q = this.getDao().createQuery(jpql);
                q.setParameter("profissional", profissional.getId());
                q.setParameter("excluido", Status.NAO);
                return this.list(q);
            }
        } catch (Exception e) {
            log.error("Erro no listByProfissional", e);
        }
        return null;
    }

    public List<Agendamento> listByPacienteAndStatus(Paciente paciente) {
        try {
            if (paciente != null) {
                String jpql = "select a from Agendamento as a" + " WHERE a.paciente.id = :paciente  AND  a.excluido= :excluido AND a.status in('P','S','N','E')" + " ORDER BY a.inicio, a.fim";
                Query q = this.getDao().createQuery(jpql);
                q.setParameter("paciente", paciente.getId());
                q.setParameter("excluido", Status.NAO);
                return this.list(q);
            }
        } catch (Exception e) {
            log.error("Erro no listByPaciente", e);
        }
        return null;
    }

    public List<Agendamento> listByPaciente(Paciente paciente) {
        try {
            if (paciente != null) {
                String jpql = "select a from Agendamento as a" + " WHERE a.paciente.id = :paciente  AND  a.excluido= :excluido" + " ORDER BY a.inicio desc";
                Query q = this.getDao().createQuery(jpql);
                q.setParameter("paciente", paciente.getId());
                q.setParameter("excluido", Status.NAO);
                return this.list(q);
            }
        } catch (Exception e) {
            log.error("Erro no listByPaciente", e);
        }
        return null;
    }
    
    public String findDataProximoAgendamentoPaciente(Paciente paciente) {
        try {
            if (paciente != null) {
                StringBuilder sb = new StringBuilder();
                sb.append("select ");
                sb.append("* ");
                sb.append("from Agendamento as a ");
                sb.append("WHERE date(a.inicio) > '" + Utils.dateToString(new Date(), "yyyy-MM-dd") + "'");
                sb.append("AND a.excluido = 'N' ");
                sb.append("AND a.ID_PACIENTE = ?1 ");               
                sb.append("ORDER BY a.inicio ASC LIMIT 1");
                Query q = this.getDao().createNativeQuery(sb.toString(), Agendamento.class);
                q.setParameter(1, paciente.getId());
                return this.list(q).get(0).getInicioStr();
            }
        } catch (Exception e) {
            log.error("Erro no findDataProximoAgendamentoPaciente", e);
        }
        return null;
    }
  
    public String findStatusProximoAgendamentoPaciente(Paciente paciente) {
        try {
            if (paciente != null) {
                StringBuilder sb = new StringBuilder();
                sb.append("select ");
                sb.append("* ");
                sb.append("from Agendamento as a ");
                sb.append("WHERE date(a.inicio) > '" + Utils.dateToString(new Date(), "yyyy-MM-dd") + "'");
                sb.append("AND a.excluido = 'N' ");
                sb.append("AND a.ID_PACIENTE = ?1 ");               
                sb.append("ORDER BY a.inicio ASC LIMIT 1");
                Query q = this.getDao().createNativeQuery(sb.toString(), Agendamento.class);
                q.setParameter(1, paciente.getId());
                
                return StatusAgendamento.findBySigla(this.list(q).get(0).getStatus()).getDescricao();
            }
        } catch (Exception e) {
            log.error("Erro no findStatusProximoAgendamentoPaciente", e);
        }
        return null;
    }    
    

    public List<Agendamento> listByProfissionalAndStatusAndDataLimite(Profissional profissional, Date data) {
        try {
            if (profissional != null) {
                StringBuilder sb = new StringBuilder();
                sb.append("select ");
                sb.append("* ");
                sb.append("from Agendamento as a ");
                sb.append("WHERE date(a.inicio) = '" + Utils.dateToString(data, "yyyy-MM-dd") + "'");
                sb.append("AND a.excluido = 'N' ");
                sb.append("AND a.ID_DENTISTA = ?1 ");
                sb.append("AND a.status in('P','S','N','E','A','I','O') ");
                sb.append("ORDER BY a.inicio ASC ");
                Query q = this.getDao().createNativeQuery(sb.toString(), Agendamento.class);
                q.setParameter(1, profissional.getId());
                return this.list(q);
            }
        } catch (Exception e) {
            log.error("Erro no listByProfissionalAndStatusAtivo", e);
        }
        return null;
    }

    public Long countByClienteNaClinica() {
        return this.countByStatus(StatusAgendamento.CLIENTE_NA_CLINICA.getSigla());
    }

    public Long countByStatus(String status) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT ");
            sb.append("COUNT(1) ");
            sb.append("FROM AGENDAMENTO A, PROFISSIONAL P ");
            sb.append("WHERE A.STATUS = '" + status + "' ");
            sb.append("AND A.EXCLUIDO = 'N' ");
            sb.append("AND DATE(A.INICIO) = CURRENT_DATE ");
            sb.append("AND A.ID_DENTISTA = P.ID ");
            sb.append("AND P.ID_EMPRESA = ?1 ");
            Query query = this.getDao().createNativeQuery(sb.toString());
            query.setParameter(1, ProfissionalBO.getProfissionalLogado().getIdEmpresa());
            return (Long) query.getSingleResult();
        } catch (Exception e) {
            log.error("Erro no countByStatus", e);
        }
        return null;
    }

    public Long countByAtendidos() {
        return this.countByStatus(StatusAgendamento.ATENDIDO.getSigla());
    }

    public Long countByPacienteNaoChegou() {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT ");
            sb.append("COUNT(1) ");
            sb.append("FROM AGENDAMENTO A, PROFISSIONAL P ");
            sb.append("WHERE A.STATUS IN ('P','S','N','E') ");
            sb.append("AND A.EXCLUIDO = 'N' ");
            sb.append("AND DATE(A.INICIO) = CURRENT_DATE ");
            sb.append("AND A.ID_DENTISTA = P.ID ");
            sb.append("AND P.ID_EMPRESA = ?1 ");
            sb.append("AND A.CHEGOU_AS IS NULL ");
            Query query = this.getDao().createNativeQuery(sb.toString());
            query.setParameter(1, ProfissionalBO.getProfissionalLogado().getIdEmpresa());
            return (Long) query.getSingleResult();
        } catch (Exception e) {
            log.error("Erro no listByAtendidos", e);
        }
        return null;
    }

    public List<Agendamento> countByFuturos(String filtro) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT ");
            sb.append("DISTINCT(A.*) ");
            sb.append("FROM AGENDAMENTO A, PROFISSIONAL P ");
            sb.append("WHERE A.STATUS IN ('P','S','N','E') ");
            sb.append("AND A.EXCLUIDO = 'N' ");
            sb.append("AND DATE(A.INICIO) = " + filtro + " ");
            sb.append("AND A.ID_DENTISTA = P.ID ");
            sb.append("AND P.ID_EMPRESA = ?1 ");
            sb.append("AND A.CHEGOU_AS IS NULL ");
            Query query = this.getDao().createNativeQuery(sb.toString(), Agendamento.class);
            query.setParameter(1, ProfissionalBO.getProfissionalLogado().getIdEmpresa());
            return query.getResultList();
        } catch (Exception e) {
            log.error("Erro no countByFuturos", e);
        }
        return null;
    }

    public List<Agendamento> listAgendmantosValidosByDate(String data) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT ");
            sb.append("DISTINCT(A.*),DB.NOME  ");
            sb.append("FROM AGENDAMENTO A, PROFISSIONAL P, DADOS_BASICOS DB ");
            sb.append("WHERE A.STATUS NOT IN ('F','C','R') ");
            sb.append("AND A.EXCLUIDO = 'N' ");
            sb.append("AND DATE(A.INICIO) = " + data + " ");
            sb.append("AND P.ID_DADOS_BASICOS = DB.ID ");
            sb.append("AND A.ID_DENTISTA = P.ID ");
            sb.append("AND P.ID_EMPRESA = ?1 ");
            sb.append("ORDER BY A.INICIO,DB.NOME ");
            Query query = this.getDao().createNativeQuery(sb.toString(), Agendamento.class);
            query.setParameter(1, ProfissionalBO.getProfissionalLogado().getIdEmpresa());
            return query.getResultList();
        } catch (Exception e) {
            log.error("Erro no listAgendmantosValidosByDate", e);
        }
        return null;
    }

    public List<Agendamento> listAgendmantosValidosDeHojeByProfissional(Profissional profissional) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT ");
            sb.append("DISTINCT(A.*) ");
            sb.append("FROM AGENDAMENTO A, PROFISSIONAL P ");
            sb.append("WHERE A.STATUS NOT IN ('F','C','R') ");
            sb.append("AND A.EXCLUIDO = 'N' ");
            sb.append("AND DATE(A.INICIO) = CURRENT_DATE ");
            sb.append("AND A.ID_DENTISTA = ?1 ");
            sb.append("ORDER BY A.INICIO ");
            Query query = this.getDao().createNativeQuery(sb.toString(), Agendamento.class);
            query.setParameter(1, profissional.getId());
            return query.getResultList();
        } catch (Exception e) {
            log.error("Erro no listAgendmantosDeHojeByProfissional", e);
        }
        return null;
    }

    public Long countByEmAtendimento() {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT ");
            sb.append("COUNT(1) ");
            sb.append("FROM AGENDAMENTO A, PROFISSIONAL P ");
            sb.append("WHERE A.STATUS = '" + StatusAgendamento.EM_ATENDIMENTO.getSigla() + "' ");
            sb.append("AND A.EXCLUIDO = 'N' ");
            sb.append("AND DATE(A.INICIO) = CURRENT_DATE ");
            sb.append("AND A.ID_DENTISTA = P.ID ");
            sb.append("AND P.ID_EMPRESA = ?1 ");
            sb.append("AND A.CHEGOU_AS IS NOT NULL ");
            sb.append("AND A.INICIOU_AS IS NOT NULL ");
            Query query = this.getDao().createNativeQuery(sb.toString());
            query.setParameter(1, ProfissionalBO.getProfissionalLogado().getIdEmpresa());
            return (Long) query.getSingleResult();
        } catch (Exception e) {
            log.error("Erro no listByAtendidos", e);
        }
        return null;
    }

    public Long countByAtrasado() {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT ");
            sb.append("COUNT(1) ");
            sb.append("FROM AGENDAMENTO A, PROFISSIONAL P ");
            sb.append("WHERE A.STATUS = '" + StatusAgendamento.CLIENTE_NA_CLINICA.getSigla() + "' ");
            sb.append("AND A.EXCLUIDO = 'N' ");
            sb.append("AND DATE(A.INICIO) = CURRENT_DATE ");
            sb.append("AND A.ID_DENTISTA = P.ID ");
            sb.append("AND P.ID_EMPRESA = ?1 ");
            sb.append("AND A.CHEGOU_AS > A.INICIO ");
            sb.append("AND A.INICIOU_AS IS NULL ");
            Query query = this.getDao().createNativeQuery(sb.toString());
            query.setParameter(1, ProfissionalBO.getProfissionalLogado().getIdEmpresa());
            return (Long) query.getSingleResult();
        } catch (Exception e) {
            log.error("Erro no listByAtendidos", e);
        }
        return null;
    }

    public List<Agendamento> listByDataAndProfissionalAndFilial(Profissional profissional, Date inicio, Date fim) {
        try {
            if (profissional != null && inicio != null && fim != null) {
                String jpql = "";
                jpql = "select vo from Agendamento as vo" + " WHERE vo.profissional.id = :profissional" + " AND (vo.inicio between :inicio and :fim" + " OR vo.fim between :inicio and :fim) AND  vo.excluido= :excluido  ORDER BY vo.inicio, vo.fim";
                Query q = this.getDao().createQuery(jpql);
                q.setParameter("profissional", profissional.getId());
                q.setParameter("inicio", inicio);
                q.setParameter("fim", fim);
                q.setParameter("excluido", Status.NAO);
                return this.list(q);
            }
        } catch (Exception e) {
            log.error("Erro no listByDataAndProfissional", e);
        }
        return null;
    }

    public List<Agendamento> listByDataAndPacienteAndFilial(Paciente paciente, Date inicio, Date fim) {
        try {
            if (paciente != null && inicio != null && fim != null) {
                String jpql = "";
                jpql = "select vo from Agendamento as vo" + " WHERE vo.paciente.id = :paciente" + " AND (vo.inicio between :inicio and :fim" + " OR vo.fim between :inicio and :fim) AND  vo.excluido= :excluido  ORDER BY vo.inicio, vo.fim";
                Query q = this.getDao().createQuery(jpql);
                q.setParameter("paciente", paciente.getId());
                q.setParameter("inicio", inicio);
                q.setParameter("fim", fim);
                q.setParameter("excluido", Status.NAO);
                return this.list(q);
            }
        } catch (Exception e) {
            log.error("Erro no listByDataAndPaciente", e);
        }
        return null;
    }

    public List<Object[]> listQuantidadeAgendamentosMes() throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT ");
        sb.append("EXTRACT(YEAR FROM A.INICIO)||''||EXTRACT(MONTH FROM A.INICIO),COUNT(1) ");
        sb.append("FROM AGENDAMENTO A,PROFISSIONAL P ");
        sb.append("WHERE A.ID_DENTISTA = P.ID ");
        sb.append("AND P.ID_EMPRESA = ?1 ");
        sb.append("AND A.STATUS NOT IN ('F','C','R') ");
        sb.append("AND A.DATA_AGENDAMENTO IS NOT NULL ");
        sb.append("GROUP BY EXTRACT(YEAR FROM A.INICIO)||''||EXTRACT(MONTH FROM A.INICIO) ");
        sb.append("ORDER BY EXTRACT(YEAR FROM A.INICIO)||''||EXTRACT(MONTH FROM A.INICIO) DESC ");
        Query query = this.getDao().createNativeQuery(sb.toString());
        query.setParameter(1, ProfissionalBO.getProfissionalLogado().getIdEmpresa());
        List<Object[]> resultList = query.getResultList();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM");
        for (Object[] objects : resultList) {
            String data = "" + objects[0];
            objects[0] = Utils.dateToString(sdf.parse(data), "MMMM/yyyy");
        }
        return resultList;
    }

    public Integer findQuantidadeAgendamentosMesAtual(long empIntCod) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT ");
        sb.append("COUNT(1)::integer ");
        sb.append("FROM AGENDAMENTO A, PROFISSIONAL P ");
        sb.append("WHERE A.ID_DENTISTA = P.ID ");
        sb.append("AND P.ID_EMPRESA = ?1 ");
        sb.append("AND A.STATUS NOT IN ('F','C','R') ");
        sb.append("AND EXTRACT(YEAR FROM INICIO)||''||EXTRACT(MONTH FROM INICIO) = EXTRACT(YEAR FROM CURRENT_DATE)||''||EXTRACT(MONTH FROM CURRENT_DATE) ");
        sb.append("AND EXTRACT(YEAR FROM FIM)||''||EXTRACT(MONTH FROM FIM) = EXTRACT(YEAR FROM CURRENT_DATE)||''||EXTRACT(MONTH FROM CURRENT_DATE) ");
        Query query = this.getDao().createNativeQuery(sb.toString());
        query.setParameter(1, empIntCod);
        return (Integer) query.getSingleResult();
    }

    public Integer findQuantidadeAgendamentosHoje(long empIntCod) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT ");
        sb.append("COUNT(1)::integer ");
        sb.append("FROM AGENDAMENTO A, PROFISSIONAL P ");
        sb.append("WHERE A.ID_DENTISTA = P.ID ");
        sb.append("AND P.ID_EMPRESA = ?1 ");
        sb.append("AND A.STATUS NOT IN ('F','C','R') ");
        sb.append("AND DATE(INICIO) = CURRENT_DATE ");
        sb.append("AND DATE(FIM) = CURRENT_DATE ");
        Query query = this.getDao().createNativeQuery(sb.toString());
        query.setParameter(1, empIntCod);
        return (Integer) (query.getSingleResult());
    }
}
