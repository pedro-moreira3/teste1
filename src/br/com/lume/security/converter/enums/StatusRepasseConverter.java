package br.com.lume.security.converter.enums;

import javax.faces.convert.FacesConverter;

import br.com.lume.common.bo.EnumController;
import br.com.lume.repasse.RepasseFaturasSingleton.StatusRepasse;

@FacesConverter(forClass = StatusRepasse.class, value = "enumStatusRepasse")
public class StatusRepasseConverter extends EnumController<StatusRepasse> {

    private static final long serialVersionUID = 1L;

    public StatusRepasseConverter() {
        super();
        super.setClazz(StatusRepasse.class);
    }

}
