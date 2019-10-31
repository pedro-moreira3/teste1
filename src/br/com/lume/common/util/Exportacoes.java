package br.com.lume.common.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.primefaces.component.api.UIColumn;
import org.primefaces.component.datatable.DataTable;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import br.com.lume.security.entity.Empresa;

public class Exportacoes implements Serializable{
    
    private static Exportacoes exportacao;
    
    private Exportacoes() {
        
    }
    
    public static Exportacoes getInstance() {
        
        if(exportacao != null) {
            return exportacao;
        }else {
            exportacao = new Exportacoes();
            return exportacao;
        }
            
    }
    
    private File exportarTabelaExcel(String header,DataTable tabela) {

        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheetTabela = workbook.createSheet(header);
        
        Row cabecalho = sheetTabela.createRow(0);
        
        for(int i = 0; i < tabela.getRowCount(); i++) {
            
            tabela.setRowIndex(i);
            tabela.getRowData();

            List<UIColumn> tabelaColunas = tabela.getColumns();
            
            Row linhaPlanilha = sheetTabela.createRow(i+1);
            
            for(int j = 0; j < tabela.getColumnsCount(); j++) {
                
                if(i == 0) {
                    
                    CellStyle styleTitulo = workbook.createCellStyle();
                    
                    styleTitulo.setAlignment(HorizontalAlignment.CENTER);
                    styleTitulo.setBorderLeft(BorderStyle.THIN);
                    styleTitulo.setBorderRight(BorderStyle.THIN);
                    styleTitulo.setBorderBottom(BorderStyle.THIN);
                    styleTitulo.setBorderTop(BorderStyle.THIN);
                    
                    Cell celula = cabecalho.createCell(j);
                    celula.setCellValue(tabelaColunas.get(j).getHeaderText());
                    
                    celula.setCellStyle(styleTitulo);
                    
                }
                
                Cell celula = linhaPlanilha.createCell(j);
                celula.setCellValue(this.formatar(tabelaColunas.get(j).getSortBy()));
                
            }
            
        }
        
        try {
            
            File arquivoXLS = new File("../"+header+".xls");
            FileOutputStream arq = new FileOutputStream(arquivoXLS);
            workbook.write(arq);
            
            arq.flush();
            arq.close();
            
            workbook.close();
            
            return arquivoXLS;
            
        }catch(Exception e) {
            
        }
        
        return null;
        
    }
    
    private File exportarTabelaPDF(String header, DataTable table) {

        try {
            
            int qtdColunasTabela = validaColuna(table.getColumns());
            
            Document documento = new Document(PageSize.A4.rotate(),30,30,30,30);
            
            File arquivoPDF = new File("../"+header+".pdf");
            FileOutputStream arq = new FileOutputStream(arquivoPDF);
            PdfWriter pdfWriter = PdfWriter.getInstance(documento, arq);
            
            documento.open();
            
            documento.newPage();
            
            PdfPTable tabelaPDF = new PdfPTable(qtdColunasTabela);
            tabelaPDF.setWidthPercentage(100f);
            
            for(int i = 0; i < table.getRowCount() ; i++) {
                
                table.setRowIndex(i);
                table.getRowData();

                List<UIColumn> tabelaColunas = table.getColumns();
                
                //Construção do cabeçalho
                if(i == 0) {
                    
                    StringBuilder string = new StringBuilder();
                    Empresa empresa = UtilsFrontEnd.getEmpresaLogada();
                    
                    string.append(empresa.getEmpStrEndereco());
                    string.append(" " + empresa.getEmpStrCidade());
                    string.append("/" + empresa.getEmpChaUf());
                    string.append(" - " + empresa.getEmpChaFone() + "\n\n");
                    
                    documento.addTitle(header);
                    
                    documento.add(new Paragraph(empresa.getEmpStrNmefantasia()));
                    documento.add(new Paragraph(string.toString()));
                    
                    for(int j = 0; j < qtdColunasTabela; j++) {
                        
                        String tituloTabela = tabelaColunas.get(j).getHeaderText();
                        
                        Font fonte = new Font(Font.BOLD, 10, Font.BOLD);
                        
                        PdfPCell celula = new PdfPCell(new Phrase(tituloTabela,fonte));
                        celula.setHorizontalAlignment(Element.ALIGN_CENTER);
                        celula.setBorderWidth(1);
                        celula.setColspan(tabelaColunas.get(j).getColspan());

                        tabelaPDF.addCell(celula);
                            
                    }
                }
                
                for(int j = 0; j < qtdColunasTabela; j++) {
                    
                    Font fonte = new Font(5,10);
                    
                    PdfPCell celula = new PdfPCell(new Phrase(this.formatar(tabelaColunas.get(j).getSortBy()), fonte));
                    celula.setHorizontalAlignment(Element.ALIGN_CENTER);
                    
                    tabelaPDF.addCell(celula);
                    
                }
                
            }
            
            documento.add(tabelaPDF);

            documento.close();
            
            return arquivoPDF;
            
        }catch(Exception e) {
            
        }
        
        return null;
        
    }
    
    private File exportarTabelaCSV(String header, DataTable table) {
        return null;
    }
    
    private int validaColuna(List<UIColumn> colunas) {
        
        int c = 0;
        
        for(int i = 0; i < colunas.size(); i++) {
            
            String t = colunas.get(i).getHeaderText();
            
            if(t != null && !t.isEmpty())
                c++;
            
        }
        
        return c;
    }
    
    private String formatar(Object obj) {
        if(obj == null){
            return "";
        }else if(obj instanceof Date) {
            Date data = (Date) obj;
            return new SimpleDateFormat("dd/MM/yyyy HH:mm:ss",new Locale("PT BR")).format(data);
        }else if(obj instanceof Number) {
            Number valor = (Number) obj;
            return String.valueOf(valor);
        }else {
            return String.valueOf(obj);
        }
    }
    
    public File exportarTabela(String header, DataTable table, String type) {
        if(header != null && table != null && type != null) {
            switch(type) {
                case "xls":
                    return this.exportarTabelaExcel(header, table);
                case "pdf":
                    return this.exportarTabelaPDF(header, table);
                case "csv":
                    return this.exportarTabelaCSV(header, table);
            }
        }
        
        return null;
        
    }
        
}
