package br.com.lume.odonto.managed;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

import org.apache.log4j.Logger;
import org.primefaces.PrimeFaces;

import br.com.lume.afiliacao.AfiliacaoSingleton;
import br.com.lume.common.OdontoPerfil;
import br.com.lume.common.log.LogIntelidenteSingleton;
import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.JSFHelper;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.odonto.entity.Paciente;
import br.com.lume.odonto.entity.Profissional;
import br.com.lume.paciente.PacienteSingleton;
import br.com.lume.profissional.ProfissionalSingleton;
import br.com.lume.security.EmpresaSingleton;
import br.com.lume.security.LoginSingleton;
import br.com.lume.security.ObjetoSingleton;
import br.com.lume.security.SistemaSingleton;
import br.com.lume.security.UsuarioAfiliacaoSingleton;
import br.com.lume.security.UsuarioSingleton;
import br.com.lume.security.entity.Login;
import br.com.lume.security.entity.Objeto;
import br.com.lume.security.entity.Usuario;

@Named
@ViewScoped
public class ValidaremailMB extends LumeManagedBean<Usuario> implements Serializable {

    private static final long serialVersionUID = 1L;

    private static Logger log = Logger.getLogger(ValidaremailMB.class);

    Usuario usuarioLogando;

    private String usuLogin;

    private String usuSenha;

    private String usuRepetirSenha;

    private boolean mostrarSomenteErro = true;

    public ValidaremailMB() {
        super(UsuarioSingleton.getInstance().getBo());
        validarEmail();
        setClazz(Usuario.class);
      //  setUsuarioLogado(null);
     //   setBo(new UsuarioBO());
    }

    private void validarEmail() {
        mostrarSomenteErro = true;
        FacesContext context = FacesContext.getCurrentInstance();

        Map<String, String> paramMap = context.getExternalContext().getRequestParameterMap();
        String token = paramMap.get("token");
        try {
            //UsuarioBO usuarioBo = new UsuarioBO();
            usuarioLogando = UsuarioSingleton.getInstance().getBo().buscarPorTokenEdata(token);
            if (usuarioLogando == null) {
                addError("O seu token está expirado ou é inválido, efetue novamente a troca de senha para receber um novo e-mail.",
                        "O seu token está expirado ou é inválido, efetue novamente a troca de senha para receber um novo e-mail.");
                mostrarSomenteErro = false;
            }
        } catch (Exception e) {
            addError("O seu token está expirado ou é inválido, efetue novamente a troca de senha para receber um novo e-mail.",
                    "O seu token está expirado ou é inválido, efetue novamente a troca de senha para receber um novo e-mail.");
            mostrarSomenteErro = false;         
        }

    }
 
    public String getLogo() {
        return "/images/logo_idente.png";
    }

   // public String actionLogin() {
   //     return actionLogin("vdd");
  //  }

