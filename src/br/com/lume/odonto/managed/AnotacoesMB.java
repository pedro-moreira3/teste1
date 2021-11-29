package br.com.lume.odonto.managed;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ActionEvent;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.primefaces.PrimeFaces;

import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.odonto.entity.Paciente;
import br.com.lume.odonto.entity.PacienteAnotacao;
import br.com.lume.odonto.entity.Profissional;
import br.com.lume.paciente.PacienteSingleton;
import br.com.lume.pacienteAnotacao.PacienteAnotacaoSingleton;
import br.com.lume.planoTratamento.PlanoTratamentoSingleton;
import br.com.lume.profissional.ProfissionalSingleton;

@ManagedBean
@ViewScoped
public class AnotacoesMB extends LumeManagedBean<PacienteAnotacao> {

    private static final long serialVersionUID = 1L;
    private Logger log = Logger.getLogger(AnotacoesMB.class);

    private PacienteAnotacao anotacaoSelecionada;

    @ManagedProperty(value = "#{pacienteMB}")
    private PacienteMB pacienteMB;

    public AnotacoesMB() {
        super(PacienteAnotacaoSingleton.getInstance().getBo());
        this.setClazz(PacienteAnotacao.class);

    }

    @Override
    public void actionNew(ActionEvent event) {
        this.setEntity(new PacienteAnotacao());
    }

    @Override
    public void actionPersist(ActionEvent event) {
        if (!getEntity().getDescricao().equals("")) {
            getEntity().setData(Calendar.getInstance().getTime());
            getEntity().setProfissional(UtilsFrontEnd.getProfissionalLogado());
            getEntity().setPaciente(this.pacienteMB.getEntity());
            pacienteMB.getEntity().getPacienteAnotacoes().add(getEntity());
            pacienteMB.actionPersist(null);
            PrimeFaces.current().executeScript("PF('dlgNovaAnotacao').hide();");
            atualizaAnotacoes();
        } else {
            addWarn("Atenção!", "A descrição não pode estar vazia.");
        }

    }

    public void closeDlg() {
        setEntity(new PacienteAnotacao());
    }

    private void atualizaAnotacoes() {
        this.setEntityList(PacienteAnotacaoSingleton.getInstance().getBo().listByPaciente(this.pacienteMB.getEntity()));
    }

    public void visualizaAnotacao(PacienteAnotacao anotacao) {
        this.anotacaoSelecionada = anotacao;
    }
    
    public static void main(String[] args) {
        atualizaAnotacaoBD();
    }
    
    public static void atualizaAnotacaoBD() {
        PacienteAnotacao pan = new PacienteAnotacao();
        List<Paciente> pacientes = new ArrayList<>();
        List<Profissional> profs = new ArrayList<>();
        Date data = Calendar.getInstance().getTime();
        
        
        try {
            pacientes = PacienteSingleton.getInstance().getBo().listByAnotacaoNotnull();
            for(Paciente p : pacientes) {
                pan = new PacienteAnotacao();
                pan.setDescricao(Jsoup.parse(p.getAnotacoes()).text());
                pan.setData(data);
                pan.setPaciente(p);
                pan.setProfissional(null);
                
                profs = ProfissionalSingleton.getInstance().getBo().listByEmpresaAndAtivo(p.getIdEmpresa());
                
                
                
                PacienteAnotacaoSingleton.getInstance().getBo().persist(pan);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }

    public PacienteAnotacao getAnotacaoSelecionada() {
        return anotacaoSelecionada;
    }

    public void setAnotacaoSelecionada(PacienteAnotacao anotacaoSelecionada) {
        this.anotacaoSelecionada = anotacaoSelecionada;
    }

    public PacienteMB getPacienteMB() {
        return pacienteMB;
    }

    public void setPacienteMB(PacienteMB pacienteMB) {
        this.pacienteMB = pacienteMB;
    }

}
