package br.com.lume.common.util;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class GeradorSenha {

    private static final int DIGITOS = 6;

    public static String gerarSenha() {
        String[] keys = new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9", "0", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z" };

        List<String> chars = Arrays.asList(keys);
        Random random = new Random();
        String senha = "";
        for (int i = 0; i < DIGITOS; i++) {
            senha += chars.get(random.nextInt(chars.size()));
        }
        return senha;
    }
}
