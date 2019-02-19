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
import br.com.lume.odonto.entity.Fornecedor;
import br.com.lume.odonto.entity.FornecedorItem;

public class FornecedorItemBO extends BO<FornecedorItem> {

    private Logger log = Logger.getLogger(FornecedorItemBO.class);

    private static final long serialVersionUID = 1L;

    public FornecedorItemBO() {
        super(PersistenceUnitName.ODONTO);
        this.setClazz(FornecedorItem.class);
    }

    @Override
    public boolean remove(FornecedorItem kitItem) throws BusinessException, TechnicalException {
        kitItem.setExcluido(Status.SIM);
        kitItem.setDataExclusao(Calendar.getInstance().getTime());
        kitItem.setExcluidoPorProfissional(ProfissionalBO.getProfissionalLogado().getId());
        this.persist(kitItem);
        return true;
    }

    @Override
    public List<FornecedorItem> listAll() throws Exception {
        return this.listByEmpresa();
    }

    public List<FornecedorItem> listByEmpresa() throws Exception {
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

    public FornecedorItem findByFornecedorAndItem(FornecedorItem fornecedorItem) {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("idEmpresa", ProfissionalBO.getProfissionalLogado().getIdEmpresa());
            parametros.put("excluido", Status.NAO);
            parametros.put("fornecedor", fornecedorItem.getFornecedor());
            parametros.put("item", fornecedorItem.getItem());
            return this.findByFields(parametros);
        } catch (Exception e) {
            this.log.error("Erro no listByEmpresa", e);
        }
        return null;
    }

    public List<FornecedorItem> listByFornecedor(Fornecedor fornecedor) {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("idEmpresa", ProfissionalBO.getProfissionalLogado().getIdEmpresa());
            parametros.put("excluido", Status.NAO);
            parametros.put("fornecedor", fornecedor);
            return this.listByFields(parametros);
        } catch (Exception e) {
            this.log.error("Erro no listByEmpresa", e);
        }
        return null;
    }
}
