package br.com.lume.security.converter.enums;

import javax.faces.convert.FacesConverter;

import br.com.lume.common.bo.EnumController;
import br.com.lume.odonto.entity.DetailPacoteConfirmacaoAuto.PrioridadeEnvio;

@FacesConverter(forClass = PrioridadeEnvio.class, value = "enumPrioridadeEnvio")
public class PrioridadeEnvioConverter extends EnumController<PrioridadeEnvio> {

    private static final long serialVersionUID = 1L;

    public PrioridadeEnvioConverter() {
        super();
        super.setClazz(PrioridadeEnvio.class);
    }

}
