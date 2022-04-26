package br.com.lume.odonto.managed;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.faces.view.ViewScoped;
import javax.inject.Named;

import org.apache.log4j.Logger;
import org.primefaces.model.charts.ChartData;
import org.primefaces.model.charts.axes.cartesian.CartesianScales;
import org.primefaces.model.charts.axes.cartesian.linear.CartesianLinearAxes;
import org.primefaces.model.charts.bar.BarChartDataSet;
import org.primefaces.model.charts.bar.BarChartModel;
import org.primefaces.model.charts.bar.BarChartOptions;
import org.primefaces.model.charts.line.LineChartDataSet;
import org.primefaces.model.charts.line.LineChartModel;
import org.primefaces.model.charts.line.LineChartOptions;
import org.primefaces.model.charts.optionconfig.title.Title;
import org.primefaces.model.charts.optionconfig.tooltip.Tooltip;

import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.Utils;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.indicador.bo.IndicadorBO;
import br.com.lume.indicador.bo.IndicadoresAgendamentoBO;
import br.com.lume.indicador.bo.IndicadoresBO;
import br.com.lume.indicador.bo.IndicadoresFinanceiroBO;
import br.com.lume.indicador.bo.IndicadoresOrcamentoBO;
import br.com.lume.odonto.dto.IndicadorDTO;
import br.com.lume.odonto.entity.Indicador;

@Named
@ViewScoped
public class IndicadorMB extends LumeManagedBean<Indicador> implements Serializable {

    private static final long serialVersionUID = 1L;
    private static Logger log = Logger.getLogger(IndicadorMB.class);

    //Fitros de tela
    private String filtroPeriodo;
    private Date inicio, fim;
    private String tabela;
    private String tipoDado = "C";
    private String tipoIntervalo = "aberto";
    
    private List<IndicadorDTO> indicadoresAgendamento, indicadoresFinanceiro, indicadoresOrcamento;
    
    //Graficos agendamento
    private BarChartModel chartAgendamentosRealizados;
    private BarChartModel chartAgendamentosNaoRealizados;
    
    //Graficos financeiro
    private LineChartModel chartFluxoCaixa;
    private BarChartModel chartGastosPorTipo;
    private LineChartModel chartPlanejamentoPendente;
    
    //Graficos orcamento
    private BarChartModel chartOrcamentos;
    private BarChartModel chartOrcamentosQtde;
    private LineChartModel chartTicketMedio;
    private LineChartModel chartIndiceAprovacao;
    
    public IndicadorMB() {
        super(new IndicadorBO());
        this.setClazz(Indicador.class);
    }

    public void listDashboard() {
        indicadoresAgendamento = construirMetricaVariacao(inicio, fim, new IndicadoresAgendamentoBO());
        indicadoresFinanceiro = construirMetricaVariacao(inicio, fim, new IndicadoresFinanceiroBO());
        indicadoresOrcamento = construirMetricaVariacao(inicio, fim, new IndicadoresOrcamentoBO());
        
        chartAgendamentosRealizados = graficoAgendamentosRealizados();
        chartAgendamentosNaoRealizados = graficoAgendamentosNaoRealizados();
        
        chartFluxoCaixa = graficoFluxoCaixa();
        chartGastosPorTipo = graficoGastosPorTipo();
        chartPlanejamentoPendente = graficoPlanejamentoPendente();
        
        chartOrcamentos = graficoOrcamentos();
        chartOrcamentosQtde = graficoOrcamentosQtde();
        chartTicketMedio = graficoTicketMedioOrcamento();
        chartIndiceAprovacao = graficoIndiceAprovacaoOrcamento();
    }
    
