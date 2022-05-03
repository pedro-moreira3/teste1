package br.com.lume.odonto.converter;

import java.io.Serializable;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import br.com.lume.odonto.entity.MotivoInativacaoPaciente;
import br.com.lume.odonto.entity.Paciente;
import br.com.lume.paciente.MotivoInativacaoPacienteSingleton;
import br.com.lume.paciente.PacienteSingleton;

@FacesConverter(forClass = Paciente.class, value = "motivoInativacaoPaciente")
public class MotivoInativacaoPacienteConverter implements Converter, Serializable {

    private static final long serialVersionUID = 1L;

    @Override
    public Object getAsObject(FacesContext facesContext, UIComponent uiComponent, String value) {

        try {
            if (value != null && !value.trim().isEmpty() && value.matches("\\d+")) {
                final Long id = Long.parseLong(value);
                return MotivoInativacaoPacienteSingleton.getInstance().getBo().find(id);
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
                MotivoInativacaoPaciente motivo = (MotivoInativacaoPaciente) value;
                return String.valueOf(motivo.getId());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}
