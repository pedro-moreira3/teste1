//package br.com.lume.odonto.managed;
//
//import java.io.BufferedReader;
//import java.io.ByteArrayInputStream;
//import java.io.ByteArrayOutputStream;
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileOutputStream;
//import java.io.FileReader;
//import java.io.IOException;
//import java.lang.reflect.Field;
//import java.text.SimpleDateFormat;
//import java.util.ArrayList;
//import java.util.Calendar;
//import java.util.Date;
//import java.util.List;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//
//import javax.faces.bean.ManagedBean;
//import javax.faces.bean.SessionScoped;
//import javax.faces.bean.ViewScoped;
//import javax.faces.context.FacesContext;
//import javax.faces.event.ActionEvent;
//import javax.swing.text.html.parser.ParserDelegator;
//
//import org.primefaces.PrimeFaces;
//import org.primefaces.model.DefaultStreamedContent;
//import org.primefaces.model.StreamedContent;
//
//import com.lowagie.text.Document;
//import com.lowagie.text.Element;
//import com.lowagie.text.Image;
//import com.lowagie.text.PageSize;
//import com.lowagie.text.Paragraph;
//import com.lowagie.text.pdf.PdfWriter;
//
//import br.com.lume.cid.CidSingleton;
//import br.com.lume.common.log.LogIntelidenteSingleton;
//import br.com.lume.common.managed.LumeManagedBean;
//import br.com.lume.common.util.Mensagens;
//import br.com.lume.common.util.Status;
//import br.com.lume.common.util.UtilsFrontEnd;
//import br.com.lume.documento.DocumentoSingleton;
//import br.com.lume.documentoEmitido.DocumentoEmitidoSingleton;
//import br.com.lume.dominio.DominioSingleton;
//import br.com.lume.odonto.entity.CID;
//// import br.com.lume.odonto.bo.DocumentoBO;
//// import br.com.lume.odonto.bo.DominioBO;
//// import br.com.lume.odonto.bo.ProfissionalBO;
//import br.com.lume.odonto.entity.Documento;
//import br.com.lume.odonto.entity.DocumentoEmitido;
//import br.com.lume.odonto.entity.Dominio;
//import br.com.lume.odonto.entity.Paciente;
//import br.com.lume.odonto.entity.Profissional;
//import br.com.lume.odonto.entity.Tag;
//import br.com.lume.odonto.entity.TagDocumentoModelo;
//import br.com.lume.odonto.entity.TagEntidade;
//import br.com.lume.paciente.PacienteSingleton;
//import br.com.lume.profissional.ProfissionalSingleton;
//import br.com.lume.security.entity.Empresa;
//import br.com.lume.tag.TagSingleton;
//import br.com.lume.tagEntidade.TagEntidadeSingleton;
//
//@ManagedBean
//@ViewScoped
//public class EmissaoDocumentoMB extends LumeManagedBean<DocumentoEmitido> {
//
//    private static final long serialVersionUID = 1L;
//
//    private Documento modeloSelecionado;
//    private List<Dominio> listaTiposDocumentos;
//    private Dominio filtroTipoDocumento;
//    private Dominio filtroTipoDocumentoEmitir;
//    private List<Documento> listaDocumentosModelos;
//    private List<TagEntidade> tags = new ArrayList<TagEntidade>();
//    private List<TagEntidade> tagsDinamicas = new ArrayList<TagEntidade>();
//    private List<Documento> documentos;
//    private Tag tag;
//    private Profissional filtroProfissionalEmissao;
//    private Paciente pacienteSelecionado;
//    private CID cid;
//    private DocumentoEmitido docEmitido;
//    private boolean teste = false;
//    private StringBuilder modeloHtml = new StringBuilder("");
//    
//    //Filtros
//    private Date dataInicio;
//    private Date dataFim;
//    private Paciente emitidoPara;
//    private String filtroPeriodo;
//    
//    private StreamedContent arqTemp;
//    private StreamedContent arqEmitido;
//
//    public EmissaoDocumentoMB() {
//        super(DocumentoEmitidoSingleton.getInstance().getBo());
//        this.setClazz(DocumentoEmitido.class);
//        this.carregarTiposDocumento();
//        pesquisar();
//    }
//
//    public void pesquisar() {
//        Date dataInicial = null, dataFinal = null;
//        
//        if (getDataInicio() != null && getDataFim() != null) {
//            Calendar c = Calendar.getInstance();
//            c.setTime(getDataInicio());
//            c.add(Calendar.DAY_OF_MONTH, -1);
//            dataInicial = c.getTime();
//
//            c.setTime(getDataFim());
//            c.add(Calendar.DAY_OF_MONTH, +1);
//            dataFinal = c.getTime();
//        }
//        
//        this.setEntityList(DocumentoEmitidoSingleton.getInstance().getBo().listByFiltros(dataInicial, dataFinal, filtroProfissionalEmissao, getEmitidoPara(),
//                filtroTipoDocumento, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa()));
//        
//    }
//
//    public List<Profissional> sugestoesProfissionais(String query) {
//        return ProfissionalSingleton.getInstance().getBo().listSugestoesCompleteDentista(query, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa(), true);
//    }
//    
//    public void carregarDocumentoHtmlModelo() {
//        BufferedReader reader = null;
//        
//        try {
//            if(this.modeloSelecionado != null && this.modeloSelecionado.getPathModelo() != null) {
//                reader = new BufferedReader(new FileReader(new File(this.modeloSelecionado.getPathModelo())));
//                
//                while(reader.ready()) {
//                    this.modeloHtml.append(reader.readLine());
//                }
//                
//            }
//        }catch (Exception e) {
//            this.addError("Erro", "Não foi possível carregar o documento modelo");
//            e.printStackTrace();
//        }finally {
//            try {
//                reader.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }
//    
//    public void emitirDocumento() {
//        Document documento = null;
//
//        this.processarDocumento();
//
//        DocumentoEmitido doc = new DocumentoEmitido();
//        doc.setDataEmissao(new Date());
//        doc.setEmitidoPor(UtilsFrontEnd.getProfissionalLogado());
//            
//        if(this.pacienteSelecionado != null) {
//            doc.setEmitidoPara(this.pacienteSelecionado);
//            doc.setPathDocumento("/app/odonto/documentos/" + UtilsFrontEnd.getEmpresaLogada().getEmpStrNme() + "/" + this.modeloSelecionado.getDescricao()+ "-" + this.pacienteSelecionado.getId() + ".pdf");
//        }else {
//            doc.setPathDocumento("/app/odonto/documentos/" + UtilsFrontEnd.getEmpresaLogada().getEmpStrNme() + "/" + this.modeloSelecionado.getDescricao() + ".pdf");
//        }
//        
//        doc.setDocumentoModelo(this.modeloSelecionado);
//
//        if (this.modeloSelecionado.getLayout() == null || this.modeloSelecionado.getLayout().isEmpty()) {
//            this.modeloSelecionado.setLayout("A4");
//        }
//
//        switch (this.modeloSelecionado.getLayout()) {
//            case "A3": {
//                documento = new Document(PageSize.A3.rotate(), 30, 30, 30, 30);
//            }
//                break;
//            case "A4": {
//                documento = new Document(PageSize.A4.rotate(), 30, 30, 30, 30);
//            }
//                break;
//            case "A5": {
//                documento = new Document(PageSize.A5.rotate(), 30, 30, 30, 30);
//            }
//                break;
//        }
//
//        try {
//            ByteArrayOutputStream outputData = new ByteArrayOutputStream();
//            PdfWriter pdfWriter = PdfWriter.getInstance(documento, outputData);
//
//            documento.open();
//            documento.newPage();
//
//            if(this.modeloSelecionado.getMostrarLogo() != null && this.modeloSelecionado.getMostrarLogo().equals(Status.SIM)) {
//                Image img = Image.getInstance(String.format("\\app\\odonto\\imagens\\"+
//                        UtilsFrontEnd.getEmpresaLogada().getEmpStrLogo()));
//                
//                float w = img.getWidth();
//                float h = img.getHeight();
//                float r = ((w*h)/10000);
//                
//                float w1 = 0;
//                float h1 = 0;
//                
//                if( r > 1 ) {
//                    w1 = (w/100)/r;
//                    h1 = (h/100)/r;
//                }
//                
//                img.scaleAbsolute(w*w1, h*h1);
//                img.setAlignment(Element.PARAGRAPH);
//                documento.add(img);
//                documento.addHeader(UtilsFrontEnd.getEmpresaLogada().getEmpStrNme(), "Telefone para contato: "+UtilsFrontEnd.getEmpresaLogada().getEmpChaFone());
//            }
//            
//            if(this.modeloSelecionado.getModelo().contains("|new-page|")) {
//                String buf[] = this.modeloSelecionado.getModelo().split("\\|(.*?)\\|");
//                for(int i = 0; i<buf.length; i++){
//                    documento.add(new Paragraph(buf[i]));
//                    documento.newPage();
//                }
//            }else {
//                documento.add(new Paragraph(this.modeloSelecionado.getModelo()));
//            }
//
//            DocumentoEmitidoSingleton.getInstance().getBo().persist(doc);
//            this.docEmitido = doc;
//
//            File file = new File("/app/odonto/documentos/" + UtilsFrontEnd.getEmpresaLogada().getEmpStrNme() + "/");
//            file.mkdirs();
//
//            documento.close();
//
//            FileOutputStream out = new FileOutputStream(file + "/" + this.modeloSelecionado.getDescricao()+
//                    (this.pacienteSelecionado != null ? "-" + this.pacienteSelecionado.getId() : "") + ".pdf");
//            out.write(outputData.toByteArray());
//
//            outputData.flush();
//            outputData.close();
//
//            out.flush();
//            out.close();
//            
//            this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "Emissão do documento realizada com sucesso.");
//            
//            this.setEntity(doc);
//            this.pesquisar();
//
//        } catch (Exception e) {
//            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), e.getMessage());
//            e.printStackTrace();
//        }
//    }
//
//    public StreamedContent getArquivo(DocumentoEmitido doc) {
//        
//        StreamedContent arquivo = null;
//        if (doc.getPathDocumento() != null) {
//            try {                
//                File file = new File("/app/odonto/documentos/" + UtilsFrontEnd.getEmpresaLogada().getEmpStrNme() + "/");
//                FileInputStream in = new FileInputStream(file + "/" + doc.getDocumentoModelo().getDescricao() + "-" + doc.getEmitidoPara().getId() + ".pdf");                
//                arquivo = new DefaultStreamedContent(in, null, doc.getDocumentoModelo().getDescricao() + ".pdf");
//            } catch (Exception e) {
//                this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "Não foi possível carregar o documento.");
//                e.printStackTrace();
//            }
//        }
//        return arquivo;
//    }
//    
//    public StreamedContent getArquivo() {
//        return this.getArquivo(this.getEntity());
//    }
//    
//    public void carregarDocumentoModelo() {
//        if (modeloSelecionado != null) {
//            List<TagDocumentoModelo> tags = modeloSelecionado.getTags();
//            List<TagDocumentoModelo> tagsProcessadas = new ArrayList<>();
//            String textoDocumento = modeloSelecionado.getModelo();
//
//            for (TagDocumentoModelo tag : tags) {
//                if (tag.getTagEntidade().getEntidade().getEntidade().equals("Paciente")) {
//                    this.tag = TagSingleton.getInstance().getBo().listByEntidade("Paciente");
//                }
//
//                if (tag.getTagEntidade().getInserirDado().equals(Status.NAO)) {
//                    if (tag.getTagEntidade().getEntidade().getEntidade().equals("Paciente")) {
//                        this.tags.add(tag.getTagEntidade());
//                    } else {
//                        this.processarTag(tag.getTagEntidade(), textoDocumento);
//                        tagsProcessadas.add(tag);
//                    }
//                } else {
//                    this.tags.add(tag.getTagEntidade());
//                }
//            }
//            
//        }
//    }
//    
//    public void montarTags() {
//        String regex = "\\{(.*?)\\}";
//        String retorno = "";
//        
//        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
//        Matcher comparator = pattern.matcher(this.modeloSelecionado.getModelo());
//        
//        carregarDocumentoHtmlModelo();
//
//        while (comparator.find()) {
//            retorno = comparator.group();
//
//            if (retorno != null && !retorno.isEmpty()) {
//                retorno = retorno.replaceAll("\\{", "");
//                retorno = retorno.replaceAll("\\}", "");
//                String str[] = retorno.trim().split("-");
//
//                if (str != null && str.length > 0) {
//                    try {
//                        if (str[0].equals("Custom")) {
//                            TagEntidade tag = new TagEntidade();
//                            Tag tagPai = new Tag();
//                            tagPai.setEntidade("Custom");
//                            
//                            tag.setDescricaoCampo(str[1]);
//                            tag.setInserirDado("S");
//                            tag.setTipoAtributo("texto");
//                            tag.setEntidade(tagPai);
//                            
//                            this.tagsDinamicas.add(tag);
//                        } else {
//                            TagEntidade tag = TagEntidadeSingleton.getInstance().getBo().findByAttributeAndEntidade(str[1], str[0]);
//
//                            if (tag != null) {
//                                if(tag.getEntidade().getEntidade().equals("Paciente")) {
//                                    this.tag = tag.getEntidade();
//                                    this.tags.add(tag);
//                                }else if(tag.getEntidade().getEntidade().equals("Empresa")){
//                                    this.tags.add(tag);
//                                }else {
//                                    this.tagsDinamicas.add(tag);
//                                }
//                            }
//                        }
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                        LogIntelidenteSingleton.getInstance().makeLog(e.getMessage());
//                    }
//                }
//            }
//        }
//        
//        if(this.tag == null && this.validaRecebedor()) {
//            this.tag = TagSingleton.getInstance().getBo().listByEntidade("Paciente");
//        }
//        
//    }
//
//    public boolean validaRecebedor() {
//        if(this.modeloSelecionado.getTipo().getNome().equals("Faturamento") || this.modeloSelecionado.getTipo().getNome().equals("Contrato Profissional"))
//            return false;
//        return true;
//    }
//    
//    public void preview() {
//        if(this.modeloHtml.toString().contains("|new-page|")) {
//            this.modeloHtml = new StringBuilder(this.modeloHtml.toString().replaceAll("\\|new-page\\|", "\n\n<hr style=\\\"border-color:#aaa;box-sizing:border-box;width:100%;\\\"></hr>\n"));
//        }
//    }
//    
//    public List<CID> geraSugestoesCID(String query) {
//        List<CID> suggestions = new ArrayList<>();
//        try {
//            suggestions = CidSingleton.getInstance().getBo().listSugestoesComplete(query, false);
//        } catch (Exception e) {
//            this.addError("Erro", "Falha ao carregar os CID");
//        }
//        return suggestions;
//    }
//
//    public void carregarDocumentos() {
//        try {
//            if (this.filtroTipoDocumentoEmitir != null) {
//                this.listaDocumentosModelos = DocumentoSingleton.getInstance().getBo().listByTipoDocumento(filtroTipoDocumentoEmitir, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            this.addError("Erro", "Não foi possível carregar os documentos");
//        }
//    }
//
//    public void carregarTiposDocumento() {
//        try {
//            this.setListaTiposDocumentos(DominioSingleton.getInstance().getBo().listByObjeto("documento"));
//        } catch (Exception e) {
//            this.addError("Erro ao carregar tipos de documentos", "");
//            e.printStackTrace();
//        }
//    }
//
//    public void atualizarTag() {
//        for(TagEntidade tag : this.tags) {
//            if(tag.getEntidade().getEntidade().equals("Paciente") || tag.getEntidade().getEntidade().equals("Atestado")) {
//                if(tag.getEntidade().getEntidade().equals("Paciente") && this.pacienteSelecionado != null)
//                    this.processarTag(tag, this.modeloSelecionado.getModelo());
//                else if(tag.getEntidade().getEntidade().equals("Atestado") && this.cid != null)
//                    this.processarTag(tag, this.modeloSelecionado.getModelo());
//            }else {
//                this.processarTag(tag, this.modeloSelecionado.getModelo());
//            }
//            
//        }
//        
//        for(TagEntidade tag : this.tagsDinamicas) {
//            if(tag.getRespTag() != null || tag.getRespTagData() != null || (tag.getDescricaoCampo().equals("CID") && this.cid != null)) {
//                this.processarTag(tag, this.modeloSelecionado.getModelo());
//            }
//        }
//        
//        preview();
//    }
//    
//    private void processarTag(TagEntidade tag, String modelo) {
//        try {
//            Class<?> c = null;
//            switch (tag.getEntidade().getEntidade()) {
//                case "Empresa": {
//                    Empresa emp = UtilsFrontEnd.getEmpresaLogada();
//                    c = emp.getClass();
//
//                    Field campo = c.getDeclaredField(tag.getAtributo());
//                    campo.setAccessible(true);
//                    Object obj = campo.get(emp);
//
//                    if (obj instanceof String) {
//                        modelo = modelo.replaceAll("\\{" + tag.getEntidade().getEntidade() + "-" + tag.getAtributo() + "\\}", (obj != null ? (String) obj : ""));
//                    }else if (obj == null) {
//                        modelo = modelo.replaceAll("\\{"+ tag.getEntidade().getEntidade() + "-" + tag.getAtributo() + "\\}", "");
//                    }
//                }
//                    break;
//                case "Paciente": {
//                    c = this.pacienteSelecionado.getDadosBasico().getClass();
//
//                    Field campo = c.getDeclaredField(tag.getAtributo());
//                    campo.setAccessible(true);
//                    Object obj = campo.get(this.pacienteSelecionado.getDadosBasico());
//
//                    if (obj instanceof String) {
//                        modelo = modelo.replaceAll("\\{" + tag.getEntidade().getEntidade() + "-" + tag.getAtributo() + "\\}", (obj != null ? (String) obj : ""));
//                    }else if (obj instanceof Date) {
//                        Date data = (Date) obj;
//                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
//                        
//                        modelo = modelo.replaceAll("\\{"+ tag.getEntidade().getEntidade() + "-" + tag.getAtributo() + "\\}", sdf.format(data));
//                    }else if (obj == null) {
//                        modelo = modelo.replaceAll("\\{"+ tag.getEntidade().getEntidade() + "-" + tag.getAtributo() + "\\}", "");
//                    }
//                }
//                    break;
//                default: {
//                    if (tag.getEntidade().getEntidadePai() != null) {
//
//                    } else {
//                        if(tag.getAtributo() != null && tag.getAtributo().equals("cid")) {
//                            modelo = modelo.replaceAll("\\{"+ tag.getEntidade().getEntidade() + "-" + tag.getAtributo() + "\\}", this.cid.getDescricao());
//                        }else if (tag.getEntidade().getEntidade().equals("Custom")) {
//                            if (tag.getRespTag() != null) {
//                                modelo = modelo.replaceAll("\\{Custom-" + tag.getDescricaoCampo() + "\\}", tag.getRespTag());
//                            }
//                        }else {
//                            if (tag.getRespTagData() != null) {
//                                Date data = tag.getRespTagData();
//                                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
//                                
//                                modelo = modelo.replaceAll("\\{"+ tag.getEntidade().getEntidade() + "-" + tag.getAtributo() + "\\}", sdf.format(data));
//                            }
//                        }
//                    }
//                }
//            }
//            
//            
//            this.modeloSelecionado.setModelo(modelo);
//            modeloHtml = new StringBuilder("");
//            this.modeloHtml.append(modelo);
//            
//        } catch (Exception e) {
//            this.addError("Erro na emissão", "Falha ao processar as tags do documento.");
//            e.printStackTrace();
//            LogIntelidenteSingleton.getInstance().makeLog(e);
//        }
//    }
//    
//    @Override
//    public void actionNew(ActionEvent event) {
//        super.actionNew(event);
//        this.setEntity(null);
//        this.setCid(null);
//        this.setPacienteSelecionado(null);
//        this.setFiltroTipoDocumentoEmitir(null);
//        this.setModeloSelecionado(null);
//    }
//    
//    public void processarDocumento() {
//        for(TagEntidade tag : this.tags) {
//            this.processarTag(tag, this.modeloSelecionado.getModelo());
//        }
//        
//        for(TagEntidade tag : this.tagsDinamicas) {
//            this.processarTag(tag, this.modeloSelecionado.getModelo());
//        }
//        
//        preview();
//    }
//    
//    public void processarCID() {
//        if (this.cid != null) {
//            this.modeloSelecionado.getModelo().replaceAll("\\{Atestado-cid\\}", (String) this.cid.getDescricao());
//            this.atualizarTag();
//        }
//    }
//
//    public List<Paciente> sugestoesPacientes(String query) {
//        try {
//            return PacienteSingleton.getInstance().listSugestoesComplete(query, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
//        } catch (Exception e) {
//            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
//            e.printStackTrace();
//        }
//        return null;
//    }
//
//    public void actionTrocaDatasCriacao() {
//        try {
//
//            this.dataInicio = getDataInicio(getFiltroPeriodo());
//            this.dataFim = getDataFim(getFiltroPeriodo());
//
//            PrimeFaces.current().ajax().update(":lume:dataInicial");
//            PrimeFaces.current().ajax().update(":lume:dataFinal");
//
//        } catch (Exception e) {
//            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
//        }
//    }
//
//    public Date getDataFim(String filtro) {
//        Date dataFim = null;
//        try {
//            Calendar c = Calendar.getInstance();
//            if ("O".equals(filtro)) {
//                c.add(Calendar.DAY_OF_MONTH, -1);
//                dataFim = c.getTime();
//            } else if (filtro == null) {
//                dataFim = null;
//            } else {
//                dataFim = c.getTime();
//            }
//            return dataFim;
//        } catch (Exception e) {
//            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
//            return null;
//        }
//    }
//
//    public Date getDataInicio(String filtro) {
//        Date dataInicio = null;
//        try {
//            Calendar c = Calendar.getInstance();
//            if ("O".equals(filtro)) {
//                c.add(Calendar.DAY_OF_MONTH, -1);
//                dataInicio = c.getTime();
//            } else if ("H".equals(filtro)) { //Hoje                
//                dataInicio = c.getTime();
//            } else if ("S".equals(filtro)) { //Últimos 7 dias              
//                c.add(Calendar.DAY_OF_MONTH, -7);
//                dataInicio = c.getTime();
//            } else if ("Q".equals(filtro)) { //Últimos 15 dias              
//                c.add(Calendar.DAY_OF_MONTH, -15);
//                dataInicio = c.getTime();
//            } else if ("T".equals(filtro)) { //Últimos 30 dias                
//                c.add(Calendar.DAY_OF_MONTH, -30);
//                dataInicio = c.getTime();
//            } else if ("M".equals(filtro)) { //Mês Atual              
//                c.set(Calendar.DAY_OF_MONTH, 1);
//                dataInicio = c.getTime();
//            } else if ("I".equals(filtro)) { //Mês Atual             
//                c.add(Calendar.MONTH, -6);
//                dataInicio = c.getTime();
//            }
//            return dataInicio;
//        } catch (Exception e) {
//            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
//            return null;
//        }
//    }
//
//    private boolean validarIntervaloDatas() {
//
//        if ((dataInicio != null && dataFim != null) && dataInicio.getTime() > dataFim.getTime()) {
//            this.addError("Intervalo de datas", "A data inicial deve preceder a data final.", true);
//            return false;
//        }
//        return true;
//    }
//
////  public void construirModeloPDF() {
////  Document documento = null;
////
////  if (this.modeloSelecionado.getLayout() == null || this.modeloSelecionado.getLayout().isEmpty()) {
////      this.modeloSelecionado.setLayout("A4");
////  }
////
////  switch (this.modeloSelecionado.getLayout()) {
////      case "A3": {
////          documento = new Document(PageSize.A3.rotate(), 30, 30, 30, 30);
////      }
////          break;
////      case "A4": {
////          documento = new Document(PageSize.A4.rotate(), 30, 30, 30, 30);
////      }
////          break;
////      case "A5": {
////          documento = new Document(PageSize.A5.rotate(), 30, 30, 30, 30);
////      }
////          break;
////  }
////
////  try {
////      ByteArrayOutputStream outputData = new ByteArrayOutputStream();
////      PdfWriter pdfWriter = PdfWriter.getInstance(documento, outputData);
////
////      documento.open();
////      documento.newPage();
////      
////      documento.add(new Paragraph(this.modeloSelecionado.getModelo()));
////
////      documento.close();
////      
////      //this.setStreamOut(new ByteArrayInputStream(outputData.toByteArray()));
////      
////      outputData.flush();
////      outputData.close();
////      
////      //this.setArquivoDownload(new DefaultStreamedContent(this.getStreamOut(), "application/pdf"));
////
////  } catch (Exception e) {
////      this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), e.getMessage());
////      e.printStackTrace();
////  }
////}
//    
//    public Tag getTag() {
//        return tag;
//    }
//
//    public void setTag(Tag tag) {
//        this.tag = tag;
//    }
//
//    public Documento getModeloSelecionado() {
//        return modeloSelecionado;
//    }
//
//    public void setModeloSelecionado(Documento modeloSelecionado) {
//        this.modeloSelecionado = modeloSelecionado;
//    }
//
//    public List<Dominio> getListaTiposDocumentos() {
//        return listaTiposDocumentos;
//    }
//
//    public void setListaTiposDocumentos(List<Dominio> listaTiposDocumentos) {
//        this.listaTiposDocumentos = listaTiposDocumentos;
//    }
//
//    public Dominio getFiltroTipoDocumento() {
//        return filtroTipoDocumento;
//    }
//
//    public void setFiltroTipoDocumento(Dominio filtroTipoDocumento) {
//        this.filtroTipoDocumento = filtroTipoDocumento;
//    }
//
//    public List<Documento> getListaDocumentosModelos() {
//        return listaDocumentosModelos;
//    }
//
//    public void setListaDocumentosModelos(List<Documento> listaDocumentosModelos) {
//        this.listaDocumentosModelos = listaDocumentosModelos;
//    }
//
//    public List<TagEntidade> getTags() {
//        return tags;
//    }
//
//    public void setTags(List<TagEntidade> tags) {
//        this.tags = tags;
//    }
//
//    public List<Documento> getDocumentos() {
//        return documentos;
//    }
//
//    public void setDocumentos(List<Documento> documentos) {
//        this.documentos = documentos;
//    }
//
//    public Profissional getFiltroProfissionalEmissao() {
//        return filtroProfissionalEmissao;
//    }
//
//    public void setFiltroProfissionalEmissao(Profissional filtroProfissionalEmissao) {
//        this.filtroProfissionalEmissao = filtroProfissionalEmissao;
//    }
//
//    public Dominio getFiltroTipoDocumentoEmitir() {
//        return filtroTipoDocumentoEmitir;
//    }
//
//    public void setFiltroTipoDocumentoEmitir(Dominio filtroTipoDocumentoEmitir) {
//        this.filtroTipoDocumentoEmitir = filtroTipoDocumentoEmitir;
//    }
//
//    public Paciente getPacienteSelecionado() {
//        return pacienteSelecionado;
//    }
//
//    public void setPacienteSelecionado(Paciente pacienteSelecionado) {
//        this.pacienteSelecionado = pacienteSelecionado;
//    }
//
//    public CID getCid() {
//        return cid;
//    }
//
//    public void setCid(CID cid) {
//        this.cid = cid;
//    }
//
//    public DocumentoEmitido getDocEmitido() {
//        return docEmitido;
//    }
//
//    public void setDocEmitido(DocumentoEmitido docEmitido) {
//        this.docEmitido = docEmitido;
//    }
//
//    public List<TagEntidade> getTagsDinamicas() {
//        return tagsDinamicas;
//    }
//
//    public void setTagsDinamicas(List<TagEntidade> tagsDinamicas) {
//        this.tagsDinamicas = tagsDinamicas;
//    }
//
//    public StreamedContent getArqTemp() {
//        return arqTemp;
//    }
//
//    public void setArqTemp(StreamedContent arqTemp) {
//        this.arqTemp = arqTemp;
//    }
//
//    public StreamedContent getArqEmitido() {
//        return arqEmitido;
//    }
//
//    public void setArqEmitido(StreamedContent arqEmitido) {
//        this.arqEmitido = arqEmitido;
//    }
//
//    public boolean isTeste() {
//        return teste;
//    }
//
//    public void setTeste(boolean teste) {
//        this.teste = teste;
//    }
//
//    public String getModeloHtml() {
//        return modeloHtml.toString();
//    }
//
//    public void setModeloHtml(StringBuilder modeloHtml) {
//        this.modeloHtml = modeloHtml;
//    }
//
//    public Date getDataInicio() {
//        return dataInicio;
//    }
//
//    public void setDataInicio(Date dataInicio) {
//        this.dataInicio = dataInicio;
//    }
//
//    public Date getDataFim() {
//        return dataFim;
//    }
//
//    public void setDataFim(Date dataFim) {
//        this.dataFim = dataFim;
//    }
//
//    public Paciente getEmitidoPara() {
//        return emitidoPara;
//    }
//
//    public void setEmitidoPara(Paciente emitidoPara) {
//        this.emitidoPara = emitidoPara;
//    }
//
//    public String getFiltroPeriodo() {
//        return filtroPeriodo;
//    }
//
//    public void setFiltroPeriodo(String filtroPeriodo) {
//        this.filtroPeriodo = filtroPeriodo;
//    }
//
//}
