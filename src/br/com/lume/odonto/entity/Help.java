package br.com.lume.odonto.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "HELP")
public class Help implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String tela;

    private String label;

    @Lob
    private String conteudo;

    @OneToMany(mappedBy = "help", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<HelpImg> imagens;

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTela() {
        return this.tela;
    }

    public void setTela(String tela) {
        this.tela = tela;
    }

    public String getConteudo() {
        return this.conteudo;
    }

    public void setConteudo(String conteudo) {
        this.conteudo = conteudo;
    }

    public String getLabel() {
        return this.label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public List<HelpImg> getImagens() {
        if (this.imagens == null) {
            this.imagens = new ArrayList<>();
        }
        return this.imagens;
    }

    public void setImagens(List<HelpImg> imagens) {
        this.imagens = imagens;
    }
}
