package br.com.lume.security.converter;

import java.io.Serializable;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import org.apache.log4j.Logger;

import br.com.lume.security.bo.EmpresaBO;
import br.com.lume.security.entity.Empresa;

@FacesConverter(forClass = Empresa.class, value = "empresa")
public class EmpresaConverter implements Converter, Serializable {

    private static final long serialVersionUID = 1L;
    private Logger log = Logger.getLogger(EmpresaConverter.class);

    @Override
    public Object getAsObject(FacesContext facesContext, UIComponent uiComponent, String value) {

        try {
            if (value != null && !value.trim().isEmpty()) {
                final Long empIntCod = Long.parseLong(value);
                EmpresaBO empresaBO = new EmpresaBO();
                return empresaBO.find(empIntCod);
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
                Empresa empresa = (Empresa) value;
                return String.valueOf(empresa.getEmpIntCod());
            }
        } catch (Exception e) {
            this.log.error("Erro no getAsString", e);
        }
        return "";
    }
}
