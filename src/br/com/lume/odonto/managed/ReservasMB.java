package br.com.lume.odonto.managed;

import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;

import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.odonto.bo.ReservaKitBO;
import br.com.lume.odonto.entity.ReservaKit;

@ManagedBean
@RequestScoped
public class ReservasMB extends LumeManagedBean<ReservaKit> {

    /**
     *
     */
    private static final long serialVersionUID = -7295521296049343800L;
    private List<ReservaKit> reservas;

    public ReservasMB() {
        super(new ReservaKitBO());
        this.setClazz(ReservaKit.class);
        this.reservas = ((ReservaKitBO) this.getbO()).listReservasDia();
    }

    public List<ReservaKit> getReservas() {
        return this.reservas;
    }

    public void setReservas(List<ReservaKit> reservas) {
        this.reservas = reservas;
    }
}
