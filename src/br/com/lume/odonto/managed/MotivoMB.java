package br.com.lume.odonto.managed;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ActionEvent;

import org.apache.log4j.Logger;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.odonto.bo.ContaBO;
import br.com.lume.odonto.bo.MotivoBO;
import br.com.lume.odonto.entity.Conta;
import br.com.lume.odonto.entity.Item;
import br.com.lume.odonto.entity.Motivo;

@ManagedBean
@ViewScoped
public class MotivoMB extends LumeManagedBean<Motivo> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(MotivoMB.class);

    private List<Motivo> motivos = new ArrayList<>();

    private List<Conta> contas = new ArrayList<>();

    private TreeNode root, selectedConta;

    private String digitacao;

    private ContaBO contaBO;

    public MotivoMB() {
        super(new MotivoBO());
        contaBO = new ContaBO();
        this.geraLista();
        this.setClazz(Motivo.class);
        try {
            this.setRoot(new DefaultTreeNode("", null));
            Item firstLevel = new Item();
            firstLevel.setDescricao("RAIZ");
        } catch (Exception e) {
            log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
        }
    }

    private void geraLista() {
        try {
            motivos = ((MotivoBO) this.getbO()).listAll();
            contas = contaBO.listAll();
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
        }
        Collections.sort(contas);
        Collections.sort(motivos);
    }

    @Override
    public void actionPersist(ActionEvent event) {
        super.actionPersist(event);
        this.geraLista();
    }

    public List<String> filtraConta(String digitacao) {
        this.setDigitacao(digitacao);
        return this.convert(this.getContas());
    }

    public List<String> convert(List<Conta> contas) {
        List<String> strings = new ArrayList<>();
        for (Conta conta : contas) {
            strings.add(conta.getDescricao());
        }
        return strings;
    }

    public List<Motivo> getMotivos() {
        return motivos;
    }

    public void setMotivos(List<Motivo> motivos) {
        this.motivos = motivos;
    }

    public List<Conta> getContas() {
        return contas;
    }

    public void setContas(List<Conta> contas) {
        this.contas = contas;
    }

    public TreeNode getRoot() {
        return root;
    }

    public void setRoot(TreeNode root) {
        this.root = root;
    }

    public TreeNode getSelectedConta() {
        return selectedConta;
    }

    public void setSelectedConta(TreeNode selectedConta) {
        this.selectedConta = selectedConta;
    }

    public void setDigitacao(String digitacao) {
        this.digitacao = digitacao;
    }
}
