package br.com.lume.odonto.managed;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
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

import br.com.lume.common.exception.business.BusinessException;
import br.com.lume.common.exception.techinical.TechnicalException;
import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.Status;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.dominio.DominioSingleton;
import br.com.lume.estoque.EstoqueSingleton;
import br.com.lume.item.ItemSingleton;
import br.com.lume.material.MaterialSingleton;
import br.com.lume.odonto.entity.Dominio;
import br.com.lume.odonto.entity.Item;
import br.com.lume.odonto.entity.Material;
import br.com.lume.odonto.entity.Pedido;
import br.com.lume.odonto.entity.PedidoItem;
import br.com.lume.odonto.util.OdontoMensagens;
import br.com.lume.pedido.PedidoSingleton;
import br.com.lume.pedidoItem.PedidoItemSingleton;

@ManagedBean
@ViewScoped
public class PedidoMB extends LumeManagedBean<Pedido> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(PedidoMB.class);

    private TreeNode root, selectedItem;

    private Long quantidade;

    private PedidoItem pedidoItem;

    private Item item;

    private List<Item> itens;

    private List<Pedido> pedidos;

    private String digitacao;

    private boolean incluindo;

    private List<PedidoItem> pedidoItens = new ArrayList<>();

    private Date dataAtual;

    private List<Dominio> dominios = new ArrayList<>();

    public PedidoMB() {
        super(PedidoSingleton.getInstance().getBo());

        this.setClazz(Pedido.class);
        this.setIncluindo(true);
        try {
            this.dominios = DominioSingleton.getInstance().getBo().listByEmpresaAndObjetoAndTipo("pedido", "status");
            this.dataAtual = new Date();
            this.setRoot(new DefaultTreeNode("", null));
            Item firstLevel = new Item();
            firstLevel.setDescricao("RAIZ");
            this.chargeTree(new DefaultTreeNode(firstLevel, this.getRoot()));
        } catch (Exception e) {
            this.log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
        }
        this.geraLista();
    }

    private void geraLista() {
        try {
            this.setPedidos(PedidoSingleton.getInstance().getBo().listByEmpresa(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa()));
            if (this.pedidos != null) {
                Collections.sort(this.pedidos);
            }
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            this.log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
        }
    }

    public void emTransito(ActionEvent event) {
        this.getEntity().setStatus(Pedido.EM_TRANSITO);
        super.actionPersist(event);
    }

    public void cancelado(ActionEvent event) {
        this.getEntity().setStatus(Pedido.CANCELADO);
        super.actionPersist(event);
    }

    public void finalizado(ActionEvent event) {
        this.getEntity().setStatus(Pedido.FINALIZADO);
        super.actionPersist(event);
    }

    @Override
    public void actionPersist(ActionEvent event) {
        this.getEntity().setIdEmpresa(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
        this.getEntity().setData(new Date());
        this.getEntity().setProfissional(UtilsFrontEnd.getProfissionalLogado());
        for (PedidoItem pedidoItem : this.getPedidoItens()) {
            if (pedidoItem.getId() == 0 || !this.getEntity().getPedidoItens().contains(pedidoItem)) {
                this.getEntity().getPedidoItens().add(pedidoItem);
            }
        }
        if ((this.getPedidoItens() == null) || (this.getPedidoItens().size() < 1)) {
            this.log.error(OdontoMensagens.getMensagem("error.pedidoitens.vazio"));
            this.addError(OdontoMensagens.getMensagem("error.pedidoitens.vazio"), "");
        } else {
            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date());
            cal.add(Calendar.DAY_OF_MONTH, -1);
            if (this.getEntity().getId() != 0 || this.getEntity().getPrazo().getTime() >= cal.getTime().getTime()) {
                super.actionPersist(event);
                this.geraLista();
            } else {
                this.addError(OdontoMensagens.getMensagem("pedido.prazo.erro"), "");
            }
        }
    }

    @Override
    public void actionNew(ActionEvent event) {
        super.actionNew(event);
        this.setIncluindo(true);
    }

    public void adicionar() {
        if (this.validaItem()) {
            PedidoItem pedidoItem = new PedidoItem();
            pedidoItem.setItem(this.getItem());
            pedidoItem.setPedido(this.getEntity());
            pedidoItem.setQuantidade(this.getQuantidade());
            if (this.getPedidoItens() == null) {
                this.setPedidoItens(new ArrayList<PedidoItem>());
            }
            pedidoItem.setId(new Long(0));
            this.getPedidoItens().add(pedidoItem);
            if (this.getEntity().getPedidoItens() == null) {
                this.getEntity().setPedidoItens(new ArrayList<PedidoItem>());
            }
        }
    }

    public void remover() throws Exception {
        try {
            PedidoItemSingleton.getInstance().getBo().remove(this.getPedidoItem());
        } catch (BusinessException e) {
            e.printStackTrace();
        } catch (TechnicalException e) {
            e.printStackTrace();
        }
        this.getPedidoItens().remove(this.getPedidoItem());
        this.setIncluindo(true);
    }

    public void atualizar() {
        if (this.validaItem()) {
            for (PedidoItem pedidoItem : this.getPedidoItens()) {
                if (pedidoItem.equals(this.getPedidoItem())) {
                    pedidoItem.setQuantidade(this.getQuantidade());
                }
            }
        }
    }

    public void limpar() {
        this.setQuantidade(new Long(0));
        if (this.getSelectedItem() != null) {
            this.getSelectedItem().setSelected(false);
        }
        this.setItem(null);
        this.setIncluindo(true);
        this.setPedidoItem(null);
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
        } else if (this.getItem().getCategoria().equalsIgnoreCase("S")) {
            this.log.error(OdontoMensagens.getMensagem("erro.categoria.proibido"));
            this.addError(OdontoMensagens.getMensagem("erro.categoria.proibido"), "");
            return false;
        } else if ((this.getItem().getEstoqueMaximo() - this.quantidadeTotal().floatValue() - this.getQuantidade()) < 0) {
            if (this.isGestor()) {
                PrimeFaces.current().ajax().addCallbackParam("dlg", true);
            } else {
                this.log.error(OdontoMensagens.getMensagem("erro.quantidade.acima"));
                this.addError(OdontoMensagens.getMensagem("erro.quantidade.acima"), "");
                return false;
            }
        }
        return true;
    }

    public void decline() {
        this.getPedidoItens().remove(this.getPedidoItens().size() - 1);
        this.setIncluindo(true);
    }

    private BigDecimal quantidadeTotal() {
        BigDecimal quantidadeTotal = new BigDecimal(1);
        try {
            List<Material> materiais = MaterialSingleton.getInstance().getBo().listByItem(this.getItem());
            for (Material material : materiais) {
                BigDecimal quantidade =  EstoqueSingleton.getInstance().getBo().findByMaterialLocal(material,material.getEstoque().get(0).getLocal()).getQuantidade();
                quantidadeTotal = quantidadeTotal.add(quantidade);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return quantidadeTotal;
    }

    public void carregaTelaGeral() {
        this.pedidoItens = new ArrayList<>();
        this.setPedidoItens(this.getEntity().getPedidoItens());
    }

    public void carregaTela() {
        this.setSelected();
        try {
            this.setQuantidade(this.getPedidoItem().getQuantidade());
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
        Item itemSelecionado;
        if (this.getPedidoItem() != null) {
            itemSelecionado = this.getPedidoItem().getItem();
        } else {
            itemSelecionado = new Item();
            itemSelecionado.setDescricao(this.getDigitacao());
        }
        while (hasChildren) {
            nodesAux = new ArrayList<>();
            for (TreeNode node : nodes) {
                Object item = node.getData();
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
                this.setItens(ItemSingleton.getInstance().getBo().listByEmpresaAndDescricaoParcial(this.getDigitacao(), UtilsFrontEnd.getProfissionalLogado().getIdEmpresa()));
            } else {
                this.setItens(ItemSingleton.getInstance().getBo().listByEmpresa(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa()));
            }
            Collections.sort(this.itens);
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            this.log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
        }
    }

    public List<Pedido> getPedidos() {
        return this.pedidos;
    }

    public void setPedidos(List<Pedido> pedidos) {
        this.pedidos = pedidos;
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

    public PedidoItem getPedidoItem() {
        return this.pedidoItem;
    }

    public void setPedidoItem(PedidoItem pedidoItem) {
        this.pedidoItem = pedidoItem;
    }

    public boolean isIncluindo() {
        return this.incluindo;
    }

    public Long getQuantidade() {
        return this.quantidade;
    }

    public void setQuantidade(Long quantidade) {
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
        return this.getItem() != null ? this.getItem().getDescricao() : this.digitacao;
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

    public List<PedidoItem> getPedidoItens() {
        return this.pedidoItens;
    }

    public void setPedidoItens(List<PedidoItem> pedidoItens) {
        this.pedidoItens = new ArrayList<>();
        for (PedidoItem pedidoItem : pedidoItens) {
            if (pedidoItem.getExcluido().equals(Status.NAO)) {
                this.pedidoItens.add(pedidoItem);
            }
        }
    }

    public Date getDataAtual() {
        return this.dataAtual;
    }

    public void setDataAtual(Date dataAtual) {
        this.dataAtual = dataAtual;
    }

    public List<Dominio> getDominios() {
        return this.dominios;
    }

    public void setDominios(List<Dominio> dominios) {
        this.dominios = dominios;
    }
}
