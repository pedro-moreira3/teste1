package br.com.lume.odonto.managed;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ActionEvent;

import org.apache.log4j.Logger;
import org.primefaces.component.datatable.DataTable;

import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.contrato.ContratoSingleton;
import br.com.lume.documento.DocumentoSingleton;
import br.com.lume.dominio.DominioSingleton;
// import br.com.lume.odonto.bo.ContratoBO;
// import br.com.lume.odonto.bo.DocumentoBO;
// import br.com.lume.odonto.bo.DominioBO;
// import br.com.lume.odonto.bo.ProfissionalBO;
import br.com.lume.odonto.entity.Contrato;
import br.com.lume.odonto.entity.Documento;
import br.com.lume.odonto.entity.Dominio;
import br.com.lume.odonto.entity.TagDocumento;
import br.com.lume.odonto.util.OdontoMensagens;

@ManagedBean
@ViewScoped
public class ContratoMB extends LumeManagedBean<Contrato> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(ContratoMB.class);

    private String documento;

    private List<Documento> documentos;

    private boolean visivel = false;

    private Documento documentoSelecionado;

    private Set<TagDocumento> tagDinamicas;

    //  private ContratoBO contratoBO;

    //   private DocumentoBO documentoBO;

    //   private DominioBO dominioBO;

    @ManagedProperty(value = "#{profissionalMB}")
    private ProfissionalMB profissionalMB;

    private List<String> legendas = new ArrayList<>();

    //EXPORTAÇÃO TABELA
    private DataTable tabelaContrato;

    public ContratoMB() {
        super(ContratoSingleton.getInstance().getBo());
//        dominioBO = new DominioBO();
//        documentoBO = new DocumentoBO();
//        contratoBO = new ContratoBO();
        try {
            if (UtilsFrontEnd.getProfissionalLogado() != null) {
                Dominio dominio = DominioSingleton.getInstance().getBo().findByEmpresaAndObjetoAndTipoAndValor("documento", "tipo", "C");
                documentos = DocumentoSingleton.getInstance().getBo().listByTipoDocumento(dominio, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
            }
        } catch (Exception e) {
            this.addError(OdontoMensagens.getMensagem("documento.erro.documento.carregar"), "");
            e.printStackTrace();
        }
        this.setClazz(Contrato.class);
    }

    @Override
    public void actionPersist(ActionEvent event) {
        try {
            if (this.getEntity().getDataInicial().getTime() < this.getEntity().getDataFinal().getTime()) {
                if (documentoSelecionado != null) {
                    documento = documentoSelecionado.getModelo();
                    this.replaceDocumento();
                    visivel = true;
                }
                this.getEntity().setProfissionalContratante(UtilsFrontEnd.getProfissionalLogado());
                this.getEntity().setContratoGerado(documento);
                this.getEntity().setProfissionalContratado(profissionalMB.getEntity());
                ContratoSingleton.getInstance().getBo().persist(this.getEntity());
                this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");
            } else {
                this.addError(OdontoMensagens.getMensagem("afastamento.dtInicio.maior.dtFim"), "");
            }
        } catch (Exception e) {
            log.error("Erro no actionPersist Atestado", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
        }
    }

    @Override
    public void actionNew(ActionEvent arg0) {
        tagDinamicas = null;
        visivel = false;
        documentoSelecionado = null;
        this.setEntity(new Contrato());
    }

    @Override
    public List<Contrato> getEntityList() {
        // return super.getEntityList();
        if (profissionalMB != null) {
            try {
                return ContratoSingleton.getInstance().getBo().listByProfissional(profissionalMB.getEntity());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public void atualizaTela() {
        documento = this.getEntity().getContratoGerado();
        visivel = true;
    }

    private void replaceDocumento() {
        documento = documento.replaceAll("#dataInicial", this.getEntity().getDataInicialStr());
        documento = documento.replaceAll("#dataFinal", this.getEntity().getDataFinalStr());
        documento = documento.replaceAll("#formaContratacao", this.getEntity().getFormaContratacao().getNome());
        documento = DocumentoSingleton.getInstance().getBo().replaceDocumento(tagDinamicas, profissionalMB.getEntity().getDadosBasico(), documento, UtilsFrontEnd.getProfissionalLogado());

        documento = documento.replaceAll("#contratado", profissionalMB.getEntity().getDadosBasico().getNome());
        documento = documento.replaceAll("span", "div");
    }

    public void exportarTabela(String type) {
        exportarTabela("Contratos", tabelaContrato, type);
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
                new String[] { "#formaContratacao", "#dataFinal", "#dataInicial", "#contratado", "#rg", "#datahoje", "#clinica_nome", "#clinica_cnpj", "#clinica_endereco", "#clinica_numero", "#clinica_complemento", "#clinica_bairro", "#clinica_cidade", "#clinica_estado", "#clinica_fone", "#clinica_email", "#clinica_cro_responsavel", "#clinica_logo" });
        this.documentoSelecionado = documentoSelecionado;
        visivel = true;
        documento = documentoSelecionado.getModelo();
    }

    public Set<TagDocumento> getTagDinamicas() {
        return tagDinamicas;
    }

    public List<TagDocumento> getTagDinamicasAsList() {
        List<TagDocumento> listAux = tagDinamicas != null ? new ArrayList<>(tagDinamicas) : new ArrayList<>();
        //Collections.sort(listAux);
        return listAux;
    }

    public void setTagDinamicas(Set<TagDocumento> tagDinamicas) {
        this.tagDinamicas = tagDinamicas;
    }

    public ProfissionalMB getProfissionalMB() {
        return profissionalMB;
    }

    public void setProfissionalMB(ProfissionalMB profissionalMB) {
        this.profissionalMB = profissionalMB;
    }

    public List<String> getLegendas() {
        return legendas;
    }

    public void setLegendas(List<String> legendas) {
        this.legendas = legendas;
    }

    public List<Dominio> getContratacoes() {
        try {
            return DominioSingleton.getInstance().getBo().listByEmpresaAndObjetoAndTipo("contrato", "forma");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public DataTable getTabelaContrato() {
        return tabelaContrato;
    }

    public void setTabelaContrato(DataTable tabelaContrato) {
        this.tabelaContrato = tabelaContrato;
    }
}
