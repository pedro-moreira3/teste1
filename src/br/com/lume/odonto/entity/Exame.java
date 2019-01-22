package br.com.lume.odonto.entity;

import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.sql.Timestamp;
import java.text.Normalizer;
import java.util.Calendar;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

import br.com.lume.common.util.Status;
import br.com.lume.common.util.Utils;

@Entity
@Table(name = "EXAME")
public class Exame implements Serializable, Comparable<Exame> {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "DESCRICAO")
    private String descricao;

    @Lob
    private byte[] anexo;

    @Lob
    @Column(name = "ANEXO_ALTERADO")
    private byte[] anexoAlterado;

    @Column(name = "NOME_ANEXO")
    private String nomeAnexo;

    // bi-directional many-to-one association to Paciente
    @ManyToOne
    @JoinColumn(name = "ID_PACIENTE")
    private Paciente paciente;

    @Column(name = "DATA_HORA")
    private Timestamp dataHora = new Timestamp(Calendar.getInstance().getTimeInMillis());

    private String excluido = Status.NAO;

    @Transient
    private StreamedContent arquivoEditado;

    @Column(name = "DATA_EXCLUSAO")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataExclusao;

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

    public Exame() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Paciente getPaciente() {
        return paciente;
    }

    public void setPaciente(Paciente paciente) {
        this.paciente = paciente;
    }

    public Timestamp getDataHora() {
        return dataHora;
    }

    public void setDataHora(Timestamp dataHora) {
        this.dataHora = dataHora;
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
        Exame other = (Exame) obj;
        if (id != other.id) {
            return false;
        }
        return true;
    }

    @Transient
    public String getDataHoraStr() {
        return Utils.dateToString(dataHora);
    }

    @Transient
    public String getDataHoraStrOrd() {
        return Utils.dateToString(dataHora, "yyyy/MM/dd HH:mm:ss");
    }

    public byte[] getAnexo() {
        return anexo;
    }

    public void setAnexo(byte[] anexo) {
        this.anexo = anexo;
    }

    public byte[] getAnexoAlterado() {
        return anexoAlterado;
    }

    public void setAnexoAlterado(byte[] anexoAlterado) {
        this.anexoAlterado = anexoAlterado;
    }

    public String getNomeAnexo() {
        return nomeAnexo;
    }

    public void setNomeAnexo(String nomeAnexo) {
        this.nomeAnexo = nomeAnexo;
    }

    @Override
    public int compareTo(Exame o) {
        if (descricao == null || o == null || o.getDescricao() == null) {
            return 1;
        } else {
            return Normalizer.normalize(descricao, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "").compareToIgnoreCase(Normalizer.normalize(o.descricao, Normalizer.Form.NFD));
        }
    }

    public void setArquivoEditado(StreamedContent arquivoEditado) {
        this.arquivoEditado = arquivoEditado;
    }

    @Transient
    public StreamedContent getArquivo() {
        StreamedContent arquivo = null;
        if (this.getAnexo() != null) {
            try {
                ByteArrayInputStream bis = new ByteArrayInputStream(this.getAnexo());
                arquivo = new DefaultStreamedContent(bis, null, this.getNomeAnexo());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return arquivo;
    }

    public StreamedContent getArquivoEditado() {
        StreamedContent arquivo = null;
        if (this.getAnexoAlterado() != null) {
            try {
                ByteArrayInputStream bis = new ByteArrayInputStream(this.getAnexoAlterado());
                arquivo = new DefaultStreamedContent(bis, null, this.getNomeAnexo());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return arquivo;
    }
}
