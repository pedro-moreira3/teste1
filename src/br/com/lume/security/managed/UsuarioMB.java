package br.com.lume.security.managed;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ActionEvent;
import javax.mail.SendFailedException;

import org.apache.log4j.Logger;

import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.security.bo.UsuarioBO;
import br.com.lume.security.entity.Empresa;
import br.com.lume.security.entity.Usuario;

@ManagedBean
@ViewScoped
public class UsuarioMB extends LumeManagedBean<Usuario> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(UsuarioMB.class);

    private boolean resetarSenha;

    private Empresa empresaSelecionada;

    public UsuarioMB() {
        super(new UsuarioBO());
        this.setClazz(Usuario.class);
    }

    public boolean isResetarSenha() {
        return this.resetarSenha;
    }

    public void setResetarSenha(boolean ressetarSenha) {
        this.resetarSenha = ressetarSenha;
    }

    @Override
    public void actionPersist(ActionEvent event) {
        UsuarioBO bo = new UsuarioBO();
        Usuario userAux = bo.getUsuarioByLogin(this.getEntity());
        if (userAux != null && userAux.getUsuIntCod() != this.getEntity().getUsuIntCod()) {
            this.addError(Mensagens.getMensagem(Mensagens.USUARIO_DUPLICADO), "");
            this.log.error(Mensagens.getMensagem(Mensagens.USUARIO_DUPLICADO));
        } else {
            if (this.getEntity().getUsuIntCod() == 0 || this.resetarSenha) {
                try {
                    bo.doSenhaPadrao(this.getEntity(), UtilsFrontEnd.getEmpresaLogada());
                } catch (SendFailedException e) {
                    this.addWarn(Mensagens.getMensagem(Mensagens.EMAIL_INVALIDO), "");
                    this.log.error(Mensagens.getMensagem(Mensagens.EMAIL_INVALIDO), e);
                } catch (Exception e) {
                    this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
                    this.log.error(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), e);
                }
            }
            super.actionPersist(event);
        }
    }

    @Override
    public void setEntity(Usuario entity) {
        if (entity != null) {
            this.setEmpresaSelecionada(entity.getEmpresa());
        }
        super.setEntity(entity);
    }

//    @Override
//    public Usuario getEntity() {
//        Usuario usuario = super.getEntity();
//        if (usuario.getEmpresa() == null) {
//            if (getLumeSecurity() != null) {
//                usuario.setEmpresa(getLumeSecurity().getEmpresa());
//            }
//        }
//        return usuario;
//    }

    public Empresa getEmpresaSelecionada() {
        return this.empresaSelecionada;
    }

    public void setEmpresaSelecionada(Empresa empresaSelecionada) {
        //getEntity().setEmpresa(empresaSelecionada);
        UsuarioBO usuarioBO = new UsuarioBO();
        this.setEntityList(usuarioBO.getAllUsuariosByEmpresa(this.getEntity().getEmpresa()));
        this.empresaSelecionada = empresaSelecionada;
    }
}
