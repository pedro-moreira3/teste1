package br.com.lume.odonto.managed;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ActionEvent;

import org.apache.log4j.Logger;
import org.primefaces.event.SelectEvent;

import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.odonto.bo.DocumentoBO;
import br.com.lume.odonto.bo.DominioBO;
import br.com.lume.odonto.bo.PacienteBO;
import br.com.lume.odonto.bo.ProfissionalBO;
import br.com.lume.odonto.bo.ReceituarioBO;
import br.com.lume.odonto.entity.Documento;
import br.com.lume.odonto.entity.Dominio;
import br.com.lume.odonto.entity.OdontoPerfil;
import br.com.lume.odonto.entity.Paciente;
import br.com.lume.odonto.entity.Profissional;
import br.com.lume.odonto.entity.Receituario;
import br.com.lume.odonto.entity.TagDocumento;
import br.com.lume.odonto.util.OdontoMensagens;

@ManagedBean
@ViewScoped
public class ReceituarioMB extends LumeManagedBean<Receituario> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(ReceituarioMB.class);

    private String documento;

    private List<Documento> documentos;

    private boolean visivel = false, liberaBotao = false;

    private Documento documentoSelecionado;

    private Set<TagDocumento> tagDinamicas;

    private Paciente paciente;

    private DominioBO dominioBO;

    private DocumentoBO documentoBO;

    private ReceituarioBO receituarioBO;

    private PacienteBO pacienteBO;

    private ProfissionalBO profissionalBO;

    private Profissional profissionalLogado;

    public ReceituarioMB() {
        super(new ReceituarioBO());
        profissionalBO = new ProfissionalBO();
        dominioBO = new DominioBO();
        documentoBO = new DocumentoBO();
        receituarioBO = new ReceituarioBO();
        pacienteBO = new PacienteBO();
        try {
            Dominio dominio = dominioBO.findByEmpresaAndObjetoAndTipoAndValor("documento", "tipo", "R");
            documentos = documentoBO.listByTipoDocumento(dominio);
            this.setPaciente(PacienteBO.getPacienteSelecionado());
        } catch (Exception e) {
            this.addError(OdontoMensagens.getMensagem("documento.erro.documento.carregar"), "");
            e.printStackTrace();
        }
        profissionalLogado = ProfissionalBO.getProfissionalLogado();
        if (profissionalLogado.getPerfil().equals(OdontoPerfil.ADMINISTRADOR) || profissionalLogado.getPerfil().equals(OdontoPerfil.DENTISTA) || profissionalLogado.getPerfil().equals(
                OdontoPerfil.RESPONSAVEL_TECNICO) || profissionalLogado.getPerfil().equals(OdontoPerfil.ADMINISTRADOR_CLINICA)) {
            liberaBotao = true;
        }

        this.setClazz(Receituario.class);
    }

    @Override
    public void actionPersist(ActionEvent event) {
        try {
            if (documentoSelecionado != null) {
                documento = documentoSelecionado.getModelo();
                this.replaceDocumento();
                visivel = true;
            }
            this.getEntity().setProfissional(ProfissionalBO.getProfissionalLogado());
            this.getEntity().setReceituarioGerado(documento);
            this.getEntity().setPaciente(paciente);
            receituarioBO.persist(this.getEntity());
            this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");
        } catch (Exception e) {
            log.error("Erro no actionPersist Atestado", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
        }
    }

    @Override
    public void actionNew(ActionEvent event) {
        tagDinamicas = null;
        visivel = false;
        documentoSelecionado = null;
        paciente = null;
        this.setEntity(new Receituario());
    }

    @Override
    public List<Receituario> getEntityList() {
        if (paciente != null) {
            try {
                return receituarioBO.listByPaciente(paciente);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public void atualizaTela() {
        documento = this.getEntity().getReceituarioGerado();
        visivel = true;
    }

    private void replaceDocumento() {
        documento = documentoBO.replaceDocumento(tagDinamicas, paciente.getDadosBasico(), documento);
        documento = documento.replaceAll("#paciente", paciente.getDadosBasico().getNome());
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

    public Documento getDocumentoSelecionado() {
        return documentoSelecionado;
    }

    public void setDocumentoSelecionado(Documento documentoSelecionado) {
        tagDinamicas = documentoBO.getTagDinamicas(documentoSelecionado, this.documentoSelecionado, tagDinamicas,
                new String[] { "#paciente", "#rg", "#datahoje", "#endereco_completo", "#clinica_nome", "#clinica_cnpj", "#clinica_endereco", "#clinica_numero", "#clinica_complemento", "#clinica_bairro", "#clinica_cidade", "#clinica_estado", "#clinica_fone", "#clinica_email", "#clinica_cro_responsavel", "#clinica_logo" });
        this.documentoSelecionado = documentoSelecionado;
        visivel = true;
        documento = documentoSelecionado.getModelo();
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
        PacienteBO.setPacienteSelecionado(paciente);
    }

    public List<Paciente> geraSugestoes(String query) {
        return pacienteBO.listSugestoesComplete(query);
    }

    public boolean isLiberaBotao() {
        return liberaBotao;
    }

    public void setLiberaBotao(boolean liberaBotao) {
        this.liberaBotao = liberaBotao;
    }
}
