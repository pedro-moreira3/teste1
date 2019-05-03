package br.com.lume.odonto.converter;

import java.io.Serializable;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import br.com.lume.convenioProcedimento.ConvenioProcedimentoSingleton;
import br.com.lume.odonto.entity.Convenio;
import br.com.lume.odonto.entity.ConvenioProcedimento;

@FacesConverter(forClass = Convenio.class, value = "convenioProcedimento")
public class ConvenioProcedimentoConverter implements Converter, Serializable {

    private static final long serialVersionUID = 1L;

    @Override
    public Object getAsObject(FacesContext facesContext, UIComponent uiComponent, String value) {

        try {
            if (value != null && !value.trim().isEmpty()) {
                final Long id = Long.parseLong(value);
                return ConvenioProcedimentoSingleton.getInstance().getBo().find(id);
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
                ConvenioProcedimento convenio = (ConvenioProcedimento) value;
                return String.valueOf(convenio.getId());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}
