package br.com.lume.odonto.managed;

import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.event.ActionEvent;

import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.odonto.entity.Stat;
import br.com.lume.odonto.entity.User;
import br.com.lume.stat.StatSingleton;
import br.com.lume.user.UserSingleton;

@ManagedBean(name = "testeMB")
@RequestScoped
public class TesteMB extends LumeManagedBean<User> {

    /**
     *
     */
    private static final long serialVersionUID = -5530392226749410446L;

    public TesteMB() {
        super(UserSingleton.getInstance().getBo());
      
        this.setClazz(User.class);
    }

    @Override
    public void actionPersist(ActionEvent event) {
        this.getEntity().setStatus(this.Statusus.getId());
        super.actionPersist(event);
    }

    @Override
    public void actionRemove(ActionEvent event) {
        try {
            System.out.println("=============================1");
            User u = new User();
            u.setId(30);
            this.setEntity(UserSingleton.getInstance().getBo().find(u));
            System.out.println("=============================2");
            super.actionRemove(event);
            System.out.println("=============================3");
        } catch (Exception e) {
            // Já removeu
        }
        System.out.println("===4");
    }

    private Stat Statusus;
    private List<Stat> Statususs;

    public Stat getStatususStr() {
        try {
            return StatSingleton.getInstance().getBo().find(this.getEntity().getStatus());
        } catch (Exception e) {
            return new Stat();
        }
    }

    public Stat getStatusus() {
        return this.Statusus;
    }

    public void setStatusus(Stat Statusus) {
        this.Statusus = Statusus;
    }

    public List<Stat> getStatususs() {
        try {
            this.Statususs = StatSingleton.getInstance().getBo().listAll();
        } catch (Exception e) {
            this.Statususs = new ArrayList<>();
        }
        return this.Statususs;
    }

    public void setStatususs(List<Stat> Statususs) {
        this.Statususs = Statususs;
    }
}
