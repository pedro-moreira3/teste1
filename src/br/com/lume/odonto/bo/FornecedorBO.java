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
import br.com.lume.odonto.exception.CpfCnpjDuplicadoException;

public class FornecedorBO extends BO<Fornecedor> {

    /**
     *
     */
    private static final long serialVersionUID = 8141120896186772178L;
    private Logger log = Logger.getLogger(FornecedorBO.class);

    public FornecedorBO() {
        super(PersistenceUnitName.ODONTO);
        this.setClazz(Fornecedor.class);
    }

    @Override
    public boolean remove(Fornecedor fornecedor) throws BusinessException, TechnicalException {
        fornecedor.setExcluido(Status.SIM);
        fornecedor.setDataExclusao(Calendar.getInstance().getTime());
        fornecedor.setExcluidoPorProfissional(ProfissionalBO.getProfissionalLogado().getId());
        this.persist(fornecedor);
        return true;
    }

    @Override
    public List<Fornecedor> listAll() throws Exception {
        return this.listByEmpresa();
    }

    public List<Fornecedor> listByEmpresa() throws Exception {
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

    public Fornecedor findByDocumentoandEmpresa(Fornecedor fornecedor) throws Exception {
        Map<String, Object> parametros = new HashMap<>();
        parametros.put("idEmpresa", fornecedor.getIdEmpresa());
        parametros.put("dadosBasico.documento", fornecedor.getDadosBasico().getDocumento());
        parametros.put("excluido", Status.NAO);
        return this.findByFields(parametros);
    }

    public void validaDuplicado(Fornecedor fornecedor) throws Exception {
        Fornecedor findByCNPJandEmpresa = null;
        if (!fornecedor.getDadosBasico().getDocumento().equals("")) {
            findByCNPJandEmpresa = new FornecedorBO().findByDocumentoandEmpresa(fornecedor);
        }
        if (findByCNPJandEmpresa != null) {
            if (!findByCNPJandEmpresa.equals(fornecedor)) {
                throw new CpfCnpjDuplicadoException();
            }
        }
    }
}
