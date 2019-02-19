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
import br.com.lume.odonto.entity.Esterilizacao;
import br.com.lume.odonto.entity.EsterilizacaoKit;

public class EsterilizacaoKitBO extends BO<EsterilizacaoKit> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(EsterilizacaoKitBO.class);

    public EsterilizacaoKitBO() {
        super(PersistenceUnitName.ODONTO);
        this.setClazz(EsterilizacaoKit.class);
    }

    @Override
    public boolean remove(EsterilizacaoKit esterilizacaoKit) throws BusinessException, TechnicalException {
        esterilizacaoKit.setExcluido(Status.SIM);
        esterilizacaoKit.setDataExclusao(Calendar.getInstance().getTime());
        esterilizacaoKit.setExcluidoPorProfissional(ProfissionalBO.getProfissionalLogado().getId());
        this.persist(esterilizacaoKit);
        return true;
    }

    public List<EsterilizacaoKit> listByEsterilizacao(Esterilizacao esterilizacao) throws Exception {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("excluido", Status.NAO);
            parametros.put("esterilizacao", esterilizacao);
            return this.listByFields(parametros);
        } catch (Exception e) {
            this.log.error("Erro no listByEsterilizacao", e);
        }
        return null;
    }

}
