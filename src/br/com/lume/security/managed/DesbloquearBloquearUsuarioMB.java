package br.com.lume.security.managed;

import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ActionEvent;

import org.apache.log4j.Logger;

import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.security.bo.UsuarioBO;
import br.com.lume.security.entity.Usuario;

@ManagedBean
@ViewScoped
public class DesbloquearBloquearUsuarioMB extends LumeManagedBean<Usuario> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(DesbloquearBloquearUsuarioMB.class);

    public DesbloquearBloquearUsuarioMB() {
        super(new UsuarioBO());
        this.setClazz(Usuario.class);
    }

    @Override
    public void actionPersist(ActionEvent event) {
        List<Usuario> usuarios = this.getEntityList();
        UsuarioBO usuBO = new UsuarioBO();
        this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");
        for (Usuario usuario : usuarios) {
            try {
                usuBO.persist(usuario);
            } catch (Exception e) {
                this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
                this.log.error("Erro no actionPersist", e);
            }
        }
    }

    @Override
    public List<Usuario> getEntityList() {
        UsuarioBO usuarioBO = new UsuarioBO();
        return usuarioBO.getAllUsuariosByEmpresa(this.getEntity().getEmpresa());
    }
}
