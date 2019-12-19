package br.com.lume.odonto.managed;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.primefaces.event.TabChangeEvent;
import org.primefaces.model.charts.ChartData;
import org.primefaces.model.charts.bar.BarChartDataSet;
import org.primefaces.model.charts.bar.BarChartModel;
import org.primefaces.model.charts.bar.BarChartOptions;
import org.primefaces.model.charts.line.LineChartDataSet;
import org.primefaces.model.charts.optionconfig.title.Title;

import br.com.lume.common.log.LogIntelidenteSingleton;
import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.Utils.Mes;
import br.com.lume.common.util.Utils.ValidacaoLancamento;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.lancamento.LancamentoSingleton;
import br.com.lume.odonto.entity.Lancamento;
import br.com.lume.security.entity.Empresa;

@ManagedBean
@ViewScoped
public class MovimentacoesMB extends LumeManagedBean<Lancamento> {

    private static final long serialVersionUID = 1L;
    private List<Lancamento> aPagar, aReceber;
    private Mes mesAPagar, mesAReceber;
    private boolean consideraLancamentosValidadosAPagar, consideraLancamentosValidadosAReceber;

    private Empresa empresaLogada = UtilsFrontEnd.getEmpresaLogada();
    private BarChartModel lineModel;
    private Thread atualizaGraficoContas = new Thread(new Runnable() {

        @Override
        public void run() {
            lineModel = new BarChartModel();
            ChartData data = new ChartData();

            List<Number> valuesAPagar = new ArrayList<>(), valuesAReceber = new ArrayList<>(), valuesPagos = new ArrayList<>(), valuesRecebidos = new ArrayList<>();
            List<String> labels = new ArrayList<>();

            Mes mesAtual = Mes.getMesAtual();
            int mesAtualIdx = Arrays.asList(Mes.values()).indexOf(mesAtual), anoAtual = Calendar.getInstance().get(Calendar.YEAR);
            for (int i = 0; i < 12; i++) {
                List<Lancamento> lancamentos;
                Double valor;

                lancamentos = LancamentoSingleton.getInstance().getBo().listContasAPagar(empresaLogada.getConta(), mesAtual, anoAtual, ValidacaoLancamento.NAO_VALIDADO);
                valor = LancamentoSingleton.getInstance().sumLancamentos(lancamentos);
                valuesAPagar.add(valor);

                lancamentos = LancamentoSingleton.getInstance().getBo().listContasAPagar(empresaLogada.getConta(), mesAtual, anoAtual, ValidacaoLancamento.VALIDADO);
                valor = LancamentoSingleton.getInstance().sumLancamentos(lancamentos);
                valuesPagos.add(valor);

                lancamentos = LancamentoSingleton.getInstance().getBo().listContasAReceber(empresaLogada.getConta(), mesAtual, anoAtual, ValidacaoLancamento.NAO_VALIDADO);
                valor = LancamentoSingleton.getInstance().sumLancamentos(lancamentos);
                valuesAReceber.add(valor);

                lancamentos = LancamentoSingleton.getInstance().getBo().listContasAReceber(empresaLogada.getConta(), mesAtual, anoAtual, ValidacaoLancamento.NAO_VALIDADO);
                valor = LancamentoSingleton.getInstance().sumLancamentos(lancamentos);
                valuesRecebidos.add(valor);

                labels.add(mesAtual.getRotulo() + "/" + String.valueOf(anoAtual));
                mesAtualIdx = mesAtualIdx - 1;
                if (mesAtualIdx < 0) {
                    mesAtualIdx = 11;
                    anoAtual = anoAtual - 1;
                }
                mesAtual = Mes.values()[mesAtualIdx];
            }
            Collections.reverse(valuesAPagar);
            Collections.reverse(valuesAReceber);
            Collections.reverse(labels);

            LineChartDataSet dataSetAPagar = new LineChartDataSet();
            dataSetAPagar.setData(valuesAPagar);
            dataSetAPagar.setFill(false);
            dataSetAPagar.setLabel("Lançamentos a Pagar");
            dataSetAPagar.setBorderColor("rgb(220, 53, 69)");
            dataSetAPagar.setLineTension(0.1);
            data.addChartDataSet(dataSetAPagar);

            BarChartDataSet dataSetPagos = new BarChartDataSet();
            dataSetPagos.setData(valuesPagos);
            dataSetPagos.setLabel("Lançamentos Pagos");
            dataSetPagos.setBorderColor("rgb(220, 53, 69)");
            dataSetPagos.setBackgroundColor("rgba(220, 53, 69, 0.2)");
            data.addChartDataSet(dataSetPagos);

            LineChartDataSet dataSetAReceber = new LineChartDataSet();
            dataSetAReceber.setData(valuesAReceber);
            dataSetAReceber.setFill(false);
            dataSetAReceber.setLabel("Lançamentos a Receber");
            dataSetAReceber.setBorderColor("rgb(0, 123, 255)");
            dataSetAReceber.setLineTension(0.1);
            data.addChartDataSet(dataSetAReceber);

            BarChartDataSet dataRecebidos = new BarChartDataSet();
            dataRecebidos.setData(valuesRecebidos);
            dataRecebidos.setLabel("Lançamentos Recebidos");
            dataRecebidos.setBorderColor("rgb(0, 123, 255)");
            dataRecebidos.setBackgroundColor("rgba(0, 123, 255, 0.2)");
            data.addChartDataSet(dataRecebidos);

            BarChartOptions options = new BarChartOptions();
            Title title = new Title();
            title.setDisplay(true);
            title.setText("Movimentações Previstas/Efetivadas (R$)");
            options.setTitle(title);

            data.setLabels(labels);
            lineModel.setOptions(options);
            lineModel.setData(data);
            //attMovimentacoesChart()
        }
    });

