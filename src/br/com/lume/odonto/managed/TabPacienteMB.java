package br.com.lume.odonto.managed;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;

import org.apache.log4j.Logger;
import org.primefaces.PrimeFaces;
import org.primefaces.event.TabChangeEvent;

import br.com.lume.common.log.LogIntelidenteSingleton;
import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.JSFHelper;
import br.com.lume.common.util.Mensagens;
import br.com.lume.odonto.entity.Paciente;
import br.com.lume.odonto.entity.PlanoTratamento;
import br.com.lume.paciente.PacienteSingleton;
import br.com.lume.planoTratamento.PlanoTratamentoSingleton;

@ManagedBean
@ViewScoped
public class TabPacienteMB extends LumeManagedBean<Paciente> {

    private static final long serialVersionUID = -5181569186377576508L;

    private Logger log = Logger.getLogger(TabPacienteMB.class);

    @ManagedProperty(value = "#{pacienteMB}")
    private PacienteMB pacienteMB;

    @ManagedProperty(value = "#{odontogramaMB}")
    private OdontogramaMB odontogramaMB;

    @ManagedProperty(value = "#{ortodontiaMB}")
    private OrtodontiaMB ortodontiaMB;

    @ManagedProperty(value = "#{planoTratamentoMB}")
    private PlanoTratamentoMB planoTratamentoMB;

    @ManagedProperty(value = "#{periogramaMB}")
    private PeriogramaMB periogramaMB;

    @ManagedProperty(value = "#{exameMB}")
    private ExameMB exameMB;

    @ManagedProperty(value = "#{evolucaoMB}")
    private EvolucaoMB evolucaoMB;

    private Integer activeIndex;

    public TabPacienteMB() {
        super(PacienteSingleton.getInstance().getBo());
        this.setClazz(Paciente.class);
    }

    @PostConstruct
    public void processarParametros() {
        String idpt = JSFHelper.getRequestParameterMap("idpt");
        if (idpt != null) {
            try {
                PlanoTratamento pt = PlanoTratamentoSingleton.getInstance().getBo().find(new Long(idpt));
                pacienteMB.setEntity(pt.getPaciente());
                planoTratamentoMB.carregarPlanosTratamento();
                planoTratamentoMB.setEntity(pt);
                planoTratamentoMB.atualizaTela();
                setActiveIndex(4);
            } catch (Exception e) {
                log.error("Erro no TabPacienteMB", e);
                this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            }
        }
    }

    public void onTabChange(TabChangeEvent event) {
        if (event.getTab().getTitle().equals("Plano de Tratamento")) {
            planoTratamentoMB.carregarPlanosTratamento();
        } else if (event.getTab().getTitle().equals("Odontograma")) {
            odontogramaMB.atualizaOdontograma();
        } else if (event.getTab().getTitle().equals("Plano Ortodôntico")) {
            ortodontiaMB.carregarTela();
        } else if ("Periograma".equals(event.getTab().getTitle())) {
            periogramaMB.carregarTela();
        } else if ("Evolução".equals(event.getTab().getTitle())) {
            //evolucaoMB.setPaciente(pacienteMB.getEntity());
            evolucaoMB.atualizaEvolucao();
        } else if ("Exames".equals(event.getTab().getTitle())) {
            exameMB.limpaExames();
        } else if ("Frequência".equals(event.getTab().getTitle())) {
            pacienteMB.carregarAgendamentos();
        }
    }

    public void refreshDadosPaciente() {
        try {
            if (pacienteMB.getEntity() != null && pacienteMB.getEntity().getId() != null && pacienteMB.getEntity().getId() > 0) {
                Paciente paciente = getbO().find(pacienteMB.getEntity().getId());
                getbO().refresh(paciente);
                pacienteMB.setEntity(paciente);
                List<PlanoTratamento> planosTratamento = planoTratamentoMB.getPlanosTratamento();
                refreshPlanoTratamento(planosTratamento);
                planosTratamento = odontogramaMB.getPlanosTratamento();
                refreshPlanoTratamento(planosTratamento);
                planosTratamento = ortodontiaMB.getPlanosTratamento();
                refreshPlanoTratamento(planosTratamento);
            }
        } catch (Exception e) {
            log.error("Erro no refreshDadosPaciente", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
        }
    }

    private void refreshPlanoTratamento(List<PlanoTratamento> planosTratamento) throws Exception {
        if (planosTratamento != null && !planosTratamento.isEmpty()) {
            for (PlanoTratamento planoTratamento : planosTratamento) {
                PlanoTratamentoSingleton.getInstance().getBo().refresh(planoTratamento);
            }
        }
    }

    public PacienteMB getPacienteMB() {
        return pacienteMB;
    }

    public void setPacienteMB(PacienteMB pacienteMB) {
        this.pacienteMB = pacienteMB;
    }

    public PlanoTratamentoMB getPlanoTratamentoMB() {
        return planoTratamentoMB;
    }

    public void setPlanoTratamentoMB(PlanoTratamentoMB planoTratamentoMB) {
        this.planoTratamentoMB = planoTratamentoMB;
    }

    public OdontogramaMB getOdontogramaMB() {
        return odontogramaMB;
    }

    public void setOdontogramaMB(OdontogramaMB odontogramaMB) {
        this.odontogramaMB = odontogramaMB;
    }

    public OrtodontiaMB getOrtodontiaMB() {
        return ortodontiaMB;
    }

    public void setOrtodontiaMB(OrtodontiaMB ortodontiaMB) {
        this.ortodontiaMB = ortodontiaMB;
    }

    public Integer getActiveIndex() {
        return activeIndex;
    }

    public void setActiveIndex(Integer activeIndex) {
        this.activeIndex = activeIndex;
    }

    public ExameMB getExameMB() {
        return exameMB;
    }

    public void setExameMB(ExameMB exameMB) {
        this.exameMB = exameMB;
    }

    public EvolucaoMB getEvolucaoMB() {
        return evolucaoMB;
    }

    public void setEvolucaoMB(EvolucaoMB evolucaoMB) {
        this.evolucaoMB = evolucaoMB;
    }

    public PeriogramaMB getPeriogramaMB() {
        return periogramaMB;
    }

    public void setPeriogramaMB(PeriogramaMB periogramaMB) {
        this.periogramaMB = periogramaMB;
    }

    public void openFicha(Paciente paciente) {
        try {
            this.pacienteMB.setEntity(paciente);
            this.activeIndex = 0;
            PrimeFaces.current().executeScript("PF('dlgFichaPaciente').show();");
        } catch (Exception e) {
            LogIntelidenteSingleton.getInstance().makeLog(e);
            this.addError("Erro ao abrir a ficha.", "Houve uma falha na busca pelos dados!");
        }
    }

}
