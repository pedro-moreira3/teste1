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
import br.com.lume.configuracao.Configurar;
import br.com.lume.odonto.dao.PersistenceUnitName;
import br.com.lume.odonto.entity.Local;
//import br.com.lume.security.bo.EmpresaBO;

public class LocalBO extends BO<Local> {

    private Logger log = Logger.getLogger(LocalBO.class);

    private static final long serialVersionUID = 1L;

    public LocalBO() {
        super(PersistenceUnitName.ODONTO);
        this.setClazz(Local.class);
    }

    @Override
    public boolean remove(Local local) throws BusinessException, TechnicalException {
        local.setExcluido(Status.SIM);
        local.setDataExclusao(Calendar.getInstance().getTime());
        local.setExcluidoPorProfissional(ProfissionalBO.getProfissionalLogado().getId());
        this.persist(local);
        return true;
    }

    @Override
    public List<Local> listAll() throws Exception {
        return this.listByEmpresa();
    }

    public List<Local> listByEmpresa() throws Exception {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("idEmpresa", ProfissionalBO.getProfissionalLogado().getIdEmpresa());
            parametros.put("excluido", Status.NAO);
            return this.listByFields(parametros);
        } catch (Exception e) {
            log.error("Erro no listByEmpresa", e);
        }
        return null;
    }

    public List<Local> listByEmpresaAndTipo(String tipo) throws Exception {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("idEmpresa", ProfissionalBO.getProfissionalLogado().getIdEmpresa());
            parametros.put("tipo", tipo);
            parametros.put("excluido", Status.NAO);
            return this.listByFields(parametros);
        } catch (Exception e) {
            log.error("Erro no listByEmpresaAndTipo", e);
        }
        return null;
    }

    public List<Local> listByEmpresaAndDescricaoParcial(String digitacao) throws Exception {
        try {
            String jpql = "SELECT l FROM Local AS l " + "WHERE UPPER(l.descricao) LIKE :descricao AND l.idEmpresa = :empresa AND  l.excluido= :excluido";
            Query q = this.getDao().createQuery(jpql);
            q.setParameter("empresa", ProfissionalBO.getProfissionalLogado().getIdEmpresa());
            q.setParameter("descricao", "%" + digitacao.toUpperCase() + "%");
            q.setParameter("excluido", Status.NAO);
            return this.list(q);
        } catch (Exception e) {
            log.error("Erro no listByEmpresaAndDescricaoParcial", e);
        }
        return null;
    }

    public Local getLocalPadraoSistema() {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("idEmpresa", ProfissionalBO.getProfissionalLogado().getIdEmpresa());
            parametros.put("excluido", Status.NAO);
            parametros.put("descricao", "Local Padrão Sistema");
            parametros.put("tipo", "SM");
            List<Local> locais = this.listByFields(parametros);
            if (locais != null && !locais.isEmpty()) {
                return locais.get(0);
            } else {
                Local local = new Local("Local Padrão Sistema", "SM", Configurar.getInstance().getConfiguracao().getEmpresaLogada().getEmpIntCod());
                persist(local);
                return local;
            }
        } catch (Exception e) {
            log.error("Erro no listByEmpresa", e);
        }
        return null;
    }

}
