package br.com.lume.odonto.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
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

import br.com.lume.common.util.Status;
import br.com.lume.common.util.Utils;

/**
 * The persistent class for the PACIENTE_ANAMNESE database table.
 */
@Entity
@Table(name = "ANAMNESE")
public class Anamnese implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Lob()
    @Column(name = "XML_ASSINADO")
    private String xmlAssinado;

    // bi-directional many-to-one association to Anamnese
    @OneToMany(mappedBy = "anamnese", cascade = CascadeType.ALL)
    private List<ItemAnamnese> itensAnamnese;

    // bi-directional many-to-one association to Paciente
    @ManyToOne
    @JoinColumn(name = "ID_PACIENTE")
    private Paciente paciente;

    @ManyToOne
    @JoinColumn(name = "ID_PROFISSIONAL")
    private Profissional profissional;

    @Column(name = "DATA_HORA")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataHora;

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

    public Anamnese() {
    }

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getXmlAssinado() {
        return this.xmlAssinado;
    }

    public void setXmlAssinado(String xmlAssinado) {
        this.xmlAssinado = xmlAssinado;
    }

    public List<ItemAnamnese> getItensAnamnese() {
        return this.itensAnamnese;
    }

    public void setItensAnamnese(List<ItemAnamnese> itensAnamnese) {
        this.itensAnamnese = itensAnamnese;
    }

    public Paciente getPaciente() {
        return this.paciente;
    }

    public void setPaciente(Paciente paciente) {
        this.paciente = paciente;
    }

    public Profissional getProfissional() {
        return this.profissional;
    }

    public void setProfissional(Profissional profissional) {
        this.profissional = profissional;
    }

    public Date getDataHora() {
        return this.dataHora;
    }

    public void setDataHora(Date dataHora) {
        this.dataHora = dataHora;
    }

    @Transient
    public String getDataHoraStr() {
        return Utils.dateToString(this.dataHora);
    }

    @Transient
    public String getDataHoraStrOrd() {
        return Utils.dateToString(this.dataHora, "yyyy/MM/dd");
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

    @Transient
    public String getEspecialidades() {
        String especialidades = "";
        if (this.itensAnamnese != null && !this.itensAnamnese.isEmpty()) {
            HashSet<String> hsEspecialidades = new HashSet<>();
            for (ItemAnamnese ia : this.itensAnamnese) {
                if (ia.getPergunta() != null && ia.getPergunta().getEspecialidade() != null) {
                    hsEspecialidades.add(ia.getPergunta().getEspecialidade().getDescricao());
                } else {
                    hsEspecialidades.add("GENÉRICAS");
                }
            }
            especialidades = hsEspecialidades.toString();
            especialidades = especialidades.replaceAll("\\[", "");
            especialidades = especialidades.replaceAll("\\]", "");
        }
        return especialidades;
    }
}
