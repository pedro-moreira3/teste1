package br.com.lume.odonto.managed;

import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ActionEvent;

import org.apache.log4j.Logger;
import org.primefaces.model.DualListModel;

import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.grafico.GraficoSingleton;
import br.com.lume.graficoProfissional.GraficoProfissionalSingleton;
// import br.com.lume.odonto.bo.GraficoBO;
// import br.com.lume.odonto.bo.GraficoProfissionalBO;
// import br.com.lume.odonto.bo.ProfissionalBO;
import br.com.lume.odonto.entity.Grafico;
import br.com.lume.odonto.entity.GraficoProfissional;

@ManagedBean
@ViewScoped
public class GraficoProfissionalMB extends LumeManagedBean<GraficoProfissional> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(GraficoProfissionalMB.class);

    private DualListModel<Grafico> graficos;

    //  private GraficoBO graficoBO;

    // private GraficoProfissionalBO graficoProfissionalBO;

    public GraficoProfissionalMB() {
        super(GraficoProfissionalSingleton.getInstance().getBo());
        // this.graficoBO = new GraficoBO();
        //this.graficoProfissionalBO = new GraficoProfissionalBO();
        this.setClazz(GraficoProfissional.class);
        try {
            if (UtilsFrontEnd.getProfissionalLogado() != null) {
                this.getEntity().setProfissional(UtilsFrontEnd.getProfissionalLogado());
                this.carregaPicklist();
            }
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            this.log.error(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), e);
        }
    }

    @Override
    public void actionPersist(ActionEvent arg0) {
        try {
            this.getEntity().setIdEmpresa(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
            List<GraficoProfissional> atuais = GraficoProfissionalSingleton.getInstance().getBo().listByProfissional(this.getEntity().getProfissional(),
                    UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
            if (atuais != null && !atuais.isEmpty()) {
                this.remove(atuais);
                this.novo(atuais);
            } else {
                for (Grafico g : this.graficos.getTarget()) {
                    this.persistNovo(g);
                }
            }
            this.actionNew(arg0);
            this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
            this.log.error(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), e);
        }
    }

    private void novo(List<GraficoProfissional> atuais) throws Exception {
        // Novo
        for (Grafico g : this.graficos.getTarget()) {
            boolean insere = true;
            for (GraficoProfissional gp : atuais) {
                if (gp.getGrafico().equals(g)) {
                    insere = false;
                }
            }
            if (insere) {
                this.persistNovo(g);
            }
        }
    }

    private void remove(List<GraficoProfissional> listByProfissional) throws Exception {
        for (GraficoProfissional gp : listByProfissional) {
            // Remove
            if (!this.graficos.getTarget().contains(gp.getGrafico())) {
                GraficoProfissionalSingleton.getInstance().getBo().remove(gp);
            }
        }
    }

    private void persistNovo(Grafico g) throws Exception {
        this.getEntity().setGrafico(g);
        GraficoProfissionalSingleton.getInstance().getBo().persist(this.getEntity());
        this.getEntity().setId(0);
    }

    public void carregaPicklist() {
        try {
            List<Grafico> source = new ArrayList<>();
            List<Grafico> target = new ArrayList<>();
            source = GraficoSingleton.getInstance().getBo().listByEmpresa(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
            if (this.getEntity().getProfissional() != null) {
                List<GraficoProfissional> listByProfissional = GraficoProfissionalSingleton.getInstance().getBo().listByProfissional(this.getEntity().getProfissional(),
                        UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
                for (GraficoProfissional gp : listByProfissional) {
                    target.add(gp.getGrafico());
                }
            }
            if (source == null) {
                source = new ArrayList<>();
            }
            source.removeAll(target);
            this.graficos = new DualListModel<>(source, target);
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            this.log.error(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), e);
        }
    }

    public DualListModel<Grafico> getGraficos() {
        return this.graficos;
    }

    public void setGraficos(DualListModel<Grafico> graficos) {
        this.graficos = graficos;
    }
}
