package br.com.lume.odonto.managed;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.faces.event.ActionEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

import org.primefaces.PrimeFaces;

import br.com.lume.common.iugu.Iugu;
import br.com.lume.common.iugu.objects.NewItem;
import br.com.lume.common.iugu.responses.InvoiceResponse;
import br.com.lume.common.iugu.responses.SubItemResponse;
import br.com.lume.common.iugu.responses.SubscriptionResponse;
import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.UtilsPadraoRelatorio;
import br.com.lume.common.util.UtilsPadraoRelatorio.PeriodoBusca;
import br.com.lume.security.EmpresaSingleton;
import br.com.lume.security.bo.EmpresaBO;
import br.com.lume.security.entity.Empresa;


@Named
@ViewScoped
public class GerenciamentoIuguMB extends LumeManagedBean<Empresa> implements Serializable {

    private static final long serialVersionUID = 1L;
    private PeriodoBusca periodoUltimoPagamento;
    private Date inicioUltimoPagamento, fimUltimoPagamento;
    private String filtroTexto, iuguSuspenso;
    private Date alteraAssinaturaDataVencimento, dataVencimentoSegVia;
    private Boolean suspended;
    private InvoiceResponse invoiceSelected;

    // New Item
    private NewItem newItemSubscription = new NewItem();

    // New Invoice
    private String newInvoiceEmail;
    private Date newInvoiceDataVencimento;
    private List<NewItem> newInvoiceItems;
    private NewItem newInvoiceNewItem = new NewItem();

    public GerenciamentoIuguMB() {
        super(EmpresaSingleton.getInstance().getBo());
        this.setClazz(Empresa.class);      
    }

    public void carregaLista() {
        try {
            
            
            
            setEntityList(EmpresaSingleton.getInstance().getBo().findClientesByFilterIUGU(filtroTexto, iuguSuspenso));
            if (inicioUltimoPagamento != null || fimUltimoPagamento != null) {
                getEntityList().removeIf(cliente -> {
                    if (EmpresaSingleton.getInstance().getUltimoPagamento(cliente) == null)
                        return true;

                    if (inicioUltimoPagamento != null && EmpresaSingleton.getInstance().getUltimoPagamento(cliente).before(inicioUltimoPagamento))
                        return true;
                    if (fimUltimoPagamento != null && EmpresaSingleton.getInstance().getUltimoPagamento(cliente).after(fimUltimoPagamento))
                        return true;

                    return false;
                });
            }
        } catch (Exception e) {
           e.printStackTrace();
           this.addError("Erro ao filtrar", "");
        }
    }

    public void actionTrocaDatasUltimoPagamento() {
        try {
            setInicioUltimoPagamento(UtilsPadraoRelatorio.getDataInicio(this.periodoUltimoPagamento));
            setFimUltimoPagamento(UtilsPadraoRelatorio.getDataFim(this.periodoUltimoPagamento));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void visualizarAssinatura(Empresa empresa) {
        setEntity(empresa);
        this.alteraAssinaturaDataVencimento = EmpresaSingleton.getInstance().getAssinaturaIugu(empresa).getExpiresAtDate();
        this.suspended = EmpresaSingleton.getInstance().getAssinaturaIugu(empresa).getSuspended().booleanValue();
    }

    public void removerItem(SubItemResponse item) {
        try {
            Iugu.getInstance().removeItemAssinatura(EmpresaSingleton.getInstance().getAssinaturaIugu(getEntity()).getId(), item.getId());
            this.addInfo("Item removido com sucesso","");
        } catch (Exception e) {
            e.printStackTrace();
            this.addError("Falha ao remover o item. " + e.getMessage(),"");
        }
    }

    public void actionAlteraCobranca() {
        try {
            Iugu.getInstance().alteraCobranca(getEntity().getAssinaturaIuguBanco(), this.alteraAssinaturaDataVencimento, this.suspended);    
            this.addInfo("Cobrança alterada com sucesso","");
        } catch (Exception e) {
            e.printStackTrace();
            this.addError("Falha ao alterar a data de vencimento. " + e.getMessage(),"");
        }
    }

    public void cancelaFatura(InvoiceResponse fatura) {
        try {
            Iugu.getInstance().cancelaFatura(fatura.getId());
            this.addInfo("Fatura cancelada com sucesso","");
        } catch (Exception e) {
            e.printStackTrace();
            this.addError("Falha ao cancelar a fatura. " + e.getMessage(),"");
        }
    }

    public void actionAtivaAssinatura() {
        try {
            Iugu.getInstance().ativaAssinatura(getEntity().getAssinaturaIuguBanco());            

            this.addInfo("Assinatura ativada com sucesso","");
        } catch (Exception e) {
            e.printStackTrace();
            this.addError("Falha ao ativar a assinatura. " + e.getMessage(),"");
        }
    }

    public void actionStartSegundaVia(InvoiceResponse fatura) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 1);
        this.dataVencimentoSegVia = cal.getTime();
        this.invoiceSelected = fatura;
    }

