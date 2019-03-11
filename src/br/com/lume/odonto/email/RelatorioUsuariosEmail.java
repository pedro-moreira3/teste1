package br.com.lume.odonto.email;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import br.com.lume.common.util.EnviaEmail;
import br.com.lume.common.util.Mensagens;
import br.com.lume.odonto.bo.RelatorioGerencialBO;

public class RelatorioUsuariosEmail implements Job {

    private Logger log = Logger.getLogger(RelatorioUsuariosEmail.class);

    @Override
    public void execute(JobExecutionContext arg0) throws JobExecutionException {
        this.doRelatorioGerencialEmail();
    }

    public static void main(String[] args) {
        new RelatorioUsuariosEmail().doRelatorioGerencialEmail();
    }

    public void doRelatorioGerencialEmail() {
        try {
            Calendar cal = Calendar.getInstance();
            if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY || cal.get(Calendar.DAY_OF_WEEK) == Calendar.TUESDAY ) {
                String emails = "alvaro@consultoriolegal.com.br;joao.serra@lumetec.com.br;ariel.pires@lumetec.com.br;ricardo.poncio@lumetec.com.br;rogerio.nagata@lumetec.com.br";

                Map<String, String> valores = new HashMap<>();
                RelatorioGerencialBO bo = new RelatorioGerencialBO();

                valores.put("#relatorio", bo.gerarRelatorioUsuario());
                valores.put("#cliente", "");

                EnviaEmail.enviaEmailOffLine("no-reply@intelidente.com", emails, "Intelidente - Resumo semanal de usuários ", EnviaEmail.buscarTemplate(valores, EnviaEmail.RESUMO_USUARIO),
                        Mensagens.getMensagemOffLine("email.smtpHost.prod"));
           }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
