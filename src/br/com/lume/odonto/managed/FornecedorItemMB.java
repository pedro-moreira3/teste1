package br.com.lume.odonto.managed;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ActionEvent;

import org.apache.log4j.Logger;
import org.primefaces.event.NodeSelectEvent;
import org.primefaces.event.NodeUnselectEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.odonto.bo.FornecedorBO;
import br.com.lume.odonto.bo.FornecedorItemBO;
import br.com.lume.odonto.bo.ItemBO;
import br.com.lume.odonto.bo.ProfissionalBO;
import br.com.lume.odonto.entity.Fornecedor;
import br.com.lume.odonto.entity.FornecedorItem;
import br.com.lume.odonto.entity.Item;
import br.com.lume.odonto.util.OdontoMensagens;

@ManagedBean
@ViewScoped
public class FornecedorItemMB extends LumeManagedBean<FornecedorItem> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(FornecedorItemMB.class);

    private List<Fornecedor> fornecedores;

    private TreeNode root, selectedItem;

    private FornecedorItem fornecedorItem;

    private List<Item> itens;

    private String digitacao;

    private List<FornecedorItem> fornecedoresItens;

    private FornecedorBO fornecedorBO;

    private FornecedorItemBO fornecedorItemBO;

    private ItemBO itemBO;

    public FornecedorItemMB() {
        super(new FornecedorItemBO());
        this.fornecedorBO = new FornecedorBO();
        this.fornecedorItemBO = new FornecedorItemBO();
        this.itemBO = new ItemBO();
        this.setClazz(FornecedorItem.class);
        this.geralist();
        try {
            this.setRoot(new DefaultTreeNode("", null));
            Item firstLevel = new Item();
            firstLevel.setDescricao("RAIZ");
            this.chargeTree(new DefaultTreeNode(firstLevel, this.getRoot()));
        } catch (Exception e) {
            this.log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
        }
        try {
            this.fornecedores = this.fornecedorBO.listByEmpresa();
        } catch (Exception e) {
            this.log.error("Erro no setEntity", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
        }
    }

    private void geralist() {
        try {
            this.setFornecedoresItens(this.fornecedorItemBO.listByEmpresa());
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public List<Fornecedor> geraSugestoes(String query) {
        List<Fornecedor> suggestions = new ArrayList<>();
        for (Fornecedor f : this.fornecedores) {
            if (Normalizer.normalize(f.getDadosBasico().getNome().toLowerCase(), Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "").contains(
                    Normalizer.normalize(query.toLowerCase(), Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", ""))) {
                suggestions.add(f);
            }
        }
        Collections.sort(suggestions, new Comparator<Fornecedor>() {

            @Override
            public int compare(Fornecedor o1, Fornecedor o2) {
                return o1.getDadosBasico().getNome().compareToIgnoreCase(o2.getDadosBasico().getNome());
            }
        });
        return suggestions;
    }

    @Override
    public void actionPersist(ActionEvent event) {
        this.getEntity().setIdEmpresa(ProfissionalBO.getProfissionalLogado().getIdEmpresa());
        if (this.getEntity().getItem() != null) {
            if (this.validaFornecedorItem()) {
                super.actionPersist(event);
                this.geralist();
            } else {
                this.addInfo(OdontoMensagens.getMensagem("fornecedorItem.vinculo.item"), "");
            }
        } else {
            this.addError(OdontoMensagens.getMensagem("fornecedorItem.item.vazio"), "");
        }
    }

    public boolean validaFornecedorItem() {
        if (((FornecedorItemBO) this.getbO()).findByFornecedorAndItem(this.getEntity()) == null) {
            for (FornecedorItem fI : ((FornecedorItemBO) this.getbO()).listByFornecedor(this.getEntity().getFornecedor())) {
                if (fI.getItem().getIdItemPai() != null) {
                    if (fI.getItem().getIdItemPai().equals(this.getEntity().getItem())) {
                        return false;
                    }
                } else {
                    if (fI.getItem().equals(this.getEntity().getItem().getIdItemPai())) {
                        return false;
                    }
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public void actionNew(ActionEvent event) {
        super.actionNew(event);
    }

    public List<String> filtraItem(String digitacao) {
        this.setDigitacao(digitacao);
        this.filtraItens();
        return this.convert(this.getItens());
    }

    public List<String> convert(List<Item> itens) {
        List<String> strings = new ArrayList<>();
        for (Item item : itens) {
            strings.add(item.getDescricao());
        }
        return strings;
    }

    public void handleSelect() {
        this.filtraItem(this.getDigitacao());
        this.setSelected();
    }

    public boolean validaItem() {
        if (this.getEntity().getItem() == null) {
            this.log.error(OdontoMensagens.getMensagem("erro.item.obrigatorio"));
            this.addError(OdontoMensagens.getMensagem("erro.item.obrigatorio"), "");
            return false;
        }
        return true;
    }

    public void carregaTelaGeral() {
        this.filtraItem(this.getEntity().getItem().getDescricao());
        this.setSelected();
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
                if (this.getFornecedorItem() != null) {
                    itemSelecionado = this.getFornecedorItem().getItem();
                } else {
                    itemSelecionado = new Item();
                    itemSelecionado.setDescricao(this.getDigitacao());
                }
                node.setSelected(false);
                if (((item instanceof String) && (item.equals(itemSelecionado.getDescricao()))) || // RAIZ?
                        ((item instanceof Item) && (((Item) item).getDescricao().equals(itemSelecionado.getDescricao())))) { // Encontrou o
                                                                                                                            // Node?
                    node.setSelected(true);
                    this.getEntity().setItem(((Item) item));
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

    public void chargeTree(TreeNode root) {
        List<TreeNode> nodes = new ArrayList<>();
        List<TreeNode> nodesAux;
        this.filtraItens();
        List<Item> locaisRestantes = this.getItens();
        root.setExpanded(true);
        nodes.add(root);
        locaisRestantes = this.setLevel(locaisRestantes, nodes);
        List<TreeNode> subNodes;
        while (locaisRestantes.size() > 0) {
            subNodes = new ArrayList<>();
            nodesAux = new ArrayList<>();
            nodesAux.addAll(nodes);
            for (TreeNode node : nodesAux) {
                subNodes.addAll(node.getChildren());
            }
            locaisRestantes = this.setLevel(locaisRestantes, subNodes);
            nodes = new ArrayList<>();
            nodes.addAll(subNodes);
        }
    }

    public List<Item> setLevel(List<Item> locaisRestantes, List<TreeNode> nodes) {
        boolean anotherLevel;
        List<Item> locaisRestantesAux = new ArrayList<>();
        for (Item item : locaisRestantes) {
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

    public void filtraItens() {
        this.setItens(new ArrayList<Item>());
        try {
            if (this.getDigitacao() != null) {
                this.setItens(this.itemBO.listByEmpresaAndDescricaoParcial(this.getDigitacao()));
            } else {
                this.setItens(this.itemBO.listByEmpresa());
            }
            Collections.sort(this.itens);
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            this.log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
        }
    }

    public void onNodeSelect(NodeSelectEvent event) {
        this.getEntity().setItem((Item) (event.getTreeNode().getData()));
        this.setDigitacao(this.getEntity().getItem().getDescricao());
        this.filtraItem(this.getDigitacao());
    }

    public void onNodeUnselect(NodeUnselectEvent event) {
        this.getEntity().setItem(null);
    }

    public TreeNode getSelectedItem() {
        return this.selectedItem;
    }

    public TreeNode getRoot() {
        return this.root;
    }

    public void setRoot(TreeNode root) {
        this.root = root;
    }

    public void setSelectedItem(TreeNode selectedItem) {
        this.selectedItem = selectedItem;
    }

    public String getDigitacao() {
        return this.getEntity().getItem() != null ? this.getEntity().getItem().getDescricao() : this.digitacao;
    }

    public void setDigitacao(String digitacao) {
        this.digitacao = digitacao;
    }

    public void setItens(List<Item> itens) {
        this.itens = itens;
    }

    public List<Item> getItens() {
        this.filtraItens();
        return this.itens;
    }

    public FornecedorItem getFornecedorItem() {
        return this.fornecedorItem;
    }

    public void setFornecedorItem(FornecedorItem fornecedorItem) {
        this.fornecedorItem = fornecedorItem;
    }

    public List<Fornecedor> getFornecedores() {
        if (this.fornecedores != null) {
            Collections.sort(this.fornecedores);
        }
        return this.fornecedores;
    }

    public void setFornecedores(List<Fornecedor> fornecedores) {
        this.fornecedores = fornecedores;
    }

    public List<FornecedorItem> getFornecedoresItens() {
        return this.fornecedoresItens;
    }

    public void setFornecedoresItens(List<FornecedorItem> fornecedoresItens) {
        this.fornecedoresItens = fornecedoresItens;
    }
}
