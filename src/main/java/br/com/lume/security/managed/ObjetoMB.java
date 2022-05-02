package br.com.lume.security.managed;

import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ActionEvent;

import org.apache.log4j.Logger;
import org.primefaces.model.menu.MenuModel;

import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.security.bo.MenuBO;
import br.com.lume.security.bo.ObjetoBO;
import br.com.lume.security.entity.Objeto;
import br.com.lume.security.entity.Sistema;

@ManagedBean
@ViewScoped
public class ObjetoMB extends LumeManagedBean<Objeto> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(ObjetoMB.class);

    private List<Objeto> objetosPai;

    private MenuModel menuModel;

    private Sistema sistemaSelecionado;

    public ObjetoMB() {
        super(new ObjetoBO());
        this.setClazz(Objeto.class);
    }

    @Override
    public List<Objeto> getEntityList() {
        return new ObjetoBO().getAllObjetosBySistema(this.sistemaSelecionado);
    }

    public List<Objeto> getObjetosPai() {
        if (this.getEntity() != null) {
            this.objetosPai = new ObjetoBO().getAllObjetosLabelBySistema(this.sistemaSelecionado);
        }
        return this.objetosPai;
    }

    public void setObjetosPai(List<Objeto> objetosPai) {
        this.objetosPai = objetosPai;
    }

    @Override
    public void actionPersist(ActionEvent event) {
        if (this.getEntity().getObjChaTipo().equals(Objeto.LABEL) && this.getEntity().getObjetoPai() == null) {
            this.getEntity().setCaminho("LABEL_RAIZ");
        } else if (this.getEntity().getObjChaTipo().equals(Objeto.LABEL) && this.getEntity().getObjetoPai() != null) {
            this.getEntity().setCaminho("LABEL_NO");
        }
        super.actionPersist(event);
    }

    public MenuModel getMenuModel() {
        if (this.sistemaSelecionado != null) {
            this.menuModel = new MenuBO().getlMenuTreeBySistema(this.sistemaSelecionado);
        }
        return this.menuModel;
    }

    public void setMenuModel(MenuModel menuModel) {
        this.menuModel = menuModel;
    }

    public Sistema getSistemaSelecionado() {
        return this.sistemaSelecionado;
    }

    public void setSistemaSelecionado(Sistema sistemaSelecionado) {
        this.getEntity().setSistema(sistemaSelecionado);
        this.sistemaSelecionado = sistemaSelecionado;
    }

}
