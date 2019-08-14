package br.com.lume.odonto.managed;

import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ActionEvent;

import org.apache.log4j.Logger;
import org.primefaces.PrimeFaces;

import br.com.lume.common.log.LogIntelidenteSingleton;
import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.evolucao.EvolucaoSingleton;
import br.com.lume.odonto.entity.Evolucao;
import br.com.lume.odonto.entity.Paciente;

@ManagedBean
@ViewScoped
public class EvolucaoMB extends LumeManagedBean<Evolucao> {

    private static final long serialVersionUID = 1L;
    private Logger log = Logger.getLogger(EvolucaoMB.class);

    @ManagedProperty(value = "#{pacienteMB}")
    private PacienteMB pacienteMB;

    public EvolucaoMB() {
        super(EvolucaoSingleton.getInstance().getBo());
        this.setClazz(Evolucao.class);
    }

    @Override
    public void actionPersist(ActionEvent event) {
        try {
            this.getEntity().setPaciente(this.pacienteMB.getEntity());         
            this.getEntity().setProfissional(UtilsFrontEnd.getProfissionalLogado());
            EvolucaoSingleton.getInstance().getBo().persist(this.getEntity());
            this.actionNew(event);
            
            this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");
            PrimeFaces.current().executeScript("PF('dlgEvolucao').hide();");
            
            atualizaEvolucao();
        } catch (Exception e) {
            this.log.error("Erro no actionPersist", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
        }
    }

    public void limpaEvolucoes() {
        this.setEntity(new Evolucao());
    }

    @Override
    public void actionNew(ActionEvent event) {
        this.setEntity(new Evolucao());
    }

    public PacienteMB getPacienteMB() {
        return this.pacienteMB;
    }

    public void setPacienteMB(PacienteMB pacienteMB) {
        this.pacienteMB = pacienteMB;
    }

    public void atualizaEvolucao() {
        try {
            setEntityList(EvolucaoSingleton.getInstance().getBo().listByPaciente(this.pacienteMB.getEntity()));
        } catch (Exception e) {
            LogIntelidenteSingleton.getInstance().makeLog(e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
        }
    }

}
