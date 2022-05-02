package br.com.lume.odonto.contify.model;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Contact implements Serializable {

    private static final long serialVersionUID = 4513448297592921299L;

    @JsonProperty("cpf_titular")
    private Value cpfTitular;

    @JsonProperty("token")
    private Value token;

    private List<ContactData> data;

    public Value getCpfTitular() {
        return cpfTitular;
    }

    public void setCpfTitular(Value cpfTitular) {
        this.cpfTitular = cpfTitular;
    }

    public Value getToken() {
        return token;
    }

    public void setToken(Value token) {
        this.token = token;
    }

    public List<ContactData> getData() {
        return data;
    }

    public void setData(List<ContactData> data) {
        this.data = data;
    }

}
