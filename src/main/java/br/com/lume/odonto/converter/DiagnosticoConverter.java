package br.com.lume.odonto.converter;

import java.io.Serializable;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import br.com.lume.diagnosticoOrtodontico.DiagnosticoOrtodonticoSingleton;
import br.com.lume.odonto.entity.DiagnosticoOrtodontico;
import br.com.lume.odonto.entity.Paciente;

@FacesConverter(forClass = Paciente.class, value = "diagnostico")
public class DiagnosticoConverter implements Converter, Serializable {

    private static final long serialVersionUID = 1L;

    @Override
    public Object getAsObject(FacesContext facesContext, UIComponent uiComponent, String value) {

        try {
            if (value != null && !value.trim().isEmpty()) {
                final Long id = Long.parseLong(value);
                return DiagnosticoOrtodonticoSingleton.getInstance().getBo().find(id);
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
                DiagnosticoOrtodontico diagnosticoOrtodontico = (DiagnosticoOrtodontico) value;
                return String.valueOf(diagnosticoOrtodontico.getId());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}
