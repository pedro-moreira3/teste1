package br.com.lume.odonto.managed;

import java.math.BigDecimal;
import java.math.MathContext;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Calendar;
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
import br.com.lume.common.util.Status;
import br.com.lume.odonto.bo.ConvenioBO;
import br.com.lume.odonto.bo.ConvenioProcedimentoBO;
import br.com.lume.odonto.bo.EspecialidadeBO;
import br.com.lume.odonto.bo.ProcedimentoBO;
import br.com.lume.odonto.bo.ProfissionalBO;
import br.com.lume.odonto.entity.Convenio;
import br.com.lume.odonto.entity.ConvenioProcedimento;
import br.com.lume.odonto.entity.Especialidade;
import br.com.lume.odonto.entity.Procedimento;
import br.com.lume.odonto.util.OdontoMensagens;

@ManagedBean
@ViewScoped
public class ProcedimentoMB extends LumeManagedBean<Procedimento> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(ProcedimentoMB.class);

    private List<Especialidade> especialidades;

    private TreeNode root;

    private TreeNode selectedNode;

    private String filtroTable;

    private EspecialidadeBO especialidadeBO;

    private ConvenioProcedimentoBO convenioProcedimentoBO;

    private ConvenioBO convenioBO;

    public ProcedimentoMB() {
        super(new ProcedimentoBO());
        especialidadeBO = new EspecialidadeBO();
        convenioProcedimentoBO = new ConvenioProcedimentoBO();
        convenioBO = new ConvenioBO();
        this.setClazz(Procedimento.class);
        this.geralist();
        this.carregaTreeProcedimentos(null);
    }

    private void geralist() {
        try {
            this.setEspecialidades(especialidadeBO.listByEmpresa());
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            log.error(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS));
        }
    }

    private void carregaTreeProcedimentos(String filtro) {
        root = new DefaultTreeNode("root", null);
        for (Especialidade especialidade : especialidades) {
            TreeNode node = new DefaultTreeNode(especialidade, root);
            node.setExpanded(true);
            List<Procedimento> procedimentos = this.removeExcluidos(especialidade);
            if (procedimentos != null) {
                Collections.sort(procedimentos);
            }
            for (Procedimento procedimento : procedimentos) {
                if (filtro != null && filtro.length() > 0) {
                    if (procedimento.getDescricaoIdLimpo().toUpperCase().contains(Normalizer.normalize(filtro.toUpperCase(), Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", ""))) {
                        TreeNode node2 = new DefaultTreeNode(procedimento, node);
                        node2.setExpanded(true);
                    }
                } else {
                    TreeNode node2 = new DefaultTreeNode(procedimento, node);
                    node2.setExpanded(true);
                }
            }
        }
    }

    private List<Procedimento> removeExcluidos(Especialidade especialidade) {
        List<Procedimento> procedimentos = especialidade.getProcedimentos();
        List<Procedimento> procedimentosAux = new ArrayList<>();
        for (Procedimento procedimento : procedimentos) {
            if (procedimento.getExcluido().equals(Status.SIM)) {
                procedimentosAux.add(procedimento);
            }
        }
        procedimentos.removeAll(procedimentosAux);
        return procedimentos;
    }

    public List<Especialidade> getEspecialidades() {
        if (especialidades != null) {
            Collections.sort(especialidades);
        }
        return especialidades;
    }

    public void setEspecialidades(List<Especialidade> especialidades) {
        this.especialidades = especialidades;
    }

    public TreeNode getRoot() {
        return root;
    }

    public void setRoot(TreeNode root) {
        this.root = root;
    }

    public TreeNode getSelectedNode() {
        return selectedNode;
    }

    @Override
    public void actionRemove(ActionEvent event) {
        super.actionRemove(event);
        this.geralist();
    }

    @Override
    public void actionPersist(ActionEvent event) {
        if (this.getEntity().getQuantidadeFaces() != null && this.getEntity().getQuantidadeFaces() > 5) {
            this.addError(OdontoMensagens.getMensagem("procedimento.faces.maximo"), "");
        } else {
            if (this.getEntity().getIdadeMinima() != null && this.getEntity().getIdadeMaxima() != null && this.getEntity().getIdadeMinima() > this.getEntity().getIdadeMaxima()) {
                this.addError(OdontoMensagens.getMensagem("procedimento.idademinima.maior"), "");
            } else {
                try {
                    this.atualizaConvenioProcedimento();
                } catch (Exception e) {
                    log.error(e);
                }
                if (this.getEntity().getCodigoCfo() == null) {
                    this.getEntity().setCodigoCfo(0);
                }
                this.getEntity().setIdEmpresa(ProfissionalBO.getProfissionalLogado().getIdEmpresa());
                super.actionPersist(event);
                this.geralist();
            }
        }
    }

    private void atualizaConvenioProcedimento() throws Exception {
        List<ConvenioProcedimento> cps = convenioProcedimentoBO.listByProcedimento(this.getEntity());
        Boolean msg = false;
        if (cps != null && !cps.isEmpty()) {
            for (ConvenioProcedimento cp : cps) {
                cp.setProcedimento(this.getEntity());
                cp.setAlteradoPor(ProfissionalBO.getProfissionalLogado());
                cp.setDataUltimaAlteracao(Calendar.getInstance().getTime());
                cp.setIdEmpresa(ProfissionalBO.getProfissionalLogado().getIdEmpresa());
                if (cp.getPorcentagem() != null && cp.getPorcentagem().longValue() >= 0) {
                    cp.setValor(cp.getProcedimento().getValor().multiply(cp.getPorcentagem().divide(new BigDecimal(100), MathContext.DECIMAL32)));
                }
                convenioProcedimentoBO.persist(cp);
                msg = true;
            }
        } else {
            List<Convenio> convenios = convenioBO.listByEmpresa();
            if (convenios != null && !convenios.isEmpty()) {
                for (Convenio convenio : convenios) {
                    ConvenioProcedimento cp = new ConvenioProcedimento();
                    cp.setConvenio(convenio);
                    cp.setPorcentagem(new BigDecimal(100));
                    cp.setProcedimento(this.getEntity());
                    cp.setAlteradoPor(ProfissionalBO.getProfissionalLogado());
                    cp.setDataUltimaAlteracao(Calendar.getInstance().getTime());
                    cp.setIdEmpresa(ProfissionalBO.getProfissionalLogado().getIdEmpresa());
                    if (cp.getPorcentagem() != null && cp.getPorcentagem().longValue() >= 0) {
                        cp.setValor(cp.getProcedimento().getValor().multiply(cp.getPorcentagem().divide(new BigDecimal(100), MathContext.DECIMAL32)));
                    }
                    msg = true;
                }
            }
        }
        if (msg == true) {
            this.addInfo(OdontoMensagens.getMensagem("procedimento.convenio.atualizado"), "");
        }
    }

    public void setSelectedNode(TreeNode selectedNode) {
        if (selectedNode != null) {
            Object data = selectedNode.getData();
            if (data instanceof Procedimento) {
                this.setEntity((Procedimento) data);
            }
            this.selectedNode = selectedNode;
        }
    }

    public String getFiltroTable() {
        return filtroTable;
    }

    public void setFiltroTable(String filtroTable) {
        this.filtroTable = filtroTable;
        this.carregaTreeProcedimentos(this.filtroTable);
    }
}
