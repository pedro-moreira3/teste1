package br.com.lume.common.util;

import java.util.Arrays;

import javax.sound.midi.Soundbank;

import com.twilio.Twilio;
import com.twilio.base.ResourceSet;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.rest.conversations.v1.configuration.Webhook;
import com.twilio.type.PhoneNumber;

public class MessagesManager {

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
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
        com.twilio.rest.conversations.v1.conversation.Webhook webhook = 
                com.twilio.rest.conversations.v1.conversation.Webhook.creator(
                sidEnv,
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
    
    public void receiveMsgs() {
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
        ResourceSet<com.twilio.rest.conversations.v1.conversation.Webhook> webhooks =
                com.twilio.rest.conversations.v1.conversation.Webhook.reader(sidWebhook)
            .limit(20)
            .read();

        for(com.twilio.rest.conversations.v1.conversation.Webhook record : webhooks) {
            System.out.println("------- REGISTRO ---------- ");
            System.out.println("SID: " + record.getSid());
            System.out.println("TARGET: " + record.getTarget() + "\n");
        }
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
