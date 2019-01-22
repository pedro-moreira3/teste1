package br.com.lume.odonto.converter;

import java.io.Serializable;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import br.com.lume.odonto.bo.AgendamentoBO;
import br.com.lume.odonto.bo.AgendamentoPlanoTratamentoProcedimentoBO;
import br.com.lume.odonto.bo.PlanoTratamentoProcedimentoBO;
import br.com.lume.odonto.entity.Agendamento;
import br.com.lume.odonto.entity.AgendamentoPlanoTratamentoProcedimento;
import br.com.lume.odonto.entity.PlanoTratamentoProcedimento;

@FacesConverter(forClass = AgendamentoPlanoTratamentoProcedimento.class, value = "agendamentoPlanoDash")
public class AgendamentoPlanoTratamentoProcedimentoConverterDash implements Converter, Serializable {

    private static final long serialVersionUID = 1L;

    @Override
    public Object getAsObject(FacesContext facesContext, UIComponent uiComponent, String value) {
        try {
            if (value != null && !value.trim().isEmpty()) {
                String[] split = value.split("\\|");
                if (!split[0].equals("0")) {
                    final Long id = Long.parseLong(split[0]);
                    return new AgendamentoPlanoTratamentoProcedimentoBO().find(id);
                } else {
                    Agendamento ag = null;
                    if (!split[1].equals("0")) {
                        ag = new AgendamentoBO().find(Long.parseLong(split[1]));
                    }
                    PlanoTratamentoProcedimento ptp = new PlanoTratamentoProcedimentoBO().find(Long.parseLong(split[2]));
                    return new AgendamentoPlanoTratamentoProcedimento(ptp, ag);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String getAsString(FacesContext facesContext, UIComponent uiComponent, Object value) {
        try {
            if (value != null) {
                AgendamentoPlanoTratamentoProcedimento a = (AgendamentoPlanoTratamentoProcedimento) value;
                return String.valueOf(a.getId() + "|" + a.getAgendamento().getId() + "|" + a.getPlanoTratamentoProcedimento().getId());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}
