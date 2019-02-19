package br.com.lume.common.exception.business;

import br.com.lume.common.util.Mensagens;

public class SenhaInvalidaException extends BusinessException {

    /**
     *
     */
    private static final long serialVersionUID = 8370043953710278129L;

    @Override
    public String getMessage() {
        return Mensagens.getMensagem(Mensagens.USUARIO_SENHA_INVALIDO);
    }
}
