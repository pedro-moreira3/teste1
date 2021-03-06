package br.com.lume.odonto.managed;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import javax.faces.event.ActionEvent;
import javax.faces.push.Push;
import javax.faces.push.PushContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.log4j.Logger;
import org.primefaces.PrimeFaces;
import org.primefaces.event.CloseEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.DefaultScheduleEvent;
import org.primefaces.model.DualListModel;
import org.primefaces.model.LazyScheduleModel;
import org.primefaces.model.ScheduleEvent;
import org.primefaces.model.ScheduleModel;

import br.com.lume.afastamento.AfastamentoSingleton;
import br.com.lume.agendamento.AgendamentoSingleton;
import br.com.lume.agendamentoPlanoTratamentoProcedimento.AgendamentoPlanoTratamentoProcedimentoSingleton;
import br.com.lume.common.OdontoPerfil;
import br.com.lume.common.exception.business.BusinessException;
import br.com.lume.common.exception.business.ServidorEmailDesligadoException;
import br.com.lume.common.exception.business.UsuarioDuplicadoException;
import br.com.lume.common.exception.techinical.TechnicalException;
import br.com.lume.common.log.LogIntelidenteSingleton;
import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.GeradorSenha;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.Status;
import br.com.lume.common.util.StatusAgendamentoUtil;
import br.com.lume.common.util.Utils;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.conta.ContaSingleton;
import br.com.lume.convenio.ConvenioSingleton;
import br.com.lume.dadosBasico.DadosBasicoSingleton;
import br.com.lume.dominio.DominioSingleton;
import br.com.lume.horasUteisProfissional.HorasUteisProfissionalSingleton;
import br.com.lume.odonto.entity.Afastamento;
import br.com.lume.odonto.entity.Agendamento;
import br.com.lume.odonto.entity.AgendamentoPlanoTratamentoProcedimento;
import br.com.lume.odonto.entity.Convenio;
import br.com.lume.odonto.entity.Dominio;
import br.com.lume.odonto.entity.HorasUteisProfissional;
import br.com.lume.odonto.entity.Paciente;
import br.com.lume.odonto.entity.PlanoTratamento;
import br.com.lume.odonto.entity.PlanoTratamentoProcedimento;
import br.com.lume.odonto.entity.Profissional;
import br.com.lume.odonto.entity.Reserva;
import br.com.lume.odonto.entity.Retorno;
import br.com.lume.odonto.entity.Retorno.StatusRetorno;
import br.com.lume.odonto.exception.TelefoneException;
import br.com.lume.odonto.util.OdontoMensagens;
import br.com.lume.orcamento.OrcamentoSingleton;
import br.com.lume.origemAgendamento.OrigemAgendamentoSingleton;
import br.com.lume.paciente.PacienteSingleton;
import br.com.lume.planoTratamento.PlanoTratamentoSingleton;
import br.com.lume.planoTratamentoProcedimento.PlanoTratamentoProcedimentoSingleton;
import br.com.lume.profissional.ProfissionalSingleton;
import br.com.lume.reserva.ReservaSingleton;
import br.com.lume.retorno.RetornoSingleton;
import br.com.lume.security.PerfilSingleton;
import br.com.lume.security.UsuarioSingleton;
import br.com.lume.security.entity.Perfil;
import br.com.lume.security.entity.Usuario;
import br.com.lume.security.validator.GenericValidator;

@Named
@ViewScoped
public class AgendamentoMB extends LumeManagedBean<Agendamento> {

    private static final long serialVersionUID = -4445481223043257997L;

    private Logger log = Logger.getLogger(AgendamentoMB.class);

    private ScheduleModel schedule;

    private ScheduleModel scheduleProfissional;

    private Profissional profissional, profissionalDentroAgenda;
    private Integer cadeiraDentroAgenda;

    private List<Profissional> profissionais;

    private List<Convenio> convenios;

    private Integer tempoConsulta;

    private List<Agendamento> agendamentos, agendamentosAfastamento = new ArrayList<>();

    private Date inicio, fim;

    private Date initialDateConsult;

    private Date chegouAsDentroAgenda;

    private Date iniciouAsDentroAgenda;

    private Date finalizouAsDentroAgenda;

    private Paciente pacienteSelecionado, pacientePesquisado, paciente = new Paciente();
    private Paciente pacienteConflito;
    private List<Paciente> pacientesConflito;

    private PlanoTratamento planoTratamentoSelecionado;

    private List<PlanoTratamento> planoTratamentos;

    private String mensagemWhats;

    private boolean visivel = false, horaUtilValida, responsavel = false, mostraFinalizados = false, telefonesVisiveis = false;

    private DualListModel<AgendamentoPlanoTratamentoProcedimento> procedimentosPickList = new DualListModel<>();

    private Dominio justificativa;

    private String status;

    private int qtdProfissionais;

    private String gmt;

    private List<HorasUteisProfissional> horasUteisProfissional;

    private List<HorasUteisProfissional> horasUteisProfissionalAgenda;

    private String observacoes;

    private List<Integer> cadeiras;

    private boolean habilitaSalvar = true;

    private List<String> filtroAgendamento = new ArrayList<>();

    private boolean visivelDadosPaciente = true;

    private Retorno retorno;

    private LocalDate initialDate;

    //S - Scheduler, C - Cadeiras, P - Profissional
    private String visualizacao = "S";

    private boolean checkFiltro = false;

    //FILTROS E LISTAS DISPONIBILIDADE DO PROFISSIONAL
    private List<Profissional> profissionaisDisponiveis;
    private Profissional profissionalDisponivel;
    private List<Agendamento> agendamentosDisponiveis;
    private Date dataAgendamentoInicial;
    private Date dataAgendamentoFinal;
    private List<Afastamento> afastamentos = new ArrayList<>();
    private boolean agendamentoRapido = false;

    private String statusColorBlack[] = { "P", "D", "O", "H", "F" };

    private Date chegouAsEstadoInicial = null;

    private Date iniciouAsEstadoInicial = null;

    private Date finalizaouAsEstadoInicial = null;

    @Inject
    @Push
    private PushContext someChannel;

    @Inject
    @Push
    private PushContext canalAgendamentoRapido;

    private String idEmpresaParaSocket;

    public AgendamentoMB() {
        super(AgendamentoSingleton.getInstance().getBo());
        if (UtilsFrontEnd.getProfissionalLogado() != null) {
            idEmpresaParaSocket = "" + UtilsFrontEnd.getProfissionalLogado().getIdEmpresa();
        }

        this.setClazz(Agendamento.class);
        try {
            if (UtilsFrontEnd.getProfissionalLogado() != null) {
                if (UtilsFrontEnd.getProfissionalLogado().getPerfil().equals(OdontoPerfil.DENTISTA) && UtilsFrontEnd.getEmpresaLogada().isEmpBolDentistaAdmin() == false) {
                    visivelDadosPaciente = false;
                }

                gmt = DominioSingleton.getInstance().getBo().findByEmpresaAndObjetoAndTipoAndValor("agendamento", "hora", "GM").getNome();
                this.carregarProfissionais();

                qtdProfissionais = profissionais.size();
                tempoConsulta = UtilsFrontEnd.getProfissionalLogado().getTempoConsulta();
                carregarCadeiras();
                filtroAgendamento.addAll(Arrays.asList("F", "A", "I", "S", "O", "E", "B", "N", "P", "G", "H", "C"));
                initialDate = LocalDate.now();
                convenios = ConvenioSingleton.getInstance().getBo().listByEmpresa(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());

                this.profissionaisDisponiveis = new ArrayList<Profissional>();
            }
        } catch (Exception e) {
            log.error(e);
            e.printStackTrace();
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
        }
        paciente = new Paciente();
        if (this.isDentista()) {
            profissional = UtilsFrontEnd.getProfissionalLogado();
        } else {
            profissional = null;
        }
        carregarScheduleTarefas();
    }

    public List<Profissional> sugestoesProfissionais(String query) {
        return ProfissionalSingleton.getInstance().getBo().listSugestoesCompleteDentista(query, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa(), false);
    }

    public void setVideos() {
        getListaVideosTutorial().clear();
        getListaVideosTutorial().put("Como fazer um agendamento", "https://www.youtube.com/v/9MqXHL6urTk?autoplay=1");
        getListaVideosTutorial().put("Atualizando informa????es de agendamento", "https://www.youtube.com/v/Jw48ioxUdOg?autoplay=1");
    }

    public void carregarAgenda() {
        //limpaPacienteSelecionado();
        carregarScheduleTarefas();
        this.horasUteisProfissional = getHorasUteisProfissional();
        //PrimeFaces.current().ajax().update(":lume:dadosFiltroAg");
        //PrimeFaces.current().ajax().update(":lume:schedule");
    }

    public void limparDlgMensagem() {
        this.mensagemWhats = "";
    }

    public void enviarMensagem() {
        String url = this.getUrlMessage(this.mensagemWhats, this.getEntity().getPaciente());
        PrimeFaces.current().executeScript("window.open('" + url + "');");
    }

    public void carregarMensagemPadrao() {
        this.mensagemWhats = OdontoMensagens.getMensagem("whatsapp.defaultmessage.agendamento");
    }

    public void atualizaPacientePosFicha() {
        try {
            pacienteSelecionado = PacienteSingleton.getInstance().getBo().find(pacienteSelecionado);
            PrimeFaces.current().ajax().update(":lume:paciente");

            planoTratamentos = new ArrayList<>();
            planoTratamentos = PlanoTratamentoSingleton.getInstance().getBo().listByPaciente(pacienteSelecionado);
            PrimeFaces.current().ajax().update(":lume:planoTratamento");
        } catch (Exception e) {
            addError("Erro", "Falha ao atualizar paciente selecionado no agendamento!");
        }
    }

    public void popularAgendamento(Agendamento ag) {
        Agendamento agendamento = ag;
        agendamento.setId(0);
        this.setEntity(agendamento);
        profissionalDentroAgenda = this.getEntity().getProfissional();
        cadeiraDentroAgenda = this.getEntity().getCadeira();
        chegouAsDentroAgenda = this.getEntity().getChegouAs();
        iniciouAsDentroAgenda = this.getEntity().getIniciouAs();
        finalizouAsDentroAgenda = this.getEntity().getFinalizouAs();

        UtilsFrontEnd.setPacienteSelecionado(this.getEntity().getPaciente());
        this.setEntity(this.getEntity());

        this.setInicio(this.getEntity().getInicio());
        this.setFim(this.getEntity().getFim());
        geraAgendamentoAfastamento(this.getEntity().getInicio(), this.getEntity().getFim(), profissionalDentroAgenda);
        this.setPacienteSelecionado(this.getEntity().getPaciente());
        this.setJustificativa(DominioSingleton.getInstance().getBo().findByEmpresaAndObjetoAndTipoAndNome("agendamento", "justificativa", this.getEntity().getJustificativa()));
        this.setStatus(this.getEntity().getStatusNovo());
        this.validaAfastamento();

        if (this.getEntity().getPlanoTratamentoProcedimentosAgendamento() != null && this.getEntity().getPlanoTratamentoProcedimentosAgendamento().size() > 0) {
            this.planoTratamentoSelecionado = this.getEntity().getPlanoTratamentoProcedimentosAgendamento().get(0).getPlanoTratamentoProcedimento().getPlanoTratamento();
            this.procedimentosPickList.setSource(this.getEntity().getPlanoTratamentoProcedimentosAgendamento());
        }

        validaHabilitaSalvar();
        this.validaHoraUtilProfissional(profissionalDentroAgenda);

        PrimeFaces.current().ajax().update(":lume:pnAgendamento");

    }

    public void retorno(Retorno r) {
        if (r.getAgendamento() != null) {
            addError("Erro", "Retorno j?? possui agendamento. pesquise novamente para verificar");
            return;
        }
        if (r != null) {
            pacienteSelecionado = r.getPaciente();
            retorno = r;
            profissional = null;
            PrimeFaces.current().executeScript("PF('eventDialog').show()");
            PrimeFaces.current().ajax().update(":lume:pnAgendamento");
        }
    }

