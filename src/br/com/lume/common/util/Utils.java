package br.com.lume.common.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.Normalizer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.imageio.stream.FileImageOutputStream;
import javax.swing.text.MaskFormatter;

import org.primefaces.event.FileUploadEvent;

import com.google.gson.Gson;

import br.com.lume.configuracao.Configurar;
import br.com.lume.odonto.entity.ViaCep;
import br.com.lume.odonto.util.OdontoMensagens;

public class Utils {

    public static final Integer BIGDECIMAL_ROUDING = BigDecimal.ROUND_HALF_UP;

    public static final Integer BIGDECIMAL_SCALE = 2;

    private static final String USER_AGENT = "Mozilla/5.0";

    public static BigDecimal divide(BigDecimal dividendo, BigDecimal divisor) {
        return divide(dividendo, divisor, BIGDECIMAL_SCALE);
    }

    public static boolean validaDataNascimento(Date dataNascimento) {
        if (dataNascimento != null) {
            Calendar c = Calendar.getInstance();
            c.set(1900, Calendar.JANUARY, 1);
            if (dataNascimento.before(c.getTime())) {
                return false;
            }
            c = Calendar.getInstance();
            if (dataNascimento.after(c.getTime())) {
                return false;
            }
        }
        return true;
    }

    public static String formataCEP(String linha) {
        return format("##.###-###", linha);
    }
    
    public static Date getLastHourOfDate(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.MONTH, 1);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.add(Calendar.DATE, -1);
        cal.set(Calendar.HOUR, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
       return cal.getTime();
    }
    
    public static Date setLastHourDate(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        return cal.getTime();
    }
    
    public static Date setFirstHourDate(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        return cal.getTime();
    }

    public static String format(String pattern, Object value) {
        MaskFormatter mask;
        try {
            mask = new MaskFormatter(pattern);
            mask.setValueContainsLiteralCharacters(false);
            return mask.valueToString(value);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return String.valueOf(value);
    }

    public static ViaCep buscaCep(String cep) {
        String json = Utils.sendGet("https://viacep.com.br/ws/" + cep + "/json/", "GET");
        if (json != null && !json.contains("erro")) {
            return new Gson().fromJson(json, ViaCep.class);
        }
        return null;
    }

    public static String sendGet(String url, String method) {
        try {
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod(method);
            con.setRequestProperty("User-Agent", Utils.USER_AGENT);
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), Charset.forName("UTF-8")));
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            return response.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static Date getDataAtual(Date d) {
        if (d != null) {
            Calendar hoje = Calendar.getInstance();
            Calendar param = Calendar.getInstance();
            param.setTime(d);
            param.set(Calendar.YEAR, hoje.get(Calendar.YEAR));
            param.set(Calendar.MONTH, hoje.get(Calendar.MONTH));
            param.set(Calendar.DAY_OF_MONTH, hoje.get(Calendar.DAY_OF_MONTH));
            return param.getTime();
        }
        return null;
    }

    public static Date getPrimeiroDiaMesCorrente() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        return cal.getTime();
    }

    public static BigDecimal divide(BigDecimal dividendo, BigDecimal divisor, Integer scale) {
        BigDecimal retorno = new BigDecimal(0);
        if (dividendo != null && divisor != null && divisor.signum() != 0) {
            retorno = dividendo.divide(divisor, BIGDECIMAL_SCALE, scale);
        }
        return retorno;
    }

    public static String formataTempoHHMM(long totalMinutos) {
        String retorno = "";
        long totalHoras = totalMinutos / 60;
        if (totalHoras < 10) {
            retorno += "0";
        }
        retorno += totalHoras + ":";
        totalMinutos = totalMinutos % 60;
        if (totalMinutos < 10) {
            retorno += "0";
        }
        retorno += totalMinutos;
        return retorno;
    }

    public static String toFirstUpperCase(String nome) {
        String nomeCorreto = "";
        if (nome != null && !nome.equals("")) {
            String[] split = nome.split(" ");
            for (String string : split) {
                String lowerCase = string.toLowerCase();
                char charAt = lowerCase.charAt(0);
                String upperCase = (charAt + "").toUpperCase();
                nomeCorreto += upperCase + "" + lowerCase.substring(1, lowerCase.length()) + " ";
            }
        }
        return nomeCorreto;
    }

    public static String getExtensao(String fileName) {
        String[] split = fileName.split("\\.");
        String extensao = "";
        if (split.length > 0) {
            extensao = split[split.length - 1];
        }
        return extensao;
    }

    public static String dateToString(Date data) {
        return dateToString(data, "dd/MM/yy HH:mm:ss");
    }

    public static String dateToStringAgenda(Date data) {
        return dateToString(data, "yyyy-MM-ddTHH:mm:ss");
    }

    public static String dateToString(Date data, String pattern) {
        String dataStr = null;
        if (data != null) {
            SimpleDateFormat sdf = new SimpleDateFormat(pattern);
            dataStr = sdf.format(data);
        }
        return dataStr;
    }

    public static String handleFotoUpload(FileUploadEvent event, String nomeImagem) throws Exception {
        File targetFile = null;
        if (nomeImagem != null && !nomeImagem.equals("")) {
            targetFile = new File(OdontoMensagens.getMensagem("template.dir.imagens") + File.separator + nomeImagem);
        }
        InputStream initialStream = event.getFile().getInputstream();
        byte[] buffer = new byte[initialStream.available()];
        initialStream.read(buffer);
        if (targetFile == null || !targetFile.exists()) {
            nomeImagem = Calendar.getInstance().getTimeInMillis() + "." + event.getFile().getFileName().split("\\.")[1];
            targetFile = new File(OdontoMensagens.getMensagem("template.dir.imagens") + File.separator + nomeImagem);
        }
        OutputStream outStream = new FileOutputStream(targetFile);
        outStream.write(buffer);
        outStream.close();
        return targetFile.getName();
    }

    public static String handleFoto(byte[] data, String nomeImagem) throws Exception {
        File targetFile = null;
        if (nomeImagem != null && !nomeImagem.equals("")) {
            targetFile = new File(OdontoMensagens.getMensagem("template.dir.imagens") + File.separator + nomeImagem);
        }

        if (targetFile == null || !targetFile.exists()) {
            nomeImagem = Configurar.getInstance().getConfiguracao().getProfissionalLogado().getIdEmpresa() + "_" + Calendar.getInstance().getTimeInMillis() + ".jpeg";
            targetFile = new File(OdontoMensagens.getMensagem("template.dir.imagens") + File.separator + nomeImagem);
        }
        FileImageOutputStream imageOutput = new FileImageOutputStream(targetFile);
        imageOutput.write(data, 0, data.length);
        imageOutput.close();
        return targetFile.getName();
    }

    public static String split(String s, int t) {
        return s != null && s.length() > t ? s.substring(0, t) : s;
    }

    public static String normalize(String s) {
        return Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "");
    }

    public static Date stringToDate(String dataStr) {
        return stringToDate(dataStr, "yyyy-MM-dd HH:mm");
    }

    public static Date stringToDate(String dataStr, String pattern) {
        SimpleDateFormat formatter = new SimpleDateFormat(pattern);
        String dateInString = dataStr;
        Date date;
        try {
            date = formatter.parse(dateInString);
            return date;
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    public static String stringToCurrency(BigDecimal valor) {
        String currency = String.valueOf(valor);
        currency = currency.replaceAll(",", "");
        if (!currency.contains(".")) {
            currency = currency.concat(",00");
        } else {
            currency = currency.replaceAll("\\.", ",");
        }
        return currency;
    }
}
