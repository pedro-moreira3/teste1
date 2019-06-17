package br.com.lume.common.config;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Singleton;
import javax.ejb.Startup;

@Singleton
@Startup
public class OdontoInitializer {
    
    @PostConstruct
    public void init() {
       // Configurar.getInstance().getConfiguracao().setAmbiente(Configuracao.AMBIENTES.DEV);
    }

    @PreDestroy
    public void destroy() {

    }

}
