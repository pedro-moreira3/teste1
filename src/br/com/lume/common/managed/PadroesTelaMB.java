/**
 *
 */
package br.com.lume.common.managed;

import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;

import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.UtilsPadraoRelatorio.PeriodoBusca;
import br.com.lume.odonto.entity.Paciente;
import br.com.lume.paciente.PacienteSingleton;

@ManagedBean
@RequestScoped
public class PadroesTelaMB extends LumeManagedBean<Paciente> {

    private static final long serialVersionUID = 1L;

    public PadroesTelaMB() {
        super(PacienteSingleton.getInstance().getBo());
        this.setClazz(Paciente.class);
    }

    public List<PeriodoBusca> getPeriodosDataBusca() {
        return PeriodoBusca.listAllValues();
    }

    public String getTooltipValue(String tela, String campo) {
        return Mensagens.getMensagem("tooltip." + tela + "." + campo);
    }

}
