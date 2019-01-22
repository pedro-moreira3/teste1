package br.com.lume.security.managed;

import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.apache.log4j.Logger;

import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.security.bo.PerfilBO;
import br.com.lume.security.entity.Perfil;

@ManagedBean
@ViewScoped
public class PerfilMB extends LumeManagedBean<Perfil> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(PerfilMB.class);

    public PerfilMB() {
        super(new PerfilBO());
        this.setClazz(Perfil.class);
    }

    @Override
    public List<Perfil> getEntityList() {
        return new PerfilBO().getAllPerfisBySistema(this.getEntity().getSistema());
    }
}
