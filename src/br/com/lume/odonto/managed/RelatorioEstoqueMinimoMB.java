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
import br.com.lume.emprestimoKit.EmprestimoKitSingleton;
import br.com.lume.estoque.EstoqueSingleton;
import br.com.lume.item.ItemSingleton;
import br.com.lume.material.MaterialSingleton;
import br.com.lume.odonto.entity.EmprestimoKit;
import br.com.lume.odonto.entity.Estoque;
import br.com.lume.odonto.entity.Item;
import br.com.lume.odonto.entity.MateriaisEmprestados;
import br.com.lume.odonto.entity.Material;
import br.com.lume.odonto.entity.MaterialEmprestado;
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
    
    private List<Estoque> detalhes = new ArrayList<>();
    
    private String filtroItem, filtroTipo;
    
    private BigDecimal quantidadeTotal = new BigDecimal(0);
    private BigDecimal valorTotal = new BigDecimal(0);
    private BigDecimal custoMedio = new BigDecimal(0);
    private Item itemDetalhamento; 
    
    //EXPORTAÇÃO TABELA
    private DataTable tabelaEstoque;
    
    private boolean mostrarSomenteEstoqueMinimo;
    
    private List<MateriaisEmprestados> emprestados = new ArrayList<>();

    public RelatorioEstoqueMinimoMB() {
        super(RelatorioEstoqueMinimoSingleton.getInstance().getBo());
     
        this.setClazz(RelatorioEstoqueMinimo.class);
        this.filtra();
    }

    public void detalhes(RelatorioEstoqueMinimo relatorioEstoqueMinimo) {
        try {
            itemDetalhamento = ItemSingleton.getInstance().getBo().find(relatorioEstoqueMinimo.getId()); 
            if(itemDetalhamento != null) {
                detalhes = EstoqueSingleton.getInstance().getBo().listAllByEmpresaAndItem(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa(),itemDetalhamento);
                this.quantidadeTotal = new BigDecimal(0);
                this.valorTotal = new BigDecimal(0);
                this.custoMedio = new BigDecimal(0);
                for (Estoque estoque : detalhes) {
                    this.quantidadeTotal = estoque.getQuantidade().add(this.quantidadeTotal);
                    this.valorTotal = this.valorTotal.add(estoque.getMaterial().getValor().multiply(estoque.getQuantidade()));
                    //montando todos os emprestimos                  
                }          
                montaKitsEmprestados(detalhes);
                //TODO unitario, lavagel, esterilizacao, 
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
    
    public void montaKitsEmprestados(List<Estoque> estoques) {
        for (Estoque estoque : estoques) {
            List<EmprestimoKit> kits;
            try {
                kits = EmprestimoKitSingleton.getInstance().getBo().listByEmpresaAndMaterial(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa(),estoque.getMaterial());
                if(kits != null && !kits.isEmpty()) {
                    this.emprestados = new ArrayList<MateriaisEmprestados>();
                    for (EmprestimoKit kit : kits) {
                        //TODO verificar se sao esses os status emprestados mesmo kit.getReservaKit().getStatus().equals("PE") kit.getReservaKit().getStatus().equals("EN")       
                        //para colocar nesse if
                        if(kit.getReservaKit().getExcluido().equals("N") && kit.getReservaKit().getReserva().getExcluido().equals("N")) {
                            MateriaisEmprestados emprestado = new MateriaisEmprestados();
                            emprestado.setLocal("Kit emprestado");
                            emprestado.setDetalhes(kit.getReservaKit().getDetalhamento());
                            emprestado.setQuantidade(kit.getQuantidade());
                            //TODO trocar para objetos local?                              
                            this.emprestados.add(emprestado);
                        }  
                    
                    }
               
                }
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }           
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

    
    public List<Estoque> getDetalhes() {
        return detalhes;
    }

    
    public void setDetalhes(List<Estoque> detalhes) {
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

    
    public List<MateriaisEmprestados> getEmprestados() {
        return emprestados;
    }

    
    public void setEmprestados(List<MateriaisEmprestados> emprestados) {
        this.emprestados = emprestados;
    }

}
