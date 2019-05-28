package br.com.lume.security.converter;

import java.io.Serializable;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import org.apache.log4j.Logger;

import br.com.lume.security.AcaoSingleton;
import br.com.lume.security.entity.Acao;

@FacesConverter(forClass = Acao.class, value = "acao")
public class AcaoConverter implements Converter, Serializable {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(AcaoConverter.class);

    @Override
    public Object getAsObject(FacesContext facesContext, UIComponent uiComponent, String value) {
        try {
            if (value != null && !value.trim().isEmpty()) {
                final Long acaIntCod = Long.parseLong(value);
               
                return AcaoSingleton.getInstance().getBo().find(acaIntCod);
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
                Acao acao = (Acao) value;
                return String.valueOf(acao.getAcaIntCod());
            }
        } catch (Exception e) {
            this.log.error("Erro no getAsString", e);
        }
        return "";
    }
}
