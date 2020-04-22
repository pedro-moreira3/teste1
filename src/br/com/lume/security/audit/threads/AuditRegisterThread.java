package br.com.lume.security.audit.threads;

import java.util.ArrayList;
import java.util.List;

import br.com.lume.audit.AuditSingleton;
import br.com.lume.common.log.LogIntelidenteSingleton;
import br.com.lume.security.audit.object.AuditInfo;

public class AuditRegisterThread extends Thread {

    private static List<AuditInfo> infos = new ArrayList<>();

    public AuditRegisterThread() {
    }

    @Override
    public void run() {
        super.run();

        while (true) {
            try {
                // Registro os dados em banco
                List<AuditInfo> audits2Process = new ArrayList<>(infos);
                if (audits2Process != null && !audits2Process.isEmpty()) {
                    List<AuditInfo> audits2RemoveFromQueue = new ArrayList<>();
                    for (AuditInfo auditInfo : audits2Process) {
                        AuditSingleton.getInstance().createAuditReg(auditInfo.getMethods(), auditInfo.getTimeExecution(),
                                auditInfo.getPhaseId().getName() + " - " + auditInfo.getPhaseId().getOrdinal(), auditInfo.getTela(), auditInfo.getHost(), auditInfo.getBrowser(),
                                auditInfo.getBrowser(), auditInfo.getProfissional());
                        audits2RemoveFromQueue.add(auditInfo);
                    }
                    if (audits2RemoveFromQueue != null)
                        for (AuditInfo audit : audits2RemoveFromQueue)
                            removeInfoFromQueue(audit);
                }
            } catch (Exception e) {
                LogIntelidenteSingleton.getInstance().makeLog(e);
            }

            sleepThread(500);
        }
    }
    
    private void sleepThread(long millis) {
        try {
            Thread.sleep(millis);
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    private static void removeInfoFromQueue(AuditInfo info) {
        if (infos != null && infos.indexOf(info) >= 0)
            infos.remove(info);
    }

    public static void addInfo2Queue(AuditInfo info) {
        if (info == null)
            return;
        if (infos == null)
            infos = new ArrayList<>();
        infos.add(info);
    }

}
