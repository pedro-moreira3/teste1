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
import br.com.lume.odonto.entity.Resposta;

public class RespostaBO extends BO<Resposta> {

    /**
     *
     */
    private static final long serialVersionUID = -1503965041967634034L;
    private Logger log = Logger.getLogger(RespostaBO.class);

    public RespostaBO() {
        super(PersistenceUnitName.ODONTO);
        this.setClazz(Resposta.class);
    }

    @Override
    public boolean remove(Resposta resposta) throws BusinessException, TechnicalException {
        resposta.setExcluido(Status.SIM);
        resposta.setDataExclusao(Calendar.getInstance().getTime());
        resposta.setExcluidoPorProfissional(ProfissionalBO.getProfissionalLogado().getId());
        this.persist(resposta);
        return true;
    }

    @Override
    public List<Resposta> listAll() throws Exception {
        return this.listByEmpresa();
    }

    public List<Resposta> listByEmpresa() {
        try {
            Map<String, Object> parametros = new HashMap<>();
            Long idEmpresa = ProfissionalBO.getProfissionalLogado().getIdEmpresa();
            parametros.put("pergunta.idEmpresa", idEmpresa);
            parametros.put("excluido", Status.NAO);
            return this.listByFields(parametros);
        } catch (Exception e) {
            this.log.error("Erro no listByEmpresa", e);
        }
        return null;
    }
}
