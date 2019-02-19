package br.com.lume.odonto.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

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

@Entity
@Table(name = "MATERIAL_INDISPONIVEL")
public class ControleMaterial implements Serializable, Comparable<ControleMaterial> {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    @JoinColumn(name = "ID_RESERVA_KIT")
    private ReservaKit reservaKit;

    @Column(name = "ID_EMPRESA")
    private long idEmpresa;

    @ManyToOne
    @JoinColumn(name = "ID_MATERIAL")
    private Material material;

    @Column(name = "QUANTIDADE")
    private BigDecimal quantidade;

    @Column(name = "STATUS")
    private String status;

    @Transient
    private BigDecimal quantidadeDevolvida;

    private String excluido = Status.NAO;

    private Integer unidade;

    @Column(name = "DATA_EXCLUSAO")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataExclusao;

    @Column(name = "EXCLUIDO_POR")
    private Long excluidoPorProfissional;

    public static final String ENTREGUE = "EN";

    public static final String PENDENTE = "PE";

    public static final String FINALIZADO = "FI";

    public static final String UTILIZADO_KIT = "UK";

    public static final String UTILIZADO_UNITARIO = "UU";

    public static final String LAVAGEM_UNITARIO = "LU";

    public static final String LAVAGEM_KIT = "LK";

    public static final String DESCARTE = "DD";

    public static final String NAOUTILIZADO = "NU";

    public ControleMaterial() {

    }

    public ControleMaterial(long idEmpresa, Material material, BigDecimal quantidade) {
        super();
        this.idEmpresa = idEmpresa;
        this.material = material;
        this.quantidade = quantidade;
        unidade = 1;
    }

    public Long getExcluidoPorProfissional() {
        return excluidoPorProfissional;
    }

    public void setExcluidoPorProfissional(Long excluidoPorProfissional) {
        this.excluidoPorProfissional = excluidoPorProfissional;
    }

    public BigDecimal getQuantidadeDevolvida() {
        if (quantidadeDevolvida == null) {
            this.setQuantidadeDevolvida(new BigDecimal(0));
        }
        return quantidadeDevolvida;
    }

    public BigDecimal getQuantidadeDevolvidaStr() {
        if (quantidadeDevolvida == null) {
            this.setQuantidadeDevolvida(new BigDecimal(0));
        }
        return quantidadeDevolvida.setScale(2, BigDecimal.ROUND_DOWN);
    }

    public void setQuantidadeDevolvida(BigDecimal quantidadeDevolvida) {
        this.quantidadeDevolvida = quantidadeDevolvida;
    }

    public ReservaKit getReservaKit() {
        return reservaKit;
    }

    public void setReservaKit(ReservaKit reservaKit) {
        this.reservaKit = reservaKit;
    }

    public Material getMaterial() {
        return material;
    }

    public void setMaterial(Material material) {
        this.material = material;
    }

    public BigDecimal getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(BigDecimal quantidade) {
        this.quantidade = quantidade;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (id ^ (id >>> 32));
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
        ControleMaterial other = (ControleMaterial) obj;
        if (id != other.id) {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(ControleMaterial cm) {
        if (this.getId() > cm.getId()) {
            return 1;
        } else {
            return -1;
        }
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

    public Integer getUnidade() {
        return unidade;
    }

    public void setUnidade(Integer unidade) {
        this.unidade = unidade;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getIdEmpresa() {
        return idEmpresa;
    }

    public void setIdEmpresa(long idEmpresa) {
        this.idEmpresa = idEmpresa;
    }
}
