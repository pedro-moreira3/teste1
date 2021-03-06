package br.com.lume.odonto.managed;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ActionEvent;

import org.apache.log4j.Logger;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.event.SelectEvent;

import br.com.lume.common.OdontoPerfil;
import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.documento.DocumentoSingleton;
import br.com.lume.dominio.DominioSingleton;
import br.com.lume.odonto.entity.Documento;
import br.com.lume.odonto.entity.Dominio;
import br.com.lume.odonto.entity.Paciente;
import br.com.lume.odonto.entity.PedidoExame;
import br.com.lume.odonto.entity.Profissional;
import br.com.lume.odonto.entity.TagDocumento;
import br.com.lume.odonto.util.OdontoMensagens;
import br.com.lume.paciente.PacienteSingleton;
import br.com.lume.pedidoExame.PedidoExameSingleton;

@ManagedBean
@ViewScoped
public class PedidoExameMB extends LumeManagedBean<PedidoExame> {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(PedidoExameMB.class);

    private String documento;

    private List<Documento> documentos;

    private boolean visivel = false, liberaBotao = false;

    private Documento documentoSelecionado;

    private Set<TagDocumento> tagDinamicas;

    private Paciente paciente;

    private Profissional profissionalLogado;

    //EXPORTAÇÃO TABELA
    private DataTable tabelaExame;

    public PedidoExameMB() {
        super(PedidoExameSingleton.getInstance().getBo());
        if (UtilsFrontEnd.getProfissionalLogado() != null) {
            try {
                Dominio dominio = DominioSingleton.getInstance().getBo().findByEmpresaAndObjetoAndTipoAndValor("documento", "tipo", "PE");
                documentos = DocumentoSingleton.getInstance().getBo().listByTipoDocumento(dominio, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
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
            this.setClazz(PedidoExame.class);
        }
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
            this.getEntity().setDocumentoGerado(documento);
            this.getEntity().setPaciente(paciente);
            PedidoExameSingleton.getInstance().getBo().persist(this.getEntity());
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
        this.setEntity(new PedidoExame());
    }

    @Override
    public List<PedidoExame> getEntityList() {
        // return super.getEntityList();
        if (paciente != null) {
            try {
                return PedidoExameSingleton.getInstance().getBo().listByPaciente(paciente);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public void atualizaTela() {
        documento = this.getEntity().getDocumentoGerado();
        visivel = true;
    }

    private void replaceDocumento() {
        documento = DocumentoSingleton.getInstance().getBo().replaceDocumento(tagDinamicas, paciente.getDadosBasico(), documento, UtilsFrontEnd.getProfissionalLogado());
        documento = documento.replaceAll("#paciente", paciente.getDadosBasico().getNome());
        documento = documento.replaceAll("span", "div");
    }

    public void exportarTabela(String type) {
        exportarTabela("Pedidos de Exames", tabelaExame, type);
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
        tagDinamicas = DocumentoSingleton.getInstance().getBo().getTagDinamicas(documentoSelecionado, this.documentoSelecionado, tagDinamicas,
                new String[] { "#paciente", "#rg", "#datahoje", "#endereco_completo", "#idade", "#sexo", "#clinica_nome", "#clinica_cnpj", "#clinica_endereco", "#clinica_numero", "#clinica_complemento", "#clinica_bairro", "#clinica_cidade", "#clinica_estado", "#clinica_fone", "#clinica_email", "#clinica_cro_responsavel", "#clinica_logo" });
        this.documentoSelecionado = documentoSelecionado;
        visivel = true;
        documento = documentoSelecionado.getModelo();
    }

    public Set<TagDocumento> getTagDinamicas() {
        return tagDinamicas;
    }

    public List<TagDocumento> getTagDinamicasAsList() {
        List<TagDocumento> listAux = tagDinamicas != null ? new ArrayList<>(tagDinamicas) : new ArrayList<>();
//        Collections.sort(listAux);
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

    public List<Paciente> geraSugestoes(String query) {
        try {
            return PacienteSingleton.getInstance().listSugestoesComplete(query, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            e.printStackTrace();
        }
        return null;
    }

    public boolean isLiberaBotao() {
        return liberaBotao;
    }

    public void setLiberaBotao(boolean liberaBotao) {
        this.liberaBotao = liberaBotao;
    }

    public DataTable getTabelaExame() {
        return tabelaExame;
    }

    public void setTabelaExame(DataTable tabelaExame) {
        this.tabelaExame = tabelaExame;
    }
}
