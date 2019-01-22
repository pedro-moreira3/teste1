
package br.com.lume.odonto.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.Normalizer;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
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

/**
 * The persistent class for the PLANO_TRATAMENTO database table.
 */
@Entity
@Table(name = "PLANO_TRATAMENTO")
public class PlanoTratamento implements Serializable, Comparable<PlanoTratamento> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // bi-directional many-to-one association to Paciente
    @ManyToOne
    @JoinColumn(name = "ID_PACIENTE")
    private Paciente paciente;

    // bi-directional many-to-one association to Profissional
    @ManyToOne
    @JoinColumn(name = "ID_PROFISSIONAL")
    private Profissional profissional;

    @Column(name = "DATA_HORA")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataHora = new Timestamp(Calendar.getInstance().getTimeInMillis());

    @Column(name = "VALOR_TOTAL")
    private BigDecimal valorTotal;

    @Column(name = "VALOR_TOTAL_DESCONTO")
    private BigDecimal valorTotalDesconto;

    private BigDecimal desconto;

    private Integer meses;

    @OneToMany(mappedBy = "planoTratamento")
    private List<PlanoTratamentoProcedimento> planoTratamentoProcedimentos;

    @OneToMany(mappedBy = "planoTratamento", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Orcamento> orcamentos;

    @OneToMany(mappedBy = "planoTratamento", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PlanoTratamentoDiagnostico> diagnosticos;

    @OneToMany(mappedBy = "planoTratamento", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PlanoTratamentoAparelho> aparelhos;

    @Column(name = "DESCRICAO")
    private String descricao;

    private String excluido = Status.NAO;

    @Column(name = "DATA_EXCLUSAO")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataExclusao;

    @Column(name = "INICIO")
    @Temporal(TemporalType.DATE)
    private Date inicio;

    @Column(name = "FIM")
    @Temporal(TemporalType.DATE)
    private Date fim;

    @ManyToOne
    @JoinColumn(name = "FINALIZADO_POR")
    private Profissional finalizadoPorProfissional;

    @ManyToOne
    @JoinColumn(name = "ID_ODONTOGRAMA")
    private Odontograma odontograma;

    @Column(name = "DATA_FINALIZADO")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataFinalizado;// = new Timestamp(Calendar.getInstance().getTimeInMillis());

    private String finalizado = Status.NAO;

    private String justificativa;

    @Column(name = "EXCLUIDO_POR")
    private Long excluidoPorProfissional;

    private boolean alterouvaloresdesconto;

    @Transient
    private BigDecimal valor;

    @Transient
    private BigDecimal valorPago;

    @Transient
    private BigDecimal valorOrcamentoRestante;

    @Transient
    private BigDecimal valorTotalRestante;

    private boolean bconvenio;

    private boolean ortodontico;

    @ManyToOne
    @JoinColumn(name = "ID_CONVENIO")
    private Convenio convenio;

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

    public void setDataExclusao(Date dataExclusao) {
        this.dataExclusao = dataExclusao;
    }

    public PlanoTratamento() {
    }

    public PlanoTratamento(Odontograma odontograma) {
        this.odontograma = odontograma;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getDataHora() {
        return dataHora;
    }

    public void setDataHora(Date dataHora) {
        this.dataHora = dataHora;
    }

    public Paciente getPaciente() {
        return paciente;
    }

    public void setPaciente(Paciente paciente) {
        this.paciente = paciente;
    }

    public Profissional getProfissional() {
        return profissional;
    }

    public void setProfissional(Profissional profissional) {
        this.profissional = profissional;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(BigDecimal valorTotal) {
        this.valorTotal = valorTotal;
    }

    @Transient
    public String getDataHoraStr() {
        return Utils.dateToString(dataHora);
    }

    @Transient
    public String getDataHoraStrOrd() {
        return Utils.dateToString(dataHora, "yyyy/MM/dd HH:mm:ss");
    }

    @Transient
    public BigDecimal getValorPagar() {
        valor = new OrcamentoBO().valorTotalAPagar(this, ortodontico);
        return valor == null ? valorTotalDesconto : valor;
    }

    @Transient
    public BigDecimal getValorTotalOrcamento() {
        BigDecimal valor = new OrcamentoBO().valorTotal(this, ortodontico);
        return valor;
    }

    @Transient
    public String getDataHoraDescStr() {
        if (dataHora != null) {
            if ((descricao != null) && (!descricao.equals(""))) {
                return this.getDataHoraStr() + "-" + descricao;
            }
        }
        return this.getDataHoraStr();
    }

    public List<PlanoTratamentoProcedimento> getPlanoTratamentoProcedimentos() {
        return planoTratamentoProcedimentos;
    }

    public void setPlanoTratamentoProcedimentos(List<PlanoTratamentoProcedimento> planoTratamentoProcedimentos) {
        this.planoTratamentoProcedimentos = planoTratamentoProcedimentos;
    }

    public BigDecimal getValorTotalDesconto() {
        return valorTotalDesconto;
    }

    public void setValorTotalDesconto(BigDecimal valorTotalDesconto) {
        this.valorTotalDesconto = valorTotalDesconto;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
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
        PlanoTratamento other = (PlanoTratamento) obj;
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        return true;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getDescricaoComConvenio() {
        return "[ " + (convenio != null ? convenio.getDadosBasico().getNome() : "Particular") + " ]" + descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    @Override
    public int compareTo(PlanoTratamento o) {
        if (descricao == null) {
            if (o.descricao == null) {
                return 0;
            } else {
                return -1;
            }
        } else if (o.descricao == null) {
            return 1;
        }
        return Normalizer.normalize(descricao, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "").compareToIgnoreCase(Normalizer.normalize(o.descricao, Normalizer.Form.NFD));
    }

    public Profissional getFinalizadoPorProfissional() {
        return finalizadoPorProfissional;
    }

    public void setFinalizadoPorProfissional(Profissional finalizadoPorProfissional) {
        this.finalizadoPorProfissional = finalizadoPorProfissional;
    }

    public String getFinalizado() {
        return finalizado;
    }

    public String getFinalizadoStr() {
        if (finalizado != null) {
            return finalizado.equals(Status.SIM) ? "Sim" : "Não";
        }
        return "";
    }

    public void setFinalizado(String finalizado) {
        this.finalizado = finalizado;
    }

    public Date getDataFinalizado() {
        return dataFinalizado;
    }

    public void setDataFinalizado(Date dataFinalizado) {
        this.dataFinalizado = dataFinalizado;
    }

    @Transient
    public String getDataFinalizadoStr() {
        return Utils.dateToString(dataFinalizado);
    }

    @Transient
    public String getDataFinalizadoStrOrd() {
        return Utils.dateToString(dataFinalizado, "yyyy/MM/dd HH:mm:ss");
    }

    public String getJustificativa() {
        return justificativa;
    }

    public void setJustificativa(String justificativa) {
        this.justificativa = justificativa;
    }

    @Transient
    public String getExcluidoStr() {
        return excluido.equals(Status.SIM) ? "Sim" : "Não";
    }

    @Transient
    public String getStatus() {
        /*
         * Finalizado justificativa is null Encerrado justificativa is not null Ativo others Excluido Pendente Pagamento orcamento>lancamento status ativo!
         */
        String retorno = "Ativo";
        if (Status.SIM.equals(finalizado) && justificativa == null) {
            retorno = "Finalizado";
        } else if (Status.SIM.equals(finalizado) && justificativa != null) {
            retorno = "Encerrado [" + justificativa + "]";
        } else if (Status.SIM.equals(excluido)) {
            retorno = "Excluido";
        } else if (orcamentos != null) {
            for (Orcamento o : orcamentos) {
                if (o.getLancamentos() != null) {
                    for (Lancamento l : o.getLancamentos()) {
                        if ("Ativo".equals(l.getStatus())) {
                            retorno = "Pendente Pagamento";
                            break;
                        }
                    }
                }
            }
        }
        return retorno;
    }

    public List<Orcamento> getOrcamentos() {
        return orcamentos;
    }

    public void setOrcamentos(List<Orcamento> orcamentos) {
        this.orcamentos = orcamentos;
    }

    public BigDecimal getValor() {
        valor = new OrcamentoBO().valorTotalAPagar(this, ortodontico);
        return valor == null ? valorTotalDesconto : valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public Odontograma getOdontograma() {
        return odontograma;
    }

    public void setOdontograma(Odontograma odontograma) {
        this.odontograma = odontograma;
    }

    public BigDecimal getDesconto() {
        return desconto;
    }

    public void setDesconto(BigDecimal desconto) {
        this.desconto = desconto;
    }

    public Convenio getConvenio() {
        return convenio;
    }

    public void setConvenio(Convenio convenio) {
        this.convenio = convenio;
    }

    public boolean isBconvenio() {
        return bconvenio;
    }

    public boolean isConvenioValor() {
        if (bconvenio && convenio != null && convenio.getTipo().equals(Convenio.CONVENIO_PLANO_SAUDE)) {
            return true;
        }
        return false;
    }

    public void setBconvenio(boolean bconvenio) {
        this.bconvenio = bconvenio;
    }

    @Transient
    public String getInicioStr() {
        return Utils.dateToString(inicio, "dd/MM/yyyy");
    }

    @Transient
    public String getFimStr() {
        return Utils.dateToString(fim, "dd/MM/yyyy");
    }

    public Date getInicio() {
        return inicio;
    }

    public void setInicio(Date inicio) {
        this.inicio = inicio;
    }

    public Date getFim() {
        return fim;
    }

    public void setFim(Date fim) {
        this.fim = fim;
    }

    public boolean isExisteProcedimentoFinalizado() {
        if (planoTratamentoProcedimentos != null && !planoTratamentoProcedimentos.isEmpty()) {
            for (PlanoTratamentoProcedimento ptp : planoTratamentoProcedimentos) {
                if (ptp.isFinalizado()) {
                    return true;
                }
            }
        }
        return false;
    }

    public List<PlanoTratamentoDiagnostico> getDiagnosticos() {
        return diagnosticos;
    }

    public void setDiagnosticos(List<PlanoTratamentoDiagnostico> diagnosticos) {
        this.diagnosticos = diagnosticos;
    }

    public List<PlanoTratamentoAparelho> getAparelhos() {
        return aparelhos;
    }

    public void setAparelhos(List<PlanoTratamentoAparelho> aparelhos) {
        this.aparelhos = aparelhos;
    }

    public boolean isOrtodontico() {
        return ortodontico;
    }

    public void setOrtodontico(boolean ortodontico) {
        this.ortodontico = ortodontico;
    }

    public Integer getMeses() {
        return meses;
    }

    public void setMeses(Integer meses) {
        this.meses = meses;
    }

    public BigDecimal getValorPago() {
        return valorPago;
    }

    public void setValorPago(BigDecimal valorPago) {
        this.valorPago = valorPago;
    }

    public BigDecimal getValorOrcamentoRestante() {
        return valorOrcamentoRestante;
    }

    public void setValorOrcamentoRestante(BigDecimal valorOrcamentoRestante) {
        this.valorOrcamentoRestante = valorOrcamentoRestante;
    }

    public BigDecimal getValorTotalRestante() {
        return valorTotalRestante;
    }

    public void setValorTotalRestante(BigDecimal valorTotalRestante) {
        this.valorTotalRestante = valorTotalRestante;
    }

    public boolean isAlterouvaloresdesconto() {
        return alterouvaloresdesconto;
    }

    public void setAlterouvaloresdesconto(boolean alterouvaloresdesconto) {
        this.alterouvaloresdesconto = alterouvaloresdesconto;
    }

}
