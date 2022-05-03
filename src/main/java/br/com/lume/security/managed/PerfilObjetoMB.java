package br.com.lume.security.managed;

import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ActionEvent;

import org.apache.log4j.Logger;
import org.primefaces.model.DualListModel;

import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.security.bo.ObjetoBO;
import br.com.lume.security.bo.PerfilBO;
import br.com.lume.security.entity.Objeto;
import br.com.lume.security.entity.Perfil;

@ManagedBean
@ViewScoped
public class PerfilObjetoMB extends LumeManagedBean<Perfil> {

    private static final long serialVersionUID = 1L;

    private DualListModel<Objeto> objetosPickList;

    private Logger log = Logger.getLogger(PerfilObjetoMB.class);

    public PerfilObjetoMB() {
        super(new PerfilBO());
        this.setClazz(Perfil.class);
    }

    @Override
    public List<Perfil> getEntityList() {
        PerfilBO perfilBO = new PerfilBO();
        return perfilBO.getAllPerfisBySistema(this.getLumeSecurity().getSistema());
    }

    public DualListModel<Objeto> getObjetosPickList() {
        List<Objeto> objetosTodos = new ObjetoBO().getAllObjetosTelaBySistema(this.getLumeSecurity().getSistema());
        if (objetosTodos != null && this.getEntity() != null && this.getEntity().getPerStrDes() != null) {
            if (this.getEntity().getPerfisObjeto() != null) {
                objetosTodos.removeAll(this.getEntity().getPerfisObjeto());
                this.objetosPickList = new DualListModel<>(objetosTodos, this.getEntity().getPerfisObjeto());
            } else {
                this.objetosPickList = new DualListModel<>(objetosTodos, new ArrayList<Objeto>());
            }

        } else {
            this.objetosPickList = new DualListModel<>(new ArrayList<Objeto>(), new ArrayList<Objeto>());
        }
        return this.objetosPickList;
    }

    @Override
    public void actionPersist(ActionEvent event) {
        Perfil perfil = this.getEntity();
        perfil.setPerfisObjeto(this.objetosPickList.getTarget());
        try {
            new PerfilBO().persist(perfil);
            this.getLumeSecurity().clearLumeSecurity();
            this.setEntity(null);
            this.setObjetosPickList(null);
            this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
            this.log.error("Erro no actionPersist", e);
        }
    }

    public void setObjetosPickList(DualListModel<Objeto> objetosPickList) {
        this.objetosPickList = objetosPickList;
    }
}
