package br.com.lume.odonto.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import br.com.lume.common.util.Status;

@Entity
@Table(name = "ESTERILIZACAO_KIT")
public class EsterilizacaoKit implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "ID_KIT")
    private Kit kit;

    @ManyToOne
    @JoinColumn(name = "ID_ITEM")
    private Item item;

    @OneToOne
    @JoinColumn(name = "ID_MATERIAL_INDISPONIVEL")
    private ControleMaterial controleMaterial;

    @OneToOne
    @JoinColumn(name = "ID_ABASTECIMENTO")
    private Abastecimento abastecimento;

    @OneToOne
    @JoinColumn(name = "ID_RESERVA_KIT")
    private ReservaKit reservaKit;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "ID_ESTERILIZACAO")
    private Esterilizacao esterilizacao;

    @Column(name = "QUANTIDADE")
    private Long quantidade;

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

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Esterilizacao getEsterilizacao() {
        return this.esterilizacao;
    }

    public void setEsterilizacao(Esterilizacao esterilizacao) {
        this.esterilizacao = esterilizacao;
    }

    public Long getQuantidade() {
        return this.quantidade;
    }

    public void setQuantidade(Long quantidade) {
        this.quantidade = quantidade;
    }

    public Kit getKit() {
        return this.kit;
    }

    public void setKit(Kit kit) {
        this.kit = kit;
    }

    public Item getItem() {
        return this.item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public ControleMaterial getControleMaterial() {
        return this.controleMaterial;
    }

    public void setControleMaterial(ControleMaterial controleMaterial) {
        this.controleMaterial = controleMaterial;
    }

    public Abastecimento getAbastecimento() {
        return this.abastecimento;
    }

    public void setAbastecimento(Abastecimento abastecimento) {
        this.abastecimento = abastecimento;
    }

    public ReservaKit getReservaKit() {
        return this.reservaKit;
    }

    public void setReservaKit(ReservaKit reservaKit) {
        this.reservaKit = reservaKit;
    }
}
