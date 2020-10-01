package br.com.lume.odonto.managed;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ActionEvent;

import org.apache.log4j.Logger;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.extensions.component.ckeditor.CKEditor;
import org.primefaces.model.menu.DefaultMenuItem;
import org.primefaces.model.menu.DefaultMenuModel;
import org.primefaces.model.menu.DefaultSubMenu;
import org.primefaces.model.menu.MenuModel;

import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.documento.DocumentoSingleton;
import br.com.lume.dominio.DominioSingleton;
// import br.com.lume.odonto.bo.DocumentoBO;
// import br.com.lume.odonto.bo.DominioBO;
// import br.com.lume.odonto.bo.ProfissionalBO;
import br.com.lume.odonto.entity.Documento;
import br.com.lume.odonto.entity.Dominio;
import br.com.lume.odonto.entity.Tag;
import br.com.lume.odonto.entity.TagEntidade;
import br.com.lume.tag.TagSingleton;

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

    //FILTROS
    private Dominio filtroTipoDocumento;
    private List<Dominio> listaTiposDocumentos;

    private DataTable tabelaDocumentos;

    //  private DominioBO dominioBO;

    public DocumentoMB() {
        super(DocumentoSingleton.getInstance().getBo());
        this.setClazz(Documento.class);
        carregarTiposDocumento();
    }

    public void pesquisar() {
        this.documentos = DocumentoSingleton.getInstance().getBo().listByFiltros(filtroTipoDocumento, 
                null, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
    }
    
    public void carregarTiposDocumento() {
        try {
            this.listaTiposDocumentos = DominioSingleton.getInstance().getBo().listByObjeto("documento");
        } catch (Exception e) {
            this.addError("Erro ao carregar tipos de documentos", "");
            e.printStackTrace();
        }
    }
    
    public void carregarTags() {
        if(this.getEntity().getTipo() != null) {
            this.classificacaoTag = TagSingleton.getInstance().getBo().listByTipoDocumento(this.getEntity().getTipo());
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

                for (TagEntidade tag : entidade.getTags()) {
                    DefaultMenuItem menuItem = new DefaultMenuItem();
                    menuItem.setId(tag.getAtributo());
                    menuItem.setTitle(tag.getDescricaoCampo());
                    menuItem.setValue(tag.getDescricaoCampo());
                    menuItem.setOnclick("PrimeFaces.widgets.editor.instance.insertText('" + "{" + 
                            tag.getEntidade().getEntidade() + " - " + tag.getAtributo() + "}" + "');");

                    submenu.addElement(menuItem);
                }
            }
        }
    }
    
//    public void listAll() {
//        try {
//            dominios = DominioSingleton.getInstance().getBo().listByEmpresaAndObjetoAndTipo("documento", "tipo");
//            if (this.getEntity() != null && this.getEntity().getTipo() != null) {
//                documentos = DocumentoSingleton.getInstance().getBo().listByTipoDocumento(this.getEntity().getTipo(), UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
//            } else {
//                documentos = DocumentoSingleton.getInstance().getBo().listAll();
//            }
//        } catch (Exception e) {
//            log.error("Erro ao buscar lista de documentos", e);
//            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
//        }
//    }

    @Override
    public void actionPersist(ActionEvent event) {
        //this.getEntity().setIdEmpresa(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
        this.getEntity().setCriadoPor(UtilsFrontEnd.getProfissionalLogado());
        this.getEntity().setDataCriacao(new Date());
        
        String regex = "(\\{(\\w+)\\})+";
        String retorno = "";

        Pattern pattern = Pattern.compile(regex,Pattern.CASE_INSENSITIVE);
        Matcher comparator = pattern.matcher(this.documento);
        
        while(comparator.find()) {
            retorno = comparator.group();
        }
        
        //super.actionPersist(event);
    }
//
//    @Override
//    public void actionRemove(ActionEvent event) {
//        super.actionRemove(event);
//        this.listAll();
//    }

    @Override
    public void actionNew(ActionEvent event) {
        super.actionNew(event);
        this.setEntity(new Documento());
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

}
