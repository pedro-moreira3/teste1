package br.com.lume.odonto.managed;

import java.math.BigDecimal;
import java.math.MathContext;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ActionEvent;

import org.apache.log4j.Logger;
import org.primefaces.event.SelectEvent;

import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.configuracao.Configurar;
import br.com.lume.documento.DocumentoSingleton;
import br.com.lume.documentoFaturamento.DocumentoFaturamentoSingleton;
import br.com.lume.dominio.DominioSingleton;
import br.com.lume.odonto.entity.Documento;
import br.com.lume.odonto.entity.DocumentoFaturamento;
import br.com.lume.odonto.entity.Dominio;
import br.com.lume.odonto.entity.Lancamento;
import br.com.lume.odonto.entity.Orcamento;
import br.com.lume.odonto.entity.PlanoTratamentoProcedimento;
import br.com.lume.odonto.entity.Profissional;
import br.com.lume.odonto.entity.TagDocumento;
import br.com.lume.odonto.util.OdontoMensagens;
import br.com.lume.orcamento.OrcamentoSingleton;
import br.com.lume.planoTratamentoProcedimento.PlanoTratamentoProcedimentoSingleton;
import br.com.lume.profissional.ProfissionalSingleton;

@ManagedBean
@ViewScoped
public class DocumentoFaturamentoMB extends LumeManagedBean<DocumentoFaturamento> {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(DocumentoFaturamentoMB.class);

    private String documento;

    private List<Documento> documentos;

    private boolean visivel = false;

    private Documento documentoSelecionado;

    private PlanoTratamentoProcedimento faturamento;

    private List<PlanoTratamentoProcedimento> faturamentos;

    private Set<TagDocumento> tagDinamicas;

    private Profissional profissional;

    private Date inicio, fim;

//    private DominioBO dominioBO;
//
//    private DocumentoBO documentoBO;
//
//    private DocumentoFaturamentoBO documentoFaturamentoBO;
//
//    private PlanoTratamentoProcedimentoBO planoTratamentoProcedimentoBO;
//
//    private OrcamentoBO orcamentoBO;
//
//    private ProfissionalBO profissionalBO;

    private List<DocumentoFaturamento> documentosFaturamento;

    public DocumentoFaturamentoMB() {
        super(DocumentoFaturamentoSingleton.getInstance().getBo());
//        this.dominioBO = new DominioBO();
//        this.documentoBO = new DocumentoBO();
//        this.documentoFaturamentoBO = new DocumentoFaturamentoBO();
//        this.planoTratamentoProcedimentoBO = new PlanoTratamentoProcedimentoBO();
//        this.orcamentoBO = new OrcamentoBO();
//        this.profissionalBO = new ProfissionalBO();
        try {
            Dominio dominio = DominioSingleton.getInstance().getBo().findByEmpresaAndObjetoAndTipoAndValor("documento", "tipo", "F");
            this.documentos = DocumentoSingleton.getInstance().getBo().listByTipoDocumento(dominio);
        } catch (Exception e) {
            this.addError(OdontoMensagens.getMensagem("documento.erro.documento.carregar"), "");
            e.printStackTrace();
        }
        this.setClazz(DocumentoFaturamento.class);
    }

