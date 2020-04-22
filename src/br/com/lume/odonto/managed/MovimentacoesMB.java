package br.com.lume.odonto.managed;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
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
import br.com.lume.common.util.UtilsPadraoRelatorio;
import br.com.lume.common.util.UtilsPadraoRelatorio.PeriodoBusca;
import br.com.lume.faturamento.FaturaSingleton;
import br.com.lume.lancamento.LancamentoSingleton;
import br.com.lume.odonto.entity.Fatura;
import br.com.lume.odonto.entity.Lancamento;
import br.com.lume.odonto.entity.Lancamento.DirecaoLancamento;
import br.com.lume.odonto.entity.Lancamento.StatusLancamento;
import br.com.lume.odonto.entity.Lancamento.SubStatusLancamento;
import br.com.lume.odonto.entity.Paciente;
import br.com.lume.odonto.entity.Profissional;
import br.com.lume.paciente.PacienteSingleton;
import br.com.lume.profissional.ProfissionalSingleton;
import br.com.lume.security.entity.Empresa;

@ManagedBean
@ViewScoped
public class MovimentacoesMB extends LumeManagedBean<Lancamento> {

    private static final long serialVersionUID = 1L;
    private List<Lancamento> aPagar, aReceber;
    private BigDecimal somaTotalAPagar = BigDecimal.ZERO, somaTotalAReceber = BigDecimal.ZERO;
    private Date dataInicioAReceber, dataFimAReceber, dataInicioAPagar, dataFimAPagar;
    private PeriodoBusca periodoBuscaAReceber, periodoBuscaAPagar;
    private Paciente paciente;
    private Profissional profissional;
    private StatusLancamento statusLancamentoAReceber, statusLancamentoAPagar;
    private SubStatusLancamento[] subStatusLancamentoAReceber, subStatusLancamentoAPagar;
    private List<SubStatusLancamento> listaSubStatusLancamentoAReceber, listaSubStatusLancamentoAPagar;

    private Empresa empresaLogada = UtilsFrontEnd.getEmpresaLogada();
    private BarChartModel lineModel;
    private boolean graficoCompleto = false;
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
        this.periodoBuscaAPagar = PeriodoBusca.MES_ATUAL;
        actionTrocaDatasAPagar();
        this.periodoBuscaAReceber = PeriodoBusca.MES_ATUAL;
        actionTrocaDatasAReceber();
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

    public List<StatusLancamento> getListaStatusLancamentoAReceber() {
        return Lancamento.getStatusLancamento(DirecaoLancamento.CREDITO);
    }

    public void alteraStatusLancamentoAReceber() {
        this.listaSubStatusLancamentoAReceber = new ArrayList<>();
        this.subStatusLancamentoAReceber = null;
        for (SubStatusLancamento subStatus : SubStatusLancamento.values())
            if (subStatus.isSonOfStatusLancamento(this.statusLancamentoAReceber))
                this.listaSubStatusLancamentoAReceber.add(subStatus);
    }

    public List<StatusLancamento> getListaStatusLancamentoAPagar() {
        return Lancamento.getStatusLancamento(DirecaoLancamento.DEBITO);
    }

    public void alteraStatusLancamentoAPagar() {
        this.listaSubStatusLancamentoAPagar = new ArrayList<>();
        this.subStatusLancamentoAPagar = null;
        for (SubStatusLancamento subStatus : SubStatusLancamento.values())
            if (subStatus.isSonOfStatusLancamento(this.statusLancamentoAPagar))
                this.listaSubStatusLancamentoAPagar.add(subStatus);
    }

    public String getFaturaInfo(Fatura f) {
        return FaturaSingleton.getInstance().getFaturaInfo(f);
    }

