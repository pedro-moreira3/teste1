package br.com.lume.security.managed;

import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.persistence.NonUniqueResultException;

import org.apache.log4j.Logger;

import br.com.lume.common.exception.business.PlanoExpiradoException;
import br.com.lume.common.exception.business.SenhaInvalidaException;
import br.com.lume.common.exception.business.UsuarioBloqueadoException;
import br.com.lume.common.exception.business.UsuarioSemPagamentoException;
import br.com.lume.common.exception.business.UsuarioSemPerfilException;
import br.com.lume.common.log.LogIntelidenteSingleton;
import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.EnviaEmail;
import br.com.lume.common.util.JSFHelper;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.security.UsuarioSingleton;
import br.com.lume.security.bo.LoginBO;
import br.com.lume.security.bo.ObjetoBO;
import br.com.lume.security.bo.SistemaBO;
import br.com.lume.security.entity.Objeto;
import br.com.lume.security.entity.Sistema;
import br.com.lume.security.entity.Usuario;

@ManagedBean
@ViewScoped
public class LoginMB extends LumeManagedBean<Usuario> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(LoginMB.class);

    private String confirmacaoEmail;

    public LoginMB() {
        super(new LoginBO());
        this.setClazz(Usuario.class);
        // JSFHelper.getExternalContext().invalidateSession();
        // getLumeSecurity().setUsuario(null);
    }

    public String actionLogin() {
        // JSFHelper.getExternalContext().invalidateSession();
        Sistema sistema = new SistemaBO().getSistemaBySigla(JSFHelper.getSistemaAtual());
        try {
            Usuario userLogin = new LoginBO().doLogin(this.getEntity(), sistema);
            this.carregaObjetosPermitidos(userLogin, sistema);
            return "home.xhtml";
        } catch (UsuarioBloqueadoException e) {
            this.addError(e.getMessage(), "");
            this.log.error("Usuário bloqueado.", e);
        } catch (SenhaInvalidaException e) {
            this.addError(e.getMessage(), "");
            this.log.error("User/Senha inválido.", e);
        } catch (UsuarioSemPerfilException e) {
            this.addError(e.getMessage(), "");
            this.log.error("Usuário sem perfil vinculado.", e);
        } catch (UsuarioSemPagamentoException e) {
            this.addError(e.getMessage(), "");
            this.log.error("Sem Pagamento.", e);
        } catch (PlanoExpiradoException e) {
            this.addError(e.getMessage(), "");
            this.log.error("Plano acabo.", e);
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.LOGIN_ERRO_GENERICO), "");
            this.log.error("Erro ao efetuar login.", e);
        }
        return "";
    }

//    public void actionResetSenha() {
//        try {
//            if (this.getEntity().getUsuStrLogin() != null && !this.getEntity().getUsuStrLogin().equals("")) {
//                new UsuarioBO().resetSenha(this.getEntity());
//                this.addInfo(Mensagens.getMensagem("lumeSecurity.login.reset.sucesso"), "");
//            } else {
//                this.addError(Mensagens.getMensagem("lumeSecurity.login.reset.login.obrigatorio"), "");
//            }
//        } catch (ServidorEmailDesligadoException sed) {
//            this.addError(sed.getLocalizedMessage(), "");
//            this.log.error("Erro ao efetuar login.", sed);
//        } catch (Exception e) {
//            this.addError(Mensagens.getMensagem("lumeSecurity.login.reset.erro"), "");
//            this.log.error("Erro ao efetuar login.", e);
//        }
//    }

    /**
     * Método para reconfiguração de senha do usuário. O método verifica se o email usado está cadastrado, se não estiver é apresentada uma mensagem de erro, caso esteja cadastrado, é instanciado um
     * novo objeto Usuário, em seguida é gerada e enviada uma mensagen para o email do usuário para configuração de uma nova senha.
     * 
     * @param  void
     *                  - não há parâmetros.
     * @return      void - Não há retorno.
     */
    public void actionResetSenha() {
        try {
            if (getConfirmacaoEmail() == null || getConfirmacaoEmail().equals("")) {
                addError("Erro!", Mensagens.getMensagem("login.email.nao.cadastrado"));
            } else {
                Usuario usuarioTrocaSenha = UsuarioSingleton.getInstance().getBo().findByEmail(getConfirmacaoEmail());
                if (usuarioTrocaSenha != null) {
                    EnviaEmail.envioResetSenha(usuarioTrocaSenha);
                    addInfo("Sucesso!", "E-mail enviado com sucesso!");
                } else {
                    addError("Erro!", Mensagens.getMensagem("login.email.nao.cadastrado"));
                }

            }
        } catch (NonUniqueResultException e) {
            LogIntelidenteSingleton.getInstance().makeLog(e);
            addError("Erro!", Mensagens.getMensagem("login.reset.erro"));
        } catch (Exception e) {
            LogIntelidenteSingleton.getInstance().makeLog(e);
            addError("Erro!", Mensagens.getMensagem("login.reset.erro"));
        }
    }

    private void carregaObjetosPermitidos(Usuario userLogin, Sistema sistema) {
        this.getLumeSecurity().setUsuario(userLogin);
        ObjetoBO objetoBO = new ObjetoBO();
        List<Objeto> objetosPermitidos = objetoBO.getAllObjetosByUsuarioAndSistema(userLogin, sistema, UtilsFrontEnd.getEmpresaLogada().getEmpIntCod());
        Objeto objeto = new Objeto();
        objeto.setObjStrDes("home");
        objeto.setCaminho("home");
        objetosPermitidos.add(objeto);
        this.getLumeSecurity().setObjetosPermitidos(objetosPermitidos);
    }

    @Override
    public List<Usuario> getEntityList() {
        return null;
    }

    public String actionLogoff() {
        this.getLumeSecurity().setUsuario(null);
        this.getLumeSecurity().setSistema(null);
        this.getLumeSecurity().setEmpresa(null);
        JSFHelper.getExternalContext().invalidateSession();
        return "login.xhtml";
    }

    public String getConfirmacaoEmail() {
        return confirmacaoEmail;
    }

    public void setConfirmacaoEmail(String confirmacaoEmail) {
        this.confirmacaoEmail = confirmacaoEmail;
    }
}
