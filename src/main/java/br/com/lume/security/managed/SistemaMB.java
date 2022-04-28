package br.com.lume.security.managed;

import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.apache.log4j.Logger;

import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.security.bo.SistemaBO;
import br.com.lume.security.entity.Sistema;

@ManagedBean
@ViewScoped
public class SistemaMB extends LumeManagedBean<Sistema> {

    private static final long serialVersionUID = 1L;

    private final SistemaBO sistemaBO = new SistemaBO();

    private Logger log = Logger.getLogger(SistemaMB.class);

    public SistemaMB() {
        super(new SistemaBO());
        this.setClazz(Sistema.class);
    }

    @Override
    public List<Sistema> getEntityList() {
        return this.sistemaBO.getAllSistemas();
    }
}
