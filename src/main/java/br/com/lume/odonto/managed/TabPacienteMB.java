package br.com.lume.odonto.managed;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;

import org.apache.log4j.Logger;
import org.primefaces.component.tabview.TabView;
import org.primefaces.event.TabChangeEvent;

import br.com.lume.common.OdontoPerfil;
import br.com.lume.common.log.LogIntelidenteSingleton;
import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.JSFHelper;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.odonto.entity.Paciente;
import br.com.lume.odonto.entity.PlanoTratamento;
import br.com.lume.paciente.PacienteSingleton;
import br.com.lume.planoTratamento.PlanoTratamentoSingleton;

/**
 * @author ricardo.poncio
 */
@ManagedBean
@RequestScoped
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

    @ManagedProperty(value = "#{pacienteFinanceiroMB}")
    private PacienteFinanceiroMB pacienteFinanceiroMB;
    
    @ManagedProperty(value = "#{anotacoesMB}")
    private AnotacoesMB anotacoesMB;

    private org.primefaces.component.tabview.TabView tabview = null;   

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
            } catch (Exception e) {
                log.error("Erro no TabPacienteMB", e);
                this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            }
        }
    }

    public void onTabChange(TabChangeEvent event) {
        if ("Plano de Tratamento".equals(event.getTab().getTitle())) {
            planoTratamentoMB.carregarPlanosTratamento();
        }else if ("Anamnese".equals(event.getTab().getTitle())) {
            pacienteMB.carregarAnamneses();            
        }else if ("Odontograma".equals(event.getTab().getTitle())) {
            odontogramaMB.atualizaOdontograma();
        } else if ("Manuten????o Orto".equals(event.getTab().getTitle())) {
            ortodontiaMB.carregarTela();
        } else if ("Periograma".equals(event.getTab().getTitle())) {
            periogramaMB.carregarTela();
        } else if ("Evolu????o".equals(event.getTab().getTitle())) {
            //evolucaoMB.setPaciente(pacienteMB.getEntity());
            evolucaoMB.atualizaEvolucao();
        } else if ("Exames".equals(event.getTab().getTitle())) {
            exameMB.limpaExames();
        } else if ("Frequ??ncia".equals(event.getTab().getTitle())) {
            pacienteMB.carregarAgendamentos();
        } else if ("Financeiro".equals(event.getTab().getTitle())) {
            pacienteFinanceiroMB.setPaciente(getPacienteMB().getEntity());
            pacienteFinanceiroMB.setInicio(null);
            pacienteFinanceiroMB.setFim(null);
            pacienteFinanceiroMB.pesquisar();
        }else if("Anota????es".equals(event.getTab().getTitle())) {
            anotacoesMB.carregarAnotacoes(getPacienteMB().getEntity());
        }else if("Documentos".equals(event.getTab().getTitle())) {
            pacienteMB.carregarDocumentosPaciente();
        }
    }
    
//    public void refreshDadosPaciente() {
//        try {
//            if (pacienteMB.getEntity() != null && pacienteMB.getEntity().getId() != null && pacienteMB.getEntity().getId() > 0) {
//                Paciente paciente = getbO().find(pacienteMB.getEntity().getId());
//                getbO().refresh(paciente);
//                pacienteMB.setEntity(paciente);
//                List<PlanoTratamento> planosTratamento = planoTratamentoMB.getPlanosTratamento();
//                refreshPlanoTratamento(planosTratamento);
//                planosTratamento = odontogramaMB.getPlanosTratamento();
//                refreshPlanoTratamento(planosTratamento);
//                planosTratamento = ortodontiaMB.getPlanosTratamento();
//                refreshPlanoTratamento(planosTratamento);
//            }
//        } catch (Exception e) {
//            log.error("Erro no refreshDadosPaciente", e);
//            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
//        }
//    }

