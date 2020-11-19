package br.com.lume.odonto.managed;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ActionEvent;

import org.apache.log4j.Logger;
import org.primefaces.PrimeFaces;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.extensions.component.ckeditor.CKEditor;
import org.primefaces.model.menu.DefaultMenuItem;
import org.primefaces.model.menu.DefaultMenuModel;
import org.primefaces.model.menu.DefaultSubMenu;
import org.primefaces.model.menu.MenuModel;

import br.com.lume.common.log.LogIntelidenteSingleton;
import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.HtmlToText;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.Status;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.documento.DocumentoSingleton;
import br.com.lume.dominio.DominioSingleton;
// import br.com.lume.odonto.bo.DocumentoBO;
// import br.com.lume.odonto.bo.DominioBO;
// import br.com.lume.odonto.bo.ProfissionalBO;
import br.com.lume.odonto.entity.Documento;
import br.com.lume.odonto.entity.Dominio;
import br.com.lume.odonto.entity.Tag;
import br.com.lume.odonto.entity.TagDocumentoModelo;
import br.com.lume.odonto.entity.TagEntidade;
import br.com.lume.tag.TagSingleton;
import br.com.lume.tagEntidade.TagEntidadeSingleton;

@ManagedBean
@ViewScoped
public class DocumentoMB extends LumeManagedBean<Documento> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(DocumentoMB.class);

    private List<String> legendas = new ArrayList<>();
    private List<Documento> documentos;
    private List<Dominio> dominios;
    private List<Tag> classificacaoTag;
    private String documento;
    private CKEditor ckEditor;    
    private MenuModel menuModel;

    private String novaTag;

    //FILTROS
    private Date dataInicio;
    private Date dataFim;
    private String filtroPeriodo;
    private Dominio filtroTipoDocumento;
    private List<Dominio> listaTiposDocumentos;
    private String status = "A";
    private List<Documento> listaDocumentosModelos;
    private Documento modeloSelecionado;

    private DataTable tabelaDocumentos;
    
    private boolean mostrarCabecalho = false;

    //  private DominioBO dominioBO;

    public DocumentoMB() {
        super(DocumentoSingleton.getInstance().getBo());
        this.setClazz(Documento.class);
        carregarTiposDocumento();
        pesquisar();
    }

    public void pesquisar() {
        Date dataInicial = null, dataFinal = null;
        
        Calendar c = Calendar.getInstance();
        if(dataFim != null) {
            c.setTime(this.dataFim);
            c.set(Calendar.HOUR_OF_DAY, 23);
            dataFinal = c.getTime();
        }
        if(dataInicio != null) {
            c.setTime(this.dataInicio);
            c.set(Calendar.HOUR_OF_DAY, 0);
            dataInicial = c.getTime();
        }
        
        this.documentos = DocumentoSingleton.getInstance().getBo().listByFiltros(dataInicial, dataFinal, filtroTipoDocumento, modeloSelecionado,
                this.status, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
    }

    public void carregarTiposDocumento() {
        try {
            this.listaTiposDocumentos = DominioSingleton.getInstance().getBo().listByObjeto("documento");
        } catch (Exception e) {
            this.addError("Erro ao carregar tipos de documentos", "");
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "Erro ao carregar documentos.");
            e.printStackTrace();
        }
    }
    
    public void carregarDocumentos() {
        try {
            if (this.filtroTipoDocumento != null) {
                this.listaDocumentosModelos = DocumentoSingleton.getInstance().getBo().listByTipoDocumento(filtroTipoDocumento, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
            }
        } catch (Exception e) {
            e.printStackTrace();
            this.addError("Erro", "Não foi possível carregar os documentos");
        }
    }

    public void carregarTags() {
        if (this.getEntity().getTipo() != null) {
            try {
                this.classificacaoTag = TagSingleton.getInstance().getBo().listAll();
            } catch (Exception e) {
                this.addError("Erro", "Erro ao buscar tags");
                e.printStackTrace();
            }
        }
    }

    public void actionAtivar(Documento doc) {
        try {
            doc.setAtivo(true);
            doc.setDataAlteracaoStatus(new Date());
            doc.setAlteradoPor(UtilsFrontEnd.getProfissionalLogado());
            DocumentoSingleton.getInstance().getBo().persist(doc);
            this.pesquisar();
            this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "Registro ativado.");
        }catch (Exception e) {
            e.printStackTrace();
            this.addError("Erro", "Erro ao ativar registro");
        }
    }
    
    public void actionInativar(Documento doc) {
        try {
            doc.setAtivo(false);
            doc.setDataAlteracaoStatus(new Date());
            doc.setAlteradoPor(UtilsFrontEnd.getProfissionalLogado());
            DocumentoSingleton.getInstance().getBo().persist(doc);
            this.pesquisar();
            this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "Registro inativado.");
        }catch (Exception e) {
            this.addError("Erro", "Erro ao inativar registro");
        }
    }
    
    public void carregarPaleta() {

        this.carregarTags();

        if (classificacaoTag != null) {
            menuModel = new DefaultMenuModel();

            for (Tag entidade : classificacaoTag) {

                DefaultSubMenu submenu = new DefaultSubMenu();
                submenu.setId(entidade.getEntidade());
                submenu.setLabel(entidade.getDescricao());

                menuModel.addElement(submenu);
                Collections.sort(entidade.getTags());
                for (TagEntidade tag : entidade.getTags()) {
                    DefaultMenuItem menuItem = new DefaultMenuItem();
                    menuItem.setId(tag.getAtributo());
                    menuItem.setTitle(tag.getDescricaoCampo());
                    menuItem.setValue(tag.getDescricaoCampo());
                    if(!tag.getDescricaoCampo().equals("Logo empresa")) {
                        menuItem.setOnclick("PrimeFaces.widgets.editor.instance.insertText('" + "#" + tag.getAtributo() + "" + "');");
                    }else {
                        menuItem.setOnclick("PrimeFaces.widgets.editor.instance.insertHtml('<img src=\"/app/odonto/imagens/" + UtilsFrontEnd.getEmpresaLogada().getEmpStrLogo() + "\"/>" + tag.getAtributo() + "');");
                    }

                    submenu.addElement(menuItem);
                }
            }
        }
    }

