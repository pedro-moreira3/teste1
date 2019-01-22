package br.com.lume.odonto.entity;

import java.io.Serializable;
import java.text.Normalizer;
import java.util.Date;
import java.util.List;

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

import br.com.lume.common.util.Status;

/**
 * The persistent class for the PERGUNTA database table.
 */
@Entity
@Table(name = "PERGUNTA")
public class Pergunta implements Serializable, Comparable<Pergunta> {

    private static final long serialVersionUID = 1L;

    public static final String TIPO_RESPOSTA_UMA_EM_VARIAS = "UMA_EM_VARIAS";

    public static final String TIPO_RESPOSTA_VARIAS_EM_VARIAS = "VARIAS_EM_VARIAS";

    public static final String TIPO_RESPOSTA_SIM_OU_NAO = "SIM_OU_NAO";

    public static final String TIPO_RESPOSTA_QUANTIDADE = "QUANTIDADE";

    public static final String TIPO_RESPOSTA_TEXTO = "TEXTO";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String descricao;

    @Column(name = "TIPO_RESPOSTA")
    private String tipoResposta;

    @OneToMany(mappedBy = "pergunta")
    private List<ItemAnamnese> anamneses;

    @OneToMany(mappedBy = "pergunta", fetch = FetchType.EAGER)
    private List<Resposta> respostas;

    @ManyToOne
    @JoinColumn(name = "ID_ESPECIALIDADE")
    private Especialidade especialidade;

    @Column(name = "ID_EMPRESA")
    private Long idEmpresa;

    @Column(name = "PRE_CADASTRO")
    private String preCadastro;

    private int ordem;

    private String excluido = Status.NAO;

    @Column(name = "DATA_EXCLUSAO")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataExclusao;

    private String requerida;

    @Column(name = "EXCLUIDO_POR")
    private Long excluidoPorProfissional;

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

    public Pergunta() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getTipoResposta() {
        return tipoResposta;
    }

    public void setTipoResposta(String tipoResposta) {
        this.tipoResposta = tipoResposta;
    }

    public List<ItemAnamnese> getAnamneses() {
        return anamneses;
    }

    public void setAnamneses(List<ItemAnamnese> anamneses) {
        this.anamneses = anamneses;
    }

    public List<Resposta> getRespostas() {
        return respostas;
    }

    public void setRespostas(List<Resposta> respostas) {
        this.respostas = respostas;
    }

    public int getOrdem() {
        return ordem;
    }

    public void setOrdem(int ordem) {
        this.ordem = ordem;
    }

    public Especialidade getEspecialidade() {
        return especialidade;
    }

    public void setEspecialidade(Especialidade especialidade) {
        this.especialidade = especialidade;
    }

    public String getTipoRespostaLabel() {
        if (tipoResposta.equals(TIPO_RESPOSTA_UMA_EM_VARIAS)) {
            return "Uma em Várias";
        } else if (tipoResposta.equals(TIPO_RESPOSTA_VARIAS_EM_VARIAS)) {
            return "Múltiplas";
        } else if (tipoResposta.equals(TIPO_RESPOSTA_SIM_OU_NAO)) {
            return "Sim ou Não";
        } else if (tipoResposta.equals(TIPO_RESPOSTA_QUANTIDADE)) {
            return "Quantidade";
        } else if (tipoResposta.equals(TIPO_RESPOSTA_TEXTO)) {
            return "Texto Livre";
        }
        return "";
    }

    public String getTipoRespostaUmaEmVarias() {
        return TIPO_RESPOSTA_UMA_EM_VARIAS;
    }

    public String getTipoRespostaVariasEmVarias() {
        return TIPO_RESPOSTA_VARIAS_EM_VARIAS;
    }

    public String getTipoRespostaSimOuNao() {
        return TIPO_RESPOSTA_SIM_OU_NAO;
    }

    public String getTipoRespostaQuantidade() {
        return TIPO_RESPOSTA_QUANTIDADE;
    }

    public String getTipoRespostaTexto() {
        return TIPO_RESPOSTA_TEXTO;
    }

    public Long getIdEmpresa() {
        return idEmpresa;
    }

    public void setIdEmpresa(Long idEmpresa) {
        this.idEmpresa = idEmpresa;
    }

    public String getPreCadastro() {
        return preCadastro;
    }

    public void setPreCadastro(String preCadastro) {
        this.preCadastro = preCadastro;
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
        Pergunta other = (Pergunta) obj;
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(Pergunta o) {
        if (descricao == null || o == null || o.getDescricao() == null) {
            return 1;
        } else {
            return Normalizer.normalize(descricao, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "").compareToIgnoreCase(Normalizer.normalize(o.descricao, Normalizer.Form.NFD));
        }
    }

    public String getRequerida() {
        return requerida;
    }

    public void setRequerida(String requerida) {
        this.requerida = requerida;
    }
}
