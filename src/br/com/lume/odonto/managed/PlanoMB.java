package br.com.lume.odonto.managed;

import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.apache.log4j.Logger;

import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.odonto.bo.PlanoBO;
import br.com.lume.odonto.entity.Plano;

@ManagedBean
@ViewScoped
public class PlanoMB extends LumeManagedBean<Plano> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(PlanoMB.class);

    private List<Plano> planos;

    public PlanoMB() {
        super(new PlanoBO());
        this.setClazz(Plano.class);
        this.carregarPlanos();
    }

    private void carregarPlanos() {
        try {
            this.planos = ((PlanoBO) this.getbO()).listAll();
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            this.log.error(e);
        }
    }

    public List<Plano> getPlanos() {
        return this.planos;
    }

    public void setPlanos(List<Plano> planos) {
        this.planos = planos;
    }
}
