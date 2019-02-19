package br.com.lume.common.util;

import java.util.ArrayList;
import java.util.List;

public enum UF {

    AC("Acre"), AL("Alagoas"), AP("Amap�"), AM("Amazonas"), BA("Bahia"), CE("Cear�"), DF("Distrito Federal"), ES("Espirito Santo"), GO("Goi�s"), MA("Maranh�o"), MT("Mato Grosso"), MS(
            "Mato Grosso do Sul"), MG("Minas Gerais"), PA("Par�"), PB("Para�ba"), PR("Paran�"), PE("Pernambuco"), PI("Piaui"), RJ(
                    "Rio de Janeiro"), RN("Rio Grande do Norte"), RS("Rio Grande do Sul"), RO("Rond�nia"), RR("Roraima"), SC("Santa Catarina"), SP("S�o Paulo"), SE("Sergipe"), TO("Tocantins");

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
