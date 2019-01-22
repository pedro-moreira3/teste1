package br.com.lume.odonto.entity;

import java.util.Date;

public class AgendamentoAgenda {

    private static final long serialVersionUID = 1L;

    private long id;

    private String descricaoAgenda, enderecoCompleto, descricaoInternaAgenda;

    private Date inicioStrAgenda, fimStrAgenda;

    public AgendamentoAgenda(long id, String descricaoAgenda, String descricaoInternaAgenda, String enderecoCompleto, Date inicioStrAgenda, Date fimStrAgenda) {
        super();
        this.id = id;
        this.descricaoAgenda = descricaoAgenda;
        this.descricaoInternaAgenda = descricaoInternaAgenda;
        this.enderecoCompleto = enderecoCompleto;
        this.inicioStrAgenda = inicioStrAgenda;
        this.fimStrAgenda = fimStrAgenda;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getDescricaoAgenda() {
        return descricaoAgenda;
    }

    public void setDescricaoAgenda(String descricaoAgenda) {
        this.descricaoAgenda = descricaoAgenda;
    }

    public String getEnderecoCompleto() {
        return enderecoCompleto;
    }

    public void setEnderecoCompleto(String enderecoCompleto) {
        this.enderecoCompleto = enderecoCompleto;
    }

    public Date getInicioStrAgenda() {
        return inicioStrAgenda;
    }

    public void setInicioStrAgenda(Date inicioStrAgenda) {
        this.inicioStrAgenda = inicioStrAgenda;
    }

    public Date getFimStrAgenda() {
        return fimStrAgenda;
    }

    public void setFimStrAgenda(Date fimStrAgenda) {
        this.fimStrAgenda = fimStrAgenda;
    }

    public String getDescricaoInternaAgenda() {
        return descricaoInternaAgenda;
    }

    public void setDescricaoInternaAgenda(String descricaoInternaAgenda) {
        this.descricaoInternaAgenda = descricaoInternaAgenda;
    }

}
