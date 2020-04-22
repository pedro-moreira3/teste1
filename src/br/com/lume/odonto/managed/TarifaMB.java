package br.com.lume.odonto.managed;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ActionEvent;

import org.apache.log4j.Logger;
import org.primefaces.component.datatable.DataTable;

import br.com.lume.common.exception.business.BusinessException;
import br.com.lume.common.exception.techinical.TechnicalException;
import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.UtilsFrontEnd;
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

    //EXPORTAÇÃO TABELA
    private DataTable tabelaTarifa;

    public TarifaMB() {
        super(TarifaSingleton.getInstance().getBo());
        this.geraLista();
        this.setClazz(Tarifa.class);
    }

    private void geraLista() {
        try {
            this.tarifas = TarifaSingleton.getInstance().getBo().listByEmpresa(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            this.log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
        }
        if (this.tarifas != null)
            Collections.sort(this.tarifas);
    }

    @Override
    public void actionPersist(ActionEvent event) {
        this.getEntity().setIdEmpresa(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
        DadosBasico basico = new DadosBasico();
        if (DadosBasicoSingleton.getInstance().getBo().findByNome(this.getEntity().getProduto()) == null) {
            basico.setNome(this.getEntity().getProduto());
            try {
                DadosBasicoSingleton.getInstance().getBo().persist(basico);
            } catch (BusinessException e) {
                e.printStackTrace();
            } catch (TechnicalException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        super.actionPersist(event);
        this.geraLista();
    }

    public void actionInativar(ActionEvent event) {
        if (this.getEntity() != null) {
            try {
                TarifaSingleton.getInstance().inativarTarifa(this.getEntity(), UtilsFrontEnd.getProfissionalLogado());
                this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "Sucesso ao inativar");
            } catch (Exception e) {
                this.log.error("Erro no actionInativar");
                e.printStackTrace();
                this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_REMOVER_REGISTRO), "Erro ao inativar tarifa");
            }
        }
    }

    public void actionAtivar(ActionEvent event) {
        if (this.getEntity() != null) {
            try {
                TarifaSingleton.getInstance().ativarTarifa(this.getEntity(), UtilsFrontEnd.getProfissionalLogado());
                this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "Sucesso ao ativar tarifa");
            } catch (Exception e) {
                this.log.error("Erro no actionAtivar");
                e.printStackTrace();
                this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_REMOVER_REGISTRO), "Erro ao ativar tarifa");
            }
        }
    }

    public void exportarTabela(String type) {
        exportarTabela("Tarifas", tabelaTarifa, type);
    }

    public List<Tarifa> getTarifas() {
        return this.tarifas;
    }

    public void setTarifas(List<Tarifa> tarifas) {
        this.tarifas = tarifas;
    }

    public DataTable getTabelaTarifa() {
        return tabelaTarifa;
    }

    public void setTabelaTarifa(DataTable tabelaTarifa) {
        this.tabelaTarifa = tabelaTarifa;
    }
}
