package br.com.lume.odonto.managed;

import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ActionEvent;

import org.apache.log4j.Logger;

import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.documento.DocumentoSingleton;
import br.com.lume.dominio.DominioSingleton;
//import br.com.lume.odonto.bo.DocumentoBO;
//import br.com.lume.odonto.bo.DominioBO;
//import br.com.lume.odonto.bo.ProfissionalBO;
import br.com.lume.odonto.entity.Documento;
import br.com.lume.odonto.entity.Dominio;

@ManagedBean
@ViewScoped
public class DocumentoMB extends LumeManagedBean<Documento> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(DocumentoMB.class);

    private List<String> legendas = new ArrayList<>();

    private List<Documento> documentos;

    private List<Dominio> dominios;

  //  private DominioBO dominioBO;

    public DocumentoMB() {
        super(DocumentoSingleton.getInstance().getBo());
     //   dominioBO = new DominioBO();
        this.setClazz(Documento.class);
        this.addLegendas();
        this.listAll();
    }

    public void listAll() {
        try {
            dominios = DominioSingleton.getInstance().getBo().listByEmpresaAndObjetoAndTipo("documento", "tipo");
            if (this.getEntity() != null && this.getEntity().getTipo() != null) {
                documentos = DocumentoSingleton.getInstance().getBo().listByTipoDocumento(this.getEntity().getTipo(), UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
            } else {
                documentos = DocumentoSingleton.getInstance().getBo().listAll();
            }
        } catch (Exception e) {
            log.error("Erro ao buscar lista de documentos", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
        }
    }

    @Override
    public void actionPersist(ActionEvent event) {
        this.getEntity().setIdEmpresa(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
        super.actionPersist(event);
        this.listAll();
    }

    @Override
    public void actionRemove(ActionEvent event) {
        super.actionRemove(event);
        this.listAll();
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
        this.listAll();
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
}
