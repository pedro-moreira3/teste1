package br.com.lume.odonto.converter;

import java.io.Serializable;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import br.com.lume.afiliacao.AfiliacaoSingleton;
import br.com.lume.odonto.entity.Afiliacao;

@FacesConverter(forClass = Afiliacao.class, value = "afiliacao")
public class AfiliacaoConverter implements Converter, Serializable {

    private static final long serialVersionUID = 1L;

    @Override
    public Object getAsObject(FacesContext facesContext, UIComponent uiComponent, String value) {

        try {
            if (value != null && !value.trim().isEmpty() && value.matches("\\d+")) {
                final Long id = Long.parseLong(value);
                return AfiliacaoSingleton.getInstance().getBo().find(id);
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
                Afiliacao afiliacao = (Afiliacao) value;
                return String.valueOf(afiliacao.getId());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}
