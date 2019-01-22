package br.com.lume.common.util;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class EnviaEmailThread implements Runnable {

    String from, to, subject, content, host, smtpPort;

    public EnviaEmailThread(String from, String to, String subject, String content, String host, String smtpPort) throws AddressException, MessagingException {
        this.from = from;
        this.to = to;
        this.subject = subject;
        this.content = content;
        this.host = host;
        this.smtpPort = smtpPort;
    }

    @Override
    public void run() {
        try {
            Properties props = new Properties();
            props.put("mail.smtp.host", Mensagens.getMensagem("email.smtpHost.prod"));
            props.put("mail.smtp.port", smtpPort);
            to += ";faruk.zahra@lumetec.com.br;atendimento@lumetec.com.br";
            // Session session = Session.getDefaultInstance(props, null);
            Session session = Session.getDefaultInstance(props, null);
            Message msg = new MimeMessage(session);
            if (from == null) {
                msg.setFrom(new InternetAddress(Mensagens.getMensagem("email.from")));
            } else {
                msg.setFrom(new InternetAddress(from));
            }
            if (to.contains(";")) {
                String[] split = to.split(";");
                InternetAddress[] emails = new InternetAddress[split.length];
                for (int i = 0; i < split.length; i++) {
                    emails[i] = new InternetAddress(split[i]);
                }
                msg.addRecipients(Message.RecipientType.TO, emails);
            } else {
                msg.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
            }
            msg.setSubject(subject);
            msg.setContent(content, "text/html; charset=utf-8");
            Transport.send(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
