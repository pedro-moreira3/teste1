package br.com.lume.odonto.converter;

import java.io.Serializable;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import br.com.lume.odonto.bo.TipoCategoriaBO;
import br.com.lume.odonto.entity.TipoCategoria;

@FacesConverter(forClass = TipoCategoria.class, value = "tipocategoria")
public class TipoCategoriaConverter implements Converter, Serializable {

    private static final long serialVersionUID = 1L;

    @Override
    public Object getAsObject(FacesContext facesContext, UIComponent uiComponent, String value) {
        try {
            if (value != null && !value.trim().isEmpty()) {
                final Long id = Long.parseLong(value);
                return new TipoCategoriaBO().find(id);
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
                TipoCategoria m = (TipoCategoria) value;
                return String.valueOf(m.getId());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}
