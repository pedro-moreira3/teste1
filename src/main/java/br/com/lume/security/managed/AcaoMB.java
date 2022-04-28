package br.com.lume.security.managed;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.apache.log4j.Logger;

import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.security.bo.AcaoBO;
import br.com.lume.security.entity.Acao;

@ManagedBean
@ViewScoped
public class AcaoMB extends LumeManagedBean<Acao> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(AcaoMB.class);

    public AcaoMB() {
        super(new AcaoBO());
        this.setClazz(Acao.class);
    }
}
