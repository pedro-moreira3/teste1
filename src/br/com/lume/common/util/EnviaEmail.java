package br.com.lume.common.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

import org.apache.log4j.Logger;

import br.com.lume.odonto.bo.RelatorioGerencialBO;
import br.com.lume.security.bo.EmpresaBO;

public class EnviaEmail {

    public static final String BEM_VINDO = "bemvindo";

    public static final String RESET = "reset";

    public static final String PRIMEIRO_PAGAMENTO = "primeiropagamento";

    public static final String CANCELAMENTO = "cancelamento";

    public static final String ATRASO = "atraso";

    public static final String ACESSO_PACIENTE = "acessopaciente";

    public static final String PLANO_FINALIZANDO = "planofinalizando";

    public static final String AGENDAMENTO_PACIENTE = "agendamentopaciente";

    public static final String RESUMO_DIARIO = "resumodiario";

    public static final String RESUMO_USUARIO = "resumousuario";

    private static Logger log = Logger.getLogger(EnviaEmail.class);

    public static void enviaEmail(String to, String subject, String content) throws Exception {
        enviaEmail(null, to, subject, content);
    }

    public static void enviaEmail(String from, String to, String subject, String content) throws Exception {
        enviaEmail(from, to, subject, content, null, Mensagens.getMensagem("email.smtpPort"));
    }

    public static void enviaEmailOffLine(String from, String to, String subject, String content) throws Exception {
        enviaEmail(from, to, subject, content, Mensagens.getMensagemOffLine("email.smtpHost.prod"), Mensagens.getMensagemOffLine("email.smtpPort"));
    }

    public static void enviaEmailOffLine(String from, String to, String subject, String content, String host) throws Exception {
        enviaEmail(from, to, subject, content, host, Mensagens.getMensagemOffLine("email.smtpPort"));
    }

    public static String buscarTemplate(Map<String, String> valores, String arquivoTemplate) {
        try {
            StringBuilder sb = new StringBuilder();
            BufferedReader bf = new BufferedReader(new InputStreamReader(EnviaEmail.class.getResourceAsStream("templates/" + arquivoTemplate), "UTF-8"));
            while (bf.ready()) {
                sb.append(bf.readLine());
            }
            String conteudo = sb.toString();
            if (valores != null) {
                Set<String> chaves = valores.keySet();
                for (String c : chaves) {
                    String valor = valores.get(c);
                    conteudo = conteudo.replace(c, valor);
                }
            }
            return conteudo;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static void main(String[] args) {
        try {
            Date hoje = Calendar.getInstance().getTime();

            Map<String, String> valores = new HashMap<>();
            RelatorioGerencialBO bo = new RelatorioGerencialBO();

            valores.put("#relatorio", bo.gerarRelatorioEmail(new EmpresaBO().find(41L)));

            //valores.put("#cliente", new EmpresaBO().find(41L).);

            String template = buscarTemplate(valores, EnviaEmail.RESUMO_DIARIO);

            System.out.println(template);

            EnviaEmail.enviaEmailOffLine("no-reply@intelidente.com", "faruk.zahra@lumetec.com.br", "Intelidente - Resumo diário - " + Utils.dateToString(hoje, "dd/MM/yyyy"), template,
                    Mensagens.getMensagemOffLine("email.smtpHost.prod"));
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private static void enviaEmail(String from, String to, String subject, String content, String host, String smtpPort) throws AddressException, MessagingException {
        EnviaEmailThread enviaEmailThread = new EnviaEmailThread(from, to, subject, content, host, smtpPort);
        Thread t = new Thread(enviaEmailThread);
        t.start();
    }
}
