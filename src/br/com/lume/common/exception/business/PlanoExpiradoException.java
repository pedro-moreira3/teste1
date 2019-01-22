package br.com.lume.common.exception.business;

import br.com.lume.common.util.Mensagens;

public class PlanoExpiradoException extends BusinessException {

    /**
     *
     */
    private static final long serialVersionUID = 8003158744046926990L;

    @Override
    public String getMessage() {
        return Mensagens.getMensagem(Mensagens.PLANO_EXPIRADO);
    }
}
