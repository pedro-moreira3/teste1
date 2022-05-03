package br.com.lume.odonto.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Set;

public class GXComparatorDeEstoque {

    public static void main(String[] args) {

        try {
            HashMap<Long, Integer> hojem = new HashMap<>();
            HashMap<Long, String> hojev = new HashMap<>();
            HashMap<Long, Integer> dia1m = new HashMap<>();

            BufferedReader hoje = new BufferedReader(new FileReader(new File("C:\\Users\\faruk.zahra\\Desktop\\hoje.csv")));
            while (hoje.ready()) {
                String[] split = hoje.readLine().split(";");
                hojem.put(Long.parseLong(split[0].trim()), Integer.parseInt(split[4]));
                hojev.put(Long.parseLong(split[0].trim()), split[1] + ";" + split[2] + ";" + split[3]);
            }
            hoje.close();

            BufferedReader dia1 = new BufferedReader(new FileReader(new File("C:\\Users\\faruk.zahra\\Desktop\\dia1.csv")));
            while (dia1.ready()) {
                String[] split = dia1.readLine().split(";");
                dia1m.put(Long.parseLong(split[0].trim()), Integer.parseInt(split[4]));
            }
            dia1.close();

            Set<Long> keySet = hojem.keySet();
            for (Long k : keySet) {
                Integer integer = dia1m.get(k);
                Integer hojeq = hojem.get(k);
                String hojevv = hojev.get(k);
                String x = "";
                if (integer != null && (integer.intValue() != hojeq.intValue())) {
                    x = "x";
                }
                System.out.println(k + ";" + hojevv + ";" + hojeq + ";" + (integer != null ? integer : "-") + ";" + x);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
