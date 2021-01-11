package br.com.lume.common.util;

import java.io.IOException;
import java.util.Arrays;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sound.midi.Soundbank;

import com.twilio.Twilio;
import com.twilio.base.ResourceSet;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.twiml.messaging.*;
import com.twilio.type.PhoneNumber;
import com.twilio.twiml.MessagingResponse;
import com.twilio.twiml.messaging.Body;
//import static spark.Spark.*;

public class MessagesManager extends HttpServlet{

    private static MessagesManager instance;
    private String sidWebhook;
    private String sidEnv;
    
    public static final String ACCOUNT_SID = "ACf95921d69a6b7507b84009f0f818b319";
    public static final String AUTH_TOKEN = "fb650d8f11c74d7af01731b88ea16cae";
    
    private MessagesManager() {
    }
    
    public static MessagesManager getInstance() {
        if(instance != null) {
            return instance;
        }else {
            instance = new MessagesManager();
            return instance;
        }
    }

    public void smsSender() {
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
        Message message = Message.creator(
                new PhoneNumber("whatsapp:+554199473590"),
                new PhoneNumber("whatsapp:+14155238886"),
                "Teste Twilio")
            .create();

        System.out.println(message.getSid());
        sidEnv = message.getSid();
    }
    
    public void configurationWebhook() {
//        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
//        Webhook webhook = Webhook.updater()
//            .setFilters(
//                Arrays.asList("onMessageAdd", "onMessageUpdate", "onMessageRemove"))
//            .setTarget(Webhook.Target.WEBHOOK)
//            .setPreWebhookUrl("https://dev-intelidente.lumetec.com.br/webhook")
//            .setPostWebhookUrl("https://dev-intelidente.lumetec.com.br/webhook")
//            .setMethod("POST")
//            .update();

    }

    public void configurationConversation() {
        
        Body body = new Body.Builder("TESTE").build();
        com.twilio.twiml.messaging.Message sms = new com.twilio.twiml.messaging.Message.Builder().body(body).build();
        
        
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
        com.twilio.rest.conversations.v1.conversation.Webhook webhook = 
                com.twilio.rest.conversations.v1.conversation.Webhook.creator("chxxx",
                com.twilio.rest.conversations.v1.conversation.Webhook.Target.WEBHOOK)
            .setConfigurationMethod(com.twilio.rest.conversations.v1.conversation.Webhook.Method.GET)
            .setConfigurationFilters(
                Arrays.asList("onMessageAdded", "onConversationAdded", "onConversationRemoved", "onConversationUpdate", 
                        "onMessageAdd", "onMessageAdded"))
            .setConfigurationUrl("https://dev-intelidente.lumetec.com.br/webhook")
            .create();

        System.out.println("--------------- configurationConversation ----------------");
        System.out.println(webhook.getSid());
        sidWebhook = webhook.getSid();
    }
    
    public void service(HttpServletRequest request, HttpServletResponse response) throws IOException {        
        System.out.println("------------Entrou aqui---------");
        Body body = new Body
              .Builder("Resposta teste!")
              .build();
      
        com.twilio.twiml.messaging.Message sms = new com.twilio.twiml.messaging.Message
              .Builder()
              .body(body)
              .build();
        MessagingResponse twiml = new MessagingResponse
              .Builder()
              .message(sms)
              .build();

        System.out.println(twiml.toXml());
        response.setContentType("text/xml");
        response.getWriter().print(twiml.toXml());
      }
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println("------------Entrou aqui---------");
        Body body = new Body
              .Builder("Resposta teste!")
              .build();
      
        com.twilio.twiml.messaging.Message sms = new com.twilio.twiml.messaging.Message
              .Builder()
              .body(body)
              .build();
        MessagingResponse twiml = new MessagingResponse
              .Builder()
              .message(sms)
              .build();

        System.out.println(twiml.toXml());
        resp.setContentType("text/xml");
        resp.getWriter().print(twiml.toXml());
        super.doPost(req, resp);
    }
    
    public void receiveMsgs() {
//        get("/", (req, res) -> "Olá");
//
//        post("/sms", (req, res) -> {
//            res.type("application/xml");
//            Body body = new Body
//                    .Builder("Resposta teste!")
//                    .build();
//            com.twilio.twiml.messaging.Message sms = new com.twilio.twiml.messaging.Message
//                    .Builder()
//                    .body(body)
//                    .build();
//            MessagingResponse twiml = new MessagingResponse
//                    .Builder()
//                    .message(sms)
//                    .build();
//            return twiml.toXml();
//        });
    }
    
    public void listAllMessagesInHistory() {
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
        ResourceSet<Message> messages = Message.reader().limit(20).read();

        System.out.println("------------- Listagem do histórico ----------");
        for(Message record : messages) {
            System.out.println(record.getSid());
        }
    }
}
