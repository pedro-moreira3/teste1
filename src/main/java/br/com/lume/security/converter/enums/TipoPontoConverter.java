package br.com.lume.security.converter.enums;

import javax.faces.convert.FacesConverter;

import br.com.lume.common.bo.EnumController;
import br.com.lume.odonto.entity.Fatura.TipoFatura;
import br.com.lume.odonto.entity.ProfissionalDiaria.TipoPonto;

@FacesConverter(forClass = TipoPonto.class, value = "enumTipoPonto")
public class TipoPontoConverter extends EnumController<TipoPonto> {

    private static final long serialVersionUID = 1L;

    public TipoPontoConverter() {
        super();
        super.setClazz(TipoPonto.class);
    }

}
