package br.com.lume.security.validator;

import java.util.Date;

public class GenericValidator {

    public static boolean validarSequenciaIgual(String campo, int qtdCaracter) {
        for (int i = 0; i <= 9; i++) {
            String compare = "";
            for (int j = 0; j < qtdCaracter; j++) {
                compare += i;
            }
            if (compare.equals(campo)) {
                return true;
            }
        }
        return false;
    }

    public static boolean validarRangeData(Date dataIni, Date dataFim, boolean required) {
        if (dataIni != null && dataFim != null) {
            return dataFim.getTime() >= dataIni.getTime();
        }
        return !required;
    }

}
