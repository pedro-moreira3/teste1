//package br.com.lume.odonto.managed;
//
//import java.lang.reflect.Field;
//import java.lang.reflect.InvocationTargetException;
//import java.util.ArrayList;
//import java.util.List;
//
//import javax.faces.bean.ManagedBean;
//import javax.faces.bean.ViewScoped;
//import javax.faces.component.UIComponent;
//import javax.faces.context.FacesContext;
//
//import br.com.lume.cid.CidSingleton;
//import br.com.lume.common.log.LogIntelidenteSingleton;
//import br.com.lume.common.managed.LumeManagedBean;
//import br.com.lume.common.util.Mensagens;
//import br.com.lume.common.util.Status;
//import br.com.lume.common.util.UtilsFrontEnd;
//import br.com.lume.documento.DocumentoSingleton;
//import br.com.lume.dominio.DominioSingleton;
//import br.com.lume.odonto.entity.CID;
//// import br.com.lume.odonto.bo.DocumentoBO;
//// import br.com.lume.odonto.bo.DominioBO;
//// import br.com.lume.odonto.bo.ProfissionalBO;
//import br.com.lume.odonto.entity.Documento;
//import br.com.lume.odonto.entity.Dominio;
//import br.com.lume.odonto.entity.Paciente;
//import br.com.lume.odonto.entity.Profissional;
//import br.com.lume.odonto.entity.Tag;
//import br.com.lume.odonto.entity.TagDocumentoModelo;
//import br.com.lume.odonto.entity.TagEntidade;
//import br.com.lume.security.entity.Empresa;
//import br.com.lume.tag.TagSingleton;
//import br.com.lume.tagEntidade.TagEntidadeSingleton;
//
//@ManagedBean
//@ViewScoped
//public class EmissaoDocumentoMB extends LumeManagedBean<Documento> {
//
//    private static final long serialVersionUID = 1L;
//
//    private Documento modeloSelecionado;
//    private List<Dominio> listaTiposDocumentos;
//    private Dominio filtroTipoDocumento;
//    private Dominio filtroTipoDocumentoEmitir;
//    private List<Documento> listaDocumentosModelos;
//    private List<TagEntidade> tags;
//    private List<Documento> documentos;
//    private Tag tag;
//    private Profissional filtroProfissionalEmissao;
//    private Paciente pacienteSelecionado;
//
//    public EmissaoDocumentoMB() {
//        super(DocumentoSingleton.getInstance().getBo());
//        this.setClazz(Documento.class);
//        this.carregarTiposDocumento();
//        
//        this.modeloSelecionado = new Documento();
//        try {
////            TagEntidade entidade = TagEntidadeSingleton.getInstance().getBo().find(1l);
////            modeloSelecionado.setTipo(DominioSingleton.getInstance().getBo().find(155l));
////            modeloSelecionado.setModelo("{empChaCnpj} e {nome}");
////            TagDocumentoModelo tagModelo = new TagDocumentoModelo();
////            tagModelo.setDocumentoModelo(modeloSelecionado);
////            tagModelo.setTagEntidade(entidade);
////            this.carregarDocumentoModelo();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        
//    }
//
//    public void pesquisar() {
//        
//    }
//    
//    public void carregarDocumentoModelo() {
//        if (modeloSelecionado != null) {
//            List<TagDocumentoModelo> tags = modeloSelecionado.getTags();
//            List<TagDocumentoModelo> tagsProcessadas = new ArrayList<>();
//            String textoDocumento = modeloSelecionado.getModelo();
//
//            for (TagDocumentoModelo tag : tags) {
//                if (tag.getTagEntidade().getInserirDado().equals(Status.NAO)) {
//                    if(tag.getTagEntidade().getEntidade().getEntidade().equals("Paciente")) {
//                        this.tag = TagSingleton.getInstance().getBo().listByEntidade("Paciente");
//                        this.tags.add(tag.getTagEntidade());
//                    }else {
//                        this.processarTag(tag.getTagEntidade(), textoDocumento);
//                        tagsProcessadas.add(tag);
//                    }
//                }else {
//                    this.tags.add(tag.getTagEntidade());
//                }
//            }
//        }
//    }
//    
//    public List<CID> geraSugestoesCID(String query) {
//        List<CID> suggestions = new ArrayList<>();
//        try {
//            //cids = CidSingleton.getInstance().getBo().listAll();
//            suggestions = CidSingleton.getInstance().getBo().listSugestoesComplete(query, false);
//        } catch (Exception e) {
//            this.addError("Erro", "Falha ao carregar os CID");
//        }
//        return suggestions;
//    }
//
//    
//    public void carregarDocumentos() {
//        try {
//            if(this.filtroTipoDocumentoEmitir != null) {
//                this.listaDocumentosModelos = DocumentoSingleton.getInstance().getBo().listByTipoDocumento(filtroTipoDocumentoEmitir, 
//                        UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
//            }
//        }catch (Exception e) {
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
//    private void processarTag(TagEntidade tag, String modelo) {
//        try {
//            Class<?> c = null;
//            switch (tag.getEntidade().getEntidade()) {
//                case "Empresa": {
//                    Empresa emp = UtilsFrontEnd.getEmpresaLogada();
//                    c = emp.getClass();
//
//                    Field campo = c.getField(tag.getAtributo());
//                    Object obj = campo.get(emp);
//                    
//                    if(obj instanceof String) {
//                        modelo.replaceAll("{" + tag.getEntidade().getEntidade() + " - " + tag.getAtributo()+"}", (String) obj);
//                    }
//                }break;
//                case "Paciente":{
//                    c = this.pacienteSelecionado.getDadosBasico().getClass();
//
//                    Field campo = c.getField(tag.getAtributo());
//                    Object obj = campo.get(this.pacienteSelecionado.getDadosBasico());
//                    
//                    if(obj instanceof String) {
//                        modelo.replaceAll("{" + tag.getEntidade().getEntidade() + " - " + tag.getAtributo()+"}", (String) obj);
//                    }
//                }
//            }
//        } catch (Exception e) {
//            this.addError("Erro na emissão", "Falha ao processar as tags do documento.");
//            e.printStackTrace();
//            LogIntelidenteSingleton.getInstance().makeLog(e);
//        }
//    }
//
//    public List carregarSugestoes(String query) {
//        try {
//            FacesContext context = FacesContext.getCurrentInstance();
//            TagEntidade tag = (TagEntidade) UIComponent.getCurrentComponent(context).getAttributes().get("tag");
//            return TagEntidadeSingleton.getInstance().getListOnEntity(tag, query);
//        } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | InstantiationException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//        return null;
//    }
//
//    public void atualizarTags() {
//        if (modeloSelecionado != null) {
//            List<TagDocumentoModelo> tags = modeloSelecionado.getTags();
//            List<TagDocumentoModelo> tagsProcessadas = new ArrayList<>();
//            String textoDocumento = modeloSelecionado.getModelo();
//
//            for (TagDocumentoModelo tag : tags) {
//                if (tag.getTagEntidade().getInserirDado().equals(Status.NAO)) {
//                    if(tag.getTagEntidade().getEntidade().getEntidade().equals("Paciente")) {
//                        this.tag = TagSingleton.getInstance().getBo().listByEntidade("Paciente");
//                        this.tags.add(tag.getTagEntidade());
//                    }else {
//                        this.processarTag(tag.getTagEntidade(), textoDocumento);
//                        tagsProcessadas.add(tag);
//                    }
//                }
//            }
//        }
//    }
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
//}
