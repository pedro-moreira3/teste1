package br.com.lume.odonto.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/report.pdf")
public class PdfReportServlet extends HttpServlet {

    /**
     *
     */
    private static final long serialVersionUID = 4820938928759476494L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        /*
         * File f = new File("C:\\Users\\jeferson.luchtenberg\\Downloads\\TESTE.pdf"); FileInputStream fis = new FileInputStream(f); byte content[] = new byte[(int) f.length()]; fis.read(content);
         */
        byte[] content = (byte[]) request.getSession().getAttribute("reportBytes");
        if (content != null) {
            response.setContentType("application/pdf");
            response.setContentLength(content.length);
            response.getOutputStream().write(content);
        }
    }
}
