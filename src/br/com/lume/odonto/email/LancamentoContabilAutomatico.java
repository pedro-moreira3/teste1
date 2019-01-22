package br.com.lume.odonto.email;

import java.util.Calendar;
import java.util.List;

import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import br.com.lume.odonto.bo.LancamentoContabilBO;
import br.com.lume.odonto.bo.MotivoBO;
import br.com.lume.odonto.bo.ProfissionalBO;
import br.com.lume.odonto.entity.LancamentoContabil;
import br.com.lume.odonto.entity.Motivo;
import br.com.lume.odonto.entity.Profissional;

public class LancamentoContabilAutomatico implements Job {

    private Logger log = Logger.getLogger(EmailListener.class);

    @Override
    public void execute(JobExecutionContext arg0) throws JobExecutionException {
        this.doLancamentoContabilAutomatico();
    }

    public static void main(String[] args) {
        new LancamentoContabilAutomatico().doLancamentoContabilAutomatico();
    }

    public void doLancamentoContabilAutomatico() {
        try {
            ProfissionalBO profissionalBO = new ProfissionalBO();
            MotivoBO motivoBO = new MotivoBO();
            LancamentoContabilBO lancamentoContabilBO = new LancamentoContabilBO();
            List<Profissional> profissionais = profissionalBO.listProfissionalLancamentoContabilAutomatico();
            for (Profissional profissional : profissionais) {
                LancamentoContabil lc = new LancamentoContabil();
                Motivo motivo = motivoBO.findBySigla(Motivo.PAGAMENTO_PROFISSIONAL);
                lc.setIdEmpresa(profissional.getIdEmpresa());
                lc.setTipo(motivo.getTipo());
                lc.setDadosBasico(profissional.getDadosBasico());
                lc.setMotivo(motivo);
                lc.setValor(profissional.getValorRemuneracao());
                lc.setNotaFiscal(null);
                lc.setData(Calendar.getInstance().getTime());
                lancamentoContabilBO.persist(lc);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
