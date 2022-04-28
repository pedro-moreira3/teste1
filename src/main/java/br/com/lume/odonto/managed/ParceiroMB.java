/**
 *
 */
package br.com.lume.odonto.managed;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.event.ActionEvent;

import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.odonto.entity.Parceiro;
import br.com.lume.parceiro.ParceiroSingleton;

@ManagedBean(name = "pc_ParceiroView")
@RequestScoped
public class ParceiroMB extends LumeManagedBean<Parceiro> {

    /**
     *
     */
    private static final long serialVersionUID = -1184686074982797243L;
   

    public ParceiroMB() {
        super(ParceiroSingleton.getInstance().getBo());
       
        this.setClazz(Parceiro.class);
    }

    @Override
    public void actionPersist(ActionEvent event) {
        super.actionPersist(event);
    }

    public void actionRemove(long id) {
        try {
            System.out.println("=======================Entrei");
            Parceiro u = new Parceiro();
            u.setId(id);
            this.setEntity(ParceiroSingleton.getInstance().getBo().find(u));
            super.actionRemove(null);
        } catch (Exception e) {
            // Já removeu
            System.out.println("=======================Erro :" + e.getMessage());
        }
    }
}
