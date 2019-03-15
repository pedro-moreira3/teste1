package br.com.lume.odonto.entity;

import java.io.Serializable;
import java.text.Normalizer;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.primefaces.model.StreamedContent;

import br.com.lume.common.util.Status;
import br.com.lume.common.util.Utils;
import br.com.lume.odonto.bo.AgendamentoBO;
import br.com.lume.odonto.bo.PacienteBO;
import br.com.lume.odonto.bo.ReservaBO;

@Entity
@Table(name = "PACIENTE")
public class Paciente implements Serializable, Comparable<Paciente> {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // bi-directional many-to-one association to DadosBasico
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "ID_DADOS_BASICOS")
    private DadosBasico dadosBasico = new DadosBasico();

    @ManyToOne
    @JoinColumn(name = "ID_CONVENIO")
    private Convenio convenio;

    @Column(name = "ID_USUARIO")
    private Long idUsuario;

    @Column(name = "ID_EMPRESA")
    private Long idEmpresa;

    @Column(name = "PRE_CADASTRO")
    private String preCadastro;

    private String excluido = Status.NAO;

    @Column(name = "DATA_EXCLUSAO")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataExclusao;

    @ManyToOne
    @JoinColumn(name = "TITULAR")
    private Paciente titular;

    @Column(name = "ALTERADO_POR")
    private long alteradoPor;

    @Column(name = "DATA_ULTIMA_ALTERACAO")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataUltimaAlteracao;

    @Column(name = "DATA_CRIACAO")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataCriacao;

    @Column(name = "STATUS")
    private String status = Status.ATIVO;

    @ManyToOne
    @JoinColumn(name = "JUSTIFICATIVA")
    private Dominio justificativa;

    @Column(name = "NOME_MAE")
    private String nomeMae;

    @ManyToOne
    @JoinColumn(name = "PLANO")
    private Dominio plano;

    @Column(name = "EXCLUIDO_POR")
    private Long excluidoPorProfissional;

    private String indicacao;

    @Column(name = "NUMERO_PLANO")
    private String numeroPlano;

    private boolean legado;

    @Column(name = "NOME_IMAGEM")
    private String nomeImagem;

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

    public Paciente() {
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
        Paciente other = (Paciente) obj;
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

    public Convenio getConvenio() {
        return convenio;
    }

    public void setConvenio(Convenio convenio) {
        this.convenio = convenio;
    }

    public String getPreCadastro() {
        return preCadastro;
    }

    public void setPreCadastro(String preCadastro) {
        this.preCadastro = preCadastro;
    }

    @Override
    public int compareTo(Paciente o) {
        if (dadosBasico == null || dadosBasico.getNome() == null || o == null || o.getDadosBasico() == null || o.getDadosBasico().getNome() == null) {
            return 1;
        } else {
            return Normalizer.normalize(dadosBasico.getNome(), Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "").toLowerCase().compareToIgnoreCase(
                    Normalizer.normalize(o.dadosBasico.getNome(), Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "").toLowerCase());
        }
    }

    public long getAlteradoPor() {
        return alteradoPor;
    }

    public void setAlteradoPor(long alteradoPor) {
        this.alteradoPor = alteradoPor;
    }

    public Paciente getTitular() {
        return titular;
    }

    public void setTitular(Paciente titular) {
        this.titular = titular;
    }

    public Date getDataUltimaAlteracao() {
        return dataUltimaAlteracao;
    }

    public String getDataUltimaAlteracaosStr() {
        return Utils.dateToString(dataUltimaAlteracao, "dd/MM/yyyy");
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

    public String getNomeMae() {
        return nomeMae;
    }

    public void setNomeMae(String nomeMae) {
        this.nomeMae = nomeMae;
    }

    public Dominio getPlano() {
        return plano;
    }

    public void setPlano(Dominio plano) {
        this.plano = plano;
    }

    public Dominio getJustificativa() {
        return justificativa;
    }

    public void setJustificativa(Dominio justificativa) {
        this.justificativa = justificativa;
    }

    public Date getDataCriacao() {
        return dataCriacao;
    }

    public String getDataCriacaoStr() {
        return Utils.dateToString(dataCriacao, "dd/MM/yyyy");
    }

    public void setDataCriacao(Date dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public boolean isLegado() {
        return legado;
    }

    public void setLegado(boolean legado) {
        this.legado = legado;
    }

    public String getIndicacao() {
        return indicacao;
    }

    public void setIndicacao(String indicacao) {
        this.indicacao = indicacao;
    }

    public String getNomeImagem() {
        return nomeImagem;
    }

    public void setNomeImagem(String nomeImagem) {
        this.nomeImagem = nomeImagem;
    }

    public StreamedContent getImagemUsuario() {
        return PacienteBO.getImagemUsuario(this);
    }

    public String getSiglaConvenio() {
        return convenio != null ? "C" : "P";
    }

    public String getNumeroPlano() {
        return numeroPlano;
    }

    public void setNumeroPlano(String numeroPlano) {
        this.numeroPlano = numeroPlano;
    }

    public String getWhatsURL() {
        if (getDadosBasico() != null && getDadosBasico().getCelular() != null && !getDadosBasico().getCelular().equals("")) {
            return "https://api.whatsapp.com/send?phone=55" + getDadosBasico().getCelularSemMascara() + "&text=";
        } else {
            return null;
        }
    }

    public String getStatusUltimoAgendamento() {
        try {
            return StatusAgendamento.findBySigla(nomeMae).getDescricao();
        } catch (Exception e) {}
        return "";
    }
    
    public String getStatusProximoAgendamento() {
        try {
            return new AgendamentoBO().findStatusProximoAgendamentoPaciente(this);
        } catch (Exception e) {}
        return "";
    }
    
    public String getDataProximoAgendamentoStr() {
        try {
            return new AgendamentoBO().findDataProximoAgendamentoPaciente(this);
        } catch (Exception e) {}
        return "";
    }

}
