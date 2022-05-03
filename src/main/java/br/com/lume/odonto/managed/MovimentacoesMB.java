package br.com.lume.odonto.managed;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.faces.view.ViewScoped;
import javax.inject.Named;

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
import br.com.lume.common.util.UtilsPadraoRelatorio;
import br.com.lume.common.util.UtilsPadraoRelatorio.PeriodoBusca;
import br.com.lume.faturamento.FaturaSingleton;
import br.com.lume.lancamento.LancamentoSingleton;
import br.com.lume.odonto.entity.Fatura;
import br.com.lume.odonto.entity.Lancamento;
import br.com.lume.odonto.entity.Paciente;
import br.com.lume.odonto.entity.Profissional;
import br.com.lume.paciente.PacienteSingleton;
import br.com.lume.profissional.ProfissionalSingleton;
import br.com.lume.security.entity.Empresa;

@Named
@ViewScoped
public class MovimentacoesMB extends LumeManagedBean<Lancamento> {

    private static final long serialVersionUID = 1L;
    private List<Lancamento> aPagar, aReceber;
    private BigDecimal somaTotalAPagar = BigDecimal.ZERO, somaTotalAReceber = BigDecimal.ZERO;

    //Filtros novos A Receber
    private PeriodoBusca periodoDataCreditoAReceber;
    private Date inicioDataCreditoAReceber, fimDataCreditoAReceber;
    private PeriodoBusca periodoDataPagamentoAReceber;
    private Date inicioDataPagamentoAReceber, fimDataPagamentoAReceber;
    private Paciente pacienteAReceber;

    //Filtros novos A Pagar
    private PeriodoBusca periodoDataCreditoAPagar;
    private Date inicioDataCreditoAPagar, fimDataCreditoAPagar;
    private PeriodoBusca periodoDataPagamentoAPagar;
    private Date inicioDataPagamentoAPagar, fimDataPagamentoAPagar;
    private Profissional profissionalAPagar;

