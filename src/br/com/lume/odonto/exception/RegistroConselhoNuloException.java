package br.com.lume.odonto.exception;

import br.com.lume.common.exception.business.BusinessException;
import br.com.lume.odonto.util.OdontoMensagens;

public class RegistroConselhoNuloException extends BusinessException {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @Override
    public String getMessage() {
        return OdontoMensagens.getMensagem("erro.registro.conselho.obrigatorio.perfil");
    }
}
