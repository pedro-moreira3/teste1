package br.com.lume.common.exception.business;

import br.com.lume.common.util.Mensagens;

public class UsuarioTrialExpiradoException extends BusinessException {

    /**
     *
     */
    private static final long serialVersionUID = -632202971583528145L;
    private String msg = Mensagens.TRIAL_EXPIRADO;

    public UsuarioTrialExpiradoException() {
    }

    public UsuarioTrialExpiradoException(
            String msg) {
        this.msg = msg;
    }

    @Override
    public String getMessage() {
        return Mensagens.getMensagem(this.msg);
    }
}
