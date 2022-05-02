package br.com.lume.odonto.exception;

import br.com.lume.common.exception.business.BusinessException;
import br.com.lume.odonto.util.OdontoMensagens;

public class RemoverPlanoTratamComAgendException extends BusinessException {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    public static final String ERRO_PREVISTO = "CC1351686702435";

    @Override
    public String getMessage() {
        return OdontoMensagens.getMensagem("erro.business.remover.plano.tratam.com.agenda");
    }

    public RemoverPlanoTratamComAgendException() {
    }

    public RemoverPlanoTratamComAgendException(
            Exception e) {
        super(e);
    }

}
