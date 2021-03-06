package br.com.lume.odonto.managed;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.apache.log4j.Logger;
import org.primefaces.PrimeFaces;

import br.com.lume.common.bo.FaturasIuguBO;
import br.com.lume.common.iugu.Iugu;
import br.com.lume.common.iugu.model.FaturasIugu;
import br.com.lume.common.iugu.model.SubItem;
import br.com.lume.common.iugu.model.Subscription;
import br.com.lume.common.iugu.responses.InvoiceResponse;
import br.com.lume.common.iugu.responses.ItemResponse;
import br.com.lume.common.iugu.responses.SubscriptionResponse;
import br.com.lume.common.iugu.service.SubscriptionService;
import br.com.lume.common.log.LogUtils;
import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.JSFHelper;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.Utils;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.faturasiugu.FaturasIuguSingleton;
import br.com.lume.security.EmpresaSingleton;
import br.com.lume.security.bo.EmpresaBO;
import br.com.lume.security.entity.Empresa;

@ManagedBean
@ViewScoped
public class MensalMB extends LumeManagedBean<Empresa> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(MensalMB.class);
    private List<InvoiceResponse> faturas;
    private SubscriptionResponse assinatura;
    private Date dataVencimentoAntiga;

    public MensalMB() {
        super(EmpresaSingleton.getInstance().getBo());
        this.setClazz(Empresa.class);
        carregarTela();
    }

    private void carregarTela() {
//        if(UtilsFrontEnd.getEmpresaLogada().getIdIugu() != null && !UtilsFrontEnd.getEmpresaLogada().getIdIugu().isEmpty()) {
            
            this.carregarSubscriptionResponse();
//        }
    }
    
    public void gerarSegundaViaFatura() {
        try {
            System.out.println("vai buscar assinatura");
            assinatura = new SubscriptionService().find(UtilsFrontEnd.getEmpresaLogada().getAssinaturaIuguBanco());
            System.out.println("buscou assinatura OK");
            dataVencimentoAntiga = Utils.stringToDate(assinatura.getCreatedAt(), "yyyy-MM-dd");
            System.out.println(dataVencimentoAntiga);
            Date agora = new Date();
            int diff = (int) ((agora.getTime() - Utils.stringToDate(converteDataIugu(faturas.get(0).getDueDate()), "dd-MM-yyyy").getTime())
                              / (1000 * 60 * 60 * 24));
            if (diff > 23) {
                System.out.println("entrou gerar 2 faturas");
                SubItem subItem = new SubItem(Mensagens.getMensagemOffLine("descricao.iugu.fatura.reativacao"), 1, assinatura.getPriceCents());
                subItem.setRecurrent(false);
                subItem.set_destroy(false);
                Subscription subscription = new Subscription(null);
                subscription.setSubItems(new ArrayList<SubItem>());
                subscription.getSubItems().add(subItem);
                subscription.setTwoStep(false);
                subscription.setSuspendOnInvoiceExpired(true);
                System.out.println("populou objeto para enviar pra api do iugu");
                SubscriptionResponse result = new SubscriptionService().change(assinatura.getId(), subscription);

                if (result != null && result.getSubitems().size() > 0) {
                    System.out.println("retornou ok");
                    ativaAsinatura();
                    salvarFaturaPendente();
                    atualizar();
                    PrimeFaces.current().executeScript("window.open('" + faturas.get(0).getSecureUrl() + "')");
                    addInfo("OK","2?? via de fatura gerada com sucesso!");
                    System.out.println("final fluxo 2 faturas ok");
                } else {
                    System.out.println("retornou sem adicionar subitem");
                    addError("ERRO","Falha ao gerar 2?? via de fatura.");
                }

            } else {
                System.out.println("entrou gerar 1 fatura");
                ativaAsinatura();
                salvarFaturaPendente();
                atualizar();
                PrimeFaces.current().executeScript("window.open('" + faturas.get(0).getSecureUrl() + "')");
                addInfo("OK","2?? via de fatura gerada com sucesso!");
                System.out.println("final fluxo 1 fatura ok");
            }
        } catch (Exception e) {
            e.printStackTrace();
            addError("ERRO","Falha ao gerar 2?? via de fatura.");
        }

    }
    
    private void ativaAsinatura() throws Exception {
        System.out.println("chamou altera cobran??a");
        Iugu.getInstance().alteraCobranca(UtilsFrontEnd.getEmpresaLogada().getAssinaturaIuguBanco(), new Date(), true);
        System.out.println("altera cobranca OK");
        System.out.println("chamou ativa assinatura");
        Iugu.getInstance().ativaAssinatura(UtilsFrontEnd.getEmpresaLogada().getAssinaturaIuguBanco());
        System.out.println("ativa assinatura OK");
//        getEntity().getAssinaturaIugu(Boolean.TRUE);

    }
    
    private void salvarFaturaPendente() throws Exception {
        this.carregarSubscriptionResponse();
        System.out.println("chamou salvar fatura no banco");
        InvoiceResponse ultimaGerada = faturas.get(0);
        FaturasIugu fatura = new FaturasIugu();
        fatura.setEmpresa(UtilsFrontEnd.getEmpresaLogada());
        fatura.setIdClienteIugu(assinatura.getCustomerId());
        fatura.setIdAssinaturaIugu(assinatura.getId());
        fatura.setIdFaturaIugu(ultimaGerada.getId());
        fatura.setValorTotal(ultimaGerada.getTotalCents());
        fatura.setStatus(ultimaGerada.getStatus());

        fatura.setUltimoVencimentoAssinatura(dataVencimentoAntiga);
        System.out.println(fatura.getUltimoVencimentoAssinatura());
        fatura.setVencimentoFatura(Utils.stringToDate(ultimaGerada.getDueDate(), "yyyy-MM-dd"));
        System.out.println(fatura.getVencimentoFatura());

        System.out.println("populou objeto para salvar");
        FaturasIuguSingleton.getInstance().getBo().persist(fatura);
        System.out.println("salvou OK");
    }
    
    public void carregarSubscriptionResponse() {
        faturas = new ArrayList<InvoiceResponse>();
        faturas = Iugu.getInstance().atualizaFaturas(UtilsFrontEnd.getEmpresaLogada().getAssinaturaIuguBanco());
        reloadViewSub();
    }
    
    public void atualizar() {
        System.out.println("chamou atualizar()");
        carregarSubscriptionResponse();
        try {
            if (!Iugu.getInstance().isSuspenso(UtilsFrontEnd.getEmpresaLogada().getAssinaturaIuguBanco())) {
                UtilsFrontEnd.getEmpresaLogada().setBloqueado("N");
                new EmpresaBO().persist(UtilsFrontEnd.getEmpresaLogada());
                JSFHelper.redirect("home.jsf");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static String converteDataIugu(String data) throws Exception {
        String split[] = new String[3];
        split = data.split("-");
        return split[2] + "-" + split[1] + "-" + split[0];
    }

    
    public List<InvoiceResponse> getFaturas() {
        return faturas;
    }

    
    public void setFaturas(List<InvoiceResponse> faturas) {
        this.faturas = faturas;
    }

    


}
