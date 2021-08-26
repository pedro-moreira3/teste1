package br.com.lume.common.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import br.com.lume.common.bo.FaturasIuguBO;
import br.com.lume.common.iugu.Iugu;
import br.com.lume.common.iugu.model.FaturasIugu;
import br.com.lume.security.entity.InvoiceStatusChange;


public class IuguHook extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private FaturasIuguBO faturasIuguBO = new FaturasIuguBO();

    public IuguHook() {
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(req, resp);
    }

    public HttpServletResponse processRequest(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        System.out.println("- RECEBENDO CHAMADA IUGU -");

        BufferedReader br = new BufferedReader(new InputStreamReader(req.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line + "\n");
        }
        br.close();
        System.out.println("sb: " + sb.toString());

        try {
            InvoiceStatusChange invoice = converterObjetoIugu(sb.toString());
            System.out.println("converteu objeto OK");
            if (invoice.getSubscriptionId() != null && invoice.getStatus().equals("paid")) {
                System.out.println("status fatura == pago");
                FaturasIugu fatura = faturasIuguBO.findByFaturaId(invoice.getId());
                System.out.println("buscou se a fatura está salva no banco");
                if (fatura != null) {
                    System.out.println("fatura != null");
                    Date novaDataVencimento = atualizaDataVencimentoAssinatura(fatura.getUltimoVencimentoAssinatura());
                    System.out.println("atualizou data de vencimento para enviar");
                    Iugu.getInstance().alteraCobranca(fatura.getIdAssinaturaIugu(), novaDataVencimento, false);
                    System.out.println("enviou alteração da data OK");
                    atualizaStatusFaturaSalva(fatura, invoice);
                    System.out.println("atualizou o status da fatura no banco");
                    System.out.println("final fluxo de atualizacao de datas no hook do iugu");
                } else {
                    System.out.println("fatura == null");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            resp.setContentType("application/json");
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return resp;
        }

        resp.setContentType("application/json");
        resp.setStatus(HttpServletResponse.SC_OK);
        return resp;

    }

    private void atualizaStatusFaturaSalva(FaturasIugu fatura, InvoiceStatusChange invoiceRecebida) throws Exception {
        fatura.setStatus(invoiceRecebida.getStatus());
        faturasIuguBO.persist(fatura);
    }

    private InvoiceStatusChange converterObjetoIugu(String request) {
        InvoiceStatusChange invoiceChamada = new InvoiceStatusChange();
        String[] split1 = request.split("&");
        for (String s : split1) {
            System.out.println(s);
        }

        String[] split2 = split1[0].split("=");
        invoiceChamada.setEvent(split2[1]);
        System.out.println(invoiceChamada.getEvent());
        split2 = split1[1].split("=");
        invoiceChamada.setId(split2[1]);
        System.out.println(invoiceChamada.getId());
        split2 = split1[2].split("=");
        invoiceChamada.setStatus(split2[1]);
        System.out.println(invoiceChamada.getStatus());
        split2 = split1[3].split("=");
        invoiceChamada.setAccountId(split2[1]);
        System.out.println(invoiceChamada.getAccountId());
        if (split1.length > 4) {
            split2 = split1[4].split("=");
            invoiceChamada.setSubscriptionId(split2[1]);
            System.out.println(invoiceChamada.getSubscriptionId());
        }

        return invoiceChamada;
    }

    private Date atualizaDataVencimentoAssinatura(Date vencimentoAtual) {
        Date hoje = new Date();
        Calendar c1 = Calendar.getInstance();
        Calendar c2 = Calendar.getInstance();
        c1.setTime(hoje);
        c2.setTime(vencimentoAtual);
        c2.set(Calendar.MONTH, c1.get(Calendar.MONTH) + 1);
        c2.set(Calendar.YEAR, c1.get(Calendar.YEAR));
        return c2.getTime();
    }

}
