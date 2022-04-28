package br.com.lume.common.config;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Singleton;
import javax.ejb.Startup;

import br.com.lume.security.audit.threads.AuditRegisterThread;

@Singleton
@Startup
public class OdontoInitializer {

  //  private static AuditRegisterThread thread = null;

    @PostConstruct
    public void init() {
        // Configurar.getInstance().getConfiguracao().setAmbiente(Configuracao.AMBIENTES.DEV);
//        if (OdontoInitializer.thread == null) {
//            OdontoInitializer.thread = new AuditRegisterThread();
//            OdontoInitializer.thread.start();
//        }
    }

    @PreDestroy
    public void destroy() {

    }

}
