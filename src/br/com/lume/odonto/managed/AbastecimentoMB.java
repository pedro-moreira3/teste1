package br.com.lume.odonto.managed;

import java.math.BigDecimal;
import java.text.Normalizer;
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
import org.primefaces.event.SelectEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

import br.com.lume.abastecimento.AbastecimentoSingleton;
import br.com.lume.agendamento.AgendamentoSingleton;
import br.com.lume.agendamentoPlanoTratamentoProcedimento.AgendamentoPlanoTratamentoProcedimentoSingleton;
import br.com.lume.common.OdontoPerfil;
import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.dominio.DominioSingleton;
import br.com.lume.item.ItemSingleton;
import br.com.lume.material.MaterialSingleton;
import br.com.lume.materialLog.MaterialLogSingleton;
//import br.com.lume.odonto.bo.AbastecimentoBO;
//import br.com.lume.odonto.bo.AgendamentoBO;
//import br.com.lume.odonto.bo.AgendamentoPlanoTratamentoProcedimentoBO;
//import br.com.lume.odonto.bo.ItemBO;
//import br.com.lume.odonto.bo.MaterialBO;
//import br.com.lume.odonto.bo.MaterialLogBO;
//import br.com.lume.odonto.bo.PlanoTratamentoProcedimentoBO;
//import br.com.lume.odonto.bo.PlanoTratamentoProcedimentoCustoBO;
//import br.com.lume.odonto.bo.ProfissionalBO;
import br.com.lume.odonto.entity.Abastecimento;
import br.com.lume.odonto.entity.Agendamento;
import br.com.lume.odonto.entity.AgendamentoPlanoTratamentoProcedimento;
import br.com.lume.odonto.entity.ControleMaterial;
import br.com.lume.odonto.entity.Item;
import br.com.lume.odonto.entity.Material;
import br.com.lume.odonto.entity.MaterialLog;
import br.com.lume.odonto.entity.PedidoItem;
import br.com.lume.odonto.entity.PlanoTratamentoProcedimento;
import br.com.lume.odonto.entity.PlanoTratamentoProcedimentoCusto;
import br.com.lume.odonto.entity.Profissional;
import br.com.lume.odonto.util.OdontoMensagens;
import br.com.lume.planoTratamentoProcedimento.PlanoTratamentoProcedimentoSingleton;
import br.com.lume.planoTratamentoProcedimentoCusto.PlanoTratamentoProcedimentoCustoSingleton;
import br.com.lume.profissional.ProfissionalSingleton;

@ManagedBean
@ViewScoped
public class AbastecimentoMB extends LumeManagedBean<Abastecimento> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(AbastecimentoMB.class);

    private String digitacao;

    private Abastecimento abastecimento;

    private Agendamento agendamento;

    private List<PedidoItem> pedidoItens = new ArrayList<>();

    private List<Material> materiaisSelecionado;

    private Item item;

    private Date data;

    private List<Item> itens;

    private TreeNode root, selectedItem;

    private PedidoItem pedidoItem;

    private BigDecimal quantidade;

    private List<Material> materiaisDisponiveis;

    private List<Abastecimento> abastecimentos, abastecimentosDevolucao;

    private Profissional profissional;

    private BigDecimal quantidadeDevolvida;

    private boolean enableDevolucao;

    private boolean enableLavagem;

    private List<Agendamento> agendamentos = new ArrayList<>();

    private List<AgendamentoPlanoTratamentoProcedimento> agendamentoProcedimentos;

    private List<AgendamentoPlanoTratamentoProcedimento> planoTratamentoProcedimentoAgendamentos;

    public static String ENTREGUE = "EN";

    public static String PENDENTE = "PE";

    public static String FINALIZADO = "FI";

  //  private MaterialBO materialBO;

   // private AgendamentoPlanoTratamentoProcedimentoBO agendamentoPlanoTratamentoProcedimentoBO;

 //   private AgendamentoBO agendamentoBO;

