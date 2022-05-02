package br.com.lume.common.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class MessagesReceive extends HttpServlet{
    
    private static final long serialVersionUID = 1L;
    
    public static final String MESSAGES = "messages";
    public static final String STATUS_MESSAGES = "statusCallback";

    public MessagesReceive() {
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
     //   BufferedReader bff = null;
 //       try {
            String servletPath = req.getServletPath();
        //    StringBuilder sb = new StringBuilder();
            System.out.println("- Recebendo msg -");
            
            System.out.println("query string: " + req.getQueryString());
            
            
            BufferedReader br = new BufferedReader(new InputStreamReader(req.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line+"\n");
            }
            br.close();
            System.out.println("sb: " + sb.toString());
            
//            
//            ToCountry=BR
//             ToState=MS
//            SmsMessageSid=SM7a4b178c29ec279b8df9c11dca856b76
//            NumMedia=0
//            ToCity=Mato+Grosso+do+Sul
//            &FromZip=
//            &SmsSid=SM7a4b178c29ec279b8df9c11dca856b76
//            &FromState=RJ
//            &SmsStatus=received
//            &FromCity=Rio+de+Janeiro
//            &Body=Teste
//            &FromCountry=BR&To=%2B5567933007506
//            &MessagingServiceSid=MG69f9930787fe30fd5ae8acdf42c9b3bd
//            &ToZip=
//            &NumSegments=1&MessageSid=SM7a4b178c29ec279b8df9c11dca856b76&AccountSid=ACf95921d69a6b7507b84009f0f818b319&From=%2B5521974985639&ApiVersion=2010-04-01

            
            
            
//            bff = new BufferedReader(new InputStreamReader(req.getInputStream()));
//            
//            
//            System.out.println(req.getInputStream());
//            
//            while (bff.ready()) {
//                System.out.println("linha do buffer: " + bff.readLine());
//                sb.append(bff.readLine());
//                System.out.println(sb.toString());
//            }
//            
//            bff.close();
        
            
//            // TODO Implementar as classes referentes ao tratamento do JSON para realizar a conversão.
      //      Gson gson = new Gson();
     //       Object objeto = gson.fromJson(sb.toString(), Object.class);
            
            switch (servletPath) {
                case MESSAGES: {
//                  HistoricoMensagemIntegracaoSingleton historicoIntegracao = HistoricoMensagemIntegracaoSingleton
//                          .getInstance();
    
               //     System.out.println(sb.toString());
                    
                    // TODO Implementar o preenchimento do historico com os dados pertinente ao
                    // paciente
                    // historicoIntegracao.criaHistorico(paciente, agendamento, msgTemplate,
                    // metodoEnvio, TipoEnvio.ENVIO, profissionalLogado, null, null,
                    // sidMsg);
                }break;
                case STATUS_MESSAGES: {
                 //   System.out.println(sb.toString());
                }break;
                default: {
                    
              //      System.out.println(sb.toString());
                }
            }

            
            resp.setContentType("text/html");
            resp.setStatus(HttpServletResponse.SC_OK); 
            
    }
    
}
