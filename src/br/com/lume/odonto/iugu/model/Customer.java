package br.com.lume.odonto.iugu.model;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Customer implements Serializable {

    private static final long serialVersionUID = 3266886175287194L;

    public Customer(String email, String name) {
        this.email = email;
        this.name = name;
    }

    public Customer(String email, String name, String cpfCnpj, String zipCode, Integer number, String street, String distrinct, String city, String state) {
        super();
        this.email = email;
        this.name = name;
        this.cpfCnpj = cpfCnpj;
        this.zipCode = zipCode;
        this.number = number;
        this.street = street;
        district = distrinct;
        this.city = city;
        this.state = state;
    }

    private String email;

    private String name;

    private String notes;

    @JsonProperty("cpf_cnpj")
    private String cpfCnpj;

    @JsonProperty("cc_emails")
    private String ccEmails;

    @JsonProperty("zip_code")
    private String zipCode;

    private Integer number;

    private String street;

    private String city;

    private String state;

    private String district;

    private String complement;

    @JsonProperty("custom_variables")
    private List<CustomVariable> customVariables;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getCpfCnpj() {
        return cpfCnpj;
    }

    public void setCpfCnpj(String cpfCnpj) {
        this.cpfCnpj = cpfCnpj;
    }

    public String getCcEmails() {
        return ccEmails;
    }

    public void setCcEmails(String ccEmails) {
        this.ccEmails = ccEmails;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getComplement() {
        return complement;
    }

    public void setComplement(String complement) {
        this.complement = complement;
    }

    public List<CustomVariable> getCustomVariables() {
        return customVariables;
    }

    public void setCustomVariables(List<CustomVariable> customVariables) {
        this.customVariables = customVariables;
    }

    public static long getSerialversionuid() {
        return serialVersionUID;
    }

}
