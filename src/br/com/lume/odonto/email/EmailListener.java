package br.com.lume.odonto.email;

import java.net.InetAddress;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.Logger;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;

import br.com.lume.odonto.contify.ContifyBatch;
import br.com.lume.odonto.util.OdontoMensagens;

public class EmailListener implements ServletContextListener {

    private Logger log = Logger.getLogger(EmailListener.class);

    private Scheduler scheduler;

    @Override
    public void contextDestroyed(ServletContextEvent arg0) {
        if (scheduler != null) {
            try {
                scheduler.shutdown();
            } catch (SchedulerException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void contextInitialized(ServletContextEvent arg0) {
        try {
            String servidor = InetAddress.getLocalHost().getHostName();
            if (OdontoMensagens.getMensagem("servidor.producao").trim().equals(servidor.trim())) {

                String hora = arg0.getServletContext().getInitParameter("HORA_BATCH");

                CronTrigger trigger = new CronTrigger("MyTrigger", Scheduler.DEFAULT_GROUP, "0 0 " + hora + " * * ?");
                CronTrigger trigger2 = new CronTrigger("MyTrigger2", Scheduler.DEFAULT_GROUP, "0 15 " + hora + " * * ?");
                CronTrigger trigger3 = new CronTrigger("MyTrigger3", Scheduler.DEFAULT_GROUP, "0 30 " + hora + " * * ?");
                CronTrigger trigger4 = new CronTrigger("MyTrigger3", Scheduler.DEFAULT_GROUP, "0 45 " + hora + " * * ?");

                scheduler = new StdSchedulerFactory().getScheduler();

                JobDetail jobDetail = new JobDetail("LancamentoContabilProfissional", Scheduler.DEFAULT_GROUP, LancamentoContabilAutomatico.class);
                scheduler.scheduleJob(jobDetail, trigger);

//                JobDetail jobDetail2 = new JobDetail("RelatorioGerencialEmail", Scheduler.DEFAULT_GROUP, RelatorioGerencialEmail.class);
//                scheduler.scheduleJob(jobDetail2, trigger2);

                JobDetail jobDetail3 = new JobDetail("RelatorioUsuariosEmail", Scheduler.DEFAULT_GROUP, RelatorioUsuariosEmail.class);
                scheduler.scheduleJob(jobDetail3, trigger3);

                JobDetail jobDetail4 = new JobDetail("Contify", Scheduler.DEFAULT_GROUP, ContifyBatch.class);
                scheduler.scheduleJob(jobDetail4, trigger4);

                scheduler.start();

                System.out.println("Listener Email Iniciado.");
            } else {
                System.out.println("Contexto em desenvolvimento : " + servidor);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
