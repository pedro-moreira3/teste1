package br.com.lume.odonto.contify.model;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Launch implements Serializable {

    private static final long serialVersionUID = 4513448297592921299L;

    @JsonProperty("cpf_titular")
    private Value cpfTitular;

    @JsonProperty("base_year")
    private Value baseYear;

    @JsonProperty("base_month")
    private Value baseMonth;

    @JsonProperty("token")
    private Value token;

    private List<LaunchData> data;

    public Value getCpfTitular() {
        return cpfTitular;
    }

    public void setCpfTitular(Value cpfTitular) {
        this.cpfTitular = cpfTitular;
    }

    public Value getBaseYear() {
        return baseYear;
    }

    public void setBaseYear(Value baseYear) {
        this.baseYear = baseYear;
    }

    public Value getBaseMonth() {
        return baseMonth;
    }

    public void setBaseMonth(Value baseMonth) {
        this.baseMonth = baseMonth;
    }

    public Value getToken() {
        return token;
    }

    public void setToken(Value token) {
        this.token = token;
    }

    public List<LaunchData> getData() {
        return data;
    }

    public void setData(List<LaunchData> data) {
        this.data = data;
    }

}