    private Empresa empresaLogada = UtilsFrontEnd.getEmpresaLogada();
    private BarChartModel lineModel;
    private boolean graficoCompleto = false;
    private Thread atualizaGraficoContas = new Thread(new Runnable() {

        @Override
        public void run() {
            lineModel = new BarChartModel();
            ChartData data = new ChartData();

            List<Object> valuesAPagar = new ArrayList<>(), valuesAReceber = new ArrayList<>();
            List<Number> valuesPagos = new ArrayList<>(), valuesRecebidos = new ArrayList<>();
            List<String> labels = new ArrayList<>();

            Mes mesAtual = Mes.getMesAtual();
            int mesAtualIdx = Arrays.asList(Mes.values()).indexOf(mesAtual), anoAtual = Calendar.getInstance().get(Calendar.YEAR);
            for (int i = 0; i < 12; i++) {
                BigDecimal valor;

                valor = LancamentoSingleton.getInstance().getBo().listContasAPagarValor(empresaLogada.getConta(), mesAtual, anoAtual, ValidacaoLancamento.NAO_VALIDADO);
                valuesAPagar.add((valor == null ? 0d : valor.doubleValue()));

                valor = LancamentoSingleton.getInstance().getBo().listContasAPagarValor(empresaLogada.getConta(), mesAtual, anoAtual, ValidacaoLancamento.VALIDADO);
                valuesPagos.add((valor == null ? 0d : valor.doubleValue()));

                valor = LancamentoSingleton.getInstance().getBo().listContasAReceberValor(empresaLogada.getConta(), mesAtual, anoAtual, ValidacaoLancamento.NAO_VALIDADO);
                valuesAReceber.add((valor == null ? 0d : valor.doubleValue()));

                valor = LancamentoSingleton.getInstance().getBo().listContasAReceberValor(empresaLogada.getConta(), mesAtual, anoAtual, ValidacaoLancamento.VALIDADO);
                valuesRecebidos.add((valor == null ? 0d : valor.doubleValue()));

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
            graficoCompleto = true;
        }
    });

    public MovimentacoesMB() {
        super(LancamentoSingleton.getInstance().getBo());
        this.setClazz(Lancamento.class);
        this.periodoDataCreditoAReceber = PeriodoBusca.ULTIMOS_30_DIAS;
        this.periodoDataCreditoAPagar = PeriodoBusca.ULTIMOS_30_DIAS;
        this.periodoDataPagamentoAReceber = PeriodoBusca.ULTIMOS_30_DIAS;
        this.periodoDataPagamentoAPagar = PeriodoBusca.ULTIMOS_30_DIAS;
        actionTrocaDatasCreditoAPagar();
        actionTrocaDatasPagamentoAPagar();
        actionTrocaDatasCreditoAReceber();
        actionTrocaDatasPagamentoAReceber();
        this.carregaListaAReceber();

        this.atualizaGraficoContas.start();
    }

    public void onTabChange(TabChangeEvent event) {
        if ("Contas a Receber".equals(event.getTab().getTitle())) {
            carregaListaAReceber();
        } else if ("Contas a Pagar".equals(event.getTab().getTitle())) {
            carregaListaAPagar();
        }
    }

    public String getFaturaInfo(Fatura f) {
        return FaturaSingleton.getInstance().getFaturaInfo(f);
    }

    public String getFaturaInfoNome(Fatura f) {        
        return FaturaSingleton.getInstance().getFaturaInfoNome(f);
    }
    
    public List<Paciente> sugestoesPacientes(String query) {
        try {
            return PacienteSingleton.getInstance().listSugestoesComplete(query, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            e.printStackTrace();
        }
        return null;
    }

    public List<Profissional> geraSugestoesProfissional(String query) {
        List<Profissional> sugestoes = new ArrayList<>();
        List<Profissional> profissionais = new ArrayList<>();
        try {
            profissionais = ProfissionalSingleton.getInstance().getBo().listDentistasByEmpresa(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
            for (Profissional p : profissionais) {
                if (Normalizer.normalize(p.getDadosBasico().getNome().toLowerCase(), Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "").toLowerCase().contains(
                        Normalizer.normalize(query, Normalizer.Form.NFD).toLowerCase())) {
                    sugestoes.add(p);
                }
            }
            Collections.sort(sugestoes);
        } catch (Exception e) {
            LogIntelidenteSingleton.getInstance().makeLog(e);
            this.addError("Erro", Mensagens.ERRO_AO_BUSCAR_REGISTROS, true);
        }
        return sugestoes;
    }

    public void actionTrocaDatasCreditoAPagar() {
        try {
            setInicioDataCreditoAPagar(UtilsPadraoRelatorio.getDataInicio(getPeriodoDataCreditoAPagar()));
            setFimDataCreditoAPagar(UtilsPadraoRelatorio.getDataFim(getPeriodoDataCreditoAPagar()));
        } catch (Exception e) {
            LogIntelidenteSingleton.getInstance().makeLog(e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
        }
    }

    public void actionTrocaDatasPagamentoAPagar() {
        try {
            setInicioDataPagamentoAPagar(UtilsPadraoRelatorio.getDataInicio(getPeriodoDataPagamentoAPagar()));
            setFimDataPagamentoAPagar(UtilsPadraoRelatorio.getDataFim(getPeriodoDataPagamentoAPagar()));
        } catch (Exception e) {
            LogIntelidenteSingleton.getInstance().makeLog(e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
        }
    }

    public void carregaListaAPagar() {
        try {
            this.somaTotalAPagar = BigDecimal.ZERO;
            this.aPagar = LancamentoSingleton.getInstance().getBo().listContasAPagar(UtilsFrontEnd.getEmpresaLogada().getConta(), inicioDataCreditoAPagar, fimDataCreditoAPagar,
                    inicioDataPagamentoAPagar, fimDataPagamentoAPagar, profissionalAPagar);
            for (Lancamento lAPagar : this.aPagar)
                this.somaTotalAPagar = this.somaTotalAPagar.add(lAPagar.getValor());
        } catch (Exception e) {
            LogIntelidenteSingleton.getInstance().makeLog(e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
        }
    }

    public void actionTrocaDatasCreditoAReceber() {
        try {
            setInicioDataCreditoAReceber(UtilsPadraoRelatorio.getDataInicio(getPeriodoDataCreditoAReceber()));
            setFimDataCreditoAReceber(UtilsPadraoRelatorio.getDataFim(getPeriodoDataCreditoAReceber()));
        } catch (Exception e) {
            LogIntelidenteSingleton.getInstance().makeLog(e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
        }
    }

    public void actionTrocaDatasPagamentoAReceber() {
        try {
            setInicioDataPagamentoAReceber(UtilsPadraoRelatorio.getDataInicio(getPeriodoDataPagamentoAReceber()));
            setFimDataPagamentoAReceber(UtilsPadraoRelatorio.getDataFim(getPeriodoDataPagamentoAReceber()));
        } catch (Exception e) {
            LogIntelidenteSingleton.getInstance().makeLog(e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
        }
    }

    public void carregaListaAReceber() {
        try {
            this.somaTotalAReceber = BigDecimal.ZERO;
            this.aReceber = LancamentoSingleton.getInstance().getBo().listContasAReceber(UtilsFrontEnd.getEmpresaLogada().getConta(), inicioDataCreditoAReceber, fimDataCreditoAReceber,
                    inicioDataPagamentoAReceber, fimDataPagamentoAReceber, pacienteAReceber);
            for (Lancamento lAReceber : this.aReceber)
                this.somaTotalAReceber = this.somaTotalAReceber.add(lAReceber.getValor());
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

    public List<Mes> getMeses() {
        return Arrays.asList(Mes.values());
    }

    public BarChartModel getLineModel() {
        return lineModel;
    }

    public void setLineModel(BarChartModel lineModel) {
        this.lineModel = lineModel;
    }

    public boolean isGraficoCompleto() {
        return graficoCompleto;
    }

    public void setGraficoCompleto(boolean graficoCompleto) {
        this.graficoCompleto = graficoCompleto;
    }

    public BigDecimal getSomaTotalAPagar() {
        return somaTotalAPagar;
    }

    public void setSomaTotalAPagar(BigDecimal somaTotalAPagar) {
        this.somaTotalAPagar = somaTotalAPagar;
    }

    public BigDecimal getSomaTotalAReceber() {
        return somaTotalAReceber;
    }

    public void setSomaTotalAReceber(BigDecimal somaTotalAReceber) {
        this.somaTotalAReceber = somaTotalAReceber;
    }

    public PeriodoBusca getPeriodoDataCreditoAReceber() {
        return periodoDataCreditoAReceber;
    }

    public void setPeriodoDataCreditoAReceber(PeriodoBusca periodoDataCreditoAReceber) {
        this.periodoDataCreditoAReceber = periodoDataCreditoAReceber;
    }

    public Date getInicioDataCreditoAReceber() {
        return inicioDataCreditoAReceber;
    }

    public void setInicioDataCreditoAReceber(Date inicioDataCreditoAReceber) {
        this.inicioDataCreditoAReceber = inicioDataCreditoAReceber;
    }

    public Date getFimDataCreditoAReceber() {
        return fimDataCreditoAReceber;
    }

    public void setFimDataCreditoAReceber(Date fimDataCreditoAReceber) {
        this.fimDataCreditoAReceber = fimDataCreditoAReceber;
    }

    public PeriodoBusca getPeriodoDataPagamentoAReceber() {
        return periodoDataPagamentoAReceber;
    }

    public void setPeriodoDataPagamentoAReceber(PeriodoBusca periodoDataPagamentoAReceber) {
        this.periodoDataPagamentoAReceber = periodoDataPagamentoAReceber;
    }

    public Date getInicioDataPagamentoAReceber() {
        return inicioDataPagamentoAReceber;
    }

    public void setInicioDataPagamentoAReceber(Date inicioDataPagamentoAReceber) {
        this.inicioDataPagamentoAReceber = inicioDataPagamentoAReceber;
    }

    public Date getFimDataPagamentoAReceber() {
        return fimDataPagamentoAReceber;
    }

    public void setFimDataPagamentoAReceber(Date fimDataPagamentoAReceber) {
        this.fimDataPagamentoAReceber = fimDataPagamentoAReceber;
    }

    public Paciente getPacienteAReceber() {
        return pacienteAReceber;
    }

    public void setPacienteAReceber(Paciente pacienteAReceber) {
        this.pacienteAReceber = pacienteAReceber;
    }

    public PeriodoBusca getPeriodoDataCreditoAPagar() {
        return periodoDataCreditoAPagar;
    }

    public void setPeriodoDataCreditoAPagar(PeriodoBusca periodoDataCreditoAPagar) {
        this.periodoDataCreditoAPagar = periodoDataCreditoAPagar;
    }

    public Date getInicioDataCreditoAPagar() {
        return inicioDataCreditoAPagar;
    }

    public void setInicioDataCreditoAPagar(Date inicioDataCreditoAPagar) {
        this.inicioDataCreditoAPagar = inicioDataCreditoAPagar;
    }

    public Date getFimDataCreditoAPagar() {
        return fimDataCreditoAPagar;
    }

    public void setFimDataCreditoAPagar(Date fimDataCreditoAPagar) {
        this.fimDataCreditoAPagar = fimDataCreditoAPagar;
    }

    public PeriodoBusca getPeriodoDataPagamentoAPagar() {
        return periodoDataPagamentoAPagar;
    }

    public void setPeriodoDataPagamentoAPagar(PeriodoBusca periodoDataPagamentoAPagar) {
        this.periodoDataPagamentoAPagar = periodoDataPagamentoAPagar;
    }

    public Date getInicioDataPagamentoAPagar() {
        return inicioDataPagamentoAPagar;
    }

    public void setInicioDataPagamentoAPagar(Date inicioDataPagamentoAPagar) {
        this.inicioDataPagamentoAPagar = inicioDataPagamentoAPagar;
    }

    public Date getFimDataPagamentoAPagar() {
        return fimDataPagamentoAPagar;
    }

    public void setFimDataPagamentoAPagar(Date fimDataPagamentoAPagar) {
        this.fimDataPagamentoAPagar = fimDataPagamentoAPagar;
    }

    public Profissional getProfissionalAPagar() {
        return profissionalAPagar;
    }

    public void setProfissionalAPagar(Profissional profissionalAPagar) {
        this.profissionalAPagar = profissionalAPagar;
    }

}
