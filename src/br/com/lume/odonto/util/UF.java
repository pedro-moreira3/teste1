package br.com.lume.odonto.util;

import java.util.ArrayList;
import java.util.List;

public enum UF {

    AC("Acre"), AL("Alagoas"), AP("Amapá"), AM("Amazonas"), BA("Bahia"), CE("Ceara"), DF("Distrito Federal"), ES("Espirito Santo"), GO("Goiás"), MA("Maranhão"), MT("Mato Grosso"), MS(
            "Mato Grosso do Sul"), MG("Minas Gerais"), PA("Pará"), PB("Paraíba"), PR("Paraná"), PE("Pernambuco"), PI("Piaui"), RJ(
                    "Rio de Janeiro"), RN("Rio Grande do Norte"), RS("Rio Grande do Sul"), RO("Rondônia"), RR("Roraima"), SC("Santa Catarina"), SP("São Paulo"), SE("Sergipe"), TO("Tocantins");

    private String nome;

    private static List<UF> list;

    private UF(
            String nome) {
        this.nome = nome;
    }

    public String getNome() {
        return this.nome;
    }

    public static void setList(List<UF> list) {
        UF.list = list;
    }

    public static List<UF> getList() {

        if (list == null) {
            list = new ArrayList<>();
            for (UF uf : values()) {
                list.add(uf);
            }
        }

        return list;
    }
}
