package br.com.lume.common.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Image;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfImage;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.codec.Base64.OutputStream;

import br.com.lume.security.entity.Empresa;

public class DocumentoFactory {

    public static final String PAISAGEM = "P";
    public static final String RETRATO = "R";
    
    public DocumentoFactory() {
    }
    
    public void gerarDocumento(String header, Rectangle layout, String orientacaoPDF, String texto, Empresa empresaLogada) throws DocumentException, IOException {
        
        Document documento = null;
        
        if(orientacaoPDF.equals(PAISAGEM))
            documento = new Document(layout.rotate(), 30, 30, 30, 30);
        else
            documento = new Document(layout, 30, 30, 30, 30);
        
        ByteArrayOutputStream outputData = new ByteArrayOutputStream();
        FileOutputStream fos = new FileOutputStream("../app/odonto/"+empresaLogada.getEmpStrNme()+"/"+header);
        PdfWriter pdfWriter = PdfWriter.getInstance(documento, fos);
        
        documento.open();
        documento.newPage();
        
        documento.addTitle(header);
        
        //Constroi o cabeçalho do documento
        Image image = Image.getInstance("../app/odonto/imagens/"+empresaLogada.getEmpStrLogo());
        PdfImage imagem = new PdfImage(image, "Logo", null);
        
        documento.add(new Paragraph(empresaLogada.getEmpStrNmefantasia()));
        documento.add(new Paragraph(texto));
        
        documento.close();

        ByteArrayInputStream inputData = new ByteArrayInputStream(outputData.toByteArray());
        
        fos.flush();
        fos.close();
    }
    
}
