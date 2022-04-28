package br.com.lume.odonto.exception;

import br.com.lume.common.exception.techinical.TechnicalException;

public class BancoDadosException extends TechnicalException {

    /**
     * 
     */
    private static final long serialVersionUID = 2198858386418201969L;

    public BancoDadosException(Exception e) {
        super(e);
    }
}
