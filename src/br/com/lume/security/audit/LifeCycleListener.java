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
            // Começo o cronômetro e armazeno o Start no contexto da requisição
            LifeCycleListenerUtils.putParamOnContext(event.getFacesContext(), "TimerStart", AuditTimer.StartCount());
        } catch (Exception e) {
            LogIntelidenteSingleton.getInstance().makeLog(e);
        }
    }

    public void afterPhase(PhaseEvent event) {
        try {
            // Guardo o contexto da requisição em variável local
            FacesContext context = event.getFacesContext();
            
            // Busco o identificador do Start salvo no beforePhase() e calculo o tempo de execução
            String hash = LifeCycleListenerUtils.getParamOnContext(context, "TimerStart", String.class);
            Long time = AuditTimer.StopCount(hash);
            
            // Listo os métodos envolvidos na chamada e coloco-os em string
            List<String> methods = LifeCycleListenerUtils.getMethodsCallFromPhaseEvent(event.getFacesContext());
            String methodsStr = String.join(", ", methods);
            
            // Registro os dados em banco
            AuditSingleton.getInstance().createAuditReg(context, methodsStr, time, UtilsFrontEnd.getProfissionalLogado());
        } catch (Exception e) {
            LogIntelidenteSingleton.getInstance().makeLog(e);
        }
    }

}
