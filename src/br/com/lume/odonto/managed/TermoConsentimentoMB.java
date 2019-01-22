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
import br.com.lume.odonto.bo.PlanoTratamentoBO;
import br.com.lume.odonto.bo.ProfissionalBO;
import br.com.lume.odonto.bo.TermoConsentimentoBO;
import br.com.lume.odonto.entity.Documento;
import br.com.lume.odonto.entity.Dominio;
import br.com.lume.odonto.entity.OdontoPerfil;
import br.com.lume.odonto.entity.Paciente;
import br.com.lume.odonto.entity.PlanoTratamento;
import br.com.lume.odonto.entity.PlanoTratamentoProcedimento;
import br.com.lume.odonto.entity.Profissional;
import br.com.lume.odonto.entity.TagDocumento;
import br.com.lume.odonto.entity.TermoConsentimento;
import br.com.lume.odonto.util.OdontoMensagens;

@ManagedBean
@ViewScoped
public class TermoConsentimentoMB extends LumeManagedBean<TermoConsentimento> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(TermoConsentimentoMB.class);

    private String documento;

    private List<Documento> documentos;

    private boolean visivel = false, liberaBotao = false;

    private Documento documentoSelecionado;

    private Set<TagDocumento> tagDinamicas;

    private List<PlanoTratamento> planoTratamentos;

    private Paciente paciente;

    private Profissional profissionalLogado = new Profissional();

    private DominioBO dominioBO;

    private DocumentoBO documentoBO;

    private TermoConsentimentoBO termoConsentimentoBO;

    private PlanoTratamentoBO planoTratamentoBO;

    private PacienteBO pacienteBO;

    private ProfissionalBO profissionalBO;

    public TermoConsentimentoMB() {
        super(new TermoConsentimentoBO());
        dominioBO = new DominioBO();
        documentoBO = new DocumentoBO();
        termoConsentimentoBO = new TermoConsentimentoBO();
        planoTratamentoBO = new PlanoTratamentoBO();
        pacienteBO = new PacienteBO();
        try {
            Dominio dominio = dominioBO.findByEmpresaAndObjetoAndTipoAndValor("documento", "tipo", "T");
            documentos = documentoBO.listByTipoDocumento(dominio);
            profissionalLogado = ProfissionalBO.getProfissionalLogado();
            if (profissionalLogado.getPerfil().equals(OdontoPerfil.ADMINISTRADOR) || profissionalLogado.getPerfil().equals(OdontoPerfil.DENTISTA) || profissionalLogado.getPerfil().equals(
                    OdontoPerfil.RESPONSAVEL_TECNICO)) {
                liberaBotao = true;
            }
            this.setPaciente(PacienteBO.getPacienteSelecionado());
        } catch (Exception e) {
            this.addError(OdontoMensagens.getMensagem("documento.erro.documento.carregar"), "");
            e.printStackTrace();
        }
        this.setClazz(TermoConsentimento.class);
    }

    @Override
    public void actionPersist(ActionEvent event) {
        try {
            if (documentoSelecionado != null) {
                documento = documentoSelecionado.getModelo();
                this.replaceDocumento();
                visivel = true;
                this.getEntity().setProfissional(ProfissionalBO.getProfissionalLogado());
                this.getEntity().setTermoGerado(documento);
                this.getEntity().setPaciente(paciente);
                termoConsentimentoBO.persist(this.getEntity());
                this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");
            } else {
                this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
            }
        } catch (Exception e) {
            log.error("Erro no actionPersist Atestado", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
        }
    }

    @Override
    public void actionNew(ActionEvent arg0) {
        // planoTratamentoSelecionado = null;
        tagDinamicas = null;
        visivel = false;
        documentoSelecionado = null;
        paciente = null;
        this.setEntity(new TermoConsentimento());
    }

    @Override
    public List<TermoConsentimento> getEntityList() {
        // return super.getEntityList();
        if (paciente != null) {
            try {
                return termoConsentimentoBO.listByPaciente(paciente);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public void atualizaTela() {
        documento = this.getEntity().getTermoGerado();
        visivel = true;
    }

    private void replaceDocumento() {
        String procedimentos = "";
        if (this.getEntity().getPlanoTratamento() != null) {
            for (PlanoTratamentoProcedimento planoTratamentoProcedimento : this.getEntity().getPlanoTratamento().getPlanoTratamentoProcedimentos()) {
                procedimentos += "</br>&bull;";
                procedimentos += planoTratamentoProcedimento.getProcedimento().getDescricao();
            }
        }
        documento = documentoBO.replaceDocumento(tagDinamicas, paciente.getDadosBasico(), documento);
        documento = documento.replaceAll("#paciente", paciente.getDadosBasico().getNome());
        documento = documento.replaceAll("#plano_tratamento", procedimentos);
        if ((profissionalLogado.getPerfil().equals(OdontoPerfil.DENTISTA)) || (profissionalLogado.getPerfil().equals(OdontoPerfil.ADMINISTRADOR))) {
            documento = documento.replaceAll("#profissional", profissionalLogado.getDadosBasico().getNome());
            documento = documento.replaceAll("#cro", profissionalLogado.getRegistroConselho() != null ? profissionalLogado.getRegistroConselho().toString() : "");
        }
        documento = documento.replaceAll("#documento", paciente.getDadosBasico().getDocumento());
        documento = documento.replaceAll("#nascimento", paciente.getDadosBasico().getDataNascimentoStr());
        documento = documento.replaceAll("#telefone", paciente.getDadosBasico().getTelefoneStr());
        documento = documento.replaceAll("#email", paciente.getDadosBasico().getEmail());
        //documento = documentoBO.replacePacienteTitular(documento, paciente);
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
        if ((ProfissionalBO.getProfissionalLogado().getPerfil().equals(OdontoPerfil.DENTISTA)) || (ProfissionalBO.getProfissionalLogado().getPerfil().equals(OdontoPerfil.ADMINISTRADOR))) {
            tagDinamicas = documentoBO.getTagDinamicas(documentoSelecionado, this.documentoSelecionado, tagDinamicas,
                    new String[] { "#paciente", "#rg", "#datahoje", "#endereco_completo", "#datanascimento", "#datanascimento_titular", "#documento", "#documento_titular", "#email", "#email_titular", "#endereco_completo_titular", "#paciente_titular", "#rg_titular", "#telefone", "#telefone_titular", "#plano_tratamento", "#profissional", "#cro", "#clinica_nome", "#clinica_cnpj", "#clinica_endereco", "#clinica_numero", "#clinica_complemento", "#clinica_bairro", "#clinica_cidade", "#clinica_estado", "#clinica_fone", "#clinica_email", "#clinica_cro_responsavel", "#clinica_logo" });
        } else {
            tagDinamicas = documentoBO.getTagDinamicas(documentoSelecionado, this.documentoSelecionado, tagDinamicas,
                    new String[] { "#paciente", "#rg", "#datahoje", "#endereco_completo", "#datanascimento", "#datanascimento_titular", "#documento", "#documento_titular", "#email", "#email_titular", "#endereco_completo_titular", "#paciente_titular", "#rg_titular", "#telefone", "#telefone_titular", "#plano_tratamento", "#clinica_nome", "#clinica_cnpj", "#clinica_endereco", "#clinica_numero", "#clinica_complemento", "#clinica_bairro", "#clinica_cidade", "#clinica_estado", "#clinica_fone", "#clinica_email", "#clinica_cro_responsavel", "#clinica_logo" });
        }
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

    public List<PlanoTratamento> getPlanoTratamentos() {
        if (paciente != null) {
            planoTratamentos = planoTratamentoBO.listByPaciente(paciente);
        }
        return planoTratamentos;
    }

    public void setPlanoTratamentos(List<PlanoTratamento> planoTratamentos) {
        this.planoTratamentos = planoTratamentos;
    }

    public Profissional getProfissionalLogado() {
        return profissionalLogado;
    }

    public void setProfissionalLogado(Profissional profissionalLogado) {
        this.profissionalLogado = profissionalLogado;
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
