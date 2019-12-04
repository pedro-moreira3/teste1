package br.com.lume.odonto.managed;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.apache.log4j.Logger;
import org.primefaces.component.datatable.DataTable;

import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Status;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.item.ItemSingleton;
import br.com.lume.material.MaterialSingleton;
import br.com.lume.odonto.entity.Item;
import br.com.lume.odonto.entity.Material;
import br.com.lume.odonto.entity.Paciente;
import br.com.lume.odonto.entity.RelatorioEstoqueMinimo;
import br.com.lume.paciente.PacienteSingleton;
import br.com.lume.relatorioEstoqueMinimo.RelatorioEstoqueMinimoSingleton;

@ManagedBean
@ViewScoped
public class RelatorioEstoqueMinimoMB extends LumeManagedBean<RelatorioEstoqueMinimo> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(RelatorioEstoqueMinimoMB.class);

    private List<RelatorioEstoqueMinimo> materiais = new ArrayList<>();
    
    private List<Material> detalhes = new ArrayList<>();
    
    private String filtroItem, filtroTipo;
    
    private BigDecimal quantidadeTotal = new BigDecimal(0);
    private BigDecimal valorTotal = new BigDecimal(0);
    private BigDecimal custoMedio = new BigDecimal(0);
    private Item itemDetalhamento; 
    
    //EXPORTAÇÃO TABELA
    private DataTable tabelaEstoque;
    
    private boolean mostrarSomenteEstoqueMinimo;

    public RelatorioEstoqueMinimoMB() {
        super(RelatorioEstoqueMinimoSingleton.getInstance().getBo());
     
        this.setClazz(RelatorioEstoqueMinimo.class);
        this.filtra();
    }

    public void detalhes(RelatorioEstoqueMinimo relatorioEstoqueMinimo) {
        try {
            itemDetalhamento = ItemSingleton.getInstance().getBo().find(relatorioEstoqueMinimo.getId()); 
            if(itemDetalhamento != null) {
                detalhes = MaterialSingleton.getInstance().getBo().listAtivosByEmpresaAndItem(itemDetalhamento,UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
                this.quantidadeTotal = new BigDecimal(0);
                this.valorTotal = new BigDecimal(0);
                this.custoMedio = new BigDecimal(0);
                for (Material material : detalhes) {
                    this.quantidadeTotal = this.quantidadeTotal.add(material.getEstoque().getQuantidade());
                    this.valorTotal = this.valorTotal.add(material.getValorTotal());
                }          
                if(this.valorTotal.compareTo(BigDecimal.ZERO) != 0 && this.quantidadeTotal.compareTo(BigDecimal.ZERO) != 0) {                   
                    this.custoMedio = this.valorTotal.divide(this.quantidadeTotal, MathContext.DECIMAL32);    
                }                
                Collections.sort(detalhes);    
            }else {
                this.addError("Erro", "Item não encontrado!", true);
            }
        } catch (Exception e) {
            this.addError("Erro", "Falha ao abrir detalhes do item!", true);  
            e.printStackTrace();
        }        
    }
    
    public void exportarTabela(String type) {
        exportarTabela("Estoque mínimo", tabelaEstoque, type);
    }
    
    public void filtra() {  
        this.materiais = RelatorioEstoqueMinimoSingleton.getInstance().getBo().listAllByFilterToReportGroupByItemFiltrado(mostrarSomenteEstoqueMinimo, filtroItem, filtroTipo,UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
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

    
    public boolean isMostrarSomenteEstoqueMinimo() {
        return mostrarSomenteEstoqueMinimo;
    }

    
    public void setMostrarSomenteEstoqueMinimo(boolean mostrarSomenteEstoqueMinimo) {
        this.mostrarSomenteEstoqueMinimo = mostrarSomenteEstoqueMinimo;
    }

    
    public List<Material> getDetalhes() {
        return detalhes;
    }

    
    public void setDetalhes(List<Material> detalhes) {
        this.detalhes = detalhes;
    }

    
    public BigDecimal getQuantidadeTotal() {
        return quantidadeTotal;
    }

    
    public void setQuantidadeTotal(BigDecimal quantidadeTotal) {
        this.quantidadeTotal = quantidadeTotal;
    }

    
    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    
    public void setValorTotal(BigDecimal valorTotal) {
        this.valorTotal = valorTotal;
    }

    
    public BigDecimal getCustoMedio() {
        return custoMedio;
    }

    
    public void setCustoMedio(BigDecimal custoMedio) {
        this.custoMedio = custoMedio;
    }

    
    public Item getItemDetalhamento() {
        return itemDetalhamento;
    }

    
    public void setItemDetalhamento(Item itemDetalhamento) {
        this.itemDetalhamento = itemDetalhamento;
    }

    

}
