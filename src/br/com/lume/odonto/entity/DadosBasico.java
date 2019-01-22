package br.com.lume.odonto.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.Normalizer;
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
import br.com.lume.common.util.Utils;

@Entity
@Table(name = "DADOS_BASICOS")
public class DadosBasico implements Serializable, Comparable<DadosBasico> {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String bairro;

    private String celular;

    private String cep;

    private String cidade;

    private String documento;

    private String cpfresponsavel;

    @Temporal(TemporalType.DATE)
    @Column(name = "DATA_NASCIMENTO")
    private Date dataNascimento;

    private String email;

    private String endereco;

    private String nome;

    private Integer numero;

    private String rg;

    private String sexo;

    private String telefone;

    @Column(name = "TELEFONE_RESPONSAVEL")
    private String telefoneResponsavel;

    private String uf;

    private String complemento;

    private String prefixo;

    private String excluido = Status.NAO;

    private String responsavel;

    @Column(name = "DATA_EXCLUSAO")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataExclusao;

    @ManyToOne
    @JoinColumn(name = "INDICACAO")
    private Paciente indicacao;

    private Integer banco;

    private Integer agencia;

    private BigDecimal conta;

    @Column(name = "EXCLUIDO_POR")
    private Long excluidoPorProfissional;

    @Column(name = "NOME_PREFERENCIAL")
    private String nomePreferencial;

    @Transient
    private String tipoInformacao;

    private String particularidades;

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

    public DadosBasico() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBairro() {
        return bairro;
    }

    public void setBairro(String bairro) {
        this.bairro = bairro;
    }

    public String getCelular() {
        return celular;
    }

    public void setCelular(String celular) {
        this.celular = celular;
    }

    public String getCep() {
        return cep;
    }

    public void setCep(String cep) {
        this.cep = cep;
    }

    public String getCidade() {
        return cidade;
    }

    public void setCidade(String cidade) {
        this.cidade = cidade;
    }

    public Date getDataNascimento() {
        return dataNascimento;
    }

    // o metodo generico Utils.dateToString(dataNascimento); pega dd/MM/yyyy HH:MM:SS p/ nascimento é necessario somente "dd/MM/yyy"
    @Transient
    public String getDataNascimentoStr() {
        return Utils.dateToString(dataNascimento, "dd/MM/yyyy");
    }

    @Transient
    public String getDataNascimentoStrOrd() {
        return Utils.dateToString(dataNascimento, "yyyy/MM/dd");
    }

    public void setDataNascimento(Date dataNascimento) {
        this.dataNascimento = dataNascimento;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

    public String getNome() {
        return nome;
    }

    public String getNomeStr() {
        return Normalizer.normalize(nome, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "");
    }

    public String getNomeLimpo() {
        return Normalizer.normalize(this.getNome(), Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "");
    }

    @Transient
    public String getPrefixoNome() {
        return prefixo != null ? prefixo + " " + nome : nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Integer getNumero() {
        return numero;
    }

    public void setNumero(Integer numero) {
        this.numero = numero;
    }

    public String getRg() {
        return rg;
    }

    public void setRg(String rg) {
        this.rg = rg;
    }

    public String getSexo() {
        return sexo;
    }

    public void setSexo(String sexo) {
        this.sexo = sexo;
    }

    public String getTelefone() {
        return telefone;
    }

    public String getTelefoneStr() {
        String espaco = telefone != null && !telefone.equals("") && celular != null && !celular.equals("") ? " - " : "";
        return telefone + espaco + celular;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getUf() {
        return uf;
    }

    public void setUf(String uf) {
        this.uf = uf;
    }

    public String getComplemento() {
        return complemento;
    }

    public void setComplemento(String complemento) {
        this.complemento = complemento;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getDocumento() {
        return documento;
    }

    public String getDocumentoStr() {
        if (documento != null) {
            return documento.replaceAll("[^0-9]", "");
        } else {
            return documento;
        }
    }

    public void setDocumento(String documento) {
        this.documento = documento;
    }

    public String getPrefixo() {
        return prefixo;
    }

    public void setPrefixo(String prefixo) {
        this.prefixo = prefixo;
    }

    public String getNomeDocumento() {
        return documento != null ? nome + " - " + documento : nome + " - " + "";
    }

    public String getTipoInformacaoNome() {
        return nome + " [" + tipoInformacao + "]";
    }

    @Override
    public int compareTo(DadosBasico o) {
        if (nome == null || o == null || o.getNome() == null) {
            return 1;
        } else {
            return Normalizer.normalize(this.getNome(), Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "").compareToIgnoreCase(Normalizer.normalize(o.getNome(), Normalizer.Form.NFD));
        }
    }

    public String getResponsavel() {
        return responsavel;
    }

    public void setResponsavel(String responsavel) {
        this.responsavel = responsavel;
    }

    public Integer getBanco() {
        return banco;
    }

    public void setBanco(Integer banco) {
        this.banco = banco;
    }

    public Integer getAgencia() {
        return agencia;
    }

    public void setAgencia(Integer agencia) {
        this.agencia = agencia;
    }

    public BigDecimal getConta() {
        return conta;
    }

    public void setConta(BigDecimal conta) {
        this.conta = conta;
    }

    public String getNomePreferencial() {
        return nomePreferencial;
    }

    public void setNomePreferencial(String nomePreferencial) {
        this.nomePreferencial = nomePreferencial;
    }

    public Paciente getIndicacao() {
        return indicacao;
    }

    public void setIndicacao(Paciente indicacao) {
        this.indicacao = indicacao;
    }

    public String getTipoInformacao() {
        return tipoInformacao;
    }

    public void setTipoInformacao(String tipoInformacao) {
        this.tipoInformacao = tipoInformacao;
    }

    public String getCpfresponsavel() {
        return cpfresponsavel;
    }

    public void setCpfresponsavel(String cpfresponsavel) {
        this.cpfresponsavel = cpfresponsavel;
    }

    public String getNomeAbreviado() {
        if (nome != null && !nome.equals("") && nome.contains(" ")) {
            String[] nomes = nome.split(" ");
            return nomes[0] + " " + nomes[1].substring(0, 1).toUpperCase() + ".";
        }
        return nome;
    }

    public String getCelularSemMascara() {
        if (celular != null && !celular.equals("")) {
            celular = celular.replaceAll("\\(", "");
            celular = celular.replaceAll("\\)", "");
            celular = celular.replaceAll("-", "");
            celular = celular.replaceAll("\\.", "");
            celular = celular.replaceAll(" ", "");
            return celular;
        }
        return celular;
    }

    public String getTelefoneResponsavel() {
        return telefoneResponsavel;
    }

    public void setTelefoneResponsavel(String telefoneResponsavel) {
        this.telefoneResponsavel = telefoneResponsavel;
    }

    public String getParticularidades() {
        return particularidades;
    }

    public void setParticularidades(String particularidades) {
        this.particularidades = particularidades;
    }

}
