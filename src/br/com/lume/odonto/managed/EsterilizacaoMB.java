package br.com.lume.odonto.managed;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
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

import br.com.lume.emprestimoUnitario.EmprestimoUnitarioSingleton;
import br.com.lume.common.exception.business.BusinessException;
import br.com.lume.common.exception.techinical.TechnicalException;
import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.Status;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.emprestimoKit.EmprestimoKitSingleton;
import br.com.lume.dominio.DominioSingleton;
import br.com.lume.esterilizacao.EsterilizacaoSingleton;
import br.com.lume.esterilizacaoKit.EsterilizacaoKitSIngleton;
import br.com.lume.estoque.EstoqueSingleton;
import br.com.lume.kit.KitSingleton;
import br.com.lume.local.LocalSingleton;
import br.com.lume.material.MaterialSingleton;
import br.com.lume.materialLog.MaterialLogSingleton;
//import br.com.lume.odonto.bo.EmprestimoUnitarioBO;
//import br.com.lume.odonto.bo.EmprestimoKitBO;
//import br.com.lume.odonto.bo.DominioBO;
//import br.com.lume.odonto.bo.EsterilizacaoBO;
//import br.com.lume.odonto.bo.EsterilizacaoKitBO;
//import br.com.lume.odonto.bo.KitBO;
//import br.com.lume.odonto.bo.MaterialBO;
//import br.com.lume.odonto.bo.MaterialLogBO;
//import br.com.lume.odonto.bo.ProfissionalBO;
import br.com.lume.odonto.datamodel.EsterilizacaoDataModel;
import br.com.lume.odonto.entity.EmprestimoUnitario;
import br.com.lume.odonto.entity.EmprestimoKit;
import br.com.lume.odonto.entity.Dominio;
import br.com.lume.odonto.entity.Esterilizacao;
import br.com.lume.odonto.entity.EsterilizacaoKit;
import br.com.lume.odonto.entity.Item;
import br.com.lume.odonto.entity.Kit;
import br.com.lume.odonto.entity.KitItem;
import br.com.lume.odonto.entity.Local;
import br.com.lume.odonto.entity.Material;
import br.com.lume.odonto.entity.MaterialLog;
import br.com.lume.odonto.entity.Profissional;
import br.com.lume.odonto.util.OdontoMensagens;
import br.com.lume.profissional.ProfissionalSingleton;

