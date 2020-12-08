package br.com.lume.common.util;

import java.util.ArrayList;
import java.util.List;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

public class MessagesManager {

    private static MessagesManager instance;
    
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
    }
    
    public void smsReceive() {
        
    }
    
}
