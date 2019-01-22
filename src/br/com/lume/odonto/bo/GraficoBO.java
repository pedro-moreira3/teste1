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
import br.com.lume.odonto.entity.Grafico;

public class GraficoBO extends BO<Grafico> {

    private Logger log = Logger.getLogger(Grafico.class);

    private static final long serialVersionUID = 1L;

    public GraficoBO() {
        super(PersistenceUnitName.ODONTO);
        this.setClazz(Grafico.class);
    }

    @Override
    public List<Grafico> listAll() throws Exception {
        return this.listByEmpresa();
    }

    @Override
    public boolean remove(Grafico grafico) throws BusinessException, TechnicalException {
        grafico.setExcluido(Status.SIM);
        grafico.setDataExclusao(Calendar.getInstance().getTime());
        grafico.setExcluidoPorProfissional(ProfissionalBO.getProfissionalLogado().getId());
        this.persist(grafico);
        return true;
    }

    public List<Grafico> listByEmpresa() throws Exception {
        Map<String, Object> parametros = new HashMap<>();
        parametros.put("idEmpresa", ProfissionalBO.getProfissionalLogado().getIdEmpresa());
        parametros.put("excluido", Status.NAO);
        return this.listByFields(parametros);
    }

    public int findGenerico(Grafico grafico) throws Exception {
        Query query = this.getDao().createNativeQuery(grafico.getSql());
        return (Integer) query.getSingleResult();
    }

}
