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
@Table(name = "LAVAGEM_KIT")
public class LavagemKit implements Serializable {

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
    @JoinColumn(name = "ID_LAVAGEM")
    private Lavagem lavagem;

    @Column(name = "QUANTIDADE")
    private Long quantidade;

    private String excluido = Status.NAO;

    @Column(name = "DATA_EXCLUSAO")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataExclusao;

    @Column(name = "EXCLUIDO_POR")
    private Long excluidoPorProfissional;

    public LavagemKit() {
    }

    public LavagemKit(Item item, Lavagem lavagem, Long quantidade, ControleMaterial controleMaterial) {
        super();
        this.item = item;
        this.lavagem = lavagem;
        this.quantidade = quantidade;
        this.controleMaterial = controleMaterial;
    }

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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Long quantidade) {
        this.quantidade = quantidade;
    }

    public Kit getKit() {
        return kit;
    }

    public void setKit(Kit kit) {
        this.kit = kit;
    }

    public Lavagem getLavagem() {
        return lavagem;
    }

    public void setLavagem(Lavagem lavagem) {
        this.lavagem = lavagem;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public ControleMaterial getControleMaterial() {
        return controleMaterial;
    }

    public void setControleMaterial(ControleMaterial controleMaterial) {
        this.controleMaterial = controleMaterial;
    }

    public ReservaKit getReservaKit() {
        return reservaKit;
    }

    public void setReservaKit(ReservaKit reservaKit) {
        this.reservaKit = reservaKit;
    }

    public Abastecimento getAbastecimento() {
        return abastecimento;
    }

    public void setAbastecimento(Abastecimento abastecimento) {
        this.abastecimento = abastecimento;
    }
}
