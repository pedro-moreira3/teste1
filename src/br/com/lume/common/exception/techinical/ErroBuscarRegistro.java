package br.com.lume.common.exception.techinical;

import br.com.lume.common.util.Mensagens;

public class ErroBuscarRegistro extends TechnicalException {

    /**
     *
     */
    private static final long serialVersionUID = 6617285545749781371L;

    public ErroBuscarRegistro() {
    }

    public ErroBuscarRegistro(
            Exception e) {
        super(e);
    }

    @Override
    public String getMessageUser() {
        return Mensagens.getMensagem("erro.technical.buscar");
    }
}
