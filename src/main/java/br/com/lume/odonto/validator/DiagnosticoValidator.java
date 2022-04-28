package br.com.lume.odonto.validator;

import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

@FacesValidator("diagnosticoValidator")
public class DiagnosticoValidator implements Validator {

    @Override
    public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {
        List<String> selectedItemscheckbox = (List<String>) value;

        if (selectedItemscheckbox.size() > 4) {
            throw new ValidatorException(new FacesMessage("Máximo 4 diagnósticos."));
        }
    }

}
