package br.com.lume.odonto.converter;

import java.io.Serializable;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import br.com.lume.odonto.entity.Parceiro;
import br.com.lume.parceiro.ParceiroSingleton;

@FacesConverter(forClass = Parceiro.class, value = "parceiro")
public class ParceiroConverter implements Converter, Serializable {

    private static final long serialVersionUID = 1L;

    @Override
    public Object getAsObject(FacesContext facesContext, UIComponent uiComponent, String value) {
        try {
            if (value != null && !value.trim().isEmpty()) {
                final Long id = Long.parseLong(value);
                return ParceiroSingleton.getInstance().getBo().find(id);
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
                System.out.println("===============================" + value + "==========================================");
                Parceiro parceiro = (Parceiro) value;
                return String.valueOf(parceiro.getId());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}
