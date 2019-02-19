package br.com.lume.odonto.util;

import java.util.ResourceBundle;

public class OdontoMensagens {

    public static String getMensagem(String key) {
        return getMensagemOffLine(key);
    }

    public static String getMensagemOffLine(String key) {
        ResourceBundle rb = ResourceBundle.getBundle("br.com.lume.odonto.resources.odonto");
        return rb.getString(key);
    }
}
