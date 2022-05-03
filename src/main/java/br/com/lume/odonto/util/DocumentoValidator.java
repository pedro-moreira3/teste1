package br.com.lume.odonto.util;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

import br.com.lume.security.validator.CnpjValidator;
import br.com.lume.security.validator.CpfValidator;

@FacesValidator(value = "documentoValidator")
public class DocumentoValidator implements Validator {

    @Override
    public void validate(FacesContext arg0, UIComponent arg1, Object valorTela) throws ValidatorException {
        if (String.valueOf(valorTela).length() == 14) {
            new CpfValidator().validate(arg0, arg1, valorTela);
        } else if (String.valueOf(valorTela).length() == 18) {
            new CnpjValidator().validate(arg0, arg1, valorTela);
        } else if (String.valueOf(valorTela).length() < 14 && String.valueOf(valorTela).length() > 1) {
            FacesMessage message = new FacesMessage();
            message.setSeverity(FacesMessage.SEVERITY_ERROR);
            message.setSummary(OdontoMensagens.getMensagem("erro.validacao.documento"));
            throw new ValidatorException(message);
        }
    }
}
