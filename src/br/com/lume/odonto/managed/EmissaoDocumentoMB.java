package br.com.lume.odonto.managed;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import org.primefaces.PrimeFaces;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

import com.lowagie.text.Document;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;

import br.com.lume.cid.CidSingleton;
import br.com.lume.common.log.LogIntelidenteSingleton;
import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.Status;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.documento.DocumentoSingleton;
import br.com.lume.dominio.DominioSingleton;
import br.com.lume.odonto.entity.CID;
// import br.com.lume.odonto.bo.DocumentoBO;
// import br.com.lume.odonto.bo.DominioBO;
// import br.com.lume.odonto.bo.ProfissionalBO;
import br.com.lume.odonto.entity.Documento;
import br.com.lume.odonto.entity.DocumentoEmitido;
import br.com.lume.odonto.entity.Dominio;
import br.com.lume.odonto.entity.Paciente;
import br.com.lume.odonto.entity.Profissional;
import br.com.lume.odonto.entity.Tag;
import br.com.lume.odonto.entity.TagDocumentoModelo;
import br.com.lume.odonto.entity.TagEntidade;
import br.com.lume.paciente.PacienteSingleton;
import br.com.lume.security.entity.Empresa;
import br.com.lume.tag.TagSingleton;
import br.com.lume.tagEntidade.TagEntidadeSingleton;

@ManagedBean
@ViewScoped
public class EmissaoDocumentoMB extends LumeManagedBean<Documento> {

    private static final long serialVersionUID = 1L;

    private Documento modeloSelecionado;
    private List<Dominio> listaTiposDocumentos;
    private Dominio filtroTipoDocumento;
    private Dominio filtroTipoDocumentoEmitir;
    private List<Documento> listaDocumentosModelos;
    private List<TagEntidade> tags = new ArrayList<TagEntidade>();
    private List<TagEntidade> tagsDinamicas = new ArrayList<TagEntidade>();
    private List<Documento> documentos;
    private Tag tag;
    private Profissional filtroProfissionalEmissao;
    private Paciente pacienteSelecionado;
    private CID cid;
    private DocumentoEmitido docEmitido;
    private boolean teste = false;
    
    private StreamedContent arqTemp;
    private StreamedContent arqEmitido;

    public EmissaoDocumentoMB() {
        super(DocumentoSingleton.getInstance().getBo());
        this.setClazz(Documento.class);
        this.carregarTiposDocumento();
    }

    public void pesquisar() {

    }

