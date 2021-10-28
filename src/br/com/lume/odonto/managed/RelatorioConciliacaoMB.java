package br.com.lume.odonto.managed;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.view.ViewScoped;
import javax.inject.Named;

import org.apache.log4j.Logger;
import org.primefaces.PrimeFaces;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.model.DashboardModel;
import org.primefaces.model.charts.ChartData;
import org.primefaces.model.charts.bar.BarChartDataSet;
import org.primefaces.model.charts.bar.BarChartModel;
import org.primefaces.model.charts.bar.BarChartOptions;
import org.primefaces.model.charts.line.LineChartDataSet;
import org.primefaces.model.charts.optionconfig.title.Title;

import br.com.lume.common.iugu.responses.InvoiceResponse;
import br.com.lume.common.iugu.responses.ItemResponse;
import br.com.lume.common.iugu.service.InvoiceService;
import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.Utils.Mes;
import br.com.lume.security.EmpresaSingleton;
import br.com.lume.security.entity.Empresa;

@Named
@ViewScoped
public class RelatorioConciliacaoMB extends LumeManagedBean<Empresa> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(RelatorioConciliacaoMB.class);
    
    private DashboardModel dashboardModel;
    private BarChartModel lineModel;
    
    //DATA
    private BigDecimal receiveCurrentMonth = BigDecimal.ZERO;
    private BigDecimal pendingReceive = BigDecimal.ZERO;
    private BigDecimal subscribers = BigDecimal.ZERO;
    private BigDecimal totalInvoices = BigDecimal.ZERO;
    private BigDecimal totalPaidInvoices = BigDecimal.ZERO;
    private List<ItemResponse> invoices = new ArrayList<ItemResponse>();
    private InvoiceResponse invoicesResponse;
    
    //FILTERS
    private String timeCourse;
    private Date initialDate;
    private Date finalDate;
    
    private DataTable tableReport;

    public RelatorioConciliacaoMB() {
        super(EmpresaSingleton.getInstance().getBo());
        this.setClazz(Empresa.class);
        
        initializeView();
    }

    public void filter() {
        try {
            SimpleDateFormat sm = new SimpleDateFormat("yyyy/MM/dd'T'hh:mm:ssZ");
            InvoiceService service = new InvoiceService();
            int startRegister = 0;
            
            Map<String, String> values = new HashMap<String, String>();
            values.put("created_at_from", sm.format(getInitialDate()));
            values.put("created_at_to", sm.format(getFinalDate()));
            
            do {
                values.put("start", String.valueOf(startRegister));
                
                startRegister += 100;
                this.invoicesResponse = service.listInvoices(values);
                this.invoices.addAll(filterInvoicesBySignature());
            } while(invoicesResponse.getItems().size() == 100);
            
            this.invoices.forEach((invoice) ->{
                
                String value[] = invoice.getTotal().split(" ");
                this.totalInvoices = this.totalInvoices.add(new BigDecimal(value[1].replaceAll(",", ".")));

                if(invoice.getPaidAt() != null && invoice.getTotalPaid() != null) {
                    value = invoice.getTotalPaid().split(" ");
                    this.totalPaidInvoices = this.totalPaidInvoices.add(new BigDecimal(value[1].replaceAll(",", ".")));
                }
            });
        } catch (Exception e) {
            log.error("Erro no filter", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
        }
    }
    
    public void initializeView() {
        setTimeCourse("M");
        
        this.initialDate = changeInitialDate(timeCourse);
        this.finalDate = changeFinalDate(timeCourse);
        
        filter();
        
        for(ItemResponse item : this.invoices) {
            if(item.getPaidAt() != null && item.getTotalPaid() != null) {
                String value[] = item.getTotalPaid().split(" ");
                this.receiveCurrentMonth = this.receiveCurrentMonth.add(new BigDecimal(value[1].replaceAll(",", ".")));
            } else if((item.getPaidAt() == null && item.getStatusStr() != null) && item.getStatusStr().toLowerCase().equals("pending") && item.getTotal() != null) {
                String value[] = item.getTotal().split(" ");
                this.pendingReceive = this.pendingReceive.add(new BigDecimal(value[1].replaceAll(",", ".")));
            }
        }
        
        this.subscribers = new BigDecimal(this.invoices.size());
    }
    
    public List<ItemResponse> filterInvoicesBySignature() {
        List<ItemResponse> items = new ArrayList<ItemResponse>();
        boolean existe = false;
        for(ItemResponse item : invoicesResponse.getItems()) {
            if(item.getItems().get(0).getDescription().toUpperCase().contains("INTELIDENTE") 
                    && item.getStatusStr() != null 
                    && (item.getStatusStr().toLowerCase().equals("pending") || item.getStatusStr().toLowerCase().equals("paid"))) {
                existe = false;
                for(ItemResponse i : this.invoices)
                    if(i.getId().equals(item.getId()))
                        existe = true;
                if(!existe)
                    items.add(item);
            }
        }
        return items;
    }
    
    public void actionChangeDates() {
        try {
            this.initialDate = changeInitialDate(timeCourse);
            this.finalDate = changeFinalDate(timeCourse);

            PrimeFaces.current().ajax().update(":lume:initialDate");
            PrimeFaces.current().ajax().update(":lume:finalDate");
        } catch (Exception e) {
            log.error("Erro no actionTrocaDatasCriacao", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
        }
    }
    
    private Date changeInitialDate(String timeCourse) {
        Date dataInicio = null;
        try {
            Calendar c = Calendar.getInstance();
            if ("O".equals(timeCourse)) { // Mês anterior
                c.add(Calendar.MONTH, -1);
                dataInicio = c.getTime();
            } else if ("H".equals(timeCourse)) { //Ultimos 3 meses                
                c.add(Calendar.MONTH, -3);
                dataInicio = c.getTime();
            } else if ("A".equals(timeCourse)) { //Ano atual       
                c.set(Calendar.MONTH, 0);
                c.set(Calendar.DAY_OF_MONTH, 1);
                dataInicio = c.getTime();
            } else if ("M".equals(timeCourse)) { //Mês Atual              
                c.set(Calendar.DAY_OF_MONTH, 1);
                dataInicio = c.getTime();
            } else if ("I".equals(timeCourse)) { //Ultimos 6 meses            
                c.add(Calendar.MONTH, -6);
                dataInicio = c.getTime();
            }
            return dataInicio;
        } catch (Exception e) {
            log.error("Erro no changeInitialDate", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            return null;
        }
    }
    
    private Date changeFinalDate(String timeCourse) {
        Date dataFim = null;
        try {
            Calendar c = Calendar.getInstance();
            if ("O".equals(timeCourse)) {
                c.add(Calendar.DAY_OF_MONTH, -1);
                dataFim = c.getTime();
            } else if("A".equals(timeCourse)) {
                c.set(Calendar.MONTH, 11);
                c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH));
                dataFim = c.getTime();
            }else if("M".equals(timeCourse)) {
                c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH));
                dataFim = c.getTime();
            } else if (timeCourse == null) {
                dataFim = null;
            } else {
                dataFim = c.getTime();
            }
            return dataFim;
        } catch (Exception e) {
            log.error("Erro no changeFinalDate", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            return null;
        }
    }
    
    public void generateChart() {
        lineModel = new BarChartModel();
        ChartData data = new ChartData();

        List<Number> valuesAPagar = new ArrayList<>(), valuesAReceber = new ArrayList<>(), valuesPagos = new ArrayList<>(), valuesRecebidos = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        Mes mesAtual = Mes.getMesAtual();
        int mesAtualIdx = Arrays.asList(Mes.values()).indexOf(mesAtual), anoAtual = Calendar.getInstance().get(Calendar.YEAR);
        for (int i = 0; i < 12; i++) {
            BigDecimal valor;

//            valor = LancamentoSingleton.getInstance().getBo().listContasAPagarValor(empresaLogada.getConta(), mesAtual, anoAtual, ValidacaoLancamento.NAO_VALIDADO);
//            valuesAPagar.add((valor == null ? 0d : valor.doubleValue()));
//
//            valor = LancamentoSingleton.getInstance().getBo().listContasAPagarValor(empresaLogada.getConta(), mesAtual, anoAtual, ValidacaoLancamento.VALIDADO);
//            valuesPagos.add((valor == null ? 0d : valor.doubleValue()));
//
//            valor = LancamentoSingleton.getInstance().getBo().listContasAReceberValor(empresaLogada.getConta(), mesAtual, anoAtual, ValidacaoLancamento.NAO_VALIDADO);
//            valuesAReceber.add((valor == null ? 0d : valor.doubleValue()));
//
//            valor = LancamentoSingleton.getInstance().getBo().listContasAReceberValor(empresaLogada.getConta(), mesAtual, anoAtual, ValidacaoLancamento.VALIDADO);
//            valuesRecebidos.add((valor == null ? 0d : valor.doubleValue()));

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
    }

    public DashboardModel getDashboardModel() {
        return dashboardModel;
    }

    public void setDashboardModel(DashboardModel dashboardModel) {
        this.dashboardModel = dashboardModel;
    }

    public BigDecimal getReceiveCurrentMonth() {
        return receiveCurrentMonth;
    }

    public void setReceiveCurrentMonth(BigDecimal receiveCurrentMonth) {
        this.receiveCurrentMonth = receiveCurrentMonth;
    }

    public BigDecimal getSubscribers() {
        return subscribers;
    }

    public void setSubscribers(BigDecimal subscribers) {
        this.subscribers = subscribers;
    }

    public BigDecimal getPendingReceive() {
        return pendingReceive;
    }

    public void setPendingReceive(BigDecimal pendingReceive) {
        this.pendingReceive = pendingReceive;
    }

    public String getTimeCourse() {
        return timeCourse;
    }

    public void setTimeCourse(String timeCourse) {
        this.timeCourse = timeCourse;
    }

    public Date getInitialDate() {
        return initialDate;
    }

    public void setInitialDate(Date initialDate) {
        this.initialDate = initialDate;
    }

    public Date getFinalDate() {
        return finalDate;
    }

    public void setFinalDate(Date finalDate) {
        this.finalDate = finalDate;
    }

    public DataTable getTableReport() {
        return tableReport;
    }

    public void setTableReport(DataTable tableReport) {
        this.tableReport = tableReport;
    }

    public List<ItemResponse> getInvoices() {
        return invoices;
    }

    public void setInvoices(List<ItemResponse> invoices) {
        this.invoices = invoices;
    }

    public InvoiceResponse getInvoicesResponse() {
        return invoicesResponse;
    }

    public void setInvoicesResponse(InvoiceResponse invoicesResponse) {
        this.invoicesResponse = invoicesResponse;
    }

    public BigDecimal getTotalInvoices() {
        return totalInvoices;
    }

    public void setTotalInvoices(BigDecimal totalInvoices) {
        this.totalInvoices = totalInvoices;
    }

    public BigDecimal getTotalPaidInvoices() {
        return totalPaidInvoices;
    }

    public void setTotalPaidInvoices(BigDecimal totalPaidInvoices) {
        this.totalPaidInvoices = totalPaidInvoices;
    }

}
