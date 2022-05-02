package br.com.lume.odonto.converter;

import java.io.Serializable;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import br.com.lume.convenio.ConvenioSingleton;
import br.com.lume.odonto.entity.Convenio;

@FacesConverter(forClass = Convenio.class, value = "convenio")
public class ConvenioConverter implements Converter, Serializable {

    private static final long serialVersionUID = 1L;

    @Override
    public Object getAsObject(FacesContext facesContext, UIComponent uiComponent, String value) {

        try {
            if (value != null && !value.trim().isEmpty()) {
                final Long id = Long.parseLong(value);
                return ConvenioSingleton.getInstance().getBo().find(id);
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
                Convenio convenio = (Convenio) value;
                return String.valueOf(convenio.getId());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}
