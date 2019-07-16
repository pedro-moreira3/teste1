package br.com.lume.odonto.managed;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ActionEvent;

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
import br.com.lume.agendamento.bo.AgendamentoBO;
import br.com.lume.agendamentoPlanoTratamentoProcedimento.AgendamentoPlanoTratamentoProcedimentoSingleton;
import br.com.lume.common.OdontoPerfil;
import br.com.lume.common.exception.business.BusinessException;
import br.com.lume.common.exception.business.ServidorEmailDesligadoException;
import br.com.lume.common.exception.business.UsuarioDuplicadoException;
import br.com.lume.common.exception.techinical.TechnicalException;
import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.GeradorSenha;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.Status;
import br.com.lume.common.util.StatusAgendamentoUtil;
import br.com.lume.common.util.Utils;
import br.com.lume.common.util.UtilsFrontEnd;

import br.com.lume.dadosBasico.DadosBasicoSingleton;
import br.com.lume.dominio.DominioSingleton;
import br.com.lume.horasUteisProfissional.HorasUteisProfissionalSingleton;
//import br.com.lume.odonto.bo.AgendamentoBO;
//import br.com.lume.odonto.bo.AgendamentoPlanoTratamentoProcedimentoBO;
//import br.com.lume.odonto.bo.DadosBasicoBO;
//import br.com.lume.odonto.bo.DominioBO;
//import br.com.lume.odonto.bo.HorasUteisProfissionalBO;
//import br.com.lume.odonto.bo.PacienteBO;
//import br.com.lume.odonto.bo.PlanoTratamentoBO;
//import br.com.lume.odonto.bo.PlanoTratamentoProcedimentoBO;
//import br.com.lume.odonto.bo.ProcedimentoBO;
//import br.com.lume.odonto.bo.ProfissionalBO;
//import br.com.lume.odonto.bo.ReservaBO;
//import br.com.lume.odonto.bo.RetornoBO;
import br.com.lume.odonto.entity.Afastamento;
import br.com.lume.odonto.entity.Agendamento;
import br.com.lume.odonto.entity.AgendamentoPlanoTratamentoProcedimento;
import br.com.lume.odonto.entity.Dominio;
import br.com.lume.odonto.entity.HorasUteisProfissional;
import br.com.lume.odonto.entity.Paciente;
import br.com.lume.odonto.entity.PlanoTratamento;
import br.com.lume.odonto.entity.PlanoTratamentoProcedimento;
import br.com.lume.odonto.entity.Profissional;
import br.com.lume.odonto.entity.Reserva;
import br.com.lume.odonto.entity.Retorno;
import br.com.lume.odonto.entity.StatusAgendamento;
import br.com.lume.odonto.exception.TelefoneException;
import br.com.lume.odonto.util.OdontoMensagens;
import br.com.lume.paciente.PacienteSingleton;
import br.com.lume.planoTratamento.PlanoTratamentoSingleton;
import br.com.lume.planoTratamentoProcedimento.PlanoTratamentoProcedimentoSingleton;
import br.com.lume.profissional.ProfissionalSingleton;
import br.com.lume.reserva.ReservaSingleton;
import br.com.lume.retorno.RetornoSingleton;
import br.com.lume.security.PerfilSingleton;
import br.com.lume.security.UsuarioSingleton;
//import br.com.lume.security.bo.PerfilBO;
//import br.com.lume.security.bo.UsuarioBO;
import br.com.lume.security.entity.Perfil;
import br.com.lume.security.entity.Usuario;
import br.com.lume.security.validator.GenericValidator;

@ManagedBean
@ViewScoped
public class AgendamentoMB extends LumeManagedBean<Agendamento> {

    private static final long serialVersionUID = -4445481223043257997L;

    private Logger log = Logger.getLogger(AgendamentoMB.class);

    private ScheduleModel schedule;

    private Profissional profissional, profissionalDentroAgenda;

    private List<Profissional> profissionais;

    private List<Paciente> pacientes;

    private Integer tempoConsulta;

    private List<Agendamento> agendamentos, agendamentosAfastamento = new ArrayList<>();

    private Date inicio, fim;

    private Paciente pacienteSelecionado, pacientePesquisado, paciente = new Paciente();

    private PlanoTratamento planoTratamentoSelecionado;

    private List<PlanoTratamento> planoTratamentos;