@ManagedBean
@ViewScoped
public class EsterilizacaoMB extends LumeManagedBean<Esterilizacao> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(EsterilizacaoMB.class);

    private TreeNode root, selectedItem;

    private Long quantidade;

    private EsterilizacaoKit esterilizacaoKit;

    private Kit kit;

    private EsterilizacaoKit esterilizacaoKitSelecionada;

    private Dominio justificativa;

    private Long quantidadeDescarte;

    private List<Kit> kits;

    private Item item;

    private List<Item> itens = new ArrayList<>();

    private List<Esterilizacao> esterilizacoes, esterilizacoesSolicitadas;

    private List<Esterilizacao> esterilizacaoSelecionadas;

    private EsterilizacaoDataModel esterilizacaoDataModel;

    private String digitacao, data;

    private boolean incluindo, enableDevolucao;

    private List<EsterilizacaoKit> esterilizacaoKits = new ArrayList<>();

    private List<EsterilizacaoKit> kitsSolicitados;

    private Date dataAtual, dataValidade;

    private String msg;

  //  private EsterilizacaoKitBO esterilizacaoKitBO;

  //  private EmprestimoUnitarioBO emprestimoUnitarioBO;

   // private EmprestimoKitBO controleMaterialBO;

  //  private MaterialBO materialBO;

  //  private KitBO kitBO;

 //   private ProfissionalBO profissionalBO;

 //   private DominioBO dominioBO;

 //   private EsterilizacaoBO esterilizacaoBO;

 //   private MaterialLogBO materialLogBO = new MaterialLogBO();

    //EXPORTAÇÃO TABELA
    private DataTable tabelaEsterilizacao;
    
    public EsterilizacaoMB() {
        super(EsterilizacaoSingleton.getInstance().getBo());
     //   esterilizacaoKitBO = new EsterilizacaoKitBO();
     //   emprestimoUnitarioBO = new EmprestimoUnitarioBO();
     //   controleMaterialBO = new EmprestimoKitBO();
      //  materialBO = new MaterialBO();
     //   kitBO = new KitBO();
     //   profissionalBO = new ProfissionalBO();
    //    dominioBO = new DominioBO();
  //      esterilizacaoBO = new EsterilizacaoBO();
        this.setClazz(Esterilizacao.class);
        this.setIncluindo(true);
        dataAtual = new Date();
        try {
            this.geraLista();
            this.geraListaSolicitadas();
            this.setRoot(new DefaultTreeNode("", null));
            this.chargeTreeLocal(new DefaultTreeNode("RAIZ", this.getRoot()));
            this.setEnableDevolucao(false);
            for (Kit kit : this.getKits()) {
                for (KitItem kitItem : kit.getKitItens()) {
                    itens.add(kitItem.getItem());
                }
            }
        } catch (Exception e) {
            log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
        }
    }

    public List<Esterilizacao> getEsterilizacoesSolicitadas() {
        return esterilizacoesSolicitadas;
    }

    public void habilitaDevolucao() {
        if (getEsterilizacaoSelecionadas() != null && !getEsterilizacaoSelecionadas().isEmpty()) {
            this.setEnableDevolucao(true);
        } else {
            this.setEnableDevolucao(false);
        }
    }

    @Override
    public void actionPersist(ActionEvent event) {
        this.getEntity().setIdEmpresa(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
        this.getEntity().setData(Calendar.getInstance().getTime());
        this.getEntity().setProfissional(UtilsFrontEnd.getProfissionalLogado());
        this.getEntity().setStatus(Esterilizacao.ABERTO);
        for (EsterilizacaoKit esterilizacaoItem : this.getEsterilizacaoKits()) {
            if (esterilizacaoItem.getId() == 0 || !this.getEntity().getEsterilizacaoKits().contains(esterilizacaoItem)) {
                this.getEntity().getEsterilizacaoKits().add(esterilizacaoItem);
            }
        }
        /*
         * if ((getEsterilizacaoKits() == null) || (getEsterilizacaoKits().size() < 1)) { log.error(OdontoMensagens.getMensagem("error.esterilizacaokits.vazio"));
         * addError(OdontoMensagens.getMensagem("error.esterilizacaokits.vazio"), ""); } else {
         */
        try {
            this.getbO().persist(this.getEntity());
            this.geraProtocolo();
            this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");
            this.geraLista();
            PrimeFaces.current().ajax().addCallbackParam("dlg", true);
        } catch (Exception e) {
            log.error("Erro no actionPersist", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
        }
        /* } */
    }

    public void actionDescarte(ActionEvent event) throws Exception {
        
        Profissional profisisonalLogado = UtilsFrontEnd.getProfissionalLogado();
        
        if (this.getQuantidadeDescarte() > this.getEsterilizacaoKitSelecionada().getQuantidade()) {
            this.addError(OdontoMensagens.getMensagem("lavagem.descarte.acima"), "");
        } else if (this.getEsterilizacaoKitSelecionada().getItem() == null) {
            this.addError(OdontoMensagens.getMensagem("lavagem.descarte.soItem"), "");
        } else {
            if (this.getEsterilizacaoKitSelecionada().getEsterilizacao().getClinica()) {

                this.getEsterilizacaoKitSelecionada().setQuantidade(this.getEsterilizacaoKitSelecionada().getQuantidade() - this.getQuantidadeDescarte());
                EsterilizacaoKitSIngleton.getInstance().getBo().persist(this.getEsterilizacaoKitSelecionada());
                EmprestimoUnitario a = this.getEsterilizacaoKitSelecionada().getEmprestimoUnitario();
                EmprestimoKit cm = this.getEsterilizacaoKitSelecionada().getEmprestimoKit();
                Material m;
                if (a != null) {
                    m = a.getMaterial();
                    a.setQuantidade(a.getQuantidade().subtract(new BigDecimal(this.getQuantidadeDescarte())));
                    EmprestimoUnitarioSingleton.getInstance().getBo().persist(a);
                   // MaterialLogSingleton.getInstance().getBo().persist(new MaterialLog(null, a, m, profisisonalLogado, new BigDecimal(getQuantidadeDescarte() * -1), m.getQuantidadeAtual(),
                        //    MaterialLog.DEVOLUCAO_ESTERILIZACAO_DESCARTAR));
                } else if (cm != null) {
                    m = cm.getMaterial();
                    cm.setQuantidade(cm.getQuantidade().subtract(new BigDecimal(this.getQuantidadeDescarte())));
                    EmprestimoKitSingleton.getInstance().getBo().persist(cm);
                  //  MaterialLogSingleton.getInstance().getBo().persist(new MaterialLog(cm, null, m, profisisonalLogado, new BigDecimal(getQuantidadeDescarte() * -1), m.getQuantidadeAtual(),
                   //        MaterialLog.DEVOLUCAO_ESTERILIZACAO_DESCARTAR));;
                } else {
                    m = (Material) MaterialSingleton.getInstance().getBo().listAllAtivosByEmpresaAndItem(this.getEsterilizacaoKitSelecionada().getItem(), UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
                  //  MaterialLogSingleton.getInstance().getBo().persist(new MaterialLog(null, null, m, profisisonalLogado, new BigDecimal(getQuantidadeDescarte() * -1), m.getQuantidadeAtual(),
                 //           MaterialLog.DEVOLUCAO_ESTERILIZACAO_DESCARTAR));
                }                
                
                Local descarte = LocalSingleton.getInstance().getBo().getLocalPorDescricao(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa(),"DESCARTE");
                Local localOrigem = LocalSingleton.getInstance().getBo().getLocalPorDescricao(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa(), "DESCARTAR_ESTERILIZACAO"); 
                EstoqueSingleton.getInstance().transferencia(m, localOrigem, descarte, new BigDecimal(this.getQuantidadeDescarte()), EstoqueSingleton.DESCARTAR_ESTERILIZACAO, UtilsFrontEnd.getProfissionalLogado());
                
                
               // Material m2 = new Material();

               // m2.setItem(m.getItem());
               // m2.setLocal(m.getLocal());
               // m2.setMarca(m.getMarca());
               // m2.setDataCadastro(Calendar.getInstance().getTime());
               // m2.setLote(m.getLote());
               // m2.setFornecedor(m.getFornecedor());
               // m2.setExcluidoPorProfissional(profisisonalLogado.getId());
               // m2.setQuantidadeAtual(new BigDecimal(this.getQuantidadeDescarte()));
              //  m2.setQuantidade(new BigDecimal(this.getQuantidadeDescarte()));
              //  m2.setValor(m.getValor());
               // m2.setTamanhoUnidade(m.getTamanhoUnidade());
               // m2.setQuantidadeUnidade(m.getQuantidadeUnidade());
              //  m2.setIdEmpresa(m.getIdEmpresa());
              //  m2.setConsignacao(m.getConsignacao());

              //  m2.setStatus(MaterialMB.DESCARTE);
               // m2.setJustificativa(this.getJustificativa().getNome());
              //  this.persist(m2);

                if (getEsterilizacaoKitSelecionada().getQuantidade() == 0) {
                    Esterilizacao e = getEsterilizacaoKitSelecionada().getEsterilizacao();
                    e.setDevolvidoPorProfissional(profisisonalLogado);
                    e.setStatus(Esterilizacao.DESCARTADO);
                    e.setDataDevolucao(Calendar.getInstance().getTime());
                    EsterilizacaoSingleton.getInstance().getBo().persist(e);
                }

                this.actionNew(event);
                this.setEsterilizacoesSolicitadas(null);
                this.geraListaSolicitadas();
                this.setEnableDevolucao(false);
                this.actionNew(event);
                this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");
                PrimeFaces.current().ajax().addCallbackParam("descartar", true);
            } else {
                this.addError(OdontoMensagens.getMensagem("lavagem.descarte.externo"), "");
            }
        }
    }

//    public void persist(Material m) throws Exception {
//        Material mNew = new Material();
//        mNew.setDataCadastro(Calendar.getInstance().getTime());
//        mNew.setIdEmpresa(m.getIdEmpresa());
//        mNew.setItem(m.getItem());
//        mNew.setJustificativa(m.getJustificativa());
//        mNew.setLocal(m.getLocal());
//        mNew.setLote(m.getLote());
//        mNew.setMarca(m.getMarca());
//        mNew.setNotaFiscal(m.getNotaFiscal());
//        mNew.setProcedencia(m.getProcedencia());
//        mNew.setQuantidadeAtual(m.getQuantidadeAtual());
//       // mNew.setQuantidade(m.getQuantidade());
//        mNew.setConsignacao(m.getConsignacao());
//       // mNew.setQuantidadeUnidade(m.getQuantidadeUnidade());
//        mNew.setStatus(m.getStatus());
//        mNew.setTamanhoUnidade(m.getTamanhoUnidade());
//        mNew.setValidade(m.getValidade());
//        mNew.setValor(m.getValor());
//        MaterialSingleton.getInstance().getBo().persist(mNew);
//    }

    private void geraProtocolo() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy - hh:mm:ss");
        data = "<b>PROTOCOLO DE ENTREGA Nº: " + this.getEntity().getId();
        for (EsterilizacaoKit esterilizacaoKit : this.getEntity().getEsterilizacaoKits()) {
            String descricao;
            if (esterilizacaoKit.getKit() != null) {
                descricao = esterilizacaoKit.getKit().getDescricao();
            } else {
                descricao = esterilizacaoKit.getItem().getDescricao();
            }
            data += "<br>" + descricao + " - " + esterilizacaoKit.getQuantidade();
        }
        data += "<br>" + "OBS: " + this.getEntity().getObservacao();
        data += "<br>" + "DATA : " + sdf.format(this.getEntity().getData()) + "</b>";
        data = data.toUpperCase();
    }

    @Override
    public void actionNew(ActionEvent event) {
        super.actionNew(event);
        this.setIncluindo(true);
        this.setDigitacao(null);
        this.setSelectedItem(null);
        this.setSelected();
        this.setEsterilizacaoKits(new ArrayList<EsterilizacaoKit>());
    }

    public void adicionar() {
        if (this.validaKit()) {
            EsterilizacaoKit esterilizacaoKit = new EsterilizacaoKit();
            esterilizacaoKit.setKit(this.getKit());
            esterilizacaoKit.setItem(this.getItem());
            esterilizacaoKit.setEsterilizacao(this.getEntity());
            esterilizacaoKit.setQuantidade(this.getQuantidade());
            if (this.getEsterilizacaoKits() == null) {
                this.setEsterilizacaoKits(new ArrayList<EsterilizacaoKit>());
            }
            esterilizacaoKit.setId(new Long(0));
            this.getEsterilizacaoKits().add(esterilizacaoKit);
            if (this.getEntity().getEsterilizacaoKits() == null) {
                this.getEntity().setEsterilizacaoKits(new ArrayList<EsterilizacaoKit>());
            }
        }
    }

    public void remover() throws Exception {
        try {
            EsterilizacaoKitSIngleton.getInstance().getBo().remove(this.getEsterilizacaoKit());
        } catch (BusinessException e) {
            e.printStackTrace();
        } catch (TechnicalException e) {
            e.printStackTrace();
        }
        this.getEsterilizacaoKits().remove(this.getEsterilizacaoKit());
        this.setIncluindo(true);
    }

    public void atualizar() throws Exception {
        if (this.validaKit()) {
            this.remover();
            this.adicionar();
        }
    }

    public void limpar() {
        this.setDigitacao(null);
        this.setQuantidade(new Long(0));
        if (this.getSelectedItem() != null) {
            this.getSelectedItem().setSelected(false);
        }
        this.setKit(null);
        this.setIncluindo(true);
        this.setEsterilizacaoKit(null);
    }

    public List<String> filtraKit(String digitacao) {
        List<String> retorno;
        this.setDigitacao(digitacao);
        this.filtraKits();
        retorno = this.convert(this.filtraItens());
        retorno.addAll(this.convert(this.getKits()));
        return retorno;
    }

    private List<Item> filtraItens() {
        List<Item> itens = new ArrayList<>();
        try {
            if (this.getDigitacao() != null) {
                for (Item item : this.getItens()) {
                    if (item.getDescricaoLimpa().toLowerCase().contains(this.getDigitacao().toLowerCase())) {
                        itens.add(item);
                    }
                }
            } else {
                itens = this.getItens();
            }
            Collections.sort(itens);
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
        }
        return itens;
    }

    public List<String> convert(@SuppressWarnings("rawtypes") List objects) {
        List<String> strings = new ArrayList<>();
        for (Object object : objects) {
            if (object instanceof Kit) {
                strings.add(((Kit) object).getDescricao());
            } else if (object instanceof Item) {
                strings.add(((Item) object).getDescricao());
            }
        }
        return strings;
    }

    public void handleSelect() {
        this.filtraKit(this.getDigitacao());
        this.setSelected();
    }

    public boolean validaKit() {
        if (this.getKit() == null && this.getItem() == null) {
            log.error(OdontoMensagens.getMensagem("erro.kitItem.obrigatorio"));
            this.addError(OdontoMensagens.getMensagem("erro.kitItem.obrigatorio"), "");
            return false;
        } else if (this.getQuantidade() == 0 || this.getQuantidade() < 1) {
            log.error(OdontoMensagens.getMensagem("erro.quantidade.obrigatorio"));
            this.addError(OdontoMensagens.getMensagem("erro.quantidade.obrigatorio"), "");
            return false;
        }
        return true;
    }

    public void carregaTelaGeral() {
        this.setEsterilizacaoKits(new ArrayList<EsterilizacaoKit>());
        this.setEsterilizacaoKits(this.getEntity().getEsterilizacaoKits());
        this.setDigitacao(null);
        this.setSelectedItem(null);
        this.setSelected();
    }

    public void carregaTela() {
        this.setSelected();
        try {
            this.setKit(this.getEsterilizacaoKit().getKit());
            this.setItem(this.getEsterilizacaoKit().getItem());
            if (this.getKit() != null) {
                this.setDigitacao(this.getKit().getDescricao());
            } else {
                this.setDigitacao(this.getItem().getDescricao());
            }
            this.setQuantidade(this.getEsterilizacaoKit().getQuantidade());
            this.setIncluindo(false);
        } catch (Exception e) {
            log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
        }
    }

    public void setSelected() {
        List<TreeNode> nodes = new ArrayList<>();
        List<TreeNode> nodesAux = new ArrayList<>();
        boolean hasChildren = true;
        nodes.add(this.getRoot());
        String selecionado;
        if (this.getEsterilizacaoKit() != null) {
            if (this.getEsterilizacaoKit().getKit() != null) {
                selecionado = this.getEsterilizacaoKit().getKit().getDescricao();
            } else {
                selecionado = this.getEsterilizacaoKit().getItem().getDescricao();
            }
        } else {
            selecionado = this.getDigitacao();
        }
        while (hasChildren) {
            nodesAux = new ArrayList<>();
            for (TreeNode node : nodes) {
                Object kit = node.getData();
                node.setSelected(false);
                if ((((kit instanceof String) && (kit.equals(selecionado))) || // RAIZ?
                        ((kit instanceof Kit) && (((Kit) kit).getDescricao().equals(selecionado)))) || kit instanceof Item) { // Encontrou
                                                                                                                             // o
                                                                                                                             // Node?
                    node.setSelected(true);
                    this.setItemKitSelecionado((String) kit);
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

    public void chargeTreeLocal(TreeNode root) {
        List<TreeNode> nodes = new ArrayList<>();
        List<TreeNode> nodesAux;
        this.filtraKits();
        List<Kit> locaisRestantes = this.getKits();
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

    public List<Kit> setLevelLocal(List<Kit> locaisRestantes, List<TreeNode> nodes) {
        boolean anotherLevel;
        List<Kit> locaisRestantesAux = new ArrayList<>();
        for (Kit kit : locaisRestantes) {
            anotherLevel = true;
            for (TreeNode node : nodes) {
                TreeNode nodeKit = new DefaultTreeNode(kit.getDescricao(), node);
                nodeKit.setExpanded(false);
                for (KitItem ki : kit.getKitItens()) {
                    if (!ki.getExcluido().equals("S")) {
                        (new DefaultTreeNode(ki.getItem().getDescricao(), nodeKit)).setExpanded(true);
                    }
                }
                anotherLevel = false;
                break;
            }
            if (anotherLevel) {
                locaisRestantesAux.add(kit);
            }
        }
        return locaisRestantesAux;
    }

    public void filtraKits() {
        this.setKits(new ArrayList<Kit>());
        try {
            if (this.getDigitacao() != null) {
                this.setKits(KitSingleton.getInstance().getBo() .listByEmpresaAndDescricaoParcialAndTipo(this.getDigitacao(), UtilsFrontEnd.getProfissionalLogado().getIdEmpresa()));
            } else {
                this.setKits(KitSingleton.getInstance().getBo().listByEmpresaAndTipo(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa()));
            }
            Collections.sort(kits);
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
        }
    }

    public List<Esterilizacao> getEsterilizacoes() {
        return esterilizacoes;
    }

    public void setEsterilizacoes(List<Esterilizacao> esterilizacoes) {
        this.esterilizacoes = esterilizacoes;
    }

    public void onNodeSelect(NodeSelectEvent event) {
        String descricao = event.getTreeNode().getData().toString();
        this.setDigitacao(descricao);
        this.setItemKitSelecionado(descricao);
        this.filtraKit(this.getDigitacao());
    }

    private void setItemKitSelecionado(String descricao) {
        try {
            for (Kit kit : KitSingleton.getInstance().getBo().listByEmpresa(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa())) {
                if (kit.getDescricao().equals(descricao)) {
                    this.setKit(kit);
                    this.setDigitacao(this.getKit().getDescricao());
                    this.setItem(null);
                    break;
                } else {
                    this.setKit(null);
                }
            }
            if (this.getKit() == null) {
                for (Item item : itens) {
                    if (item.getDescricao().equals(descricao)) {
                        this.setItem(item);
                        this.setKit(null);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void handleSelectProfissional(SelectEvent event) {
        this.getEntity().setSolicitante((Profissional) event.getObject());
    }

    public List<Profissional> geraSugestoes(String query) {
        List<Profissional> sugestoes = new ArrayList<>();
        List<Profissional> profissionais = new ArrayList<>();
        try {
            profissionais = ProfissionalSingleton.getInstance().getBo().listByEmpresa(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
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

    public void onNodeUnselect(NodeUnselectEvent event) {
        this.setKit(null);
    }

    public TreeNode getSelectedItem() {
        return selectedItem;
    }

    public TreeNode getRoot() {
        return root;
    }

    public void setRoot(TreeNode root) {
        this.root = root;
    }

    public EsterilizacaoKit getEsterilizacaoKit() {
        return esterilizacaoKit;
    }

    public void setEsterilizacaoKit(EsterilizacaoKit esterilizacaoKit) {
        this.esterilizacaoKit = esterilizacaoKit;
    }

    public boolean isIncluindo() {
        return incluindo;
    }

    public Long getQuantidade() {
        return quantidade;
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

    public String getDigitacao() {
        return this.getKit() != null ? this.getKit().getDescricao() : this.getItem() != null ? this.getItem().getDescricao() : digitacao;
    }

    public void setDigitacao(String digitacao) {
        this.digitacao = digitacao;
    }

    public String getData() {
        return data;
    }

    public void actionDevolucao(ActionEvent event) {
        try {
            
            Profissional profissionalLogado = UtilsFrontEnd.getProfissionalLogado();
            
            for (Esterilizacao e : this.getEsterilizacaoSelecionadas()) {
                if (e.getClinica() != null && e.getClinica()) {
                    for (EsterilizacaoKit lk : e.getEsterilizacaoKits()) {
                        if (lk.getEmprestimoKit() != null) {
                            lk.getEmprestimoKit().setQuantidade(lk.getEmprestimoKit().getQuantidade().subtract(new BigDecimal(lk.getQuantidade())));
                            br.com.lume.emprestimoKit.EmprestimoKitSingleton.getInstance().getBo().persist(lk.getEmprestimoKit());// Atualizando estoque
                            //MaterialSingleton.getInstance().getBo().refresh(lk.getEmprestimoKit().getMaterial());
                            //lk.getEmprestimoKit().getMaterial().setQuantidadeAtual(lk.getEmprestimoKit().getMaterial().getQuantidadeAtual().add(new BigDecimal(lk.getQuantidade())));
                           
                            Local localOrigem = LocalSingleton.getInstance().getBo().getLocalPorDescricao(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa(), "MATERIAL_ESTERELIZADO_DEVOLUCAO_LAVAGEM");
                            EstoqueSingleton.getInstance().transferencia(lk.getEmprestimoKit().getMaterial(),localOrigem,lk.getEmprestimoKit().getMaterial().getEstoque().get(0).getLocal(),new BigDecimal(lk.getQuantidade()),EstoqueSingleton.DEVOLUCAO_ESTERILIZACAO_ESTERILIZADO,UtilsFrontEnd.getProfissionalLogado());

                            
                            MaterialSingleton.getInstance().getBo().persist(lk.getEmprestimoKit().getMaterial());// Atualizando estoque
                            //MaterialLogSingleton.getInstance().getBo().persist(new MaterialLog(lk.getEmprestimoKit(), null, lk.getEmprestimoKit().getMaterial(), profissionalLogado,
                            //        new BigDecimal(lk.getQuantidade()), lk.getEmprestimoKit().getMaterial().getQuantidadeAtual(), MaterialLog.DEVOLUCAO_ESTERILIZACAO_ESTERILIZADO));
                        } else if (lk.getEmprestimoUnitario() != null) {
                            lk.getEmprestimoUnitario().setQuantidade(lk.getEmprestimoUnitario().getQuantidade().subtract(new BigDecimal(lk.getQuantidade())));
                            EmprestimoUnitarioSingleton.getInstance().getBo().persist(lk.getEmprestimoUnitario());// Atualizando estoque
                            MaterialSingleton.getInstance().getBo().refresh(lk.getEmprestimoUnitario().getMaterial());
                            //lk.getEmprestimoUnitario().getMaterial().setQuantidadeAtual(lk.getEmprestimoUnitario().getMaterial().getQuantidadeAtual().add(new BigDecimal(lk.getQuantidade())));
                            
                            Local localOrigem = LocalSingleton.getInstance().getBo().getLocalPorDescricao(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa(), "MATERIAL_ESTERELIZADO_DEVOLUCAO_LAVAGEM");
                            EstoqueSingleton.getInstance().transferencia(lk.getEmprestimoKit().getMaterial(),localOrigem,lk.getEmprestimoKit().getMaterial().getEstoque().get(0).getLocal(),new BigDecimal(lk.getQuantidade()),EstoqueSingleton.DEVOLUCAO_ESTERILIZACAO_ESTERILIZADO,UtilsFrontEnd.getProfissionalLogado());
                            
                            MaterialSingleton.getInstance().getBo().persist(lk.getEmprestimoUnitario().getMaterial());// Atualizando estoque
                            //MaterialLogSingleton.getInstance().getBo().persist(new MaterialLog(null, lk.getEmprestimoUnitario(), lk.getEmprestimoUnitario().getMaterial(), profissionalLogado,
                            //        new BigDecimal(lk.getQuantidade()), lk.getEmprestimoUnitario().getMaterial().getQuantidadeAtual(), MaterialLog.DEVOLUCAO_ESTERILIZACAO_ESTERILIZADO));
                        } else {
                            List<Material> material = MaterialSingleton.getInstance().getBo().listAllAtivosByEmpresaAndItem(lk.getItem(), UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
                            if (!material.isEmpty()) {
                                MaterialSingleton.getInstance().getBo().refresh(material.get(0));
                                //material.get(0).setQuantidadeAtual(material.get(0).getQuantidadeAtual().add(new BigDecimal(lk.getQuantidade())));
                                
                                 Local localOrigem = LocalSingleton.getInstance().getBo().getLocalPorDescricao(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa(), "MATERIAL_ESTERELIZADO_DEVOLUCAO_LAVAGEM");
                                EstoqueSingleton.getInstance().transferencia(lk.getEmprestimoKit().getMaterial(),localOrigem,lk.getEmprestimoKit().getMaterial().getEstoque().get(0).getLocal(),new BigDecimal(lk.getQuantidade()),EstoqueSingleton.DEVOLUCAO_ESTERILIZACAO_ESTERILIZADO,UtilsFrontEnd.getProfissionalLogado());
                              
                                
                                MaterialSingleton.getInstance().getBo().persist(material.get(0));// Atualizando estoque
                               // MaterialLogSingleton.getInstance().getBo().persist(new MaterialLog(null, null, material.get(0), profissionalLogado, new BigDecimal(lk.getQuantidade()),
                               //         material.get(0).getQuantidadeAtual(), MaterialLog.DEVOLUCAO_ESTERILIZACAO_ESTERILIZADO));
                            }
                        }
                    }
                }
                e.setDevolvidoPorProfissional(profissionalLogado);
                e.setStatus(Esterilizacao.DEVOLVIDO);
                e.setDataDevolucao(Calendar.getInstance().getTime());
                e.setDataValidade(dataValidade);
                EsterilizacaoSingleton.getInstance().getBo().persist(e);
            }
            this.setEsterilizacaoKitSelecionada(null);
            this.setEsterilizacaoSelecionadas(null);
            this.setKitsSolicitados(null);
            this.geraListaSolicitadas();
            this.setEnableDevolucao(false);
            dataValidade = null;
            PrimeFaces.current().ajax().addCallbackParam("esterilizar", true);
            this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");
        } catch (Exception e) {
            log.error(Mensagens.ERRO_AO_SALVAR_REGISTRO, e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
        }
    }

    public void actionEmpacotar(ActionEvent event) {
        try {
            for (Esterilizacao e : this.getEsterilizacaoSelecionadas()) {
                e.setStatus(Esterilizacao.EMPACOTADO);
                EsterilizacaoSingleton.getInstance().getBo().persist(e);
            }
            this.setEsterilizacaoKitSelecionada(null);
            this.setEsterilizacaoSelecionadas(null);
            this.setKitsSolicitados(null);
            this.geraListaSolicitadas();
            this.setEnableDevolucao(false);
            this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");
        } catch (Exception e) {
            log.error(Mensagens.ERRO_AO_SALVAR_REGISTRO, e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
        }
    }

    public void actionEsterilizacao(ActionEvent event) {
        try {
            for (Esterilizacao e : this.getEsterilizacaoSelecionadas()) {
                e.setDevolvidoPorProfissional(UtilsFrontEnd.getProfissionalLogado());
                e.setStatus(Esterilizacao.ESTERILIZADO);
                e.setDataValidade(dataValidade);
                EsterilizacaoSingleton.getInstance().getBo().persist(e);
            }
            this.setEsterilizacaoKitSelecionada(null);
            this.setEsterilizacaoSelecionadas(null);
            this.geraListaSolicitadas();
            this.setEnableDevolucao(false);
            this.setKitsSolicitados(null);
            this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");
            dataValidade = null;
        } catch (Exception e) {
            log.error(Mensagens.ERRO_AO_SALVAR_REGISTRO, e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
        }
    }

    public void setaValidade() {
        Dominio d = DominioSingleton.getInstance().getBo().findByEmpresaAndObjetoAndTipoAndNome("esterilizacao", "validade", "sugestao");
        int days = d != null ? Integer.parseInt(d.getValor()) : 0;
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, days);
        this.setDataValidade(cal.getTime());
    }

    public void setEsterilizacoesSolicitadas(List<Esterilizacao> esterilizacoesSolicitadas) {
        this.esterilizacoesSolicitadas = esterilizacoesSolicitadas;
    }

    public boolean isEnableDevolucao() {
        return enableDevolucao;
    }

    public void setEnableDevolucao(boolean enableDevolucao) {
        this.enableDevolucao = enableDevolucao;
    }

    public List<EsterilizacaoKit> getKitsSolicitados() {
        if (this.getEsterilizacaoSelecionadas() != null) {
            List<EsterilizacaoKit> eks = new ArrayList<>();
            for (Esterilizacao e : this.getEsterilizacaoSelecionadas()) {
                if (e != null) {
                    for (EsterilizacaoKit ek : e.getEsterilizacaoKits()) {
                        if (ek.getQuantidade() > 0) {
                            eks.add(ek);
                        }
                    }
                }
            }
            this.setKitsSolicitados(eks);
        }
        return kitsSolicitados;
    }

    public List<EsterilizacaoKit> getEsterilizacaoKits() {
        return esterilizacaoKits;
    }

    public void setEsterilizacaoKits(List<EsterilizacaoKit> esterilizacaoKits) {
        this.esterilizacaoKits = new ArrayList<>();
        for (EsterilizacaoKit esterilizacaoKit : esterilizacaoKits) {
            if (esterilizacaoKit.getExcluido().equals(Status.NAO)) {
                this.esterilizacaoKits.add(esterilizacaoKit);
            }
        }
    }

    public List<Kit> getKits() {
        this.filtraKits();
        return kits;
    }

    public void setKits(List<Kit> kits) {
        this.kits = kits;
    }

    public Kit getKit() {
        return kit;
    }

    public void setKit(Kit kit) {
        this.kit = kit;
    }

    public void setKitsSolicitados(List<EsterilizacaoKit> kitsSolicitados) {
        this.kitsSolicitados = kitsSolicitados;
    }

    public void geraListaSolicitadas() {
        try {
            
            long idEmpresaLogada = UtilsFrontEnd.getProfissionalLogado().getIdEmpresa();
            
            this.setEsterilizacoesSolicitadas(EsterilizacaoSingleton.getInstance().getBo().listByEmpresaAndStatus(Esterilizacao.ABERTO, idEmpresaLogada));
            esterilizacoesSolicitadas.addAll(EsterilizacaoSingleton.getInstance().getBo().listByEmpresaAndStatus(Esterilizacao.ESTERILIZADO, idEmpresaLogada));
            esterilizacoesSolicitadas.addAll(EsterilizacaoSingleton.getInstance().getBo().listByEmpresaAndStatus(Esterilizacao.EMPACOTADO, idEmpresaLogada));
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
        }
    }

    private void geraLista() {
        try {
            
            long idEmpresaLogada = UtilsFrontEnd.getProfissionalLogado().getIdEmpresa();
            
            esterilizacoes = new ArrayList<>();
            esterilizacoes.addAll(EsterilizacaoSingleton.getInstance().getBo().listByEmpresaAndStatus(Esterilizacao.ABERTO, idEmpresaLogada));
            esterilizacoes.addAll(EsterilizacaoSingleton.getInstance().getBo().listByEmpresaAndStatus(Esterilizacao.ESTERILIZADO, idEmpresaLogada));
            esterilizacoes.addAll(EsterilizacaoSingleton.getInstance().getBo().listByEmpresaAndStatus(Esterilizacao.EMPACOTADO, idEmpresaLogada));
            if (esterilizacoes != null) {
                Collections.sort(esterilizacoes);
            }
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
        }
    }

    public void exportarTabela(String type) {
        this.exportarTabela("Esterilizações", tabelaEsterilizacao, type);
    }
    
    public Date getDataAtual() {
        return dataAtual;
    }

    public void setDataAtual(Date dataAtual) {
        this.dataAtual = dataAtual;
    }

    public Date getDataValidade() {
        return dataValidade;
    }

    public void setDataValidade(Date dataValidade) {
        this.dataValidade = dataValidade;
    }

    public String getMsg() {
        return enableDevolucao ? "Esterilização sem kit " : " ";
    }

    public void setMsg(String msg) {
        this.msg = msg;
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

    public Long getQuantidadeDescarte() {
        return quantidadeDescarte;
    }

    public void setQuantidadeDescarte(Long quantidadeDescarte) {
        this.quantidadeDescarte = quantidadeDescarte;
    }

    public Dominio getJustificativa() {
        return justificativa;
    }

    public void setJustificativa(Dominio justificativa) {
        this.justificativa = justificativa;
    }

    public EsterilizacaoKit getEsterilizacaoKitSelecionada() {
        return esterilizacaoKitSelecionada;
    }

    public void setEsterilizacaoKitSelecionada(EsterilizacaoKit esterilizacaoKitSelecionada) {
        this.esterilizacaoKitSelecionada = esterilizacaoKitSelecionada;
    }

    public List<Dominio> getJustificativas() {
        List<Dominio> justificativas = new ArrayList<>();
        try {
            justificativas = DominioSingleton.getInstance().getBo().listByEmpresaAndObjetoAndTipo("descarte", "justificativa");
        } catch (Exception e) {
            log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS);
        }
        return justificativas;
    }

    public List<Esterilizacao> getEsterilizacaoSelecionadas() {
        return esterilizacaoSelecionadas;
    }

    public void setEsterilizacaoSelecionadas(List<Esterilizacao> esterilizacaoSelecionadas) {
        this.esterilizacaoSelecionadas = esterilizacaoSelecionadas;
    }

    public EsterilizacaoDataModel getEsterilizacaoDataModel() {
        esterilizacaoDataModel = new EsterilizacaoDataModel(this.getEsterilizacoesSolicitadas());
        return esterilizacaoDataModel;
    }

    public void setEsterilizacaoDataModel(EsterilizacaoDataModel esterilizacaoDataModel) {
        this.esterilizacaoDataModel = esterilizacaoDataModel;
    }

    public DataTable getTabelaEsterilizacao() {
        return tabelaEsterilizacao;
    }

    public void setTabelaEsterilizacao(DataTable tabelaEsterilizacao) {
        this.tabelaEsterilizacao = tabelaEsterilizacao;
    }
}
