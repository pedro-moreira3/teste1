package br.com.lume.odonto.managed;

import java.util.HashSet;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ActionEvent;

import org.apache.log4j.Logger;

import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.odonto.bo.KitBO;
import br.com.lume.odonto.bo.ProcedimentoBO;
import br.com.lume.odonto.bo.ProcedimentoKitBO;
import br.com.lume.odonto.bo.ProfissionalBO;
import br.com.lume.odonto.entity.Kit;
import br.com.lume.odonto.entity.Procedimento;
import br.com.lume.odonto.entity.ProcedimentoKit;

@ManagedBean
@ViewScoped
public class ProcedimentoKitMB extends LumeManagedBean<ProcedimentoKit> {

    private static final long serialVersionUID = 1L;

    private List<Kit> kits;

    private HashSet<String> procedimentosFiltro;

    private List<Procedimento> procedimentos;

    private Logger log = Logger.getLogger(ProcedimentoKitMB.class);

    private KitBO kitBO;

    private ProcedimentoBO procedimentoBO;

    private Procedimento procedimentoSelecionado;

    private boolean procedimentoSemKit;

    public ProcedimentoKitMB() {
        super(new ProcedimentoKitBO());
        this.setClazz(ProcedimentoKit.class);
        kitBO = new KitBO();
        procedimentoBO = new ProcedimentoBO();
        this.carregarListas();
    }

    private void carregarListas() {
        try {
            kits = kitBO.listByEmpresa();
            this.carregarProcedimentos();
            this.setEntityList(((ProcedimentoKitBO) this.getbO()).listByEmpresa());
            this.carregarProcedimentosFiltro();
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            log.error(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), e);
        }
    }

    public void carregarProcedimentos() {
        try {
            if (procedimentoSemKit) {
                procedimentos = procedimentoBO.listProcedimentosSemKitVinculados();
            } else {
                procedimentos = procedimentoBO.listByEmpresa();
            }
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            log.error(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), e);
        }
    }

    public void carregarProcedimentosFiltro() {
        procedimentosFiltro = new HashSet<>();
        List<ProcedimentoKit> procedimentosKit = this.getEntityList();
        if (procedimentosKit != null && !procedimentosKit.isEmpty()) {
            for (ProcedimentoKit procedimentoKit : procedimentosKit) {
                procedimentosFiltro.add(procedimentoKit.getProcedimento().getDescricao().trim());
            }
        }
    }

    @Override
    public void actionPersist(ActionEvent event) {
        try {
            ProcedimentoKit pk = ((ProcedimentoKitBO) this.getbO()).findByProcedimentoAndKit(this.getEntity().getProcedimento(), this.getEntity().getKit());
            if (pk != null) {
                pk.setQuantidade(getEntity().getQuantidade());
                setEntity(pk);
                ((ProcedimentoKitBO) this.getbO()).persist(this.getEntity());
            } else {
                procedimentoSelecionado = this.getEntity().getProcedimento();
                this.getEntity().setIdEmpresa(ProfissionalBO.getProfissionalLogado().getIdEmpresa());
                ((ProcedimentoKitBO) this.getbO()).persist(this.getEntity());
                this.actionNew(event);
                if (procedimentoSelecionado != null) {
                    this.getEntity().setProcedimento(procedimentoSelecionado);
                }
            }
            this.setEntity(new ProcedimentoKit());
            this.carregarListas();
            this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void actionRemove(ActionEvent event) {
        super.actionRemove(event);
        this.carregarListas();
    }

    @Override
    public void actionNew(ActionEvent event) {
        // super.actionNew(event);
        this.setEntity(new ProcedimentoKit());
    }

    public List<Kit> getKits() {
        return kits;
    }

    public void setKits(List<Kit> kits) {
        this.kits = kits;
    }

    public HashSet<String> getProcedimentosFiltro() {
        return procedimentosFiltro;
    }

    public void setProcedimentosFiltro(HashSet<String> procedimentosFiltro) {
        this.procedimentosFiltro = procedimentosFiltro;
    }

    public List<Procedimento> getProcedimentos() {
        return procedimentos;
    }

    public void setProcedimentos(List<Procedimento> procedimentos) {
        this.procedimentos = procedimentos;
    }

    public boolean isProcedimentoSemKit() {
        return procedimentoSemKit;
    }

    public void setProcedimentoSemKit(boolean procedimentoSemKit) {
        this.procedimentoSemKit = procedimentoSemKit;
    }
}
