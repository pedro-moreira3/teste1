package br.com.lume.common.exception.techinical;

import br.com.lume.common.util.Mensagens;

public class ErroDeletarRegistro extends TechnicalException {

    /**
     *
     */
    private static final long serialVersionUID = 2870631709808460119L;

    public ErroDeletarRegistro() {
    }

    public ErroDeletarRegistro(
            Exception e) {
        super(e);
    }

    @Override
    public String getMessageUser() {
        return Mensagens.getMensagem("erro.technical.deletar");
    }
}
