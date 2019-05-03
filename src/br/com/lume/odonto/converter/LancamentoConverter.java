package br.com.lume.odonto.converter;

import java.io.Serializable;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import org.apache.log4j.Logger;

import br.com.lume.lancamento.LancamentoSingleton;
import br.com.lume.odonto.entity.Lancamento;

@FacesConverter(forClass = Lancamento.class, value = "lancamento")
public class LancamentoConverter implements Converter, Serializable {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(LancamentoConverter.class);

    @Override
    public Object getAsObject(FacesContext facesContext, UIComponent uiComponent, String value) {
        try {
            if (value != null && !value.trim().isEmpty() && value.matches(".*\\d.*")) {
                final Long cod = Long.parseLong(value);
                return LancamentoSingleton.getInstance().getBo().find(cod);
            }
        } catch (Exception e) {
            this.log.error("Erro no getAsObject", e);
        }
        return null;
    }

    @Override
    public String getAsString(FacesContext facesContext, UIComponent uiComponent, Object value) {
        try {
            if (value == null) {
                return null;
            }
            Lancamento lancamento = (Lancamento) value;
            return String.valueOf(lancamento.getId());
        } catch (Exception e) {
            this.log.error("Erro no getAsString", e);
            return null;
        }
    }
}
