package br.com.lume.common.exception.techinical;

import br.com.lume.common.util.Mensagens;

public class ErroAtualizarRegistro extends TechnicalException {

    /**
     *
     */
    private static final long serialVersionUID = 5515758118369978495L;

    public ErroAtualizarRegistro() {
    }

    public ErroAtualizarRegistro(
            Exception e) {
        super(e);
    }

    @Override
    public String getMessageUser() {
        return Mensagens.getMensagem("erro.technical.atualizar");
    }
}
