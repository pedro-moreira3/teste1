package br.com.lume.common.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.URL;
import java.net.URLConnection;

public class ClienteViaCepWS implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 5960094687971771522L;

    public static String buscarCep(String cep) {
        String json;

        try {
            URL url = new URL("http://viacep.com.br/ws/" + cep + "/json");
            URLConnection urlConnection = url.openConnection();
            InputStream is = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF8"));

            StringBuilder jsonSb = new StringBuilder();

            br.lines().forEach(l -> jsonSb.append(l.trim()));

            json = jsonSb.toString();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return json;
    }

    /*
     * Exemplo de uso public static void main(String[] args) throws IOException { String json = buscarCep("69046000"); System.out.println(json); Map<String, String> mapa = new HashMap<>(); Matcher
     * matcher = Pattern.compile("\"\\D.*?\": \".*?\"").matcher(json); while (matcher.find()) { String[] group = matcher.group().split(":"); mapa.put(group[0].replaceAll("\"", "").trim(),
     * group[1].replaceAll("\"", "").trim()); } System.out.println(mapa); }
     */
}
