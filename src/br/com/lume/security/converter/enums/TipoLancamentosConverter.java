package br.com.lume.security.converter.enums;

import javax.faces.convert.FacesConverter;

import br.com.lume.common.bo.EnumController;
import br.com.lume.odonto.entity.Fatura.TipoLancamentos;

@FacesConverter(forClass = TipoLancamentos.class, value = "enumTipoLancamentos")
public class TipoLancamentosConverter extends EnumController<TipoLancamentos> {

    private static final long serialVersionUID = 1L;

    public TipoLancamentosConverter() {
        super();
        super.setClazz(TipoLancamentos.class);
    }

}