    @Override
    public void actionPersist(ActionEvent event) {
        try {
            if (this.documentoSelecionado != null) {
                this.carregarDados();
                this.documento = this.documentoSelecionado.getModelo();
                this.replaceDocumento();
                this.visivel = true;
            }
            this.getEntity().setDocumentoGerado(this.documento);
            this.getEntity().setProfissional(this.profissional);
            DocumentoFaturamentoSingleton.getInstance().getBo().persist(this.getEntity());
            this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");
        } catch (Exception e) {
            this.log.error("Erro no actionPersist Atestado", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
        }
    }

    @Override
    public void actionNew(ActionEvent event) {
        this.tagDinamicas = null;
        this.visivel = false;
        this.documentoSelecionado = null;
        this.profissional = null;
        this.inicio = null;
        this.fim = null;
        this.setEntity(new DocumentoFaturamento());
    }

    public void carregarDados() {
        if (this.profissional != null) {
            try {
                this.faturamentos = PlanoTratamentoProcedimentoSingleton.getInstance().getBo().listAllByPacienteAndProfissionalAndPeriodoAndProcedimento(null, this.profissional, this.inicio, this.fim, null, "Pagos");
                this.documentosFaturamento = DocumentoFaturamentoSingleton.getInstance().getBo().listByProfissional(this.profissional);
            } catch (Exception e) {
                this.log.error("Erro no carregarDados", e);
                this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            }
        }
    }

    public void atualizaTela() {
        this.documento = this.getEntity().getDocumentoGerado();
        this.visivel = true;
    }

    public String format(BigDecimal num, boolean money) {
        String retorno = new java.text.DecimalFormat("###,###,###,###,##0.00").format(num);
        if (money) {
            retorno = "R\\$ " + retorno;
        } else {
            retorno = "(" + retorno + " \\%)";
        }
        return retorno;
    }

    public List<Lancamento> listLancamentos(PlanoTratamentoProcedimento ptp) {
        Orcamento orcamento = null;
        for (Orcamento o : OrcamentoSingleton.getInstance().getBo().listByPlanoTratamento(ptp.getPlanoTratamento())) {
            if (!o.getExcluido().equals("S")) {
                orcamento = o;
                break;
            }
        }
        return orcamento.getLancamentos();
    }

    private void replaceDocumento() {
        this.documento = DocumentoSingleton.getInstance().getBo().replaceDocumento(this.tagDinamicas, this.profissional.getDadosBasico(), this.documento);
        this.documento = this.documento.replaceAll("#periodo", this.getInicioStr() + " à " + this.getFimStr());
        // String finalizacao = "<br><br><br><br><br><br><br>";// Cabeçalho e Rodapé
        String faturamento = INICIO_TABELA;
        BigDecimal valor = BigDecimal.ZERO;
        BigDecimal custos = BigDecimal.ZERO;
        BigDecimal liquido = BigDecimal.ZERO;
        BigDecimal valorLiquidoSemTributo = BigDecimal.ZERO;
        BigDecimal percentualTributo = BigDecimal.ZERO;
        BigDecimal valorTributo = BigDecimal.ZERO;
        BigDecimal valorTotal = BigDecimal.ZERO;
        BigDecimal custoTotal = BigDecimal.ZERO;
        BigDecimal liquidoTotal = BigDecimal.ZERO;
        BigDecimal tributoTotal = BigDecimal.ZERO;
        BigDecimal valorSemTributosTotal = BigDecimal.ZERO;
        BigDecimal valorRepassadoPeriodoTotal = BigDecimal.ZERO;
        BigDecimal valorRepassadoAnteriorTotal = BigDecimal.ZERO;
        BigDecimal valorRepassadoRestanteTotal = BigDecimal.ZERO;
        BigDecimal valorProfissionalTotal = BigDecimal.ZERO;
        for (PlanoTratamentoProcedimento ptp : this.faturamentos) {
            valor = ptp.getValorProporcional();
            custos = ptp.getCustos();
            liquido = valor.subtract(custos);
            percentualTributo = ptp.getTributo();
            valorTributo = liquido.multiply(percentualTributo.divide(new BigDecimal(100), MathContext.DECIMAL32));
            valorLiquidoSemTributo = liquido.subtract(valorTributo);
            faturamento += TDe + ptp.getPlanoTratamento().getPaciente().getDadosBasico().getNome() + TDf + TDe + ptp.getProcedimento().getDescricao() + TDf + TDd + this.format(valor,
                    true) + TDf + TDd + this.format(custos, true) + TDf + TDd + this.format(valorTributo, true) + TDf + TDd + this.format(valorLiquidoSemTributo, true) + this.format(percentualTributo,
                            false) + TDf + TDd + this.format(ptp.getValorProfissional(), true) + TDf + TDd + this.format(ptp.getValoresRepassados(this.inicio, this.fim),
                                    true) + TDf + TDd + this.format(ptp.getValoresRepassados().subtract(ptp.getValoresRepassados(this.inicio, this.fim)),
                                            true) + TDf + TDd + this.format(ptp.getValorRestante(), true) + TDf + TRf;
            valorTotal = valorTotal.add(ptp.getValorProporcional());
            custoTotal = custoTotal.add(ptp.getCustos());
            liquidoTotal = liquidoTotal.add(liquido);
            tributoTotal = tributoTotal.add(valorTributo);
            valorSemTributosTotal = valorSemTributosTotal.add(valorLiquidoSemTributo);
            valorRepassadoPeriodoTotal = valorRepassadoPeriodoTotal.add(ptp.getValoresRepassados(this.inicio, this.fim));
            valorRepassadoAnteriorTotal = valorRepassadoAnteriorTotal.add(ptp.getValoresRepassados().subtract(ptp.getValoresRepassados(this.inicio, this.fim)));
            valorRepassadoRestanteTotal = valorRepassadoRestanteTotal.add(ptp.getValorRestante());
            valorProfissionalTotal = valorProfissionalTotal.add(ptp.getValorProfissional());
        }
        faturamento += INICIO_TOTAL + TDd + B + "  " + TDf + TDd + B + this.format(valorTotal, true) + Bf + TDf + TDd + B + this.format(custoTotal, true) + Bf + TDf + TDd + B + this.format(
                tributoTotal, true) + Bf + TDf + TDd + B + this.format(valorSemTributosTotal, true) + Bf + TDf + TDd + B + this.format(valorProfissionalTotal, true) + Bf + TDf + TDd + B + this.format(
                        valorRepassadoPeriodoTotal,
                        true) + Bf + TDf + TDd + B + this.format(valorRepassadoAnteriorTotal, true) + Bf + TDf + TDd + B + this.format(valorRepassadoRestanteTotal, true) + Bf + TDf + FIM_TOTAL;
        this.documento = this.documento.replaceAll("#faturamento", faturamento);
        // Se deixar algum span, o primefaces se perde e deixa um pouco de conteudo pra fora do printer
        this.documento = this.documento.replaceAll("span", "div");
    }

    public String getDocumento() {
        return this.documento;
    }

    public void setDocumento(String documento) {
        this.documento = documento;
    }

    public List<Documento> getDocumentos() {
        return this.documentos;
    }

    public void setDocumentos(List<Documento> documentos) {
        this.documentos = documentos;
    }

    public boolean isVisivel() {
        return this.visivel;
    }

    public void setVisivel(boolean visivel) {
        this.visivel = visivel;
    }

    public Documento getDocumentoSelecionado() {
        return this.documentoSelecionado;
    }

    public void setDocumentoSelecionado(Documento documentoSelecionado) {
        this.tagDinamicas = DocumentoSingleton.getInstance().getBo().getTagDinamicas(documentoSelecionado, this.documentoSelecionado, this.tagDinamicas, new String[] { "#profissional", "#periodo", "#faturamento" });
        this.documentoSelecionado = documentoSelecionado;
        this.visivel = true;
        this.documento = documentoSelecionado.getModelo();
    }

    public Set<TagDocumento> getTagDinamicas() {
        return this.tagDinamicas;
    }

    public List<TagDocumento> getTagDinamicasAsList() {
        List<TagDocumento> listAux = this.tagDinamicas != null ? new ArrayList<>(this.tagDinamicas) : new ArrayList<>();
        Collections.sort(listAux);
        return listAux;
    }

    public void setTagDinamicas(Set<TagDocumento> tagDinamicas) {
        this.tagDinamicas = tagDinamicas;
    }

    public PlanoTratamentoProcedimento getFaturamento() {
        return this.faturamento;
    }

    public void setFaturamento(PlanoTratamentoProcedimento faturamento) {
        this.faturamento = faturamento;
    }

    public List<PlanoTratamentoProcedimento> getFaturamentos() {
        return this.faturamentos;
    }

    public void setFaturamentos(List<PlanoTratamentoProcedimento> faturamentos) {
        this.faturamentos = faturamentos;
    }

    public Profissional getProfissional() {
        return this.profissional;
    }

    public void setProfissional(Profissional profissional) {
        this.profissional = profissional;
    }

    public String getInicioStr() {
        return new SimpleDateFormat("dd/MM/yyyy").format(this.inicio);
    }

    public String getFimStr() {
        return new SimpleDateFormat("dd/MM/yyyy").format(this.fim);
    }

    public Date getInicio() {
        return this.inicio;
    }

    public void setInicio(Date inicio) {
        this.inicio = inicio;
    }

    public Date getFim() {
        return this.fim;
    }

    public void setFim(Date fim) {
        this.fim = fim;
    }

    public void handleSelectProfissionalSelecionado(SelectEvent event) {
        Object object = event.getObject();
        this.profissional = (Profissional) object;
        this.carregarDados();
    }

    public List<Profissional> geraSugestoes(String query) {
        return ProfissionalSingleton.getInstance().getBo().listSugestoesComplete(query,Configurar.getInstance().getConfiguracao().getProfissionalLogado().getIdEmpresa());
    }

    public List<DocumentoFaturamento> getDocumentosFaturamento() {
        return this.documentosFaturamento;
    }

    public void setDocumentosFaturamento(List<DocumentoFaturamento> documentosFaturamento) {
        this.documentosFaturamento = documentosFaturamento;
    }

    public static final String TDd = "<td align=\"right\">";

    public static final String TDf = "</td>";

    public static final String TDe = "<td align=\"left\">";

    public static final String TRf = "</tr>";

    public static final String TR = "<tr>";

    public static final String B = "<b>";

    public static final String Bf = "</b>";

    public static final String INICIO_TABELA = "<table border=0 class=\"ui-widget\" width=\"100%\"><tr>" + "<td align=\"left\"  width=\"10%\"><b> Paciente       </b><td align=\"left\"  width=\"10%\"><b> Procedimento       </b></td>" + "<td align=\"right\" width=\"10%\"><b> Valor              </b></td>" + "<td align=\"right\" width=\"10%\"><b> Custos             </b></td><td align=\"right\" width=\"10%\"><b> Tributo      </b></td>" + "<td align=\"right\" width=\"10%\"><b> Valor sem tributos            </b></td><td align=\"right\" width=\"10%\"><b> Valor Profissional </b></td>" + "<td align=\"right\" width=\"10%\"><b> Valor repassado no período </b></td><td align=\"right\" width=\"10%\"><b> Valor repassado anterior    </b></td>" + "<td align=\"right\" width=\"10%\"><b> Valor Restante     </b></td></tr>" + "<tr><td>&nbsp;</td></tr><tr>";

    public static final String INICIO_TOTAL = "<tr><td>&nbsp;</td></tr><tr><td align=\"left\"><b> Totais </b></td>";

    public static final String FIM_TOTAL = "<td></td><td></td></tr></table>   <br><br>";
}
