package br.com.lume.odonto.bo;

import java.util.ArrayList;
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
import br.com.lume.odonto.entity.Lavagem;

public class LavagemBO extends BO<Lavagem> {

    private Logger log = Logger.getLogger(LavagemBO.class);

    private static final long serialVersionUID = 1L;

    public LavagemBO() {
        super(PersistenceUnitName.ODONTO);
        this.setClazz(Lavagem.class);
    }

    @Override
    public boolean remove(Lavagem entity) throws BusinessException, TechnicalException {
        entity.setExcluido(Status.SIM);
        entity.setDataExclusao(Calendar.getInstance().getTime());
        entity.setExcluidoPorProfissional(ProfissionalBO.getProfissionalLogado().getId());
        this.persist(entity);
        return true;
    }

    @Override
    public List<Lavagem> listAll() throws Exception {
        return this.listByEmpresa();
    }

    public List<Lavagem> listByEmpresa() throws Exception {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("idEmpresa", ProfissionalBO.getProfissionalLogado().getIdEmpresa());
            parametros.put("excluido", Status.NAO);
            return this.listByFields(parametros);
        } catch (Exception e) {
            this.log.error("Erro no listByEmpresa", e);
        }
        return null;
    }

    public List<Lavagem> listByEmpresaAndStatus(List<String> statuss) throws Exception {
        List<Lavagem> lavagens = new ArrayList<>();
        for (String status : statuss) {
            lavagens.addAll(this.listByEmpresaAndStatus(status));
        }
        return lavagens;
    }

    public List<Lavagem> listByEmpresaAndStatus(String status) throws Exception {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("idEmpresa", ProfissionalBO.getProfissionalLogado().getIdEmpresa());
            parametros.put("status", status);
            parametros.put("excluido", Status.NAO);
            return this.listByFields(parametros);
        } catch (Exception e) {
            this.log.error("Erro no listByEmpresaAndStatus", e);
        }
        return null;
    }

    public List<Lavagem> listByEmpresaAndProfissional() throws Exception {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("idEmpresa", ProfissionalBO.getProfissionalLogado().getIdEmpresa());
            parametros.put("idProfissional", ProfissionalBO.getProfissionalLogado().getIdUsuario());
            parametros.put("excluido", Status.NAO);
            return this.listByFields(parametros);
        } catch (Exception e) {
            this.log.error("Erro no listByEmpresa", e);
        }
        return null;
    }

    public List<Lavagem> listAllByPeriodo(Date inicio, Date fim) throws Exception {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("idEmpresa", ProfissionalBO.getProfissionalLogado().getIdEmpresa());
            parametros.put("excluido", Status.NAO);
            if (inicio != null && fim != null) {
                Calendar c = Calendar.getInstance();
                c.setTime(fim);
                c.add(Calendar.DAY_OF_MONTH, 1);
                fim = c.getTime();
                parametros.put("o.data between '" + Utils.dateToString(inicio, "yyyy-MM-dd HH:mm:ss") + "' and '" + Utils.dateToString(fim, "yyyy-MM-dd HH:mm:ss") + "' ",
                        GenericListDAO.FILTRO_GENERICO_QUERY);
            }
            return this.listByFields(parametros);
        } catch (Exception e) {
            this.log.error("Erro no listAllByPeriodo", e);
        }
        return null;
    }
}
