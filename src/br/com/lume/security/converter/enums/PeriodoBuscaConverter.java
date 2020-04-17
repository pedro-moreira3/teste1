package br.com.lume.security.converter.enums;

import javax.faces.convert.FacesConverter;

import br.com.lume.common.bo.EnumController;
import br.com.lume.common.util.UtilsPadraoRelatorio.PeriodoBusca;
import br.com.lume.odonto.entity.AjusteContas.StatusAjuste;

@FacesConverter(forClass = PeriodoBusca.class, value = "enumPeriodoBusca")
public class PeriodoBuscaConverter extends EnumController<PeriodoBusca> {

    private static final long serialVersionUID = 1L;

    public PeriodoBuscaConverter() {
        super();
        super.setClazz(PeriodoBusca.class);
    }

}
