/**
 *
 */
package br.com.lume.odonto.managed;

import java.util.Date;
import java.util.List;

import javax.faces.view.ViewScoped;
import javax.inject.Named;

import org.primefaces.PrimeFaces;

import br.com.lume.common.log.LogIntelidenteSingleton;
import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.Status;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.odonto.entity.PlanoTratamentoProcedimento;
import br.com.lume.odonto.entity.PlanoTratamentoProcedimentoCusto;
import br.com.lume.planoTratamentoProcedimento.PlanoTratamentoProcedimentoSingleton;
import br.com.lume.planoTratamentoProcedimentoCusto.PlanoTratamentoProcedimentoCustoSingleton;

@Named
@ViewScoped
public class PTPCustoDiretoMB extends LumeManagedBean<PlanoTratamentoProcedimento> {

    private static final long serialVersionUID = -1184686074982797243L;
    private List<PlanoTratamentoProcedimentoCusto> custos;

    public PTPCustoDiretoMB() {
        super(PlanoTratamentoProcedimentoSingleton.getInstance().getBo());
        this.setClazz(PlanoTratamentoProcedimento.class);
    }

    public void abreDialog(PlanoTratamentoProcedimento ptp) {
        setEntity(ptp);
        if(ptp.getPlanoTratamentoProcedimentoCustos() != null) {
            setCustos(ptp.getPlanoTratamentoProcedimentoCustos());
        }
        PrimeFaces.current().executeScript("PF('dlgPTPCustoDireto').show()");
    }

    public void validarCustosDiretos() {
        try {
            getEntity().setCustoDiretoValido(Status.SIM);
            getEntity().setDataCustoDiretoValidado(new Date());
            getEntity().setCustoDiretoValidadoPor(UtilsFrontEnd.getProfissionalLogado());
            PlanoTratamentoProcedimentoSingleton.getInstance().getBo().persist(getEntity());
            addInfo("Sucesso", Mensagens.getMensagemOffLine(Mensagens.REGISTRO_SALVO_COM_SUCESSO));
        } catch (Exception e) {
            LogIntelidenteSingleton.getInstance().makeLog(e);
            addError("Erro", Mensagens.getMensagemOffLine(Mensagens.ERRO_AO_SALVAR_REGISTRO));
        }
    }

    public List<PlanoTratamentoProcedimentoCusto> getCustos() {
        return custos;
    }

    public void setCustos(List<PlanoTratamentoProcedimentoCusto> custos) {
        this.custos = custos;
    }

}
