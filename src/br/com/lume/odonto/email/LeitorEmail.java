package br.com.lume.odonto.email;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import br.com.lume.agendamento.AgendamentoSingleton;
import br.com.lume.common.util.EnviaEmail;
// import br.com.lume.odonto.bo.AgendamentoBO;
// import br.com.lume.odonto.bo.PlanoBO;
import br.com.lume.odonto.entity.Plano;
import br.com.lume.odonto.util.OdontoMensagens;
import br.com.lume.plano.PlanoSingleton;
import br.com.lume.security.EmpresaSingleton;
// import br.com.lume.security.bo.EmpresaBO;
import br.com.lume.security.entity.Empresa;

public class LeitorEmail implements Job {

    private Logger log = Logger.getLogger(EmailListener.class);

    @Override
    public void execute(JobExecutionContext arg0) throws JobExecutionException {
        this.doLeitorEmail();
    }

    public static void main(String[] args) {
        new LeitorEmail().doLeitorEmail();
    }

    public void doLeitorEmail() {
        try {
            //  AgendamentoBO agendamentoBO = new AgendamentoBO();
            // EmpresaBO empresaBO = new EmpresaBO();
            //  PlanoBO planoBO = new PlanoBO();
            List<Empresa> empresas = EmpresaSingleton.getInstance().getBo().getAllEmpresas(true);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
            for (Empresa empresa : empresas) {
                Integer quantidadeAgendamentosMesAtual = AgendamentoSingleton.getInstance().getBo().findQuantidadeAgendamentosMesAtual(empresa.getEmpIntCod());
                long idPlano = empresa.getPlano().getId();
                Plano p = PlanoSingleton.getInstance().getBo().find(idPlano);
                if (p != null) {
                    System.out.println("----------------------------");
                    System.out.println(empresa.getEmpStrNme());
                    Integer consultasPlano = p.getConsultas();
                    Integer porcEnvio = Integer.parseInt(OdontoMensagens.getMensagemOffLine("batch.porc.envio"));
                    if (consultasPlano > 0) {
                        Double porcEnvio_ = (double) (porcEnvio) / 100;
                        Double limiteEmail = (double) (consultasPlano * porcEnvio_);
                        System.out.println("quantidadeAgendamentosMesAtual " + quantidadeAgendamentosMesAtual);
                        System.out.println("limiteEmail " + limiteEmail);
                        if (quantidadeAgendamentosMesAtual != null && quantidadeAgendamentosMesAtual > 0 && quantidadeAgendamentosMesAtual >= limiteEmail) {
                            System.out.println("ENVIAR EMAIL >> " + empresa.getEmpStrEmail());
                            Plano plano = PlanoSingleton.getInstance().getBo().find(empresa.getPlano().getId());
                            Map<String, String> valores = new HashMap<>();
                            valores.put("#qtd_agendamentos", plano.getConsultas() + "");
                            valores.put("#data", simpleDateFormat.format(empresa.getEmpDtmExpiracao()));
                            EnviaEmail.enviaEmail("no-reply@intelidente.com", "ariel.pires@lumetec.com.br", "Limite da Franquia", EnviaEmail.buscarTemplate(valores, EnviaEmail.PLANO_FINALIZANDO));
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
