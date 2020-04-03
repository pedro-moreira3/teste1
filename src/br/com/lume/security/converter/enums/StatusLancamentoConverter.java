package br.com.lume.security.converter.enums;

import javax.faces.convert.FacesConverter;

import br.com.lume.common.bo.EnumController;
import br.com.lume.odonto.entity.Lancamento.StatusLancamento;

@FacesConverter(forClass = StatusLancamento.class, value = "enumStatusLancamento")
public class StatusLancamentoConverter extends EnumController<StatusLancamento> {

    private static final long serialVersionUID = 1L;

    public StatusLancamentoConverter() {
        super();
        super.setClazz(StatusLancamento.class);
    }

}
