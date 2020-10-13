package br.com.lume.odonto.managed;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ActionEvent;

import org.apache.log4j.Logger;
import org.primefaces.event.SelectEvent;

import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.Status;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.convenioProcedimento.ConvenioProcedimentoSingleton;
import br.com.lume.desconto.DescontoSingleton;
import br.com.lume.documento.DocumentoSingleton;
import br.com.lume.documentoOrcamento.DocumentoOrcamentoSingleton;
import br.com.lume.dominio.DominioSingleton;
import br.com.lume.odonto.entity.ConvenioProcedimento;
import br.com.lume.odonto.entity.Desconto;
import br.com.lume.odonto.entity.Documento;
import br.com.lume.odonto.entity.DocumentoOrcamento;
import br.com.lume.odonto.entity.Dominio;
import br.com.lume.odonto.entity.Orcamento;
import br.com.lume.odonto.entity.Paciente;
import br.com.lume.odonto.entity.PlanoTratamento;
import br.com.lume.odonto.entity.PlanoTratamentoProcedimento;
import br.com.lume.odonto.entity.TagDocumento;
import br.com.lume.odonto.util.OdontoMensagens;
import br.com.lume.paciente.PacienteSingleton;
import br.com.lume.planoTratamento.PlanoTratamentoSingleton;
import br.com.lume.planoTratamentoProcedimento.PlanoTratamentoProcedimentoSingleton;

@ManagedBean
@ViewScoped
public class DocumentoOrcamentoMB extends LumeManagedBean<DocumentoOrcamento> {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(DocumentoOrcamentoMB.class);

    private String documento;

    private List<Documento> documentos;

    private boolean visivel = false;

    private Documento documentoSelecionado;

    private PlanoTratamento planoDeTratamento;

    private List<PlanoTratamentoProcedimento> planoDeTratamentoProcedimentos;

    private List<PlanoTratamento> planoDeTratamentos;

    private Set<TagDocumento> tagDinamicas;

    private Paciente paciente;

    private List<Desconto> descontos = new ArrayList<>();

//    private DominioBO dominioBO;
//
//    private DocumentoBO documentoBO;
//
//    private DescontoBO descontoBO;
//
//    private DocumentoOrcamentoBO documentoOrcamentoBO;
//
//    private PlanoTratamentoBO planoTratamentoBO;
//
//    private ConvenioProcedimentoBO convenioProcedimentoBO;
//
//    private PacienteBO pacienteBO;
//
//    private PlanoTratamentoProcedimentoBO planoTratamentoProcedimentoBO;

