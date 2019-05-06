package br.com.lume.odonto.managed;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ActionEvent;

import org.apache.log4j.Logger;

import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.Status;
import br.com.lume.configuracao.Configurar;
import br.com.lume.odonto.entity.Paciente;
import br.com.lume.odonto.entity.Retorno;
import br.com.lume.paciente.PacienteSingleton;
import br.com.lume.retorno.RetornoSingleton;

@ManagedBean
@ViewScoped
public class RetornoMB extends LumeManagedBean<Retorno> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(RetornoMB.class);

    private List<Retorno> retornos = new ArrayList<>();

    private Date dataIni, dataFim;

    public RetornoMB() {
        super(RetornoSingleton.getInstance().getBo());
        Calendar cal = Calendar.getInstance();
        dataIni = cal.getTime();
        cal.add(Calendar.MONTH, 12);
        dataFim = cal.getTime();
        this.geraLista();
        this.setClazz(Retorno.class);
    }

    public void geraLista() {
        try {
            retornos = RetornoSingleton.getInstance().getBo().listByDate(dataIni, dataFim);
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
        }
    }

    public List<Paciente> geraSugestoesPaciente(String query) {
        return PacienteSingleton.getInstance().getBo().listSugestoesComplete(query,Configurar.getInstance().getConfiguracao().getProfissionalLogado().getIdEmpresa());
    }

    public void persistRetorno(Retorno r) {
        try {
            RetornoSingleton.getInstance().getBo().persist(r);
        } catch (Exception e) {
            log.error("Erro no actionPersist", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
        }
    }

    @Override
    public void actionRemove(ActionEvent event) {
        try {
            this.getbO().remove(this.getEntity());
            this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_REMOVIDO_COM_SUCESSO), "");
            this.actionNew(event);
            this.geraLista();
        } catch (Exception e) {
            log.error("Erro no actionRemove", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_REMOVER_REGISTRO), "");
        }

    }

    @Override
    public void actionPersist(ActionEvent event) {
        super.actionPersist(event);
        this.geraLista();
    }

    public List<Retorno> getRetornos() {
        return retornos;
    }

    public void setRetornos(List<Retorno> retornos) {
        this.retornos = retornos;
    }

    public boolean isObrigatorio() {
        return this.getEntity().getRetornar().equals(Status.SIM);
    }

    public Date getDataIni() {
        return dataIni;
    }

    public void setDataIni(Date dataIni) {
        this.dataIni = dataIni;
    }

    public Date getDataFim() {
        return dataFim;
    }

    public void setDataFim(Date dataFim) {
        this.dataFim = dataFim;
    }

}
