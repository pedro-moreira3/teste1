package br.com.lume.security.bo;

import java.net.UnknownHostException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.mail.MessagingException;
import javax.persistence.Query;

import org.apache.log4j.Logger;

import br.com.lume.common.bo.BO;
import br.com.lume.common.connection.GenericDAO;
import br.com.lume.common.exception.business.SenhaInvalidaException;
import br.com.lume.common.exception.business.ServidorEmailDesligadoException;
import br.com.lume.common.exception.business.UsuarioBloqueadoException;
import br.com.lume.common.exception.business.UsuarioDuplicadoException;
import br.com.lume.common.util.CryptMD5;
import br.com.lume.common.util.EnviaEmail;
import br.com.lume.common.util.GeradorSenha;
import br.com.lume.common.util.Status;
import br.com.lume.security.entity.Empresa;
import br.com.lume.security.entity.Perfil;
import br.com.lume.security.entity.Sistema;
import br.com.lume.security.entity.Usuario;

public class UsuarioBO extends BO<Usuario> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(UsuarioBO.class);

    public UsuarioBO() {
        super(GenericDAO.LUME_SECURITY);
        this.setClazz(Usuario.class);
    }

    public boolean isSenhaExpirada(Usuario usuario) {
        long usuDtmUlttrocasenha = usuario.getUsuDtmUlttrocasenha().getTime();
        long hoje = Calendar.getInstance().getTimeInMillis();
        int totalEmDias = (int) ((hoje - usuDtmUlttrocasenha) / 86400000);
        return (usuario.getUsuIntDiastrocasenha() < totalEmDias);
    }

    public void doCrypt(Usuario usuario, String senha) {
        senha = CryptMD5.encrypt(senha);
        usuario.setUsuStrSenha(senha);
    }

    public static void main(String[] args) {
        try {
            Usuario usuario = new UsuarioBO().findFirstUserByEmpresa(11);
            System.out.println(usuario.getUsuStrNme());
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public Usuario loginCrypt(String usuStrLogin, String usuStrSenha) throws Exception {
        Usuario usu = buscarPorLogin(usuStrLogin);
        if (usu == null) {
            throw new UsuarioBloqueadoException();
        } else if (usuStrSenha == null || !usu.getUsuStrSenha().equals(usuStrSenha)) {
            throw new SenhaInvalidaException();
        }
        return usu;
    }

    public Usuario buscarPorLogin(String usuStrLogin) throws Exception {
        Map<String, Object> param = new HashMap<>();
        param.put("usuStrLogin", usuStrLogin.toUpperCase().trim());
        return super.findByFields(param);
    }

    public Usuario findFirstUserByEmpresa(long empIntCod) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT ");
            sb.append("USU.* ");
            sb.append("FROM PROFISSIONAL PROF,SEG_USUARIO USU ");
            sb.append("WHERE PROF.ID_EMPRESA = ?1 AND ");
            sb.append("PROF.ID_USUARIO = USU.USU_INT_COD ");
            sb.append("ORDER BY PROF.ID ASC FETCH FIRST 1 ROWS ONLY ");
            Query query = this.getDao().createNativeQuery(sb.toString(), Usuario.class);
            query.setParameter(1, empIntCod);
            List<Usuario> list = this.list(query);
            return list != null && !list.isEmpty() ? list.get(0) : null;
        } catch (Exception e) {
            log.error("Erro no findFirstUserByEmpresa", e);
        }
        return null;
    }

    public String resetSenha(Usuario usuario) throws Exception {
        usuario = this.getUsuarioByLogin(usuario);
        String senha = "";
        if (usuario != null && usuario.getUsuStrLogin() != null && !usuario.getUsuStrLogin().equals("")) {
            try {
                senha = this.doSenhaPadraoReset(usuario);
            } catch (MessagingException me) {
                if (me.getCause() instanceof UnknownHostException) {
                    throw new ServidorEmailDesligadoException();
                }
            }
            this.persist(usuario);
        }
        return senha;
    }

    public String doSenhaPadrao(Usuario usuario) throws Exception {
        String senhaPadrao = GeradorSenha.gerarSenha();
        this.doCrypt(usuario, senhaPadrao);
        usuario.setUsuDtmUlttrocasenha(new Timestamp(Calendar.getInstance().getTimeInMillis()));
        if (usuario.getUsuStrEml() != null && !usuario.getUsuStrEml().equals("")) {
            try {
                this.enviarEmailComSenhaPadrao(usuario, senhaPadrao);
            } catch (Exception e) {
                log.error("Erro ao enviar senha padrao : " + senhaPadrao, e);
            }
        }
        return senhaPadrao;
    }

    public String doSenhaPadraoReset(Usuario usuario) throws Exception {
        String senhaPadrao = GeradorSenha.gerarSenha();
        this.doCrypt(usuario, senhaPadrao);
        usuario.setUsuDtmUlttrocasenha(new Timestamp(Calendar.getInstance().getTimeInMillis()));
        if (usuario.getUsuStrEml() != null && !usuario.getUsuStrEml().equals("")) {
            this.enviarEmailComSenhaPadraoReset(usuario, senhaPadrao);
        }
        return senhaPadrao;
    }

    public void enviarEmailComSenhaPadraoReset(Usuario usuario, String senhaPadrao) throws Exception {
        Map<String, String> valores = new HashMap<>();
        valores.put("#nome", usuario.getUsuStrNme());
        valores.put("#usuario", usuario.getUsuStrLogin());
        valores.put("#senha", senhaPadrao);
        EnviaEmail.enviaEmail(usuario.getUsuStrEml(), "Solicitação de troca de senha", EnviaEmail.buscarTemplate(valores, EnviaEmail.RESET));
    }

    public void enviarEmailComSenhaPadrao(Usuario usuario, String senhaPadrao) throws Exception {
        if (this.isPaciente(usuario)) {
            enviarEmailPacienteComSenhaPadrao(usuario, senhaPadrao);
        } else {
            enviarEmailProfissionalComSenhaPadrao(usuario, senhaPadrao);
        }
    }

    public void enviarEmailPacienteComSenhaPadrao(Usuario usuario, String senhaPadrao) throws Exception {
        Map<String, String> valores = new HashMap<>();
        valores.put("#nome", usuario.getUsuStrNme());
        valores.put("#usuario", usuario.getUsuStrLogin());
        valores.put("#senha", senhaPadrao);
        valores.put("#nomeclinica", usuario.getEmpresa().getEmpStrNme());
        valores.put("#telclinica", usuario.getEmpresa().getEmpChaFone());
        valores.put("#emailclinica", usuario.getEmpresa().getEmpStrEmail());
        EnviaEmail.enviaEmail(usuario.getUsuStrEml(), "Bem Vindo ao Intelidente", EnviaEmail.buscarTemplate(valores, EnviaEmail.ACESSO_PACIENTE));
    }

    public void enviarEmailProfissionalComSenhaPadrao(Usuario usuario, String senhaPadrao) throws Exception {
        Map<String, String> valores = new HashMap<>();
        valores.put("#nome", usuario.getUsuStrNme());
        valores.put("#usuario", usuario.getUsuStrLogin());
        valores.put("#senha", senhaPadrao);
        EnviaEmail.enviaEmail(usuario.getUsuStrEml(), "Bem Vindo ao Intelidente", EnviaEmail.buscarTemplate(valores, EnviaEmail.BEM_VINDO));
    }

    private boolean isPaciente(Usuario usuario) {
        List<Perfil> perfis = usuario.getPerfisUsuarios();
        for (Perfil perfil : perfis) {
            if (perfil.getPerStrDes().contains(Perfil.PACIENTE)) {
                return true;
            }
        }
        return false;
    }

    public List<Usuario> getAllUsuariosByEmpresa(Empresa empresa) {
        List<Usuario> usuarios = new ArrayList<>();
        if (empresa != null) {
            try {
                String jpql = "select u from usuario u where u.empresa.empIntCod=:empIntCod order by u.usuStrNme";
                Query query = this.getDao().createQuery(jpql);
                query.setParameter("empIntCod", empresa.getEmpIntCod());
                usuarios = this.list(query);
            } catch (Exception e) {
                log.error("Erro no getAllUsuariosByEmpresa", e);
            }
        }
        return usuarios;
    }

    public Usuario findUsuarioByLogin(String usuStrLogin) {
        Usuario usuario = new Usuario();
        usuario.setUsuStrLogin(usuStrLogin);
        return this.getUsuarioByLogin(usuario);
    }

    public Usuario findByEmail(String email) {
        try {
            String jpql = "select distinct(u) from Usuario u where u.usuStrEml=:usuStrEml order by u.usuIntCod desc";
            Query query = this.getDao().createQuery(jpql);
            query.setParameter("usuStrEml", email);
            List<Usuario> users = this.list(query);
            return users != null && !users.isEmpty() ? users.get(0) : null;
        } catch (Exception e) {
            log.error("Erro no getUsuarioByLogin", e);
        }
        return null;
    }

    public List<Usuario> listUsuarioByLogin(Usuario usuario) {
        if (usuario != null) {
            try {
                String jpql = "select distinct(u) from Usuario u where u.usuStrLogin=:usuStrLogin";
                Query query = this.getDao().createQuery(jpql);
                query.setParameter("usuStrLogin", usuario.getUsuStrLogin().toUpperCase());
                List<Usuario> users = this.list(query);
                return users;
            } catch (Exception e) {
                log.error("Erro no getUsuarioByLogin", e);
            }
        }
        return null;
    }

    public Usuario getUsuarioByLogin(Usuario usuario) {
        if (usuario != null) {
            try {
                String jpql = "select distinct(u) from Usuario u where upper(u.usuStrLogin)=:usuStrLogin ";
                Query query = this.getDao().createQuery(jpql);
                query.setParameter("usuStrLogin", usuario.getUsuStrLogin().toUpperCase().trim());
                List<Usuario> users = this.list(query);
                if (users != null && users.size() > 0) {
                    return users.get(0);
                }
            } catch (Exception e) {
                log.error("Erro no getUsuarioByLogin", e);
            }
        }
        return null;
    }

    public Usuario getUsuarioByLoginAndEmpresa(Usuario usuario) {
        if (usuario != null) {
            try {
                String jpql = "select distinct(u) from Usuario u where u.usuStrLogin=:usuStrLogin and u.empresa=:empresa";
                Query query = this.getDao().createQuery(jpql);
                query.setParameter("usuStrLogin", usuario.getUsuStrLogin());
                query.setParameter("empresa", usuario.getEmpresa());
                List<Usuario> users = this.list(query);
                if (users != null && users.size() > 0) {
                    return users.get(0);
                }
            } catch (Exception e) {
                log.error("Erro no getUsuarioByLogin", e);
            }
        }
        return null;
    }

    public Usuario validaUsuario(Usuario usuario) {
        if (usuario != null) {
            try {
                String jpql = "select distinct(u) from Usuario u where u.usuStrLogin=:usuStrLogin and u.usuStrSenha=:usuStrSenha and u.usuChaSts='A' ";
                Query query = this.getDao().createQuery(jpql);
                query.setParameter("usuStrLogin", usuario.getUsuStrLogin());
                query.setParameter("usuStrSenha", usuario.getUsuStrSenha());
                List<Usuario> users = this.list(query);
                if (users != null && users.size() > 0) {
                    return users.get(0);
                }
            } catch (Exception e) {
                log.error("Erro no validaUsuario", e);
            }
        }
        return null;
    }

    public boolean isUsuarioAtivo(Usuario usuario) {
        if (usuario != null) {
            try {
                String jpql = "select distinct(u) from Usuario u where u.usuStrLogin=:usuStrLogin and u.usuChaSts='A' ";
                Query query = this.getDao().createQuery(jpql);
                query.setParameter("usuStrLogin", usuario.getUsuStrLogin());
                List<Usuario> users = this.list(query);
                if (users != null && users.size() > 0) {
                    return true;
                }
            } catch (Exception e) {
                log.error("Erro no validaUsuario", e);
            }
        }
        return false;
    }

    public Usuario verificaPerfilUsuario(Usuario usuario, Sistema sistema) {
        if (usuario != null) {
            try {
                String jpql = "select distinct(u) from Usuario u JOIN u.perfisUsuarios pu where " + "u.usuIntCod=:usuIntCod and " + "u.usuChaSts='A' and " + "pu.sistema.sisIntCod=:sisIntCod  ";
                Query query = this.getDao().createQuery(jpql);
                query.setParameter("usuIntCod", usuario.getUsuIntCod());
                query.setParameter("sisIntCod", sistema.getSisIntCod());
                List<Usuario> users = this.list(query);
                if (users != null && users.size() > 0) {
                    return users.get(0);
                }
            } catch (Exception e) {
                log.error("Erro no verificaPerfilUsuario", e);
            }
        }
        return null;
    }

    public List<Usuario> getUsuariosByPesquisa(Empresa empresa, String nomeUsuario, String loginUsuario) {
        List<Usuario> usuarios = null;
        if (empresa != null) {
            try {
                StringBuilder jpql = new StringBuilder();
                jpql.append("select u from Usuario u ");
                jpql.append("  where u.empresa.empIntCod=:empIntCod ");
                if ((nomeUsuario != null && !nomeUsuario.trim().isEmpty()) || loginUsuario != null && !loginUsuario.trim().isEmpty()) {
                    jpql.append("  and ");
                }
                if (nomeUsuario != null && !nomeUsuario.trim().isEmpty()) {
                    jpql.append("  UPPER(u.usuStrNme) like '%" + nomeUsuario.toUpperCase() + "%'");
                }
                if ((nomeUsuario != null && !nomeUsuario.trim().isEmpty()) && loginUsuario != null && !loginUsuario.trim().isEmpty()) {
                    jpql.append("  and ");
                }
                if (loginUsuario != null && !loginUsuario.trim().isEmpty()) {
                    jpql.append("  UPPER(u.usuStrLogin) like '%" + loginUsuario.toUpperCase() + "%'");
                }
                jpql.append("  order by u.usuStrNme");
                Query query = this.getDao().createQuery(jpql.toString());
                query.setParameter("empIntCod", empresa.getEmpIntCod());
                usuarios = this.list(query);
            } catch (Exception e) {
                log.error("Erro no getUsuariosByPesquisa", e);
            }
        }
        return usuarios;
    }

    public String persistUsuarioExterno(Usuario usuario) throws UsuarioDuplicadoException, ServidorEmailDesligadoException, Exception {
        String senha = "";
//        Usuario userAux = getUsuarioByLoginAndEmpresa(usuario);
//        if (userAux != null && userAux.getUsuIntCod() != usuario.getUsuIntCod()) {
//            throw new UsuarioDuplicadoException();
//        }
        if (usuario.getUsuIntCod() <= 0) {
            try {
                senha = this.doSenhaPadrao(usuario);
            } catch (MessagingException me) {
                if (me.getCause() instanceof UnknownHostException) {
                    throw new ServidorEmailDesligadoException();
                }
            }
        }
        usuario.setUsuIntDiastrocasenha(30);
        usuario.setUsuChaInlogacesso(Status.SIM);
        usuario.setUsuChaSts(Status.ATIVO);
        usuario.setUsuChaAdm(Status.NAO);
        this.persist(usuario);
        return senha;
    }

    public void alterarEmailUsuario(Usuario usuarioAtual, String email) throws Exception {
        usuarioAtual.setUsuStrEml(email);
        usuarioAtual.setUsuStrLogin(email);
        doSenhaPadrao(usuarioAtual);
        persist(usuarioAtual);
    }
}