    public MovimentacoesMB() {
        super(LancamentoSingleton.getInstance().getBo());
        this.setClazz(Lancamento.class);
        this.mesAPagar = this.mesAReceber = getMeses().get(Calendar.getInstance().get(Calendar.MONTH));
        this.consideraLancamentosValidadosAPagar = this.consideraLancamentosValidadosAReceber = false;
        this.carregaListaAReceber();
    }

    public void onTabChange(TabChangeEvent event) {
        if ("Contas a Receber".equals(event.getTab().getTitle())) {
            carregaListaAReceber();
        } else if ("Contas a Pagar".equals(event.getTab().getTitle())) {
            carregaListaAPagar();
        }
    }

    public void carregaListaAPagar() {
        try {
            this.aPagar = LancamentoSingleton.getInstance().getBo().listContasAPagar(UtilsFrontEnd.getEmpresaLogada().getConta(), this.mesAPagar,
                    (this.consideraLancamentosValidadosAPagar ? ValidacaoLancamento.TODOS : ValidacaoLancamento.NAO_VALIDADO));
        } catch (Exception e) {
            LogIntelidenteSingleton.getInstance().makeLog(e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
        }
    }

    public void carregaListaAReceber() {
        try {
            this.aReceber = LancamentoSingleton.getInstance().getBo().listContasAReceber(UtilsFrontEnd.getEmpresaLogada().getConta(), this.mesAReceber,
                    (this.consideraLancamentosValidadosAReceber ? ValidacaoLancamento.TODOS : ValidacaoLancamento.NAO_VALIDADO));
        } catch (Exception e) {
            LogIntelidenteSingleton.getInstance().makeLog(e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
        }
    }

    public String getOrigemFromLancamento(Lancamento l) {
        if (l == null || l.getFatura() == null || l.getFatura().getId() == null)
            return "";
        return "Fatura " + l.getFatura().getId();
    }

    public String getNewStatusLancamento(Lancamento l) {
        if (Arrays.asList(new String[] { Lancamento.PAGO, Lancamento.CANCELADO, Lancamento.ATRASADO }).indexOf(l.getStatus()) >= 0)
            return l.getStatus();
        return Lancamento.PENDENTE;
    }

    public List<Lancamento> getaPagar() {
        return aPagar;
    }

    public void setaPagar(List<Lancamento> aPagar) {
        this.aPagar = aPagar;
    }

    public List<Lancamento> getaReceber() {
        return aReceber;
    }

    public void setaReceber(List<Lancamento> aReceber) {
        this.aReceber = aReceber;
    }

    public Mes getMesAPagar() {
        return mesAPagar;
    }

    public void setMesAPagar(Mes mesAPagar) {
        this.mesAPagar = mesAPagar;
    }

    public Mes getMesAReceber() {
        return mesAReceber;
    }

    public void setMesAReceber(Mes mesAReceber) {
        this.mesAReceber = mesAReceber;
    }

    public boolean isConsideraLancamentosValidadosAPagar() {
        return consideraLancamentosValidadosAPagar;
    }

    public void setConsideraLancamentosValidadosAPagar(boolean consideraLancamentosValidadosAPagar) {
        this.consideraLancamentosValidadosAPagar = consideraLancamentosValidadosAPagar;
    }

    public boolean isConsideraLancamentosValidadosAReceber() {
        return consideraLancamentosValidadosAReceber;
    }

    public void setConsideraLancamentosValidadosAReceber(boolean consideraLancamentosValidadosAReceber) {
        this.consideraLancamentosValidadosAReceber = consideraLancamentosValidadosAReceber;
    }

    public List<Mes> getMeses() {
        return Arrays.asList(Mes.values());
    }

    public BarChartModel getLineModel() {
        if (lineModel == null && !this.atualizaGraficoContas.isAlive())
            this.atualizaGraficoContas.start();
        return lineModel;
    }

    public void setLineModel(BarChartModel lineModel) {
        this.lineModel = lineModel;
    }

}
