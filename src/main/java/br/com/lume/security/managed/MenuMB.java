package br.com.lume.security.managed;

import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

import org.apache.log4j.Logger;
import org.primefaces.PrimeFaces;
import org.primefaces.model.menu.MenuModel;

import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.phaselistener.AuthorizationListener;
import br.com.lume.common.util.JSFHelper;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.security.bo.MenuBO;
import br.com.lume.security.bo.ObjetoBO;
import br.com.lume.security.bo.SistemaBO;
import br.com.lume.security.entity.Objeto;
import br.com.lume.security.entity.Sistema;
import br.com.lume.security.entity.Usuario;

@ManagedBean
@SessionScoped
public class MenuMB extends LumeManagedBean<Objeto> {

    private static final long serialVersionUID = 1L;

    protected MenuModel menuModel;

    private Logger log = Logger.getLogger(MenuMB.class);

    private MenuBO menuBO;

    private ObjetoBO objetoBO;

    private List<Objeto> modulos;

    private Sistema sistema;

    public MenuMB() {
        super(new ObjetoBO());
        this.setClazz(Objeto.class);
        menuBO = new MenuBO();
        objetoBO = new ObjetoBO();
        carregarMenu();
    }

    public void carregarMenu() {
        Usuario usuario = this.getLumeSecurity().getUsuario();
        sistema = new SistemaBO().getSistemaBySigla(JSFHelper.getSistemaAtual());
        menuModel = menuBO.getMenuTreeByUsuarioAndSistema(usuario, sistema, true);
        if (usuario != null) {
            String estoque;
            if(UtilsFrontEnd.getEmpresaLogada() != null) {
                 estoque = UtilsFrontEnd.getEmpresaLogada().getEmpStrEstoque();
            }else{
                //aqui tanto faz
                estoque = "N";
            }
            modulos = objetoBO.getObjetosRaizByUsuarioAndSistema(usuario, sistema,estoque);
        }
        
        PrimeFaces.current().executeScript("document.getElementById(\"menuform:menu_0\").setAttribute(\"class\",\"active-menuitem\");");
        PrimeFaces.current().executeScript("document.getElementById(\"menuform:menu_0\").children[2].setAttribute(\"style\",\"display:block\");");
        PrimeFaces.current().executeScript("document.getElementById(\"menuform:menu_0\").children[2].children[0].setAttribute(\"class\",\"active-menuitem\");");
        
    }

    public MenuModel menuModelUnit(Objeto raiz) {
        Usuario usuario = this.getLumeSecurity().getUsuario();
        List<Objeto> objs = new ArrayList<>();
        objs.add(raiz);
        return menuBO.getMenuTreeByUsuarioAndSistema(usuario, sistema, true, objs);
    }
    
    public String redireciona(String pagina) {
        //VERIFICANDO SE CLIENTE ESTA BLOQUEADO
       if(UtilsFrontEnd.getEmpresaLogada().isBloqueado()) {
           for (String paginaLiberada : AuthorizationListener.PAGINAS_DISPONIVEIS_USUARIO_BLOQUEADO) {
               if (pagina.contains(paginaLiberada)) {
                   return pagina+"?faces-redirect=true";   
               }
           }
           return "home.jsf?faces-redirect=true";   
       }else {
           return pagina+"?faces-redirect=true";   
       }       
        
    }

    public MenuModel getMenuModel() {
        return menuModel;
    }

    public void setMenuModel(MenuModel menuModel) {
        this.menuModel = menuModel;
    }

    public List<Objeto> getModulos() {
        return modulos;
    }

    public void setModulos(List<Objeto> modulos) {
        this.modulos = modulos;
    }
}
