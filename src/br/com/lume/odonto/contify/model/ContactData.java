package br.com.lume.odonto.contify.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ContactData {

    @JsonProperty("cpf_cnpj")
    private String cpfCnpj;

    @JsonProperty("fantasy_name")
    private String fantasyName;

    public ContactData(String cpfCnpj, String fantasyName) {
        super();
        this.cpfCnpj = cpfCnpj;
        this.fantasyName = fantasyName;
    }

    public String getCpfCnpj() {
        return cpfCnpj;
    }

    public void setCpfCnpj(String cpfCnpj) {
        this.cpfCnpj = cpfCnpj;
    }

    public String getFantasyName() {
        return fantasyName;
    }

    public void setFantasyName(String fantasyName) {
        this.fantasyName = fantasyName;
    }

}
