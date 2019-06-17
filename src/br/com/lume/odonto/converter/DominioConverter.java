package br.com.lume.odonto.converter;

import java.io.Serializable;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import br.com.lume.documento.DocumentoSingleton;
import br.com.lume.dominio.DominioSingleton;
import br.com.lume.odonto.entity.Dominio;

@FacesConverter(forClass = Dominio.class, value = "dominio")
public class DominioConverter implements Converter, Serializable {

    private static final long serialVersionUID = 1L;

    @Override
    public Object getAsObject(FacesContext facesContext, UIComponent uiComponent, String value) {
        try {
            if (value != null && !value.trim().isEmpty()) {
                final Long id = Long.parseLong(value);
                return DominioSingleton.getInstance().getBo().find(id);
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
                Dominio dominio = (Dominio) value;
                return String.valueOf(dominio.getId());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}
