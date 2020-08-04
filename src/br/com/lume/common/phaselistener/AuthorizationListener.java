package br.com.lume.common.phaselistener;

import java.util.Date;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import br.com.lume.common.util.JSFHelper;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.security.LogAcessoSingleton;
import br.com.lume.security.ObjetoSingleton;
import br.com.lume.security.SistemaSingleton;
//import br.com.lume.security.bo.LogAcessoBO;
//import br.com.lume.security.bo.ObjetoBO;
//import br.com.lume.security.bo.SistemaBO;
import br.com.lume.security.entity.LogAcesso;
import br.com.lume.security.entity.Objeto;
import br.com.lume.security.entity.Sistema;
import br.com.lume.security.entity.Usuario;
import br.com.lume.security.managed.LumeSecurity;

public class AuthorizationListener implements PhaseListener {

    private static final long serialVersionUID = -8237087853801435858L;

    private Logger log = Logger.getLogger(AuthorizationListener.class);

    
    //TODO por enquanto coloquei as clincias dos parceiros aqui
    public static final String[] PAGINAS_SEM_RESTRICAO = new String[] { "chat.", "meuplano.", "confirmacao.", "cadastroWeb.", "login.", "retornos.", 
            "cadastroPagamento.", "preCadastro.", "motivo.", "cadastroPagamentoDesenv.", "loginmulti.", "tutorial.","validaremail.","clinicas." };

    @Override
    public void beforePhase(PhaseEvent event) {
        HttpServletResponse response = JSFHelper.getResponse();
        LumeSecurity lumeSecurity = (LumeSecurity) JSFHelper.getSession().getAttribute("lumeSecurity");
        Usuario usuarioLogado = lumeSecurity != null ? lumeSecurity.getUsuario() : null;
        String currentPage = FacesContext.getCurrentInstance().getViewRoot().getViewId();
        log.debug("Usu�rio Logado : " + (usuarioLogado != null ? usuarioLogado.getUsuStrNme() : "null") + ". Acessando a tela: " + currentPage);
        if (currentPage.contains("/")) {
            String[] diretorios = currentPage.split("/");
            currentPage = diretorios[diretorios.length - 1];
        }
        if (currentPage.contains("xhtml")) {
            currentPage = currentPage.replaceAll("xhtml", "jsf");
        }
        
        // Tentou entrar em uma pagina deslogado || entrou em uma pagina logado
        // mas que nao esta autorizado
        if (!this.isPaginaSemRestricao(currentPage)) {
            if(UtilsFrontEnd.getProfissionalLogado() == null && UtilsFrontEnd.getPacienteLogado() == null && UtilsFrontEnd.getAfiliacaoLogada() == null)
                JSFHelper.redirect("login.jsf");
            
            Objeto objetoAtual = this.isPageDenied(currentPage, usuarioLogado);
            if ((!currentPage.contains("sobre") && !currentPage.contains(
                    "login") && usuarioLogado == null) || (!currentPage.contains("sobre") && !currentPage.contains("login") && usuarioLogado != null && objetoAtual == null)) {
                try {
                    log.debug("Redirecionou da pagina : " + currentPage + " UserLogado : " + usuarioLogado + ". Acessando a tela: " + currentPage);
                    JSFHelper.redirect("login.jsf");
                } catch (Exception ex) {
                    log.error("Erro no beforePhase", ex);
                }
            }
            if (lumeSecurity != null) {
                lumeSecurity.setObjetoAtual(objetoAtual);
            }
        }
        
        if (lumeSecurity != null) {
            lumeSecurity.setPaginaAtual(currentPage);
            this.logarAcesso(currentPage, usuarioLogado, JSFHelper.getRequest().getRemoteAddr());
        }
        response.setHeader("Expires", "-1");
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidade, proxy-revalidade, private, post-check=0, pre-check=0");
        response.setHeader("Pragma", "no-cache");
    }

    private boolean isPaginaSemRestricao(String currentPage) {
        for (String pagina : PAGINAS_SEM_RESTRICAO) {
            if (currentPage.contains(pagina)) {
                return true;
            }
        }
        return false;
    }

    private Objeto isPageDenied(String currentPage, Usuario usuarioLogado) {
        LumeSecurity ls = ((LumeSecurity) JSFHelper.getSession().getAttribute("lumeSecurity"));
        if (ls != null) {
            List<Objeto> objetos = ls.getObjetosPermitidos();
            if (objetos != null) {
                for (Objeto objeto : objetos) {
                    if (currentPage != null && currentPage.contains(objeto.getCaminho())) {
                        return objeto;
                    }
                }
            }
        }
        return null;
    }
    
    private void logarAcesso(String page, Usuario usuario, String ip) {
        String paginaAnterior = (String) JSFHelper.getSession().getAttribute("PAGINA_ANTERIOR");
        if (!page.equals(paginaAnterior)) {
            if (usuario != null) {
                Sistema sistema = SistemaSingleton.getInstance().getBo().getSistemaBySigla(JSFHelper.getSistemaAtual());
                Objeto objeto = ObjetoSingleton.getInstance().getBo().getObjetoByCaminhoAndSistema(page, sistema);
                if (objeto != null) {
                    LogAcesso logAcesso = new LogAcesso();
                    logAcesso.setObjeto(objeto);
                    logAcesso.setLogDtmEntrada(new Date());
                    logAcesso.setUsuario(usuario);
                    logAcesso.setLogStrIp(ip);
                    try {
                        LogAcessoSingleton.getInstance().getBo().persist(logAcesso);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            JSFHelper.getSession().setAttribute("PAGINA_ANTERIOR", page);
        }
    }    

    @Override
    public void afterPhase(PhaseEvent arg0) {
        // TODO Auto-generated method stub
    }

    @Override
    public PhaseId getPhaseId() {
        return PhaseId.RENDER_RESPONSE;
    }
}
