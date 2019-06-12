package br.com.lume.security.bo;

import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.primefaces.model.menu.DefaultMenuItem;
import org.primefaces.model.menu.DefaultMenuModel;
import org.primefaces.model.menu.DefaultSubMenu;
import org.primefaces.model.menu.MenuModel;
import org.primefaces.model.menu.Submenu;

import br.com.lume.common.bo.BO;

import br.com.lume.odonto.dao.PersistenceUnitName;
import br.com.lume.odonto.entity.Profissional;
import br.com.lume.security.entity.Objeto;
import br.com.lume.security.entity.Sistema;
import br.com.lume.security.entity.Usuario;

public class MenuBO extends BO<Usuario> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(MenuBO.class);

    private int idMenu = 1;

    public MenuBO() {
        super(PersistenceUnitName.ODONTO);
        this.setClazz(Usuario.class);
    }

    public MenuModel getMenuTreeByUsuarioAndSistema(Usuario usuario, Sistema sistema, boolean mostraURL) {
        List<Objeto> objetosRaizBySistema = new ObjetoBO().getObjetosRaizBySistema(sistema);
        return this.getMenuTreeByUsuarioAndSistema(usuario, sistema, mostraURL, objetosRaizBySistema);
    }

    public MenuModel getMenuTreeByUsuarioAndSistema(Usuario usuario, Sistema sistema, boolean mostraURL, List<Objeto> objetosRaizBySistema) {
        if (usuario != null && sistema != null) {
            try {
                List<Objeto> objetosPermitidos = new ObjetoBO().carregaObjetosPermitidos((String) Configurar.getInstance().getConfiguracao().getPerfilLogado(),
                        (Profissional) Configurar.getInstance().getConfiguracao().getProfissionalLogado());
                Set<Objeto> labelsPermitidas = this.getLabelsPermitidas(objetosPermitidos);
                return this.getMenuTree(sistema, labelsPermitidas, objetosPermitidos, mostraURL, objetosRaizBySistema);
            } catch (Exception e) {
                log.error(e);
            }
        }
        return null;
    }

    public Set<Objeto> getLabelsPermitidas(List<Objeto> objetosPermitidos) {
        Set<Objeto> labelsPermitidas = new HashSet<>();
        for (Objeto objeto : objetosPermitidos) {
            labelsPermitidas = new ObjetoBO().findAllObjetosPaiLabel(labelsPermitidas, objeto);
        }
        return labelsPermitidas;
    }

    public MenuModel getlMenuTreeBySistema(Sistema sistema) {
        List<Objeto> objetosRaizBySistema = new ObjetoBO().getObjetosRaizBySistema(sistema);
        return new MenuBO().getMenuTree(sistema, null, null, false, objetosRaizBySistema);
    }

    public MenuModel getMenuTree(Sistema sistema, Set<Objeto> labelsPermitidas, List<Objeto> objetosPermitidos, boolean mostraURL, List<Objeto> objetosRaizBySistema) {
        MenuModel model = new DefaultMenuModel();
//        DefaultSubMenu root = new DefaultSubMenu();
//        root.setLabel("");
//        model.addElement(root);

        HashMap<Long, Submenu> raizesMenu = new HashMap<>();
        for (Objeto objeto : objetosRaizBySistema) {
            if (Objeto.LABEL.equals(objeto.getObjChaTipo())) {
                if (labelsPermitidas != null) {
                    if (labelsPermitidas.contains(objeto)) {
                        DefaultSubMenu submenu = new DefaultSubMenu();
                        submenu.setId("sb" + idMenu++ + idMenu++);
                        submenu.setLabel(objeto.getObjStrDes());
                        submenu.setIcon(objeto.getIcone());
//                        submenu.setIcon("fa fa-caret-right");
                        //submenu.setExpanded(true);

                        model.addElement(submenu);
                        raizesMenu.put(objeto.getObjIntCod(), submenu);
                    }
                } else {
                    DefaultSubMenu submenu = new DefaultSubMenu();
                    submenu.setIcon(objeto.getIcone());
                    submenu.setId("ssb" + idMenu++);
                    submenu.setLabel(objeto.getObjStrDes());
                    model.addElement(submenu);
                    raizesMenu.put(objeto.getObjIntCod(), submenu);
                }
            } else {
                if (objetosPermitidos != null) {
                    if (objetosPermitidos.contains(objeto)) {
                        DefaultMenuItem mi = new DefaultMenuItem();
                        //mi.setIcon(objeto.getIcone());
                        mi.setId("mi" + idMenu++ + idMenu++);
                        mi.setValue(objeto.getObjStrDes());
                        if (mostraURL) {
                            mi.setUrl(objeto.getCaminho());
                        }
                        model.addElement(mi);
                    }
                } else {
                    DefaultMenuItem mi = new DefaultMenuItem();
                    //mi.setIcon(objeto.getIcone());
                    mi.setId("mmi" + idMenu++);
                    mi.setValue(objeto.getObjStrDes());
                    if (mostraURL) {
                        mi.setUrl(objeto.getCaminho());
                    }
                    model.addElement(mi);
                }
            }
            this.getMenuTree(objeto, model, raizesMenu, labelsPermitidas, objetosPermitidos, mostraURL);
        }
        DefaultMenuItem miLogoff = new DefaultMenuItem();
        miLogoff.setId("mi" + Calendar.getInstance().getTimeInMillis() + idMenu++);
        miLogoff.setUrl("sobre.jsf");
        miLogoff.setValue("Atualizações");
        model.addElement(miLogoff);
        return model;
    }

    private void getMenuTree(Objeto objetoPai, MenuModel model, HashMap<Long, Submenu> raizesMenu, Set<Objeto> labelsPermitidas, List<Objeto> objetosPermitidos, boolean mostraURL) {
        // log.debug("Meu pai � :> " + objetoPai.getObjStrDes() +
        // "["+objetoPai.getCaminho()+"]");
        List<Objeto> objetosFilhos = new ObjetoBO().getAllObjetosFilhos(objetoPai);
        if (objetosFilhos != null) {
            for (Objeto filho : objetosFilhos) {
                if (Objeto.LABEL.equals(filho.getObjChaTipo())) {
                    if (labelsPermitidas != null) {
                        if (labelsPermitidas.contains(filho)) {
                            DefaultSubMenu submenu = new DefaultSubMenu();
                            submenu.setIcon(filho.getIcone());
                            submenu.setId("sbb" + idMenu++ + idMenu++);
                            submenu.setLabel(filho.getObjStrDes());
                            raizesMenu.get(objetoPai.getObjIntCod()).getElements().add(submenu);
                            raizesMenu.put(filho.getObjIntCod(), submenu);
                        }
                    } else {
                        DefaultSubMenu submenu = new DefaultSubMenu();
                        submenu.setIcon(filho.getIcone());
                        submenu.setId("sbb" + idMenu++);
                        submenu.setLabel(filho.getObjStrDes());
                        raizesMenu.get(objetoPai.getObjIntCod()).getElements().add(submenu);
                        raizesMenu.put(filho.getObjIntCod(), submenu);
                    }
                } else {
                    if (objetosPermitidos != null) {
                        if (objetosPermitidos.contains(filho)) {
                            DefaultMenuItem mi = new DefaultMenuItem();
                            //mi.setIcon(filho.getIcone());
                            mi.setId("mib" + idMenu++ + idMenu++);
                            mi.setValue(filho.getObjStrDes());
                            if (mostraURL) {
                                mi.setUrl(filho.getCaminho());
                            }
                            raizesMenu.get(objetoPai.getObjIntCod()).getElements().add(mi);
                        }
                    } else {
                        DefaultMenuItem mi = new DefaultMenuItem();
                        //mi.setIcon(filho.getIcone());
                        mi.setId("mib" + idMenu++);
                        mi.setValue(filho.getObjStrDes());
                        if (mostraURL) {
                            mi.setUrl(filho.getCaminho());
                        }
                        raizesMenu.get(objetoPai.getObjIntCod()).getElements().add(mi);
                    }
                }
                this.getMenuTree(filho, model, raizesMenu, labelsPermitidas, objetosPermitidos, mostraURL);
            }
        }
    }
}
