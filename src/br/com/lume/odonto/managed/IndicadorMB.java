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
import org.primefaces.model.charts.bar.BarChartDataSet;
import org.primefaces.model.charts.bar.BarChartModel;
import org.primefaces.model.charts.bar.BarChartOptions;
import org.primefaces.model.charts.line.LineChartDataSet;
import org.primefaces.model.charts.optionconfig.title.Title;
import org.primefaces.model.charts.optionconfig.tooltip.Tooltip;
import org.primefaces.model.charts.ChartData;
import org.primefaces.model.charts.axes.cartesian.CartesianScales;
import org.primefaces.model.charts.axes.cartesian.linear.CartesianLinearAxes;

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

    private String filtroPeriodo;
    private Date inicio, fim;
    private String tabela;
    private List<IndicadorDTO> indicadoresAgendamento, indicadoresFinanceiro, indicadoresOrcamento;
    
    private BarChartModel chart;
    
    public IndicadorMB() {
        super(new IndicadorBO());
        this.setClazz(Indicador.class);
        
        construirGrafico();
    }

    public void listDashboard() {
        indicadoresAgendamento = construirMetricaVariacao(inicio, fim, new IndicadoresAgendamentoBO());
        indicadoresFinanceiro = construirMetricaVariacao(inicio, fim, new IndicadoresFinanceiroBO());
        indicadoresOrcamento = construirMetricaVariacao(inicio, fim, new IndicadoresOrcamentoBO());
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
    
    public void construirGrafico() {
        setChart(new BarChartModel());
        ChartData data = new ChartData();
        
        BarChartDataSet barDataSet = new BarChartDataSet();
        barDataSet.setLabel("Dataset 1");
        barDataSet.setBackgroundColor("rgb(255, 99, 132)");
        List<Number> dataVal = new ArrayList<>();
        dataVal.add(62);
        dataVal.add(-58);
        dataVal.add(-49);
        dataVal.add(25);
        dataVal.add(4);
        dataVal.add(77);
        dataVal.add(-41);
        barDataSet.setData(dataVal);

        BarChartDataSet barDataSet2 = new BarChartDataSet();
        barDataSet2.setLabel("Dataset 2");
        barDataSet2.setBackgroundColor("rgb(54, 162, 235)");
        List<Number> dataVal2 = new ArrayList<>();
        dataVal2.add(-1);
        dataVal2.add(32);
        dataVal2.add(-52);
        dataVal2.add(11);
        dataVal2.add(97);
        dataVal2.add(76);
        dataVal2.add(-78);
        barDataSet2.setData(dataVal2);

        BarChartDataSet barDataSet3 = new BarChartDataSet();
        barDataSet3.setLabel("Dataset 3");
        barDataSet3.setBackgroundColor("rgb(75, 192, 192)");
        List<Number> dataVal3 = new ArrayList<>();
        dataVal3.add(-44);
        dataVal3.add(25);
        dataVal3.add(15);
        dataVal3.add(92);
        dataVal3.add(80);
        dataVal3.add(-25);
        dataVal3.add(-11);
        barDataSet3.setData(dataVal3);

        LineChartDataSet line = new LineChartDataSet();
        line.setLabel("teste");
        line.setBorderColor("rgb(255, 100, 100)");
        List<Object> dataVal4 = new ArrayList<>();
        
        for(int i = 0 ; i<6; i++)
            dataVal4.add(dataVal.get(i).intValue()
                    + dataVal2.get(i).intValue()
                    +dataVal3.get(i).intValue());
        
        line.setData(dataVal4);
        line.setFill(false);
        
        data.addChartDataSet(barDataSet);
        data.addChartDataSet(barDataSet2);
        data.addChartDataSet(barDataSet3);
        data.addChartDataSet(line);

        List<String> labels = new ArrayList<>();
        labels.add("January");
        labels.add("February");
        labels.add("March");
        labels.add("April");
        labels.add("May");
        labels.add("June");
        labels.add("July");
        data.setLabels(labels);
        getChart().setData(data);
        
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
        title.setText("Bar Chart - Stacked");
        options.setTitle(title);

        Tooltip tooltip = new Tooltip();
        tooltip.setMode("index");
        tooltip.setIntersect(false);
        options.setTooltip(tooltip);

        getChart().setOptions(options);
    }
    
    public List<IndicadorDTO> construirMetricaVariacao(Date inicio, Date fim, IndicadoresBO indicadorBO) {
        List<Indicador> indicadores = indicadorBO.listIndicadores(inicio, fim, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
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
                        ||indicador.getDescricao().equals("Orçamentos aprovados") 
                        || indicador.getDescricao().equals("Orçamentos não aprovados"))
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

    public BarChartModel getChart() {
        return chart;
    }

    public void setChart(BarChartModel chart) {
        this.chart = chart;
    }
}
