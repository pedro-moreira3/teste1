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
import org.primefaces.PrimeFaces;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.event.NodeSelectEvent;
import org.primefaces.event.NodeUnselectEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

import br.com.lume.agendamento.AgendamentoSingleton;
import br.com.lume.agendamentoPlanoTratamentoProcedimento.AgendamentoPlanoTratamentoProcedimentoSingleton;
import br.com.lume.common.OdontoPerfil;
import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.dominio.DominioSingleton;
import br.com.lume.emprestimoUnitario.EmprestimoUnitarioSingleton;
import br.com.lume.estoque.EstoqueSingleton;
import br.com.lume.item.ItemSingleton;
import br.com.lume.local.LocalSingleton;
import br.com.lume.material.MaterialSingleton;
import br.com.lume.odonto.entity.Agendamento;
import br.com.lume.odonto.entity.AgendamentoPlanoTratamentoProcedimento;
import br.com.lume.odonto.entity.EmprestimoKit;
import br.com.lume.odonto.entity.EmprestimoUnitario;
import br.com.lume.odonto.entity.Estoque;
import br.com.lume.odonto.entity.Item;
import br.com.lume.odonto.entity.Local;
import br.com.lume.odonto.entity.Material;
import br.com.lume.odonto.entity.PedidoItem;
import br.com.lume.odonto.entity.Profissional;
import br.com.lume.odonto.util.OdontoMensagens;
import br.com.lume.profissional.ProfissionalSingleton;

