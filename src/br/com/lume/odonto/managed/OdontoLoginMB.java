package br.com.lume.odonto.managed;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.apache.log4j.Logger;
import org.primefaces.PrimeFaces;

import br.com.lume.afiliacao.AfiliacaoSingleton;
import br.com.lume.common.OdontoPerfil;
import br.com.lume.common.exception.business.SenhaInvalidaException;
import br.com.lume.common.exception.business.ServidorEmailDesligadoException;
import br.com.lume.common.exception.business.UsuarioBloqueadoException;
import br.com.lume.common.log.LogIntelidenteSingleton;
import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.EnviaEmail;
import br.com.lume.common.util.JSFHelper;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.Utils;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.odonto.entity.Afiliacao;
// import br.com.lume.odonto.bo.PacienteBO;
// import br.com.lume.odonto.bo.ProfissionalBO;
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
// import br.com.lume.security.bo.ObjetoBO;
// import br.com.lume.security.bo.EmpresaBO;
// import br.com.lume.security.bo.LoginBO;
// import br.com.lume.security.bo.SistemaBO;
// import br.com.lume.security.bo.UsuarioBO;
import br.com.lume.security.entity.Empresa;
import br.com.lume.security.entity.Login;
import br.com.lume.security.entity.Objeto;
import br.com.lume.security.entity.Sistema;
import br.com.lume.security.entity.Usuario;
import br.com.lume.security.entity.UsuarioAfiliacao;

@ManagedBean
@ViewScoped
public class OdontoLoginMB extends LumeManagedBean<Usuario> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(OdontoLoginMB.class);

    private List<Empresa> empresasLogin;

    private Empresa empresaLogin;

    //  private UsuarioBO usuarioBO;

    //  private EmpresaBO empresaBO;

