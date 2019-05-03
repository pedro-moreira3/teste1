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
import br.com.lume.security.EmpresaSingleton;
//import br.com.lume.security.bo.EmpresaBO;
import br.com.lume.security.entity.Empresa;

public class EspecialidadeBO extends BO<Especialidade> {

    /**
     *
     */
    private static final long serialVersionUID = 6612207441715587937L;
    private Logger log = Logger.getLogger(EspecialidadeBO.class);

    public EspecialidadeBO() {
        super(PersistenceUnitName.ODONTO);
        this.setClazz(Especialidade.class);
    }

    @Override
    public List<Especialidade> listAll() throws Exception {
        return this.listByEmpresa();
    }

    @Override
    public boolean remove(Especialidade especialidade) throws BusinessException, TechnicalException {
        especialidade.setExcluido(Status.SIM);
        especialidade.setDataExclusao(Calendar.getInstance().getTime());
        especialidade.setExcluidoPorProfissional(ProfissionalBO.getProfissionalLogado().getId());
        this.persist(especialidade);
        return true;
    }

    public Especialidade findByDescricaoAndEmpresa(String descricao) {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("idEmpresa", ProfissionalBO.getProfissionalLogado().getIdEmpresa());
            parametros.put("descricao", descricao);
            List<Especialidade> listByFields = this.listByFields(parametros, new String[] { "id asc" });
            return listByFields != null && !listByFields.isEmpty() ? listByFields.get(0) : null;
        } catch (Exception e) {
            log.error("Erro no listByEmpresa", e);
        }
        return null;
    }

    public Especialidade findByDescricaoAndEmpresa(String descricao, Empresa empresa) {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("idEmpresa", empresa.getEmpIntCod());
            parametros.put("descricao", descricao);
            List<Especialidade> listByFields = this.listByFields(parametros, new String[] { "id asc" });
            return listByFields != null && !listByFields.isEmpty() ? listByFields.get(0) : null;
        } catch (Exception e) {
            log.error("Erro no listByEmpresa", e);
        }
        return null;
    }

    public List<Especialidade> listAnamnese() throws Exception {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("idEmpresa", ProfissionalBO.getProfissionalLogado().getIdEmpresa());
            parametros.put("excluido", Status.NAO);
            parametros.put("anamnese", true);
            return this.listByFields(parametros, new String[] { "descricao asc" });
        } catch (Exception e) {
            log.error("Erro no listByEmpresa", e);
        }
        return null;
    }

    public List<Especialidade> listByEmpresa() throws Exception {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("idEmpresa", ProfissionalBO.getProfissionalLogado().getIdEmpresa());
            parametros.put("excluido", Status.NAO);
            return this.listByFields(parametros, new String[] { "descricao asc" });
        } catch (Exception e) {
            log.error("Erro no listByEmpresa", e);
        }
        return null;
    }

    public List<Especialidade> listAllByEmpresa() throws Exception {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("idEmpresa", ProfissionalBO.getProfissionalLogado().getIdEmpresa());
            return this.listByFields(parametros, new String[] { "descricao asc" });
        } catch (Exception e) {
            log.error("Erro no listByEmpresa", e);
        }
        return null;
    }

    public boolean validaDuplicidade(Especialidade especialidade) throws Exception {
        String jpql = " select vo from Especialidade as vo" + " where (vo.descricao = :descricao or " + " (vo.rangeIni between :rangeIni and :rangeFim))  AND  vo.excluido= :excluido" + " AND vo.idEmpresa = :idEmpresa";
        Query q = this.getDao().createQuery(jpql);
        q.setParameter("descricao", especialidade.getDescricao());
        q.setParameter("rangeIni", especialidade.getRangeIni());
        q.setParameter("rangeFim", especialidade.getRangeFim());
        q.setParameter("excluido", Status.NAO);
        q.setParameter("idEmpresa", ProfissionalBO.getProfissionalLogado().getIdEmpresa());
        List<Especialidade> list = this.list(q);
        if (list != null && !list.isEmpty()) {
            return true;
        }
        return false;
    }

    public void persistEspecialidadesDefault(long empIntCod) {

    }

    public void clonarDadosEmpresaDefault(Empresa modelo, Empresa destino) {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("idEmpresa", modelo.getEmpIntCod());
            parametros.put("excluido", Status.NAO);
            List<Especialidade> especialidades = this.listByFields(parametros);

            for (Especialidade especialidade : especialidades) {
                especialidade.setId(0L);
                especialidade.setIdEmpresa(destino.getEmpIntCod());
                detach(especialidade);
                persist(especialidade);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            new EspecialidadeBO().clonarDadosEmpresaDefault(EmpresaSingleton.getInstance().getBo().find(41L), EmpresaSingleton.getInstance().getBo().find(61L));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