@ManagedBean
@ViewScoped
public class EmprestimoUnitarioMB extends LumeManagedBean<EmprestimoUnitario> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(EmprestimoUnitarioMB.class);

    private String digitacao;

    private EmprestimoUnitario emprestimoUnitario;

    private Agendamento agendamento;

    private List<PedidoItem> pedidoItens = new ArrayList<>();

    private Estoque estoqueSelecionado;

    private Item item;

    private Date data;

    private List<Item> itens;

    private TreeNode root, selectedItem;

    private PedidoItem pedidoItem;

    private BigDecimal quantidade;

 //  private List<Material> materiaisDisponiveis;
    
    private List<Estoque> estoquesDisponiveis;

    private List<EmprestimoUnitario> emprestimoUnitarios, emprestimoUnitariosDevolucao;

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
    
    private boolean fecharDialog;

    private boolean enableSalvar;
    
    private boolean editar;
    
    //EXPORTAÇÃO TABELA
    private DataTable tabelaEmprestimo;
    private DataTable tabelaEmprestimoUnitario;

    public EmprestimoUnitarioMB() {
        super(EmprestimoUnitarioSingleton.getInstance().getBo());

        this.setRoot(new DefaultTreeNode("", null));
        Item firstLevel = new Item();
        firstLevel.setDescricao("RAIZ");
        this.chargeTree(new DefaultTreeNode(firstLevel, this.getRoot()));
        this.setEnableSalvar(false);
        this.setClazz(EmprestimoUnitario.class);
        this.geraLista();
    }

    public void actionDevolucaoMaterial(ActionEvent event) {
        if (this.getEntity().getQuantidade().longValue() < this.getQuantidadeDevolvida().longValue()) {
            this.addInfo(OdontoMensagens.getMensagem("devolucao.acima.emprestado"), "",true);
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
                
                    Local localOrigem = LocalSingleton.getInstance().getBo().getLocalPorDescricao(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa(), "DEVOLUCAO_UNITARIA");
                    EstoqueSingleton.getInstance().transferencia(this.getEntity().getMaterial(),localOrigem,this.getEntity().getMaterial().getEstoque().get(0).getLocal(),quantidadeDevolvida,EstoqueSingleton.DEVOLUCAO_UNITARIA,UtilsFrontEnd.getProfissionalLogado());

                    
                    MaterialSingleton.getInstance().getBo().persist(this.getEntity().getMaterial());
                    // Atualizado material utilizado
                    this.getEntity().setQuantidade(quantidadeUtilizada);
                }
                this.getEntity().setStatus(EmprestimoKit.UTILIZADO_UNITARIO);
                EmprestimoUnitarioSingleton.getInstance().getBo().persist(this.getEntity());

                super.actionNew(event);
                this.geraLista();
                this.setEnableDevolucao(false);
                this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "",true);
            } catch (Exception e) {
                this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "",true);
                log.error(Mensagens.ERRO_AO_SALVAR_REGISTRO, e);
            }
        }
    }
    
  public void actionLavagemMaterial() {
 
  if (this.getEntity().getQuantidade().longValue() < this.getQuantidadeDevolvida().longValue()) {
      this.addWarn(OdontoMensagens.getMensagem("devolucao.acima.emprestado"), "",true);
  } else {
      try {
          BigDecimal quantidadeParaLavar = this.getQuantidadeDevolvida();
          // O que nÃ£o vai pra lavagem Ã© devolvido
          BigDecimal quantidadeParaDevolver = this.getEntity().getQuantidade().subtract(quantidadeParaLavar);
          // Algo a devolver?
          if (quantidadeParaDevolver.compareTo(BigDecimal.ZERO) != 0) {             
              // Devolvendo sobras
              Local localOrigem = LocalSingleton.getInstance().getBo().getLocalPorDescricao(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa(), "DEVOLUCAO_UNITARIA");
              EstoqueSingleton.getInstance().transferencia(this.getEntity().getMaterial(),localOrigem,this.getEntity().getMaterial().getEstoque().get(0).getLocal()
                      ,quantidadeParaDevolver,EstoqueSingleton.DEVOLUCAO_UNITARIA,UtilsFrontEnd.getProfissionalLogado());
              MaterialSingleton.getInstance().getBo().persist(this.getEntity().getMaterial());
           
          }
          this.getEntity().setStatus(EmprestimoKit.UTILIZADO_UNITARIO);
         // if (quantidadeParaLavar.compareTo(BigDecimal.ZERO) > 0) {
         //     new LavagemMB().lavar(this.getEntity(), quantidadeParaLavar.longValue());
         // }
          
          Local localOrigem = LocalSingleton.getInstance().getBo().getLocalPorDescricao(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa(), "DEVOLUCAO_UNITARIA");       
          //antigo
        
          //novo processo
          Local localDestino = LocalSingleton.getInstance().getBo().getLocalPorDescricao(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa(), EstoqueSingleton.EM_LAVAGEM);          
          
          EstoqueSingleton.getInstance().transferenciaPersisteLocalSistema(this.getEntity().getMaterial(),localOrigem,localDestino,
                  quantidadeParaLavar,EstoqueSingleton.ENTREGA_LAVAGEM_UNITARIA,UtilsFrontEnd.getProfissionalLogado());          
          
          MaterialSingleton.getInstance().getBo().persist(this.getEntity().getMaterial());
          
          EmprestimoUnitarioSingleton.getInstance().getBo().persist(this.getEntity());// Atualizando emprestimoUnitario
          //super.actionNew(event);
          this.geraLista();
          this.setEnableDevolucao(false);
          this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "",true);
      } catch (Exception e) {
          this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "",true);
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

    public void actionPersistFechar(ActionEvent event) {
      this.fecharDialog = true;
      actionPersist(event);
    }
    
    @Override
    public void actionPersist(ActionEvent event) {
        try {         
            if (this.existeMaterialAplicacaoDireta() && (planoTratamentoProcedimentoAgendamentos == null || planoTratamentoProcedimentoAgendamentos.isEmpty())) {
                this.addError("Material do tipo direto, é obrigatório selecionar o agendamento e procedimento.", "",true);
            } else if (this.existeMaterialAplicacaoDireta() && planoTratamentoProcedimentoAgendamentos != null && planoTratamentoProcedimentoAgendamentos.size() > 1) {
                this.addError("Material do tipo direto, é obrigatório selecionar apenas um procedimento por agendamento.", "",true);
            } else  if(estoqueSelecionado == null) {
                this.addError("Selecione um item na tabela de Materias Disponíeis", "",true);   
            }else {
                if (this.getEntity().getId() == 0) {
                    agendamento = this.getEntity().getAgendamento();
                    this.retiraQuantidade();
                    this.getEntity().setId(0);
                    this.getEntity().setIdEmpresa(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
                    this.getEntity().setDataEntrega(new Date());
                    this.getEntity().setProfissional(profissional);
                    this.getEntity().setMaterial(estoqueSelecionado.getMaterial());
                    this.getEntity().setStatus(ENTREGUE);
                    if(agendamento != null) {
                        this.getEntity().setAgendamento(agendamento);    
                    }
                    if(planoTratamentoProcedimentoAgendamentos != null) {
                        this.getEntity().setPlanoTratamentoProcedimentosAgendamentos(planoTratamentoProcedimentoAgendamentos);    
                    }
                    
                   
                    this.getEntity().setQuantidade(this.getQuantidade());
                    EmprestimoUnitarioSingleton.getInstance().getBo().persist(this.getEntity());
                   // MaterialLogSingleton.getInstance().getBo().persistBatch(logs);
                    this.geraLista();
                   // this.persistCusto();
                } else {
                    this.getEntity().setPlanoTratamentoProcedimentosAgendamentos(planoTratamentoProcedimentoAgendamentos);
                    EmprestimoUnitarioSingleton.getInstance().getBo().persist(this.getEntity());
                }
                this.actionNewCopiaAntigo();
                this.setEstoqueSelecionado(null);
                this.setEstoquesDisponiveis(null);
                this.setEnableSalvar(false);
                setData(null);
                this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "",true);
                this.actionNew(event);
                if(this.fecharDialog) {
                    PrimeFaces.current().executeScript("PF('dlg').hide();");
                    this.fecharDialog = false;
                }
            }
        } catch (Exception e) {
            log.error("Erro no actionPersist do " + this.getEntity().getClass().getName(), e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "",true);
        }
    }
    
    public void carregarEditar(EmprestimoUnitario emprestimoUnitario) {
        this.editar = true;
        setEntity(emprestimoUnitario);
        setProfissional(emprestimoUnitario.getProfissional());        
        setDigitacao(emprestimoUnitario.getMaterial().getItem().getDescricao());
        setQuantidade(emprestimoUnitario.getQuantidade());
        setData(emprestimoUnitario.getDataEntrega());
        if(this.estoqueSelecionado == null && emprestimoUnitario.getMaterial() != null) {
            this.estoqueSelecionado = new Estoque();
            //this.estoquese materiaisSelecionado.add(emprestimoUnitario.getMaterial());    
        }
        listaAgendamentos();
        carregaProcedimentos();
        setPlanoTratamentoProcedimentoAgendamentos(emprestimoUnitario.getPlanoTratamentoProcedimentosAgendamentos());     
    } 

    private boolean existeMaterialAplicacaoDireta() {
        if(estoqueSelecionado == null) {
            return false;
        }
        if (estoqueSelecionado.getMaterial().getItem().getAplicacao().equals(Item.APLICACAO_DIRETA)) {
           return true;
       }        
        return false;
    }

    private void actionNewCopiaAntigo() {       
        this.setEntity(new EmprestimoUnitario());
        this.getEntity().setAgendamento(agendamento);
        digitacao = null;
        quantidade = null;
    }

    @Override
    public void actionNew(ActionEvent event) {
        this.editar = false;
        this.setEntity(null);
        this.setEnableSalvar(false);
        profissional = null;
        agendamento = null;
        agendamentos = null;
        agendamentoProcedimentos = null;       
        setProfissional(null);
        setPlanoTratamentoProcedimentoAgendamentos(null);
        setDigitacao(null);
        setQuantidade(null);
        
    }

    public void actionVerificaSalvar() {
        if (estoqueSelecionado != null) {
            if (quantidade != null && estoqueSelecionado.getQuantidade().doubleValue() >= quantidade.doubleValue()) {
                this.setEnableSalvar(true);
            } else {
                this.setEnableSalvar(false);
            }
        } else {
            this.setEnableSalvar(false);
        }
    }



    private void retiraQuantidade() throws Exception {
        if (quantidade.doubleValue() != 0d) {   
            Local localDestino = LocalSingleton.getInstance().getBo().getLocalPorDescricao(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa(), "EMPRESTIMO_UNITARIO");                    
            EstoqueSingleton.getInstance().transferencia(estoqueSelecionado.getMaterial(),estoqueSelecionado.getLocal(),localDestino,quantidade,EstoqueSingleton.EMPRESTIMO_UNITARIO,UtilsFrontEnd.getProfissionalLogado());  
            estoqueSelecionado.getMaterial().setDataUltimaUtilizacao(Calendar.getInstance().getTime());
            MaterialSingleton.getInstance().getBo().persist(estoqueSelecionado.getMaterial());           
        }
    }

    public void actionFindMateriais(ActionEvent event) {
        try {
            if (quantidade.intValue() != 0) {
              //  materiaisDisponiveis = MaterialSingleton.getInstance().getBo().listAtivosByEmpresaAndItemAndQuantidade(item, quantidade.intValue(), UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
                estoquesDisponiveis = EstoqueSingleton.getInstance().getBo().listAllDisponiveisByEmpresaItemAndQuantidade(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa(), item, quantidade,true);
                if (estoquesDisponiveis.isEmpty() || estoquesDisponiveis == null) {
                    this.addWarn(OdontoMensagens.getMensagem("emprestimoUnitario.materiais.vazio"), "",true);
                }
            } else {
                estoquesDisponiveis = new ArrayList<>();
                this.addWarn(OdontoMensagens.getMensagem("emprestimoUnitario.quantidade.vazio"), "",true);
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

    }

    public List<String> filtraItem(String digitacao) {
        estoquesDisponiveis = new ArrayList<>();
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
            this.addError(OdontoMensagens.getMensagem("erro.item.obrigatorio"), "",true);
            return false;
        } else if (this.getQuantidade().intValue() == 0 || this.getQuantidade().intValue() < 1) {
            log.error(OdontoMensagens.getMensagem("erro.quantidade.obrigatorio"));
            this.addError(OdontoMensagens.getMensagem("erro.quantidade.obrigatorio"), "",true);
            return false;
        } else if (this.getItem().getCategoria().equalsIgnoreCase("S")) {
            log.error(OdontoMensagens.getMensagem("erro.categoria.proibido"));
            this.addError(OdontoMensagens.getMensagem("erro.categoria.proibido"), "",true);
            return false;
        } else if ((this.getItem().getEstoqueMaximo() - this.quantidadeTotal().longValue()) < 0) {
            {
                log.error(OdontoMensagens.getMensagem("erro.quantidade.acima"));
                this.addError(OdontoMensagens.getMensagem("erro.quantidade.acima"), "",true);
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
                
                quantidadeTotal = quantidadeTotal.add(material.getTamanhoUnidade().multiply(EstoqueSingleton.getInstance().getBo().findByMaterialLocal(material,material.getEstoque().get(0).getLocal()).getQuantidade().multiply(material.getTamanhoUnidade())));
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
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "",true);
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

    public List<EmprestimoUnitario> getEmprestimoUnitarios() {
        return emprestimoUnitarios;
    }

    public void setEmprestimoUnitarios(List<EmprestimoUnitario> emprestimoUnitarios) {
        this.emprestimoUnitarios = emprestimoUnitarios;
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
        if(agendamentosNew != null) {
            for (Agendamento agendamento : agendamentosNew) {
                if(agendamento.getStatusNovo().matches("P|S|N|E|A|I|O")) {
                    if(agendamentos == null) {
                        agendamentos = new ArrayList<Agendamento>();
                    }
                    if(!agendamentos.contains(agendamento)) {
                        agendamentos.add(agendamento);    
                    }
                                    
                }
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
                emprestimoUnitarios = EmprestimoUnitarioSingleton.getInstance().getBo().listByEmpresa(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
            } else {
                emprestimoUnitarios = EmprestimoUnitarioSingleton.getInstance().getBo().listByStatus(ENTREGUE, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
            }
            emprestimoUnitariosDevolucao = EmprestimoUnitarioSingleton.getInstance().getBo().listByStatus(ENTREGUE, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void exportarTabela(String type) {
        exportarTabela("Empréstimo unitário", tabelaEmprestimo, type);
    }
    
    public void exportarTabelaEmprestimoUnitario(String type) {
        exportarTabela("Materiais de devolução", getTabelaEmprestimoUnitario(), type);
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

    public List<EmprestimoUnitario> getEmprestimoUnitariosDevolucao() {
        return emprestimoUnitariosDevolucao;
    }

    public void setEmprestimoUnitariosDevolucao(List<EmprestimoUnitario> emprestimoUnitariosDevolucao) {
        this.emprestimoUnitariosDevolucao = emprestimoUnitariosDevolucao;
    }

    public EmprestimoUnitario getEmprestimoUnitario() {
        return emprestimoUnitario;
    }

    public void setEmprestimoUnitario(EmprestimoUnitario emprestimoUnitario) {
        this.emprestimoUnitario = emprestimoUnitario;
    }

    public boolean isEnableSalvar() {
        return enableSalvar;
    }

    public void setEnableSalvar(boolean enableSalvar) {
        this.enableSalvar = enableSalvar;
    }
    
    public boolean isEditar() {
        return editar;
    }

    
    public void setEditar(boolean editar) {
        this.editar = editar;
    }

    
    public boolean isFecharDialog() {
        return fecharDialog;
    }

    
    public void setFecharDialog(boolean fecharDialog) {
        this.fecharDialog = fecharDialog;
    }

    public DataTable getTabelaEmprestimo() {
        return tabelaEmprestimo;
    }

    public void setTabelaEmprestimo(DataTable tabelaEmprestimo) {
        this.tabelaEmprestimo = tabelaEmprestimo;
    }

    public DataTable getTabelaEmprestimoUnitario() {
        return tabelaEmprestimoUnitario;
    }

    public void setTabelaEmprestimoUnitario(DataTable tabelaEmprestimoUnitario) {
        this.tabelaEmprestimoUnitario = tabelaEmprestimoUnitario;
    }

    
    public List<Estoque> getEstoquesDisponiveis() {
        return estoquesDisponiveis;
    }

    
    public void setEstoquesDisponiveis(List<Estoque> estoquesDisponiveis) {
        this.estoquesDisponiveis = estoquesDisponiveis;
    }

    
    public Estoque getEstoqueSelecionado() {
        return estoqueSelecionado;
    }

    
    public void setEstoqueSelecionado(Estoque estoqueSelecionado) {
        this.estoqueSelecionado = estoqueSelecionado;
    }
}
