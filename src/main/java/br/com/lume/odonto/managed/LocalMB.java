package br.com.lume.odonto.managed;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ActionEvent;

import org.apache.log4j.Logger;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.event.NodeSelectEvent;
import org.primefaces.event.NodeUnselectEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.dominio.DominioSingleton;
import br.com.lume.local.LocalSingleton;
//import br.com.lume.odonto.bo.DominioBO;
//import br.com.lume.odonto.bo.LocalBO;
//import br.com.lume.odonto.bo.ProfissionalBO;
import br.com.lume.odonto.entity.Dominio;
import br.com.lume.odonto.entity.Local;
import br.com.lume.odonto.util.OdontoMensagens;

@ManagedBean
@ViewScoped
public class LocalMB extends LumeManagedBean<Local> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(LocalMB.class);
   
    private String passivelEmprestimo;
    
    private String disponivelRetornoEsterilizacao;

    private String descricao;

    private TreeNode root, rootPai, selectedLocal, selectedLocalPai;

    private boolean disable;

    //EXPORTAÇÃO TABELA
    private DataTable tabelaLocal;
    
   // private DominioBO dominioBO;

    public LocalMB() {
        super(LocalSingleton.getInstance().getBo());
     //   this.dominioBO = new DominioBO();
        this.setClazz(Local.class);
        this.setDisable(false);
        try {
          //  this.setTiposLocais(DominioSingleton.getInstance().getBo().listByEmpresaAndObjetoAndTipo(LOCAL, OBJETO));
            this.root = new DefaultTreeNode("", null);
            this.rootPai = new DefaultTreeNode("", null);
            Local firstLevel = new Local();
            firstLevel.setDescricao("RAIZ");
            this.chargeTree(new DefaultTreeNode(firstLevel, this.root));
            this.chargeTree(new DefaultTreeNode(firstLevel, this.rootPai));
        } catch (Exception e) {
            this.log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
        }
    }

    @Override
    public void actionPersist(ActionEvent event) {
        boolean error = false;
        this.getEntity().setIdEmpresa(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());      
        this.getEntity().setPassivelEmprestimo(this.passivelEmprestimo);
        this.getEntity().setDisponivelRetornoEsterilizacao(this.disponivelRetornoEsterilizacao);
        this.getEntity().setDesconsideraDoEstoque("N");
        //itens em processo de lavagem, esterilizacao, ou em uso nao estao disponiveis
        this.getEntity().setDisponivelParaUso("S");
        if (this.selectedLocalPai != null) {
            if (((Local) this.selectedLocalPai.getData()).getDescricao().equals("RAIZ")) {
                this.getEntity().setLocalPai(null);
            } else {
                this.getEntity().setLocalPai((Local) this.selectedLocalPai.getData());
            }
        }
        for (Local local : this.getLocais()) {
            if ((this.getEntity().getId() != local.getId() && local.getDescricao().equalsIgnoreCase(this.getDescricao()))) {
                this.log.error("erro.categoria.duplicidade");
                this.addError(OdontoMensagens.getMensagem("erro.local.duplicidade"), "");
                error = true;
                break;
            }
        }
        if (!error) {
            if (this.getEntity().equals(this.getEntity().getLocalPai())) {
                this.log.error("erro.pai.filho.iguais");
                this.addError(OdontoMensagens.getMensagem("erro.pai.filho.iguais"), "");
            } else {
                this.getEntity().setDescricao(this.descricao);
                super.actionPersist(event);
            }
        }
    }

    @Override
    public void actionNew(ActionEvent event) {
        super.actionNew(event);
        this.setDisable(false);
    }

    @Override
    public void actionRemove(ActionEvent event) {
        boolean remove = true;
        for (Local local : this.getLocais()) {
            if ((local.getLocalPai() != null) && (this.getEntity().getId() == local.getLocalPai().getId())) {
                this.log.error("erro.excluir.pai.filho.iguais");
                this.addError(OdontoMensagens.getMensagem("erro.excluir.pai.filho.iguais"), "");
                remove = false;
                break;
            }
        }
        if (remove) {
            super.actionRemove(event);
        }
    }

    public void carregaTela() {
        this.setSelectedPai();
        this.setEntity((Local) this.selectedLocal.getData());
        this.setDescricao(this.getEntity().getDescricao());
       this.passivelEmprestimo = "S";
       this.disponivelRetornoEsterilizacao = "S";
    }

  

    public void setSelectedPai() {
        List<TreeNode> nodes = new ArrayList<>();
        List<TreeNode> nodesAux = new ArrayList<>();
        boolean hasChildren = false;
        nodes.add(this.getRootPai());
        hasChildren = true;
        while (hasChildren) {
            nodesAux = new ArrayList<>();
            for (TreeNode node : nodes) {
                Object localPai = node.getData();
                Local local = (Local) this.selectedLocal.getData();
                node.setSelected(false);
                if ((((local.getLocalPai() != null)) && (localPai instanceof String) && (localPai.equals(local.getLocalPai().getDescricao()))) || // RAIZ?
                        ((localPai instanceof Local) && (((Local) localPai).equals(local.getLocalPai()))) || // Encontrou o Node?
                        ((localPai instanceof Local) && (local.getLocalPai() == null) && (((Local) localPai).getDescricao().equals("RAIZ")))) { // ROOT?
                    node.setSelected(true);
                    this.setSelectedLocalPai(node);
                    if (!(local.getLocalPai() == null)) {
                        this.setDisable(true);
                    } else {
                        this.setDisable(false);
                    }
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

//    public List<Local> getEstoques() {
//        List<Local> locais = new ArrayList<>();
//        try {
//            locais = LocalSingleton.getInstance().getBo().listByEmpresaAndTipo(ESTOQUE, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
//          
//        } catch (Exception e) {
//            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
//            this.log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
//        }
//        return locais;
//    }
//
//    public List<Local> getConsultorios() {
//        List<Local> locais = new ArrayList<>();
//        try {
//            locais = LocalSingleton.getInstance().getBo().listByEmpresaAndTipo(CONSULTORIO, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
//        } catch (Exception e) {
//            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
//            this.log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
//        }
//        return locais;
//    }

    public void chargeTree(TreeNode root) {
        List<TreeNode> nodes = new ArrayList<>();
        List<TreeNode> nodesAux;
        List<Local> locaisRestantes = this.getLocais();
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

    public List<Local> setLevel(List<Local> locaisRestantes, List<TreeNode> nodes) {
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

    public boolean isDisable() {
        return this.disable;
    }

    public void setDisable(boolean disable) {
        this.disable = disable;
    }

    public void carregarEditar(Local local) {
        this.setEntity(local);
        this.descricao = local.getDescricao();
        this.passivelEmprestimo = getEntity().getPassivelEmprestimo();
        this.disponivelRetornoEsterilizacao = getEntity().getDisponivelRetornoEsterilizacao();
    }
    
    public List<Local> getLocais() {
        List<Local> locais = new ArrayList<>();
        try {
            locais = LocalSingleton.getInstance().getBo().listByEmpresa(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa(),false);  
       
           // locais.removeIf(n -> (n.getDescricaoTipo() == null || n.getDescricaoTipo().equals(""))); 
            
            Collections.sort(locais);
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            this.log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
        }
        return locais;
    }

    public void onNodeSelect(NodeSelectEvent event) {
        try {
            Local local = (Local) (event.getTreeNode().getData());
            if (local.getLocalPai() == null) {
                this.setDisable(false);
            } else {
                this.setDisable(true);
                this.getEntity().setTipo(local.getTipo());
                this.passivelEmprestimo = "S";
                this.disponivelRetornoEsterilizacao = "S";
            }
            this.getSelectedLocalPai().setSelected(false);
            this.setSelectedLocalPai(event.getTreeNode());
            this.getSelectedLocalPai().setSelected(true);
        } catch (Exception e) {
            this.log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
        }
    }

    public void onNodeUnselect(NodeUnselectEvent event) {
        this.setDisable(false);
        try {
            this.getEntity().setTipo(((Local) this.getSelectedLocal().getData()).getTipo());
            this.passivelEmprestimo = "S";
            this.disponivelRetornoEsterilizacao = "S";
        } catch (Exception e) {
            this.log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
        }
    }
    
    public void exportarTabela(String type) {
        exportarTabela("Locais", tabelaLocal, type);
    }

    public String getDescricao() {
        return this.descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

//    public String getTipo() {
//        return this.tipo;
//    }
//
//    public void setTipo(String tipo) {
//        this.tipo = tipo;
//    }

    public TreeNode getRoot() {
        return this.root;
    }

    public void setRoot(TreeNode root) {
        this.root = root;
    }

    public TreeNode getSelectedLocal() {
        return this.selectedLocal;
    }

    public void setSelectedLocal(TreeNode selectedLocal) {
        this.selectedLocal = selectedLocal;
    }

    public TreeNode getSelectedLocalPai() {
        return this.selectedLocalPai;
    }

    public void setSelectedLocalPai(TreeNode selectedLocalPai) {
        this.selectedLocalPai = selectedLocalPai;
    }

    public TreeNode getRootPai() {
        return this.rootPai;
    }

    public void setRootPai(TreeNode rootPai) {
        this.rootPai = rootPai;
    }

    public DataTable getTabelaLocal() {
        return tabelaLocal;
    }

    public void setTabelaLocal(DataTable tabelaLocal) {
        this.tabelaLocal = tabelaLocal;
    }

    private static final String CONSULTORIO = "CO";

    private static final String ESTOQUE = "ES";

    private static final String LOCAL = "local";

    private static final String OBJETO = "tipo";

    
    public String getPassivelEmprestimo() {
        return passivelEmprestimo;
    }

    
    public void setPassivelEmprestimo(String passivelEmprestimo) {
        this.passivelEmprestimo = passivelEmprestimo;
    }

    
    public String getDisponivelRetornoEsterilizacao() {
        return disponivelRetornoEsterilizacao;
    }

    
    
    public void setDisponivelRetornoEsterilizacao(String disponivelRetornoEsterilizacao) {
        this.disponivelRetornoEsterilizacao = disponivelRetornoEsterilizacao;
    }
}
