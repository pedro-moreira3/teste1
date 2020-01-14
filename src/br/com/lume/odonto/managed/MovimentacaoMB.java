package br.com.lume.odonto.managed;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ActionEvent;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.log4j.Logger;
import org.primefaces.PrimeFaces;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.event.NodeSelectEvent;
import org.primefaces.event.NodeUnselectEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.dadosBasico.DadosBasicoSingleton;
import br.com.lume.dominio.DominioSingleton;
import br.com.lume.estoque.EstoqueSingleton;
import br.com.lume.fornecedor.FornecedorSingleton;
import br.com.lume.item.ItemSingleton;
import br.com.lume.local.LocalSingleton;
import br.com.lume.marca.MarcaSingleton;
import br.com.lume.material.MaterialSingleton;
import br.com.lume.odonto.entity.DadosBasico;
import br.com.lume.odonto.entity.Dominio;
import br.com.lume.odonto.entity.Estoque;
import br.com.lume.odonto.entity.Fornecedor;
import br.com.lume.odonto.entity.Item;
import br.com.lume.odonto.entity.Local;
import br.com.lume.odonto.entity.Marca;
import br.com.lume.odonto.entity.Material;
import br.com.lume.odonto.entity.TransferenciaEstoque;
import br.com.lume.odonto.util.OdontoMensagens;
import br.com.lume.transferenciaEstoque.TransferenciaEstoqueSingleton;

@ManagedBean
@ViewScoped
public class MovimentacaoMB extends LumeManagedBean<Estoque> {

    private static final long serialVersionUID = 1L;

    private Dominio procedencia;

    private List<Dominio> procedencias;

    private Logger log = Logger.getLogger(MovimentacaoMB.class);

    private Item item;

    private Local local;

    private List<Item> itens;

    private List<Local> locais;

    private String digitacao, digitacaoLocal, nomeMarca, nomeFornecedor;

    private TreeNode root, rootLocal, selectedItem, selectedLocal;

    private List<Marca> marcas;

    private Date dateHoje;

   // private List<Material> materiais = new ArrayList<>();
    
    private List<Estoque> estoques = new ArrayList<>();

    private boolean caixa, novaMarca, novoFornecedor;

    private Dominio justificativa;

    private List<Dominio> dominios;

  //  private BigDecimal valorTotal = new BigDecimal(0), quantidadeMovimentada, quantidadePacotes, quantidadeMovimentacao;
    
    private BigDecimal quantidadeMovimentada;

    private List<TransferenciaEstoque> listaTransferenciasEstoque;

    private String tipoMovimentacao;
    
    //EXPORTAÇÃO TABELA
    private DataTable tabelaMaterial;
    private DataTable tabelaMovimentacao;

    public MovimentacaoMB() {
        super(EstoqueSingleton.getInstance().getBo());
      
        this.setCaixa(true);
        this.setClazz(Estoque.class);
        this.geraLista();
        try {
            this.setProcedencias(this.getPrecedencias());
            this.setMarcas(MarcaSingleton.getInstance().getBo().listByEmpresa(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa()));
            this.setRoot(new DefaultTreeNode("", null));
            Item firstLevel = new Item();
            firstLevel.setDescricao("RAIZ");
            this.chargeTree(new DefaultTreeNode(firstLevel, this.getRoot()));
            this.setRootLocal(new DefaultTreeNode("", null));
            Local firstLevelLocal = new Local();
            firstLevelLocal.setDescricao("RAIZ");
            this.chargeTreeLocal(new DefaultTreeNode(firstLevelLocal, this.getRootLocal()));
            dateHoje = new Date();
        } catch (Exception e) {
            log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
            this.addError(Mensagens.ERRO_AO_BUSCAR_REGISTROS, "",true);
        }
    }

    private void geraLista() {
        try {
            estoques = EstoqueSingleton.getInstance().getBo().listAllByEmpresa(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
            Collections.sort(estoques);
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "",true);
            log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
        }
    }


