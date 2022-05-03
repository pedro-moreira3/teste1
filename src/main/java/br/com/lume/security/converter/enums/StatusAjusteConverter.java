package br.com.lume.security.converter.enums;

import javax.faces.convert.FacesConverter;

import br.com.lume.common.bo.EnumController;
import br.com.lume.odonto.entity.AjusteContas.StatusAjuste;

@FacesConverter(forClass = StatusAjuste.class, value = "enumStatusAjuste")
public class StatusAjusteConverter extends EnumController<StatusAjuste> {

    private static final long serialVersionUID = 1L;

    public StatusAjusteConverter() {
        super();
        super.setClazz(StatusAjuste.class);
    }

}
