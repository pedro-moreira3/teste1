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
import br.com.lume.odonto.entity.Desconto;

public class DescontoBO extends BO<Desconto> {

    private Logger log = Logger.getLogger(Desconto.class);

    private static final long serialVersionUID = 1L;

    public DescontoBO() {
        super(PersistenceUnitName.ODONTO);
        this.setClazz(Desconto.class);
    }

    @Override
    public List<Desconto> listAll() throws Exception {
        return this.listByEmpresa();
    }

    @Override
    public boolean remove(Desconto entity) throws BusinessException, TechnicalException {
        entity.setExcluido(Status.SIM);
        entity.setDataExclusao(Calendar.getInstance().getTime());
        entity.setExcluidoPorProfissional(ProfissionalBO.getProfissionalLogado().getId());
        this.persist(entity);
        return true;
    }

    public List<Desconto> listByEmpresa() throws Exception {
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

    public List<Desconto> listByEmpresaAndForma(String formaPagamento) {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("idEmpresa", ProfissionalBO.getProfissionalLogado().getIdEmpresa());
            parametros.put("formaPagamento", formaPagamento);
            parametros.put("excluido", Status.NAO);
            return this.listByFields(parametros);
        } catch (Exception e) {
            this.log.error("Erro no listByEmpresa", e);
        }
        return null;
    }

}
