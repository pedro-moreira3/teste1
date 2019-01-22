package br.com.lume.odonto.bo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import br.com.lume.common.util.Status;
import br.com.lume.odonto.dao.PersistenceUnitName;
import br.com.lume.odonto.entity.Dente;
import br.com.lume.odonto.entity.Odontograma;
import br.com.lume.odonto.entity.RegiaoDente;

public class RegiaoDenteBO extends BO<RegiaoDente> {

    /**
     *
     */
    private static final long serialVersionUID = -3292973331151678062L;
    private Logger log = Logger.getLogger(RegiaoDente.class);

    public RegiaoDenteBO() {
        super(PersistenceUnitName.ODONTO);
        this.setClazz(RegiaoDente.class);
    }

    public void removeByOdontograma(Odontograma odontograma) {
        try {
            List<RegiaoDente> rds = listByOdontograma(odontograma);
            if (rds != null && !rds.isEmpty()) {
                for (RegiaoDente regiaoDente : rds) {
                    remove(regiaoDente);
                }
            }
        } catch (Exception e) {
            log.error("Erro no removeByOdontogramaAndDente", e);
        }
    }

    public List<RegiaoDente> listByOdontogramaAndDente(Odontograma odontograma, Dente dente) {
        List<RegiaoDente> regiaoDente = new ArrayList<>();
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("dente.odontograma.id", odontograma.getId());
            parametros.put("dente.id", dente.getId());
            parametros.put("excluido", Status.NAO);
            return this.listByFields(parametros);
        } catch (Exception e) {
            log.error("Erro no listByOdontograma", e);
        }
        return regiaoDente;
    }

    public List<RegiaoDente> listByOdontograma(Odontograma ondontograma) {
        List<RegiaoDente> regiaoDente = new ArrayList<>();
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("dente.odontograma.id", ondontograma.getId());
            parametros.put("excluido", Status.NAO);
            return this.listByFields(parametros);
        } catch (Exception e) {
            log.error("Erro no listByOdontograma", e);
        }
        return regiaoDente;
    }

    public static String isIncisalOrOclusal(String dente) {
        if (dente != null && dente.length() == 2) {
            if (Integer.parseInt(dente.substring(1, 2)) <= 3) {
                return RegiaoDente.INCISAL;
            } else {
                return RegiaoDente.OCLUSAL;
            }
        }
        return "";
    }
}
