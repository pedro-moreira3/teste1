package br.com.lume.odonto.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.primefaces.model.StreamedContent;

import br.com.lume.common.util.Status;
import br.com.lume.odonto.bo.ProfissionalBO;

/**
 * The persistent class for the PROFISSIONAL database table.
 */
@Entity
@Table(name = "PROFISSIONAL")
public class Profissional implements Serializable, Comparable<Profissional> {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // bi-directional many-to-one association to DadosBasico
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "ID_DADOS_BASICOS")
    private DadosBasico dadosBasico = new DadosBasico();

    @Lob
    private byte[] certificado;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "ID_PROFISSIONAL")
    private List<ProfissionalFilial> profissionalFilials = new ArrayList<>();

    @Column(name = "SENHA_CERTIFICADO")
    private String senhaCertificado;

    @Column(name = "ID_USUARIO")
    private Long idUsuario;

    @OneToMany(mappedBy = "profissional", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProfissionalEspecialidade> profissionalEspecialidade;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "ID_PROFISSIONAL")
    private List<ObjetoProfissional> objetosProfissional;

    @Column(name = "ID_EMPRESA")
    private Long idEmpresa;

    @Column(name = "PERCENTUAL_REMUNERACAO")
    private Integer percentualRemuneracao;

    @Column(name = "REMUNERACAO_MANUTENCAO")
    private Integer remuneracaoManutencao;

    private String perfil;

    @Column(name = "TEMPO_CONSULTA")
    private Integer tempoConsulta = 30;

    @Column(name = "REGISTRO_CONSELHO")
    private Integer registroConselho;

    private String excluido = Status.NAO;

    @Column(name = "DATA_EXCLUSAO")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataExclusao;

    @Column(name = "DESCONTO")
    private BigDecimal desconto;

    @Column(name = "ALTERADO_POR")
    private Long alteradoPor;

    @Column(name = "DATA_ULTIMA_ALTERACAO")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataUltimaAlteracao;

    @Column(name = "STATUS")
    private String status = Status.ATIVO;

    public static final String ATIVO = "A";

    public static final String INATIVO = "I";

    @ManyToOne
    @JoinColumn(name = "JUSTIFICATIVA")
    private Dominio justificativa;

    @Column(name = "EXCLUIDO_POR")
    private Long excluidoPorProfissional;

    @Column(name = "NOME_IMAGEM")
    private String nomeImagem;

    @Column(name = "ESTADO_CONSELHO")
    private String estadoConselho;

    @Column(name = "TIPO_REMUNERACAO")
    private String tipoRemuneracao = PROCEDIMENTO;

    @Column(name = "VALOR_REMUNERACAO")
    private BigDecimal valorRemuneracao;

    @Column(name = "DIA_REMUNERACAO")
    private Integer diaRemuneracao;

    @Column(name = "FAZ_ORCAMENTO")
    private boolean fazOrcamento;

    public static final String PORCENTAGEM = "POR";
    public static final String PROCEDIMENTO = "PRO";
    public static final String FIXO = "FIX";

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

    public Profissional() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public DadosBasico getDadosBasico() {
        return dadosBasico;
    }

    public void setDadosBasico(DadosBasico dadosBasico) {
        this.dadosBasico = dadosBasico;
    }

    public String getSenhaCertificado() {
        return senhaCertificado;
    }

    public void setSenhaCertificado(String senhaCertificado) {
        this.senhaCertificado = senhaCertificado;
    }

    public Long getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Long idUsuario) {
        this.idUsuario = idUsuario;
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
        Profissional other = (Profissional) obj;
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        return true;
    }

    public Long getIdEmpresa() {
        return idEmpresa;
    }

    public void setIdEmpresa(Long idEmpresa) {
        this.idEmpresa = idEmpresa;
    }

    public Integer getPercentualRemuneracao() {
        return percentualRemuneracao;
    }

    public void setPercentualRemuneracao(Integer percentualRemuneracao) {
        this.percentualRemuneracao = percentualRemuneracao;
    }

    public List<ProfissionalEspecialidade> getProfissionalEspecialidade() {
        return profissionalEspecialidade;
    }

    public void setProfissionalEspecialidade(List<ProfissionalEspecialidade> profissionalEspecialidade) {
        this.profissionalEspecialidade = profissionalEspecialidade;
    }

    public String getPerfil() {
        return perfil;
    }

    public void setPerfil(String perfil) {
        this.perfil = perfil;
    }

    public Integer getTempoConsulta() {
        return tempoConsulta;
    }

    public void setTempoConsulta(Integer tempoConsulta) {
        this.tempoConsulta = tempoConsulta;
    }

    public Integer getRegistroConselho() {
        return registroConselho;
    }

    public String getRegistroConselhoStr() {
        return registroConselho != null && registroConselho > 0 ? "CRO-" + registroConselho : "";
    }

    public void setRegistroConselho(Integer registroConselho) {
        this.registroConselho = registroConselho;
    }

    public byte[] getCertificado() {
        return certificado;
    }

    public void setCertificado(byte[] certificado) {
        this.certificado = certificado;
    }

    @Override
    public int compareTo(Profissional o) {
        if (dadosBasico == null || dadosBasico.getNome() == null || o == null || o.dadosBasico == null || o.dadosBasico.getNome() == null) {
            return 1;
        } else {
            return Normalizer.normalize(dadosBasico.getNome(), Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "").compareToIgnoreCase(
                    Normalizer.normalize(o.dadosBasico.getNome(), Normalizer.Form.NFD));
        }
    }

    public BigDecimal getDesconto() {
        return desconto;
    }

    public void setDesconto(BigDecimal desconto) {
        this.desconto = desconto;
    }

    public Date getDataUltimaAlteracao() {
        return dataUltimaAlteracao;
    }

    public void setDataUltimaAlteracao(Date dataUltimaAlteracao) {
        this.dataUltimaAlteracao = dataUltimaAlteracao;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Dominio getJustificativa() {
        return justificativa;
    }

    public void setJustificativa(Dominio justificativa) {
        this.justificativa = justificativa;
    }

    public List<ProfissionalFilial> getProfissionalFilials() {
        return profissionalFilials;
    }

    public void setProfissionalFilials(List<ProfissionalFilial> profissionalFilials) {
        this.profissionalFilials = profissionalFilials;
    }

    public Long getAlteradoPor() {
        return alteradoPor;
    }

    public void setAlteradoPor(Long alteradoPor) {
        this.alteradoPor = alteradoPor;
    }

    public Integer getRemuneracaoManutencao() {
        return remuneracaoManutencao;
    }

    public void setRemuneracaoManutencao(Integer remuneracaoManutencao) {
        this.remuneracaoManutencao = remuneracaoManutencao;
    }

    public String getNomeImagem() {
        return nomeImagem;
    }

    public void setNomeImagem(String nomeImagem) {
        this.nomeImagem = nomeImagem;
    }

    public StreamedContent getImagemUsuario() {
        return ProfissionalBO.getImagemUsuario(this);
    }

    public String getEstadoConselho() {
        return estadoConselho;
    }

    @Transient
    public String getRegistroEstadoConselho() {
        return registroConselho != null && estadoConselho != null ? registroConselho + " - " + estadoConselho : "";
    }

    public void setEstadoConselho(String estadoConselho) {
        this.estadoConselho = estadoConselho;
    }

    public String getTipoRemuneracao() {
        return tipoRemuneracao;
    }

    public void setTipoRemuneracao(String tipoRemuneracao) {
        this.tipoRemuneracao = tipoRemuneracao;
    }

    public BigDecimal getValorRemuneracao() {
        return valorRemuneracao;
    }

    public void setValorRemuneracao(BigDecimal valorRemuneracao) {
        this.valorRemuneracao = valorRemuneracao;
    }

    public Integer getDiaRemuneracao() {
        return diaRemuneracao;
    }

    public void setDiaRemuneracao(Integer diaRemuneracao) {
        this.diaRemuneracao = diaRemuneracao;
    }

    @Override
    public String toString() {
        if (dadosBasico != null && dadosBasico.getNome() != null) {
            return dadosBasico.getNome();
        } else {
            return super.toString();
        }
    }

    public List<ObjetoProfissional> getObjetosProfissional() {
        return objetosProfissional;
    }

    public void setObjetosProfissional(List<ObjetoProfissional> objetosProfissional) {
        this.objetosProfissional = objetosProfissional;
    }

    public boolean isFazOrcamento() {
        return fazOrcamento;
    }

    public void setFazOrcamento(boolean fazOrcamento) {
        this.fazOrcamento = fazOrcamento;
    }
}
