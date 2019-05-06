package br.com.lume.odonto.managed;

import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.apache.log4j.Logger;

import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.odonto.entity.Retorno;
import br.com.lume.retorno.RetornoSingleton;

@ManagedBean
@ViewScoped
public class RetornoHojeMB extends LumeManagedBean<Retorno> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(RetornoHojeMB.class);

    private List<Retorno> retornos = new ArrayList<>();

    public RetornoHojeMB() {
        super(RetornoSingleton.getInstance().getBo());
        this.geraLista();
        this.setClazz(Retorno.class);
    }

    private void geraLista() {
        try {
            this.retornos = RetornoSingleton.getInstance().getBo().listRetornoHoje();
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            this.log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
        }
    }

    public List<Retorno> getRetornos() {
        return this.retornos;
    }

    public void setRetornos(List<Retorno> retornos) {
        this.retornos = retornos;
    }
}