    public List<Paciente> sugestoesPacientes(String query) {
        return PacienteSingleton.getInstance().getBo().listSugestoesComplete(query, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
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

    public void actionTrocaDatasAPagar() {
        try {
            setDataInicioAPagar(UtilsPadraoRelatorio.getDataInicio(getPeriodoBuscaAPagar()));
            setDataFimAPagar(UtilsPadraoRelatorio.getDataFim(getPeriodoBuscaAPagar()));
        } catch (Exception e) {
            LogIntelidenteSingleton.getInstance().makeLog(e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
        }
    }

    public void carregaListaAPagar() {
        try {
            this.somaTotalAPagar = BigDecimal.ZERO;
            this.aPagar = LancamentoSingleton.getInstance().getBo().listContasAPagar(UtilsFrontEnd.getEmpresaLogada().getConta(), getDataInicioAPagar(), getDataFimAPagar(), null, getProfissional(),
                    getStatusLancamentoAPagar(), (getSubStatusLancamentoAPagar() == null ? null : Arrays.asList(getSubStatusLancamentoAPagar())));
            for (Lancamento lAPagar : this.aPagar)
                this.somaTotalAPagar = this.somaTotalAPagar.add(lAPagar.getValor());
        } catch (Exception e) {
            LogIntelidenteSingleton.getInstance().makeLog(e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
        }
    }

    public void actionTrocaDatasAReceber() {
        try {
            setDataInicioAReceber(UtilsPadraoRelatorio.getDataInicio(getPeriodoBuscaAReceber()));
            setDataFimAReceber(UtilsPadraoRelatorio.getDataFim(getPeriodoBuscaAReceber()));
        } catch (Exception e) {
            LogIntelidenteSingleton.getInstance().makeLog(e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
        }
    }

    public void carregaListaAReceber() {
        try {
            this.somaTotalAReceber = BigDecimal.ZERO;
            this.aReceber = LancamentoSingleton.getInstance().getBo().listContasAReceber(UtilsFrontEnd.getEmpresaLogada().getConta(), getDataInicioAReceber(), getDataFimAReceber(), getPaciente(),
                    null, getStatusLancamentoAReceber(), (getSubStatusLancamentoAReceber() == null ? null : Arrays.asList(getSubStatusLancamentoAReceber())));
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

    public Date getDataInicioAReceber() {
        return dataInicioAReceber;
    }

    public void setDataInicioAReceber(Date dataInicioAReceber) {
        this.dataInicioAReceber = dataInicioAReceber;
    }

    public Date getDataFimAReceber() {
        return dataFimAReceber;
    }

    public void setDataFimAReceber(Date dataFimAReceber) {
        this.dataFimAReceber = dataFimAReceber;
    }

    public Date getDataInicioAPagar() {
        return dataInicioAPagar;
    }

    public void setDataInicioAPagar(Date dataInicioAPagar) {
        this.dataInicioAPagar = dataInicioAPagar;
    }

    public Date getDataFimAPagar() {
        return dataFimAPagar;
    }

    public void setDataFimAPagar(Date dataFimAPagar) {
        this.dataFimAPagar = dataFimAPagar;
    }

    public PeriodoBusca getPeriodoBuscaAReceber() {
        return periodoBuscaAReceber;
    }

    public void setPeriodoBuscaAReceber(PeriodoBusca periodoBuscaAReceber) {
        this.periodoBuscaAReceber = periodoBuscaAReceber;
    }

    public PeriodoBusca getPeriodoBuscaAPagar() {
        return periodoBuscaAPagar;
    }

    public void setPeriodoBuscaAPagar(PeriodoBusca periodoBuscaAPagar) {
        this.periodoBuscaAPagar = periodoBuscaAPagar;
    }

    public Paciente getPaciente() {
        return paciente;
    }

    public void setPaciente(Paciente paciente) {
        this.paciente = paciente;
    }

    public StatusLancamento getStatusLancamentoAReceber() {
        return statusLancamentoAReceber;
    }

    public void setStatusLancamentoAReceber(StatusLancamento statusLancamentoAReceber) {
        this.statusLancamentoAReceber = statusLancamentoAReceber;
    }

    public StatusLancamento getStatusLancamentoAPagar() {
        return statusLancamentoAPagar;
    }

    public void setStatusLancamentoAPagar(StatusLancamento statusLancamentoAPagar) {
        this.statusLancamentoAPagar = statusLancamentoAPagar;
    }

    public SubStatusLancamento[] getSubStatusLancamentoAReceber() {
        return subStatusLancamentoAReceber;
    }

    public void setSubStatusLancamentoAReceber(SubStatusLancamento[] subStatusLancamentoAReceber) {
        this.subStatusLancamentoAReceber = subStatusLancamentoAReceber;
    }

    public SubStatusLancamento[] getSubStatusLancamentoAPagar() {
        return subStatusLancamentoAPagar;
    }

    public void setSubStatusLancamentoAPagar(SubStatusLancamento[] subStatusLancamentoAPagar) {
        this.subStatusLancamentoAPagar = subStatusLancamentoAPagar;
    }

    public Profissional getProfissional() {
        return profissional;
    }

    public void setProfissional(Profissional profissional) {
        this.profissional = profissional;
    }

    public List<SubStatusLancamento> getListaSubStatusLancamentoAReceber() {
        return listaSubStatusLancamentoAReceber;
    }

    public void setListaSubStatusLancamentoAReceber(List<SubStatusLancamento> listaSubStatusLancamentoAReceber) {
        this.listaSubStatusLancamentoAReceber = listaSubStatusLancamentoAReceber;
    }

    public List<SubStatusLancamento> getListaSubStatusLancamentoAPagar() {
        return listaSubStatusLancamentoAPagar;
    }

    public void setListaSubStatusLancamentoAPagar(List<SubStatusLancamento> listaSubStatusLancamentoAPagar) {
        this.listaSubStatusLancamentoAPagar = listaSubStatusLancamentoAPagar;
    }

}