    public void actionSegundaVia() {
        try {
            if (!"Pendente".equals(this.invoiceSelected.getStatus()))
                throw new Exception("Última fatura não está Pendente!");
            Iugu.getInstance().geraSegundaVia(this.invoiceSelected.getId(), this.dataVencimentoSegVia);

            PrimeFaces.current().executeScript("PF('dlgDataVencimentoSegVia').hide()");
            this.addInfo("Segunda via emitida com sucesso","");
        } catch (Exception e) {
            e.printStackTrace();
            this.addError("Falha ao gerar segunda via. " + e.getMessage(),"");
        }
    }

    public void visualizaHistoricoFatura(InvoiceResponse fatura) {
        this.invoiceSelected = fatura;
    }

    public void actionNovoItem() {
        this.newItemSubscription = new NewItem();
    }

    public void actionNewInvoiceNovoItem() {
        this.newInvoiceNewItem = new NewItem();
    }

    public void actionPersistItem() {
        try {
            Iugu.getInstance().createNewItem(getEntity().getAssinaturaIuguBanco(), this.newItemSubscription);
           
            PrimeFaces.current().executeScript("PF('dlgNewItemAssinatura').hide()");

            this.addInfo("Item criado com sucesso","");
        } catch (Exception e) {
            e.printStackTrace();
            this.addError("Falha ao criar novo item. " + e.getMessage(),"");
        }
    }

    public SubscriptionResponse assinaturaIugu(Empresa empresa) {
        return EmpresaSingleton.getInstance().getAssinaturaIugu(empresa);
    }
    
    public String statusIuguStr(Empresa empresa) {
        if (EmpresaSingleton.getInstance().getAssinaturaIugu(empresa) != null)
            return (EmpresaSingleton.getInstance().getAssinaturaIugu(empresa).getSuspended().booleanValue() ? "Bloqueado" : "Ativo");
        return null;
    }
    
    public String statusUltimaFatura(Empresa empresa) {       
        return EmpresaSingleton.getInstance().getStatusUltimaFatura(empresa);
    }
    
    public Date ultimoPagamento(Empresa empresa) {
        return EmpresaSingleton.getInstance().getUltimoPagamento(empresa);
    }  
    
    public void actionNewInvoice() {
        this.newInvoiceEmail = EmpresaSingleton.getInstance().getAssinaturaIugu(getEntity()).getCustomerEmail();
        this.newInvoiceDataVencimento = null;
        this.newInvoiceItems = new ArrayList<>();
    }

    public void actionNewInvoiceDelItem(ActionEvent event) {
        NewItem param = (NewItem) event.getComponent().getAttributes().get("item");
        this.newInvoiceItems.remove(param);
    }

    public void actionNewInvoiceSaveItem() {
        this.newInvoiceItems.add(this.newInvoiceNewItem);
        this.newInvoiceNewItem = new NewItem();
        PrimeFaces.current().executeScript("PF('dlgNewItemNewInvoice').hide()");
    }