    private boolean visivel = false, horaUtilValida, dlg, responsavel = false, mostraFinalizados = false,telefonesVisiveis = false;

    private DualListModel<AgendamentoPlanoTratamentoProcedimento> procedimentosPickList = new DualListModel<>();

    private Dominio justificativa;

    private String status;

    private int qtdProfissionais;

    private String gmt;

    public List<HorasUteisProfissional> horasUteisProfissional;

  //  private DominioBO dominioBO;

  //  private PacienteBO pacienteBO;

  //  private RetornoBO retornoBO = new RetornoBO();

 //   private AgendamentoBO agendamentoBO;

   // private HorasUteisProfissionalBO horasUteisProfissionalBO;

  //  private AgendamentoPlanoTratamentoProcedimentoBO agendamentoPlanoTratamentoProcedimentoBO;

 //   private AfastamentoBO afastamentoBO;

 //   private ProcedimentoBO procedimentoBO;

 //   private PlanoTratamentoBO planoTratamentoBO;

//    private PlanoTratamentoProcedimentoBO planoTratamentoProcedimentoBO;

 //   private DadosBasicoBO dadosBasicoBO;

    //private UsuarioBO usuarioBO;

    //private PerfilBO perfilBO;

 //   private ProfissionalBO profissionalBO;

  //  private ReservaBO reservaBO;

    private List<Integer> cadeiras;

    private boolean habilitaSalvar = true;

    private List<String> filtroAgendamento = new ArrayList<>();

    private boolean visivelDadosPaciente = true;

    private Retorno retorno;

    private Date initialDate;
    
    //S - Scheduler, C - Cadeiras, P - Profissional
    private String visualizacao = "S";

