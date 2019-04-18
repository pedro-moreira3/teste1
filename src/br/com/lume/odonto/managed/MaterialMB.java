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
import org.primefaces.context.RequestContext;
import org.primefaces.event.NodeSelectEvent;
import org.primefaces.event.NodeUnselectEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.odonto.bo.DominioBO;
import br.com.lume.odonto.bo.FornecedorBO;
import br.com.lume.odonto.bo.ItemBO;
import br.com.lume.odonto.bo.LocalBO;
import br.com.lume.odonto.bo.MarcaBO;
import br.com.lume.odonto.bo.MaterialBO;
import br.com.lume.odonto.bo.MaterialLogBO;
import br.com.lume.odonto.bo.ProfissionalBO;
import br.com.lume.odonto.entity.Dominio;
import br.com.lume.odonto.entity.Fornecedor;
import br.com.lume.odonto.entity.Item;
import br.com.lume.odonto.entity.Local;
import br.com.lume.odonto.entity.Marca;
import br.com.lume.odonto.entity.Material;
import br.com.lume.odonto.entity.MaterialLog;
import br.com.lume.odonto.util.OdontoMensagens;

@ManagedBean
@ViewScoped
public class MaterialMB extends LumeManagedBean<Material> {

    private static final long serialVersionUID = 1L;

    private Dominio procedencia;

    private List<Dominio> procedencias;

    private Logger log = Logger.getLogger(MaterialMB.class);

    private Item item;

    private Local local;

    private List<Item> itens;

    private List<Local> locais;

    private String digitacao, digitacaoLocal, nomeMarca;

    private TreeNode root, rootLocal, selectedItem, selectedLocal;

    private List<Marca> marcas;

    private Date dateHoje;

    private List<Material> materiais = new ArrayList<>();

    private boolean caixa, novaMarca;

    private Dominio justificativa;

    private List<Dominio> dominios;

    private BigDecimal valorTotal = new BigDecimal(0), quantidadeMovimentada, quantidade, quantidadeMovimentacao;

    private MarcaBO marcaBO;

    private DominioBO dominioBO;

    private LocalBO localBO;

    private ItemBO itemBO;

    private List<Fornecedor> fornecedores;

    private FornecedorBO fornecedorBO = new FornecedorBO();

    private MaterialLogBO materialLogBO = new MaterialLogBO();

    private List<MaterialLog> materialLogs;

    private String tipoMovimentacao;

    public MaterialMB() {
        super(new MaterialBO());
        marcaBO = new MarcaBO();
        dominioBO = new DominioBO();
        localBO = new LocalBO();
        itemBO = new ItemBO();
        this.setCaixa(true);
        this.setClazz(Material.class);
        this.geraLista();
        try {
            this.setProcedencias(this.getPrecedencias());
            this.setMarcas(marcaBO.listByEmpresa());
            this.setRoot(new DefaultTreeNode("", null));
            Item firstLevel = new Item();
            firstLevel.setDescricao("RAIZ");
            this.chargeTree(new DefaultTreeNode(firstLevel, this.getRoot()));
            this.setRootLocal(new DefaultTreeNode("", null));
            Local firstLevelLocal = new Local();
            firstLevelLocal.setDescricao("RAIZ");
            this.chargeTreeLocal(new DefaultTreeNode(firstLevelLocal, this.getRootLocal()));
            dateHoje = new Date();
            fornecedores = fornecedorBO.listByEmpresa();
        } catch (Exception e) {
            log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
            this.addError(Mensagens.ERRO_AO_BUSCAR_REGISTROS, "");
        }
    }

    private void geraLista() {
        try {
            materiais = ((MaterialBO) this.getbO()).listAtivosByEmpresa();
            Collections.sort(materiais);
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
        }
    }

