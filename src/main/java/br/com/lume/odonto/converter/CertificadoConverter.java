package br.com.lume.odonto.converter;

import java.io.Serializable;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import br.com.lume.certificado.CertificadoSingleton;
import br.com.lume.odonto.entity.Certificado;

@FacesConverter(forClass = Certificado.class, value = "certificado")
public class CertificadoConverter implements Converter, Serializable {

    private static final long serialVersionUID = 1L;

    @Override
    public Object getAsObject(FacesContext facesContext, UIComponent uiComponent, String value) {
        try {
            if (value != null && !value.trim().isEmpty()) {
                final Long id = Long.parseLong(value);
                return CertificadoSingleton.getInstance().getBo().find(id);
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
                Certificado certificado = (Certificado) value;
                return String.valueOf(certificado.getId());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}
