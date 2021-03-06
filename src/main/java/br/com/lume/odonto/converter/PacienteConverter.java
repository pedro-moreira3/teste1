package br.com.lume.odonto.converter;

import java.io.Serializable;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import br.com.lume.odonto.entity.Paciente;
import br.com.lume.paciente.PacienteSingleton;

@FacesConverter(forClass = Paciente.class, value = "paciente")
public class PacienteConverter implements Converter, Serializable {

    private static final long serialVersionUID = 1L;

    @Override
    public Object getAsObject(FacesContext facesContext, UIComponent uiComponent, String value) {

        try {
            if (value != null && !value.trim().isEmpty() && value.matches("\\d+")) {
                final Long id = Long.parseLong(value);
                return PacienteSingleton.getInstance().getBo().find(id);
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
                Paciente paciente = (Paciente) value;
                return String.valueOf(paciente.getId());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}
