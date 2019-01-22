package br.com.lume.security.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.primefaces.model.StreamedContent;

import br.com.lume.common.util.Status;
import br.com.lume.security.bo.EmpresaBO;

/**
 * The persistent class for the SEG_EMPRESA database table.
 */
@Entity
@Table(name = "SEG_EMPRESA")
public class Empresa implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "EMP_INT_COD")
    private long empIntCod;

    @Column(name = "EMP_CHA_CEP")
    private String empChaCep;

    @Column(name = "EMP_CHA_CNPJ")
    private String empChaCnpj;

    @Column(name = "EMP_CHA_FAX")
    private String empChaFax;

    @Column(name = "EMP_CHA_FONE")
    private String empChaFone;

    @Column(name = "EMP_CHA_INSESTADUAL")
    private String empChaInsestadual;

    @Column(name = "EMP_CHA_INSMUNICIPAL")
    private String empChaInsmunicipal;

    @Column(name = "EMP_CHA_STS")
    private String empChaSts;

    @Column(name = "EMP_CHA_UF")
    private String empChaUf;

    @Column(name = "EMP_STR_BAIRRO")
    private String empStrBairro;

    @Column(name = "EMP_STR_CIDADE")
    private String empStrCidade;

    @Column(name = "EMP_STR_EMAIL")
    private String empStrEmail;

    @Column(name = "EMP_STR_EMAIL_PAG")
    private String empStrEmailPag;

    @Column(name = "EMP_STR_ENDERECO")
    private String empStrEndereco;

    @Column(name = "EMP_CHA_NUMENDERECO")
    private String empChaNumEndereco;

    @Column(name = "EMP_STR_NME")
    private String empStrNme;

    @Column(name = "EMP_STR_NMEFANTASIA")
    private String empStrNmefantasia;

    @Column(name = "EMP_STR_RESPONSAVEL")
    private String empStrResponsavel;

    @Column(name = "EMP_STR_URL")
    private String empStrUrl;

    @Column(name = "EMP_DTM_CRIACAO")
    @Temporal(TemporalType.DATE)
    private Date empDtmCriacao;

    @Column(name = "EMP_DTM_PAGAMENTO")
    @Temporal(TemporalType.DATE)
    private Date empDtmPagamento;

    @Column(name = "EMP_DTM_EXPIRACAO")
    @Temporal(TemporalType.DATE)
    private Date empDtmExpiracao;

    @Column(name = "EMP_STR_CLIENTEIUGUID")
    private String empStrClienteIuguID;

    @Column(name = "EMP_STR_ASSINATURAIUGUID")
    private String empStrAssinaturaIuguID;

    @Column(name = "EMP_CHA_TRIAL")
    private String empChaTrial;

    @Column(name = "ID_PLANO")
    private long idPlano;

    @Column(name = "EMP_CHA_TIPO")
    private String empChaTipo = "J";

    @Column(name = "EMP_CHA_CPF")
    private String empChaCpf;

    @Column(name = "EMP_CHA_RG")
    private String empChaRg;

    @Column(name = "EMP_CHA_CRO")
    private String empChaCro;

    @Column(name = "EMP_STR_ESTADO_CONSELHO")
    private String empStrEstadoConselho;

    @Column(name = "EMP_STR_COMPLEMENTO")
    private String empStrComplemento;

    @Column(name = "EMP_DTM_ACEITE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date empDtmAceite;

    @Column(name = "EMP_CHA_IP")
    private String empChaIp;

    @Column(name = "EMP_STR_ORGAO_EXPEDIDOR")
    private String empStrExpedidor;

    @Column(name = "EMP_CHA_TROCA_PLANO")
    private String empChaTrocaPlano;

    @Column(name = "EMP_STR_PAYERID")
    private String empStrPayerId;

    @Column(name = "EMP_INT_CADEIRA")
    private int empIntCadeira = 1;

    @Column(name = "EMP_INT_HORAS_UTEIS_SEMANAL")
    private int empIntHorasUteisSemanal = 40;

    @Column(name = "EMP_FLT_IMPOSTO")
    private double empFltImposto;

    @Column(name = "EMP_STR_LOGO")
    private String empStrLogo;

    @Column(name = "EMP_STR_ESTOQUE")
    private String empStrEstoque;

    @Column(name = "EMP_STR_TOKEN_CONTIFY")
    private String empStrTokenContify;

    @Column(name = "EMP_BOL_DENTISTAADMIN")
    private boolean empBolDentistaAdmin;

    @Column(name = "EMP_BOL_RETIRARMASCARATELEFONE")
    private boolean empBolRetirarMascaraTelefone;

    public static final String ESTOQUE_SIMPLIFICADO = "S";

    public static final String ESTOQUE_COMPLETO = "C";

    public static final long ID_EMPRESA_TEMPLATE = 92;

    public Empresa() {
    }

    public long getEmpIntCod() {
        return empIntCod;
    }

    public void setEmpIntCod(long empIntCod) {
        this.empIntCod = empIntCod;
    }

    public String getEmpChaCep() {
        return empChaCep;
    }

    public void setEmpChaCep(String empChaCep) {
        this.empChaCep = empChaCep;
    }

    public String getEmpChaCnpj() {
        return empChaCnpj;
    }

    public void setEmpChaCnpj(String empChaCnpj) {
        this.empChaCnpj = empChaCnpj;
    }

    public String getEmpChaFax() {
        return empChaFax;
    }

    public void setEmpChaFax(String empChaFax) {
        this.empChaFax = empChaFax;
    }

    public String getEmpChaFone() {
        return empChaFone;
    }

    public void setEmpChaFone(String empChaFone) {
        this.empChaFone = empChaFone;
    }

    public String getEmpChaInsestadual() {
        return empChaInsestadual;
    }

    public void setEmpChaInsestadual(String empChaInsestadual) {
        this.empChaInsestadual = empChaInsestadual;
    }

    public String getEmpChaInsmunicipal() {
        return empChaInsmunicipal;
    }

    public void setEmpChaInsmunicipal(String empChaInsmunicipal) {
        this.empChaInsmunicipal = empChaInsmunicipal;
    }

    public String getEmpChaSts() {
        return Status.INATIVO.equalsIgnoreCase(empChaSts) ? Boolean.TRUE.toString() : Boolean.FALSE.toString();
    }

    public void setEmpChaSts(String empChaSts) {
        if (Status.ATIVO.equalsIgnoreCase(empChaSts) || Status.INATIVO.equalsIgnoreCase(empChaSts)) {
            this.empChaSts = empChaSts;
        } else {
            this.empChaSts = Boolean.TRUE.toString().equalsIgnoreCase(empChaSts) ? Status.INATIVO : Status.ATIVO;
        }
    }

    public String getEmpChaUf() {
        return empChaUf;
    }

    public void setEmpChaUf(String empChaUf) {
        this.empChaUf = empChaUf;
    }

    public String getEmpStrBairro() {
        return empStrBairro;
    }

    public void setEmpStrBairro(String empStrBairro) {
        this.empStrBairro = empStrBairro;
    }

    public String getEmpStrCidade() {
        return empStrCidade;
    }

    public void setEmpStrCidade(String empStrCidade) {
        this.empStrCidade = empStrCidade;
    }

    public String getEmpStrEmail() {
        return empStrEmail;
    }

    public void setEmpStrEmail(String empStrEmail) {
        this.empStrEmail = empStrEmail;
    }

    public String getEmpStrEndereco() {
        return empStrEndereco;
    }

    @Transient
    public String getEnderecoCompleto() {
        return getEmpStrEndereco() + " " + getEmpChaNumEndereco() + " " + getEmpStrBairro() + " " + getEmpStrCidade() + " " + getEmpChaUf();
    }

    public void setEmpStrEndereco(String empStrEndereco) {
        this.empStrEndereco = empStrEndereco;
    }

    public String getEmpStrNme() {
        return empStrNme;
    }

    public void setEmpStrNme(String empStrNme) {
        this.empStrNme = empStrNme;
    }

    public String getEmpStrNmefantasia() {
        return empStrNmefantasia;
    }

    public void setEmpStrNmefantasia(String empStrNmefantasia) {
        this.empStrNmefantasia = empStrNmefantasia;
    }

    public String getEmpStrResponsavel() {
        return empStrResponsavel;
    }

    public void setEmpStrResponsavel(String empStrResponsavel) {
        this.empStrResponsavel = empStrResponsavel;
    }

    public String getEmpStrUrl() {
        return empStrUrl;
    }

    public void setEmpStrUrl(String empStrUrl) {
        this.empStrUrl = empStrUrl;
    }

    @Override
    public String toString() {
        return empStrNme;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (empIntCod ^ (empIntCod >>> 32));
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
        Empresa other = (Empresa) obj;
        if (empIntCod != other.empIntCod) {
            return false;
        }
        return true;
    }

    public Date getEmpDtmCriacao() {
        return empDtmCriacao;
    }

    public void setEmpDtmCriacao(Date empDtmCriacao) {
        this.empDtmCriacao = empDtmCriacao;
    }

    public String getEmpChaTrial() {
        return empChaTrial;
    }

    public void setEmpChaTrial(String empChaTrial) {
        this.empChaTrial = empChaTrial;
    }

    public String getEmpStrEmailPag() {
        return empStrEmailPag;
    }

    public void setEmpStrEmailPag(String empStrEmailPag) {
        this.empStrEmailPag = empStrEmailPag;
    }

    public Date getEmpDtmExpiracao() {
        return empDtmExpiracao;
    }

    public void setEmpDtmExpiracao(Date empDtmExpiracao) {
        this.empDtmExpiracao = empDtmExpiracao;
    }

    public Date getEmpDtmPagamento() {
        return empDtmPagamento;
    }

    public void setEmpDtmPagamento(Date empDtmPagamento) {
        this.empDtmPagamento = empDtmPagamento;
    }

    public long getIdPlano() {
        return idPlano;
    }

    public void setIdPlano(long idPlano) {
        this.idPlano = idPlano;
    }

    public String getEmpChaTipo() {
        return empChaTipo;
    }

    public void setEmpChaTipo(String empChaTipo) {
        this.empChaTipo = empChaTipo;
    }

    public String getEmpChaCpf() {
        return empChaCpf;
    }

    public void setEmpChaCpf(String empChaCpf) {
        this.empChaCpf = empChaCpf;
    }

    public String getEmpChaRg() {
        return empChaRg;
    }

    public void setEmpChaRg(String empChaRg) {
        this.empChaRg = empChaRg;
    }

    public String getEmpChaCro() {
        return empChaCro;
    }

    public void setEmpChaCro(String empChaCro) {
        this.empChaCro = empChaCro;
    }

    public String getEmpStrComplemento() {
        return empStrComplemento;
    }

    public void setEmpStrComplemento(String empStrComplemento) {
        this.empStrComplemento = empStrComplemento;
    }

    public Date getEmpDtmAceite() {
        return empDtmAceite;
    }

    public void setEmpDtmAceite(Date empDtmAceite) {
        this.empDtmAceite = empDtmAceite;
    }

    public String getEmpChaIp() {
        return empChaIp;
    }

    public void setEmpChaIp(String empChaIp) {
        this.empChaIp = empChaIp;
    }

    public String getEmpChaNumEndereco() {
        return empChaNumEndereco;
    }

    public void setEmpChaNumEndereco(String empChaNumEndereco) {
        this.empChaNumEndereco = empChaNumEndereco;
    }

    public String getEmpStrExpedidor() {
        return empStrExpedidor;
    }

    public void setEmpStrExpedidor(String empStrExpedidor) {
        this.empStrExpedidor = empStrExpedidor;
    }

    public String getEmpChaTrocaPlano() {
        return empChaTrocaPlano;
    }

    public void setEmpChaTrocaPlano(String empChaTrocaPlano) {
        this.empChaTrocaPlano = empChaTrocaPlano;
    }

    public String getEmpStrPayerId() {
        return empStrPayerId;
    }

    public void setEmpStrPayerId(String empStrPayerId) {
        this.empStrPayerId = empStrPayerId;
    }

    public int getEmpIntCadeira() {
        return empIntCadeira;
    }

    public void setEmpIntCadeira(int empIntCadeira) {
        this.empIntCadeira = empIntCadeira;
    }

    public double getEmpFltImposto() {
        return empFltImposto;
    }

    public void setEmpFltImposto(double empFltImposto) {
        this.empFltImposto = empFltImposto;
    }

    public String getEmpStrLogo() {
        return empStrLogo;
    }

    public void setEmpStrLogo(String empStrLogo) {
        this.empStrLogo = empStrLogo;
    }

    public StreamedContent getLogo() {
        return EmpresaBO.getLogo(this);
    }

    public String getEmpStrEstadoConselho() {
        return empStrEstadoConselho;
    }

    public void setEmpStrEstadoConselho(String empStrEstadoConselho) {
        this.empStrEstadoConselho = empStrEstadoConselho;
    }

    public int getEmpIntHorasUteisSemanal() {
        return empIntHorasUteisSemanal;
    }

    public void setEmpIntHorasUteisSemanal(int empIntHorasUteisSemanal) {
        this.empIntHorasUteisSemanal = empIntHorasUteisSemanal;
    }

    public int getCapacidadeInstalada() {
        return (empIntHorasUteisSemanal * 4) * empIntCadeira;
    }

    public String getEmpStrEstoque() {
        return empStrEstoque;
    }

    public void setEmpStrEstoque(String empStrEstoque) {
        this.empStrEstoque = empStrEstoque;
    }

    public boolean isEmpBolDentistaAdmin() {
        return empBolDentistaAdmin;
    }

    public void setEmpBolDentistaAdmin(boolean empBolDentistaAdmin) {
        this.empBolDentistaAdmin = empBolDentistaAdmin;
    }

    public String getEmpStrClienteIuguID() {
        return empStrClienteIuguID;
    }

    public void setEmpStrClienteIuguID(String empStrClienteIuguID) {
        this.empStrClienteIuguID = empStrClienteIuguID;
    }

    public String getEmpStrAssinaturaIuguID() {
        return empStrAssinaturaIuguID;
    }

    public void setEmpStrAssinaturaIuguID(String empStrAssinaturaIuguID) {
        this.empStrAssinaturaIuguID = empStrAssinaturaIuguID;
    }

    public boolean isEmpBolRetirarMascaraTelefone() {
        return empBolRetirarMascaraTelefone;
    }

    public void setEmpBolRetirarMascaraTelefone(boolean empBolRetirarMascaraTelefone) {
        this.empBolRetirarMascaraTelefone = empBolRetirarMascaraTelefone;
    }

    public String getEmpStrTokenContify() {
        return empStrTokenContify;
    }

    public void setEmpStrTokenContify(String empStrTokenContify) {
        this.empStrTokenContify = empStrTokenContify;
    }

}