    public void relatorio(Agendamento agendamento) {
        if (agendamento != null) {
            pacienteSelecionado = agendamento.getPaciente();
            profissional = null;
        }
    }

    public void filaAtendimento(Agendamento a) {
        if (a != null) {
            profissional = a.getProfissional();
            profissionalDentroAgenda = profissional;
            setPacienteSelecionado(a.getPaciente());
        } else {
            profissional = null;
            profissionalDentroAgenda = profissional;
            pacienteSelecionado = null;
        }
        atualizaPickList();
        this.cadeiraDentroAgenda = null;
    }

    public void agendamentoRapido(Agendamento a) {
        setAgendamentoRapido(true);
        setEntity(a);
        profissional = a.getProfissional();
        profissionalDentroAgenda = profissional;

        this.setInicio(this.getEntity().getInicio());
        this.setFim(this.getEntity().getFim());

        if (a.getPaciente() != null) {
            setPacienteSelecionado(a.getPaciente());

            cadeiraDentroAgenda = this.getEntity().getCadeira();
            chegouAsDentroAgenda = this.getEntity().getChegouAs();
            iniciouAsDentroAgenda = this.getEntity().getIniciouAs();
            finalizouAsDentroAgenda = this.getEntity().getFinalizouAs();

            UtilsFrontEnd.setPacienteSelecionado(this.getEntity().getPaciente());
            this.setEntity(this.getEntity());

            geraAgendamentoAfastamento(this.getEntity().getInicio(), this.getEntity().getFim(), profissionalDentroAgenda);
            // this.setPacienteSelecionado(this.getEntity().getPaciente());
            this.setJustificativa(DominioSingleton.getInstance().getBo().findByEmpresaAndObjetoAndTipoAndNome("agendamento", "justificativa", this.getEntity().getJustificativa()));
            this.setStatus(this.getEntity().getStatusNovo());
            //this.validaAfastamento();

            if (this.getEntity().getPlanoTratamentoProcedimentosAgendamento() != null && this.getEntity().getPlanoTratamentoProcedimentosAgendamento().size() > 0) {
                this.planoTratamentoSelecionado = this.getEntity().getPlanoTratamentoProcedimentosAgendamento().get(0).getPlanoTratamentoProcedimento().getPlanoTratamento();
                this.procedimentosPickList.setSource(this.getEntity().getPlanoTratamentoProcedimentosAgendamento());
            }

            //   validaHabilitaSalvar();
            //  this.validaHoraUtilProfissional(profissionalDentroAgenda);
            observacoes = getEntity().getDescricao();

        } else {
            pacienteSelecionado = null;
            observacoes = "";
        }
        PrimeFaces.current().ajax().update(":lume:pnAgendamento");
        atualizaPickList();
    }

    public void agendamentoRapidoEncaixe(Date data, Profissional p) {
        pacienteSelecionado = null;
        Agendamento agendamento = new Agendamento();
        this.setInicio(data);
        this.setFim(data);
        agendamento.setStatusNovo("E");
        agendamento.setInicio(data);
        agendamento.setFim(data);
        observacoes = agendamento.getDescricao();
        setEntity(agendamento);

        if (p != null) {
            profissionalDentroAgenda = p;
            observacoes = "";
        }
        PrimeFaces.current().ajax().update(":lume:pnAgendamento");
        atualizaPickList();
    }

    public void agendamentoRapidoBloqueio(Profissional p) {
        setEntity(new Agendamento());

        if (p != null) {
            setProfissional(p);
            profissionalDentroAgenda = p;
            PrimeFaces.current().executeScript("PF('dlgBloqueio').show()");
            PrimeFaces.current().executeScript("resize('#lume\\\\:dlgBloqueio')");
            PrimeFaces.current().ajax().update(":lume:pnAgendamento");
        } else {
            this.addError("Selecione o profissional antes de criar um novo bloqueio", "");
        }

    }

