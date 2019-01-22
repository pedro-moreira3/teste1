package br.com.lume.odonto.bo;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import br.com.lume.common.exception.business.BusinessException;
import br.com.lume.common.exception.techinical.TechnicalException;
import br.com.lume.common.util.Status;
import br.com.lume.odonto.dao.PersistenceUnitName;
import br.com.lume.odonto.entity.NoticiaRss;

public class NoticiaRssBO extends BO<NoticiaRss> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(NoticiaRssBO.class);

    public NoticiaRssBO() {
        super(PersistenceUnitName.ODONTO);
        this.setClazz(NoticiaRss.class);
    }

    @Override
    public boolean remove(NoticiaRss noticiaRss) throws BusinessException, TechnicalException {
        noticiaRss.setExcluido(Status.SIM);
        noticiaRss.setDataExclusao(Calendar.getInstance().getTime());
        noticiaRss.setExcluidoPorProfissional(ProfissionalBO.getProfissionalLogado().getId());
        this.persist(noticiaRss);
        return true;
    }

    @Override
    public List<NoticiaRss> listAll() throws Exception {
        return this.listByEmpresa();
    }

    public List<NoticiaRss> listByEmpresa() throws Exception {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("idEmpresa", ProfissionalBO.getProfissionalLogado().getIdEmpresa());
            parametros.put("excluido", Status.NAO);
            return this.listByFields(parametros, new String[] { "dataPublicacao asc" });
        } catch (Exception e) {
            this.log.error("Erro no listByEmpresa", e);
        }
        return null;
    }
}