    public void chargeTree(TreeNode root) {
        List<TreeNode> nodes = new ArrayList<>();
        List<TreeNode> nodesAux;
        this.filtraItens();
        List<Item> itensRestantes = this.getItens();
        root.setExpanded(true);
        nodes.add(root);
        itensRestantes = this.setLevel(itensRestantes, nodes);
        List<TreeNode> subNodes;
        while (itensRestantes.size() > 0) {
            subNodes = new ArrayList<>();
            nodesAux = new ArrayList<>();
            nodesAux.addAll(nodes);
            for (TreeNode node : nodesAux) {
                subNodes.addAll(node.getChildren());
            }
            itensRestantes = this.setLevel(itensRestantes, subNodes);
            nodes = new ArrayList<>();
            nodes.addAll(subNodes);
        }
    }

    public void carregaTela() {
        try {
           // this.setDigitacaoLocal(this.getEntity().getLocal().getDescricao());
            this.setLocal(this.getEntity().getLocal());
            this.setProcedencia(DominioSingleton.getInstance().getBo().findByEmpresaAndObjetoAndTipoAndValor(OBJETO, TIPO, this.getEntity().getMaterial().getProcedencia()));
            this.setItem(this.getEntity().getMaterial().getItem());
            this.setDigitacao(this.getEntity().getMaterial().getItem().getDescricao());
            this.setSelectedItem();
            this.setSelectedLocal();
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "",true);
            log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
        }
    }

    public void setSelectedItem() {
        List<TreeNode> nodes = new ArrayList<>();
        List<TreeNode> nodesAux = new ArrayList<>();
        Item itemSelecionado;
        boolean hasChildren = true;
        nodes.add(this.getRoot());
        while (hasChildren) {
            nodesAux = new ArrayList<>();
            for (TreeNode node : nodes) {
                Object item = node.getData();
                if (this.getItem() != null) {
                    itemSelecionado = this.getItem();
                } else {
                    itemSelecionado = new Item();
                    itemSelecionado.setDescricao(this.getDigitacao());
                }
                node.setSelected(false);
                if (((item instanceof String) && (item.equals(itemSelecionado.getDescricao()))) || // RAIZ?
                        ((item instanceof Item) && (((Item) item).getDescricao().equals(itemSelecionado.getDescricao())))) { // Encontrou o
                                                                                                                            // Node?
                    node.setSelected(true);
                    this.setItem(((Item) item));
                    this.setSelectedItem(node);
                }
                nodesAux.addAll(node.getChildren());
            }
            if (nodesAux.size() > 0) {
                nodes = nodesAux;
            } else {
                hasChildren = false;
            }
        }
    }

    public void carregarMaterialLog(Material material) {
        try {
            listaTransferenciasEstoque = TransferenciaEstoqueSingleton.getInstance().getBo().listByMaterial(material);               
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "",true);
            log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
        }
    }
        

    public void chargeTreeLocal(TreeNode root) {
        List<TreeNode> nodes = new ArrayList<>();
        List<TreeNode> nodesAux;
        this.filtraLocais();
        List<Local> locaisRestantes = this.getLocais();
        root.setExpanded(true);
        nodes.add(root);
        locaisRestantes = this.setLevelLocal(locaisRestantes, nodes);
        List<TreeNode> subNodes;
        while (locaisRestantes.size() > 0) {
            subNodes = new ArrayList<>();
            nodesAux = new ArrayList<>();
            nodesAux.addAll(nodes);
            for (TreeNode node : nodesAux) {
                subNodes.addAll(node.getChildren());
            }
            locaisRestantes = this.setLevelLocal(locaisRestantes, subNodes);
            nodes = new ArrayList<>();
            nodes.addAll(subNodes);
        }
    }

    public List<Item> setLevel(List<Item> itensRestantes, List<TreeNode> nodes) {
        boolean anotherLevel;
        List<Item> locaisRestantesAux = new ArrayList<>();
        for (Item item : itensRestantes) {
            anotherLevel = true;
            for (TreeNode node : nodes) {
                if ((item.getIdItemPai() == null) || (item.getIdItemPai().equals(node.getData()))) {
                    (new DefaultTreeNode(item, node)).setExpanded(true);
                    anotherLevel = false;
                    break;
                }
            }
            if (anotherLevel) {
                locaisRestantesAux.add(item);
            }
        }
        return locaisRestantesAux;
    }

    public List<Local> setLevelLocal(List<Local> locaisRestantes, List<TreeNode> nodes) {
        boolean anotherLevel;
        List<Local> locaisRestantesAux = new ArrayList<>();
        for (Local local : locaisRestantes) {
            anotherLevel = true;
            for (TreeNode node : nodes) {
                if ((local.getLocalPai() == null) || (local.getLocalPai().equals(node.getData()))) {
                    (new DefaultTreeNode(local, node)).setExpanded(true);
                    anotherLevel = false;
                    break;
                }
            }
            if (anotherLevel) {
                locaisRestantesAux.add(local);
            }
        }
        return locaisRestantesAux;
    }
    
    public void actionPersistFechar(ActionEvent event){
        this.actionPersist(event);
        PrimeFaces.current().executeScript("PF('dlgEntrada').hide();");
    }

    public void movimentar(ActionEvent event) {
        if (quantidadeMovimentada.equals(new BigDecimal(0))) {
            this.addError(OdontoMensagens.getMensagem("erro.material.quantidade.zerada"), "",true);
        } else if (this.getEntity().getQuantidade().doubleValue() < quantidadeMovimentada.doubleValue()) {
            this.addError(OdontoMensagens.getMensagem("erro.material.quantidade"), "",true);
        } else if ((this.getLocal() == null)) {
            this.addError(OdontoMensagens.getMensagem("erro.local.material.raiz"), "",true);
        } else if (this.getEntity().getLocal().getDescricao().equals(this.getDigitacaoLocal())) {
            this.addError(OdontoMensagens.getMensagem("erro.local.material.inalterado"), "",true);
        } else {
            try {                  
                if(this.getLocal().equals(this.getEntity().getLocal())) {
                    this.addError("Local de origem nao pode ser o mesmo do local de destino", "",true);  
                }else {
                    //atualizando somente o estoque, caso tenha sido alterada por outro usuário por exempl
                    setEntity(EstoqueSingleton.getInstance().getBo().find(getEntity().getId())); 
                    this.getEntity().getMaterial().setDataMovimentacao(new Date());
                    EstoqueSingleton.getInstance().transferencia(this.getEntity().getMaterial(),  this.getEntity().getLocal(), this.getLocal(), quantidadeMovimentada, EstoqueSingleton.MOVIMENTACAO_MATERIAL_MOVIMENTAR, UtilsFrontEnd.getProfissionalLogado()); 
                    this.addInfo(OdontoMensagens.getMensagem("material.salvo.movimentado"), "",true);
                    this.actionNew(event);
                    this.geraLista();
                }
               
            } catch (Exception e) {
                log.error("Erro no actionPersist", e);
                this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "",true);
            }
        }
    }
    
    public String getUnidadeString(Item item) {
        if(item != null)
            return DominioSingleton.getInstance().getBo().getUnidadeMedidaString(item.getUnidadeMedida());
        return null;
    }
    
    public void exportarTabela(String type) {
        exportarTabela("Entrada de Materiais", tabelaMaterial, type);
    }
    
    public void exportarTabelaMovimentacao(String type) {
        exportarTabela("Movimentação de materiais", tabelaMovimentacao, type);
    }

    public List<Local> getLocais() {
        return locais;
    }

    public void setLocais(List<Local> locais) {
        this.locais = locais;
    }

    public List<Marca> getMarcas() {
        Collections.sort(marcas);
        return marcas;
    }

    public TreeNode getSelectedLocal() {
        return selectedLocal;
    }

    public void setSelectedLocal(TreeNode selectedLocal) {
        this.selectedLocal = selectedLocal;
    }

    public TreeNode getRoot() {
        return root;
    }

    public void setRoot(TreeNode root) {
        this.root = root;
    }

    public TreeNode getRootLocal() {
        return rootLocal;
    }

    public void setRootLocal(TreeNode rootLocal) {
        this.rootLocal = rootLocal;
    }

    public TreeNode getSelectedItem() {
        return selectedItem;
    }

    public void setSelectedItem(TreeNode selectedItem) {
        this.selectedItem = selectedItem;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public List<Item> getItens() {
        return itens;
    }

    public void setItens(List<Item> itens) {
        this.itens = itens;
    }

    public String getDigitacao() {
        //return getItem() != null ? getItem().getDescricao() : digitacao;
        return digitacao;
    }

    public void setDigitacao(String digitacao) {
        this.digitacao = digitacao;
    }

    public void setMarcas(List<Marca> marcas) {
        this.marcas = marcas;
    }

    public List<Estoque> getEstoques() {
        return estoques;
    }

    public List<String> filtraItem(String digitacao) {
        this.setDigitacao(digitacao);        
        this.filtraItens();     
        return this.convert(this.getItens());                 
    }

    public List<String> filtraLocal(String digitacao) {
        this.setDigitacaoLocal(digitacao);
        this.filtraLocais();
        return this.convert(this.getLocais());
    }

    public void filtraLocais() {
        this.setLocais(new ArrayList<Local>());
        try {
            if (this.getDigitacaoLocal() != null) {
                this.setLocais(LocalSingleton.getInstance().getBo().listByEmpresaAndDescricaoParcial(this.getDigitacaoLocal(), UtilsFrontEnd.getProfissionalLogado().getIdEmpresa()));
            } else {
                this.setLocais(LocalSingleton.getInstance().getBo().listByEmpresa(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa()));
            }
            Collections.sort(this.getLocais());
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "",true);
            log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
        }
    }

    public void filtraItens() {
        this.setItens(new ArrayList<Item>());
        try {
            if (this.getDigitacao() != null) {
                this.setItens(ItemSingleton.getInstance().getBo().listByEmpresaAndDescricaoParcial(this.getDigitacao(), UtilsFrontEnd.getProfissionalLogado().getIdEmpresa()));
            } else {
                this.setItens(ItemSingleton.getInstance().getBo().listByEmpresa(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa()));
            }
            Collections.sort(itens);
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "",true);
            log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
        }
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

    public void setSelected() {
        List<TreeNode> nodes = new ArrayList<>();
        List<TreeNode> nodesAux = new ArrayList<>();
        boolean hasChildren = true;
        nodes.add(this.getRoot());
        while (hasChildren) {
            nodesAux = new ArrayList<>();
            for (TreeNode node : nodes) {
                Object item = node.getData();
                Item itemSelecionado;
                if (this.getItem() != null) {
                    itemSelecionado = this.getItem();
                } else {
                    itemSelecionado = new Item();
                    itemSelecionado.setDescricao(this.getDigitacao());
                }
                node.setSelected(false);
                if (((item instanceof String) && (item.equals(itemSelecionado.getDescricao()))) || // RAIZ?
                        ((item instanceof Item) && (((Item) item).getDescricao().equals(itemSelecionado.getDescricao())))) { // Encontrou o
                                                                                                                            // Node?
                    node.setSelected(true);
                    this.setItem(((Item) item));
                    this.setSelectedItem(node);
                }
                nodesAux.addAll(node.getChildren());
            }
            if (nodesAux.size() > 0) {
                nodes = nodesAux;
            } else {
                hasChildren = false;
            }
        }
    }

    public void setSelectedLocal() {
        List<TreeNode> nodes = new ArrayList<>();
        List<TreeNode> nodesAux = new ArrayList<>();
        boolean hasChildren = true;
        nodes.add(this.getRootLocal());
        while (hasChildren) {
            nodesAux = new ArrayList<>();
            for (TreeNode node : nodes) {
                Object local = node.getData();
                Local localSelecionado;
                if (this.getLocal() != null) {
                    localSelecionado = this.getLocal();
                } else {
                    localSelecionado = new Local();
                    localSelecionado.setDescricao(this.getDigitacaoLocal());
                }
                node.setSelected(false);
                if (((local instanceof String) && (local.equals(localSelecionado.getDescricao()))) || // RAIZ?
                        ((local instanceof Local) && (((Local) local).getDescricao().equals(localSelecionado.getDescricao()))
                                // && // Encontrou
                                  // o Node?
                                  // ((((Local)local).getIdLocal()==null && (localSelecionado.getIdLocal()==null))|| // Mesmo Pai Raiz
                                // ((((Local)local).getIdLocal().getDescricao().equals(localSelecionado.getIdLocal().getDescricao()))))
                        )) { // Mesmo Pai
                    node.setSelected(true);
                    this.setLocal(((Local) local));
                    this.setSelectedLocal(node);
                }
                nodesAux.addAll(node.getChildren());
            }
            if (nodesAux.size() > 0) {
                nodes = nodesAux;
            } else {
                hasChildren = false;
            }
        }
    }

    public List<Dominio> getPrecedencias() {
        List<Dominio> dominios = null;
        try {
            dominios = DominioSingleton.getInstance().getBo().listByEmpresaAndObjetoAndTipo(OBJETO, TIPO);
        } catch (Exception e) {
            log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
        }
        return dominios;
    }

    public void onNodeSelect(NodeSelectEvent event) {
        this.setItem((Item) (event.getTreeNode().getData()));
        this.getSelectedItem().setSelected(false);
        this.setSelectedItem(event.getTreeNode());
        this.getSelectedItem().setSelected(true);
        this.setDigitacao(this.getItem().getDescricao());
        this.filtraItem(this.getDigitacao());
    }

    public void onNodeUnselect(NodeUnselectEvent event) {
        this.setItem(null);
        this.getSelectedItem().setSelected(false);
        this.setSelectedItem(null);
    }
    
     public void handleSelectLocal() {
      this.filtraLocal(this.getDigitacaoLocal());
     this.setLocal(null);
      this.setSelectedLocal();
      }

    public void onNodeSelectLocal(NodeSelectEvent event) {
        this.setLocal((Local) (event.getTreeNode().getData()));
        this.getSelectedLocal().setSelected(false);
        this.setSelectedLocal(event.getTreeNode());
        this.getSelectedLocal().setSelected(true);
        this.setDigitacao(this.getLocal().getDescricao());
        this.setDigitacaoLocal(this.getLocal().getDescricao());
        this.filtraItem(this.getDigitacao());
    }

    public void onNodeUnselectLocal(NodeUnselectEvent event) {
        this.getSelectedLocal().setSelected(false);
        this.setLocal(null);
    }

    public List<Marca> geraSugestoes(String query) {
        List<Marca> sugestoes = new ArrayList<>();
        List<Marca> marcas = new ArrayList<>();
        try {
            marcas = MarcaSingleton.getInstance().getBo().listByEmpresa(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
            for (Marca m : marcas) {
                if (Normalizer.normalize(m.getNome().toLowerCase(), Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "").contains(
                        Normalizer.normalize(query.toLowerCase(), Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", ""))) {
                    sugestoes.add(m);
                }
            }
            Collections.sort(sugestoes);
        } catch (Exception e) {
            log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS);
        }
        return sugestoes;
    }
    
    

    public List<Dominio> getJustificativas() {
        List<Dominio> justificativas = new ArrayList<>();
        try {
            justificativas = DominioSingleton.getInstance().getBo().listByEmpresaAndObjetoAndTipo("material", "justificativa");
        } catch (Exception e) {
            log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS);
        }
        return justificativas;
    }

    public void novo() {
        this.setNovaMarca(true);
    }

    public void cancelaNovo(ActionEvent event) {
        this.setNovaMarca(false);
    }
    
    public void novoFornecedor() {
        this.setNovoFornecedor(true);
    }

    public void cancelaNovoFornecedor(ActionEvent event) {
        this.setNovoFornecedor(false);
    }    


    public Dominio getProcedencia() {
        return procedencia;
    }

    public void setProcedencia(Dominio procedencia) {
        this.procedencia = procedencia;
    }

    public List<Dominio> getProcedencias() {
        return procedencias;
    }

    public void setProcedencias(List<Dominio> procedencias) {
        this.procedencias = procedencias;
    }

    public String getDigitacaoLocal() {
        //return getLocal() != null ? getLocal().getDescricao() : digitacaoLocal;
        return digitacaoLocal;
    }

    public void setDigitacaoLocal(String digitacaoLocal) {
        this.digitacaoLocal = digitacaoLocal;
    }

    public Local getLocal() {
        return local;
    }

    public void setLocal(Local local) {
        this.local = local;
    }

    public static final String TIPO = "procedencia";

    public static final String OBJETO = "item";

    public static final String ATIVO = "A";

    public static final String DEVOLVIDO = "D";

    public static final String DESCARTE = "DE";

    public Date getDateHoje() {
        return dateHoje;
    }

    public void setDateHoje(Date dateHoje) {
        this.dateHoje = dateHoje;
    }

    public boolean isCaixa() {
        return caixa;
    }

    public void setCaixa(boolean caixa) {
        this.caixa = caixa;
    }

    public boolean isNovaMarca() {
        return novaMarca;
    }

    public void setNovaMarca(boolean novaMarca) {
        this.novaMarca = novaMarca;
    }

    public String getNomeMarca() {
        return nomeMarca;
    }

    public void setNomeMarca(String nomeMarca) {
        this.nomeMarca = nomeMarca;
    }

    public Dominio getJustificativa() {
        return justificativa;
    }

    public void setJustificativa(Dominio justificativa) {
        this.justificativa = justificativa;
    }

    public List<Dominio> getDominios() {
        return dominios;
    }

    public void setDominios(List<Dominio> dominios) {
        this.dominios = dominios;
    }

    public BigDecimal getQuantidadeMovimentada() {
        return quantidadeMovimentada;
    }

    public void setQuantidadeMovimentada(BigDecimal quantidadeMovimentada) {
        this.quantidadeMovimentada = quantidadeMovimentada;
    }

    public String getTipoMovimentacao() {
        return tipoMovimentacao;
    }

    public void setTipoMovimentacao(String tipoMovimentacao) {
        this.tipoMovimentacao = tipoMovimentacao;
    }
 
    public boolean isNovoFornecedor() {
        return novoFornecedor;
    }

    
    public void setNovoFornecedor(boolean novoFornecedor) {
        this.novoFornecedor = novoFornecedor;
    }

    
    public String getNomeFornecedor() {
        return nomeFornecedor;
    }

    
    public void setNomeFornecedor(String nomeFornecedor) {
        this.nomeFornecedor = nomeFornecedor;
    }

    
    public List<TransferenciaEstoque> getListaTransferenciasEstoque() {
        return listaTransferenciasEstoque;
    }

    
    public void setListaTransferenciasEstoque(List<TransferenciaEstoque> listaTransferenciasEstoque) {
        this.listaTransferenciasEstoque = listaTransferenciasEstoque;
    }

    public DataTable getTabelaMaterial() {
        return tabelaMaterial;
    }

    public void setTabelaMaterial(DataTable tabelaMaterial) {
        this.tabelaMaterial = tabelaMaterial;
    }

    public DataTable getTabelaMovimentacao() {
        return tabelaMovimentacao;
    }

    public void setTabelaMovimentacao(DataTable tabelaMovimentacao) {
        this.tabelaMovimentacao = tabelaMovimentacao;
    }

}