    public void agendarNoRelatorioRelacionamento(long pac) {
        try {
            onDateSelect(null);
            profissional = UtilsFrontEnd.getProfissionalLogado();
            profissionalDentroAgenda = UtilsFrontEnd.getProfissionalLogado();

            Paciente paciente = PacienteSingleton.getInstance().getBo().find(pac);

            setPacienteSelecionado(paciente);
            atualizaPickList();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isReserva() {
        try {
            List<Reserva> reservas = ReservaSingleton.getInstance().getBo().listByAgendamento(getEntity(), UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
            return reservas != null && !reservas.isEmpty();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private void carregarCadeiras() {
        cadeiras = new ArrayList<>();
        for (int i = 1; i <= UtilsFrontEnd.getEmpresaLogada().getEmpIntCadeira(); i++) {
            cadeiras.add(i);
        }
    }

    private void carregarProfissionais() throws Exception {
        List<String> perfis = new ArrayList<>();
        perfis.add(OdontoPerfil.DENTISTA);
        perfis.add(OdontoPerfil.ADMINISTRADOR);
        perfis.add(OdontoPerfil.RESPONSAVEL_TECNICO);
        profissionais = ProfissionalSingleton.getInstance().getBo().listByEmpresa(perfis, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
    }

    public void validaIdade() {
        Calendar dataNasc = Calendar.getInstance();
        dataNasc.add(Calendar.YEAR, -18);
        Calendar dataAtual = Calendar.getInstance();
        dataAtual.setTime(paciente.getDadosBasico().getDataNascimento());
        if (dataNasc.before(dataAtual)) {
            responsavel = true;
        } else {
            responsavel = false;
        }
    }

    public String agendamentoRetorno(Retorno retorno) {
        this.actionNew(null);
        if (retorno.getPlanoTratamento() != null) {
            this.setProfissional(retorno.getPlanoTratamento().getProfissional());
        }
        return "agendamento.jsf";
    }

    public List<Paciente> geraSugestoes(String query) {
        try {
            return PacienteSingleton.getInstance().getBo().listSugestoesComplete(query, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa(),false);
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            e.printStackTrace();
        }
        return null;
    }

    public void handleClose() {
        handleClose(null);
    }

    public void handleClose(CloseEvent event) {
        this.actionNew(null);
        // profissional = null;
        profissionalDentroAgenda = null;
        visivel = false;
    }

    @Override
    public void actionNew(ActionEvent arg0) {
        inicio = null;
        fim = null;
        this.setEntity(new Agendamento());
        this.setPacienteSelecionado(null);
        this.setPlanoTratamentos(null);
        this.setPlanoTratamentoSelecionado(null);
        this.setProcedimentosPickList(new DualListModel<AgendamentoPlanoTratamentoProcedimento>());
    }

    @Override
    public void actionPersist(ActionEvent event) {

        if ((procedimentosPickList.getSource().isEmpty() && procedimentosPickList.getTarget().isEmpty() && planoTratamentoSelecionado == null) || (!procedimentosPickList.getTarget().isEmpty() && planoTratamentoSelecionado != null)) {

            //itens para adicionar
            List<AgendamentoPlanoTratamentoProcedimento> paraInserir = new ArrayList<>();
            for (AgendamentoPlanoTratamentoProcedimento aptpNovo : this.insereAgendamento(procedimentosPickList.getTarget())) {
                aptpNovo.setAgendamento(getEntity());
                paraInserir.add(aptpNovo);
            }
            //itens para marcar como inativo
            List<AgendamentoPlanoTratamentoProcedimento> agendamentoPlanosTratamentoExistentes = this.getEntity().getPlanoTratamentoProcedimentosAgendamento();
            for (AgendamentoPlanoTratamentoProcedimento aptpExistente : agendamentoPlanosTratamentoExistentes) {
                if (!paraInserir.contains(aptpExistente)) {
                    aptpExistente.setAtivo(false);
                }

                paraInserir.add(aptpExistente);

            }

            this.getEntity().setPlanoTratamentoProcedimentosAgendamento(paraInserir);

            this.getEntity().setPlanoTratamento(planoTratamentoSelecionado);
            if (this.getEntity().getStatusNovo().equals("")) {
                this.getEntity().setStatusNovo(status);
            }

            this.getEntity().setChegouAs(chegouAsDentroAgenda);
            this.getEntity().setIniciouAs(iniciouAsDentroAgenda);
            this.getEntity().setFinalizouAs(finalizouAsDentroAgenda);
            if (this.validacoes()) {
                getEntity().setCadeira(cadeiraDentroAgenda);
                if (this.getEntity().getId() == 0) {
                    this.getEntity().setProfissional(profissionalDentroAgenda);
                    this.getEntity().setPaciente(pacienteSelecionado);
                }
                if (getEntity().getDataAgendamento() == null) {
                    this.getEntity().setDataAgendamento(new Date());
                }
                if (this.getEntity().getAgendador() == null) {
                    this.getEntity().setAgendador(UtilsFrontEnd.getProfissionalLogado());
                }

                if (this.getEntity().getStatusNovo().equals(StatusAgendamentoUtil.CANCELADO.getSigla())) {
                    if (justificativa != null) {
                        this.getEntity().setJustificativa(justificativa.getNome());
                    }
                } else {
                    this.getEntity().setJustificativa(null);
                }
                if (this.getEntity().getStatusNovo().equals(StatusAgendamentoUtil.REMARCADO.getSigla()) && validaRangeDatasRemarcado()) {
                    this.refreshEntity();
                    this.addError(OdontoMensagens.getMensagem("agendamento.remarcado.dataigual"), "");
                    //dlg = false;
                } else {
                    if (!this.isRemarcado()) {
                        this.getEntity().setHash(GeradorSenha.gerarSenha());
                        this.getEntity().setInicio(this.getInicio());
                        this.fim = this.fim;
                        this.getEntity().setFim(this.getFim());
                        if (this.getEntity().getStatusNovo().equals(StatusAgendamentoUtil.ENCAIXE.getSigla())) {
                            this.getEntity().setEncaixe(Status.SIM);
                        }
                        if (getObservacoes() != null)
                            getEntity().setDescricao(getObservacoes());
                        try {
                            if (profissionalDentroAgenda.getId() != getEntity().getProfissional().getIdUsuario()) {
                                getEntity().setProfissional(profissionalDentroAgenda);
                                List<Reserva> reservas = ReservaSingleton.getInstance().getBo().listByAgendamento(getEntity(), UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
                                if (reservas != null && !reservas.isEmpty()) {
                                    for (Reserva reserva : reservas) {
                                        reserva.setProfissional(profissionalDentroAgenda);
                                    }
                                    ReservaSingleton.getInstance().getBo().mergeBatch(reservas);
                                }
                            }
                            if (getEntity().getPlanoTratamento() != null) {
                                this.getEntity().setConvenioPaciente(getEntity().getPlanoTratamento().getConvenio());
                            } else if (getEntity().getPaciente().getConvenio() != null) {
                                this.getEntity().setConvenioPaciente(getEntity().getPaciente().getConvenio());
                            }
                            AgendamentoSingleton.getInstance().getBo().persist(this.getEntity(), UtilsFrontEnd.getProfissionalLogado(), UtilsFrontEnd.getEmpresaLogada().getEmpStrEstoque(),
                                    UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());

                            //salvando dentista executor, pois ja sabemos quem deve receber. somente se ptp nao estiver finalizado.
                            for (AgendamentoPlanoTratamentoProcedimento aptp : this.getEntity().getPlanoTratamentoProcedimentosAgendamento()) {
                                if (aptp.getPlanoTratamentoProcedimento() != null && aptp.getPlanoTratamentoProcedimento().getDataFinalizado() == null) {

                                    if (aptp.getPlanoTratamentoProcedimento().getDentistaExecutor() == null || !profissionalDentroAgenda.equals(
                                            aptp.getPlanoTratamentoProcedimento().getDentistaExecutor()) || "N??o Atendido".equals(this.getEntity().getStatusAgendamento().getDescricao())) {

                                        PlanoTratamentoProcedimentoSingleton.getInstance().alteraDentistaExecutor(aptp.getPlanoTratamentoProcedimento(), profissionalDentroAgenda);
                                        PlanoTratamentoProcedimentoSingleton.getInstance().verificaRepasses(aptp.getPlanoTratamentoProcedimento(), UtilsFrontEnd.getProfissionalLogado());

                                    }
                                }

                            }

                            if (retorno != null) {
                                retorno.setAgendamento(getEntity());
                                retorno.setRetornar(StatusRetorno.AGENDADO);
                                RetornoSingleton.getInstance().getBo().persist(retorno);
                            }

//                            try {
//                                RepasseFaturasSingleton.getInstance().verificaAgendamentoRepasse(getEntity(), UtilsFrontEnd.getProfissionalLogado());
//                            } catch (Exception e) {
//                                // Log...
//                            }

                            this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");
                            someChannel.send(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
                            //TODO melhorar socket para atualizar somente quando necessario, ou seja, somente quando 
                            //o profissional salvo aqui tiver na tela do agendamento rapido.
                            //    canalAgendamentoRapido.send(profissionalDentroAgenda.getDadosBasico().getNome());

                            profissionalDentroAgenda = null;

                            this.actionNew(event);
                            //PrimeFaces.current().ajax().addCallbackParam("dlg", true);
                            PrimeFaces.current().executeScript("PF('eventDialog').hide()");
                            //profissional = null;
                        } catch (BusinessException e) {
                            this.addError(OdontoMensagens.getMensagem("erro.agendamento.exclusao.procedimento.emprestimo"), "");
                            log.error("actionPersist", e);
                        } catch (Exception e) {
                            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
                            log.error("actionPersist", e);
                        }
                    } else {
                        this.getEntity().setStatusNovo(StatusAgendamentoUtil.REMARCADO.getSigla());
                        if (getEntity().getPlanoTratamento() != null) {
                            this.getEntity().setConvenioPaciente(getEntity().getPlanoTratamento().getConvenio());
                        } else if (getEntity().getPaciente().getConvenio() != null) {
                            this.getEntity().setConvenioPaciente(getEntity().getPaciente().getConvenio());
                        }
                        try {
                            AgendamentoSingleton.getInstance().getBo().persist(this.getEntity());
                            this.actionNew(event);

//                            try {
//                                RepasseFaturasSingleton.getInstance().verificaAgendamentoRepasse(getEntity(), UtilsFrontEnd.getProfissionalLogado());
//                            } catch (Exception e) {
//                                // Log...
//                            }

                            this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");
                            someChannel.send(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
                            //TODO melhorar socket para atualizar somente quando necessario, ou seja, somente quando 
                            //o profissional salvo aqui tiver na tela do agendamento rapido.
                            //canalAgendamentoRapido.send(profissionalDentroAgenda.getDadosBasico().getNome());

                            // PrimeFaces.current().ajax().addCallbackParam("dlg", true);
                            PrimeFaces.current().executeScript("PF('eventDialog').hide()");
                        } catch (Exception e) {
                            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
                            log.error("actionPersist", e);
                        }
                    }
                }
            } else {
                //this.refreshEntity();
            }
            validaHabilitaSalvar();
            this.refreshEntity();
            //carregarScheduleTarefas();
        } else {
            this.addError(OdontoMensagens.getMensagem("erro.agendamento.planotratamento.vazio"), "");
        }
    }

//    public void recarregarPagina(String idEmpresa) {
//        System.out.println("########################"+idEmpresa);
//        System.out.println("-----------------------"+UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
//        if(idEmpresa.equals(""+UtilsFrontEnd.getProfissionalLogado().getIdEmpresa())) {
//            System.out.println("dentro do if");
//            PrimeFaces.current().executeScript("PF('myschedule').update();");                
//        }
//    }

    private boolean validaRangeDatasRemarcado() {
        return this.getInicio().after(this.getEntity().getInicio()) && this.getInicio().before(this.getEntity().getFim()) || this.getFim().after(this.getEntity().getInicio()) && this.getFim().before(
                this.getEntity().getFim()) || this.getFim().getTime() == this.getEntity().getFim().getTime() || this.getInicio().getTime() == this.getEntity().getInicio().getTime() || this.getEntity().getInicio().after(
                        this.getInicio()) && this.getEntity().getInicio().before(this.getFim()) || this.getEntity().getFim().after(this.getInicio()) && this.getEntity().getFim().before(this.getFim());
    }

    private List<AgendamentoPlanoTratamentoProcedimento> insereAgendamento(List<AgendamentoPlanoTratamentoProcedimento> target) {
        if (target != null && !target.isEmpty()) {
            for (AgendamentoPlanoTratamentoProcedimento agendamentoPlanoTratamentoProcedimento : target) {
                agendamentoPlanoTratamentoProcedimento.setAgendamento(this.getEntity());
            }
        }
        return target;
    }

    private boolean isRemarcado() {
        try {
            if (this.getEntity().getStatusNovo().equals(StatusAgendamentoUtil.REMARCADO.getSigla())) {

                Agendamento agendamento = new Agendamento();
                if (this.getEntity().getAgendador() == null) {
                    agendamento.setAgendador(UtilsFrontEnd.getProfissionalLogado());
                } else {
                    agendamento.setAgendador(this.getEntity().getAgendador());
                }
                if (agendamento.getDataAgendamento() == null) {
                    agendamento.setDataAgendamento(new Date());
                } else {
                    agendamento.setDataAgendamento(this.getEntity().getDataAgendamento());
                }

                agendamento.setDataUltAlteracao(new Date());
                agendamento.setProfissionalUltAlteracao(UtilsFrontEnd.getProfissionalLogado());

                agendamento.setFim(this.getFim());
                agendamento.setInicio(this.getInicio());
                agendamento.setOrigemAgendamento(OrigemAgendamentoSingleton.getInstance().getBo().findByDescricao("Remarcacao"));

                agendamento.setPaciente(this.getEntity().getPaciente());
                agendamento.setProfissional(this.getEntity().getProfissional());
                //agendamento.setPlanoTratamentoProcedimentosAgendamento(this.getEntity().getPlanoTratamentoProcedimentosAgendamento());
                agendamento.setStatusNovo("N");
                agendamento.setFilial(this.getEntity().getFilial());
                agendamento.setHash(this.getEntity().getHash());
                agendamento.setDescricao(this.getEntity().getDescricao());
                if (getEntity().getPlanoTratamento() != null) {
                    this.getEntity().setConvenioPaciente(getEntity().getPlanoTratamento().getConvenio());
                } else if (getEntity().getPaciente().getConvenio() != null) {
                    this.getEntity().setConvenioPaciente(getEntity().getPaciente().getConvenio());
                }
                AgendamentoSingleton.getInstance().getBo().persist(agendamento);

                List<AgendamentoPlanoTratamentoProcedimento> aptps = this.getEntity().getPlanoTratamentoProcedimentosAgendamento();
                List<AgendamentoPlanoTratamentoProcedimento> aptpsNovos = new ArrayList<>();
                for (AgendamentoPlanoTratamentoProcedimento agendamentoPlanoTratamentoProcedimento : aptps) {
                    AgendamentoPlanoTratamentoProcedimento a = new AgendamentoPlanoTratamentoProcedimento();
                    a.setPlanoTratamentoProcedimento(agendamentoPlanoTratamentoProcedimento.getPlanoTratamentoProcedimento());
                    a.setAgendamento(agendamento);
                    aptpsNovos.add(a);
                }
                agendamento.setPlanoTratamentoProcedimentosAgendamento(aptpsNovos);
                if (getEntity().getPlanoTratamento() != null) {
                    this.getEntity().setConvenioPaciente(getEntity().getPlanoTratamento().getConvenio());
                } else if (getEntity().getPaciente().getConvenio() != null) {
                    this.getEntity().setConvenioPaciente(getEntity().getPaciente().getConvenio());
                }
                AgendamentoSingleton.getInstance().getBo().persist(agendamento);
                ReservaSingleton.getInstance().getBo().cancelaReservas(getEntity(), UtilsFrontEnd.getProfissionalLogado());

                return true;
            } //else if (this.getEntity().getStatusNovo().equals(StatusAgendamentoUtil.CANCELADO.getSigla()) || this.getEntity().getStatusNovo().equals(StatusAgendamentoUtil.FALTA.getSigla())) {
             //   ReservaSingleton.getInstance().getBo().cancelaReservas(getEntity(), UtilsFrontEnd.getProfissionalLogado());
             //}
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private void refreshEntity() {
        if (this.getEntity().getId() != 0) {
            try {
                this.setEntity(AgendamentoSingleton.getInstance().getBo().find(this.getEntity()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            this.setEntity(null);
            //pacienteSelecionado = null;
        }
    }

    private boolean validacoes() {
        if (GenericValidator.validarRangeData(this.getEntity().getChegouAs(), this.getEntity().getFinalizouAs(), false)) {
            if (this.getProfissionalDentroAgenda() != null && this.getEntity() != null) {
                if (this.pacienteSelecionado != null) {
                    if (GenericValidator.validarRangeData(this.getInicio(), this.getFim(), true)) {
                        //  if (this.validaHoraduplicadaProfissional(this.getEntity())) {
                        if (this.validaHoraduplicadaPaciente(this.pacienteSelecionado)) {
                            if (this.validaIntervalo()) {
                                if (validaCadeira()) {

                                    //    if (this.getEntity().getStatusNovo().matches("S|N|I|O|A")) {
                                    if (this.getEntity().getFinalizouAs() != null && (this.finalizaouAsEstadoInicial == null || !this.finalizaouAsEstadoInicial.equals(
                                            this.getEntity().getFinalizouAs()))) {
                                        this.getEntity().setStatusNovo("A");
                                        this.addInfo(OdontoMensagens.getMensagem("agendamento.status.alterado.atendido"), "");
                                    } else if (this.getEntity().getIniciouAs() != null && (this.iniciouAsEstadoInicial == null || !this.iniciouAsEstadoInicial.equals(
                                            this.getEntity().getIniciouAs()))) {
                                        if (this.getEntity().getFinalizouAs() == null) {
                                            this.getEntity().setStatusNovo("O");
                                            this.addInfo(OdontoMensagens.getMensagem("agendamento.status.alterado.ematendimento"), "");
                                        }
                                    } else if (this.getEntity().getChegouAs() != null && (this.chegouAsEstadoInicial == null || !this.chegouAsEstadoInicial.equals(this.getEntity().getChegouAs()))) {
                                        if (this.getEntity().getIniciouAs() == null) {
                                            this.getEntity().setStatusNovo("I");
                                            this.addInfo(OdontoMensagens.getMensagem("agendamento.status.alterado.clientenaclinica"), "");
                                        }
                                        //apagou todas as datas, voltar para confirmado.    
                                    } else if (this.getEntity().getChegouAs() == null && this.getEntity().getIniciouAs() == null && this.getEntity().getFinalizouAs() == null && this.chegouAsEstadoInicial != null) {
                                        this.getEntity().setStatusNovo("S");
                                        this.addInfo("Status alterado para confirmado", "");
                                    } else if (this.getEntity().getIniciouAs() == null && this.getEntity().getFinalizouAs() == null && this.iniciouAsEstadoInicial != null) {
                                        if (this.chegouAsEstadoInicial == null) {
                                            this.getEntity().setStatusNovo("S");
                                            this.addInfo("Status alterado para confirmado", "");
                                        } else {
                                            this.getEntity().setStatusNovo("I");
                                            this.addInfo(OdontoMensagens.getMensagem("agendamento.status.alterado.clientenaclinica"), "");
                                        }
                                    } else if (this.getEntity().getFinalizouAs() == null && this.finalizaouAsEstadoInicial != null) {
                                        if (this.chegouAsEstadoInicial == null) {
                                            this.getEntity().setStatusNovo("S");
                                            this.addInfo("Status alterado para confirmado", "");
                                        } else {
                                            this.getEntity().setStatusNovo("O");
                                            this.addInfo(OdontoMensagens.getMensagem("agendamento.status.alterado.ematendimento"), "");
                                        }
                                    }
                                    //  }

                                    if (this.validaData()) {
                                        return true;
                                    } else {
                                        this.addError(OdontoMensagens.getMensagem("agendamento.profissional.afastado"), "");
                                        //dlg = false;
                                    }
                                } else {
                                    this.addError("Cadeira j?? est?? em uso para este hor??rio.", "");
                                    //dlg = false;
                                }
                            } else {
                                this.addError(OdontoMensagens.getMensagem("agendamento.intervalo.incorreto"), "");
                                //dlg = false;
                            }
                        } else {
                            this.addError(OdontoMensagens.getMensagem("agendamento.paciente.horamarcada"), "", true);
                            //dlg = false;
                        }
//                        } else {
//                            this.addError(OdontoMensagens.getMensagem("agendamento.horario.utilizado"), "");
//                            dlg = false;
//                        }
                    } else {
                        this.addError(OdontoMensagens.getMensagem("agendamento.intervalo.incorreto"), "", true);
                        //dlg = false;
                    }
                } else {
                    this.addError(OdontoMensagens.getMensagem("agendamento.paciente.obrigatorio"), "", true);
                    //dlg = false;
                }
            } else {
                this.addError(OdontoMensagens.getMensagem("agendamento.profissional.obrigatorio"), "");
                //dlg = false;
            }
        } else {
            this.addError(OdontoMensagens.getMensagem("agendamento.horario.atendimento"), "");
            //dlg = false;
        }
        return false;
    }

    public boolean validaCadeira() {
        //if((this.getEntity().getId() == 0 || !this.getInicio().equals(getEntity().getInicio()) || !this.getFim().equals(getEntity().getFim()) || this.getCadeiraDentroAgenda() != getEntity().getCadeira()) && this.getCadeiraDentroAgenda() != null) {
        //    return !AgendamentoSingleton.getInstance().getBo().existeAgendamentoDaCadeira(this.getEntity(), this.getInicio(), this.getFim(), this.getCadeiraDentroAgenda(),
        //            UtilsFrontEnd.getEmpresaLogada().getEmpIntCod());
        //} else
        return true;
    }

    public boolean validaIntervalo() {
        SimpleDateFormat formatter = new SimpleDateFormat("Dy");// Dia do ano e ano
        String inicioString = formatter.format(this.getInicio().getTime());
        String fimString = formatter.format(this.getFim().getTime());
        if (!inicioString.equals(fimString)) {
            return false;
        } else {
            return true;
        }
    }

    public void atualizaCadeiraSelecionada() {
        if (getCadeiraDentroAgenda() != null && AgendamentoSingleton.getInstance().getBo().existeAgendamentoDaCadeira(this.getEntity(), this.getInicio(), this.getFim(), this.getCadeiraDentroAgenda(),
                UtilsFrontEnd.getEmpresaLogada().getEmpIntCod())) {
            addError("", "Cadeira j?? est?? em uso para este hor??rio.", true);
        }
    }

    public void validaProfissionalEmAgendamento() {
        List<Agendamento> agendamentos = AgendamentoSingleton.getInstance().getBo().listByDataAndProfissional(profissionalDentroAgenda, this.getInicio(), this.getFim());
        if (agendamentos != null && !agendamentos.isEmpty()) {
            addWarn("", "Profissional j?? est?? atendendo neste hor??rio.", true);
        }
    }

    public void validaHoraUtilProfissionalCombo() {
        validaHoraUtilProfissional(profissionalDentroAgenda);
        if (horaUtilValida == false) {
            addWarn("", "Profissional n??o est?? cadastrado para atender nesse hor??rio.", true);
        }
        validaProfissionalEmAgendamento();
    }

    public void validaHoraUtilProfissional() {
        validaHoraUtilProfissional(profissionalDentroAgenda);
        if (horaUtilValida == false) {
            addWarn("", "Profissional n??o est?? cadastrado para atender nesse hor??rio.", true);
        }
        validaProfissionalEmAgendamento();
    }

    //FIXME - Arrumar pra testar se orcamento ta aprovado!
    public void verificaProcedimentos() {
        List<AgendamentoPlanoTratamentoProcedimento> aptps = procedimentosPickList.getTarget();

        for (AgendamentoPlanoTratamentoProcedimento aptp : aptps) {

            if (aptp.getPlanoTratamentoProcedimento().getOrcamentoProcedimentos() == null || aptp.getPlanoTratamentoProcedimento().getOrcamentoProcedimentos().size() == 0) {
                addWarn("", "Aten????o! Procedimento " + aptp.getPlanoTratamentoProcedimento().getProcedimento().getDescricao() + " n??o est?? em nenhum or??amento aprovado", true);
            } else if (!OrcamentoSingleton.getInstance().isProcedimentoTemOrcamentoAprovado(aptp.getPlanoTratamentoProcedimento())) {
                addWarn("", "Aten????o! Procedimento " + aptp.getPlanoTratamentoProcedimento().getProcedimento().getDescricao() + " n??o est?? em nenhum or??amento aprovado", true);
            }

        }

    }

    public void validaHoraUtilProfissional(Profissional profissional) {
        if (profissional != null) {
            try {
                this.setHoraUtilValida(Boolean.FALSE);
                Calendar inicioConsulta = new GregorianCalendar(), fimConsulta = new GregorianCalendar(), inicioAtendimentoProfissional = new GregorianCalendar(),
                        fimAtendimentoProfissional = new GregorianCalendar();
                if (this.getInicio() != null && this.getFim() != null) {
                    inicioConsulta.setTime(this.getInicio());
                    fimConsulta.setTime(this.getFim());
                    List<HorasUteisProfissional> horasUteis = HorasUteisProfissionalSingleton.getInstance().getBo().listByProfissionalAndDiaDaSemana(profissional, inicioConsulta);
                    for (HorasUteisProfissional horasUteisProfissional : horasUteis) {
                        if (horasUteisProfissional != null) {
                            //TODO considerar intervalos de almo??o
                            if (horasUteisProfissional.getHoraIni() != null && horasUteisProfissional.getHoraFimTarde() != null) {
                                inicioAtendimentoProfissional.setTime(horasUteisProfissional.getHoraIni());
                                fimAtendimentoProfissional.setTime(horasUteisProfissional.getHoraFimTarde());
                            } else if (horasUteisProfissional.getHoraIni() != null && horasUteisProfissional.getHoraFim() != null) {
                                inicioAtendimentoProfissional.setTime(horasUteisProfissional.getHoraIni());
                                fimAtendimentoProfissional.setTime(horasUteisProfissional.getHoraFim());
                            } else if (horasUteisProfissional.getHoraIniTarde() != null && horasUteisProfissional.getHoraFimTarde() != null) {
                                inicioAtendimentoProfissional.setTime(horasUteisProfissional.getHoraIniTarde());
                                fimAtendimentoProfissional.setTime(horasUteisProfissional.getHoraFimTarde());
                            }

                            if (((inicioAtendimentoProfissional.get(Calendar.HOUR_OF_DAY) < inicioConsulta.get(
                                    Calendar.HOUR_OF_DAY)) || ((inicioAtendimentoProfissional.get(Calendar.HOUR_OF_DAY) == inicioConsulta.get(
                                            Calendar.HOUR_OF_DAY)) && (inicioAtendimentoProfissional.get(Calendar.MINUTE) <= inicioConsulta.get(Calendar.MINUTE)))) && ((fimAtendimentoProfissional.get(
                                                    Calendar.HOUR_OF_DAY) > fimConsulta.get(Calendar.HOUR_OF_DAY)) || ((fimAtendimentoProfissional.get(Calendar.HOUR_OF_DAY) == fimConsulta.get(
                                                            Calendar.HOUR_OF_DAY)) && (fimAtendimentoProfissional.get(Calendar.MINUTE) >= fimConsulta.get(Calendar.MINUTE))))) {
                                this.setHoraUtilValida(Boolean.TRUE);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            this.setHoraUtilValida(Boolean.TRUE);
        }
    }

    public void calculaDataFim() {
        if (this.getInicio() != null && profissionalDentroAgenda != null && profissionalDentroAgenda.getTempoConsulta() != null && profissionalDentroAgenda.getTempoConsulta() != 0) {
            Calendar c = Calendar.getInstance();
            c.setTime(this.getInicio());
            c.add(Calendar.MINUTE, profissionalDentroAgenda.getTempoConsulta());
            //  if(this.fim == null)
            setFim(c.getTime());
        }
    }

    public void listenerAlterouProfissional() {
        this.calculaDataFim();
        this.validarAfastamentoProfissional();
        this.validaHoraUtilProfissionalCombo();
    }

    public boolean validaHoraduplicadaProfissional(Agendamento agendamento) {
        try {
            if (agendamento == null) {
                return false;
            }
            boolean dataInicioValida = AgendamentoSingleton.getInstance().getBo().verificaDataDisponivelParaProfissional(agendamento, profissionalDentroAgenda, this.getInicio());
            boolean dataFimValida = AgendamentoSingleton.getInstance().getBo().verificaDataDisponivelParaProfissional(agendamento, profissionalDentroAgenda, this.getFim());
            if (dataInicioValida && dataFimValida) {
                return false;
            }
            return true;
//            for (Agendamento agnd : agendamentosValidaHoraDuplicadaProfissional) {
//                if ((this.getInicio().after(agnd.getInicio()) && this.getInicio().before(agnd.getFim()) || this.getFim().after(agnd.getInicio()) && this.getFim().before(
//                        agnd.getFim()) || (this.getFim().getTime() == (agnd.getFim().getTime()) || (this.getInicio().getTime() == agnd.getInicio().getTime()) || agnd.getInicio().after(
//                                this.getInicio()) && agnd.getInicio().before(this.getFim()) || agnd.getFim().after(
//                                        this.getInicio()) && agnd.getFim().before(this.getFim()))) && agnd.getId() != this.getEntity().getId()) {
//                    if ((Status.SIM.equals(this.getEntity().getEncaixe())) || (agnd.getStatusNovo().equals(StatusAgendamentoUtil.REMARCADO.getSigla())) || (this.getEntity().getStatusNovo().equals(
//                            StatusAgendamentoUtil.REMARCADO.getSigla())) || (this.getEntity().getStatusNovo().equals(
//                                    StatusAgendamentoUtil.CANCELADO.getSigla())) || (agnd.getStatusNovo().equals(StatusAgendamentoUtil.ERRO_AGENDAMENTO.getSigla())) ||
//                            (agnd.getStatusNovo().equals(StatusAgendamentoUtil.CANCELADO.getSigla())) || (Status.SIM.equals(agnd.getEncaixe()))) {
//                        return true;
//                    } else {
//                        return false;
//                    }
//                }
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    public boolean validaHoraduplicadaPaciente(Paciente paciente) {
//        try {
//            for (Agendamento agnd : ((AgendamentoBO) this.getbO()).listByPaciente(paciente)) {
//                if ((this.getInicio().after(agnd.getInicio()) && this.getInicio().before(agnd.getFim()) || this.getFim().after(agnd.getInicio()) && this.getFim().before(
//                        agnd.getFim()) || this.getFim().getTime() == (agnd.getFim().getTime()) || this.getInicio().getTime() == agnd.getInicio().getTime() || agnd.getInicio().after(
//                                this.getInicio()) && agnd.getInicio().before(this.getFim()) || agnd.getFim().after(
//                                        this.getInicio()) && agnd.getFim().before(this.getFim())) && agnd.getId() != this.getEntity().getId()) {
//                    if (agnd.getStatus().equals(StatusAgendamento.CANCELADO.getSigla()) || // ENCAIXE n??o pode ser feito se for o mesmo
//                                                                                          // Paciente
//                            (agnd.getStatus().equals(StatusAgendamento.ENCAIXE.getSigla()))) {
//                        return true;
//                    } else {
//                        return false;
//                    }
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        return true;
    }

    public void actionConfirmarPreCadastro(ActionEvent event) throws Exception {

        this.getEntity().setStatusNovo(StatusAgendamentoUtil.CONFIRMADO.getSigla());
        if (getEntity().getPlanoTratamento() != null) {
            this.getEntity().setConvenioPaciente(getEntity().getPlanoTratamento().getConvenio());
        } else if (getEntity().getPaciente().getConvenio() != null) {
            this.getEntity().setConvenioPaciente(getEntity().getPaciente().getConvenio());
        }
        try {
            AgendamentoSingleton.getInstance().getBo().persist(this.getEntity());
            this.actionNew(event);
            PrimeFaces.current().executeScript("PF('eventDialog').hide()");

        } catch (BusinessException e) {
            e.printStackTrace();
        } catch (TechnicalException e) {
            e.printStackTrace();
        }
        this.addInfo(OdontoMensagens.getMensagem("agendamento.confirmado.sucessso"), "");
    }

    @Override
    public void actionRemove(ActionEvent arg0) {
        if (!this.getEntity().getStatusNovo().equals(StatusAgendamentoUtil.REMARCADO.getSigla())) {
            //dlg = true;
            try {
                this.removeAgendamentoPlanoTratamentoProcedimento();
                ReservaSingleton.getInstance().getBo().cancelaReservas(getEntity(), UtilsFrontEnd.getProfissionalLogado());
                AgendamentoSingleton.getInstance().getBo().remove(this.getEntity());
                this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_REMOVIDO_COM_SUCESSO), "");
            } catch (Exception e) {
                this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_REMOVER_REGISTRO), "");
                log.error("actionRemove", e);
            }
        } else {
            this.addInfo(OdontoMensagens.getMensagem("agendamento.remove.remarcado"), "");
        }
    }

    private void removeAgendamentoPlanoTratamentoProcedimento() throws Exception {
        List<AgendamentoPlanoTratamentoProcedimento> aptps = AgendamentoPlanoTratamentoProcedimentoSingleton.getInstance().getBo().listByAgendamento(this.getEntity());
        for (AgendamentoPlanoTratamentoProcedimento aptp : aptps) {
            AgendamentoPlanoTratamentoProcedimentoSingleton.getInstance().getBo().remove(aptp);
        }
    }

//    private void carregarScheduleProfissionalTarefas() {
//        scheduleProfissional = new LazyScheduleModel() {
//
//            @Override
//            public void loadEvents(Date start, Date end) {
//                initialDate = start;
//            }
//        };
//    }

    private void carregarScheduleTarefas() {
        schedule = new LazyScheduleModel() {

            private static final long serialVersionUID = 1L;

            @Override
            public void loadEvents(LocalDateTime startDateTime, LocalDateTime endDateTime) {

                Date start = Utils.convertToDateViaInstant(startDateTime);
                Date end = Utils.convertToDateViaInstant(endDateTime);
                initialDateConsult = start;

                if (!initialDate.equals(start)) {
                    initialDate = Utils.convertToLocalDateViaInstant(start);
                    PrimeFaces.current().executeScript("updateSchedule2()");
                }

                if (AgendamentoMB.this.isDentista()) {
                    profissional = UtilsFrontEnd.getProfissionalLogado();
                }

                try {
                    if (profissional == null && pacientePesquisado == null) {

                        this.clear();
                        agendamentos = AgendamentoSingleton.getInstance().getBo().listByDataTodosProfissionais(start, end, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
                        afastamentos = AfastamentoSingleton.getInstance().getBo().listByDataValidos(start, end, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());

                        removerFiltrosAgendamento(agendamentos);
                        pacientePesquisado = null;

                    } else if (profissional != null && pacientePesquisado == null) {

                        tempoConsulta = profissional.getTempoConsulta();
                        this.clear();
                        agendamentos = AgendamentoSingleton.getInstance().getBo().listByDataAndProfissional(profissional, start, end);
                        removerFiltrosAgendamento(agendamentos);
                        afastamentos = AfastamentoSingleton.getInstance().getBo().listByDataAndProfissionalValidos(profissional, start, end, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
                        pacientePesquisado = null;

                    } else if (pacientePesquisado != null && profissional == null) {

                        this.clear();
                        agendamentos = AgendamentoSingleton.getInstance().getBo().listByDataAndPaciente(pacientePesquisado, start, end);
                        afastamentos = AfastamentoSingleton.getInstance().getBo().listByDataValidos(start, end, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
                        removerFiltrosAgendamento(agendamentos);

                    } else if (pacientePesquisado != null && profissional != null) {

                        this.clear();
                        agendamentos = AgendamentoSingleton.getInstance().getBo().listByDataAndPacienteAndProfissional(pacientePesquisado, profissional, start, end);
                        afastamentos = AfastamentoSingleton.getInstance().getBo().listByDataAndProfissionalValidos(profissional, start, end, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
                        removerFiltrosAgendamento(agendamentos);

                    }

                    if (agendamentos != null) {
                        Calendar dataNasc = Calendar.getInstance();
                        dataNasc.add(Calendar.YEAR, -18);
                        Calendar dataAtual = Calendar.getInstance();

                        String descricao = "";

                        if (afastamentos != null && filtroAgendamento != null && filtroAgendamento.contains("F")) {

                            Dominio dominio = null;

                            String dominioStr;

                            for (Afastamento afastamento : afastamentos) {

                                dominio = DominioSingleton.getInstance().getBo().listByTipoAndObjeto(afastamento.getTipo(), "afastamento");
                                dominioStr = dominio != null ? dominio.getNome() : "";

                                descricao = afastamento.getProfissional().getDadosBasico().getNomeAbreviado() + " - " + "[" + dominioStr + " - " + afastamento.getObservacao() + "] ";
                                String breakLine = "\r\n";
                                descricao += breakLine;

                                this.addEvent(DefaultScheduleEvent.builder().title(descricao).startDate(Utils.convertToLocalDateTimeViaInstant(afastamento.getInicio())).endDate(
                                        Utils.convertToLocalDateTimeViaInstant(afastamento.getFim())).data(afastamento).textColor("black").styleClass(
                                                StatusAgendamentoUtil.findBySigla("F").getStyleCss()).build());
                            }
                        }

                        for (Agendamento agendamento : agendamentos) {

                            descricao = "";
                            //TODO melhor jeito para mostrar consulta Inicial
                            if (agendamento.getPlanoTratamentoProcedimentosAgendamento() != null) {
                                for (AgendamentoPlanoTratamentoProcedimento aptp : agendamento.getPlanoTratamentoProcedimentosAgendamento()) {
                                    if (aptp.getPlanoTratamentoProcedimento() != null && aptp.getPlanoTratamentoProcedimento().getProcedimento() != null && aptp.getPlanoTratamentoProcedimento().getProcedimento().getDescricao() != null && aptp.getPlanoTratamentoProcedimento().getProcedimento().getDescricao().contains(
                                            "Inicial")) {
                                        descricao = "[ Consulta Inicial ] ";
                                    }
                                }
                            }

                            if (agendamento != null && agendamento.getPaciente() != null && agendamento.getPaciente().getDadosBasico() != null && agendamento.getPaciente().getDadosBasico().getDataNascimento() != null)
                                dataAtual.setTime(agendamento.getPaciente().getDadosBasico().getDataNascimento());

                            descricao += "" + agendamento.getProfissional().getDadosBasico().getNomeAbreviado() + " - " + "[" + agendamento.getPaciente().getSiglaConvenio() + "] " + agendamento.getPaciente().getDadosBasico().getNome();

                            if (PacienteSingleton.getInstance().getPendenciaFinanceiraPaciente(paciente)) {
                                descricao += " - $ ";
                            }

                            String breakLine = "\r\n";
                            if (agendamento.getDescricao() != null && !agendamento.getDescricao().isEmpty())
                                descricao += " - Obs.: " + agendamento.getDescricao();
                            descricao += breakLine;
                            if (agendamento.getDescricao() != null && !agendamento.getDescricao().isEmpty())
                                descricao += breakLine;
                            if (agendamento.getPlanoTratamentoProcedimentosAgendamento() != null && !agendamento.getPlanoTratamentoProcedimentosAgendamento().isEmpty()) {
                                descricao += " P: ";
                                for (AgendamentoPlanoTratamentoProcedimento ptpAgendamento : agendamento.getPlanoTratamentoProcedimentosAgendamento())
                                    descricao += ptpAgendamento.getPlanoTratamentoProcedimento().getProcedimento().getDescricao() + ", ";
                                descricao = descricao.substring(0, descricao.length() - 2);

                            }

                            DefaultScheduleEvent event = DefaultScheduleEvent.builder().title(descricao).startDate(Utils.convertToLocalDateTimeViaInstant(agendamento.getInicio())).endDate(
                                    Utils.convertToLocalDateTimeViaInstant(agendamento.getFim())).data(agendamento).textColor(
                                            (Arrays.asList(statusColorBlack).contains(agendamento.getStatusNovo()) ? "black" : "white")).styleClass(
                                                    StatusAgendamentoUtil.findBySigla(agendamento.getStatusNovo()).getStyleCss()).build();

                            if (PacienteSingleton.getInstance().getPendenciaFinanceiraPaciente(paciente)) {
                                event.setDescription("Verificar cobran??a do paciente");
                            }

                            this.addEvent(event);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        };
    }

    private void removerFiltrosAgendamento(List<Agendamento> agendamentos) {
        if (agendamentos != null) {
            List<Agendamento> agentamentoAux = new ArrayList<>(agendamentos);
            for (Agendamento agendamento : agentamentoAux) {
                if (!filtroAgendamento.contains(agendamento.getStatusNovo())) {
                    agendamentos.remove(agendamento);
                }
            }
        }

    }

    private void geraAgendamentoAfastamento(Date start, Date end, Profissional profissional) {
        agendamentosAfastamento = geraAgendamentoAfastamentoByProfissional(start, end, profissional);
    }

    private List<Agendamento> geraAgendamentoAfastamentoByProfissional(Date start, Date end, Profissional profissional) {
        List<Agendamento> agendamentos = new ArrayList<>();
        try {
            List<Afastamento> afastamentos = null;
            if (profissional != null) {
                afastamentos = AfastamentoSingleton.getInstance().getBo().listByDataAndProfissionalValidos(profissional, start, end, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
            } else {
                afastamentos = AfastamentoSingleton.getInstance().getBo().listByDataValidos(start, end, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
            }
            if (afastamentos != null) {
                for (Afastamento afastamento : afastamentos) {
                    Paciente pacienteAfastamento = new Paciente();
                    Dominio dominio = null;
                    dominio = DominioSingleton.getInstance().getBo().listByTipoAndObjeto(afastamento.getTipo(), "afastamento");

                    String dominioStr = dominio != null ? dominio.getNome() : "";

                    pacienteAfastamento.getDadosBasico().setNome(dominioStr);
                    Agendamento agendamento = new Agendamento();
                    agendamento.setInicio(afastamento.getInicio());
                    agendamento.setFim(afastamento.getFim());
                    agendamento.setPaciente(pacienteAfastamento);
                    agendamento.setProfissional(afastamento.getProfissional());
                    agendamento.setStatusAgendamento(null);
                    agendamento.setDescricao(afastamento.getObservacao());
                    agendamentos.add(agendamento);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return agendamentos;
    }

    public void onDateSelect(SelectEvent selectEvent) {
        Calendar c = Calendar.getInstance();
        if (selectEvent != null) {
            LocalDateTime date = (LocalDateTime) selectEvent.getObject();
            this.setInicio(Utils.convertToDateViaInstant(date));
            date.plusMinutes(tempoConsulta);
            this.setFim(Utils.convertToDateViaInstant(date));

            c.setTime(this.inicio);
            c.setTime(this.fim);
        }

        this.setEntity(new Agendamento());
        this.setPacienteSelecionado(null);
        this.setPlanoTratamentoSelecionado(null);

        this.chegouAsEstadoInicial = null;
        this.iniciouAsEstadoInicial = null;
        this.finalizaouAsEstadoInicial = null;

        //profissional = null;
        profissionalDentroAgenda = null;
        cadeiraDentroAgenda = null;

        chegouAsDentroAgenda = null;
        iniciouAsDentroAgenda = null;;
        finalizouAsDentroAgenda = null;

        this.validaHoraUtilProfissional(profissionalDentroAgenda);
        //this.validaAfastamento();
        PrimeFaces.current().ajax().addCallbackParam("hora", horaUtilValida);
        validaHabilitaSalvar();
        PrimeFaces.current().ajax().addCallbackParam("afastamento", true);
        setObservacoes(null);
    }

    private void validaHabilitaSalvar() {
        habilitaSalvar = !StatusAgendamentoUtil.CANCELADO.getSigla().equals(getEntity().getStatusNovo()) && !StatusAgendamentoUtil.REMARCADO.getSigla().equals(getEntity().getStatusNovo());
    }

    public boolean validaData() {
        Calendar c = Calendar.getInstance();
        c.setTime(getInicio());
        c.add(Calendar.DAY_OF_MONTH, -1);
        Date start = c.getTime();

        c.setTime(getFim());
        c.add(Calendar.DAY_OF_MONTH, +1);
        Date end = c.getTime();

        List<Agendamento> agendamentoBloqueado = geraAgendamentoAfastamentoByProfissional(start, end, profissionalDentroAgenda);
        for (Agendamento agnd : agendamentoBloqueado) {
            if (this.getInicio().after(agnd.getInicio()) && this.getInicio().before(agnd.getFim()) || this.getFim().after(agnd.getInicio()) && this.getFim().before(
                    agnd.getFim()) || this.getFim().getTime() == agnd.getFim().getTime() || this.getInicio().getTime() == agnd.getInicio().getTime() || agnd.getInicio().after(
                            this.getInicio()) && agnd.getInicio().before(this.getFim()) || agnd.getFim().after(this.getInicio()) && agnd.getFim().before(this.getFim())) {
                return false;
            }
        }
        return true;
    }

    public void handleSelect(SelectEvent event) {
        Object object = event.getObject();
        pacienteSelecionado = (Paciente) object;
        planoTratamentoSelecionado = null;
        atualizaPickList();
    }

    //public void handleSelectPacienteSelecionado() {
    //Object object = event.getObject();
    // pacientePesquisado = (Paciente) object;
    //  planoTratamentoSelecionado = null;
    //  profissional = null;
    //  this.setEntity(null);
    //  this.carregarScheduleTarefas();
    //}

    public void limpaPacienteSelecionado() {
        //this.setPacientePesquisado(null);
        this.actionNew(null);
        this.carregarScheduleTarefas();
    }

    public void onEventSelect(SelectEvent selectEvent) {

        Agendamento agendamento = null;
        if (selectEvent.getObject() == null) {
            addError("Erro", "Houve um problema na solicita????o, tente novamente!");
            return;
        }

        Object obj = ((ScheduleEvent) selectEvent.getObject()).getData();

        if (obj instanceof Afastamento) {

            this.addError("Profissional afastado !", "O profissional n??o est?? dispon??vel para atender.");

        } else {

            agendamento = (Agendamento) obj;

            this.profissionalDentroAgenda = agendamento.getProfissional();

            this.cadeiraDentroAgenda = agendamento.getCadeira();
            this.chegouAsDentroAgenda = agendamento.getChegouAs();
            this.iniciouAsDentroAgenda = agendamento.getIniciouAs();
            this.finalizouAsDentroAgenda = agendamento.getFinalizouAs();

            UtilsFrontEnd.setPacienteSelecionado(agendamento.getPaciente());
            this.setEntity(agendamento);
            this.setInicio(this.getEntity().getInicio());
            this.setFim(this.getEntity().getFim());

            this.setPacienteSelecionado(agendamento.getPaciente());
            this.setJustificativa(DominioSingleton.getInstance().getBo().findByEmpresaAndObjetoAndTipoAndNome("agendamento", "justificativa", this.getEntity().getJustificativa()));
            this.setStatus(this.getEntity().getStatusNovo());
            //this.validaAfastamento();

            if (agendamento.getPlanoTratamentoProcedimentosAgendamento() != null && agendamento.getPlanoTratamentoProcedimentosAgendamento().size() > 0)
                this.setPlanoTratamentoSelecionado(agendamento.getPlanoTratamentoProcedimentosAgendamento().get(0).getPlanoTratamentoProcedimento().getPlanoTratamento());

            atualizaPickList();

            this.setObservacoes(agendamento.getDescricao());
            this.validaHoraUtilProfissional(profissionalDentroAgenda);

            validaHabilitaSalvar();

            PrimeFaces.current().executeScript("PF('eventDialog').show()");
            PrimeFaces.current().ajax().update(":lume:pnAgendamento");

        }
    }

    private void validaAfastamento() {
        boolean afastamento = true;

        // TODO - O Afastamento representa o bloqueio de agenda do profissional, o motivo da aus??ncia do profissional ?? salvo em um agendamento.
        // Isso deve ser corrigido assim que poss??vel.

        for (Agendamento agendamento : agendamentosAfastamento) {
            if (this.getEntity().getStatusNovo().equals(StatusAgendamentoUtil.AFASTAMENTO.getSigla()) || this.getInicio().after(agendamento.getInicio()) && this.getInicio().before(
                    agendamento.getFim()) || this.getInicio().getTime() == agendamento.getInicio().getTime()) {
                afastamento = false;
                this.addError(OdontoMensagens.getMensagem("info.agendamento.afastamento"), "");
                PrimeFaces.current().ajax().addCallbackParam("dlg", true);
                return;
            }
        }
        PrimeFaces.current().ajax().addCallbackParam("afastamento", afastamento);
    }

    public void validarAfastamentoProfissional() {
        this.validaAfastamento();
    }

    private void criarUsuario(Usuario usuario, Paciente paciente) throws UsuarioDuplicadoException, ServidorEmailDesligadoException, Exception {
        usuario.setUsuStrNme(paciente.getDadosBasico().getNome());
        usuario.setUsuStrEml(paciente.getDadosBasico().getEmail());
        usuario.setUsuStrLogin(paciente.getDadosBasico().getEmail());
        Perfil perfilbyDescricao = PerfilSingleton.getInstance().getBo().getPerfilbyDescricaoAndSistema(OdontoPerfil.PACIENTE, this.getLumeSecurity().getSistemaAtual());
        usuario.setPerfisUsuarios(Arrays.asList(perfilbyDescricao));
        usuario.setUsuIntDiastrocasenha(999);
        UsuarioSingleton.getInstance().getBo().persistUsuarioExterno(usuario, UtilsFrontEnd.getEmpresaLogada());
    }

    public void actionPersistPaciente(ActionEvent event) {
        Usuario usuario = null;
        try {
            if (paciente.getDadosBasico().getDataNascimento() != null && Utils.validaDataNascimento(paciente.getDadosBasico().getDataNascimento()) == false) {
                addError("Data de nascimento inv??lida.", "");
                return;
            }
            DadosBasicoSingleton.getInstance().getBo().validaTelefonePaciente(paciente.getDadosBasico());
            //pacienteBO.validaPacienteDuplicadoEmpresa(paciente);
            paciente.setIdEmpresa(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
            if (paciente.getDadosBasico().getEmail() != null && !paciente.getDadosBasico().getEmail().isEmpty()) {
                usuario = UsuarioSingleton.getInstance().getBo().findUsuarioByLogin(paciente.getDadosBasico().getEmail().toUpperCase());
                if (usuario == null) {
                    usuario = new Usuario();
                }
                if (paciente.getId() == null || this.getEntity().getId() == 0) {
                    if (usuario.getUsuIntCod() == 0) {
                        this.criarUsuario(usuario, paciente);
                    }
                }
                if (usuario.getUsuIntCod() != 0) {
                    paciente.setIdUsuario(usuario.getUsuIntCod());
                }
            }
            paciente.setPreCadastro("S");
            if (usuario != null) {
                paciente.setIdEmpresa(UtilsFrontEnd.getEmpresaLogada().getEmpIntCod());
                paciente.setIdUsuario(usuario.getUsuIntCod());
            }
            if (paciente.getId() == null || paciente.getId() == 0) {
                paciente.setDataCriacao(Calendar.getInstance().getTime());
            }
            boolean novoPaciente = paciente.getId() == null || paciente.getId().longValue() == 0;

            PacienteSingleton.getInstance().getBo().persist(paciente);
            PlanoTratamento pt = PlanoTratamentoSingleton.getInstance().getBo().persistPlano(paciente, UtilsFrontEnd.getProfissionalLogado());

            if (novoPaciente) {
                ContaSingleton.getInstance().criaConta(ContaSingleton.TIPO_CONTA.PACIENTE, UtilsFrontEnd.getProfissionalLogado(), BigDecimal.ZERO, paciente, null, null, null, null);
                PrimeFaces.current().executeScript("PF('dlgFichaPaciente').hide()");
            }

            pacienteSelecionado = paciente;
            this.getEntity().setPaciente(paciente);
            if (pt != null) {
                planoTratamentos = new ArrayList<>();
                planoTratamentos.add(pt);
                planoTratamentoSelecionado = pt;
            }
            this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");
            atualizaPickList();
            visivel = false;
            // pacientes = PacienteSingleton.getInstance().getBo().listByEmpresa(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
            paciente = new Paciente();
        } catch (TelefoneException te) {
            this.addError(OdontoMensagens.getMensagem("erro.valida.telefone"), "");
            log.error(OdontoMensagens.getMensagem("erro.valida.telefone"));
        } catch (UsuarioDuplicadoException ud) {
            this.addError(Mensagens.getMensagem(Mensagens.USUARIO_DUPLICADO), "");
            log.error(Mensagens.getMensagem(Mensagens.USUARIO_DUPLICADO));
        } catch (Exception e) {
            log.error("Erro no actionPersistPaciente", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
            try {
                UsuarioSingleton.getInstance().getBo().remove(usuario);
            } catch (Exception e1) {
                log.error("Erro no actionPersistPaciente", e);
                this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_REMOVER_REGISTRO), "");
            }
        }
    }

    public List<Dominio> getJustificativas() {
        List<Dominio> justificativas = new ArrayList<>();
        try {
            justificativas = DominioSingleton.getInstance().getBo().listByEmpresaAndObjetoAndTipo("agendamento", "justificativa");
        } catch (Exception e) {
            log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS);
        }
        return justificativas;
    }

    public void novoPaciente(ActionEvent event) {
        visivel = true;
    }

    public void cancelarNovoPaciente(ActionEvent event) {
        visivel = false;
    }

    public void mostrarTelefones(ActionEvent event) {
        telefonesVisiveis = true;
    }

    public void cancelarMostrarTelefones(ActionEvent event) {
        telefonesVisiveis = false;
    }

    public ScheduleModel getSchedule() {
        return schedule;
    }

    public void setSchedule(ScheduleModel schedule) {
        this.schedule = schedule;
    }

    public Profissional getProfissional() {
        return profissional;
    }

    public void setProfissional(Profissional profissional) {
        this.profissional = profissional;
        profissionalDentroAgenda = profissional;
    }

    public List<Profissional> getProfissionais() {
        if (profissionais != null) {
            Collections.sort(profissionais);
        }
        return profissionais;
    }

    public void setProfissionais(List<Profissional> profissionais) {
        this.profissionais = profissionais;
    }

//    public List<Paciente> getPacientes() {
//        if (pacientes != null) {
//            Collections.sort(pacientes);
//        }
//        return pacientes;
//    }
//
//    public void setPacientes(List<Paciente> pacientes) {
//        this.pacientes = pacientes;
//    }

    public Integer getTempoConsulta() {
        return tempoConsulta;
    }

    public void setTempoConsulta(Integer tempoConsulta) {
        this.tempoConsulta = tempoConsulta;
    }

    public List<Agendamento> getAgendamentos() {
        return agendamentos;
    }

    public void setAgendamentos(List<Agendamento> agendamentos) {
        this.agendamentos = agendamentos;
    }

    public Paciente getPacienteSelecionado() {
        return pacienteSelecionado;
    }

    public void setPacienteSelecionado(Paciente pacienteSelecionado) {
        this.getEntity().setPaciente(pacienteSelecionado);
        this.pacienteSelecionado = pacienteSelecionado;
        try {
            planoTratamentos = new ArrayList<>();
            planoTratamentos = PlanoTratamentoSingleton.getInstance().getBo().listByPaciente(pacienteSelecionado);
        } catch (Exception e) {
            LogIntelidenteSingleton.getInstance().makeLog(e);
        }
    }

    public PlanoTratamento getPlanoTratamentoSelecionado() {
        return planoTratamentoSelecionado;
    }

    public void setPlanoTratamentoSelecionado(PlanoTratamento planoTratamentoSelecionado) {
        this.planoTratamentoSelecionado = planoTratamentoSelecionado;
    }

    public List<PlanoTratamento> getPlanoTratamentos() {
        return planoTratamentos;
    }

    public void setPlanoTratamentos(List<PlanoTratamento> planoTratamentos) {
        this.planoTratamentos = planoTratamentos;
    }

    public Paciente getPaciente() {
        return paciente;
    }

    public void setPaciente(Paciente paciente) {
        this.paciente = paciente;
    }

    public boolean isVisivel() {
        return visivel;
    }

    public void setVisivel(boolean visivel) {
        this.visivel = visivel;
    }

    public void atualizaPickList() {
        List<AgendamentoPlanoTratamentoProcedimento> ptpSelecionadosPreviamente = new ArrayList<>();
        if (procedimentosPickList != null && !procedimentosPickList.getTarget().isEmpty()) {
            ptpSelecionadosPreviamente.addAll(procedimentosPickList.getTarget());
        }
        if (planoTratamentoSelecionado != null) {
            List<AgendamentoPlanoTratamentoProcedimento> planoTratamentoProcedimentos;
            if (mostraFinalizados) {
                planoTratamentoProcedimentos = this.converteParaAgendamentoPlanoTratamentoProcedimento(
                        PlanoTratamentoProcedimentoSingleton.getInstance().getBo().listProcedimentosPickListAgendamento(planoTratamentoSelecionado.getId(), this.getEntity().getId(), false, false));
            } else {
                planoTratamentoProcedimentos = this.converteParaAgendamentoPlanoTratamentoProcedimento(
                        PlanoTratamentoProcedimentoSingleton.getInstance().getBo().listProcedimentosPickListAgendamento(planoTratamentoSelecionado.getId(), this.getEntity().getId(), true, false));
            }

            procedimentosPickList = new DualListModel<>(planoTratamentoProcedimentos != null ? planoTratamentoProcedimentos : new ArrayList<AgendamentoPlanoTratamentoProcedimento>(),
                    this.getEntity().getPlanoTratamentoProcedimentosAgendamento() != null ? this.getEntity().getPlanoTratamentoProcedimentosAgendamento() : new ArrayList<AgendamentoPlanoTratamentoProcedimento>());
        } else {
            procedimentosPickList = new DualListModel<>();
        }
    }

    public DualListModel<AgendamentoPlanoTratamentoProcedimento> getProcedimentosPickList() throws Exception {
        return procedimentosPickList;
    }

    private List<AgendamentoPlanoTratamentoProcedimento> converteParaAgendamentoPlanoTratamentoProcedimento(List<PlanoTratamentoProcedimento> ptps) {
        List<AgendamentoPlanoTratamentoProcedimento> retorno = new ArrayList<>();
        if (ptps != null && !ptps.isEmpty()) {
            for (PlanoTratamentoProcedimento planoTratamentoProcedimento : ptps) {
                retorno.add(new AgendamentoPlanoTratamentoProcedimento(planoTratamentoProcedimento, this.getEntity()));
            }
        }
        return retorno;
    }

    public void onCalendarAgChange() {
        calculaDataFim();
        atualizaCadeiraSelecionada();
        validaHoraUtilProfissional();
    }

    public void onCalendarAgChangeDataFim() {
        atualizaCadeiraSelecionada();
        validaHoraUtilProfissional();
    }

    public void marcarFiltros() {
        if (this.checkFiltro) {
            this.filtroAgendamento.addAll(Arrays.asList("F", "A", "G", "C", "D", "I", "S", "O", "E", "H", "B", "N", "P", "R"));
        } else {
            this.filtroAgendamento.removeAll(Arrays.asList("F", "A", "G", "C", "D", "I", "S", "O", "E", "H", "B", "N", "P", "R"));
        }
    }

    public void setProcedimentosPickList(DualListModel<AgendamentoPlanoTratamentoProcedimento> procedimentosPickList) {
        this.procedimentosPickList = procedimentosPickList;
    }

    public boolean isHoraUtilValida() {
        return horaUtilValida;
    }

    public void setHoraUtilValida(boolean horaUtilValida) {
        this.horaUtilValida = horaUtilValida;
    }

    //TODO - Corrigir time picker que est?? sempre em '00:00' na vers??o posterior ?? 12 apr.
    public Date getInicio() {
        return inicio;
    }

    public void setInicio(Date inicio) {
        this.inicio = inicio;
    }

    public Date getFim() {
        return fim;
    }

    public void setFim(Date fim) {
        this.fim = fim;
    }

    public Dominio getJustificativa() {
        return justificativa;
    }

    public void setJustificativa(Dominio justificativa) {
        this.justificativa = justificativa;
    }

    public Paciente getPacientePesquisado() {
        return pacientePesquisado;
    }

    public void setPacientePesquisado(Paciente pacientePesquisado) {
        this.pacientePesquisado = pacientePesquisado;
    }

    public boolean isResponsavel() {
        return responsavel;
    }

    public void setResponsavel(boolean responsavel) {
        this.responsavel = responsavel;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getQtdProfissionais() {
        return qtdProfissionais;
    }

    public void setQtdProfissionais(int qtdProfissionais) {
        this.qtdProfissionais = qtdProfissionais;
    }

    public String getGmt() {
        return gmt;
    }

    public void setGmt(String gmt) {
        this.gmt = gmt;
    }

    public List<HorasUteisProfissional> getHorasUteisProfissional() {
        try {
            if (profissional != null) {
                horasUteisProfissional = HorasUteisProfissionalSingleton.getInstance().getBo().listByProfissional(profissional);
                this.ordenaPorDiaDaSemana();
            } else {
                horasUteisProfissional = null;
            }
        } catch (Exception e) {
            this.addError(OdontoMensagens.getMensagem("horasuteisprofissional.erro.carregar.hora"), "");
        }
        return horasUteisProfissional;
    }

    public List<HorasUteisProfissional> getHorasUteisProfissionalAgenda() {
        try {

            if (horasUteisProfissionalAgenda != null)
                horasUteisProfissionalAgenda = new ArrayList<HorasUteisProfissional>();

            if (profissionalDentroAgenda != null) {

                horasUteisProfissionalAgenda = HorasUteisProfissionalSingleton.getInstance().getBo().listByProfissional(profissionalDentroAgenda);
                this.ordenaPorDiaDaSemanaAgenda();
            }
        } catch (Exception e) {
            this.addError(OdontoMensagens.getMensagem("horasuteisprofissional.erro.carregar.hora"), "");
        }
        return horasUteisProfissionalAgenda;
    }

    public void ordenaPorDiaDaSemana() {
        for (HorasUteisProfissional hup : horasUteisProfissional) {
            hup.setDiaDaSemanaInt(HorasUteisProfissionalSingleton.getInstance().getBo().getDiaDaSemana(hup.getDiaDaSemana()));
        }
        Collections.sort(horasUteisProfissional, new Comparator<HorasUteisProfissional>() {

            @Override
            public int compare(HorasUteisProfissional o1, HorasUteisProfissional o2) {
                return o1.getDiaDaSemanaInt() > o2.getDiaDaSemanaInt() ? 1 : -1;
            }
        });
    }

    public void ordenaPorDiaDaSemanaAgenda() {
        for (HorasUteisProfissional hup : horasUteisProfissionalAgenda) {
            hup.setDiaDaSemanaInt(HorasUteisProfissionalSingleton.getInstance().getBo().getDiaDaSemana(hup.getDiaDaSemana()));
        }
        Collections.sort(horasUteisProfissionalAgenda, new Comparator<HorasUteisProfissional>() {

            @Override
            public int compare(HorasUteisProfissional o1, HorasUteisProfissional o2) {
                return o1.getDiaDaSemanaInt() > o2.getDiaDaSemanaInt() ? 1 : -1;
            }
        });
    }

    public void setHorasUteisProfissional(List<HorasUteisProfissional> horasUteisProfissional) {
        this.horasUteisProfissional = horasUteisProfissional;
    }

    public void setHorasUteisProfissionalAgenda(List<HorasUteisProfissional> horasUteisProfissional) {
        this.horasUteisProfissional = horasUteisProfissional;
    }

    public boolean isMostraFinalizados() {
        return mostraFinalizados;
    }

    public void setMostraFinalizados(boolean mostraFinalizados) {
        this.mostraFinalizados = mostraFinalizados;
    }

    public Profissional getProfissionalDentroAgenda() {
        return profissionalDentroAgenda;
    }

    public void setProfissionalDentroAgenda(Profissional profissionalDentroAgenda) {
        this.profissionalDentroAgenda = profissionalDentroAgenda;
    }

    public List<Integer> getCadeiras() {
        return cadeiras;
    }

    public void setCadeiras(List<Integer> cadeiras) {
        this.cadeiras = cadeiras;
    }

    public boolean isHabilitaSalvar() {

        return habilitaSalvar;
    }

    public void setHabilitaSalvar(boolean habilitaSalvar) {
        this.habilitaSalvar = habilitaSalvar;
    }

    public List<String> getFiltroAgendamento() {
        return filtroAgendamento;
    }

    public void setFiltroAgendamento(List<String> filtroAgendamento) {
        this.filtroAgendamento = filtroAgendamento;
    }

    public boolean isVisivelDadosPaciente() {
        return visivelDadosPaciente;
    }

    public void setVisivelDadosPaciente(boolean visivelDadosPaciente) {
        this.visivelDadosPaciente = visivelDadosPaciente;
    }

    public LocalDate getInitialDate() {
        return initialDate;
    }

    public void setInitialDate(LocalDate date) {
        this.initialDate = date;
        this.initialDateConsult = Utils.convertToDateViaInstant(date);
    }

    public String getAtendimentosChart() {
        StringBuilder sb = new StringBuilder();
        Calendar hojeCedo = Calendar.getInstance();
        hojeCedo.setTime(Utils.convertToDateViaInstant(initialDate));
        hojeCedo.set(Calendar.HOUR_OF_DAY, 0);
        hojeCedo.set(Calendar.MINUTE, 0);
        hojeCedo.set(Calendar.SECOND, 0);
        Calendar hojeTarde = Calendar.getInstance();
        hojeTarde.setTime(Utils.convertToDateViaInstant(initialDate));
        hojeTarde.set(Calendar.HOUR_OF_DAY, 23);
        hojeTarde.set(Calendar.MINUTE, 59);
        hojeTarde.set(Calendar.SECOND, 59);
        List<Agendamento> agendamentosHoje = AgendamentoSingleton.getInstance().getBo().listByDataTodosProfissionais(hojeCedo.getTime(), hojeTarde.getTime(),
                UtilsFrontEnd.getEmpresaLogada().getEmpIntCod());
        List<String> classesCss = new ArrayList<>();
        HashSet<String> profissionaisAgendamentoHoje = new HashSet<>();
        removerFiltrosAgendamento(agendamentosHoje);
        if (agendamentosHoje != null && !agendamentosHoje.isEmpty()) {
            for (Agendamento agendamento : agendamentosHoje) {
                String classCss = StatusAgendamentoUtil.findBySigla(agendamento.getStatusNovo()).getStyleCss();
                String siglaStatus = StatusAgendamentoUtil.findBySigla(agendamento.getStatusNovo()).getSigla();
                if (!classesCss.contains(siglaStatus + ": " + classCss))
                    classesCss.add(siglaStatus + ": '" + classCss + "'");

                Calendar c = Calendar.getInstance();
                c.setTime(agendamento.getInicio());
                String hora1 = c.get(Calendar.HOUR_OF_DAY) + "";
                hora1 += "," + c.get(Calendar.MINUTE);

                c.setTime(agendamento.getFim());
                String hora2 = c.get(Calendar.HOUR_OF_DAY) + "";
                hora2 += "," + c.get(Calendar.MINUTE);

                String telefone = agendamento.getPaciente().getDadosBasico().getTelefoneStr();
                telefone = (telefone == null || telefone.trim().isEmpty() ? agendamento.getPaciente().getDadosBasico().getCelular() : telefone);
                sb.append(
                        "[ '" + agendamento.getProfissional().getDadosBasico().getNome() + "', '" + siglaStatus + "', '" + agendamento.getPaciente().getDadosBasico().getNome() + "', new Date(0,0,0," + hora1 + ",0), new Date(0,0,0," + hora2 + ",0), '" + (agendamento.getCadeira() != null ? "Cadeira " + agendamento.getCadeira() : "Sem Cadeira") + "', '" + telefone + "' ],");
                profissionaisAgendamentoHoje.add(agendamento.getProfissional().getDadosBasico().getNome());
            }
            String jsonFull = "{ classes: {";
            for (String regraCategoria : classesCss)
                jsonFull += regraCategoria + ", ";
            jsonFull += "}, dados: [" + sb.toString() + "]";
            jsonFull += ", length: " + (profissionaisAgendamentoHoje != null ? profissionaisAgendamentoHoje.toArray().length : 0);
            jsonFull += "}";
            return jsonFull;
        }
        return "[]";
    }

    public void loadChartProfissionais() {
        PrimeFaces.current().executeScript("google.charts.setOnLoadCallback(drawChartProfissionais(" + getAtendimentosChart() + "));");
    }

    public void loadChartCadeiras() {
        PrimeFaces.current().executeScript("google.charts.setOnLoadCallback(drawChartCadeiras(" + getAtendimentosChart() + "));");
    }

    public void actionCancelConflitoPaciente() {
        cancelarNovoPaciente(null);
        paciente = new Paciente();
    }

    public void actionPersistConflitoPaciente() {
        actionPersistPaciente(null);
        PrimeFaces.current().executeScript("PF('dlgPacienteConflito').hide()");
    }

    public void actionPersistPacienteVerificaConflito() {
        try {
            List<Paciente> pacienteExistente = PacienteSingleton.getInstance().getBo().findByNomeList(paciente.getDadosBasico().getNome(), UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
            if (pacienteExistente != null && !pacienteExistente.isEmpty()) {
                this.pacientesConflito = pacienteExistente;
                this.pacienteConflito = null;
                PrimeFaces.current().executeScript("PF('dlgPacienteConflito').show()");
            } else {
                actionPersistPaciente(null);
            }
        } catch (Exception e) {
            LogIntelidenteSingleton.getInstance().makeLog(e);
            addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
        }
    }

    public void verificarDisponibilidadeProfissional() {
        try {
            if (this.profissionalDentroAgenda != null) {

                if (this.dataAgendamentoInicial != null) {

                    Profissional profissionalAgenda = this.profissionalDentroAgenda;
                    int tempoConsulta = profissionalAgenda.getTempoConsulta();

                    Calendar c = Calendar.getInstance();

                    c.setTime(dataAgendamentoInicial);
                    c.add(Calendar.DAY_OF_MONTH, -1);
                    Date data1 = c.getTime();

                    c.add(Calendar.DAY_OF_MONTH, +2);
                    Date data2 = c.getTime();

                    SimpleDateFormat formatarData = new SimpleDateFormat("HH:mm");

                    List<Agendamento> horariosDisponiveis = new ArrayList<>();
                    List<Agendamento> agendamentosProfissional = AgendamentoSingleton.getInstance().getBo().listByDataAndProfissional(profissionalAgenda, data1, data2);
                    List<HorasUteisProfissional> horasUteis = HorasUteisProfissionalSingleton.getInstance().getBo().listByProfissional(profissionalAgenda);

                    Date dataExpedienteInicial = new Date();
                    Date dataExpedienteFinal = new Date();

                    for (Agendamento agendamento : agendamentosProfissional) {

                        if (horasUteis != null) {
                            for (HorasUteisProfissional expediente : horasUteis) {

                                c.setTime(agendamento.getInicio());

                                if (true) {

                                    Agendamento agendamentoDisp = new Agendamento();
                                    agendamentoDisp.setId(0);

                                    if ((expediente.getHoraIni().getTime() + (tempoConsulta * 60000)) < (agendamento.getInicio().getTime())) {

                                        agendamentoDisp.setInicio(new Date(expediente.getHoraIni().getTime()));
                                        agendamentoDisp.setFim(new Date(expediente.getHoraIni().getTime() + (tempoConsulta * 60000)));

                                        horariosDisponiveis.add(agendamentoDisp);

                                    }

                                } else {

                                }

                            }
                        } else {

                        }
                    }

                    agendamentosDisponiveis = horariosDisponiveis;

                    PrimeFaces.current().ajax().update(":lume:dtProfissionalDisponivel");
                }

            } else {

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void verificarProfissional() {
        PrimeFaces.current().executeScript("PF('dlgNovoAgendamento').show();");
        PrimeFaces.current().executeScript("PF('dtProfissionalDisponivel').process();");
        PrimeFaces.current().ajax().update(":lume:dtProfissionalDisponivel");
    }

    public String formatarData(Date data) {
        if (data != null) {
            return new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", new Locale("PT BR")).format(data);
        }
        return "";
    }

    public String teste() {
        return "teste";
    }

    public String getVisualizacao() {
        return visualizacao;
    }

    public void setVisualizacao(String visualizacao) {
        this.visualizacao = visualizacao;
    }

    public boolean isTelefonesVisiveis() {
        return telefonesVisiveis;
    }

    public void setTelefonesVisiveis(boolean telefonesVisiveis) {
        this.telefonesVisiveis = telefonesVisiveis;
    }

    public Integer getCadeiraDentroAgenda() {
        return cadeiraDentroAgenda;
    }

    public void setCadeiraDentroAgenda(Integer cadeiraDentroAgenda) {
        this.cadeiraDentroAgenda = cadeiraDentroAgenda;
    }

    public void setEntity(Agendamento agendamento) {
        super.setEntity(agendamento);
    }

    public List<Paciente> getPacientesConflito() {
        return pacientesConflito;
    }

    public void setPacientesConflito(List<Paciente> pacientesConflito) {
        this.pacientesConflito = pacientesConflito;
    }

    public Paciente getPacienteConflito() {
        return pacienteConflito;
    }

    public void setPacienteConflito(Paciente pacienteConflito) {
        this.pacienteConflito = pacienteConflito;
    }

    public boolean isCheckFiltro() {
        return checkFiltro;
    }

    public void setCheckFiltro(boolean checkFiltro) {
        this.checkFiltro = checkFiltro;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    public List<Convenio> getConvenios() {
        return convenios;
    }

    public void setConvenios(List<Convenio> convenios) {
        this.convenios = convenios;
    }

    public ScheduleModel getScheduleProfissional() {
        return scheduleProfissional;
    }

    public void setScheduleProfissional(ScheduleModel scheduleProfissional) {
        this.scheduleProfissional = scheduleProfissional;
    }

    public List<Profissional> getProfissionaisDisponiveis() {
        return profissionaisDisponiveis;
    }

    public void setProfissionaisDisponiveis(List<Profissional> profissionaisDisponiveis) {
        this.profissionaisDisponiveis = profissionaisDisponiveis;
    }

    public Profissional getProfissionalDisponivel() {
        return profissionalDisponivel;
    }

    public void setProfissionalDisponivel(Profissional profissionalDisponivel) {
        this.profissionalDisponivel = profissionalDisponivel;
    }

    public List<Agendamento> getAgendamentosDisponiveis() {
        return agendamentosDisponiveis;
    }

    public void setAgendamentosDisponiveis(List<Agendamento> agendamentosDisponiveis) {
        this.agendamentosDisponiveis = agendamentosDisponiveis;
    }

    public Date getDataAgendamentoInicial() {
        return dataAgendamentoInicial;
    }

    public void setDataAgendamentoInicial(Date dataAgendamentoInicial) {
        this.dataAgendamentoInicial = dataAgendamentoInicial;
    }

    public Date getDataAgendamentoFinal() {
        return dataAgendamentoFinal;
    }

    public void setDataAgendamentoFinal(Date dataAgendamentoFinal) {
        this.dataAgendamentoFinal = dataAgendamentoFinal;
    }

    public Date getChegouAsEstadoInicial() {
        return chegouAsEstadoInicial;
    }

    public void setChegouAsEstadoInicial(Date chegouAsEstadoInicial) {
        this.chegouAsEstadoInicial = chegouAsEstadoInicial;
    }

    public Date getIniciouAsEstadoInicial() {
        return iniciouAsEstadoInicial;
    }

    public void setIniciouAsEstadoInicial(Date iniciouAsEstadoInicial) {
        this.iniciouAsEstadoInicial = iniciouAsEstadoInicial;
    }

    public Date getFinalizaouAsEstadoInicial() {
        return finalizaouAsEstadoInicial;
    }

    public void setFinalizaouAsEstadoInicial(Date finalizaouAsEstadoInicial) {
        this.finalizaouAsEstadoInicial = finalizaouAsEstadoInicial;
    }

    public PushContext getSomeChannel() {
        return someChannel;
    }

    public void setSomeChannel(PushContext someChannel) {
        this.someChannel = someChannel;
    }

    public Date getChegouAsDentroAgenda() {
        return chegouAsDentroAgenda;
    }

    public void setChegouAsDentroAgenda(Date chegouAsDentroAgenda) {
        this.chegouAsDentroAgenda = chegouAsDentroAgenda;
    }

    public Date getIniciouAsDentroAgenda() {
        return iniciouAsDentroAgenda;
    }

    public void setIniciouAsDentroAgenda(Date iniciouAsDentroAgenda) {
        this.iniciouAsDentroAgenda = iniciouAsDentroAgenda;
    }

    public Date getFinalizouAsDentroAgenda() {
        return finalizouAsDentroAgenda;
    }

    public void setFinalizouAsDentroAgenda(Date finalizouAsDentroAgenda) {
        this.finalizouAsDentroAgenda = finalizouAsDentroAgenda;
    }

    public PushContext getCanalAgendamentoRapido() {
        return canalAgendamentoRapido;
    }

    public void setCanalAgendamentoRapido(PushContext canalAgendamentoRapido) {
        this.canalAgendamentoRapido = canalAgendamentoRapido;
    }

    public String getMensagemWhats() {
        return mensagemWhats;
    }

    public void setMensagemWhats(String mensagemWhats) {
        this.mensagemWhats = mensagemWhats;
    }

    public String getIdEmpresaParaSocket() {
        return idEmpresaParaSocket;
    }

    public void setIdEmpresaParaSocket(String idEmpresaParaSocket) {
        this.idEmpresaParaSocket = idEmpresaParaSocket;
    }

    public Retorno getRetorno() {
        return retorno;
    }

    public void setRetorno(Retorno retorno) {
        this.retorno = retorno;
    }

    public Date getInitialDateConsult() {
        return initialDateConsult;
    }

    public void setInitialDateConsult(Date initialDateConsult) {
        this.initialDateConsult = initialDateConsult;
        setInitialDate(Utils.convertToLocalDateViaInstant(initialDateConsult));
    }

    public boolean isAgendamentoRapido() {
        return agendamentoRapido;
    }

    public void setAgendamentoRapido(boolean agendamentoRapido) {
        this.agendamentoRapido = agendamentoRapido;
    }

//    public PushContext getSomeChannel() {
//        return someChannel;
//    }
//
//    
//    public void setSomeChannel(PushContext someChannel) {
//        this.someChannel = someChannel;
//    }
}