    public void actionTrocaDatasCriacao() {
        try {
            setInicio(getDataInicio(filtroPeriodo));
            setFim(getDataFim(filtroPeriodo));
        } catch (Exception e) {
            log.error("Erro no actionTrocaDatasCriacao", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
        }
    }
    
    public Date getDataFim(String filtro) {
        Date dataFim = null;
        try {
            Calendar c = Calendar.getInstance();
            if ("O".equals(filtro)) {
                c.add(Calendar.DAY_OF_MONTH, -1);
                dataFim = c.getTime();
            } else if (filtro == null) {
                dataFim = null;
            } else {
                dataFim = c.getTime();
            }
            return dataFim;
        } catch (Exception e) {
            log.error("Erro no getDataFim", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            return null;
        }
    }

    public Date getDataInicio(String filtro) {
        Date dataInicio = null;
        try {
            Calendar c = Calendar.getInstance();
            if ("S".equals(filtro)) { //Últimos 7 dias              
                c.add(Calendar.DAY_OF_MONTH, -7);
                dataInicio = c.getTime();
            } else if ("Q".equals(filtro)) { //Últimos 15 dias              
                c.add(Calendar.DAY_OF_MONTH, -15);
                dataInicio = c.getTime();
            } else if ("T".equals(filtro)) { //Últimos 30 dias                
                c.add(Calendar.DAY_OF_MONTH, -30);
                dataInicio = c.getTime();
            } else if ("I".equals(filtro)) { //Mês Atual             
                c.add(Calendar.MONTH, -6);
                dataInicio = c.getTime();
            }
            return dataInicio;
        } catch (Exception e) {
            log.error("Erro no getDataInicio", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            return null;
        }
    }
    
    public LineChartModel graficoFluxoCaixa() {        
        LineChartModel grafico = new LineChartModel();
        ChartData dados = new ChartData();
        List<String> labels = new ArrayList<String>();
        
        LineChartDataSet l1 = new LineChartDataSet();
        List<Object> list1 = new ArrayList<Object>();
        
        LineChartDataSet l2 = new LineChartDataSet();
        List<Object> list2 = new ArrayList<Object>();
        
        l1.setLabel("Ganhos");
        l1.setBorderColor("rgb(50,205,50)");
        l1.setFill(false);
        
        l2.setLabel("Gastos");
        l2.setBorderColor("rgb(217,5,12)");
        l2.setFill(false);
        
        for(IndicadorDTO indicador : indicadoresFinanceiro) {
            switch(indicador.getDescricao()) {
                case "Ganhos" :{
                    for(IndicadorDTO ind : indicador.getIndicadores()) {
                        list1.add(ind.getValor());
                        labels.add(ind.getMes());
                    }
                }break;
                case "Gastos" :{
                    for(IndicadorDTO ind : indicador.getIndicadores())
                        list2.add(ind.getValor());
                }break;
            }
        }
        
        l1.setData(list1);
        l2.setData(list2);
        
        dados.addChartDataSet(l1);
        dados.addChartDataSet(l2);

        dados.setLabels(labels);
        
        grafico.setData(dados);
        
        LineChartOptions options = new LineChartOptions();
        CartesianScales cScales = new CartesianScales();
        CartesianLinearAxes linearAxes = new CartesianLinearAxes();
        linearAxes.setStacked(true);
        linearAxes.setOffset(true);
        cScales.addXAxesData(linearAxes);
        cScales.addYAxesData(linearAxes);
        options.setScales(cScales);
        
        Title title = new Title();
        title.setDisplay(true);
        title.setText("Fluxo de caixa (R$)");
        options.setTitle(title);

        Tooltip tooltip = new Tooltip();
        tooltip.setMode("index");
        tooltip.setIntersect(false);
        options.setTooltip(tooltip);
        
        grafico.setOptions(options);
        
        return grafico;
    }
    
    public BarChartModel graficoGastosPorTipo() {
        List<BarChartDataSet> listaBarras = new ArrayList<BarChartDataSet>();
        List<LineChartDataSet> listaLinhas = new ArrayList<LineChartDataSet>();
        List<String> labels = new ArrayList<String>();
        
        BarChartModel grafico = new BarChartModel();
        
        LineChartDataSet total = new LineChartDataSet();
        List<Object> listTotal = new ArrayList<Object>();
        
        BarChartDataSet b1 = new BarChartDataSet(), b2 = new BarChartDataSet(),
                b3 = new BarChartDataSet();
        
        List<Number> list1 = new ArrayList<Number>(), list2 = new ArrayList<Number>(),
                list3 = new ArrayList<Number>();
        
        b1.setLabel("Odontológicos");
        b1.setBackgroundColor("rgb(255,100,100)");
        
        b2.setLabel("Gerais");
        b2.setBackgroundColor("rgb(255,150,50)");
        
        b3.setLabel("Operacionais");
        b3.setBackgroundColor("rgb(255,120,70)");
        
        total.setLabel("Total");
        total.setBorderColor("rgb(135,206,235)");
        total.setFill(false);
        
        for(IndicadorDTO indicador : indicadoresFinanceiro) {
            switch(indicador.getDescricao()) {
                case "Gastos odontologicos" :{
                    for(IndicadorDTO ind : indicador.getIndicadores()) {
                        list1.add(ind.getValor().abs());
                        labels.add(ind.getMes());
                    }
                }break;
                case "Gastos gerais" :{
                    for(IndicadorDTO ind : indicador.getIndicadores())
                        list2.add(ind.getValor().abs());
                }break;
                case "Gastos operacionais" :{
                    for(IndicadorDTO ind : indicador.getIndicadores())
                        list3.add(ind.getValor().abs());
                }break;
            }
        }
        
        int count = 0;
        for(Number n : list1)
            listTotal.add(new BigDecimal(n.doubleValue())
                    .add(new BigDecimal(list2.get(count).doubleValue())
                            .add(new BigDecimal(list3.get(count++).doubleValue()))));
        
        b1.setData(list1);
        b2.setData(list2);
        b3.setData(list3);
        total.setData(listTotal);
        
        listaBarras.add(b1);
        listaBarras.add(b2);
        listaBarras.add(b3);
        
        listaLinhas.add(total);

        grafico = construirGraficoBarra("Gastos por tipo (R$)", listaBarras, listaLinhas, labels);
        
        return grafico;
    }
    
    public LineChartModel graficoPlanejamentoPendente() {
        LineChartModel grafico = new LineChartModel();
        ChartData dados = new ChartData();
        List<String> labels = new ArrayList<String>();
        
        LineChartDataSet l1 = new LineChartDataSet();
        List<Object> list1 = new ArrayList<Object>();
        
        l1.setLabel("Planejamento pendente");
        l1.setBorderColor("rgb(255,100,100)");
        l1.setFill(false);
        
        for(IndicadorDTO indicador : indicadoresFinanceiro) {
            if(indicador.getDescricao().equals("Valor planejamento pendente")) {
                for(IndicadorDTO ind : indicador.getIndicadores()) {
                    list1.add(ind.getValor());
                    labels.add(ind.getMes());
                }
            }
        }
        
        l1.setData(list1);
        
        dados.addChartDataSet(l1);
        dados.setLabels(labels);
        
        grafico.setData(dados);
        
        LineChartOptions options = new LineChartOptions();
        CartesianScales cScales = new CartesianScales();
        CartesianLinearAxes linearAxes = new CartesianLinearAxes();
        linearAxes.setStacked(true);
        linearAxes.setOffset(true);
        cScales.addXAxesData(linearAxes);
        cScales.addYAxesData(linearAxes);
        options.setScales(cScales);
        
        Title title = new Title();
        title.setDisplay(true);
        title.setText("Planejamento pendente (R$)");
        options.setTitle(title);

        Tooltip tooltip = new Tooltip();
        tooltip.setMode("index");
        tooltip.setIntersect(false);
        options.setTooltip(tooltip);
        
        grafico.setOptions(options);
        
        return grafico;
    }
    
    public BarChartModel graficoOrcamentos() {
        List<BarChartDataSet> listaBarras = new ArrayList<BarChartDataSet>();
        List<LineChartDataSet> listaLinhas = new ArrayList<LineChartDataSet>();
        List<String> labels = new ArrayList<String>();
        
        BarChartModel grafico = new BarChartModel();
        
        LineChartDataSet total = new LineChartDataSet();
        List<Object> listTotal = new ArrayList<Object>();
        
        BarChartDataSet b1 = new BarChartDataSet(), b2 = new BarChartDataSet(),
                b3 = new BarChartDataSet();
        
        List<Number> list1 = new ArrayList<Number>(), list2 = new ArrayList<Number>(),
                list3 = new ArrayList<Number>();
        
        b1.setLabel("Aprovado");
        b1.setBackgroundColor("rgb(50,205,50)");
        
        b2.setLabel("Não aprovado");
        b2.setBackgroundColor("rgb(217,5,12)");
        
        b3.setLabel("Pendentes de aprovação");
        b3.setBackgroundColor("rgb(255,120,70)");
        
        total.setLabel("Total");
        total.setFill(false);
        total.setBorderColor("rgb(135,206,235)");
        
        for(IndicadorDTO indicador : indicadoresOrcamento) {
            switch(indicador.getDescricao()) {
                case "Orçamentos aprovados" :{
                    for(IndicadorDTO ind : indicador.getIndicadores()) {
                        list1.add(ind.getValor());
                        labels.add(ind.getMes());
                    }
                }break;
                case "Orçamentos não aprovados" :{
                    for(IndicadorDTO ind : indicador.getIndicadores())
                        list2.add(ind.getValor());
                }break;
                case "Pendentes de aprovação" :{
                    for(IndicadorDTO ind : indicador.getIndicadores())
                        list3.add(ind.getValor());
                }break;
            }
        }
        
        int count = 0;
        for(Number n : list1)
            listTotal.add(new BigDecimal(n.doubleValue())
                    .add(new BigDecimal(list2.get(count).doubleValue())
                            .add(new BigDecimal(list3.get(count++).doubleValue()))));
        
        b1.setData(list1);
        b2.setData(list2);
        b3.setData(list3);
        total.setData(listTotal);
        
        listaBarras.add(b1);
        listaBarras.add(b2);
        listaBarras.add(b3);
        
        listaLinhas.add(total);

        grafico = construirGraficoBarra("Status de orçamento (R$)", listaBarras, listaLinhas, labels);
        
        return grafico;
    }
    
    public BarChartModel graficoOrcamentosQtde() {
        List<BarChartDataSet> listaBarras = new ArrayList<BarChartDataSet>();
        List<LineChartDataSet> listaLinhas = new ArrayList<LineChartDataSet>();
        List<String> labels = new ArrayList<String>();
        
        BarChartModel grafico = new BarChartModel();
        
        LineChartDataSet total = new LineChartDataSet();
        List<Object> listTotal = new ArrayList<Object>();
        
        BarChartDataSet b1 = new BarChartDataSet(), b2 = new BarChartDataSet();
        List<Number> list1 = new ArrayList<Number>(), list2 = new ArrayList<Number>();
        
        b1.setLabel("Aprovado");
        b1.setBackgroundColor("rgb(50,205,50)");
        
        b2.setLabel("Pendentes de aprovação");
        b2.setBackgroundColor("rgb(255,150,50)");
        
        total.setLabel("Total");
        total.setBorderColor("rgb(135,206,235)");
        total.setFill(false);
        
        for(IndicadorDTO indicador : indicadoresOrcamento) {
            switch(indicador.getDescricao()) {
                case "Quantidade de orçamentos aprovados" :{
                    for(IndicadorDTO ind : indicador.getIndicadores()) {
                        list1.add(ind.getValor());
                        labels.add(ind.getMes());
                    }
                }break;
                
                case "Quantidade pendente aprovação" :{
                    for(IndicadorDTO ind : indicador.getIndicadores())
                        list2.add(ind.getValor());
                }break;
            }
        }

        int count = 0;
        for(Number n : list1)
            listTotal.add(new BigDecimal(n.doubleValue())
                    .add(new BigDecimal(list2.get(count++).doubleValue())));
        
        b1.setData(list1);
        b2.setData(list2);
        total.setData(listTotal);
        
        listaBarras.add(b1);
        listaBarras.add(b2);
        
        listaLinhas.add(total);

        grafico = construirGraficoBarra("Status de orçamento (Quantidade) (R$)", listaBarras, listaLinhas, labels);
        
        return grafico;
    }
    
    public LineChartModel graficoTicketMedioOrcamento() {
        LineChartModel grafico = new LineChartModel();
        ChartData dados = new ChartData();
        
        List<String> labels = new ArrayList<String>();
        
        LineChartDataSet l1 = new LineChartDataSet();
        List<Object> list1 = new ArrayList<Object>();
        
        l1.setLabel("Ticket médio");
        l1.setBorderColor("rgb(255,100,100)");
        l1.setFill(false);
        
        for(IndicadorDTO indicador : indicadoresOrcamento) {
            if(indicador.getDescricao().equals("Ticket medio")) {
                for(IndicadorDTO ind : indicador.getIndicadores()) {
                    list1.add(ind.getValor());
                    labels.add(ind.getMes());
                }
            }
        }
        
        l1.setData(list1);
        
        dados.addChartDataSet(l1);
        dados.setLabels(labels);
        
        grafico.setData(dados);
        
        LineChartOptions options = new LineChartOptions();
        CartesianScales cScales = new CartesianScales();
        CartesianLinearAxes linearAxes = new CartesianLinearAxes();
        linearAxes.setStacked(true);
        linearAxes.setOffset(true);
        cScales.addXAxesData(linearAxes);
        cScales.addYAxesData(linearAxes);
        options.setScales(cScales);
        
        Title title = new Title();
        title.setDisplay(true);
        title.setText("Ticket médio (%)");
        options.setTitle(title);

        Tooltip tooltip = new Tooltip();
        tooltip.setMode("index");
        tooltip.setIntersect(false);
        options.setTooltip(tooltip);
        
        grafico.setOptions(options);
        
        return grafico;
    }
    
    public LineChartModel graficoIndiceAprovacaoOrcamento() {
        List<String> labels = new ArrayList<String>();
        
        LineChartModel grafico = new LineChartModel();
        ChartData dados = new ChartData();
        
        LineChartDataSet l1 = new LineChartDataSet();
        List<Object> list1 = new ArrayList<Object>();
        
        l1.setLabel("Índice de aprovação");
        l1.setBorderColor("rgb(50,205,50)");
        l1.setFill(false);
        
        for(IndicadorDTO indicador : indicadoresOrcamento) {
            if(indicador.getDescricao().equals("Índice de aprovação")) {
                for(IndicadorDTO ind : indicador.getIndicadores()) {
                    list1.add(ind.getValor());
                    labels.add(ind.getMes());
                }
            }
        }
        
        l1.setData(list1);
        
        dados.addChartDataSet(l1);
        dados.setLabels(labels);
        
        grafico.setData(dados);
        
        LineChartOptions options = new LineChartOptions();
        CartesianScales cScales = new CartesianScales();
        CartesianLinearAxes linearAxes = new CartesianLinearAxes();
        linearAxes.setStacked(true);
        linearAxes.setOffset(true);
        cScales.addXAxesData(linearAxes);
        cScales.addYAxesData(linearAxes);
        options.setScales(cScales);
        
        Title title = new Title();
        title.setDisplay(true);
        title.setText("Índice de aprovação (%)");
        options.setTitle(title);

        Tooltip tooltip = new Tooltip();
        tooltip.setMode("index");
        tooltip.setIntersect(false);
        options.setTooltip(tooltip);
        
        grafico.setOptions(options);
        
        return grafico;
    }
    
    public BarChartModel graficoAgendamentosRealizados() {
        List<LineChartDataSet> listaLinhas = new ArrayList<LineChartDataSet>();
        List<BarChartDataSet> listaBarras = new ArrayList<BarChartDataSet>();
        List<String> labels = new ArrayList<String>();
        
        BarChartModel grafico = new BarChartModel();
        
        LineChartDataSet total = new LineChartDataSet();
        List<Object> listTotal = new ArrayList<Object>();
        
        BarChartDataSet b1 = new BarChartDataSet();
        List<Number> list1 = new ArrayList<Number>();
        
        BarChartDataSet b2 = new BarChartDataSet();
        List<Number> list2 = new ArrayList<Number>();        
        
        b1.setLabel("Agendamentos realizados");
        b1.setBackgroundColor("rgb(255,100,100)");
        
        b2.setLabel("Agendamentos não realizados");
        b2.setBackgroundColor("rgb(250,150,50)");
        
        total.setFill(false);
        total.setBorderColor("rgb(135,206,235)");
        total.setLabel("Total");
        
        for(IndicadorDTO indicador : indicadoresAgendamento) {
            switch(indicador.getDescricao()) {
                case "Atendimentos" :{
                    for(IndicadorDTO ind : indicador.getIndicadores()) {
                        list1.add(ind.getValor());
                        labels.add(ind.getMes());
                    }
                }break;
                case "Não atendidos" :{
                    for(IndicadorDTO ind : indicador.getIndicadores())
                        list2.add(ind.getValor());
                }break;
            }
        }
        
        int count = 0;
        for(Number n : list1)
            listTotal.add(new BigDecimal(n.doubleValue())
                    .add(new BigDecimal(list2.get(count++).doubleValue())));
        
        b1.setData(list1);
        b2.setData(list2);
        total.setData(listTotal);
        
        listaLinhas.add(total);
        listaBarras.add(b1);
        listaBarras.add(b2);
        
        grafico = construirGraficoBarra("Agendamentos realizados", listaBarras, listaLinhas, labels);
        
        return grafico;
    }
    
    public BarChartModel graficoAgendamentosNaoRealizados() {
        List<LineChartDataSet> listaLinhas = new ArrayList<LineChartDataSet>();
        List<BarChartDataSet> listaBarras = new ArrayList<BarChartDataSet>();
        List<String> labels = new ArrayList<String>();
        
        BarChartModel grafico = new BarChartModel();
        
        LineChartDataSet total = new LineChartDataSet();
        List<Object> listTotal = new ArrayList<Object>();
        
        BarChartDataSet b1 = new BarChartDataSet(), b2 = new BarChartDataSet(),
                b3 = new BarChartDataSet(), b4 = new BarChartDataSet();
        
        List<Number> list1 = new ArrayList<Number>(), list2 = new ArrayList<Number>(),
                list3 = new ArrayList<Number>(), list4 = new ArrayList<Number>();
        
        b1.setLabel("Remarcados");
        b1.setBackgroundColor("rgb(208,80,208)");
        
        b2.setLabel("Cancelados");
        b2.setBackgroundColor("rgb(128,0,128)");
        
        b3.setLabel("Faltas");
        b3.setBackgroundColor("rgb(178,34,34)");
        
        b4.setLabel("Erro de agendamento");
        b4.setBackgroundColor("rgb(196,196,196)");
        
        total.setFill(false);
        total.setBorderColor("rgb(135,206,235)");
        total.setLabel("Total");
        
        for(IndicadorDTO indicador : indicadoresAgendamento) {
            switch(indicador.getDescricao()) {
                case "Remarcados" :{
                    for(IndicadorDTO ind : indicador.getIndicadores()) {
                        list1.add(ind.getValor());
                        labels.add(ind.getMes());
                    }
                }break;
                case "Cancelamentos" :{
                    for(IndicadorDTO ind : indicador.getIndicadores())
                        list2.add(ind.getValor());
                }break;
                case "Faltas" :{
                    for(IndicadorDTO ind : indicador.getIndicadores())
                        list3.add(ind.getValor());
                }break;
                case "Agendamentos com erro" :{
                    for(IndicadorDTO ind : indicador.getIndicadores())
                        list4.add(ind.getValor());
                }break;
            }
        }
        
        int count = 0;
        for(Number n : list1)
            listTotal.add(new BigDecimal(n.doubleValue())
                    .add(new BigDecimal(list2.get(count).doubleValue())
                            .add(new BigDecimal(list3.get(count).doubleValue())
                                    .add(new BigDecimal(list4.get(count++).doubleValue())))));
        
        b1.setData(list1);
        b2.setData(list2);
        b3.setData(list3);
        b4.setData(list4);
        total.setData(listTotal);
        
        listaLinhas.add(total);
        listaBarras.add(b1);
        listaBarras.add(b2);
        listaBarras.add(b3);
        listaBarras.add(b4);
        
        grafico = construirGraficoBarra("Motivo agendamentos não realizados", listaBarras, listaLinhas, labels);
        
        return grafico;
    }
    
    public BarChartModel construirGraficoBarra(String titulo, List<BarChartDataSet> listaBarras,
            List<LineChartDataSet> listaLinhas, List<String> labels) {
        BarChartModel grafico = new BarChartModel();
        ChartData dados = new ChartData();
        
        listaLinhas.forEach((l) -> dados.addChartDataSet(l));
        listaBarras.forEach((b) -> dados.addChartDataSet(b));

        dados.setLabels(labels);
        
        grafico.setData(dados);
        
        //Options
        BarChartOptions options = new BarChartOptions();
        CartesianScales cScales = new CartesianScales();
        CartesianLinearAxes linearAxes = new CartesianLinearAxes();
        linearAxes.setStacked(true);
        linearAxes.setOffset(true);
        cScales.addXAxesData(linearAxes);
        cScales.addYAxesData(linearAxes);
        options.setScales(cScales);
        
        Title title = new Title();
        title.setDisplay(true);
        title.setText(titulo);
        options.setTitle(title);

        Tooltip tooltip = new Tooltip();
        tooltip.setMode("index");
        tooltip.setIntersect(false);
        options.setTooltip(tooltip);
        
        grafico.setOptions(options);

        return grafico;
    }
    
    public List<IndicadorDTO> construirMetricaVariacao(Date inicio, Date fim, IndicadoresBO indicadorBO) {
        List<Indicador> indicadores = indicadorBO.listIndicadores(inicio, fim, tipoIntervalo,
                UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
        List<IndicadorDTO> indicadoresDTO = IndicadorDTO.converter(indicadores);
        List<IndicadorDTO> listaResultado = new ArrayList<>();
        
        Map<String, List<IndicadorDTO>> mapIndicadores = indicadoresDTO.stream()
                .collect(Collectors.groupingBy(IndicadorDTO::getDescricao));
        
        IndicadorDTO indicadorAnterior = null;
        IndicadorDTO indicadorPai = null;
        
        for(List<IndicadorDTO> values : mapIndicadores.values()) {
            listaResultado.add(new IndicadorDTO(null));
            
            for(IndicadorDTO indicador : values) {
                indicador.setMetrica("(0%)");
                
                if(indicador.getDescricao().equals("Saldo") || indicador.getDescricao().equals("Gastos") 
                        || indicador.getDescricao().equals("Ganhos")
                        || indicador.getDescricao().equals("Orçamentos aprovados") 
                        || indicador.getDescricao().equals("Orçamentos não aprovados")
                        || indicador.getDescricao().equals("Gastos odontologicos")
                        || indicador.getDescricao().equals("Gastos operacionais")
                        || indicador.getDescricao().equals("Gastos gerais")
                        || indicador.getDescricao().equals("Valor planejamento pendente"))
                    indicador.setTipoDado("moeda");
                
                if(indicadorAnterior == null) {
                    indicadorPai = listaResultado.get(listaResultado.size()-1);
                    indicadorPai.setId(indicador.getId());
                    indicadorPai.setDescricao(indicador.getDescricao());
                    indicadorPai.setMes(indicador.getMes());
                    indicadorPai.setIndicadores(new ArrayList<IndicadorDTO>());
                }
                
                if(indicadorAnterior != null) {
                    BigDecimal valorAtual = indicador.getValor().abs();
                    BigDecimal valorAnterior = indicadorAnterior.getValor().abs();

                    if(valorAnterior.compareTo(BigDecimal.ZERO) != 0) {
                        BigDecimal metrica = ((valorAtual.subtract(valorAnterior))
                                .divide(valorAnterior,2,BigDecimal.ROUND_HALF_UP)).multiply(new BigDecimal(100));
                        indicador.setMetrica("(" + String.valueOf(metrica) + "%" + ")");
                    }
                }
                
                indicadorAnterior = indicador;
                indicadorPai.getIndicadores().add(indicador);
            }
            indicadorAnterior = null;
        }
        
        listaResultado.sort((i1,i2) -> i1.getId().compareTo(i2.getId()));
        
        return listaResultado;
    }
    
    public int rowIndexByKeyFinanceiro(IndicadorDTO key) {
        int count = 0;
        if(key != null) {
            for(IndicadorDTO indicador : indicadoresFinanceiro) {
                if(indicador.getDescricao().equals(key.getDescricao()))
                    return count;
                count++;
            }

        }
        return count;
    }
    
    public int rowIndexByKeyOrcamento(IndicadorDTO key) {
        int count = 0;
        if(key != null) {
            for(IndicadorDTO indicador : indicadoresOrcamento) {
                if(indicador.getDescricao().equals(key.getDescricao()))
                    return count;
                count++;
            }

        }
        return count;
    }
    
    public int rowIndexByKeyAgendamento(IndicadorDTO key) {
        int count = 0;
        if(key != null) {
            for(IndicadorDTO indicador : indicadoresAgendamento) {
                if(indicador.getDescricao().equals(key.getDescricao()))
                    return count;
                count++;
            }

        }
        return count;
    }
    
    public Date getInicio() {
        return inicio;
    }

    public void setInicio(Date inicio) {
        this.inicio = Utils.formataComecoDia(inicio);
    }

    public Date getFim() {
        return fim;
    }

    public void setFim(Date fim) {
        this.fim = Utils.formataFimDia(fim);
    }

    public String getTabela() {
        return tabela;
    }

    public void setTabela(String tabela) {
        this.tabela = tabela;
    }

    
    public String getFiltroPeriodo() {
        return filtroPeriodo;
    }

    
    public void setFiltroPeriodo(String filtroPeriodo) {
        this.filtroPeriodo = filtroPeriodo;
    }

    
    public List<IndicadorDTO> getIndicadoresAgendamento() {
        return indicadoresAgendamento;
    }

    
    public void setIndicadoresAgendamento(List<IndicadorDTO> indicadoresAgendamento) {
        this.indicadoresAgendamento = indicadoresAgendamento;
    }

    
    public List<IndicadorDTO> getIndicadoresFinanceiro() {
        return indicadoresFinanceiro;
    }

    
    public void setIndicadoresFinanceiro(List<IndicadorDTO> indicadoresFinanceiro) {
        this.indicadoresFinanceiro = indicadoresFinanceiro;
    }

    public List<IndicadorDTO> getIndicadoresOrcamento() {
        return indicadoresOrcamento;
    }

    public void setIndicadoresOrcamento(List<IndicadorDTO> indicadoresOrcamento) {
        this.indicadoresOrcamento = indicadoresOrcamento;
    }

    public BarChartModel getChartAgendamentosRealizados() {
        return chartAgendamentosRealizados;
    }

    public void setChartAgendamentosRealizados(BarChartModel chart) {
        this.chartAgendamentosRealizados = chart;
    }

    public BarChartModel getChartOrcamentos() {
        return chartOrcamentos;
    }

    public void setChartOrcamentos(BarChartModel chartOrcamentos) {
        this.chartOrcamentos = chartOrcamentos;
    }

    public LineChartModel getChartFluxoCaixa() {
        return chartFluxoCaixa;
    }

    public void setChartFluxoCaixa(LineChartModel chartFinanceiro) {
        this.chartFluxoCaixa = chartFinanceiro;
    }

    public BarChartModel getChartOrcamentosQtde() {
        return chartOrcamentosQtde;
    }

    public void setChartOrcamentosQtde(BarChartModel chartOrcamentosQtde) {
        this.chartOrcamentosQtde = chartOrcamentosQtde;
    }

    public BarChartModel getChartGastosPorTipo() {
        return chartGastosPorTipo;
    }

    public void setChartGastosPorTipo(BarChartModel chartGastosPorTipo) {
        this.chartGastosPorTipo = chartGastosPorTipo;
    }

    public LineChartModel getChartPlanejamentoPendente() {
        return chartPlanejamentoPendente;
    }

    public void setChartPlanejamentoPendente(LineChartModel chartPlanejamentoPendente) {
        this.chartPlanejamentoPendente = chartPlanejamentoPendente;
    }

    public LineChartModel getChartTicketMedio() {
        return chartTicketMedio;
    }

    public void setChartTicketMedio(LineChartModel chartTicketMedio) {
        this.chartTicketMedio = chartTicketMedio;
    }

    public LineChartModel getChartIndiceAprovacao() {
        return chartIndiceAprovacao;
    }

    public void setChartIndiceAprovacao(LineChartModel chartIndiceAprovacao) {
        this.chartIndiceAprovacao = chartIndiceAprovacao;
    }

    public String getTipoDado() {
        return tipoDado;
    }

    public void setTipoDado(String tipoDado) {
        this.tipoDado = tipoDado;
    }

    public String getTipoIntervalo() {
        return tipoIntervalo;
    }

    public void setTipoIntervalo(String tipoIntervalo) {
        this.tipoIntervalo = tipoIntervalo;
    }

    public BarChartModel getChartAgendamentosNaoRealizados() {
        return chartAgendamentosNaoRealizados;
    }

    public void setChartAgendamentosNaoRealizados(BarChartModel chartAgendamentosNaoRealizados) {
        this.chartAgendamentosNaoRealizados = chartAgendamentosNaoRealizados;
    }
}
