package br.com.lume.odonto.managed;

import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ActionEvent;

import org.apache.log4j.Logger;

import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.evolucao.EvolucaoSingleton;
//import br.com.lume.odonto.bo.EvolucaoBO;
import br.com.lume.odonto.entity.Evolucao;
import br.com.lume.odonto.entity.Paciente;

@ManagedBean
@ViewScoped
public class EvolucaoMB extends LumeManagedBean<Evolucao> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(EvolucaoMB.class);

    @ManagedProperty(value = "#{pacienteMB}")
    private PacienteMB pacienteMB;

    private List<Evolucao> evolucoes;

    private Evolucao evolucao;

    private Paciente pacienteAnterior;

//    private EvolucaoBO evolucaoBO;

    public EvolucaoMB() {
        super(EvolucaoSingleton.getInstance().getBo());
     //   this.evolucaoBO = new EvolucaoBO();
        this.setClazz(Evolucao.class);
    }

    @Override
    public void actionPersist(ActionEvent event) {
        try {
            this.getEntity().setPaciente(this.pacienteMB.getEntity());         
            this.getEntity().setProfissional(UtilsFrontEnd.getProfissionalLogado());
            EvolucaoSingleton.getInstance().getBo().persist(this.getEntity());
            this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");
            this.actionNew(event);
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
        this.pacienteAnterior = null;
        this.setEntity(new Evolucao());
    }

    public PacienteMB getPacienteMB() {
        return this.pacienteMB;
    }

    public void setPacienteMB(PacienteMB pacienteMB) {
        this.pacienteMB = pacienteMB;
    }

    public void setEvolucao(Evolucao evolucao) {
        this.evolucao = evolucao;
    }

    public List<Evolucao> getEvolucoes() {
        Paciente pacienteAtual = this.pacienteMB.getEntity();
        if (this.pacienteAnterior == null || this.pacienteAnterior.getId() != pacienteAtual.getId()) {
            this.pacienteAnterior = pacienteAtual;
            if (this.pacienteMB != null && this.pacienteMB.getEntity() != null) {
                this.evolucoes = EvolucaoSingleton.getInstance().getBo().listByPaciente(this.pacienteMB.getEntity());
            }
        }
        return this.evolucoes;
    }

    public void setEvolucoes(List<Evolucao> evolucoes) {
        this.evolucoes = evolucoes;
    }

    public Evolucao getEvolucao() {
        return this.evolucao;
    }
}
