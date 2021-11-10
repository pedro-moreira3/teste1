package br.com.lume.security.managed;

import java.sql.Timestamp;
import java.util.Calendar;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ActionEvent;

import org.apache.log4j.Logger;

import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.CryptMD5;
import br.com.lume.common.util.Mensagens;
import br.com.lume.security.bo.UsuarioBO;
import br.com.lume.security.entity.Usuario;

@ManagedBean
@ViewScoped
public class TrocarSenhaMB extends LumeManagedBean<Usuario> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(TrocarSenhaMB.class);

    private String senhaNova;

    private String senhaNovaConfirmacao;

    private String senhaAtual;

    public TrocarSenhaMB() {
        super(new UsuarioBO());
        this.setClazz(Usuario.class);
    }

    @Override
    public void actionPersist(ActionEvent event) {
        Usuario usuarioLogado = this.getLumeSecurity().getUsuario();
        String senhaAtualCriptografada = CryptMD5.encrypt(this.senhaAtual);
        String senhaNovaCriptografada = CryptMD5.encrypt(this.senhaNova);
        this.log.debug("senhaAtualCriptografada: " + senhaAtualCriptografada + " senhaNovaCriptografada: " + senhaNovaCriptografada);
        this.log.debug("usuarioLogado.getUsuStrSenha(): " + usuarioLogado.getUsuStrSenha() + " senhaAtualCriptografada: " + senhaAtualCriptografada);
        if (this.senhaNovaConfirmacao.equalsIgnoreCase(this.senhaNova)) {
            if (usuarioLogado.getUsuStrSenha().equals(senhaAtualCriptografada)) {
                if (!senhaAtualCriptografada.equals(senhaNovaCriptografada)) {
                    usuarioLogado.setUsuStrSenha(senhaNovaCriptografada);
                    usuarioLogado.setUsuDtmUlttrocasenha(new Timestamp(Calendar.getInstance().getTimeInMillis()));
                    this.setEntity(usuarioLogado);
                    super.actionPersist(event);
                } else {
                    this.addError(Mensagens.getMensagem("lumeSecurity.trocar.senha.senha.atual.igual.antiga"), "");
                }
            } else {
                this.addError(Mensagens.getMensagem(Mensagens.SENHA_ATUAL_INVALIDA), "");
            }
        } else {
            this.addError(Mensagens.getMensagem(Mensagens.SENHA_CONFIRMACAO_INVALIDA), "");
        }
    }

    public String getSenhaNovaConfirmacao() {
        return this.senhaNovaConfirmacao;
    }

    public void setSenhaNovaConfirmacao(String senhaNovaConfirmacao) {
        this.senhaNovaConfirmacao = senhaNovaConfirmacao;
    }

    public String getSenhaNova() {
        return this.senhaNova;
    }

    public void setSenhaNova(String senhaNova) {
        this.senhaNova = senhaNova;
    }

    public String getSenhaAtual() {
        return this.senhaAtual;
    }

    public void setSenhaAtual(String senhaAtual) {
        this.senhaAtual = senhaAtual;
    }
}
