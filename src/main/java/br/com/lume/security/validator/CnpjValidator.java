package br.com.lume.security.validator;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

import br.com.lume.common.util.Mensagens;

@FacesValidator(value = "cnpjValidator")
public class CnpjValidator implements Validator {

    @Override
    public void validate(FacesContext arg0, UIComponent arg1, Object valorTela) throws ValidatorException {
        String valorTelaStr = replaceCaracteres(String.valueOf(valorTela));
        if (valorTelaStr != null && valorTelaStr.length() == 14) {
            if (!validaCNPJ(String.valueOf(valorTelaStr))) {
                FacesMessage message = new FacesMessage();
                message.setSeverity(FacesMessage.SEVERITY_ERROR);
                message.setSummary(Mensagens.getMensagem("erro.validacao.cnpj"));

                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, Mensagens.getMensagem("erro.validacao.cnpj"), ""));
                throw new ValidatorException(message);
            }
        }
    }

    /**
     * Valida CNPJ do usu�rio.
     * 
     * @param cnpj
     *            String valor com 14 d�gitos
     */
    public static boolean validaCNPJ(String cnpj) {
        if (cnpj == null || cnpj.length() != 14 || GenericValidator.validarSequenciaIgual(cnpj, 14)) {
            return false;
        }
        try {
            Long.parseLong(cnpj);
        } catch (NumberFormatException e) { // CNPJ n�o possui somente n�meros
            return false;
        }
        int soma = 0;
        String cnpj_calc = cnpj.substring(0, 12);
        char chr_cnpj[] = cnpj.toCharArray();
        for (int i = 0; i < 4; i++) {
            if (chr_cnpj[i] - 48 >= 0 && chr_cnpj[i] - 48 <= 9) {
                soma += (chr_cnpj[i] - 48) * (6 - (i + 1));
            }
        }
        for (int i = 0; i < 8; i++) {
            if (chr_cnpj[i + 4] - 48 >= 0 && chr_cnpj[i + 4] - 48 <= 9) {
                soma += (chr_cnpj[i + 4] - 48) * (10 - (i + 1));
            }
        }
        int dig = 11 - soma % 11;
        cnpj_calc = (new StringBuilder(String.valueOf(cnpj_calc))).append(dig != 10 && dig != 11 ? Integer.toString(dig) : "0").toString();
        soma = 0;
        for (int i = 0; i < 5; i++) {
            if (chr_cnpj[i] - 48 >= 0 && chr_cnpj[i] - 48 <= 9) {
                soma += (chr_cnpj[i] - 48) * (7 - (i + 1));
            }
        }
        for (int i = 0; i < 8; i++) {
            if (chr_cnpj[i + 5] - 48 >= 0 && chr_cnpj[i + 5] - 48 <= 9) {
                soma += (chr_cnpj[i + 5] - 48) * (10 - (i + 1));
            }
        }
        dig = 11 - soma % 11;
        cnpj_calc = (new StringBuilder(String.valueOf(cnpj_calc))).append(dig != 10 && dig != 11 ? Integer.toString(dig) : "0").toString();
        if (!cnpj.equals(cnpj_calc)) {
            return false;
        }
        return true;
    }

    private static String replaceCaracteres(String value) {
        return value.replaceAll("\\.", "").replaceAll("-", "").replaceAll("\\/", "");
    }
}
