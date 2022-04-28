package br.com.lume.odonto.managed;

import java.math.BigDecimal;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ActionEvent;

import org.apache.log4j.Logger;

import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.odonto.entity.Orcamento;
import br.com.lume.orcamento.OrcamentoSingleton;

@ManagedBean
@ViewScoped
public class OrcamentoNovoMB extends LumeManagedBean<Orcamento> {

    private Logger log = Logger.getLogger(OrcamentoNovoMB.class);

    private BigDecimal valorTotalOriginal, valorTotal, totalPago;

    private BigDecimal porcentagem = new BigDecimal(0d);

    @ManagedProperty(value = "#{planoTratamentoMB}")
    private PlanoTratamentoMB planoTratamentoMB;

    public OrcamentoNovoMB() {
        super(OrcamentoSingleton.getInstance().getBo());
        setClazz(Orcamento.class);
    }

    public void actionCalcularDesconto() {

    }

    @Override
    public void actionPersist(ActionEvent event) {

    }

    @Override
    public void actionRemove(ActionEvent event) {
    }
}
