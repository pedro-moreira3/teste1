package br.com.lume.security.converter;

import java.io.Serializable;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import org.apache.log4j.Logger;

import br.com.lume.security.UsuarioSingleton;
import br.com.lume.security.entity.Usuario;

@FacesConverter(forClass = Usuario.class, value = "usuario")
public class UsuarioConverter implements Converter, Serializable {

    private static final long serialVersionUID = 1L;
    private Logger log = Logger.getLogger(UsuarioConverter.class);

    @Override
    public Object getAsObject(FacesContext facesContext, UIComponent uiComponent, String value) {

        try {
            if (value != null && !value.trim().isEmpty()) {
                final Long usuIntCod = Long.parseLong(value);
               
                return UsuarioSingleton.getInstance().getBo().find(usuIntCod);
            }
        } catch (Exception e) {
            this.log.error("Erro no getAsObject", e);
        }
        return null;
    }

    @Override
    public String getAsString(FacesContext facesContext, UIComponent uiComponent, Object value) {
        try {
            if (value == null) {
                return null;
            }
            Usuario usuario = (Usuario) value;
            return String.valueOf(usuario.getUsuIntCod());
        } catch (Exception e) {
            this.log.error("Erro no getAsString", e);
            return null;
        }
    }
}
