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
import br.com.lume.odonto.entity.Noticia;

public class NoticiaBO extends BO<Noticia> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(NoticiaBO.class);

    public NoticiaBO() {
        super(PersistenceUnitName.ODONTO);
        this.setClazz(Noticia.class);
    }

    @Override
    public boolean remove(Noticia noticia) throws BusinessException, TechnicalException {
        noticia.setExcluido(Status.SIM);
        noticia.setDataExclusao(Calendar.getInstance().getTime());
        noticia.setExcluidoPorProfissional(ProfissionalBO.getProfissionalLogado().getId());
        this.persist(noticia);
        return true;
    }

    @Override
    public List<Noticia> listAll() throws Exception {
        return this.listByEmpresa();
    }

    public List<Noticia> listByEmpresa() throws Exception {
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
}
