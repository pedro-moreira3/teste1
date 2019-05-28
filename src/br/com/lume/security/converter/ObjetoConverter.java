package br.com.lume.security.converter;

import java.io.Serializable;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import org.apache.log4j.Logger;

import br.com.lume.security.ObjetoSingleton;
import br.com.lume.security.entity.Objeto;

@FacesConverter(forClass = Objeto.class, value = "objeto")
public class ObjetoConverter implements Converter, Serializable {

    private static final long serialVersionUID = 1L;
    private Logger log = Logger.getLogger(ObjetoConverter.class);

    @Override
    public Object getAsObject(FacesContext facesContext, UIComponent uiComponent, String value) {

        try {
            if (value != null && !value.trim().isEmpty()) {
                final Long objIntCod = Long.parseLong(value);
               
                return ObjetoSingleton.getInstance().getBo().find(objIntCod);
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
                Objeto objeto = (Objeto) value;
                return String.valueOf(objeto.getObjIntCod());
            }
        } catch (Exception e) {
            this.log.error("Erro no getAsString", e);
        }
        return "";
    }
}
