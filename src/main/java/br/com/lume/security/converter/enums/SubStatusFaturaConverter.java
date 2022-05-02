package br.com.lume.security.converter.enums;

import javax.faces.convert.FacesConverter;

import br.com.lume.common.bo.EnumController;
import br.com.lume.odonto.entity.Fatura.SubStatusFatura;

@FacesConverter(forClass = SubStatusFatura.class, value = "enumSubStatusFatura")
public class SubStatusFaturaConverter extends EnumController<SubStatusFatura> {

    private static final long serialVersionUID = 1L;

    public SubStatusFaturaConverter() {
        super();
        super.setClazz(SubStatusFatura.class);
    }

}
