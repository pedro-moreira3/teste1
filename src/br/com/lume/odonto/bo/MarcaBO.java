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
import br.com.lume.odonto.entity.Marca;

public class MarcaBO extends BO<Marca> {

    private Logger log = Logger.getLogger(Marca.class);

    private static final long serialVersionUID = 1L;

    public MarcaBO() {
        super(PersistenceUnitName.ODONTO);
        this.setClazz(Marca.class);
    }

    @Override
    public List<Marca> listAll() throws Exception {
        return this.listByEmpresa();
    }

    @Override
    public boolean remove(Marca marca) throws BusinessException, TechnicalException {
        marca.setExcluido(Status.SIM);
        marca.setDataExclusao(Calendar.getInstance().getTime());
        marca.setExcluidoPorProfissional(ProfissionalBO.getProfissionalLogado().getId());
        this.persist(marca);
        return true;
    }

    public List<Marca> listByEmpresa() throws Exception {
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

    public Marca findByNomeAndEmpresa(String nome, Long idEmpresa) {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("nome", nome);
            parametros.put("idEmpresa", ProfissionalBO.getProfissionalLogado().getIdEmpresa());
            parametros.put("excluido", Status.NAO);
            return this.findByFields(parametros);
        } catch (Exception e) {
            this.log.error("Erro no listByEmpresa", e);
        }
        return null;
    }
}
