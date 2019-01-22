package br.com.lume.common.exception.techinical;

import br.com.lume.common.util.Mensagens;

public class ErroListarRegistros extends TechnicalException {

    /**
     *
     */
    private static final long serialVersionUID = 3873417870968489568L;

    public ErroListarRegistros() {
    }

    public ErroListarRegistros(
            Exception e) {
        super(e);
    }

    @Override
    public String getMessageUser() {
        return Mensagens.getMensagem("erro.technical.listar");
    }
}
