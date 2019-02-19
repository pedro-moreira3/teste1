package br.com.lume.odonto.bo;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import br.com.lume.common.connection.GenericListDAO;
import br.com.lume.common.exception.business.BusinessException;
import br.com.lume.common.exception.techinical.TechnicalException;
import br.com.lume.common.util.Status;
import br.com.lume.odonto.dao.PersistenceUnitName;
import br.com.lume.odonto.entity.GraficoProfissional;
import br.com.lume.odonto.entity.Profissional;

public class GraficoProfissionalBO extends BO<GraficoProfissional> {

    private Logger log = Logger.getLogger(GraficoProfissional.class);

    private static final long serialVersionUID = 1L;

    public GraficoProfissionalBO() {
        super(PersistenceUnitName.ODONTO);
        this.setClazz(GraficoProfissional.class);
    }

    @Override
    public List<GraficoProfissional> listAll() throws Exception {
        return this.listByEmpresa();
    }

    @Override
    public boolean remove(GraficoProfissional grafico) throws BusinessException, TechnicalException {
        grafico.setExcluido(Status.SIM);
        grafico.setDataExclusao(Calendar.getInstance().getTime());
        grafico.setExcluidoPorProfissional(ProfissionalBO.getProfissionalLogado().getId());
        this.persist(grafico);
        return true;
    }

    public List<GraficoProfissional> listByEmpresa() throws Exception {
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

    public List<GraficoProfissional> listByProfissional(Profissional profissional) throws Exception {
        Map<String, Object> parametros = new HashMap<>();
        parametros.put("idEmpresa", ProfissionalBO.getProfissionalLogado().getIdEmpresa());
        parametros.put("excluido", Status.NAO);
        parametros.put("profissional", profissional);
        return this.listByFields(parametros);
    }

    public List<GraficoProfissional> listByNotInProfissional(Profissional profissional) throws Exception {
        Map<String, Object> parametros = new HashMap<>();
        parametros.put("idEmpresa", ProfissionalBO.getProfissionalLogado().getIdEmpresa());
        parametros.put("excluido", Status.NAO);
        parametros.put("o.profissional.id not in (" + profissional.getId() + ")", GenericListDAO.FILTRO_GENERICO_QUERY);
        return this.listByFields(parametros);
    }

}
