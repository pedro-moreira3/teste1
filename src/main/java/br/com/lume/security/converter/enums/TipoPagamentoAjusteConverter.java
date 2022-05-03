package br.com.lume.security.converter.enums;

import javax.faces.convert.FacesConverter;

import br.com.lume.common.bo.EnumController;
import br.com.lume.odonto.entity.AjusteContas.TipoPagamento;

@FacesConverter(forClass = TipoPagamento.class, value = "enumTipoPagamentoAjuste")
public class TipoPagamentoAjusteConverter extends EnumController<TipoPagamento> {

    private static final long serialVersionUID = 1L;

    public TipoPagamentoAjusteConverter() {
        super();
        super.setClazz(TipoPagamento.class);
    }

}
