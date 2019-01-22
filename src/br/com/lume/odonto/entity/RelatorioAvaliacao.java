package br.com.lume.odonto.entity;

import java.io.Serializable;
import java.text.Normalizer;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import br.com.lume.common.util.Status;

public class RelatorioAvaliacao implements Serializable, Comparable<RelatorioAvaliacao> {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private Profissional profissional;

    private Integer avaliacao;

    private Long total;

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

    public RelatorioAvaliacao() {
    }

    public RelatorioAvaliacao(
            Profissional profissional,
            Integer avaliacao) {
        this.profissional = profissional;
        this.avaliacao = avaliacao;
    }

    public RelatorioAvaliacao(
            Integer avaliacao,
            Long total) {
        super();
        this.avaliacao = avaliacao;
        this.total = total;
    }

    public Profissional getProfissional() {
        return this.profissional;
    }

    public void setProfissional(Profissional profissional) {
        this.profissional = profissional;
    }

    public Integer getAvaliacao() {
        return this.avaliacao;
    }

    public void setAvaliacao(Integer avaliacao) {
        this.avaliacao = avaliacao;
    }

    public Long getTotal() {
        return this.total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    @Override
    public int compareTo(RelatorioAvaliacao o) {
        if (this.profissional == null || this.profissional.getDadosBasico() == null || this.profissional.getDadosBasico().getNome() == null || o == null || o.profissional == null || o.profissional.getDadosBasico() == null || o.profissional.getDadosBasico().getNome() == null) {
            return 1;
        } else {
            return Normalizer.normalize(this.profissional.getDadosBasico().getNome(), Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "").compareToIgnoreCase(
                    Normalizer.normalize(o.profissional.getDadosBasico().getNome(), Normalizer.Form.NFD));
        }
    }
}
