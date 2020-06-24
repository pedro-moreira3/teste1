package br.com.lume.common.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import javax.faces.component.UIComponent;
import javax.faces.component.html.HtmlOutputText;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.primefaces.component.api.UIColumn;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.component.treetable.TreeTable;
import org.primefaces.convert.NumberConverter;
import org.primefaces.model.TreeNode;

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
import br.com.lume.odonto.entity.Especialidade;
import br.com.lume.odonto.entity.Procedimento;
import br.com.lume.security.entity.Empresa;

public class Exportacoes implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private static Exportacoes exportacao;

    private Logger log = Logger.getLogger(LumeManagedBean.class);

    private Exportacoes() {

    }

    public static Exportacoes getInstance() {
        if (exportacao != null) {
            return exportacao;
        } else {
            exportacao = new Exportacoes();
            return exportacao;
        }
    }

    private ByteArrayInputStream exportarTabelaExcel(String header, DataTable tabela) {

        ArrayList<Integer> colunasValidas = validarColunas(tabela);

        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheetTabela = workbook.createSheet(header);

        Row cabecalho = sheetTabela.createRow(0);

        int tableCount = tabela.getRowCount();

        for (int i = 0; i < tableCount; i++) {

            tabela.setRowIndex(i);
            tabela.getRowData();

            List<UIColumn> tabelaColunas = tabela.getColumns();

            Row linhaPlanilha = sheetTabela.createRow(i + 1);
            Row linhaRodape = null;

            if ((tableCount - i) == 1) {
                linhaRodape = sheetTabela.createRow(i + 2);
            }

            for (int j = 0; j < colunasValidas.size(); j++) {

                if (i == 0) {

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

                HtmlOutputText dadoColuna = null;
                Object obj = tabelaColunas.get(colunasValidas.get(j)).getSortBy();

                List children = tabelaColunas.get(colunasValidas.get(j)).getChildren();
                if (children != null && !children.isEmpty()) {
                    if (children.get(0) instanceof HtmlOutputText)
                        dadoColuna = (HtmlOutputText) children.get(0);
                }

                if (obj != null) {
                    if (obj instanceof BigDecimal) {

                        BigDecimal valor = (BigDecimal) obj;
                        valor = valor.setScale(2, BigDecimal.ROUND_HALF_UP);

//                        if(dadoColuna.getConverter() != null) {
//                            NumberConverter nc = (NumberConverter) dadoColuna.getConverter();
//                            if(nc.getCurrencySymbol().equals("R$")) {
//                                CellStyle currencyFormat = workbook.createCellStyle();
//                                currencyFormat.setDataFormat(HSSFDataFormat.getBuiltinFormat("$#,##0.00"));
//                                
//                                celula.setCellValue(valor.doubleValue());
//                                celula.setCellStyle(currencyFormat);
//                            }                            
//                        }else {
//                            celula.setCellValue(valor.doubleValue());
//                        }

                        celula.setCellValue(valor.doubleValue());

                    } else if (obj instanceof Date) {

                        Date data = (Date) obj;

                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", new Locale("PT BR"));
                        celula.setCellValue(sdf.format(data));

                    } else if (obj instanceof String) {

                        String txt = (String) obj;
                        Double value = null;

                        if (txt.contains("R$") || txt.contains("[^0-9,.-]")) {
                            txt = getValueFromString(txt);
                            value = Double.valueOf(txt);

                            CellStyle currencyFormat = workbook.createCellStyle();
                            currencyFormat.setDataFormat(HSSFDataFormat.getBuiltinFormat("$#,##0.00"));

                            celula.setCellValue(value);
                            celula.setCellStyle(currencyFormat);
                        } else {
                            celula.setCellValue(txt);
                        }

                    }
                }

                if ((tableCount - i) == 1) {
                    UIComponent component = tabelaColunas.get(colunasValidas.get(j)).getFacet("footer");
                    if (component != null) {
                        Cell celulaRodape = linhaRodape.createCell(j);

                        List components = component.getChildren();

                        if (components != null && !components.isEmpty()) {
                            String rodape = "";

                            for (Object c : components) {
                                if (c instanceof HtmlOutputText) {

                                    HtmlOutputText valor = (HtmlOutputText) c;

                                    if (valor.getValue() instanceof String) {
                                        rodape += (String) valor.getValue();
                                    } else if (valor.getValue() instanceof BigDecimal) {
                                        BigDecimal v = (BigDecimal) valor.getValue();
                                        v = v.setScale(2, BigDecimal.ROUND_HALF_UP);
                                        rodape += String.valueOf(v.doubleValue());
                                    }
                                }
                            }

                            CellStyle style = workbook.createCellStyle();

                            celulaRodape.setCellStyle(style);
                            celulaRodape.setCellValue(rodape);

                        }

                    }
                }

            }

        }

        try {

            ByteArrayOutputStream outputData = new ByteArrayOutputStream();

            workbook.write(outputData);

            ByteArrayInputStream inputData = new ByteArrayInputStream(outputData.toByteArray());

            outputData.flush();
            outputData.close();

            workbook.close();

            return inputData;

        } catch (Exception e) {
            this.log.error("Erro ao exportar Tabela Excel", e);
        } finally {
            try {
                workbook.close();
            } catch (IOException e) {
                this.log.error("Erro ao exportar Tabela Excel", e);
                e.printStackTrace();
            }
        }

        return null;

    }

    private ByteArrayInputStream exportarTabelaPDF(String header, DataTable table) {

        try {

            ArrayList<Integer> colunasValidas = validarColunas(table);
            Document documento = new Document(PageSize.A4.rotate(), 30, 30, 30, 30);
            ByteArrayOutputStream outputData = new ByteArrayOutputStream();
            PdfWriter pdfWriter = PdfWriter.getInstance(documento, outputData);

            documento.open();

            documento.newPage();

            PdfPTable tabelaPDF = new PdfPTable(colunasValidas.size());
            tabelaPDF.setWidthPercentage(100f);

            int tableCount = table.getRowCount();

            for (int i = 0; i < tableCount; i++) {

                table.setRowIndex(i);
                table.getRowData();

                List<UIColumn> tabelaColunas = table.getColumns();

                //Construção do cabeçalho
                if (i == 0) {

                    StringBuilder string = new StringBuilder();
                    Empresa empresa = UtilsFrontEnd.getEmpresaLogada();

                    string.append(empresa.getEmpStrEndereco());
                    string.append(" " + empresa.getEmpStrCidade());
                    string.append("/" + empresa.getEmpChaUf());
                    string.append(" - " + empresa.getEmpChaFone() + "\n\n");

                    documento.addTitle(header);

                    documento.add(new Paragraph(empresa.getEmpStrNmefantasia()));
                    documento.add(new Paragraph(string.toString()));

                    for (int j = 0; j < colunasValidas.size(); j++) {

                        String tituloTabela = "";

                        tituloTabela = tabelaColunas.get(colunasValidas.get(j)).getHeaderText();

                        Font fonte = new Font(Font.BOLD, 10, Font.BOLD);

                        PdfPCell celula = new PdfPCell(new Phrase(tituloTabela, fonte));
                        celula.setHorizontalAlignment(Element.ALIGN_CENTER);
                        celula.setBorderWidth(1);
                        celula.setColspan(tabelaColunas.get(colunasValidas.get(j)).getColspan());

                        tabelaPDF.addCell(celula);

                    }
                }

                HashMap<Integer, UIComponent> footer = new HashMap<Integer, UIComponent>();
                Font fonte = new Font(5, 10);

                for (int j = 0; j < colunasValidas.size(); j++) {

                    PdfPCell celula = new PdfPCell(new Phrase(this.formatar(tabelaColunas.get(colunasValidas.get(j)).getSortBy()), fonte));
                    celula.setHorizontalAlignment(Element.ALIGN_CENTER);

                    tabelaPDF.addCell(celula);

                    if ((tableCount - i) == 1) {
                        UIComponent component = tabelaColunas.get(colunasValidas.get(j)).getFacet("footer");
                        if (component != null && !component.getChildren().isEmpty()) {
                            footer.put(j, component);
                        }
                    }

                }

                //CONSTRÓI O RODAPÉ DA TABELA
                if (!footer.isEmpty()) {
                    for (int j = 0; j < colunasValidas.size(); j++) {

                        if (footer.containsKey(j)) {
                            String rodape = "";
                            List components = footer.get(j).getChildren();
                            for (Object c : components) {
                                if (c instanceof HtmlOutputText) {
                                    HtmlOutputText valor = (HtmlOutputText) c;

                                    if (valor.getConverter() != null) {
                                        NumberConverter nc = (NumberConverter) valor.getConverter();
                                        if (nc.getCurrencySymbol().equals("R$")) {
                                            Locale ptBr = new Locale("pt", "BR");
                                            String valorString = NumberFormat.getCurrencyInstance(ptBr).format(
                                                    ((BigDecimal) valor.getValue()).doubleValue());
                                            
                                            rodape += "R$ " + valorString;
                                        }
                                    } else {
                                        rodape += (String) valor.getValue();
                                    }
                                }
                            }

                            PdfPCell celulaRodape = new PdfPCell(new Phrase(rodape, fonte));
                            celulaRodape.setHorizontalAlignment(Element.ALIGN_CENTER);

                            tabelaPDF.addCell(celulaRodape);
                        } else {
                            PdfPCell celulaRodape = new PdfPCell(new Phrase("", fonte));
                            celulaRodape.setHorizontalAlignment(Element.ALIGN_CENTER);

                            tabelaPDF.addCell(celulaRodape);
                        }

                    }
                }

            }

            documento.add(tabelaPDF);

            documento.close();

            ByteArrayInputStream inputData = new ByteArrayInputStream(outputData.toByteArray());

            outputData.flush();
            outputData.close();

            return inputData;

        } catch (Exception e) {
            this.log.error("Erro ao exportar Tabela Pdf", e);
        }

        return null;

    }

    private ByteArrayInputStream exportarTabelaCSV(String header, DataTable tabela) {

        ArrayList<Integer> colunasValidas = validarColunas(tabela);

        try (ByteArrayOutputStream outputData = new ByteArrayOutputStream();) {

            for (int i = 0; i < tabela.getRowCount(); i++) {

                tabela.setRowIndex(i);
                tabela.getRowData();

                List<UIColumn> tabelaColunas = tabela.getColumns();

                if (i == 0) {

                    for (int j = 0; j < colunasValidas.size(); j++) {

                        outputData.write((tabelaColunas.get(colunasValidas.get(j)).getHeaderText() + ";").getBytes());

                    }

                    outputData.write(("\n").getBytes());
                }

                for (int j = 0; j < colunasValidas.size(); j++) {

                    outputData.write((this.formatar(tabelaColunas.get(colunasValidas.get(j)).getFilterBy()) + ";").getBytes());

                }

                outputData.write(("\n").getBytes());

            }

            ByteArrayInputStream inputData = new ByteArrayInputStream(outputData.toByteArray());

            outputData.flush();
            outputData.close();

            return inputData;

        } catch (Exception e) {
            this.log.error("Erro no ExportarTabelaCSV", e);
        }

        return null;
    }

    private int verificarQtdLinhas(TreeTable tabela) {

        int j = 0, i = 0;

        for (i = 0; i < tabela.getValue().getChildren().size(); i++) {
            if (i == 0)
                j = tabela.getValue().getChildren().get(i).getChildren().size();

            if (i > 0 && j < tabela.getValue().getChildren().get(i).getChildren().size())
                j = tabela.getValue().getChildren().get(i).getChildren().size();

        }

        return j;
    }

    private ByteArrayInputStream exportarTreeTableExcel(String header, TreeTable tabela) {

        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheetTabela = workbook.createSheet(header);

        Row cabecalho = sheetTabela.createRow(0);

        int linhas = verificarQtdLinhas(tabela);

        for (int i = 0; i < linhas; i++) {

            List<TreeNode> colunas = tabela.getValue().getChildren();
            Row linhaPlanilha = sheetTabela.createRow(i + 1);

            for (int j = 0; j < colunas.size(); j++) {

                if (i == 0) {

                    CellStyle styleTitulo = workbook.createCellStyle();

                    styleTitulo.setAlignment(HorizontalAlignment.CENTER);
                    styleTitulo.setBorderLeft(BorderStyle.THIN);
                    styleTitulo.setBorderRight(BorderStyle.THIN);
                    styleTitulo.setBorderBottom(BorderStyle.THIN);
                    styleTitulo.setBorderTop(BorderStyle.THIN);

                    Cell celula = cabecalho.createCell(j);
                    celula.setCellValue(((Especialidade) colunas.get(j).getData()).getDescricao());

                    celula.setCellStyle(styleTitulo);

                }

                Cell celula = linhaPlanilha.createCell(j);

                if (colunas.get(j).getChildren().size() > i) {
                    if (colunas.get(j).getChildren().size() > 0) {
                        Object obj = colunas.get(j).getChildren().get(i).getData();

                        if (obj != null && obj instanceof Procedimento) {
                            Procedimento proc = (Procedimento) obj;

                            if (proc != null) {
                                String txt = proc.getDescricao();
                                celula.setCellValue(txt);
                            }
                        }
                    }

                }

            }

        }

        try {

            ByteArrayOutputStream outputData = new ByteArrayOutputStream();

            workbook.write(outputData);

            ByteArrayInputStream inputData = new ByteArrayInputStream(outputData.toByteArray());

            outputData.flush();
            outputData.close();

            workbook.close();

            return inputData;

        } catch (Exception e) {
            this.log.error("Erro ao exportar Tabela Excel", e);
        } finally {
            try {
                workbook.close();
            } catch (IOException e) {
                this.log.error("Erro ao exportar Tabela Excel", e);
                e.printStackTrace();
            }
        }

        return null;
    }

    private ByteArrayInputStream exportarTabelaProcedimentosExcel(String header, TreeTable tabela) {

        HSSFWorkbook workbook = new HSSFWorkbook();

        List<TreeNode> colunas = tabela.getValue().getChildren();
        for (int i = 0; i < colunas.size(); i++) {

            Especialidade especialidade = (Especialidade) colunas.get(i).getData();
            HSSFSheet sheetTabela = workbook.createSheet(especialidade.getDescricao());

            Row cabecalho = sheetTabela.createRow(0);
            Row cabecalho2 = sheetTabela.createRow(1);

            if (colunas.get(i).getChildren().size() > 0)
                for (int j = 0; j < colunas.get(i).getChildren().size(); j++) {
                    Row linhaPlanilha = sheetTabela.createRow(j + 2);

                    if (j == 0) {

                        CellRangeAddress cellRangeAddress = new CellRangeAddress(0, 0, 0, 3);
                        sheetTabela.addMergedRegion(cellRangeAddress);

                        CellStyle styleTitulo = workbook.createCellStyle();

                        styleTitulo.setAlignment(HorizontalAlignment.CENTER);
                        styleTitulo.setBorderLeft(BorderStyle.THIN);
                        styleTitulo.setBorderRight(BorderStyle.THIN);
                        styleTitulo.setBorderBottom(BorderStyle.THIN);
                        styleTitulo.setBorderTop(BorderStyle.THIN);

                        Cell celula = cabecalho.createCell(0);
                        celula.setCellValue(especialidade.getDescricao());
                        celula.setCellStyle(styleTitulo);

                        Cell celula5 = cabecalho2.createCell(0);
                        celula5.setCellValue("Procedimento");
                        celula5.setCellStyle(styleTitulo);

                        Cell celula2 = cabecalho2.createCell(1);
                        celula2.setCellValue("Código convênio");
                        celula2.setCellStyle(styleTitulo);

                        Cell celula3 = cabecalho2.createCell(2);
                        celula3.setCellValue("Valor");
                        celula3.setCellStyle(styleTitulo);

                        Cell celula4 = cabecalho2.createCell(3);
                        celula4.setCellValue("Valor repasse");
                        celula4.setCellStyle(styleTitulo);

                    }

                    Cell celula = linhaPlanilha.createCell(0);
                    Cell celula2 = linhaPlanilha.createCell(1);
                    Cell celula3 = linhaPlanilha.createCell(2);
                    Cell celula4 = linhaPlanilha.createCell(3);
                    Procedimento proc = (Procedimento) colunas.get(i).getChildren().get(j).getData();

                    if (proc != null) {
                        if (proc.getDescricao() != null)
                            celula.setCellValue(proc.getDescricao());

                        if (proc.getCodigoConvenio() != null)
                            celula2.setCellValue(proc.getCodigoConvenio());

                        if (proc.getValor() != null)
                            celula3.setCellValue(proc.getValor().doubleValue());

                        if (proc.getValorRepasse() != null)
                            celula4.setCellValue(proc.getValorRepasse().doubleValue());
                    }

                }

        }

        try {

            ByteArrayOutputStream outputData = new ByteArrayOutputStream();

            workbook.write(outputData);

            ByteArrayInputStream inputData = new ByteArrayInputStream(outputData.toByteArray());

            outputData.flush();
            outputData.close();

            workbook.close();

            return inputData;

        } catch (Exception e) {
            this.log.error("Erro ao exportar Tabela Excel", e);
        } finally {
            try {
                workbook.close();
            } catch (IOException e) {
                this.log.error("Erro ao exportar Tabela Excel", e);
                e.printStackTrace();
            }
        }

        return null;
    }

    private ArrayList<Integer> validarColunas(DataTable tabela) {

        ArrayList<Integer> colunasValidas = new ArrayList<Integer>();

        int quantidadeLinhas = tabela.getRowCount();

        if (quantidadeLinhas > 1) {
            for (int i = 0; i < 1; i++) {

                tabela.setRowIndex(i);
                tabela.getRowData();

                List<UIColumn> tabelaColunas = tabela.getColumns();

                for (int j = 0; j < tabelaColunas.size(); j++) {

                    if (tabelaColunas.get(j).isExportable()) {

                        if (!((tabelaColunas.get(j).getChildren().toString().contains("HtmlPanelGroup")))) {
                            colunasValidas.add(j);
                        }

                    }
                }

            }
        }

        return colunasValidas;
    }

    private String formatar(Object obj) {
        if (obj == null) {
            return "";
        } else if (obj instanceof Date) {
            Date data = (Date) obj;
            return new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", new Locale("PT BR")).format(data);
        } else if (obj instanceof Number) {
            NumberFormat f = (NumberFormat) NumberFormat.getInstance(new Locale("pt", "BR"));
            Number valor = (Number) obj;
            return String.valueOf(f.format(valor));
        } else if (obj instanceof String) {
            return (String) obj;
        }
        return "";
    }

    public ByteArrayInputStream exportarTabela(String header, DataTable table, String type) {
        if (header != null && table != null && type != null) {
            switch (type) {
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

    public ByteArrayInputStream exportarTreeTabela(String header, TreeTable table, String type) {
        if (header != null && table != null && type != null) {
            switch (type) {
                case "xls":
                    return this.exportarTabelaProcedimentosExcel(header, table);
            }
        }

        return null;
    }

    private String getValueFromString(String txt) {
        try {
            String aux = "";
            for (int i = 0; i < txt.length(); i++) {
                if (txt.charAt(i) == ',') {
                    aux += ".";
                } else if (txt.charAt(i) == '.') {
                } else {
                    aux += txt.charAt(i);
                }
            }

            return aux.replaceAll("[^0-9.,-]", "");
        } catch (Exception e) {
            return null;
        }
    }

}