    public void atualizaQuantidadeTotal() {
        BigDecimal quantidadeTotal = this.getEntity().getQuantidade().multiply(this.getEntity().getTamanhoUnidade());
        this.getEntity().setQuantidadeTotal(quantidadeTotal);
        this.getEntity().setQuantidadeAtual(this.getEntity().getQuantidadeTotal());
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
            this.setDigitacaoLocal(this.getEntity().getLocal().getDescricao());
            this.setLocal(this.getEntity().getLocal());
            this.setProcedencia(dominioBO.findByEmpresaAndObjetoAndTipoAndValor(OBJETO, TIPO, this.getEntity().getProcedencia()));
            this.setItem(this.getEntity().getItem());
            this.setDigitacao(this.getEntity().getItem().getDescricao());
            this.setSelectedItem();
            this.setSelectedLocal();
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
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
            materialLogs = materialLogBO.listByMaterial(material);
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
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
                if ((local.getIdLocal() == null) || (local.getIdLocal().equals(node.getData()))) {
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

    public void actionDevolver(ActionEvent event) {
        try {
            if (this.getEntity().getQuantidadeAtual().intValue() >= quantidade.intValue()) {
                this.getbO().refresh(getEntity());
                this.getEntity().setQuantidadeAtual(this.getEntity().getQuantidadeAtual().subtract(quantidade));
                this.getbO().persist(this.getEntity());
                if (justificativa != null) {
                    this.getEntity().setJustificativa(justificativa.getNome());
                }
                materialLogBO.persist(new MaterialLog(null, null, getEntity(), ProfissionalBO.getProfissionalLogado(), quantidade.multiply(new BigDecimal(-1)), getEntity().getQuantidadeAtual(),
                        MaterialLog.ENTRADA_MATERIAL_DEVOLVER));
                this.actionNew(event);
                this.geraLista();
                RequestContext.getCurrentInstance().addCallbackParam("justificativa", true);
            } else {
                this.addError(OdontoMensagens.getMensagem("material.quantidade.maior"), "");
            }
        } catch (Exception e) {
            log.error("Erro no actionPersist", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
        }

    }

    @Override
    public void actionPersist(ActionEvent event) {
        try {

            if (!isEstoqueCompleto() && getEntity().getId() != 0) {
                actionMovimentacaoEstoqueSimplificado(event);
                return;
            } else if (!isEstoqueCompleto() && getEntity().getId() == 0) {
                List<Material> mats = ((MaterialBO) getbO()).listByItem(getItem());
                if (mats != null && !mats.isEmpty()) {
                    quantidadeMovimentacao = getEntity().getQuantidadeAtual();
                    tipoMovimentacao = Material.MOVIMENTACAO_ENTRADA;
                    setEntity(mats.get(0));
                    actionMovimentacaoEstoqueSimplificado(event);
                    return;
                }
            }

            if (this.getValorTotal() != null && this.getEntity().getQuantidadeTotal() != null) {
                this.getEntity().setValor(this.getValorTotal().divide(this.getEntity().getQuantidadeTotal(), 2, RoundingMode.HALF_UP));
            }
            this.atualizaQuantidadeTotal();
            this.getEntity().setIdEmpresa(ProfissionalBO.getProfissionalLogado().getIdEmpresa());
            if (this.getEntity().getStatus() == null) {
                this.getEntity().setStatus(ATIVO);
            }
            if (this.getEntity().getId() == 0) {
                this.getEntity().setDataCadastro(new Date());
            }
            if (this.getProcedencia() != null) {
                this.getEntity().setProcedencia(this.getProcedencia().getValor());
            }
            this.getEntity().setDataCadastro(new Date());
            if ((this.getItem() == null) || (this.getItem().getDescricao().equals("RAIZ")) || // RAIZ
                    (this.getItem().getCategoria().equals("S"))) {
                this.addError(OdontoMensagens.getMensagem("erro.item.material.raiz"), "");
            } else {
                if (isEstoqueCompleto() && (this.getLocal() == null || this.getLocal().getDescricao().equals("RAIZ"))) {
                    this.addError(OdontoMensagens.getMensagem("erro.local.material.raiz"), "");
                } else {
                    this.getEntity().setItem(this.getItem());
                    if (this.getLocal() == null) {
                        this.getEntity().setLocal(localBO.getLocalPadraoSistema());
                    } else {
                        this.getEntity().setLocal(this.getLocal());
                    }

                    Calendar cal = Calendar.getInstance();
                    cal.setTime(new Date());
                    cal.add(Calendar.DAY_OF_MONTH, -1);
                    if (this.getEntity().getStatus().equals(DEVOLVIDO) || this.getEntity().getValidade() == null || this.getEntity().getValidade().getTime() >= cal.getTime().getTime()) {
                        boolean novo = false;
                        if (getEntity().getId() == 0) {
                            novo = true;
                        }
                        if (this.getbO().persist(this.getEntity())) {
                            if (this.getEntity().getStatus().equals(DEVOLVIDO)) {
                                this.addInfo(OdontoMensagens.getMensagem("material.salvo.devolvido"), "");
                            } else {
                                this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");
                            }
                            if (novo) {
                                materialLogBO.persist(new MaterialLog(null, null, getEntity(), ProfissionalBO.getProfissionalLogado(), getEntity().getQuantidadeAtual(),
                                        getEntity().getQuantidadeAtual(), MaterialLog.ENTRADA_MATERIAL_CADASTRO));
                            }
                            //actionNew(event);
                            Fornecedor f = this.getEntity().getFornecedor();
                            Marca m = this.getEntity().getMarca();
                            this.setEntity(new Material());
                            this.getEntity().setFornecedor(f);
                            this.getEntity().setMarca(m);
                            valorTotal = null;
                            this.setSelectedItem(null);
                            digitacao = null;
                            this.geraLista();
                        }
                    } else {
                        this.addError(OdontoMensagens.getMensagem("material.validade.erro"), "");
                    }

                }
            }
        } catch (Exception e) {
            log.error("Erro no actionPersist", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
        }
    }

    public void actionMovimentacaoEstoqueSimplificado(ActionEvent event) {
        if (quantidadeMovimentacao.equals(new BigDecimal(0))) {
            this.addError(OdontoMensagens.getMensagem("erro.material.quantidade.zerada"), "");
        } else if (tipoMovimentacao.equals(Material.MOVIMENTACAO_SAIDA) && getEntity().getQuantidadeAtual().doubleValue() < quantidadeMovimentacao.doubleValue()) {
            this.addError("Não é possível retirar mais quantidade do que a quantidade atual.", "");
        } else {
            try {
                this.getbO().refresh(getEntity());
                if (tipoMovimentacao.equals(Material.MOVIMENTACAO_ENTRADA)) {
                    this.getEntity().setQuantidadeAtual(this.getEntity().getQuantidadeAtual().add(quantidadeMovimentacao));
                } else {
                    this.getEntity().setQuantidadeAtual(this.getEntity().getQuantidadeAtual().subtract(quantidadeMovimentacao));
                    quantidadeMovimentacao = quantidadeMovimentacao.multiply(new BigDecimal(-1));
                }
                this.getEntity().setQuantidadeTotal(this.getEntity().getQuantidadeAtual());
                this.getEntity().setDataMovimentacao(Calendar.getInstance().getTime());
                materialLogBO.persist(new MaterialLog(null, null, getEntity(), ProfissionalBO.getProfissionalLogado(), quantidadeMovimentacao, getEntity().getQuantidadeAtual(),
                        MaterialLog.ENTRADA_MATERIAL_DEVOLVER_MOVIMENTACAO_SIMPLIFICADA));
                this.getbO().persist(this.getEntity());

                this.addInfo(OdontoMensagens.getMensagem("material.salvo.movimentado"), "");
                this.actionNew(event);
                this.geraLista();
            } catch (Exception e) {
                log.error("Erro no actionPersist", e);
                this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
            }
        }
    }

    public void movimentar(ActionEvent event) {
        if (quantidadeMovimentada.equals(new BigDecimal(0))) {
            this.addError(OdontoMensagens.getMensagem("erro.material.quantidade.zerada"), "");
        } else if (this.getEntity().getQuantidadeAtual().doubleValue() < quantidadeMovimentada.doubleValue()) {
            this.addError(OdontoMensagens.getMensagem("erro.material.quantidade"), "");
        } else if ((this.getLocal() == null)) {
            this.addError(OdontoMensagens.getMensagem("erro.local.material.raiz"), "");
        } else if (this.getEntity().getLocal().getDescricao().equals(this.getDigitacaoLocal())) {
            this.addError(OdontoMensagens.getMensagem("erro.local.material.inalterado"), "");
        } else {

            try {
                this.getbO().refresh(getEntity());
                this.getEntity().setQuantidadeAtual(this.getEntity().getQuantidadeAtual().subtract(quantidadeMovimentada));
                materialLogBO.persist(new MaterialLog(null, null, getEntity(), ProfissionalBO.getProfissionalLogado(), quantidadeMovimentada.multiply(new BigDecimal(-1)),
                        getEntity().getQuantidadeAtual(), MaterialLog.MOVIMENTACAO_MATERIAL_MOVIMENTAR));
                this.getbO().persist(this.getEntity());
                Material material = new Material();
                PropertyUtils.copyProperties(material, this.getEntity());
                material.setQuantidadeAtual(quantidadeMovimentada);
                material.setQuantidadeTotal(quantidadeMovimentada);
                material.setId(0);
                material.setLocal(this.getLocal());
                this.setEntity(material);

                this.getEntity().setDataMovimentacao(new Date());
                this.getbO().persist(this.getEntity());

                materialLogBO.persist(new MaterialLog(null, null, getEntity(), ProfissionalBO.getProfissionalLogado(), quantidadeMovimentada, getEntity().getQuantidadeAtual(),
                        MaterialLog.MOVIMENTACAO_MATERIAL_MOVIMENTAR));

                this.addInfo(OdontoMensagens.getMensagem("material.salvo.movimentado"), "");
                this.actionNew(event);
                this.geraLista();
            } catch (Exception e) {
                log.error("Erro no actionPersist", e);
                this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
            }
        }
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

    public List<Material> getMateriais() {
        return materiais;
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
                this.setLocais(localBO.listByEmpresaAndDescricaoParcial(this.getDigitacaoLocal()));
            } else {
                this.setLocais(localBO.listByEmpresa());
            }
            Collections.sort(this.getLocais());
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
        }
    }

    public void filtraItens() {
        this.setItens(new ArrayList<Item>());
        try {
            if (this.getDigitacao() != null) {
                this.setItens(itemBO.listByEmpresaAndDescricaoParcial(this.getDigitacao()));
            } else {
                this.setItens(itemBO.listByEmpresa());
            }
            Collections.sort(itens);
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
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

    public void handleSelect() {
        this.filtraItem(this.getDigitacao());
        this.setItem(null);
        this.setSelected();
    }

    public void handleSelectLocal() {
        this.filtraLocal(this.getDigitacaoLocal());
        this.setLocal(null);
        this.setSelectedLocal();
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
                        ((local instanceof Local) && (((Local) local).getDescricao().equals(localSelecionado.getDescricao()))// && // Encontrou
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
            dominios = dominioBO.listByEmpresaAndObjetoAndTipo(OBJETO, TIPO);
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
            marcas = marcaBO.listByEmpresa();
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

    public void handleSelectMarca(SelectEvent event) {
        this.getEntity().setMarca((Marca) event.getObject());
    }

    public List<Dominio> getJustificativas() {
        List<Dominio> justificativas = new ArrayList<>();
        try {
            justificativas = dominioBO.listByEmpresaAndObjetoAndTipo("material", "justificativa");
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

    public void actionPersistMarca(ActionEvent event) {
        this.getEntity().setMarca(new Marca());
        this.getEntity().getMarca().setNome(this.getNomeMarca());
        Marca marca = marcaBO.findByNomeAndEmpresa(this.getEntity().getMarca().getNome(), ProfissionalBO.getProfissionalLogado().getIdEmpresa());
        if (marca != null) {
            if (marca.getId() != this.getEntity().getMarca().getId() && marca.getNome().equals(this.getEntity().getMarca().getNome())) {
                this.addError(OdontoMensagens.getMensagem("marca.erro.duplicado"), "");
                try {
                    this.getbO().refresh(this.getEntity());
                } catch (Exception e) {
                    log.error("refresh" + e);
                }
            }
        } else {
            this.getEntity().getMarca().setIdEmpresa(ProfissionalBO.getProfissionalLogado().getIdEmpresa());
            this.getEntity().getMarca().setDataCadastro(Calendar.getInstance().getTime());
            try {
                marcaBO.persist(this.getEntity().getMarca());
                this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");
            } catch (Exception e) {
                log.error(Mensagens.ERRO_AO_SALVAR_REGISTRO);
                this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
            }
            this.geraLista();
        }
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

    public BigDecimal getValorTotal() {
        if (this.getEntity().getValorUnidadeInformado() != null && !this.getEntity().getValorUnidadeInformado().equals(new BigDecimal(0))) {
            valorTotal = this.getEntity().getValorUnidadeInformado().multiply(this.getEntity().getQuantidade());
        }
        return valorTotal;
    }

    public void valorTotal() {
        this.atualizaQuantidadeTotal();
        if (this.getEntity().getValorUnidadeInformado() != null && !this.getEntity().getValorUnidadeInformado().equals(new BigDecimal(0))) {
            valorTotal = this.getEntity().getValorUnidadeInformado().multiply(this.getEntity().getQuantidade());
        }
    }

    public void setValorTotal(BigDecimal valorTotal) {
        this.valorTotal = valorTotal;
    }

    public BigDecimal getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(BigDecimal quantidade) {
        this.quantidade = quantidade;
    }

    public List<Fornecedor> getFornecedores() {
        return fornecedores;
    }

    public void setFornecedores(List<Fornecedor> fornecedores) {
        this.fornecedores = fornecedores;
    }

    public List<MaterialLog> getMaterialLogs() {
        return materialLogs;
    }

    public void setMaterialLogs(List<MaterialLog> materialLogs) {
        this.materialLogs = materialLogs;
    }

    public String getTipoMovimentacao() {
        return tipoMovimentacao;
    }

    public void setTipoMovimentacao(String tipoMovimentacao) {
        this.tipoMovimentacao = tipoMovimentacao;
    }

    public BigDecimal getQuantidadeMovimentacao() {
        return quantidadeMovimentacao;
    }

    public void setQuantidadeMovimentacao(BigDecimal quantidadeMovimentacao) {
        this.quantidadeMovimentacao = quantidadeMovimentacao;
    }

}
