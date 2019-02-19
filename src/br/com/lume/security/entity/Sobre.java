package br.com.lume.security.entity;

public class Sobre {

    private String informacao;

    private String valor;

    public Sobre(
            String informacao,
            String valor) {
        this.informacao = informacao;
        this.valor = valor;
    }

    public String getInformacao() {
        return this.informacao;
    }

    public void setInformacao(String informacao) {
        this.informacao = informacao;
    }

    public String getValor() {
        return this.valor;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }
}
