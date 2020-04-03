package br.com.lume.security.audit;

import java.util.List;

import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;

import br.com.lume.audit.AuditSingleton;
import br.com.lume.common.log.LogIntelidenteSingleton;
import br.com.lume.common.util.UtilsFrontEnd;

public class LifeCycleListener implements PhaseListener {

    private static final long serialVersionUID = 1L;

    public PhaseId getPhaseId() {
        return PhaseId.INVOKE_APPLICATION;
    }

    public void beforePhase(PhaseEvent event) {
        try {
            LifeCycleListenerUtils.putParamOnContext(event.getFacesContext(), "TimerStart", AuditTimer.StartCount());
        } catch (Exception e) {
            LogIntelidenteSingleton.getInstance().makeLog(e);
        }
    }

    public void afterPhase(PhaseEvent event) {
        try {
            List<String> methods = LifeCycleListenerUtils.getMethodsCallFromPhaseEvent(event.getFacesContext());
            String methodsStr = String.join(", ", methods);
            String hash = LifeCycleListenerUtils.getParamOnContext(event.getFacesContext(), "TimerStart", String.class);
            Long time = AuditTimer.StopCount(hash);
            String currentPage = FacesContext.getCurrentInstance().getViewRoot().getViewId();
            AuditSingleton.getInstance().createAuditReg(currentPage, methodsStr, time, UtilsFrontEnd.getProfissionalLogado(), UtilsFrontEnd.getEmpresaLogada());
        } catch (Exception e) {
            LogIntelidenteSingleton.getInstance().makeLog(e);
        }
    }

}
