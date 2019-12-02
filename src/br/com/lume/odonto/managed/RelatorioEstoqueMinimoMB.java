package br.com.lume.odonto.managed;

import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.apache.log4j.Logger;
import org.primefaces.component.datatable.DataTable;

import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.odonto.entity.RelatorioEstoqueMinimo;
import br.com.lume.relatorioEstoqueMinimo.RelatorioEstoqueMinimoSingleton;

@ManagedBean
@ViewScoped
public class RelatorioEstoqueMinimoMB extends LumeManagedBean<RelatorioEstoqueMinimo> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(RelatorioEstoqueMinimoMB.class);

    private List<RelatorioEstoqueMinimo> materiais = new ArrayList<>();
    
    private String filtroItem, filtroTipo;

    //EXPORTAÇÃO TABELA
    private DataTable tabelaEstoque;

    public RelatorioEstoqueMinimoMB() {
        super(RelatorioEstoqueMinimoSingleton.getInstance().getBo());
     
        this.setClazz(RelatorioEstoqueMinimo.class);
        this.filtra();
    }

    public void exportarTabela(String type) {
        exportarTabela("Estoque mínimo", tabelaEstoque, type);
    }
    
    public void filtra() {        
        this.materiais = RelatorioEstoqueMinimoSingleton.getInstance().getBo().listAllByFilterToReportGroupByItemFiltrado(filtroItem, filtroTipo,UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
    }

    public List<RelatorioEstoqueMinimo> getMateriais() {
        return this.materiais;
    }

    public void setMateriais(List<RelatorioEstoqueMinimo> materiais) {
        this.materiais = materiais;
    }

    public DataTable getTabelaEstoque() {
        return tabelaEstoque;
    }

    public void setTabelaEstoque(DataTable tabelaEstoque) {
        this.tabelaEstoque = tabelaEstoque;
    }

    
    public String getFiltroItem() {
        return filtroItem;
    }

    
    public void setFiltroItem(String filtroItem) {
        this.filtroItem = filtroItem;
    }

    
    public String getFiltroTipo() {
        return filtroTipo;
    }

    
    public void setFiltroTipo(String filtroTipo) {
        this.filtroTipo = filtroTipo;
    }

    

}
