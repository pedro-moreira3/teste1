package br.com.lume.odonto.queue;

import java.io.Serializable;
import java.util.TimerTask;
import br.com.lume.execucaoTimer.interfaces.Executor;
import br.com.lume.security.EmpresaSingleton;


public class EnvioMensagensConfirmacao extends TimerTask implements Serializable, Executor {

    private static final long serialVersionUID = 1L;

    public String getCodigo() {
        return "ENVIO_MENSAGEM_CONFIRMACAO";
    }

    @Override
    public void run() {
        try {
    //        EmpresaSingleton.getInstance().getBo().listEmpresasComPacotesMensagens();
        //
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
