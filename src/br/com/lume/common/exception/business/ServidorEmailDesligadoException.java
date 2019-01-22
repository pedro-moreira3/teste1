package br.com.lume.common.exception.business;

import br.com.lume.common.util.Mensagens;

public class ServidorEmailDesligadoException extends BusinessException {

    /**
     *
     */
    private static final long serialVersionUID = 4352119292380878533L;

    @Override
    public String getMessage() {
        return Mensagens.getMensagem("lumeSecurity.servidor.email.off");
    }
}
