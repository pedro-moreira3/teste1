package br.com.lume.odonto.managed;

import java.math.BigDecimal;
import java.util.ArrayList;
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

import br.com.lume.common.OdontoPerfil;
import br.com.lume.common.exception.business.BusinessException;
import br.com.lume.common.exception.techinical.TechnicalException;
import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.Status;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.dominio.DominioSingleton;
import br.com.lume.item.ItemSingleton;
import br.com.lume.material.MaterialSingleton;
import br.com.lume.odonto.entity.Dominio;
import br.com.lume.odonto.entity.Item;
import br.com.lume.odonto.entity.Material;
import br.com.lume.odonto.entity.Profissional;
import br.com.lume.odonto.entity.Sugestao;
import br.com.lume.odonto.entity.SugestaoItem;
import br.com.lume.odonto.util.OdontoMensagens;
import br.com.lume.sugestao.SugestaoSingleton;
import br.com.lume.sugestaoItem.SugestaoItemSingleton;

@ManagedBean
@ViewScoped
public class SugestaoMB extends LumeManagedBean<Sugestao> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(SugestaoMB.class);

    private TreeNode root, selectedItem;

    private Integer quantidade;

    private SugestaoItem sugestaoItem;

    private Item item;

    private List<Item> itens;

    private List<Sugestao> sugestoes;

    private String digitacao;

    private boolean incluindo;

    private List<SugestaoItem> SugestaoItens = new ArrayList<>();

    private Date dataAtual;

    private Dominio status;

    private List<Dominio> dominios;

    private String novoItem;

    private Profissional profissionalLogado;

    private boolean visivel = false, tipo = true; 

    public SugestaoMB() {
        super(SugestaoSingleton.getInstance().getBo());
     
        this.setClazz(Sugestao.class);
        this.setIncluindo(true);
        this.geraList();
        try {
            this.profissionalLogado = UtilsFrontEnd.getProfissionalLogado();
            if (this.profissionalLogado.getPerfil().equals(OdontoPerfil.AUXILIAR_ADMINISTRATIVO) || this.profissionalLogado.getPerfil().equals(
                    OdontoPerfil.ADMINISTRADOR) || this.profissionalLogado.getPerfil().equals(OdontoPerfil.ADMINISTRADORES)) {
                this.visivel = true;
            }
            this.dataAtual = new Date();
            this.setRoot(new DefaultTreeNode("", null));
            Item firstLevel = new Item();
            firstLevel.setDescricao("RAIZ");
            this.chargeTree(new DefaultTreeNode(firstLevel, this.getRoot()));
        } catch (Exception e) {
            this.log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
        }
    }

    public void geraList() {
        try {
            this.setSugestoes(SugestaoSingleton.getInstance().getBo().listByEmpresa(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa()));
            if (this.sugestoes != null) {
                Collections.sort(this.sugestoes);
            }
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            this.log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
        }
    }

    @Override
    public void actionPersist(ActionEvent event) {
        this.getEntity().setIdEmpresa(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
        this.getEntity().setData(new Date());
        this.getEntity().setProfissional(UtilsFrontEnd.getProfissionalLogado());
        for (SugestaoItem sugestaoItem : this.getSugestaoItens()) {
            if (sugestaoItem.getId() == 0) {
                this.getEntity().getSugestaoItens().add(sugestaoItem);
            }
        }
        if ((this.getSugestaoItens() == null) || (this.getSugestaoItens().size() < 1)) {
            this.log.error(OdontoMensagens.getMensagem("error.pedidoitens.vazio"));
            this.addError(OdontoMensagens.getMensagem("error.pedidoitens.vazio"), "");
        } else {
            super.actionPersist(event);
        }
        this.geraList();
    }

    @Override
    public void actionNew(ActionEvent event) {
        super.actionNew(event);
        this.setIncluindo(true);
    }

    public void adicionar() {
        if (this.validaItem()) {
            SugestaoItem sugestaoItem = new SugestaoItem();
            sugestaoItem.setSugestao(this.getEntity());
            sugestaoItem.setQuantidade(this.getQuantidade());
            if (this.getSugestaoItens() == null) {
                this.setSugestaoItens(new ArrayList<SugestaoItem>());
            }
            sugestaoItem.setId(new Long(0));
            sugestaoItem.setStatus(ABERTO);
            if (this.tipo) {
                sugestaoItem.setItem(this.getItem());
            } else {
                sugestaoItem.setNovoItem(this.novoItem);
            }
            sugestaoItem.setDataStatus(new Date());
            this.getSugestaoItens().add(sugestaoItem);
            if (this.getEntity().getSugestaoItens() == null) {
                this.getEntity().setSugestaoItens(new ArrayList<SugestaoItem>());
            }
        }
        this.limpar();
    }

    public void remover() throws Exception {
        try {
            SugestaoItemSingleton.getInstance().getBo().remove(this.getSugestaoItem());
        } catch (BusinessException e) {
            e.printStackTrace();
        } catch (TechnicalException e) {
            e.printStackTrace();
        }
        this.getSugestaoItens().remove(this.getSugestaoItem());
        this.setIncluindo(true);
    }

    public void atualizar() {
        if (this.validaItem()) {
            for (SugestaoItem sugestaoItem : this.getSugestaoItens()) {
                if (sugestaoItem.equals(this.getSugestaoItem())) {
                    sugestaoItem.setQuantidade(this.getQuantidade());
                    sugestaoItem.setStatus(this.status.getValor());
                    if (this.tipo) {
                        sugestaoItem.setItem(this.getItem());
                        this.novoItem = "";
                        if (this.getSugestaoItem() != null) {
                            this.getSugestaoItem().setNovoItem("");
                        }
                    } else {
                        sugestaoItem.setNovoItem(this.novoItem);
                        if (this.getSelectedItem() != null) {
                            this.getSelectedItem().setSelected(false);
                        }
                        this.setItem(null);
                        if (this.getSugestaoItem() != null) {
                            this.getSugestaoItem().setItem(null);
                        }
                    }
                }
            }
        }
    }

    public void limpar() {
        this.setStatus(this.getDominios().get(0));
        this.setNovoItem(null);
        this.setDigitacao(null);
        this.setQuantidade(0);
        if (this.getSelectedItem() != null) {
            this.getSelectedItem().setSelected(false);
        }
        this.setItem(null);
        this.setIncluindo(true);
        this.setSugestaoItem(null);
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
        if (this.novoItem == null || this.novoItem.equals("")) {
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
            } else if ((this.getItem().getEstoqueMaximo() - this.quantidadeTotal().floatValue()) < 0) {
                if (this.isGestor()) {
                    PrimeFaces.current().ajax().addCallbackParam("dlg", true);
                } else {
                    this.log.error(OdontoMensagens.getMensagem("erro.quantidade.acima"));
                    this.addError(OdontoMensagens.getMensagem("erro.quantidade.acima"), "");
                    return false;
                }
            }
        }
        return true;
    }

    public void decline() {
        this.getSugestaoItens().remove(this.getSugestaoItens().size() - 1);
        this.setIncluindo(true);
    }

    private BigDecimal quantidadeTotal() {
        BigDecimal quantidadeTotal = new BigDecimal(0);
        try {
            List<Material> materiais = MaterialSingleton.getInstance().getBo().listByItem(this.getItem());
            for (Material material : materiais) {
                quantidadeTotal = quantidadeTotal.add(material.getTamanhoUnidade().multiply(material.getQuantidadeAtual().multiply(material.getTamanhoUnidade())));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return quantidadeTotal;
    }

    public void carregaTelaGeral() {
        this.setStatus(this.getDominios().get(0));
        this.setDigitacao(null);
        this.setQuantidade(0);
        if (this.getSelectedItem() != null) {
            this.getSelectedItem().setSelected(false);
        }
        this.setItem(null);
        this.setIncluindo(true);
        this.setNovoItem(null);
        this.SugestaoItens = new ArrayList<>();
        this.setSugestaoItens(this.getEntity().getSugestaoItens());
    }

    public void carregaTela() {
        if (this.getSugestaoItem().getItem() != null) {
            this.setTipo(true);
        } else {
            this.setTipo(false);
        }
        this.setDigitacao(null);
        this.setQuantidade(0);
        if (this.getSelectedItem() != null) {
            this.getSelectedItem().setSelected(false);
        }
        this.setItem(null);
        this.setIncluindo(true);
        this.setNovoItem(null);
        if (this.sugestaoItem.getNovoItem() == null || this.sugestaoItem.getNovoItem().equals("")) {
            this.setSelected();
        } else {
            this.setNovoItem(this.sugestaoItem.getNovoItem());
        }
        try {
            this.setQuantidade(this.getSugestaoItem().getQuantidade());
            this.setIncluindo(false);
        } catch (Exception e) {
            this.log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
        }
        try {
            this.status = DominioSingleton.getInstance().getBo().findByEmpresaAndObjetoAndTipoAndValor("sugestao", "status", this.getSugestaoItem().getStatus());
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
        if (this.getSugestaoItem() != null) {
            itemSelecionado = this.getSugestaoItem().getItem();
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

    public Date getDataAtual() {
        return this.dataAtual;
    }

    public void setDataAtual(Date dataAtual) {
        this.dataAtual = dataAtual;
    }

    public List<Sugestao> getSugestoes() {
        return this.sugestoes;
    }

    public void setSugestoes(List<Sugestao> sugestoes) {
        this.sugestoes = sugestoes;
    }

    public List<SugestaoItem> getSugestaoItens() {
        return this.SugestaoItens;
    }

    public void setSugestaoItens(List<SugestaoItem> sugestaoItens) {
        this.SugestaoItens = new ArrayList<>();
        for (SugestaoItem si : sugestaoItens) {
            if (si.getExcluido().equals(Status.NAO)) {
                this.SugestaoItens.add(si);
            }
        }
    }

    public SugestaoItem getSugestaoItem() {
        return this.sugestaoItem;
    }

    public void setSugestaoItem(SugestaoItem sugestaoItem) {
        this.sugestaoItem = sugestaoItem;
    }

    public Dominio getStatus() {
        return this.status;
    }

    public void setStatus(Dominio status) {
        this.status = status;
    }

    public List<Dominio> getDominios() {
        try {
            this.dominios = DominioSingleton.getInstance().getBo().listByEmpresaAndObjetoAndTipo("sugestao", "status");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this.dominios;
    }

    public void setDominios(List<Dominio> dominios) {
        this.dominios = dominios;
    }

    public String getNovoItem() {
        return this.novoItem;
    }

    public void setNovoItem(String novoItem) {
        this.novoItem = novoItem;
    }

    public Profissional getProfissionalLogado() {
        return this.profissionalLogado;
    }

    public void setProfissionalLogado(Profissional profissionalLogado) {
        this.profissionalLogado = profissionalLogado;
    }

    public boolean isVisivel() {
        return this.visivel;
    }

    public void setVisivel(boolean visivel) {
        this.visivel = visivel;
    }

    public static final String ABERTO = "AB";

    public boolean isTipo() {
        return this.tipo;
    }

    public void setTipo(boolean tipo) {
        this.tipo = tipo;
    }
}
