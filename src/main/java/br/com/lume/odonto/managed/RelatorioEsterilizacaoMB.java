package br.com.lume.odonto.managed;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ActionEvent;

import org.apache.log4j.Logger;
import org.primefaces.component.datatable.DataTable;

import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.item.ItemSingleton;
import br.com.lume.lavagem.LavagemSingleton;
import br.com.lume.lavagemKit.LavagemKitSingleton;
import br.com.lume.odonto.entity.Item;
import br.com.lume.odonto.entity.Lavagem;
import br.com.lume.odonto.entity.LavagemKit;
import br.com.lume.odonto.entity.Material;
import br.com.lume.odonto.entity.Profissional;
import br.com.lume.odonto.entity.TransferenciaEstoque;
import br.com.lume.odonto.util.OdontoMensagens;
import br.com.lume.profissional.ProfissionalSingleton;
import br.com.lume.transferenciaEstoque.TransferenciaEstoqueSingleton;

@ManagedBean
@ViewScoped
public class RelatorioEsterilizacaoMB extends LumeManagedBean<TransferenciaEstoque> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(RelatorioEsterilizacaoMB.class);

    private List<TransferenciaEstoque> transferenciaEstoques = new ArrayList<>();

    private Date inicio, fim, inicioFinalizacao, fimFinalizacao;
    
    private String filtroPeriodo;

    private String filtroPeriodoFinalizacao;
    
    private Item filtroItem;
    
    private Profissional filtroProfissional;

    
    //EXPORTAÇÃO TABELA
    private DataTable tabelaRelatorio; 
    
    private List<TransferenciaEstoque> listaTransferenciasEstoque;

    public RelatorioEsterilizacaoMB() {
        super(TransferenciaEstoqueSingleton.getInstance().getBo());
        this.setClazz(TransferenciaEstoque.class);
       // this.filtra();
    }
    
    public List<Profissional> sugestoesProfissionais(String query) {
        return ProfissionalSingleton.getInstance().getBo().listSugestoesCompleteProfissional(query, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa(), true);
    }
    
    public List<Item> sugestoesItens(String query) {
        return ItemSingleton.getInstance().getBo().listSugestoesComplete(query,UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
    }

    public void filtra() {
        try {
            if ((this.inicio != null && this.fim != null && this.inicio.getTime() > this.fim.getTime())
                    || (this.inicioFinalizacao != null && this.fimFinalizacao != null && this.inicioFinalizacao.getTime() > this.fimFinalizacao.getTime())) {
                this.addError(OdontoMensagens.getMensagem("afastamento.dtFim.menor.dtInicio"), "");
            } else {               
                this.transferenciaEstoques = TransferenciaEstoqueSingleton.getInstance().getBo().listAllEsterilizacoesByPeriodo(this.inicio, this.fim,
                        this.inicioFinalizacao,this.fimFinalizacao, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa(),this.filtroItem,this.filtroProfissional);
                
                if (this.transferenciaEstoques == null || this.transferenciaEstoques.isEmpty()) {
                    this.addError(OdontoMensagens.getMensagem("relatorio.procedimento.vazio"), "");
                    this.log.error(OdontoMensagens.getMensagem("relatorio.procedimento.vazio"));
                }
            }
        } catch (Exception e) {
            this.log.error(e);
        }

    }
    
    public void carregarMaterialLog(TransferenciaEstoque transferencia) {
        try {
            listaTransferenciasEstoque = TransferenciaEstoqueSingleton.getInstance().getBo().listByMaterial(transferencia.getMaterial());
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "", true);
            log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
        }
    }
    
    public void actionTrocaDatasCriacao() {
        try {
            setInicio(getDataInicio(filtroPeriodo));
            setFim(getDataFim(filtroPeriodo));
          //  actionFiltrar(null);            
        } catch (Exception e) {
            log.error("Erro no actionTrocaDatasCriacao", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
        }
    }
    
    public void actionTrocaDatasFinal() {
        try {
            setInicioFinalizacao(getDataInicio(filtroPeriodoFinalizacao));
            setFimFinalizacao(getDataFim(filtroPeriodoFinalizacao));
         //   actionFiltrar(null);
        } catch (Exception e) {
            log.error("Erro no actionTrocaDatasFinal", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
        }
    }    
    
    public Date getDataInicio(String filtro) {        
        Date dataInicio = null;
        try {
            Calendar c = Calendar.getInstance();
                if ("O".equals(filtro)) {
                c.add(Calendar.DAY_OF_MONTH, -1);                
                dataInicio = c.getTime(); 
            } else if ("H".equals(filtro)) { //Hoje                
                dataInicio = c.getTime(); 
            } else if ("S".equals(filtro)) { //Últimos 7 dias              
                c.add(Calendar.DAY_OF_MONTH, -7);
                dataInicio = c.getTime();
            } else if ("Q".equals(filtro)) { //Últimos 15 dias              
                c.add(Calendar.DAY_OF_MONTH, -15);
                dataInicio = c.getTime();
            } else if ("T".equals(filtro)) { //Últimos 30 dias                
                c.add(Calendar.DAY_OF_MONTH, -30);
                dataInicio = c.getTime();
            } else if ("M".equals(filtro)) { //Mês Atual              
                c.set(Calendar.DAY_OF_MONTH, 1);
                dataInicio = c.getTime();
            } else if ("I".equals(filtro)) { //Mês Atual             
                c.add(Calendar.MONTH, -6);
                dataInicio = c.getTime();
            }
            return dataInicio;
        } catch (Exception e) {
            log.error("Erro no getDataInicio", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            return null;
        }
    }  
    
    public Date getDataFim(String filtro) {
        Date dataFim = null;
        try {
            Calendar c = Calendar.getInstance();
            if ("O".equals(filtro)) {
                c.add(Calendar.DAY_OF_MONTH, -1);
                dataFim = c.getTime();
            }else if(filtro == null) { 
                dataFim = null;
            } else { 
                dataFim = c.getTime();
            } 
            return dataFim;
        } catch (Exception e) {
            log.error("Erro no getDataFim", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            return null;
        }
    }     

    @Override
    public void actionNew(ActionEvent arg0) {
        this.inicio = null;
        this.fim = null;
        super.actionNew(arg0);
    }

    public void exportarTabela(String type) {
        this.exportarTabela("", tabelaRelatorio, type);
    }   

    public Date getInicio() {
        return this.inicio;
    }

    public void setInicio(Date inicio) {
        this.inicio = inicio;
    }

    public Date getFim() {
        return this.fim;
    }

    public void setFim(Date fim) {
        this.fim = fim;
    }
    
    public DataTable getTabelaRelatorio() {
        return tabelaRelatorio;
    }

    public void setTabelaRelatorio(DataTable tabelaRelatorio) {
        this.tabelaRelatorio = tabelaRelatorio;
    }

    
    public Date getInicioFinalizacao() {
        return inicioFinalizacao;
    }

    
    public void setInicioFinalizacao(Date inicioFinalizacao) {
        this.inicioFinalizacao = inicioFinalizacao;
    }

    
    public Date getFimFinalizacao() {
        return fimFinalizacao;
    }

    
    public void setFimFinalizacao(Date fimFinalizacao) {
        this.fimFinalizacao = fimFinalizacao;
    }

    
    public String getFiltroPeriodo() {
        return filtroPeriodo;
    }

    
    public void setFiltroPeriodo(String filtroPeriodo) {
        this.filtroPeriodo = filtroPeriodo;
    }

    
    public String getFiltroPeriodoFinalizacao() {
        return filtroPeriodoFinalizacao;
    }

    
    public void setFiltroPeriodoFinalizacao(String filtroPeriodoFinalizacao) {
        this.filtroPeriodoFinalizacao = filtroPeriodoFinalizacao;
    }

    
    public List<TransferenciaEstoque> getTransferenciaEstoques() {
        return transferenciaEstoques;
    }

    
    public void setTransferenciaEstoques(List<TransferenciaEstoque> transferenciaEstoques) {
        this.transferenciaEstoques = transferenciaEstoques;
    }

    
    public Item getFiltroItem() {
        return filtroItem;
    }

    
    public void setFiltroItem(Item filtroItem) {
        this.filtroItem = filtroItem;
    }

    
    public Profissional getFiltroProfissional() {
        return filtroProfissional;
    }

    
    public void setFiltroProfissional(Profissional filtroProfissional) {
        this.filtroProfissional = filtroProfissional;
    }

    
    public List<TransferenciaEstoque> getListaTransferenciasEstoque() {
        return listaTransferenciasEstoque;
    }

    
    public void setListaTransferenciasEstoque(List<TransferenciaEstoque> listaTransferenciasEstoque) {
        this.listaTransferenciasEstoque = listaTransferenciasEstoque;
    }

}
