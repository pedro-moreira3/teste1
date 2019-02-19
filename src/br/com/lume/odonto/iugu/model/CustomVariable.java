package br.com.lume.odonto.iugu.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CustomVariable {

    public CustomVariable(String name, String value) {
        this.name = name;
        this.value = value;
    }

    private String name;

    private String value;

    @JsonProperty("_destroy")
    private Boolean destroy;

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public Boolean getDestroy() {
        return destroy;
    }

    public void setDestroy(Boolean destroy) {
        this.destroy = destroy;
    }

}
