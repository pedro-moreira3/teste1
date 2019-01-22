package br.com.lume.odonto.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

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
import javax.persistence.Transient;

import br.com.lume.common.util.Status;

/**
 * The persistent class for the ANAMNESE database table.
 */
@Entity
@Table(name = "ITEM_ANAMNESE")
public class ItemAnamnese implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String resposta;

    @ManyToOne
    @JoinColumn(name = "ID_PERGUNTA")
    private Pergunta pergunta;

    @ManyToOne
    @JoinColumn(name = "ID_ANAMNESE")
    private Anamnese anamnese;

    @Transient
    private List<String> respostas;

    @Transient
    private String especialidade;

    private String excluido = Status.NAO;

    @Column(name = "DATA_EXCLUSAO")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataExclusao;
    @Column(name = "EXCLUIDO_POR")
    private Long excluidoPorProfissional;

    public Long getExcluidoPorProfissional() {
        return this.excluidoPorProfissional;
    }

    public void setExcluidoPorProfissional(Long excluidoPorProfissional) {
        this.excluidoPorProfissional = excluidoPorProfissional;
    }

    public String getExcluido() {
        return this.excluido;
    }

    public void setExcluido(String excluido) {
        this.excluido = excluido;
    }

    public Date getDataExclusao() {
        return this.dataExclusao;
    }

    public void setDataExclusao(Date dataExclusao) {
        this.dataExclusao = dataExclusao;
    }

    public ItemAnamnese() {
        this.respostas = new ArrayList<>();
    }

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getResposta() {
        return this.resposta;
    }

    public void setResposta(String resposta) {
        this.resposta = resposta;
    }

    public Pergunta getPergunta() {
        return this.pergunta;
    }

    public void setPergunta(Pergunta pergunta) {
        this.pergunta = pergunta;
    }

    public List<String> getRespostas() {
        if (this.respostas.size() == 0 && this.resposta != null && this.pergunta.getTipoResposta().equals(Pergunta.TIPO_RESPOSTA_VARIAS_EM_VARIAS)) {
            String[] split = this.resposta.split("\\|");
            this.respostas.addAll(Arrays.asList(split));
        }
        return this.respostas;
    }

    public void setRespostas(List<String> respostas) {
        this.resposta = "";
        for (String resposta_ : respostas) {
            this.resposta += resposta_ + "|";
        }
        this.respostas = respostas;
    }

    public Anamnese getAnamnese() {
        return this.anamnese;
    }

    public void setAnamnese(Anamnese anamnese) {
        this.anamnese = anamnese;
    }

    public String toXml() {
        StringBuilder builder = new StringBuilder();
        builder.append("<anamnese>");
        if (this.anamnese != null && this.anamnese.getPaciente() != null) {
            builder.append("<paciente>");
            builder.append("<id>" + this.anamnese.getPaciente().getId() + "</id>");
            builder.append("<nome>" + this.anamnese.getPaciente().getDadosBasico().getNome() + "</nome>");
            builder.append("</paciente>");
        }
        if (this.anamnese != null && this.anamnese.getProfissional() != null) {
            builder.append("<profissional>");
            builder.append("<id>" + this.anamnese.getProfissional().getId() + "</id>");
            builder.append("<nome>" + this.anamnese.getProfissional().getDadosBasico().getNome() + "</nome>");
            builder.append("</profissional>");
        }
        if (this.pergunta != null) {
            builder.append("<pergunta>");
            builder.append("<id>" + this.pergunta.getId() + "</id>");
            builder.append("<descricao>" + this.pergunta.getDescricao() + "</descricao>");
            builder.append("</pergunta>");
        }
        builder.append("<resposta>");
        builder.append("<descricao>" + this.resposta + "</descricao>");
        builder.append("</resposta>");
        builder.append("</anamnese>");
        return builder.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (this.id ^ (this.id >>> 32));
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
        ItemAnamnese other = (ItemAnamnese) obj;
        if (this.id != other.id) {
            return false;
        }
        return true;
    }

    public String getEspecialidade() {

        return this.especialidade;
    }

    public void setEspecialidade(String especialidade) {

        this.especialidade = especialidade;
    }
}