    public AgendamentoMB() {
        super(AgendamentoSingleton.getInstance().getBo());
      
      //  usuarioBO = new UsuarioBO();
     //   perfilBO = new PerfilBO();
      
        this.setClazz(Agendamento.class);
        try {
            if (UtilsFrontEnd.getProfissionalLogado().getPerfil().equals(OdontoPerfil.DENTISTA) && UtilsFrontEnd.getEmpresaLogada().isEmpBolDentistaAdmin() == false) {
                visivelDadosPaciente = false;
            }
            gmt = DominioSingleton.getInstance().getBo().findByEmpresaAndObjetoAndTipoAndValor("agendamento", "hora", "GM").getNome();
            this.carregarProfissionais();
            qtdProfissionais = profissionais.size();
            pacientes = PacienteSingleton.getInstance().getBo().listByEmpresa(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
            tempoConsulta = UtilsFrontEnd.getProfissionalLogado().getTempoConsulta();
            carregarCadeiras();
            filtroAgendamento.addAll(Arrays.asList("F", "A", "I", "S", "O", "E", "B", "N", "P", "G", "H"));
            initialDate = Calendar.getInstance().getTime();
        } catch (Exception e) {
            log.error(e);
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

    public void retorno(Retorno r) {
        if (r != null) {
            pacienteSelecionado = r.getPaciente();
            retorno = r;
            profissional = null;
        }
    }

    public void filaAtendimento(Agendamento a) {
        if (a != null) {
            profissional = a.getProfissional();
            profissionalDentroAgenda = profissional;
            pacienteSelecionado = a.getPaciente();
        } else {
            profissional = null;
            pacienteSelecionado = null;
        }
    }
    
    public boolean isReserva() {
        try {
            List<Reserva> reservas = ReservaSingleton.getInstance().getBo().listByAgendamento(getEntity(),UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
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
        profissionais = ProfissionalSingleton.getInstance().getBo().listByEmpresa(perfis,UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
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
        return PacienteSingleton.getInstance().getBo().listSugestoesComplete(query,UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
    }

    public void handleClose() {
        handleClose(null);
    }

    public void handleClose(CloseEvent event) {
        this.actionNew(null);
       // profissional = null;
        profissionalDentroAgenda = null;
    }

    @Override
    public void actionNew(ActionEvent arg0) {
        inicio = null;
        fim = null;
        this.setEntity(new Agendamento());
        this.setPacienteSelecionado(null);
        this.setPlanoTratamentos(null);
        this.setPlanoTratamentoSelecionado(null);
        this.setProcedimentosPickList(null);
    }

    @Override
    public void actionPersist(ActionEvent event) {

     //   if (profissionalDentroAgenda != null) {
        //    profissional = profissionalDentroAgenda;
      //  }
        if ((procedimentosPickList.getSource().isEmpty() && procedimentosPickList.getTarget().isEmpty() && planoTratamentoSelecionado == null) || (!procedimentosPickList.getTarget().isEmpty() && planoTratamentoSelecionado != null)) {
            this.getEntity().setPlanoTratamentoProcedimentosAgendamento(this.insereAgendamento(procedimentosPickList.getTarget()));
            this.getEntity().setPlanoTratamento(planoTratamentoSelecionado);
            if (this.getEntity().getStatusNovo().equals("")) {
                this.getEntity().setStatusNovo(status);
            }
            if (this.validacoes()) {
                if (this.getEntity().getId() == 0) {
                    this.getEntity().setProfissional(profissionalDentroAgenda);
                    this.getEntity().setDataAgendamento(new Date());
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
                    dlg = false;
                } else {
                    if (!this.isRemarcado()) {
                        this.getEntity().setHash(GeradorSenha.gerarSenha());
                        this.getEntity().setInicio(this.getInicio());
                        this.getEntity().setFim(this.getFim());
                        if (this.getEntity().getStatusNovo().equals(StatusAgendamentoUtil.ENCAIXE.getSigla())) {
                            this.getEntity().setEncaixe(Status.SIM);
                        }
                        try {
                            if (profissionalDentroAgenda.getId() != getEntity().getProfissional().getIdUsuario()) {
                                getEntity().setProfissional(profissionalDentroAgenda);
                                List<Reserva> reservas = ReservaSingleton.getInstance().getBo().listByAgendamento(getEntity(),UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
                                if (reservas != null && !reservas.isEmpty()) {
                                    for (Reserva reserva : reservas) {
                                        reserva.setProfissional(profissionalDentroAgenda);
                                    }
                                    ReservaSingleton.getInstance().getBo().mergeBatch(reservas);
                                }
                            }
                            AgendamentoSingleton.getInstance().getBo().persist(this.getEntity(),UtilsFrontEnd.getProfissionalLogado(),UtilsFrontEnd.getEmpresaLogada().getEmpStrEstoque(),UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());                    
                            if (retorno != null) {
                                retorno.setAgendamento(getEntity());
                                retorno.setRetornar("A");
                                RetornoSingleton.getInstance().getBo().persist(retorno);
                            }
                            profissionalDentroAgenda = null;
                            this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");
                            this.actionNew(event);
                            PrimeFaces.current().ajax().addCallbackParam("dlg", dlg);
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
                        try {
                            AgendamentoSingleton.getInstance().getBo().persist(this.getEntity());
                            this.actionNew(event);
                            this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");
                        } catch (Exception e) {
                            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
                            log.error("actionPersist", e);
                        }
                    }
                }
            } else {
                this.refreshEntity();
            }
            validaHabilitaSalvar();
            carregarScheduleTarefas();
        } else {
            this.addError(OdontoMensagens.getMensagem("erro.agendamento.planotratamento.vazio"), "");
        }
    }

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
                agendamento.setAgendador(this.getEntity().getAgendador());
                agendamento.setFim(this.getFim());
                agendamento.setInicio(this.getInicio());
                agendamento.setDataAgendamento(this.getEntity().getDataAgendamento());
                agendamento.setPaciente(this.getEntity().getPaciente());
                agendamento.setProfissional(this.getEntity().getProfissional());
                //agendamento.setPlanoTratamentoProcedimentosAgendamento(this.getEntity().getPlanoTratamentoProcedimentosAgendamento());
                agendamento.setStatusNovo("N");
                agendamento.setFilial(this.getEntity().getFilial());
                agendamento.setHash(this.getEntity().getHash());
                agendamento.setDescricao(this.getEntity().getDescricao());
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
                AgendamentoSingleton.getInstance().getBo().persist(agendamento);
                ReservaSingleton.getInstance().getBo().cancelaReservas(getEntity(),UtilsFrontEnd.getProfissionalLogado());
                return true;
            } else if (this.getEntity().getStatusNovo().equals(StatusAgendamentoUtil.CANCELADO.getSigla()) || this.getEntity().getStatusNovo().equals(StatusAgendamentoUtil.FALTA.getSigla())) {
                ReservaSingleton.getInstance().getBo().cancelaReservas(getEntity(),UtilsFrontEnd.getProfissionalLogado());                
            }
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
        }
    }

    private boolean validacoes() {
        if (GenericValidator.validarRangeData(this.getEntity().getChegouAs(), this.getEntity().getFinalizouAs(), false)) {
            if (this.getProfissionalDentroAgenda() != null || this.getEntity().getId() != 0) {
                if (this.getEntity().getPaciente() != null) {
                    if (GenericValidator.validarRangeData(this.getInicio(), this.getFim(), true)) {
                      //  if (this.validaHoraduplicadaProfissional(this.getEntity())) {
                            if (this.validaHoraduplicadaPaciente(this.getEntity().getPaciente())) {
                                if (this.validaIntervalo()) {
                                    dlg = true;
                                    
                                    if(this.getEntity().getStatusNovo().matches("S|N|I|O|A")) {
                                         if(this.getEntity().getChegouAs() != null && this.getEntity().getIniciouAs() != null && this.getEntity().getFinalizouAs() != null) {
                                            this.getEntity().setStatusNovo("A");
                                            this.addInfo(OdontoMensagens.getMensagem("agendamento.status.alterado.atendido"), "");
                                        }else if(this.getEntity().getChegouAs() != null && this.getEntity().getIniciouAs() == null && this.getEntity().getFinalizouAs() == null) {
                                            this.getEntity().setStatusNovo("I");
                                            this.addInfo(OdontoMensagens.getMensagem("agendamento.status.alterado.clientenaclinica"), "");
                                        }else if(this.getEntity().getChegouAs() != null && this.getEntity().getIniciouAs() != null && this.getEntity().getFinalizouAs() == null) {
                                            this.getEntity().setStatusNovo("O");
                                            this.addInfo(OdontoMensagens.getMensagem("agendamento.status.alterado.ematendimento"), "");
                                        }
                                    }
                                    
//                                    if (this.getEntity().getIniciouAs() == null && this.getEntity().getChegouAs() != null && (!this.getEntity().getStatusNovo().equals(
//                                            StatusAgendamentoUtil.CANCELADO.getSigla()) || !this.getEntity().getStatusNovo().equals(
//                                                    StatusAgendamentoUtil.REMARCADO.getSigla()) || !this.getEntity().getStatusNovo().equals(StatusAgendamentoUtil.ATENDIDO.getSigla()))) {
//                                        if (!this.getEntity().getStatusNovo().equals(StatusAgendamentoUtil.CLIENTE_NA_CLINICA.getSigla())) {
//                                            this.addInfo(OdontoMensagens.getMensagem("agendamento.status.alterado.clientenaclinica"), "");
//                                        }
//                                        this.getEntity().setStatusNovo(StatusAgendamentoUtil.CLIENTE_NA_CLINICA.getSigla());
//                                    }
//                                    if (this.getEntity().getFinalizouAs() == null && this.getEntity().getIniciouAs() != null && (!this.getEntity().getStatusNovo().equals(
//                                            StatusAgendamentoUtil.CANCELADO.getSigla()) || !this.getEntity().getStatusNovo().equals(
//                                                    StatusAgendamentoUtil.REMARCADO.getSigla()) || !this.getEntity().getStatusNovo().equals(StatusAgendamentoUtil.ATENDIDO.getSigla()))) {
//                                        if (!this.getEntity().getStatusNovo().equals(StatusAgendamentoUtil.EM_ATENDIMENTO.getSigla())) {
//                                            this.addInfo(OdontoMensagens.getMensagem("agendamento.status.alterado.ematendimento"), "");
//                                        }
//                                        this.getEntity().setStatusNovo(StatusAgendamentoUtil.EM_ATENDIMENTO.getSigla());
//                                    }
//                                    if (this.getEntity().getFinalizouAs() != null) {
//                                        if (!this.getEntity().getStatusNovo().equals(StatusAgendamentoUtil.ATENDIDO.getSigla())) {
//                                            this.addInfo(OdontoMensagens.getMensagem("agendamento.status.alterado.atendido"), "");
//                                        }
//                                        this.getEntity().setStatusNovo(StatusAgendamentoUtil.ATENDIDO.getSigla());
//                                    }
                                    if (this.validaData()) {
                                        return true;
                                    } else {
                                        this.addError(OdontoMensagens.getMensagem("agendamento.profissional.afastado"), "");
                                        dlg = false;
                                    }
                                } else {
                                    this.addError(OdontoMensagens.getMensagem("agendamento.intervalo.incorreto"), "");
                                    dlg = false;
                                }
                            } else {
                                this.addError(OdontoMensagens.getMensagem("agendamento.paciente.horamarcada"), "");
                                dlg = false;
                            }
//                        } else {
//                            this.addError(OdontoMensagens.getMensagem("agendamento.horario.utilizado"), "");
//                            dlg = false;
//                        }
                    } else {
                        this.addError(OdontoMensagens.getMensagem("agendamento.intervalo.incorreto"), "");
                        dlg = false;
                    }
                } else {
                    this.addError(OdontoMensagens.getMensagem("agendamento.paciente.obrigatorio"), "");
                    dlg = false;
                }
            } else {
                this.addError(OdontoMensagens.getMensagem("agendamento.profissional.obrigatorio"), "");
                dlg = false;
            }
        } else {
            this.addError(OdontoMensagens.getMensagem("agendamento.horario.atendimento"), "");
            dlg = false;
        }
        return false;
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

    public void validaHoraUtilProfissionalCombo() {
        validaHoraUtilProfissional(profissionalDentroAgenda);
        if (horaUtilValida == false) {
            addError("Profissional não está cadastrado para atender nesse horário.", "");
        }
    }

    public void validaHoraUtilProfissional() {
        validaHoraUtilProfissional(profissionalDentroAgenda);
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
                            inicioAtendimentoProfissional.setTime(horasUteisProfissional.getHoraIni());
                            fimAtendimentoProfissional.setTime(horasUteisProfissional.getHoraFim());
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
        if (this.getInicio() != null && profissionalDentroAgenda != null) {
            Calendar c = Calendar.getInstance();
            c.setTime(inicio);
            c.add(Calendar.MINUTE, profissionalDentroAgenda.getTempoConsulta());
            fim = c.getTime();
        }
    }

    public boolean validaHoraduplicadaProfissional(Agendamento agendamento) {
        try {
            if (agendamento == null) {
            return false;
            }
            boolean dataInicioValida = AgendamentoSingleton.getInstance().getBo().verificaDataDisponivelParaProfissional(agendamento,profissionalDentroAgenda,this.getInicio());
            boolean dataFimValida = AgendamentoSingleton.getInstance().getBo().verificaDataDisponivelParaProfissional(agendamento,profissionalDentroAgenda,this.getFim());
             if(dataInicioValida && dataFimValida) {
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
//                    if (agnd.getStatus().equals(StatusAgendamento.CANCELADO.getSigla()) || // ENCAIXE não pode ser feito se for o mesmo
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
        try {
            AgendamentoSingleton.getInstance().getBo().persist(this.getEntity());
            this.actionNew(event);
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
            dlg = true;
            try {
                this.removeAgendamentoPlanoTratamentoProcedimento();
                ReservaSingleton.getInstance().getBo().cancelaReservas(getEntity(),UtilsFrontEnd.getProfissionalLogado());
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

    private void carregarScheduleTarefas() {
        schedule = new LazyScheduleModel() {

            private static final long serialVersionUID = 1L;

            @Override
            public void loadEvents(Date start, Date end) {
                if (AgendamentoMB.this.isDentista()) {
                    profissional = UtilsFrontEnd.getProfissionalLogado();
                }
                
                if (profissional == null && pacientePesquisado == null) {
                    this.clear();
                    agendamentos = AgendamentoSingleton.getInstance().getBo().listByDataTodosProfissionais(start, end,UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
                    removerFiltrosAgendamento(agendamentos);
                    pacientePesquisado = null;
                }else if (profissional != null && pacientePesquisado == null) {
                    tempoConsulta = profissional.getTempoConsulta();
                    this.clear();                 
                    agendamentos = AgendamentoSingleton.getInstance().getBo().listByDataAndProfissional(profissional, start, end);                        
                    removerFiltrosAgendamento(agendamentos);      
                    geraAgendamentoAfastamento(start, end, profissional);
                    agendamentos.addAll(agendamentosAfastamento);
                    pacientePesquisado = null;
                }else if (pacientePesquisado != null && profissional == null) {
                    this.clear();
                    agendamentos = AgendamentoSingleton.getInstance().getBo().listByDataAndPaciente(pacientePesquisado, start, end);
                    removerFiltrosAgendamento(agendamentos);
                }else if (pacientePesquisado != null && profissional != null) {
                    this.clear();
                    agendamentos = AgendamentoSingleton.getInstance().getBo().listByDataAndPacienteAndProfissional(pacientePesquisado,profissional, start, end);
                    removerFiltrosAgendamento(agendamentos);
                }
                
                if (agendamentos != null) {
                    Calendar dataNasc = Calendar.getInstance();
                    dataNasc.add(Calendar.YEAR, -18);
                    Calendar dataAtual = Calendar.getInstance();
                    for (Agendamento agendamento : agendamentos) {
                        String descricao = "";
                        if (agendamento.getStatusNovo().equals(StatusAgendamentoUtil.AFASTAMENTO.getSigla())) {
                            descricao = agendamento.getPaciente().getDadosBasico().getNome();
                            descricao += " - " + agendamento.getDescricao();
                            agendamento.setProfissional(profissional);
                        } else {

                            if(agendamento != null && agendamento.getPaciente() != null && agendamento.getPaciente().getDadosBasico() != null &&
                                    agendamento.getPaciente().getDadosBasico().getDataNascimento() != null)   
                                dataAtual.setTime(agendamento.getPaciente().getDadosBasico().getDataNascimento());
                            if (profissional != null) {
                                descricao = "[" + agendamento.getPaciente().getSiglaConvenio() + "] " + agendamento.getDescricaoAgenda(UtilsFrontEnd.getProfissionalLogado());
                            } else {
                                descricao = agendamento.getProfissional().getDadosBasico().getNomeAbreviado() + " - " + "[" + agendamento.getPaciente().getSiglaConvenio() + "] " + agendamento.getPaciente().getDadosBasico().getNome();
                            }

                        }
                        DefaultScheduleEvent event = new DefaultScheduleEvent(descricao, agendamento.getInicio(), agendamento.getFim(), agendamento);
                        event.setStyleClass(StatusAgendamentoUtil.findBySigla(agendamento.getStatusNovo()).getStyleCss());
                        this.addEvent(event);
                    }
                }
            } 
        };
    }
    
    private void removerFiltrosAgendamento(List<Agendamento> agendamentos) { 
        if(agendamentos != null) {
            List<Agendamento> agentamentoAux = new ArrayList<>(agendamentos);
            for (Agendamento agendamento : agentamentoAux) {
                if(!filtroAgendamento.contains(agendamento.getStatusNovo())) {
                    agendamentos.remove(agendamento);    
                }
            }
        }
        
    }

    private void geraAgendamentoAfastamento(Date start, Date end, Profissional profissional) {
        agendamentosAfastamento = new ArrayList<>();
        try {
            List<Afastamento> afastamentos = null;
            if (profissional != null) {
                afastamentos = AfastamentoSingleton.getInstance().getBo().listByDataAndProfissionalValidos(profissional, start, end);
            } else {
                afastamentos = AfastamentoSingleton.getInstance().getBo().listByDataValidos(start, end);
            }
            if(afastamentos != null) {
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
                   agendamento.setStatusAgendamento(null);
                    agendamento.setDescricao(afastamento.getObservacao());
                    agendamentosAfastamento.add(agendamento);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onDateSelect(SelectEvent selectEvent) {
        Date date = (Date) selectEvent.getObject();
        this.setEntity(new Agendamento());
        this.setPacienteSelecionado(null);
        this.setPlanoTratamentoSelecionado(null);
        this.setInicio(date);
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.MINUTE, tempoConsulta);
        this.setFim(cal.getTime());
        //profissional = null;
        profissionalDentroAgenda = null;
        this.validaHoraUtilProfissional(profissionalDentroAgenda);
        this.validaAfastamento();
        PrimeFaces.current().ajax().addCallbackParam("hora", horaUtilValida);
        validaHabilitaSalvar();
    }

    private void validaHabilitaSalvar() {
        habilitaSalvar = !StatusAgendamentoUtil.CANCELADO.getSigla().equals(getEntity().getStatusNovo()) && !StatusAgendamentoUtil.REMARCADO.getSigla().equals(getEntity().getStatusNovo());
    }

    public boolean validaData() {
        if (agendamentosAfastamento == null || agendamentosAfastamento.isEmpty()) {
            Calendar c = Calendar.getInstance();
            c.setTime(getInicio());
            c.set(Calendar.DAY_OF_MONTH, -1);
            Date start = c.getTime();

            c.setTime(getFim());
            c.set(Calendar.DAY_OF_MONTH, +1);
            Date end = c.getTime();
            geraAgendamentoAfastamento(start, end, profissional);
        }
        for (Agendamento agnd : agendamentosAfastamento) {
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
        Agendamento agendamento = (Agendamento) ((ScheduleEvent) selectEvent.getObject()).getData();
       // profissional = agendamento.getProfissional();
        profissionalDentroAgenda = agendamento.getProfissional();        
    
        UtilsFrontEnd.setPacienteSelecionado(agendamento.getPaciente());
        this.setEntity(agendamento);
        this.setInicio(this.getEntity().getInicio());
        this.setFim(this.getEntity().getFim());
        geraAgendamentoAfastamento(this.getEntity().getInicio(), this.getEntity().getFim(), profissionalDentroAgenda);
        this.setPacienteSelecionado(agendamento.getPaciente());
        this.setJustificativa(DominioSingleton.getInstance().getBo().findByEmpresaAndObjetoAndTipoAndNome("agendamento", "justificativa", this.getEntity().getJustificativa()));
        this.setStatus(this.getEntity().getStatusNovo());
        this.validaAfastamento();
        if (agendamento.getPlanoTratamentoProcedimentosAgendamento() != null && agendamento.getPlanoTratamentoProcedimentosAgendamento().size() > 0) {
            this.setPlanoTratamentoSelecionado(agendamento.getPlanoTratamentoProcedimentosAgendamento().get(0).getPlanoTratamentoProcedimento().getPlanoTratamento());
        }
        this.validaHoraUtilProfissional(profissionalDentroAgenda);
        validaHabilitaSalvar();
    }

    private void validaAfastamento() {
        boolean afastamento = true;        
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

    private void criarUsuario(Usuario usuario, Paciente paciente) throws UsuarioDuplicadoException, ServidorEmailDesligadoException, Exception {
        usuario.setUsuStrNme(paciente.getDadosBasico().getNome());
        usuario.setUsuStrEml(paciente.getDadosBasico().getEmail());
        usuario.setUsuStrLogin(paciente.getDadosBasico().getEmail());
        Perfil perfilbyDescricao = PerfilSingleton.getInstance().getBo().getPerfilbyDescricaoAndSistema(OdontoPerfil.PACIENTE, this.getLumeSecurity().getSistemaAtual());
        usuario.setPerfisUsuarios(Arrays.asList(perfilbyDescricao));
        usuario.setUsuIntDiastrocasenha(999);
        UsuarioSingleton.getInstance().getBo().persistUsuarioExterno(usuario,UtilsFrontEnd.getEmpresaLogada());
    }

    public void actionPersistPaciente(ActionEvent event) {
        Usuario usuario = null;
        try {
            Paciente pacienteExistente = PacienteSingleton.getInstance().getBo().findByNome(paciente.getDadosBasico().getNome(),UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
            if (pacienteExistente != null) {
                pacienteSelecionado = pacienteExistente;
                return;
            }
            if (Utils.validaDataNascimento(paciente.getDadosBasico().getDataNascimento()) == false) {
                addError("Data de nascimento inválida.", "");
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
            PacienteSingleton.getInstance().getBo().persist(paciente);
            PlanoTratamento pt = PlanoTratamentoSingleton.getInstance().getBo().persistPlano(paciente, UtilsFrontEnd.getProfissionalLogado());
            if (pt != null) {
                pacienteSelecionado = paciente;
                planoTratamentos = new ArrayList<>();
                planoTratamentos.add(pt);
                planoTratamentoSelecionado = pt;
            }
            this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");
            visivel = false;
            pacientes = PacienteSingleton.getInstance().getBo().listByEmpresa(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
            pacienteSelecionado = paciente;
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

    public List<Paciente> getPacientes() {
        if (pacientes != null) {
            Collections.sort(pacientes);
        }
        return pacientes;
    }

    public void setPacientes(List<Paciente> pacientes) {
        this.pacientes = pacientes;
    }

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
        planoTratamentos = PlanoTratamentoSingleton.getInstance().getBo().listByPaciente(pacienteSelecionado);
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

    public DualListModel<AgendamentoPlanoTratamentoProcedimento> getProcedimentosPickList() throws Exception {
        List<AgendamentoPlanoTratamentoProcedimento> ptpSelecionadosPreviamente = new ArrayList<>();
        if (procedimentosPickList != null && !procedimentosPickList.getTarget().isEmpty()) {
            ptpSelecionadosPreviamente.addAll(procedimentosPickList.getTarget());
        }
        if (planoTratamentoSelecionado != null) {
            List<AgendamentoPlanoTratamentoProcedimento> planoTratamentoProcedimentos;
            if (mostraFinalizados) {
                planoTratamentoProcedimentos = this.converteParaAgendamentoPlanoTratamentoProcedimento(
                        PlanoTratamentoProcedimentoSingleton.getInstance().getBo().listProcedimentosPickListAgendamento(planoTratamentoSelecionado.getId(), this.getEntity().getId(), false));
            } else {
                planoTratamentoProcedimentos = this.converteParaAgendamentoPlanoTratamentoProcedimento(
                        PlanoTratamentoProcedimentoSingleton.getInstance().getBo().listProcedimentosPickListAgendamento(planoTratamentoSelecionado.getId(), this.getEntity().getId(), true));
            }

            procedimentosPickList = new DualListModel<>(planoTratamentoProcedimentos != null ? planoTratamentoProcedimentos : new ArrayList<AgendamentoPlanoTratamentoProcedimento>(),
                    this.getEntity().getPlanoTratamentoProcedimentosAgendamento() != null ? this.getEntity().getPlanoTratamentoProcedimentosAgendamento() : new ArrayList<AgendamentoPlanoTratamentoProcedimento>());
            return procedimentosPickList;
        } else {
            return new DualListModel<>();
        }
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

    public void setProcedimentosPickList(DualListModel<AgendamentoPlanoTratamentoProcedimento> procedimentosPickList) {
        this.procedimentosPickList = procedimentosPickList;
    }

    public boolean isHoraUtilValida() {
        return horaUtilValida;
    }

    public void setHoraUtilValida(boolean horaUtilValida) {
        this.horaUtilValida = horaUtilValida;
    }

    //TODO - Corrigir time picker que está sempre em '00:00' na versão posterior à 12 apr.
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

    public void setHorasUteisProfissional(List<HorasUteisProfissional> horasUteisProfissional) {
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

    public Date getInitialDate() {
        return initialDate;
    }

    public void setInitialDate(Date initialDate) {
        this.initialDate = initialDate;
    }
    
    public String getAtendimentosChart() {
        StringBuilder sb = new StringBuilder();
        Calendar hojeCedo = Calendar.getInstance();
        hojeCedo.setTime(initialDate);
        hojeCedo.set(Calendar.HOUR_OF_DAY, 0);
        hojeCedo.set(Calendar.MINUTE, 0);
        hojeCedo.set(Calendar.SECOND, 0);
        Calendar hojeTarde = Calendar.getInstance();
        hojeTarde.setTime(initialDate);
        hojeTarde.set(Calendar.HOUR_OF_DAY, 23);
        hojeTarde.set(Calendar.MINUTE, 59);
        hojeTarde.set(Calendar.SECOND, 59);
        List<Agendamento> agendamentosHoje = AgendamentoSingleton.getInstance().getBo().listByDataTodosProfissionais(hojeCedo.getTime(), hojeTarde.getTime(), UtilsFrontEnd.getEmpresaLogada().getEmpIntCod());
        List<String> classesCss = new ArrayList<>();
        HashSet<String> profissionaisAgendamentoHoje = new HashSet<>();
        removerFiltrosAgendamento(agendamentosHoje);
        if (agendamentosHoje != null && !agendamentosHoje.isEmpty()) {
            for (Agendamento agendamento : agendamentosHoje) {
                String classCss = StatusAgendamentoUtil.findBySigla(agendamento.getStatusNovo()).getStyleCss();
                String siglaStatus = StatusAgendamentoUtil.findBySigla(agendamento.getStatusNovo()).getSigla();
                if(!classesCss.contains(siglaStatus + ": " + classCss))
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
                        "[ '" + agendamento.getProfissional().getDadosBasico().getNome() + "', '" + siglaStatus + "', '" + agendamento.getPaciente().getDadosBasico().getNome() + "', new Date(0,0,0," + hora1 + ",0), new Date(0,0,0," + hora2 + ",0), 'Cadeira " + agendamento.getCadeira() +  "', '" + telefone + "' ],");
                profissionaisAgendamentoHoje.add(agendamento.getProfissional().getDadosBasico().getNome());
            }
            String jsonFull = "{ classes: {";
            for(String regraCategoria: classesCss)
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

}
