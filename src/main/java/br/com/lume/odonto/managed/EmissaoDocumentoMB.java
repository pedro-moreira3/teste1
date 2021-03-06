package br.com.lume.odonto.managed;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.faces.event.ActionEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

import org.primefaces.PrimeFaces;
import org.primefaces.extensions.component.ckeditor.CKEditor;
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
import br.com.lume.common.util.Utils;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.documento.DocumentoSingleton;
import br.com.lume.documentoEmitido.DocumentoEmitidoSingleton;
import br.com.lume.dominio.DominioSingleton;
import br.com.lume.odonto.entity.CID;
import br.com.lume.odonto.entity.Documento;
import br.com.lume.odonto.entity.DocumentoEmitido;
import br.com.lume.odonto.entity.Dominio;
import br.com.lume.odonto.entity.Paciente;
import br.com.lume.odonto.entity.Profissional;
import br.com.lume.odonto.entity.Tag;
import br.com.lume.odonto.entity.TagDocumentoModelo;
import br.com.lume.odonto.entity.TagEntidade;
import br.com.lume.paciente.PacienteSingleton;
import br.com.lume.profissional.ProfissionalSingleton;
import br.com.lume.security.entity.Empresa;
import br.com.lume.tag.TagSingleton;
import br.com.lume.tagEntidade.TagEntidadeSingleton;

@Named
@ViewScoped
public class EmissaoDocumentoMB extends LumeManagedBean<DocumentoEmitido> {

    private static final long serialVersionUID = 1L;

    private Documento modeloSelecionado;
    private List<Dominio> listaTiposDocumentos;
    private Dominio filtroTipoDocumento;
    private Dominio filtroTipoDocumentoEmitir;
    private List<Documento> listaDocumentosModelos;
    private List<TagEntidade> tags = new ArrayList<TagEntidade>();
    private List<TagEntidade> tagsDinamicas = new ArrayList<TagEntidade>();
    private List<Documento> documentos;
    private Profissional filtroProfissionalEmissao;
    private Paciente pacienteSelecionado;
    private Profissional profissionalSelecionado;
    private CID cid;
    private DocumentoEmitido docEmitido;
    private boolean teste = false;
    private String modeloHtml = "";
    private StringBuilder cabecalhoHtml = new StringBuilder("");
    private StringBuilder modeloHtmlSemCabecalho = new StringBuilder("");
    private StringBuilder rodapeHtml = new StringBuilder("");

    private BigDecimal somatorioValorTotal = new BigDecimal(0);

    private Tag tag;
    private Tag tagPlano;

    //Filtros
    private Date dataInicio;
    private Date dataFim;
    private Paciente emitidoPara;
    private String filtroPeriodo = "H";

    private StreamedContent arqTemp;
    private StreamedContent arqEmitido;

    private boolean mostraCabecalho = false;
    private boolean mostraLogo = false;
    private boolean mostraRodape = false;
    private boolean mostraLogoCentral = false;
    //  private String cabecalho = "";

    private CKEditor ckEditorEmissao;

    private boolean mostrarProf;

    public EmissaoDocumentoMB() {
        super(DocumentoEmitidoSingleton.getInstance().getBo());
        this.setClazz(DocumentoEmitido.class);
        this.carregarTiposDocumento();
        pesquisar();
    }

    public void pesquisar() {
        Date dataInicial = null, dataFinal = null;

        actionTrocaDatasCriacao();
        if (getDataInicio() != null && getDataFim() != null) {
            Calendar c = Calendar.getInstance();
            c.setTime(getDataInicio());
            c.add(Calendar.HOUR_OF_DAY, 0);
            c.set(Calendar.MINUTE, 0);
            c.set(Calendar.SECOND, 0);
            dataInicial = c.getTime();

            c.setTime(getDataFim());
            c.add(Calendar.HOUR_OF_DAY, 23);
            c.set(Calendar.MINUTE, 59);
            c.set(Calendar.SECOND, 59);
            dataFinal = c.getTime();
        }

        if (UtilsFrontEnd.getProfissionalLogado() != null) {
            this.setEntityList(DocumentoEmitidoSingleton.getInstance().getBo().listByFiltros(dataInicial, dataFinal, filtroProfissionalEmissao, getEmitidoPara(), filtroTipoDocumento,
                    UtilsFrontEnd.getProfissionalLogado().getIdEmpresa()));
            if (getEntityList().size() > 0) {
                for (DocumentoEmitido doc : getEntityList()) {
                    if (doc.getValor() != 0) {
                        somatorioValorTotal.add(new BigDecimal(doc.getValor())).setScale(2, BigDecimal.ROUND_HALF_UP);
                    }
                }
            } else {
                somatorioValorTotal = new BigDecimal(0);
            }

        }
    }

