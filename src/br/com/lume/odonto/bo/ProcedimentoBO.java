package br.com.lume.odonto.bo;

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
import br.com.lume.odonto.entity.Especialidade;
import br.com.lume.odonto.entity.Procedimento;
import br.com.lume.security.entity.Empresa;

public class ProcedimentoBO extends BO<Procedimento> {

    /**
     *
     */
    private static final long serialVersionUID = 7501719170967902429L;
    private Logger log = Logger.getLogger(ProcedimentoBO.class);

    public ProcedimentoBO() {
        super(PersistenceUnitName.ODONTO);
        this.setClazz(Procedimento.class);
    }

    @Override
    public boolean remove(Procedimento procedimento) throws BusinessException, TechnicalException {
        procedimento.setExcluido(Status.SIM);
        procedimento.setDataExclusao(Calendar.getInstance().getTime());
        procedimento.setExcluidoPorProfissional(ProfissionalBO.getProfissionalLogado().getId());
        this.persist(procedimento);
        return true;
    }

    public List<Procedimento> listByConvenio(long idConvenio) throws Exception {
        StringBuffer sb = new StringBuffer();
        sb.append("SELECT ");
        sb.append("P.* ");
        sb.append("FROM CONVENIO_PROCEDIMENTO CP, PROCEDIMENTO P ");
        sb.append("WHERE CP.ID_CONVENIO = ?1 ");
        sb.append("AND CP.EXCLUIDO = 'N' ");
        sb.append("AND P.EXCLUIDO = 'N' ");
        sb.append("AND CP.ID_PROCEDIMENTO = P.ID ");
        sb.append("ORDER BY P.DESCRICAO ");
        Query q = this.getDao().createNativeQuery(sb.toString(), Procedimento.class);
        q.setParameter(1, idConvenio);
        return q.getResultList();
    }

    public List<Procedimento> listProcedimentosSemKitVinculados() throws Exception {
        StringBuffer sb = new StringBuffer();
        sb.append("SELECT ");
        sb.append("* ");
        sb.append("FROM PROCEDIMENTO P ");
        sb.append("WHERE P.ID_EMPRESA = ?1 AND P.EXCLUIDO = 'N' ");
        sb.append("AND NOT EXISTS ");
        sb.append("( ");
        sb.append("   SELECT ");
        sb.append("   1 ");
        sb.append("   FROM PROCEDIMENTO_KIT PK ");
        sb.append("   WHERE PK.ID_PROCEDIMENTO = P.ID ");
        sb.append("   AND PK.ID_EMPRESA = ?1 ");
        sb.append(") ORDER BY P.DESCRICAO ");
        Query q = this.getDao().createNativeQuery(sb.toString(), Procedimento.class);
        q.setParameter(1, ProfissionalBO.getProfissionalLogado().getIdEmpresa());
        return q.getResultList();
    }

