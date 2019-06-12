package br.com.lume.odonto.managed;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ActionEvent;

import org.apache.log4j.Logger;
import org.primefaces.event.SelectEvent;

import br.com.lume.atestado.AtestadoSingleton;
import br.com.lume.cid.CidSingleton;
import br.com.lume.common.OdontoPerfil;
import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.documento.DocumentoSingleton;
import br.com.lume.dominio.DominioSingleton;
//import br.com.lume.odonto.bo.CIDBO;
//import br.com.lume.odonto.bo.DocumentoBO;
//import br.com.lume.odonto.bo.DominioBO;
//import br.com.lume.odonto.bo.PacienteBO;
//import br.com.lume.odonto.bo.ProfissionalBO;
import br.com.lume.odonto.entity.Atestado;
import br.com.lume.odonto.entity.CID;
import br.com.lume.odonto.entity.Documento;
import br.com.lume.odonto.entity.Dominio;
import br.com.lume.odonto.entity.Paciente;
import br.com.lume.odonto.entity.Profissional;
import br.com.lume.odonto.entity.TagDocumento;
import br.com.lume.odonto.util.OdontoMensagens;
import br.com.lume.paciente.PacienteSingleton;

@ManagedBean
@ViewScoped
public class AtestadoMB extends LumeManagedBean<Atestado> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(AtestadoMB.class);

    private String documento, dias;

    private CID cid;

    private List<CID> cids;

    private List<Documento> documentos;

    private boolean visivel = false, liberaBotao = false;

    private Documento documentoSelecionado;

    private Set<TagDocumento> tagDinamicas;

  //  private CIDBO cidBO;

  //  private DominioBO dominioBO;

  //  private DocumentoBO documentoBO;

   // private AtestadoBO atestadoBO;

 //   private PacienteBO pacienteBO;

    private Paciente paciente;

 //   private ProfissionalBO profissionalBO;

    private Profissional profissionalLogado;

    public AtestadoMB() {
        super(AtestadoSingleton.getInstance().getBo());
     //   profissionalBO = new ProfissionalBO();
     //   dominioBO = new DominioBO();
      //  documentoBO = new DocumentoBO();
    //    atestadoBO = new AtestadoBO();
    //    pacienteBO = new PacienteBO();
        try {
            Dominio dominio = DominioSingleton.getInstance().getBo().findByEmpresaAndObjetoAndTipoAndValor("documento", "tipo", "A");
            documentos = DocumentoSingleton.getInstance().getBo().listByTipoDocumento(dominio);
          //  cidBO = new CIDBO();
            cids = CidSingleton.getInstance().getBo().listAll();
            this.setPaciente(UtilsFrontEnd.getPacienteSelecionado());
        } catch (Exception e) {
            this.addError(OdontoMensagens.getMensagem("documento.erro.documento.carregar"), "");
            e.printStackTrace();
        }
        profissionalLogado = UtilsFrontEnd.getProfissionalLogado();
        if (profissionalLogado.getPerfil().equals(OdontoPerfil.ADMINISTRADOR) || profissionalLogado.getPerfil().equals(OdontoPerfil.DENTISTA) || profissionalLogado.getPerfil().equals(
                OdontoPerfil.RESPONSAVEL_TECNICO) || profissionalLogado.getPerfil().equals(OdontoPerfil.ADMINISTRADOR_CLINICA)) {
            liberaBotao = true;
        }
        this.setClazz(Atestado.class);
    }

    @Override
    public void actionPersist(ActionEvent event) {
        try {
            if (documentoSelecionado != null) {
                documento = documentoSelecionado.getModelo();
                this.replaceDocumento();
                visivel = true;
            }
            this.getEntity().setProfissional(UtilsFrontEnd.getProfissionalLogado());
            this.getEntity().setAtestadoGerado(documento);
            this.getEntity().setPaciente(paciente);
            AtestadoSingleton.getInstance().getBo().persist(this.getEntity());
            this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");
            this.setCid(null);
        } catch (Exception e) {
            log.error("Erro no actionPersist Atestado", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
        }
    }

    @Override
    public void actionNew(ActionEvent arg0) {
        tagDinamicas = null;
        visivel = false;
        dias = null;
        documentoSelecionado = null;
        this.setCid(null);
        paciente = null;
        this.setEntity(new Atestado());
    }

    @Override
    public List<Atestado> getEntityList() {
        if (paciente != null) {
            try {
                return AtestadoSingleton.getInstance().getBo().listByPaciente(paciente);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public void atualizaTela() {
        documento = this.getEntity().getAtestadoGerado();
        visivel = true;
        this.setCid(null);
    }

    public List<CID> geraSugestoes(String query) {
        List<CID> suggestions = new ArrayList<>();
        try {
            cids = CidSingleton.getInstance().getBo().listAll();
        } catch (Exception e) {
            log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS);
        }
        for (CID c : cids) {
            if (c.getLabel().toLowerCase().contains(query.toLowerCase())) {
                suggestions.add(c);
            }
        }
        Collections.sort(suggestions, new Comparator<CID>() {

            @Override
            public int compare(CID o1, CID o2) {
                return o1.getLabel().compareToIgnoreCase(o2.getLabel());
            }
        });
        return suggestions;
    }

    public void handleSelect(SelectEvent event) {
        Object object = event.getObject();
        cid = (CID) object;
    }

    private void replaceDocumento() {
        documento = DocumentoSingleton.getInstance().getBo().replaceDocumento(tagDinamicas, paciente.getDadosBasico(), documento);
        documento = documento.replaceAll("#paciente", paciente.getDadosBasico().getNome());
        if(dias != null && !dias.isEmpty()) {
            documento = documento.replaceAll("#dias", this.getDias());    
        }
        if (this.getCid() != null && this.getCid().getId() != null) {
            documento = documento.replaceAll("#cid", this.getCid().getId());
        } else {
            documento = documento.replaceAll("#cid", " ");
        }
        // documento = documentoBO.replacePacienteTitular(documento, paciente);
        documento = documento.replaceAll("span", "div");
    }

    public String getDocumento() {
        return documento;
    }

    public void setDocumento(String documento) {
        this.documento = documento;
    }

    public List<Documento> getDocumentos() {
        return documentos;
    }

    public void setDocumentos(List<Documento> documentos) {
        this.documentos = documentos;
    }

    public boolean isVisivel() {
        return visivel;
    }

    public void setVisivel(boolean visivel) {
        this.visivel = visivel;
    }

    public String getDias() {
        return dias;
    }

    public void setDias(String dias) {
        this.dias = dias;
    }

    public Documento getDocumentoSelecionado() {
        return documentoSelecionado;
    }

    public void setDocumentoSelecionado(Documento documentoSelecionado) {
        tagDinamicas = DocumentoSingleton.getInstance().getBo().getTagDinamicas(documentoSelecionado, this.documentoSelecionado, tagDinamicas,
                new String[] { "#dias", "#paciente", "#rg", "#datahoje", "#endereco_completo", "#profissional", "#cid", "#clinica_nome", "#clinica_cnpj", "#clinica_endereco", "#clinica_numero", "#clinica_complemento", "#clinica_bairro", "#clinica_cidade", "#clinica_estado", "#clinica_fone", "#clinica_email", "#clinica_cro_responsavel", "#clinica_logo" });
        this.documentoSelecionado = documentoSelecionado;
        visivel = true;
        documento = documentoSelecionado.getModelo();
    }

    public CID getCid() {
        return cid;
    }

    public void setCid(CID cid) {
        this.cid = cid;
    }

    public List<CID> getCids() {
        return cids;
    }

    public void setCids(List<CID> cids) {
        this.cids = cids;
    }

    public Set<TagDocumento> getTagDinamicas() {
        return tagDinamicas;
    }

    public List<TagDocumento> getTagDinamicasAsList() {
        List<TagDocumento> listAux = tagDinamicas != null ? new ArrayList<>(tagDinamicas) : new ArrayList<>();
        Collections.sort(listAux);
        return listAux;
    }

    public void setTagDinamicas(Set<TagDocumento> tagDinamicas) {
        this.tagDinamicas = tagDinamicas;
    }

    public Paciente getPaciente() {
        return paciente;
    }

    public void setPaciente(Paciente paciente) {
        this.paciente = paciente;
    }

    public void handleSelectPacienteSelecionado(SelectEvent event) {
        Object object = event.getObject();
        paciente = (Paciente) object;
        UtilsFrontEnd.setPacienteSelecionado(paciente);
    }

    public List<Paciente> geraSugestoesPaciente(String query) {
        return PacienteSingleton.getInstance().getBo().listSugestoesComplete(query,UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
    }

    public boolean isLiberaBotao() {
        return liberaBotao;
    }

    public void setLiberaBotao(boolean liberaBotao) {
        this.liberaBotao = liberaBotao;
    }
}
