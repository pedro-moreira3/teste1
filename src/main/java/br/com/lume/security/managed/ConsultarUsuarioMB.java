package br.com.lume.security.managed;

import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.apache.log4j.Logger;
import org.primefaces.model.menu.MenuModel;

import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.security.bo.MenuBO;
import br.com.lume.security.bo.PerfilBO;
import br.com.lume.security.bo.UsuarioBO;
import br.com.lume.security.entity.Objeto;
import br.com.lume.security.entity.Perfil;
import br.com.lume.security.entity.Usuario;

@ManagedBean
@ViewScoped
public class ConsultarUsuarioMB extends LumeManagedBean<Usuario> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(ConsultarUsuarioMB.class);

    private Usuario usuario;

    private MenuModel menuModel;

    private List<Perfil> perfis;

    private List<Objeto> objetos;

    public ConsultarUsuarioMB() {
        super(new UsuarioBO());
        this.objetos = new ArrayList<>();
        this.setClazz(Usuario.class);
    }

    public List<Usuario> getUsuarios() {
        return new UsuarioBO().getAllUsuariosByEmpresa(this.getLumeSecurity().getEmpresa());
    }

    public Usuario getUsuario() {
        return this.usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public MenuModel getMenuModel() {
        this.menuModel = new MenuBO().getMenuTreeByUsuarioAndSistema(this.usuario, this.getLumeSecurity().getSistema(), false, null);
        return this.menuModel;
    }

    public void setMenuModel(MenuModel menuModel) {
        this.menuModel = menuModel;
    }

    public List<Perfil> getPerfis() {
        this.perfis = new PerfilBO().getAllPerfisByUsuarioAndSistema(this.usuario, this.getLumeSecurity().getSistema());
        this.objetos = new ArrayList<>();
        for (Perfil p : this.perfis) {
            List<Objeto> perfisObjeto = p.getPerfisObjeto();
            this.objetos.addAll(perfisObjeto);
        }
        return this.perfis;
    }

    public void setPerfis(List<Perfil> perfis) {
        this.perfis = perfis;
    }

    public List<Objeto> getObjetos() {
        return this.objetos;
    }

    public void setObjetos(List<Objeto> objetos) {
        this.objetos = objetos;
    }
}
