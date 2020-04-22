package br.com.lume.security.converter.enums;

import javax.faces.convert.FacesConverter;

import br.com.lume.common.bo.EnumController;
import br.com.lume.odonto.entity.Lancamento.SubStatusLancamento;

@FacesConverter(forClass = SubStatusLancamento.class, value = "enumSubStatusLancamento")
public class SubStatusLancamentoConverter extends EnumController<SubStatusLancamento> {

    private static final long serialVersionUID = 1L;

    public SubStatusLancamentoConverter() {
        super();
        super.setClazz(SubStatusLancamento.class);
    }

}