//    private ItemBO itemBO;

  //  private ProfissionalBO profissionalBO;

 //   private PlanoTratamentoProcedimentoCustoBO planoTratamentoProcedimentoCustoBO;

 //   private PlanoTratamentoProcedimentoBO planoTratamentoProcedimentoBO;

    private boolean enableSalvar;

//    private MaterialLogBO materialLogBO = new MaterialLogBO();

    public AbastecimentoMB() {
        super(AbastecimentoSingleton.getInstance().getBo());
     //   agendamentoPlanoTratamentoProcedimentoBO = new AgendamentoPlanoTratamentoProcedimentoBO();
      //  agendamentoBO = new AgendamentoBO();
     //   materialBO = new MaterialBO();
    //    itemBO = new ItemBO();
    //   profissionalBO = new ProfissionalBO();
    //    planoTratamentoProcedimentoCustoBO = new PlanoTratamentoProcedimentoCustoBO();
    //    planoTratamentoProcedimentoBO = new PlanoTratamentoProcedimentoBO();
        this.setRoot(new DefaultTreeNode("", null));
        Item firstLevel = new Item();
        firstLevel.setDescricao("RAIZ");
        this.chargeTree(new DefaultTreeNode(firstLevel, this.getRoot()));
        this.setEnableSalvar(false);
        this.setClazz(Abastecimento.class);
        this.geraLista();
    }

    public void actionDevolucaoMaterial(ActionEvent event) {
        if (this.getEntity().getQuantidade().longValue() < this.getQuantidadeDevolvida().longValue()) {
            this.addInfo(OdontoMensagens.getMensagem("devolucao.acima.emprestado"), "");
        } else {
            try {

                BigDecimal quantidadeUtilizada;
                if (this.getEntity().getMaterial().getItem().getTipo().equals("I")) {
                    quantidadeDevolvida = this.getEntity().getQuantidade();
                    quantidadeUtilizada = BigDecimal.ZERO;
                } else {
                    quantidadeUtilizada = this.getEntity().getQuantidade().subtract(this.getQuantidadeDevolvida());
                }

                if (!quantidadeUtilizada.equals(this.getEntity().getQuantidade())) {// Foi solicitado material e devolvido uma parte ou o total
                    // Devolvendo sobras
                    // Material material = materialBO.find(cm.getMaterial());
                    MaterialSingleton.getInstance().getBo().refresh(getEntity().getMaterial());
                    this.getEntity().getMaterial().setQuantidadeAtual(this.getEntity().getMaterial().getQuantidadeAtual().add(quantidadeDevolvida));
                    MaterialSingleton.getInstance().getBo().persist(this.getEntity().getMaterial());
                    // Atualizado material utilizado
                    this.getEntity().setQuantidade(quantidadeUtilizada);
                }
                this.getEntity().setStatus(ControleMaterial.UTILIZADO_UNITARIO);
                this.getbO().persist(this.getEntity());
                if (quantidadeDevolvida.doubleValue() != 0d && this.getEntity().getMaterial().getItem().getAplicacao().equals(Item.APLICACAO_DIRETA)) {
                    this.devolveCusto(quantidadeUtilizada);
                }
                MaterialLogSingleton.getInstance().getBo().persist(new MaterialLog(null, getEntity(), getEntity().getMaterial(), UtilsFrontEnd.getProfissionalLogado(), quantidadeDevolvida,
                        this.getEntity().getMaterial().getQuantidadeAtual(), MaterialLog.DEVOLUCAO_UNITARIA_DEVOLUCAO));
                super.actionNew(event);
                this.geraLista();
                this.setEnableDevolucao(false);
                this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");
            } catch (Exception e) {
                this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
                log.error(Mensagens.ERRO_AO_SALVAR_REGISTRO, e);
            }
        }
    }

    private void devolveCusto(BigDecimal quantidadeUtilizada) {
        try {
            PlanoTratamentoProcedimentoCusto ptpCusto =  PlanoTratamentoProcedimentoCustoSingleton.getInstance().getBo().findByAbastecimento(this.getEntity());
            BigDecimal valorCusto = this.getEntity().getMaterial().getValor().multiply(quantidadeUtilizada);
            ptpCusto.setValor(valorCusto);
            PlanoTratamentoProcedimentoCustoSingleton.getInstance().getBo().merge(ptpCusto);
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
        }
    }

    public void actionLavagemMaterial(ActionEvent event) {
        if (this.getEntity().getQuantidade().longValue() < this.getQuantidadeDevolvida().longValue()) {
            this.addWarn(OdontoMensagens.getMensagem("devolucao.acima.emprestado"), "");
        } else {
            try {
                // O que não vai pra lavagem é devolvido
                BigDecimal quantidadeDevolver = this.getEntity().getQuantidade().subtract(this.getQuantidadeDevolvida());
                // Algo a devolver?
                if (quantidadeDevolver.compareTo(BigDecimal.ZERO) != 0) {
                    MaterialSingleton.getInstance().getBo().refresh(getEntity().getMaterial());
                    this.getEntity().getMaterial().setQuantidadeAtual(this.getEntity().getMaterial().getQuantidadeAtual().add(quantidadeDevolver));
                    MaterialSingleton.getInstance().getBo().persist(this.getEntity().getMaterial());// Atualizando estoque
                    this.getEntity().setQuantidade(quantidadeDevolvida);
                    MaterialLogSingleton.getInstance().getBo().persist(new MaterialLog(null, getEntity(), getEntity().getMaterial(), UtilsFrontEnd.getProfissionalLogado(), quantidadeDevolver,
                            getEntity().getMaterial().getQuantidadeAtual(), MaterialLog.DEVOLUCAO_UNITARIA_LAVAGEM));
                }
                this.getEntity().setStatus(ControleMaterial.UTILIZADO_UNITARIO);
                if (quantidadeDevolvida.compareTo(BigDecimal.ZERO) > 0) {
                    new LavagemMB().lavar(this.getEntity(), quantidadeDevolvida.longValue());
                }
                this.getbO().persist(this.getEntity());// Atualizando abastecimento
                super.actionNew(event);
                this.geraLista();
                this.setEnableDevolucao(false);
                this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");
            } catch (Exception e) {
                this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
                log.error(Mensagens.ERRO_AO_SALVAR_REGISTRO, e);
            }
        }
    }

    public void atualizaTela() {
        this.setEnableDevolucao(true);
        this.setEnableLavagem(false);
        try {
            if (this.getEntity() != null && this.getEntity().getMaterial().getItem().getTipo().equals("I")) {
                this.setEnableLavagem(true);
            }
        } catch (Exception e) {
            this.setEnableLavagem(false);
        }
        this.setQuantidadeDevolvida(this.getEntity().getQuantidade());
    }

    @Override
    public void actionPersist(ActionEvent event) {
        try {
            if (this.existeMaterialAplicacaoDireta() && (planoTratamentoProcedimentoAgendamentos == null || planoTratamentoProcedimentoAgendamentos.isEmpty())) {
                this.addError("Material do tipo direto, é obrigatório selecionar o agendamento e procedimento.", "");
            } else if (this.existeMaterialAplicacaoDireta() && planoTratamentoProcedimentoAgendamentos != null && planoTratamentoProcedimentoAgendamentos.size() > 1) {
                this.addError("Material do tipo direto, é obrigatório selecionar apenas um procedimento por agendamento.", "");
            } else {
                if (this.getEntity().getId() == 0) {
                    agendamento = this.getEntity().getAgendamento();
                    List<MaterialLog> logs = this.retiraQuantidade();
                    this.getEntity().setId(0);
                    this.getEntity().setIdEmpresa(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
                    this.getEntity().setDataEntrega(new Date());
                    this.getEntity().setProfissional(profissional);
                    this.getEntity().setMaterial(materiaisSelecionado.get(materiaisSelecionado.size() - 1));
                    this.getEntity().setStatus(ENTREGUE);
                    this.getEntity().setAgendamento(agendamento);
                    this.getEntity().setPlanoTratamentoProcedimentosAgendamentos(planoTratamentoProcedimentoAgendamentos);
                    this.getEntity().setQuantidade(this.getQuantidade());
                    this.getbO().persist(this.getEntity());
                    MaterialLogSingleton.getInstance().getBo().persistBatch(logs);
                    this.geraLista();
                    this.persistCusto();
                } else {
                    this.getEntity().setPlanoTratamentoProcedimentosAgendamentos(planoTratamentoProcedimentoAgendamentos);
                    this.getbO().persist(this.getEntity());
                }
                this.actionNewCopiaAntigo();
                this.setMateriaisSelecionado(null);
                this.setMateriaisDisponiveis(null);
                this.setEnableSalvar(false);
                this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");
            }
        } catch (Exception e) {
            log.error("Erro no actionPersist do " + this.getEntity().getClass().getName(), e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
        }
    }

    private boolean existeMaterialAplicacaoDireta() {
        for (Material m : materiaisSelecionado) {
            if (m.getItem().getAplicacao().equals(Item.APLICACAO_DIRETA)) {
                return true;
            }
        }
        return false;
    }

    private void actionNewCopiaAntigo() {
        this.setEntity(new Abastecimento());
        this.getEntity().setAgendamento(agendamento);
        digitacao = null;
        quantidade = null;
    }

    @Override
    public void actionNew(ActionEvent event) {
        this.setEntity(null);
        this.setEnableSalvar(false);
        profissional = null;
        agendamento = null;
        agendamentos = null;
        agendamentoProcedimentos = null;
    }

    public void actionVerificaSalvar() {
        if (materiaisSelecionado != null && !materiaisSelecionado.isEmpty()) {
            BigDecimal total = new BigDecimal(0);
            for (Material m : materiaisSelecionado) {
                MaterialSingleton.getInstance().getBo().refresh(m);
                total = total.add(m.getQuantidadeAtual());
            }
            if (quantidade != null && total.doubleValue() >= quantidade.doubleValue()) {
                this.setEnableSalvar(true);
            } else {
                this.setEnableSalvar(false);
            }
        } else {
            this.setEnableSalvar(false);
        }
    }

    private void persistCusto() {
        try {
            for (Material m : materiaisSelecionado) {
                if (m.getItem().getAplicacao().equals(Item.APLICACAO_DIRETA)) {
                    Date d = Calendar.getInstance().getTime();
                    BigDecimal valorCusto = m.getValor().multiply(this.getQuantidade());
                    AgendamentoPlanoTratamentoProcedimento ag = planoTratamentoProcedimentoAgendamentos.get(0);
                    PlanoTratamentoProcedimentoCusto ptpc = new PlanoTratamentoProcedimentoCusto(this.getEntity(), ag.getPlanoTratamentoProcedimento(), d, d, valorCusto, m.getItem().getDescricao());
                    PlanoTratamentoProcedimentoCustoSingleton.getInstance().getBo().persist(ptpc);
                    PlanoTratamentoProcedimento planoTratamentoProcedimento = ag.getPlanoTratamentoProcedimento();
                    if (planoTratamentoProcedimento.isFinalizado()) {
                        planoTratamentoProcedimento.setValorRepasse(PlanoTratamentoProcedimentoSingleton.getInstance().getBo().findValorRepasse(planoTratamentoProcedimento,UtilsFrontEnd.getEmpresaLogada().getEmpFltImposto()));
                        PlanoTratamentoProcedimentoSingleton.getInstance().getBo().merge(planoTratamentoProcedimento);
                    }
                    return;
                }
            }
        } catch (Exception e) {
            log.error("Erro no actionPersistCusto do " + this.getEntity().getClass().getName(), e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
        }
    }

    private List<MaterialLog> retiraQuantidade() throws Exception {
        BigDecimal quantidadeRetirar = quantidade;
        List<MaterialLog> logs = new ArrayList<>();
        for (Material m : materiaisSelecionado) {
            if (quantidadeRetirar.doubleValue() != 0d) {
                BigDecimal quantidadeRetirada = new BigDecimal(0d);

                if (m.getQuantidadeAtual().doubleValue() >= quantidadeRetirar.doubleValue()) {
                    MaterialSingleton.getInstance().getBo().refresh(m);
                    m.setQuantidadeAtual(m.getQuantidadeAtual().subtract(quantidadeRetirar));
                    quantidadeRetirada = quantidadeRetirar;
                    quantidadeRetirar = new BigDecimal(0);
                } else {
                    quantidadeRetirar = quantidadeRetirar.subtract(m.getQuantidadeAtual());
                    quantidadeRetirada = m.getQuantidadeAtual();

                    m.setQuantidadeAtual(new BigDecimal(0));
                }
                m.setDataUltimaUtilizacao(Calendar.getInstance().getTime());
                MaterialSingleton.getInstance().getBo().persist(m);
                logs.add(new MaterialLog(null, getEntity(), m, UtilsFrontEnd.getProfissionalLogado(), quantidadeRetirada.multiply(new BigDecimal(-1)), m.getQuantidadeAtual(),
                        MaterialLog.EMPRESTIMO_UNITARIO));
            }
        }
        return logs;
    }

    public void actionFindMateriais(ActionEvent event) {
        try {
            if (quantidade.intValue() != 0) {
                materiaisDisponiveis = MaterialSingleton.getInstance().getBo().listAtivosByEmpresaAndItemAndQuantidade(item, quantidade.intValue(), UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
                if (materiaisDisponiveis.isEmpty() || materiaisDisponiveis == null) {
                    this.addWarn(OdontoMensagens.getMensagem("abastecimento.materiais.vazio"), "");
                }
            } else {
                materiaisDisponiveis = new ArrayList<>();
                this.addWarn(OdontoMensagens.getMensagem("abastecimento.quantidade.vazio"), "");
            }
        } catch (Exception e) {
            log.error("Erro no listByEmpresa", e);
        }
    }

    public void carregaTelaGeral() {
        this.setEnableSalvar(true);
        this.setAgendamento(this.getEntity().getAgendamento());
        this.setProfissional(this.getEntity().getProfissional());
        if (this.getEntity().getAgendamento() != null) {
            this.setData(this.getEntity().getAgendamento().getInicio());
        }
        this.setQuantidade(this.getEntity().getQuantidade());
        this.setPlanoTratamentoProcedimentoAgendamentos(this.getEntity().getPlanoTratamentoProcedimentosAgendamentos());
        this.listaAgendamentos();
        this.setItem(this.getEntity().getMaterial().getItem());
        this.setDigitacao(this.getItem().getDescricao());
        this.filtraItem(this.getDigitacao());
        this.setSelected();
        this.carregaProcedimentos();
//        try {
//            materiaisDisponiveis = new ArrayList<>();
//            Material m = this.getEntity().getMaterial();
//            m.setQuantidade(this.getEntity().getQuantidade());
//            materiaisDisponiveis.add(m);
//        } catch (Exception e) {
//            log.error("Erro no findMaterial", e);
//        }
    }

    public List<String> filtraItem(String digitacao) {
        materiaisDisponiveis = new ArrayList<>();
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

    public boolean validaItem() {
        if (this.getItem() == null) {
            log.error(OdontoMensagens.getMensagem("erro.item.obrigatorio"));
            this.addError(OdontoMensagens.getMensagem("erro.item.obrigatorio"), "");
            return false;
        } else if (this.getQuantidade().intValue() == 0 || this.getQuantidade().intValue() < 1) {
            log.error(OdontoMensagens.getMensagem("erro.quantidade.obrigatorio"));
            this.addError(OdontoMensagens.getMensagem("erro.quantidade.obrigatorio"), "");
            return false;
        } else if (this.getItem().getCategoria().equalsIgnoreCase("S")) {
            log.error(OdontoMensagens.getMensagem("erro.categoria.proibido"));
            this.addError(OdontoMensagens.getMensagem("erro.categoria.proibido"), "");
            return false;
        } else if ((this.getItem().getEstoqueMaximo() - this.quantidadeTotal().longValue()) < 0) {
            {
                log.error(OdontoMensagens.getMensagem("erro.quantidade.acima"));
                this.addError(OdontoMensagens.getMensagem("erro.quantidade.acima"), "");
                return false;
            }
        }
        return true;
    }

    private BigDecimal quantidadeTotal() {
        BigDecimal quantidadeTotal = new BigDecimal(0);
        try {
            List<Material> materiais = MaterialSingleton.getInstance().getBo().listByItem(this.getItem());
            for (Material material : materiais) {
                quantidadeTotal = quantidadeTotal.add(material.getTamanhoUnidade().multiply(material.getQuantidade().multiply(material.getTamanhoUnidade())));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return quantidadeTotal;
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

    public void handleSelect() {
        this.filtraItem(this.getDigitacao());
        this.setSelected();
    }

    public void filtraItens() {
        this.setItens(new ArrayList<Item>());
        try {
            if (this.getDigitacao() != null) {
                this.setItens(ItemSingleton.getInstance().getBo().listByEmpresaAndDescricaoParcial(this.getDigitacao(),UtilsFrontEnd.getProfissionalLogado().getIdEmpresa()));
            } else {
                this.setItens(ItemSingleton.getInstance().getBo().listByEmpresa(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa()));
                // Collections.sort(itens);
            }
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
        }
    }

    public void onNodeSelect(NodeSelectEvent event) {
        this.setItem((Item) (event.getTreeNode().getData()));
        this.setDigitacao(this.getItem().getDescricao());
        this.filtraItem(this.getDigitacao());
    }
    
    public String getUnidadeString(Item item) {
        if(item != null)
            return DominioSingleton.getInstance().getBo().getUnidadeMedidaString(item.getUnidadeMedida());
        return null;
    }

    public void onNodeUnselect(NodeUnselectEvent event) {
        this.setItem(null);
    }

    public String getDigitacao() {
        return digitacao;
    }

    public void setDigitacao(String digitacao) {
        this.digitacao = digitacao;
    }

    public List<PedidoItem> getPedidoItens() {
        return pedidoItens;
    }

    public void setPedidoItens(List<PedidoItem> pedidoItens) {
        this.pedidoItens = pedidoItens;
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

    public PedidoItem getPedidoItem() {
        return pedidoItem;
    }

    public void setPedidoItem(PedidoItem pedidoItem) {
        this.pedidoItem = pedidoItem;
    }

    public BigDecimal getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(BigDecimal quantidade) {
        this.quantidade = quantidade;
    }

    public List<Material> getMateriaisDisponiveis() {
        return materiaisDisponiveis;
    }

    public void setMateriaisDisponiveis(List<Material> materiaisDisponiveis) {
        this.materiaisDisponiveis = materiaisDisponiveis;
    }

    public List<Abastecimento> getAbastecimentos() {
        return abastecimentos;
    }

    public void setAbastecimentos(List<Abastecimento> abastecimentos) {
        this.abastecimentos = abastecimentos;
    }

    public List<Profissional> geraSugestoes(String query) {
        List<Profissional> sugestoes = new ArrayList<>();
        List<Profissional> profissionais = new ArrayList<>();
        try {
            profissionais = ProfissionalSingleton.getInstance().getBo().listByEmpresaAndAtivo(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
            for (Profissional p : profissionais) {
                if (Normalizer.normalize(p.getDadosBasico().getNome().toLowerCase(), Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "").contains(
                        Normalizer.normalize(query.toLowerCase(), Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", ""))) {
                    sugestoes.add(p);
                }
            }
            Collections.sort(sugestoes);
        } catch (Exception e) {
            log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS);
        }
        return sugestoes;
    }

    public void handleSelectProfissional(SelectEvent event) {
        this.setProfissional((Profissional) event.getObject());
        this.listaAgendamentos();
    }

    public void handleSelectData(SelectEvent event) {
        Date date = (Date) event.getObject();
        this.setData(date);
        this.listaAgendamentos();
    }

    private void listaAgendamentos() {
        Calendar cal = Calendar.getInstance();
        if (data != null) {
            cal.setTime(data);
        }
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);        
        List<Agendamento> agendamentosNew = AgendamentoSingleton.getInstance().getBo().listByProfissionalAndStatusAndDataLimite(profissional, cal.getTime());
        for (Agendamento agendamento : agendamentosNew) {
            if(agendamento.getStatusNovo().matches("P|S|N|E|A|I|O")) {
                agendamentos.add(agendamento);                
            }
        }
    }

    public void carregaProcedimentos() {
        agendamentoProcedimentos = AgendamentoPlanoTratamentoProcedimentoSingleton.getInstance().getBo().listByAgendamento(this.getEntity().getAgendamento());
    }

    public Profissional getProfissional() {
        return profissional;
    }

    public void setProfissional(Profissional profissional) {
        this.profissional = profissional;
    }

    private void geraLista() {
        try {            
            if (UtilsFrontEnd.getProfissionalLogado().getPerfil().equals(OdontoPerfil.ADMINISTRADOR)) {
                abastecimentos = AbastecimentoSingleton.getInstance().getBo().listByEmpresa(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
            } else {
                abastecimentos = AbastecimentoSingleton.getInstance().getBo().listByStatus(ENTREGUE, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
            }
            abastecimentosDevolucao = AbastecimentoSingleton.getInstance().getBo().listByStatus(ENTREGUE, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public BigDecimal getQuantidadeDevolvida() {
        return quantidadeDevolvida;
    }

    public void setQuantidadeDevolvida(BigDecimal quantidadeDevolvida) {
        this.quantidadeDevolvida = quantidadeDevolvida;
    }

    public boolean isEnableDevolucao() {
        return enableDevolucao;
    }

    public void setEnableDevolucao(boolean enableDevolucao) {
        this.enableDevolucao = enableDevolucao;
    }

    public boolean isEnableLavagem() {
        return enableLavagem;
    }

    public void setEnableLavagem(boolean enableLavagem) {
        this.enableLavagem = enableLavagem;
    }

    public List<Agendamento> getAgendamentos() {
        return agendamentos;
    }

    public void setAgendamentos(List<Agendamento> agendamentos) {
        this.agendamentos = agendamentos;
    }

    public List<AgendamentoPlanoTratamentoProcedimento> getAgendamentoProcedimentos() {
        return agendamentoProcedimentos;
    }

    public void setAgendamentoProcedimentos(List<AgendamentoPlanoTratamentoProcedimento> agendamentoProcedimentos) {
        this.agendamentoProcedimentos = agendamentoProcedimentos;
    }

    public List<AgendamentoPlanoTratamentoProcedimento> getPlanoTratamentoProcedimentoAgendamentos() {
        return planoTratamentoProcedimentoAgendamentos;
    }

    public void setPlanoTratamentoProcedimentoAgendamentos(List<AgendamentoPlanoTratamentoProcedimento> planoTratamentoProcedimentoAgendamentos) {
        this.planoTratamentoProcedimentoAgendamentos = planoTratamentoProcedimentoAgendamentos;
    }

    public Date getData() {
        return data;
    }

    public void setData(Date data) {
        this.data = data;
    }

    public Agendamento getAgendamento() {
        return agendamento;
    }

    public void setAgendamento(Agendamento agendamento) {
        this.agendamento = agendamento;
    }

    public List<Abastecimento> getAbastecimentosDevolucao() {
        return abastecimentosDevolucao;
    }

    public void setAbastecimentosDevolucao(List<Abastecimento> abastecimentosDevolucao) {
        this.abastecimentosDevolucao = abastecimentosDevolucao;
    }

    public Abastecimento getAbastecimento() {
        return abastecimento;
    }

    public void setAbastecimento(Abastecimento abastecimento) {
        this.abastecimento = abastecimento;
    }

    public boolean isEnableSalvar() {
        return enableSalvar;
    }

    public void setEnableSalvar(boolean enableSalvar) {
        this.enableSalvar = enableSalvar;
    }

    public List<Material> getMateriaisSelecionado() {
        return materiaisSelecionado;
    }

    public void setMateriaisSelecionado(List<Material> materiaisSelecionado) {
        this.materiaisSelecionado = materiaisSelecionado;
    }
}
