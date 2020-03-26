package br.com.lume.security.converter.enums;

import javax.faces.convert.FacesConverter;

import br.com.lume.common.bo.EnumController;
import br.com.lume.odonto.entity.Fatura.StatusFatura;

@FacesConverter(forClass = StatusFatura.class, value = "enumStatusFatura")
public class StatusFaturaConverter extends EnumController<StatusFatura> {

    private static final long serialVersionUID = 1L;

    public StatusFaturaConverter() {
        super();
        super.setClazz(StatusFatura.class);
    }

}
