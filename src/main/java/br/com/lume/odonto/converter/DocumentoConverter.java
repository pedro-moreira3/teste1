package br.com.lume.odonto.converter;

import java.io.Serializable;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import br.com.lume.documento.DocumentoSingleton;
import br.com.lume.odonto.entity.Convenio;
import br.com.lume.odonto.entity.Documento;

@FacesConverter(forClass = Convenio.class, value = "documento")
public class DocumentoConverter implements Converter, Serializable {

    private static final long serialVersionUID = 1L;

    @Override
    public Object getAsObject(FacesContext facesContext, UIComponent uiComponent, String value) {

        try {
            if (value != null && !value.trim().isEmpty()) {
                final Long id = Long.parseLong(value);
                return DocumentoSingleton.getInstance().getBo().find(id);
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
                Documento documento = (Documento) value;
                return String.valueOf(documento.getId());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}
