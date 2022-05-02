package br.com.lume.odonto.managed;

import java.util.Collections;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.apache.log4j.Logger;
import org.primefaces.model.chart.PieChartModel;

import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.odonto.entity.Profissional;
import br.com.lume.odonto.entity.RelatorioAvaliacao;
import br.com.lume.relatorioAvaliacao.RelatorioAvaliacaoSingleton;

@ManagedBean
@ViewScoped
public class RelatorioAvaliacaoMB extends LumeManagedBean<RelatorioAvaliacao> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(RelatorioAvaliacaoMB.class);

    private List<RelatorioAvaliacao> avaliacoes;

    private PieChartModel pieModel;

    private String titulo;

    public RelatorioAvaliacaoMB() {
        super(RelatorioAvaliacaoSingleton.getInstance().getBo());
        this.setClazz(RelatorioAvaliacao.class);
        try {
            if (UtilsFrontEnd.getProfissionalLogado() != null) {
                this.avaliacoes = RelatorioAvaliacaoSingleton.getInstance().getBo().listAgrupadoPorProfissional(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
                this.graficoGeral();
            }
        } catch (Exception e) {
            this.log.error(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
        }
    }

    public void graficoGeral() {
        try {
            List<RelatorioAvaliacao> listAgrupadoPorTotal = RelatorioAvaliacaoSingleton.getInstance().getBo().listAgrupadoPorTotal(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
            this.titulo = "Avaliação de desempenho dos Profissionais da clínica";
            this.carregaDadosGrafico(listAgrupadoPorTotal);
        } catch (Exception e) {
            this.log.error(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
        }
    }

    public void graficoProfissional() {
        try {
            if (this.getEntity() != null && this.getEntity().getProfissional() != null) {
                Profissional profissional = this.getEntity().getProfissional();
                List<RelatorioAvaliacao> listAgrupadoPorTotal = RelatorioAvaliacaoSingleton.getInstance().getBo().listAgrupadoPorTotalByProfissional(profissional,
                        UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
                this.titulo = "Avaliação de desempenho do profissional " + profissional.getDadosBasico().getNome();
                this.carregaDadosGrafico(listAgrupadoPorTotal);
            }
        } catch (Exception e) {
            this.log.error(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
        }
    }

    private void carregaDadosGrafico(List<RelatorioAvaliacao> listAgrupadoPorTotal) {
        this.pieModel = new PieChartModel();
        this.pieModel.setTitle(this.titulo);
        this.pieModel.setLegendPosition("e");
        this.pieModel.setFill(false);
        this.pieModel.setShowDataLabels(true);
        this.pieModel.setSliceMargin(5);
        this.pieModel.setDiameter(300);
        for (RelatorioAvaliacao relatorioAvaliacao : listAgrupadoPorTotal) {
            this.pieModel.set("" + relatorioAvaliacao.getAvaliacao() + " estrela" + (relatorioAvaliacao.getAvaliacao() > 1 ? "s" : ""), relatorioAvaliacao.getTotal());
        }
    }

    public List<RelatorioAvaliacao> getAvaliacoes() {
        if (this.avaliacoes != null) {
            Collections.sort(this.avaliacoes);
        }
        return this.avaliacoes;
    }

    public void setAvaliacoes(List<RelatorioAvaliacao> avaliacoes) {
        this.avaliacoes = avaliacoes;
    }

    public PieChartModel getPieModel() {
        return this.pieModel;
    }

    public void setPieModel(PieChartModel pieModel) {
        this.pieModel = pieModel;
    }

    public String getTitulo() {
        return this.titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }
}