    public List<Documento> sugestoesModelos(String query) {
        return DocumentoSingleton.getInstance().getBo().listSugestoesComplete(query, filtroTipoDocumentoEmitir, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
    }

    public List<Profissional> sugestoesProfissionais(String query) {
        return ProfissionalSingleton.getInstance().getBo().listSugestoesCompleteDentista(query, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa(), true);
    }

    public void emitirNovoDocumento() {
        try {
            DocumentoEmitido doc = new DocumentoEmitido();
            doc.setId(0l);
            doc.setDataEmissao(new Date());
            doc.setEmitidoPor(UtilsFrontEnd.getProfissionalLogado());
            doc.setEmitidoPara(pacienteSelecionado);
            doc.setProfissionalSelecionado(profissionalSelecionado);
            doc.setDocumentoModelo(modeloSelecionado);
            doc.setModelo(modeloHtml);
            doc.setTipoDoc(modeloSelecionado.getTipo());
            ckEditorEmissao.getValue();
            DocumentoEmitidoSingleton.getInstance().getBo().persist(doc);
            this.setEntity(doc);

            this.docEmitido = doc;
            this.pesquisar();
            this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "Emiss??o do documento realizada com sucesso.");
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "N??o foi poss??vel emitir o documento");
        }
    }

    public void emitirDocumento() {
        Document documento = null;

        // this.processarDocumento();

        DocumentoEmitido doc = new DocumentoEmitido();
        doc.setDataEmissao(new Date());
        doc.setEmitidoPor(UtilsFrontEnd.getProfissionalLogado());
        doc.setProfissionalSelecionado(profissionalSelecionado);
        if (this.pacienteSelecionado != null) {
            doc.setEmitidoPara(this.pacienteSelecionado);
            doc.setPathDocumento(
                    "/app/odonto/documentos/" + UtilsFrontEnd.getEmpresaLogada().getEmpStrNme() + "/" + this.modeloSelecionado.getDescricao() + "-" + this.pacienteSelecionado.getId() + ".pdf");
        } else {
            doc.setPathDocumento("/app/odonto/documentos/" + UtilsFrontEnd.getEmpresaLogada().getEmpStrNme() + "/" + this.modeloSelecionado.getDescricao() + ".pdf");
        }

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

            if (this.modeloSelecionado.getModelo().contains("|new-page|")) {
                String buf[] = this.modeloSelecionado.getModelo().split("\\|(.*?)\\|");
                for (int i = 0; i < buf.length; i++) {
                    documento.add(new Paragraph(buf[i]));
                    documento.newPage();
                }
            } else {
                documento.add(new Paragraph(this.modeloSelecionado.getModelo()));
            }

            DocumentoEmitidoSingleton.getInstance().getBo().persist(doc);
            this.docEmitido = doc;

            File file = new File("/app/odonto/documentos/" + UtilsFrontEnd.getEmpresaLogada().getEmpStrNme() + "/");
            file.mkdirs();

            documento.close();

            FileOutputStream out = new FileOutputStream(file + "/" + this.modeloSelecionado.getDescricao() + (this.pacienteSelecionado != null ? "-" + this.pacienteSelecionado.getId() : "") + ".pdf");
            out.write(outputData.toByteArray());

            outputData.flush();
            outputData.close();

            out.flush();
            out.close();

            this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "Emiss??o do documento realizada com sucesso.");

            this.setEntity(doc);
            this.pesquisar();

        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), e.getMessage());
            e.printStackTrace();
        }
    }

    public StreamedContent getArquivo(DocumentoEmitido doc) {

        StreamedContent arquivo = null;
        if (doc.getPathDocumento() != null) {
            try {
                File file = new File("/app/odonto/documentos/" + UtilsFrontEnd.getEmpresaLogada().getEmpStrNme() + "/");
                FileInputStream in = new FileInputStream(file + "/" + doc.getDocumentoModelo().getDescricao() + "-" + doc.getEmitidoPara().getId() + ".pdf");
                arquivo = DefaultStreamedContent.builder().name(doc.getDocumentoModelo().getDescricao() + ".pdf").contentType("application/pdf").stream(() -> {
                    return in;
                }).build();
            } catch (Exception e) {
                this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "N??o foi poss??vel carregar o documento.");
                e.printStackTrace();
            }
        }
        return arquivo;
    }

    public StreamedContent getArquivo() {
        return this.getArquivo(this.getEntity());
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

    public void loadDoc(DocumentoEmitido doc) {
        if (doc != null) {
            this.modeloHtml = doc.getModelo();
        }
        PrimeFaces.current().ajax().update("lume:impressaoDoc");
//        modeloHtml.append("<div id='pageFooter'>P??gina </div>"); 
//        
//        
//      
//            modeloHtml.append("<style>@media screen { .pageFooter {display: none;} } @media print { .pageFooter {display: block;}}"
//                    + "#content { display: table; } #pageFooter { display: table-footer-group; } #pageFooter:after { counter-increment: page; content: counter(page);  }</style>");
//             
        // modeloHtml.insert(0,"<style>@page { @bottom-right {    content: \"P??gina \" counter(page) \" de \" counter(pages);}}</style>");

    }

    public void copiar(DocumentoEmitido doc) {
        DocumentoEmitido novo = new DocumentoEmitido();
        if (doc != null) {

            this.carregarDocumentos();

            carregarTiposDocumento();

            this.filtroTipoDocumentoEmitir = doc.getDocumentoModelo().getTipo();

            novo.setDataEmissao(doc.getDataEmissao());
            novo.setDocumentoModelo(doc.getDocumentoModelo());
            novo.setEmitidoPara(doc.getEmitidoPara());
            novo.setEmitidoPor(doc.getEmitidoPor());
            novo.setModelo(doc.getModelo());
            this.setEntity(novo);
            pacienteSelecionado = null;
            profissionalSelecionado = null;
            modeloSelecionado = doc.getDocumentoModelo();
            montarTags(doc.getModelo());
        }

    }

    public void replaceDocExistente() {
        //DocumentoSingleton.getInstance().getBo().replaceDocumentoAntigo(tagDinamicas, dadosBasico, documento, profissionalLogado);
    }

    public void montarTags(String textoEditor) {
        if (textoEditor == null || textoEditor.equals("")) {
            textoEditor = this.modeloSelecionado.getModelo();
        } else {
            this.modeloSelecionado.setModelo(textoEditor);
        }

        mostrarProf = false;
        if (textoEditor.contains("#registroConselho") || textoEditor.contains("#nmeProfissional")) {
            mostrarProf = true;
        }

        String regex = "#(\\w+|\\W+)";
        String retorno = "";

        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        Matcher comparator = pattern.matcher(textoEditor);

        this.modeloHtmlSemCabecalho = new StringBuilder(textoEditor);
        //carregarDocumentoHtmlModelo();
        this.tagsDinamicas = new ArrayList<TagEntidade>();
        List<String> listaTagsExistentes = new ArrayList<String>();
        while (comparator.find()) {
            retorno = comparator.group();

            if (retorno != null && !retorno.isEmpty()) {
                retorno = retorno.replaceAll("\\#", "");
                //  retorno = retorno.replaceAll("\\}", "");
                //  String str[] = retorno.trim().split("-");

                if (retorno != null) {
                    try {

                        TagEntidade tag = TagEntidadeSingleton.getInstance().getBo().findByAttribute(retorno.trim());

                        if (tag != null) {
                            if (tag.getEntidade().getEntidade().equals("Paciente")) {
                                this.tag = tag.getEntidade();
                                this.tags.add(tag);
                            } else if (tag.getEntidade().getEntidade().equals("Empresa") || tag.getEntidade().getEntidade().equals("Sistema")) {
                                this.tags.add(tag);
                            } else if (tag.getEntidade().getEntidade().equals("PlanoTratamento")) {
                                this.tagPlano = tag.getEntidade();
                                this.tags.add(tag);
                            } else {
                                if (!listaTagsExistentes.contains(retorno)) {
                                    this.tagsDinamicas.add(tag);
                                }
                            }
                            listaTagsExistentes.add(retorno);
                        } else {
                            //tags docs antigos
                            if (!retorno.contains("rg") && !retorno.contains("endereco_completo") && !retorno.contains("datahoje") && !retorno.contains("profissional") && !retorno.contains(
                                    "sexo") && !retorno.contains("idade") && !retorno.contains(
                                            "datanascimento") && !retorno.contains("documento") && !retorno.contains("telefone") && !retorno.contains("email") && !retorno.contains("paciente")) {
                                TagEntidade tagNova = new TagEntidade();
                                Tag tagPai = new Tag();
                                tagPai.setEntidade("Custom");
                                tagNova.setDescricaoCampo(retorno);
                                tagNova.setInserirDado("S");
                                tagNova.setTipoAtributo("texto");
                                tagNova.setEntidade(tagPai);
                                if (!listaTagsExistentes.contains(retorno)) {
                                    this.tagsDinamicas.add(tagNova);
                                }
                                listaTagsExistentes.add(retorno);
                            }
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        LogIntelidenteSingleton.getInstance().makeLog(e.getMessage());
                    }
                }
            }
        }

        if (this.tag == null && this.validaRecebedor()) {
            this.tag = TagSingleton.getInstance().getBo().listByEntidade("Paciente");
        }
        processarDocumento();

    }

    public void montaCabecalho() {

    }

    public boolean validaRecebedor() {
        if (this.modeloSelecionado.getTipo().getNome().equals("Faturamento") || this.modeloSelecionado.getTipo().getNome().equals("Contrato Profissional"))
            return false;
        return true;
    }

    public void preview() {
        if (this.modeloHtml.toString().contains("|new-page|")) {
            this.modeloHtml = this.modeloHtml.toString().replaceAll("\\|new-page\\|", "\n\n<hr style=\\\"border-color:#aaa;box-sizing:border-box;width:100%;\\\"></hr>\n");
        }
    }

    public List<CID> geraSugestoesCID(String query) {
        List<CID> suggestions = new ArrayList<>();
        try {
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
            this.addError("Erro", "N??o foi poss??vel carregar os documentos");
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

    public void atualizarTag() {
        for (TagEntidade tag : this.tags) {
            if (tag.getEntidade().getEntidade().equals("Paciente") || tag.getEntidade().getEntidade().equals("Atestado")) {
                if (tag.getEntidade().getEntidade().equals("Paciente") && this.pacienteSelecionado != null)
                    this.processarTag(tag, this.modeloSelecionado.getModelo());
                else if (tag.getEntidade().getEntidade().equals("Atestado") && this.cid != null)
                    this.processarTag(tag, this.modeloSelecionado.getModelo());
            } else {
                this.processarTag(tag, this.modeloSelecionado.getModelo());
            }

        }

        for (TagEntidade tag : this.tagsDinamicas) {
            if (tag.getRespTag() != null || tag.getRespTagData() != null || (tag.getDescricaoCampo().equals("CID") && this.cid != null)) {
                this.processarTag(tag, this.modeloSelecionado.getModelo());
            }
            if (tag.getAtributo() != null && (tag.getAtributo().equals("registroConselho") || tag.getAtributo().equals("nmeProfissional"))) {
                this.processarTag(tag, this.modeloSelecionado.getModelo());
            }

        }

        preview();
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
                        modelo = modelo.replaceAll("\\#" + tag.getAtributo(), (obj != null ? (String) obj : ""));
                    } else if (obj == null) {
                        modelo = modelo.replaceAll("\\#" + tag.getAtributo(), "");
                    }
                }
                    break;
                case "Profissional": {
                    if (this.profissionalSelecionado != null) {
                        c = this.profissionalSelecionado.getDadosBasico().getClass();
                        if (this.profissionalSelecionado.getRegistroConselhoStr() != null && !this.profissionalSelecionado.getRegistroConselhoStr().isEmpty()) {
                            modelo = modelo.replaceAll("\\#registroConselho", this.profissionalSelecionado.getRegistroConselhoStr());
                        }

                        modelo = modelo.replaceAll("\\#nmeProfissional", this.profissionalSelecionado.getDadosBasico().getNome());
                    }

                }
                    break;
                case "Paciente": {
                    if (this.pacienteSelecionado != null) {
                        c = this.pacienteSelecionado.getDadosBasico().getClass();

                        //tags q nao sao por reflexao
                        if (tag.getAtributo().contains("empresaOndeTrabalha") || tag.getAtributo().contains("profissaoPaciente") || tag.getAtributo().contains(
                                "idadePaciente") || tag.getAtributo().contains("convenioPaciente")) {
                            if (this.pacienteSelecionado.getDadosBasico().getEmpresaOndeTrabalha() != null && !this.pacienteSelecionado.getDadosBasico().getEmpresaOndeTrabalha().equals("")) {
                                modelo = modelo.replaceAll("\\#empresaOndeTrabalha", this.pacienteSelecionado.getDadosBasico().getEmpresaOndeTrabalha());
                            }
                            if (this.pacienteSelecionado.getDadosBasico().getProfissao() != null && !this.pacienteSelecionado.getDadosBasico().getProfissao().equals("")) {
                                modelo = modelo.replaceAll("\\#profissaoPaciente", this.pacienteSelecionado.getDadosBasico().getProfissao());
                            }
                            if (!Utils.getIdadePaciente(this.pacienteSelecionado).equals("")) {
                                modelo = modelo.replaceAll("\\#idadePaciente", Utils.getIdadePaciente(this.pacienteSelecionado) + " anos");
                            }
                            if (this.pacienteSelecionado.getConvenio() != null && this.pacienteSelecionado.getConvenio().getDadosBasico().getNome() != null && !this.pacienteSelecionado.getDadosBasico().getNome().equals(
                                    "")) {
                                modelo = modelo.replaceAll("\\#convenioPaciente", this.pacienteSelecionado.getConvenio().getDadosBasico().getNome());
                            }
                        } else {
                            Field campo = c.getDeclaredField(tag.getAtributo());
                            campo.setAccessible(true);
                            Object obj = campo.get(this.pacienteSelecionado.getDadosBasico());

                            if (obj instanceof String) {
                                modelo = modelo.replaceAll("\\#" + tag.getAtributo(), (obj != null ? (String) obj : ""));
                            } else if (obj instanceof Date) {
                                Date data = (Date) obj;
                                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

                                modelo = modelo.replaceAll("\\#" + tag.getAtributo(), sdf.format(data));
                            } else if (obj == null) {
                                modelo = modelo.replaceAll("\\#" + tag.getAtributo(), (obj != null ? (String) obj : ""));
                            }
                        }

                    }

                }
                    break;

                case "Sistema": {
                    modelo = modelo.replaceAll("\\#" + tag.getAtributo(), this.moduloSistema(tag.getAtributo()));
                }
                    break;
                default: {
                    if (tag.getEntidade().getEntidadePai() != null) {

                    } else {
                        if (tag.getAtributo() != null && tag.getAtributo().equals("cid")) {
                            if (this.cid != null) {
                                modelo = modelo.replaceAll("\\#" + tag.getAtributo(), this.cid.getId());
                            }

                        } else if (tag.getEntidade().getEntidade().equals("Custom")) {
                            if (tag.getRespTag() != null) {

                                modelo = modelo.replaceAll("\\#" + tag.getDescricaoCampo(), tag.getRespTag());

                                //  modelo = modelo.replaceAll(Pattern.quote("#(\\w+|\\W+)"),tag.getRespTag());

                            }
                        } else {
                            if (tag.getRespTagData() != null) {
                                Date data = tag.getRespTagData();
                                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

                                modelo = modelo.replaceAll("\\#" + tag.getAtributo(), sdf.format(data));
                            }
                        }
                    }
                }
            }
            //tags dos documentos antigos
            if (pacienteSelecionado != null && pacienteSelecionado.getDadosBasico() != null) {
                modelo = DocumentoSingleton.getInstance().getBo().replaceDocumento(pacienteSelecionado.getDadosBasico(), modelo, UtilsFrontEnd.getProfissionalLogado());
            }
            this.modeloSelecionado.setModelo(modelo);

            modeloHtmlSemCabecalho = new StringBuilder("");
            modeloHtmlSemCabecalho.append(modelo);
            modeloHtml = modeloHtmlSemCabecalho.toString();
        } catch (Exception e) {
            this.addError("Erro na emiss??o", "Falha ao processar as tags do documento.");
            e.printStackTrace();
            LogIntelidenteSingleton.getInstance().makeLog(e);
        }
    }

    private String moduloSistema(String v) {

        switch (v) {
            case "dataAtual":
                return Utils.dateToString(new Date(), "dd/MM/yyyy");
            case "horaAtual":
                return Utils.dateToString(new Date(), "HH:mm");
            case "profissionalLogado":
                return UtilsFrontEnd.getProfissionalLogado().getDadosBasico().getNome();
        }
        return "";
    }

    @Override
    public void actionNew(ActionEvent event) {
//        super.actionNew(event);
        this.setEntity(null);

        if (this.filtroTipoDocumento != null) {
            this.filtroTipoDocumentoEmitir = this.filtroTipoDocumento;
            this.carregarDocumentos();
        }
        this.setPacienteSelecionado(null);
        if (emitidoPara != null) {
            this.setPacienteSelecionado(emitidoPara);
        }

        this.setCid(null);

        this.setProfissionalSelecionado(null);
        this.setModeloSelecionado(null);
        this.modeloHtml = "";
        this.tags = new ArrayList<TagEntidade>();
        this.tagsDinamicas = new ArrayList<TagEntidade>();
        this.tag = null;
        mostraCabecalho = false;
        mostraLogo = false;
        //  mostraRodape = false;
    }

    public void processarDocumento() {
        for (TagEntidade tag : this.tags) {
            this.processarTag(tag, this.modeloSelecionado.getModelo());
        }

        for (TagEntidade tag : this.tagsDinamicas) {
            this.processarTag(tag, this.modeloSelecionado.getModelo());
        }

        preview();
    }

    public void processarCID() {
        if (this.cid != null) {
            this.modeloSelecionado.getModelo().replaceAll("\\{Atestado-cid\\}", (String) this.cid.getDescricao());
            this.atualizarTag();
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

    public void actionTrocaDatasCriacao() {
        try {

            this.dataInicio = getDataInicio(filtroPeriodo);
            this.dataFim = getDataFim(filtroPeriodo);

            PrimeFaces.current().ajax().update(":lume:dataInicial");
            PrimeFaces.current().ajax().update(":lume:dataFinal");

        } catch (Exception e) {
            this.addError("Erro no intervalo de datas", "");
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
        }
    }

    public Date getDataFim(String filtro) {
        Date dataFim = null;
        try {
            Calendar c = Calendar.getInstance();
            if ("O".equals(filtro)) {
                c.add(Calendar.DAY_OF_MONTH, -1);
                dataFim = c.getTime();
            } else if (filtro == null) {
                dataFim = null;
            } else {
                dataFim = c.getTime();
            }
            return dataFim;
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            return null;
        }
    }

    public Date getDataInicio(String filtro) {
        Date dataInicio = null;
        try {
            Calendar c = Calendar.getInstance();
            if ("O".equals(filtro)) {
                c.add(Calendar.DAY_OF_MONTH, -1);
                dataInicio = c.getTime();
            } else if ("H".equals(filtro)) { //Hoje                
                dataInicio = c.getTime();
            } else if ("S".equals(filtro)) { //??ltimos 7 dias              
                c.add(Calendar.DAY_OF_MONTH, -7);
                dataInicio = c.getTime();
            } else if ("Q".equals(filtro)) { //??ltimos 15 dias              
                c.add(Calendar.DAY_OF_MONTH, -15);
                dataInicio = c.getTime();
            } else if ("T".equals(filtro)) { //??ltimos 30 dias                
                c.add(Calendar.DAY_OF_MONTH, -30);
                dataInicio = c.getTime();
            } else if ("M".equals(filtro)) { //M??s Atual              
                c.set(Calendar.DAY_OF_MONTH, 1);
                dataInicio = c.getTime();
            } else if ("I".equals(filtro)) { //M??s Atual             
                c.add(Calendar.MONTH, -6);
                dataInicio = c.getTime();
            }
            return dataInicio;
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            return null;
        }
    }

    private boolean validarIntervaloDatas() {

        if ((dataInicio != null && dataFim != null) && dataInicio.getTime() > dataFim.getTime()) {
            this.addError("Intervalo de datas", "A data inicial deve preceder a data final.", true);
            return false;
        }
        return true;
    }

//  public void construirModeloPDF() {
//  Document documento = null;
//
//  if (this.modeloSelecionado.getLayout() == null || this.modeloSelecionado.getLayout().isEmpty()) {
//      this.modeloSelecionado.setLayout("A4");
//  }
//
//  switch (this.modeloSelecionado.getLayout()) {
//      case "A3": {
//          documento = new Document(PageSize.A3.rotate(), 30, 30, 30, 30);
//      }
//          break;
//      case "A4": {
//          documento = new Document(PageSize.A4.rotate(), 30, 30, 30, 30);
//      }
//          break;
//      case "A5": {
//          documento = new Document(PageSize.A5.rotate(), 30, 30, 30, 30);
//      }
//          break;
//  }
//
//  try {
//      ByteArrayOutputStream outputData = new ByteArrayOutputStream();
//      PdfWriter pdfWriter = PdfWriter.getInstance(documento, outputData);
//
//      documento.open();
//      documento.newPage();
//      
//      documento.add(new Paragraph(this.modeloSelecionado.getModelo()));
//
//      documento.close();
//      
//      //this.setStreamOut(new ByteArrayInputStream(outputData.toByteArray()));
//      
//      outputData.flush();
//      outputData.close();
//      
//      //this.setArquivoDownload(new DefaultStreamedContent(this.getStreamOut(), "application/pdf"));
//
//  } catch (Exception e) {
//      this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), e.getMessage());
//      e.printStackTrace();
//  }
//}

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

    public String getModeloHtml() {
        return modeloHtml.toString();
    }

//    public void setModeloHtml(String modeloHtml) {
//        this.modeloHtml = modeloHtml;
//    }

    public Date getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(Date dataInicio) {
        this.dataInicio = dataInicio;
    }

    public Date getDataFim() {
        return dataFim;
    }

    public void setDataFim(Date dataFim) {
        this.dataFim = dataFim;
    }

    public Paciente getEmitidoPara() {
        return emitidoPara;
    }

    public void setEmitidoPara(Paciente emitidoPara) {
        this.emitidoPara = emitidoPara;
    }

    public String getFiltroPeriodo() {
        return filtroPeriodo;
    }

    public void setFiltroPeriodo(String filtroPeriodo) {
        this.filtroPeriodo = filtroPeriodo;
    }

    public Tag getTagPlano() {
        return tagPlano;
    }

    public void setTagPlano(Tag tagPlano) {
        this.tagPlano = tagPlano;
    }

    public boolean isMostraCabecalho() {
        return mostraCabecalho;
    }

    public void setMostraCabecalho(boolean mostraCabecalho) {
        this.mostraCabecalho = mostraCabecalho;
    }

    public boolean isMostraLogo() {
        return mostraLogo;
    }

    public void setMostraLogo(boolean mostraLogo) {
        this.mostraLogo = mostraLogo;
    }

    public StringBuilder getCabecalhoHtml() {
        return cabecalhoHtml;
    }

    public void setCabecalhoHtml(StringBuilder cabecalhoHtml) {
        this.cabecalhoHtml = cabecalhoHtml;
    }

    public StringBuilder getModeloHtmlSemCabecalho() {
        return modeloHtmlSemCabecalho;
    }

    public void setModeloHtmlSemCabecalho(StringBuilder modeloHtmlSemCabecalho) {
        this.modeloHtmlSemCabecalho = modeloHtmlSemCabecalho;
    }

    public StringBuilder getRodapeHtml() {
        return rodapeHtml;
    }

    public void setRodapeHtml(StringBuilder rodapeHtml) {
        this.rodapeHtml = rodapeHtml;
    }

    public Profissional getProfissionalSelecionado() {
        return profissionalSelecionado;
    }

    public void setProfissionalSelecionado(Profissional profissionalSelecionado) {
        this.profissionalSelecionado = profissionalSelecionado;
    }

    public CKEditor getCkEditorEmissao() {
        return ckEditorEmissao;
    }

    public void setCkEditorEmissao(CKEditor ckEditorEmissao) {
        this.ckEditorEmissao = ckEditorEmissao;
    }

    public void setModeloHtml(String modeloHtml) {
        this.modeloHtml = modeloHtml;
    }

    public boolean isMostrarProf() {
        return mostrarProf;
    }

    public void setMostrarProf(boolean mostrarProf) {
        this.mostrarProf = mostrarProf;
    }

    public boolean isMostraRodape() {
        return mostraRodape;
    }

    public void setMostraRodape(boolean mostraRodape) {
        this.mostraRodape = mostraRodape;
    }

    public boolean isMostraLogoCentral() {
        return mostraLogoCentral;
    }

    public void setMostraLogoCentral(boolean mostraLogoCentral) {
        this.mostraLogoCentral = mostraLogoCentral;
    }

    public BigDecimal getSomatorioValorTotal() {
        return somatorioValorTotal;
    }

    public void setSomatorioValorTotal(BigDecimal somatorioValorTotal) {
        this.somatorioValorTotal = somatorioValorTotal;
    }

}