//    public void parserDocumento() {
//        FileWriter writer = null;
//        FileReader reader = null;
//        try {
//            HtmlToText parser = new HtmlToText();
//            //String modelo = this.getEntity().getModelo();
//            this.pageBreak();
//            
//            String path = "/app/odonto/modelo documentos/"+UtilsFrontEnd.getEmpresaLogada().getEmpStrNme()+"/";
//
//            File file = new File(path);
//            file.mkdirs();
//            
//            writer = new FileWriter(file+"/"+this.getEntity().getDescricao()+".html");
//            writer.write(this.getEntity().getModelo());
//            writer.flush();
//            
//            reader = new FileReader(new File(path+this.getEntity().getDescricao()+".html").getAbsolutePath());            
//            parser.parse(reader);
//            
//            this.getEntity().setModelo(parser.getText());
//            this.getEntity().setPathModelo(path+this.getEntity().getDescricao()+".html");
//        }catch (Exception e) {
//            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "Erro no parser do documento.");
//            e.printStackTrace();
//        }finally {
//            try {
//                writer.close();
//                reader.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }
//    
    private void pageBreak() {
        if(this.getEntity().getModelo().contains("page-break-after")) {
            this.getEntity().setModelo(this.getEntity().getModelo().replaceAll("<div style=\"page-break-after: always\"><span style=\"display:none\">&nbsp;</span></div>",
                    "<hr style=\\\"border-color:#aaa;box-sizing:border-box;width:100%;\\\">|new-page|</hr>"));
        }
    }
    
    @Override
    public void actionPersist(ActionEvent event) {
        try {
            this.getEntity().setIdEmpresa(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
            if(this.getEntity().getDescricao() != null && !this.getEntity().getDescricao().isEmpty()) {
                this.getEntity().setCriadoPor(UtilsFrontEnd.getProfissionalLogado());
                this.getEntity().setDataCriacao(new Date());
                
                this.getEntity().setModelo(this.documento);
                this.getEntity().setIdEmpresa(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
                
                //parserDocumento();

                DocumentoSingleton.getInstance().getBo().persist(this.getEntity());
                this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "Sucesso ao criar documento.");
                pesquisar();
            }else {
                this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "é necessário informar a descrição do documento.");
            }
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "Não foi possível salvar o documento.");
            e.printStackTrace();
        }
    }

    public List<TagDocumentoModelo> montarTags() {
        String regex = "\\{(.*?)\\}";
        String retorno = "";

        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        Matcher comparator = pattern.matcher(this.documento);

        List<TagDocumentoModelo> tagsDocumento = new ArrayList<TagDocumentoModelo>();

        while (comparator.find()) {
            retorno = comparator.group();

            if (retorno != null && !retorno.isEmpty()) {
                retorno = retorno.replaceAll("\\{","");
                retorno = retorno.replaceAll("\\}","");
                String str[] = retorno.trim().split("-");

                if (str != null && str.length > 0) {
                    try {
                        TagEntidade tag = TagEntidadeSingleton.getInstance().getBo().findByAttributeAndEntidade(str[1], str[0]);
                        
                        if (tag != null) {
                            TagDocumentoModelo tagDoc = new TagDocumentoModelo();
                            tagDoc.setDocumentoModelo(this.getEntity());

                            if (tag != null)
                                tagDoc.setTagEntidade(tag);

                            tagsDocumento.add(tagDoc);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        LogIntelidenteSingleton.getInstance().makeLog(e.getMessage());
                    }
                }
            }
        }

        return tagsDocumento;
    }

    public void controiModeloDocsAntigos(Documento doc) {
        if(doc.getModelo() == null || doc.getModelo().isEmpty()) {
            this.setEntity(doc);
            //parserDocumento();
            carregarDocumento(doc);
        }
    }
    
    public void carregarDocumento(Documento doc) {
        setEntity(doc);
        
        if(doc.getModelo() != null && !doc.getModelo().isEmpty()) {
            if(doc.getMostrarLogo() != null)
                this.setMostrarCabecalho((doc.getMostrarLogo().equals(Status.SIM)));

            this.documento = doc.getModelo();
            carregarPaleta();
            
            
        }else {
            this.controiModeloDocsAntigos(doc);
        }
    }
    
    public void inserirCabecalho() {
        if(this.mostrarCabecalho) {
            this.getEntity().setMostrarLogo("S");
        }
    }
    
    public void actionTrocaDatasCriacao() {
        try {

            this.dataInicio = getDataInicio(getFiltroPeriodo());
            this.dataFim = getDataFim(getFiltroPeriodo());

            PrimeFaces.current().ajax().update(":lume:dataInicial");
            PrimeFaces.current().ajax().update(":lume:dataFinal");

        } catch (Exception e) {
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
            } else if ("S".equals(filtro)) { //Últimos 7 dias              
                c.add(Calendar.DAY_OF_MONTH, -7);
                dataInicio = c.getTime();
            } else if ("Q".equals(filtro)) { //Últimos 15 dias              
                c.add(Calendar.DAY_OF_MONTH, -15);
                dataInicio = c.getTime();
            } else if ("T".equals(filtro)) { //Últimos 30 dias                
                c.add(Calendar.DAY_OF_MONTH, -30);
                dataInicio = c.getTime();
            } else if ("M".equals(filtro)) { //Mês Atual              
                c.set(Calendar.DAY_OF_MONTH, 1);
                dataInicio = c.getTime();
            } else if ("I".equals(filtro)) { //Mês Atual             
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
    
    @Override
    public void actionNew(ActionEvent event) {
        //super.actionNew(event);
       this.documento = ""; 
       menuModel = new DefaultMenuModel();
        this.setEntity(new Documento());
        if(this.getFiltroTipoDocumento() != null) {
            this.getEntity().setTipo(getFiltroTipoDocumento());
            carregarPaleta();
        }
    }

    public List<Documento> getDocumentos() {
        return documentos;
    }

    public void setDocumentos(List<Documento> documentos) {
        this.documentos = documentos;
    }

    public void addLegendas() {
        legendas = new ArrayList<>();
        legendas.add("#clinica_nome");
        legendas.add("#clinica_cnpj");
        legendas.add("#clinica_endereco");
        legendas.add("#clinica_numero");
        legendas.add("#clinica_complemento");
        legendas.add("#clinica_bairro");
        legendas.add("#clinica_cidade");
        legendas.add("#clinica_estado");
        legendas.add("#clinica_fone");
        legendas.add("#clinica_email");
        legendas.add("#clinica_cro_responsavel");
        legendas.add("#clinica_logo");
        if (this.getEntity() != null && this.getEntity().getTipo() != null) {
            if (this.getEntity().getTipo().getValor().equals("C")) {
                legendas.add("#contratado");
                legendas.add("#dataInicial");
                legendas.add("#dataFinal");
                legendas.add("#formaContratacao");
            } else {
                legendas.add("#paciente");
                legendas.add("#rg");
                legendas.add("#datahoje");
                legendas.add("#endereco_completo");
                legendas.add("#datanascimento");
                legendas.add("#documento");
                legendas.add("#telefone");
                legendas.add("#email");
                if (this.getEntity().getTipo().getValor().equals("T")) {
                    legendas.add("#plano_tratamento");
                    legendas.add("#profissional");
                    legendas.add("#cro");
                } else if (this.getEntity().getTipo().getValor().equals("A")) {
                    legendas.add("#dias");
                    legendas.add("#cid");
                } else if (this.getEntity().getTipo().getValor().equals("O")) {
                    legendas.add("#plano_tratamento");
                }
            }
        }
        //this.listAll();
    }

    public List<String> getLegendas() {
        return legendas;
    }

    public void setLegendas(List<String> legendas) {
        this.legendas = legendas;
    }

    public List<Dominio> getDominios() {
        return dominios;
    }

    public void setDominios(List<Dominio> dominios) {
        this.dominios = dominios;
    }

    public CKEditor getCkEditor() {
        return ckEditor;
    }

    public void setCkEditor(CKEditor ckEditor) {
        this.ckEditor = ckEditor;
    }

    public Dominio getFiltroTipoDocumento() {
        return filtroTipoDocumento;
    }

    public void setFiltroTipoDocumento(Dominio filtroTipoDocumento) {
        this.filtroTipoDocumento = filtroTipoDocumento;
    }

    public List<Dominio> getListaTiposDocumentos() {
        return listaTiposDocumentos;
    }

    public void setListaTiposDocumentos(List<Dominio> listaTiposDocumentos) {
        this.listaTiposDocumentos = listaTiposDocumentos;
    }

    public DataTable getTabelaDocumentos() {
        return tabelaDocumentos;
    }

    public void setTabelaDocumentos(DataTable tabelaDocumentos) {
        this.tabelaDocumentos = tabelaDocumentos;
    }

    public List<Tag> getClassificacaoTag() {
        return classificacaoTag;
    }

    public void setClassificacaoTag(List<Tag> classificacaoTag) {
        this.classificacaoTag = classificacaoTag;
    }

    public String getDocumento() {
        return documento;
    }

    public void setDocumento(String documento) {
        this.documento = documento;
    }

    public MenuModel getMenuModel() {
        return menuModel;
    }

    public void setMenuModel(MenuModel menuModel) {
        this.menuModel = menuModel;
    }

    public String getNovaTag() {
        return novaTag;
    }

    public void setNovaTag(String novaTag) {
        this.novaTag = novaTag;
    }

    public boolean isMostrarCabecalho() {
        return mostrarCabecalho;
    }

    public void setMostrarCabecalho(boolean mostrarCabecalho) {
        this.mostrarCabecalho = mostrarCabecalho;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

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

    public String getFiltroPeriodo() {
        return filtroPeriodo;
    }

    public void setFiltroPeriodo(String filtroPeriodo) {
        this.filtroPeriodo = filtroPeriodo;
    }

    public List<Documento> getListaDocumentosModelos() {
        return listaDocumentosModelos;
    }

    public void setListaDocumentosModelos(List<Documento> listaDocumentosModelos) {
        this.listaDocumentosModelos = listaDocumentosModelos;
    }

    public Documento getModeloSelecionado() {
        return modeloSelecionado;
    }

    public void setModeloSelecionado(Documento modeloSelecionado) {
        this.modeloSelecionado = modeloSelecionado;
    }    
  
}