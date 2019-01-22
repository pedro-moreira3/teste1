package br.com.lume.odonto.managed;

import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ActionEvent;

import org.apache.log4j.Logger;
import org.primefaces.event.SelectEvent;

import br.com.lume.common.exception.business.BusinessException;
import br.com.lume.common.exception.techinical.TechnicalException;
import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.odonto.bo.DominioBO;
import br.com.lume.odonto.bo.FornecedorBO;
import br.com.lume.odonto.bo.LancamentoContabilBO;
import br.com.lume.odonto.bo.MaterialBO;
import br.com.lume.odonto.bo.MotivoBO;
import br.com.lume.odonto.bo.NotaFiscalBO;
import br.com.lume.odonto.bo.ProfissionalBO;
import br.com.lume.odonto.entity.Fornecedor;
import br.com.lume.odonto.entity.LancamentoContabil;
import br.com.lume.odonto.entity.Material;
import br.com.lume.odonto.entity.Motivo;
import br.com.lume.odonto.entity.NotaFiscal;
import br.com.lume.odonto.util.OdontoMensagens;

@ManagedBean
@ViewScoped
public class NotaFiscalMB extends LumeManagedBean<NotaFiscal> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(NotaFiscalMB.class);

    private Material material;

    private List<Material> materiais;

    private List<NotaFiscal> notasFiscais;

    private List<Fornecedor> fornecedores;

    private Material[] slctMateriais;

    private FornecedorBO fornecedorBO;

    private MaterialBO materialBO;

    private LancamentoContabilBO lancamentoContabilBO;

    private MotivoBO motivoBO;

    private DominioBO dominioBO;

    public NotaFiscalMB() {
        super(new NotaFiscalBO());
        this.fornecedorBO = new FornecedorBO();
        this.materialBO = new MaterialBO();
        this.lancamentoContabilBO = new LancamentoContabilBO();
        this.motivoBO = new MotivoBO();
        this.setClazz(NotaFiscal.class);
        this.dominioBO = new DominioBO();
        try {
            this.setFornecedores(this.fornecedorBO.listAll());
        } catch (Exception e) {
            this.log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
            this.addError(Mensagens.ERRO_AO_BUSCAR_REGISTROS, "");
        }
    }

    @Override
    public void actionPersist(ActionEvent event) {
        try {
            this.getEntity().setIdEmpresa(ProfissionalBO.getProfissionalLogado().getIdEmpresa());
            this.getbO().persist(this.getEntity());
            for (Material material : this.getEntity().getMateriais()) {
                this.materialBO.persist(material);
            }
            this.geraLancamentoContabil();
            this.actionNew(event);
            this.setNotasFiscais(null);
            this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");
        } catch (BusinessException e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), e.toString());
            e.printStackTrace();
        } catch (TechnicalException e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), e.toString());
            e.printStackTrace();
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), e.toString());
            e.printStackTrace();
        }
    }

    private void geraLancamentoContabil() throws Exception, BusinessException, TechnicalException {
        LancamentoContabil lc = null;
        if (this.getEntity().getId() != null) {
            lc = this.lancamentoContabilBO.findByNotafiscal(this.getEntity());
        }
        if (lc == null) {
            lc = new LancamentoContabil();
        }
        Motivo motivo = this.motivoBO.findBySigla(Motivo.COMPRA_MATERIAIS);
        lc.setIdEmpresa(ProfissionalBO.getProfissionalLogado().getIdEmpresa());
        lc.setTipo(motivo.getTipo());
        lc.setDadosBasico(this.getEntity().getFornecedor().getDadosBasico());
        lc.setMotivo(motivo);
        lc.setValor(this.getEntity().getValorTotal());
        lc.setNotaFiscal(this.getEntity());
        lc.setData(this.getEntity().getDataEntrada());
        this.lancamentoContabilBO.persist(lc);
    }

    @Override
    public void actionNew(ActionEvent event) {
        super.actionNew(event);
    }

    public Material getMaterial() {
        return this.material;
    }

    public void setMaterial(Material material) {
        this.material = material;
    }

    public List<Material> getMateriais() {
        if (this.materiais == null) {
            try {
                this.setMateriais(this.materialBO.listAtivosByEmpresaWithoutNotaFiscal());
                if (this.getEntity().getMateriais() != null && this.getEntity().getMateriais().size() > 0) {
                    this.getMateriais().removeAll(this.getEntity().getMateriais());
                }
            } catch (Exception e) {
                this.log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
                this.addError(Mensagens.ERRO_AO_BUSCAR_REGISTROS, "");
            }
        }
        return this.materiais;
    }

    public void setMateriais(List<Material> materiais) {
        this.materiais = materiais;
    }

    public List<NotaFiscal> getNotasFiscais() {
        if (this.notasFiscais == null) {
            try {
                this.setNotasFiscais(((NotaFiscalBO) this.getbO()).listByEmpresa());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return this.notasFiscais;
    }

    public void setNotasFiscais(List<NotaFiscal> notasFiscais) {
        this.notasFiscais = notasFiscais;
    }

    public List<Fornecedor> getFornecedores() {
        return this.fornecedores;
    }

    public void setFornecedores(List<Fornecedor> fornecedores) {
        this.fornecedores = fornecedores;
    }

    public Material[] getSlctMateriais() {
        return this.slctMateriais;
    }

    public void setSlctMateriais(Material[] slctMateriais) {
        this.slctMateriais = slctMateriais;
    }

    public void actionOpenDialogMateriais(ActionEvent event) {
        this.setSlctMateriais(null);
        this.setMateriais(null);
    }

    @Override
    public void actionRemove(ActionEvent event) {
        //getEntity().setMateriais(null);
        try {
            for (Material m : this.getEntity().getMateriais()) {
                m.setNotaFiscal(null);
            }
            new MaterialBO().mergeBatch(this.getEntity().getMateriais());
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.actionRemove(event);
    }

    public void actionAdicionarMateriais(ActionEvent event) {
        if (this.getSlctMateriais() != null && this.getSlctMateriais().length > 0) {
            if (this.getEntity().getMateriais() == null) {
                this.getEntity().setMateriais(new ArrayList<Material>());
            }
            for (Material material : this.getSlctMateriais()) {
                material.setNotaFiscal(this.getEntity());
                if (!this.getEntity().getMateriais().contains(material)) {
                    this.getEntity().getMateriais().add(material);
                }
            }
        } else {
            this.addWarn(OdontoMensagens.getMensagem("notafiscal.materialslct.vazio"), null);
        }
    }

    public void actionRemoveMaterial(ActionEvent event) {
        if (this.getMaterial() != null && this.getEntity().getMateriais().contains(this.getMaterial())) {
            this.getEntity().getMateriais().get(this.getEntity().getMateriais().indexOf(this.getMaterial())).setNotaFiscal(null);
        }
        this.getEntity().getMateriais().remove(this.getMaterial());
        this.getMaterial().setNotaFiscal(null);
    }

    public void onRowSelect(SelectEvent event) {
        try {
            this.getbO().refresh(((NotaFiscal) event.getObject()));
        } catch (Exception e) {
            this.log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
            this.addError(Mensagens.ERRO_AO_BUSCAR_REGISTROS, "");
        }
    }
}