    public void actionPersistNewInvoice() {
        try {
            Iugu.getInstance().createNewInvoice(EmpresaSingleton.getInstance().getAssinaturaIugu(getEntity()).getId(), 
                    EmpresaSingleton.getInstance().getAssinaturaIugu(getEntity()).getCustomerId(), this.newInvoiceDataVencimento, this.newInvoiceEmail,
                    this.newInvoiceItems);
           
            PrimeFaces.current().executeScript("PF('dlgNewInvoice').hide()");

            this.addInfo("Fatura criada com sucesso","");
        } catch (Exception e) {
            e.printStackTrace();
            this.addError("Falha ao criar nova Fatura. " + e.getMessage(),"");
        }
    }

    public PeriodoBusca getPeriodoUltimoPagamento() {
        return periodoUltimoPagamento;
    }

    public void setPeriodoUltimoPagamento(PeriodoBusca periodoUltimoPagamento) {
        this.periodoUltimoPagamento = periodoUltimoPagamento;
    }

    public Date getInicioUltimoPagamento() {
        return inicioUltimoPagamento;
    }

    public void setInicioUltimoPagamento(Date inicioUltimoPagamento) {
        this.inicioUltimoPagamento = inicioUltimoPagamento;
    }

    public Date getFimUltimoPagamento() {
        return fimUltimoPagamento;
    }

    public void setFimUltimoPagamento(Date fimUltimoPagamento) {
        this.fimUltimoPagamento = fimUltimoPagamento;
    }

    public String getFiltroTexto() {
        return filtroTexto;
    }

    public void setFiltroTexto(String filtroTexto) {
        this.filtroTexto = filtroTexto;
    }

    public String getIuguSuspenso() {
        return iuguSuspenso;
    }

    public void setIuguSuspenso(String iuguSuspenso) {
        this.iuguSuspenso = iuguSuspenso;
    }

    public Date getAlteraAssinaturaDataVencimento() {
        return alteraAssinaturaDataVencimento;
    }

    public void setAlteraAssinaturaDataVencimento(Date alteraAssinaturaDataVencimento) {
        this.alteraAssinaturaDataVencimento = alteraAssinaturaDataVencimento;
    }

    public NewItem getNewItemSubscription() {
        return newItemSubscription;
    }

    public void setNewItemSubscription(NewItem newItemSubscription) {
        this.newItemSubscription = newItemSubscription;
    }

    public Boolean getSuspended() {
        return suspended;
    }

    public void setSuspended(Boolean suspended) {
        this.suspended = suspended;
    }

    public Date getDataVencimentoSegVia() {
        return dataVencimentoSegVia;
    }

    public void setDataVencimentoSegVia(Date dataVencimentoSegVia) {
        this.dataVencimentoSegVia = dataVencimentoSegVia;
    }

    public InvoiceResponse getInvoiceSelected() {
        return invoiceSelected;
    }

    public void setInvoiceSelected(InvoiceResponse invoiceSelected) {
        this.invoiceSelected = invoiceSelected;
    }

    public String getNewInvoiceEmail() {
        return newInvoiceEmail;
    }

    public void setNewInvoiceEmail(String newInvoiceEmail) {
        this.newInvoiceEmail = newInvoiceEmail;
    }

    public Date getNewInvoiceDataVencimento() {
        return newInvoiceDataVencimento;
    }

    public void setNewInvoiceDataVencimento(Date newInvoiceDataVencimento) {
        this.newInvoiceDataVencimento = newInvoiceDataVencimento;
    }

    public List<NewItem> getNewInvoiceItems() {
        return newInvoiceItems;
    }

    public void setNewInvoiceItems(List<NewItem> newInvoiceItems) {
        this.newInvoiceItems = newInvoiceItems;
    }

    public NewItem getNewInvoiceNewItem() {
        return newInvoiceNewItem;
    }

    public void setNewInvoiceNewItem(NewItem newInvoiceNewItem) {
        this.newInvoiceNewItem = newInvoiceNewItem;
    }

}
