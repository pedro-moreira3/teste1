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
import br.com.lume.odonto.entity.Origem;
import br.com.lume.odonto.exception.CpfCnpjDuplicadoException;

public class OrigemBO extends BO<Origem> {

    private Logger log = Logger.getLogger(OrigemBO.class);

    private static final long serialVersionUID = 1L;

    public OrigemBO() {
        super(PersistenceUnitName.ODONTO);
        this.setClazz(Origem.class);
    }

    @Override
    public List<Origem> listAll() throws Exception {
        return this.listByEmpresa();
    }

    @Override
    public boolean remove(Origem marca) throws BusinessException, TechnicalException {
        marca.setExcluido(Status.SIM);
        marca.setDataExclusao(Calendar.getInstance().getTime());
        marca.setExcluidoPorProfissional(ProfissionalBO.getProfissionalLogado().getId());
        this.persist(marca);
        return true;
    }

    public List<Origem> listByEmpresa() throws Exception {
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

    public Origem findByDocumentoandEmpresa(Origem origem) throws Exception {
        Map<String, Object> parametros = new HashMap<>();
        parametros.put("idEmpresa", origem.getIdEmpresa());
        parametros.put("dadosBasico.documento", origem.getDadosBasico().getDocumento());
        parametros.put("excluido", Status.NAO);
        return this.findByFields(parametros);
    }

    public void validaDuplicado(Origem origem) throws Exception {
        Origem findByCPFandEmpresa = null;
        if (!origem.getDadosBasico().getDocumento().equals("")) {
            findByCPFandEmpresa = new OrigemBO().findByDocumentoandEmpresa(origem);
        }
        if (findByCPFandEmpresa != null) {
            if (!findByCPFandEmpresa.equals(origem)) {
                throw new CpfCnpjDuplicadoException();
            }
        }
    }
}
