package br.com.lume.odonto.managed;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ActionEvent;

import org.apache.log4j.Logger;
import org.primefaces.event.NodeSelectEvent;
import org.primefaces.event.NodeUnselectEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

import br.com.lume.common.OdontoPerfil;
import br.com.lume.common.exception.business.BusinessException;
import br.com.lume.common.exception.techinical.TechnicalException;
import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.Status;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.dominio.DominioSingleton;
import br.com.lume.item.ItemSingleton;
import br.com.lume.kit.KitSingleton;
import br.com.lume.kitItem.KitItemSingleton;
// import br.com.lume.odonto.bo.DominioBO;
// import br.com.lume.odonto.bo.ItemBO;
// import br.com.lume.odonto.bo.KitBO;
// import br.com.lume.odonto.bo.KitItemBO;
// import br.com.lume.odonto.bo.ProfissionalBO;
import br.com.lume.odonto.entity.Dominio;
import br.com.lume.odonto.entity.Item;
import br.com.lume.odonto.entity.Kit;
import br.com.lume.odonto.entity.KitItem;
import br.com.lume.odonto.entity.Profissional;
import br.com.lume.odonto.util.OdontoMensagens;

@ManagedBean
@ViewScoped
public class KitMB extends LumeManagedBean<Kit> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(KitMB.class);

    private TreeNode root, selectedItem;

    private Integer quantidade;

    private KitItem kitItem;

    private Item item;

    private List<Item> itens;

    private List<Kit> kits;

    private String digitacao;

    private boolean incluindo, admin;

    // private KitBO kitBO;

    //  private KitItemBO kitItemBO;

    //  private ItemBO itemBO;

    //  private DominioBO dominioBO;

    private List<KitItem> kitItens = new ArrayList<>();

    private Profissional profissionalLogado;

    //  private ProfissionalBO profissionalBO;

    private boolean visivel = true;

    public KitMB() {
        super(KitSingleton.getInstance().getBo());
        // this.kitBO = new KitBO();
        // this.kitItemBO = new KitItemBO();
        //  this.itemBO = new ItemBO();
        // this.dominioBO = new DominioBO();
        //  this.profissionalBO = new ProfissionalBO();
        this.setClazz(Kit.class);
        this.setIncluindo(true);
        this.carregaTree();
        this.geralista();
        try {
            if (UtilsFrontEnd.getProfissionalLogado() != null) {
                this.profissionalLogado = UtilsFrontEnd.getProfissionalLogado();
                if (this.profissionalLogado.getPerfil().equals(OdontoPerfil.DENTISTA) || this.profissionalLogado.getPerfil().equals(OdontoPerfil.AUXILIAR_DENTISTA)) {
                    this.visivel = false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void carregaTree() {
        try {
            this.setDigitacao(null);
            this.setRoot(new DefaultTreeNode("", null));
            Item firstLevel = new Item();
            firstLevel.setDescricao("RAIZ");
            this.chargeTree(new DefaultTreeNode(firstLevel, this.getRoot()));
        } catch (Exception e) {
            this.log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
        }
    }

    private void geralista() {
        try {
            this.setKits(KitSingleton.getInstance().getBo().listByEmpresa(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa()));
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            this.log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
        }
    }

    @Override
    public void actionPersist(ActionEvent event) {
        this.getEntity().setIdEmpresa(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
        this.getEntity().setData(new Date());
        for (KitItem kitItem : this.getKitItens()) {
            if (!((kitItem.getItem().getTipo().equals("I")) && (kitItem.getKit().getTipo().equals("Instrumental"))) && !((kitItem.getItem().getTipo().equals(
                    "C")) && (kitItem.getKit().getTipo().equals("Consumo")))) {
                this.log.error(OdontoMensagens.getMensagem("erro.item.kit.incompativel"));
                this.addError(OdontoMensagens.getMensagem("erro.item.kit.incompativel"), "");
                return;
            }
            if (kitItem.getId() == 0 || !this.getEntity().getKitItens().contains(kitItem)) {
                this.getEntity().getKitItens().add(kitItem);
            }
        }
        if ((this.getKitItens() == null) || (this.getKitItens().size() < 1)) {
            this.log.error(OdontoMensagens.getMensagem("error.kits.vazio"));
            this.addError(OdontoMensagens.getMensagem("error.kits.vazio"), "");
        } else {
            super.actionPersist(event);
            this.geralista();
        }
    }

    @Override
    public void actionNew(ActionEvent event) {
        super.actionNew(event);
        this.setIncluindo(true);
        this.visivel = true;
    }

    public void adicionar() {
        if (this.validaItem()) {
            KitItem kitItem = new KitItem();
            kitItem.setItem(this.getItem());
            kitItem.setKit(this.getEntity());
            kitItem.setQuantidade(this.getQuantidade());
            if (this.getKitItens() == null) {
                this.setKitItens(new ArrayList<KitItem>());
            }
            kitItem.setId(new Long(Calendar.getInstance().getTimeInMillis()));
            this.getKitItens().add(kitItem);
            if (this.getEntity().getKitItens() == null) {
                this.getEntity().setKitItens(new ArrayList<KitItem>());
            }
        }
    }

    //TODO - Solução para contornar a falha de itens nao sendo removidos dos kits.]
    // Construir forma mais simples com entityList direto sem passar por listas cópias.
    public void remover() throws Exception {
        if (this.getEntity().getKitItens() != null)
            this.getEntity().getKitItens().remove(this.getKitItem());
        try {
            KitItemSingleton.getInstance().getBo().remove(this.getKitItem());
        } catch (BusinessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (TechnicalException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        this.getKitItens().remove(this.getKitItem());
        this.setKitItem(null);
        this.setSelectedItem(null);
        this.setIncluindo(true);
    }

    public void atualizar() {
        if (this.validaItem()) {
            for (KitItem kitItem : this.getKitItens()) {
                if (kitItem.equals(this.getKitItem())) {
                    kitItem.setQuantidade(this.getQuantidade());
                }
            }
        }
    }

    public void limpar() {
        this.setQuantidade(0);
        if (this.getSelectedItem() != null) {
            this.getSelectedItem().setSelected(false);
        }
        this.setItem(null);
        this.setIncluindo(true);
        this.setKitItem(null);
        this.kitItens = null;
        this.setDigitacao(null);
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
        if (this.getItem() == null) {
            this.log.error(OdontoMensagens.getMensagem("erro.item.obrigatorio"));
            this.addError(OdontoMensagens.getMensagem("erro.item.obrigatorio"), "");
            return false;
        } else if (this.getQuantidade() == 0 || this.getQuantidade() < 1) {
            this.log.error(OdontoMensagens.getMensagem("erro.quantidade.obrigatorio"));
            this.addError(OdontoMensagens.getMensagem("erro.quantidade.obrigatorio"), "");
            return false;
        } else if (this.getItem().getTipo() == null) {
            this.log.error(OdontoMensagens.getMensagem("erro.item.desconfigurado"));
            this.addError(OdontoMensagens.getMensagem("erro.item.desconfigurado"), "");
            return false;
        } else if (this.getItem().getAplicacao().equals("D")) {
            this.log.error(OdontoMensagens.getMensagem("erro.item.kit.aplicacao"));
            this.addError(OdontoMensagens.getMensagem("erro.item.kit.aplicacao"), "");
            return false;
        }
        return true;
    }

    public void carregaTelaGeral() {
        this.setKitItens(this.getEntity().getKitItens());
        this.carregaTree();
        this.setDigitacao(null);
    }

    public void carregaTela() {
        this.setSelected();
        try {
            this.setQuantidade(this.getKitItem().getQuantidade());
            this.setIncluindo(false);
        } catch (Exception e) {
            this.log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
        }
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
                if (this.getKitItem() != null) {
                    itemSelecionado = this.getKitItem().getItem();
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
            // if(itensRestantes.size() == 2){
            // for (Item i : itensRestantes) {
            // System.out.println(i.getDescricao());
            // }
            // }
            // System.out.println(itensRestantes.size());
            nodes = new ArrayList<>();
            nodes.addAll(subNodes);
        }
    }

    public List<Item> setLevel(List<Item> itensRestantes, List<TreeNode> nodes) {
        boolean anotherLevel;
        List<Item> itensRestantesAux = new ArrayList<>();
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
                itensRestantesAux.add(item);
            }
        }
        return itensRestantesAux;
    }

    public void filtraItens() {
        this.setItens(new ArrayList<Item>());
        try {
            if (this.getEntity().getTipo() == null || this.getEntity().getTipo().equals("")) {
                this.setItens(new ArrayList<Item>());
            } else {
                if (this.getDigitacao() != null) {
                    this.setItens(ItemSingleton.getInstance().getBo().listByEmpresaAndDescricaoParcialAndTipo(this.getDigitacao(), this.getEntity().getTipo().charAt(0) + "",
                            UtilsFrontEnd.getProfissionalLogado().getIdEmpresa()));
                } else {
                    this.setItens(ItemSingleton.getInstance().getBo().listByEmpresaAndTipo(this.getEntity().getTipo().charAt(0) + "", UtilsFrontEnd.getProfissionalLogado().getIdEmpresa()));
                }
            }
            Collections.sort(this.itens);
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            this.log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
        }
    }

    public String getUnidadeString(Item item) {
        if (item != null)
            return DominioSingleton.getInstance().getBo().getUnidadeMedidaString(item.getUnidadeMedida());
        return null;
    }

    public List<Kit> getKits() {
        return this.kits;
    }

    public void setKits(List<Kit> kits) {
        this.kits = kits;
    }

    public void onNodeSelect(NodeSelectEvent event) {
        this.setItem((Item) (event.getTreeNode().getData()));
        this.setDigitacao(this.getItem().getDescricao());
        this.filtraItem(this.getDigitacao());
    }

    public void onNodeUnselect(NodeUnselectEvent event) {
        this.setItem(null);
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

    public KitItem getKitItem() {
        return this.kitItem;
    }

    public void setKitItem(KitItem kitItem) {
        this.kitItem = kitItem;
    }

    public boolean isIncluindo() {
        return this.incluindo;
    }

    public Integer getQuantidade() {
        return this.quantidade;
    }

    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
    }

    public void setSelectedItem(TreeNode selectedItem) {
        this.selectedItem = selectedItem;
    }

    public void setIncluindo(boolean incluindo) {
        this.incluindo = incluindo;
    }

    public Item getItem() {
        return this.item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public String getDigitacao() {
        return this.digitacao;
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

    public List<KitItem> getKitItens() {
        return this.kitItens;
    }

    public void setKitItens(List<KitItem> kitItens) {
        this.kitItens = new ArrayList<>();
        for (KitItem kitItem : kitItens) {
            if (kitItem.getExcluido().equals(Status.NAO)) {
                this.kitItens.add(kitItem);
            }
        }
    }

    public List<String> getTipos() {
        try {
            List<String> s = new ArrayList<>();
            List<Dominio> dominios = DominioSingleton.getInstance().getBo().listByEmpresaAndObjetoAndTipo("kit", "tipo");
            for (Dominio dominio : dominios) {
                s.add(dominio.getNome());
            }
            return s;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean isVisivel() {
        return this.visivel;
    }

    public void setVisivel(boolean visivel) {
        this.visivel = visivel;
    }
}
