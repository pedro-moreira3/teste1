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
                    if (cliente.getUltimoPagamento() == null)
                        return true;

                    if (inicioUltimoPagamento != null && cliente.getUltimoPagamento().before(inicioUltimoPagamento))
                        return true;
                    if (fimUltimoPagamento != null && cliente.getUltimoPagamento().after(fimUltimoPagamento))
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

    public void visualizarAssinatura(Empresa objeto) {
        setEntity(objeto);
        this.alteraAssinaturaDataVencimento = objeto.getAssinaturaIugu().getExpiresAtDate();
        this.suspended = objeto.getAssinaturaIugu().getSuspended().booleanValue();
    }

    public void removerItem(SubItemResponse item) {
        try {
            Iugu.getInstance().removeItemAssinatura(getEntity().getAssinaturaIugu().getId(), item.getId());
            getEntity().getAssinaturaIugu(Boolean.TRUE);

            this.addInfo("Item removido com sucesso","");
        } catch (Exception e) {
            e.printStackTrace();
            this.addError("Falha ao remover o item. " + e.getMessage(),"");
        }
    }

    public void actionAlteraCobranca() {
        try {
            Iugu.getInstance().alteraCobranca(getEntity().getAssinaturaIuguBanco(), this.alteraAssinaturaDataVencimento, this.suspended);
            getEntity().getAssinaturaIugu(Boolean.TRUE);

            this.addInfo("Cobrança alterada com sucesso","");
        } catch (Exception e) {
            e.printStackTrace();
            this.addError("Falha ao alterar a data de vencimento. " + e.getMessage(),"");
        }
    }

    public void cancelaFatura(InvoiceResponse fatura) {
        try {
            Iugu.getInstance().cancelaFatura(fatura.getId());
            getEntity().getAssinaturaIugu(Boolean.TRUE);

            this.addInfo("Fatura cancelada com sucesso","");
        } catch (Exception e) {
            e.printStackTrace();
            this.addError("Falha ao cancelar a fatura. " + e.getMessage(),"");
        }
    }

    public void actionAtivaAssinatura() {
        try {
            Iugu.getInstance().ativaAssinatura(getEntity().getAssinaturaIuguBanco());
            getEntity().getAssinaturaIugu(Boolean.TRUE);

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
            getEntity().getAssinaturaIugu(Boolean.TRUE);

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
            getEntity().getAssinaturaIugu(Boolean.TRUE);
            PrimeFaces.current().executeScript("PF('dlgNewItemAssinatura').hide()");

            this.addInfo("Item criado com sucesso","");
        } catch (Exception e) {
            e.printStackTrace();
            this.addError("Falha ao criar novo item. " + e.getMessage(),"");
        }
    }

    public void actionNewInvoice() {
        this.newInvoiceEmail = getEntity().getAssinaturaIugu().getCustomerEmail();
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
            Iugu.getInstance().createNewInvoice(getEntity().getAssinaturaIugu().getId(), getEntity().getAssinaturaIugu().getCustomerId(), this.newInvoiceDataVencimento, this.newInvoiceEmail,
                    this.newInvoiceItems);
            getEntity().getAssinaturaIugu(Boolean.TRUE);
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
