package br.com.lume.odonto.managed;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.apache.log4j.Logger;
import org.primefaces.component.datatable.DataTable;

import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.emprestimoKit.EmprestimoKitSingleton;
import br.com.lume.emprestimoUnitario.EmprestimoUnitarioSingleton;
import br.com.lume.esterilizacaoKit.EsterilizacaoKitSIngleton;
import br.com.lume.estoque.EstoqueSingleton;
import br.com.lume.item.ItemSingleton;
import br.com.lume.lavagemKit.LavagemKitSingleton;
import br.com.lume.local.LocalSingleton;
import br.com.lume.odonto.entity.EmprestimoKit;
import br.com.lume.odonto.entity.EmprestimoUnitario;
import br.com.lume.odonto.entity.EsterilizacaoKit;
import br.com.lume.odonto.entity.Estoque;
import br.com.lume.odonto.entity.Item;
import br.com.lume.odonto.entity.LavagemKit;
import br.com.lume.odonto.entity.Local;
import br.com.lume.odonto.entity.MateriaisEmprestados;
import br.com.lume.odonto.entity.Material;
import br.com.lume.odonto.entity.RelatorioEstoqueMinimo;
import br.com.lume.odonto.entity.TransferenciaEstoque;
import br.com.lume.relatorioEstoqueMinimo.RelatorioEstoqueMinimoSingleton;
import br.com.lume.transferenciaEstoque.TransferenciaEstoqueSingleton;

