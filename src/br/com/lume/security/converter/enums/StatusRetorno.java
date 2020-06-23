package br.com.lume.security.converter.enums;

import javax.faces.convert.FacesConverter;

import br.com.lume.common.bo.EnumController;

@FacesConverter(forClass = br.com.lume.odonto.entity.Retorno.StatusRetorno.class, value = "enumStatusRetorno")
public class StatusRetorno extends EnumController<br.com.lume.odonto.entity.Retorno.StatusRetorno> {

    private static final long serialVersionUID = 1L;

    public StatusRetorno() {
        super();
        super.setClazz(br.com.lume.odonto.entity.Retorno.StatusRetorno.class);
    }

}