    public void emitirDocumento() {
        Document documento = null;

        this.processarDocumento();

        DocumentoEmitido doc = new DocumentoEmitido();
        doc.setDataEmissao(new Date());
        doc.setEmitidoPor(UtilsFrontEnd.getProfissionalLogado());
        doc.setPathDocumento("\\app\\odonto\\documentos\\" + UtilsFrontEnd.getEmpresaLogada().getEmpStrNme() + "\\" + this.modeloSelecionado.getDescricao() + ".pdf");
        doc.setDocumentoModelo(this.modeloSelecionado);

        if (this.modeloSelecionado.getLayout() == null || this.modeloSelecionado.getLayout().isEmpty()) {
            this.modeloSelecionado.setLayout("A4");
        }

        switch (this.modeloSelecionado.getLayout()) {
            case "A3": {
                documento = new Document(PageSize.A3.rotate(), 30, 30, 30, 30);
            }
                break;
            case "A4": {
                documento = new Document(PageSize.A4.rotate(), 30, 30, 30, 30);
            }
                break;
            case "A5": {
                documento = new Document(PageSize.A5.rotate(), 30, 30, 30, 30);
            }
                break;
        }

        try {
            ByteArrayOutputStream outputData = new ByteArrayOutputStream();
            PdfWriter pdfWriter = PdfWriter.getInstance(documento, outputData);

            documento.open();
            documento.newPage();

            documento.add(new Paragraph(this.modeloSelecionado.getModelo()));

            //DocumentoEmitidoSingleton.getInstance().getBo().persist(doc);
            this.docEmitido = doc;

            File file = new File("\\app\\odonto\\documentos\\" + UtilsFrontEnd.getEmpresaLogada().getEmpStrNme() + "\\");
            file.mkdirs();

            documento.close();
            
            this.arqEmitido = new DefaultStreamedContent(new ByteArrayInputStream(outputData.toByteArray()));

            FileOutputStream out = new FileOutputStream(file + "\\" + this.modeloSelecionado.getDescricao() + ".pdf");
            out.write(outputData.toByteArray());

            outputData.flush();
            outputData.close();

            out.flush();
            out.close();
            
            this.setTeste(true);

        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), e.getMessage());
            e.printStackTrace();
        }
    }

    public void carregarDocumentoModelo() {
        if (modeloSelecionado != null) {
            List<TagDocumentoModelo> tags = modeloSelecionado.getTags();
            List<TagDocumentoModelo> tagsProcessadas = new ArrayList<>();
            String textoDocumento = modeloSelecionado.getModelo();

            for (TagDocumentoModelo tag : tags) {
                if (tag.getTagEntidade().getEntidade().getEntidade().equals("Paciente")) {
                    this.tag = TagSingleton.getInstance().getBo().listByEntidade("Paciente");
                }

                if (tag.getTagEntidade().getInserirDado().equals(Status.NAO)) {
                    if (tag.getTagEntidade().getEntidade().getEntidade().equals("Paciente")) {
                        this.tags.add(tag.getTagEntidade());
                    } else {
                        this.processarTag(tag.getTagEntidade(), textoDocumento);
                        tagsProcessadas.add(tag);
                    }
                } else {
                    this.tags.add(tag.getTagEntidade());
                }
            }
            
        }
    }

    public void construirModeloPDF() {
        Document documento = null;

        if (this.modeloSelecionado.getLayout() == null || this.modeloSelecionado.getLayout().isEmpty()) {
            this.modeloSelecionado.setLayout("A4");
        }

        switch (this.modeloSelecionado.getLayout()) {
            case "A3": {
                documento = new Document(PageSize.A3.rotate(), 30, 30, 30, 30);
            }
                break;
            case "A4": {
                documento = new Document(PageSize.A4.rotate(), 30, 30, 30, 30);
            }
                break;
            case "A5": {
                documento = new Document(PageSize.A5.rotate(), 30, 30, 30, 30);
            }
                break;
        }

        try {
            ByteArrayOutputStream outputData = new ByteArrayOutputStream();
            PdfWriter pdfWriter = PdfWriter.getInstance(documento, outputData);

            documento.open();
            documento.newPage();

            documento.add(new Paragraph(this.modeloSelecionado.getModelo()));

            documento.close();
            
            //this.setStreamOut(new ByteArrayInputStream(outputData.toByteArray()));
            
            outputData.flush();
            outputData.close();
            
            //this.setArquivoDownload(new DefaultStreamedContent(this.getStreamOut(), "application/pdf"));

        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void montarTags() {
        String regex = "\\{(.*?)\\}";
        String retorno = "";

        construirModeloPDF();
        
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        Matcher comparator = pattern.matcher(this.modeloSelecionado.getModelo());

        while (comparator.find()) {
            retorno = comparator.group();

            if (retorno != null && !retorno.isEmpty()) {
                retorno = retorno.replaceAll("\\{", "");
                retorno = retorno.replaceAll("\\}", "");
                String str[] = retorno.trim().split("-");

                if (str != null && str.length > 0) {
                    try {
                        if (str[0].equals("Custom")) {
                            TagEntidade tag = new TagEntidade();
                            Tag tagPai = new Tag();
                            tagPai.setEntidade("Custom");
                            
                            tag.setDescricaoCampo(str[1]);
                            tag.setInserirDado("S");
                            tag.setEntidade(tagPai);
                            
                            this.tagsDinamicas.add(tag);
                        } else {
                            TagEntidade tag = TagEntidadeSingleton.getInstance().getBo().findByAttributeAndEntidade(str[1], str[0]);

                            if (tag != null) {
                                if(tag.getEntidade().getEntidade().equals("Paciente"))
                                    this.tag = tag.getEntidade();
                                this.tags.add(tag);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        LogIntelidenteSingleton.getInstance().makeLog(e.getMessage());
                    }
                }
            }
        }
    }

    public List<CID> geraSugestoesCID(String query) {
        List<CID> suggestions = new ArrayList<>();
        try {
            //cids = CidSingleton.getInstance().getBo().listAll();
            suggestions = CidSingleton.getInstance().getBo().listSugestoesComplete(query, false);
        } catch (Exception e) {
            this.addError("Erro", "Falha ao carregar os CID");
        }
        return suggestions;
    }

    public void carregarDocumentos() {
        try {
            if (this.filtroTipoDocumentoEmitir != null) {
                this.listaDocumentosModelos = DocumentoSingleton.getInstance().getBo().listByTipoDocumento(filtroTipoDocumentoEmitir, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
            }
        } catch (Exception e) {
            e.printStackTrace();
            this.addError("Erro", "Não foi possível carregar os documentos");
        }
    }

    public void carregarTiposDocumento() {
        try {
            this.setListaTiposDocumentos(DominioSingleton.getInstance().getBo().listByObjeto("documento"));
        } catch (Exception e) {
            this.addError("Erro ao carregar tipos de documentos", "");
            e.printStackTrace();
        }
    }

    private void processarTag(TagEntidade tag, String modelo) {
        try {
            Class<?> c = null;
            switch (tag.getEntidade().getEntidade()) {
                case "Empresa": {
                    Empresa emp = UtilsFrontEnd.getEmpresaLogada();
                    c = emp.getClass();

                    Field campo = c.getDeclaredField(tag.getAtributo());
                    campo.setAccessible(true);
                    Object obj = campo.get(emp);

                    if (obj instanceof String) {
                        modelo = modelo.replaceAll("\\{" + tag.getEntidade().getEntidade() + "-" + tag.getAtributo() + "\\}", (String) obj);
                        this.modeloSelecionado.setModelo(modelo);
                    }
                }
                    break;
                case "Paciente": {
                    c = this.pacienteSelecionado.getDadosBasico().getClass();

                    Field campo = c.getDeclaredField(tag.getAtributo());
                    campo.setAccessible(true);
                    Object obj = campo.get(this.pacienteSelecionado.getDadosBasico());

                    if (obj instanceof String) {
                        modelo = modelo.replaceAll("\\{" + tag.getEntidade().getEntidade() + "-" + tag.getAtributo() + "\\}", (String) obj);
                        this.modeloSelecionado.setModelo(modelo);
                    }
                }
                    break;
                default: {
                    if (tag.getEntidade().getEntidadePai() != null) {

                    } else {
                        if (tag.getEntidade().getEntidade().equals("Custom")) {
                            if (tag.getRespTag() != null) {
                                modelo = modelo.replaceAll("\\{Custom-" + tag.getDescricaoCampo() + "\\}", tag.getRespTag());
                                this.modeloSelecionado.setModelo(modelo);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            this.addError("Erro na emissão", "Falha ao processar as tags do documento.");
            e.printStackTrace();
            LogIntelidenteSingleton.getInstance().makeLog(e);
        }
    }
    
    public void processarDocumento() {
        for(TagEntidade tag : this.tags) {
            this.processarTag(tag, this.modeloSelecionado.getModelo());
        }
        
        for(TagEntidade tag : this.tagsDinamicas) {
            this.processarTag(tag, this.modeloSelecionado.getModelo());
        }
    }
    
    public void processarCID() {
        if (this.cid != null) {
            this.modeloSelecionado.getModelo().replaceAll("\\{Atestado-cid\\}", (String) this.cid.getDescricao());
        }
    }

    public List<Paciente> sugestoesPacientes(String query) {
        try {
            return PacienteSingleton.getInstance().listSugestoesComplete(query, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            e.printStackTrace();
        }
        return null;
    }

    public void atualizarTags() {
        if (modeloSelecionado != null) {
            List<TagDocumentoModelo> tags = modeloSelecionado.getTags();
            List<TagDocumentoModelo> tagsProcessadas = new ArrayList<>();
            String textoDocumento = modeloSelecionado.getModelo();

            for (TagDocumentoModelo tag : tags) {
                if (tag.getTagEntidade().getInserirDado().equals(Status.NAO)) {
                    if (tag.getTagEntidade().getEntidade().getEntidade().equals("Paciente")) {
                        if (this.pacienteSelecionado != null) {
                            this.processarTag(tag.getTagEntidade(), textoDocumento);
                        }
                    } else {
                        this.processarTag(tag.getTagEntidade(), textoDocumento);
                        tagsProcessadas.add(tag);
                    }
                } else if (tag.getTagEntidade().getInserirDado().equals(Status.SIM)) {

                }
            }
        }
    }

    public Tag getTag() {
        return tag;
    }

    public void setTag(Tag tag) {
        this.tag = tag;
    }

    public Documento getModeloSelecionado() {
        return modeloSelecionado;
    }

    public void setModeloSelecionado(Documento modeloSelecionado) {
        this.modeloSelecionado = modeloSelecionado;
    }

    public List<Dominio> getListaTiposDocumentos() {
        return listaTiposDocumentos;
    }

    public void setListaTiposDocumentos(List<Dominio> listaTiposDocumentos) {
        this.listaTiposDocumentos = listaTiposDocumentos;
    }

    public Dominio getFiltroTipoDocumento() {
        return filtroTipoDocumento;
    }

    public void setFiltroTipoDocumento(Dominio filtroTipoDocumento) {
        this.filtroTipoDocumento = filtroTipoDocumento;
    }

    public List<Documento> getListaDocumentosModelos() {
        return listaDocumentosModelos;
    }

    public void setListaDocumentosModelos(List<Documento> listaDocumentosModelos) {
        this.listaDocumentosModelos = listaDocumentosModelos;
    }

    public List<TagEntidade> getTags() {
        return tags;
    }

    public void setTags(List<TagEntidade> tags) {
        this.tags = tags;
    }

    public List<Documento> getDocumentos() {
        return documentos;
    }

    public void setDocumentos(List<Documento> documentos) {
        this.documentos = documentos;
    }

    public Profissional getFiltroProfissionalEmissao() {
        return filtroProfissionalEmissao;
    }

    public void setFiltroProfissionalEmissao(Profissional filtroProfissionalEmissao) {
        this.filtroProfissionalEmissao = filtroProfissionalEmissao;
    }

    public Dominio getFiltroTipoDocumentoEmitir() {
        return filtroTipoDocumentoEmitir;
    }

    public void setFiltroTipoDocumentoEmitir(Dominio filtroTipoDocumentoEmitir) {
        this.filtroTipoDocumentoEmitir = filtroTipoDocumentoEmitir;
    }

    public Paciente getPacienteSelecionado() {
        return pacienteSelecionado;
    }

    public void setPacienteSelecionado(Paciente pacienteSelecionado) {
        this.pacienteSelecionado = pacienteSelecionado;
    }

    public CID getCid() {
        return cid;
    }

    public void setCid(CID cid) {
        this.cid = cid;
    }

    public DocumentoEmitido getDocEmitido() {
        return docEmitido;
    }

    public void setDocEmitido(DocumentoEmitido docEmitido) {
        this.docEmitido = docEmitido;
    }

    public List<TagEntidade> getTagsDinamicas() {
        return tagsDinamicas;
    }

    public void setTagsDinamicas(List<TagEntidade> tagsDinamicas) {
        this.tagsDinamicas = tagsDinamicas;
    }

    public StreamedContent getArqTemp() {
        return arqTemp;
    }

    public void setArqTemp(StreamedContent arqTemp) {
        this.arqTemp = arqTemp;
    }

    public StreamedContent getArqEmitido() {
        return arqEmitido;
    }

    public void setArqEmitido(StreamedContent arqEmitido) {
        this.arqEmitido = arqEmitido;
    }

    public boolean isTeste() {
        return teste;
    }

    public void setTeste(boolean teste) {
        this.teste = teste;
    }

}
