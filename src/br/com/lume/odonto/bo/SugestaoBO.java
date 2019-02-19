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
import br.com.lume.odonto.entity.Profissional;
import br.com.lume.odonto.entity.Sugestao;

public class SugestaoBO extends BO<Sugestao> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(SugestaoBO.class);

    public SugestaoBO() {
        super(PersistenceUnitName.ODONTO);
        this.setClazz(Sugestao.class);
    }

    @Override
    public boolean remove(Sugestao sugestao) throws BusinessException, TechnicalException {
        sugestao.setExcluido(Status.SIM);
        sugestao.setDataExclusao(Calendar.getInstance().getTime());
        sugestao.setExcluidoPorProfissional(ProfissionalBO.getProfissionalLogado().getId());
        this.persist(sugestao);
        return true;
    }

    public List<Sugestao> listByProfissional(Profissional profissional) throws Exception {
        Map<String, Object> parametros = new HashMap<>();
        parametros.put("profissional.id", profissional.getId());
        parametros.put("excluido", Status.NAO);
        return this.listByFields(parametros);
    }

    @Override
    public List<Sugestao> listAll() throws Exception {
        return this.listByEmpresa();
    }

    public List<Sugestao> listByEmpresa() throws Exception {
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
