package br.com.lume.odonto.entity;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "PLANO_TRATAMENTO_APARELHO")
public class PlanoTratamentoAparelho implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_PLANO_TRATAMENTO")
    private PlanoTratamento planoTratamento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_APARELHO")
    private AparelhoOrtodontico aparelhoOrtodontico;

    public PlanoTratamentoAparelho() {
    }

    public PlanoTratamentoAparelho(PlanoTratamento planoTratamento, AparelhoOrtodontico aparelhoOrtodontico) {
        super();
        this.planoTratamento = planoTratamento;
        this.aparelhoOrtodontico = aparelhoOrtodontico;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public PlanoTratamento getPlanoTratamento() {
        return planoTratamento;
    }

    public void setPlanoTratamento(PlanoTratamento planoTratamento) {
        this.planoTratamento = planoTratamento;
    }

    public AparelhoOrtodontico getAparelhoOrtodontico() {
        return aparelhoOrtodontico;
    }

    public void setAparelhoOrtodontico(AparelhoOrtodontico aparelhoOrtodontico) {
        this.aparelhoOrtodontico = aparelhoOrtodontico;
    }

}