    public List<Procedimento> listByEmpresa() throws Exception {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("idEmpresa", ProfissionalBO.getProfissionalLogado().getIdEmpresa());
            parametros.put("excluido", Status.NAO);
            parametros.put("especialidade.excluido", Status.NAO);
            return this.listByFields(parametros, new String[] { "descricao" });
        } catch (Exception e) {
            log.error("Erro no listByEmpresa", e);
        }
        return null;
    }

    public List<Procedimento> listSugestoesComplete(String descricao) {
        try {
            if (descricao != null && !descricao.equals("")) {
                StringBuilder sb = new StringBuilder();

                sb.append("SELECT P.* ");
                sb.append("FROM PROCEDIMENTO P ");
                sb.append("WHERE ");
                sb.append("TRANSLATE(UPPER(P.DESCRICAO),'ÁÀÄÂÃÉÈËÊÍÌÏÎÓÒÖÔÕÚÙÜÇ', 'AAAAAEEEEIIIIOOOOOUUUC') LIKE '%" + descricao.toUpperCase() + "%' ");
                sb.append("AND P.ID_EMPRESA = ?1 AND P.EXCLUIDO = 'N' ");
                sb.append("ORDER BY P.DESCRICAO ");

                Query query = this.getDao().createNativeQuery(sb.toString(), Procedimento.class);

                query.setParameter(1, ProfissionalBO.getProfissionalLogado().getIdEmpresa());

                return query.getResultList();
            } else {
                return this.listByEmpresa();
            }
        } catch (Exception e) {
            log.error("Erro no listSugestoesComplete", e);
        }
        return null;
    }

    public List<Procedimento> listSugestoesCompleteOrtodontico(String descricao) {
        try {

            StringBuilder sb = new StringBuilder();

            sb.append("SELECT P.* ");
            sb.append("FROM PROCEDIMENTO P, ESPECIALIDADE E ");
            sb.append("WHERE ");
            if (descricao != null && !descricao.equals("")) {
                sb.append("TRANSLATE(UPPER(P.DESCRICAO),'ÁÀÄÂÃÉÈËÊÍÌÏÎÓÒÖÔÕÚÙÜÇ', 'AAAAAEEEEIIIIOOOOOUUUC') LIKE '%" + descricao.toUpperCase() + "%' AND ");
            }
            sb.append("P.ID_EMPRESA = ?1 AND P.EXCLUIDO = 'N' ");
            sb.append("AND P.ID_ESPECIALIDADE = E.ID AND (UPPER(E.DESCRICAO) = 'ORTODONTIA' OR UPPER(E.DESCRICAO) ='ORTODONTIA E ORTOPEDIA FACIAL') ");
            sb.append("ORDER BY P.DESCRICAO ");

            Query query = this.getDao().createNativeQuery(sb.toString(), Procedimento.class);

            query.setParameter(1, ProfissionalBO.getProfissionalLogado().getIdEmpresa());

            return query.getResultList();

        } catch (Exception e) {
            log.error("Erro no listSugestoesComplete", e);
        }
        return null;
    }

    public List<Procedimento> listSugestoesCompleteConvenio(long idConvenio, String descricao) {

        try {
            if (descricao != null && !descricao.equals("")) {
                StringBuilder sb = new StringBuilder();

                sb.append("SELECT ");
                sb.append("P.* ");
                sb.append("FROM CONVENIO_PROCEDIMENTO CP, PROCEDIMENTO P ");
                sb.append("WHERE CP.ID_CONVENIO = ?1 ");
                sb.append("AND CP.EXCLUIDO = 'N' ");
                sb.append("AND P.EXCLUIDO = 'N' ");
                sb.append("AND TRANSLATE(UPPER(P.DESCRICAO),'ÁÀÄÂÃÉÈËÊÍÌÏÎÓÒÖÔÕÚÙÜÇ', 'AAAAAEEEEIIIIOOOOOUUUC') LIKE '%" + descricao.toUpperCase() + "%' ");
                sb.append("AND CP.ID_PROCEDIMENTO = P.ID ");
                sb.append("ORDER BY P.DESCRICAO ");

                Query query = this.getDao().createNativeQuery(sb.toString(), Procedimento.class);

                query.setParameter(1, idConvenio);

                return query.getResultList();
            } else {
                return this.listByConvenio(idConvenio);
            }
        } catch (Exception e) {
            log.error("Erro no listSugestoesCompleteConvenio", e);
        }
        return null;
    }

    public Procedimento findByProcedimentoInicial() throws Exception {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("idEmpresa", ProfissionalBO.getProfissionalLogado().getIdEmpresa());
            parametros.put("excluido", Status.NAO);
            parametros.put("codigoCfo", 81000065);
            List<Procedimento> list = this.listByFields(parametros, new String[] { "descricao" });
            return list != null && !list.isEmpty() ? list.get(0) : null;
        } catch (Exception e) {
            log.error("Erro no findByProcedimentoInicial", e);
        }
        return null;
    }

    public Procedimento findByCodigoProcedimento(Integer codigoCfo) throws Exception {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("idEmpresa", ProfissionalBO.getProfissionalLogado().getIdEmpresa());
            parametros.put("excluido", Status.NAO);
            parametros.put("codigoCfo", codigoCfo);
            List<Procedimento> list = this.listByFields(parametros, new String[] { "descricao" });
            return list != null && !list.isEmpty() ? list.get(0) : null;
        } catch (Exception e) {
            log.error("Erro no findByCodigoProcedimento", e);
        }
        return null;
    }

    public Procedimento findByProcedimento(Long id) {
        try {
            return this.find(id);
        } catch (Exception e) {
            log.error("Erro no listByProcedimento", e);
        }
        return null;
    }

    public void clonarDadosEmpresaDefault(Empresa modelo, Empresa destino) {
        try {
            EspecialidadeBO especialidadeBO = new EspecialidadeBO();
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("idEmpresa", modelo.getEmpIntCod());
            parametros.put("excluido", Status.NAO);
            List<Procedimento> procedimentos = this.listByFields(parametros);

            for (Procedimento procedimento : procedimentos) {

                Especialidade esp = especialidadeBO.findByDescricaoAndEmpresa(procedimento.getEspecialidade().getDescricao(), destino);
                procedimento.setEspecialidade(esp);
                procedimento.setId(0L);
                procedimento.setIdEmpresa(destino.getEmpIntCod());
                detach(procedimento);
                persist(procedimento);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Procedimento findByDescricaoAndEmpresa(String descricao, Empresa empresa) {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("idEmpresa", empresa.getEmpIntCod());
            parametros.put("descricao", descricao);
            parametros.put("excluido", Status.NAO);
            List<Procedimento> listByFields = this.listByFields(parametros, new String[] { "id asc" });
            return listByFields != null && !listByFields.isEmpty() ? listByFields.get(0) : null;
        } catch (Exception e) {
            log.error("Erro no findByDescricaoAndEmpresa", e);
        }
        return null;
    }
}
