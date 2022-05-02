package br.com.lume.odonto.converter;

import java.io.Serializable;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import br.com.lume.odonto.entity.RepasseProfissional;
import br.com.lume.repasseProfissional.RepasseProfissionalSingleton;

@FacesConverter(forClass = RepasseProfissional.class, value = "repasseProfissional")
public class RepasseProfissionalConverter implements Converter, Serializable {

    private static final long serialVersionUID = 1L;

    @Override
    public Object getAsObject(FacesContext facesContext, UIComponent uiComponent, String value) {

        try {
            if (value != null && !value.trim().isEmpty()) {
                final Long id = Long.parseLong(value);
                return RepasseProfissionalSingleton.getInstance().getBo().find(id);
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
                RepasseProfissional RepasseProfissional = (RepasseProfissional) value;
                return String.valueOf(RepasseProfissional.getId());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}