  //TODO copiado do login, colocar no mesmo lugar para evitar copia de codigo e melhorar manutenção
    public String actionLogin() {
        if (usuarioLogando == null) {
            addError("Usuário não encontrado!", "Usuário não encontrado!");
            return "";
        }

        if (usuSenha.equals("") || usuRepetirSenha.equals("")) {
            addError("Informe os campos senha e repetir senha!", "Informe os campos senha e repetir senha!");
            return "";
        }

        if (!usuSenha.equals(usuRepetirSenha)) {
            addError("Campo senha diferente do campo repetir senha!", "Campo senha diferente do campo repetir senha!");
            return "";
        }
             
        try {
            usuarioLogando.setTokenAcesso(null);
            usuarioLogando.setDataToken(null);
            UsuarioSingleton.getInstance().getBo().doCrypt(usuarioLogando, usuSenha);          
            UsuarioSingleton.getInstance().getBo().persist(usuarioLogando);
            usuarioLogando.setUsuStrSenha(usuSenha);
        JSFHelper.getSession().invalidate();
        Usuario userLogin = LoginSingleton.getInstance().getBo().doLogin(usuarioLogando, SistemaSingleton.getInstance().getBo().getSistemaBySigla(JSFHelper.getSistemaAtual()));
        Usuario usuario = UsuarioSingleton.getInstance().getBo().findUsuarioByLogin(userLogin.getUsuStrLogin());
        List<Profissional> profissionais = ProfissionalSingleton.getInstance().getBo().listByUsuario(usuario);
        List<Paciente> pacientes = PacienteSingleton.getInstance().getBo().listByUsuario(usuario);
        LogIntelidenteSingleton.getInstance().makeLog("profissionais = "+profissionais.size());
        // Se tiver o mesmo login em profissionais e pacientes, ou repetidos na mesma lista
        if ((profissionais != null && profissionais.size() > 1) || (pacientes != null && pacientes.size() > 1) || (profissionais != null && profissionais.size() == 1 && pacientes != null && pacientes.size() == 1)) {
            List<Login> logins = this.carregarLogins(pacientes, profissionais);    
            
            LogIntelidenteSingleton.getInstance().makeLog("entrou aqui");
            
            UtilsFrontEnd.setLogins(logins);
            UtilsFrontEnd.setUsuarioNome(usuario.getUsuStrNme());
                            
            JSFHelper.redirect("loginmulti.jsf");
            return "";
        } else {
            String perfilLogado = "";
            Profissional profissional = ProfissionalSingleton.getInstance().getBo().findByUsuario(userLogin);
            if(perfilLogado.equals(OdontoPerfil.PARCEIRO)) {               
                perfilLogado = OdontoPerfil.PARCEIRO;  
                UtilsFrontEnd.setPerfilLogado(OdontoPerfil.PARCEIRO);    
                //nesse caso so tem uma afiliacao
               UtilsFrontEnd.setAfiliacaoLogada(UsuarioAfiliacaoSingleton.getInstance().getBo().listByUsuario(usuario).get(0).getAfiliacao());
               UtilsFrontEnd.setUsuarioLogado(userLogin);
            }else if (profissional == null) {
                perfilLogado = OdontoPerfil.PACIENTE;
                Paciente paciente = PacienteSingleton.getInstance().getBo().findByUsuario(userLogin);
                UtilsFrontEnd.setPacienteLogado(paciente);
                UtilsFrontEnd.setPerfilLogado(OdontoPerfil.PACIENTE);
                UtilsFrontEnd.setEmpresaLogada(EmpresaSingleton.getInstance().getBo().find(paciente.getIdEmpresa()));                   
            } else {
                if (!Profissional.INATIVO.equals(profissional.getStatus())) {
                    LogIntelidenteSingleton.getInstance().makeLog("entrou aqui 2");
                    perfilLogado = profissional.getPerfil();
                    UtilsFrontEnd.setProfissionalLogado(profissional);
                    UtilsFrontEnd.setPerfilLogado(perfilLogado);
                    UtilsFrontEnd.setEmpresaLogada(EmpresaSingleton.getInstance().getBo().find(profissional.getIdEmpresa()));                      
                } else {
                    PrimeFaces.current().executeScript("PF('loading').hide();");
                    this.addError("Profissional Inativo.", "");
                    return "";
                }
            }            
            if(!OdontoPerfil.PARCEIRO.equals(perfilLogado)) {
                LoginSingleton.getInstance().getBo().validaSituacaoEmpresa(this.getEntity(), UtilsFrontEnd.getEmpresaLogada(), UtilsFrontEnd.getPacienteLogado());    
            }
            //((LoginBO) this.getbO()).carregaObjetosPermitidos(userLogin, perfilLogado, this.getLumeSecurity(), profissional);
           // ObjetoBO objetoBO = new ObjetoBO();
            this.getLumeSecurity().setUsuario(userLogin);
            List<Objeto> objetosPermitidos = ObjetoSingleton.getInstance().getBo().carregaObjetosPermitidos(perfilLogado, profissional);
            Objeto objeto = new Objeto();
            objeto.setObjStrDes("home");
            objeto.setCaminho("home");
            objetosPermitidos.add(objeto);
            this.getLumeSecurity().setObjetosPermitidos(objetosPermitidos);
            
            
            
        }
    } catch (Exception e) {
        PrimeFaces.current().executeScript("PF('loading').hide();");
        this.addError(e.getMessage(), "");
        log.error("Erro ao efetuar login.", e);
        return "";
    }
    return this.verificaPaginaInicial();
        
//        try {
//            //  usuarioLogando = ((TcadusuarioBO) getBo()).login(usuLogin, getEntity().getUsusenha());
//            setUsuarioLogado(usuarioLogando);
//            setUsuarioLogando(usuarioLogando);
//            LogUtils.getInstance().makeLog("Usu�rio logando: " + usuarioLogando.getNome());
//            setLoginOff(usuarioLogando.getLogin());
//            setSenhaOff(usuarioLogando.getSenha());
//
//            this.atualizaEntidadesAtivas();
//            this.setFirstEntidadeSelecionada();
//
//            LoginBO loginBO = new LoginBO();
//            loginBO.authorizaUserERedireciona(usuarioLogando);
//        } catch (BusinessException e) {
//            addError(e.getMensagem(), "", e);
//            PrimeFaces.current().executeScript("PF('loading').hide();");
//        } catch (NoResultException e) {
//            //  System.out.println("nenhum resultado");
//            addError("O usu�rio informado � inv�lido.", "", e);
//        } catch (TechnicalException e) {
//            addError(Mensagens.getMensagem(Mensagens.LOGIN_ERRO_GENERICO), "", e);
//            PrimeFaces.current().executeScript("PF('loading').hide();");
//        } catch (NonUniqueResultException e) {
//            addError(Mensagens.getMensagem(Mensagens.LOGIN_ERRO_GENERICO), "", e);
//            PrimeFaces.current().executeScript("PF('loading').hide();");
//        } catch (Exception e) {
//            addErrorNoHeader(e.getMessage());
//            PrimeFaces.current().executeScript("PF('loading').hide();");
//        }
//        return "";
    }
    
