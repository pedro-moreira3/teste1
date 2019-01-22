package br.com.lume.odonto.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Calendar;
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

import br.com.lume.common.util.Utils;

@Entity
@Table(name = "MATERIAL_LOG")
public class MaterialLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    @JoinColumn(name = "id_material_indisponivel")
    private ControleMaterial controleMaterial;

    @ManyToOne
    @JoinColumn(name = "id_abastecimento")
    private Abastecimento abastecimento;

    @ManyToOne
    @JoinColumn(name = "id_material")
    private Material material;

    @ManyToOne
    @JoinColumn(name = "id_profissional")
    private Profissional profissional;

    @Column(name = "quantidade_alterada")
    private BigDecimal quantidadeAlterada;

    @Column(name = "quantidade_atual")
    private BigDecimal quantidadeAtual;

    private String acao;

    @Temporal(TemporalType.TIMESTAMP)
    private Date data;

    public static final String EMPRESTIMO_KIT_DISPONIBILIZAR = "EMPRÉSTIMO KIT - DISPONIBILIZAR";

    public static final String EMPRESTIMO_KIT_CANCELAR = "EMPRÉSTIMO KIT - CANCELAR";

    public static final String DEVOLUCAO_KIT_FINALIZAR = "DEVOLUÇÃO KIT - FINALIZAR";

    public static final String DEVOLUCAO_KIT_LAVAGEM = "DEVOLUÇÃO KIT - LAVAGEM";

    public static final String DEVOLUCAO_LAVAGEM_FINALIZAR = "DEVOLUÇÃO LAVAGEM - FINALIZAR";

    public static final String DEVOLUCAO_LAVAGEM_DESCARTAR = "DEVOLUÇÃO LAVAGEM - DESCARTAR";

    public static final String DEVOLUCAO_ESTERILIZACAO_DESCARTAR = "DEVOLUÇÃO ESTERILIZAÇÃO - DESCARTAR";

    public static final String DEVOLUCAO_ESTERILIZACAO_ESTERILIZADO = "DEVOLUÇÃO LAVAGEM - ESTERILIZADO";

    public static final String DEVOLUCAO_KIT_NAO_UTILIZADO = "DEVOLUÇÃO KIT - NÃO UTILIZADO";

    public static final String EMPRESTIMO_UNITARIO = "EMPRÉSTIMO UNITÁRIO";

    public static final String DEVOLUCAO_UNITARIA_DEVOLUCAO = "DEVOLUÇÃO UNITÁRIA - DEVOLUÇÃO";

    public static final String DEVOLUCAO_UNITARIA_LAVAGEM = "DEVOLUÇÃO UNITÁRIA - LAVAGEM";

    public static final String MOVIMENTACAO_MATERIAL_MOVIMENTAR = "MOVIMENTAÇÃO DE MATERIAL - MOVIMENTAR";

    public static final String AJUSTE_MATERIAL = "AJUSTE DE MATERIAL - QTD ADICIONADA/REMOVIDA";

    public static final String ENTRADA_MATERIAL_CADASTRO = "ENTRADE DE MATERIAL - CADASTRO";

    public static final String ENTRADA_MATERIAL_DEVOLVER = "ENTRADE DE MATERIAL - DEVOLVER";

    public static final String ENTRADA_MATERIAL_DEVOLVER_MOVIMENTACAO_SIMPLIFICADA = "ENTRADE DE MATERIAL - MOVIMENTAÇÃO SIMPLIFICADA";

    public static final String RESERVA_EXCLUIDA = "RESERVA - EXCLUIDA";

    public static final String RESERVA_KIT_EXCLUIDA = "RESERVA - RESERVA KIT - EXCLUIDA";

    public static final String ENTREGA_LAVAGEM_MANUAL = "ENTREGA P/ LAVAGEM MANUAL";

    public MaterialLog(ControleMaterial controleMaterial, Abastecimento abastecimento, Material material, Profissional profissional, BigDecimal quantidadeAlterada, BigDecimal quantidadeAtual,
            String acao) {
        super();
        this.controleMaterial = controleMaterial;
        this.abastecimento = abastecimento;
        this.material = material;
        this.profissional = profissional;
        this.quantidadeAlterada = quantidadeAlterada;
        this.quantidadeAtual = quantidadeAtual;
        this.acao = acao;
        data = Calendar.getInstance().getTime();
    }

    public MaterialLog() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public ControleMaterial getControleMaterial() {
        return controleMaterial;
    }

    public void setControleMaterial(ControleMaterial controleMaterial) {
        this.controleMaterial = controleMaterial;
    }

    public Abastecimento getAbastecimento() {
        return abastecimento;
    }

    public void setAbastecimento(Abastecimento abastecimento) {
        this.abastecimento = abastecimento;
    }

    public Material getMaterial() {
        return material;
    }

    public void setMaterial(Material material) {
        this.material = material;
    }

    public Profissional getProfissional() {
        return profissional;
    }

    public void setProfissional(Profissional profissional) {
        this.profissional = profissional;
    }

    public BigDecimal getQuantidadeAlterada() {
        return quantidadeAlterada;
    }

    public void setQuantidadeAlterada(BigDecimal quantidadeAlterada) {
        this.quantidadeAlterada = quantidadeAlterada;
    }

    public BigDecimal getQuantidadeAtual() {
        return quantidadeAtual;
    }

    public void setQuantidadeAtual(BigDecimal quantidadeAtual) {
        this.quantidadeAtual = quantidadeAtual;
    }

    public String getAcao() {
        return acao;
    }

    public void setAcao(String acao) {
        this.acao = acao;
    }

    public Date getData() {
        return data;
    }

    public String getDataStr() {
        return Utils.dateToString(data, "dd/MM/yyyy HH:mm:ss");
    }

    public void setData(Date data) {
        this.data = data;
    }
}
