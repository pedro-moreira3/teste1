package br.com.lume.odonto.converter;

import java.io.Serializable;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import br.com.lume.cid.CidSingleton;
import br.com.lume.odonto.entity.CID;

@FacesConverter(forClass = CID.class, value = "cid")
public class CIDConverter implements Converter, Serializable {

    private static final long serialVersionUID = 1L;

    @Override
    public Object getAsObject(FacesContext facesContext, UIComponent uiComponent, String value) {
        try {
            if (value != null && !value.trim().isEmpty()) {
                CID cid = CidSingleton.getInstance().getBo().find(value);
                return cid;
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
                CID cid = (CID) value;
                return cid.getId();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}