//    private ProfissionalBO profissionalBO;

    // private PacienteBO pacienteBO;

    private String login;

    private String erroLogin;

    //private SistemaBO sistemaBO;

    private Sistema sistema;

    public OdontoLoginMB() {
        super(LoginSingleton.getInstance().getBo());
        this.setClazz(Usuario.class);
        //   profissionalBO = new ProfissionalBO();
        //   pacienteBO = new PacienteBO();
        //   empresaBO = new EmpresaBO();
        //  usuarioBO = new UsuarioBO();
        //   sistemaBO = new SistemaBO();

        //  Usuario usuario = UsuarioSingleton.getInstance().getBo().findUsuarioByLogin("FARUK.ZAHRA@LUMETEC.COM.BR");
        //List<Profissional> profissionais = profissionalBO.listByUsuario(usuario);
        //  List<Profissional> profissionais = ProfissionalSingleton.getInstance().getBo().listByUsuario(usuario);
        //  System.out.println(profissionais);

        sistema = SistemaSingleton.getInstance().getBo().getSistemaBySigla(JSFHelper.getSistemaAtual());
    }

    public String actionLogin() {
        try {
            JSFHelper.getSession().invalidate();

            Usuario userLogin = LoginSingleton.getInstance().getBo().doLogin(this.getEntity(), sistema);
            Usuario usuario = UsuarioSingleton.getInstance().getBo().findUsuarioByLogin(userLogin.getUsuStrLogin());
            List<Profissional> profissionais = ProfissionalSingleton.getInstance().getBo().listByUsuario(usuario);
            List<Paciente> pacientes = PacienteSingleton.getInstance().getBo().listByUsuario(usuario);
            List<UsuarioAfiliacao> usuariosAfiliados = UsuarioAfiliacaoSingleton.getInstance().getBo().listByUsuario(usuario);
            
           
            int somaPerfis = 0;
            if (profissionais.size() > 0) {
                somaPerfis++;
            }
            if (pacientes.size() > 0) {
                somaPerfis++;
            }
            if (usuariosAfiliados.size() > 0) {
                somaPerfis++;
            }
            
            
            // Se tiver o mesmo login em profissionais e pacientes, ou repetidos na mesma lista
            if (somaPerfis > 1) {
                List<Login> logins = this.carregarLogins(pacientes, profissionais,usuariosAfiliados);

                UtilsFrontEnd.setLogins(logins);
                UtilsFrontEnd.setUsuarioNome(usuario.getUsuStrNme());

                JSFHelper.redirect("loginmulti.jsf");
                return "";
            } else {
                String perfilLogado = "";
                Profissional profissional = ProfissionalSingleton.getInstance().getBo().findByUsuario(userLogin);
                
                Paciente paciente = PacienteSingleton.getInstance().getBo().findByUsuario(userLogin);
                
                if (profissional == null && paciente != null) {
                    perfilLogado = OdontoPerfil.PACIENTE;                    
                    UtilsFrontEnd.setPacienteLogado(paciente);
                    UtilsFrontEnd.setPerfilLogado(OdontoPerfil.PACIENTE);
                    UtilsFrontEnd.setEmpresaLogada(EmpresaSingleton.getInstance().getBo().find(paciente.getIdEmpresa()));
                } else if (profissional != null){
                    if (!Profissional.INATIVO.equals(profissional.getStatus())) {
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
                
                if(perfilLogado.equals("")) {
                    perfilLogado = OdontoPerfil.PARCEIRO;
                }
                
                if(!OdontoPerfil.PARCEIRO.equals(perfilLogado)) {
                    LoginSingleton.getInstance().getBo().validaSituacaoEmpresa(this.getEntity(), UtilsFrontEnd.getEmpresaLogada(), UtilsFrontEnd.getPacienteLogado());    
                }else {
                    List<Login> logins = this.carregarLogins(pacientes, profissionais,usuariosAfiliados);

                    UtilsFrontEnd.setLogins(logins);
                    UtilsFrontEnd.setUsuarioNome(usuario.getUsuStrNme());

                    JSFHelper.redirect("loginmulti.jsf");
                    return "";

                }
                
                
               
                this.getLumeSecurity().setUsuario(userLogin);
                List<Objeto> objetosPermitidos = ObjetoSingleton.getInstance().getBo().carregaObjetosPermitidos(perfilLogado, profissional);
                Objeto objeto = new Objeto();
                objeto.setObjStrDes("home");
                objeto.setCaminho("home");
                objetosPermitidos.add(objeto);
                this.getLumeSecurity().setObjetosPermitidos(objetosPermitidos);

            }
        } catch (UsuarioBloqueadoException ub) {
            this.addError("Erro no login", "Senha inválida", true);
            log.error("Erro ao efetuar login.", ub);
            return "";
        } catch (SenhaInvalidaException si) {
            this.addError("Erro no login", "Senha inválida", true);
            log.error("Erro ao efetuar login.", si);
            return "";
        } catch (Exception e) {
            this.addError("Erro no login", "Senha inválida", true);
            log.error("Erro ao efetuar login.", e);
            return "";
        }
        return this.verificaPaginaInicial();
    }

    public String actionLoginMulti() {
        try {
            String[] split = login.split("_");
            String id = split[0];
            String perfil = split[1];
            Usuario userLogin = null;
            String perfilLogado = "";
            Profissional profissional = null;
            if(OdontoPerfil.PARCEIRO.equals(perfil)) {
                String idParceiro = split[2];
                perfilLogado = OdontoPerfil.PARCEIRO;            
                userLogin = UsuarioSingleton.getInstance().getBo().find(Long.parseLong(id));              
                UtilsFrontEnd.setPerfilLogado(OdontoPerfil.PARCEIRO);                
                UtilsFrontEnd.setAfiliacaoLogada(AfiliacaoSingleton.getInstance().getBo().find(Long.parseLong(idParceiro)));
                UtilsFrontEnd.setUsuarioLogado(userLogin);
            }else if (!OdontoPerfil.PACIENTE.equals(perfil)) {
                profissional = ProfissionalSingleton.getInstance().getBo().find(Long.parseLong(id));

                if (!Profissional.INATIVO.equals(profissional.getStatus())) {
                    perfilLogado = profissional.getPerfil();
                    userLogin = UsuarioSingleton.getInstance().getBo().find(profissional.getIdUsuario());
                    UtilsFrontEnd.setPerfilLogado(perfilLogado);
                    UtilsFrontEnd.setProfissionalLogado(profissional);
                    UtilsFrontEnd.setEmpresaLogada(EmpresaSingleton.getInstance().getBo().find(profissional.getIdEmpresa()));
                    this.validarSituacaoIuguEmpresa(UtilsFrontEnd.getEmpresaLogada());
                } else {
                    this.addError("Profissional Inativo.", "");
                    return "";
                }
            } else {
                perfilLogado = OdontoPerfil.PACIENTE;
                Paciente paciente = PacienteSingleton.getInstance().getBo().find(Long.parseLong(id));
                userLogin = UsuarioSingleton.getInstance().getBo().find(paciente.getIdUsuario());
                UtilsFrontEnd.setPacienteLogado(paciente);
                UtilsFrontEnd.setPerfilLogado(OdontoPerfil.PACIENTE);
                UtilsFrontEnd.setEmpresaLogada(EmpresaSingleton.getInstance().getBo().find(paciente.getIdEmpresa()));
            }
            if(!OdontoPerfil.PARCEIRO.equals(perfil)) {
                LoginSingleton.getInstance().getBo().validaSituacaoEmpresa(this.getEntity(), UtilsFrontEnd.getEmpresaLogada(), UtilsFrontEnd.getPacienteLogado());  
                //salvando data de ultimo acesso 
                UtilsFrontEnd.getEmpresaLogada().setDataUltimoAcesso(new Date());
                EmpresaSingleton.getInstance().getBo().persist(UtilsFrontEnd.getEmpresaLogada());
                
            }
            
           
            
            List<Objeto> objetosPermitidos = LoginSingleton.getInstance().getBo().carregaObjetosPermitidos(userLogin, perfilLogado, profissional);
            this.getLumeSecurity().setUsuario(userLogin);
            this.getLumeSecurity().setObjetosPermitidos(objetosPermitidos);

        } catch (Exception e) {
            this.addError(e.getMessage(), "");
            log.error("Erro ao efetuar login.", e);
            return "";
        }
        return this.verificaPaginaInicial();
    }

    private List<Login> carregarLogins(List<Paciente> pacientes, List<Profissional> profissionais,List<UsuarioAfiliacao> usuariosAfiliados) {
        List<Login> logins = new ArrayList<>();
        
        if(usuariosAfiliados != null && !usuariosAfiliados.isEmpty()) {
            for (UsuarioAfiliacao usuarioAfiliacao : usuariosAfiliados) {
                try {
                    String nomeAfiliacao = AfiliacaoSingleton.getInstance().getBo().find(usuarioAfiliacao.getAfiliacao().getId()).getNome();
                    logins.add(new Login(OdontoPerfil.PARCEIRO, usuarioAfiliacao.getUsuario().getUsuIntCod(), usuarioAfiliacao.getAfiliacao().getId(), nomeAfiliacao));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        
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
                    if(!profissional.getStatus().equals("I")) {
                        String empStrNme = EmpresaSingleton.getInstance().getBo().find(profissional.getIdEmpresa()).getEmpStrNme();
                        logins.add(new Login(profissional.getPerfil(), profissional.getId(), profissional.getIdEmpresa(), "Profissional da clínica " + empStrNme));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            logins.sort((emp1, emp2) -> {
                String aux = emp1.getDescricao();
                return aux.compareTo(emp2.getDescricao());
            });
        }       
        
        
        
        return logins;
    }

    public List<Login> getLogins() {
        return UtilsFrontEnd.getLogins();

    }

    public String getUsuarioNome() {
        return UtilsFrontEnd.getUsuarioNome();
    }

    private String verificaPaginaInicial() {
        Profissional profissional = UtilsFrontEnd.getProfissionalLogado();
        Paciente paciente = UtilsFrontEnd.getPacienteLogado();
        Afiliacao afiliacao = UtilsFrontEnd.getAfiliacaoLogada();
        String actionLoginRetorno = "";

        boolean goToTutorial = false;
        
        if (afiliacao != null) {
            actionLoginRetorno = "clinicas.jsf";
        }else if (profissional != null || paciente != null) {
            if (paciente != null) {
                actionLoginRetorno = "pacienteExterno.jsf";
            } else {
                Boolean mostrarTutorial = new Boolean(this.getLumeSecurity().getUsuario().getUsuChaTutorial());
                if (mostrarTutorial)
                    goToTutorial = true;

                if (profissional.getPerfil().equals(OdontoPerfil.DENTISTA)) {
                    actionLoginRetorno = "home.jsf";
                } else if (profissional.getPerfil().equals(OdontoPerfil.ORCAMENTADOR)) {
                    actionLoginRetorno = "home.jsf";
                } else if (profissional.getPerfil().equals(OdontoPerfil.SECRETARIA)) {
                    actionLoginRetorno = "home.jsf";
                } else if (profissional.getPerfil().equals(OdontoPerfil.TOTEM)) {
                    actionLoginRetorno = "pacienteTotem.jsf";
                } else if (profissional.getPerfil().equals(OdontoPerfil.AUXILIAR_ADMINISTRATIVO)) {
                    actionLoginRetorno = "home.jsf";
                } else if (profissional.getPerfil().equals(OdontoPerfil.ADMINISTRADOR)) {
                    actionLoginRetorno = "home.jsf";
                } else if (profissional.getPerfil().equals(OdontoPerfil.ADMINISTRADORES)) {
                    actionLoginRetorno = "plano.jsf";
                } else if (profissional.getPerfil().equals(OdontoPerfil.ALMOXARIFA)) {
                    actionLoginRetorno = "home.jsf";
                } else if (profissional.getPerfil().equals(OdontoPerfil.AUXILIAR_DENTISTA)) {
                    actionLoginRetorno = "paciente.jsf";
                } else if (profissional.getPerfil().equals(OdontoPerfil.PARCEIRO)) {
                    actionLoginRetorno = "clinicas.jsf";
                }
                else {
                    actionLoginRetorno = "home.jsf";
                }
            }
        }

        if (!actionLoginRetorno.equals("")) {
            goToTutorial = false;
            JSFHelper.redirect((goToTutorial ? "tutorial.jsf?next=" : "") + actionLoginRetorno);
        }

        return "";
    }

    private void validarSituacaoIuguEmpresa(Empresa empresa) throws InterruptedException {
        Thread th = new Thread(new Runnable() {
            
            @Override
            public void run() {
                try {
                    EmpresaSingleton.getInstance().carregarStatusFaturas(empresa);
                } catch (Exception e) {
                    LogIntelidenteSingleton.getInstance().makeLog(e);
                    e.printStackTrace();
                }
            }
        });
        
        th.start();
    }
    
    private List<Empresa> carregarEmpresasLogin(Usuario entity) {
        List<Usuario> usuarios = UsuarioSingleton.getInstance().getBo().listUsuarioByLogin(this.getEntity());
        if (usuarios != null && !usuarios.isEmpty()) {
            empresasLogin = new ArrayList<>();
            for (Usuario usuario : usuarios) {
                empresasLogin.add(UtilsFrontEnd.getEmpresaLogada());
            }
        }
        return empresasLogin;
    }

    public void actionResetSenha() {
        try {
            if (this.getEntity().getUsuStrLogin() != null && !this.getEntity().getUsuStrLogin().equals("")) {
                EnviaEmail.envioResetSenha(this.getEntity());
                this.addInfo(Mensagens.getMensagem("lumeSecurity.login.reset.sucesso"), "");
            } else {
                this.addError(Mensagens.getMensagem("lumeSecurity.login.reset.login.obrigatorio"), "");
            }
        } catch (ServidorEmailDesligadoException sed) {
            this.addError(sed.getLocalizedMessage(), "");
            log.error("Erro ao efetuar login.", sed);
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem("lumeSecurity.login.reset.erro"), "");
            log.error("Erro ao efetuar login.", e);
        }
    }

    public String actionLogoff() {
        this.getLumeSecurity().setUsuario(null);
        this.getLumeSecurity().setSistema(null);
        this.getLumeSecurity().setEmpresa(null);
        JSFHelper.getExternalContext().invalidateSession();
        return "login.xhtml";
    }

    public String actionCadastroWeb() {
        return "cadastroWeb.xhtml";
    }

    public String actionCadastroPagamento() {
        return "cadastroPagamento.xhtml";
    }

    public String actionPreCadastro() {
        return "preCadastro.xhtml";
    }

    public String getMsgTrial() {
        //this.getLumeSecurity().getUsuario().getEmpresa(
        if ("S".equals(UtilsFrontEnd.getEmpresaLogada().getEmpChaTrocaPlano())) {
            return this.getLumeSecurity().getUsuario() != null && UtilsFrontEnd.getEmpresaLogada() != null ? "Seu acesso expira no dia " + Utils.dateToString(
                    UtilsFrontEnd.getEmpresaLogada().getEmpDtmExpiracao(), "dd/MM/yyyy") + " - Troque de plano já!" : "";
        } else {
            return this.getLumeSecurity().getUsuario() != null && UtilsFrontEnd.getEmpresaLogada() != null ? "Seu acesso expira no dia " + Utils.dateToString(
                    UtilsFrontEnd.getEmpresaLogada().getEmpDtmExpiracao(), "dd/MM/yyyy") + " - Assine Já!" : "";
        }
    }

    // public List<Empresa> getEmpresasLogin() {
    //     return (List<Empresa>) JSFHelper.getSession().getAttribute("EMPRESAS_LOGIN");
    // }

    public void setEmpresasLogin(List<Empresa> empresasLogin) {
        this.empresasLogin = empresasLogin;
    }

    public Empresa getEmpresaLogin() {
        return empresaLogin;
    }

    public void setEmpresaLogin(Empresa empresaLogin) {
        this.empresaLogin = empresaLogin;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getErroLogin() {
        return erroLogin;
    }

    public void setErroLogin(String erroLogin) {
        this.erroLogin = erroLogin;
    }
}
