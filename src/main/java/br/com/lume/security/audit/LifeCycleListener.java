package br.com.lume.security.audit;

import java.util.List;

import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;

import br.com.lume.common.log.LogIntelidenteSingleton;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.security.audit.object.AuditInfo;
import br.com.lume.security.audit.threads.AuditRegisterThread;
import br.com.lume.security.audit.utils.ReqUtils;

public class LifeCycleListener implements PhaseListener {

    private static final long serialVersionUID = 1L;

    public PhaseId getPhaseId() {
        return PhaseId.ANY_PHASE;
    }

    public void beforePhase(PhaseEvent event) {
        try {
            // Começo o cronômetro e armazeno o Start no contexto da requisição
            LifeCycleListenerUtils.putParamOnContext(event.getFacesContext(), "TimerStart-" + event.getPhaseId().getOrdinal(), AuditTimer.StartCount());
        } catch (Exception e) {
            LogIntelidenteSingleton.getInstance().makeLog(e);
        }
    }

    public void afterPhase(PhaseEvent event) {
        //String countProcessAfterPhase = AuditTimer.StartCount();

        try {
            // Busco o identificador do Start salvo no beforePhase() e calculo o tempo de execução
            String hash = LifeCycleListenerUtils.getParamOnContext(event.getFacesContext(), "TimerStart-" + event.getPhaseId().getOrdinal(), String.class);
            Long time = AuditTimer.StopCount(hash);

            // Listo os métodos envolvidos na chamada e coloco-os em string
            List<String> methods = LifeCycleListenerUtils.getMethodsCallFromPhaseEvent(event.getFacesContext());
            String methodsStr = String.join(", ", methods);

            String tela = null, host = null, browser = null, os = null;
            try {
                tela = ReqUtils.getAddressURLFromContext(event.getFacesContext());
                host = ReqUtils.getIPFromReq(ReqUtils.getRequestFromContext(event.getFacesContext()));
                browser = ReqUtils.getBrowserFromReq(ReqUtils.getRequestFromContext(event.getFacesContext()));
                os = ReqUtils.getOSFromReq(ReqUtils.getRequestFromContext(event.getFacesContext()));
            } catch (Exception e) {
                LogIntelidenteSingleton.getInstance().makeLog(e);
            }

            AuditInfo info = new AuditInfo(methodsStr, time, event.getPhaseId(), tela, host, browser, os, UtilsFrontEnd.getProfissionalLogado());
            AuditRegisterThread.addInfo2Queue(info);
        } catch (Exception e) {
            LogIntelidenteSingleton.getInstance().makeLog(e);
        }

        //System.out.println("Tempo de afterPhase: " + AuditTimer.StopCount(countProcessAfterPhase) + "ms");
    }

}