    //TODO copiado do login, colocar no mesmo lugar para evitar copia de codigo e melhorar manutenção
    private List<Login> carregarLogins(List<Paciente> pacientes, List<Profissional> profissionais) {
        List<Login> logins = new ArrayList<>();
        if (pacientes != null && !pacientes.isEmpty()) {
            for (Paciente paciente : pacientes) {
                try {
                    String empStrNme = EmpresaSingleton.getInstance().getBo().find(paciente.getIdEmpresa()).getEmpStrNme();
                    logins.add(new Login(OdontoPerfil.PACIENTE, paciente.getId(), paciente.getIdEmpresa(), "Paciente da clínica " + empStrNme));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        if (profissionais != null && !profissionais.isEmpty()) {
            for (Profissional profissional : profissionais) {
                try {
                    String empStrNme = EmpresaSingleton.getInstance().getBo().find(profissional.getIdEmpresa()).getEmpStrNme();
                    logins.add(new Login(profissional.getPerfil(), profissional.getId(), profissional.getIdEmpresa(), "Profissional da clínica " + empStrNme));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return logins;
    }

    //TODO copiado do login, colocar no mesmo lugar para evitar copia de codigo e melhorar manutenção
    private String verificaPaginaInicial() {
        Profissional profissional = UtilsFrontEnd.getProfissionalLogado();
        Paciente paciente = UtilsFrontEnd.getPacienteLogado();
        String actionLoginRetorno = "";
        if (profissional != null || paciente != null) {
            if (paciente != null) {
                actionLoginRetorno = "pacienteExterno.jsf";
            } else {
                Boolean mostrarTutorial = new Boolean(this.getLumeSecurity().getUsuario().getUsuChaTutorial());
                if (mostrarTutorial) {
                    actionLoginRetorno = "tutorial.jsf";
                } else if (profissional.getPerfil().equals(OdontoPerfil.DENTISTA)) {
                    actionLoginRetorno = "paciente.jsf";
                } else if (profissional.getPerfil().equals(OdontoPerfil.ORCAMENTADOR)) {
                    actionLoginRetorno = "paciente.jsf";
                } else if (profissional.getPerfil().equals(OdontoPerfil.SECRETARIA)) {
                    actionLoginRetorno = "paciente.jsf";
                } else if (profissional.getPerfil().equals(OdontoPerfil.TOTEM)) {
                    actionLoginRetorno = "pacienteTotem.jsf";
                } else if (profissional.getPerfil().equals(OdontoPerfil.AUXILIAR_ADMINISTRATIVO)) {
                    actionLoginRetorno = "paciente.jsf";
                } else if (profissional.getPerfil().equals(OdontoPerfil.ADMINISTRADOR)) {
                    actionLoginRetorno = "paciente.jsf";
                } else if (profissional.getPerfil().equals(OdontoPerfil.ADMINISTRADORES)) {
                    actionLoginRetorno = "plano.jsf";
                } else if (profissional.getPerfil().equals(OdontoPerfil.ALMOXARIFA)) {
                    actionLoginRetorno = "paciente.jsf";
                } else if (profissional.getPerfil().equals(OdontoPerfil.AUXILIAR_DENTISTA)) {
                    actionLoginRetorno = "paciente.jsf";
                } else {
                    actionLoginRetorno = "paciente.jsf";
                }
            }
        }
        if (!actionLoginRetorno.equals("")) {
            JSFHelper.redirect(actionLoginRetorno);
        }
        return "";
    }
    public Usuario getUsuarioLogando() {
        return usuarioLogando;
    }

    public void setUsuarioLogando(Usuario usuarioLogando) {
        this.usuarioLogando = usuarioLogando;
    }

    public String getUsuLogin() {
        return usuLogin;
    }

    public void setUsuLogin(String usuLogin) {
        this.usuLogin = usuLogin;
    }

    public String getUsuSenha() {
        return usuSenha;
    }

    public void setUsuSenha(String usuSenha) {
        this.usuSenha = usuSenha;
    }

    public String getUsuRepetirSenha() {
        return usuRepetirSenha;
    }

    public void setUsuRepetirSenha(String usuRepetirSenha) {
        this.usuRepetirSenha = usuRepetirSenha;
    }

    public boolean isMostrarSomenteErro() {
        return mostrarSomenteErro;
    }

    public void setMostrarSomenteErro(boolean mostrarSomenteErro) {
        this.mostrarSomenteErro = mostrarSomenteErro;
    }

}
