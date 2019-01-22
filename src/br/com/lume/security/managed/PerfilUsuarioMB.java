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
import br.com.lume.security.bo.PerfilBO;
import br.com.lume.security.bo.UsuarioBO;
import br.com.lume.security.entity.Perfil;
import br.com.lume.security.entity.Usuario;

@ManagedBean
@ViewScoped
public class PerfilUsuarioMB extends LumeManagedBean<Usuario> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(PerfilUsuarioMB.class);

    private DualListModel<Perfil> perfisPickList = new DualListModel<>();

    public PerfilUsuarioMB() {
        super(new UsuarioBO());
        this.setClazz(Usuario.class);
    }

    @Override
    public List<Usuario> getEntityList() {
        UsuarioBO usuarioBO = new UsuarioBO();
        return usuarioBO.getAllUsuariosByEmpresa(this.getLumeSecurity().getEmpresa());
    }

    public DualListModel<Perfil> getPerfisPickList() {
        List<Perfil> perfisTodos = new PerfilBO().getAllPerfisBySistema(this.getLumeSecurity().getSistema());
        if (perfisTodos != null && this.getEntity() != null && this.getEntity().getUsuStrNme() != null) {
            List<Perfil> perfisUsuarioSistema = new PerfilBO().getAllPerfisByUsuarioAndSistema(this.getEntity(), this.getLumeSecurity().getSistema());
            if (this.getEntity().getPerfisUsuarios() != null) {
                perfisTodos.removeAll(this.getEntity().getPerfisUsuarios());
                this.perfisPickList = new DualListModel<>(perfisTodos, perfisUsuarioSistema);
            } else {
                this.perfisPickList = new DualListModel<>(perfisTodos, new ArrayList<Perfil>());
            }
        } else {
            this.perfisPickList = new DualListModel<>(new ArrayList<Perfil>(), new ArrayList<Perfil>());
        }
        return this.perfisPickList;
    }

    public void setPerfisPickList(DualListModel<Perfil> perfisPickList) {
        this.perfisPickList = perfisPickList;
    }

    @Override
    public void actionPersist(ActionEvent event) {
        Usuario usuario = this.getEntity();
        // Pegar todos os perfis que n�o s�o do sistema selecionado
        List<Perfil> perfisUsuarios = usuario.getPerfisUsuarios();
        List<Perfil> target = this.perfisPickList.getTarget();
        for (Perfil perfil : perfisUsuarios) {
            if (perfil.getSistema().getSisIntCod() != this.getLumeSecurity().getSistema().getSisIntCod()) {
                target.add(perfil);
            }
        }
        usuario.setPerfisUsuarios(target);
        try {
            new UsuarioBO().persist(usuario);
            this.getLumeSecurity().clearLumeSecurity();
            this.setEntity(null);
            this.setPerfisPickList(null);
            this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
            this.log.error("Erro no actionPersist", e);
        }
    }
}
