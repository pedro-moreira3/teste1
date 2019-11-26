package br.com.lume.odonto.email;

import java.util.Calendar;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import br.com.lume.common.util.EnviaEmail;
import br.com.lume.common.util.Mensagens;
import br.com.lume.relatorioGerencial.RelatorioGerencialSingleton;

public class RelatorioUsuariosEmail implements Job {

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

            if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY) {
                //joao.serra@lumetec.com.br;ariel.pires@lumetec.com.br;rogerio.nagata@lumetec.com.br;alvaro@consultoriolegal.com.br;ricardo.poncio@lumetec.com.br
                String emails = "ricardo.poncio@lumetec.com.br";

                EnviaEmail.enviaEmailOffLine("no-reply@intelidente.com", emails, "Intelidente - Resumo semanal de usuários ", RelatorioGerencialSingleton.getInstance().getBo().gerarRelatorioUsuario(),
                        Mensagens.getMensagemOffLine("email.smtpHost.prod"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
