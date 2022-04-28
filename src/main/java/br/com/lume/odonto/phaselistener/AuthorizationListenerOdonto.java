package br.com.lume.odonto.phaselistener;

import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.servlet.http.HttpServletResponse;

import br.com.lume.common.phaselistener.AuthorizationListener;
import br.com.lume.common.util.JSFHelper;

public class AuthorizationListenerOdonto extends AuthorizationListener {

    private static final long serialVersionUID = 1L;

    public static final String[] PAGINAS_SEM_RESTRICAO = new String[] { "confirmacao.", "cadastroWeb.", "login.", "retorno.", "cadastroPagamento.", "preCadastro." };

    @Override
    public void beforePhase(PhaseEvent event) {
        String currentPage = FacesContext.getCurrentInstance().getViewRoot().getViewId();
        if (!this.isPaginaSemRestricao(currentPage)) {
            super.beforePhase(event);
        }
        HttpServletResponse response = JSFHelper.getResponse();
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

    @Override
    public void afterPhase(PhaseEvent arg0) {
    }

    @Override
    public PhaseId getPhaseId() {
        return PhaseId.RENDER_RESPONSE;
    }
}
