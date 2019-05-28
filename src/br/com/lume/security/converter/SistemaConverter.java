package br.com.lume.security.converter;

import java.io.Serializable;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import org.apache.log4j.Logger;

import br.com.lume.security.SistemaSingleton;
import br.com.lume.security.entity.Sistema;

@FacesConverter(forClass = Sistema.class, value = "sistema")
public class SistemaConverter implements Converter, Serializable {

    private static final long serialVersionUID = 1L;
    private Logger log = Logger.getLogger(SistemaConverter.class);

    @Override
    public Object getAsObject(FacesContext facesContext, UIComponent uiComponent, String value) {

        try {
            if (value != null && !value.trim().isEmpty()) {
                final Long sisIntCod = Long.parseLong(value);
                
                return SistemaSingleton.getInstance().getBo().find(sisIntCod);
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
                Sistema sistema = (Sistema) value;
                return String.valueOf(sistema.getSisIntCod());
            }
        } catch (Exception e) {
            this.log.error("Erro no getAsString", e);
        }
        return "";
    }
}
