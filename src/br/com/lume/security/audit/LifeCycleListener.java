package br.com.lume.security.audit;

import java.util.Map;

import javax.faces.component.UICommand;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;

public class LifeCycleListener implements PhaseListener {

    private static final long serialVersionUID = 1L;

    public PhaseId getPhaseId() {
        return PhaseId.INVOKE_APPLICATION;
    }

    public void beforePhase(PhaseEvent event) {
        System.out.println("INICIANDO FASE: " + event.getPhaseId());
        FacesContext context = event.getFacesContext();
        if (context.isPostback()) {
            UICommand component = findInvokedCommandComponent(context);
            if (component != null) {
                String methodExpression = component.getActionExpression().getExpressionString();
                System.out.println("Method Called: " + methodExpression);
            }
        }
    }

    public void afterPhase(PhaseEvent event) {
        System.out.println("FINALIZANDO FASE: " + event.getPhaseId());
    }

    private UICommand findInvokedCommandComponent(FacesContext context) {
        UIViewRoot view = context.getViewRoot();
        Map<String, String> params = context.getExternalContext().getRequestParameterMap();

        if (context.getPartialViewContext().isAjaxRequest()) {
            return (UICommand) view.findComponent(params.get("javax.faces.source"));
        } else {
            for (String clientId : params.keySet()) {
                UIComponent component = view.findComponent(clientId);

                if (component instanceof UICommand) {
                    return (UICommand) component;
                }
            }
        }

        return null;
    }

}
