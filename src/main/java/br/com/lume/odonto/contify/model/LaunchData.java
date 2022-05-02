package br.com.lume.odonto.contify.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LaunchData {

    private String date, type, value, description;

    @JsonProperty("deductible_expense_code")
    private Integer code;

    @JsonProperty("cpf_cnpj_contact")
    private String cpfCnpjContact;

    public static final String DESPESA = "D";

    public static final String RECEITA = "R";

    public LaunchData(String date, String type, String value, String description, Integer code, String cpfCnpjContact) {
        super();
        this.date = date;
        this.type = type;
        this.value = value;
        this.description = description;
        this.code = code;
        this.cpfCnpjContact = cpfCnpjContact;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

}
