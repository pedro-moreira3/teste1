package br.com.lume.odonto.queue;

import java.io.Serializable;
import java.util.List;
import java.util.TimerTask;

import br.com.lume.common.log.LogIntelidenteSingleton;
import br.com.lume.execucaoTimer.ExecucaoTimerSingleton;
import br.com.lume.execucaoTimer.interfaces.Executor;
import br.com.lume.faturamento.FaturaSingleton;
import br.com.lume.odonto.entity.Fatura;
import br.com.lume.odonto.entity.Fatura.StatusFatura;
import br.com.lume.profissional.ProfissionalSistemaSingleton;

public class AtualizacaoFaturas extends TimerTask implements Serializable, Executor {

    private static final long serialVersionUID = 1L;

    public String getCodigo() {
        return "ATUALIZACAO_FATURAS";
    }

    @Override
    public void run() {
        try {
            //pra enviar somente entre as 1 e 2 da manha
            if (!GerenciadorTarefasAgendadas.isWithinRange(1, 2)) {
                LogIntelidenteSingleton.getInstance().makeLog("Task '" + getCodigo() + "': Horário atual fora da range de 1 e 2 da manhã estabelecida!");
            } else if (ExecucaoTimerSingleton.getInstance().jaRodouHoje(this)) {
                LogIntelidenteSingleton.getInstance().makeLog("Task '" + getCodigo() + "': Já rodou nesta data!");
            } else {

                System.out.println("chamou método de atualização dos status das faturas");
                List<Fatura> faturas = FaturaSingleton.getInstance().getBo().listAllByStatusAndCredito(null, null, null);
                faturas.sort((o1,o2) -> o2.getDataCriacao().compareTo(o1.getDataCriacao()));
                System.out.println(faturas.size());
                int count = 0;
                for (Fatura fatura : faturas) {
                    FaturaSingleton.getInstance().atualizarStatusFatura(fatura, ProfissionalSistemaSingleton.getInstance().getSysProfissional());
                    System.out.println(fatura.getDataCriacao());
                    count++;
                    System.out.println(count);
                }
                
//                List<Fatura> faturas = FaturaSingleton.getInstance().getBo().listAllByStatusAndCredito(null, StatusFatura.A_RECEBER, null);
//                for (Fatura fatura : faturas)
//                    FaturaSingleton.getInstance().atualizarStatusFatura(fatura, ProfissionalSistemaSingleton.getInstance().getSysProfissional());

                ExecucaoTimerSingleton.getInstance().postResult(this, true, null);
                LogIntelidenteSingleton.getInstance().makeLog("Task '" + getCodigo() + "': Execução concluida!");
            }
        } catch (Exception e) {
            ExecucaoTimerSingleton.getInstance().postResult(this, false, e.getMessage());
            LogIntelidenteSingleton.getInstance().makeLog("Task '" + getCodigo() + "': Erro ao executar chamada", e);
        }
    }

}
