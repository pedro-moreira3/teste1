package br.com.lume.odonto.queue;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class GerenciadorTarefasAgendadas implements ServletContextListener, Serializable {

    private static final long serialVersionUID = 1L;

    //20 em 20 minutos
    private static final long TEMPO_CONSUMO_ATUALIZACAO_FATURAS = GerenciadorTarefasAgendadas.minutesToMillis(20);
    private AtualizacaoFaturas atualizacaoFaturas = new AtualizacaoFaturas();

    @Override
    public void contextInitialized(ServletContextEvent arg0) {
        Timer time = new Timer(true);
        time.scheduleAtFixedRate(atualizacaoFaturas, 0, TEMPO_CONSUMO_ATUALIZACAO_FATURAS);
    }

    public static boolean isWithinRange(int horaIni, int horaFim) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.HOUR_OF_DAY, horaIni);
        Date dataInicial = cal.getTime();
        cal.set(Calendar.HOUR_OF_DAY, horaFim);
        Date dataFinal = cal.getTime();
        return !(new Date().before(dataInicial) || new Date().after(dataFinal));
    }

    public static int minutesToMillis(int minutes) {
        return minutes * 60000;
    }

    @Override
    public void contextDestroyed(ServletContextEvent arg0) {

    }

}