@ManagedBean
@ViewScoped
public class RelatorioEstoqueMinimoMB extends LumeManagedBean<Estoque> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(RelatorioEstoqueMinimoMB.class);

   // private List<RelatorioEstoqueMinimo> materiais = new ArrayList<>();
    
    //TODO substituir a lista de materiais pela lista de estoque
    //private List<RelatorioEstoqueMinimo> estoques = new ArrayList<>();
    
    private List<Estoque> detalhes = new ArrayList<>();
    
    private String filtroItem, filtroTipo;
    
    private BigDecimal quantidadeTotal = new BigDecimal(0);
    private BigDecimal valorTotal = new BigDecimal(0);
    private BigDecimal custoMedio = new BigDecimal(0);
    private Item itemDetalhamento; 
    
    //EXPORTAÇÃO TABELA
    private DataTable tabelaEstoque;

    
    private List<MateriaisEmprestados> emprestados = new ArrayList<>();
    
    private List<TransferenciaEstoque> listaTransferenciasEstoque;
    
    private String local;    
    private List<Local> locais;
    
    private List<Estoque> estoques = new ArrayList<>();

    public RelatorioEstoqueMinimoMB() {
        super(EstoqueSingleton.getInstance().getBo());
     
        this.setClazz(Estoque.class);
        this.filtra();
    }
    
    public void carregarMaterialLog(Estoque estoque) {
        try {
            listaTransferenciasEstoque = TransferenciaEstoqueSingleton.getInstance().getBo().listByEstoqueDestino(estoque);
            listaTransferenciasEstoque.addAll(TransferenciaEstoqueSingleton.getInstance().getBo().listByEstoqueOrigem(estoque));
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "", true);
            log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
        }
    }
    
    public List<String> filtraLocal(String digitacao) {
        this.setLocal(digitacao);
        this.filtraLocais();
        return this.convert(this.getLocais());
    }
    
    public void filtraLocais() {
        this.setLocais(new ArrayList<Local>());
        try {
            if (this.getLocal() != null) {
                this.setLocais(LocalSingleton.getInstance().getBo().listByEmpresaAndDescricaoParcial(this.getLocal(), UtilsFrontEnd.getProfissionalLogado().getIdEmpresa(),true));
            } else {
                this.setLocais(LocalSingleton.getInstance().getBo().listByEmpresa(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa(),true));
            }
            Collections.sort(this.getLocais());
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "",true);
            log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
        }
    }
    
    public void handleSelectLocal() {
        this.filtraLocal(this.getLocal());     
        }
    
    public List<String> convert(@SuppressWarnings("rawtypes") List objects) {
        List<String> strings = new ArrayList<>();
        for (Object object : objects) {
            if (object instanceof Item) {
                strings.add(((Item) object).getDescricao());
            } else if (object instanceof Local) {
                strings.add(((Local) object).getDescricao());
            }
        }
        return strings;
    }
    

    public void detalhes(Estoque estoque) {
        try {
            itemDetalhamento = estoque.getMaterial().getItem(); 
            if(itemDetalhamento != null) {
              //  detalhes = EstoqueSingleton.getInstance().getBo().listAllDisponiveisByEmpresaItemAndQuantidade(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa(),itemDetalhamento,new BigDecimal(0),false);
                this.quantidadeTotal = new BigDecimal(0);
                this.valorTotal = new BigDecimal(0);
                this.custoMedio = new BigDecimal(0);               
              //  for (Estoque estoque : detalhes) {
                    this.quantidadeTotal = estoque.getQuantidade().add(this.quantidadeTotal);
                    this.valorTotal = this.valorTotal.add(estoque.getMaterial().getValor().multiply(estoque.getQuantidade()));
                    //montando todos os emprestimos                  
              //  }        
               
                    this.emprestados = new ArrayList<MateriaisEmprestados>();
                detalhes.add(estoque); 
                montaKitsEmprestados(detalhes);
                montaUnitarioEmprestados(detalhes);
                montaEmLavagem(itemDetalhamento);
                montaEmEsterilizacao(itemDetalhamento);                
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
                kits = EmprestimoKitSingleton.getInstance().getBo().listByEmpresaAndMaterialComQuantidade(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa(),estoque.getMaterial());
                if(kits != null && !kits.isEmpty()) {
         
                    for (EmprestimoKit kit : kits) {                       
                        if(kit.getReservaKit().getExcluido().equals("N") && kit.getReservaKit().getReserva().getExcluido().equals("N") && !kit.getReservaKit().getStatus().equals("FI")) {
                            if(kit.getReservaKit().getStatus().equals(EmprestimoKit.ENTREGUE) || kit.getReservaKit().getStatus().equals(EmprestimoKit.PENDENTE)) {
                                MateriaisEmprestados emprestado = new MateriaisEmprestados();
                                if(kit.getReservaKit().getStatus().equals(EmprestimoKit.PENDENTE)) {
                                    emprestado.setLocal("Kit emprestado - disponibilizado");
                                }else if(kit.getReservaKit().getStatus().equals(EmprestimoKit.ENTREGUE)) {
                                    emprestado.setLocal("Kit emprestado - entregue");
                                }
                                emprestado.setId(kit.getId());
                                emprestado.setDetalhes(kit.getReservaKit().getDetalhamento());
                                emprestado.setQuantidade(kit.getQuantidade());
                                //TODO trocar para objetos local?   
                                if(!this.emprestados.contains(emprestado)) {
                                    this.emprestados.add(emprestado);    
                                }  
                            }
                        }  
                    
                    }
               
                }
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }           
        }    
            
    }
    
    public void montaUnitarioEmprestados(List<Estoque> estoques) {
        for (Estoque estoque : estoques) {
            List<EmprestimoUnitario> unitarios;
            try {
                unitarios = EmprestimoUnitarioSingleton.getInstance().getBo().listByEmpresaAndMaterialComQuantidade(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa(),estoque.getMaterial());
                if(unitarios != null && !unitarios.isEmpty()) {
                    for (EmprestimoUnitario item : unitarios) {                    
                        if(item.getMaterial().getExcluido().equals("N")) {
                            MateriaisEmprestados emprestado = new MateriaisEmprestados();
                            emprestado.setId(item.getId());
                            emprestado.setLocal("Empréstimo unitário");
                            emprestado.setDetalhes(item.getDetalhamento());
                            emprestado.setQuantidade(item.getQuantidade());                                      
                            if(!this.emprestados.contains(emprestado)) {
                                this.emprestados.add(emprestado);    
                            }
                               
                        }  
                    
                    }                  
               
                }
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }           
        }    
            
    }
    
    public void montaEmLavagem(Item item) throws Exception {
//        List<Estoque> estoques = EstoqueSingleton.getInstance().getParaLavagemPorItem(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa(),item);
//        for (Estoque estoque : estoques) {
//            if(estoque.getQuantidade().compareTo(new BigDecimal(0)) > 0) {
//                MateriaisEmprestados emprestado = new MateriaisEmprestados();
//                emprestado.setLocal("Lavagem");
//                emprestado.setDetalhes("Material separado para lavagem");
//                emprestado.setQuantidade(estoque.getQuantidade());
//                emprestado.setId(estoque.getId());     
//                this.emprestados.add(emprestado); 
//            }
//        }
       estoques = EstoqueSingleton.getInstance().getEmLavagemPorItem(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa(),item);
        for (Estoque estoque : estoques) {      
            if(estoque.getQuantidade().compareTo(new BigDecimal(0)) > 0) {
                MateriaisEmprestados emprestado = new MateriaisEmprestados();
                emprestado.setLocal("Lavagem");
                emprestado.setDetalhes("Material em processo de lavagem");
                emprestado.setQuantidade(estoque.getQuantidade());
                emprestado.setId(estoque.getId());  
                this.emprestados.add(emprestado);    
            }
        }    
            
    }
    
    public void montaEmEsterilizacao(Item item) throws Exception {
        List<Estoque> estoques = EstoqueSingleton.getInstance().getParaEsterilizacaoPorItem(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa(),item);
        for (Estoque estoque : estoques) {
            if(estoque.getQuantidade().compareTo(new BigDecimal(0)) > 0) {
                MateriaisEmprestados emprestado = new MateriaisEmprestados();
                emprestado.setLocal("Esterilização");
                emprestado.setDetalhes("Material separado para esterilização");
                emprestado.setQuantidade(estoque.getQuantidade());
                emprestado.setId(estoque.getId());   
                this.emprestados.add(emprestado);     
            }
               
        }
       estoques = EstoqueSingleton.getInstance().getEmEsterilizacaoPorItem(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa(),item);
        for (Estoque estoque : estoques) {  
            if(estoque.getQuantidade().compareTo(new BigDecimal(0)) > 0) {
                MateriaisEmprestados emprestado = new MateriaisEmprestados();
                emprestado.setLocal("Esterilização");
                emprestado.setDetalhes("Material em processo de esterilização");
                emprestado.setQuantidade(estoque.getQuantidade());
                emprestado.setId(estoque.getId());  
                this.emprestados.add(emprestado);
            }
        }    
            
    }    
    
    public void exportarTabela(String type) {
        exportarTabela("Estoque mínimo", tabelaEstoque, type);
    }
    
    public void filtra() {  
       
        //Local localDestino = LocalSingleton.getInstance().getBo().getLocalPorDescricao(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa(),  getLocal);
        
        
        //this.materiais = RelatorioEstoqueMinimoSingleton.getInstance().getBo().listAllByFilterToReportGroupByItemFiltrado(mostrarSomenteEstoqueMinimo, filtroItem, filtroTipo,
        //        UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
        
        this.estoques = EstoqueSingleton.getInstance().getBo().listAllByFilterToReportGroupByItemFiltrado( filtroItem, filtroTipo,
                UtilsFrontEnd.getProfissionalLogado().getIdEmpresa(),local);
        
        
    }   
    

//    public List<RelatorioEstoqueMinimo> getMateriais() {
//        return this.materiais;
//    }
//
//    public void setMateriais(List<RelatorioEstoqueMinimo> materiais) {
//        this.materiais = materiais;
//    }

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

    
    public List<TransferenciaEstoque> getListaTransferenciasEstoque() {
        return listaTransferenciasEstoque;
    }

    
    public void setListaTransferenciasEstoque(List<TransferenciaEstoque> listaTransferenciasEstoque) {
        this.listaTransferenciasEstoque = listaTransferenciasEstoque;
    }

    
    public List<Local> getLocais() {
        return locais;
    }

    
    public void setLocais(List<Local> locais) {
        this.locais = locais;
    }

    
    public String getLocal() {
        return local;
    }

    
    public void setLocal(String local) {
        this.local = local;
    }

    
    public List<Estoque> getEstoques() {
        return estoques;
    }

    
    public void setEstoques(List<Estoque> estoques) {
        this.estoques = estoques;
    }

}
