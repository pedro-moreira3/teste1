package br.com.lume.security.converter.enums;

import javax.faces.convert.FacesConverter;

import br.com.lume.common.bo.EnumController;
import br.com.lume.odonto.entity.Fatura.TipoFatura;
import br.com.lume.odonto.entity.ProfissionalDiaria.TipoPonto;
import br.com.lume.odonto.managed.FaturaPagtoMB.TipoRecibo;

@FacesConverter(forClass = TipoPonto.class, value = "enumTipoRecibo")
public class TipoReciboConverter extends EnumController<TipoRecibo> {

    private static final long serialVersionUID = 1L;

    public TipoReciboConverter() {
        super();
        super.setClazz(TipoRecibo.class);
    }

}
