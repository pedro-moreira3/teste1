package br.com.lume.odonto.bo;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import br.com.lume.common.connection.GenericListDAO;
import br.com.lume.common.exception.business.BusinessException;
import br.com.lume.common.exception.techinical.TechnicalException;
import br.com.lume.common.util.Status;
import br.com.lume.common.util.Utils;
import br.com.lume.odonto.dao.PersistenceUnitName;
import br.com.lume.odonto.entity.Esterilizacao;

public class EsterilizacaoBO extends BO<Esterilizacao> {

    private Logger log = Logger.getLogger(EsterilizacaoBO.class);

    private static final long serialVersionUID = 1L;

    public EsterilizacaoBO() {
        super(PersistenceUnitName.ODONTO);
        this.setClazz(Esterilizacao.class);
    }

    @Override
    public boolean remove(Esterilizacao esterilizacao) throws BusinessException, TechnicalException {
        esterilizacao.setExcluido(Status.SIM);
        esterilizacao.setDataExclusao(Calendar.getInstance().getTime());
        esterilizacao.setExcluidoPorProfissional(ProfissionalBO.getProfissionalLogado().getId());
        this.persist(esterilizacao);
        return true;
    }

    @Override
    public List<Esterilizacao> listAll() throws Exception {
        return this.listByEmpresa();
    }

    public List<Esterilizacao> listByEmpresa() throws Exception {
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

    public List<Esterilizacao> listByEmpresaAndStatus(String status) throws Exception {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("idEmpresa", ProfissionalBO.getProfissionalLogado().getIdEmpresa());
            parametros.put("status", status);
            parametros.put("excluido", Status.NAO);
            return this.listByFields(parametros);
        } catch (Exception e) {
            log.error("Erro no listByEmpresaAndStatus", e);
        }
        return null;
    }

    public List<Esterilizacao> listByEmpresaAndProfissional() throws Exception {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("idEmpresa", ProfissionalBO.getProfissionalLogado().getIdEmpresa());
            parametros.put("idProfissional", ProfissionalBO.getProfissionalLogado().getIdUsuario());
            parametros.put("excluido", Status.NAO);
            return this.listByFields(parametros);
        } catch (Exception e) {
            log.error("Erro no listByEmpresa", e);
        }
        return null;
    }

    public List<Esterilizacao> listAllByPeriodo(Date inicio, Date fim) {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("idEmpresa", ProfissionalBO.getProfissionalLogado().getIdEmpresa());
            parametros.put("excluido", Status.NAO);
            if (inicio != null && fim != null) {
                parametros.put("o.data between '" + Utils.dateToString(inicio, "yyyy-MM-dd HH:mm:ss") + "' and '" + Utils.dateToString(fim, "yyyy-MM-dd HH:mm:ss") + "' ",
                        GenericListDAO.FILTRO_GENERICO_QUERY);
            }
            return this.listByFields(parametros);
        } catch (Exception e) {
            log.error("Erro no listAllByPeriodo", e);
        }
        return null;
    }
}
