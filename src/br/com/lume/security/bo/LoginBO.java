package br.com.lume.security.bo;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import br.com.lume.common.bo.BO;
import br.com.lume.common.connection.GenericDAO;
import br.com.lume.common.exception.business.PlanoExpiradoException;
import br.com.lume.common.exception.business.SenhaInvalidaException;
import br.com.lume.common.exception.business.UsuarioBloqueadoException;
import br.com.lume.common.exception.business.UsuarioSemPagamentoException;
import br.com.lume.common.exception.business.UsuarioSemPerfilException;
import br.com.lume.common.exception.business.UsuarioTrialExpiradoException;
import br.com.lume.common.util.JSFHelper;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.Status;
import br.com.lume.odonto.bo.AgendamentoBO;
import br.com.lume.odonto.bo.PacienteBO;
import br.com.lume.odonto.bo.PlanoBO;
import br.com.lume.odonto.entity.Plano;
import br.com.lume.odonto.entity.Profissional;
import br.com.lume.odonto.iugu.services.Iugu;
import br.com.lume.security.entity.Objeto;
import br.com.lume.security.entity.Sistema;
import br.com.lume.security.entity.Usuario;
import br.com.lume.security.managed.LumeSecurity;

public class LoginBO extends BO<Usuario> {

    /**
     *
     */
    private static final long serialVersionUID = -2615318992329054259L;

    public LoginBO() {
        super(GenericDAO.LUME_SECURITY);
        this.setClazz(Usuario.class);
    }

    public void carregaObjetosPermitidos(Usuario userLogin, String perfil, LumeSecurity lumeSecurity, Profissional profissional) throws Exception {
        ObjetoBO objetoBO = new ObjetoBO();
        lumeSecurity.setUsuario(userLogin);
        List<Objeto> objetosPermitidos = objetoBO.carregaObjetosPermitidos(perfil, profissional);
        Objeto objeto = new Objeto();
        objeto.setObjStrDes("home");
        objeto.setCaminho("home");
        objetosPermitidos.add(objeto);
        lumeSecurity.setObjetosPermitidos(objetosPermitidos);
    }

    public void validaSituacaoEmpresa(Usuario usuario) throws Exception {
        Calendar c = Calendar.getInstance();
        if (usuario.getEmpresa().getEmpChaTrial().equals(Status.SIM) && usuario.getEmpresa().getEmpDtmExpiracao() != null && usuario.getEmpresa().getEmpDtmExpiracao().before(c.getTime())) {
            if (PacienteBO.getPacienteLogado() != null) {
                throw new UsuarioTrialExpiradoException(Mensagens.TRIAL_EXPIRADO_PACIENTE);
            } else {
                throw new UsuarioTrialExpiradoException();
            }
        }
        c = Calendar.getInstance();
        if (usuario.getEmpresa().getEmpChaTrial().equals(Status.NAO)) {
            if (usuario.getEmpresa().getEmpStrAssinaturaIuguID() != null) {
                Date buscarDataExpiracao = Iugu.buscarDataExpiracao(usuario.getEmpresa().getEmpStrAssinaturaIuguID());
                if (buscarDataExpiracao != null) {
                    usuario.getEmpresa().setEmpDtmExpiracao(buscarDataExpiracao);
                    new EmpresaBO().persist(usuario.getEmpresa());
                }
                boolean suspenso = Iugu.isSuspenso(usuario.getEmpresa().getEmpStrAssinaturaIuguID());
                if (suspenso) {
                    throw new UsuarioSemPagamentoException();
                }
            }

            if (usuario.getEmpresa().getEmpDtmExpiracao() != null && usuario.getEmpresa().getEmpDtmExpiracao().before(c.getTime())) {
                throw new UsuarioSemPagamentoException();
            }
        }
        Integer consultasRealizadasMes = new AgendamentoBO().findQuantidadeAgendamentosMesAtual(usuario.getEmpresa().getEmpIntCod());
        Plano plano = new PlanoBO().find(usuario.getEmpresa().getIdPlano());
        if (consultasRealizadasMes != null && plano != null) {
            if (consultasRealizadasMes > plano.getConsultas()) {
                throw new PlanoExpiradoException();
            }
        }
        JSFHelper.getSession().setAttribute("QUANTIDADE_ERROS_ACESSO", 0);
        JSFHelper.getSession().setAttribute("menuMB", null);
    }

    public Usuario doLogin(Usuario usuario, Sistema sistema) throws Exception {
        UsuarioBO usuarioBO = new UsuarioBO();
        new UsuarioBO().doCrypt(usuario, usuario.getUsuStrSenha());
        Usuario usuarioByLogin = usuarioBO.getUsuarioByLogin(usuario);
        if (usuarioByLogin != null) {
            boolean usuarioAtivo = usuarioBO.isUsuarioAtivo(usuario);
            if (usuarioAtivo) {
                usuario = usuarioBO.validaUsuario(usuario);
                if (usuario != null) {
                    usuario = usuarioBO.verificaPerfilUsuario(usuario, sistema);
                    if (usuario == null) {
                        throw new UsuarioSemPerfilException();
                    }
                } else {
                    int qtdErrosAcesso = JSFHelper.getSession().getAttribute("QUANTIDADE_ERROS_ACESSO") != null ? (Integer) JSFHelper.getSession().getAttribute("QUANTIDADE_ERROS_ACESSO") : 0;
                    JSFHelper.getSession().setAttribute("QUANTIDADE_ERROS_ACESSO", qtdErrosAcesso + 1);
                    throw new SenhaInvalidaException();
                }
            } else {
                throw new UsuarioBloqueadoException();
            }
        } else {
            throw new SenhaInvalidaException();
        }
        return usuario;
    }
}
