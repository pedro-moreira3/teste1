package br.com.lume.common.util;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Version {

    public static String getVersion(String warName) {
        File file = new File(warName + ".war");
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        if (file.exists()) {
            return sdf.format(new Date(file.lastModified()));
        } else {
            return "N�o consegui achar o war !!";
        }
    }

}
