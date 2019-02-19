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
import br.com.lume.odonto.entity.HorasUteisProfissional;
import br.com.lume.odonto.entity.Profissional;

public class HorasUteisProfissionalBO extends BO<HorasUteisProfissional> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(HorasUteisProfissionalBO.class);

    public HorasUteisProfissionalBO() {
        super(PersistenceUnitName.ODONTO);
        this.setClazz(HorasUteisProfissional.class);
    }

    @Override
    public boolean remove(HorasUteisProfissional horasUteisProfissional) throws BusinessException, TechnicalException {
        horasUteisProfissional.setExcluido(Status.SIM);
        horasUteisProfissional.setDataExclusao(Calendar.getInstance().getTime());
        horasUteisProfissional.setExcluidoPorProfissional(ProfissionalBO.getProfissionalLogado().getId());
        this.persist(horasUteisProfissional);
        return true;
    }

    public List<HorasUteisProfissional> listByProfissional(Profissional profissional) throws Exception {
        Map<String, Object> parametros = new HashMap<>();
        parametros.put("profissional.id", profissional.getId());
        parametros.put("excluido", Status.NAO);
        return this.listByFields(parametros, new String[] { "diaDaSemana asc" });
    }

    public List<HorasUteisProfissional> listByProfissionalAndDiaDaSemana(Profissional profissional, Calendar calendar) throws Exception {
        Map<String, Object> parametros = new HashMap<>();
        int data = calendar.get(Calendar.DAY_OF_WEEK);
        String diaDaSemana = this.getDiaDaSemana(data);
        parametros.put("diaDaSemana", diaDaSemana);
        parametros.put("profissional.id", profissional.getId());
        parametros.put("excluido", Status.NAO);
        return this.listByFields(parametros);
    }

    public String getDiaDaSemana(int data) {
        return data == 1 ? DOMINGO : data == 2 ? SEGUNDA : data == 3 ? TERCA : data == 4 ? QUARTA : data == 5 ? QUINTA : data == 6 ? SEXTA : SABADO;
    }

    public int getDiaDaSemana(String diaDaSemana) {
        return diaDaSemana.equals(
                DOMINGO) ? 1 : diaDaSemana.equals(SEGUNDA) ? 2 : diaDaSemana.equals(TERCA) ? 3 : diaDaSemana.equals(QUARTA) ? 4 : diaDaSemana.equals(QUINTA) ? 5 : diaDaSemana.equals(SEXTA) ? 6 : 7;
    }

    public static final String DOMINGO = "DOM", SEGUNDA = "SEG", TERCA = "TER", QUARTA = "QUA", QUINTA = "QUI", SEXTA = "SEX", SABADO = "SAB";
}
