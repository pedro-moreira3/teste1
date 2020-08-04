package br.com.lume.odonto.managed;

import javax.faces.view.ViewScoped;
import javax.inject.Named;

import org.apache.log4j.Logger;

import br.com.lume.avisos.AvisosSingleton;
import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.logImportacao.LogImportacaoSingleton;
import br.com.lume.odonto.entity.Avisos;
import br.com.lume.odonto.entity.LogImportacao;

@Named
@ViewScoped
public class AvisosMB extends LumeManagedBean<Avisos>{

    private static final long serialVersionUID = 1L;
    private Logger log = Logger.getLogger(AvisosMB.class);
    
    public AvisosMB() {
        super(AvisosSingleton.getInstance().getBo());
        this.setClazz(Avisos.class);
    }
    
}
