package br.com.lume.odonto.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.MathContext;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import br.com.lume.common.util.Status;
import br.com.lume.common.util.Utils;
import br.com.lume.odonto.bo.OrcamentoBO;
import br.com.lume.odonto.bo.RegiaoDenteBO;

@Entity
@Table(name = "PLANO_TRATAMENTO_PROCEDIMENTO")
public class PlanoTratamentoProcedimento implements Serializable, Comparable<PlanoTratamentoProcedimento> {

    public PlanoTratamentoProcedimento() {
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "VALOR")
    private BigDecimal valor;

    @Column(name = "TRIBUTO")
    private BigDecimal tributo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_PLANO_TRATAMENTO")
    private PlanoTratamento planoTratamento;

    @OneToMany(mappedBy = "planoTratamentoProcedimento", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Repasse> repasses;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_PROCEDIMENTO")
    private Procedimento procedimento;

    @Column(name = "VALOR_DESCONTO")
    private BigDecimal valorDesconto;

    @OneToMany(mappedBy = "planoTratamentoProcedimento", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RepasseLancamento> repasseLancamentos;

    @Transient
    private BigDecimal valorRepassadoProporcional;

    @Column(name = "DESCRICAO")
    private String descricao;

    @Column(name = "STATUS")
    private String status;

    @Column(name = "STATUS_PAGAMENTO")
    private Character statusPagamento = PAGAMENTO_PENDENTE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_DENTE")
    private Dente denteObj;

    @Column(name = "DENTE")
    private String dente;

    @Column(name = "STATUS_DENTE")
    private String statusDente;

    @Transient
    private String statusNovo;

    @Transient
    private boolean finalizado;

    @Transient
    private BigDecimal valorLabel;

    @Transient
    private BigDecimal valorDescontoLabel;

    @Transient
    private BigDecimal valorLiquido;

    @Transient
    private BigDecimal valorTributo;

    @Transient
    private BigDecimal valorRestante;

    @Transient
    private BigDecimal valorProfissional;

    @Transient
    private BigDecimal valorFinal;

    @Transient
    private BigDecimal valorAnterior;

    @Transient
    private String descricaoCompleta;

    private String excluido = Status.NAO;

    @Column(name = "DATA_FATURAMENTO")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataFaturamento;

    @Column(name = "DATA_REPASSE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataRepasse;

    @Column(name = "DATA_EXCLUSAO")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataExclusao;

    @ManyToOne
    @JoinColumn(name = "ID_CONVENIO")
    private Convenio convenio;

    @Column(name = "CODIGO_CONVENIO")
    private String codigoConvenio;

    @Column(name = "TIPO_REMUNERACAO_PROF_CALC")
    private String tipoRemuneracaoProfissionalCalc;

    @Column(name = "VALOR_REPASSADO")
    private BigDecimal valorRepassado;

    @Column(name = "VALOR_REPASSE")
    private BigDecimal valorRepasse;

    private int sequencial;

    private int requisito;

    private int prioridade;

    @Column(name = "QTD_CONSULTAS")
    private int qtdConsultas = 0;

    @ManyToOne
    @JoinColumn(name = "FINALIZADO_POR")
    private Profissional finalizadoPorProfissional;

    @Column(name = "DATA_FINALIZADO")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataFinalizado;

    @Transient
    private List<String> statuss;

    @OneToMany(mappedBy = "planoTratamentoProcedimento", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PlanoTratamentoProcedimentoFace> planoTratamentoProcedimentoFaces;

    @OneToMany(mappedBy = "planoTratamentoProcedimento", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PlanoTratamentoProcedimentoCusto> planoTratamentoProcedimentoCustos;

    @OneToMany(mappedBy = "planoTratamentoProcedimento")
    private List<AgendamentoPlanoTratamentoProcedimento> agendamentoPTP;

    @Transient
    private List<String> facesSelecionadas;

    @Column(name = "EXCLUIDO_POR")
    private Long excluidoPorProfissional;

    @Column(name = "valor_calc")
    private BigDecimal valorCalc;

    @Column(name = "tributo_calc")
    private BigDecimal tributoCalc;

    @Column(name = "custo_calc")
    private BigDecimal custoCalc;

    @Column(name = "percentual_calc")
    private Integer percentualCalc;

    private String regiao;

    private boolean ortodontico;

    public final static BigDecimal CEM = new BigDecimal(100);

    public static final Character PAGAMENTO_PENDENTE = 'P';

    public static final Character PAGAMENTO_RESERVADO = 'R';

    public static final Character PAGAMENTO_PAGO = 'G';

    @Transient
    private long qtdProcedimentosRelatorio;

    public Long getExcluidoPorProfissional() {
        return excluidoPorProfissional;
    }

    public void setExcluidoPorProfissional(Long excluidoPorProfissional) {
        this.excluidoPorProfissional = excluidoPorProfissional;
    }

    public String getExcluido() {
        return excluido;
    }

    public void setExcluido(String excluido) {
        this.excluido = excluido;
    }

    public Date getDataExclusao() {
        return dataExclusao;
    }

    public void setDescricaoCompleta(String descricaoCompleta) {
        this.descricaoCompleta = descricaoCompleta;
    }

    public void setDataExclusao(Date dataExclusao) {
        this.dataExclusao = dataExclusao;
    }

    public PlanoTratamentoProcedimento(BigDecimal valor, BigDecimal valorDesconto, PlanoTratamento planoTratamento, Procedimento procedimento) {
        super();
        this.valor = valor;
        this.valorDesconto = valorDesconto;
        this.planoTratamento = planoTratamento;
        this.procedimento = procedimento;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public BigDecimal getValor() {
        return valor == null ? BigDecimal.ZERO : valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (id ^ (id >>> 32));
        result = prime * result + ((procedimento == null) ? 0 : procedimento.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        PlanoTratamentoProcedimento other = (PlanoTratamentoProcedimento) obj;
        if (id != other.id) {
            return false;
        }
        if (procedimento == null) {
            if (other.procedimento != null) {
                return false;
            }
        } else if (!procedimento.equals(other.procedimento)) {
            return false;
        }
        return true;
    }

    public PlanoTratamento getPlanoTratamento() {
        return planoTratamento;
    }

    public void setPlanoTratamento(PlanoTratamento planoTratamento) {
        this.planoTratamento = planoTratamento;
    }

    public Procedimento getProcedimento() {
        return procedimento;
    }

    public void setProcedimento(Procedimento procedimento) {
        this.procedimento = procedimento;
    }

    public BigDecimal getValorDesconto() {
        return (valorDesconto == null || valorDesconto.compareTo(BigDecimal.ZERO) == 0) ? valor : valorDesconto;
    }

    public String getValorDescontoStr() {
        DecimalFormat df = new DecimalFormat("##,###,###,##0.00", new DecimalFormatSymbols(new Locale("pt", "BR")));
        df.setMinimumFractionDigits(2);
        df.setParseBigDecimal(true);
        return df.format(valorDesconto);
    }

    public void setValorDesconto(BigDecimal valorDesconto) {
        this.valorDesconto = valorDesconto;
    }

    public BigDecimal getValorLabel() {
        return valorLabel;
    }

    public void setValorLabel(BigDecimal valorLabel) {
        this.valorLabel = valorLabel;
    }

    public BigDecimal getValorDescontoLabel() {
        return valorDescontoLabel;
    }

    public void setValorDescontoLabel(BigDecimal valorDescontoLabel) {
        this.valorDescontoLabel = valorDescontoLabel;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getDescricaoStr() {
        String descricaoStr = "";
        if (denteObj != null && denteObj.getDescricao() != null && !denteObj.getDescricao().isEmpty()) {
            descricaoStr = this.getProcedimento().getDescricao() + " ( " + denteObj.getDescricao() + " )";
            return descricaoStr;
        } else if (dente != null && !dente.isEmpty()) {
            descricaoStr = this.getProcedimento().getDescricao() + " ( " + dente + " )";
            return descricaoStr;
        } else {
            return this.getProcedimento().getDescricao();
        }
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<String> getStatuss() {
        List<String> statuss = new ArrayList<>();
        statuss.add(status);
        return statuss;
    }

    public void setStatuss(List<String> statuss) {
        if (statuss == null || statuss.isEmpty()) {
            this.setStatus(null);
        } else {
            this.setStatus(statuss.get(0));
        }
    }

    @Transient
    public String getStatusStr() {
        return status;
    }

    @Override
    public int compareTo(PlanoTratamentoProcedimento o) {
        if (o == null || this == null || o.getDescricao() == null || this.getDescricao() == null) {
            return -1;
        }
        return Normalizer.normalize(descricao, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "").compareToIgnoreCase(Normalizer.normalize(o.getDescricao(), Normalizer.Form.NFD));
    }

    public int getSequencial() {
        return sequencial;
    }

    public void setSequencial(int sequencial) {
        this.sequencial = sequencial;
    }

    public int getRequisito() {
        return requisito;
    }

    public void setRequisito(int requisito) {
        this.requisito = requisito;
    }

    public int getPrioridade() {
        return prioridade;
    }

    public void setPrioridade(int prioridade) {
        this.prioridade = prioridade;
    }

    public int getQtdConsultas() {
        return qtdConsultas;
    }

    public void setQtdConsultas(int qtdConsultas) {
        this.qtdConsultas = qtdConsultas;
    }

    public String getStatusDente() {
        return statusDente;
    }

    public void setStatusDente(String statusDente) {
        this.statusDente = statusDente;
    }

    @Transient
    public List<String> getFaces() {
        List<String> faces = new ArrayList<>();
        faces.add(RegiaoDente.DISTAL);
        faces.add(RegiaoDente.LINGUAL);
        faces.add(RegiaoDente.MESIAL);
        faces.add(RegiaoDente.VESTIBULAR);
        if (denteObj != null && denteObj.getDescricao() != null && !denteObj.getDescricao().isEmpty()) {
            faces.add(RegiaoDenteBO.isIncisalOrOclusal(denteObj.getDescricao()));
        }
        return faces;
    }

    @Transient
    public List<String> geraSugestoesFace(String query) {
        List<String> faces = new ArrayList<>();
        faces.add(RegiaoDente.DISTAL);
        faces.add(RegiaoDente.LINGUAL);
        faces.add(RegiaoDente.MESIAL);
        faces.add(RegiaoDente.VESTIBULAR);
        if (denteObj != null && denteObj.getDescricao() != null && !denteObj.getDescricao().isEmpty()) {
            faces.add(RegiaoDenteBO.isIncisalOrOclusal(denteObj.getDescricao()));
        }
        List<String> suggestions = new ArrayList<>();
        for (String face : faces) {
            if (face.toLowerCase().startsWith(query.toLowerCase())) {
                suggestions.add(face);
            }
        }
        return suggestions;
    }

    public Profissional getFinalizadoPorProfissional() {
        return finalizadoPorProfissional;
    }

    public void setFinalizadoPorProfissional(Profissional finalizadoPorProfissional) {
        this.finalizadoPorProfissional = finalizadoPorProfissional;
    }

    public Date getDataFinalizado() {
        return dataFinalizado;
    }

    public void setDataFinalizado(Date dataFinalizado) {
        this.dataFinalizado = dataFinalizado;
    }

    public List<Lancamento> listLancamentos() {
        Orcamento orcamento = null;
        for (Orcamento o : new OrcamentoBO().listByPlanoTratamento(this.getPlanoTratamento())) {
            if (!o.getExcluido().equals("S")) {
                orcamento = o;
                break;
            }
        }
        if (orcamento == null) {
            return new ArrayList<>();
        }
        return orcamento.getLancamentos();
    }

    public Date getDataFaturamento() {
        return dataFaturamento;
    }

    public void setDataFaturamento(Date dataFaturamento) {
        this.dataFaturamento = dataFaturamento;
    }

    @Transient
    public String getDataFinalizadoStr() {
        if (this.getDataFinalizado() == null) {
            return "";
        } else {
            return Utils.dateToString(this.getDataFinalizado());
        }
    }

    @Transient
    public String getDataFinalizadoStrOrd() {
        return Utils.dateToString(this.getDataFinalizado(), "yyyy/MM/dd HH:mm:ss");
    }

    @Transient
    public String getDataFaturamentoStr() {
        if (this.getDataFaturamento() != null) {
            return Utils.dateToString(this.getDataFaturamento());
        } else {
            return null;
        }
    }

    @Transient
    public String getDataPagamentoStr() {
        Date data = null;
        if (repasses != null && !repasses.isEmpty()) {
            for (Repasse r : repasses) {
                if (r.getExcluido().equals(Status.NAO)) {
                    if (data == null || r.getData().after(data)) {
                        data = r.getData();
                    }
                }
            }
            return Utils.dateToString(data);
        }
        return null;
    }

    public String getStatusNovo() {
        return statusNovo;
    }

    public void setStatusNovo(String statusNovo) {
        this.statusNovo = statusNovo;
    }

    public boolean isFinalizado() {
        // return finalizado;
        return status != null && status.equals("F") ? true : false;
    }

    public void setFinalizado(boolean finalizado) {
        this.finalizado = finalizado;
    }

    public Convenio getConvenio() {
        return convenio;
    }

    public void setConvenio(Convenio convenio) {
        this.convenio = convenio;
    }

    public List<PlanoTratamentoProcedimentoFace> getPlanoTratamentoProcedimentoFaces() {
        return planoTratamentoProcedimentoFaces;
    }

    public void setPlanoTratamentoProcedimentoFaces(List<PlanoTratamentoProcedimentoFace> planoTratamentoProcedimentoFaces) {
        this.planoTratamentoProcedimentoFaces = planoTratamentoProcedimentoFaces;
    }

    public List<PlanoTratamentoProcedimentoCusto> getPlanoTratamentoProcedimentoCustos() {
        return planoTratamentoProcedimentoCustos;
    }

    public void setPlanoTratamentoProcedimentoCustos(List<PlanoTratamentoProcedimentoCusto> planoTratamentoProcedimentoCustos) {
        this.planoTratamentoProcedimentoCustos = planoTratamentoProcedimentoCustos;
    }

    public List<String> getFacesSelecionadas() {
        return facesSelecionadas;
    }

    public String getFacesSelecionadasStr() {
        String retorno = "";
        if (getPlanoTratamentoProcedimentoFaces() != null && !getPlanoTratamentoProcedimentoFaces().isEmpty()) {
            List<String> faces = new ArrayList<>();
            for (PlanoTratamentoProcedimentoFace ptpf : getPlanoTratamentoProcedimentoFaces()) {
                faces.add(ptpf.getFace());
            }
            if (faces != null && !faces.isEmpty()) {
                for (String f : faces) {
                    retorno += " - " + f;
                }
                retorno = retorno.replaceFirst(" - ", "");
            }
        } else {
            retorno = "[Selecione]";
        }
        return retorno;
    }

    public void setFacesSelecionadas(List<String> facesSelecionadas) {
        this.facesSelecionadas = facesSelecionadas;
    }

    public String getDescricaoCompleta() {
        if (denteObj != null && denteObj.getDescricao() != null && !denteObj.getDescricao().isEmpty()) {
            return sequencial + " - " + procedimento.getDescricao() + "( Dente " + denteObj.getDescricao() + ")";
        } else if (dente != null && !dente.isEmpty()) {
            return sequencial + " - " + procedimento.getDescricao() + "( Dente " + dente + ")";
        } else {
            return sequencial + " - " + procedimento.getDescricao();
        }
    }

    public BigDecimal getCustos() {
        BigDecimal custos = BigDecimal.ZERO;
        if (this.getPlanoTratamentoProcedimentoCustos() != null) {
            for (PlanoTratamentoProcedimentoCusto custo : this.getPlanoTratamentoProcedimentoCustos()) {
                if (!custo.getExcluido().equals(Status.SIM)) {
                    custos = custos.add(custo.getValor());
                }
            }
        }
        return custos;
    }

    public BigDecimal getValorLiquido() {
        BigDecimal custos = this.getCustos();
        return this.getValorProporcional().subtract(custos);
    }

    // n usar este metodo
    @Deprecated
    public BigDecimal getValorProfissional() {
        return null;
    }

    public Orcamento getOrcamentoRecente(List<Orcamento> orcamentos) {
        Orcamento orcamento = null;
        for (Orcamento o : orcamentos) {
            if (orcamento == null || o.getDataAprovacao().after(orcamento.getDataAprovacao())) {
                orcamento = o;
            }
        }
        return orcamento;
    }

    public BigDecimal getValorProporcional() {
        boolean orcamentoAtivo = false;
        BigDecimal valorSemTaxas = BigDecimal.ZERO;
        if (this.getPlanoTratamento().getOrcamentos() == null || this.getPlanoTratamento().getOrcamentos().isEmpty()) {
            return this.getValorDesconto();
        } else {
            for (Orcamento o : this.getPlanoTratamento().getOrcamentos()) {
                if (o.getExcluido().equals(Status.NAO)) {
                    orcamentoAtivo = true;
                }
                for (Lancamento l : o.getLancamentos()) {
                    if (l.getExcluido().equals(Status.NAO)) {
                        if (l.getDataPagamento() != null) {
                            if (l.getLancamentosContabeis() != null) {
                                for (LancamentoContabil lc : l.getLancamentosContabeis()) {
                                    valorSemTaxas = valorSemTaxas.add(lc.getValor());
                                }
                            }
                        } else {
                            valorSemTaxas = valorSemTaxas.add(l.getValor());
                        }
                    }
                }
            }
            if (orcamentoAtivo) {
                if (this.getPlanoTratamento().getValorTotal().compareTo(BigDecimal.ZERO) == 0) {
                    return BigDecimal.ZERO;
                } else {
                    // FIXME TA ERRADO ISSO, VERIFICAR
                    return this.getValorDesconto().multiply(valorSemTaxas.divide(this.getPlanoTratamento().getValorTotal(), MathContext.DECIMAL32));
                }
            } else {
                return this.getValorDesconto();
            }
        }
    }

    public BigDecimal getTributo() {
        return tributo;
    }

    public void setTributo(BigDecimal tributo) {
        this.tributo = tributo;
    }

    public String getCodigoConvenio() {
        return codigoConvenio;
    }

    public void setCodigoConvenio(String codigoConvenio) {
        this.codigoConvenio = codigoConvenio;
    }

    public void setValorTributo(BigDecimal valorTributo) {
        this.valorTributo = valorTributo;
    }

    public BigDecimal getValorFinal() {
        return this.getValorLiquido() != null ? this.getValorLiquido().subtract(valorTributo) : BigDecimal.ZERO;
    }

    public void setValorFinal(BigDecimal valorFinal) {
        this.valorFinal = valorFinal;
    }

    public BigDecimal getValoresRepassados() {
        BigDecimal valor = BigDecimal.ZERO;
        if (repasses != null && !repasses.isEmpty()) {
            for (Repasse r : repasses) {
                if (r.getExcluido().equals(Status.NAO)) {
                    valor = valor.add(r.getValor());
                }
            }
        }
        return valor;
    }

    public BigDecimal getValoresRepassados(Date inicio, Date fim) {
        BigDecimal valor = BigDecimal.ZERO;
        if (repasses != null && !repasses.isEmpty()) {
            for (Repasse r : repasses) {
                if (inicio == null || (inicio != null && inicio.before(r.getData()))) {
                    if (fim == null || (fim != null && fim.after(r.getData()))) {
                        if (r.getExcluido().equals(Status.NAO)) {
                            valor = valor.add(r.getValor());
                        }
                    }
                }
            }
        }
        return valor;
    }

    public BigDecimal getValorRestante() {
        //return getValorRepasse().subtract(getValorPago().add(getValorReservado()));
        return this.getValorRepasse().subtract(this.getValorRepassado());
    }

    public void setValorRepassadoProporcional(BigDecimal valorRepassadoProporcional) {
        this.valorRepassadoProporcional = valorRepassadoProporcional;
    }

    public List<Repasse> getRepasses() {
        return repasses;
    }

    public void setRepasses(List<Repasse> repasses) {
        this.repasses = repasses;
    }

    public BigDecimal getValorRepassado() {
        return valorRepassado != null ? valorRepassado : new BigDecimal(0);
    }

    public void setValorRepassado(BigDecimal valorRepassado) {
        this.valorRepassado = valorRepassado;
    }

    public BigDecimal getValorRepasse() {
        return valorRepasse != null ? valorRepasse : new BigDecimal(0);
    }

    public void setValorRepasse(BigDecimal valorRepasse) {
        this.valorRepasse = valorRepasse;
    }

    public Date getDataRepasse() {
        return dataRepasse;
    }

    public String getDataRepasseStr() {
        return Utils.dateToString(dataRepasse);
    }

    public void setDataRepasse(Date dataRepasse) {
        this.dataRepasse = dataRepasse;
    }

    public Character getStatusPagamento() {
        return statusPagamento;
    }

    public BigDecimal getValorReservado() {
        BigDecimal repassado = new BigDecimal(0);
        if (repasseLancamentos != null && !repasseLancamentos.isEmpty()) {
            for (RepasseLancamento rl : repasseLancamentos) {
                if (RepasseItem.REPASSADO.equals(rl.getStatus())) {
                    repassado = repassado.add(rl.getValor());
                }
            }
        }
        return repassado;
    }

    public BigDecimal getValorPago() {
        BigDecimal repassado = new BigDecimal(0);
        if (repasseLancamentos != null && !repasseLancamentos.isEmpty()) {
            for (RepasseLancamento rl : repasseLancamentos) {
                if (RepasseItem.PAGO_COMPLETO.equals(rl.getStatus())) {
                    repassado = repassado.add(rl.getValor());
                }
            }
        }
        return repassado;
    }

    public void setStatusPagamento(Character statusPagamento) {
        this.statusPagamento = statusPagamento;
    }

    public String getStatusPagamentoStr() {
        if (PAGAMENTO_PENDENTE.equals(statusPagamento)) {
            return "Pendente";
        } else if (PAGAMENTO_RESERVADO.equals(statusPagamento)) {
            return "Reservado";
        } else if (PAGAMENTO_PAGO.equals(statusPagamento)) {
            return "Pago";
        }
        return "";
    }

    public void addRepasseLancamento(RepasseLancamento repasseLancamento) {
        if (repasseLancamentos == null) {
            repasseLancamentos = new ArrayList<>();
        }
        repasseLancamentos.add(repasseLancamento);
    }

    public List<RepasseLancamento> getRepasseLancamentos() {
        return repasseLancamentos;
    }

    public void setRepasseLancamentos(List<RepasseLancamento> repasseLancamentos) {
        this.repasseLancamentos = repasseLancamentos;
    }

    public BigDecimal getValorCalc() {
        return valorCalc;
    }

    public void setValorCalc(BigDecimal valorCalc) {
        this.valorCalc = valorCalc;
    }

    public BigDecimal getTributoCalc() {
        return tributoCalc;
    }

    public void setTributoCalc(BigDecimal tributoCalc) {
        this.tributoCalc = tributoCalc;
    }

    public BigDecimal getCustoCalc() {
        return custoCalc;
    }

    public void setCustoCalc(BigDecimal custoCalc) {
        this.custoCalc = custoCalc;
    }

    public Integer getPercentualCalc() {
        return percentualCalc;
    }

    public void setPercentualCalc(Integer percentualCalc) {
        this.percentualCalc = percentualCalc;
    }

    public String getTipoRemuneracaoProfissionalCalc() {
        return tipoRemuneracaoProfissionalCalc;
    }

    public void setTipoRemuneracaoProfissionalCalc(String tipoRemuneracaoProfissionalCalc) {
        this.tipoRemuneracaoProfissionalCalc = tipoRemuneracaoProfissionalCalc;
    }

    public BigDecimal getValorAnterior() {
        return valorAnterior;
    }

    public void setValorAnterior(BigDecimal valorAnterior) {
        this.valorAnterior = valorAnterior;
    }

    public Dente getDenteObj() {
        return denteObj;
    }

    public void setDenteObj(Dente denteObj) {
        this.denteObj = denteObj;
    }

    public String getRegiao() {
        return regiao;
    }

    public void setRegiao(String regiao) {
        this.regiao = regiao;
    }

    public String getDenteRegiao() {
        if (denteObj != null) {
            return denteObj.getDescricao();
        } else if (dente != null) {
            return dente;
        } else {
            return regiao;
        }
    }

    public String getDente() {
        return dente;
    }

    public void setDente(String dente) {
        this.dente = dente;
    }

    public List<AgendamentoPlanoTratamentoProcedimento> getAgendamentoPTP() {
        return agendamentoPTP;
    }

    public void setAgendamentoPTP(List<AgendamentoPlanoTratamentoProcedimento> agendamentoPTP) {
        this.agendamentoPTP = agendamentoPTP;
    }

    public String getAgendamentoPTPDatas() {
        String datas = "";
        if (agendamentoPTP != null && !agendamentoPTP.isEmpty()) {
            for (AgendamentoPlanoTratamentoProcedimento agendamentoPlanoTratamentoProcedimento : agendamentoPTP) {
                datas += agendamentoPlanoTratamentoProcedimento.getAgendamento().getInicioStr() + " ";
            }
        }
        return datas;
    }

    public boolean isOrtodontico() {
        return ortodontico;
    }

    public void setOrtodontico(boolean ortodontico) {
        this.ortodontico = ortodontico;
    }

    public long getQtdProcedimentosRelatorio() {
        return qtdProcedimentosRelatorio;
    }

    public void setQtdProcedimentosRelatorio(long qtdProcedimentosRelatorio) {
        this.qtdProcedimentosRelatorio = qtdProcedimentosRelatorio;
    }

}
