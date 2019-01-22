package br.com.lume.common.exception.business;

import br.com.lume.common.util.Mensagens;

public class UsuarioSemPerfilException extends BusinessException {

    /**
     *
     */
    private static final long serialVersionUID = -910839381498978079L;

    @Override
    public String getMessage() {
        return Mensagens.getMensagem(Mensagens.USUARIO_SEM_PERFIL);
    }
}
