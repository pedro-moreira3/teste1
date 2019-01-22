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
@Table(name = "PLANO_TRATAMENTO_DIAGNOSTICO")
public class PlanoTratamentoDiagnostico implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_PLANO_TRATAMENTO")
    private PlanoTratamento planoTratamento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_DIAGNOSTICO")
    private DiagnosticoOrtodontico diagnosticoOrtodontico;

    public PlanoTratamentoDiagnostico() {
    }

    public PlanoTratamentoDiagnostico(PlanoTratamento planoTratamento, DiagnosticoOrtodontico diagnosticoOrtodontico) {
        super();
        this.planoTratamento = planoTratamento;
        this.diagnosticoOrtodontico = diagnosticoOrtodontico;
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

    public DiagnosticoOrtodontico getDiagnosticoOrtodontico() {
        return diagnosticoOrtodontico;
    }

    public void setDiagnosticoOrtodontico(DiagnosticoOrtodontico diagnosticoOrtodontico) {
        this.diagnosticoOrtodontico = diagnosticoOrtodontico;
    }

}
