package br.com.lume.common.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

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
        BufferedReader bff = null;
 //       try {
            String servletPath = req.getServletPath();
            StringBuilder sb = new StringBuilder();
            System.out.println("Recebendo msg");
            
            bff = new BufferedReader(new InputStreamReader(req.getInputStream()));
            
            while (bff.ready()) {
                sb.append(bff.readLine());
                System.out.println(sb.toString());
            }
            
            bff.close();
            resp.setContentType("text/html");
            resp.setStatus(HttpServletResponse.SC_OK); 
            
//            // TODO Implementar as classes referentes ao tratamento do JSON para realizar a
//            // conversão.
//            Gson gson = new Gson();
//            Object objeto = gson.fromJson(sb.toString(), Object.class);
//            
//            switch (servletPath) {
//                case MESSAGES: {
////                  HistoricoMensagemIntegracaoSingleton historicoIntegracao = HistoricoMensagemIntegracaoSingleton
////                          .getInstance();
//    
//                    System.out.println(sb.toString());
//                    
//                    // TODO Implementar o preenchimento do historico com os dados pertinente ao
//                    // paciente
//                    // historicoIntegracao.criaHistorico(paciente, agendamento, msgTemplate,
//                    // metodoEnvio, TipoEnvio.ENVIO, profissionalLogado, null, null,
//                    // sidMsg);
//                }break;
//                case STATUS_MESSAGES: {
//                    System.out.println(sb.toString());
//                }break;
//                default: {
//                    
//                    System.out.println(sb.toString());
//                }
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            if (bff != null)
//                bff.close();
//        }
    }
    
}
