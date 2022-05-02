package br.com.lume.odonto.managed;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ActionEvent;

import org.apache.log4j.Logger;
import org.primefaces.PrimeFaces;
import org.primefaces.component.datatable.DataTable;

import br.com.lume.common.exception.business.BusinessException;
import br.com.lume.common.exception.techinical.TechnicalException;
import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.FormaPagamento;
import br.com.lume.common.util.JSFHelper;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.dadosBasico.DadosBasicoSingleton;
import br.com.lume.fornecedor.FornecedorSingleton;
import br.com.lume.odonto.entity.DadosBasico;
import br.com.lume.odonto.entity.Tarifa;
import br.com.lume.odonto.exception.CpfCnpjDuplicadoException;
import br.com.lume.odonto.exception.TelefoneException;
import br.com.lume.odonto.util.OdontoMensagens;
import br.com.lume.tarifa.TarifaSingleton;

@ManagedBean
@ViewScoped
public class TarifaMB extends LumeManagedBean<Tarifa> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(TarifaMB.class);

    private List<Tarifa> tarifas = new ArrayList<>();

    //EXPORTAÇÃO TABELA
    private DataTable tabelaTarifa;

    private String filtroStatus = "A";

    public TarifaMB() {
        super(TarifaSingleton.getInstance().getBo());
        if (UtilsFrontEnd.getProfissionalLogado() != null) {
            this.geraLista();
        }
        this.setClazz(Tarifa.class);

    }

    public void geraLista() {
        try {
            this.tarifas = TarifaSingleton.getInstance().getBo().listByEmpresaAndStatus(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa(), filtroStatus, FormaPagamento.AMBOS);
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            this.log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
        }
        if (this.tarifas != null)
            Collections.sort(this.tarifas);
    }

    public void mudaPrazo() {
        if (getEntity().getTipo().equals("CC")) {
            getEntity().setPrazo(30);
        } else if (getEntity().getTipo().equals("CD")) {
            getEntity().setPrazo(2);
        } else {
            getEntity().setPrazo(0);
        }

    }

    @Override
    public void actionPersist(ActionEvent event) {
        try {
            if (getEntity().getParcelaMinima() > getEntity().getParcelaMaxima()) {
                this.addError("Quantidade de parcela mínima não pode ser maior que parcela máxima.", "", true);
            } else if (getEntity().getParcelaMinima() < 1 || getEntity().getParcelaMaxima() < 1) {
                this.addError("Quantidade de parcela mínima e máxima não podem ser 0, deve ser no mínimo 1", "", true);
            } else {
                this.getEntity().setIdEmpresa(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
                super.actionPersist(event);
                this.geraLista();
                PrimeFaces.current().executeScript("PF('dlg').hide()");
                PrimeFaces.current().executeScript("PF('dtTarifa').filter();");
            }
        } catch (Exception e) {
            log.error("Erro no actionPersist", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "", true);
        }
    }

    public void actionInativar(Tarifa tarifa) {
        tarifa.setStatus("I");
        try {
            TarifaSingleton.getInstance().getBo().persist(tarifa);
            this.geraLista();
            this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "Forma de pagamento inativada com sucesso");
        } catch (Exception e) {
            log.error("Erro no actionInativar", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "", true);
        }
    }

    public void actionAtivar(Tarifa tarifa) {
        tarifa.setStatus("A");
        try {
            TarifaSingleton.getInstance().getBo().persist(tarifa);
            this.geraLista();
            this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "Forma de pagamento ativada com sucesso");
        } catch (Exception e) {
            log.error("Erro no actionInativar", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "", true);
        }
    }

    public void actionEditar(Tarifa tarifa) {
        setEntity(tarifa);
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

    public String getFiltroStatus() {
        return filtroStatus;
    }

    public void setFiltroStatus(String filtroStatus) {
        this.filtroStatus = filtroStatus;
    }
}
