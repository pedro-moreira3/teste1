package br.com.lume.common.util;

import com.google.gson.Gson;

import br.com.lume.odonto.entity.ViaCep;

public class Endereco {

    private String bairro, cidade, rua, estado;

    public static Endereco getEndereco(String cep) {
        String json = Utils.sendGet("https://viacep.com.br/ws/" + cep + "/json/", "GET");
        if (json != null && !json.contains("erro")) {
            ViaCep vc = new Gson().fromJson(json, ViaCep.class);
            if (vc != null) {
                return new Endereco(vc.getBairro(), vc.getLocalidade(), vc.getLogradouro(), vc.getUf().toUpperCase().trim());
            }
        }
        return null;
    }

    public Endereco(String bairro, String cidade, String rua, String estado) {
        super();
        this.bairro = bairro;
        this.cidade = cidade;
        this.rua = rua;
        this.estado = estado;
    }

    public String getBairro() {
        return bairro;
    }

    public void setBairro(String bairro) {
        this.bairro = bairro;
    }

    public String getCidade() {
        return cidade;
    }

    public void setCidade(String cidade) {
        this.cidade = cidade;
    }

    public String getRua() {
        return rua;
    }

    public void setRua(String rua) {
        this.rua = rua;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

}
