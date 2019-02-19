package br.com.lume.odonto.converter;

import java.io.Serializable;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import br.com.lume.odonto.bo.OdontogramaBO;
import br.com.lume.odonto.entity.Odontograma;

@FacesConverter(forClass = Odontograma.class, value = "odontograma")
public class OdontogramaConverter implements Converter, Serializable {

    private static final long serialVersionUID = 1L;

    @Override
    public Object getAsObject(FacesContext facesContext, UIComponent uiComponent, String value) {

        try {
            if (value != null && !value.trim().isEmpty()) {
                final Long id = Long.parseLong(value);
                return new OdontogramaBO().find(id);
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
                Odontograma odontograma = (Odontograma) value;
                return String.valueOf(odontograma.getId());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}
