package br.com.lume.security.converter;

import java.io.Serializable;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import org.apache.log4j.Logger;

import br.com.lume.security.PerfilSingleton;
import br.com.lume.security.entity.Perfil;

@FacesConverter(forClass = Perfil.class, value = "perfil")
public class PerfilConverter implements Converter, Serializable {

    private static final long serialVersionUID = 1L;
    private Logger log = Logger.getLogger(PerfilConverter.class);

    @Override
    public Object getAsObject(FacesContext facesContext, UIComponent uiComponent, String value) {

        try {
            if (value != null && !value.trim().isEmpty()) {
                final Long perIntCod = Long.parseLong(value);
                
                return PerfilSingleton.getInstance().getBo().find(perIntCod);
            }
        } catch (Exception e) {
            this.log.error("Erro no getAsObject", e);
        }
        return null;
    }

    @Override
    public String getAsString(FacesContext facesContext, UIComponent uiComponent, Object value) {
        try {
            if (value != null) {
                Perfil perfil = (Perfil) value;
                return String.valueOf(perfil.getPerIntCod());
            }
        } catch (Exception e) {
            this.log.error("Erro no getAsString", e);
        }
        return "";
    }
}
