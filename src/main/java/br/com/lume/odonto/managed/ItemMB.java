package br.com.lume.odonto.managed;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ActionEvent;

import org.apache.log4j.Logger;
import org.primefaces.PrimeFaces;
import org.primefaces.event.NodeSelectEvent;
import org.primefaces.event.NodeUnselectEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.Status;
import br.com.lume.common.util.Utils;
import br.com.lume.common.util.UtilsFrontEnd;

import br.com.lume.dominio.DominioSingleton;
import br.com.lume.item.ItemSingleton;
import br.com.lume.material.MaterialSingleton;
import br.com.lume.odonto.entity.Dominio;
import br.com.lume.odonto.entity.Fornecedor;
import br.com.lume.odonto.entity.Item;
import br.com.lume.odonto.entity.Material;
import br.com.lume.odonto.util.OdontoMensagens;

@ManagedBean
@ViewScoped
public class ItemMB extends LumeManagedBean<Item> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(ItemMB.class);

    private List<Dominio> formasArmazenamento, utilizacoes, fracoesUnitarias, unidadesMedida;

    private Dominio formaArmazenamento, utilizacao, fracaoUnitaria, unidadeMedida;

    private String descricao;

    private TreeNode root, rootPai, selectedItem, selectedItemPai;

    private boolean hasChildren, disable, disableItem;

    private String categoria;

    private String digitacao;

    private List<Item> itens;

    private Item item;

    private String filtroTable,filtroItemCategoria;

    private List<Item> listByPai = new ArrayList<>();

    List<TreeNode> nodes, nodesAux;


    public ItemMB() {
        super(ItemSingleton.getInstance().getBo());

        this.setClazz(Item.class);
        categoria = "N";
        this.setDisable(false);
        
        try {
           //    this.setItens(ItemSingleton.getInstance().getBo().listByEmpresa(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa()));
            this.setFormasArmazenamento(this.getDominios(FORMA_ARMAZENAMENTO));
            this.setUtilizacoes(this.getDominios(UTILIZACAO));
            this.setFracoesUnitarias(this.getDominios(FRACAO_UNITARIA));
            this.setUnidadesMedida(this.getDominios(UNIDADE_MEDIDA));
           // this.setRoot(new DefaultTreeNode("", null));
            this.setRootPai(new DefaultTreeNode("", null));
            Item firstLevel = new Item();
            firstLevel.setDescricao("RAIZ");
            this.chargeTree(new DefaultTreeNode(firstLevel, rootPai), true, false);
            filtroTable = "";
          //  this.actionTodos(null);
        } catch (Exception e) {
            log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
        }
    }
    
    public void carregarEditar(Item item) {
        setEntity(item);
        this.setItem(null);
        if(item.getIdItemPai() != null) {
            setDigitacao(item.getIdItemPai().getDescricao());    
        }
        setCategoria(item.getCategoria());
        setDescricao(item.getDescricao());
        setParametros();

        if (item.getIdItemPai() != null) {
            this.setDigitacao(item.getIdItemPai().getDescricao());
        } else {
            this.setDigitacao(null);
        }
       // this.setSelected();
        
        PrimeFaces.current().executeScript("PF('dlg').show();");
    }   
    
    public void carregarExcluir(Item item) {
        setEntity(item);
    }

    @Override
    public void actionPersist(ActionEvent event) {
        boolean error = false;
        this.getEntity().setIdEmpresa(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
        if (this.getFormaArmazenamento() != null) {
            this.getEntity().setFormaArmazenamento(this.getFormaArmazenamento().getValor());
        }
        if (this.getUtilizacao() != null) {
            this.getEntity().setUtilizacao(this.getUtilizacao().getValor());
        }
        if (this.getFracaoUnitaria() != null) {
            this.getEntity().setFracaoUnitaria(this.getFracaoUnitaria().getValor());
        }
        if (this.getUnidadeMedida() != null) {
            this.getEntity().setUnidadeMedida(this.getUnidadeMedida().getValor());
        }
        this.getEntity().setCategoria(categoria);
        if (this.getItem() != null) {
            if (this.getItem().getDescricao().equals("RAIZ")) {
                this.getEntity().setIdItemPai(null);
            } else {
                if (this.getItem().getCategoria().equals(Status.NAO)) {
                    log.error("erro.categoria.item");
                    this.addError(OdontoMensagens.getMensagem("erro.categoria.item"), "",true);
                    error = true;
                }
                if (!this.getItem().getTipo().equals(this.getEntity().getTipo())) {
                    this.addError("O tipo da Categoria (" + this.getItem().getTipoStr() + ") é diferente do tipo do Item (" + this.getEntity().getTipoStr() + ")", "");
                    error = true;
                    return;
                }
                this.getEntity().setIdItemPai(this.getItem());
            }
        }
        try {
            for (Item item : ItemSingleton.getInstance().getBo().listByEmpresa(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa())) {
                if (item.getDescricao().equalsIgnoreCase(this.getDescricao()) && item.getId() != this.getEntity().getId()) {
                    log.error("erro.categoria.duplicidade");
                    this.addError(OdontoMensagens.getMensagem("erro.categoria.duplicidade"), "",true);
                    error = true;
                    break;
                }
            }
        } catch (Exception e) {
            log.error(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "",true);
        }
        this.getEntity().setPedidoItens(null);
        if (!error) {
            if (this.getEntity().equals(this.getEntity().getIdItemPai())) {
                log.error("erro.pai.filho.itens.iguais");
                this.addError(OdontoMensagens.getMensagem("erro.pai.filho.itens.iguais"), "",true);
            } else {
                this.getEntity().setDescricao(this.getDescricao());
                this.getEntity().setExcluido(Status.NAO);
                super.actionPersist(event);
            }
        }
    }

    @Override
    public void actionNew(ActionEvent event) {
        super.actionNew(event);
        this.setDisable(false);
    }

    public void removeItens() {
        try {
           // for (Item item : listByPai) {
             //   this.removeRecursivo(item);
            //    ItemSingleton.getInstance().getBo().remove(item);
          //  }
            ItemSingleton.getInstance().getBo().remove(this.getEntity());
            this.actionNew(null);
            this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_REMOVIDO_COM_SUCESSO), "",true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    private void removeRecursivo(Item item) throws Exception {
//        List<Item> listByPaiAux = ItemSingleton.getInstance().getBo().listByPai(item.getId(), UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
//        if (listByPaiAux != null && !listByPaiAux.isEmpty()) {
//            for (Item itemAux : listByPaiAux) {
//                this.removeRecursivo(itemAux);
//            }
//            for (Item itemAux : listByPaiAux) {
//                ItemSingleton.getInstance().getBo().remove(item);
//            }
//        } else {
//            ItemSingleton.getInstance().getBo().remove(item);
//        }
//    }

    @Override
    public void actionRemove(ActionEvent arg0) {
        try {
            listByPai = ItemSingleton.getInstance().getBo().listByPai(this.getEntity().getId(), UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
            if (listByPai != null && !listByPai.isEmpty()) {
                this.addError("Não é possível excluir itens com itens filhos, remova-os antes.", "",true);
            } else {
                List<Material> materiais = MaterialSingleton.getInstance().getBo().listByItem(this.getEntity());
                if (materiais != null && !materiais.isEmpty()) {
                    this.addError("Não é possível excluir itens com materiais vinculados.", "",true);
                } else {
                    super.actionRemove(arg0);
                }
            }
        } catch (Exception e) {
            log.error(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "",true);
        }
    }

    public void alteraCategoria() {
        if (this.getCategoria().equalsIgnoreCase("S")) {
            this.setDisableItem(true);
        } else {
            this.setDisableItem(false);
        }
    }

    public void carregaTela() {
        this.setItem(null);
      //  Item itemAux = ((Item) this.getSelectedItem().getData());
      //  if (itemAux.getIdItemPai() != null) {
      //      this.setDigitacao(itemAux.getIdItemPai().getDescricao());
      //  } else {
            this.setDigitacao(null);
    //    }
      //  this.setSelected();
        try {
          //  this.setEntity((Item) selectedItem.getData());
            this.setParametros();
            this.setDescricao(this.getEntity().getDescricao());
        } catch (Exception e) {
            log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
        }
        this.setDisable(true);
    }

    public void setSelected() {
        nodes = new ArrayList<>();
        nodesAux = new ArrayList<>();
        hasChildren = true;
        nodes.add(this.getRootPai());
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
                    this.setDisable(true);
                    this.setSelectedItemPai(node);
                    node.setSelected(true);
                    this.setItem(((Item) item));
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

    public void chargeTree(TreeNode root, boolean categoria, boolean filtro) {
        List<TreeNode> nodes = new ArrayList<>();
        List<TreeNode> nodesAux;
      //  if (categoria) {
      //      this.filtraCategorias();
      //  } else {
      //      this.filtraItens(filtro);
     //   }
        List<Item> locaisRestantes = new ArrayList<Item>();
        try {
            locaisRestantes = ItemSingleton.getInstance().getBo().listByEmpresaTipoCategoriaDescricao(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa(),"S",null);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        root.setExpanded(true);
        nodes.add(root);
        locaisRestantes = this.setLevel(locaisRestantes, nodes, categoria);
        List<TreeNode> subNodes;
        while (locaisRestantes.size() > 0) {
            subNodes = new ArrayList<>();
            nodesAux = new ArrayList<>();
            nodesAux.addAll(nodes);
            for (TreeNode node : nodesAux) {
                subNodes.addAll(node.getChildren());
            }
            locaisRestantes = this.setLevel(locaisRestantes, subNodes, categoria);
            nodes = new ArrayList<>();
            nodes.addAll(subNodes);
        }
    }

    public void eliminaCategoriasVazias(TreeNode tree) {
        List<TreeNode> treeAux = new ArrayList<>();
        treeAux.addAll(tree.getChildren());
        boolean categoria;
        for (TreeNode n : treeAux) {
            if (((Item) n.getData()).getDescricao().equals("RAIZ")) {
                categoria = true;
            } else {
                categoria = (((Item) n.getData()).getCategoria().equals(Status.SIM));
            }
            if (((n.getChildren().isEmpty() || this.isTreeEmpty(n)) && categoria)) {
                if (!((Item) n.getData()).getDescricaoLimpa().toUpperCase().contains(filtroTable.toUpperCase())) {
                    tree.getChildren().remove(n);
                }
            } else if (!n.getChildren().isEmpty()) {
                this.eliminaCategoriasVazias(tree.getChildren().get(tree.getChildren().indexOf(n)));
            }
        }
    }

    public boolean isTreeEmpty(TreeNode tree) {
        boolean retorno = true;
        for (TreeNode n : tree.getChildren()) {
            if (((Item) n.getData()).getCategoria().equals(Status.NAO)) {
                retorno = false;
            } else if (((Item) n.getData()).getDescricaoLimpa().toUpperCase().contains(filtroTable.toUpperCase())) {
                retorno = false;
            } else if (!n.getChildren().isEmpty()) {
                retorno = this.isTreeEmpty(tree.getChildren().get(tree.getChildren().indexOf(n)));
            }
            if (!retorno) {
                return retorno;
            }
        }
        return retorno;
    }

    public List<Item> setLevel(List<Item> locaisRestantes, List<TreeNode> nodes, boolean categoria) {
        boolean anotherLevel;
        List<Item> locaisRestantesAux = new ArrayList<>();
        for (Item item : locaisRestantes) {
            anotherLevel = true;
            for (TreeNode node : nodes) {
                if ((item.getIdItemPai() == null) || (item.getIdItemPai().equals(node.getData()))) {
                    if ((!categoria) || (item.getCategoria().equals("S"))) {
                        (new DefaultTreeNode(item, node)).setExpanded(true);
                    }
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

    public void onNodeSelect(NodeSelectEvent event) {
        try {
            this.setItem((Item) (event.getTreeNode().getData()));
            getEntity().setTipo(null);
            if (!this.item.getDescricao().equals("RAIZ")) {
                this.setDisable(false);
                if(this.getItem().getTipo() != null) {
                    getEntity().setTipo(this.getItem().getTipo());                    
                }
            }  
            PrimeFaces.current().ajax().update(":lume:tipo");
            this.setDigitacao(this.getItem().getDescricao());
            this.filtraItem(this.getDigitacao());
        } catch (Exception e) {
            log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
        }
    }

    public void handleSelect() {
        this.setItem(null);
        this.filtraItem(this.getDigitacao());
        this.setSelected();
    }

    public List<String> filtraItem(String digitacao) {
        this.setDigitacao(digitacao);
   //     this.filtraCategorias();
        return this.convert(this.getItens());
    }

    public void filtraItemCategoria() {
        try {
            this.setItens(ItemSingleton.getInstance().getBo().listByEmpresaTipoCategoriaDescricao(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa(),this.filtroItemCategoria,this.filtroTable));          
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "",true);
            log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
        }

    }
    
//    public void filtraItens(boolean filtro) {
//     //   this.setItens(new ArrayList<Item>());
//        try {
//           // if (!filtro && this.getDigitacao() != null) {
//           //     this.setItens(ItemSingleton.getInstance().getBo().listByEmpresaAndDescricaoParcial(this.getDigitacao(), UtilsFrontEnd.getProfissionalLogado().getIdEmpresa()));
//           // } else if (filtro && this.getFiltroTable() != null) {
//            //    this.setItens(ItemSingleton.getInstance().getBo().listByEmpresaAndDescricaoParcialAndCategoria(this.getFiltroTable(), UtilsFrontEnd.getProfissionalLogado().getIdEmpresa()));
//          //  } else {
//       //         this.setItens(ItemSingleton.getInstance().getBo().listByEmpresa(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa()));
//        //    }
//            Collections.sort(itens);
//        } catch (Exception e) {
//            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "",true);
//            log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
//        }
//    }
//
//    public void filtraCategorias() {
//      //  this.setItens(new ArrayList<Item>());
//        try {
//            if (this.getDigitacao() != null) {
//       //         this.setItens(ItemSingleton.getInstance().getBo().listCategoriasByEmpresaAndDescricaoParcial(this.getDigitacao(), UtilsFrontEnd.getProfissionalLogado().getIdEmpresa()));
//            } else {
//       //         this.setItens(ItemSingleton.getInstance().getBo().listCategoriasByEmpresa(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa()));
//            }
//            Collections.sort(itens);
//        } catch (Exception e) {
//            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "",true);
//            log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
//        }
//    }

    public boolean isDisable() {
        return disable;
    }

    public void setDisable(boolean disable) {
        this.disable = disable;
    }

    public List<Item> getItens() {
        return itens;
    }

    public void setParametros() {
        try {
            this.setFormaArmazenamento(DominioSingleton.getInstance().getBo().findByEmpresaAndObjetoAndTipoAndValor(OBJETO, TIPO[FORMA_ARMAZENAMENTO], this.getEntity().getFormaArmazenamento()));
            this.setUtilizacao(DominioSingleton.getInstance().getBo().findByEmpresaAndObjetoAndTipoAndValor(OBJETO, TIPO[UTILIZACAO], this.getEntity().getUtilizacao()));
            this.setFracaoUnitaria(DominioSingleton.getInstance().getBo().findByEmpresaAndObjetoAndTipoAndValor(OBJETO, TIPO[FRACAO_UNITARIA], this.getEntity().getFracaoUnitaria()));
            this.setUnidadeMedida(DominioSingleton.getInstance().getBo().findByEmpresaAndObjetoAndTipoAndValor(OBJETO, TIPO[UNIDADE_MEDIDA], this.getEntity().getUnidadeMedida()));
            if (this.getEntity().getCategoria() != null) {
                this.setCategoria(this.getEntity().getCategoria());
            } else {
                this.setCategoria("S");
            }
            if (this.getCategoria().equals("S")) {
                this.setDisableItem(true);
            } else {
                this.setDisableItem(false);
            }
        } catch (Exception e) {
            log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
        }
    }

    public void onNodeUnselect(NodeUnselectEvent event) {
        this.setDisable(false);
    }

    public List<Dominio> getDominios(int tipo) {
        List<Dominio> dominios = null;
        try {
            dominios = DominioSingleton.getInstance().getBo().listByEmpresaAndObjetoAndTipo(OBJETO, TIPO[tipo]);
        } catch (Exception e) {
            log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
        }
        return dominios;
    }
    
    public String getUnidadeString(Item item) {
        if(item != null)
            return DominioSingleton.getInstance().getBo().getUnidadeMedidaString(item.getUnidadeMedida());
        return null;
    }

    public Dominio getFormaArmazenamento() {
        return formaArmazenamento;
    }

    public void setFormaArmazenamento(Dominio formaArmazenamento) {
        this.formaArmazenamento = formaArmazenamento;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public TreeNode getRoot() {
        return root;
    }

    public void setRoot(TreeNode root) {
        this.root = root;
    }

    public TreeNode getSelectedItem() {
        return selectedItem;
    }

    public void setSelectedItem(TreeNode selectedItem) {
        this.selectedItem = selectedItem;
    }

    public TreeNode getSelectedItemPai() {
        return selectedItemPai;
    }

    public void setSelectedItemPai(TreeNode selectedItemPai) {
        this.selectedItemPai = selectedItemPai;
    }
//
    public TreeNode getRootPai() {
        return rootPai;
    }
//
    public void setRootPai(TreeNode rootPai) {
        this.rootPai = rootPai;
    }

    public List<Dominio> getFormasArmazenamento() {
        return formasArmazenamento;
    }

    public Dominio getUtilizacao() {
        return utilizacao;
    }

    public void setUtilizacao(Dominio utilizacao) {
        this.utilizacao = utilizacao;
    }

    public void setFormasArmazenamento(List<Dominio> formasArmazenamento) {
        this.formasArmazenamento = formasArmazenamento;
    }

    public List<Dominio> getUtilizacoes() {
        return utilizacoes;
    }

    public void setUtilizacoes(List<Dominio> utilizacoes) {
        this.utilizacoes = utilizacoes;
    }

    public String getCategoria() {
        return categoria;
    }

    public String getCategoriaStr() {
        return categoria.equals("S") ? "Sim" : "Não";
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public boolean isDisableItem() {
        return disableItem;
    }

    public void setDisableItem(boolean disableItem) {
        this.disableItem = disableItem;
    }

    public Dominio getFracaoUnitaria() {
        return fracaoUnitaria;
    }

    public void setFracaoUnitaria(Dominio fracaoUnitaria) {
        this.fracaoUnitaria = fracaoUnitaria;
    }

    public Dominio getUnidadeMedida() {
        return unidadeMedida;
    }

    public void setUnidadeMedida(Dominio unidadeMedida) {
        this.unidadeMedida = unidadeMedida;
    }

    public List<Dominio> getFracoesUnitarias() {
        return fracoesUnitarias;
    }

    public void setFracoesUnitarias(List<Dominio> fracoesUnitarias) {
        this.fracoesUnitarias = fracoesUnitarias;
    }

    public List<Dominio> getUnidadesMedida() {
        return unidadesMedida;
    }

    public void setUnidadesMedida(List<Dominio> unidadesMedida) {
        this.unidadesMedida = unidadesMedida;
    }

    public String getDigitacao() {
        return digitacao;
    }

    public void setDigitacao(String digitacao) {
        this.digitacao = digitacao;
    }

    public List<String> convert(@SuppressWarnings("rawtypes") List objects) {
        List<String> strings = new ArrayList<>();
        if(objects != null) {
            for (Object object : objects) {
                if (object instanceof Item) {
                    strings.add(((Item) object).getDescricao());
                }
            }
        }
        return strings;
    }

    public static final String[] TIPO = new String[] { "forma_armazenamento", "utilizacao", "fracao_unitaria", "unidade_medida" };

    public static final String OBJETO = "item";

    public static final int FORMA_ARMAZENAMENTO = 0;

    public static final int UTILIZACAO = 1;

    public static final int FRACAO_UNITARIA = 2;

    public static final int UNIDADE_MEDIDA = 3;

    public void setItens(List<Item> itens) {
        this.itens = itens;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public String getFiltroTable() {
        return filtroTable;
    }

    public void setFiltroTable(String filtroTable) {
        this.filtroTable = filtroTable;
//        if (this.getFiltroTable() != null && !this.getFiltroTable().isEmpty() && this.getFiltroTable().length() >= 3) {
//            this.setRoot(new DefaultTreeNode("", null));
//            Item firstLevel = new Item();
//            firstLevel.setDescricao("RAIZ");
//            this.chargeTree(new DefaultTreeNode(firstLevel, root), false, true);
//            List<Item> itens2 = new ArrayList<>();
//            for (Item item : this.getItens()) {
//                //System.out.println(item.getDescricaoLimpa().toUpperCase());
//                if (item.getDescricaoLimpa().toUpperCase().contains(Utils.normalize(filtroTable.toUpperCase()))) {
//                    itens2.add(item);
//                }
//            }
//            root = new DefaultTreeNode("root", null);
//            for (Item item : itens2) {
//                TreeNode node2 = new DefaultTreeNode(item, root);
//                node2.setExpanded(true);
//            }
//        }
    }

    
    public String getFiltroItemCategoria() {
        return filtroItemCategoria;
    }

    
    public void setFiltroItemCategoria(String filtroItemCategoria) {
        this.filtroItemCategoria = filtroItemCategoria;
    }

//    public void actionTodos(ActionEvent event) {
//        this.setRoot(new DefaultTreeNode("", null));
//        Item firstLevel = new Item();
//        firstLevel.setDescricao("RAIZ");
//        this.chargeTree(new DefaultTreeNode(firstLevel, root), false, true);
//        this.eliminaCategoriasVazias(root);
//        // filtroTable = null;
//    }
}
