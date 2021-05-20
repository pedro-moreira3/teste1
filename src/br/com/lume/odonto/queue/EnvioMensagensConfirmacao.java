package br.com.lume.odonto.queue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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

            System.out.println("RODAAANDO");
            
            //TODO arrumar para a hora certA
            //Vamos enviar todas as mensagens entre as 7 e 8 da manha;
            if (GerenciadorTarefasAgendadas.isWithinRange(10, 12)) {

                Calendar cal = Calendar.getInstance();
                cal.setTime(new Date());

                List<Agendamento> agendamentos = new ArrayList<Agendamento>();
                Date dataInicio = null;
                Date dataFim = null;
                //na segunda, pegamos todos os agendamentos de terça e enviamos as mensagens;
                //na terça, pegamos todos os agendamentos de quarta e enviamos as mensagens;
                //na quarta, pegamos todos os agendamentos de quinta e enviamos as mensagens;
                //na quinta, pegamos todos os agendamentos de sexta e enviamos as mensagens;
                if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY || cal.get(Calendar.DAY_OF_WEEK) == Calendar.TUESDAY || cal.get(Calendar.DAY_OF_WEEK) == Calendar.WEDNESDAY || cal.get(
                        Calendar.DAY_OF_WEEK) == Calendar.THURSDAY) {
                    dataInicio = adicionaDiaPrimeiraHora(new Date(), 1);
                    dataFim = adicionaDiaUltimaHora(new Date(), 1);
                } else if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY) {
                    //na sexta, pegamos todos os agendamentos de sabado, domingo e segunda e enviamos as mensagens;
                    dataInicio = adicionaDiaPrimeiraHora(new Date(), 1);
                    dataFim = adicionaDiaUltimaHora(new Date(), 3);
                }

                System.out.println("DATA INICIO: " + dataInicio);
                System.out.println("DATA FIM: " + dataFim);

                //TODO tratar feriados;

                if (dataInicio != null && dataFim != null) {
                    agendamentos = AgendamentoSingleton.getInstance().getBo().listAgendamentosParaEnvioConfirmacaoAutomaticaPorData(dataInicio, dataFim);
                    if (agendamentos != null) {
                        for (Agendamento agendamento : agendamentos) {
                            Paciente paciente = agendamento.getPaciente();
                            Empresa empresa = EmpresaSingleton.getInstance().getBo().find(paciente.getIdEmpresa());
                            boolean retornoEnvio = HistoricoMensagemIntegracaoSingleton.getInstance().enviaMensagemAutomaticaParaCliente(paciente, agendamento, null, empresa);
                            if (retornoEnvio) {
                                //TODO transformar em log para salvar em tb to banco
                                System.out.println("MENSAGEM CONFIRMACAO: Mensagem enviada com sucesso para o agendamento id: " + agendamento.getId());
                            } else {
                                //TODO transformar em log para salvar em tb to banco
                                System.out.println("MENSAGEM CONFIRMACAO: Mensagem não enviada para o  agendamento id: " + agendamento.getId());
                            }
                        }
                    }
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        //TODO transformar em log para salvar em tb to banco
        System.out.println("MENSAGEM CONFIRMACAO: RODOU O ENVIO");
    }

    public static Date adicionaDiaPrimeiraHora(Date date, int days) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DATE, days);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        return cal.getTime();
    }

    public static Date adicionaDiaUltimaHora(Date date, int days) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DATE, days);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        return cal.getTime();
    }

}
