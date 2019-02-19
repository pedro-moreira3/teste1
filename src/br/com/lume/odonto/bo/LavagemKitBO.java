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
import br.com.lume.odonto.entity.Lavagem;
import br.com.lume.odonto.entity.LavagemKit;

public class LavagemKitBO extends BO<LavagemKit> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(LavagemKitBO.class);

    public LavagemKitBO() {
        super(PersistenceUnitName.ODONTO);
        this.setClazz(LavagemKit.class);
    }

    @Override
    public boolean remove(LavagemKit entity) throws BusinessException, TechnicalException {
        entity.setExcluido(Status.SIM);
        entity.setDataExclusao(Calendar.getInstance().getTime());
        entity.setExcluidoPorProfissional(ProfissionalBO.getProfissionalLogado().getId());
        this.persist(entity);
        return true;
    }

    public List<LavagemKit> listByLavagem(Lavagem lavagem) throws Exception {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("excluido", Status.NAO);
            parametros.put("lavagem", lavagem);
            return this.listByFields(parametros);
        } catch (Exception e) {
            this.log.error("Erro no listByLavagem", e);
        }
        return null;
    }
}
