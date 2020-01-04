package br.com.lume.odonto.converter;

import java.io.Console;
import java.io.Serializable;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import br.com.lume.agendamento.AgendamentoSingleton;
import br.com.lume.agendamentoPlanoTratamentoProcedimento.AgendamentoPlanoTratamentoProcedimentoSingleton;
import br.com.lume.odonto.entity.Agendamento;
import br.com.lume.odonto.entity.AgendamentoPlanoTratamentoProcedimento;
import br.com.lume.odonto.entity.PlanoTratamentoProcedimento;
import br.com.lume.planoTratamentoProcedimento.PlanoTratamentoProcedimentoSingleton;

@FacesConverter(forClass = AgendamentoPlanoTratamentoProcedimento.class, value = "agendamentoPlanoDash")
public class AgendamentoPlanoTratamentoProcedimentoConverterDash implements Converter, Serializable {

    private static final long serialVersionUID = 1L;

    @Override
    public Object getAsObject(FacesContext facesContext, UIComponent uiComponent, String value) {
        try {
            if (value != null && !value.trim().isEmpty()) {
                String[] split = value.split("\\|");
                if (split[0] != null && !"0".equals(split[0])) {
                    final Long id = Long.parseLong(split[0]);
                    return AgendamentoPlanoTratamentoProcedimentoSingleton.getInstance().getBo().find(id);
                } else {
                    Agendamento ag = null;
                    PlanoTratamentoProcedimento ptp = null;
                    if (split[1] != null && !"0".equals(split[1])) {
                        ag = AgendamentoSingleton.getInstance().getBo().find(Long.parseLong(split[1]));
                    }
                    if (split[2] != null && !"0".equals(split[2])) {
                        ptp = PlanoTratamentoProcedimentoSingleton.getInstance().getBo().find(Long.parseLong(split[2]));
                    }
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
                String toString = "";
                toString += String.valueOf(a.getId()) + "|";
                toString += String.valueOf(a.getAgendamento() == null ? 0l : a.getAgendamento().getId()) + "|";
                toString += String.valueOf(a.getPlanoTratamentoProcedimento() == null ? 0l : a.getPlanoTratamentoProcedimento().getId());
                return toString;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}
