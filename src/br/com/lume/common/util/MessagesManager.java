package br.com.lume.common.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.datatype.jsr310.ser.ZonedDateTimeSerializer;
import com.twilio.Twilio;
import com.twilio.base.ResourceSet;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
//import static spark.Spark.*;

import br.com.lume.common.bo.IEnumController;

public class MessagesManager extends HttpServlet{

    private static final long serialVersionUID = -5113750481940092741L;
    
    private static MessagesManager instance;
    
    public static final String ACCOUNT_SID = "ACf95921d69a6b7507b84009f0f818b319";
    public static final String AUTH_TOKEN = "fb650d8f11c74d7af01731b88ea16cae";
    
    public static final String MESSAGES = "messages";
    public static final String STATUS_MESSAGES = "statusCallback";
    
    private MessagesManager() {
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
    }
    
    public static MessagesManager getInstance() {
        if(instance != null) {
            return instance;
        }else {
            instance = new MessagesManager();
            return instance;
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(req, resp);
    }
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(req, resp);
    }
    
    public void processRequest(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {        
        String servletPath = req.getServletPath();
        
        System.out.println(servletPath);
        
        BufferedReader bff = new BufferedReader(new InputStreamReader(req.getInputStream()));
        while(bff.ready()) {
            System.out.println(bff.readLine());
        }
        
//        switch(servletPath) {
//            case MESSAGES:{
//                BufferedReader bff = new BufferedReader(new InputStreamReader(req.getInputStream()));
//                while(bff.ready()) {
//                    System.out.println(bff.readLine());
//                }
//            }break;
//            case STATUS_MESSAGES:{
//                BufferedReader bff = new BufferedReader(new InputStreamReader(req.getInputStream()));
//                while(bff.ready()) {
//                    System.out.println(bff.readLine());
//                }
//            }break;
//            default:{
//                BufferedReader bff = new BufferedReader(new InputStreamReader(req.getInputStream()));
//                while(bff.ready()) {
//                    System.out.println(bff.readLine());
//                }
//            }
//        }
    }

    public String messageSender(String fromNumber, String toNumber, String mensagem) {
        Message message = Message.creator(
                new PhoneNumber("whatsapp:"+toNumber),
                new PhoneNumber("whatsapp:+14155238886"),
                mensagem)
            .create();
        
        return message.getSid();
    }
    
    public Message messageFind(String sid) {
        Message msg = Message.fetcher(sid)
                .fetch();
        return msg;
    }
    
    public ResourceSet<Message> messagesList(){
        ResourceSet<Message> messages = Message.reader().limit(20).read();
        return messages;
    }
    
    public ResourceSet<Message> messagesList(String fromNumber, String toNumber, int year, int month, int dayMonth, int hour, int limit){
        ResourceSet<Message> messages = Message.reader()
                .setDateSent(
                    ZonedDateTime.of(year, month, dayMonth, hour, 0, 0, 0, ZoneId.of("UTC")))
                .setFrom(new com.twilio.type.PhoneNumber(fromNumber))
                .setTo(new com.twilio.type.PhoneNumber(toNumber))
                .limit(limit)
                .read();
            
        return messages;
    }
}
