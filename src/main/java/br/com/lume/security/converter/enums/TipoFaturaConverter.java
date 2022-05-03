package br.com.lume.security.converter.enums;

import javax.faces.convert.FacesConverter;

import br.com.lume.common.bo.EnumController;
import br.com.lume.odonto.entity.Fatura.TipoFatura;

@FacesConverter(forClass = TipoFatura.class, value = "enumTipoFatura")
public class TipoFaturaConverter extends EnumController<TipoFatura> {

    private static final long serialVersionUID = 1L;

    public TipoFaturaConverter() {
        super();
        super.setClazz(TipoFatura.class);
    }

}
