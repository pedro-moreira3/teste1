package br.com.lume.odonto.managed;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ActionEvent;

import org.apache.log4j.Logger;

import br.com.lume.common.exception.business.BusinessException;
import br.com.lume.common.exception.techinical.TechnicalException;
import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.configuracao.Configurar;
import br.com.lume.dadosBasico.DadosBasicoSingleton;
import br.com.lume.odonto.entity.DadosBasico;
import br.com.lume.odonto.entity.Tarifa;
import br.com.lume.tarifa.TarifaSingleton;

@ManagedBean
@ViewScoped
public class TarifaMB extends LumeManagedBean<Tarifa> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(TarifaMB.class);

    private List<Tarifa> tarifas = new ArrayList<>();

    public TarifaMB() {
        super(TarifaSingleton.getInstance().getBo());   
        this.setClazz(Tarifa.class);
    }

    private void geraLista() {
        try {
            this.tarifas = TarifaSingleton.getInstance().getBo().listByEmpresa();
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            this.log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
        }
        Collections.sort(this.tarifas);
    }

    @Override
    public void actionPersist(ActionEvent event) {
        this.getEntity().setIdEmpresa(Configurar.getInstance().getConfiguracao().getProfissionalLogado().getIdEmpresa());
        DadosBasico basico = new DadosBasico();
        if (DadosBasicoSingleton.getInstance().getBo().findByNome(this.getEntity().getProduto()) == null) {
            basico.setNome(this.getEntity().getProduto());
            try {
                DadosBasicoSingleton.getInstance().getBo().persist(basico);
            } catch (BusinessException e) {
                e.printStackTrace();
            } catch (TechnicalException e) {
                e.printStackTrace();
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
        super.actionPersist(event);
        this.geraLista();
    }

    public List<Tarifa> getTarifas() {
        return this.tarifas;
    }

    public void setTarifas(List<Tarifa> tarifas) {
        this.tarifas = tarifas;
    }
}
