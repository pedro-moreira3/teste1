package br.com.lume.common.exception.business;

import br.com.lume.common.util.Mensagens;

public class UsuarioBloqueadoException extends BusinessException {

    /**
     *
     */
    private static final long serialVersionUID = 722677658299409813L;

    @Override
    public String getMessage() {
        return Mensagens.getMensagem(Mensagens.USUARIO_BLOQUEADO);
    }
}
