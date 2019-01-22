package br.com.lume.odonto.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import br.com.lume.odonto.util.OdontoMensagens;

@Entity
@Table(name = "HELP_IMG")
public class HelpImg implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private long id;

    @Column(name = "NOME_ARQUIVO")
    private String nomeArquivo;

    @Column(name = "NOME_ORIGINAL")
    private String nomeOriginal;

    @ManyToOne
    @JoinColumn(name = "ID_HELP")
    private Help help;

    public HelpImg(
            String nomeArquivo,
            String nomeOriginal,
            Help help) {
        super();
        this.nomeArquivo = nomeArquivo;
        this.nomeOriginal = nomeOriginal;
        this.help = help;
    }

    public HelpImg() {
    }

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getNomeArquivo() {
        return this.nomeArquivo;
    }

    public void setNomeArquivo(String nomeArquivo) {
        this.nomeArquivo = nomeArquivo;
    }

    public String getNomeOriginal() {
        return this.nomeOriginal;
    }

    public void setNomeOriginal(String nomeOriginal) {
        this.nomeOriginal = nomeOriginal;
    }

    public Help getHelp() {
        return this.help;
    }

    public void setHelp(Help help) {
        this.help = help;
    }

    public String getImgHtml() {
        return "<img src=\"" + OdontoMensagens.getMensagem("template.path.imagens") + this.getNomeArquivo() + "\">";
    }
}
