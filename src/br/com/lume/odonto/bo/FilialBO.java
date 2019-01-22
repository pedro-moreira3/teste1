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
import br.com.lume.odonto.entity.Filial;

public class FilialBO extends BO<Filial> {

    private Logger log = Logger.getLogger(Filial.class);

    private static final long serialVersionUID = 1L;

    public FilialBO() {
        super(PersistenceUnitName.ODONTO);
        this.setClazz(Filial.class);
    }

    @Override
    public List<Filial> listAll() throws Exception {
        return this.listByEmpresa();
    }

    @Override
    public boolean remove(Filial filial) throws BusinessException, TechnicalException {
        filial.setExcluido(Status.SIM);
        filial.setDataExclusao(Calendar.getInstance().getTime());
        filial.setExcluidoPorProfissional(ProfissionalBO.getProfissionalLogado().getId());
        this.persist(filial);
        return true;
    }

    public List<Filial> listByEmpresa() throws Exception {
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

    public Filial findByNomeAndDocumento(String nome, Long idEmpresa) {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("dadosBasico.nome", nome);
            parametros.put("idEmpresa", ProfissionalBO.getProfissionalLogado().getIdEmpresa());
            parametros.put("excluido", Status.NAO);
            return this.findByFields(parametros);
        } catch (Exception e) {
            this.log.error("Erro no listByNomeAndEmpresa", e);
        }
        return null;
    }

    public Filial findByNomeAndEmpresa(String nome, Long idEmpresa) {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("dadosBasico.nome", nome);
            parametros.put("idEmpresa", ProfissionalBO.getProfissionalLogado().getIdEmpresa());
            parametros.put("excluido", Status.NAO);
            return this.findByFields(parametros);
        } catch (Exception e) {
            this.log.error("Erro no listByNomeAndEmpresa", e);
        }
        return null;
    }
}
