package br.com.lume.odonto.email;

import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import br.com.lume.odonto.bo.PlanoTratamentoBO;

public class FinalizaPlano implements Job {

    private Logger log = Logger.getLogger(FinalizaPlano.class);

    @Override
    public void execute(JobExecutionContext arg0) throws JobExecutionException {
        new PlanoTratamentoBO().finalizaPlanoBatch();
    }
}
