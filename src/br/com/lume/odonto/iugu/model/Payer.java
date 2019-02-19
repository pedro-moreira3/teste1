package br.com.lume.odonto.iugu.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Payer implements Serializable {

    private static final long serialVersionUID = 3266886175287194L;

    @JsonProperty("cpf_cnpj")
    private String cpfCnpj;

    private String name;

    @JsonProperty("phone_prefix")
    private String phonePrefix;

    private String phone;

    private String email;

    private Address address;

    public Payer() {
    }

    public Payer(String cpfCnpj, String name, Address address) {
        super();
        this.cpfCnpj = cpfCnpj;
        this.name = name;
        this.address = address;
    }

    public String getCpfCnpj() {
        return cpfCnpj;
    }

    public void setCpfCnpj(String cpfCnpj) {
        this.cpfCnpj = cpfCnpj;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhonePrefix() {
        return phonePrefix;
    }

    public void setPhonePrefix(String phonePrefix) {
        this.phonePrefix = phonePrefix;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public static long getSerialversionuid() {
        return serialVersionUID;
    }

}
