package br.com.lume.common.exception.techinical;

import br.com.lume.common.util.Mensagens;

public class TechnicalException extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = -848939263616366750L;
    private String messageUser = Mensagens.getMensagem("erro.technical");

    public TechnicalException() {
    }

    public TechnicalException(
            Exception e) {
        super(e);
    }

    public String getMessageUser() {
        return this.messageUser;
    }
}