//    private void refreshPlanoTratamento(List<PlanoTratamento> planosTratamento) throws Exception {
//        if (planosTratamento != null && !planosTratamento.isEmpty()) {
//            for (PlanoTratamento planoTratamento : planosTratamento) {
//                PlanoTratamentoSingleton.getInstance().getBo().refresh(planoTratamento);
//            }
//        }
//    }

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

    public TabView getTabview() {
        return tabview;
    }

    public void setTabview(TabView tabview) {
        this.tabview = tabview;
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

    public PacienteFinanceiroMB getPacienteFinanceiroMB() {
        return pacienteFinanceiroMB;
    }

    public void setPacienteFinanceiroMB(PacienteFinanceiroMB pacienteFinanceiroMB) {
        this.pacienteFinanceiroMB = pacienteFinanceiroMB;
    }

    public int getIndexTabView() {
        return this.tabview.getActiveIndex();
    }

    public void loadPaciente(Paciente paciente) {
        try {
            Paciente aux = null;
            String idPaciente = (String) FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("paciente_selecionado");
            
            if(idPaciente != null && !idPaciente.isEmpty())
                aux = PacienteSingleton.getInstance().getBo().find(Long.valueOf(idPaciente));

            if(aux != null)
                paciente = aux;
            
            this.pacienteFinanceiroMB.setDisableFinanceiro(false);
            this.pacienteMB.setEntity(paciente);
            System.out.println(this.pacienteMB.getEntity().getDadosBasico().getDocumento());
            this.pacienteMB.setIndicacao(paciente.getIndicacaoDominio());
            this.tabview.setActiveIndex(0);
        } catch (Exception e) {
            e.printStackTrace();
            LogIntelidenteSingleton.getInstance().makeLog(e);
            this.addError("Erro ao visualizar paciente.", "Houve uma falha na busca pelos dados!");
        }
    }
    
    public void loadPacienteSemFinanceiro(Paciente paciente) {
        try {          
            this.pacienteFinanceiroMB.setDisableFinanceiro(true);
            this.pacienteMB.setEntity(paciente);
            this.tabview.setActiveIndex(0);            
        } catch (Exception e) {
            LogIntelidenteSingleton.getInstance().makeLog(e);
            this.addError("Erro ao visualizar paciente.", "Houve uma falha na busca pelos dados!");
        }
    }
    
    public void loadPacienteSemFinanceiro() {
        try {
            String idPaciente = (String) FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("paciente_selecionado");
            Paciente paciente = PacienteSingleton.getInstance().getBo().find(Long.valueOf(idPaciente));
            
            this.pacienteFinanceiroMB.setDisableFinanceiro(true);
            this.pacienteMB.setEntity(paciente);
            this.tabview.setActiveIndex(0);            
        } catch (Exception e) {
            LogIntelidenteSingleton.getInstance().makeLog(e);
            this.addError("Erro ao visualizar paciente.", "Houve uma falha na busca pelos dados!");
        }
    }
    
    public void loadPacienteNoFinanceiro(Paciente paciente) {
        try {
            this.pacienteMB.setEntity(paciente);
            pacienteFinanceiroMB.setPaciente(getPacienteMB().getEntity());
            pacienteFinanceiroMB.setInicio(null);
            pacienteFinanceiroMB.setFim(null);
            pacienteFinanceiroMB.pesquisar();

            this.tabview.setActiveIndex(8);
        } catch (Exception e) {
            LogIntelidenteSingleton.getInstance().makeLog(e);
            this.addError("Erro ao visualizar paciente.", "Houve uma falha na busca pelos dados!");
        }
    }

    public void loadPacienteRO(Paciente paciente, String namePanel) {
        try {
            if (UtilsFrontEnd.getProfissionalLogado().getPerfil().equals(OdontoPerfil.DENTISTA) && UtilsFrontEnd.getEmpresaLogada().isEmpBolDentistaAdmin() == false)
                this.pacienteMB.setVisivelDadosPaciente(false);
            this.pacienteMB.abreReadOnly(paciente, namePanel);
            this.tabview.setActiveIndex(0);
        } catch (Exception e) {
            LogIntelidenteSingleton.getInstance().makeLog(e);
            this.addError("Erro ao visualizar paciente.", "Houve uma falha na busca pelos dados!");
        }
    }

    
    public AnotacoesMB getAnotacoesMB() {
        return anotacoesMB;
    }

    
    public void setAnotacoesMB(AnotacoesMB anotacoesMB) {
        this.anotacoesMB = anotacoesMB;
    }
}
