package br.com.lume.odonto.managed;

import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;

import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.odonto.entity.ReservaKit;
import br.com.lume.reservaKit.ReservaKitSingleton;

@ManagedBean
@RequestScoped
public class ReservasMB extends LumeManagedBean<ReservaKit> {

    /**
     *
     */
    private static final long serialVersionUID = -7295521296049343800L;
    private List<ReservaKit> reservas;

    public ReservasMB() {
        super(ReservaKitSingleton.getInstance().getBo());
        this.setClazz(ReservaKit.class);
        this.reservas = ReservaKitSingleton.getInstance().getBo().listReservasDia(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
    }

    public List<ReservaKit> getReservas() {
        return this.reservas;
    }

    public void setReservas(List<ReservaKit> reservas) {
        this.reservas = reservas;
    }
}
