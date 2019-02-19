package br.com.lume.common.exception.business;

import br.com.lume.common.util.Mensagens;

public class BusinessException extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = 7460707325284981380L;

    @Override
    public String getMessage() {
        return Mensagens.getMensagem("erro.business");
    }

    public BusinessException() {
    }

    public BusinessException(
            Exception e) {
        super(e);
    }

}
