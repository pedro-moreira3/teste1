package br.com.lume.common.exception.techinical;

import br.com.lume.common.util.Mensagens;

public class ErroInserirRegistro extends TechnicalException {

    /**
     *
     */
    private static final long serialVersionUID = -3677173415397211350L;

    public ErroInserirRegistro() {
    }

    public ErroInserirRegistro(
            Exception e) {
        super(e);
    }

    @Override
    public String getMessageUser() {
        return Mensagens.getMensagem("erro.technical.inserir");
    }
}
