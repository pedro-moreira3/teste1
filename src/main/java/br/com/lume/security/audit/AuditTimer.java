package br.com.lume.security.audit;

import java.util.HashMap;

import br.com.lume.common.util.RandomString;

public class AuditTimer {

    private static HashMap<String, Long> times = new HashMap<>();

    private AuditTimer() {

    }

    public static String StartCount() {
        String hash = new RandomString().nextString();
        AuditTimer.times.put(hash, System.currentTimeMillis());
        return hash;
    }

    public static Long StopCount(String hash) {
        Long start = AuditTimer.times.get(hash);
        AuditTimer.times.remove(hash);
        return (System.currentTimeMillis() - start);
    }

}
