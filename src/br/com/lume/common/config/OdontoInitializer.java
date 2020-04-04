package br.com.lume.common.config;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Singleton;
import javax.ejb.Startup;

import br.com.lume.security.audit.threads.AuditRegisterThread;

@Singleton
@Startup
public class OdontoInitializer {

    @PostConstruct
    public void init() {
        // Configurar.getInstance().getConfiguracao().setAmbiente(Configuracao.AMBIENTES.DEV);
        new AuditRegisterThread().start();
    }

    @PreDestroy
    public void destroy() {

    }

}
