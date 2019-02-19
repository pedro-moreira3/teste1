package br.com.lume.odonto.exception;

import br.com.lume.odonto.util.OdontoMensagens;

public class SenhaCertificadoInvalidaException extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @Override
    public String getMessage() {
        return OdontoMensagens.getMensagem("certificado.senha.certificado.invalida");
    }
}
