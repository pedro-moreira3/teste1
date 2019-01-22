package br.com.lume.odonto.managed;

import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.apache.log4j.Logger;
import org.primefaces.model.chart.MeterGaugeChartModel;

import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.odonto.bo.GraficoBO;
import br.com.lume.odonto.bo.GraficoProfissionalBO;
import br.com.lume.odonto.bo.ProfissionalBO;
import br.com.lume.odonto.entity.Grafico;
import br.com.lume.odonto.entity.GraficoProfissional;

@ManagedBean
@ViewScoped
public class CockpitMB extends LumeManagedBean<Grafico> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(CockpitMB.class);

    private List<GraficoProfissional> graficos1 = new ArrayList<>(), graficos2 = new ArrayList<>(), graficos3 = new ArrayList<>();

    private GraficoProfissionalBO graficoProfissionalBO;

    private GraficoBO graficoBO;

    public CockpitMB() {
        super(new GraficoBO());
        this.graficoProfissionalBO = new GraficoProfissionalBO();
        this.graficoBO = new GraficoBO();
        this.setClazz(Grafico.class);
        try {
            this.createMeterGaugeModel();
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            this.log.error(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), e);
        }
    }

    private void createMeterGaugeModel() throws Exception {
        List<GraficoProfissional> graficos = this.graficoProfissionalBO.listByProfissional(ProfissionalBO.getProfissionalLogado());
        for (GraficoProfissional gp : graficos) {
            int value = this.graficoBO.findGenerico(gp.getGrafico());
            List<Number> intervals = new ArrayList<>();
            intervals.add(gp.getGrafico().getMinimo());
            intervals.add(gp.getGrafico().getMaximo());
            intervals.add(gp.getGrafico().getFim());
            List<Number> ticks = new ArrayList<>();
            ticks.add(gp.getGrafico().getInicio());
            ticks.add(gp.getGrafico().getFim() / 6);
            ticks.add((gp.getGrafico().getFim() / 6) * 2);
            ticks.add((gp.getGrafico().getFim() / 6) * 3);
            ticks.add((gp.getGrafico().getFim() / 6) * 4);
            ticks.add((gp.getGrafico().getFim() / 6) * 5);
            ticks.add(gp.getGrafico().getFim());
            MeterGaugeChartModel meterGaugeChartModel = new MeterGaugeChartModel();
            meterGaugeChartModel.setValue(value);
            meterGaugeChartModel.setIntervals(intervals);
            meterGaugeChartModel.setTicks(ticks);

            meterGaugeChartModel.setTitle(gp.getGrafico().getDescricao());
            meterGaugeChartModel.setIntervalOuterRadius(105);
            meterGaugeChartModel.setSeriesColors("cc6666, FF8787, E7E658,66cc66");

            gp.setChartModel(meterGaugeChartModel);
            if (this.graficos1.size() == this.graficos2.size() && this.graficos1.size() == this.graficos3.size()) {
                this.graficos1.add(gp);
            } else if (this.graficos1.size() > this.graficos2.size() && this.graficos2.size() == this.graficos3.size()) {
                this.graficos2.add(gp);
            } else if (this.graficos1.size() == this.graficos2.size() && this.graficos2.size() > this.graficos3.size()) {
                this.graficos3.add(gp);
            }
        }
    }

    public List<GraficoProfissional> getGraficos1() {
        return this.graficos1;
    }

    public void setGraficos1(List<GraficoProfissional> graficos1) {
        this.graficos1 = graficos1;
    }

    public List<GraficoProfissional> getGraficos2() {
        return this.graficos2;
    }

    public void setGraficos2(List<GraficoProfissional> graficos2) {
        this.graficos2 = graficos2;
    }

    public List<GraficoProfissional> getGraficos3() {
        return this.graficos3;
    }

    public void setGraficos3(List<GraficoProfissional> graficos3) {
        this.graficos3 = graficos3;
    }
}
