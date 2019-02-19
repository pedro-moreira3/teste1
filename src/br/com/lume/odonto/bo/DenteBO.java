package br.com.lume.odonto.bo;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import br.com.lume.common.exception.business.BusinessException;
import br.com.lume.common.exception.techinical.TechnicalException;
import br.com.lume.common.util.Status;
import br.com.lume.odonto.dao.PersistenceUnitName;
import br.com.lume.odonto.entity.Dente;
import br.com.lume.odonto.entity.Odontograma;

public class DenteBO extends BO<Dente> {

    /**
     *
     */
    private static final long serialVersionUID = 1048596378859010053L;
    private Logger log = Logger.getLogger(Dente.class);

    public DenteBO() {
        super(PersistenceUnitName.ODONTO);
        this.setClazz(Dente.class);
    }

    @Override
    public boolean remove(Dente dente) throws BusinessException, TechnicalException {
        dente.setExcluido(Status.SIM);
        dente.setDataExclusao(Calendar.getInstance().getTime());
        dente.setExcluidoPorProfissional(ProfissionalBO.getProfissionalLogado().getId());
        this.persist(dente);
        return true;
    }

    public Dente findByDescAndOdontograma(String descricao, Odontograma odontograma) throws Exception {
        if (descricao != null && odontograma != null) {
            Map<String, Object> filtros = new HashMap<>();
            filtros.put("descricao", descricao);
            filtros.put("odontograma.id", odontograma.getId());
            filtros.put("excluido", Status.NAO);
            return this.findByFields(filtros);
        }
        return null;
    }
}
