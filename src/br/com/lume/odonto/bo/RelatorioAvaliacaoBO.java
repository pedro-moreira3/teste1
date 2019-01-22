package br.com.lume.odonto.bo;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.persistence.Query;

import org.apache.log4j.Logger;

import br.com.lume.common.exception.business.BusinessException;
import br.com.lume.common.exception.techinical.TechnicalException;
import br.com.lume.common.util.Status;
import br.com.lume.odonto.dao.PersistenceUnitName;
import br.com.lume.odonto.entity.Profissional;
import br.com.lume.odonto.entity.RelatorioAvaliacao;

public class RelatorioAvaliacaoBO extends BO<RelatorioAvaliacao> {

    /**
     *
     */
    private static final long serialVersionUID = 2338815908446282878L;
    private Logger log = Logger.getLogger(RelatorioAvaliacao.class);

    public RelatorioAvaliacaoBO() {
        super(PersistenceUnitName.ODONTO);
        this.setClazz(RelatorioAvaliacao.class);
    }

    @Override
    public boolean remove(RelatorioAvaliacao relatorioAvaliacao) throws BusinessException, TechnicalException {
        relatorioAvaliacao.setExcluido(Status.SIM);
        relatorioAvaliacao.setDataExclusao(Calendar.getInstance().getTime());
        relatorioAvaliacao.setExcluidoPorProfissional(ProfissionalBO.getProfissionalLogado().getId());
        this.persist(relatorioAvaliacao);
        return true;
    }

    public List<RelatorioAvaliacao> listAgrupadoPorProfissional() throws Exception {
        try {
            String jpql = "select a.agendamento.profissional.id  ,avg(a.avaliacao) from AvaliacaoConsulta a" + " where a.agendamento.profissional.idEmpresa = :idEmpresa AND  a.excluido= :excluido " + " group by a.agendamento.profissional.id";
            Query q = this.getDao().createQuery(jpql);
            q.setParameter("idEmpresa", ProfissionalBO.getProfissionalLogado().getIdEmpresa());
            q.setParameter("excluido", Status.NAO);
            List<Object[]> listArrayObjects = super.listArrayObjects(q);
            List<RelatorioAvaliacao> relatorio = new ArrayList<>();
            for (Object[] objects : listArrayObjects) {
                Profissional profissinal = new ProfissionalBO().find(objects[0]);
                RelatorioAvaliacao relatorioAvaliacao = new RelatorioAvaliacao(profissinal, ((Double) objects[1]).intValue());
                relatorio.add(relatorioAvaliacao);
            }
            return relatorio;
        } catch (Exception e) {
            this.log.error("Erro no listAll", e);
        }
        return null;
    }

    public List<RelatorioAvaliacao> listAgrupadoPorTotal() throws Exception {
        try {
            String jpql = "select a.avaliacao,count(a.avaliacao) from AvaliacaoConsulta a where a.agendamento.profissional.idEmpresa = :idEmpresa AND  a.excluido= :excluido " + "group by a.avaliacao";
            Query q = this.getDao().createQuery(jpql);
            q.setParameter("idEmpresa", ProfissionalBO.getProfissionalLogado().getIdEmpresa());
            q.setParameter("excluido", Status.NAO);
            return this.processaArrayObjects(q);
        } catch (Exception e) {
            this.log.error("Erro no listAll", e);
        }
        return null;
    }

    private List<RelatorioAvaliacao> processaArrayObjects(Query query) throws Exception {
        List<Object[]> listArrayObjects = super.listArrayObjects(query);
        List<RelatorioAvaliacao> relatorio = new ArrayList<>();
        for (Object[] objects : listArrayObjects) {
            RelatorioAvaliacao relatorioAvaliacao = new RelatorioAvaliacao((Integer) objects[0], (Long) objects[1]);
            relatorio.add(relatorioAvaliacao);
        }
        return relatorio;
    }

    public List<RelatorioAvaliacao> listAgrupadoPorTotalByProfissional(Profissional profissional) throws Exception {
        try {
            String jpql = "select a.avaliacao,count(a.avaliacao) from AvaliacaoConsulta a where a.agendamento.profissional.idEmpresa = :idEmpresa and a.agendamento.profissional.id = :idProfissional AND  a.excluido= :excluido " + "group by a.avaliacao";
            Query q = this.getDao().createQuery(jpql);
            q.setParameter("idEmpresa", ProfissionalBO.getProfissionalLogado().getIdEmpresa());
            q.setParameter("idProfissional", profissional.getId());
            q.setParameter("excluido", Status.NAO);
            return this.processaArrayObjects(q);
        } catch (Exception e) {
            this.log.error("Erro no listAll", e);
        }
        return null;
    }
}