    public DocumentoOrcamentoMB() {
        super(DocumentoOrcamentoSingleton.getInstance().getBo());
//        dominioBO = new DominioBO();
//        documentoBO = new DocumentoBO();
//        descontoBO = new DescontoBO();
//        documentoOrcamentoBO = new DocumentoOrcamentoBO();
//        planoTratamentoBO = new PlanoTratamentoBO();
//        convenioProcedimentoBO = new ConvenioProcedimentoBO();
//        pacienteBO = new PacienteBO();
//        planoTratamentoProcedimentoBO = new PlanoTratamentoProcedimentoBO();
        try {
            Dominio dominio = DominioSingleton.getInstance().getBo().findByEmpresaAndObjetoAndTipoAndValor("documento", "tipo", "O");
            documentos = DocumentoSingleton.getInstance().getBo().listByTipoDocumento(dominio, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
            descontos = DescontoSingleton.getInstance().getBo().listByEmpresa(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
            this.setPaciente(UtilsFrontEnd.getPacienteLogado());
        } catch (Exception e) {
            this.addError(OdontoMensagens.getMensagem("documento.erro.documento.carregar"), "");
            e.printStackTrace();
        }
        this.setClazz(DocumentoOrcamento.class);
    }

    @Override
    public void actionPersist(ActionEvent event) {
        try {
//            if (planoDeTratamento.getOrcamentos() == null || planoDeTratamento.getOrcamentos().isEmpty()) {
//                log.error(OdontoMensagens.getMensagem("documentoOrcamento.orcamentos.vazio"));
//                addError(OdontoMensagens.getMensagem("documentoOrcamento.orcamentos.vazio"), "");
//            } else {
            if (documentoSelecionado != null) {
                documento = documentoSelecionado.getModelo();
                this.replaceDocumento();
                visivel = true;
            }
            this.getEntity().setProfissional(UtilsFrontEnd.getProfissionalLogado());
            this.getEntity().setDocumentoGerado(documento);
            this.getEntity().setPaciente(paciente);
            DocumentoOrcamentoSingleton.getInstance().getBo().persist(this.getEntity());
            this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");
//            }
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
        this.setEntity(new DocumentoOrcamento());
    }

    @Override
    public List<DocumentoOrcamento> getEntityList() {
        if (paciente != null) {
            try {
                planoDeTratamentos = PlanoTratamentoSingleton.getInstance().getBo().listByPacienteAndValorTotalDesconto(paciente);
                return DocumentoOrcamentoSingleton.getInstance().getBo().listByPaciente(paciente);
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

    public String formataValor(BigDecimal valor) {
        DecimalFormat df = new DecimalFormat("##,###,###,##0.00", new DecimalFormatSymbols(new Locale("pt", "BR")));
        df.setMinimumFractionDigits(2);
        df.setParseBigDecimal(true);

        return df.format(valor);
    }

    private void replaceDocumento() {
        BigDecimal valorPS = BigDecimal.ZERO;
        documento = DocumentoSingleton.getInstance().getBo().replaceDocumento(tagDinamicas, paciente.getDadosBasico(), documento, UtilsFrontEnd.getProfissionalLogado());
        documento = documento.replaceAll("#paciente", paciente.getDadosBasico().getNome());
        String orcamento = "<table border=0 class=\"ui-widget\" width=\"100%\"><tr><td width=\"80%\"><b>Procedimento</b></td><td width=\"20%\"><b>Valor</b></td></tr><tr><td>&nbsp;</td></tr>";
        List<ConvenioProcedimento> convenioProcedimentos = ConvenioProcedimentoSingleton.getInstance().getBo().listByConvenio(paciente.getConvenio(), UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
        boolean PS = false;
        for (PlanoTratamentoProcedimento ptp : planoDeTratamentoProcedimentos) {
            PS = false;
            orcamento += "<tr><td>" + ptp.getProcedimento().getDescricao() + ((ptp.getDenteObj() != null && !ptp.getDenteObj().getDescricao().equals(
                    "")) ? " ( Dente " + ptp.getDenteObj().getDescricao() + ")" : "") + "</td><td>";
            for (ConvenioProcedimento cp : convenioProcedimentos) {
                if (cp.getProcedimento().equals(ptp.getProcedimento())) {
                    if (cp.getConvenio().getTipo().equals("PS")) {
                        orcamento += "0,00 - (Convênio)";
                        valorPS = valorPS.add(ptp.getValorDesconto());
                        PS = true;
                        break;
                    }
                }
            }
            if (!PS) {
                orcamento += this.formataValor(ptp.getValorDesconto());
            }
            orcamento += "</td></td>";
        }
        BigDecimal valorOrcamento = BigDecimal.ZERO;
        // TODO - Estrutura mudada, nao foi alterado aqui pois o MB não é mais usado
//        for (Orcamento o : planoDeTratamento.getOrcamentos()) {
//            if (o.isAtivo()) {
//                valorOrcamento = o.getValorTotal();
//            }
//        }
        BigDecimal desconto = valorOrcamento.compareTo(BigDecimal.ZERO) == 0 ? valorOrcamento : planoDeTratamento.getValorTotal().subtract(valorOrcamento);
        BigDecimal valorTotal = planoDeTratamento.getValorTotalDesconto().subtract((valorPS));
        orcamento += "<tr><td>&nbsp;</td></tr><tr><td><b> Total</b></td><td><b>" + this.formataValor(planoDeTratamento.getValorTotal().subtract(valorPS)) + "</b></td></tr>";
        orcamento += "<tr><td>&nbsp;</td></tr><tr><td><b> Desconto</b></td><td><b>" + this.formataValor(desconto) + "</b></td></tr>";
        orcamento += "<tr><td>&nbsp;</td></tr><tr><td><b> Total com desconto</b></td><td><b>" + this.formataValor(valorTotal.subtract(desconto)) + "</b></td></tr>";
        /*
         * for (Desconto d : descontos) { orcamento += "<tr><td>&nbsp;</td></tr><tr><td><b>Total com desconto "+ d.getDescricao()+"</b></td><td><b>"
         * +valorTotal.subtract(valorTotal.multiply(d.getValorDesconto()).divide(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_UP)) + "</b></td></tr>"; finalizacao += "<br>"; }
         */
        orcamento += "</table>   <br><br>";
        documento = documento.replaceAll("#plano_tratamento", orcamento);
        // Se deixar algum span, o primefaces se perde e deixa um pouco de conteudo pra fora do printer
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
        tagDinamicas = DocumentoSingleton.getInstance().getBo().getTagDinamicas(documentoSelecionado, this.documentoSelecionado, tagDinamicas,
                new String[] { "#paciente", "#rg", "#datahoje", "#endereco_completo", "#plano_tratamento" });
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

    public PlanoTratamento getPlanoDeTratamento() {
        return planoDeTratamento;
    }

    public void setPlanoDeTratamento(PlanoTratamento planoDeTratamento) {
        this.planoDeTratamento = planoDeTratamento;
        planoDeTratamentoProcedimentos = PlanoTratamentoProcedimentoSingleton.getInstance().getBo().listByPlanoTratamento(planoDeTratamento);
    }

    public List<PlanoTratamento> getPlanoDeTratamentos() {
        return planoDeTratamentos;
    }

    public void setPlanoDeTratamentos(List<PlanoTratamento> planoDeTratamentos) {
        this.planoDeTratamentos = planoDeTratamentos;
    }

    public List<PlanoTratamentoProcedimento> getPlanoDeTratamentoProcedimentos() {
        return planoDeTratamentoProcedimentos;
    }

    public void setPlanoDeTratamentoProcedimentos(List<PlanoTratamentoProcedimento> planoDeTratamentoProcedimentos) {
        this.planoDeTratamentoProcedimentos = planoDeTratamentoProcedimentos;
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
}
