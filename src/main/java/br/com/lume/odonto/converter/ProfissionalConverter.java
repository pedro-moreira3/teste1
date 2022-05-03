package br.com.lume.odonto.converter;

import java.io.Serializable;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import br.com.lume.odonto.entity.Profissional;
import br.com.lume.profissional.ProfissionalSingleton;

@FacesConverter(forClass = Profissional.class, value = "profissional")
public class ProfissionalConverter implements Converter, Serializable {

    private static final long serialVersionUID = 1L;

    @Override
    public Object getAsObject(FacesContext facesContext, UIComponent uiComponent, String value) {

        try {
            if (value != null && !value.trim().isEmpty()) {
                final Long id = Long.parseLong(value);
                return ProfissionalSingleton.getInstance().getBo().find(id);
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
                Profissional profissional = (Profissional) value;
                return String.valueOf(profissional.getId());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}
