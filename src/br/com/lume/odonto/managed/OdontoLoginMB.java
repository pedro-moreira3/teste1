package br.com.lume.odonto.managed;

import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.apache.log4j.Logger;
import org.primefaces.PrimeFaces;

import br.com.lume.common.exception.business.ServidorEmailDesligadoException;
import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.JSFHelper;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.Utils;
import br.com.lume.odonto.bo.PacienteBO;
import br.com.lume.odonto.bo.ProfissionalBO;
import br.com.lume.odonto.entity.OdontoPerfil;
import br.com.lume.odonto.entity.Paciente;
import br.com.lume.odonto.entity.Profissional;
import br.com.lume.security.bo.EmpresaBO;
import br.com.lume.security.bo.LoginBO;
import br.com.lume.security.bo.SistemaBO;
import br.com.lume.security.bo.UsuarioBO;
import br.com.lume.security.entity.Empresa;
import br.com.lume.security.entity.Login;
import br.com.lume.security.entity.Sistema;
import br.com.lume.security.entity.Usuario;

@ManagedBean
@ViewScoped
public class OdontoLoginMB extends LumeManagedBean<Usuario> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(OdontoLoginMB.class);

    private List<Empresa> empresasLogin;

    private Empresa empresaLogin;

    private UsuarioBO usuarioBO;

    private EmpresaBO empresaBO;

    private ProfissionalBO profissionalBO;

    private PacienteBO pacienteBO;

    private String login;

    private SistemaBO sistemaBO;

    private Sistema sistema;

    public OdontoLoginMB() {
        super(new LoginBO());
        this.setClazz(Usuario.class);
        profissionalBO = new ProfissionalBO();
        pacienteBO = new PacienteBO();
        empresaBO = new EmpresaBO();
        usuarioBO = new UsuarioBO();
        sistemaBO = new SistemaBO();
        sistema = sistemaBO.getSistemaBySigla(JSFHelper.getSistemaAtual());
    }

    public String actionLogin() {
        try {
            JSFHelper.getSession().invalidate();
            Usuario userLogin = ((LoginBO) this.getbO()).doLogin(this.getEntity(), sistema);
            Usuario usuario = usuarioBO.findUsuarioByLogin(userLogin.getUsuStrLogin());
            List<Profissional> profissionais = profissionalBO.listByUsuario(usuario);
            List<Paciente> pacientes = pacienteBO.listByUsuario(usuario);
            // Se tiver o mesmo login em profissionais e pacientes, ou repetidos na mesma lista
            if ((profissionais != null && profissionais.size() > 1) || (pacientes != null && pacientes.size() > 1) || (profissionais != null && profissionais.size() == 1 && pacientes != null && pacientes.size() == 1)) {
                List<Login> logins = this.carregarLogins(pacientes, profissionais);
                JSFHelper.getSession().setAttribute("LOGINS", logins);
                JSFHelper.getSession().setAttribute("USUARIO_NOME", usuario.getUsuStrNme());
                JSFHelper.redirect("loginmulti.jsf");
                return "";
            } else {
                String perfilLogado = "";
                Profissional profissional = profissionalBO.findByUsuario(userLogin);
                if (profissional == null) {
                    perfilLogado = OdontoPerfil.PACIENTE;
                    Paciente paciente = pacienteBO.findByUsuario(userLogin);
                    JSFHelper.getSession().setAttribute("PACIENTE_LOGADO", paciente);
                    JSFHelper.getSession().setAttribute("PERFIL_LOGADO", OdontoPerfil.PACIENTE);
                    JSFHelper.getSession().setAttribute("EMPRESA_LOGADA", empresaBO.find(paciente.getIdEmpresa()));
                } else {
                    if (!Profissional.INATIVO.equals(profissional.getStatus())) {
                        perfilLogado = profissional.getPerfil();
                        JSFHelper.getSession().setAttribute("PROFISSIONAL_LOGADO", profissional);
                        JSFHelper.getSession().setAttribute("PERFIL_LOGADO", perfilLogado);
                        JSFHelper.getSession().setAttribute("EMPRESA_LOGADA", empresaBO.find(profissional.getIdEmpresa()));
                    } else {
                        PrimeFaces.current().executeScript("PF('loading').hide();");
                        this.addError("Profissional Inativo.", "");
                        return "";
                    }
                }
                ((LoginBO) this.getbO()).validaSituacaoEmpresa(this.getEntity());
                ((LoginBO) this.getbO()).carregaObjetosPermitidos(userLogin, perfilLogado, this.getLumeSecurity(), profissional);
            }
        } catch (Exception e) {
            PrimeFaces.current().executeScript("PF('loading').hide();");
            this.addError(e.getMessage(), "");
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
            if (!OdontoPerfil.PACIENTE.equals(perfil)) {
                profissional = profissionalBO.find(Long.parseLong(id));

                if (!Profissional.INATIVO.equals(profissional.getStatus())) {
                    perfilLogado = profissional.getPerfil();
                    userLogin = usuarioBO.find(profissional.getIdUsuario());
                    JSFHelper.getSession().setAttribute("PROFISSIONAL_LOGADO", profissional);
                    JSFHelper.getSession().setAttribute("PERFIL_LOGADO", perfilLogado);
                    JSFHelper.getSession().setAttribute("EMPRESA_LOGADA", empresaBO.find(profissional.getIdEmpresa()));
                } else {
                    this.addError("Profissional Inativo.", "");
                    return "";
                }
            } else {
                perfilLogado = OdontoPerfil.PACIENTE;
                Paciente paciente = pacienteBO.find(Long.parseLong(id));
                userLogin = usuarioBO.find(paciente.getIdUsuario());
                JSFHelper.getSession().setAttribute("PACIENTE_LOGADO", paciente);
                JSFHelper.getSession().setAttribute("PERFIL_LOGADO", OdontoPerfil.PACIENTE);
                JSFHelper.getSession().setAttribute("EMPRESA_LOGADA", empresaBO.find(paciente.getIdEmpresa()));
            }
            ((LoginBO) this.getbO()).validaSituacaoEmpresa(this.getEntity());
            ((LoginBO) this.getbO()).carregaObjetosPermitidos(userLogin, perfilLogado, this.getLumeSecurity(), profissional);
        } catch (Exception e) {
            this.addError(e.getMessage(), "");
            log.error("Erro ao efetuar login.", e);
            return "";
        }
        return this.verificaPaginaInicial();
    }

    private List<Login> carregarLogins(List<Paciente> pacientes, List<Profissional> profissionais) {
        List<Login> logins = new ArrayList<>();
        if (pacientes != null && !pacientes.isEmpty()) {
            for (Paciente paciente : pacientes) {
                try {
                    String empStrNme = empresaBO.find(paciente.getIdEmpresa()).getEmpStrNme();
                    logins.add(new Login(OdontoPerfil.PACIENTE, paciente.getId(), paciente.getIdEmpresa(), "Paciente da clínica " + empStrNme));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        if (profissionais != null && !profissionais.isEmpty()) {
            for (Profissional profissional : profissionais) {
                try {
                    String empStrNme = empresaBO.find(profissional.getIdEmpresa()).getEmpStrNme();
                    logins.add(new Login(profissional.getPerfil(), profissional.getId(), profissional.getIdEmpresa(), "Profissional da clínica " + empStrNme));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return logins;
    }

    public List<Login> getLogins() {
        return (List<Login>) JSFHelper.getSession().getAttribute("LOGINS");
    }

    public String getUsuarioNome() {
        return (String) JSFHelper.getSession().getAttribute("USUARIO_NOME");
    }

    private String verificaPaginaInicial() {
        Profissional profissional = (Profissional) JSFHelper.getSession().getAttribute("PROFISSIONAL_LOGADO");
        Paciente paciente = (Paciente) JSFHelper.getSession().getAttribute("PACIENTE_LOGADO");
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

    private List<Empresa> carregarEmpresasLogin(Usuario entity) {
        List<Usuario> usuarios = usuarioBO.listUsuarioByLogin(this.getEntity());
        if (usuarios != null && !usuarios.isEmpty()) {
            empresasLogin = new ArrayList<>();
            for (Usuario usuario : usuarios) {
                empresasLogin.add(usuario.getEmpresa());
            }
        }
        return empresasLogin;
    }

    public void actionResetSenha() {
        try {
            if (this.getEntity().getUsuStrLogin() != null && !this.getEntity().getUsuStrLogin().equals("")) {
                new UsuarioBO().resetSenha(this.getEntity());
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
        if ("S".equals(this.getLumeSecurity().getUsuario().getEmpresa().getEmpChaTrocaPlano())) {
            return this.getLumeSecurity().getUsuario() != null && this.getLumeSecurity().getUsuario().getEmpresa() != null ? "Seu acesso expira no dia " + Utils.dateToString(
                    this.getLumeSecurity().getUsuario().getEmpresa().getEmpDtmExpiracao(), "dd/MM/yyyy") + " - Troque de plano já!" : "";
        } else {
            return this.getLumeSecurity().getUsuario() != null && this.getLumeSecurity().getUsuario().getEmpresa() != null ? "Seu acesso expira no dia " + Utils.dateToString(
                    this.getLumeSecurity().getUsuario().getEmpresa().getEmpDtmExpiracao(), "dd/MM/yyyy") + " - Assine Já!" : "";
        }
    }

    public List<Empresa> getEmpresasLogin() {
        return (List<Empresa>) JSFHelper.getSession().getAttribute("EMPRESAS_LOGIN");
    }

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
}
