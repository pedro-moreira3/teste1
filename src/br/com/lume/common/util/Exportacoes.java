package br.com.lume.common.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;
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

import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.security.entity.Empresa;

public class Exportacoes implements Serializable{
    
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private static Exportacoes exportacao;
    
    private Logger log = Logger.getLogger(LumeManagedBean.class);
    
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

        ArrayList<Integer> colunasValidas = validarColunas(tabela);
        
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheetTabela = workbook.createSheet(header);
        
        Row cabecalho = sheetTabela.createRow(0);
        
        for(int i = 0; i < tabela.getRowCount(); i++) {
            
            tabela.setRowIndex(i);
            tabela.getRowData();

            List<UIColumn> tabelaColunas = tabela.getColumns();
            
            Row linhaPlanilha = sheetTabela.createRow(i+1);
            
            for(int j = 0; j < colunasValidas.size(); j++) {
                
                if(i == 0) {
                    
                    CellStyle styleTitulo = workbook.createCellStyle();
                    
                    styleTitulo.setAlignment(HorizontalAlignment.CENTER);
                    styleTitulo.setBorderLeft(BorderStyle.THIN);
                    styleTitulo.setBorderRight(BorderStyle.THIN);
                    styleTitulo.setBorderBottom(BorderStyle.THIN);
                    styleTitulo.setBorderTop(BorderStyle.THIN);
                    
                    Cell celula = cabecalho.createCell(j);
                    celula.setCellValue(tabelaColunas.get(colunasValidas.get(j)).getHeaderText());
                    
                    celula.setCellStyle(styleTitulo);
                    
                }
                
                Cell celula = linhaPlanilha.createCell(j);
                celula.setCellValue(this.formatar(tabelaColunas.get(colunasValidas.get(j)).getSortBy()));
                
            }
            
        }
        
        try {
            
            File arquivoXLS = new File("/"+header+".xls");
            FileOutputStream arq = new FileOutputStream(arquivoXLS);
            workbook.write(arq);
            
            arq.flush();
            arq.close();
            
            workbook.close();
            
            return arquivoXLS;
            
        }catch(Exception e) {
            this.log.error("Erro ao exportar Tabela Excel", e);
        }
        
        return null;
        
    }
    
    private File exportarTabelaPDF(String header, DataTable table) {

        try {
            
            //int qtdColunasTabela = validaColuna(table.getColumns());
            
            ArrayList<Integer> colunasValidas = validarColunas(table);
            
            Document documento = new Document(PageSize.A4.rotate(),30,30,30,30);
            
            File arquivoPDF = new File("/"+header+".pdf");
            FileOutputStream arq = new FileOutputStream(arquivoPDF);
            PdfWriter pdfWriter = PdfWriter.getInstance(documento, arq);
            
            documento.open();
            
            documento.newPage();
            
            PdfPTable tabelaPDF = new PdfPTable(colunasValidas.size());
            tabelaPDF.setWidthPercentage(100f);
            
            for(int i = 0; i < table.getRowCount(); i++) {
                
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
                    
                    for(int j = 0; j < colunasValidas.size(); j++) {
                        
                        String tituloTabela = "";
                        
                        tituloTabela = tabelaColunas.get(colunasValidas.get(j)).getHeaderText();
                        
                        Font fonte = new Font(Font.BOLD, 10, Font.BOLD);
                        
                        PdfPCell celula = new PdfPCell(new Phrase(tituloTabela,fonte));
                        celula.setHorizontalAlignment(Element.ALIGN_CENTER);
                        celula.setBorderWidth(1);
                        celula.setColspan(tabelaColunas.get(colunasValidas.get(j)).getColspan());

                        tabelaPDF.addCell(celula);
                            
                    }
                }
                
                for(int j = 0; j < colunasValidas.size(); j++) {
                    
                    Font fonte = new Font(5,10);
                    
                    PdfPCell celula = new PdfPCell(new Phrase(this.formatar(tabelaColunas.get(colunasValidas.get(j)).getSortBy()), fonte));
                    celula.setHorizontalAlignment(Element.ALIGN_CENTER);
                    
                    tabelaPDF.addCell(celula);
                    
                }
                
            }
            
            documento.add(tabelaPDF);

            documento.close();
            
            return arquivoPDF;
            
        }catch(Exception e) {
            this.log.error("Erro ao exportar Tabela Pdf", e);
        }
        
        return null;
        
    }
    
    private File exportarTabelaCSV(String header, DataTable tabela) {
        
        ArrayList<Integer> colunasValidas = validarColunas(tabela);
        File file = new File("/"+header+".csv");
        
        try(BufferedWriter buffer = new BufferedWriter(new FileWriter(file))) {
            
            for(int i = 0; i < tabela.getRowCount(); i++) {
                
                tabela.setRowIndex(i);
                tabela.getRowData();
                
                List<UIColumn> tabelaColunas = tabela.getColumns();
                
                if(i == 0) {
                    
                    for(int j = 0; j < colunasValidas.size(); j++) {
                        
                        buffer.write(tabelaColunas.get(colunasValidas.get(j)).getHeaderText() + ";");
                            
                    }
                    
                    buffer.write("\n");
                }
                
                for(int j = 0; j < colunasValidas.size(); j++) {
                    
                    buffer.write(this.formatar(tabelaColunas.get(colunasValidas.get(j)).getFilterBy()) + ";");
                    
                }
                
                buffer.write("\n");
                
            }
            
            return file;
            
        } catch (Exception e) {
            this.log.error("Erro no ExportarTabelaCSV",e);
        }
        
        return null;
    }
    
    private ArrayList<Integer> validarColunas(DataTable tabela) {

        ArrayList<Integer> colunasValidas = new ArrayList<Integer>();
        
        int quantidadeLinhas = tabela.getRows();
        
        if(quantidadeLinhas > 1) {
            for(int i = 0; i < 1; i++) {
                
                tabela.setRowIndex(i);
                tabela.getRowData();
                
                List<UIColumn> tabelaColunas = tabela.getColumns();
                
                for(int j = 0; j < tabelaColunas.size(); j++) {
                    
                    if(tabelaColunas.get(j).isExportable()) {
                        
                        if( !((tabelaColunas.get(j).getChildren().toString().contains("HtmlPanelGroup")) || (tabelaColunas.get(j).getChildren().toString().contains("CommandButton")) ||
                                (tabelaColunas.get(j).getChildren().toString().contains("Button"))) ) {
                            colunasValidas.add(j);
                        }
                        
                    }
                }
                
            }
        }
        
        return colunasValidas;
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
                    //alterarArquivo();
                    return this.exportarTabelaExcel(header, table);
                case "pdf":
                    return this.exportarTabelaPDF(header, table);
                case "csv":
                    return this.exportarTabelaCSV(header, table);
            }
        }
        
        return null;
    }
    
    public void alterarArquivo() {
        
        File file = new File("C:\\Users\\eduardo.tremarin\\Desktop\\Eduardo\\intelidente\\afastamento.xhtml");
        
        try(BufferedReader bReader = new BufferedReader(new FileReader(file));
                BufferedWriter bWriter = new BufferedWriter(new FileWriter(file, true))){
            
            ArrayList<String> listaString = new ArrayList<String>();
            StringBuilder sb = new StringBuilder();
            
            String idTabela = "";
            String managedBean = "";
            String metodoExportacao = "";
            
            String linha = "";
            
            while(bReader.ready()) {
                
                linha = bReader.readLine();
                
                
                
            }
            
        }catch (Exception e) {
            this.log.error("Erro no alterarArquivo");
        }
        
    }
    
        
}
