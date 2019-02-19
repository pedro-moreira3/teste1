package br.com.lume.common.exception.business;

import br.com.lume.common.util.Mensagens;

public class UsuarioSemPagamentoException extends BusinessException {

    /**
     *
     */
    private static final long serialVersionUID = -224130873665571495L;

    @Override
    public String getMessage() {
        return Mensagens.getMensagem(Mensagens.USUARIO_SEM_PAGAMENTO);
    }
}
