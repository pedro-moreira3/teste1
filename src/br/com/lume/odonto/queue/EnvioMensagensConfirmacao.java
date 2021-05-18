package br.com.lume.odonto.queue;

import java.io.Serializable;
import java.util.List;
import java.util.TimerTask;

import br.com.lume.agendamento.AgendamentoSingleton;
import br.com.lume.execucaoTimer.interfaces.Executor;
import br.com.lume.integracao.DetailPacoteConfirmacaoAutoSingleton;
import br.com.lume.integracao.HistoricoMensagemIntegracaoSingleton;
import br.com.lume.odonto.entity.Agendamento;
import br.com.lume.odonto.entity.DetailPacoteConfirmacaoAuto;
import br.com.lume.odonto.entity.Paciente;
import br.com.lume.security.EmpresaSingleton;
import br.com.lume.security.entity.Empresa;

public class EnvioMensagensConfirmacao extends TimerTask implements Serializable, Executor {

    private static final long serialVersionUID = 1L;

    public String getCodigo() {
        return "ENVIO_MENSAGEM_CONFIRMACAO";
    }

    @Override
    public void run() {
        try {
            List<Agendamento> agendamentos = AgendamentoSingleton.getInstance().getBo().listAgendamentosParaEnvioConfirmacaoAutomatica();
            for (Agendamento agendamento : agendamentos) {                
                Paciente paciente = agendamento.getPaciente();
                Empresa empresa = EmpresaSingleton.getInstance().getBo().find(paciente.getIdEmpresa());
                boolean retornoEnvio  = HistoricoMensagemIntegracaoSingleton.getInstance().enviaMensagemAutomaticaParaCliente(
                        paciente, agendamento, null, empresa);
                if(retornoEnvio) {
                    //TODO transformar em log para salvar em tb to banco
                    System.out.println("MENSAGEM CONFIRMACAO: Mensagem enviada com sucesso para o agendamento id: " + agendamento.getId());
                }else {
                    //TODO transformar em log para salvar em tb to banco
                    System.out.println("MENSAGEM CONFIRMACAO: Mensagem não enviada para o  agendamento id: " + agendamento.getId());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //TODO transformar em log para salvar em tb to banco
        System.out.println("MENSAGEM CONFIRMACAO: RODOU O ENVIO");
    }

}
